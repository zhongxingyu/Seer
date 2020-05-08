 // JUnit Assert framework can be used for verification
 
 import net.sf.sahi.client.Browser;
 import static junit.framework.Assert.*;
 
 public class UserCreation {
 
 	
 	private Browser browser;
 
 	public UserCreation(Browser browser) {
 		this.browser = browser;
 	}
 
 
 	public void createUserWithPassword(String userEmail, String password)
 			throws Exception {
 		browser.link("Log In").click();
 		browser.link("Create a new account").click();
 		browser.textbox("user[email]").setValue(userEmail);
 		browser.password("user[password]").setValue(password);
 		browser.password("user[password_confirmation]").setValue(password);
 		browser.submit("Create").click();
 	}
 
 
	public void delete(String userId) throws Exception {
		navigateToUserManagement();
		browser.textbox("search[email_contains]").setValue(userId);
		browser.submit("Search").click();
	}

 	public void verifyIfIsCreatedSuccessfully(String userId) throws Exception {
 		navigateToUserManagement();
 		browser.textbox("search[email_contains]").setValue(userId);
 		browser.submit("Search").click();
 		assertTrue(browser.link(userId).exists());
 	}
 	private void navigateToUserManagement() {
 		if(browser.link("Logout").exists())
 			browser.link("Logout").click();
 		browser.link("Log In").click();
 		browser.textbox("user[email]").setValue("spree@example.com");
 		browser.password("user[password]").setValue("spree123");
 		browser.submit("Log In").click();
 		browser.navigateTo(OpenSpreeWebsite.SPREE_DEMO_URL + "/admin");
 		browser.link("Users").click();
 	}
 
 }
