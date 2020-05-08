 package de.twenty11.skysail.ext.osgimonitor.jwebunit;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import net.sourceforge.jwebunit.exception.TestingEngineResponseException;
 import net.sourceforge.jwebunit.junit.WebTester;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class LoginTest {
 
     private WebTester tester;
     private String username = "admin";
     private String password = "skysail";
 
     @Before
     public void setup() {
         Properties prop = new Properties();
 
         try {
             // load a properties file
             prop.load(new FileInputStream("/home/carsten/jwebunit/passwd.txt"));
             username  = prop.getProperty("user");
             password = prop.getProperty("pass");
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
         tester = new WebTester();
         tester.setBaseUrl("http://localhost:2011/osgimonitor/");
         tester.getTestContext().setAuthorization(username, password);
     }
 
     @Test(expected = TestingEngineResponseException.class)
     public void gives_401_for_wrong_password() {
         tester.getTestContext().setAuthorization(username, "wrongOneForSure");
         tester.beginAt("");
 
     }
 
     @Test
     public void returns_html_startpage_when_providing_proper_credentials_and_media_type() {
         tester.beginAt("?media=html");
         // System.out.println(tester.getPageSource());
         tester.assertTitleEquals("Skysail Json Html Viewer");
     }
 
     @Test
     public void returns_json_startpage_when_providing_proper_credentials_and_media_type() {
         tester.beginAt("?media=json");
        tester.assertTitleEquals("Skysail Json Html Viewer");
     }
 }
