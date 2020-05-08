 package org.xtest.test;
 
 import org.junit.Test;
 import org.xtest.XTestInjectorProvider;
 import org.xtest.XTestRunner;
 import org.xtest.types.XTestTypeProvider;
 import org.xtest.xTest.Body;
 
 import com.google.inject.Injector;
 
 /**
  * Container for testing bugs found running xtest to prevent regression
  * 
  * @author Michael Barry
  */
 @SuppressWarnings("restriction")
 public class XTestRegressionTests {
     private static Injector injector = new XTestInjectorProvider().getInjector();
     private static XTestTypeProvider typeProvider = new XTestInjectorProvider().getInjector()
             .getInstance(XTestTypeProvider.class);
 
     @Test
     public void testBug1() throws Exception {
         Body result = XTestRunner.parse("xsuite \"test\": {\n" + "    xtest \"case\": {\n"
                 + "        assert 1 == 1\n" + "      assert 2 ==\n" + "        assert 3 == 3\n"
                 + "    }}  ", injector);
         typeProvider.getCommonReturnType(result, true);
     }
 
 }
