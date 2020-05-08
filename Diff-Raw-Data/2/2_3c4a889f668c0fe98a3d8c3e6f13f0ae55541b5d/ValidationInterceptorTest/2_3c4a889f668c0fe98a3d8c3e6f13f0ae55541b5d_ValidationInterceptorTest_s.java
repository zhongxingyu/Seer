 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cs.wintoosa.controller.interceptor;
 
 import cs.wintoosa.AbstractTest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author jonimake
  */
 public class ValidationInterceptorTest extends AbstractTest {
     
     public ValidationInterceptorTest() {
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
      * Test of preHandle method, of class ValidationInterceptor.
      */
     @Test
     public void testPreHandle() throws Exception {
         System.out.println("preHandle");
         
         String jsondata = "{\"lat\":1.0,\"lon\":2.0,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365}";
        
         MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/log/gps");
         request.setQueryString(jsondata);
         HttpServletResponse response = null;
         Object handler = null;
         ValidationInterceptor instance = new ValidationInterceptor();
        boolean expResult = false;
         boolean result = instance.preHandle(request, response, handler);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
     }
 
     /**
      * Test of postHandle method, of class ValidationInterceptor.
      */
     @Test
     public void testPostHandle() throws Exception {
         System.out.println("postHandle");
         HttpServletRequest request = null;
         HttpServletResponse response = null;
         Object handler = null;
         ModelAndView modelAndView = null;
         ValidationInterceptor instance = new ValidationInterceptor();
        // instance.postHandle(request, response, handler, modelAndView);
         // TODO review the generated test code and remove the default call to fail.
 
     }
 }
