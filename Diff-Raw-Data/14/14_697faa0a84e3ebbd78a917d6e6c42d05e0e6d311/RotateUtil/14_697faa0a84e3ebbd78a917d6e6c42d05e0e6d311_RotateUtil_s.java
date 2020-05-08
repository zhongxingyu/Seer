 package org.bongomice.rotate;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class RotateUtil {
     
     static final Map<String, Integer> user_tools = new HashMap<String, Integer>();
     
     static final int[] valid_tools = {
         290, 291, 292, 293, 294,  // Hoes
        271, 258, 275, 279, 286,  // Axes 
        270, 257, 277, 278, 285,  // Pickaxes
         280, 262                  // Sticks & Arrows
			
     };
 
     static final int[] valid_targets = {
         44, 126,                                    // Slabs
         66, 28, 27,                                 // Rails
         33, 29,                                     // Pistons
         109, 108, 67, 53, 128, 114, 136, 135, 134,  // Stairs
         17,                                         // Wood            
         323, 63,                                    // Signs
         23, 61, 62,                                 // Dispensers & Furnaces
 		130, 54,									// Enderchests & chest
 		86, 91										// Pumpkin & Jack'O'Lantern
     };
 
     public static boolean isValidTarget(int typeID) {
         
         for(int i : valid_targets){
             if (typeID == i) {
                 return true;
             }
         }
         
         return false;
     }
     
     public static boolean isValidTool(int typeID) {
         
         for(int i : valid_tools){
             if (typeID == i) {
                 return true;
             }
         }
         
         return false;
     }
     
     public static void setUserTool(String username, int toolTypeID) {
         user_tools.put(username, toolTypeID);
     }
 
     public static int getUserTool(String username) {
         if (!user_tools.containsKey(username)) {
             return 1337;
         }
         
         return user_tools.get(username);
     }
 
     public static void resetUserTool(String username) {
         if (!user_tools.containsKey(username)) {
             return;
         }
         
         user_tools.remove(username);
     }
     
 }
