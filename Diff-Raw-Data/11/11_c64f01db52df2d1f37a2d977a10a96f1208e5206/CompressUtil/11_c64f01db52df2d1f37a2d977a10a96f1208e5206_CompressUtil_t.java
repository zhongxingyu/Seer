 package com.madgag.compress;
 
 
 import org.apache.commons.compress.archivers.ArchiveException;
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.input.BoundedInputStream;
 
 import java.io.*;
 
 import static org.apache.commons.io.FileUtils.openOutputStream;
 import static org.apache.commons.io.IOUtils.closeQuietly;
 import static org.apache.commons.io.IOUtils.copy;
 
 public class CompressUtil {
 
 	public static void unzip(InputStream rawZipFileInputStream, File destinationDirectory) throws IOException, ArchiveException {
         ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(rawZipFileInputStream);
 
         ZipArchiveEntry entry;
         while ((entry=archiveInputStream.getNextZipEntry())!=null) {
             File entryDestinationFile = new File(destinationDirectory, entry.getName());
             if (entry.isDirectory()) {
                 entryDestinationFile.mkdirs();
             } else {
                 FileOutputStream inflatedFileOutputStream = openOutputStream(entryDestinationFile);
                 try {
                    copy(new BoundedInputStream(archiveInputStream, entry.getSize()), inflatedFileOutputStream);
                    inflatedFileOutputStream.flush();
                 } finally {
                     closeQuietly(inflatedFileOutputStream); // don't close BoundedInputStream - throw it away, don't propagate close!
                 }
             }
 
             entryDestinationFile.setLastModified(entry.getLastModifiedDate().getTime());
         }
         closeQuietly(archiveInputStream);
 	}
 
 }
