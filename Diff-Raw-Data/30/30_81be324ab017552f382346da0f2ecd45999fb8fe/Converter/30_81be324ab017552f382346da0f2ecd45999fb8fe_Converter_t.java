 package fedora.services.diringest.sip2fox;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import org.apache.log4j.*;
 
 import fedora.services.diringest.sip2fox.pidgen.*;
 
 /**
  * Utility for converting METS-encoded SIPs to self-contained FOXML objects.
  */
 public class Converter {
 
     private static final Logger _LOG = 
             Logger.getLogger(Converter.class.getName());
 
     private PIDGenerator m_pidgen;
 
     public Converter(PIDGenerator pidgen) {
         m_pidgen = pidgen;
     }
 
     /**
      * Convert the given SIP into a series of self-contained FOXML files.
      *
      * @throws IOException If conversion failed for any reason.
      */
     public FOXMLResult[] convert(ConversionRules rules, 
                                  File sipFile, 
                                  String user, 
                                  String pass) throws Exception {
         _LOG.info("Constructing tree from SIP");
         SIPReader reader = getSIPReader(sipFile);
         if (_LOG.isDebugEnabled()) {
             PrintWriter out = new PrintWriter(System.out);
             reader.getRoot().print("", out);
             out.flush();
         }
         _LOG.info("Constructing foxml using pid generator, conversion rules, and tree");
         try {
             List resultList = new ArrayList();
             FOXMLMaker maker = new FOXMLMaker(m_pidgen, rules, 
                                               reader.getRoot(), 
                                               user, 
                                               pass);
             while (maker.hasNext()) {
                 resultList.add(maker.next());
             }
             return (FOXMLResult[]) resultList.toArray(new FOXMLResult[0]);
         } finally {
             reader.close();
         }
     }
 
     // only supports zip-based sips for now
     private SIPReader getSIPReader(File sipFile) throws Exception {
         if (!sipFile.exists()) {
             throw new IOException("File not found: " + sipFile.getPath());
         }
         if (sipFile.isDirectory()) {
             return new DirectorySIPReader(sipFile, false);
         } else {
            if (sipFile.length() < 200 * 1024 * 1024) {
                 // less than 250MB, so process it directly from
                 // the ZIP file
                 return new ZIPSIPReader(new ZipFile(sipFile));
             } else {
                 // 250MB+, so unzip it to temp dir and
                 // return a DirectorySIPReader instead
                 return new DirectorySIPReader(unzipToTemp(sipFile), true);
             }
         }
     }
 
     /**
      * Create a temporary directory, unzip the contents of
      * the given zip file to it, and return the directory.
      *
      * If anything goes wrong during this process, clean up
      * the temporary directory and throw an exception.
      */
     private static File unzipToTemp(File sipFile) throws Exception {
         // get a temporary directory to work with
         File tempDir = File.createTempFile("diringest", null);
         tempDir.delete();
         tempDir.mkdir();
         _LOG.info("Unzipping to temporary directory: " + tempDir.getPath());
         try {
             unzip(new FileInputStream(sipFile), tempDir);
             return tempDir;
         } catch (Exception e) {
             // attempt cleanup, then re-throw
             deleteTempDir(tempDir);
             throw e;
         }
     }
 
     /**
      * Unzip to the given directory, creating subdirectories as
      * needed, and ignoring empty directories.
      */
     private static void unzip(InputStream is, File destDir) throws IOException {
         BufferedOutputStream dest = null;
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
         ZipEntry entry;
         while((entry = zis.getNextEntry()) != null) {
             if (!entry.isDirectory()) {
                 _LOG.info("Extracting: " + entry);
                 File f = new File(destDir, entry.getName());
                 f.getParentFile().mkdirs();
                 int count;
                 byte data[] = new byte[8192];
                 // write the files to the disk
                 FileOutputStream fos = new FileOutputStream(f);
                 dest = new BufferedOutputStream(fos, 8192);
                 while ((count = zis.read(data, 0, 8192)) != -1) {
                     dest.write(data, 0, count);
                 }
                 dest.flush();
                 dest.close();
             }
         }
         zis.close();
     }
 
     /**
      * Delete the given directory and all contents, recursively.
      */
     public static void deleteTempDir(File dir) {
         File[] files = dir.listFiles();
         for (int i = 0; i < files.length; i++) {
             if (files[i].isDirectory()) {
                 deleteTempDir(files[i]);
             } else {
                 if (!files[i].delete()) {
                     _LOG.warn("Unable to delete file: " + files[i].getPath());
                 }
             }
         }
         if (!dir.delete()) {
             _LOG.warn("Unable to delete directory: " + dir.getPath());
         }
     }
 
 }
