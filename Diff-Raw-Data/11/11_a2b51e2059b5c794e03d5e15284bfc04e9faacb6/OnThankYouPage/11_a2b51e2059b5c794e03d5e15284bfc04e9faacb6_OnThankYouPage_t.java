 package org.bloodtorrent.functionaltests.scenarios;
 
 import static com.thoughtworks.twist.core.execution.TwistVerification.verifyEquals;
 import net.sf.sahi.client.Browser;
 import org.bloodtorrent.functionaltests.pages.ThankYou;
 
 public class OnThankYouPage {
 
 	private ThankYou thankyou;
 
 	public OnThankYouPage(Browser browser) {
 		thankyou = new ThankYou(browser);
 	}
 
	public void verifyThankYouMessage(String message) throws Exception {
		verifyEquals(message, thankyou.getMessageForBloodRequest());
 	}
 
 	public void verifyThankYouMessageForRegister() throws Exception {
 		verifyEquals("Thank you for signing up as a donor. Please go ahead and log in", thankyou.getMessageForRegistering());
 	}
 }
