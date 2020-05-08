 package org.bloodtorrent.functionaltests.scenarios;
 
 import java.util.Random;
 
 import junit.framework.Assert;
 import net.sf.sahi.client.Browser;
 import org.bloodtorrent.functionaltests.pages.RegisterDonor;
 import static com.thoughtworks.twist.core.execution.TwistVerification.verifyEquals;
 
 public class OnRegisterDonorPage {
 
 	private static final double TOLERANCE_FOR_COMPARING_LAT_LONG = 0.003;
 
 	private RegisterDonor registerDonor;
 	
 	private String email = "administrator@bloodtorrent.org";
 	private String firstName = "Jane";
 	private String lastName = "Jung";
 	private String passWord = "12341234";
 	private String confirmPassWord = "12341234";
 	private String address = "Seoul";
 	private String city = "Seoul";
 	private String state = "Orissa";
 	private String cellPhone = "0102003000";
 	private String bloodGroup = "B+";
 	private String distance = "10";
 
 	
 	public OnRegisterDonorPage(Browser browser) {
 		registerDonor = new RegisterDonor(browser);
 	}
 
 	private void setMendatoryFieldsWithoutEmail() {
 		registerDonor.setFirstName(firstName);
 		registerDonor.setLastName(lastName);
 		registerDonor.setPassWord(passWord);
 		registerDonor.setConfirmPassWord(confirmPassWord);
 		registerDonor.setAddress(address);
 		registerDonor.setCity(city);
 		registerDonor.setState(state);
 		registerDonor.setCellPhone(cellPhone);
 		registerDonor.setBloodGroup(bloodGroup);
 		registerDonor.setDistance(distance);
 	}
 	
 	public void fillOutTheValidInformationToMandatoryFields() throws Exception {
 		
 		//TODO: Refactor this to use a more robust mechanism to generate unique email address.
 		
 		Random rd= new Random();
 		int rdEmailcount = rd.nextInt(1000)*2;
 		email = rdEmailcount + "id@samsung.com";
 		
 		registerDonor.setEmail(email);
 		setMendatoryFieldsWithoutEmail();
 	}
 
 
 	public void registerDonor() throws Exception {
 		registerDonor.register();
 	}
 
 	public void cancelRegistration() throws Exception {
 		registerDonor.cancel();
 	}
 
 	public void fillOutTheValidInformationToMendatoryFieldsWithExistingUsersEmailAddress() throws Exception {
 		registerDonor.setEmail(email);
 		setMendatoryFieldsWithoutEmail();
 	}
 
 	public void fillOutTheAddressInformation() throws Exception {
 		fillOutTheAddressInformation("Pune", "Pune", "Maharashtra");
 	}
 	
 	public void fillOutMissingAddressInformation() throws Exception {
 		fillOutTheAddressInformation("", "Pune", "Maharashtra");
 	}
 	
 	public void fillOutTheAddressInformation(String address, String city, String state) throws Exception {
 		registerDonor.setAddress(address);
 		registerDonor.setCity(city);
 		registerDonor.setState(state);
 	}
 
 	public void openMap() throws Exception {
 		registerDonor.openMap();
 	}
 
 	public void verifyTheMessageForMandatoryFieldsAboutAddressInformation() throws Exception {
 		verifyEquals(registerDonor.returnAlertText(), "Please provide the address, city and state before using map.");
 	}
 
 	public void verifyLocationValue() throws Exception {
 		Assert.assertEquals(73.8, registerDonor.getLng(), TOLERANCE_FOR_COMPARING_LAT_LONG);
 		Assert.assertEquals(18.5, registerDonor.getLat(), TOLERANCE_FOR_COMPARING_LAT_LONG);
 	}
 }
