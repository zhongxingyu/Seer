 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.parser.impl;
 
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.customparser.impl.TestAnnoParserClass;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ComplexType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.OperationHolder;
 import org.ebayopensource.turmeric.tools.annoparser.exception.ParserException;
 import org.ebayopensource.turmeric.tools.annoparser.parser.WsdlParser;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * The Class WsdlParserTest.
  *
  * @author sdaripelli
  */
 public class WsdlParserTest {
 
 	private String wsdlPath = null;
 	private WSDLDocInterface wsdlInterface = null; 
 	private WsdlParserImpl parser = null; 
 		
 	/**
 	 * Sets the up.
 	 *
 	 * @throws Exception the exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		wsdlPath = this.getClass().getClassLoader().getResource("ebaySvc.wsdl").toExternalForm();
 		Context.getContext().addParser("maxLength",new TestAnnoParserClass());
 	}
 
 	/**
 	 * Tear down.
 	 *
 	 * @throws Exception the exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test get type op map.
 	 */
 	@Test
 	public void testGetTypeOpMap() {
 		parser = new WsdlParserImpl();
 		String operationName = "testOp";
 		List<OperationHolder> op = new ArrayList<OperationHolder>();
 		OperationHolder operation = new OperationHolder();
 		operation.setName(operationName);
 		op.add(operation);
 		ComplexType ctype = new ComplexType();
 		ctype.setName("test");
 		
 		Map<String, List<OperationHolder>> testMap = new HashMap<String,List<OperationHolder>>();
 		testMap.put(ctype.getName(), op);
 		parser.setTypeOpMap(testMap);
 		
 		Map returnMap = parser.getTypeOpMap();
 		if(returnMap != null && returnMap.containsKey("test")) {
 			List<OperationHolder> operList = (List<OperationHolder>)returnMap.get("test");
 			assertEquals(1,operList.size());
 		}
 		
 	}
 
 	/**
 	 * Test set type op map.
 	 */
 	@Test
 	public void testSetTypeOpMap() {
 		parser = new WsdlParserImpl();
 		String operationName = "testOp";
 		List<OperationHolder> op = new ArrayList<OperationHolder>();
 		OperationHolder operation = new OperationHolder();
 		operation.setName(operationName);
 		op.add(operation);
 		ComplexType ctype = new ComplexType();
 		ctype.setName("test");
 		
 		Map returnMap = parser.getTypeOpMap();
 		//Map<String, List<OperationHolder>> testMap = new HashMap<String,List<OperationHolder>>();
 		returnMap.put(ctype.getName(), op);
 		parser.setTypeOpMap(returnMap);
 		
 		Map testMap = parser.getTypeOpMap();
 		assertEquals(1,testMap.keySet().size());
 		
 		
 	}
 
 	/**
 	 * Test add entry.
 	 */
 	@Test
 	public void testAddEntry() {
 		parser = new WsdlParserImpl();
 		String operationName = "testOp";
 		List<OperationHolder> op = new ArrayList<OperationHolder>();
 		OperationHolder operation = new OperationHolder();
 		operation.setName(operationName);
 		op.add(operation);
 		ComplexType ctype = new ComplexType();
 		ctype.setName("test");
 		parser.addEntry(ctype.getName(), operation);
 		Map testMap = parser.getTypeOpMap();
 		assertEquals(1,testMap.keySet().size());
 		
 	}
 
 	
 
 	/**
 	 * Test parse.
 	 */
 	@Test
 	public void testParse() {
 		parser = new WsdlParserImpl();
 		try {
 			WSDLDocInterface wsdlInterface  = parser.parse(this.wsdlPath);
 			List<OperationHolder> operations = wsdlInterface.getAllOperations();
 			assertEquals(8, operations.size());
 		} catch (ParserException e) {
 			// TODO Auto-generated catch block
 			fail("Failed to parse wsdl file" + e.getMessage());
 		}
 	}
 
 	/**
 	 * Test find all paths.
 	 */
 //	@Test
 //	public void testFindAllPaths() {
 //		try {
 //			if (parser == null) {
 //				parser = new WsdlParserImpl();
 //			}
 //
 //			WSDLDocInterface wsdlInterface = parser.parse(this.wsdlPath);
 //			List<String> paths = parser
 //					.getUsageHiearchyForField("bestMatchBusinessRules");
 //			System.out.println(paths);
 //		} catch (ParserException e) {
 //			// TODO Auto-generated catch block
 //			fail("Failed to parse wsdl file" + e.getMessage());
 //		}
 //	}
 
 //	/**
 //	 * Test get usage hiearchy for field.
 //	 */
 //	@Test
 //	public void testGetUsageHiearchyForField() {
 //		try {
 //			if (parser == null) {
 //				parser = new WsdlParserImpl();
 //			}
 //
 //			WSDLDocInterface wsdlInterface = parser.parse(this.wsdlPath);
 //			List<String> paths = parser
 //					.getUsageHiearchyForField("bestMatchBusinessRules");
 //			System.out.println(paths);
 //		} catch (ParserException e) {
 //			// TODO Auto-generated catch block
 //			fail("Failed to parse wsdl file" + e.getMessage());
 //		}
 //	}
 
 	
 
 }
