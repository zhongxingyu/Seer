 package dk.apaq.vfs.mime;
 
 import dk.apaq.vfs.File;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author michael
  */
 public class MimeType {
 
 
     private static boolean initialized=false;
     private static Properties mimetypes;
 
     private static void init(){
         if(initialized) return;
 
         mimetypes=new Properties();
         try {
            mimetypes.load(MimeType.class.getResourceAsStream("mimetypes.properties"));
         } catch (IOException ex) {
             Logger.getLogger(MimeType.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         initialized=true;
 
     }
 
     /**
      * Retrieves the mimetype.
      * @param file The file.
      * @return The mimetype.
      */
     public static String retrieve(File file){
         return retrieve(file.getSuffix());
     }
 
     public static String retrieve(String suffix){
         init();
         String type = null;
         if(suffix!=null)
             type = mimetypes.getProperty(suffix);
         
         if(type==null)
             type="application/octet-stream";
         return type;
     }
    
     public static void add(String suffix, String mimetype){
         if(suffix==null || mimetype==null)
             return;
         mimetypes.put(suffix, mimetype);
     }
 
     public static void remove(String suffix){
         if(suffix==null)
             return;
         mimetypes.remove(suffix);
     }
 }
