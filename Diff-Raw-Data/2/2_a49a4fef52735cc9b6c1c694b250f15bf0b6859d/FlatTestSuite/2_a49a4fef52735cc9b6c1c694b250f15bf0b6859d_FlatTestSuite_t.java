 package com.reeltwo.jumble.fast;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * A test suite containing other tests, not test suites. If test suites are
  * added, they are flattened. This is useful for ordering tests in Jumble.
  *
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class FlatTestSuite extends TestSuite {
   /**
    * Constructs a new FlatTestSuite. Just calls the parent constructor.
    */
   public FlatTestSuite() {
     super();
   }
 
   /**
    * Constructs a new FlatTestSuite. Checks for a <code>suite()</code> method.
    * If it exists, it uses that, otherwise uses JUnit's reflection method.
    *
    * @param theClass
    *          the class to construct the test suite from.
    */
  public FlatTestSuite(final Class<?> theClass) {
     Method suiteMethod;
     try {
       suiteMethod = theClass.getMethod("suite", new Class[] {});
       if ((suiteMethod.getModifiers() & Modifier.STATIC) == 0) {
         // No nonstatic methods
         suiteMethod = null;
       } else if ((suiteMethod.getModifiers() & Modifier.PUBLIC) == 0) {
         // No nonpublic methods
         suiteMethod = null;
       }
     } catch (NoSuchMethodException e) {
       suiteMethod = null;
     }
 
     if (suiteMethod == null) {
       //No suite method, so we need to construct either a JUnit 3 or JUnit 4 test case.
       addTest(new TestSuite(theClass));
     } else {
       //We have a suite method which will construct the test case.
       try {
         Test suite = (Test) suiteMethod.invoke(null, new Object[] {});
         addTest(suite);
       } catch (InvocationTargetException e) {
         // Should never happen - static method
         throw new RuntimeException("Dumb programmer", e);
       } catch (IllegalAccessException e) {
         // Should never happen - public method
         throw new RuntimeException("Dumb programmer", e);
       }
     }
   }
 
   /**
    * Constructs a new FlatTestSuite. Just calls the parent constructor.
    *
    * @param theClass
    *          the class to construct the test suite from.
    * @param name
    *          the name of the test suite
    */
   public FlatTestSuite(final Class<? extends TestCase> theClass, final String name) {
     super(theClass, name);
   }
 
   /**
    * Constructs a new FlatTestSuite. Just calls the parent constructor.
    *
    * @param name
    *          the neame of the test suite
    */
   public FlatTestSuite(final String name) {
     super(name);
   }
 
   /**
    * Since this class is used mainly for timing tests, the suite hierarchy
    * becomes meaningless, so we want to break up the hierarchy and only get the
    * leaf tests.
    */
   @Override
 public void addTest(final Test t) {
     if (t instanceof TestSuite) {
       TestSuite suite = (TestSuite) t;
 
       for (int i = 0; i < suite.testCount(); i++) {
         addTest(suite.testAt(i));
       }
     } else {
       super.addTest(t);
     }
   }
 
   /**
    * Adds the test suite specified by a class using the <code>suite()</code>
    * method first.
    */
   @Override
 public void addTestSuite(Class testClass) {
     addTest(new FlatTestSuite(testClass));
   }
 }
