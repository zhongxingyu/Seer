 package memeograph;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * At its core just a bunch of names and values for properties
  * This is a singleton
  * Some handy things:
  *    - Loads a bunch of values from "default.properties"
  *
  *    - Grabs properties from the command line args and those overwrite anything
  *      already defined in "default.properties"
 *
  *    - Comma separated values can be loaded as an array of Strings
  *
  *    - Has the ability to create instances of classes that are pointed to in a
  *    property.
  *        ex) if a property were defined as such: myclass = java.util.ArrayList
  *            then: List list = getClass("myclass", List)
  *            Would create a list using its default empty constructor.
  *        To get a class using getClass(String, Class<E>) it MUST have a public
  *        Constructor that either takes a Config (This object is passed in) or
  *        have an empty constructor.
  *
  * @author Sandro Badame <a href="mailto:sandro@sbcoded.com">sandro&amp;sbcoded.com</a>
  */
 public class Config extends Properties{
  public static final String PORT = "port";
  public static final String SUT_MAIN = "target";
  public static final String VM_OPTIONS = "vm_options";
   public static final String GENERATOR = "generator";
   public static final String RENDERER = "renderer";
  public static final String FILTERS = "filters";
   public static final String VERBOSE = "verbose";
  public static String TRIGGER = "trigger";
 
   //Some of our own properties...
   public static final String PROPERTY_DIVIDER = ",";
 
   private static Config config;
 
   /*
    * Create a config using the String[] as arguments
    */
   public static Config createConfig(String[] args){
     config = new Config(args);
     return config;
   }
 
   /**
    * @return get the only config instance in existence
    */
   public static Config getConfig(){
     return config;
   }
 
   private Config(String[] args){
     try {
       load(getClass().getResourceAsStream("default.properties"));
     } catch (IOException ex) {
       Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
     }
 
     StringBuilder fullargs = new StringBuilder();
     for (String arg : args) { fullargs = fullargs.append(arg).append(" ");}
     putAll(parseArgs(fullargs.toString()));
 
     String vm_args = getProperty(VM_OPTIONS);
     if (vm_args.matches("^\\d+$")) {
       setProperty(PORT, vm_args);
     }else{
       String t = vm_args.substring(vm_args.lastIndexOf(' ')+1,vm_args.length());
       setProperty(SUT_MAIN, t);
 
       setProperty(VM_OPTIONS, vm_args.substring(0,vm_args.lastIndexOf(' ')));
     }
 
     if (isSwitchSet(VERBOSE, false)) {
       System.out.println("Passed in Arguments: " + fullargs);
       LinkedList<String> keyvaluepairs = new LinkedList<String>();
       for (Object object : keySet()) {
         keyvaluepairs.add(object + "=" + getProperty((String) object));
       }
       Collections.sort(keyvaluepairs);
       System.out.println("Printing Config:");
       for (String string : keyvaluepairs) { System.out.println("\t"+string); }
     }
   }
 
   public boolean isSwitchSet(String key, boolean def){
      if (getProperty(key) != null && getProperty(key).isEmpty() == false) {
       return Boolean.valueOf(getProperty(key)).equals(Boolean.TRUE);
      }else{
        return def;
      }
   }
 
   public boolean isPropertySet(String property){
     return getProperty(property) != null && !getProperty(property).isEmpty();
   }
 
   private GraphGenerator generator = null;
   public GraphGenerator getGenerator() {
       if (generator == null) {
         generator = getClass(GENERATOR, GraphGenerator.class);
       }
       return generator;
   }
 
   private GraphRenderer renderer = null;
   public GraphRenderer getRenderer() {
       if (renderer == null) {
         renderer = getClass(RENDERER, GraphRenderer.class);
       }
       return renderer;
   }
 
   public String[] getStringArrayProperty(String propertyname) {
     String value = getProperty(propertyname);
     if (value != null) {
        return value.split(PROPERTY_DIVIDER);
     }
     return new String[0];
   }
 
   /**
    * Returns an instance of the class pointed to by a property.
    * You must either have a public constructor that takes a Config instance
    * as it's only argument or a default constructor.
    *
    * Note: Writing this class makes me hate java exception handling with a passion.
    *       It also convinced me that maybe sometimes reflection can be the
    *       right answer...
    * @param <E>  The class type that you want loaded
    * @param property Name of the property that you want an instance of
    * @param cls The class type that you want the instance returned as
    * @return An instance of the class pointed to by property using either
    *          the public constructor that takes a Config as it's only argument
    *          or the default constructor.
    */
   @SuppressWarnings("unchecked")
   public <E> E getClass(String property, Class<E> cls){
      //I hate java exception handling sometimes...
     try {
       ClassLoader classLoader = getClass().getClassLoader();
       Class<?> loadClass = classLoader.loadClass(getProperty(property));
       E object = null;
       Constructor<?> constructor = null;
       try {
         constructor = loadClass.getConstructor(Config.class);
         try {
           return (E)constructor.newInstance(Config.this);
         } catch (InstantiationException ex) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
         }
       } catch (NoSuchMethodException ex) {
         //Ok maybe, it has a empty constructor... lets try again...
         try {
           constructor = loadClass.getConstructor();
           try {
             return (E) constructor.newInstance();
           } catch (InstantiationException ex1) {
             Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
           } catch (IllegalAccessException ex1) {
             Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
           } catch (IllegalArgumentException ex1) {
             Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
           } catch (InvocationTargetException ex1) {
             Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
           }
         } catch (NoSuchMethodException ex1) {
           System.err.println("There is an error trying to set the property \"" + property + "\" to \"" + cls.getCanonicalName() +
           "\".\nThe problem is that I'm trying to instantiate it, but can't find a valid constructor.\n" +
           "Could you please create one that is public and either takes no arguements, or takes a Config object?");
         } catch (SecurityException ex1) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
         }
       } catch (SecurityException ex) {
         Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
       }
     } catch (ClassNotFoundException ex) {
       System.err.println("I couldn't find the class: \'" + getProperty(property) + "\' in your classpath. It's pointed to by the property: \'" + property + "\'");
     }
     return null;
   }
 
 
   /**
    * Only public so that I can weakly test this...
    * What a horribly hacky parsing function... please kill me.
    * args that start with a '+' get saved as a property, anything else
    * gets passed to the target program.
    */
   public static Map<String, String> parseArgs(String args){
     HashMap<String, String> target = new HashMap<String, String>();
     //Trim off the whitespace!
     args = args.trim();
 
     ArrayList<String> splitupArgs = new ArrayList<String>();
     StringBuilder currentArg = new StringBuilder();
     boolean isInQuotes = false;
     boolean isInArg = false;
     for(int i = 0; i < args.length(); i++){
       char c = args.charAt(i);
 
       if (!isInArg){
         //Not even collecting for an arguement yet
         if (c == ' '){
           continue;
         } else {
           isInArg = true;
           currentArg.append(c);
         }
 
       }else if (isInQuotes){
 
           if (c == '"'){
             isInQuotes = false;
           }else{
             currentArg.append(c);
           }
 
       }else if (c == '"'){
           isInQuotes = true;
       }else if (c == ' '){
         isInArg = false;
         splitupArgs.add(currentArg.toString());
         currentArg.setLength(0);
       }else{
         currentArg.append(c);
       }
 
     }
 
     if (currentArg.length() > 0) splitupArgs.add(currentArg.toString());
 
     StringBuilder target_vm = new StringBuilder();
     for (String arg : splitupArgs) {
        if(arg.startsWith("+")){
            if(arg.contains("=")){
              int i = arg.indexOf("=");
              String name = arg.substring(1, i).trim();
              String value = arg.substring(i+1, arg.length()).trim();
              target.put(name, value);
            }else{
              target.put(arg, Boolean.toString(true));
            }
        }else{
          target_vm.append(arg).append(" ");
        }
     }
     String tvm = target_vm.toString().trim();
     if(!tvm.isEmpty()){
       target.put(VM_OPTIONS, tvm);
     }
     return target;
   }
 
 }
