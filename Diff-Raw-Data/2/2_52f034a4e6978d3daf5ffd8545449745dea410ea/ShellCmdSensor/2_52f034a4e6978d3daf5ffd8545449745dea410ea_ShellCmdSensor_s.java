 /**
  * User: pizuricv
  * Date: 12/20/12
  */
 package com.ai.myplugin;
 import com.ai.bayes.model.BayesianNetwork;
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.StringTokenizer;
 
 @PluginImplementation
 public class ShellCmdSensor implements BNSensorPlugin{
     private String command;
     private ArrayList<Long> threshold = new ArrayList<Long>();
     private ArrayList<String> states = new ArrayList<String>();
     private static final String parseString = "result=";
     private int exitVal = -1;
     private String result = "error";
 
     private ShellCmdSensor(){};
 
     @Override
     public String[] getRequiredProperties() {
         return new String [] {"threshold", "command"} ;
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
     public BNSensorPlugin getNewInstance() {
         return new ShellCmdSensor();
     }
 
     @Override
     public void setNodeName(String s) {
 
     }
 
     @Override
     public void setBayesianNetwork(BayesianNetwork bayesianNetwork) {
 
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         try {
 
             Runtime rt = Runtime.getRuntime();
             Process process = rt.exec(command);
 
             StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), StdType.ERROR);
             StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), StdType.OUTPUT);
 
             errorGobbler.start();
             outputGobbler.start();
 
             exitVal = process.waitFor();
 
             System.out.println(getName() + " ExitValue: " + exitVal);
 
             return new TestResult() {
                 {
                     (new Runnable() {
                         //waitForResult is not a timeout for the command itself, but how long you wait before the stream of
                         //output data is processed, should be really fast.
                         private int waitForResult = 3;
                         @Override
                         public void run() {
                             while("error".equals(result) && waitForResult > 0)
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
                    return  exitVal == 0 && !("error").equals("error");
                 }
 
                 @Override
                 public String getName() {
                     return command;
                 }
 
                 @Override
                 public String getObserverState() {
                     return result;
                 }
             }  ;
 
         } catch (Throwable t) {
             System.err.println(t.getLocalizedMessage());
             t.printStackTrace();
             throw new RuntimeException(t);
         }
     }
 
     @Override
     public String getName() {
         return "Shell sensor";
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
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
 
         private void logLine(String line, StdType type) {
             if(type.equals(StdType.ERROR)){
                 System.err.println("Error executing the script >" + line);
                 throw new RuntimeException("Error executing the script "+ getName() + " : error is "+ line);
             } else{
                 if(line.startsWith(parseString)){
                     System.out.println("Found result " + line);
                     result = mapResult(line.replaceAll(parseString,""));
                 } else{
                     System.out.println(line);
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
 
     public static void main(String []args){
 
         ShellCmdSensor shellExecutor  = (ShellCmdSensor) new ShellCmdSensor().getNewInstance();
         shellExecutor.setProperty("threshold", "1,2,4,12,14");
         shellExecutor.setProperty("command", "C:\\Users\\pizuricv\\MyProjects\\BayesProject\\script\\sensor.bat");
         System.out.println(Arrays.toString(shellExecutor.getSupportedStates()));
         TestResult testResult = shellExecutor.execute(null);
         System.out.println(testResult.getObserverState());
 
         shellExecutor  = (ShellCmdSensor) new ShellCmdSensor().getNewInstance();
         shellExecutor.setProperty("threshold", 13l);
         shellExecutor.setProperty("command", "C:\\Users\\pizuricv\\MyProjects\\BayesProject\\script\\sensor.bat");
         System.out.println(Arrays.toString(shellExecutor.getSupportedStates()));
         testResult = shellExecutor.execute(null);
         System.out.println(testResult.getObserverState());
     }
 
 }
