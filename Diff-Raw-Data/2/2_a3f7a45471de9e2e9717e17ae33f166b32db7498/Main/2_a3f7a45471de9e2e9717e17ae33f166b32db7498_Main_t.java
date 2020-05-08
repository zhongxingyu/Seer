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
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.GenericAnnotationType;
 import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.filters.its.html5.HTML5Filter;
 import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * Main entry point for generating test files for ITS.
  */
 public class Main {
 
 	public static final String DC_TRANSLATE = "translate";
 	public static final String DC_LOCALIZATIONNOTE = "locnote";
 	public static final String DC_TERMINOLOGY = "terminology";
 	public static final String DC_DIRECTIONALITY = "dir";
 	public static final String DC_LANGUAGEINFORMATION = "lang";
 	public static final String DC_WITHINTEXT = "withintext";
 	public static final String DC_DOMAIN = "domain";
 	public static final String DC_DISAMBIGUATION = "disambiguation";
 	public static final String DC_LOCALEFILTER = "localefilter";
 	public static final String DC_PROVENANCE = "provenance";
 	public static final String DC_EXTERNALRESOURCE = "externalresource";
 	public static final String DC_TARGETPOINTER = "targetpointer";
 	public static final String DC_IDVALUE = "idvalue";
 	public static final String DC_PRESERVESPACE = "preservespace";
 	public static final String DC_LOCQUALITYISSUE = "locqualityissue";
 	public static final String DC_LOCQUALITYRATING = "locqualityrating";
 	public static final String DC_MTCONFIDENCE = "mtconfidence";
 	public static final String DC_STORAGESIZE = "storagesize";
 	public static final String DC_ALLOWEDCHARACTERS = "allowedcharacters";
 	
 	public static void main (String[] args) {
  
 		PrintWriter writer = null;
 		
 		try {
 			System.out.println("ITSTest - Test File Geneator for XML+ITS and HTML5+ITS");
 			
 			File inputFile = null;
 			File outputFile = null;
 			File rulesFile = null;
 			String dc = "translate";
 			boolean isHTML5 = false;
 			
 			for ( int i=0; i<args.length; i++ ) {
 				String arg = args[i];
 				if ( arg.equals("-r") ) { // External rule file
 					i++; rulesFile = new File(args[i]);
 				}
 				else if ( arg.equals("-dc") ) { // Data category
 					i++; dc = args[i].toLowerCase();
 				}
 				else if ( arg.equals("-?") ) {
 					showUsage();
 					return;
 				}
 				else if ( arg.equals("-l") ) {
 					System.out.println(DC_TRANSLATE
 						+ "\n" + DC_LOCALIZATIONNOTE
 						+ "\n" + DC_TERMINOLOGY
 						+ "\n" + DC_DIRECTIONALITY
 						+ "\n" + DC_LANGUAGEINFORMATION
 						+ "\n" + DC_WITHINTEXT
 						+ "\n" + DC_DOMAIN
 						+ "\n" + DC_DISAMBIGUATION
 						+ "\n" + DC_LOCALEFILTER
 						+ "\n" + DC_PROVENANCE
 						+ "\n" + DC_EXTERNALRESOURCE
 						+ "\n" + DC_TARGETPOINTER
 						+ "\n" + DC_IDVALUE
 						+ "\n" + DC_PRESERVESPACE
 						+ "\n" + DC_LOCQUALITYISSUE
 						+ "\n" + DC_LOCQUALITYRATING
 						+ "\n" + DC_MTCONFIDENCE
 						+ "\n" + DC_STORAGESIZE
 						+ "\n" + DC_ALLOWEDCHARACTERS
 					);
 					return;
 				}
 				else {
 					if ( inputFile == null ) {
 						inputFile = new File(args[i]);
 						isHTML5 = Util.getExtension(args[i]).toLowerCase().startsWith(".htm");
 					}
 					else {
 						outputFile = new File(args[i]);
 					}
 				}
 			}
 			
 			if ( inputFile == null ) {
 				showUsage();
 				return;
 			}
 			
 			// Default output
 			if ( outputFile == null ) {
 				//String ext = Util.getExtension(inputFile.getAbsolutePath());
 				String name = inputFile.getAbsolutePath();
 				int n = name.lastIndexOf('.');
 				if ( n > -1 ) name = name.substring(0, n);
 				name += "output";
 				name += ".txt";
 				outputFile = new File(name);
 			}
 			
 			// Trace
 			System.out.println("   input: " + inputFile.getAbsolutePath());
 			System.out.println("  output: " + outputFile.getAbsolutePath());
 			if ( rulesFile != null ) {
 				System.out.print("   rules: " + rulesFile.getAbsolutePath());
 			}
 			
 			Util.createDirectories(outputFile.getAbsolutePath());
 			writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");
 			
 			// Read the document
 			Document doc;
 			if ( isHTML5 ) {
 				HtmlDocumentBuilder docBuilder = new HtmlDocumentBuilder();
 				doc = docBuilder.parse(inputFile);
 			}
 			else {
 				DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 				fact.setNamespaceAware(true);
 				fact.setValidating(false);
 				doc = fact.newDocumentBuilder().parse(inputFile);
 			}
 			
 			// Applies the rules
 			ITraversal trav = applyITSRules(doc, inputFile, rulesFile, isHTML5);
 
 			String path = null;
 			Stack<Integer> stack = new Stack<Integer>();
 			stack.push(1);
 			int prevCount = 0;
 			
 			// Process the document
 			trav.startTraversal();
 			Node node;
 			while ( (node = trav.nextNode()) != null ) {
 				switch ( node.getNodeType() ) {
 				case Node.ELEMENT_NODE:
 					// Use !backTracking() to get to the elements only once
 					// and to include the empty elements (for attributes).
 					if ( trav.backTracking() ) {
 						int n = path.lastIndexOf('/');
 						if ( n > -1 ) path = path.substring(0, n);
 						prevCount = stack.pop();
 					}
 					else {
 						Element element = (Element)node;
 						// Get the previous sibling element (if any)
 						Node prev = element;
 						do {
 							prev = prev.getPreviousSibling();
 						}						
 						while (( prev != null ) && 
 								(( prev.getNodeType() != Node.ELEMENT_NODE ) || 
 								(( prev.getNodeType() == Node.ELEMENT_NODE )  && ( !prev.getNodeName().equals(element.getNodeName())))));
 
 						// If it's the same kind of element, we increment the counter
 						if (( prev != null ) && prev.getNodeName().equals(element.getNodeName()) ) { 
 							stack.push(prevCount+1);
 						}
 						else {
 							stack.push(1);
 						}
 						
 						if ( element == doc.getDocumentElement() ) {
 							path = "/"+element.getNodeName();
 						}
 						else {
 							path += String.format("/%s[%d]", element.getNodeName(), stack.peek());
 						}
 
 						// Gather and output the values for the element
 						output(writer, dc, path, trav, null);
 
 						if ( element.hasAttributes() ) {
 							NamedNodeMap map = element.getAttributes();
 							
 							ArrayList<String> list = new ArrayList<String>();
 							for ( int i=0; i<map.getLength(); i++ ) {
 								list.add(((Attr)map.item(i)).getNodeName());
 							}
 							Collections.sort(list);
 							
 							for ( String attrName : list ) {
 								Attr attr = (Attr)map.getNamedItem(attrName);
								if ( attr.getNodeName().startsWith("xmlns") ) continue; // Skip NS declarations
 								// gather and output the values for the attribute
 								output(writer, dc, path+"/@"+attr.getNodeName(), trav, attr);
 							}
 						}
 						
 						// Empty elements:
 						if ( !element.hasChildNodes() ) {
 							int n = path.lastIndexOf('/');
 							if ( n > -1 ) path = path.substring(0, n);
 							prevCount = stack.pop();
 						}
 						
 					}
 					break; // End switch
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 		}
 		finally {
 			if ( writer != null ) {
 				writer.flush();
 				writer.close();
 			}
 		}
 	}
 
 	private static void showUsage () {
 		System.out.println("Usage: <inputFile>[ <outputFile>][ <options>]");
 		System.out.println("Where the options are:");
 		System.out.println(" -? shows this help");
 		System.out.println(" -r <ruleFile> : associates the input with an ITS rule file");
 		System.out.println(" -l : lists all the avaibale data categories to use with -dc");
 		System.out.println(" -dc <data-category> : sets the data category to process (default=translate)");
 	}
 	
 	private static void output (PrintWriter writer,
 		String dc,
 		String path,
 		ITraversal trav,
 		Attr attr)
 	{
 		// Path
 		writer.print(path);
 
 		// Values
 		String out1 = null;
 		if ( dc.equals(DC_TRANSLATE) ) {
 			out1 = (trav.getTranslate(attr) ? "yes" : "no");
 			writer.print(String.format("\ttranslate=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_LOCALIZATIONNOTE) ) {
 			out1 = trav.getLocNote(attr);
 			if ( out1 != null ) {
 				//--somewhat ugly hack to remove white spaces.--
 				//--TODO: May need to be done more selectively--
 				out1 = unwrap(out1);
 				//--re-formatting the refs--
 				if ( out1.startsWith(GenericAnnotationType.REF_PREFIX) ) {
 					writer.print(String.format("\tlocNoteRef=\"%s\"", escape(out1.substring(GenericAnnotationType.REF_PREFIX.length())).replace("&quot;", "\"")));
 				}
 				else {
 					writer.print(String.format("\tlocNote=\"%s\"", escape(out1).replace("&quot;", "\"")));					
 				}
 				out1 = trav.getLocNoteType(attr);
 				writer.print(String.format("\tlocNoteType=\"%s\"", escape(out1)));
 			}
 		}
 		else if ( dc.equals(DC_TERMINOLOGY) ) {
 			out1 = trav.getAnnotatorsRef();
 			if ( out1 != null ) {
 				writer.print(String.format("\tannotatorsRef=\"%s\"", escape(out1)));
 			}
 			out1 = (trav.getTerm(attr) ? "yes" : "no");
 			if ( out1 != null ) writer.print(String.format("\tterm=\"%s\"", escape(out1)));
 			Double outF1 = trav.getTermConfidence(attr);
 			if ( outF1 != null ) {
 				writer.print(String.format("\ttermConfidence=\"%s\"", Util.formatDouble(outF1)));
 			}
 			out1 = trav.getTermInfo(attr);
 			if ( out1 != null ){
 				if ( out1.startsWith(GenericAnnotationType.REF_PREFIX) ) {
 					writer.print(String.format("\ttermInfoRef=\"%s\"", escape(out1.substring(GenericAnnotationType.REF_PREFIX.length()) )));
 				}
 				else {
 					if ( !Util.isEmpty(out1) ) {
 						writer.print(String.format("\ttermInfo=\"%s\"", escape(unwrap(out1))));
 					}
 				}
 			}
 		}
 		else if ( dc.equals(DC_DIRECTIONALITY) ) {
 			int dir = trav.getDirectionality(attr);
 			switch ( dir ) {
 			case ITraversal.DIR_LRO: out1 = "lro"; break;
 			case ITraversal.DIR_LTR: out1 = "ltr"; break;
 			case ITraversal.DIR_RLO: out1 = "rlo"; break;
 			case ITraversal.DIR_RTL: out1 = "rtl"; break;
 			}
 			writer.print(String.format("\tdir=\"%s\"", out1));
 		}
 		else if ( dc.equals(DC_LANGUAGEINFORMATION) ) {
 			out1 = trav.getLanguage();
 			if ( out1 != null ) writer.print(String.format("\tlang=\"%s\"", out1));			
 		}
 		else if ( dc.equals(DC_WITHINTEXT) ) {
 			if ( attr != null ){
 				writer.print("\n");
 				return;				
 			}
 			int wt = trav.getWithinText();
 			switch ( wt ) {
 			case ITraversal.WITHINTEXT_NESTED: out1 = "nested"; break;
 			case ITraversal.WITHINTEXT_NO: out1 = "no"; break;
 			case ITraversal.WITHINTEXT_YES: out1 = "yes"; break;
 			}
 			writer.print(String.format("\twithinText=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_DOMAIN) ) {
 			out1 = trav.getDomains(attr);
 			if ( out1 != null ) writer.print(String.format("\tdomains=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_DISAMBIGUATION) ) {
 			out1 = trav.getAnnotatorsRef();
 			if ( out1 != null ) {
 				writer.print(String.format("\tannotatorsRef=\"%s\"", escape(out1)));
 			}
 			out1 = trav.getDisambigClass(attr);
 			if ( out1 != null ) {
 				if ( out1.startsWith(GenericAnnotationType.REF_PREFIX) ) {
 					writer.print(String.format("\tdisambigClassRef=\"%s\"", escape(out1.substring(GenericAnnotationType.REF_PREFIX.length()))));
 				}
 				else {
 					writer.print(String.format("\tdisambigClass=\"%s\"", escape(out1)));
 				}
 			}
 			Double outFloat = trav.getDisambigConfidence(attr);
 			if ( outFloat != null ) {
 				writer.print(String.format("\tdisambigConfidence=\"%s\"", Util.formatDouble(outFloat)));
 			}
 			out1 = trav.getDisambigGranularity(attr);
 			if ( out1 != null ) writer.print(String.format("\tdisambigGranularity=\"%s\"", escape(out1)));
 			out1 = trav.getDisambigIdent(attr);
 			if ( out1 != null ) {
 				if ( out1.startsWith(GenericAnnotationType.REF_PREFIX) ) {
 					writer.print(String.format("\tdisambigIdentRef=\"%s\"", escape(out1.substring(GenericAnnotationType.REF_PREFIX.length()))));
 				}
 				else {
 					writer.print(String.format("\tdisambigIdent=\"%s\"", escape(out1)));
 				}
 			}
 			out1 = trav.getDisambigSource(attr);
 			if ( out1 != null ) {
 				writer.print(String.format("\tdisambigSource=\"%s\"", escape(out1)));
 			}
 		}
 		else if ( dc.equals(DC_LOCALEFILTER) ) {
 			out1 = trav.getLocaleFilter();
 			if ( out1 != null ) writer.print(String.format("\tlocaleFilterList=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_PROVENANCE) ) {
 			int count = trav.getProvRecordCount(attr);
 			if ( count == 0 ) {
 				writer.print("\n");
 				return; // Done for this node
 			}
 			// Else: print the entries
 			// IssuesRef
 			boolean standoff = false;
 			out1 = trav.getProvRecordsRef(attr);
 			if ( out1 != null ) {
 				writer.print(String.format("\tprovenanceRecordsRef=\"%s\"", escape(out1)));
 				standoff = true;
 			}
 			for ( int i=0; i<count; i++ ) {
 				printProvString(standoff, trav.getProvOrg(attr, i), "org", i, writer);
 				printProvString(standoff, trav.getProvPerson(attr, i), "person", i, writer);
 				printProvString(standoff, trav.getProvRef(attr, i), "provRef", i, writer);
 				printProvString(standoff, trav.getProvRevOrg(attr, i), "revOrg", i, writer);
 				printProvString(standoff, trav.getProvRevPerson(attr, i), "revPerson", i, writer);
 				printProvString(standoff, trav.getProvRevTool(attr, i), "revTool", i, writer);
 				printProvString(standoff, trav.getProvTool(attr, i), "tool", i, writer);
 			}
 		}
 		else if ( dc.equals(DC_EXTERNALRESOURCE) ) {
 			out1 = trav.getExternalResourceRef(attr);
 			if ( out1 != null ) writer.print(String.format("\texternalResourceRef=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_TARGETPOINTER) ) {
 			out1 = trav.getTargetPointer(attr);
 			if ( out1 != null ) writer.print(String.format("\ttargetPointer=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_IDVALUE) ) {
 			out1 = trav.getIdValue(attr);
 			if ( out1 != null ) writer.print(String.format("\tidValue=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_PRESERVESPACE) ) {
 			out1 = (trav.preserveWS() ? "preserve" : "default");
 			if ( out1 != null ) writer.print(String.format("\tspace=\"%s\"", escape(out1)));
 		}
 		else if ( dc.equals(DC_LOCQUALITYISSUE) ) {
 			int count = trav.getLocQualityIssueCount(attr);
 			if ( count == 0 ) {
 				writer.print("\n");
 				return; // Done for this node
 			}
 			// Else: print the entries
 			// IssuesRef
 			boolean standoff = false;
 			out1 = trav.getLocQualityIssuesRef(attr);
 			if ( out1 != null ) {
 				writer.print(String.format("\tlocQualityIssuesRef=\"%s\"", escape(out1)));
 				standoff = true;
 			}
 			for ( int i=0; i<count; i++ ) {
 				// Comment
 				out1 = trav.getLocQualityIssueComment(attr, i);
 				if ( out1 != null ) {
 					if ( standoff ) {
 						writer.print(String.format("\tlocQualityIssueComment[%d]=\"%s\"", i+1, escape(out1)));
 					}
 					else {
 						writer.print(String.format("\tlocQualityIssueComment=\"%s\"", escape(out1)));
 					}
 				}
 				// Enabled
 				Boolean outBool1 = trav.getLocQualityIssueEnabled(attr, i);
 				if ( outBool1 == null ) throw new NullPointerException("lQI-enabled is null.");
 				if ( standoff ) {
 					writer.print(String.format("\tlocQualityIssueEnabled[%d]=\"%s\"", i+1, (outBool1 ? "yes" : "no")));
 				}
 				else {
 					writer.print(String.format("\tlocQualityIssueEnabled=\"%s\"", (outBool1 ? "yes" : "no")));
 				}
 				// ProfileRef
 				out1 = trav.getLocQualityIssueProfileRef(attr, i);
 				if ( out1 != null ) {
 					if ( standoff ) { 
 						writer.print(String.format("\tlocQualityIssueProfileRef[%d]=\"%s\"", i+1, escape(out1)));
 					}
 					else {
 						writer.print(String.format("\tlocQualityIssueProfileRef=\"%s\"", escape(out1)));
 					}
 				}
 				// Severity
 				Double outFloat1 = trav.getLocQualityIssueSeverity(attr, i);
 				if ( outFloat1 != null ) {
 					if ( standoff ) {
 						writer.print(String.format("\tlocQualityIssueSeverity[%d]=\"%s\"", i+1, Util.formatDouble(outFloat1)));
 					}
 					else {
 						writer.print(String.format("\tlocQualityIssueSeverity=\"%s\"", Util.formatDouble(outFloat1)));
 					}
 				}
 				// Type
 				out1 = trav.getLocQualityIssueType(attr, i);
 				if ( out1 != null ) {
 					if ( standoff ) {
 						writer.print(String.format("\tlocQualityIssueType[%d]=\"%s\"", i+1, escape(out1)));
 					}
 					else {
 						writer.print(String.format("\tlocQualityIssueType=\"%s\"", escape(out1)));
 					}
 				}
 			}
 		}
 		else if ( dc.equals(DC_LOCQUALITYRATING) ) {
 			out1 = trav.getLocQualityRatingProfileRef(attr);
 			if ( out1 != null ) {
 				writer.print(String.format("\tlocQualityRatingProfileRef=\"%s\"", escape(out1.substring(GenericAnnotationType.REF_PREFIX.length()))));
 			}
 			Double outF1 = trav.getLocQualityRatingScore(attr);
 			if ( outF1 != null ) {
 				writer.print(String.format("\tlocQualityRatingScore=\"%s\"", Util.formatDouble(outF1)));
 			}
 			outF1 = trav.getLocQualityRatingScoreThreshold(attr);
 			if ( outF1 != null ) {
 				writer.print(String.format("\tlocQualityRatingScoreThreshold=\"%s\"", Util.formatDouble(outF1)));
 			}
 			Integer outInt = trav.getLocQualityRatingVote(attr);
 			if ( outInt != null ) {
 				writer.print(String.format("\tlocQualityRatingVote=\"%d\"", outInt));
 			}
 			outInt = trav.getLocQualityRatingVoteThreshold(attr);
 			if ( outInt != null ) {
 				writer.print(String.format("\tlocQualityRatingVoteThreshold=\"%d\"", outInt));
 			}
 		}
 		else if ( dc.equals(DC_MTCONFIDENCE) ) {
 			out1 = trav.getAnnotatorsRef();
 			if ( out1 != null ) {
 				writer.print(String.format("\tannotatorsRef=\"%s\"", escape(out1)));
 			}
 			Double outFloat1 = trav.getMtConfidence(attr);
 			if ( outFloat1 != null ) {
 				writer.print(String.format("\tmtConfidence=\"%s\"", Util.formatDouble(outFloat1)));
 			}
 		}
 		else if ( dc.equals(DC_STORAGESIZE) ) {
 			Integer intVal = trav.getStorageSize(attr);
 			if ( intVal != null ) {
 				out1 = trav.getLineBreakType(attr);
 				if ( out1 != null ) writer.print(String.format("\tlineBreakType=\"%s\"", out1));
 				out1 = trav.getStorageEncoding(attr);
 				if ( out1 != null ) writer.print(String.format("\tstorageEncoding=\"%s\"", escape(out1).replace("UTF-8", "utf-8")));
 				if ( intVal != null ) writer.print(String.format("\tstorageSize=\"%d\"", intVal));
 			}
 		}
 		else if ( dc.equals(DC_ALLOWEDCHARACTERS) ) {
 			out1 = trav.getAllowedCharacters(attr);
 			if ( out1 != null ) writer.print(String.format("\tallowedCharacters=\"%s\"", escape(out1)));
 		}
 		
 		writer.print("\n");
 	}
 
 	private static void printProvString (boolean standoff,
 		String value,
 		String name, 
 		int index, 
 		PrintWriter writer)
 	{
 		if ( value != null ) {
 			String suffix = "";
 			if ( value.startsWith(GenericAnnotationType.REF_PREFIX) ) {
 				value = value.substring(GenericAnnotationType.REF_PREFIX.length());
 				suffix = "Ref";
 			}
 			if ( standoff ) {
 				writer.print(String.format("\t%s%s[%d]=\"%s\"", name, suffix, index+1, escape(value)));
 			}
 			else {
 				writer.print(String.format("\t%s%s=\"%s\"", name, suffix, escape(value)));
 			}
 		}
 		
 	}
 	private static String unwrap (String text){
 		TextFragment tf = new TextFragment(text);
 		TextFragment.unwrap(tf);
 		return tf.toString();
 	}
 	
 	private static String escape (String text) {
 		text = text.replace("\n", "\\n");
 		text = text.replace("\t", "\\t");
 		return Util.escapeToXML(text, 3, false, null);
 	}
 	
 	private static ITraversal applyITSRules (Document doc,
 		File inputFile,
 		File rulesFile,
 		boolean isHTML5)
 	{
 		// Create the ITS engine
 		ITSEngine itsEng = new ITSEngine(doc, inputFile.toURI(), isHTML5, null);
 		
 		// For HTML5: load the default rules
 		if ( isHTML5 ) {
 			URL url = HTML5Filter.class.getResource("strict.fprm");
 			try {
 				itsEng.addExternalRules(url.toURI());
 			}
 			catch ( URISyntaxException e ) {
 				throw new OkapiBadFilterParametersException("Cannot load strict default parameters.");
 			}
 		}
 
 		// Add any external rules file(s)
 		if ( rulesFile != null ) {
 			itsEng.addExternalRules(rulesFile.toURI());
 		}
 		
 		// Load the linked rules for HTML
 		if ( isHTML5 ) {
 			HTML5Filter.loadLinkedRules(doc, inputFile.toURI(), itsEng);
 		}
 		
 		// Apply the all rules (external and internal) to the document
 		itsEng.applyRules(ITSEngine.DC_ALL);
 		
 		return itsEng;
 	}
 
 }
