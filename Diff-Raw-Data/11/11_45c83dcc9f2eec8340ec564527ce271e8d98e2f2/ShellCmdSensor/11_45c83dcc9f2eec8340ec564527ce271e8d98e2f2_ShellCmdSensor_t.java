 /**
  * User: pizuricv
  * Date: 12/20/12
  */
 package com.ai.myplugin.sensor;
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
 
 @PluginImplementation
 public class ShellCmdSensor implements BNSensorPlugin{
     private static final Log log = LogFactory.getLog(ShellCmdSensor.class);
     private String command;
     private ArrayList<Long> threshold = new ArrayList<Long>();
     private ArrayList<String> states = new ArrayList<String>();
     private static final String parseString = "result=";
     private int exitVal = -1;
    private String result ="";
     private static final String NAME = "ShellCommand";
     private String output = "";
    private AtomicBoolean done = new AtomicBoolean(false);
 
     @Override
     public String[] getRequiredProperties() {
         return new String [] {"threshold", "command"} ;
     }
 
     @Override
     public String[] getRuntimeProperties() {
         return new String[]{};
     }
 
 
     //comma separated list of thresholds
     @Override
     public void setProperty(String s, Object o) {
         if("threshold".endsWith(s)){
             if(o instanceof String)  {
                 String input = (String) o;
                 StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                 int i = 0;
                 states.add("level_"+ i++);
                 while(stringTokenizer.hasMoreElements()){
                     threshold.add(Long.parseLong(stringTokenizer.nextToken()));
                     states.add("level_"+ i++);
                 }
             } else {
                 threshold.add((Long) o);
                 states.add("level_0");
                 states.add("level_1");
             }
             Collections.reverse(threshold);
         } else if ("command".equals(s)){
             command = o.toString();
 
         }
     }
 
     @Override
     public Object getProperty(String s) {
         if("threshold".endsWith(s)){
             return threshold;
         } else if("command".endsWith(s)){
             return command;
         }
         else{
             throw new RuntimeException("Property " + s + " not recognised by " + getName());
         }
     }
 
     @Override
     public String getDescription() {
         return "Shell script, in order to parse the result correctly, add the line in the script in format result=$RES\n" +
                 "example: \"result=5\", and 5 will be compared to the threshold\n" +
                 "the result state is in a format level_$num, ant the number of states is the number_of_thresholds+1";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
         try {
 
             Runtime rt = Runtime.getRuntime();
             Process process = rt.exec(command);
 
             StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), StdType.ERROR);
             StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), StdType.OUTPUT);
 
             errorGobbler.start();
             outputGobbler.start();
 
             exitVal = process.waitFor();
 
             log.debug(getName() + " ExitValue: " + exitVal);
 
             return new TestResult() {
                 {
                     (new Runnable() {
                         //waitForResult is not a timeout for the command itself, but how long you wait before the stream of
                         //output data is processed, should be really fast.
                         private int waitForResult = 3;
                         @Override
                         public void run() {
                            while(!done.get() && waitForResult > 0)
                                 try {
                                     Thread.sleep(1000);
                                     System.out.print(".");
                                     waitForResult --;
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                     break;
                                 }
                         }
                     } ).run();
                 }
 
                 @Override
                 public boolean isSuccess() {
                     return  exitVal == 0 && !("error").equals("command");
                 }
 
                 @Override
                 public String getName() {
                     return "Shell Result";
                 }
 
                 @Override
                 public String getObserverState() {
                     return result;
                 }
 
                 @Override
                 public List<Map<String, Number>> getObserverStates() {
                     return null;
                 }
 
                 @Override
                 public String getRawData() {
                     return output;
                 }
             }  ;
 
         } catch (Throwable t) {
             log.error(t.getLocalizedMessage());
             t.printStackTrace();
             throw new RuntimeException(t);
         }
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public String[] getSupportedStates() {
         return states.toArray(new String[states.size()]);
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
                 throw new RuntimeException("Error executing the script "+ getName() + " : error is "+ line);
             } else{
                 if(line.startsWith(parseString)){
                     output += line;
                     log.debug("Found result " + line);
                     result = mapResult(line.replaceAll(parseString,""));
                 } else{
                     log.debug(line);
                 }
             }
         }
     }
 
     private String mapResult(String result) {
         Long res = Long.parseLong(result);
         int i = states.size() - 1;
         for(Long l : threshold){
             if(res  > l){
                 return "level_" + i;
             }
             i --;
         }
         return "level_0";
     }
 
     @Override
     public void shutdown(TestSessionContext testSessionContext) {
         log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
     }
 }
