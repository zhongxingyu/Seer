 package javamv.sysvar.verify;
 
 import javamv.sysvar.controllers.SysvarController;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.web.servlet.HandlerAdapter;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import static javamv.sysvar.verify.SharedContext.context;
 import static javamv.sysvar.verify.SysvarVerifyTest.*;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNot.not;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VMoskalenko
  * Date: 17.07.12
  * Time: 23:27
  */
 @RunWith(Suite.class)
 @Suite.SuiteClasses({WhenSimpleFormRequest.class, WhenSimpleVerifyRequest.class})
 public class SysvarVerifyTest {
 
     public static class WhenSimpleFormRequest {
 
         private static SysvarController sysvarController;
         private static ModelAndView result;
 
         @BeforeClass
         public static void setupContext() throws Exception {
             HandlerAdapter handler = context.getBean(HandlerAdapter.class);
             HttpServletRequest request = new MockHttpServletRequest("GET", "/home");
             HttpServletResponse response = new MockHttpServletResponse();
             sysvarController = context.getBean(SysvarController.class);
             result = handler.handle(request, response, sysvarController);
         }
 
         @Test
         public void shouldBeProperlyMapped() throws Exception {
             assertThat(result, is(not(nullValue())));
         }
 
         @Test
         public void shouldForwardToProperView() {
             assertThat(result.getViewName(), is("sysvar_home"));
         }
 
     }
 
     public static class WhenSimpleVerifyRequest {
         private static SysvarController sysvarController;
         private static ModelAndView result;
         private static MockHttpServletRequest request;
 
         @BeforeClass
         public static void setupContext() throws Exception {
             HandlerAdapter handler = context.getBean(HandlerAdapter.class);
             request = new MockHttpServletRequest("POST", "/verify");
             request.setParameter("systemVariable", "JAVA_HOME");
             HttpServletResponse response = new MockHttpServletResponse();
             sysvarController = context.getBean(SysvarController.class);
             result = handler.handle(request, response, sysvarController);
         }
 
         @Test
         public void shouldBeProperlyMapped() throws Exception {
             assertThat(result, is(not(nullValue())));
         }
 
         @Test
         public void shouldForwardToProperView() {
             assertThat(result.getViewName(), is("sysvar_home"));
         }
 
         @Test
         public void shouldPopulateResultInRequest() {
            assertThat(request.getAttribute("sysvarValue"), is(not(nullValue())));
         }
     }
 
 }
