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
 import java.util.zip.ZipException;
 
 import org.apache.log4j.Logger;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowMakumba;
 import org.makumba.parade.model.RowWebapp;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 
 public class MakumbaManager implements RowRefresher, ParadeManager {
 
     private static Logger logger = Logger.getLogger(MakumbaManager.class.getName());
     
     private boolean hasMakumba = false;
 
     public void softRefresh(Row row) {
         logger.debug("Refreshing row information for row " + row.getRowname());
 
         RowMakumba makumbadata = new RowMakumba();
         makumbadata.setDataType("makumba");
 
         String root = row.getRowpath() + File.separator
                 + ((RowWebapp) row.getRowdata().get("webapp")).getWebappPath();
         makumbadata.setVersion(getMakumbaVersion(root, row));
         makumbadata.setDb(getMakumbaDatabase(root));
         
         makumbadata.setHasMakumba(hasMakumba);
 
         row.addManagerData(makumbadata);
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
                     logger.warn("No WEB-INF/lib directory found for row " + r.getRowname()
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
                    return "Two makumba JARs found! Please remove one";
                 } else {
                     hasMakumba = true;
                     String makPath = path + "/WEB-INF/lib/" + mak.get(0).replace('/', java.io.File.separatorChar);
                     if (makPath.endsWith(java.io.File.separator)) {
                         makPath = makPath.substring(0, makPath.length() - 1);
                     }
                     fl = new java.io.File(makPath);
                 }
 
             }
             
             hasMakumba=true;
 
             try {
                 
             
             JarFile jar = new JarFile(fl);
             Manifest mf = jar.getManifest();
             Attributes att = mf.getAttributes("Makumba");
             version = att.getValue("Version");
             jar.close();
 
             } catch(ZipException ze) {
                 if(!(ze.getMessage().indexOf("error in opening zip file") > -1)) {
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
