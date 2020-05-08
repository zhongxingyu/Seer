 package dk.statsbiblioteket.metadatarepository.xmltapes.common;
 
 import org.kamranzafar.jtar.TarEntry;
 
 import java.net.URI;
 
 /**
  * Static utility methods to convert between index entries, fedora URIs and tar Entries and tar filenames
  * //TODO the rest of the methods
  */
 public class TapeUtils {
 
 
     public static final String NAME_SEPARATOR = "#";
     public static final String DELETED = "DELETED";
     public static final String GZ = ".gz";
 
     public static URI toURI(TarEntry entry) {
         return toURI(entry.getName());
     }
 
     public static URI toURI(String filename) {
 
 
        int endIndex = filename.lastIndexOf(NAME_SEPARATOR);
         if (endIndex < 0) {
             endIndex = filename.length();
         }
         return URI.create(filename.substring(0, endIndex));
     }
 
 
     public static String toFilename(URI id) {
         return id.toString() + NAME_SEPARATOR + System.currentTimeMillis();
     }
 
     public static String toFilenameGZ(URI id) {
         return toFilename(id)+ GZ;
     }
 
 
     public static String toDeleteFilename(URI id) {
         return id.toString() + NAME_SEPARATOR + System.currentTimeMillis() + NAME_SEPARATOR + DELETED;
     }
 
     public static long getTimestamp(TarEntry entry) {
         String name = entry.getName();
         String[] splits = name.split("["+NAME_SEPARATOR+"\\.]");
         return Long.parseLong(splits[1]);
     }
 
 }
