 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.client;
 
 import cpw.mods.fml.common.Mod;
 import monnef.core.MonnefCorePlugin;
 import monnef.core.utils.CallerFinder;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class PackageToModIdRegistry {
     private static LinkedHashMap<String, String> packagePathToModId = new LinkedHashMap<String, String>();
 
     public static void registerClassToModId(String packagePath, String modId) {
         if (!searchModId(packagePath).equals("")) {
             throw new RuntimeException(String.format("Overwriting registration! '%s' -> '%s'", packagePath, modId));
         }
         MonnefCorePlugin.Log.printFine(String.format("[%s] Registering : '%s' -> '%s'", PackageToModIdRegistry.class.getSimpleName(), packagePath, modId));
         packagePathToModId.put(packagePath, modId);
     }
 
     public static void registerClassToModId() {
         registerClassToModId(1);
     }
 
     public static void registerClassToModId(int depth) {
         int currDepth = depth + 1;
         String callerPackage = CallerFinder.getCallerPackage(currDepth);
         Class callerModId = CallerFinder.getCallerClass(currDepth);
         Mod ann = (Mod) callerModId.getAnnotation(Mod.class);
         if (ann == null) {
             throw new RuntimeException("Mod class doesn't have proper annotation, incorrect class?");
         }
         registerClassToModId(callerPackage, ann.name());
     }
 
     public static String searchModId(String packagePath) {
         if (packagePathToModId.containsValue(packagePath)) return packagePathToModId.get(packagePath);
         for (Map.Entry<String, String> item : packagePathToModId.entrySet()) {
             if (packagePath.startsWith(item.getKey())) {
                 return item.getValue();
             }
         }
         return "";
     }
 
     public static String searchModIdFromCurrentPackage() {
         return searchModIdFromCurrentPackage(1);
     }
 
     public static String searchModIdFromCurrentPackage(int depth) {
         String caller = CallerFinder.getCallerPackage(depth + 1);
         return PackageToModIdRegistry.searchModId(caller);
     }
 
     public static String searchModId(Class clazz) {
        String packageName = clazz.getPackage().getName();
         return PackageToModIdRegistry.searchModId(packageName);
     }
 }
