 package ca.q0r.kfreqs.app.util;
 
 import com.stericson.RootTools.RootTools;
 import com.stericson.RootTools.execution.CommandCapture;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RootUtils {
     private static String LOG_TAG = RootUtils.class.getName();
 
     private static Boolean rooted = false;
 
     public RootUtils(String tag) {
         LOG_TAG = tag;
         initRoot();
     }
 
     public static void initRoot() {
 
         RootTools.log(LOG_TAG, "Logging Initialized", 3, null);
 
         if (RootTools.isRootAvailable()
                 || RootTools.isAccessGiven()) {
             rooted = true;
         }
     }
 
     public static Boolean isRooted() {
         return rooted;
     }
 
     public static void reset() {
         rooted = false;
 
         RootTools.log(LOG_TAG, "Logging Stopped", 3, null);
     }
 
     public static List<String> executeCommand(String comm) {
         if (!rooted) {
             return null;
         }
 
         final List<String> output = new ArrayList<String>();
 
         CommandCapture command = new CommandCapture(0, comm){
             @Override
             public void output(int id, String line) {
                 output.add(line);
             }
         };
 
         try {
             RootTools.getShell(true).add(command).waitForFinish();
         } catch (Exception ignored) { }
 
         return output;
     }
}
