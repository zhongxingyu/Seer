 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.jeditplugin;
 
 import jason.asSemantics.TransitionSystem;
 import jason.infra.centralised.CentralisedFactory;
 import jason.infra.jade.JadeFactory;
 import jason.infra.saci.SaciFactory;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 /**
  * Jason configuration (used by JasonID to generate the project's scripts)
  * 
  * @author jomi
  */
 public class Config extends Properties {
 
 	private static final long  serialVersionUID = 1L;
 
 	/** path to jason.jar */
 	public static final String JASON_JAR     = "jasonJar";
 	
 	/** path to saci.jar */
     public static final String SACI_JAR      = "saciJar";
     
     /** path to ant home (jar directory) */
     public static final String ANT_LIB       = "antLib";
 
     /** path to jade.jar */
     public static final String JADE_JAR      = "jadeJar";
     
     /** runtime jade arguments (the same used in jade.Boot) */
     public static final String JADE_ARGS     = "jadeArgs";
     
     /** boolean, whether to start jade RMA or not */
     public static final String JADE_RMA      = "jadeRMA";
 
     /** boolean, whether to start jade Sniffer or not */
     public static final String JADE_SNIFFER  = "jadeSniffer";
 
     /** path to java home */
     public static final String JAVA_HOME     = "javaHome";
     
     public static final String RUN_AS_THREAD = "runCentralisedInsideJIDE";
     public static final String SHELL_CMD     = "shellCommand";
     public static final String CLOSEALL      = "closeAllBeforeOpenMAS2J";
     public static final String CHECK_VERSION = "checkLatestVersion";
     public static final String WARN_SING_VAR = "warnSingletonVars";
 
     private static Config      singleton     = null;
 
     public static Config get() {
         if (singleton == null) {
             singleton = new Config();
             if (!singleton.load()) {
            	singleton.fix();
            	singleton.store();
             }
         }
         return singleton;
     }
 
     private Config() {
     }
 
     /** returns the file where the user preferences are stored */
     public File getUserConfFile() {
         return new File(System.getProperties().get("user.home") + File.separator + ".jason/user.properties");
     }
 
     /** Returns true if the file was loaded */
     public boolean load() {
         try {
             File f = getUserConfFile();
             if (f.exists()) {
                 super.load(new FileInputStream(f));
                 return true;
             }
         } catch (Exception e) {
             System.err.println("Error reading preferences");
             e.printStackTrace();
         }
         return false;
     }
 
     public boolean getBoolean(String key) {
         return "true".equals(get(key));
     }
 
     /** Returns the full path to the jason.jar file */
     public String getJasonJar() {
         return getProperty(JASON_JAR);
     }
     
     /** returns the jason home (based on jason.jar) */
     public String getJasonHome() {
         try {
         	return new File(getJasonJar()).getParentFile().getParent();
         } catch (Exception e) {
         	e.printStackTrace();
         }
     	return "";
     }
 
     /** Returns the full path to the saci.jar file */
     public String getSaciJar() {
         return getProperty(SACI_JAR);
     }
 
     /** Returns the full path to the jade.jar file */
     public String getJadeJar() {
         return getProperty(JADE_JAR);
     }
 
     /** Return the jade args (those used in jade.Boot) */
     public String getJadeArgs() {
         return getProperty(JADE_ARGS);
     }
 
     public String[] getJadeArrayArgs() {
         List<String> ls = new ArrayList<String>();
         String jadeargs = getProperty(JADE_ARGS);
         if (jadeargs != null && jadeargs.length() > 0) {
             StringTokenizer t = new StringTokenizer(jadeargs);
             while (t.hasMoreTokens()) {
                 ls.add(t.nextToken());
             }
         }
         String[] as = new String[ls.size()];
         for (int i=0; i<ls.size(); i++) {
             as[i] = ls.get(i);
         }
         return as;
     }
 
     /** Returns the path to the java  home directory */    
     public String getJavaHome() {
         return getProperty(JAVA_HOME);
     }
 
     /** Returns the path to the ant home directory (where its jars are stored) */    
     public String getAntLib() {
         return getProperty(ANT_LIB);
     }
 
     public void setJavaHome(String jh) {
         if (jh != null) {
             jh = new File(jh).getAbsolutePath();
             if (!jh.endsWith(File.separator)) {
                 jh += File.separator;
             }
             put(JAVA_HOME, jh);
         }
     }
 
     public void setAntLib(String al) {
         if (al != null) {
             al = new File(al).getAbsolutePath();
             if (!al.endsWith(File.separator)) {
                 al += File.separator;
             }
             put(ANT_LIB, al);
         }
     }
 
     public String getShellCommand() {
         return getProperty(SHELL_CMD);
     }
 
     /** Set most important parameters with default values */
     public void fix() {
         tryToFixJarFileConf(JASON_JAR, "jason.jar", 600000);
         tryToFixJarFileConf(SACI_JAR, "saci.jar", 300000);
         tryToFixJarFileConf(JADE_JAR, "jade.jar", 800000);
 
         // fix java home
         if (get(JAVA_HOME) == null || !checkJavaHomePath(getProperty(JAVA_HOME))) {
             String javaHome = System.getProperty("java.home");
             if (checkJavaHomePath(javaHome)) {
                 setJavaHome(javaHome);
             } else {
                 setJavaHome(File.separator);
             }
         }
 
         // fix ant lib
         if (get(ANT_LIB) == null || !checkAntLib(getAntLib())) {
             try {
                 String antlib = new File(getJasonJar()).getParentFile().getParentFile().getAbsolutePath() + File.separator + "lib";
                 if (checkAntLib(antlib)) {
                     setAntLib(antlib);
                 } else {
                     antlib = new File(".") + File.separator + "lib";
                     if (checkAntLib(antlib)) {
                         setAntLib(antlib);
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         // Jason version
         put("version", getJasonRunningVersion());
 
         // font
         if (get("font") == null) {
             put("font", "Monospaced");
         }
         if (get("fontSize") == null) {
             put("fontSize", "14");
         }
 
         // shell command
         if (get(SHELL_CMD) == null) {
             if (System.getProperty("os.name").startsWith("Windows 9")) {
                 put(SHELL_CMD, "command.com /e:1024 /c ");
             } else if (System.getProperty("os.name").indexOf("indows") > 0) {
                 put(SHELL_CMD, "cmd /c ");
             } else {
                 put(SHELL_CMD, "/bin/sh ");
             }
         }
 
         // close all
         if (get(CLOSEALL) == null) {
             put(CLOSEALL, "true");
         }
 
         if (get(CHECK_VERSION) == null) {
             put(CHECK_VERSION, "true");
         }
 
         // jade args
         if (getProperty(JADE_RMA) == null) {
             put(JADE_RMA, "true");
         }
         
         // Default infrastructures
         put("infrastructure.Centralised", CentralisedFactory.class.getName());
         put("infrastructure.Saci", SaciFactory.class.getName());
         put("infrastructure.Jade", JadeFactory.class.getName());
     }
 
     public void store() {
         try {
             File f = getUserConfFile();
             if (!f.getParentFile().exists()) {
                 f.getParentFile().mkdirs();
             }
 
             super.store(new FileOutputStream(f), "Jason user configuration");
         } catch (Exception e) {
             System.err.println("Error writting preferences");
             e.printStackTrace();
         }
     }
 
     public String[] getAvailableInfrastructures() {
     	try {
     		List<String> infras = new ArrayList<String>();
     		infras.add("Centralised"); // set Centralised as the first
     		for (Object k: keySet()) {
     			String sk = k.toString();
     			int p = sk.indexOf(".");
     			if (p > 0 && sk.startsWith("infrastructure") && p == sk.lastIndexOf(".")) { // only one "."
     				String newinfra = sk.substring(p+1);
     				if (!infras.contains(newinfra)) {
     					infras.add(newinfra);
     				}
     			}
     		}
     		if (infras.size() > 0) {
 	    		// copy infras to a array
 	    		String[] r = new String[infras.size()];
 	    		for (int i=0; i<r.length; i++) {
 	    			r[i] = infras.get(i);
 	    		}
 	    		return r;
     		}
     	} catch (Exception e) {
             System.err.println("Error getting user infrastructures.");    		
     	}
 		return new String[] {"Centralised","Jade","Saci"};
     }
     
     public String getInfrastructureFactoryClass(String infraId) {
         return get("infrastructure." + infraId).toString();
     }
 
     public String getJasonRunningVersion() {
         try {
             Properties p = new Properties();
             p.load(Config.class.getResource("/dist.properties").openStream());
             return p.getProperty("version") + "." + p.getProperty("release");
         } catch (Exception ex) {
             return "?";
         }
     }
 
     void tryToFixJarFileConf(String jarEntry, String jarName, int minSize) {
         String jarFile = getProperty(jarEntry);
         if (jarFile == null || !checkJar(jarFile)) {
             System.out.println("Wrong configuration for " + jarName + ", current is " + jarFile);
 
             // try to get from classpath
             jarFile = getJavaHomePathFromClassPath(jarName);
             if (checkJar(jarFile)) {
                 put(jarEntry, jarFile);
                 System.out.println("found at " + jarFile);
                 return;
             }
 
             // try current dir
             jarFile = "." + File.separator + jarName;
             if (checkJar(jarFile)) {
                 put(jarEntry, new File(jarFile).getAbsolutePath());
                 System.out.println("found at " + jarFile);
                 return;
             }
 
             // try current dir + lib
             jarFile = ".." + File.separator + "lib" + File.separator + jarName;
             if (checkJar(jarFile)) {
                 put(jarEntry, new File(jarFile).getAbsolutePath());
                 System.out.println("found at " + jarFile);
                 return;
             }
             jarFile = "." + File.separator + "lib" + File.separator + jarName;
             if (checkJar(jarFile)) {
                 put(jarEntry, new File(jarFile).getAbsolutePath());
                 System.out.println("found at " + jarFile);
                 return;
             }
 
             // try current dir + bin
             jarFile = "." + File.separator + "bin" + File.separator + jarName;
             if (checkJar(jarFile)) {
                 put(jarEntry, new File(jarFile).getAbsolutePath());
                 System.out.println("found at " + jarFile);
                 return;
             }
 
             // try from java web start
             String jwsDir = System.getProperty("jnlpx.deployment.user.home");
             if (jwsDir == null) {
                 // try another property (windows)
                 try {
                     jwsDir = System.getProperty("deployment.user.security.trusted.certs");
                     jwsDir = new File(jwsDir).getParentFile().getParent();
                 } catch (Exception e) {
                 }
             }
             if (jwsDir != null) {
                 jarFile = findFile(new File(jwsDir), jarName, minSize);
                 System.out.print("Searching " + jarName + " in " + jwsDir + " ... ");
                 if (jarFile != null && checkJar(jarFile)) {
                     System.out.println("found at " + jarFile);
                     put(jarEntry, jarFile);
                 } else {
                     put(jarEntry, File.separator);
                 }
             }
         }
 
     }
 
     static String findFile(File p, String file, int minSize) {
         if (p.isDirectory()) {
             File[] files = p.listFiles();
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     String r = findFile(files[i], file, minSize);
                     if (r != null) {
                         return r;
                     }
                 } else {
                     if (files[i].getName().endsWith(file) && files[i].length() > minSize) {
                         return files[i].getAbsolutePath();
                     }
                 }
             }
         }
         return null;
     }
 
     public static boolean checkJar(String jar) {
         try {
             if (jar != null && new File(jar).exists() && jar.endsWith(".jar")) {
                 return true;
             }
         } catch (Exception e) {
         }
         return false;
     }
 
     public static boolean checkJavaHomePath(String javaHome) {
         try {
             if (!javaHome.endsWith(File.separator)) {
                 javaHome += File.separator;
             }
             File javac1 = new File(javaHome + "bin" + File.separatorChar + "javac");
             File javac2 = new File(javaHome + "bin" + File.separatorChar + "javac.exe");
             if (javac1.exists() || javac2.exists()) {
                 return true;
             }
         } catch (Exception e) {
         }
         return false;
     }
 
     public static boolean checkAntLib(String al) {
         try {
             if (!al.endsWith(File.separator)) {
                 al = al + File.separator;
             }
             File antjar = new File(al + "ant.jar");
             if (antjar.exists()) {
                 return true;
             }
         } catch (Exception e) {
         }
         return false;
     }
 
     public static boolean isWindows() {
         return System.getProperty("os.name").startsWith("Windows");
     }
     
     static private String getJavaHomePathFromClassPath(String file) {
         StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
         while (st.hasMoreTokens()) {
             String token = st.nextToken();
             if (token.endsWith(file)) {
                 return new File(token).getAbsolutePath();
             }
         }
         return null;
     }
 
     
     public String getTemplate(String templateName) {
         try {
             String nl = System.getProperty("line.separator");
             // get template
             BufferedReader in;
             
             // if there is jason/src/xml/build-template.xml, use it; otherwise use the file in jason.jar
             File bt = new File("src/templates/"+templateName);
             if (bt.exists()) {
                 in = new BufferedReader(new FileReader(bt));                
             } else {
                 bt = new File("../src/templates/"+templateName);
                 if (bt.exists()) {
                     in = new BufferedReader(new FileReader(bt));
                 } else {
                     in = new BufferedReader(new InputStreamReader(TransitionSystem.class.getResource("/templates/"+templateName).openStream()));
                 }
             }
             
             StringBuilder scriptBuf = new StringBuilder();
             String line = in.readLine();
             while (line != null) {
                 scriptBuf.append(line + nl);
                 line = in.readLine();
             }
             return scriptBuf.toString();
         } catch (Exception e) {
             System.err.println("Error reading template: " + templateName);
             e.printStackTrace();
             return null;
         }
     }
     
     public static void main(String[] args) {
         Config.get().fix();
         Config.get().store();
     }
 }
