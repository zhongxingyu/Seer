 package platformer;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL12.*;
 
 import java.awt.image.BufferedImage;
 import java.nio.ByteBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.Display;
 
 public class Utilities {
     private final int fpsSamples = 100;
     private int fpsindex = 0;
     private double[] fpslist;
     public static double tX, tY;
     
     public void sync (int hertz)
     {
         Display.sync(hertz);
         /*try {
             Thread.sleep(1000 / hertz);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }*/
     }
     public static int getWidth () { return Display.getWidth(); }
     public static int getHeight() { return Display.getHeight(); }
     public double averageFps (double fps)
     {
         if (fpslist == null) {
             fpslist = new double[fpsSamples];
             for (int i = 0; i < fpsSamples; i++) fpslist[i] = 0.0;
         }
         
         fpslist[fpsindex] = fps;
         if (++fpsindex == fpsSamples) fpsindex = 0;
         
         double fpssum = 0.0;
         
         for (double ftime : fpslist) {
             fpssum += ftime;
         }
         return (((double) fpssum) / fpsSamples);
     }
     public static void translateFromOrigin(double x, double y)
     {
         glTranslated(x - tX,
                      y - tY,
                      0.0);
         tX = x;
         tY = y;
     }
     public static int loadTexture(BufferedImage image)
     {
         int BYTES_PER_PIXEL = 4;
         int[] pixels = new int[image.getWidth() * image.getHeight()];
         image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
 
         ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
           
         for(int y = 0; y < image.getHeight(); y++){
             for(int x = 0; x < image.getWidth(); x++){
                 int pixel = pixels[y * image.getWidth() + x];
                 buffer.put((byte) ((pixel >> 16) & 0xFF));
                 buffer.put((byte) ((pixel >> 8) & 0xFF));
                 buffer.put((byte) (pixel & 0xFF));
                 buffer.put((byte) ((pixel >> 24) & 0xFF));
             }
         }
         buffer.flip();
 
         int textureID = glGenTextures();
         glBindTexture(GL_TEXTURE_2D, textureID);
         
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
 
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
           
         glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
         
         return textureID;
     }
 }
