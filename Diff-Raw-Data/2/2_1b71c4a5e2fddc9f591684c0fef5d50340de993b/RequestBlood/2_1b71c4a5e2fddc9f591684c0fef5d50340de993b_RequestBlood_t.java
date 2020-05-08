 package org.bloodtorrent.functionaltests.pages;
 
 import net.sf.sahi.client.Browser;
 import net.sf.sahi.client.BrowserCondition;
 import net.sf.sahi.client.ExecutionException;
 
 public class RequestBlood extends BasePage {
 
 	private static final int DEFAULT_WAIT_MILLISECONDS = 1 * 1000;
 	
 	public RequestBlood(Browser browser){
 		super(browser);
 	}
 
 	public void setFirstName(String patientFirstName) {
 		browser.textbox("firstName").setValue(patientFirstName);		
 	}
 
 	public void setLastName(String patientLastName) {
 		browser.textbox("lastName").setValue(patientLastName);		
 	}
 
 	public void setHospitaAddress(String hospitalAddress) {
 		browser.textarea("hospitalAddress").setValue(hospitalAddress);
 	}
 
 	public void setPatientCity(String patientCity) {
 		browser.textbox("city").setValue(patientCity);
 	}
 
 	public void setPatientState(String patientState) {
 		browser.select("state").choose(patientState);
 	}
 	
 	public void setPhone(String phone) {
 		browser.textbox("phone").setValue(phone);
 	}
 	
 	public void setEmail(String email) {
 		browser.textbox("email").setValue(email);
 	}
 	
 	public void setBirthday(String birthday) {
 		browser.textbox("birthday").setValue(birthday);
 	}
 	
 	public void setBloodVolume(String bloodVolume) {
 		browser.textbox("bloodVolume").setValue(bloodVolume);
 	}
 
 	public void setRequesterTypeToPatient() {		
 		browser.radio("requesterType[1]").click();
 	}	
 	
 	public void register(){
 		browser.byId("register").click();
 	}
 
 	public void cancel() {
		browser.byId("cancel").click();
 	}
 
 	public String getErrorMessage() {
 		BrowserCondition condition = new BrowserCondition(browser) {
 			public boolean test() throws ExecutionException {
 				// initial : hide -> show up
 				// change error : compare the messages
 				
 				boolean isChanged = false;
 				String errorMessage = browser.div("message error").getText();
 				if (errorMessage != null || errorMessage.trim().length() > 0) {
 					isChanged = true;
 				} else {
 					String changedMessage = "";					
 					do {
 						changedMessage = browser.div("message error").getText();
 					} while (!changedMessage.equals(errorMessage));
 					
 					isChanged = true;
 				}
 
 				return isChanged;
 			}
 		};
 		browser.waitFor(condition, DEFAULT_WAIT_MILLISECONDS);		
 		
 		return browser.div("message error").getText(); 
 	}
 }
