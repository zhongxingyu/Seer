 package iTests.framework.utils;
 
 import com.j_spaces.kernel.PlatformVersion;
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyObject;
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.apache.commons.exec.Executor;
 import org.apache.commons.exec.LogOutputStream;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.hyperic.sigar.Sigar;
 import org.testng.Assert;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.concurrent.TimeoutException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ScriptUtils {
 
     /**
      * this method should be used to forcefully kill windows processes by pid
      * @param pid - the process id of witch to kill
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     public static int killWindowsProcess(int pid) throws IOException, InterruptedException {
         String cmdLine = "cmd /c call taskkill /pid " + pid + " /F";
         String[] parts = cmdLine.split(" ");
         ProcessBuilder pb = new ProcessBuilder(parts);
         LogUtils.log("Executing Command line: " + cmdLine);
         Process process = pb.start();
         return process.waitFor();
     }
 
     /**
      *
      * @param commandLine The command line to run.
      * @param timeout Timeout in milliseconds for the command.
      * @throws TimeoutException In case the timeout expired.
      */
     public static void executeCommandLine(final String commandLine, final long timeout) throws TimeoutException {
 
         Executor executor = new DefaultExecutor();
         executor.setExitValue(0);
         ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
         executor.setWatchdog(watchdog);
         ProcessOutputStream outAndErr = new ProcessOutputStream();
         try {
             PumpStreamHandler streamHandler = new PumpStreamHandler(outAndErr);
             executor.setStreamHandler(streamHandler);
             LogUtils.log("Executing commandLine : '" + commandLine + "'");
             executor.execute(CommandLine.parse(commandLine));
             LogUtils.log("Execution completed successfully. Process output was : " + outAndErr.getOutput());
         } catch (final Exception e) {
             if (watchdog.killedProcess()) {
                 throw new TimeoutException("Timed out while executing commandLine : '" + commandLine + "'");
             }
             throw new RuntimeException("Failed executing commandLine : '" + commandLine
                     + ". Process output was : " + outAndErr.getOutput(), e);
         }
     }
 
     /**
      * Logs process output to the logger.
      * @author elip
      *
      */
     private static class ProcessOutputStream extends LogOutputStream {
 
         private List<String> output = new java.util.LinkedList<String>();
 
         private String getOutput() {
             return StringUtils.join(output, "\n");
         }
 
         @Override
         protected void processLine(final String line, final int level) {
             output.add(line);
             LogUtils.log(line);
         }
     }
 
     @SuppressWarnings("deprecation")
     public static String runScriptRelativeToGigaspacesBinDir(String args, long timeout) throws Exception {
         return RunScript.run(args, timeout, true);
     }
 
     public static String runScriptWithAbsolutePath(String args, long timeout) throws Exception {
         return RunScript.run(args, timeout, false);
     }
 
     public static RunScript runScriptWithAbsolutePath(String args) {
         return new RunScript(args, false);
     }
 
     public static RunScript runScriptRelativeToGigaspacesBinDir(String args) throws Exception {
         return new RunScript(args, true);
     }
 
     @SuppressWarnings("deprecation")
     public static String runScript(String args, long timeout) throws Exception {
         return RunScript.run(args, timeout, true);
     }
 
     public static RunScript runScript(String args) throws Exception {
         return new RunScript(args, true);
     }
 
     public static class RunScript implements Runnable {
 
         private final String[] args;
         private final StringBuilder sb;
         private final ProcessBuilder processBuilder;
         private final Thread thread;
         private Process process;
 
         public static String run(String args, long timeout, boolean relativeToGigaspacesBinDir)
                 throws InterruptedException, IOException {
             RunScript runScript = new RunScript(args, relativeToGigaspacesBinDir);
             runScript.thread.join(timeout);
             if (runScript.thread.isAlive())
                 runScript.thread.stop();
             runScript.kill();
             return runScript.getScriptOutput();
         }
 
         public RunScript(String args, boolean relativeToGigaspacesBinDir) {
 
             this.sb = new StringBuilder();
             this.args = args.split(" ");
             String binPath = null;
             if (relativeToGigaspacesBinDir) {
                 binPath = getBuildBinPath();
                 this.args[0] = binPath + "/" + this.args[0] + getScriptSuffix();
             }
            this.processBuilder = new ProcessBuilder(args + getScriptSuffix())
                    .redirectErrorStream(true);
            if (relativeToGigaspacesBinDir)
                processBuilder.directory(new File(binPath));
             this.thread = new Thread(this);
             this.thread.start();
         }
 
         public void run() {
 
             try {
                 process = processBuilder.start();
                 BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
 
                 String line;
                 while ((line = br.readLine()) != null) {
                     sb.append(line).append("\n");
                 }
 
             } catch (IOException e) {
                 if (process == null)
                     LogUtils.log("Failed to start process", e);
                 else
                     LogUtils.log("Failed to monitor process", e);
             }
         }
 
         public String getScriptOutput() {
             return sb.toString();
         }
 
         public void kill() throws IOException, InterruptedException {
             String scriptOutput = sb.toString();
 
             //This regular expression captures the script's pid
             String regex = "(?:Log file:.+-)([0-9]+)(?:\\.log)";
             Pattern pattern = Pattern.compile(regex);
             Matcher matcher = pattern.matcher(scriptOutput);
 
             while (matcher.find()) {
                 int pid = Integer.parseInt(matcher.group(1));
                 String processIdAsString = Integer.toString(pid);
                 try {
                     if (isLinuxMachine()) {
                         new ProcessBuilder("kill", "-9", processIdAsString).start().waitFor();
                     } else {
                         new ProcessBuilder("taskkill", "/F", "/PID", processIdAsString).start().waitFor();
                     }
                 } catch (IOException e) {
                     LogUtils.log("caught IOException while teminating process", e);
                 }
             }
             if(process != null)
                 process.destroy();
         }
     }
 
     public static void startLocalProcess(String dir, String ... command) throws IOException, InterruptedException {
         ProcessBuilder pb = new ProcessBuilder(command).directory(new File(URLDecoder.decode(dir, "ISO-8859-1")));
         pb.redirectErrorStream(true);
         Process p = pb.start();
         String line;
         InputStream stdout = p.getInputStream ();
         BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
         while ((line = reader.readLine ()) != null) {
             System.out.println(line);
         }
 
     }
 
     public static String getScriptSuffix() {
         return isLinuxMachine() ? ".sh" : ".bat";
     }
 
     public static boolean isLinuxMachine() {
         return !isWindows();
     }
 
     public static String getBuildPath() {
         String referencePath = getClassLocation(Sigar.class);
         String jarPath = referencePath.split("!")[0];
         int startOfPathLocation = jarPath.indexOf("/");
         if (isLinuxMachine()) {
             jarPath = jarPath.substring(startOfPathLocation);
         }
         else {
             jarPath = jarPath.substring(startOfPathLocation + 1);
         }
         int startOfJarFilename = jarPath.indexOf("sigar.jar");
         jarPath = jarPath.substring(0, startOfJarFilename);
         jarPath += "../../..";
         return jarPath;
     }
 
     public static String getBuildBinPath() {
         return getBuildPath() + File.separator + "bin";
     }
 
     public static String getBuildRecipesPath() {
         return getBuildPath() + "/recipes";
     }
 
     public static String getBuildRecipesServicesPath() {
         return getBuildRecipesPath() + "/services";
     }
 
     public static String getBuildRecipesApplicationsPath() {
         return getBuildRecipesPath() + "/apps";
     }
 
     public static String getClassLocation(Class<?> clazz) {
         String clsAsResource = clazz.getName().replace('.', '/').concat(".class");
 
         final ClassLoader clsLoader = clazz.getClassLoader();
 
         URL result = clsLoader != null ? clsLoader.getResource(clsAsResource) :
             ClassLoader.getSystemResource(clsAsResource);
         return result.getPath();
     }
 
     public static File getGigaspacesZipFile() throws IOException {
 
         File gigaspacesDirectory = new File ( ScriptUtils.getBuildPath());
         String gigaspacesFolderName = "gigaspaces-" + PlatformVersion.getEdition() + "-" + PlatformVersion.getVersion() + "-" + PlatformVersion.getMilestone();
         String gigaspacesZipFilename = gigaspacesFolderName + "-b" + PlatformVersion.getBuildNumber() + ".zip";
         LogUtils.log("zip name: " + gigaspacesZipFilename);
         File gigaspacesZip = new File(gigaspacesDirectory.getAbsoluteFile() +"/../" + gigaspacesZipFilename);
         if (!gigaspacesZip.exists()) {
             File[] excludeDirectories = new File[]{new File(gigaspacesDirectory,"work"),
                     new File(gigaspacesDirectory,"logs"),
                     new File(gigaspacesDirectory,"deploy"),
                     new File(gigaspacesDirectory,"clouds/ec2/upload")};
             File[] includeDirectories = new File[]{new File(gigaspacesDirectory,"deploy/templates")};
             File[] emptyDirectories = new File[]{new File(gigaspacesDirectory,"logs"),
                     new File(gigaspacesDirectory,"lib/platform/esm")};
             ZipUtils.zipDirectory(gigaspacesZip, gigaspacesDirectory, includeDirectories, excludeDirectories, emptyDirectories, gigaspacesFolderName);
         }
 
         if (!ZipUtils.isZipIntact(gigaspacesZip)) {
             throw new IOException(gigaspacesZip + " is not a valid zip file.");
         }
 
         return gigaspacesZip.getCanonicalFile();
     }
 
     //////////////////////
 
     public static class ScriptPatternIterator implements Iterator<Object[]> {
 
         private final InputStream is;
         private final XMLStreamReader parser;
         private int event;
 
         public ScriptPatternIterator(String xmlPath) throws XMLStreamException, FactoryConfigurationError, IOException {
             is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlPath);
             parser = XMLInputFactory.newInstance().createXMLStreamReader(is);
             nextScript();
         }
 
         public boolean hasNext() {
             return (event != XMLStreamConstants.END_DOCUMENT);
         }
 
         public Object[] next() {
             String currentScript = parser.getAttributeValue(null, "name");
             long timeout = Long.parseLong(parser.getAttributeValue(null, "timeout"));
 
             List<String[]> patterns = new ArrayList<String[]>();
 
             try {
                 nextPattern();
             } catch (XMLStreamException e1) {
                 e1.printStackTrace();
             }
             while (event != XMLStreamConstants.END_ELEMENT ||
                     !parser.getName().toString().equals("script")) {
 
                 String currentRegex = parser.getAttributeValue(null, "regex");
                 String currentExpectedAmount = parser.getAttributeValue(null, "expected-amount");
 
                 patterns.add(new String[] { currentRegex, currentExpectedAmount });
 
                 try {
                     nextPattern();
                 } catch (XMLStreamException e) {
                     e.printStackTrace();
                 }
             }
 
             String[][] patternObjects = new String[patterns.size()][];
             for (int i = 0; i < patterns.size(); i++) {
                 patternObjects[i] = patterns.get(i);
             }
 
             try {
                 nextScript();
             } catch (XMLStreamException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             return new Object[] { currentScript, patternObjects , timeout };
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
 
         private void nextScript() throws XMLStreamException, IOException {
             while (true) {
                 event = parser.next();
                 if (event == XMLStreamConstants.END_DOCUMENT) {
                     parser.close();
                     is.close();
                     return;
                 }
                 if (event == XMLStreamConstants.START_ELEMENT
                         && parser.getName().toString().equals("script")) {
                     return;
                 }
             }
         }
 
         private void nextPattern() throws XMLStreamException {
             while (true) {
                 event = parser.next();
                 if (event == XMLStreamConstants.END_ELEMENT
                         && parser.getName().toString().equals("script")) {
                     return;
                 }
                 if (event == XMLStreamConstants.START_ELEMENT
                         && parser.getName().toString().equals("pattern")) {
                     return;
                 }
             }
         }
 
     }
 
     public static Map<String,String> loadPropertiesFromClasspath(String resourceName) throws IOException {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         InputStream in = classLoader.getResourceAsStream(resourceName);
         Properties properties = null;
         if (in != null) {
             properties = new Properties();
             properties.load(in);
         }
         return convertPropertiesToStringProperties(properties);
     }
 
     private static Map<String,String> convertPropertiesToStringProperties(Properties properties) {
         Map<String,String> stringProperties = new HashMap<String,String>();
         for (Object key : properties.keySet()) {
             stringProperties.put(key.toString(),properties.get(key).toString());
         }
         return stringProperties;
     }
 
     public static Map<String, String> loadPropertiesFromFile(File file) throws FileNotFoundException, IOException {
         Properties properties = new Properties();
         properties.load(new FileInputStream(file));
         return convertPropertiesToStringProperties(properties);
     }
 
     public static GroovyObject getGroovyObject(String className) throws CompilationFailedException, IOException, InstantiationException, IllegalAccessException{
         ClassLoader parent = ScriptUtils.class.getClassLoader();
         GroovyClassLoader loader = new GroovyClassLoader(parent);
         InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(className);
         if (is == null) {
             Assert.fail("Failed finding " + className);
         }
         try {
             Class<?> groovyClass = loader.parseClass(convertStreamToString(is));
             return (GroovyObject) groovyClass.newInstance();
         } finally {
             is.close();
         }
     }
 
     public static String convertStreamToString(InputStream is) {
         String ans = "";
         Scanner s = new Scanner(is);
         s.useDelimiter("\\A");
         ans = s.hasNext() ? s.next() : "";
         s.close();
         return ans;
     }
 
     /**
      * @param className the name of the .groovy file with path starting from sgtest base dir
      * @param methodName the name of the method in className we wnt to invoke
      * @throws org.codehaus.groovy.control.CompilationFailedException
      * @throws IOException
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public static void runGroovy(String className, String methodName) throws CompilationFailedException, IOException, InstantiationException, IllegalAccessException{
         GroovyObject obj = getGroovyObject(className);
         Object[] args = {};
         obj.invokeMethod(methodName, args);
     }
 
     /**
      * @param className  the name of the .groovy file with path starting from sgtest base dir
      * @param methodName the name of the method in className we wnt to invoke
      * @param args the arguments for the method to invoke
      * @throws org.codehaus.groovy.control.CompilationFailedException
      * @throws IOException
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public static void runGroovy(String className, String methodName, Object...args) throws CompilationFailedException, IOException, InstantiationException, IllegalAccessException{
         GroovyObject obj = getGroovyObject(className);
         obj.invokeMethod(methodName, args);
     }
 
     /**
      * this method runs a .groovy file with a relative path to current working directory
      * use example: <code>ScriptUtils.runGroovy("src/test/maven/copyMuleJars.groovy")</code>
      *
      * @param className the name of the .groovy file with path starting from current working directory
      * @throws org.codehaus.groovy.control.CompilationFailedException
      * @throws IOException
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public static void runGroovy(String className) throws CompilationFailedException, IOException, InstantiationException, IllegalAccessException{
         GroovyObject obj = getGroovyObject(className);
         Object[] args = {};
         obj.invokeMethod("run", args);
     }
 
     public static boolean isWindows() {
         return (System.getenv("windir") != null);
     }
 
 }
