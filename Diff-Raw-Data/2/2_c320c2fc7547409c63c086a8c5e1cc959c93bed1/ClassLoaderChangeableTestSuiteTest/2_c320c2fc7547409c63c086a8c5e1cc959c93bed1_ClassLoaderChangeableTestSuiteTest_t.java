 package jumble.util;
 
 import java.lang.reflect.Method;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.apache.bcel.util.ClassLoader;
 
 public class ClassLoaderChangeableTestSuiteTest extends TestCase {
 
   /*
    * Test method for 'jumble.util.ClassLoaderChangeableTestSuite.changeClassLoader(ClassLoader)'
    */
   public final void testChangeClassLoader() throws Exception {
     java.lang.ClassLoader cl = new ClassLoader();
     Object o = new ClassLoaderChangeableTestSuite("experiments.JumblerExperimentTest").changeClassLoader(cl);
     //System.out.println(o);   
     Method m = o.getClass().getMethod("testCount", new Class[0]);
     assertEquals(new Integer(2), m.invoke(o, new Object[0]));
     
   }
 
   public static Test suite() {
    TestSuite suite = new TestSuite(ClassLoaderChangeableTestSuiteTest.class);
     return suite;
   }
   
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 }
