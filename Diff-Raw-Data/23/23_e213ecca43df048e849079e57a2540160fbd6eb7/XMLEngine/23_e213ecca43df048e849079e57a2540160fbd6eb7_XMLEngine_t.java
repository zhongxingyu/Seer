 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		K. Stuart Smith,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package org.nchelp.meteor.aggregation;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.apache.xpath.XPathAPI;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.util.XMLDataTypes;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 
 /***********************
  * XMLEngine
  **********************/

//   Warning:  Fools rush in where angels fear to tread!!!
//   			This code is not for the faint of heart.  
//				Please understand this completely before
//				you even consider modifying one single
//				character of this code.  You have been warned.

 public class XMLEngine {
 	/*
 	 * We are dealing with three documents (DOM representations of XML
 	 * files). The "rules" is a XACML (v0.7) subset that expresses the
 	 * tests to make on the primaryOp and secondaryOp documents. Those
 	 * latter documents  are representations of  loan details provided
 	 * by Meteor data providers.
 	 */
 	private Document rules;
 	private Document primaryOp;
 	private Document secondaryOp;
 	
 	private final Logger log = Logger.create(this.getClass());
 
 	/* the predicateTypeStack is used to record the declared type of op-
 	 * erands involved in the predicate evaluation since there is really
 	 * no good way to  deduce that info. For example, is 2001 an integer
 	 * or a string; is Today a String or a date? The stack structure has
 	 * to be used since predicates can be recursively defined.
 	 */
 	private Stack predicateTypeStack;
 
 	/* so far, the grammar handles the operations "delta" and "difference"
 	 * so we use  these finals to encode those rather than perform exces-
 	 * sive numbers of string comparisons: there are enough already.
 	 */
 	private final int OP_DELTA = 0;
 	private final int OP_DIFFERENCE = 1;
 
 	/*
 	 * Access to fields of the primaryOp and secondaryOp documents will
 	 * be accomplished through use of XPath URI references specified in
 	 * the rules  document and applied to the Op documents. A "wrapper"
 	 * for XPath manipulation is provided by XPathAPI.
 	 */
 	private XPathAPI xPath;
 	
 	/*
 	 * We are forever going to be looking for an element-type node
 	 * with a specific  signature. Rather  do so as a utility than
 	 * spread it out/replicate it all over the place.
 	 * 
 	 * aNode:the "first" node in a linked list of siblings. Search
 	 * 		only this node and later in the list.
 	 * elementName: the name associated with the element-type node
 	 * 		we are looking for.
 	 * return: the desired node, or null if the search fails.
 	 */
 	private Node findEChild (Node aNode, String elementName) {
 		while (aNode != null)
 			if ((aNode.getNodeType() == Node.ELEMENT_NODE) &&
 				(aNode.getNodeName().equals (elementName)))
 				break;
 			else
 				aNode = aNode.getNextSibling();
 		return aNode;
 	}
 
 	/*
 	 * The "architecture" of this class' methods is to provide a method
 	 * for each nonterminal and terminal production used in the portion
 	 * of the XACML "rule" grammar we are going to accept. Each produc-
 	 * tion is represented as an element-type node of the rule document.
 	 * 
 	 * When a production method is invoked, the assumption is made that
 	 * the production-naming element-type node has been located and the
 	 * children of that  node (in the DOM hierarchy) should contain the
 	 * production-completing nodes.
 	 */
 
 	private boolean rule(Node aRule) {
 		// rule ::= and | or | not | predicate
 		
 		boolean result = false;
 		
 		while (aRule != null) {
 			if (aRule.getNodeType() == Node.ELEMENT_NODE) {
 				String ruleName = aRule.getNodeName();
 
 				if (ruleName.equals ("and")){
 					result = and(aRule);
					break;
 				}					
 				else if (ruleName.equals ("or")){
 					result = or (aRule);
					break;
 				}					
 				else if (ruleName.equals ("not")){
 					result = not (aRule);
					break;
 				}
 				else if (ruleName.equals ("predicate")){
 					result = predicate (aRule);
					break;
 				}
 			}
 			aRule = aRule.getNextSibling();
 		}
 		log.debug("rule method returning " + result);
 		return result;
 	}
 	
 	private boolean and (Node aRule) {
 		// and ::= *rule *rule
 		aRule = aRule.getFirstChild();
 		boolean result = true;
 		if (aRule != null)
 			do {
 				if (aRule.getNodeType() == Node.ELEMENT_NODE) {
 					result &= rule (aRule);
 				}
 				aRule = aRule.getNextSibling();
 			} while (result && aRule != null);
 		
 		log.debug("and method returning " + result);
 		return result;
 	}
 	
 	private boolean or (Node aRule) {
 		// or ::= *rule *rule
 		aRule = aRule.getFirstChild();
 		boolean result = false;
 		if (aRule != null)
 			do {
 				if (aRule.getNodeType() == Node.ELEMENT_NODE) {
 					result |= rule (aRule);
 				}
 				aRule = aRule.getNextSibling();
 			} while (!result && aRule != null);
 		
 		log.debug("or method returning " + result);
 		return result;
 	}
 	
 	private boolean not (Node aRule) {
 		// not ::= *rule
 
 		aRule = aRule.getFirstChild();
 		boolean result = (aRule != null) && !rule (aRule);
 		log.debug("not method returning " + result);
 		return result;
 	}
 
 	private boolean predicate (Node aRule) {
 		// predicate ::= equality | greaterOrEqual | lessOrEqual | exists
 
 		boolean result = false;
 		
 		aRule = aRule.getFirstChild();
 		while (aRule != null) {
 			if (aRule.getNodeType() == Node.ELEMENT_NODE) {
 				String predicateName = aRule.getNodeName();
 
 				if (predicateName.equals ("equality")){
 					result = equality (aRule);
 					//break;
 				}
 				else if (predicateName.equals ("greaterOrEqual")){
 					result = greaterOrEqual (aRule);
 					//break;
 				}
 				else if (predicateName.equals ("lessOrEqual")){
 					result = lessOrEqual (aRule);
 					//break;
 				}
 				else if (predicateName.equals ("exists")){
 					result = exists (aRule);
 					//break;
 				}
 			}
 			aRule = aRule.getNextSibling();
 		}
 		
 		log.debug("predicate method returning " + result);
 		return result;
 	}
 
 	/*
 	 * These three productions are not really reflected in the grammar
 	 * as we have chosen  to implement them. CompareOps will return an
 	 * integer value specifying a value comparison between two compar-
 	 * ands. That value is tested to determine whether  the evaluation
 	 * of the specific production is successful.
 	 * 
 	 * Each of these productions save an operand type on a pushdown so
 	 * that the operands are properly interpreted and then restore the
 	 * stack prior to return.
 	 */
 	private boolean equality(Node aRule) {
 		// equality ::= compareOps
 		cacheType (aRule);
 		boolean result = compareOps (aRule) == 0;
 		predicateTypeStack.pop();
 		
 		log.debug("equality method returning " + result);
 		return result;
 	}
 	
 	private boolean greaterOrEqual(Node aRule) {
 		// greaterOrEqual ::= compareOps
 		cacheType (aRule);
 		boolean result = compareOps (aRule) >= 0;
 		predicateTypeStack.pop();
 		return result;
 	}
 	
 	private boolean lessOrEqual(Node aRule) {
 		// lessOrEqual ::= compareOps
 		cacheType (aRule);
 		boolean result = compareOps (aRule) <= 0;
 		predicateTypeStack.pop();
 		return result;
 	}
 
 	private boolean exists (Node aRule) {
 		// this method is pretty awkward since it has to do all of the work
 		// necessary to dereference an operand, but only those operands that
 		// are in the DOM documents.
 		
 		Comparable op = null;
 		predicateTypeStack.push (null);	// we can always look at strings
 
 		aRule = aRule.getFirstChild();
 		while (aRule != null) {
 			if (aRule.getNodeType() == Node.ELEMENT_NODE) {
 				String operandName = aRule.getNodeName();
 
 				if (operandName.equals ("primary")) {
 					op = dereference (aRule, primaryOp);
 					break;
 				}			
 				if (operandName.equals ("secondary")) {
 					op = dereference (aRule, secondaryOp);
 					break;
 				}
 			}
 			aRule = aRule.getNextSibling();
 		}
 		predicateTypeStack.pop();
 		boolean result = op != null && ((String)op).length() > 0;
 		
 		log.debug("exists method returning " + result);
 		return result;
 	}
 	
 	private String getAttribute (Node aRule, String attribName) {
 		// a bit of DOM magic here--find the value of the named attribute
 		// tied to the specified node, if any. Return null on failure.
 
 		NamedNodeMap nodeMap = aRule.getAttributes();
 		Node typeNode = nodeMap.getNamedItem(attribName);
 		return (typeNode == null) ? null : typeNode.getNodeValue();
 	}
 
 	private void cacheType (Node aRule) {
 		predicateTypeStack.push (getAttribute (aRule,"type"));
 	}
 
 	private Integer compute (Node aRule, int operation) {
 		// compute ::= delta | difference
 
 		predicateTypeStack.push (getAttribute (aRule,"type"));
 		
 		Comparable opA = null;
 		Comparable opB = null;
 
 		aRule = aRule.getFirstChild();
 		while (aRule != null && opA == null)
 			if (aRule.getNodeType() == Node.ELEMENT_NODE)
 				opA = operand (aRule);
 			else
 				aRule = aRule.getNextSibling();
 
 		if (opA != null) {
 			aRule = aRule.getNextSibling();
 			while (aRule != null && opB == null)
 				if (aRule.getNodeType() == Node.ELEMENT_NODE)
 					opB = operand (aRule);
 				else
 					aRule = aRule.getNextSibling();
 		}
 
 		/* this mess is, well, pretty messy. Computation only makes sense on
 		 * things that are computable. Assume that things are of the correct
 		 * type--Java exception handling will handle problems.
 		 */
 		
 		String opFormat = (String)predicateTypeStack.pop();
 
 		if (opFormat.equals ("date")) {
 			// dates are converted by number of days since epoch by dividing
 			// the number of milliseconds in a day. We can safely force this
 			// into an int since nobdy is going to care 4GDays from epoch.
 			//
 			opA = new Integer ((int) (((Date)opA).getTime() / 86400000));
 			opB = new Integer ((int) (((Date)opB).getTime() / 86400000));
 		}
 		else
 			// anything else had better be in int format...
 			//
 			if (!opFormat.equals ("int"))
 				throw new UnsupportedOperationException(
 					"XML Engine: Illegal Compute Operand Type:" + opFormat);
 
 		// now that the operands are represented by Integer instances, we can
 		// go ahead and do whatever computation is interesting...
 		//
 		if (operation == OP_DELTA)
 			// absolute value of difference
 			return new Integer (Math.abs (((Integer)opA).intValue() - ((Integer)opB).intValue()));
 
 		if (operation == OP_DIFFERENCE)
 			// simple arithmetic difference
 			return new Integer (((Integer)opA).intValue() - ((Integer)opB).intValue());
 
 		throw new UnsupportedOperationException(
 			"XML Engine: Unsupported op: " + operation);
 	}
 
 	private Comparable operand (Node aRule) {
 		/* operand ::= primary | secondary | value | compute */
 
 		Comparable op = null;
 		String operandName = aRule.getNodeName();
 
 		/* the first couple  of possiblilities are for references into the
 		 * DOM documents, primaryOp and secondaryOp. Dereference will look
 		 * at the actual XPATH and look it up in the appropriate document.
 		 */
 
 		if (operandName.equals ("primary"))
 			op = dereference (aRule, primaryOp);
 		else				
 		if (operandName.equals ("secondary"))
 			op = dereference (aRule, secondaryOp);
 			
 		/* the next "case" is for a hardcoded value. Simply grab this as a
 		 * String (format conversion comes later).
 		 */
 
 		else	
 		if (operandName.equals ("value")) {
 			aRule = aRule.getFirstChild();
 			if (aRule.getNodeType() == Node.TEXT_NODE)
 				op = aRule.getNodeValue().trim();
 		}
 
 		/* the next couple "cases" are for operations that return Integer
 		 * values via the "compute" method.
 		 */
 
 		else
 		if (operandName.equals ("delta"))
 			return compute (aRule, OP_DELTA);
 		else
 		if (operandName.equals ("difference"))
 			return compute (aRule, OP_DIFFERENCE);
 
 		/* format conversion occurs here. We pretty bluntly expect that the
 		 * string we've referenced is capable of being parsed into the form
 		 * requested by the opType.
 		 */
 
 		String opType = (String)predicateTypeStack.peek();
 		if (op != null && opType != null)
 			if (opType.equals ("int"))
 				op = new Integer (op.toString());
 			else
 			if (opType.equals ("date"))
 				try {
 					op = XMLDataTypes.XML2Date (op.toString());
 				}
 				catch (java.text.ParseException e) {
 					log.error ("XML Engine: Date Format Exception");
 					op = new java.util.Date();
 				}
 
 		return op;
 	}
 
 	private int compareOps (Node aRule) {
 		/* compareOps ::= operand operand */
 
 		/* We will find the  operands (referencedData and secondOperand)
 		 * and return an integer that reflects their relative values and
 		 * let the callers determine whether the  comparison meets their
 		 * specific requirements.
 		 */
 
 		Comparable opA = null;
 		Comparable opB = null;
 
 		aRule = aRule.getFirstChild();
 		while (aRule != null && opA == null) {
 			if (aRule.getNodeType() == Node.ELEMENT_NODE)
 				opA = operand (aRule);
 			aRule = aRule.getNextSibling();
 		}
 
 		if (opA != null) {
 			aRule = aRule.getNextSibling();
 			while (aRule != null && opB == null) {
 				if (aRule.getNodeType() == Node.ELEMENT_NODE)
 					opB = operand (aRule);
 				aRule = aRule.getNextSibling();
 			}
 		}
 					
 		// From JavaDocs on Comparable Interface
 		//This implies that x.compareTo(y) must throw an 
 		//exception iff y.compareTo(x) throws an exception	
 		if(opA == null || opB == null) return 0;
 	
 		int result = opA.compareTo (opB);
 		log.debug("compareOps: " + opA.toString() + " " + opB.toString() + " returns: " + result);
 		return result;
 	}
 
 	private String dereference (Node aRule, Document doc)
 	{   // the rule node subordinate text node identifies the URI for the
 		// classification attribute that is to be extracted from the doc.
 		// 
 		aRule = aRule.getFirstChild();
 		if (aRule.getNodeType() == Node.TEXT_NODE) {
 			String uri = aRule.getNodeValue();
 			log.debug("Dereferencing node: " + uri);
 			try {
 				aRule = xPath.selectSingleNode(doc, uri);
 			}
 			catch (org.apache.xpath.XPathException e) {
 				// when the schema allows "0" occurences...
				log.error("Caught XPathException: " + e.getMessage());
				return null;
 			}
 			catch (javax.xml.transform.TransformerException e) {
 				log.error("XML Engine TransformerException", e);
 				return null;
 			}
 			// System.out.println (aRule.getNodeValue());
 			if (aRule != null && aRule.getNodeType() == Node.ELEMENT_NODE) {
 				aRule = aRule.getFirstChild();
 			}
 
 			String value = (aRule == null) ? null : aRule.getNodeValue();
 			return (value == null) ? null : value.trim();
 		}
 		return null;
 	}
 
 	/**
 	 * XMLEngine instances are used to apply a set of rules, expressed in
 	 * accordance  with a XACML schema and provided as an argument to the
 	 * constructor, to sets of DOM-represented XML files.
 	 * 
 	 * @param xmlRuleSrc specifies the name of an XML file that specifies
 	 * 		rules as an XML file conforming to the XACML schema.
 	 */
 	public XMLEngine (String xmlRuleSrc) throws ParsingException{
 
 		predicateTypeStack = new Stack();
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		
 		/*
 		 * I'm having a problem accessing the schema... turning off the
 		 * validation gets me around it... at least, for now.
 		 */
 //		factory.setValidating(true);
 
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			builder.setErrorHandler(
   				new org.xml.sax.ErrorHandler() {
   					// ignore fatal errors (an exception is guaranteed)
       				public void fatalError(SAXParseException exception)
       					throws SAXException {
       				}
 
       				// treat validation errors as fatal
       				public void error(SAXParseException e)
       					throws SAXParseException {
         				throw e;
       				}
 
       				// dump warnings too
       				public void warning(SAXParseException err)
       					throws SAXParseException {
         				System.out.println("** Warning"
           									+ ", line " + err.getLineNumber()
            									+ ", uri " + err.getSystemId());
         				System.out.println("   " + err.getMessage());
       				}
   				}); 
   			
   			ClassLoader loader = this.getClass().getClassLoader();
   			InputStream is = loader.getResourceAsStream(xmlRuleSrc);
   			log.debug("XMLEngine: Loading resource '" + xmlRuleSrc + "'");
   			
   			// This is here to catch when debugging this locally versus 
   			// running the code through the app server.  If the class 
   			// loader cannot find the resource through its means, then
   			// try just a standard File object since it looks things up differently
   			if(is == null){
 	   			rules = builder.parse( new File (xmlRuleSrc) );
   			} else {
 	   			rules = builder.parse( is );
   			}
    		}
    		catch (SAXException sxe) {
  			log.error(sxe);
  			throw new ParsingException(sxe);
     	}
     	catch (ParserConfigurationException pce) {
        		// Parser with specified options can't be built
  			log.error(pce);
  			throw new ParsingException(pce);
     	}
     	catch (IOException ioe) {
        		// I/O error
  			log.error("I/O Exception", ioe);
  			throw new ParsingException(ioe);
     	}
 	}
 	
 	/**
 	 * This method is invoked  to apply the rules specified in the XACML
 	 * file supplied at construction time to the document files supplied
 	 * as this method's arguments. All comparison operations  are of the
 	 * form <primary-document field> op <secondary-document field>.
 	 * 
 	 * @param primary is a DOM representation of the left-side XML
 	 * @param secondary is a DOM representation of the right-side XML
 	 * @return a boolean value of the evaluation of the rule
 	 */
 	public synchronized boolean eval(Document primary, Document secondary) {
 		if (rules == null)
 			throw (new IllegalStateException("No Document"));
 
 		primaryOp = primary;
 		secondaryOp = secondary;
 		xPath = new XPathAPI();
 
 		Node aRule = findEChild (rules.getFirstChild(),"rule");
 		if (aRule != null)
 			aRule = aRule.getFirstChild();
 		return (aRule != null) && rule (aRule);
 	}
 }
