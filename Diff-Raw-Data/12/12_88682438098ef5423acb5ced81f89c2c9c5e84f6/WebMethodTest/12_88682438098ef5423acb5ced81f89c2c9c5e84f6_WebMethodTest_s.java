 package org.snowflake;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 public class WebMethodTest {
 
     @Test
     public void testInitialize() throws Exception {
         WebPage webPage = new WebPage(new TestPage(), "/");
         WebMethod webMethod = webPage.getWebMethodByName("someMethod");
 
         webMethod.initializeArgs(null);
         assertEquals(int.class, webMethod.getHttpArgType());
     }
 
     @Test
     public void testIsWebMethod() {
         Method[] methods = TestPage.class.getMethods();
         Map<String, Method> methodMap = new HashMap<String, Method>();
         for (Method method : methods) {
             methodMap.put(method.getName(), method);
         }
 
         assertTrue(WebMethod.isWebMethod(methodMap.get("index")));
         assertTrue(WebMethod.isWebMethod(methodMap.get("foo")));
         assertTrue(WebMethod.isWebMethod(methodMap.get("someMethod")));
     }
 
     @Test(expected = RuntimeException.class)
    public void testValidateInvalid1() throws Exception {
        WebMethod.validate(InvalidTestPage.class.getMethod("invalid1", Answer.class, Question.class), null);
    }

    @Test(expected = RuntimeException.class)
     public void testValidateInvalid2() throws Exception {
         WebMethod.validate(InvalidTestPage.class.getMethod("invalid2", Question.class, Answer.class, int.class,
                 TestDataObject.class), null);
     }
 
     @Test()
     public void testValidateValidMethods() throws Exception {
         WebMethod.validate(InvalidTestPage.class
                 .getMethod("valid1", Question.class, Answer.class, TestDataObject.class), null);
         WebMethod.validate(InvalidTestPage.class.getMethod("valid2", Question.class, Answer.class, String.class,
                 int.class), null);
         WebMethod.validate(InvalidTestPage.class.getMethod("valid3", String.class, int.class), null);
         WebMethod.validate(InvalidTestPage.class.getMethod("valid4", String.class, int.class, int.class), null);
     }
 }
