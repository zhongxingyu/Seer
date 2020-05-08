 package com.photon.phresco.api;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugins.model.Mojos;
 
 
 public interface DynamicParameter {
 	
 	String KEY_APP_INFO = "applicationInfo";
 	String KEY_BUILD_NO = "buildNumber";
 	String KEY_ENVIRONMENT = "environmentName";
 	String KEY_SHOW_SETTINGS = "showSettings";
 	String KEY_DATABASE = "dataBase";
 	String KEY_SERVER = "server";
 	String KEY_WEBSERVICE = "webService";
 	String KEY_TEST_AGAINST = "testAgainst";
 	String KEY_CUSTOMER_ID = "customerId";
 	String KEY_MOJO =  "mojo";
 	String KEY_GOAL = "goal";
	String KEY_DEVICE_TYPE = "deviceType";
	String KEY_TRIGGER_SIMULATOR = "triggerSimulator";
 
     public Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues getValues(Map<String, Object> map) throws IOException, ParserConfigurationException, SAXException, ConfigurationException, PhrescoException;
 }
