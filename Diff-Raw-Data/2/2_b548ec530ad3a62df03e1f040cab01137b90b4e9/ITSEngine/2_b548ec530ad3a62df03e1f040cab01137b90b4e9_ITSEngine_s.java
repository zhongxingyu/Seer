 /*===========================================================================
   Copyright (C) 2008-2012 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package org.w3c.its;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import net.sf.okapi.common.Util;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class ITSEngine implements IProcessor, ITraversal {
 
 	public static final String    ITS_VERSION1 = "1.0";
 	public static final String    ITS_VERSION2 = "2.0";
 	
 	public static final String    XML_NS_URI = "http://www.w3.org/XML/1998/namespace";
 	public static final String    XML_NS_PREFIX  = "xml";
 	public static final String    ITS_NS_URI = "http://www.w3.org/2005/11/its";
 	public static final String    ITS_NS_PREFIX = "its";
 	public static final String    ITSX_NS_URI = "http://www.w3.org/2008/12/its-extensions";
 	public static final String    ITSX_NS_PREFIX = "itsx";
 	public static final String    XLINK_NS_URI = "http://www.w3.org/1999/xlink";
 	public static final String    XLINK_NS_PREFIX = "xlink";
 	
 	private static final String   FLAGNAME            = "\u00ff"; // Name of the user-data property that holds the flags
 	private static final String   FLAGSEP             = "\u001c"; // Separator between data categories
 	
 	// Must have '?' as many times as there are FP_XXX entries +1
 	// Must have +FLAGSEP as many times as there are FP_XXX_DATA entries +1
 	private static final String   FLAGDEFAULTDATA     = "????????????"
 		+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP;
 
 	// Indicator position
 	private static final int      FP_TRANSLATE             = 0;
 	private static final int      FP_DIRECTIONALITY        = 1;
 	private static final int      FP_WITHINTEXT            = 2;
 	private static final int      FP_TERMINOLOGY           = 3;
 	private static final int      FP_LOCNOTE               = 4;
 	private static final int      FP_PRESERVEWS            = 5;
 	private static final int      FP_LANGINFO              = 6;
 	private static final int      FP_DOMAIN                = 7;
 	private static final int      FP_EXTERNALRES           = 8;
 	private static final int      FP_LOCFILTER             = 9;
 	private static final int      FP_LQISSUE               = 10;
 	private static final int      FP_STORAGESIZE           = 11;
 	
 	// Data position 
 	private static final int      FP_TERMINOLOGY_DATA      = 0;
 	private static final int      FP_LOCNOTE_DATA          = 1;
 	private static final int      FP_LANGINFO_DATA         = 2;
 	private static final int      FP_TARGETPOINTER_DATA    = 3;
 	private static final int      FP_IDVALUE_DATA          = 4;
 	private static final int      FP_DOMAIN_DATA           = 5;
 	private static final int      FP_EXTERNALRES_DATA      = 6;
 	private static final int      FP_LOCFILTER_DATA        = 7;
 	private static final int      FP_LQISSUE_DATA          = 8;
 	private static final int      FP_STORAGESIZE_DATA      = 9;
 	
 	private static final int      TERMINFOTYPE_POINTER     = 1;
 	private static final int      TERMINFOTYPE_REF         = 2;
 	private static final int      TERMINFOTYPE_REFPOINTER  = 3;
 	
 	private static final int      LOCNOTETYPE_TEXT         = 1;
 	private static final int      LOCNOTETYPE_POINTER      = 2;
 	private static final int      LOCNOTETYPE_REF          = 3;
 	private static final int      LOCNOTETYPE_REFPOINTER   = 4;
 
 	private DocumentBuilderFactory fact; 
 	private Document doc;
 	private URI docURI;
 	private NSContextManager nsContext;
 	private VariableResolver varResolver;
 	private XPathFactory xpFact;
 	private XPath xpath;
 	private ArrayList<ITSRule> rules;
 	private Node node;
 	private boolean startTraversal;
 	private Stack<ITSTrace> trace;
 	private boolean backTracking;
 	private boolean translatableAttributeRuleTriggered;
 	private boolean targetPointerRuleTriggered;
 	private String version;
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	
 	public ITSEngine (Document doc,
 		URI docURI)
 	{
 		this.doc = doc;
 		this.docURI = docURI;
 		node = null;
 		rules = new ArrayList<ITSRule>();
 		nsContext = new NSContextManager();
 		nsContext.addNamespace(ITS_NS_PREFIX, ITS_NS_URI);
 		varResolver = new VariableResolver();
 
 		// Macintosh work-around
 		// When you use -XstartOnFirstThread as a java -Xarg on Leopard, your ContextClassloader gets set to null.
 		// That is not the case on 10.4 or with Windows or Linux flavors
 		// This allows XPathFactory.newInstance() to have a non-null context
 		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
 		// end work-around
 		xpFact = XPathFactory.newInstance();
 
 		xpath = xpFact.newXPath();
 		xpath.setNamespaceContext(nsContext);
 		xpath.setXPathVariableResolver(varResolver);
 	}
 
 	/**
 	 * Indicates if the processed document has triggered a rule for a translatable attribute.
 	 * This must be called only after {@link #applyRules(int)}.
 	 * @return true if the document has triggered a rule for a translatable attribute.
 	 */
 	public boolean getTranslatableAttributeRuleTriggered () {
 		return translatableAttributeRuleTriggered;
 	}
 	
 	/**
 	 * Indicates if the processed document has triggered a target pointer rule.
 	 * This must be called only after {@link #applyRules(int)}. 
 	 * @return true if the processed document has triggered a target pointer rule.
 	 */
 	public boolean getTargetPointerRuleTriggered () {
 		return targetPointerRuleTriggered;
 	}
 	
 	/**
 	 * Gets internal XPath object used in this ITS engine. 
 	 * @return the internal XPath object used in this ITS engine.
 	 */
 	public XPath getXPath () {
 		return xpath;
 	}
 	
 	public void addExternalRules (URI docURI) {
 		try {
 			if ( fact == null ) { 
 				fact = DocumentBuilderFactory.newInstance();
 				fact.setNamespaceAware(true);
 				fact.setValidating(false);
 			}
 			Document rulesDoc = fact.newDocumentBuilder().parse(docURI.toString());
 			addExternalRules(rulesDoc, docURI);
 		}
 		catch ( SAXException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void addExternalRules (Document rulesDoc,
 		URI docURI)
 	{
 		compileRules(rulesDoc, docURI, false);
 	}
 	
 	private void compileRules (Document rulesDoc,
 		URI docURI,
 		boolean isInternal)
 	{
 		try {
 			// Compile the namespaces
 			XPathExpression expr = xpath.compile("//*[@selector]//namespace::*");
 			NodeList nl = (NodeList)expr.evaluate(rulesDoc, XPathConstants.NODESET);
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				String prefix = nl.item(i).getLocalName();
 				if ( "xml".equals(prefix) ) continue; // Set by default
 				String uri = nl.item(i).getNodeValue();
 				nsContext.addNamespace(prefix, uri);
 			}
 			
 			// Compile the rules
 			// First: get the its:rules element(s)
 			expr = xpath.compile("//"+ITS_NS_PREFIX+":rules");
 			nl = (NodeList)expr.evaluate(rulesDoc, XPathConstants.NODESET);
 			if ( nl.getLength() == 0 ) return; // Nothing to do
 			
 			// Process each its:rules element
 			Element rulesElem;
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				rulesElem = (Element)nl.item(i);
 				// Check version
 				version = rulesElem.getAttributeNS(null, "version");
 				if ( !version.equals(ITS_VERSION1) && !version.equals(ITS_VERSION2) ) {
 					throw new ITSException(String.format("Invalid or missing ITS version (\"%s\")", version));
 				}
 
 				// Check for link
 				String href = rulesElem.getAttributeNS(XLINK_NS_URI, "href");
 				if ( href.length() > 0 ) {
 					//String id = null;
 					int n = href.lastIndexOf('#');
 					if ( n > -1 ) {
 						//id = href.substring(n+1);
 						href = href.substring(0, n);
 					}
 
 					// xlink:href allows the use of xml:base so we need to calculate it
 					// The initial base is the folder of the current document
 					String baseFolder = "";
 					if ( docURI != null) baseFolder = getPartBeforeFile(docURI);
 					
 					// Then we look for the last xml:base specified
 					Node node = rulesElem;
 					while ( node != null ) {
 						if ( node.getNodeType() == Node.ELEMENT_NODE ) {
 							//TODO: Relative path with ../../ constructs
 							String xmlBase = ((Element)node).getAttribute("xml:base");
 							if ( xmlBase.length() > 0 ) {
 								if ( xmlBase.endsWith("/") )
 									xmlBase = xmlBase.substring(0, xmlBase.length()-1);
 								if ( !baseFolder.startsWith("/") )
 									baseFolder = xmlBase + "/" + baseFolder;
 								else
 									baseFolder = xmlBase + baseFolder;
 							}
 						}
 						node = node.getParentNode(); // Back-track to parent
 					}
 					if ( baseFolder.length() > 0 ) {
 						if ( baseFolder.endsWith("/") )
 							baseFolder = baseFolder.substring(0, baseFolder.length()-1);
 						if ( !href.startsWith("/") ) href = baseFolder + "/" + href;
 						else href = baseFolder + href;
 					}
 
 					// Load the document and the rules
 					URI linkedDoc = new URI(href);
 					loadLinkedRules(linkedDoc, isInternal);
 				}
 
 				// Process each rule inside its:rules
 				expr = xpath.compile("//"+ITS_NS_PREFIX+":*");
 				NodeList nl2 = (NodeList)expr.evaluate(rulesElem, XPathConstants.NODESET);
 				if ( nl2.getLength() == 0 ) break; // Nothing to do, move to next its:rules
 				
 				Element ruleElem;
 				for ( int j=0; j<nl2.getLength(); j++ ) {
 					ruleElem = (Element)nl2.item(j);
 					String locName = ruleElem.getLocalName();
 					if ( "translateRule".equals(locName) ) {
 						compileTranslateRule(ruleElem, isInternal);
 					}
 					else if ( "withinTextRule".equals(locName) ) {
 						compileWithinTextRule(ruleElem, isInternal);
 					}
 					else if ( "langRule".equals(locName) ) {
 						compileLangRule(ruleElem, isInternal);
 					}
 					else if ( "dirRule".equals(locName) ) {
 						compileDirRule(ruleElem, isInternal);
 					}
 					else if ( "locNoteRule".equals(locName) ) {
 						compileLocNoteRule(ruleElem, isInternal);
 					}
 					else if ( "termRule".equals(locName) ) {
 						compileTermRule(ruleElem, isInternal);
 					}
 					else if ( "idValueRule".equals(locName) ) {
 						compileIdValueRule(ruleElem, isInternal);
 					}
 					else if ( "domainRule".equals(locName) ) {
 						compileDomainRule(ruleElem, isInternal);
 					}
 					else if ( "targetPointerRule".equals(locName) ) {
 						compileTargetPointerRule(ruleElem, isInternal);
 					}
 					else if ( "localeFilterRule".equals(locName) ) {
 						compileLocaleFilterRule(ruleElem, isInternal);
 					}
 					else if ( "preserveSpaceRule".equals(locName) ) {
 						compilePrserveSpaceRule(ruleElem, isInternal);
 					}
 					else if ( "externalResourceRefRule".equals(locName) ) {
 						compileExternalResourceRule(ruleElem, isInternal);
 					}
 					else if ( "locQualityIssueRule".equals(locName) ) {
 						compileLocQualityIssueRule(ruleElem, isInternal);
 					}
 					else if ( "storageSizeRule".equals(locName) ) {
 						compileStorageSizeRule(ruleElem, isInternal);
 					}
 					else if ( "param".equals(locName) ) {
 						processParam(ruleElem);
 					}
 					else if ( !"rules".equals(locName) && !"span".equals(locName) 
 						&& !"locQualityIssues".equals(locName) && !"locQualityIssue".equals(locName) ) {
 						logger.warning(String.format("Unknown element '%s'.", ruleElem.getNodeName()));
 					}
 				}
 			}
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( URISyntaxException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void processParam (Element elem) {
 		String value = elem.getTextContent();
 		String name = elem.getAttribute("name");
 		if ( name.isEmpty() ) {
 			throw new ITSException("Invalid value for 'name' in param.");
  		}
 		varResolver.add(new QName(name), value);
 	}
 	
 	private String getPartBeforeFile (URI uri) {
 		String tmp = uri.toString();
 		int n = tmp.lastIndexOf('/');
 		if ( n == -1 ) return uri.toString();
 		else return tmp.substring(0, n+1);
 	}
 	
 	private void loadLinkedRules (URI docURI,
 		boolean isInternal)
 	{
 		try {
 			if ( fact == null ) { 
 				fact = DocumentBuilderFactory.newInstance();
 				fact.setNamespaceAware(true);
 				fact.setValidating(false);
 			}
 			Document rulesDoc = fact.newDocumentBuilder().parse(docURI.toString());
 			compileRules(rulesDoc, docURI, isInternal);
 		}
 		catch ( SAXException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void compileTranslateRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_TRANSLATE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String value = elem.getAttribute("translate");
 		if ( "yes".equals(value) ) rule.flag = true;
 		else if ( "no".equals(value) ) rule.flag = false;
 		else throw new ITSException("Invalid value for 'translate'.");
 
 		// idValue extension (deprecated but supported)
 		value = elem.getAttributeNS(ITSX_NS_URI, "idValue");
 		if ( !value.isEmpty() ) {
 			if ( version.equals(ITS_VERSION2) ) {
 				// Warn if the extension is used in ITS 2.0
 				logger.warning(String.format("This document uses the %s:idValue extension instead of the ITS 2.0 Id Value data category.",
 					ITSX_NS_URI));
 			}
 			rule.idValue = value;
 		}
 		
 		// whiteSpaces extension (deprecated but supported)
 		value = elem.getAttributeNS(ITSX_NS_URI, "whiteSpaces");
 		if ( !value.isEmpty() ) {
 			if ( version.equals(ITS_VERSION2) ) {
 				// Warn if the extension is used in ITS 2.0
 				logger.warning(String.format("This document uses the %s:whiteSpaces extension instead of the ITS 2.0 Preserve Space data category.",
 					ITSX_NS_URI));
 			}
 			if ( "preserve".equals(value) ) rule.preserveWS = true;
 			else if ( "default".equals(value) ) rule.preserveWS = false;
 			else throw new ITSException("Invalid value for 'whiteSpaces'.");
 		}
 		
 		rules.add(rule);
 	}
 
 	private void compileDirRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_DIRECTIONALITY);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String value = elem.getAttribute("dir");
 		if ( "ltr".equals(value) ) rule.value = DIR_LTR;
 		else if ( "rtl".equals(value) ) rule.value = DIR_RTL;
 		else if ( "lro".equals(value) ) rule.value = DIR_LRO;
 		else if ( "rlo".equals(value) ) rule.value = DIR_RLO;
 		else throw new ITSException("Invalid value for 'dir'.");
 		
 		rules.add(rule);
 	}
 
 	private void compileWithinTextRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_WITHINTEXT);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 			
 		String value = elem.getAttribute("withinText");
 		if ( "yes".equals(value) ) rule.value = WITHINTEXT_YES;
 		else if ( "no".equals(value) ) rule.value = WITHINTEXT_NO;
 		else if ( "nested".equals(value) ) rule.value = WITHINTEXT_NESTED;
 		else throw new ITSException("Invalid value for 'withinText'.");
 			
 		rules.add(rule);
 	}
 
 	private void compileIdValueRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_IDVALUE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 			
 		String value = elem.getAttribute("idValue");
 		if ( value.isEmpty() ) {
 			throw new ITSException("Invalid value for 'idValue'.");
 		}
 		rule.idValue = value;
 		rules.add(rule);
 	}
 
 	private void compileDomainRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_DOMAIN);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 				
 		String pointer = elem.getAttribute("domainPointer");
 		if ( pointer.isEmpty() ) {
 			throw new ITSException("Invalid value for 'domainPointer'.");
 		}
 		rule.info = pointer;
 
 		// Process domainMapping attribute if it's there
 		rule.map = fromStringToMap(elem.getAttribute("domainMapping"));
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileExternalResourceRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_EXTERNALRES);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 				
 		String pointer = elem.getAttribute("externalResourceRefPointer");
 		if ( pointer.isEmpty() ) {
 			throw new ITSException("Invalid value for 'externalResourceRefPointer'.");
 		}
 		rule.info = pointer;
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileStorageSizeRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_STORAGESIZE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String np[] = retrieveStorageSizeData(elem, false);
 		
 		String storageSizeP = null;
 		if ( elem.hasAttribute("storageSizePointer") )
 			storageSizeP = elem.getAttribute("storageSizePointer");
 		
 		String storageSizeEncodingP = null;
 		if (elem.hasAttribute("storageSizeEncodingPointer"))
 			storageSizeEncodingP = elem.getAttribute("storageSizeEncodingPointer");
 		
 		// Check we have the mandatory attributes
 		if ( Util.isEmpty(np[0]) && Util.isEmpty(storageSizeP) ) {
 			throw new ITSException("You must have at least an attribute storageSize or storageSizePointer.");
 		}
 		
 		rule.map = new HashMap<String, String>();
 
 		if ( !Util.isEmpty(np[0]) ) {
 			if ( !Util.isEmpty(storageSizeP) ) {
 				throw new ITSException("Cannot have both storageSize and storageSizePointer.");
 			}
 			rule.map.put("size", np[0]);
 		}
 		else {
 			rule.map.put("sizePointer", storageSizeP);
 		}
 		if ( !Util.isEmpty(np[1]) ) {
 			if ( !Util.isEmpty(storageSizeEncodingP) ) {
 				throw new ITSException("Cannot have both storageSizeEncoding and storageSizeEncodingPointer.");
 			}
 			rule.map.put("encoding", np[1]);
 		}
 		else {
 			rule.map.put("encodingPointer", storageSizeEncodingP);
 		}
 		
 		// Add the rule
 		rules.add(rule);
 	}
 	
 	private void compileLocQualityIssueRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LOCQUALITYISSUE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 
 		// Get the local attributes
 		String np[] = retrieveLocQualityIssueData(elem, false);
 		
 		String issuesRefP = null;
 		if ( elem.hasAttribute("locQualityIssuesRefPointer") )
 			issuesRefP = elem.getAttribute("locQualityIssuesRefPointer");
 		
 		String typeP = null;
 		if (elem.hasAttribute("locQualityIssueTypePointer"))
 			typeP = elem.getAttribute("locQualityIssueTypePointer");
 		
 		String commentP = null;
 		if ( elem.hasAttribute("locQualityIssueCommentPointer"))
 			commentP = elem.getAttribute("locQualityIssueCommentPointer");
 		
 		// Check we have the mandatory attributes
 		if (( Util.isEmpty(np[0]) && Util.isEmpty(issuesRefP) )
 			&& ( Util.isEmpty(np[1]) && Util.isEmpty(typeP) )
 			&& ( Util.isEmpty(np[2]) && Util.isEmpty(commentP) ))
 		{
 			throw new ITSException("You must have at least a type or a comment or isses reference ainformation defined.");
 		}
 		rule.map = new HashMap<String, String>();
 
 		if ( !Util.isEmpty(np[0]) ) {
 			if ( !Util.isEmpty(issuesRefP) ) {
 				throw new ITSException("Cannot have both locQualityIssuesRef and locQualityIssuesRefPointer.");
 			}
 			rule.map.put("issuesRef", np[0]);
 		}
 		else {
 			rule.map.put("issuesRefPointer", issuesRefP);
 		}
 		
 		if ( !Util.isEmpty(np[1]) ) {
 			if ( !Util.isEmpty(typeP) ) {
 				throw new ITSException("Cannot have both locQualityIssueType and locQualityIssueTypePointer.");
 			}
 			rule.map.put("type", np[1]);
 			// TODO: verify the value?
 		}
 		else {
 			rule.map.put("typePointer", typeP);
 		}
 		
 		// Get the comment
 		if ( !Util.isEmpty(np[2]) ) {
 			if ( !Util.isEmpty(commentP) ) {
 				throw new ITSException("Cannot have both locQualityIssueComment and locQualityIssueCommentPointer.");
 			}
 			rule.map.put("comment", np[2]);
 		}
 		else {
 			rule.map.put("commentPointer", commentP);
 		}
 		
 		// Get the optional score
 		String scoreP = null;
 		if ( elem.hasAttribute("locQualityIssueScorePointer") )
 			scoreP = elem.getAttribute("locQualityIssueScorePointer");
 		if ( !Util.isEmpty(np[3]) ) {
 			if ( !Util.isEmpty(scoreP) ) {
 				throw new ITSException("Cannot have both locQualityIssueScore and locQualityIssueScorePointer.");
 			}
 			// TODO: verify the value? // 0. to 100.00
 			rule.map.put("score", np[3]);
 		}
 		else {
 			rule.map.put("scorePointer", scoreP);
 		}
 		
 		// Get the optional profile reference
 		String profileRefP = null;
 		if ( elem.hasAttribute("locQualityIssueProfileRefPointer"))
 			profileRefP = elem.getAttribute("locQualityIssueProfileRefPointer");
 		if ( !Util.isEmpty(np[4]) ) {
 			if ( !Util.isEmpty(profileRefP) ) {
 				throw new ITSException("Cannot have both locQualityIssueProfileRef and locQualityIssueProfileRefPointer.");
 			}
 			// TODO: verify the value? URI
 			rule.map.put("profileRef", np[4]);
 		}
 		else {
 			rule.map.put("profileRefPointer", profileRefP);
 		}
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileLocaleFilterRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LOCFILTER);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		// Retrieve the list
 		rule.info = retrieveLocaleFilterList(elem, false);
 		// Add the rule
 		rules.add(rule);
 	}
 	
 	private void compilePrserveSpaceRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_PRESERVESPACE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		// Get the value
 		String value = elem.getAttribute("space");
 		if (( !"preserve".equals(value) ) && ( !"default".equals(value) )) {
 			throw new ITSException("Invalid value for 'space'.");
 		}
 		rule.preserveWS = "preserve".equals(value);
 		// Add the rule
 		rules.add(rule);
 	}
 		
 
 	private void compileTargetPointerRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_TARGETPOINTER);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 				
 		String pointer = elem.getAttribute("targetPointer");
 		if ( pointer.isEmpty() ) {
 			throw new ITSException("Invalid value for 'targetPointer'.");
 		}
 		rule.info = pointer;
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	/**
 	 * Converts a string like domainMapping to a map.
 	 * @param mapping the string to process.
 	 * @return the map for the given string, or null if there is no values. 
 	 */
 	private Map<String, String> fromStringToMap (String mapping) {
 		if ( mapping.isEmpty() ) return null;
 		
 		Map<String, String> map = null;
 
 		if ( !mapping.isEmpty() ) {
 			// Parse the list of paired values
 			// Split list on commas
 			String[] pairs = mapping.split(",", 0);
 			// Split the pairs
 			char endQuote = 0x00; int state = 0;
 			for ( String pair : pairs ) {
 				pair = pair.trim();
 				StringBuilder left = new StringBuilder();
 				StringBuilder right = new StringBuilder();
 				StringBuilder str = left;
 				for ( int i=0; i<pair.length(); i++ ) {
 					char ch = pair.charAt(i);
 					switch ( ch ) {
 					case '\"':
 						if ( state == 0 )  {
 							endQuote = ch;
 							state = 1;
 						}
 						else {
 							if ( ch == endQuote ) state = 0; // End of string
 							else str.append(ch); // Else: we store
 						}
 						continue;
 					case '\'':
 						if ( state == 0 )  {
 							endQuote = ch;
 							state = 1;
 						}
 						else {
 							if ( ch == endQuote ) state = 0; // End of string
 							else str.append(ch); // Else: we store
 						}
 						continue;
 					case ' ':
 						// If it's a space inside a quoted string: we add to the string
 						// Otherwise it we change to the right side value
 						if ( state == 1 ) str.append(' ');
 						else str = right;
 						continue;
 					default:
 						str.append(pair.charAt(i));
 						break;
 					}
 				}
 				
 				if (( left.length() == 0 ) || ( right.length() == 0 )) {
 					throw new ITSException("Invalid pair in mapping value.");
 				}
 				
 				if ( map == null ) {
 					map = new LinkedHashMap<String, String>();
 				}
 				map.put(left.toString(), right.toString());
 			}
 		}
 		
 		return map;
 	}
 	
 	private void compileTermRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_TERMINOLOGY);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		// term
 		String value = elem.getAttribute("term");
 		if ( "yes".equals(value) ) rule.flag = true;
 		else if ( "no".equals(value) ) rule.flag = false;
 		else throw new ITSException("Invalid value for 'term'.");
 		
 		value = elem.getAttribute("termInfoPointer");
 		String value2 = elem.getAttribute("termInfoRef");
 		String value3 = elem.getAttribute("termInfoRefPointer");
 		
 		if ( value.length() > 0 ) {
 			rule.infoType = TERMINFOTYPE_POINTER;
 			rule.info = value;
 			if (( value2.length() > 0 ) || ( value3.length() > 0 )) {
 				throw new ITSException("Too many termInfo attributes specified");
 			}
 		}
 		else {
 			if ( value2.length() > 0 ) {
 				rule.infoType = TERMINFOTYPE_REF;
 				rule.info = value2;
 				if ( value3.length() > 0 ) {
 					throw new ITSException("Too many termInfo attributes specified");
 				}
 			}
 			else {
 				if ( value3.length() > 0 ) {
 					rule.infoType = TERMINFOTYPE_REFPOINTER;
 					rule.info = value3;
 				}
 				// Else: No associate information, rule.termInfo is null
 			}
 		}
 
 		rules.add(rule);
 	}
 
 	private void compileLocNoteRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LOCNOTE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String type = elem.getAttribute("locNoteType");
 		if ( type.length() == 0 ) {
 			throw new ITSException("locNoteType attribute missing.");
 		}
 		
 		// Try to get the locNote element
 		String value1 = "";
 		NodeList list = elem.getElementsByTagNameNS(ITS_NS_URI, "locNote");
 		if ( list.getLength() > 0 ) {
 			value1 = getTextContent(list.item(0));
 		}
 		// Get the attributes
 		String value2 = elem.getAttribute("locNotePointer");
 		String value3 = elem.getAttribute("locNoteRef");
 		String value4 = elem.getAttribute("locNoteRefPointer");
 		
 		if ( value1.length() > 0 ) {
 			rule.infoType = LOCNOTETYPE_TEXT;
 			rule.info = value1;
 			if (( value2.length() > 0 ) || ( value3.length() > 0 ) || ( value4.length() > 0 )) {
 				throw new ITSException("Too many locNote attributes specified");
 			}
 		}
 		else {
 			if ( value2.length() > 0 ) {
 				rule.infoType = LOCNOTETYPE_POINTER;
 				rule.info = value2;
 				if (( value3.length() > 0 ) || ( value4.length() > 0 )) {
 					throw new ITSException("Too many locNote attributes specified");
 				}
 			}
 			else {
 				if ( value3.length() > 0 ) {
 					rule.infoType = LOCNOTETYPE_REF;
 					rule.info = value3;
 					if ( value4.length() > 0 ) {
 						throw new ITSException("Too many locNote attributes specified");
 					}
 				}
 				else {
 					if ( value4.length() > 0 ) {
 						rule.infoType = LOCNOTETYPE_REFPOINTER;
 						rule.info = value4;
 					}
 				}
 			}
 		}
 
 		rules.add(rule);
 	}
 
 	private void compileLangRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LANGINFO);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		rule.info = elem.getAttribute("langPointer");
 		if ( rule.info.isEmpty() ) {
 			throw new ITSException("langPointer attribute missing.");
 		}
 		rules.add(rule);
 	}
 
 	public void applyRules (int dataCategories) {
 		translatableAttributeRuleTriggered = false;
 		targetPointerRuleTriggered = false;
 		version = "0"; // Needs to be not null (in case there is no ITS at all in file)
 		processGlobalRules(dataCategories);
 		processLocalRules(dataCategories);
 	}
 
 	private void removeFlag (Node node) {
 		//TODO: Any possible optimization, instead of using recursive calls
 		if ( node == null ) return;
 		node.setUserData(FLAGNAME, null, null);
 		if ( node.hasChildNodes() )
 			removeFlag(node.getFirstChild());
 		if ( node.getNextSibling() != null )
 			removeFlag(node.getNextSibling());
 	}
 	
 	public void disapplyRules () {
 		removeFlag(doc.getDocumentElement());
 		translatableAttributeRuleTriggered = false;
 		targetPointerRuleTriggered = false;
 	}
 
 	public boolean backTracking () {
 		return backTracking;
 	}
 
 	public Node nextNode () {
 		if ( startTraversal ) {
 			startTraversal = false;
 			// Set the initial trace with default behaviors
 			ITSTrace startTrace = new ITSTrace();
 			backTracking = false;
 			startTrace.translate = true;
 			startTrace.isChildDone = true;
 			trace.push(startTrace); // For first child
 			node = doc.getFirstChild();
 			trace.push(new ITSTrace(trace.peek(), false));
 			// Overwrite any default behaviors if needed
 			updateTraceData(node);
 			return node;
 		}
 		if ( node != null ) {
 			backTracking = false;
 			if ( !trace.peek().isChildDone && node.hasChildNodes() ) {
 				// Change the flag for the current node
 				ITSTrace tmp = new ITSTrace(trace.peek(), true);
 				trace.pop();
 				trace.push(tmp);
 				// Get the new node and push its flag
 				node = node.getFirstChild();
 				trace.push(new ITSTrace(trace.peek(), false));
 			}
 			else {
 				Node TmpNode = node.getNextSibling();
 				if ( TmpNode == null ) {
 					node = node.getParentNode();
 					trace.pop();
 					backTracking = true;
 				}
 				else {
 					node = TmpNode;
 					trace.pop(); // Remove flag for previous sibling
 					trace.push(new ITSTrace(trace.peek(), false)); // Set new flag for new sibling
 				}
 			}
 		}
 		updateTraceData(node);
 		return node;
 	}
 	
 	/**
 	 * Updates the trace stack.
 	 * @param newNode Node to update 
 	 */
 	private void updateTraceData (Node newNode) {
 		// Check if the node is null
 		if ( newNode == null ) return;
 
 		// Get the flag data
 		String data = (String)newNode.getUserData(FLAGNAME);
 		
 		// If this node has no ITS flags, then we leave the current states
 		// as they are. They have been set by inheritance.
 		if ( data == null ) return;
 		
 		// Otherwise: see if there are any flags to change
 		if ( data.charAt(FP_TRANSLATE) != '?' ) {
 			trace.peek().translate = (data.charAt(FP_TRANSLATE) == 'y');
 		}
 		
 		trace.peek().idValue = getFlagData(data, FP_IDVALUE_DATA);
 		
 		if ( data.charAt(FP_DOMAIN) != '?' ) {
 			trace.peek().domains = getFlagData(data, FP_DOMAIN_DATA);
 		}
 
 		if ( data.charAt(FP_EXTERNALRES) != '?' ) {
 			trace.peek().externalRes = getFlagData(data, FP_EXTERNALRES_DATA);
 		}
 
 		if ( data.charAt(FP_LOCFILTER) != '?' ) {
 			trace.peek().localeFilter = getFlagData(data, FP_LOCFILTER_DATA);
 		}
 		
 		if ( data.charAt(FP_LQISSUE) != '?' ) {
 			String[] values = fromSingleString(getFlagData(data, FP_LQISSUE_DATA));
 			// Do not override if the value is no defined
 			if ( values[0] != null ) trace.peek().lqIssuesRef = values[0];
 			if ( values[1] != null ) trace.peek().lqIssueType = values[1];
 			if ( values[2] != null ) trace.peek().lqIssueComment = values[2];
 			if ( values[3] != null ) trace.peek().lqIssueScore = values[3];
 			if ( values[4] != null ) trace.peek().lqIssueProfileRef = values[4];
 		}
 
 		if ( data.charAt(FP_STORAGESIZE) != '?' ) {
 			String[] tmp = fromSingleString(getFlagData(data, FP_STORAGESIZE_DATA));
 			trace.peek().storeSize = tmp[0];
 			trace.peek().storeSizeEncoding = tmp[1];
 		}
 		
 		trace.peek().targetPointer = getFlagData(data, FP_TARGETPOINTER_DATA);
 		
 		if ( data.charAt(FP_DIRECTIONALITY) != '?' ) {
 			switch ( data.charAt(FP_DIRECTIONALITY) ) {
 			case '0':
 				trace.peek().dir = DIR_LTR;
 				break;
 			case '1':
 				trace.peek().dir = DIR_RTL;
 				break;
 			case '2':
 				trace.peek().dir = DIR_LRO;
 				break;
 			case '3':
 				trace.peek().dir = DIR_LRO;
 				break;
 			}
 		}
 		
 		if ( data.charAt(FP_WITHINTEXT) != '?' ) {
 			switch ( data.charAt(FP_WITHINTEXT) ) {
 			case '0':
 				trace.peek().withinText = WITHINTEXT_NO;
 				break;
 			case '1':
 				trace.peek().withinText = WITHINTEXT_YES;
 				break;
 			case '2':
 				trace.peek().withinText = WITHINTEXT_NESTED;
 				break;
 			}
 		}
 		
 		if ( data.charAt(FP_TERMINOLOGY) != '?' ) {
 			trace.peek().term = (data.charAt(FP_TERMINOLOGY) == 'y');
 			trace.peek().termInfo = getFlagData(data, FP_TERMINOLOGY_DATA);
 		}
 		
 		if ( data.charAt(FP_LOCNOTE) != '?' ) {
 			trace.peek().locNote = getFlagData(data, FP_LOCNOTE_DATA);
 		}
 
 		// Preserve white spaces
 		if ( data.charAt(FP_PRESERVEWS) != '?' ) {
 			trace.peek().preserveWS = (data.charAt(FP_PRESERVEWS) == 'y');
 		}
 		
 		if ( data.charAt(FP_LANGINFO) != '?' ) {
 			trace.peek().language = getFlagData(data, FP_LANGINFO_DATA);
 		}
 		
 	}
 
 	public void startTraversal () {
 		node = null;
 		trace = new Stack<ITSTrace>();
 		//trace.push(new ITSTrace(true)); // For root #document
 		startTraversal = true;
 	}
 	
 	private void clearInternalGlobalRules () {
 		for ( int i=0; i<rules.size(); i++ ) {
 			if ( rules.get(i).isInternal ) {
 				rules.remove(i);
 				i--;
 			}
 		}
 	}
 
 	private void processGlobalRules (int dataCategories) {
 		try {
 			// Compile any internal global rules
 			clearInternalGlobalRules();
 			compileRules(doc, docURI, true);
 			
 			// Now apply the compiled rules
 		    for ( ITSRule rule : rules ) {
 		    	// Check if we should apply this type of rule
 		    	if ( (dataCategories & rule.ruleType) == 0 ) continue;
 		    	
 		    	// Get the selected nodes for the rule
 		    	String data1;
 				XPathExpression expr = xpath.compile(rule.selector);
 				NodeList NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				
 				// Apply the rule specific action on the selected nodes
 				// Global rules are applies before local so they should 
 				// always override existing flag. override should be set to false
 				// only for default attributes.
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					switch ( rule.ruleType ) {
 					case IProcessor.DC_TRANSLATE:
 						setFlag(NL.item(i), FP_TRANSLATE, (rule.flag ? 'y' : 'n'), true);
 						// Set the hasTranslatabledattribute flag if it is an attribute node
 						if ( NL.item(i).getNodeType() == Node.ATTRIBUTE_NODE ) {
 							if ( rule.flag ) translatableAttributeRuleTriggered = true; 
 						}
 						if ( rule.idValue != null ) { // For deprecated extension
 							setFlag(NL.item(i), FP_IDVALUE_DATA, resolveExpressionAsString(NL.item(i), rule.idValue), true);							
 						}
 						// For deprecated extension
 						setFlag(NL.item(i), FP_PRESERVEWS, (rule.preserveWS ? 'y' : '?'), true);
 						break;
 						
 					case IProcessor.DC_DIRECTIONALITY:
 						setFlag(NL.item(i), FP_DIRECTIONALITY,
 							String.valueOf(rule.value).charAt(0), true);
 						break;
 						
 					case IProcessor.DC_WITHINTEXT:
 						setFlag(NL.item(i), FP_WITHINTEXT,
 							String.valueOf(rule.value).charAt(0), true);
 						break;
 						
 					case IProcessor.DC_TERMINOLOGY:
 						setFlag(NL.item(i), FP_TERMINOLOGY, (rule.flag ? 'y' : 'n'), true);
 						switch ( rule.infoType ) {
 						case TERMINFOTYPE_POINTER:
 							setFlag(NL.item(i), FP_TERMINOLOGY_DATA, resolvePointer(NL.item(i), rule.info), true);
 							break;
 						case TERMINFOTYPE_REF:
 							setFlag(NL.item(i), FP_TERMINOLOGY_DATA, "REF:"+rule.info, true);
 							break;
 						case TERMINFOTYPE_REFPOINTER:
 							setFlag(NL.item(i), FP_TERMINOLOGY_DATA, "REF:"+resolvePointer(NL.item(i), rule.info), true);
 							break;
 						}
 						break;
 						
 					case IProcessor.DC_LOCNOTE:
 						setFlag(NL.item(i), FP_LOCNOTE, 'y', true);
 						switch ( rule.infoType ) {
 						case LOCNOTETYPE_TEXT:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, rule.info, true);
 							break;
 						case LOCNOTETYPE_POINTER:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, resolvePointer(NL.item(i), rule.info), true);
 							break;
 						case LOCNOTETYPE_REF:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, "REF:"+rule.info, true);
 							break;
 						case LOCNOTETYPE_REFPOINTER:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, "REF:"+resolvePointer(NL.item(i), rule.info), true);
 							break;
 						}
 						break;
 						
 					case IProcessor.DC_LANGINFO:
 						setFlag(NL.item(i), FP_LANGINFO, 'y', true);
 						setFlag(NL.item(i), FP_LANGINFO_DATA, resolvePointer(NL.item(i), rule.info), true);
 						break;
 						
 					case IProcessor.DC_EXTERNALRES:
 						setFlag(NL.item(i), FP_EXTERNALRES, 'y', true);
 						setFlag(NL.item(i), FP_EXTERNALRES_DATA, resolvePointer(NL.item(i), rule.info), true);
 						break;
 						
 					case IProcessor.DC_LOCFILTER:
 						setFlag(NL.item(i), FP_LOCFILTER, 'y', true);
 						setFlag(NL.item(i), FP_LOCFILTER_DATA, rule.info, true);
 						break;
 						
 					case IProcessor.DC_PRESERVESPACE:
 						// For new ITS 2.0 rule, but deprecated extension still supported in DC_PRESERVESPACE case
 						setFlag(NL.item(i), FP_PRESERVEWS, (rule.preserveWS ? 'y' : '?'), true);
 						break;
 						
 					case IProcessor.DC_IDVALUE:
 						// For new ITS 2.0 rule, but deprecated extension still supported in DC_TRANSLATE case
 						if ( rule.idValue != null ) {
 							setFlag(NL.item(i), FP_IDVALUE_DATA, resolveExpressionAsString(NL.item(i), rule.idValue), true);							
 						}
 						break;
 						
 					case IProcessor.DC_DOMAIN:
 						List<String> list = resolveExpressionAsList(NL.item(i), rule.info);
 						// Map the values and build the final string
 						StringBuilder tmp = new StringBuilder();
 						for ( String value : list ) {
 							if ( rule.map != null ) {
 								if ( rule.map.containsKey(value) ) {
 									value = rule.map.get(value);
 								}
 							}
 							if ( tmp.length() > 0 ) tmp.append("\t");
 							tmp.append(value);
 						}
 						setFlag(NL.item(i), FP_DOMAIN, 'y', true);
 						setFlag(NL.item(i), FP_DOMAIN_DATA, tmp.toString(), true);
 						break;
 						
 					case IProcessor.DC_TARGETPOINTER:
 						targetPointerRuleTriggered = true;
 						setFlag(NL.item(i), FP_TARGETPOINTER_DATA, rule.info, true);							
 						break;
 						
 					case IProcessor.DC_LOCQUALITYISSUE:
 						// Get and resolve type
 						// Get and resolve issues reference
 						data1 = rule.map.get("issuesRef");
 						if ( data1 == null ) {
 							data1 = rule.map.get("issuesRefPointer");
 							if ( !Util.isEmpty(data1) ) data1 = resolvePointer(NL.item(i), data1);
 						}
 						String data2 = rule.map.get("type");
 						if ( data2 == null ) {
 							data2 = rule.map.get("typePointer");
 							if ( !Util.isEmpty(data2) ) data2 = resolvePointer(NL.item(i), data2);
 						}
 						// Get and resolve comment
 						String data3 = rule.map.get("comment");
 						if ( data3 == null ) {
 							data3 = rule.map.get("commentPointer");
 							if ( !Util.isEmpty(data3) ) data3 = resolvePointer(NL.item(i), data3);
 						}
 						// Get and resolve score
 						String data4 = rule.map.get("score");
 						if ( data4 == null ) {
 							data4 = rule.map.get("scorePointer");
 							if ( !Util.isEmpty(data4) ) data4 = resolvePointer(NL.item(i), data4);
 						}
 						// Get and resolve profile reference
 						String data5 = rule.map.get("profileRef");
 						if ( data5 == null ) {
 							data5 = rule.map.get("profileRefPointer");
 							if ( !Util.isEmpty(data5) ) data5 = resolvePointer(NL.item(i), data5);
 						}
 						setFlag(NL.item(i), FP_LQISSUE, 'y', true);
 						setFlag(NL.item(i), FP_LQISSUE_DATA, toSingleString(data1, data2, data3, data4, data5), true);
 						break;
 						
 					case IProcessor.DC_STORAGESIZE:
 						data1 = rule.map.get("size");
 						if ( data1 == null ) {
 							data1 = rule.map.get("sizePointer");
 							if ( !Util.isEmpty(data1) ) data1 = resolvePointer(NL.item(i), data1);
 						}
 						data2 = rule.map.get("encoding");
 						if ( data2 == null ) {
 							data2 = rule.map.get("encodingPointer");
 							if ( !Util.isEmpty(data2) ) data2 = resolvePointer(NL.item(i), data2);
 						}
 						setFlag(NL.item(i), FP_STORAGESIZE, 'y', true);
 						setFlag(NL.item(i), FP_STORAGESIZE_DATA, toSingleString(data1, data2), true);
 						break;
 					}
 				}
 		    }
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Converts a list of strings arguments to a single string that is delimited with end-of-group characters. 
 	 * @param values the values to store. Null values are ok and mean no value.
 	 * @return a single string with all values.
 	 */
 	private String toSingleString (String ... values) {
 		StringBuilder data = new StringBuilder();
 		for ( String value : values ) {
 			if ( value == null ) data.append("\u001A");
 			if ( value != null ) data.append(value);
 			data.append("\u001D");
 		}
 		return data.toString();
 	}
 	
 	private String[] fromSingleString (String data) {
 		String[] values = data.split("\u001D", -1);
 		for ( int i=0; i<values.length; i++ ) {
 			if ( values[i].equals("\u001A") ) values[i] = null;
 		}
 		return values;
 	}
 	
 	private void processLocalRules (int dataCategories) {
 		XPathExpression expr;
 		NodeList NL;
 		Attr attr;
 		try {
 			if ( (dataCategories & IProcessor.DC_TRANSLATE) > 0 ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":translate|//"+ITS_NS_PREFIX+":span/@translate");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "translateRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Validate the value
 					String value = attr.getValue();
 					if (( !"yes".equals(value) ) && ( !"no".equals(value) )) {
 						throw new ITSException("Invalid value for 'translate'.");
 					}
 					// Set the flag
 					setFlag(attr.getOwnerElement(), FP_TRANSLATE, value.charAt(0), attr.getSpecified());
 					// No need to update hasTranslatabledattribute here because all nodes have to be elements in locale rules
 				}
 			}
 			
 			if ( (dataCategories & IProcessor.DC_DIRECTIONALITY) > 0 ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":dir|//"+ITS_NS_PREFIX+":span/@dir");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "dirRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag on the others
 					int n = DIR_LTR;
 					if ( "rtl".equals(attr.getValue()) ) n = DIR_LTR; 
 					else if ( "ltr".equals(attr.getValue()) ) n = DIR_RTL; 
 					else if ( "rlo".equals(attr.getValue()) ) n = DIR_RLO; 
 					else if ( "lro".equals(attr.getValue()) ) n = DIR_LRO;
 					else throw new ITSException("Invalid value for 'dir'."); 
 					setFlag(attr.getOwnerElement(), FP_DIRECTIONALITY,
 						String.format("%d", n).charAt(0), attr.getSpecified());
 				}
 			}
 			
 			if ( (dataCategories & IProcessor.DC_TERMINOLOGY) > 0 ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":term|//"+ITS_NS_PREFIX+":span/@term"
					+"//*/@"+ITS_NS_PREFIX+":termInfoRef|//"+ITS_NS_PREFIX+":span/@termInfoRef");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				String localName;
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					localName = attr.getLocalName();
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "termRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// term
 					if ( localName.equals("term") ) {
 						// Validate the value
 						String value = attr.getValue();
 						if (( !"yes".equals(value) ) && ( !"no".equals(value) )) {
 							throw new ITSException("Invalid value for 'term'.");
 						}
 						// Set the flag
 						setFlag(attr.getOwnerElement(), FP_TERMINOLOGY, value.charAt(0), attr.getSpecified());
 					}
 					else if ( localName.equals("termInfoPointer") ) {
 						setFlag(attr.getOwnerElement(), FP_TERMINOLOGY_DATA,
 							"REF:"+resolvePointer(attr.getOwnerElement(), attr.getValue()), attr.getSpecified());
 					}
 				}
 			}
 
 			if ( (dataCategories & IProcessor.DC_LOCNOTE) > 0 ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":locNote|//"+ITS_NS_PREFIX+":span/@locNote"
 					+"|//*/@"+ITS_NS_PREFIX+":locNoteRef|//"+ITS_NS_PREFIX+":span/@locNoteRef");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				String localName;
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					localName = attr.getLocalName();
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "locNoteRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					setFlag(attr.getOwnerElement(), FP_LOCNOTE, 'y', attr.getSpecified());
 					if ( localName.equals("locNote") ) {
 						setFlag(attr.getOwnerElement(), FP_LOCNOTE_DATA,
 							attr.getValue(), attr.getSpecified());
 					}
 					else if ( localName.equals("termInfoPointer") ) {
 						setFlag(attr.getOwnerElement(), FP_LOCNOTE_DATA,
 							"REF:"+resolvePointer(attr.getOwnerElement(), attr.getValue()), attr.getSpecified());
 					}
 				}
 			}
 
 			if ( (dataCategories & IProcessor.DC_LANGINFO) > 0 ) {
 				expr = xpath.compile("//*/@"+XML_NS_PREFIX+":lang");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Set the flag
 					setFlag(attr.getOwnerElement(), FP_LANGINFO, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_LANGINFO_DATA,
 						attr.getValue(), attr.getSpecified());
 				}
 			}
 
 			// Local withinText attribute (ITS 2.0 only)
 			if (( (dataCategories & IProcessor.DC_WITHINTEXT) > 0 ) && isVersion2() ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":withinText");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "withinTextRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					String value = attr.getValue();
 					char ch;
 					if ( "no".equals(value) ) ch='0'; // WITHINTEXT_NO;
 					else if ( "yes".equals(value) ) ch='1'; // WITHINTEXT_YES;
 					else if ( "nested".equals(value) ) ch='2'; // WITHINTEXT_NESTED;
 					else throw new ITSException("Invalid value for 'withinText'.");
 					setFlag(attr.getOwnerElement(), FP_WITHINTEXT, ch, attr.getSpecified());
 				}
 			}
 			
 			// xml:space always applied
 			expr = xpath.compile("//*/@"+XML_NS_PREFIX+":space");
 			NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			for ( int i=0; i<NL.getLength(); i++ ) {
 				attr = (Attr)NL.item(i);
 				// Validate the value
 				String value = attr.getValue();
 				if (( !"preserve".equals(value) ) && ( !"default".equals(value) )) {
 					throw new ITSException("Invalid value for 'xml:space'.");
 				}
 				// Set the flag
 				setFlag(attr.getOwnerElement(), FP_PRESERVEWS,
 					("preserve".equals(value) ? 'y' : '?'), attr.getSpecified());
 			}
 			
 			// locale filter
 			if (( (dataCategories & IProcessor.DC_LOCFILTER) > 0 ) && isVersion2() ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":localeFilterList|//"+ITS_NS_PREFIX+":span/@localeFilterList");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "localeFilterRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String value = retrieveLocaleFilterList(attr.getOwnerElement(), qualified);
 					setFlag(attr.getOwnerElement(), FP_LOCFILTER, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_LOCFILTER_DATA,
 						value, attr.getSpecified()); 
 				}
 			}
 			
 			// xml:id always applied
 			expr = xpath.compile("//*/@"+XML_NS_PREFIX+":id");
 			NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			for ( int i=0; i<NL.getLength(); i++ ) {
 				attr = (Attr)NL.item(i);
 				String value = attr.getValue();
 				if (( value != null ) && ( value.length() > 0 )) {
 					setFlag(attr.getOwnerElement(), FP_IDVALUE_DATA,
 						value, attr.getSpecified());
 				}
 			}
 			
 			// Localization quality issue
 			if (( (dataCategories & IProcessor.DC_LOCQUALITYISSUE) > 0 ) && isVersion2() ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":locQualityIssueType|//"+ITS_NS_PREFIX+":span/@locQualityIssueType"
 					+"|//*/@"+ITS_NS_PREFIX+":locQualityIssueComment|//"+ITS_NS_PREFIX+":span/@locQualityIssueComment"
 					+"|//*/@"+ITS_NS_PREFIX+":locQualityIssuesRef|//"+ITS_NS_PREFIX+":span/@locQualityIssuesRef");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "locQualityIssueRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String[] values = retrieveLocQualityIssueData(attr.getOwnerElement(), qualified);
 					// Get the previous data (if any)
 					String oriData = (String)attr.getOwnerElement().getUserData(FLAGNAME);
 					if ( oriData != null ) {
 						String[] ori = fromSingleString(getFlagData(oriData, FP_LQISSUE_DATA));
 						// Use original values if local one is not defined
 						if ( values[0] == null ) values[0] = ori[0];
 						if ( values[1] == null ) values[1] = ori[1];
 						if ( values[2] == null ) values[2] = ori[2];
 						if ( values[3] == null ) values[3] = ori[3];
 						if ( values[4] == null ) values[4] = ori[4];
 					}
 					// Set the updated flags
 					setFlag(attr.getOwnerElement(), FP_LQISSUE, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_LQISSUE_DATA,
 						toSingleString(values[0], values[1], values[2], values[3], values[4]), attr.getSpecified()); 
 				}
 			}
 			
 			// Storage size
 			if (( (dataCategories & IProcessor.DC_STORAGESIZE) > 0 ) && isVersion2() ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":storageSize|//"+ITS_NS_PREFIX+":span/@storageSize");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "storageSizeRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String[] values = retrieveStorageSizeData(attr.getOwnerElement(), qualified);
 					// Get the previous data (if any)
 					String oriData = (String)attr.getOwnerElement().getUserData(FLAGNAME);
 					if ( oriData != null ) {
 						String[] ori = fromSingleString(getFlagData(oriData, FP_STORAGESIZE_DATA));
 						// Use original values if local one is not defined
 						if ( values[0] == null ) values[0] = ori[0];
 						if ( values[1] == null ) values[1] = ori[1];
 					}
 					// Set the updated flags
 					setFlag(attr.getOwnerElement(), FP_STORAGESIZE, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_STORAGESIZE_DATA,
 						toSingleString(values[0], values[1]), attr.getSpecified()); 
 				}
 			}
 
 			// Local targetPointer attribute (ITS 2.0 only)
 			if (( (dataCategories & IProcessor.DC_TARGETPOINTER) > 0 ) && isVersion2() ) {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":targetPointer");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "targetPointerRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					String value = attr.getValue();
 					if ( value != null ) {
 						setFlag(attr.getOwnerElement(), FP_TARGETPOINTER_DATA,
 							value, attr.getSpecified());
 					}
 				}
 			}
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Retrieve the final list to use for a locale filter data category
 	 * @param elem the element where the attributes are defined.
 	 * @param qualified true if the attributes are expected to be qualified (local markup)
 	 * @return the final list.
 	 */
 	private String retrieveLocaleFilterList (Element elem,
 		boolean qualified)
 	{
 		if ( qualified ) { // Locally
 			return elem.getAttributeNS(ITS_NS_URI, "localeFilterList").trim();
 		}
 		else { // Inside a global rule
 			return elem.getAttribute("localeFilterList").trim();
 		}
 	}
 	
 	private String[] retrieveStorageSizeData (Element elem,
 		boolean qualified)
 	{
 		String[] data = new String[2];
 		
 		if ( qualified ) {
 			if ( elem.hasAttributeNS(ITS_NS_URI, "storageSize") )
 				data[0] = elem.getAttributeNS(ITS_NS_URI, "storageSize");
 			if ( elem.hasAttributeNS(ITS_NS_URI, "storageSizeEncoding") )
 				data[1] = elem.getAttributeNS(ITS_NS_URI, "storageSizeEncoding");
 		}
 		else {
 			if ( elem.hasAttribute("storageSize") )
 				data[0] = elem.getAttribute("storageSize");
 			if ( elem.hasAttribute("storageSizeEncoding") )
 				data[1] = elem.getAttribute("storageSizeEncoding");
 		}
 		
 		return data;
 	}
 	
 	/**
 	 * Retrieves the non-pointer information of the Localization Quality issue data category.
 	 * @param elem the element where to get the data.
 	 * @param qualified true if the attributes are expected to be qualified.
 	 * @return an array of the value: issues reference, type, comment, score, profile reference.
 	 */
 	private String[] retrieveLocQualityIssueData (Element elem,
 		boolean qualified)
 	{
 		String[] data = new String[5];
 		
 		if ( qualified ) {
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssuesRef") )
 				data[0] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssuesRef");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueType") )
 				data[1] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueType");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueComment") )
 				data[2] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueComment");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueScore") )
 				data[3] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueScore");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueProfileRef") )
 				data[4] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueProfileRef");
 		}
 		else {
 			if ( elem.hasAttribute("locQualityIssuesRef") )
 				data[0] = elem.getAttribute("locQualityIssuesRef");
 			
 			if ( elem.hasAttribute("locQualityIssueType") )
 				data[1] = elem.getAttribute("locQualityIssueType");
 			
 			if ( elem.hasAttribute("locQualityIssueComment") )
 				data[2] = elem.getAttribute("locQualityIssueComment");
 			
 			if ( elem.hasAttribute("locQualityIssueScore") )
 				data[3] = elem.getAttribute("locQualityIssueScore");
 			
 			if ( elem.hasAttribute("locQualityIssueProfileRef") )
 				data[4] = elem.getAttribute("locQualityIssueProfileRef");
 		}
 
 		// Do not check for complete set of required characters
 		// This because we could have a global pointer that defines a non-native way to get the data
 
 		return data;
 	}
 	
 	private boolean isVersion2 () throws XPathExpressionException {
 		// If the version is not detected yet: detect it.
 		if ( version.equals("0") ) {
 			XPathExpression expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":version|//"+ITS_NS_PREFIX+":span/@version");
 			NodeList NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			if (( NL == null ) || ( NL.getLength() == 0 )) {
 				// No version detected: we assume it's a 2.0 behavior
 				version = "2.0";
 			}
 			else {
 				if ( NL.getLength() > 0 ) {
 					version = ((Attr)NL.item(0)).getValue();
 					if ( !version.equals(ITS_VERSION1) && !version.equals(ITS_VERSION2) ) {
 						throw new ITSException(String.format("Invalid or missing ITS version (\"%s\")", version));
 					}
 				}
 				if ( NL.getLength() > 1 ) {
 					throw new ITSException("More than one ITS version is defined in this document.");
 				}
 			}
 		}
 		return version.equals(ITS_VERSION2);		
 	}
 	
 	/**
 	 * Gets the text content of the first TEXT child of an element node.
 	 * This is to use instead of node.getTextContent() which does not work with some
 	 * Macintosh Java VMs. Note this work-around get <b>only the first TEXT node</b>.
 	 * @param node the container element.
 	 * @return the text of the first TEXT child node.
 	 */
 	public static String getTextContent (Node node) {
 		Node tmp = node.getFirstChild();
 		while ( true ) {
 			if ( tmp == null ) return "";
 			if ( tmp.getNodeType() == Node.TEXT_NODE ) {
 				return tmp.getNodeValue();
 			}
 			tmp = tmp.getNextSibling();
 		}
 	}
 
 	private String resolvePointer (Node node,
 		String pointer)
 	{
 		try {
 			XPathExpression expr = xpath.compile(pointer);
 			NodeList list = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
 			if (( list == null ) || ( list.getLength() == 0 )) {
 				return "";
 			}
 			switch ( list.item(0).getNodeType() ) {
 			case Node.ELEMENT_NODE:
 				return getTextContent(list.item(0));
 			case Node.ATTRIBUTE_NODE:
 				return list.item(0).getNodeValue();
 			}
 		}
 		catch (XPathExpressionException e) {
 			return "Bad XPath expression in pointer \""+pointer+"\".";
 		}
 		return "pointer("+pointer+")";
 	}
 	
 	private String resolveExpressionAsString (Node node,
 		String expression)
 	{
 		try {
 			XPathExpression expr = xpath.compile(expression);
 			return (String)expr.evaluate(node, XPathConstants.STRING);
 		}
 		catch (XPathExpressionException e) {
 			return "Bab XPath expression \""+expression+"\".";
 		}
 	}
 		
 	private List<String> resolveExpressionAsList (Node node,
 		String expression)
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		try {
 			XPathExpression expr = xpath.compile(expression);
 			NodeList nl = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				Node tmpNode = nl.item(i);
 				if ( tmpNode.getNodeType() == Node.ELEMENT_NODE ) {
 					list.add(tmpNode.getTextContent());
 				}
 				else { // Attribute
 					list.add(tmpNode.getNodeValue());
 				}
 			}
 		}
 		catch (XPathExpressionException e) {
 			list.add("Bab XPath expression \""+expression+"\".");
 		}
 		return list;
 	}
 			
 	/**
 	 * Sets the flag for a given node.
 	 * @param node The node to flag.
 	 * @param position The position for the data category.
 	 * @param value The value to set.
 	 * @param override True if the value should override an existing value.
 	 * False should be used only for default attribute values.
 	 */
 	private void setFlag (Node node,
 		int position,
 		char value,
 		boolean override)
 	{
 		StringBuilder data = new StringBuilder();
 		if ( node.getUserData(FLAGNAME) == null )
 			data.append(FLAGDEFAULTDATA);
 		else
 			data.append((String)node.getUserData(FLAGNAME));
 		// Set the new value (if not there yet or override requested)
 		if ( override || ( data.charAt(position) != '?' )) 
 			data.setCharAt(position, value);
 		node.setUserData(FLAGNAME, data.toString(), null);
 	}
 
 	/**
 	 * Sets the data for a flag for a given node.
 	 * @param node The node where to set the data.
 	 * @param position The position for the data category.
 	 * @param value The value to set.
 	 * @param override True if the value should override an existing value.
 	 * False should be used only for default attribute values.
 	 */
 	private void setFlag (Node node,
 		int position,
 		String value,
 		boolean override)
 	{
 		StringBuilder data = new StringBuilder();
 		if ( node.getUserData(FLAGNAME) == null )
 			data.append(FLAGDEFAULTDATA);
 		else
 			data.append((String)node.getUserData(FLAGNAME));
 		// Get the data
 		int n1 = 0;
 		int n2 = data.indexOf(FLAGSEP, 0);
 		for ( int i=0; i<=position; i++ ) {
 			n1 = n2;
 			n2 = data.indexOf(FLAGSEP, n1+1);
 		}
 		// Set the new value (if not there yet or override requested)
 		if ( override || ( n2>n1+1 )) {
 			data.replace(n1+1, n2, value);
 		}
 		node.setUserData(FLAGNAME, data.toString(), null);
 	}
 	
 	private String getFlagData (String data,
 		int position)
 	{
 		int n1 = 0;
 		int n2 = data.indexOf(FLAGSEP, 0);
 		for ( int i=0; i<=position; i++ ) {
 			n1 = n2;
 			n2 = data.indexOf(FLAGSEP, n1+1);
 		}
 		if ( n2>n1+1 ) return data.substring(n1+1, n2);
 		else return "";
 	}
 
 	public boolean translate () {
 		return trace.peek().translate;
 	}
 	
 	public boolean translate (Attr attribute) {
 		if ( attribute == null ) return false;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return false;
 		// '?' and 'n' will return (correctly) false
 		return (tmp.charAt(FP_TRANSLATE) == 'y');
 	}
 
 	public String getTargetPointer () {
 		return trace.peek().targetPointer;
 	}
 	
 	public String getIdValue () {
 		return trace.peek().idValue;
 	}
 	
 	public int getDirectionality () {
 		return trace.peek().dir;
 	}
 
 	public int getDirectionality (Attr attribute) {
 		if ( attribute == null ) return DIR_LTR;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return DIR_LTR;
 		return Integer.valueOf(tmp.charAt(FP_DIRECTIONALITY));
 	}
 	
 	public int getWithinText () {
 		return trace.peek().withinText;
 	}
 	
 	public boolean isTerm () {
 		return trace.peek().term;
 	}
 	
 	public String getTermInfo () {
 		return trace.peek().termInfo;
 	}
 
 	public boolean isTerm (Attr attribute) {
 		if ( attribute == null ) return false;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return false;
 		// '?' and 'n' will return (correctly) false
 		return (tmp.charAt(FP_TERMINOLOGY) == 'y');
 	}
 
 	public String getNote () {
 		return trace.peek().locNote;
 	}
 	
 	public String getNote (Attr attribute) {
 		if ( attribute == null ) return null;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LOCNOTE) != 'y' ) return null;
 		return getFlagData(tmp, FP_LOCNOTE_DATA);
 	}
 
 	public String getDomains () {
 		return trace.peek().domains;
 	}
 	
 	public String getDomains (Attr attribute) {
 		if ( attribute == null ) return null;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_DOMAIN) != 'y' ) return null;
 		return getFlagData(tmp, FP_DOMAIN_DATA);
 	}
 
 	public boolean preserveWS () {
 		return trace.peek().preserveWS;
 	}
 
 	public String getLanguage () {
 		return trace.peek().language;
 	}
 
 	@Override
 	public String getExternalResourceRef () {
 		return trace.peek().externalRes;
 	}
 
 	@Override
 	public String getExternalResourceRef (Attr attribute) {
 		if ( attribute == null ) return null;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_EXTERNALRES) != 'y' ) return null;
 		return getFlagData(tmp, FP_EXTERNALRES_DATA);
 	}
 
 	@Override
 	public String getLocaleFilter () {
 		return trace.peek().localeFilter;
 	}
 
 	@Override
 	public String getLocQualityIssuesRef () {
 		return trace.peek().lqIssuesRef;
 	}
 
 	@Override
 	public String getLocQualityIssueType () {
 		return trace.peek().lqIssueType;
 	}
 
 	@Override
 	public String getLocQualityIssueComment () {
 		return trace.peek().lqIssueComment;
 	}
 
 	@Override
 	public String getLocQualityIssueScore () {
 		return trace.peek().lqIssueScore;
 	}
 
 	@Override
 	public String getLocQualityIssueProfileRef () {
 		return trace.peek().lqIssueProfileRef;
 	}
 
 	@Override
 	public String getStorageSize () {
 		return trace.peek().storeSize;
 	}
 
 	@Override
 	public String getStorageSize (Attr attribute) {
 		if ( attribute == null ) return null;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_STORAGESIZE) != 'y' ) return null;
 		String[] values = fromSingleString(getFlagData(tmp, FP_STORAGESIZE_DATA));
 		return values[0];
 	}
 
 	@Override
 	public String getStorageSizeEncoding () {
 		String tmp = trace.peek().storeSizeEncoding;
 		if ( tmp == null ) return "UTF-8";
 		return tmp;
 	}
 
 	@Override
 	public String getStorageSizeEncoding(Attr attribute) {
 		if ( attribute == null ) return null;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_STORAGESIZE) != 'y' ) return null;
 		String[] values = fromSingleString(getFlagData(tmp, FP_STORAGESIZE_DATA));
 		if ( values[1] == null ) return "UTF-8";
 		else return values[1];
 	}
 
 }
