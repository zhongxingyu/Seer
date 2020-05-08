 package gov.nih.nci.cagrid.introduce.updater;
 
 import gov.nih.nci.cagrid.introduce.beans.software.ExtensionType;
 import gov.nih.nci.cagrid.introduce.beans.software.IntroduceType;
 import gov.nih.nci.cagrid.introduce.beans.software.SoftwareType;
 import gov.nih.nci.cagrid.introduce.codegen.SyncTools;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.axis.utils.XMLUtils;
 import org.apache.log4j.Logger;
 import org.globus.wsrf.encoding.DeserializationException;
 import org.globus.wsrf.encoding.ObjectDeserializer;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 public class UpdateManager {
     
     private static final Logger logger = Logger.getLogger(UpdateManager.class);
 
     private SoftwareType software = null;
 
 
     public UpdateManager(SoftwareType software) throws Exception {
         this.software = software;
         if (software == null) {
             throw new Exception("SoftwareType cannot be null");
         }
     }
 
 
     public void execute() {
         IntroduceType[] introduceTypes = software.getIntroduce();
 
         ExtensionType[] extensionTypes = software.getExtension();
 
         if (introduceTypes != null) {
             for (int i = 0; i < introduceTypes.length; i++) {
                 IntroduceType update = introduceTypes[i];
                 if (!update.getIsInstalled().booleanValue()) {
                     // if it is an introduce update i need to delete all files
                     // and directories before unzipping
                     File baseDir = new File(".");
                     File[] files = baseDir.listFiles();
                     logger.info("Removing old version of Introduce.");
                     for (int fileI = 0; fileI < files.length; fileI++) {
                         File f = files[fileI];
                         if (f.isDirectory() && !f.getName().equals("updates")) {
                             delete(f);
                         } else if (!f.isDirectory()) {
                             delete(f);
                         }
                     }
                     delete(new File("." + File.separator + "updates" + File.separator + "lib"));
 
                     logger.info("Installing new version of Introduce.");
                     File updateFile = new File("." + File.separator + "updates" + File.separator + "introduce"
                         + update.getVersion() + ".zip");
                     try {
                         unzipIntroduce(updateFile);
                         updateFile.delete();
                     } catch (IOException e) {
                         logger.error(e);
                     }
                 }
 
                 if (update.getIntroduceRev() != null && update.getIntroduceRev(0) != null) {
                     // just a patch, unzip overtop
                     logger.info("Installing updates for current version of Introduce.");
                     File updateFile = new File("." + File.separator + "updates" + File.separator + "introduce"
                         + update.getVersion() + "Patch" + update.getIntroduceRev(0).getPatchVersion() + ".zip");
                     try {
                         unzipIntroduce(updateFile);
                         updateFile.delete();
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     // need to set the patch version in the
                    // introduce.engine.properties file
                     File engineProps = new File("." + File.separator + "conf" + File.separator
                        + "introduce.engine.properties");
                     Properties props = new Properties();
                     try {
                         props.load(new FileInputStream(engineProps));
                         props.setProperty("introduce.patch.version", String.valueOf(update.getIntroduceRev(0)
                             .getPatchVersion()));
                         FileOutputStream fos = new FileOutputStream(engineProps);
                         props.store(fos, "Introduce Engine Properties");
                         fos.close();
                     } catch (FileNotFoundException e) {
                         logger.error(e);
                     } catch (IOException e) {
                         logger.error(e);
                     }
                     File enginePropsT = new File("." + File.separator + "conf" + File.separator
                        + "introduce.engine.properties.template");
                     Properties propsT = new Properties();
                     try {
                         propsT.load(new FileInputStream(enginePropsT));
                         propsT.setProperty("introduce.patch.version", String.valueOf(update.getIntroduceRev(0)
                             .getPatchVersion()));
                         FileOutputStream fos = new FileOutputStream(enginePropsT);
                         propsT.store(fos, "Introduce Engine Properties");
                         fos.close();
                     } catch (FileNotFoundException e) {
                         logger.error(e);
                     } catch (IOException e) {
                         logger.error(e);
                     }
 
                 }
 
             }
         }
 
         if (extensionTypes != null) {
             for (int i = 0; i < extensionTypes.length; i++) {
                 ExtensionType update = extensionTypes[i];
                 File updateFile = new File("." + File.separator + "updates" + File.separator + update.getName()
                     + update.getVersion() + ".zip");
                 try {
                     unzipExtension(updateFile);
                     updateFile.delete();
                 } catch (IOException e) {
                     logger.error(e);
                 }
             }
         }
 
     }
 
 
     public static void delete(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 delete(new File(dir, children[i]));
             }
             boolean success = dir.delete();
             if (!success) {
                 System.err.println("unable to delete directory: " + dir.getAbsolutePath());
             }
         } else {
             boolean success = dir.delete();
             if (!success) {
                 System.err.println("unable to delete file: " + dir.getAbsolutePath());
             }
         }
 
     }
 
 
     private void unzipIntroduce(File cachedFile) throws IOException {
 
         InputStream in = new BufferedInputStream(new FileInputStream(cachedFile));
         ZipInputStream zin = new ZipInputStream(in);
         ZipEntry e;
         while ((e = zin.getNextEntry()) != null) {
             if (e.isDirectory()) {
                 new File(e.getName()).mkdirs();
             } else {
                 unzip(".", zin, e.getName());
             }
         }
         zin.close();
     }
 
 
     private void unzipExtension(File cachedFile) throws IOException {
 
         InputStream in = new BufferedInputStream(new FileInputStream(cachedFile));
         ZipInputStream zin = new ZipInputStream(in);
         ZipEntry e;
         while ((e = zin.getNextEntry()) != null) {
             if (e.isDirectory()) {
                 new File("." + File.separator + "extensions" + File.separator + e.getName()).mkdirs();
             } else {
                 unzip("." + File.separator + "extensions", zin, e.getName());
             }
         }
         zin.close();
     }
 
 
     private static void unzip(String baseDir, ZipInputStream zin, String s) throws IOException {
         File file = new File(new File(baseDir).getAbsolutePath() + File.separator + s);
         file.getParentFile().mkdirs();
         FileOutputStream out = new FileOutputStream(file);
         logger.info(".");
         byte[] b = new byte[512];
         int len = 0;
         while ((len = zin.read(b)) != -1) {
             out.write(b, 0, len);
         }
         out.close();
     }
 
 
     public static void main(String[] args) {
         File updateFile = new File("updates" + File.separator + "software.xml");
         if (!updateFile.exists()) {
             logger.info("No updates to process");
             System.exit(0);
         }
 
         org.w3c.dom.Document doc = null;
         try {
             doc = XMLUtils.newDocument(new InputSource(new FileInputStream(updateFile)));
         } catch (FileNotFoundException e) {
             logger.error(e);
         } catch (ParserConfigurationException e) {
             logger.error(e);
         } catch (SAXException e) {
             logger.error(e);
         } catch (IOException e) {
             logger.error(e);
         }
         SoftwareType software = null;
         try {
             software = (SoftwareType) ObjectDeserializer.toObject(doc.getDocumentElement(), SoftwareType.class);
         } catch (DeserializationException e) {
             logger.error(e);
         }
         UpdateManager manager = null;
         try {
             manager = new UpdateManager(software);
         } catch (Exception e) {
             logger.error(e);
             System.exit(2);
         }
         manager.execute();
         updateFile.delete();
         System.exit(1);
     }
 
 }
