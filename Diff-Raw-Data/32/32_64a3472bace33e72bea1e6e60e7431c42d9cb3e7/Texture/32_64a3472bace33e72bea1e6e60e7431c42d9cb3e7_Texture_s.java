 package de.codesourcery.engine.raytracer;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Texture
 {
     private final int maxX;
     private final int maxY;
     
     private final int width;
     private final int height;
     
     private final int[][] imageData;
     
     public static Texture load(File file) throws IOException {
         return new Texture( ImageIO.read( file ) );
     }
     
     public int getWidth() {
 		return width;
 	}
     
     public int getHeight() {
 		return height;
 	}
     
     public Texture(BufferedImage image) 
     {
         this.width = image.getWidth();
         this.height = image.getHeight();
         this.maxX= image.getWidth()-1;
         this.maxY = image.getHeight()-1;
         this.imageData = convertTo2DWithoutUsingGetRGB(image);
     }
     
     public Vector4 getColorAt(double u,double v) {
         
         final double u1 = Math.abs( u ) % 1.0d;
         final double v1 = Math.abs( v ) % 1.0d;
         
         int x = (int) Math.round( maxX * u1 );
         int y = (int) Math.round( maxY * v1 );
         
         final int rgb = imageData[x][y];
 //        final int rgb= image.getRGB( x , y );
 
         int r = (rgb >> 16) & 0xff;
         int g = (rgb >> 8) & 0xff;
         int b = rgb & 0xff;
         return new Vector4( r/255.0,  g/255.0, b/255.0);
     }
     
     private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {
 
         final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
         final int width = image.getWidth();
         final int height = image.getHeight();
         final boolean hasAlphaChannel = image.getAlphaRaster() != null;
 
         int[][] result = new int[height][width];
         if (hasAlphaChannel) 
         {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
               int argb = 0;
               argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
               argb += ((int) pixels[pixel + 1] & 0xff); // blue
               argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
               argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
               result[row][col] = argb;
               col++;
               if (col == width) {
                  col = 0;
                  row++;
               }
            }
         } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
               int argb = 0;
               argb += -16777216; // 255 alpha
               argb += ((int) pixels[pixel] & 0xff); // blue
               argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
               argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
               result[row][col] = argb;
               col++;
               if (col == width) {
                  col = 0;
                  row++;
               }
            }
         }
         return result;
      }    
 }
