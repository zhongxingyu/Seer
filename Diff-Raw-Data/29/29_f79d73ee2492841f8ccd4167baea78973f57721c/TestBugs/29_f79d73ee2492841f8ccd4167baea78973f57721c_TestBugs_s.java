 /*******************************************************************************
  * Copyright (c) 2009 Standards for Technology in Automotive Retail and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     David Carver (STAR) - initial API and implementation
  *     Mukul Gandhi - bug 273719 - improvements to fn:string-length function
  *     Mukul Gandhi - bug 273795 - improvements to fn:substring function
  *     Mukul Gandhi - bug 274471 - improvements to fn:string function
  *     Mukul Gandhi - bug 274725 - improvements to fn:base-uri function.
  *     Mukul Gandhi - bug 274784 - improvements to xs:boolean data type
  *******************************************************************************/
 package org.eclipse.wst.xml.xpath2.processor.test;
 
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.xerces.xs.XSModel;
 import org.eclipse.wst.xml.xpath2.processor.DefaultEvaluator;
 import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
 import org.eclipse.wst.xml.xpath2.processor.Evaluator;
 import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
 import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
 import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
 
 public class TestBugs extends AbstractPsychoPathTest {
 	
 	public void testStringLengthWithElementArg() throws Exception {
 		// Bug 273719
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug273719.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 
 		String xpath = "string-length(x) > 2";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testBug273795Arity2() throws Exception {
 		// Bug 273795
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug273795.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 
 		// test with arity 2
 		String xpath = "substring(x, 3) = 'happy'";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testBug273795Arity3() throws Exception {
 		// Bug 273795
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug273795.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 
 		// test with arity 3
 		String xpath = "substring(x, 3, 4) = 'happ'";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testStringFunctionBug274471() throws Exception {
 		// Bug 274471
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug274471.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 
 		String xpath = "x/string() = 'unhappy'";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testStringLengthFunctionBug274471() throws Exception {
 		// Bug 274471. string-length() with arity 0
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug274471.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 
 		String xpath = "x/string-length() = 7";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testNormalizeSpaceFunctionBug274471() throws Exception {
 		// Bug 274471. normalize-space() with arity 0
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug274471.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 
 		String xpath = "x/normalize-space() = 'unhappy'";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testAnyUriEqualityBug() throws Exception {
 		// Bug 274719
 		// reusing the XML document from another bug
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug274471.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 		
 		String xpath = "xs:anyURI('abc') eq xs:anyURI('abc')";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 
 	public void testBaseUriBug() throws Exception {
 		// Bug 274725 - Mukul Ghandi
 		
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
 		
 		// for testing this bug, we read the XML document from the web. 
 		// this ensures, that base-uri property of DOM is not null.
 		domDoc = docBuilder.parse("http://www.w3schools.com/xml/note.xml");
 
 		// we pass XSModel as null for this test case. Otherwise, we would
 		// get an exception.
 		DynamicContext dc = setupDynamicContext(null);
 		
 		String xpath = "base-uri(note) eq xs:anyURI('http://www.w3schools.com/xml/note.xml')";
 		
 		// please note: The below XPath would also work, with base-uri using arity 0.
 		//String xpath = "note/base-uri() eq xs:anyURI('http://www.w3schools.com/xml/note.xml')";
 		
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 	
 	public void testBooleanTypeBug() throws Exception {
 		// Bug 274784
 		// reusing the XML document from another bug
 		URL fileURL = bundle.getEntry("/bugTestFiles/bug273719.xml");
 		loadDOMDocument(fileURL);
 		
 		// Get XML Schema Information for the Document
 		XSModel schema = getGrammar();
 
 		DynamicContext dc = setupDynamicContext(schema);
 		
 		String xpath = "xs:boolean('1') eq xs:boolean('true')";
 		XPath path = compileXPath(dc, xpath);
 
 		Evaluator eval = new DefaultEvaluator(dc, domDoc);
 		ResultSequence rs = eval.evaluate(path);
 
 		XSBoolean result = (XSBoolean) rs.first();
 
 		String actual = result.string_value();
 
 		assertEquals("true", actual);
 	}
 
 }
