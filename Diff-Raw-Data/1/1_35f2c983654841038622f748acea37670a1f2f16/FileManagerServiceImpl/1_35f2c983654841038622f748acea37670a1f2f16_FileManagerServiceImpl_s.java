 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.file.impl;
 
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.services.core.file.FileManagerService;
 
 import java.io.*;
 
 /**
  * The <code>UserStorageService</code> manages a temporary directory used for the storage of
  * user's persistent data.
  */
 public class FileManagerServiceImpl implements FileManagerService, PortletServiceProvider {
 
     protected PortletLog log = SportletLog.getInstance(FileManagerServiceImpl.class);
     private static boolean inited = false;
     protected static String PORTAL_TMP_DIR = null;
     protected static String catalina = null;
 
     public void init(PortletServiceConfig config) {
         if (!inited) {
             String tmpDir = config.getInitParameter("tmp_dir");
             PORTAL_TMP_DIR = GridSphereConfig.getServletContext().getRealPath("/" + tmpDir);
             File f = new File(PORTAL_TMP_DIR);
             if (!f.exists()) {
                 log.debug("Creating temp directory for users: " + PORTAL_TMP_DIR);
                 if (!f.mkdir()) log.error("Unable to create directory" + PORTAL_TMP_DIR);
             }
             inited = true;
         }
     }
 
     public void destroy() {
 
     }
 
 
     public String getLocationPath(User user, String fileName) {
         String userLoc = PORTAL_TMP_DIR + File.separator + user.getID() + File.separator + fileName;
         //String fileLoc = userLoc + File.separator + fileName;
         //File userDir = new File(userLoc);
         //if (userDir.exists()) {
         //    log.debug("Creating temp directory for user: " + user.getID());
         //    if (!userDir.mkdir()) log.error("Unable to create directory" + userLoc);
         //}
         return userLoc;
     }
 
     /*
     public void storeFile(User user, FileInputBean inputBean, String fileName) throws IOException, Exception {
         String userLoc = getLocationPath(user, "");
         File f = new File(userLoc);
         if (!f.exists()) {
             if (!f.mkdirs()) throw new IOException("Unable to create dir: " + userLoc);
         }
         String path = getLocationPath(user, fileName);
         System.err.println("storeFile: " + path);
         inputBean.saveFile(path);
     }
     */
 
     public void deleteFile(User user, String filename) {
         File f = new File(getLocationPath(user, filename));
         if (f.exists()) f.delete();
     }
 
     public File getFile(User user, String fileName) throws FileNotFoundException {
         String userLoc = getLocationPath(user, fileName);
         File f = new File(userLoc);
         if (!f.exists()) throw new FileNotFoundException("Unable to find file: " + fileName);
         return f;
     }
 
     public String[] getUserFileList(User user) {
         String userLoc = getLocationPath(user, "");
         File f = new File(userLoc);
         if (f.exists()) {
             return f.list();
         } else {
             return null;
         }
     }
 
     protected String getUserStoragePath(User user) {
         File userDir = new File(PORTAL_TMP_DIR + File.separator + user.getID());
         if (userDir.exists()) {
             log.debug("Creating temp directory for users: " + PORTAL_TMP_DIR);
             if (!userDir.mkdir()) log.error("Unable to create directory" + PORTAL_TMP_DIR);
         }
         return userDir.getAbsolutePath();
     }
 
     protected void copyFile(File oldFile, File newFile) throws IOException {
         // Destination and streams
         log.debug("in copyFile(): oldFile: " + oldFile.getAbsolutePath() + " newFile: " + newFile.getCanonicalPath());
         FileInputStream fis = new FileInputStream(oldFile);
         FileOutputStream fos = new FileOutputStream(newFile);
 
         // Amount of data to copy
         long fileLength = oldFile.length();
         byte[] bytes = new byte[1024]; // 1K at a time
         int length = 0;
         long totalLength = 0;
         while (length > -1) {
             length = fis.read(bytes);
             if (length > 0) {
                 fos.write(bytes, 0, length);
                 totalLength += length;
             }
         }
         // Test that we copied all the data
         if (fileLength != totalLength) {
             throw new IOException("File copy size missmatch");
         }
         fos.flush();
         fos.close();
         fis.close();
     }
 
 
 }
