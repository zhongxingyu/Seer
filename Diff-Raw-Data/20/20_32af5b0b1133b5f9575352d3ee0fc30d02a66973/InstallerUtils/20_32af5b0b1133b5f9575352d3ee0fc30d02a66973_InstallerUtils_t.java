 /**
  * 
  */
 package org.cagrid.installer.util;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.installer.steps.Constants;
 import org.w3c.dom.Node;
 
 
 /**
  * @author <a href="mailto:joshua.phillips@semanticbits.com">Joshua Phillips</a>
  */
 public class InstallerUtils {
 
     private static final Log logger = LogFactory.getLog(InstallerUtils.class);
 
 
     public InstallerUtils() {
 
     }
 
 
     public static boolean isWindows() {
         return System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
     }
 
 
     public static void showError(String msg) {
         JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
     }
 
 
     public static void addToClassPath(String s) throws IOException {
         File f = new File(s);
         addToClassPath(f);
     }
 
 
     public static void addToClassPath(File f) throws IOException {
         addToClassPath(f.toURL());
     }
 
 
     public static void addToClassPath(URL u) throws IOException {
 
         URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
         Class sysclass = URLClassLoader.class;
 
         try {
             Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
             method.setAccessible(true);
             method.invoke(sysloader, new Object[]{u});
         } catch (Throwable t) {
             t.printStackTrace();
             throw new IOException("Error, could not add URL to system classloader");
         }
 
     }
 
 
     public static void copyFile(String from, String to) throws IOException {
     	File inCannon  = new File(from);
     	File outCannon = new File(to);
     	
     	// [#20196] Installer overwrites the installed credentials if already installed credentials are selected
         // Applies to Windows only
         if (inCannon.equals(outCannon)) {
             return;
         }
 
         // ensure the output file location exists
         outCannon.getParentFile().mkdirs();
 
         BufferedInputStream fis = new BufferedInputStream(new FileInputStream(inCannon));
         BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(outCannon));
 
         // a temporary buffer to read into
         byte[] tmpBuffer = new byte[8192];
         int len = 0;
         while ((len = fis.read(tmpBuffer)) != -1) {
             // add the temp data to the output
             fos.write(tmpBuffer, 0, len);
         }
         // close the input stream
         fis.close();
         // close the output stream
         fos.flush();
         fos.close();
     }
 
 
     public static String toString(Node node) throws Exception {
         StringWriter w = new StringWriter();
         Source s = new DOMSource(node);
         Result r = new StreamResult(w);
         Transformer t = TransformerFactory.newInstance().newTransformer();
         // t.setOutputProperty("omit-xml-declaration", "yes");
         t.setOutputProperty("indent", "yes");
         t.transform(s, r);
         return w.getBuffer().toString();
     }
 
 
     public static boolean isEmpty(String value) {
         return value == null || value.trim().length() == 0;
     }
 
 
     public static void setUpCellRenderer(JTable table) {
         DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                 boolean hasFocus, int row, int column) {
                 Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                     column);
                 setBorder(BorderFactory.createEtchedBorder());
                 return renderer;
             }
         };
         int colCount = table.getColumnCount();
         for (int i = 0; i < colCount; i++) {
             TableColumn col = table.getColumnModel().getColumn(i);
             col.setCellRenderer(r);
         }
     }
 
 
     public static GridBagConstraints getGridBagConstraints(int x, int y) {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = x;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1;
         gbc.gridy = y;
         return gbc;
     }
 
 
     public static String getDbNameFromJdbcUrl(String jdbcUrl) {
         return jdbcUrl.substring(jdbcUrl.lastIndexOf("/") + 1);
     }
 
 
     public static String getJdbcBaseFromJdbcUrl(String jdbcUrl) {
         return jdbcUrl.substring(0, jdbcUrl.lastIndexOf("/"));
     }
 
 
     public static boolean checkCaGridIsValid(String home) {
         boolean isValid = false;
         try {
             File buildFile = new File(home + File.separator + "build.xml");
             File projectsDir = new File(home + File.separator + "projects");
             isValid = buildFile.exists() && projectsDir.exists() && projectsDir.isDirectory();
         } catch (Exception ex) {
             logger.debug("Error checking caGrid installation: " + ex.getMessage(), ex);
         }
         return isValid;
     }
 
 
     public static String getJavaVersion() {
 
         String version = null;
 
         String java = "java";
         if (isWindows()) {
             java += ".exe";
         }
         try {
             Process p = Runtime.getRuntime().exec(new String[]{getJavaHomePath() + "/bin/" + java, "-version"},
                 new String[0]);
 
             StringBuffer stdout = new StringBuffer();
             new IOThread(p.getInputStream(), System.out, stdout).start();
 
             StringBuffer stderr = new StringBuffer();
             new IOThread(p.getErrorStream(), System.err, stderr).start();
 
             int code = p.waitFor();
 
             logger.info("CODE: " + code);
             logger.info("STDOUT: " + stdout);
             logger.info("STDERR: " + stderr);
 
             version = stdout.toString();
             if (InstallerUtils.isEmpty(version)) {
                 version = stderr.toString();
             }
             try {
                 version = version.substring(version.indexOf("\"") + 1, version.lastIndexOf("\""));
             } catch (Exception ex) {
                 logger.warn("Couldn't parse out version from '" + version + "'");
             }
 
         } catch (Exception ex) {
             throw new RuntimeException("Error checking java version: " + ex.getMessage(), ex);
         }
 
         return version;
     }
 
 
     public static String getJavaHomePath() {
         String javaHome = System.getenv("JAVA_HOME");
         if (isEmpty(javaHome)) {
             javaHome = System.getProperty("java.home");
         }
         return javaHome;
     }
 
 
     public static String getJavacVersion() {
 
         String version = "version";
 
         String javacCmd = "javac";
         if (isWindows()) {
             javacCmd += ".exe";
         }
 
         try {
             Process p = Runtime.getRuntime().exec(new String[]{javacCmd, "-help", "-version"}, new String[0]);
 
             StringBuffer stdout = new StringBuffer();
             new IOThread(p.getInputStream(), System.out, stdout).start();
 
             StringBuffer stderr = new StringBuffer();
             new IOThread(p.getErrorStream(), System.err, stderr).start();
 
             int code = p.waitFor();
 
             logger.info("CODE: " + code);
             logger.info("STDOUT: " + stdout);
             logger.info("STDERR: " + stderr);
 
             String out = stdout.toString();
             if (InstallerUtils.isEmpty(out)) {
                 out = stderr.toString();
             }
             try {
                 int idx = out.lastIndexOf("javac");
                 version = out.substring(idx + "javac".length() + 1).trim();
                 logger.info("Found javac version '" + version + "'");
             } catch (Exception ex) {
                 logger.warn("Couldn't parse out javac version from '" + version + "'");
             }
 
         } catch (Exception ex) {
             logger.warn("Error checking javac version: " + ex.getMessage(), ex);
         }
 
         return version;
     }
 
 
     public static boolean checkTomcatVersion(String home) {
         boolean correctVersion = false;
        logger.info("Checking Tomcat version...");
         try {
             String javaHome = getJavaHomePath();
             String[] envp = new String[]{"JAVA_HOME=" + javaHome, "CATALINA_HOME=" + home};
 
             String[] cmd = null;
             if (InstallerUtils.isWindows()) {
            	// change to use catalina.bat due to a version.bat bug in Tomcat 5.5.27 
                cmd = new String[]{"cmd.exe", "/c", home + "/bin/catalina.bat", "version"};
             } else {
                 cmd = new String[]{"sh", home + "/bin/version.sh"};
             }
            
            Process p = Runtime.getRuntime().exec(cmd, envp);          
             StringBuffer stdout = new StringBuffer();
            
             new IOThread(p.getInputStream(), System.out, stdout).start();
             StringBuffer stderr = new StringBuffer();
            
             new IOThread(p.getErrorStream(), System.err, stderr).start();
            
             int code = p.waitFor();
            
             correctVersion = stdout.toString().indexOf("Apache Tomcat/5.5.27") != -1;
             if (!correctVersion) {
 
                 logger.warn("The Tomcat version utility indicates " + "that the correct tomcat version is not "
                     + "installed. Here is the output from that tool: \n" + stdout);
 
                 logger.warn("Exit code: " + code);
                 logger.warn("STDERR:\n" + stderr);
 
             }
         } catch (Exception ex) {
             logger.warn("Error checking Tomcat version: " + ex.getMessage(), ex);
         }
         return correctVersion;
 
     }
     
     //TODO: implement
     public static boolean checkJBossVersion(String home) {
         boolean correctVersion = true;
         
         return correctVersion;
 
     }
 
 
     public static boolean checkGlobusVersion(String home) {
         return home.indexOf("4.0.3") != -1;
     }
 
 
     public static boolean checkAntVersion(String home) {
         boolean correctVersion = false;
         try {
             String[] envp = new String[]{"JAVA_HOME=" + getJavaHomePath(), "ANT_HOME=" + home};
 
             String[] cmd = null;
             if (InstallerUtils.isWindows()) {
                 cmd = new String[]{"cmd.exe", "/c", home + "/bin/ant.bat", "-version"};
             } else {
                 cmd = new String[]{"sh", home + "/bin/ant", "-version"};
             }
 
             Process p = Runtime.getRuntime().exec(cmd, envp);
             StringBuffer stdout = new StringBuffer();
             new IOThread(p.getInputStream(), System.out, stdout).start();
             p.waitFor();
             correctVersion = stdout.toString().indexOf("Apache Ant version 1.7.0") != -1;
         } catch (Exception ex) {
             logger.warn("Error checking Ant version: " + ex.getMessage(), ex);
         }
         return correctVersion;
     }
 
 
     public static String trim(String s) {
         String trimmed = s;
         if (trimmed != null) {
             trimmed = trimmed.trim();
         }
         return trimmed;
     }
 
 
     public static String getInstallerDirBase() {
         return System.getProperty("user.home") + "/.cagrid/installer";
     }
 
 
     public static String buildInstallerDirPath(String cagridVersion) {
         return InstallerUtils.getInstallerDirBase() + "-" + cagridVersion;
     }
 
 
     public static void handleException(String msg, Exception ex) {
     
         String htmlMsg = "";
         if (!isEmpty(msg)) {
             htmlMsg = "<html><body>" + msg.replaceAll("\n", "<br>") + "</body></html>";
         }
     
         JLabel msgLabel = new JLabel(htmlMsg);
     
         JOptionPane.showMessageDialog(null, msgLabel, "Error", JOptionPane.ERROR_MESSAGE);
         if (ex != null) {
             logger.error(msg, ex);
             throw new RuntimeException(msg, ex);
         } else {
             logger.error(msg);
             throw new RuntimeException(msg);
         }
     }
 
 
     public static void assertCorrectJavaVersion(Map<String, String> defaultState) throws Exception {
         String versionPattern = defaultState.get(Constants.JAVA_VERSION_PATTERN);
         if (versionPattern == null) {
             throw new Exception("Couldn't find version pattern property.");
         }
         String home = getJavaHomePath();
         String version = getJavaVersion();
         logger.info("At '" + home + "', found Java version: " + version);
         if (!version.matches(versionPattern)) {
             throw new Exception("The version of Java found at '" + home + "' is not correct. Found '" + version
                 + "'. Expected version to match '" + versionPattern + "'.\n"
                 + "Set the JAVA_HOME environment variable to"
                 + " point to where you have installed the correct version of" + " Java before running the installer.");
         }
     }
 
 }
