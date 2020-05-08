 package edu.wpi.first.wpilibj.templates.debugging;
 
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 import java.util.Hashtable;
 
 /**
  * Main RobotDebugger Class. Always use this instead of pushing directly to
  * SmartDashboard/Console.
  */
 public class RobotDebugger {
 
     private static Hashtable table = new Hashtable();
 
     private static void push(String key, String message, boolean isConsole, boolean isDashboard) {
         if (key == null || message == null) {
             throw new IllegalArgumentException("No Null Arguments");
         }
        if (!message.equals((String) table.get(key))) {
            table.put(key, message);
            if (VstM.Debug.DEBUG) {
                if (VstM.Debug.CONSOLE) {
                     System.out.println("Update: " + key + ": " + message);
                 }
 
                if (VstM.Debug.DASHBOARD) {
                     SmartDashboard.putString("Status:" + key, message);
                 }
             }
         }
     }
 
     /**
      * Push this raw info. This should not be used for status of SubSystems,
      * only for other places. If you have a subsystem it should inherent
      * Debuggable, then you should push it.
      */
     public static void push(DebugInfo di) {
         if (VstM.Debug.DEBUG) {
             push(di.key(), di.message(), di.isConsole(), di.isDashboard());
         }
     }
 
     public static void push(Debuggable d) {
         if (VstM.Debug.DEBUG) {
             d.getStatus().printEach();
         }
     }
 
     public static void push(DebugInfoGroup infoGroup) {
         infoGroup.printEach();
     }
 }
