 package test;
 
 
 import org.junit.Test;
 
 public class LoginPageTest extends BaseTest{
 	@Test
 	public void should_redirect_to_request_url_after_valid_login() {
 		launchKabbadi()
         .loginWithValidCredentials();
 	}
 
     @Test
     public void should_not_require_credential_when_returning_to_kabbadi() {
         launchKabbadi()
         .loginWithValidCredentials()
         .goToGoogle()
         .returnToKabbadi();
     }
     
     @Test
     public void should_not_allow_user_entering_system_with_invalid_credential(){
         launchKabbadi()
          .loginWithInvalidCredentials();
     }
 }
