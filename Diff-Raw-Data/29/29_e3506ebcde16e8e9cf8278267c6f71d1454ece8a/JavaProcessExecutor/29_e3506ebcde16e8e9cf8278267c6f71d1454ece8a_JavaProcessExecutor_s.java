 package de.skuzzle.polly.process;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * The {@link JavaProcessExecutor} is a factory class which is not instantiable. It
  * has static methods to create a {@link ProcessExecutor} that can create a new java
  * process.
  * 
  * @author Simon
  * @see ProcessExecutor
  */
 public class JavaProcessExecutor {
 
     public static void main(String[] args) throws IOException {
         ProcessExecutor jpe = JavaProcessExecutor.getOsInstance(true);
         jpe.setExecuteIn(new File("D:\\Dokumente\\Programmieren\\Java Workspace\\polly\\polly\\dist"));
         jpe.addCommandsFromString("-jar polly.jar -u false -n pollyv2 -j #debugging");
         System.out.println(jpe.toString());
         
         jpe.setInputHandler(new RedirectStreamHandler("REDIRECT_INPUT"));
         jpe.start();
         System.out.println("fertig");
     }
 
     
     
     public static ProcessExecutor getOsInstance(boolean runInConsole, List<String> commands) {
         File java = null;
         if (ProcessExecutor.IS_WINDOWS) {
             java = getJava("sun.boot.library.path", "javaw");
         }
         
         if (java == null || !java.exists()) {
             java = getJava("java.home", "bin/java");
         }
         if (!java.exists()) {
             throw new UnsupportedOperationException("java home error: " + java);
         }
         ProcessExecutor pe = ProcessExecutor.getOsInstance(runInConsole);
         pe.addCommand(java.getAbsolutePath());
         pe.addCommand(commands);
         return pe;
     }
 
     
     
     private static File getJava(String propertyName, String child) {
         if (ProcessExecutor.IS_WINDOWS) {
             child += ".exe";
         }
         return new File(System.getProperty(propertyName), child);
     }
     
     
     
     /**
      * Creates a ProcessExecutor which will start a new JVM with the same arguments
      * as the current vm.
      * 
      * @param runInConsole
      * @return
      */
     public static ProcessExecutor getCurrentInstance(boolean runInConsole) {
         ProcessExecutor pe = JavaProcessExecutor.getOsInstance(runInConsole);
         List<String> vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
         for (String arg : vmArgs) {
            if (arg.contains("-agentlib")) {
                 pe.addCommand(arg);
             }
         }
         return pe;
     }
     
     
     
     /**
      * Creates a new {@link ProcessExecutor} which can run a new java process. All 
      * commands added to the resulting ProcessExecutor will be arguments for 
      * <code>java</code> or <code>javaw</code>.
      *  
      * @param runInConsole This flag is system dependent. If set to false, the 
      *          ProcessExecutor will create the new process running in background. If set 
      *          to true on Windows machines, the ProcessExecutor opens a new Console 
      *          window running the process. On Unix machines the executor creates a new 
      *          shell process which then executes the command. 
      * @return The {@link ProcessExecutor} instance.
      */
     public static ProcessExecutor getOsInstance(boolean runInConsole) {
         return JavaProcessExecutor.getOsInstance(runInConsole, new LinkedList<String>());
     }
     
     
     private JavaProcessExecutor() {}
 }
