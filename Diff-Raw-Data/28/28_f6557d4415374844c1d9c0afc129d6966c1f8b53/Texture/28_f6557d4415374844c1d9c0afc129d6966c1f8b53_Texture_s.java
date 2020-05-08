 package graphics;
 
 import java.util.HashMap;
 import javax.imageio.*;
 import javax.media.opengl.GL2;
 import java.awt.Graphics2D;
 import java.awt.color.ColorSpace;
 import java.awt.geom.AffineTransform;
 import java.awt.image.*;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 public class Texture {
     private static final int NO_TEXTURE = -1;
     protected static HashMap<String, Texture> textures = new HashMap<String, Texture>();
     
     public static Texture findByName(String name) {
         return textures.get(name);
     }
     
     public static Texture findOrCreateByName(String name){
         Texture tex = textures.get(name);
         if(tex == null){
             tex = new Texture(name);
             textures.put(name, tex);
         }
         return tex;
     }
     
     public static void initialize(GL2 gl){
         for(Texture tex : textures.values()){
             tex.init(gl);
         }
     }
 
     private transient int glTexture;
     private String name; /* fileName */
     
     private Texture(String filePath){
         glTexture = NO_TEXTURE;
         name = filePath;
     }
     
     public void bind(GL2 gl){
         if(glTexture == NO_TEXTURE)
             init(gl);
         gl.glBindTexture(GL2.GL_TEXTURE_2D, glTexture);
     }
     
     public int getGlTexture() {
         return glTexture;
     }
     
     public String getName(){
         return name;
     }
 
     private void init(GL2 gl){
         BufferedImage image;
         File textureFile = new File(name);
         try {
             image = ImageIO.read(textureFile);
         } catch (IOException ie) {
             ie.printStackTrace();
             throw new RuntimeException("unable to open " + textureFile.getAbsolutePath());
         }
         // Java really wanted to modify an array pointer
         int[] texture_ids = new int[1];
         gl.glGenTextures(1, texture_ids, 0); // not sure what the third argument is.
         glTexture = texture_ids[0];
 
         gl.glBindTexture(GL2.GL_TEXTURE_2D, glTexture);
         makeRGBTexture(gl, image, GL2.GL_TEXTURE_2D);
         // Setup filters
         gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
         gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
     }
 
     /* Switched texture loading method, to method from
      * http://today.java.net/pub/a/today/2003/09/11/jogl2d.html
      * 
      * This uses the Java graphics library to convert the color space
      * byte order and flip the image vertically so it is suitable for OpenGL.
      */
     private void makeRGBTexture(GL2 gl, BufferedImage img, int target) {
         /* Setup a BufferedImage suitable for OpenGL */
         WritableRaster raster = Raster.createInterleavedRaster (DataBuffer.TYPE_BYTE,
                 img.getWidth(),
                 img.getHeight(),
                 4,
                 null);
         ComponentColorModel colorModel = new ComponentColorModel (ColorSpace.getInstance(ColorSpace.CS_sRGB),
                 new int[] {8,8,8,8},
                 true,
                 false,
                 ComponentColorModel.TRANSLUCENT,
                 DataBuffer.TYPE_BYTE);          
         BufferedImage bufImg = new BufferedImage (colorModel,
                 raster,
                 false,
                 null);
 
 
         /* Setup a Graphic2D context that will flip the image vertically along the way */
         Graphics2D g = bufImg.createGraphics();
         AffineTransform gt = new AffineTransform();
         gt.translate (0, img.getHeight());
         gt.scale (1, -1d);
         g.transform (gt);
         g.drawImage (img, null, null );
 
         /* Fetch the raw data out of the image and destroy the graphics context */
         byte[] imgRGBA = ((DataBufferByte)raster.getDataBuffer()).getData();
         g.dispose();
 
         /* Convert the raw data to a buffer for glTexImage2D */
         ByteBuffer dest = ByteBuffer.allocateDirect(imgRGBA.length);
         dest.order(ByteOrder.nativeOrder());
         dest.put(imgRGBA, 0, imgRGBA.length);
 
         // Rewind the buffer so we can read it starting and beginning
         dest.rewind();
 
         gl.glTexImage2D(target, 0, GL2.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, dest);
     }
 }
