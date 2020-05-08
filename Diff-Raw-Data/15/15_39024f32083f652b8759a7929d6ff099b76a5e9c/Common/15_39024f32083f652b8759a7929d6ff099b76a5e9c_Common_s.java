 package org.hackystat.sensor.ant.perforce;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.perforce.api.Debug;
 import com.perforce.api.Env;
 
 /**
  * Convenience class for the examples. This is where the environment gets set up for all the
  * examples. It's extracted here in an attempt to clarify the examples.
  * @author Philip Johnson
  */
 public class Common {
   private static Env env;
   private static Properties prps;
 
   /**
    * Sets up the environment from the p4.properties file. 
    * @return The Env object.
    */
   public static final Env setup() { //NOPMD
     prps = new Properties(System.getProperties());
     BufferedInputStream props = null;
     try {
       // First, try loading from the properties file.
      props = new BufferedInputStream(new FileInputStream("p4.properties"));
       prps.load(props);
       System.setProperties(prps);
       env = new Env(prps);
     }
     catch (IOException ioex) {
       Debug.error("Could not load properties.");
       /*
        * If the properties file failed, we could set the environment manually. Another option would
        * be to simply fail at this point.
        */
       System.exit(-1);
       env = new Env();
       /*
        * env.setPort("public.perforce.com:1666"); env.setUser("david_markley");
        * env.setClient("david_markley-pc"); env.setPassword("mylittlesecret");
        * env.setSystemDrive("D"); env.setSystemRoot("D:\\WINNT"); env.setExecutable("D:\\Program
        * Files\\Perforce\\P4.EXE");
        */
     }
     finally {
       try {
         props.close();
       }
       catch (Exception e) {
         System.out.println("We're hosed.");
       }
     }
     /* Uncomment these lines to turn on stdout debugging info. */
     Debug.setDebugLevel(Debug.VERBOSE);
     Debug.setLogLevel(Debug.LOG_SPLIT);
 
     return env;
   }
 
   /**
    * Gets the properties. 
    * @return The Properties. 
    */
   public Properties getProperties() {
     return prps;
   }
 
   /**
    * Gets the Env object.
    * @return The Env Object. 
    */
   public Env getEnv() {
     return env;
   }
 }
