 import java.io.*;
 import org.nlogo.headless.HeadlessWorkspace;
 
 public class RunHeadless {
   
   private static String usage = "Usage: java [java args] -classpath [classpath] RunHeadless.java filename \n" +
     "  filename: Name of the HubNet activity to launch.\n" +
     "Launches a HubNet activity headlessly and keeps it running by repeatedly issuing the 'go' command.\n" +
     "NetLogo's dependencies must be on the classpath.";
     
   public static void main(String[] args) {
     if (args.length != 1)
       error(ErrorCode.Usage, usage);
       
     String filename = args[0];
     
     try {
       HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
       workspace.open(filename);
       workspace.command("startup");
 
       // The HubNet webapp looks for this line in the output to determine
       // the port that the activity is running on.
       System.out.println("Running on port: " + workspace.hubnetManager().getPort());
 
       workspace.command("setup");
       
       // The HubNet webapp looks for this line in the output to determine
       // that launching the activity was successful, and that startup/setup
       // got called without problems.
      // See: app/controllers/Activities.scala
       System.out.println("The model is running...");
       
       while(true) {
         workspace.command("go");
       }
     }
     catch (FileNotFoundException e) {
       error(ErrorCode.InvalidFile, "** Error: File not found: " + filename);
     }
     catch (org.nlogo.api.CompilerException e) {
       error(ErrorCode.ModelError, "** Error:" + 
         "There were errors in the model file (CompilerException):\n" +
           getStackTrace(e) + "\n\n");
     }
     catch (org.nlogo.nvm.EngineException e) {
       error(ErrorCode.ModelError, "** Error:" + 
         "There were errors in the model file (EngineException):\n" +
         e.context.buildRuntimeErrorMessage(e.instruction, e) + "\n" + 
         getStackTrace(e) + "\n\n");
     }
     catch (NoClassDefFoundError e) {
       error(ErrorCode.Usage, e + "\n" + classpathErrorMessage);
     }
     catch (Exception e) {
       error(ErrorCode.UnknownError, "An error occurred while opening the model:\n" +
         e.getMessage() + "\n" + getStackTrace(e) + "\n\n");
     }
   }
   
   private static void error(ErrorCode error, String message) {
     System.err.println(message);
     System.exit(error.code());
   }
   
   private enum ErrorCode {
     UnknownError(1), Usage(101), InvalidFile(102), ModelError(103);
     
     private final int _code;
     public int code() { return _code; }
     ErrorCode(int code) {
       _code = code;
     }
   }
   
   private static String classpathErrorMessage = "The following must be on the classpath in order to run this file: \n" +
     "  * The directory containing the compiled RunHeadless.class file\n" +
     "  * NetLogo.jar\n" +
     "  * scala-library.jar (version 2.9.0-1)\n" +
     "  * NetLogo's dependencies:\n" +
     "     asm-3.3.1.jar           asm-tree-3.3.1.jar    jhotdraw-6.0b1.jar  log4j-1.2.16.jar           parboiled-java-0.11.0.jar  quaqua-7.3.4.jar\n" +
     "     asm-all-3.3.1.jar       asm-util-3.3.1.jar    jmf-2.1.1e.jar      mrjadapter-1.2.jar         pegdown-0.9.1.jar          swing-layout-7.3.4.jar\n" +
     "     asm-analysis-3.3.1.jar  gluegen-rt-1.1.1.jar  jogl-1.1.1.jar      parboiled-core-0.11.0.jar  picocontainer-2.11.1.jar\n";
   
   public static String getStackTrace(Throwable aThrowable) {
     final Writer result = new StringWriter();
     final PrintWriter printWriter = new PrintWriter(result);
     aThrowable.printStackTrace(printWriter);
     return result.toString();
   }
 }
 
