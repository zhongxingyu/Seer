 package org.hackystat.sensor.ant.antbuild;
 
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.Task;
 import org.hackystat.sensor.ant.util.LongTimeConverter;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.SensorShellException;
 import org.hackystat.sensorshell.SensorShellProperties;
 import org.hackystat.sensorshell.usermap.SensorShellMap;
 import org.hackystat.sensorshell.usermap.SensorShellMapException;
 
 /**
  * Ant build sensor. It's implemented as an ant listener.  Note that we are unable to access the
  * Ant properties directly. Because of this almost all the Ant methods have a check to see if
  * properties have been retrieved by this sensor. The order of the methods depends on the build
  * so almost all methods need to do this check.
  * 
  * @author (Cedric) Qin Zhang, Julie Ann Sakuda
  */
 public class BuildSensorAntListener implements BuildListener {
   /** Flag indicating if the sensor properties have already been retrieved. */
   private boolean propertiesSet = false;
 
   private boolean debug = false;
 
   private String tool;
   private String toolAccount;
   
   private String buildType;
 
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
   /** The string that prefixes all error messages from this sensor. */
   private String errMsgPrefix = "Hackystat Build Sensor Error: ";
 
   /**
    * Constructs an instance of the build sensor listener. This constructor allows the build sensor
    * to be installed using the -listener ant argument. Unfortunately, using this approach we lose
    * the ability to pass in constructor arguments. This constructor must be public with no
    * parameters.
    */
   public BuildSensorAntListener() {
     // can't do anything here because we don't have the sensor properties yet
   }
 
   /**
    * Prints out debug message.
    * 
    * @param message The debug message.
    */
   private void logDebugMessage(String message) {
     if (this.debug) {
       System.out.println("[Hackystat Build Sensor] " + message);
     }
   }
 
   /**
    * Callback function when ant starts the build. Note that the Ant properties at this point do
    * not have any properties specified with '-D'. Do not try to get the property values here.
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
     if (!propertiesSet) {
       this.setProperties(buildEvent);
     }
 
     // set up/initialize the sensor shell to use
     this.setUpSensorShell();
 
     long endTimeMillis = System.currentTimeMillis();
    String workingDirectory = buildEvent.getProject().getBaseDir().getAbsolutePath();
 
     Map<String, String> keyValMap = new TreeMap<String, String>();
     keyValMap.put("Tool", "Ant");
     XMLGregorianCalendar startTime = LongTimeConverter.convertLongToGregorian(this.startTimeMillis);
     keyValMap.put("Timestamp", startTime.toString());
     keyValMap.put("Resource", workingDirectory);
     keyValMap.put("SensorDataType", "Build");
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
     
     if (this.buildType != null) {
       keyValMap.put("Type", this.buildType);
     }
 
     System.out.print("Sending build result to Hackystat server... ");
     try {
       this.shell.add(keyValMap);
     }
     catch (Exception e) {
       throw new BuildException(errMsgPrefix + "Error adding data to SensorShell.", e);
     }
     try {
       this.shell.send();
     }
     catch (SensorShellException e) {
       throw new BuildException("errMsgPrefix + Error sensor data.", e);
     }
     System.out.println();
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
     if (!propertiesSet) {
       this.setProperties(buildEvent);
     }
     
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
     if (!propertiesSet) {
       this.setProperties(buildEvent);
     }
     
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
     if (!propertiesSet) {
       this.setProperties(buildEvent);
     }
     
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
   
   /** Sets up the sensorshell instance that should be used. */
   private void setUpSensorShell() {
     if (isUsingUserMap()) {
       try {
         // get shell from SensorShellMap/UserMap when user supplies tool and toolAccount.
         SensorShellMap map = new SensorShellMap(this.tool);
         this.shell = map.getUserShell(this.toolAccount);
       }
       catch (SensorShellMapException e) {
         throw new BuildException(errMsgPrefix + "Could not create SensorShellMap", e);
       }
     }
     // User did not supply tool/toolAccount, so do a normal default instantiation of SensorShell.
     else {
       try {
         this.shell = new SensorShell(new SensorShellProperties(), false, "Ant");
       }
       catch (SensorShellException e) {
         throw new BuildException(errMsgPrefix + "Unable to initialize sensor properties.", e);
       }
     }
   }
   
   /**
    * Sets the Hackystat properties using the properties map from Ant.
    * 
    * @param buildEvent Build event to use to retrieve sensor properties.
    */
   @SuppressWarnings("unchecked")
   private void setProperties(BuildEvent buildEvent) {
     Hashtable properties = buildEvent.getProject().getProperties();
 
     Object debugObject = properties.get("hackystat.ant.debug");
     if (debugObject != null) {
       this.debug = Boolean.valueOf((String) debugObject);
     }
 
     Object toolObject = properties.get("hackystat.ant.tool");
     if (toolObject != null) {
       this.tool = (String) toolObject;
     }
 
     Object toolAccountObject = properties.get("hackystat.ant.toolAccount");
     if (toolAccountObject != null) {
       this.toolAccount = (String) toolAccountObject;
     }
     
     Object typeObject = properties.get("hackystat.ant.build.type");
     if (typeObject != null) {
       this.buildType = (String) typeObject;
     }
 
     this.propertiesSet = true;
   }
 }
