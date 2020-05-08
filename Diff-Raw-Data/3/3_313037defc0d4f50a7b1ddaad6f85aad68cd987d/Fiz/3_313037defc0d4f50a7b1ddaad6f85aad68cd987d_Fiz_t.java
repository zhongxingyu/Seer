 package org.fiz.tools;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.fiz.Dataset;
 import org.fiz.StringUtil;
 import org.fiz.YamlDataset;
 
 import javax.swing.filechooser.FileSystemView;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 
 /**
  * This class implements the command-line tool used to manage Fiz installations,
  * Fiz web applications and extensions on a computer.
  * For usage information on how the tool works, use the built-in help commands.
  * e.g. Supply {@code help} as an argument when invoking the tool.
  */
 public class Fiz {
 
     /**
      * A ToolError is thrown whenever there is an error processing a command.
      */
     public static class ToolError extends Error {
         // The command during whose execution the error occurred.
         private Command command = Command.unknown;
 
         /**
          * Constructs a new ToolError with a given message. The value of
          * {@code command} indicates executing which command caused the
          * error.
          *  
          * @param command       The command whose execution caused the error.
          * @param message       Detailed information about the problem.
          */
         public ToolError(Command command, String message) {
             super(message);
             this.command = command;
         }
 
         /**
          * Returns the value of {@code command} field if it was set in the
          * constructor.
          *
          * @return     The command that caused this eror.
          */
         public Command getCommand() {
             return command;
         }
 
         /**
          * Sets the value of the {@code command} field.
          *
          * @param command    The value to set.
          */
         public void setCommand(Command command) {
             this.command = command;
         }
     }
 
     /**
      * An enumeration of all the available commands.
      */
     public enum Command {
         checkCore     ("check core"),
         checkExt      ("check ext"),
         createApp     ("create app"),
         createExt     ("create ext"),
         help          ("help"),
         installCore   ("install core"),
         installExt    ("install ext"),
         upgrade       ("upgrade"),
         version       ("version"),
         unknown       ("unknown");
 
         // The command string that this object corresponds to.
         protected String commandStr;
 
         /**
          * Constructs a new Command.
          * 
          * @param commandStr   The corresponding command string.
          */
         Command(String commandStr) {
             this.commandStr = commandStr;
         }
 
         /**
          * Returns the value of the command as a string.
          *
          * @return   The value of the command.
          */
         public String toString() {
             return commandStr;
         }
     }
 
     /**
      * Defines different log levels for the tool
      */
     protected static class LogLevel {
         final static int verbose = 1; // Print verbose information.
         final static int normal = 2;  // Print summary information.
         final static int quiet = 3;   // Print nothing except errors.
     }
 
     /**
      * This type is used by some methods to identify the 3 different kinds of
      * objects the tool works with: Fiz core installations, Fiz applications
      * and Fiz extensions.
      */
     protected enum DirType {
         core,
         application,
         extension
     }
 
     // The home directory of the default Fiz installation.
     protected String fizHome;
     // The url of the server hosting the Fiz installers and extensions.
     protected String serverUrl;
     // The current logging level for the tool.
     protected int logLevel = LogLevel.normal;
 
     /**
      * Entry method for the tool.
      * 
      * @param args    The command-line arguments used when invoking the tool.
      */
     public static void main(String[] args) {
         (new Fiz()).executeCommand(args);
     }
 
     /**
      * Executes commands specified in {@code args}.
      * 
      * @param args    Commmand-line arguments used when invoking the tool.
      */
     protected void executeCommand(String[] args) {
         try {
             // First extract any global options that were specified.
             ArrayList<String> argList = parseGlobalOptions(args);
 
             if (argList.size() == 0) {
                 // No commands were specified; just print general help
                 // information and return.
                 printHelp();
                 return;
             }
 
             Command command = parseCommand(argList);
             switch (command) {
                 case checkCore:
                     executeCheckCoreCommand(argList);
                     break;
                 case checkExt:
                     executeCheckExtCommand(argList);
                     break;
                 case createApp:
                     executeCreateAppCommand(argList);
                     break;
                 case createExt:
                     executeCreateExtCommand(argList);
                     break;
                 case help:
                     executeHelpCommand(argList);
                     break;
                 case installCore:
                     executeInstallCoreCommand(argList);
                     break;
                 case installExt:
                     executeInstallExtCommand(argList);
                     break;
                 case upgrade:
                     executeUpgradeCommand(argList);
                     break;
                 case version:
                     executeVersionCommand(argList);
                     break;
             }
         }
         catch (ToolError e) {
             logError(e.getMessage());
             Command c = e.getCommand();
             if (c == Command.unknown) {
                 logError("\nType 'fiz help' for usage information.");
             } else {
                 logError("\nType 'fiz help " + c + "' for usage information.");
             }
             System.exit(1);
         }
     }
 
     /**
      * Execute the "help" command.
      *
      * @param argList     The command-line arguments.
      */
     protected void executeHelpCommand(ArrayList<String> argList) {
         if (argList.size() == 0) {
             // No commands were specified; just print general help
             // information.
             printHelp();
             return;
         }
 
         // Print help information for each of the commands that were specified.
         while (argList.size() > 0) {
             Command command = parseCommand(argList);
             switch (command) {
                 case help:
                     printHelp();
                     break;
                 case checkCore:
                     printCheckCoreHelp();
                     break;
                 case checkExt:
                     printCheckExtHelp();
                     break;
                 case createApp:
                     printCreateAppHelp();
                     break;
                 case createExt:
                     printCreateExtHelp();
                     break;
                 case installCore:
                     printInstallCoreHelp();
                     break;
                 case installExt:
                     printInstallExtHelp();
                     break;
                 case upgrade:
                     printUpgradeHelp();
                     break;
                 case version:
                     printVersionHelp();
                     break;
             }
         }
     }
 
     /**
      * Execute the "create app" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeCreateAppCommand(ArrayList<String> argList) {
         if (argList.size() != 1) {
             throw new ToolError(Command.createApp, "invalid usage");
         }
 
         String appName = argList.remove(0);
         File appDir = new File(appName);
         String appDirPath = appDir.getAbsolutePath();
         // Check if the directory already exists.
         if (appDir.exists()) {
             throw new ToolError(Command.createApp,
                     "a directory with this name already exists");
         }
         String buildFileName = fizHome + File.separator + "antscripts" +
                 File.separator + "create-app.xml";
         HashMap<String, String> properties = new HashMap<String, String>();
         properties.put("fizhome", fizHome);
         properties.put("destdir", appDirPath);
         invokeAnt(buildFileName, "create", properties);
 
         log(LogLevel.normal, "created new Fiz application at " +
                 getCanonicalPath(appDirPath));
     }
 
     /**
      * Execute the "create ext" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeCreateExtCommand(ArrayList<String> argList) {
         if (argList.size() != 1) {
             throw new ToolError(Command.createExt, "invalid usage");
         }
 
         String extName = argList.remove(0);
         File extDir = new File(extName);
         String extDirPath = extDir.getAbsolutePath();
         // Check if the directory already exists.
         if (extDir.exists()) {
             throw new ToolError(Command.createExt,
                     "a directory with this name already exists");
         }
         String buildFileName = fizHome + File.separator + "antscripts" +
                 File.separator + "create-ext.xml";
         HashMap<String, String> properties = new HashMap<String, String>();
         properties.put("fizhome", fizHome);
         properties.put("destdir", extDirPath);
         invokeAnt(buildFileName, "create", properties);
 
         log(LogLevel.normal, "created new extension at " +
                 getCanonicalPath(extDirPath));        
     }
 
     /**
      * Execute the "check core" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeCheckCoreCommand(ArrayList<String> argList) {
         HashMap<String, String> options =
                 parseOptions(Command.checkCore, argList, "v");
 
         // This command does not take any other arguments.
         if (argList.size() != 0) {
             throw new ToolError(Command.checkCore, "invalid usage");
         }
         
         // Verify and process the options that were specified.
         String versionSince = null;
         for (String key : options.keySet()) {
             if (key.equals("v")) {
                 versionSince = options.get(key);
             } else {
                 throw new ToolError(Command.checkCore, "invalid option: " + key);
             }
         }
 
         if (versionSince == null) {
             // If a version was not specified in the command line, use the
             // version of the Fiz installation.
             versionSince = checkFizVersion(fizHome);
         }
 
         String urlStr = serverUrl + "/fiz/fizCore/versionInfo";
         if (versionSince != null) {
             urlStr = urlStr + "?since=" + versionSince;
         }
 
         String result;
         try {
             result = openUrl(urlStr);
         }
         catch (Exception e) {
             throw new ToolError(Command.checkCore, "error occurred while " +
                     "fetching information from server " + urlStr);
         }
 
         if (result.equals("")) {
             // There are no new versions of Fiz.
             log(LogLevel.normal, "there are no newer versions of Fiz available");
             return;
         }
 
         ArrayList<Dataset> versionDataList = 
                 YamlDataset.newStringInstance(result).getChildren("record");
         log(LogLevel.quiet, "new releases since Fiz version " +
                 versionSince + ":");
         // Print out version information for each new new version.
         for(Dataset versionData : versionDataList) {
             log(LogLevel.quiet, versionData.get("version"));
             String description = versionData.check("short_description");
             if (description != null) {
                 log(LogLevel.verbose, "    " + description);
             }
         }
     }
 
     /**
      * Execute the "check ext" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeCheckExtCommand(ArrayList<String> argList) {
         HashMap<String, String> options =
                 parseOptions(Command.checkExt, argList, "v");
 
         // Check that an extension name was specified.
         if (argList.size() != 1) {
             throw new ToolError(Command.checkExt, "invalid usage");
         }
 
         String extName = argList.remove(0);
 
         // Verify and process the options that were specified.
         String versionSince = null;
         for (String key : options.keySet()) {
             if (key.equals("v")) {
                 versionSince = options.get(key);
             } else {
                 throw new ToolError(Command.checkExt, "invalid option: " + key);
             }
         }
 
         String urlStr = serverUrl +
                 "/fiz/extensions/versionInfo?extensionName=" + extName;
         if (versionSince != null) {
             urlStr = urlStr + "&since=" + versionSince;
         }
 
         String result;
         try {
             result = openUrl(urlStr);
         }
         catch (Exception e) {
             throw new ToolError(Command.checkExt, "error occurred while " +
                     "fetching information from server " + urlStr);
         }
 
         if (result.equals("")) {
             // There are no new versions.
             log(LogLevel.normal, "there are no newer versions of " + extName +
                     " available");
             return;
         }
         
         ArrayList<Dataset> versionDataList =
                 YamlDataset.newStringInstance(result).getChildren("record");
         if (versionSince != null) {
             log(LogLevel.quiet, "new releases of " + extName +
                     " since version " + versionSince + ":");
         } else {
             log(LogLevel.quiet, "all available releases of " + extName +
                     ":");
         }
         // Print out version information for each new new version.
         for(Dataset versionData : versionDataList) {
             log(LogLevel.quiet, versionData.get("version"));
             String description = versionData.check("short_description");
             if (description != null) {
                 log(LogLevel.verbose, "    " + description);
             }
         }
     }
 
     /**
      * Execute the "install core" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeInstallCoreCommand(ArrayList<String> argList) {
         HashMap<String, String> options =
                 parseOptions(Command.installCore, argList, "dv");
 
         if (argList.size() > 1) {
             throw new ToolError(Command.installCore, "invalid usage");
         }
 
         String version = null;
         String path = null;
         // Check any command line options that were specified.
         for (String key : options.keySet()) {
             if (key.equals("v")) {
                 version = options.get(key);
             } else if (key.equals("d")) {
                 path = options.get(key);
             } else {
                 throw new ToolError(Command.installCore, "invalid option: " + key);
             }
         }
 
         if (argList.size() == 1) {
             if (version != null) {
                 // A version was already specified using the -v option, throw
                 // an error.
                 throw new ToolError(Command.installCore, "invalid usage");
             }
             
             version = argList.remove(0);
         }
 
         if (version == null) {
             // If no version was specified, install the latest version.
             String urlStr = serverUrl + "/fiz/fizCore/latestVersion";
             try {
                 version = openUrl(urlStr);
             }
             catch (IOException e) {
                 throw new ToolError(Command.installCore, "error occurred " +
                         "while fetching information from server: " + urlStr);
             }
         }
 
         if (path == null) {
             // If no path was specified, install the new version in the same
             // directory as the default installation.
             path = fizHome + File.separator + "..";
         }
 
         // Check that the target install directory exists.
         File installDir = new File(path);
         if (!installDir.isDirectory()) {
             throw new ToolError(Command.installCore, "make sure " +
                     installDir.getAbsolutePath() + " is a valid directory");
         }
 
         // Check that a directory with the same version does not already exist.
         File fizInstallDir = new File(path + File.separator + "fiz-" + version);
         if (fizInstallDir.exists()) {
             log(LogLevel.normal, "Fiz version " + version +
                     " is already installed at " +
                     getCanonicalPath(fizInstallDir.getAbsolutePath()));
             return;
         }
 
         // Invoke the appropriate ant target.
         String buildFileName = fizHome + File.separator + "antscripts" +
                 File.separator + "install-core.xml";
         HashMap<String, String> properties = new HashMap<String, String>();
         properties.put("fizserver", serverUrl);
         properties.put("destdir", getCanonicalPath(path));
         properties.put("version", version);
         invokeAnt(buildFileName, "install", properties);
 
         log(LogLevel.normal, "installed Fiz version " + version + " at " +
                 getCanonicalPath(path));
     }
 
     /**
      * Execute the "install ext" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeInstallExtCommand(ArrayList<String> argList) {
         HashMap<String, String> options =
                 parseOptions(Command.installExt, argList, "dfv");
 
         boolean installSources = false;
         String version = null;
         // The path of the application to which this extension is to be
         // installed.
         String path = null;
         // The path to the installer.
         String filePath = null;
         // Check any command line options that were specified.
         for (String key : options.keySet()) {
             if (key.equals("d")) {
                 path = options.get(key);
             } else if (key.equals("f")) {
                 filePath = options.get(key);
             } else if (key.equals("s")) {
                 installSources = true;
             } else if (key.equals("v")) {
                 version = options.get(key);
             } else {
                 throw new ToolError(Command.installExt, "invalid option: " +
                         key);
             }
         }
 
         if (path == null) {
             // If no path was specified, install to the current working
             // directory.
             path = ".";
         }
 
         // Find the root directory of the Fiz application.
         String appRootDir = findRootDir(path, DirType.application);
         if (appRootDir == null) {
             throw new ToolError(Command.upgrade, "make sure " + path +
                     " is the path to a valid Fiz web application.");            
         }
 
         // The extension should be unpacked into the "extensions" sub-directory
         // of the application.
         String installPath = appRootDir + File.separator + "extensions";
         
         if (filePath == null) {
             installExtFromServer(argList, version, installSources, installPath);
         } else {
             installExtFromFile(argList, filePath, installPath);
         }
     }
 
     /**
      * Invokes the ant target that downloads ant installs an extension from the
      * Fiz server.
      *
      * @param argList         The command-line arguments.
      * @param version         The version to install. If null, the latest
      *                        version is installed.
      * @param installSources  If true, the source package is installed,
      *                        otherwise only binaries are installed.
      * @param installPath     The path where the extension is to be installed.
      */
     protected void installExtFromServer(ArrayList<String> argList,
                                         String version, boolean installSources,
                                         String installPath) {
         // The extension name is a required parameter in this case.
         if (argList.size() != 1) {
             throw new ToolError(Command.installExt, "invalid usage");
         }
 
         String extensionName = argList.remove(0);
 
         if (version == null) {
             // If no version was specified, install the latest version.
             String urlStr = serverUrl +
                     "/fiz/extensions/latestVersion?extensionName=" +
                     extensionName;
             try {
                 version = openUrl(urlStr);
             }
             catch (IOException e) {
                 throw new ToolError(Command.installExt, "error occurred " +
                         "while fetching information from server: " + urlStr);
             }
 
             // "none" is the special value returned by the server when the
             // server does not have the extension.
             if (version.equals("none")) {
                 throw new ToolError(Command.installExt, "no extension named " + 
                         extensionName + " on " + serverUrl);
             }
         }
         
         File installDir = new File(installPath);
         if (!installDir.isDirectory()) {
             throw new ToolError(Command.installExt, installPath +
                     " does not exist or is not a directory");
         }
         // Invoke the appropriate ant target.
         String buildFileName = fizHome + File.separator + "antscripts" +
                 File.separator + "install-ext.xml";
         HashMap<String, String> properties = new HashMap<String, String>();
         properties.put("fizserver", serverUrl);
         properties.put("destdir", installDir.getAbsolutePath());
         properties.put("version", version);
         properties.put("extname", extensionName);
         if (installSources) {
             invokeAnt(buildFileName, "install-src-from-server", properties);
         } else {
             invokeAnt(buildFileName, "install-bin-from-server", properties);
         }
 
         log(LogLevel.normal, "installed version " + version + " of " +
                 extensionName + " at " + getCanonicalPath(installPath));
     }
 
     /**
      * Invokes the ant target that installs an extension from a file.
      * 
      * @param argList        The command-line arguments.
      * @param filePath       The path to the installer.
      * @param installPath    The path where the extension is to be installed.
      */
     protected void installExtFromFile(ArrayList<String> argList,
                                       String filePath, String installPath) {
         if (argList.size() > 1) {
             throw new ToolError(Command.installExt, "invalid usage");
         }
 
         File installDir = new File(installPath);
         if (!installDir.isDirectory()) {
             throw new ToolError(Command.installExt, installPath +
                     " does not exist or is not a directory");
         }
         File file = new File(filePath);
         if (!file.exists()) {
             throw new ToolError(Command.installExt, filePath +
                     " does not exist");
         }
 
         // Invoke the appropriate ant target.
         String buildFileName = fizHome + File.separator + "antscripts" +
                 File.separator + "install-ext.xml";
         HashMap<String, String> properties = new HashMap<String, String>();
         properties.put("destdir", installDir.getAbsolutePath());
         properties.put("extinstaller", file.getAbsolutePath());
         invokeAnt(buildFileName, "install-from-file", properties);
 
         log(LogLevel.normal, "installed extension " +
                 getCanonicalPath(filePath) + " at " +
                 getCanonicalPath(installPath));
     }
 
     /**
      * Execute the "upgrade" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeUpgradeCommand(ArrayList<String> argList) {
         HashMap<String, String> options =
                 parseOptions(Command.upgrade, argList, "d");
 
         if (argList.size() > 1) {
             throw new ToolError(Command.upgrade, "invalid usage");
         }
 
         String version;
         if (argList.size() == 1) {
             version = argList.remove(0);
         } else {
             // Query the server for the latest version of Fiz.
             String urlStr = serverUrl + "/fiz/fizCore/latestVersion";
             try {
                 version = openUrl(urlStr);
             }
             catch (IOException e) {
                 throw new ToolError(Command.upgrade, "error occurred while " +
                         "fetching information from server: " + urlStr);
             }
         }
 
         String appDirPath = null;
         // Check any command line options that were specified.
         for (String key : options.keySet()) {
             if (key.equals("d")) {
                 appDirPath = options.get(key);
             } else {
                 throw new ToolError(Command.upgrade, "invalid option: " + key);
             }
         }
 
         if (appDirPath == null) {
             // If the path to the application was not specified, assume the
             // current working directory.
             appDirPath = ".";
         }
 
         // Find the root directory of the Fiz application.
         String appRootDirPath = findRootDir(appDirPath, DirType.application);
         if (appRootDirPath == null) {
            throw new ToolError(Command.upgrade, "make sure " + appDirPath +
                     " is the path to a valid Fiz web application, or " +
                     "extension.");
         }
 
         // Invoke the appropriate ant target.
         String buildFileName = fizHome + File.separator + "antscripts" +
                 File.separator + "upgrade-app.xml";
         HashMap<String, String> properties = new HashMap<String, String>();
         properties.put("fizserver", serverUrl);
         properties.put("appdir", appRootDirPath);
         properties.put("version", version);
         invokeAnt(buildFileName, "upgrade", properties);
 
         log(LogLevel.normal, "upgraded application at " +
                 getCanonicalPath(appDirPath) + " to Fiz version " + version);
     }
     
     /**
      * Execute the "version" command.
      *
      * @param argList    The command-line arguments.
      */
     protected void executeVersionCommand(ArrayList<String> argList) {
         if (argList.size() == 0) {
             // Print the version number of the default Fiz installation.
             String version = checkFizVersion(fizHome);
             if (version == null) {
                 throw new ToolError(Command.version, "error occurred while " +
                         "checking the version of Fiz located at " + fizHome);
             }
 
             log(LogLevel.quiet, "Fiz version " + version);
             log(LogLevel.quiet, "installed at " + fizHome);
             return;
         }
 
         if (argList.size() != 1) {
             throw new ToolError(Command.version, "invalid usage");
         }
 
         // The path to (a subdirectory of) the Fiz installation, application,
         // or extension.
         String path = argList.remove(0);
         
         // Check if this is a path to a Fiz core installation.
         String rootDirPath = findRootDir(path, DirType.core);
         if (rootDirPath != null) {
             // This is a Fiz core installation.
             String version = checkFizVersion(rootDirPath);
             if (version == null) {
                 throw new ToolError(Command.version, "error occurred while " +
                         "checking the version. Make sure " + path +
                         " is the path to a valid Fiz installation, web " +
                         "application, or extension.");
             }
 
             log(LogLevel.quiet, "Fiz version " + version);
             log(LogLevel.quiet, "installed at " + rootDirPath);
             return;
         }
 
         // Check if this is a path to an extension.
         // Note: It is important to check for an extension before checking for
         // an application since extensions are usually located under the 
         // "extensions" directory of an application.
         rootDirPath = findRootDir(path, DirType.extension);
         if (rootDirPath != null) {
             // This is a Fiz extension.
             String extName = (new File(rootDirPath)).getName();
             String version = checkJarVersion(rootDirPath + File.separator +
                     "lib" + File.separator + extName + ".jar");
             if (version == null) {
                 throw new ToolError(Command.version, "error occurred while " +
                         "checking the version. Make sure " + path +
                         " is the path to a valid Fiz installation, web " +
                         "application, or extension.");
             }
 
             log(LogLevel.quiet, extName + " version " + version);
             log(LogLevel.quiet, "extension located at " + rootDirPath);
             return;
         }
 
         // Check if this is a path to a Fiz application.
         rootDirPath = findRootDir(path, DirType.application);
         if (rootDirPath != null) {
             // This is a Fiz core application.
             String version = checkFizVersion(rootDirPath);
             if (version == null) {
                 throw new ToolError(Command.version, "error occurred while " +
                         "checking the version. Make sure " + path +
                         " is the path to a valid Fiz installation, web " +
                         "application, or extension.");
             }
 
             log(LogLevel.quiet, "Fiz library version " + version);
             log(LogLevel.quiet, "application located at  " + rootDirPath);
             return;
         }
 
         // The path was not recognized as the subdirectory of a Fiz
         // installation, application or extension.
         throw new ToolError(Command.version, "error occurred while " +
                 "checking the version. Make sure " + path +
                 " is the path to a valid Fiz installation, web " +
                 "application, or extension.");        
     }
 
     /**
      * Parses the argument list and determines which command was specified.
      * 
      * @param argList      The command-line arguments.
      * @return             A {@link org.fiz.tools.Fiz.Command} object which
      *                     indicating the command that was specified.
      * @throws ToolError   Thrown in case the command is not
      *                     recognized.
      */
     protected Command parseCommand(ArrayList<String> argList)
             throws ToolError {
         Command command = Command.unknown;
 
         String arg1 = argList.remove(0);
         String arg2 = null;
         if (arg1.equals("h") || arg1.equals("help")) {
             command = Command.help;
         } else if (arg1.equals("chc")) {
             command = Command.checkCore;
         } else if (arg1.equals("che")) {
             command = Command.checkExt;
         } else if (arg1.equals("check")) {
             if (argList.size() > 0) {
                 arg2 = argList.remove(0);
                 if (arg2.equals("core")) {
                     command = Command.checkCore;
                 } else if (arg2.equals("ext")) {
                     command = Command.checkExt;
                 }
             }
         } else if (arg1.equals("cra")) {
             command = Command.createApp;
         } else if (arg1.equals("cre")) {
             command = Command.createExt;
         } else if (arg1.equals("create")) {
             if (argList.size() > 0) {
                 arg2 = argList.remove(0);
                 if (arg2.equals("app")) {
                     command = Command.createApp;
                 } else if (arg2.equals("ext")) {
                     command = Command.createExt;
                 }
             }
         } else if (arg1.equals("ic")) {
             command = Command.installCore;
         } else if (arg1.equals("ie")) {
             command = Command.installExt;
         } else if (arg1.equals("install")) {
             if (argList.size() > 0) {
                 arg2 = argList.remove(0);
                 if (arg2.equals("core")) {
                     command = Command.installCore;
                 } else if (arg2.equals("ext")) {
                     command = Command.installExt;
                 }
             }
         } else if (arg1.equals("u") || arg1.equals("upgrade")) {
             command = Command.upgrade;
         } else if (arg1.equals("v") || arg1.equals("version")) {
             command = Command.version;
         }
 
         if (command == Command.unknown) {
             // This command was not recognized; throw an error with detailed
             // information.
             String args = arg1;
             if (arg2 != null) {
                 args += " " + arg2;
             }
             throw new ToolError(Command.unknown, "invalid command: " + args);
         }
 
         return command;
     }
 
     /**
      * Invoke the ant target named {@code targetName} in the build file called
      * {@code buildFileName}.
      * 
      * @param buildFileName    The path to the build file.
      * @param targetName       The name of the target to invoke. If null, the
      *                         default target is invoked.
      * @param properties       A {@code HashMap} containing any properties that
      *                         need to be set before executing the target.
      */
     protected void invokeAnt(String buildFileName, String targetName,
                            HashMap<String, String> properties) {
         File buildFile = new File(buildFileName);
         Project p = new Project();
         p.setUserProperty("ant.file", buildFile.getAbsolutePath());
         // Set properties.
         for (String key : properties.keySet()) {
             p.setProperty(key, properties.get(key));
         }
         // Set up a build listener.
         DefaultLogger consoleLogger = new DefaultLogger();
         consoleLogger.setErrorPrintStream(System.err);
         consoleLogger.setOutputPrintStream(System.out);
         if (logLevel == LogLevel.verbose) {
             consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
         } else {
             consoleLogger.setMessageOutputLevel(Project.MSG_ERR);
         }
         p.addBuildListener(consoleLogger);
 
         try {
             p.fireBuildStarted();
             p.init();
             ProjectHelper helper = ProjectHelper.getProjectHelper();
             p.addReference("ant.projectHelper", helper);
             helper.parse(p, buildFile);
             if (targetName == null) {
                 targetName = p.getDefaultTarget();
             }
             p.executeTarget(targetName);
             if (logLevel == LogLevel.verbose) {
                 // The "BUILD SUCCESSFUL" message should only be printed in
                 // verbose mode. Unfortunately, simply setting the output log
                 // level to Project.MSG_ERR does not guarantee this.
                 p.fireBuildFinished(null);
             }
         }
         catch (BuildException e) {
             if (logLevel == LogLevel.verbose) {
                 p.fireBuildFinished(e);
             }
             throw new ToolError(Command.unknown,
                     StringUtil.lcFirst(e.getMessage()));
         }
     }
 
     /**
      * Extracts any command-line options that were specified.
      *
      * @param command              The command being processed. This is used
      *                             only for setting the correct value when
      *                             throwing a {@code ToolError}.
      * @param argList              The command-line arguments.
      * @param optionsWithParams    The options for which a parameter is
      *                             required. The parameter must immediately
      *                             follow the option. e.g. "pv" implies that
      *                             both "-p" and "-v" require a parameter each.
      * @return                     A {@code HashMap} that contains the option
      *                             as key and the corresponding parameter or
      *                             {@code null} if the option does not require
      *                             a parameter.
      */
     protected HashMap<String, String> parseOptions(Command command,
                                                    ArrayList<String> argList,
                                                    String optionsWithParams) {
         HashMap<String, String> optionMap = new HashMap<String, String>();
         
         for (Iterator<String> iter = argList.iterator(); iter.hasNext();) {
             String arg = iter.next();
 
             if (!arg.startsWith("-")) {
                 // This argument is not an option; continue and process the next
                 // argument in the list.
                 continue;
             }
             
             iter.remove();
             // Remove the leading '-' character.
             String options = arg.substring(1);
             if (options.length() == 0) {
                 throw new ToolError(command,
                         "a '-' must be followed by a letter");
             }
 
             for (int i = 0; i < options.length(); i++) {
                 char o = options.charAt(i);
                 if (!Character.isLetter(o)) {
                     // Only letters are considered to be valid options.
                     throw new ToolError(command, "invalid option: " + o);
                 }
 
                 if (optionMap.containsKey(Character.toString(o))) {
                     // This option was already specified.
                     throw new ToolError(command,
                             "found duplicate option: " + o);
                 }
 
                 if (optionsWithParams.contains(
                         Character.toString(o))) {
                     // Options that expect parameters should be specified
                     // separately, not in a group with other options.
                     if (options.length() == 1 && iter.hasNext()) {
                         String param = iter.next();
                         if (!param.startsWith("-")) {
                             iter.remove();
                             optionMap.put(options, param);
                             continue;
                         }
                     }
 
                     // No parameter was specified, throw an error.
                     throw new ToolError(command,
                             "missing parameter for -" + o + " option");
                 } else {
                     // This option does not require a parameter.
                     optionMap.put(Character.toString(o), null);
                 }
             }
         }
         
         return optionMap;
     }
 
     /**
      * Parse any global options that were specified.
      *
      * @param args    The command-line arguments to the tool.
      * @return        An {@code ArrayList} containing the command-line arguments
      *                with the global options removed.
      */
     protected ArrayList<String> parseGlobalOptions(String[] args) {
         // First set the default values for the global properties
         fizHome = System.getProperty("FIZ_HOME");
         serverUrl = System.getProperty("SERVER_URL");
 
         // Now process any global options that were passed via the command line.
         ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
 
         for (Iterator<String> i = argList.iterator(); i.hasNext();) {
             String arg = i.next();
             if (arg.startsWith("--")) {
                 if (arg.equals("--q")) {
                     i.remove();
                     logLevel = LogLevel.quiet;
                 } else if (arg.equals("--v")) {
                     i.remove();
                     logLevel = LogLevel.verbose;
                 } else if (arg.equals("--s")) {
                     i.remove();
                     if (!i.hasNext()) {
                         // A url must be specified along with this option.
                         throw new ToolError(Command.unknown,
                                 "missing argument: " + arg);
                     }
                     serverUrl = i.next();
                     i.remove();
                 } else {
                     // This option is not recognized; throw an Error.
                     throw new ToolError(Command.unknown,
                             "unknown option: " + arg);
                 }
             }
         }
 
         return argList;
     }
 
     /**
      * Returns the version number of a Fiz installation or a Fiz application.
      *
      * @param dir      The path to the root of a Fiz installation or
      *                 application.
      * @return         The version number if successful, null otherwise.
      */
     protected String checkFizVersion(String dir) {
         return checkJarVersion(dir + File.separator + "lib" + File.separator +
                 "fiz.jar");
     }
 
     /**
      * Returns the version number embedded in the Manifest of a jar file.
      * 
      * @param jarPath   The path to the jar file.
      * @return          The version number if successful, null otherwise.
      */
     protected String checkJarVersion(String jarPath) {
         try {
             JarFile jar = new JarFile(jarPath);
 
             return jar.getManifest().getMainAttributes().getValue(
                     Attributes.Name.IMPLEMENTATION_VERSION);
         }
         catch (IOException e) {
             return null;
         }        
     }
 
     /**
      * Converts {@code path} to a canonical path. Used as a convenience method
      * when {@code path} is already known to be a valid file path (it handles
      * the IOException).
      *
      * @param path     A path to a file.
      * @return         If successful, it returns the canonical path, otherwise
      *                 returns the value that was passed in as a paramter.
      */
     protected String getCanonicalPath(String path) {
         String canonicalPath;
         File f = new File(path);
         try {
             canonicalPath = f.getCanonicalPath();
         }
         catch (IOException e) {
             canonicalPath = path;
         }
 
         return canonicalPath;
     }
 
     /**
      * Opens the url specified by {@code urlStr} and returns the result as
      * a string.
      *
      * @param urlStr         The url to open.
      * @return               The result returned by the server.
      * @throws IOException   Thrown if there was an error getting the result
      *                       from the server.
      */
     protected String openUrl(String urlStr)
             throws IOException {
         StringBuilder result = new StringBuilder();
         URL url = new URL(urlStr);
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(url.openStream()));
         char[] buffer = new char[1000];
         while (true) {
             int length = in.read(buffer);
             if (length < 0) {
                 break;
             }
             result.append(buffer, 0, length);
         }
         in.close();
 
         return result.toString();
     }
 
     /**
      * Tries to find the root directory of a Fiz installation given a path to
      * one of its subdirectories (or files). It does this by checking for a
      * particular directory structure; if it finds it, then it returns that
      * path, otherwise it recursively moves one directory level up till it finds
      * the correct directory structure or reaches the root of the file system.
      *
      * @param path    The path to a directory or file to check.
      * @param type    The type of directory to check for.
      * @return        The canonical path to the root directory of the containing
      *                Fiz core installation, application or extension; or null
      *                if such a directory could not be found.
      */
     protected String findRootDir(String path, DirType type) {
         FileSystemView fsv = FileSystemView.getFileSystemView();
         while (!isRootDir(path, type)) {
             File f = new File(path);
             if (fsv.isFileSystemRoot(f)) {
                 // We have reached the root directory
                 return null;
             }
 
             // Move up one directory
             File parentDir = fsv.getParentDirectory(f);
             try {
                 path = parentDir.getCanonicalPath();
             }
             catch (IOException e) {
                 return null;
             }
         }
 
         return getCanonicalPath(path);
     }
 
     /**
      * Checks if the the directory specified by {@code path} is the root
      * directory of a Fiz core installation, Fiz application or extension.
      * 
      * @param path    The path to the directory to check.
      * @param type    The type of directory to check for.
      * @return        True if {@code path} is the root directory of (depending
      *                on the value of {@code type}) a Fiz core installation, Fiz
      *                application or extension.
      */
     protected boolean isRootDir(String path, DirType type) {
         switch (type) {
             case core:
                 return isFizCoreDir(path);
             case application:
                 return isFizApplicationDir(path);
             case extension:
                 return isFizExtensionDir(path);
             default:
                 return false;
         }
     }
 
     /**
      * Checks if the the directory specified by {@code path} is the root
      * directory of a Fiz core installation.
      *
      * @param path    The path to the directory to check.
      * @return        True if {@code path} is the root directory of a Fiz
      *                core installation, false otherwise.
      */
     protected boolean isFizCoreDir(String path) {
         // Check for the following directory structure:
         // antscripts/
         // app/
         // ext/
         // lib/fiz.jar
         // shellscripts/
         // web/fizlib/
         // web/WEB-INF/fiz
         // web/WEB-INF/web.xml
         // build.xml
 
         if (!(new File(path + "/antscripts").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/app").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/ext").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/lib/fiz.jar").isFile())) {
             return false;
         }
         if (!(new File(path + "/shellscripts").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/web/fizlib").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/web/WEB-INF/fiz").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/web/WEB-INF/web.xml").isFile())) {
             return false;
         }
         if (!(new File(path + "/build.xml").isFile())) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Checks if the the directory specified by {@code path} is the root
      * directory of a Fiz application.
      * 
      * @param path    The path to the directory to check.
      * @return        True if {@code path} is the root directory of a Fiz
      *                application, false otherwise.
      */
     protected boolean isFizApplicationDir(String path) {
         // Check for the following directory structure:
         // antscripts/
         // lib/fiz.jar
         // src/
         // web/fizlib/
         // web/WEB-INF/fiz
         // web/WEB-INF/web.xml
         // build.xml
 
         if (!(new File(path + "/antscripts").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/lib/fiz.jar").isFile())) {
             return false;
         }
         if (!(new File(path + "/src").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/web/fizlib").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/web/WEB-INF/fiz").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/web/WEB-INF/web.xml").isFile())) {
             return false;
         }
         if (!(new File(path + "/build.xml").isFile())) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Checks if the the directory specified by {@code path} is the root
      * directory of a Fiz extension.
      *
      * @param path    The path to the directory to check.
      * @return        True if {@code path} is the root directory of a Fiz
      *                extension, false otherwise.
      */
     protected boolean isFizExtensionDir(String path) {
         // Check for the following directory structure:
         // config/
         // css/
         // static/
 
         if (!(new File(path + "/config").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/css").isDirectory())) {
             return false;
         }
         if (!(new File(path + "/static").isDirectory())) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Logs a message to std out.
      * 
      * @param l         The log level of the message. The message is printed
      *                  only if the {@code l} is greater than or equal to the
      *                  current log level setting for the tool.
      * @param message   The message to print.
      */
     protected void log(int l, String message) {
         if (l >= logLevel) {
             System.out.println(message);
         }
     }
 
     /**
      * Print a message out to stderr.
      *
      * @param message    The message to print.
      */
     protected void logError(String message) {
         System.err.println(message);
     }
 
     /**
      * Print general help information.
      */
     protected void printHelp() {
         System.out.println(
                 "Usage: fiz <command> [args] [options]\n" +
                         "\n" +
                         "The following is a list of available commands along with brief usage\n" +
                         "information. Type 'fiz help <command>' for more information on a specific\n" +
                         "command.\n" +
                         "\n" +
                         "Available commands:\n" +
                         "    check core (chc)    fiz check core [version]\n" +
                         "    check ext (che)     fiz check ext <extension name> -v ARG\n" +
                         "    create app (cra)    fiz create app <name>\n" +
                         "    create ext (cre)    fiz create ext <name>\n" +
                         "    help (h)            fiz help [commands]\n" +
                         "    install core (ic)   fiz install core [version] -d ARG -v ARG\n" +
                         "    install ext (ie)    fiz install ext <extension name> -d ARG -f ARG -s\n" +
                         "                            -v ARG\n" +
                         "    upgrade (u)         fiz upgrade [version] -d ARG\n" +
                         "    version (v)         fiz version [path]\n" +
                         "\n" +
                         "Global Options:\n" +
                         "    These are options that are applicable to all commands.\n" +
                         "    --q        Print nothing, or only summary information.\n" +
                         "    --v        Print lots of information.\n" +
                         "    --s ARG    Specify the url of the server that hosts the installers for Fiz\n" +
                         "               or the extensions.\n"
         );
     }
 
     /**
      * Print help information for the "check core" command.
      */
     protected void printCheckCoreHelp() {
         System.out.println(
                 "Usage: fiz check core [version] [options]\n" +
                         "\n" +
                         "Check for new versions of Fiz.\n" +
                         "\n" +
                         "Options:\n" +
                         "    -v ARG    Check only for versions later than ARG.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz check core\n" +
                         "    Prints version information for any Fiz releases newer than the default\n" +
                         "    installation on this machine.\n" +
                         "\n" +
                         "        fiz check core 1.1\n" +
                         "    Prints version information for any Fiz releases newer than version 1.1.\n" +
                         "    This is equivalent to 'fiz check core -v 1.1'\n"
         );
     }
 
     /**
      * Print help information for the "check ext" command.
      */
     protected void printCheckExtHelp() {
         System.out.println(
                 "Usage: fiz check ext <extension name> [options]\n" +
                         "\n" +
                         "Check for new versions of the extension named <extension name>.\n" +
                         "\n" +
                         "Options:\n" +
                         "    -v ARG    Check only for versions later than ARG.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz check ext foo\n" +
                         "    Prints version information for all releases of the extension named\n" +
                         "    \"foo\".\n" +
                         "\n" +
                         "        fiz check ext foo -v 1.1.0\n" +
                         "    Prints version information for any releases of the extension named \"foo\"\n" +
                         "    newer than version 1.1.0.\n"
         );
     }
 
     /**
      * Print help information for the "create app" command.
      */
     protected void printCreateAppHelp() {
         System.out.println(
                 "Usage: fiz create app <name>\n" +
                         "\n" +
                         "Create a new application. <name> can be a relative or absolute file path.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz create app foo\n" +
                         "    Creates a new application named \"foo\" in the directory in which \n" +
                         "    the command is executed.\n" +
                         "\n" +
                         "        fiz create app bar/baz\n" +
                         "    Creates a new application called \"baz\" in ./bar.\n"
         );
     }
 
     /**
      * Print help information for the "create ext" command.
      */
     protected void printCreateExtHelp() {
         System.out.println(
                 "Usage: fiz create ext <name>\n" +
                         "\n" +
                         "Create a new extension. <name> can be a relative or absolute file path.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz create ext foo\n" +
                         "    Creates a new extension called \"foo\" in the directory in which the\n" +
                         "    command is executed.\n" +
                         "\n" +
                         "        fiz create ext bar/baz\n" +
                         "    Creates a new extension called \"baz\" in ./bar.\n"
         );
     }
 
     /**
      * Print help information for the "install core" command.
      */
     protected void printInstallCoreHelp() {
         System.out.println(
                 "Usage: fiz install core [version] [options]\n" +
                         "\n" +
                         "Install a new version of Fiz. If a version is not specified, the latest\n" +
                         "stable release of Fiz installed." +
                         "\n" +
                         "Options:\n" +
                         "    -d ARG    The path where Fiz is to be installed. If this option is not\n" +
                         "              specified, the new version is installed in the same directory\n" +
                         "              as the default Fiz installation.\n" +
                         "    -v ARG    The version of Fiz to install.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz install core 1.1 -d foo/bar\n" +
                         "    Installs version 1.1 of Fiz in the directory ./foo/bar.\n"
         );
     }
 
     /**
      * Print help information for the "install ext" command.
      */
     protected void printInstallExtHelp() {
         System.out.println(
                 "Usage: fiz install ext <extension name> [options]\n" +
                         "\n" +
                         "Install a new version of the extension named <extension name>.\n" +
                         "\n" +
                         "Options:\n" +
                         "    -d ARG    The path to the application for which the extension is to be\n" +
                         "              installed. If this option is not specified, the current working\n" +
                         "              directory is used.\n" +
                         "    -f ARG    The path to the extension's installer. Use this option to install\n" +
                         "              an extension from a file on disk instead of fetching it from the\n" +
                         "              Fiz server. Note: The <extension name> parameter need not be\n" +
                         "              specified and is ignored in this case.\n" +
                         "    -s        Install the source package for the extension instead of just the\n" +
                         "              binaries.\n" +
                         "    -v ARG    The version of the extension to install. If this option is not\n" +
                         "              specified, the latest stable version is installed.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz install ext foo -v 2.0 -d bar/extensions\n" +
                         "    Installs version 2.0 of the extension named \"foo\" in the directory\n" +
                         "    ./bar/extensions.\n" +
                         "\n" +
                         "        fiz install ext -f ../foo/bar/baz-bin-1.0.0.zip\n" +
                         "    Installs the extension located at ../foo/bar/baz-bin-1.0.0.zip\n" +
                         "    into the current directory.\n"
         );
     }
 
     /**
      * Print help information for the "version" command.
      */
     protected void printUpgradeHelp() {
         System.out.println(
                 "Usage: fiz upgrade [version] [options]\n" +
                         "\n" +
                         "Upgrades the Fiz platform files that are part of a Fiz web application.\n" +
                         "If a version is not specified, the latest version of the Fiz platform is\n" +
                         "assumed.\n" +
                         "\n" +
                         "Options:\n" +
                         "    -d ARG    The path to the Fiz application that is to be upgraded.\n" +
                         "              If this option is not specified, the current working\n" +
                         "              directory is used.\n" +
                         "\n" +
                         "Examples:\n" +
                         "        fiz upgrade\n" +
                         "    Upgrades the application to the latest version of Fiz.\n" +
                         "\n" +
                         "        fiz upgrade 2.0 -d foo/bar\n" +
                         "    Upgrades the application located at \"foo/bar\" to version 2.0 of the\n" +
                         "    Fiz platform.\n"
         );
     }
 
     /**
      * Print help information for the "version" command.
      */
     protected void printVersionHelp() {
         System.out.println(
                 "Usage: fiz version [path]\n" +
                         "\n" +
                         "Print version information for a Fiz installation, an application,\n" +
                         "or an extension. If no path is specified, the version information\n" +
                         "for the default Fiz installation is printed.\n"
         );
     }
 }
