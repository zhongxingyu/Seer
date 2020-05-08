 package uk.co.brotherlogic.jarpur;
 
 public class Debug
 {
    private static Boolean debug = null;
 
    private static void loadDebug()
    {
      debug = System.getProperty("jarpur.debug").equalsIgnoreCase("true");
    }
 
    public static Boolean isDebug()
    {
       if (debug == null)
          loadDebug();
       return debug;
    }
 }
