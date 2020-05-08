 package org.lehirti.tools;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
import java.util.Set;
 import java.util.SortedMap;
 import java.util.Map.Entry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import org.lehirti.engine.util.ContentUtils;
 import org.lehirti.engine.util.PathFinder;
 import org.lehirti.luckysurvivor.C;
 
 public final class ExportCoreContent {
   private static final File ROOT_DIR = new File("..");
   private static final File CORE_DIR = new File(ROOT_DIR, "core");
   private static final File DEST_DIR = new File("../../../luckysurvivor");
  private static final Set<String> NEW_ZIP_ENTRIES = new HashSet<String>();
   
   public static void main(final String[] args) throws IOException {
     System.out.println("START " + ExportCoreContent.class.getSimpleName());
     for (final C content : C.values()) {
       final File dir = new File(CORE_DIR, content.name());
       if (dir.getName().equals("res") || !dir.isDirectory()) {
         System.out.println("Skipping " + dir.getAbsolutePath());
         continue;
       }
       export(dir, content.requiredVersion);
     }
     
     System.out.println("FINISHED " + ExportCoreContent.class.getSimpleName());
   }
   
   private static void export(final File dir, final int versionNumber) throws IOException {
     System.out.println("START exporting " + dir.getAbsolutePath());
    NEW_ZIP_ENTRIES.clear();
     
     final String name = dir.getName();
     final File destZipFile = new File(DEST_DIR, name + "-" + versionNumber + ".zip");
     System.out.println("Target " + destZipFile.getAbsolutePath());
     
     final SortedMap<Integer, ZipFile> contentZipFiles = ContentUtils.getContentZipFiles(name, DEST_DIR);
     
     // remove "not older" zip files (should be at most one; with the same version number as the one being built
     final Iterator<Map.Entry<Integer, ZipFile>> itr = contentZipFiles.entrySet().iterator();
     while (itr.hasNext()) {
       final Entry<Integer, ZipFile> entry = itr.next();
       if (entry.getKey().intValue() >= versionNumber) {
         entry.getValue().close();
         itr.remove();
       }
     }
     
     final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destZipFile));
     
     // Make manifest
     final ZipEntry entry = new ZipEntry("core/manifest/" + name + "-" + versionNumber);
     out.putNextEntry(entry);
     
     exportDir(dir, out, contentZipFiles);
     
     out.close();
     
     System.out.println("FINISHED exporting " + dir.getAbsolutePath());
   }
   
   private static void exportDir(final File dir, final ZipOutputStream out,
       final SortedMap<Integer, ZipFile> contentZipFiles) throws FileNotFoundException, IOException {
     final String[] entries = dir.list();
     final byte[] buffer = new byte[4096];
     int bytesRead;
     
     for (final String e : entries) {
       final File f = new File(dir, e);
       if (f.isDirectory()) {
         exportDir(f, out, contentZipFiles);
       } else {
         String zipEntryString = f.getPath();
         zipEntryString = zipEntryString.substring(zipEntryString.indexOf("core"));
         final ZipEntry entry = new ZipEntry(zipEntryString);
         System.out.println("Adding ZipEntry " + entry.getName());
         out.putNextEntry(entry);
         FileInputStream in = new FileInputStream(f);
         while ((bytesRead = in.read(buffer)) != -1) {
           out.write(buffer, 0, bytesRead);
         }
         in.close();
         if (f.getName().endsWith(PathFinder.PROXY_FILENAME_SUFFIX)) {
           final File imageFile = PathFinder.imageProxyToCoreReal(f);
           final ZipEntry imageZipEntry = new ZipEntry(imageFile.getPath());
          if (!imageIsContainedInOlderZipFile(imageZipEntry, contentZipFiles)
              && !NEW_ZIP_ENTRIES.contains(imageFile.getPath())) {
            NEW_ZIP_ENTRIES.add(imageFile.getPath());
             final File correctedPathImageFile = new File(ROOT_DIR, imageFile.getPath());
             System.out.println("Adding ZipEntry " + imageZipEntry.getName());
             out.putNextEntry(imageZipEntry);
             in = new FileInputStream(correctedPathImageFile);
             while ((bytesRead = in.read(buffer)) != -1) {
               out.write(buffer, 0, bytesRead);
             }
             in.close();
           }
         }
       }
     }
   }
   
   private static boolean imageIsContainedInOlderZipFile(final ZipEntry imageFile,
       final SortedMap<Integer, ZipFile> contentZipFiles) {
     for (final ZipFile zipFile : contentZipFiles.values()) {
       if (zipFile.getEntry(imageFile.getName()) != null) {
         return true;
       }
     }
     return false;
   }
 }
