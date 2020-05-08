 /**
  * 
  */
 package org.microworld.dycapo;
 
 import org.microworlds.scenario.BaseScenario;
 import org.microworlds.scenario.Scenario;
import org.microworlds.scenario.Scenario1;
 
 /**
  * @author riccardo
  * 
  */
 public class Main {
 	public static final String USERNAME = "ozzy";
 	public static final String PASSWORD = "password";
 	public static final String URL = "http://test.dycapo.org/api/persons/";
 
 	public static void main(String[] args) {
 
 		// DycapoHttpClient.callDycapo(DycapoHttpClient.GET, URL, null,
 		// USERNAME,
 		// PASSWORD);
		Scenario scenario = new Scenario1();
 		scenario.setUp();
 		scenario.start();
 	}
 }
