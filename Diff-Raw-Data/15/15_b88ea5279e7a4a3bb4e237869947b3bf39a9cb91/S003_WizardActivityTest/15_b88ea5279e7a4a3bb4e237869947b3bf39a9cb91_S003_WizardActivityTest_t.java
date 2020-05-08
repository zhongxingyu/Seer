 package nl.coralic.beta.sms.test;
 
 import android.widget.EditText;
 
 public class S003_WizardActivityTest extends WizardActivityTest
 {
     public void testUserPassWrong() throws InterruptedException
     {
 	solo.enterText((EditText)solo.getView(nl.coralic.beta.sms.R.id.txtUsername), "test");
 	solo.enterText((EditText)solo.getView(nl.coralic.beta.sms.R.id.txtPassword), "test");
 	solo.clickOnButton("Done");
 	assertTrue(solo.searchText("Verifying account"));
	solo.waitForDialogToClose(10);
 	assertTrue(solo.searchText("please check your username and password"));
     }
 }
