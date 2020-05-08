 /**
  * User: pizuricv
  * Date: 12/20/12
  */
 package com.ai.myplugin.sensor;
 import com.ai.bayes.parser.JSONBNParser;
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.EmptyTestResult;
 import com.ai.myplugin.util.RawDataParser;
 import com.ai.myplugin.util.Utils;
 import com.ai.util.resource.NodeSessionParams;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.stringtemplate.v4.ST;
 import twitter4j.internal.org.json.JSONObject;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 @PluginImplementation
 public class NodeJSCommand implements BNSensorPlugin{
     private static final Log log = LogFactory.getLog(NodeJSCommand.class);
     private static final int WAIT_FOR_RESULT = 5;
     private static String CONFIG_FILE = "bn.properties";
     private String javaScriptCommand;
     private String nodePath = "/usr/local/bin/node";
     private String workingDir = "/var/tmp";
     private int exitVal = -1;
     private String result = "";
     private static final String NAME = "NodeJSCommand";
     private AtomicBoolean done = new AtomicBoolean(false);
 
     @Override
     public String[] getRequiredProperties() {
         Set<String> set = RawDataParser.parseKeyArgs((String) getProperty("javaScript"));
         set.add("javaScript");
         String [] ret = new String[set.size()];
         for(int i=0 ; i< set.size(); i++)
             ret[i] = (String) set.toArray()[i];
         return ret;
     }
 
     @Override
     public String[] getRuntimeProperties() {
         Set<String> set = RawDataParser.getRuntimePropertiesFromTemplate((String) getProperty("javaScript"), "runtime_");
         if(set.size() == 0)
             return new String[]{};
         String [] ret = new String[set.size()];
         for(int i=0 ; i< set.size(); i++)
             ret[i] = (String) set.toArray()[i];
         return ret;
     }
 
     @Override
     public void setProperty(String s, Object o) {
         if("javaScript".equals(s)){
             javaScriptCommand = o.toString();
         } else if ("nodePath".equals(s)){
             nodePath = o.toString();
         } else {
             Set<String> set = RawDataParser.parseKeyArgs((String) getProperty("javaScript"));
             if(set.contains(s)){
                 String template = (String) getProperty("javaScript");
                 ST hello = new ST(template);
                 try{
                     Utils.getDouble(o);
                 } catch (Exception e){
                     o = "'" +o.toString() + "'";
                 }
                 hello.add(s, o);
                 setProperty("javaScript" , hello.render());
             }
         }
     }
 
     @Override
     public Object getProperty(String s) {
         if("javaScript".endsWith(s)){
             return javaScriptCommand;
         }
         else{
             throw new RuntimeException("Property " + s + " not recognised by " + getName());
         }
     }
 
     @Override
     public String getDescription() {
         return "Node JS script, result needs to be a TestResult JSON string";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
         Properties properties = new Properties();
         try {
             properties.load(new FileInputStream(CONFIG_FILE));
              if(properties.getProperty("nodePath") != null)
                  nodePath = properties.getProperty("nodePath");
             if(properties.getProperty("nodeDir") != null)
                 workingDir = properties.getProperty("nodeDir");
         } catch (IOException e) {
             e.printStackTrace();
             log.error(e.getLocalizedMessage());
         }
         for(String runtimeProperty : getRuntimeProperties()){
             log.info("set property "+ runtimeProperty + ", for sensor " + getName());
             setProperty(runtimeProperty, testSessionContext.getAttribute(runtimeProperty));
         }
         if(testSessionContext != null && testSessionContext.getAttribute(NodeSessionParams.RAW_DATA) != null){
             Map sessionMap = (Map) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA);
             JSONObject jsonObject = new JSONObject(sessionMap);
             javaScriptCommand = "RAW_STRING = '"+jsonObject.toString() + "';\n" + javaScriptCommand;
         }
 
         File file;
         File dir = new File(workingDir);
         String javascriptFile = "";
 
         try {
             try {
                 javascriptFile =  Long.toString(System.nanoTime()) + "runs.js";
                 file = new File(dir+ File.separator + javascriptFile);
                 BufferedWriter output = new BufferedWriter(new FileWriter(file));
                 output.write(javaScriptCommand);
                 output.close();
             } catch ( IOException e ) {
                 e.printStackTrace();
                 log.error(e.getMessage());
                 return new EmptyTestResult();
             }
 
             ProcessBuilder pb = new ProcessBuilder(nodePath, javascriptFile);
             pb.directory(new File(workingDir));
             Process process = pb.start();
 
             StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), StdType.ERROR);
             StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), StdType.OUTPUT);
 
             errorGobbler.start();
             outputGobbler.start();
 
             exitVal = process.waitFor();
 
             log.debug(getName() + " ExitValue: " + exitVal);
             file.delete();
 
             (new Runnable() {
                 //waitForResult is not a timeout for the javaScriptCommand itself, but how long you wait before the stream of
                 //output data is processed, should be really fast.
                 private int waitForResult = WAIT_FOR_RESULT;
                 @Override
                 public void run() {
                     while(!done.get() && waitForResult > 0)
                         try {
                             Thread.sleep(1000);
                             System.out.print(".");
                             System.out.print(result);
                             waitForResult --;
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                             break;
                         }
                 }
             } ).run();
             return new TestResult() {
                 @Override
                 public boolean isSuccess() {
                     return  exitVal == 0 ;
                 }
 
                 @Override
                 public String getName() {
                     return "node result";
                 }
 
                 @Override
                 public String getObserverState() {
                     try {
                         JSONObject obj = new JSONObject(result);
                         return (String) obj.get("observedState");
                     } catch (Exception e) {
                         e.printStackTrace();
                         log.error(e.getMessage());
                     }
                     return null;
                 }
 
                 @Override
                 public List<Map<String, Number>> getObserverStates() {
                     try {
                         Map <String, Number> map = new ConcurrentHashMap<String, Number>();
                         ArrayList list = new ArrayList();
                         list.add(map);
                         JSONObject obj = new JSONObject(result);
                         JSONObject o  = (JSONObject) obj.get("observedStates");
                         Iterator iterator = o.keys();
                         while(iterator.hasNext()){
                             String state = (String) iterator.next();
                             Double value = Utils.getDouble(o.get(state));
                             map.put(state, value);
                         }
                         return list;
                     } catch (Exception e) {
                         e.printStackTrace();
                         log.error(e.getMessage());
                     }
                     return null;
                 }
 
                 @Override
                 public String getRawData() {
                     try {
                         JSONObject obj = new JSONObject(result);
                         return obj.get("rawData").toString();
                     } catch (Exception e) {
                         e.printStackTrace();
                         log.error(e.getMessage());
                     }
                     return null;
                 }
             }  ;
 
         } catch (Throwable t) {
             log.error(t.getLocalizedMessage());
             t.printStackTrace();
             return new EmptyTestResult();
         }
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public String[] getSupportedStates() {
         return null;
     }
 
     enum StdType {
         ERROR, OUTPUT
     }
 
     private class StreamGobbler extends Thread {
         InputStream is;
         private StdType stdType;
 
         StreamGobbler(InputStream is, StdType type) {
             this.is = is;
             this.stdType = type;
         }
 
         public void run() {
             try {
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr);
                 String line;
                 while ((line = br.readLine()) != null)
                     logLine(line, stdType);
                 done.set(true);
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
 
         private void logLine(String line, StdType type) {
             if(type.equals(StdType.ERROR)){
                 log.error("Error executing the script >" + line);
                 //throw new RuntimeException("Error executing the script "+ getName() + " : error is "+ line);
             } else{
                 result += line;
                 log.info(line);
             }
         }
     }
 
 
     @Override
     public void shutdown(TestSessionContext testSessionContext) {
         log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
     }
 
     public static void main(String [] args) {
         NodeJSCommand nodeJSCommand = new NodeJSCommand();
         String javaScript =  "a = { observedState:\"world\",\n" +
                 "      observedStates: {\n" +
                 "        state1 : 0.5,\n" +
                 "        state2 : 0.5\n" +
                 "      },\n" +
                 "      rawData: {\n" +
                 "       data1: 2,\n" +
                 "       data2: \"hello\"\n" +
                 "     }\n" +
                 "}\n" +
                 "\n" +
                 "console.log(a)" ;
         nodeJSCommand.setProperty("javaScript", javaScript);
 
        // TestResult testResult = nodeJSCommand.execute(null);
        // log.info(testResult.toString());
        // log.info("state " + testResult.getObserverState());
        // log.info("rawData " + testResult.getRawData());
        // log.info("states " + testResult.getObserverStates());
 
         javaScript = "var request = require(\"request\");\n" +
                 "var url = \"http://datatank.gent.be/Onderwijs&Opvoeding/Basisscholen.json\";\n" +
                 "request({\n" +
                 "    url: url,\n" +
                 "    json: true\n" +
                 "}, function (error, response, body) {\n" +
                 "\n" +
                 " if (!error && response.statusCode === 200) {\n" +
                 "    var locations = {\n" +
                 "       observedState: \"Found\",\n" +
                 "       rawData : {\n" +
                 "         locations: body.Basisscholen\n" +
                 "       }\n" +
                 "    };\n" +
                 "    for(location in locations.rawData.locations){\n" +
                 "      locations.rawData.locations[location].longitude = locations.rawData.locations[location].long;\n" +
                 "      locations.rawData.locations[location].latitude = locations.rawData.locations[location].lat;\n" +
                 "   }\n" +
                 "    console.log(JSON.stringify(locations));\n" +
                 "  }\n" +
                 "});";
         nodeJSCommand.setProperty("javaScript", javaScript);
 
         TestResult testResult = nodeJSCommand.execute(null);
         log.info(testResult.toString());
         log.info("state " + testResult.getObserverState());
         log.info("rawData " + testResult.getRawData());
         //log.info("states " + testResult.getObserverStates());
     }
 }
