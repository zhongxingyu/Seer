 package com.closure.utils;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.closure.utils.Logger.Level;
 
 /**
  * Basic Command Line Switches.
  * @author Mohamed Mansour
  */
 public class Switches {
 
     private HashMap<String, String> switches = null;
     private static Switches obj = null;
 
     /**
      * Constructor initilizes switches.s
      */
     private Switches() {
         switches = new HashMap<String, String>();
     }
     
     
     /**
      * Ensure only one storage for switches.
      * @return
      */
     public static Switches getInstance() {
         if (obj == null) {
             obj = new Switches();
         }
         return obj;
     }
     
     /**
      * Validate if the switch exists.
      * @param command_switches
      * @return
      */
     private boolean validate(String[][] required_switches) {
         boolean passed = true;
         for (String[] argument : required_switches) {
             if (!contains(argument[0])) {
                 passed = false;
                 Logger.log(String.format("Switch not found! '%s' - %s", argument[0], argument[1]), Level.ERROR);
             }
         }
         return passed;
     }
     
     /**
      * Parses the argument pair which is of form:
      *  --key=value
      * @param argument
      */
     private void add(String argument) {
         // Add help, no need to parse it out.
         if (argument.equals("--help")) {
             argument = "--help=menu";
         }
 
         // Split wrt to = to get the key and value.
         Pattern pattern = Pattern.compile("^--([a-z|\\-]*)\\=(.*)$");
         Matcher matcher = pattern.matcher(argument);
         
         // Must have two parts in argument.
         if (!matcher.matches() || matcher.groupCount() != 2) {
             return;
         }
         
         // Extract the key and value.
         String key = matcher.group(1);
         String value = matcher.group(2);
 
         // The argument must not nulls.
         if (key == null || value == null) {
             return;
         }
         
         // Check the sizes of each argument.
         if (key.length() < 2 || value.length() <= 0) {
             return;
         }
         
         // Add it to switches.
         add(key, value);
     }
     
     /**
      * Process the switches by checking if help is triggered or required switches is set.
      * @param arguments a list of args for the system.
      * @param required_switches A list of required switches [Switch][Description]
      */
     public void process(String[] args, String[][] required_switches) {
         // Add all arguments to the container.
         for (String argument : args) {
             add(argument.trim());
         }
         
         // If user requested help menu. Bring it up.
         if (contains("help")) {
            Logger.info("Help Menu for Encryption Reverter");
             Logger.info("---------------------------------------------");
             Logger.info(" Required Arguments:");
             for (String[] required_switch : required_switches) {
                 Logger.info("  --" + required_switch[0] + " : " + required_switch[1]);
             }
             System.exit(1);
         }
         
         // Validate required switches.
         if (!validate(required_switches)) {
             Logger.log("Missing switches, cannot continue! Exiting ...", Level.ERROR);
             System.exit(1);
         }
         Logger.debug("Command line arguments validated!");
     }
 
     /**
      * Add the argument into the hashmap.
      * @param key The key that is added to the switch.
      * @param value
      */
     private void add(String key, String value) {
         switches.put(key, value);
     }
 
     /**
      * Get the value for the switch key as a String.
      * @param key
      * @return
      */
     public String get(String key) {
         return get(key, String.class);
     }
     
     /**
      * If a switch exists return status.
      * @param key
      * @return
      */
     public boolean contains(String key) {
         return switches.containsKey(key);
     }
     
     /**
      * Generic retrieval of arguments auto casts.
      * @param <T> The object you want to auto cast.
      * @param key The key you want to query from the dict.
      * @param clazz The type of the switch.
      * @return the value of the switch.
      */
     public <T> T get(String key, Class<T> clazz) {
         String value = switches.get(key);
         Object ret = null;
         
         if (value == null) {
             value = key;
         }
         
         if (clazz == Integer.class) {
             ret = new Integer(value);
         }
         else if (clazz == String.class) {
             ret = value;
         }
         else if (clazz == Character.class) {
             ret = value.charAt(0);
         }
         else if (clazz == BigDecimal.class) {
             ret = new BigDecimal(value);
         }
         return clazz.cast(ret);
     }
     
     /**
      * List of arguments in pretty format.
      * @return a list of switches.
      */
     public String list() {
         return switches.toString();
     }
 }
