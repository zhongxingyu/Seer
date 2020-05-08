 // Copyright FreeHEP, 2006.
 package org.freehep.maven.nar;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * 
  * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/maven/nar/NarInfo.java 8a7e57a69298 2006/08/21 22:03:24 duns $
  */
 public class NarInfo {
 
     public static final String NAR_PROPERTIES = "nar.properties";
     private String groupId, artifactId, version;
     private Properties info;
 	
 	public NarInfo(String groupId, String artifactId, String version) {
 		this.groupId = groupId;
         this.artifactId = artifactId;
         this.version = version;
         info = new Properties();
     }
     
     public boolean exists(JarFile jar) {
         return getNarPropertiesEntry(jar) != null;
     }
     
     public void read(JarFile jar) throws IOException {
         read(jar.getInputStream(getNarPropertiesEntry(jar)));
 	}
 
     private JarEntry getNarPropertiesEntry(JarFile jar) {
         return jar.getJarEntry("META-INF/nar/" + groupId + "/" + artifactId + "/" + NAR_PROPERTIES);
     }
     
     public void read(File file) throws IOException {
         read(new FileInputStream(file));
     }
     
     public void read(InputStream is) throws IOException {
         info.load(is);
//		info.list(System.out);
     }
 
     /**
      * No binding means user binding.
      * @param aol
      * @return
      */
 	public String getBinding(String aol) {
 		return getProperty(aol, "libs.binding", null);
 	}
 
     public void setBinding(String aol, String value) {
         setProperty(aol, "libs.binding", value);
     }
 
 	// FIXME replace with list of AttachedNarArtifacts
 	public String[] getAttachedNars(String aol, String type) {
 		String attachedNars = getProperty(aol, "nar."+type, null);
         return attachedNars != null ? attachedNars.split(",") : null;
 	}
 
     public void addNar(String aol, String type, String nar) {
         String nars = getProperty(aol, "nar."+type, null);
         nars = (nars == null) ? nar : nars + ", " +nar;
         setProperty(aol, "nar."+type, nars);
     }
 
     public void setNar(String aol, String type, String nar) {
         setProperty(aol, "nar."+type, nar);
     }
     
 	public String getAOL(String aol) {
 		return getProperty(aol, aol, aol);
 	}
 
 	public String getLibs(String aol) {
 		return getProperty(aol, "libs.names", artifactId+"-"+version);
 	}
 	
 	public String getSysLibs(String aol) {
 		return getProperty(aol, "syslibs.names", null);
 	}
     
     public void writeToFile(File file) throws IOException {
         info.store(new FileOutputStream((file)), "NAR Properties for "+groupId+"."+artifactId+"-"+version);
     }
     
 	private String getProperty(String aol, String key, String defaultValue) {
         if (key == null) return defaultValue;
 		String value = info.getProperty(key, defaultValue);
		value = aol == null ? value : info.getProperty(aol+"."+key, value);
//		System.err.println("getProperty("+aol+", "+key+", "+defaultValue+") = " + value);
		return value;
 	}
     
     private void setProperty(String aol, String key, String value) {
         if (aol == null) {
             info.setProperty(key, value);
         } else {
             info.setProperty(aol+"."+key, value);
         }
     }
 }
