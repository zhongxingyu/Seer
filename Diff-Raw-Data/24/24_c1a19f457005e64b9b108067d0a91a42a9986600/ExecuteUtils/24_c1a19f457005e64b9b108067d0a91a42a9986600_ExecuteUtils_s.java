 /**
  * This file is part of the XP-Framework
  *
  * XP-Framework Maven plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.util;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.exec.OS;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 
 import org.apache.maven.plugin.logging.Log;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.exec.Executor;
 import org.apache.commons.exec.LogOutputStream;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.commons.exec.OS;
 
 /**
  * Utility class
  *
  */
 public final class ExecuteUtils {
   public static final String RUNNERS_RESOURCE_PATH= "/net/xp_framework/runners";
 
   /**
    * Utility classes should not have a public or default constructor
    *
    */
   private ExecuteUtils() {
   }
 
   /**
    * Gets the shell environment variables for this process
    *
    * Note that the returned mapping from variable names to values will always be case-sensitive
    * regardless of the platform, i.e. getSystemEnvVars().get("path") and getSystemEnvVars().get("PATH")
    * will in general return different values. However, on platforms with case-insensitive environment
    * variables like Windows, all variable names will be normalized to upper case
    *
    * @return java.util.Map<java.lang.String, java.lang.String> List of environment variables
    * @throws java.io.IOException when I/O errors occur
    */
   @SuppressWarnings("unchecked")
   public static Map<String, String> getEnvVars() throws IOException {
     Map retVal= new HashMap();
 
     Properties systemEnvVars= CommandLineUtils.getSystemEnvVars();
     retVal.putAll(systemEnvVars);
 
     return retVal;
   }
 
   /**
    * Get absolute path of the executable by looking in the "PATH" environment variable
    *
    * E.g.: "ls" -> "/bin/ls"
    *
    * @param  java.lang.String executable Executable name
    * @return java.io.File Executable absolute path
    * @throws java.io.FileNotFoundException when cannot get executable absolute path
    */
   public static File getExecutable(String executable) throws FileNotFoundException {
     String executableFilename= executable;
 
     // Get PATH
     String path;
     try {
       path= ExecuteUtils.getEnvVars().get("PATH");
     } catch (IOException ex) {
       throw new FileNotFoundException("Cannot get PATH");
     }
 
     if (null == path || path.trim().equals("")) {
       throw new FileNotFoundException("PATH is empty");
     }
 
     // Add ".exe" on Windows
     executableFilename+= ExecuteUtils.getExecutableExtension();
 
     // Look in PATH
     for (String elem : StringUtils.split(path, File.pathSeparator)) {
       File file = new File(new File(elem), executableFilename);
       if (file.exists()) {
         return file;
       }
     }
 
     // Executable is not in PATH
     throw new FileNotFoundException("Cannot find executable [" + executableFilename + "] in PATH [" + path + "]");
   }
 



   /**
    * Execute the specified executable with the specified arguments
    *
    * @param  java.io.File executable Executable to run
    * @param  java.util.List<String> argument Executable arguments
    * @param  java.io.File workingDirectory Executable working directory
    * @param  org.apache.maven.plugin.logging.Log cat Log cat
    * @throws java.util.concurrent.ExecutionException when command execution failed
    */
   public static void executeCommand(
     File executable,
     List<String> arguments,
     File workingDirectory,
     Map<String, String> environment,
     final Log cat
   ) throws ExecutionException {
 
     // We don't want to capture the command output; just send it to cat
     LogOutputStream logOutputStream= new LogOutputStream() {
       @Override
       protected void processLine(String line, @SuppressWarnings("unused") int level) {
         if (line.toLowerCase().indexOf("error") > -1) {
           cat.error(line);
         } else if (line.toLowerCase().indexOf("warn") > -1) {
           cat.warn(line);
         } else {
           cat.info(line);
         }
       }
     };
 
     ExecuteUtils.executeCommand(executable, arguments, workingDirectory, environment, cat, logOutputStream);
   }
 

   /**
    * Execute the specified executable with the specified arguments
    *
    * @param  java.io.File executable Executable to run
    * @param  java.util.List<String> argument Executable arguments
    * @param  java.io.File workingDirectory Executable working directory
    * @param  org.apache.maven.plugin.logging.Log cat Log cat
    * @param  org.apache.commons.exec.LogOutputStream logOutputStream
    * @throws java.util.concurrent.ExecutionException when command execution failed
    */
   public static void executeCommand(
     File executable,
     List<String> arguments,
     File workingDirectory,
     Map<String, String> environment,
     final Log cat,
     final LogOutputStream logOutputStream
   ) throws ExecutionException {
 
     // Debug
     if (cat != null) {
       cat.debug("Executable        [" + executable.getAbsolutePath() + "]");
       cat.debug("Arguments         [" + arguments.toString() + "]");
       cat.debug("Working directory [" + workingDirectory + "]");
       cat.debug("Environment vars  [" + environment.toString() + "]");
     }
 
     // Init command line
     CommandLine commandLine= new CommandLine(executable.getAbsolutePath());
 
     // Add arguments
     for (String arg : arguments) {
       // This line makes the resulting command line way shorter (and prettier), but might break a thing or two
        arg = ExecuteUtils.getRelativeToWorkingDirectory(arg, workingDirectory);

      // If complex command line arguments like such as for `xp -e <code>` are given,
      // prevent quoting those...
      boolean quote = false;
      if (arg.contains("'") || arg.contains("\"")) {
        quote = false;
      } else if (arg.contains(" ")) {
        quote = true;
      }
 
      // Escape arguments that contain spaces
      commandLine.addArgument(arg, quote);
     }
 
     Executor executor= new DefaultExecutor();
     executor.setWorkingDirectory(workingDirectory);
 
     //executor.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in));
     executor.setStreamHandler(new PumpStreamHandler(logOutputStream));
 
     // Prepare environment
     Map<String, String> env;
     try {
 
       // Get values from system
       env= ExecuteUtils.getEnvVars();
 
       // Add user-defined values
       env.putAll(environment);
 
     } catch (IOException ex) {
       throw new ExecutionException("Cannot setup execution environment", ex);
     }
 
     // Execute command
     try {
       if (null != cat) cat.debug("Command line      [" + commandLine + "]");
       int retCode= executor.execute(commandLine, env);
       if (null != cat) cat.debug("Return code       [" + retCode + "]");
 
       // Check return code
       //if (retCode != 0) {
       //  throw new ExecutionException("Result code of [" + commandLine + "] execution is [" + retCode + "]");
       //}
 
     } catch (ExecuteException ex) {
       throw new ExecutionException("Command execution failed [" + commandLine + "]", ex);
 
     } catch (IOException ex){
       throw new ExecutionException("I/O Error", ex);
     }
   }
 
   /**
    * Get OS name as string: "win" or "unix"
    *
    * @return java.lang.String
    */
   public static String getOsName() {
     if (OS.isFamilyWindows()) {
       return "win";
     }
     return "unix";
   }
 
   /**
    * Get OS-specific executable extension: ".exe" on windows, "" on unix
    *
    * @return java.lang.String
    */
   public static String getExecutableExtension() {
     if (OS.isFamilyWindows()) {
       return ".exe";
     }
     return "";
   }
 
   /**
    * Save the specified resource to the specified target directory
    *
    * @param  java.lang.String resName
    * @param  java.io.File outputFile
    * @return void
    * @throws java.io.IOException when I/O errors occur
    */
   public static void saveResource(String resName, File outputFile) throws IOException {
     InputStream resStream= ExecuteUtils.class.getResourceAsStream(resName);
 
     // Save to file
     if (null == resStream) {
       throw new IOException("Cannot find resource [" + resName + "]");
     }
     FileUtils.setFileContents(outputFile, resStream);
   }
 
   /**
    * Save the specified runner (stored as resource) to the specified target directory
    *
    * @param  java.lang.String runner E.g.: xp, xpcli, unittest, etc.
    * @param  java.io.File targetDir
    * @return java.io.File
    * @throws java.io.IOException when I/O errors occur
    * @see    net.xp_forge.maven.plugins.xp.util.ExecuteUtils.RUNNERS_RESOURCE_PATH
    */
   public static File saveRunner(String runner, File targetDir) throws IOException {
 
     // Get runner resource name
     String osName    = ExecuteUtils.getOsName();
     String extension = ExecuteUtils.getExecutableExtension();
     String resName   = ExecuteUtils.RUNNERS_RESOURCE_PATH + "/" + osName + "/" + runner + extension;
 
     // Extract runner resource
     File outputFile= new File(targetDir, runner + extension);
     ExecuteUtils.saveResource(resName, outputFile);
     outputFile.setExecutable(true);
 
     return outputFile;
   }
 
   /**
    * Get path relative to this.getWorkingDirectory()
    * All arguments to executeCommand() are filtered by this function
    *
    * Note: most likely str is not filepath at all, hence the .exists() test
    *
    * @param  java.lang.String str
    * @param  java.io.File workingDirectory
    * @return java.lang.String
    */
   public static String getRelativeToWorkingDirectory(String str, File workingDirectory) {
     File file= new File(str);
     if (!file.exists()) return str;
     return ExecuteUtils.getRelativeToWorkingDirectory(file, workingDirectory);
   }
 
   public static String getRelativeToWorkingDirectory(File file, File workingDirectory) {
     return file.getAbsolutePath().replace(workingDirectory.getAbsolutePath() + File.separator, "");
   }
 }
