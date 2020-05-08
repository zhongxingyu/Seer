 package com.ai.myplugin.sensor;
 
 /**
  * Created by User: veselin
  * On Date: 12/01/14
  */
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.*;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ParseException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 /**
  * Created by User: veselin
  * On Date: 26/12/13
  */
 @PluginImplementation
 public class AcceleratorSensor implements BNSensorPlugin {
     protected static final Log log = LogFactory.getLog(AcceleratorSensor.class);
     static final String ACCELERATOR_THRESHOLD = "accelerator_threshold";
     static final String RUNTIME_ACCELERATOR = "runtime_accelerator";
 
     Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
 
     String [] states = {"Below", "Above"};
     private static final String NAME = "AcceleratorSensor";
 
     @Override
     public String[] getRequiredProperties() {
         return new String[]{ACCELERATOR_THRESHOLD};
     }
 
     @Override
     public String[] getRuntimeProperties() {
         return new String[]{RUNTIME_ACCELERATOR};
     }
 
     @Override
     public void setProperty(String string, Object obj) {
         if(Arrays.asList(getRequiredProperties()).contains(string)) {
             propertiesMap.put(string, obj);
         } else {
             throw new RuntimeException("Property "+ string + " not in the required settings");
         }
     }
 
     @Override
     public Object getProperty(String string) {
         return propertiesMap.get(string);
     }
 
     @Override
     public String getDescription() {
         return "Accelerator sensor, check if the measurement is above the threshold";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
         if(getProperty(ACCELERATOR_THRESHOLD) == null)
             throw new RuntimeException("distance not set");
 
         Object rt1 = testSessionContext.getAttribute(RUNTIME_ACCELERATOR);
         if(rt1 == null){
             log.warn("no runtime acceleration given");
             return new EmptyTestResult();
         }
         Double runtime_force = Utils.getDouble(rt1);
         log.info("Current force: " + runtime_force);
 
         JSONObject jsonObject = new JSONObject();
 
         Double configuredThreshold = Utils.getDouble(getProperty(ACCELERATOR_THRESHOLD));
 
 
         jsonObject.put(RUNTIME_ACCELERATOR, runtime_force);
         jsonObject.put(ACCELERATOR_THRESHOLD, configuredThreshold);
 
         final String state;
        if(configuredThreshold  > runtime_force)
             state = states[0];
         else
             state = states[1];
 
         final JSONObject finalJsonObject = jsonObject;
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return true;
             }
 
             @Override
             public String getName() {
                 return "Accelerator result";
             }
 
             @Override
             public String getObserverState() {
                 return state;
             }
 
             @Override
             public List<Map<String, Number>> getObserverStates() {
                 return null;
             }
 
             @Override
             public String getRawData() {
                 return finalJsonObject.toJSONString();
             }
         };
 
     }
 
     @Override
     public void shutdown(TestSessionContext testSessionContext) {
 
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public String[] getSupportedStates() {
         return states;
     }
 
     public static void main(String []args) throws ParseException {
         AcceleratorSensor acceleratorSensor = new AcceleratorSensor();
         acceleratorSensor.setProperty(ACCELERATOR_THRESHOLD, 5);
         TestSessionContext testSessionContext = new TestSessionContext(1);
         testSessionContext.setAttribute(RUNTIME_ACCELERATOR, 19.851858);
         TestResult testResult = acceleratorSensor.execute(testSessionContext);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
         testSessionContext.setAttribute(RUNTIME_ACCELERATOR, 1);
         testResult = acceleratorSensor.execute(testSessionContext);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
     }
 }
