 package uurss;
 
 import java.io.*;
 import java.net.*;
 import java.nio.channels.*;
 import java.security.*;
 import java.util.concurrent.*;
 import java.util.zip.*;
 
 import org.apache.log4j.*;
 
 /**
  * The task which download feed data(XML).
  */
 final class DownloadTask implements Callable<File> {
 
     private static final Logger log = Logger.getLogger(DownloadTask.class);
 
     private static MessageDigest instance;
     static {
         try {
             instance = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     private FeedInfo info;
     private File root;
 
     DownloadTask(FeedInfo info, File root) {
         this.info = info;
         this.root = root;
     }
 
     /* @see java.util.concurrent.Callable#call() */
     public File call() throws Exception {
         File feedFile = new File(root, getMessageDigestString(info.name) + ".xml");
         info.setFile(feedFile);
         if (log.isDebugEnabled()) {
             log.debug(String.format("%s(%s) => [%s]", info.name, info.url, feedFile.getName()));
         }
         URL url = new URL(info.url);
         InputStream is = url.openStream();
         try {
             transfer(is, feedFile);
         } finally {
             is.close();
         }
         if (isGzipFile(feedFile)) {
             if (log.isDebugEnabled()) {
                 log.debug(String.format("unzip [%s]", feedFile.getName()));
             }
            File tmp = new File(root, ".tmp.dat");
             // unzip
             InputStream gis = new GZIPInputStream(new FileInputStream(feedFile));
             try {
                 transfer(gis, tmp);
             } finally {
                 gis.close();
             }
             if (feedFile.delete() && !tmp.renameTo(feedFile)) {
                 log.warn(String.format("failed to rename [%s] to [%s]",
                                        tmp.getName(),
                                        feedFile.getName()));
             }
         }
         return feedFile;
     }
 
     private static boolean isGzipFile(File file) throws IOException {
         if (file.length() >= 2) {
             InputStream fis = new FileInputStream(file);
             try {
                 byte[] bytes = new byte[2];
                 if (fis.read(bytes) >= 2) {
                     if (((bytes[1] & 0xFF) * 0x100 + (bytes[0] & 0xFF)) == GZIPInputStream.GZIP_MAGIC) {
                         return true;
                     }
                 }
             } finally {
                 fis.close();
             }
         }
         return false;
     }
 
     private static long transfer(InputStream is, File dst) throws IOException {
         FileOutputStream fos = new FileOutputStream(dst);
         try {
             final FileChannel wch = fos.getChannel();
             final ReadableByteChannel rch = Channels.newChannel(is);
             long p = 0L;
             for (long read = 0L; (read = wch.transferFrom(rch, p, 8192)) > 0; p += read) {
                 //
             }
             return p;
         } finally {
             fos.close();
         }
     }
 
     private static String getMessageDigestString(String input) {
         StringBuilder buffer = new StringBuilder();
         for (byte b : instance.digest(input.getBytes())) {
             buffer.append(String.format("%02X", b));
         }
         return buffer.toString();
     }
 
 }
