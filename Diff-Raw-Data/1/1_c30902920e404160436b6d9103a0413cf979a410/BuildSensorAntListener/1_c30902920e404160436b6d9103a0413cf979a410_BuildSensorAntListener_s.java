 package org.hackystat.sensor.ant.antbuild;
 
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 import java.util.Map;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.Task;
 import org.hackystat.sensor.ant.util.LongTimeConverter;
 import org.hackystat.sensorshell.SensorProperties;
 import org.hackystat.sensorshell.SensorPropertiesException;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.usermap.SensorShellMap;
 import org.hackystat.sensorshell.usermap.SensorShellMapException;
 
 /**
  * Ant build sensor. It's implemented as an ant listener.
  * 
  * @author (Cedric) Qin Zhang, Julie Ann Sakuda
  */
 public class BuildSensorAntListener implements BuildListener {
 
   private boolean debug = false;
 
   private String tool;
   private String toolAccount;
 
   private SensorShell shell = null;
 
   /** Stack of task names. */
   private Stack<String> taskNameStack = new Stack<String>();
 
   // the following two stacks are always synchronized.
   /** List of messages from single tasks. */
   private Stack<ArrayList<String>> messagesStack = new Stack<ArrayList<String>>();
   /** Stack of target names. */
   private ArrayList<String> targetNameStack = new ArrayList<String>();
 
   /** Build start time. */
   private long startTimeMillis;
   /** The last Ant target executed. */
   private String lastTargetName = "Unknown";
 
   /**
    * Constructs an instance of the build sensor listener. This constructor allows the build sensor
    * to be installed using the -listener ant argument. Unfortunately, using this approach we lose
    * the ability to pass in constructor arguments. This constructor must be public with no
    * parameters.
    */
   public BuildSensorAntListener() {
     // Check for debug, tool, and tool account properties
     String debugString = System.getProperty("hackystat.ant.debug");
     if (debugString != null) {
       this.debug = Boolean.valueOf(debugString);
     }
 
     this.tool = System.getProperty("hackystat.ant.tool");
     this.toolAccount = System.getProperty("hackystat.ant.toolAccount");
 
     if (isUsingUserMap()) {
       try {
         // get shell from SensorShellMap/UserMap
         SensorShellMap map = new SensorShellMap(this.tool);
         this.shell = map.getUserShell(this.toolAccount);
       }
       catch (SensorShellMapException e) {
         throw new BuildException(e.getMessage(), e);
       }
     }
     else {
       try {
         // use value in sensor.properties
         SensorProperties sensorProps = new SensorProperties();
         this.shell = new SensorShell(sensorProps, false, "Ant");
       }
       catch (SensorPropertiesException e) {
         throw new BuildException("Unable to initialize sensor properties.", e);
       }
     }
   }
 
   /**
    * Logs debug message.
    * 
    * @param message The debug message.
    */
   private void logDebugMessage(String message) {
     if (this.debug) {
       System.out.print("[Ant Build Sensor DEBUG] ");
       System.out.println(message);
     }
   }
 
   /**
    * Callback function when ant starts the build. Not used in this class.
    * 
    * @param buildEvent The build event object.
    */
   public void buildStarted(BuildEvent buildEvent) {
     this.startTimeMillis = System.currentTimeMillis();
   }
 
   /**
    * Callback function when ant finishes the build. It sends out the build sensor data if build
    * sensor is enabled.
    * 
    * @param buildEvent The build event object.
    */
   public void buildFinished(BuildEvent buildEvent) {
     long endTimeMillis = System.currentTimeMillis();
     String workingDirectory = buildEvent.getProject().getBaseDir().getAbsolutePath();
 
     Map<String, String> keyValMap = new TreeMap<String, String>();
     keyValMap.put("Tool", "Ant");
     XMLGregorianCalendar startTime = LongTimeConverter.convertLongToGregorian(this.startTimeMillis);
     keyValMap.put("Timestamp", startTime.toString());
     keyValMap.put("Resource", workingDirectory);
     keyValMap.put("SensorDataType", "Build");
    keyValMap.put("DevEvent-Type", "Build");
     keyValMap.put("Target", this.lastTargetName);
 
     // put result in the map
     if (buildEvent.getException() == null) {
       keyValMap.put("Result", "Success");
     }
     else {
       keyValMap.put("Result", "Failure");
     }
 
     // optional
     XMLGregorianCalendar endTime = LongTimeConverter.convertLongToGregorian(endTimeMillis);
     keyValMap.put("EndTime", endTime.toString());
 
     System.out.print("Sending build result to Hackystat server... ");
     try {
       this.shell.add(keyValMap);
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new BuildException("Error occurred adding data to SensorShell.", e);
     }
 
     if (this.shell.send() > 0) {
       System.out.println("Done!");
     }
     else {
       System.out.println("\nUnable to send build result.");
       System.out.println("\nBuild result is cached offline. It will be sent next time.");
     }
   }
 
   /**
    * Callback function when ant starts a build target. It's used to record last ant target invoked.
    * 
    * @param buildEvent The build event object.
    */
   public void targetStarted(BuildEvent buildEvent) {
     String targetName = buildEvent.getTarget().getName();
     this.targetNameStack.add(targetName);
     this.logDebugMessage("TargetStarted - " + targetName);
   }
 
   /**
    * Callback function when ant finishes a build target.
    * 
    * @param buildEvent The build event object.
    */
   public void targetFinished(BuildEvent buildEvent) {
     String targetName = buildEvent.getTarget().getName();
     this.logDebugMessage("TargetFinished - " + targetName);
 
     int size = this.targetNameStack.size();
     if (size > 0) {
       this.targetNameStack.remove(size - 1);
     }
 
     // TODO: This scheme to get top level build target works ok when build is successful
     // But if build failed, you only get the last target invoked by ANT.
     // This is a possible problem to handle in the next release.
     if (targetName != null) {
       this.lastTargetName = targetName;
     }
   }
 
   /**
    * Callback function when ant starts a build task.
    * 
    * @param buildEvent The build event object.
    */
   public void taskStarted(BuildEvent buildEvent) {
     // System.out.println("==>TaskStarted: " + buildEvent.getTask().getTaskName());
     Task task = buildEvent.getTask();
     String taskName = task.getTaskName();
     this.logDebugMessage("TaskStarted - " + taskName);
     this.taskNameStack.push(taskName);
     this.messagesStack.push(new ArrayList<String>());
   }
 
   /**
    * Callback function when ant finishes a build task.
    * 
    * @param buildEvent The build event object.
    */
   public void taskFinished(BuildEvent buildEvent) {
     String taskName = buildEvent.getTask().getTaskName();
     this.logDebugMessage("TaskFinished - " + taskName + ";  error = "
         + (buildEvent.getException() != null));
 
     // when you install the listener in a task, you will never be able to get that TaskStart event.
     // The first event you hear is TaskFinished, this is to handle this special case.
     if (this.taskNameStack.isEmpty()) {
       return;
     }
 
     this.taskNameStack.pop();
   }
 
   /**
    * Callback function when ant logs a message.
    * 
    * @param buildEvent The build event object.
    */
   public void messageLogged(BuildEvent buildEvent) {
     try {
       Task task = buildEvent.getTask();
       if (task != null && !this.taskNameStack.isEmpty()
           && task.getTaskName().equals(this.taskNameStack.peek())) {
         String message = buildEvent.getMessage();
         if (message != null) {
           ArrayList<String> list = this.messagesStack.peek();
           list.add(message);
         }
       }
     }
     catch (EmptyStackException ex) {
       // This shouldn't actually happen
       System.out.println("Error: internal stack structure has nothing to peek at.");
     }
   }
 
   /**
    * Gets whether or not this sensor instance is using a mapping in the UserMap.
    * 
    * @return Returns true of the tool and tool account are set, otherwise false.
    */
   private boolean isUsingUserMap() {
     return (this.tool != null && this.toolAccount != null);
   }
 }
