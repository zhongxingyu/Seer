 package org.wings.resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.externalizer.ExternalizeManager;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * filters an input stream of a css file for occurences of "url([classPath])".
  * These image classpaths are then externalized and replaced in the resulting
  * InputStream. 
  * This gives us the possibility to load css files and their included images
  * via the classpath, so no external files are needed.
  * If the image is not found at the classpath provided, the occurence is not
  * altered.
  * @author ole
  * @version $$
  *
  */
 public class CssUrlFilterInputStream extends BufferedInputStream {
     private final transient static Log log = LogFactory.getLog(CssUrlFilterInputStream.class);
     
     private final static byte STATE_NONE = 0;
     private final static byte STATE_U = 1;
     private final static byte STATE_UR = 2;
     private final static byte STATE_URL = 3;
     private final static byte STATE_URL_DONE = 4;
     private final static byte STATE_SUBST = 5;
     private byte state = STATE_NONE;
     
     private byte[] urlBuffer;
     private int bufferPos;
 
     /**
      * The ExternalizeManager to use for Image externalization.
      * Externalized Resources cannot count on a session, but we need an
      * ExternalizeManager for image externalization.
      */
     private ExternalizeManager extManager;
 
     /**
      * Creates a new Instance.
      * @param in the InputStream to filter
      * @param extManager The ExternalizeManager to use for Image externalization.
      */
     public CssUrlFilterInputStream(InputStream in, ExternalizeManager extManager) {
         super(in);
         this.extManager = extManager;
     }
 
     /* (non-Javadoc)
      * @see java.io.InputStream#read()
      */
    public synchronized int read() throws IOException {
         int result = 0;
         if (state == STATE_SUBST) {
             result = readFromUrlBuffer();
         } else {
             result = super.read();
             if (result != -1) {
                 analyzeState(result);
                 if (state == STATE_URL_DONE) {
                     substitute();
                 }
             }
         }
         return result;
     }
 
     /**
      * substitutes the ocurrences of "url([classPath])" with the externalized 
      * images.
      * @throws IOException
      */
     private void substitute() throws IOException {
         StringBuffer classPathBuffer = new StringBuffer();
         int tempBuffer = super.read();
         while (tempBuffer != -1 && tempBuffer != ')') {
             classPathBuffer.append((char)tempBuffer);
             super.mark(2);
             tempBuffer = super.read();
         }
         super.reset();
         String classPath = strip(classPathBuffer.toString(), ' ');
         classPath = strip(classPath, '\'');
         classPath = strip(classPath, '"');
         String extension = null;
         int dotIndex = classPath.lastIndexOf('.');
         if (dotIndex > -1) {
             extension = classPath.substring(dotIndex + 1).toLowerCase();
             if ("jpg".equals(extension)) extension = "jpeg";
             
         }
         bufferPos = 0;
         urlBuffer = externalizeImage(classPath, "image/" + extension).getBytes();
         if (urlBuffer.length == 0) {
             // not externalized, use old String
             urlBuffer = classPathBuffer.toString().getBytes();
         }
         state = STATE_SUBST;
     }
 
     /** externalizes an Image found.
      * @param classPath the classPath of the Image
      * @param mimeType the mimeType of the Image
      * @return the url of the externalized Image
      */
     private String externalizeImage(String classPath, String mimeType) {
         ClasspathResource res = new ClasspathResource(classPath, mimeType);
         if (res.getResourceStream() == null) {
             // no resource found at classPath, return old string
             log.debug("Could not find resource at classpath: " + classPath);
             return "";
         }
         StringBuffer imageUrl = new StringBuffer("'");
         imageUrl.append(extManager.externalize(res, ExternalizeManager.GLOBAL));
         imageUrl.append("'");
         return imageUrl.toString();
     }
 
     /** reads from the filename buffer. is called when an Image classPath is
      * replaces by it's url.
      * @return the character at the current position
      */
     private int readFromUrlBuffer() {
         int result = urlBuffer[bufferPos];
         bufferPos++;
         if (bufferPos == urlBuffer.length) {
             state = STATE_NONE;
         }
         return result;
     }
 
     /* (non-Javadoc)
      * @see java.io.InputStream#read(byte[], int, int)
      */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
         int i = 0;
         for (i = off; i < (off+len);i++) {
             byte tempByte = (byte)read();
             if (tempByte == -1) {
                 break;
             }
             b[i] = tempByte;
         }
         return i - off;
     }
 
     /**
      * Strips a String of occurences of character. works recursively.
      * @param buffer the String
      * @param character the Character to be stripped
      * @return the stripped string
      */
     private String strip(String buffer, char character) {
         if (buffer.charAt(0) == character) {
             return strip(buffer.substring(1, buffer.length()), character);
         }
         if (buffer.charAt(buffer.length() - 1) == character) {
             return strip(buffer.substring(0, buffer.length()-1), character);
         }
         return buffer;
     }
 
     /* (non-Javadoc)
      * @see java.io.InputStream#read(byte[])
      */
     public int read(byte[] b) throws IOException {
         return read(b, 0, b.length);
     }
 
     /**
      * analyzes the Parse State and sets the state variable accordingly.
      * 
      * @param character the character to analyze.
      */
     private void analyzeState(int character) {
         switch (character) {
         case 'u':
             if (state == STATE_NONE) {
                 state = STATE_U;
             } else {
                 state = STATE_NONE;
             }
             break;
         case 'r':
             if (state == STATE_U) {
                 state = STATE_UR;
             } else {
                 state = STATE_NONE;
             }
             break;
         case 'l':
             if (state == STATE_UR) {
                 state = STATE_URL;
             } else {
                 state = STATE_NONE;
             }
             break;
         case '(':
             if (state == STATE_URL) {
                 state = STATE_URL_DONE;
             } else {
                 state = STATE_NONE;
             }
             break;
         default:
             state = STATE_NONE;
             break;
         }
     }
 }
