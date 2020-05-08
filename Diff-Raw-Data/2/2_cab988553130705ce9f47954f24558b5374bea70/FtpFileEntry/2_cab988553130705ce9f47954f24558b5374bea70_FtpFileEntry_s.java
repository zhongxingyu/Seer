 package uk.ac.ebi.ae15.utils.files;
 
 import uk.ac.ebi.ae15.utils.RegExpHelper;
 
 import java.io.File;
 
 public class FtpFileEntry
 {
     private String location = null;
     private Long size = null;
     private Long lastModified = null;
 
     public FtpFileEntry( String aLocation, Long aSize, Long aLastModified )
     {
         location = aLocation;
         size = aSize;
         lastModified = aLastModified;
     }
     
     public FtpFileEntry( File file )
     {
         if (file.isFile()) {
             location = file.getAbsolutePath();
             size = file.length();
             lastModified = file.lastModified();
         }
     }
 
     public String getLocation()
     {
         return location;
     }
 
     public Long getSize()
     {
         return size;
     }
 
     public Long getLastModified()
     {
         return lastModified;
     }
     
     public static String getAccession( FtpFileEntry entry )
     {
         return (null != entry.location) ? accessionRegExp.matchFirst(entry.location) : "";
     }
 
     public static String getName( FtpFileEntry entry )
     {
         return (null != entry.location) ? nameRegExp.matchFirst(entry.location) : "";
     }
 
     public static String getKind( FtpFileEntry entry )
     {
         String name = getName(entry);
         if (null != name && !name.equals("")) {
             if (fgemArchiveRegExp.test(name)) {
                 return "fgem";
             }
             if (rawArchiveRegExp.test(name)) {
                 return "raw";
             }
             if (celArchiveRegExp.test(name)) {
                 return "cel";
             }
             if (magemlArchiveRegExp.test(name)) {
                 return "mageml";
             }
             if (adfFileRegExp.test(name)) {
                 return "adf";
             }
             if (idfFileRegExp.test(name)) {
                 return "idf";
             }
             if (sdrfFileRegExp.test(name)) {
                 return "sdrf";
             }
             if (twoColsFileRegExp.test(name)) {
                 return "twocolumns";
             }
             if (biosamplesFileRegExp.test(name)) {
                 return "biosamples";
             }
 
         }
         return "";
     }
 
     public static String getExtension( FtpFileEntry entry )
     {
         String name = getName(entry);
         if (null != name && !name.equals("")) {
             return extensionRegExp.matchFirst(name);
         }
         return "";
     }
 
     private static final RegExpHelper accessionRegExp
             = new RegExpHelper("/([AE]-\\w{4}-\\d+)/", "i");
 
     private static final RegExpHelper nameRegExp
             = new RegExpHelper("/([^/]+)$");
 
     private static final RegExpHelper extensionRegExp
             = new RegExpHelper("\\.([^.]+|tar\\.gz)$", "i");
 
     private static final RegExpHelper fgemArchiveRegExp
             = new RegExpHelper("\\.processed\\.zip|\\.processed\\.\\d+\\.zip|\\.processed\\.tgz|\\.processed\\.tar\\.gz", "i");
 
     private static final RegExpHelper rawArchiveRegExp
            = new RegExpHelper("\\.raw\\.zip||\\.raw\\.\\d+\\.zip|\\.raw\\.tgz|\\.raw\\.tar\\.gz", "i");
 
     private static final RegExpHelper celArchiveRegExp
             = new RegExpHelper("\\.cel\\.zip|\\.cel\\.tgz|\\.cel\\.tar\\.gz", "i");
 
     private static final RegExpHelper adfFileRegExp
             = new RegExpHelper("\\.adf\\.txt|\\.adf\\.xls", "i");
 
     private static final RegExpHelper idfFileRegExp
             = new RegExpHelper("\\.idf\\.txt|\\.idf\\.xls", "i");
 
     private static final RegExpHelper sdrfFileRegExp
             = new RegExpHelper("\\.sdrf\\.txt|\\.sdrf\\.xls", "i");
 
     private static final RegExpHelper twoColsFileRegExp
             = new RegExpHelper("\\.2columns\\.txt|\\.2columns\\.xls", "i");
 
     private static final RegExpHelper biosamplesFileRegExp
             = new RegExpHelper("\\.biosamples\\.map|\\.biosamples\\.png|\\.biosamples\\.svg", "i");
 
     private static final RegExpHelper magemlArchiveRegExp
             = new RegExpHelper("\\.mageml\\.zip|\\.mageml\\.tgz|\\.mageml\\.tar\\.gz", "i");
 }
