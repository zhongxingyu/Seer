 package edacc.updates;
 
 import edacc.EDACCApp;
 import edacc.Version;
 import edacc.VersionException;
 import edacc.model.Tasks;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author simon
  */
 public class UpdateController {
 
     private static UpdateController instance = null;
 
     public static UpdateController getInstance() {
         if (instance == null) {
             instance = new UpdateController();
         }
         return instance;
     }
 
     private List<Version> getVersions() throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
         List<Version> res = new LinkedList<Version>();
 
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
         URL url = new URL(org.jdesktop.application.Application.getInstance(EDACCApp.class).getContext().getResourceMap(Version.class).getString("version.url"));
         URLConnection con = url.openConnection();
         Document doc = docBuilder.parse(con.getInputStream());
         if (doc.getChildNodes().getLength() != 1 || !"versions".equals(doc.getChildNodes().item(0).getNodeName())) {
             return res;
         }
         Node root = doc.getChildNodes().item(0);
         for (int i = 0; i < root.getChildNodes().getLength(); i++) {
             Node node = root.getChildNodes().item(i);
             String version = null;
             String v_url = null;
             String md5 = null;
             String commit = null;
             
             if ("version".equals(node.getNodeName())) {
                 for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                     Node n = node.getChildNodes().item(j);
                     if ("v".equals(n.getNodeName())) {
                         version = n.getTextContent();
                     } else if ("location".equals(n.getNodeName())) {
                         v_url = n.getTextContent();
                     } else if ("md5".equals(n.getNodeName())) {
                         md5 = n.getTextContent();
                     } else if ("commit".equals(n.getNodeName())) {
                         commit = n.getTextContent();
                     }
                 }
                 Version v = null;
                 try {
                     v = new Version(version, v_url, md5, commit);
                     res.add(v);
                 } catch (Exception ex) {
                 }
             }
         }
 
         return res;
     }
 
     public Version getNewestVersion() throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
         Version new_version = new Version();
         for (Version v : getVersions()) {
             if (new_version.compareTo(v) < 0) {
                 new_version = v;
             }
         }
         return new_version;
     }
     
     public Version getDeveloperVersion() throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
         for (Version v : getVersions()) {
             if (v.isDeveloperVersion()) {
                 return v;
             }
         }
         return null;
     }
 
     public void download(Tasks task, Version v) throws MalformedURLException, IOException, VersionException {
         task.setOperationName("Downloading EDACC " + v);
         java.net.URL url = new java.net.URL(v.getLocation());
 
         java.net.URLConnection connection = url.openConnection();
         int length = connection.getContentLength();
         java.io.BufferedInputStream in = new java.io.BufferedInputStream(connection.getInputStream());
         
         java.io.FileOutputStream fos = new java.io.FileOutputStream(edacc.experiment.Util.getPath() + System.getProperty("file.separator") + "update.zip");
         java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024 * 24);
         byte data[] = new byte[1024 * 24];
         int count;
         int cur = 0;
         while ((count = in.read(data, 0, 1024 * 24)) > 0) {
             bout.write(data, 0, count);
             cur += count;
             task.setStatus(edacc.experiment.Util.convertUnit(cur) + " / " + edacc.experiment.Util.convertUnit(length));
             task.setTaskProgress((float) cur / (float) length);
         }
         bout.close();
         fos.close();
         in.close();
         task.setTaskProgress(0f);
         task.setStatus("Validating update.zip");
         try {
             String md5 = edacc.manageDB.Util.calculateMD5(new File(edacc.experiment.Util.getPath() + System.getProperty("file.separator") + "update.zip"));
             if (v.getMd5() == null || !v.getMd5().equals(md5)) {
                 throw new VersionException("Error while validating md5 checksum. Please try again.");
             }
         } catch (FileNotFoundException ex) {
             throw new VersionException("Error while validating md5 checksum. Please try again.");
         } catch (NoSuchAlgorithmException ex) {
             throw new VersionException("Error while validating md5 checksum. Please try again.");
         }
     }
 
     public void startUpdater() throws FileNotFoundException, IOException, ClassNotFoundException {
         ZipInputStream stream = new ZipInputStream(new FileInputStream(new File(edacc.experiment.Util.getPath() + System.getProperty("file.separator") + "EDACC.jar")));
         ZipEntry entry;
         while ((entry = stream.getNextEntry()) != null) {
             if ("Updater.class".equals(entry.getName())) {
                 break;
             }
         }
         if (entry != null) {
             java.io.FileOutputStream fos = new java.io.FileOutputStream(edacc.experiment.Util.getPath() + System.getProperty("file.separator") + "Updater.class");
             java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
             byte[] data = new byte[1024];
             int count;
             while ((count = stream.read(data)) > 0) {
                 bout.write(data, 0, count);
             }
             bout.close();
             fos.close();
            String startUpdater[] = {System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java", "-classpath", edacc.experiment.Util.getPath(), "Updater"};
            Runtime.getRuntime().exec(startUpdater);
             Runtime.getRuntime().halt(0);
         } else {
             throw new ClassNotFoundException();
         }
     }
 }
