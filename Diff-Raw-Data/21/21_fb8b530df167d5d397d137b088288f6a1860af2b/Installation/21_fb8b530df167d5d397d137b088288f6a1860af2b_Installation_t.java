 package org.python.util.install;
 
 import java.awt.GraphicsEnvironment; // should be allowed on headless systems
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 import org.python.util.install.driver.Autotest;
 import org.python.util.install.driver.InstallationDriver;
 import org.python.util.install.driver.Tunnel;
 
 public class Installation {
     protected static final String ALL = "1";
     protected static final String STANDARD = "2";
     protected static final String MINIMUM = "3";
     protected static final String STANDALONE = "9";
 
     protected static final String OS_NAME = "os.name";
     protected static final String OS_VERSION = "os.version";
     protected static final String JAVA_VM_NAME = "java.vm.name";
     protected static final String EMPTY = "";
 
     protected static final String HEADLESS_PROPERTY_NAME = "java.awt.headless";
 
     protected final static int NORMAL_RETURN = 0;
     protected final static int ERROR_RETURN = 1;
 
     private static final String RESOURCE_CLASS = "org.python.util.install.TextConstants";
 
     private static ResourceBundle _textConstants = ResourceBundle.getBundle(RESOURCE_CLASS, Locale.getDefault());
 
     private static boolean _verbose = false;
     private static boolean _isAutotesting = false;
 
     public static void main(String args[]) {
         internalMain(args, null, null);
     }
 
     public static void driverMain(String args[], Autotest autotest, Tunnel tunnel) {
         internalMain(args, autotest, tunnel);
     }
 
     protected static boolean isVerbose() {
         return _verbose;
     }
    
    protected static void setVerbose(boolean verbose) {
        _verbose = verbose;
    }
 
     protected static boolean isAutotesting() {
         return _isAutotesting;
     }
 
     protected static String getText(String key) {
         return _textConstants.getString(key);
     }
 
     protected static String getText(String key, String... parameters) {
         return MessageFormat.format(_textConstants.getString(key), (Object[])parameters);
     }
 
     protected static void setLanguage(Locale locale) {
         _textConstants = ResourceBundle.getBundle(RESOURCE_CLASS, locale);
     }
 
     protected static boolean isValidOs() {
         String osName = System.getProperty(OS_NAME, "");
         String lowerOs = osName.toLowerCase();
         if (isWindows()) {
             return true;
         }
         if (lowerOs.indexOf("linux") >= 0) {
             return true;
         }
         if (lowerOs.indexOf("mac") >= 0) {
             return true;
         }
         if (lowerOs.indexOf("unix") >= 0) {
             return true;
         }
         return false;
     }
 
     protected static boolean isValidJava(JavaVersionInfo javaVersionInfo) {
         String specificationVersion = javaVersionInfo.getSpecificationVersion();
         if (Installation.isVerbose()) {
             ConsoleInstaller.message("specification version: '" + specificationVersion + "'");
         }
         // major.minor.micro
         // according to http://java.sun.com/j2se/1.5.0/docs/guide/versioning/spec/versioning2.html
         int major = 0;
         int minor = 0;
         StringTokenizer tokenizer = new StringTokenizer(specificationVersion, ".");
         if (tokenizer.hasMoreTokens()) {
             major = Integer.valueOf(tokenizer.nextToken()).intValue();
         }
         if (tokenizer.hasMoreTokens()) {
             minor = Integer.valueOf(tokenizer.nextToken()).intValue();
         }
         boolean valid = true;
         if (major == 1) {
             if (minor < 5) {
                 valid = false;
             }
         }
         return valid;
     }
 
     public static boolean isWindows() {
         boolean isWindows = false;
         String osName = System.getProperty(OS_NAME, "");
         if (osName.toLowerCase().indexOf("windows") >= 0) {
             isWindows = true;
         }
         return isWindows;
     }
 
     protected static boolean isMacintosh() {
         boolean isMacintosh = false;
         String osName = System.getProperty(OS_NAME, "");
         if (osName.toLowerCase().indexOf("mac") >= 0) {
             isMacintosh = true;
         }
         return isMacintosh;
     }
 
     protected static boolean isGNUJava() {
         boolean isGNUJava = false;
         String javaVmName = System.getProperty(JAVA_VM_NAME, "");
         String lowerVmName = javaVmName.toLowerCase();
         if (lowerVmName.indexOf("gnu") >= 0 && lowerVmName.indexOf("libgcj") >= 0) {
             isGNUJava = true;
         }
         return isGNUJava;
     }
 
     protected static boolean isJDK141() {
         boolean isJDK141 = false;
         String javaVersion = System.getProperty(JavaVersionTester.JAVA_VERSION, "");
         if (javaVersion.toLowerCase().startsWith("1.4.1")) {
             isJDK141 = true;
         }
         return isJDK141;
     }
 
     /**
      * Get the version info of an external (maybe other) jvm.
      * 
      * @param javaHomeHandler
      *            The java home handler pointing to the java home of the external jvm.<br>
      *            The /bin directory is assumed to be a direct child directory.
      * 
      * @return The versionInfo
      */
     protected static JavaVersionInfo getExternalJavaVersion(JavaHomeHandler javaHomeHandler) {
         JavaVersionInfo versionInfo = new JavaVersionInfo();
         if (javaHomeHandler.isValidHome()) {
             try {
                 ConsoleInstaller.message(getText(TextKeys.C_CHECK_JAVA_VERSION));
                 // launch the java command - temporary file will be written by the child process
                 File tempFile = File.createTempFile("jython_installation", ".properties");
                 if (tempFile.exists() && tempFile.canWrite()) {
                     String command[] = new String[5];
                     command[0] = javaHomeHandler.getExecutableName();
                     command[1] = "-cp";
                     // our own class path should be ok here
                     command[2] = System.getProperty("java.class.path"); 
                     command[3] = JavaVersionTester.class.getName();
                     command[4] = tempFile.getAbsolutePath();
                     if (isVerbose()) {
                         ConsoleInstaller.message("executing: " + command[0] + " " + command[1]
                                 + " " + command[2] + " " + command[3] + " " + command[4]);
                     }
                     ChildProcess childProcess = new ChildProcess(command, 10000); // 10 seconds
                     childProcess.setDebug(Installation.isVerbose());
                     int errorCode = childProcess.run();
                     if (errorCode != NORMAL_RETURN) {
                         versionInfo.setErrorCode(errorCode);
                         versionInfo.setReason(getText(TextKeys.C_NO_VALID_JAVA, javaHomeHandler.toString()));
                     } else {
                         Properties tempProperties = new Properties();
                         tempProperties.load(new FileInputStream(tempFile));
                         fillJavaVersionInfo(versionInfo, tempProperties);
                     }
                 } else {
                     versionInfo.setErrorCode(ERROR_RETURN);
                     versionInfo.setReason(getText(TextKeys.C_UNABLE_CREATE_TMPFILE, tempFile.getAbsolutePath()));
                 }
             } catch (IOException e) {
                 versionInfo.setErrorCode(ERROR_RETURN);
                 versionInfo.setReason(getText(TextKeys.C_NO_VALID_JAVA, javaHomeHandler.toString()));
             }
         } else {
             versionInfo.setErrorCode(ERROR_RETURN);
             versionInfo.setReason(getText(TextKeys.C_NO_VALID_JAVA, javaHomeHandler.toString()));
         }
 
         return versionInfo;
     }
 
     protected static void fillJavaVersionInfo(JavaVersionInfo versionInfo, Properties properties) {
         versionInfo.setVersion(properties.getProperty(JavaVersionTester.JAVA_VERSION));
         versionInfo.setSpecificationVersion(properties.getProperty(JavaVersionTester.JAVA_SPECIFICATION_VERSION));
         versionInfo.setVendor(properties.getProperty(JavaVersionTester.JAVA_VENDOR));
     }
 
     protected static class JavaVersionInfo {
         private String _version;
         private String _specificationVersion;
         private String _vendor;
         private int _errorCode;
         private String _reason;
 
         protected JavaVersionInfo() {
             _version = EMPTY;
             _specificationVersion = EMPTY;
             _errorCode = NORMAL_RETURN;
             _reason = EMPTY;
         }
 
         protected void setVersion(String version) {
             _version = version;
         }
 
         protected void setSpecificationVersion(String specificationVersion) {
             _specificationVersion = specificationVersion;
         }
 
         protected void setVendor(String vendor) {
             _vendor = vendor;
         }
 
         protected void setErrorCode(int errorCode) {
             _errorCode = errorCode;
         }
 
         protected void setReason(String reason) {
             _reason = reason;
         }
 
         protected String getVersion() {
             return _version;
         }
 
         protected String getSpecificationVersion() {
             return _specificationVersion;
         }
 
         protected String getVendor() {
             return _vendor;
         }
 
         protected int getErrorCode() {
             return _errorCode;
         }
 
         protected String getReason() {
             return _reason;
         }
     }
 
     protected static class JavaFilenameFilter implements FilenameFilter {
         public boolean accept(File dir, String name) {
             if (name.toLowerCase().startsWith("java")) {
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     public static boolean isGuiAllowed() {
         boolean verbose = isVerbose();
         if (verbose) {
             ConsoleInstaller.message("checking gui availability");
         }
         if (Boolean.getBoolean(HEADLESS_PROPERTY_NAME)) {
             return false;
         }
         try {
             if (verbose) {
                 ConsoleInstaller.message("trying to get the graphics environment");
             }
             GraphicsEnvironment.getLocalGraphicsEnvironment();
             if (verbose) {
                 ConsoleInstaller.message("got the graphics environment!");
             }
             return true;
         } catch (Throwable t) {
             if (verbose) {
                 ConsoleInstaller.message("got the following exception:");
                 t.printStackTrace();
             }
             return false;
         }
     }
 
     //
     // private methods
     //
 
     private static boolean useGui(InstallerCommandLine commandLine) {
         if (commandLine.hasConsoleOption() || commandLine.hasSilentOption()) {
             return false;
         }
         return isGuiAllowed();
     }
 
     /**
      * In the normal case, this method is called with <code>(args, null, null)</code>, see <code>main(args)</code>.
      * <p>
      * However, in autotesting mode (<code>commandLine.hasAutotestOption()</code>), we pass in Autotest and Tunnel,
      * see <code>driverMain(args, autotest, tunnel)</code>.
      * <p>
      * This means that in autotesting mode this method will call itself (via <code>InstallationDriver.drive()</code>),
      * but with different arguments.
      */
     private static void internalMain(String[] args, Autotest autotest, Tunnel tunnel) {
         try {
            setVerbose(InstallerCommandLine.hasVerboseOptionInArgs(args));
            boolean verbose = isVerbose();
            if (verbose) {
                 dumpSystemProperties();
                 ConsoleInstaller.message("reading jar info");
             }
             JarInfo jarInfo = new JarInfo();
             InstallerCommandLine commandLine = new InstallerCommandLine(jarInfo);
             if (!commandLine.setArgs(args) || commandLine.hasHelpOption()) {
                 commandLine.printHelp();
                 System.exit(1);
             } else {
                 if (commandLine.hasAutotestOption()) {
                    if (verbose) {
                         ConsoleInstaller.message("running autotests");
                     }
                     _isAutotesting = true;
                     InstallationDriver autotestDriver = new InstallationDriver(commandLine);
                     autotestDriver.drive(); // ! reentrant into internalMain()
                     _isAutotesting = false;
                     ConsoleInstaller.message("\ncongratulations - autotests complete !");
                     System.exit(0);
                 }
                 if (!useGui(commandLine)) {
                    if (verbose) {
                         ConsoleInstaller.message("using the console installer");
                     }
                     ConsoleInstaller consoleInstaller = new ConsoleInstaller(commandLine, jarInfo);
                     consoleInstaller.setTunnel(tunnel);
                     consoleInstaller.install();
                     if (!isAutotesting()) {
                         System.exit(0);
                     }
                 } else {
                    if (verbose) {
                         ConsoleInstaller.message("using the gui installer");
                     }
                     new FrameInstaller(commandLine, jarInfo, autotest);
                 }
             }
         } catch (InstallationCancelledException ice) {
             ConsoleInstaller.message((getText(TextKeys.INSTALLATION_CANCELLED)));
             System.exit(1);
         } catch (Throwable t) {
             t.printStackTrace();
             System.exit(1);
         }
     }
     
     private static void dumpSystemProperties() throws IOException {
         @SuppressWarnings("unchecked")
         Enumeration<String> names = (Enumeration<String>)System.getProperties().propertyNames();
         StringBuilder contents = new StringBuilder(400);
         contents.append("Properties at the beginning of the Jython installation:\n\n");
         while (names.hasMoreElements()) {
             String name = names.nextElement();
             String value = System.getProperty(name, "");
             contents.append(name);
             contents.append('=');
             contents.append(value);
             contents.append("\n");
         }
         File output = File.createTempFile("System", ".properties");
         FileHelper.write(output, contents.toString());
         ConsoleInstaller.message("system properties dumped to " + output.getAbsolutePath());
     }
 
 }
