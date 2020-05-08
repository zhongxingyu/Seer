 package org.sorcersoft.sigar;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 /**
  * @author Rafał Krupiński
  */
 public class Zip {
     private final static Logger log = LoggerFactory.getLogger(Zip.class);
 
     /**
      * @return the list of roots, that is directories of files that are at the top level of the zip file
      * @throws IOException
      */
     public static List<File> unzip(File zip, File targetDir) throws IOException {
         ZipFile zipFile = new ZipFile(zip);
         try {
             Set<String> roots = new HashSet<String>();
             Enumeration<? extends ZipEntry> entries = zipFile.entries();
             while (entries.hasMoreElements()) {
                 ZipEntry entry = entries.nextElement();
                 if (entry.isDirectory()) {
                     File dir = new File(targetDir, entry.getName());
                     if (!dir.exists()) {
                         FileUtils.forceMkdir(dir);
                     }
                 } else
                     copyInputStream(zipFile, entry, targetDir);
                 roots.add(getRoot(entry.getName()));
             }
 
             ArrayList<File> result = new ArrayList<File>(roots.size());
             for (String root : roots) {
                 result.add(new File(targetDir, root));
             }
             return result;
         } finally {
             zipFile.close();
         }
     }
 
     private static String getRoot(String name) {
         return name.substring(0, name.indexOf('/'));
     }
 
     private static File copyInputStream(ZipFile zipFile, ZipEntry entry, File targetDir) throws IOException {
         File target = new File(targetDir, entry.getName());
         InputStream inputStream = zipFile.getInputStream(entry);
         try {
             FileUtils.copyInputStreamToFile(inputStream, target);
         } finally {
             IOUtils.closeQuietly(inputStream);
         }
         return target;
     }
 
     public static void zip(File targetFile, File root, File zipRoot, FileFilter filter) throws IOException {
         ZipOutputStream targetZip = new ZipOutputStream(new FileOutputStream(targetFile));
         for (File file : root.listFiles(filter)) {
            String entryPath = new File(zipRoot, file.getName()).getPath();
             if (file.isFile()) {
                 log.debug(entryPath);
                 targetZip.putNextEntry(new ZipEntry(entryPath));
                 FileUtils.copyFile(file, targetZip);
             } else if (file.isDirectory()) {
                 if (!entryPath.endsWith("/"))
                     entryPath += "/";
                 log.debug(entryPath);
                 targetZip.putNextEntry(new ZipEntry(entryPath));
             }
         }
        targetZip.close();
     }
 }
