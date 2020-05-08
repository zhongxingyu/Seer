 package gov.nih.nci.cagrid.introduce.common;
 
 import gov.nih.nci.cagrid.introduce.IntroduceConstants;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * @author <A HREF="MAILTO:hastings@bmi.osu.edu">Shannon Hastings </A>
  * @author <A HREF="MAILTO:oster@bmi.osu.edu">Scott Oster </A>
  * @author <A HREF="MAILTO:langella@bmi.osu.edu">Stephen Langella </A>
  */
 public class AntTools {
     private static final Logger logger = Logger.getLogger(AntTools.class);
 
     public static final String DEBUG_ANT_CALL_JAVA_OPTS[] = {"-Xdebug", "-Xnoagent", "-Djava.compiler=NONE",
             "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"};
 
     private static Boolean isWindows = null;
 
 
     public static List<String> getAntCommand(String antCommand, String buildFileDir) throws Exception {
         List<String> cmd = getAntCommandCall(buildFileDir);
 
         StringTokenizer stok = new StringTokenizer(antCommand, " ");
         while (stok.hasMoreTokens()) {
             cmd.add(stok.nextToken());
         }
         return cmd;
     }
 
 
     public static List<String> getAntAllCommand(String buildFileDir) throws Exception {
         return getAntCommand("clean all", buildFileDir);
     }
 
 
     public static List<String> getAntMergeCommand(String buildFileDir) throws Exception {
         return getAntCommand("clean merge", buildFileDir);
     }
 
 
     public static List<String> getAntDeployTomcatCommand(String buildFileDir) throws Exception {
         return createDeploymentCommand(buildFileDir, "deployTomcat");
     }
 
 
     public static List<String> getAntDeployJBossCommand(String buildFileDir) throws Exception {
         return createDeploymentCommand(buildFileDir, "deployJBoss");
     }
 
 
     public static List<String> getAntDeployGlobusCommand(String buildFileDir) throws Exception {
         return createDeploymentCommand(buildFileDir, "deployGlobus");
     }
 
 
     public static List<String> getAntUndeployTomcatCommand(String buildFileDir) throws Exception {
         return createDeploymentCommand(buildFileDir, "undeployTomcat");
     }
 
 
     public static List<String> getAntUndeployJBossCommand(String buildFileDir) throws Exception {
         return createDeploymentCommand(buildFileDir, "undeployJBoss");
     }
 
 
     public static List<String> getAntUndeployGlobusCommand(String buildFileDir) throws Exception {
         return createDeploymentCommand(buildFileDir, "undeployGlobus");
     }
 
 
     private static List<String> createDeploymentCommand(String buildFileDir, String deployTarget) throws Exception {
         String dir = buildFileDir;
         File dirF = new File(dir);
         if (!dirF.isAbsolute()) {
             dir = buildFileDir + File.separator + dir;
         }
 
         List<String> command = getAntCommandCall(buildFileDir);
         command.add(escapeIfNecessary("-Dservice.properties.file=" + dir + File.separator
             + IntroduceConstants.INTRODUCE_SERVICE_PROPERTIES));
         command.add(deployTarget);
 
         return command;
     }
 
 
     public static List<String> getAntSkeletonCreationCommand(String buildFileDir, String name, String dir,
         String packagename, String namespacedomain, String resourceOptions, String extensions) throws Exception {
         return getAntSkeletonCreationCommand(buildFileDir, name, dir, packagename, namespacedomain, resourceOptions,
             extensions, false);
     }
 
 
     public static List<String> getAntSkeletonCreationCommand(String buildFileDir, String name, String dir,
         String packagename, String namespacedomain, String resourceOptions, String extensions, boolean debug)
         throws Exception {
 
         // fix dir path if it relative......
         logger.debug("CREATION: builddir: " + buildFileDir);
         logger.debug("CREATION: destdir: " + dir);
         File dirF = new File(dir);
         if (!dirF.isAbsolute()) {
             dir = buildFileDir + File.separator + dir;
         }
 
         List<String> command = getAntCommandCall(buildFileDir, debug);
 
         command.add(escapeIfNecessary("-Dintroduce.skeleton.destination.dir=" + dir));
         command.add("-Dintroduce.skeleton.service.name=" + name);
         command.add("-Dintroduce.skeleton.package=" + packagename);
         command.add("-Dintroduce.skeleton.package.dir=" + packagename.replace('.', File.separatorChar));
         command.add("-Dintroduce.skeleton.namespace.domain=" + namespacedomain);
         command.add("-Dintroduce.skeleton.resource.options=" + resourceOptions);
         command.add("-Dintroduce.skeleton.extensions=" + extensions);
 
         command.add("createService");
 
         logger.debug("CREATION: cmd: " + command);
         return command;
     }
 
 
     public static List<String> getAntSkeletonPostCreationCommand(String buildFileDir, String name, String dir,
         String packagename, String namespacedomain, String extensions) throws Exception {
         return getAntSkeletonPostCreationCommand(buildFileDir, name, dir, packagename, namespacedomain, extensions,
             false);
     }
 
 
     public static List<String> getAntSkeletonPostCreationCommand(String buildFileDir, String name, String dir,
         String packagename, String namespacedomain, String extensions, boolean debug) throws Exception {
         logger.debug("CREATION: builddir: " + buildFileDir);
         logger.debug("CREATION: destdir: " + dir);
         File dirF = new File(dir);
         if (!dirF.isAbsolute()) {
             dir = buildFileDir + File.separator + dir;
         }
 
         List<String> command = getAntCommandCall(buildFileDir, debug);
         command.add(escapeIfNecessary("-Dintroduce.skeleton.destination.dir=" + dir));
         command.add("-Dintroduce.skeleton.service.name=" + name);
         command.add("-Dintroduce.skeleton.package=" + packagename);
         command.add("-Dintroduce.skeleton.package.dir=" + packagename.replace('.', File.separatorChar));
         command.add("-Dintroduce.skeleton.namespace.domain=" + namespacedomain);
         command.add("-Dintroduce.skeleton.extensions=" + extensions);
         command.add("postCreateService");
 
         logger.debug("POST CREATION: cmd: " + command);
         return command;
     }
 
 
     static List<String> getAntCommandCall(String buildFileDir) throws Exception {
         return getAntCommandCall(buildFileDir, false);
     }
 
 
     static List<String> getAntCommandCall(String buildFileDir, boolean debug) throws Exception {
         List<String> command = new ArrayList<String>();
         
         if (isWindowsOS()) {
             command.add(getJavaHomePath() + "/bin/java.exe");
         } else {
             command.add(getJavaHomePath() + "/bin/java");
         }
 
         if (debug) {
             command.addAll(Arrays.asList(DEBUG_ANT_CALL_JAVA_OPTS));
         }
 
         command.add("-classpath");
         command.add(escapeIfNecessary(getAntLauncherJarLocation(System.getProperty("java.class.path"))));
         command.add("org.apache.tools.ant.launch.Launcher");
         command.add("-buildfile");
         command.add(escapeIfNecessary(buildFileDir + File.separator + "build.xml"));
 
         return command;
     }
     
     static String getJavaHomePath() {
         String javaHome = System.getenv("JAVA_HOME");
         if (isEmpty(javaHome)) {
             javaHome = System.getProperty("java.home");
         }
         return javaHome;
     }
     
     static boolean isEmpty(String value) {
         return value == null || value.trim().length() == 0;
     }
 
 
 
     static String getAntLauncherJarLocation(String path) throws Exception {
        String separator = isWindowsOS() ? ";" : ":";
         StringTokenizer pathTokenizer = new StringTokenizer(path, separator);
         while (pathTokenizer.hasMoreTokens()) {
             String pathElement = pathTokenizer.nextToken();
             if ((pathElement.indexOf("ant-launcher") != -1) && pathElement.endsWith(".jar")) {
                 return pathElement;
             }
         }
         throw new Exception("Unable to locate ant-launcher in classpath");
     }
    
 
     static String escapeIfNecessary(String s) {
         if (isWindowsOS() && needsQuoting(s)) {
             return winQuote(s);
         } else {
             return s;
         }
     }
 
 
     static boolean isWindowsOS() {
         if (isWindows == null) {
             String os = System.getProperty("os.name").toLowerCase();
             isWindows = Boolean.valueOf(os.contains("windows"));
         }
         return isWindows.booleanValue();
     }
 
 
     static boolean needsQuoting(String s) {
         int len = s.length();
         if (len == 0) // empty string have to be quoted
             return true;
         for (int i = 0; i < len; i++) {
             switch (s.charAt(i)) {
                 case ' ' :
                 case '\t' :
                 case '\\' :
                 case '"' :
                     return true;
             }
         }
         return false;
     }
 
 
     static String winQuote(String s) {
         if (!needsQuoting(s))
             return s;
         s = s.replaceAll("([\\\\]*)\"", "$1$1\\\\\"");
         s = s.replaceAll("([\\\\]*)\\z", "$1$1");
         return "\"" + s + "\"";
     }
 
 }
