 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.common;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.ProgrammingException;
 import org.xins.common.Utils;
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * Tests for class <code>Utils</code>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class UtilsTests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(UtilsTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UtilsTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public UtilsTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void testGetCallingClassAndMethod() {
       // TODO: Test this for Java 1.3 (and down) as well?
       if (Utils.getJavaVersion() >= 1.4) {
          doTestGetCallingClassAndMethod(getClass().getName(), "testGetCallingClassAndMethod");
       }
    }
 
    private void doTestGetCallingClassAndMethod(String expectedClass,
                                                String expectedMethod) {
       String c = Utils.getCallingClass();
       String m = Utils.getCallingMethod();
 
       assertEquals(expectedClass, c);
       assertEquals(expectedMethod, m);
    }
 
    public void testGetNameOfClass() {
       doTestGetNameOfClass("java.lang.Object",       Object.class);
       doTestGetNameOfClass("java.lang.Object",       Object.class);
       doTestGetNameOfClass("java.util.Date",         java.util.Date.class);
       doTestGetNameOfClass("boolean",                Boolean.TYPE);
       doTestGetNameOfClass("char",                   Character.TYPE);
       doTestGetNameOfClass("byte",                   Byte.TYPE);
       doTestGetNameOfClass("short",                  Short.TYPE);
       doTestGetNameOfClass("int",                    Integer.TYPE);
       doTestGetNameOfClass("long",                   Long.TYPE);
       doTestGetNameOfClass("float",                  Float.TYPE);
       doTestGetNameOfClass("double",                 Double.TYPE);
       doTestGetNameOfClass("void",                   Void.TYPE);
       doTestGetNameOfClass("java.lang.Object[]",     Object[].class);
       doTestGetNameOfClass("java.lang.Object[][]",   Object[][].class);
       doTestGetNameOfClass("java.lang.Object[][][]", Object[][][].class);
       doTestGetNameOfClass("int[]",                  int[].class);
       doTestGetNameOfClass("int[][]",                int[][].class);
       doTestGetNameOfClass("int[][][]",              int[][][].class);
       doTestGetNameOfClass("int[][][][]",            int[][][][].class);
    }
 
    private void doTestGetNameOfClass(String expected, Class c) {
       String actual = Utils.getNameOfClass(c);
       String message = "Expected \"" + expected + "\" instead of \"" + actual + "\". Java-reported class name is \"" + c.getName() + "\".";
       assertEquals(message, expected, actual);
    }
 
    public void testGetClassName() {
       assertEquals("java.lang.Object",   Utils.getClassName(new Object()));
       assertEquals("java.util.Date",     Utils.getClassName(new java.util.Date()));
       assertEquals("java.lang.Object[]", Utils.getClassName(new Object[0]));
       assertEquals("java.lang.Object[]", Utils.getClassName(new Object[1]));
       assertEquals("int[]",              Utils.getClassName(new int[0]));
       assertEquals("int[]",              Utils.getClassName(new int[1]));
    }
    
    public void testGetJavaVersion() {
       double version = Utils.getJavaVersion();
       String message = "Incorrect Java version: " + version;
       assertTrue(message, version == 1.3 || version == 1.4 ||
                           version == 1.5 || version == 1.6);
    }
 
    public void testLogProgrammingErrorWithoutArguments() {
       testLogProgrammingErrorWithoutArguments(null, null);
    }
 
    private void testLogProgrammingErrorWithoutArguments(java.lang.String arg1,
                                                         byte[]           arg2) {
      String thisMethod = "testLogProgrammingErrorWithoutArguments("
                        + "java.lang.String,"
                        + "byte[])";
      String thatMethod = "test(java.lang.String)";
 
       InnerClass c = new InnerClass();
       ProgrammingException pe = null;
       String detail = "SomeDetail";
       try {
          c.test(detail);
       } catch (ProgrammingException exception) {
          pe = exception;
       }
 
       assertNotNull(pe);
      assertEquals(detail,                     pe.getDetail()         );
      assertEquals(UtilsTests.class.getName(), pe.getSubjectClass()   );
      assertEquals(thisMethod,                 pe.getSubjectMethod()  );
      assertEquals(InnerClass.class.getName(), pe.getDetectingClass() );
      assertEquals(thatMethod,                 pe.getDetectingMethod());
    }
 
    public void testLogProgrammingErrorWithArguments() {
 
       String c1     = ThreadLocal.class.getName();
       String m1     = "toString()";
       String c2     = getClass().getName();
       String m2     = "testLogProgrammingError()";
       String detail = "blah";
       Exception e = new SecurityException();
       ProgrammingException pe =
          Utils.logProgrammingError(c1, m1, c2, m2, detail, e);
       assertNotNull("Expected logProgrammingError to return a ProgrammingException instance, not null.", pe);
       assertEquals(c1,     pe.getDetectingClass());
       assertEquals(m1,     pe.getDetectingMethod());
       assertEquals(c2,     pe.getSubjectClass());
       assertEquals(m2,     pe.getSubjectMethod());
       assertEquals(detail, pe.getDetail());
 
       assertTrue("Incorrect cause for ProgrammingException.", ExceptionUtils.getCause(pe) == e);
 
       String[] s = new String[] { c1, m1, c2, m2, detail };
       String peMessage = pe.getMessage();
       for (int i = 0; i < s.length; i++) {
          String message = "Expected exception message (\""
                         + peMessage
                         + "\") to contain \""
                         + s[i]
                         + "\".";
          assertTrue(message, pe.getMessage().indexOf(s[i]) >= 0);
       }
 
       // TODO: Test actual logging, too
    }
 
    private class InnerClass {
       private void test(String detail) {
          throw Utils.logProgrammingError(detail);
       }
    }
 }
