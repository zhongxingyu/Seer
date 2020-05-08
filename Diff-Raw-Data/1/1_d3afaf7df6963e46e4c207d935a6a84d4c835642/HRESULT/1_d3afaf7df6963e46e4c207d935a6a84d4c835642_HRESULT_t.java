 package org.hampelratte.net.mms.io.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Utility class for M$ HRESULT values
  *
  * @author <a href="mailto:hampelratte@users.berlios.de">hampelratte@users.berlios.de</a>
  */
 public class HRESULT {
     
     private static final Map<Integer, String> hrToHuman = new HashMap<Integer, String>();
     
     static {
         hrToHuman.put(0, "The operation completed successfully.");
         hrToHuman.put(0x80070002, "File not found");
         hrToHuman.put(0x80070057, "Invalid argument");
        hrToHuman.put(0xc00d157f, "Playlist parser not available");
     }
     
     /**
      * Converts a HRESULT value to a human readable string
      * 
      * @param hresult
      * @return a human readable string or the given hresult, if no translation
      *         is available
      */
     public static String hrToHumanReadable(int hresult) {
         String s = hrToHuman.get(hresult);
         return s != null ? s : Integer.toString(hresult) + " (0x" + Integer.toHexString(hresult) + ")";
     }
 }
