 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package ${package}.web.functionaltest;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import java.net.URL;
 
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /** Test that the login page shows a "Bad credentials" message on login
  * failure.
  */
 public class LoginFailTest {
 
   @Test
   public final void testLoginFail() throws Exception {
     URL fullUrl = new URL(SimplePageVerifier.getBaseUrl()
         + "/module/institutional/dashboard.do");
     WebClient webClient = new WebClient();
     HtmlPage loginPage = (HtmlPage) webClient.getPage(fullUrl);
 
     HtmlForm loginForm = loginPage.getFormByName("login");
     assertNotNull(loginForm);
 
     HtmlInput usernameInput = null;
     try {
       usernameInput = loginForm.getInputByName("usernam");
     } catch (ElementNotFoundException ex) {
       usernameInput = loginForm.getInputByName("j_username");
     }
     assertNotNull(usernameInput);
 
     HtmlInput passwordInput = null;
     try {
       passwordInput = loginForm.getInputByName("password");
     } catch (ElementNotFoundException ex) {
       passwordInput = loginForm.getInputByName("j_password");
     }
     assertNotNull(passwordInput);
 
     usernameInput.setValueAttribute("INVALID");
     passwordInput.setValueAttribute("INVALID");
 
     HtmlInput loginButton = loginForm.getInputByName("loginButton");
     HtmlPage errorPage = (HtmlPage) loginButton.click();
     String responseBody = errorPage.asText();
     assertTrue("'Bad credentials' message not found",
         responseBody.matches("(?s).*Bad credentials.*"));
   }
 }
 
