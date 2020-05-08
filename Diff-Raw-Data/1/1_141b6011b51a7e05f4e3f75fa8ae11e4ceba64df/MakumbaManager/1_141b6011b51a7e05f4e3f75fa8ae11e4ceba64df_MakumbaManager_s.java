 package org.makumba.parade.model.managers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.Logger;
 import java.util.zip.ZipException;
 
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 import org.makumba.parade.tools.ParadeLogger;
 
 public class MakumbaManager implements RowRefresher, ParadeManager {
 
     private static Logger logger = ParadeLogger.getParadeLogger(MakumbaManager.class.getName());
 
     private boolean hasMakumba = false;
 
     public void softRefresh(Row row) {
         logger.fine("Refreshing row information for row " + row.getRowname());
 
         String root = row.getRowpath() + File.separator + row.getWebappPath();
         row.setVersion(getMakumbaVersion(root, row));
         row.setDb(getMakumbaDatabase(root));
     }
 
     public void hardRefresh(Row row) {
         softRefresh(row);
     }
 
     public String getMakumbaVersion(String p, Row r) {
         final String path = p;
         String version = "No makumba.jar";
 
         try {
             java.io.File fl = new java.io.File((path + "/WEB-INF/lib/makumba.jar").replace('/',
                     java.io.File.separatorChar));
 
             if (!fl.exists()) {
                 java.io.File lib = new java.io.File((path + "/WEB-INF/lib/").replace('/', java.io.File.separatorChar));
                 String[] libs = lib.list();
                 Vector<String> mak = new Vector<String>();
                 if (libs == null) {
                     logger.warning("No WEB-INF/lib directory found for row " + r.getRowname()
                             + ". Cannot detected Makumba version.");
                 } else {
                     for (String element : libs) {
                         if (element.indexOf("makumba") > -1 && element.endsWith(".jar")) {
                             mak.add(element);
                         }
                     }
                 }
 
                 if (mak.size() == 0) {
                     return "No makumba.jar";
                 } else if (mak.size() > 1) {
                     hasMakumba = true;
                     String result = "Error: Two makumba JARs found: ";
                     for (String m : mak) {
                         result += m + " ";
                     }
                     result += "! Please remove one";
                 } else {
                     hasMakumba = true;
                     String makPath = path + "/WEB-INF/lib/" + mak.get(0).replace('/', java.io.File.separatorChar);
                     if (makPath.endsWith(java.io.File.separator)) {
                         makPath = makPath.substring(0, makPath.length() - 1);
                     }
                     fl = new java.io.File(makPath);
                 }
 
             }
 
             hasMakumba = true;
 
             try {
 
                 JarFile jar = new JarFile(fl);
                 Manifest mf = jar.getManifest();
                 Attributes att = mf.getAttributes("Makumba");
                 version = att.getValue("Version");
                 jar.close();
 
             } catch (ZipException ze) {
                 if (!(ze.getMessage().indexOf("error in opening zip file") > -1)) {
                     // we ignore it in the other cases, it happens when deleting a mak.jar
                     ze.printStackTrace();
                 }
             }
             return version;
         } catch (Exception e) {
             // when no version info is inside JAR's META-INF/manifest.mf file
             // may be true for old Makumba versions, but they aren't used anymore
             e.printStackTrace();
         }
         return "Error detecting Makumba version";
     }
 
     public String getMakumbaDatabase(String root) {
 
         root = (root + "/WEB-INF/classes/").replace('/', File.separatorChar);
         File f = new File(root + "MakumbaDatabase.properties");
         if (!f.exists())
             return "No MakumbaDatabase.properties found";
         Properties p = new Properties();
         try {
             p.load(new FileInputStream(f));
         } catch (IOException e) {
             return "Invalid MakumbaDatabase.properties";
         }
         return "Default database: " + (String) p.get("default");
     }
 
     public void newRow(String name, Row r, Map<String, String> m) {
         // TODO Auto-generated method stub
 
     }
 
 }
