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
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
 //   Revision 1.6  2006/03/03 23:12:38  jomifred
 //   no message
 //
 //   Revision 1.5  2006/03/02 01:41:14  jomifred
 //   the jIDE package was remove, the writeScriptInterface's methods was moved to MASLauncher
 //
 //   Revision 1.4  2006/02/24 14:53:26  jomifred
 //   no message
 //
 //   Revision 1.3  2006/02/18 16:04:56  jomifred
 //   no message
 //
 //   Revision 1.2  2006/02/18 15:24:50  jomifred
 //   changes in many files to detach jason kernel from any infrastructure implementation
 //
 //   Revision 1.1  2006/01/14 15:18:58  jomifred
 //   Config and some code of RunMAS was moved to package plugin
 //
 //   Revision 1.6  2006/01/11 15:14:39  jomifred
 //   add close all befere opening a mas2j project
 //
 //   Revision 1.5  2006/01/04 02:54:41  jomifred
 //   using java log API instead of apache log
 //
 //   Revision 1.4  2006/01/02 13:49:00  jomifred
 //   add plan unique id, fix some bugs
 //
 //   Revision 1.3  2005/12/30 20:40:16  jomifred
 //   new features: unnamed var, var with annots, TE as var
 //
 //   Revision 1.1  2005/12/08 20:06:02  jomifred
 //   changes for JasonIDE plugin
 //
 //----------------------------------------------------------------------------
 
 package jason.jeditplugin;
 
 import jason.infra.centralised.CentralisedFactory;
 import jason.infra.saci.SaciFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 /** 
  * Jason configuration (used by JasonID to generate the project's scripts)
  *  
  * @author jomi
  */
 public class Config extends Properties {
 
 	public static final String JASON_JAR = "jasonJar";
 	public static final String SACI_JAR  = "saciJar";
 	//public static final String LOG4J_JAR = "log4jJar";
 	public static final String JAVA_HOME = "javaHome";
 	public static final String ANT_LIB  = "antLib";
 	public static final String RUN_AS_THREAD = "runCentralisedInsideJIDE";
 	public static final String SHELL_CMD = "shellCommand";
 	public static final String CLOSEALL = "closeAllBeforeOpenMAS2J";
 		
 	private static Config singleton = null;
 	
 	public static Config get() {
 		if (singleton == null) {
 			singleton = new Config();
 			singleton.load();
 		}
 		return singleton;
 	}
 	
 	private Config() {
 	}
 	
 	public File getUserConfFile() {
     	return new File(System.getProperties().get("user.home") + File.separator + ".jason/user.properties");
     }
 
 	public void load() {
     	try {
     		File f = getUserConfFile();
     		if (f.exists()) {
     			super.load(new FileInputStream(f));
     		}
     	} catch (Exception e) {
     		System.err.println("Error reading preferences");
     		e.printStackTrace();
     	}
 	}
 	
 	public boolean getBoolean(String key) {
 		return get(key)!=null && get(key).equals("true");
 	}
 	
 	public String getJasonJar() {
 		return getProperty(JASON_JAR);
 	}
 	public String getSaciJar() {
 		return getProperty(SACI_JAR);
 	}
 	public String getJavaHome() {
 		return getProperty(JAVA_HOME);
 	}
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
 
 	public boolean runAsInternalTread() {
 		return false; // it not works with jedit
 		/*
 		String r = getProperty(RUN_AS_THREAD);
 		return r != null && r.equals("true");
 		*/
 	}
 	
 	public String getShellCommand() {
 		return getProperty(SHELL_CMD);
 	}
 	
 	public void fix() {
     	tryToFixJarFileConf(JASON_JAR, "jason.jar", 300000);
     	tryToFixJarFileConf(SACI_JAR,  "saci.jar",  300000);
     	//tryToFixJarFileConf(LOG4J_JAR, "log4j.jar", 350000);
 		
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
	        	String antlib = new File(getJasonJar()).getParentFile().getParentFile().getAbsolutePath() + File.separator + "lib" + File.separator;
 	        	if (checkAntLib(antlib)) {
	        		put(ANT_LIB, antlib);
 	        	} else {
	        		System.out.println("nao ok para "+antlib);
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
         
         // Default infrastructures
         put("infrastructure.Centralised", CentralisedFactory.class.getName());
         put("infrastructure.Saci", SaciFactory.class.getName());
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
 
     public String getInfrastructureFactoryClass(String infraId) {
     	return get("infrastructure."+infraId).toString();
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
         	System.out.println("Wrong configuration for "+jarName+", current is "+jarFile);
 
         	// try to get from classpath
         	jarFile = getJavaHomePathFromClassPath(jarName);
         	if (checkJar(jarFile)) {
         		put(jarEntry, jarFile);
     			System.out.println("found at "+jarFile);
     			return;
         	} 
     		
         	// try current dir
         	jarFile = "."+File.separator+jarName;
     		if (checkJar(jarFile)) {
         		put(jarEntry, new File(jarFile).getAbsolutePath());
     			System.out.println("found at "+jarFile);
     			return;
     		}
 
         	// try current dir + lib
     		jarFile = ".."+File.separator+"lib"+File.separator+jarName;
     		if (checkJar(jarFile)) {
         		put(jarEntry, new File(jarFile).getAbsolutePath());
     			System.out.println("found at "+jarFile);
     			return;
     		}
     		jarFile = "."+File.separator+"lib"+File.separator+jarName;
     		if (checkJar(jarFile)) {
         		put(jarEntry, new File(jarFile).getAbsolutePath());
     			System.out.println("found at "+jarFile);
     			return;
     		}
 
         	// try current dir + bin
     		jarFile = "."+File.separator+"bin"+File.separator+jarName;
     		if (checkJar(jarFile)) {
         		put(jarEntry, new File(jarFile).getAbsolutePath());
     			System.out.println("found at "+jarFile);
     			return;
     		}
     		
     		// try from java web start
     		String jwsDir = System.getProperty("jnlpx.deployment.user.home");
     		if (jwsDir == null) {
     			// try another property (windows)
     			try {
     				jwsDir = System.getProperty("deployment.user.security.trusted.certs");
     				jwsDir = new File(jwsDir).getParentFile().getParent();
     			} catch (Exception e) {}
     		}
     		if (jwsDir != null) {
         		jarFile = findFile(new File(jwsDir), jarName, minSize);
         		System.out.print("Searching "+jarName+" in "+jwsDir+" ... ");
         		if (jarFile != null && checkJar(jarFile)) {
         			System.out.println("found at "+jarFile);
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
     		for (int i=0; i<files.length; i++) {
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
         } catch (Exception e) {}
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
         } catch (Exception e) {}
         return false;
     }
     
     public static boolean checkAntLib(String al) {
         try {
             File antjar = new File(al + "ant.jar");
             if (antjar.exists()) {
         		return true;
             }
         } catch (Exception e) {}
         return false;
     }
 
     static String getJavaHomePathFromClassPath(String file) {
 		StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
 		while (st.hasMoreTokens()) {
 			String token = st.nextToken();
 			if (token.endsWith(file)) {
 				return new File(token).getAbsolutePath();
 			}
 		}
 		return null;
  	}
     
 	public static void main(String[] args) {
 		Config.get().fix();
 	}
 }
