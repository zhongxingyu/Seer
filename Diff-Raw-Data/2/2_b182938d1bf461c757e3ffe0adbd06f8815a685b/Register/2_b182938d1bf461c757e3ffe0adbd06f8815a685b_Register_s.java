 package com.redhat.qe.sm.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsTCMS;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.auto.testopia.Assert;
 import com.redhat.qe.sm.tasks.ProductID;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 public class Register extends Setup {
 	
 	@Test(description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 			dataProvider="invalidRegistrationTest",
 			expectedExceptions={AssertionError.class},
			groups={"sm_stage1"})
 	@ImplementsTCMS(id="41691")
 	public void InvalidRegistration_Test(String username, String password){
 		this.unregisterFromCandlepin();
 		this.registerToCandlepin(username, password);
 	}
 	
 	@Test(description="subscription-manager-cli: register to a Candlepin server using bogus credentials, check for localized strings",
 			dataProvider="invalidRegistrationLocalizedTest",
 			groups={"sm_stage1", "sprint9-script", "only-IT"})
 	public void InvalidRegistrationLocalized_Test(String lang, String expectedMessage){
 		this.unregisterFromCandlepin();
 		this.runRHSMCallAsLang(lang,"register --force --username="+username+getRandInt()+" --password="+password+getRandInt());
 		String stdErr = sshCommandRunner.getStderr();
 		Assert.assertTrue(stdErr.contains(expectedMessage),
 				"Actual localized error message from failed registration: "+stdErr+" as language "+lang+" matches: "+expectedMessage);
 	}
 	
 	@Test(description="subscription-manager-cli: register to a Candlepin server using a user who hasn't accepted terms and conditions, check for localized strings",
 			dataProvider="invalidRegistrationTermsAndConditionsLocalizedTest",
 			groups={"sm_stage1", "sprint9-script", "only-IT"})
 	public void InvalidRegistrationTermsAndConditionsLocalized_Test(String lang, String expectedMessage){
 		this.unregisterFromCandlepin();
 		this.runRHSMCallAsLang(lang, "register --force --username="+tcUnacceptedUsername+" --password="+tcUnacceptedPassword);
 		String stdErr = sshCommandRunner.getStderr();
 		Assert.assertTrue(stdErr.contains(expectedMessage),
 				"Actual localized error message from unaccepted T&Cs registration: "+stdErr+" as language "+lang+" matches: "+expectedMessage);
 	}
 	
 	@Test(description="subscription-manager-cli: register to a Candlepin server",
 			dependsOnGroups={"sm_stage1"},
 			groups={"sm_stage2"},
 			alwaysRun=true)
 	@ImplementsTCMS(id="41677")
 	public void ValidRegistration_Test(){
 		this.registerToCandlepin(username, password);
 	}
 	
 	@Test(description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"sm_stage1", "sprint9-script", "only-IT"},
 			alwaysRun=true)
 	public void ValidRegistrationAutosubscribe_Test(){
 		this.unregisterFromCandlepin();
 		String autoProdCert = "autoProdCert-"+this.getRandInt()+".pem";
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/product/"+autoProdCert);
 		sshCommandRunner.runCommandAndWait("wget -O /etc/pki/product/"+autoProdCert+" "+this.prodCertLocation);
 		this.registerToCandlepinAutosubscribe(username, password);
 		this.refreshSubscriptions();
 		Assert.assertTrue(this.consumedProductIDs.contains(new ProductID(this.prodCertProduct, null)),
 				"Expected product "+this.prodCertProduct+" appears in list --consumed call after autosubscribe");
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/product/"+autoProdCert);
 	}
 	
 	@DataProvider(name="invalidRegistrationTermsAndConditionsLocalizedTest")
 	public Object[][] getInvalidRegistrationTermsAndConditionsLocalizedDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationTermsAndConditionsLocalizedDataAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationTermsAndConditionsLocalizedDataAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		ll.add(Arrays.asList(new Object[]{"en_US.UTF8","You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc"}));
 		ll.add(Arrays.asList(new Object[]{"de_DE.UTF8","Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!"}));
 		return ll;
 	}
 	
 	@DataProvider(name="invalidRegistrationLocalizedTest")
 	public Object[][] getInvalidRegistrationLocalizedDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationLocalizedDataAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationLocalizedDataAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		ll.add(Arrays.asList(new Object[]{"en_US.UTF8","Invalid username or password. To create a login, please visit https://www.redhat.com/wapps/ugc/register.html"}));
 		ll.add(Arrays.asList(new Object[]{"de_DE.UTF8","Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc"}));
 		return ll;
 	}
 	
 	@DataProvider(name="invalidRegistrationTest")
 	public Object[][] getInvalidRegistrationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationDataAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		ll.add(Arrays.asList(new Object[]{"",""}));
 		ll.add(Arrays.asList(new Object[]{username,""}));
 		ll.add(Arrays.asList(new Object[]{"",password}));
 		ll.add(Arrays.asList(new Object[]{username+getRandInt(),password+getRandInt()}));
 		return ll;
 	}
 }
