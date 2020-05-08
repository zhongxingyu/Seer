 /*
  * The MIT License
  *
  * Copyright 2013 René Döbele.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.thedarkfours.ldap.annotation;
 
 import com.thedarkfours.ldap.annotation.testdata.LdapAttributeParserTestObject;
 import com.thedarkfours.ldap.annotation.testdata.LdapAttributeParserTestObjectPrimitives;
 import com.thedarkfours.ldap.reflection.LdapAttributeParser;
 import java.util.Collection;
 import java.util.HashMap;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.Ignore;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author rene
  */
 public class LdapAttributeParserTest {
     
     private HashMap<String, Object> searchResult;
     
     public LdapAttributeParserTest() {
         String[] ocArray = {"test", "top"};
 
         searchResult = new HashMap<String, Object>();
         searchResult.put("objectClass", ocArray);
         searchResult.put("dn", "ou=thedarkfours.com");
         searchResult.put("longValue", "1234");
         searchResult.put("integerValue", "123");
         searchResult.put("boolValue", "true");
         searchResult.put("stringValue", "test");
         searchResult.put("charValue", "a");
         searchResult.put("floatValue", "13423.454");
         searchResult.put("doubleValue", "12345.231");
         searchResult.put("shortValue", "123");
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of buildMethodName method, of class LdapAttributeParser.
      */
     @Test
     public void testBuildMethodName() {
         LdapAttributeParser instance = new LdapAttributeParser();
         System.out.println("buildMethodName");
         String name = "method";
         String expResult = "setMethod";
         String result = instance.buildSetterName(name);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testCreateNewInstanceWrapperClasses() {
         System.out.println("testCreateNewInstanceWrapperClasses");
         LdapAttributeParser instance = new LdapAttributeParser();
         LdapAttributeParserTestObject result = instance.createNewInstance(searchResult, LdapAttributeParserTestObject.class);
         assertNotNull(result);
         assertTrue(result.getBoolValue());
         assertTrue(result.getLongValue().equals(1234L));
         assertTrue(result.getIntegerValue().equals(123));
         assertTrue(result.getShortValue() == 123);
         assertTrue(result.getCharValue().equals('a'));
         assertTrue(result.getStringValue().equals("test"));
         assertTrue(result.getDoubleValue().equals(12345.231d));
         assertTrue(result.getFloatValue().equals(13423.454f));
     }
 
     @Test
     public void testCreateNewInstancePrimitives() {
         System.out.println("testCreateNewInstancePrimitives");
         LdapAttributeParser instance = new LdapAttributeParser();
         LdapAttributeParserTestObjectPrimitives result = instance.createNewInstance(searchResult, LdapAttributeParserTestObjectPrimitives.class);
         assertNotNull(result);
         assertTrue(result.getBoolValue());
         assertEquals(result.getLongValue(), 1234L);
         assertEquals(result.getIntegerValue(), 123);
        assertEquals(result.getShortValue(), (short) 123);
         assertEquals(result.getCharValue(), 'a');
         assertEquals(result.getStringValue(), "test");
         assertEquals(result.getDoubleValue(), 12345.231d, 0d);
         assertEquals(result.getFloatValue(), 13423.454f, 0f);
     }
 
     @Test
     public void isObjectClassSet() {
         System.out.println("isObjectClassSet");
         LdapAttributeParser instance = new LdapAttributeParser();
         LdapAttributeParserTestObjectPrimitives result = instance.createNewInstance(searchResult, LdapAttributeParserTestObjectPrimitives.class);
         Collection<String> objectClass = result.getObjectClass();
 
         assertTrue(objectClass.contains("top"));
         assertTrue(objectClass.contains("test"));
         assertEquals(2, objectClass.size());
     }
 
     @Test
     public void isDnSet() {
         System.out.println("isDnSet");
         LdapAttributeParser instance = new LdapAttributeParser();
         LdapAttributeParserTestObjectPrimitives result = instance.createNewInstance(searchResult, LdapAttributeParserTestObjectPrimitives.class);
 
         assertEquals(searchResult.get("dn"), result.getDn());
     }
 }
