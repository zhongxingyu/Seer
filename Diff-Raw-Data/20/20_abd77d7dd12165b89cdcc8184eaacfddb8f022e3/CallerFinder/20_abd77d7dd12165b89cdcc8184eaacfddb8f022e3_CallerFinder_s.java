 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.utils;
 
 import static monnef.core.MonnefCorePlugin.Log;
 
 public class CallerFinder {
     private final static MySecurityManager manager = new MySecurityManager();
     public static final int DEPTH_OFFSET = 2;
 
     public static String getCallerClassName(int callStackDepth) {
         return manager.getCallerClassName(callStackDepth + DEPTH_OFFSET);
     }
 
     public static String getCallerPackage(int depth) {
         Class callerClass = getCallerClass(depth + 1);
         if (callerClass == null) {
             manager.dumpStack();
             throw new RuntimeException("Caller class is null, depth was " + depth + ".");
         }
         String classCallerName = callerClass.getName();
 
        return classCallerName.substring(0, classCallerName.lastIndexOf('.'));
     }
 
     public static String getMyPackage() {
         return getCallerPackage(1);
     }
 
     public static Class getCallerClass(int depth) {
         try {
             return Class.forName(getCallerClassName(depth + 1));
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     static class MySecurityManager extends SecurityManager {
         public String getCallerClassName(int callStackDepth) {
             return getClassContext()[callStackDepth].getName();
         }
 
         public void dumpStack() {
             Class[] ctx = getClassContext();
             Log.printInfo(String.format("Printing current context - %d items:", ctx.length));
             for (int i = 0; i < ctx.length; i++) {
                 Log.printInfo(String.format("%d: %s", i, ctx[i].getName()));
             }
         }
     }
 }
