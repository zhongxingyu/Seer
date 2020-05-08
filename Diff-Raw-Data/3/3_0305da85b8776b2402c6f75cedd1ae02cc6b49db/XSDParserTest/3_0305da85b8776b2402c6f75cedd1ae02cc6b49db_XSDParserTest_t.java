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
 
 import java.io.File;
 import java.net.URL;
 
import org.ebayopensource.turmeric.tools.annoparser.config.ConfigurationReader;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.customparser.impl.TestAnnoParserClass;
 import org.ebayopensource.turmeric.tools.annoparser.parser.XSDParser;
 import org.ebayopensource.turmeric.tools.annoparser.parser.impl.XSDParserImpl;
 import org.ebayopensource.turmeric.tools.annoparser.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.io.InputStream;
 
 /**
  * The Class XSDParserTest.
  *
  * @author srengarajan
  */
 public class XSDParserTest {
 
 	private String xsdPath = null;
 	
 	private XSDDocInterface intf = null;
 	
 
 	/**
 	 * Sets the up.
 	 *
 	 * @throws Exception the exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		this.xsdPath = this.getClass().getClassLoader().getResource("ebaySvc.wsdl").toExternalForm();
		ConfigurationReader.loadDefaultConfiguration();
 		Context.getContext().addParser("maxLength",new TestAnnoParserClass());
 	}
 
 	/**
 	 * Tear down.
 	 *
 	 * @throws Exception the exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		//this.stream.close();
 	}
 
 	
 	/**
 	 * Test method for {@link org.ebayopensource.turmeric.tools.annoparser.parser.impl.XSDParserImpl#parse(java.lang.String)}.
 	 *
 	 * @throws Exception the exception
 	 */
 	@Test
 	public void testParse() throws Exception {
 		
 		try {
 			XSDParser parser = new XSDParserImpl();
 			intf = parser.parse(this.xsdPath);
 			int noOfElements = intf.getAllIndependentElements().size();
 			int delta = 339 - noOfElements;
 			assertEquals("Expected elements not found", 339, noOfElements, delta);
 			System.out.println("no. of elements" + intf.getAllIndependentElements().size());
 		} catch(Throwable e) {
 			fail("failed to parse the document, reason" + e.getMessage());
 		}
 		
 	}
 
 	
 
 
 }
