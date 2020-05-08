 package nl.coralic.beta.sms.betamax;
 
 import static org.junit.Assert.*;
 
 import java.io.FileInputStream;
 import java.util.Properties;
 
 import nl.coralic.beta.sms.betamax.BetamaxHandler;
 import nl.coralic.beta.sms.utils.objects.BetamaxArguments;
 import nl.coralic.beta.sms.utils.objects.Response;
 
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 
 public class BetamaxProviderTest
 {
     private static String username;
     private static String password;
     private static String provider;
     private static String to;
     private static String from;
     private static String text;
     private static String expectedBalance;
     
     
     @BeforeClass
     public static void setUp()
     {
 	Properties prop = new Properties();
 	try
 	{
 	    prop.load(new FileInputStream("realprovider.properties"));
 	}
 	catch (Exception e)
 	{
 	   fail("Please provide a file realprovider.propertiess in the root of the Beta-SMS project. The file should contain an Betamax provider REAL account: \n" +
 	   		"username=real \n" +
 	   		"password=real \n" +
 	   		"provider=www.real.com" +
 	   		"to=00realnumber" +
 	   		"from=can be number registerd or empty" +
 	   		"test=sms text" +
 	   		"expectedBalance=the balance you expect");
 	}
 	username = prop.getProperty("username");
 	password = prop.getProperty("password");
 	provider = prop.getProperty("provider");
 	to = prop.getProperty("to");
 	from = prop.getProperty("from");
 	text = prop.getProperty("text");
 	expectedBalance = prop.getProperty("expectedBalance");
     }
     
     @Ignore
     @Test
     public void getBalance()
     {
 	//Provider always returns 0 even if the password is wrong. We are not able to make distinction between good and wrong
 	String balance = BetamaxHandler.getBalance(new BetamaxArguments(provider, username, password));
 	assertEquals(expectedBalance, balance);
     }
 
    @Ignore
     @Test
     public void sendSms()
     {
 	Response response = BetamaxHandler.sendSMS(new BetamaxArguments(provider, username, password, from, to, text));
 	System.out.println(response.getErrorMessage());
 	assertTrue(response.isResponseOke());
     }
 }
