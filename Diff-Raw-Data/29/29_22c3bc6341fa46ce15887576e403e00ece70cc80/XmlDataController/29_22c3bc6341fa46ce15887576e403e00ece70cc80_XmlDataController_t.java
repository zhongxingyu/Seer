 package org.hackystat.sensor.xmldata;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.hackystat.sensor.xmldata.option.OptionFactory;
 import org.hackystat.sensor.xmldata.option.OptionHandler;
 import org.hackystat.sensor.xmldata.option.Options;
 import org.hackystat.sensorshell.SensorShellProperties;
 import org.hackystat.sensorshell.SensorShellException;
 
 /**
  * The class which parses the command-line arguments specified by the user,
  * validates the created options and their parameters, and executes the options.
  * @author Austen Ito
  * 
  */
 public class XmlDataController {
   /** The class that manages the options created from the user's arguments. */
   private OptionHandler optionHandler = null;
   /** The class which encapsulates the firing of messages. */
   private MessageDelegate messageDelegate = null;
   /** The list of command-line arguments. */
   private List<String> arguments = new ArrayList<String>();
   /** True if all command-line arguments have been parsed correctly. */
   private boolean hasParsed = true;
   /**
    * The mapping of options -> objects that are set during each option's
    * processing.
    */
   private Map<Options, Object> optionMap = new HashMap<Options, Object>();
 
   /**
    * Constructs this controller with the classes that help manage the
    * command-line arguments and message capabilities.
    */
   public XmlDataController() {
     this.optionHandler = new OptionHandler(this);
     this.messageDelegate = new MessageDelegate(this);
     this.optionMap.put(Options.UNIQUE_TSTAMP, Boolean.FALSE);
     this.optionMap.put(Options.SET_RUNTIME, Boolean.FALSE);
   }
 
   /**
    * This method parses the specified command-line arguments and creates
    * options, which can be validated and executed.
    */
   private void processArguments() {
     Map<String, List<String>> optionToArgs = new HashMap<String, List<String>>();
     String currentOption = "";
 
     // Iterate through all -option <arguments>... pairings to build a mapping of
     // option -> arguments.
     for (String argument : this.arguments) {
       // If the current string is an option flag, create a new mapping.
       if (this.optionHandler.isOption(argument)) {
         if (optionToArgs.containsKey(argument)) {
           this.fireMessage("The option, " + argument + ", may not have duplicates.");
           this.hasParsed = false;
           break;
         }
         optionToArgs.put(argument, new ArrayList<String>());
         currentOption = argument;
       }
       // Else, add the argument to the option flag list.
       else {
         List<String> args = optionToArgs.get(currentOption);
         if (args == null) {
           this.fireMessage("The argument, " + argument + ", is not supported.");
           this.hasParsed = false;
           break;
         }
         args.add(argument);
         optionToArgs.put(currentOption, args);
       }
     }
 
     // Next, let's create the options using the command-line information.
     for (Entry<String, List<String>> entry : optionToArgs.entrySet()) {
       String optionName = entry.getKey();
       List<String> optionParams = entry.getValue();
       this.optionHandler.addOption(OptionFactory.getInstance(this, optionName, optionParams));
     }
   }
 
   /**
    * Adds a mapping of the specified option to the specified object. This allows
    * options to use objects associated with other options during the option's
    * execution phase.
    * @param option the option that is associated with an object.
    * @param object the object mapped to an option.
    */
   public void addOptionObject(Options option, Object object) {
     this.optionMap.put(option, object);
   }
 
   /**
    * Returns the object mapped to the specified option. If no object exists,
    * null is returned. It is up to the calling object to determine the type of
    * object returned.
    * @param option the option that is the key of the requested object.
    * @return the object mapped to the specified option.
    */
   public Object getOptionObject(Options option) {
     return this.optionMap.get(option);
   }
 
   /**
    * Processes the command-line arguments and creates the objects that can be
    * executed.
    * @param arguments the specified list of command-line arguments.
    */
   public void processArguments(List<String> arguments) {
     this.arguments = arguments;
     this.optionHandler.clearOptions();
     this.processArguments();
   }
 
   /** Executes all of the options specified by the user */
   public void execute() {
     if (this.hasParsed && this.optionHandler.isOptionsValid()
         && this.optionHandler.hasRequiredOptions()) {
       this.optionHandler.processOptions();
       this.optionHandler.execute();
     }
   }
 
   /**
    * Displays the specified message. The same message is displayed even if the
    * verbose option is enabled.
    * @param message the specified message to display.
    */
   public void fireMessage(String message) {
     this.messageDelegate.fireMessage(message);
   }
 
   /**
    * Displays the specified message if verbose mode is enabled.
    * @param message the specified message to display.
    */
   public void fireVerboseMessage(String message) {
     this.messageDelegate.fireVerboseMessage(message);
   }
 
   /**
    * Displays the specified message is verbose mode is disabled or the verbose
    * message if verbose mode is enabled.
    * @param message the specified message.
    * @param verboseMessage the specified verbose message.
    */
   public void fireMessage(String message, String verboseMessage) {
     this.messageDelegate.fireMessage(message, verboseMessage);
   }
 
   /**
    * Returns the SensorBase host stored in the sensor properties file.
    * @return the SensorBase host string.
    */
   public String getHost() {
     SensorShellProperties properties;
     try {
       properties = new SensorShellProperties();
       return properties.getSensorBaseHost();
     }
     catch (SensorShellException e) {
      String msg = "Problem creating SensorShellProperties: " + e.getMessage();
       this.fireMessage(msg);
     }
     return "";
   }
 }
