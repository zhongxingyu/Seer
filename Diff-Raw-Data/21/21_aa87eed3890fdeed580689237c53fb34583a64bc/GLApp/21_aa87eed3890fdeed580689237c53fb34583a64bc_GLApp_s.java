 package framework.objloader;
 
 import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Yan
  * Date: 19/09/13
  * Time: 15:49
  * To change this template use File | Settings | File Templates.
  */
 public class GLApp {
 
     public static final int SIZE_BYTE = 1;
     public static final int SIZE_INT = 4;
     public static final int SIZE_FLOAT = 4;
 
     static Hashtable OpenGLextensions;
 
     public static ByteBuffer allocBytes(int howmany) {
         return ByteBuffer.allocateDirect(howmany * SIZE_BYTE).order(ByteOrder.nativeOrder());
     }
 
     /**
      * Return a String array containing the path portion of a filename (result[0]),
      * and the fileame (result[1]).  If there is no path, then result[0] will be ""
      * and result[1] will be the full filename.
      */
     public static String[] getPathAndFile(String filename) {
         String[] pathAndFile = new String[2];
         Matcher matcher = Pattern.compile("^.*/").matcher(filename);
         if (matcher.find()) {
             pathAndFile[0] = matcher.group();
             pathAndFile[1] = filename.substring(matcher.end());
         } else {
            pathAndFile[0] = "";
             pathAndFile[1] = filename;
         }
         return pathAndFile;
     }
 
     /**
      * Create a texture and mipmap from the given image file.
      */
     public static int makeTexture(String textureImagePath) {
         int textureHandle = 0;
         GLImage textureImg = loadImage(textureImagePath);
         if (textureImg != null) {
             textureHandle = makeTexture(textureImg);
             makeTextureMipMap(textureHandle, textureImg);
         }
         return textureHandle;
     }
 
     /**
      *  Returns true if n is a power of 2.  If n is 0 return zero.
      */
     public static boolean isPowerOf2(int n) {
         if (n == 0) { return false; }
         return (n & (n - 1)) == 0;
     }
 
 
     public static IntBuffer allocInts(int howmany) {
         return ByteBuffer.allocateDirect(howmany * SIZE_INT).order(ByteOrder.nativeOrder()).asIntBuffer();
     }
     public static FloatBuffer allocFloats(int howmany) {
         return ByteBuffer.allocateDirect(howmany * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
     }
 
 
     /**
      * Allocate a texture (glGenTextures) and return the handle to it.
      */
     public static int allocateTexture()
     {
         IntBuffer textureHandle = allocInts(1);
         GL11.glGenTextures(textureHandle);
         return textureHandle.get(0);
     }
 
     /**
      * Create a texture from the given pixels in the default OpenGL RGBA pixel format.
      * Configure the texture to repeat in both directions and use LINEAR for magnification.
      * <P>
      * @return  the texture handle
      */
     public static int makeTexture(ByteBuffer pixels, int w, int h, boolean anisotropic)
     {
         // get a new empty texture
         int textureHandle = allocateTexture();
         // preserve currently bound texture, so glBindTexture() below won't affect anything)
         GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
         // 'select' the new texture by it's handle
         GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle);
         // set texture parameters
         GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
         GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
         GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); //GL11.GL_NEAREST);
         GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); //GL11.GL_NEAREST);
 
         // make texture "anisotropic" so it will minify more gracefully
         if (anisotropic && extensionExists("GL_EXT_texture_filter_anisotropic")) {
             // Due to LWJGL buffer check, you can't use smaller sized buffers (min_size = 16 for glGetFloat()).
             FloatBuffer max_a = allocFloats(16);
             // Grab the maximum anisotropic filter.
             GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_a);
             // Set up the anisotropic filter.
             GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_a.get(0));
         }
 
         // Create the texture from pixels
         GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                 0, 						// level of detail
                 GL11.GL_RGBA8,			// internal format for texture is RGB with Alpha
                 w, h, 					// size of texture image
                 0,						// no border
                 GL11.GL_RGBA, 			// incoming pixel format: 4 bytes in RGBA order
                 GL11.GL_UNSIGNED_BYTE,	// incoming pixel data type: unsigned bytes
                 pixels);				// incoming pixels
 
         // restore previous texture settings
         GL11.glPopAttrib();
 
         return textureHandle;
     }
 
     /**
      * Return true if the OpenGL context supports the given OpenGL extension.
      */
     public static boolean extensionExists(String extensionName) {
         if (OpenGLextensions == null) {
             String[] GLExtensions = GL11.glGetString(GL11.GL_EXTENSIONS).split(" ");
             OpenGLextensions = new Hashtable();
             for (int i=0; i < GLExtensions.length; i++) {
                 OpenGLextensions.put(GLExtensions[i].toUpperCase(),"");
             }
         }
         return (OpenGLextensions.get(extensionName.toUpperCase()) != null);
     }
 
     /**
      * Create a texture from the given image.
      */
     public static int makeTexture(GLImage textureImg)
     {
         if ( textureImg != null ) {
             if (isPowerOf2(textureImg.w) && isPowerOf2(textureImg.h)) {
                 return makeTexture(textureImg.pixelBuffer, textureImg.w, textureImg.h, false);
             }
             else {
                 System.out.println("GLApp.makeTexture(GLImage) Warning: not a power of two: " + textureImg.w + "," + textureImg.h);
                 textureImg.convertToPowerOf2();
                 return makeTexture(textureImg.pixelBuffer, textureImg.w, textureImg.h, false);
             }
         }
         return 0;
     }
 
     /**
      * Build Mipmaps for currently bound texture (builds a set of textures at various
      * levels of detail so that texture will scale up and down gracefully)
      *
      * @param textureImg  the texture image
      * @return   error code of buildMipMap call
      */
     public static int makeTextureMipMap(int textureHandle, GLImage textureImg)
     {
         int ret = 0;
         if (textureImg != null && textureImg.isLoaded()) {
             GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
             ret = GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA8,
                     textureImg.w, textureImg.h,
                     GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureImg.getPixelBytes());
             if (ret != 0) {
                 System.err.println("GLApp.makeTextureMipMap(): Error occured while building mip map, ret=" + ret + " error=" + GLU.gluErrorString(ret) );
             }
             // Assign the mip map levels and texture info
             GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
             GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
         }
         return ret;
     }
 
     /**
      * Load an image from the given file and return a GLImage object.
      * @return the loaded GLImage
      */
     public static GLImage loadImage(String imgFilename) {
         GLImage img = new GLImage(imgFilename);
         if (img.isLoaded()) {
             return img;
         }
         return null;
     }
 
     /**
      * Find a power of two equal to or greater than the given value.
      * Ie. getPowerOfTwoBiggerThan(800) will return 1024.
      * <p/>
      *
      * @return a power of two equal to or bigger than the given dimension
      */
     public static int getPowerOfTwoBiggerThan(int n) {
         if (n < 0)
             return 0;
         --n;
         n |= n >> 1;
         n |= n >> 2;
         n |= n >> 4;
         n |= n >> 8;
         n |= n >> 16;
         return n + 1;
     }
 
     public static FileInputStream getInputStream(String filename) {
         FileInputStream in = null;
         try {
             in = new FileInputStream(filename);
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         return in;
     }
 }
