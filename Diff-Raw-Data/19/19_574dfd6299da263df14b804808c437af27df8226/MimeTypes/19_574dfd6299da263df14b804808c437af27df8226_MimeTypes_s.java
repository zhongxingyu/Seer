 package fm.audiobox.configurations;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This is a utility class.
  * 
  * <p>
  * It is used while uploading file in order to check the {@code mimetype} of the file
  * you are trying to upload.
  * </p>
  */
 public final class MimeTypes implements Serializable {
   
   
   private static final long serialVersionUID = 34533700701918745L;
   
   private static Map<String, String[]> mimeTypes = new HashMap<String, String[]>();
   
   static {
     mimeTypes.put( "audio/aac", new String[]{"aac"} );
     mimeTypes.put( "audio/mpeg", new String[]{"mp3", "mp2"} );
     mimeTypes.put( "audio/mp4", new String[]{"m4a", "m4b", "m4r", "3gp"} );
     mimeTypes.put( "audio/ogg", new String[]{"ogg", "oga"} );
     mimeTypes.put( "audio/flac", new String[]{"flac"} );
     mimeTypes.put( "audio/speex", new String[]{"spx"} );
     mimeTypes.put( "audio/x-ms-wma", new String[]{"wma"} );
     mimeTypes.put( "audio/x-pn-realaudio", new String[]{"rm", "ram"} );
     mimeTypes.put( "audio/vnd.wave", new String[]{"wav"} );
     mimeTypes.put( "audio/x-musepack", new String[]{"mpc", "mp+", "mpp"} );
     mimeTypes.put( "audio/x-aiff", new String[]{"aiff", "aif", "aifc"} );
     mimeTypes.put( "audio/x-tta", new String[]{"tta"} );
     mimeTypes.put( "video/mp4", new String[]{"mp4"} );
     mimeTypes.put( "video/quicktime", new String[]{"mov"} );
     mimeTypes.put( "video/x-msvideo", new String[]{"avi"} );
     mimeTypes.put( "video/x-flv", new String[]{"flv"} );
     mimeTypes.put( "video/webm", new String[]{"webm"} );
   }
   
   
   /**
    * Returns the correct mime type given a file extension
    * <p>
    * This method can return null if no extensions matched
    * </p>
    * 
    * @param ext the extension of the file
    */
   public static String getMime(String ext){
     
     Set<String> keys = mimeTypes.keySet();
     
     for( String mime: keys ){
       String[] exts = mimeTypes.get(mime);
       if ( Arrays.asList(exts).contains( ext.toLowerCase() ) ){
         return mime;
       }
     }
     
     return null;
   }
   
   /**
    * This method returns {@code true} if given {@code mime} is
    * an AudioBox compatible mimetype
    * 
    * @param mime the MimeType you want to test
    * @return {@code true} if the mime type is correct. {@code false} if not
    */
   public static boolean isAllowed(String mime){
     return mimeTypes.containsKey( mime );
   }
   
   
   /**
    * Returns the correct mime type given a file
    * <p>
    * Note: This method can return null if no extensions matched
    * </p>
    * 
    * @param file the file you want to test
    */
   public static String getMime(File file){
     String fileName = file.getName();
     if ( fileName.length() <= 4 ){
       return null;
     }
     
    String ext = fileName.substring( fileName.length() - 4 );
    if ( ext.indexOf(".") != 0 ){
      // We"re expecting the extension starts with "."
      return null;
    }
     
     
    return getMime( ext.substring(1) );
   }
   
 
 }
