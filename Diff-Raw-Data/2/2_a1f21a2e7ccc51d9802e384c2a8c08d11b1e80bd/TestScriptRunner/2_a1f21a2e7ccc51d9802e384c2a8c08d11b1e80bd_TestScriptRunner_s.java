 package org.amplafi.flow.dsl;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.amplafi.flow.utils.GeneralFlowRequest;
 import org.amplafi.json.JSONArray;
 import org.amplafi.json.JSONException;
 import org.amplafi.json.JSONObject;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Factory;
 import org.testng.annotations.Test;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.AfterTest;
 import org.amplafi.dsl.ScriptRunner;
 import static org.amplafi.dsl.ScriptRunner.*;
 import org.amplafi.dsl.FlowTestBuilder;
 import static org.testng.Assert.*;
 
 /**
  * This is a test for the TestScriptRunner itself, not the wire server
  */
 public class TestScriptRunner {
     
     private ScriptRunner instance = null;
     
     @BeforeTest
     public void setup(){
        instance = new ScriptRunner("http://sandbox.farreach.es:8080/c/ampcb_e1446aa0e3e46427b591fa044c5f51c57989e393b66269140af68709e1da228e/apiv1/");
     }
     
     @AfterTest
     public void tearDown(){
         instance = null;
     }	
     
     @Test 
     public void touchTest() throws Exception{
         
         String script = "println('FFFFFFFFFFFFFFFFFFFFFFFFFFFFFf'); \n " +
                     " request('HelloFlow',['cat':'dog','hippo':'pig']);" ;
         
         instance.runScriptSource(script,"buildExe");
     }
     
     @Test 
     public void testLoadAndRunOneScript(){
         
         instance.loadAndRunOneScript(DEFAULT_SCRIPT_PATH + "/1example.groovy");
         
         
     }
 
     @Test 
     public void testLoadAndRunAllSrcipts(){
         
         instance.loadAndRunAllSrcipts();
         
         
     }	
     
     
     
 
 }
