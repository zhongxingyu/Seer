 package com.soartech.simjr.sim;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import com.soartech.math.Vector3;
 import com.soartech.math.geotrans.Geodetic;
 
 /**
  * @author mjquist
  * 
  */
 public class DetailedTerrain extends SimpleTerrain
 {
     private BufferedImage detailedTerrainImage = null;
     private int width = 0;
     private int height = 0;
     private Vector3 origin = new Vector3(0.0, 0.0, 0.0);
     private double metersPerPixel = 1.0;
 
     /**
      * @param origin
      *            The lat/lon location of (0, 0, 0)
      */
     public DetailedTerrain(Geodetic.Point origin, File terrainTypeHref)
     {
         super(origin);
 
         if (terrainTypeHref == null) { return; }
 
         try
         {
             BufferedImage image = ImageIO.read(terrainTypeHref);
             detailedTerrainImage = image;
             width = image.getWidth();
             height = image.getHeight();
         }
         catch (IOException ie)
         {
             ie.printStackTrace();
         }
     }
 
     /**
      * @param origin
      *            The origin of the terrain-type map, in the frame of the
      *            terrain
      * @param metersPerPixel
      *            The resolution of the terrain-type map
      */
     public void setCoordinateFrame(Vector3 origin, double metersPerPixel)
     {
         this.origin = origin;
         this.metersPerPixel = metersPerPixel;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.soartech.simjr.sim.SimpleTerrain#getTerrainTypeAtPoint(com.soartech.math.Vector3)
      */
     @Override
     public TerrainTypeColor getTerrainTypeAtPoint(Vector3 point)
     {
         Point imageCoords = getTerrainImageCoords(point);
        int pX = imageCoords.x;
        int pY = imageCoords.y;
        Raster raster = detailedTerrainImage.getRaster();
         int vals[] = new int[] { 0, 0, 0, 0 };
        if (pX >= 0 && pX < width && pY >= 0 && pY < height)
         {
            raster.getPixel(pX, pY, vals);
         }

         return new TerrainTypeColor(vals);
     }
     
     public Point getTerrainImageCoords(Vector3 point)
     {
         Vector3 relativePointInPixels = point.subtract(origin).multiply(1.0 / metersPerPixel);
         int pX = (int) (width / 2.0 + relativePointInPixels.x);
         int pY = (int) (height / 2.0 - relativePointInPixels.y);
         if (pX >= 0 && pX < width && pY >= 0 && pY < height)
         {
             return new Point(pX, pY);
         }
         else
         {
             return null;
         }
     }
     
     public BufferedImage getTerrainImage()
     {
         return detailedTerrainImage;
     }
 
     public static class TerrainTypeColor
     {
         public final int red;
         public final int green;
         public final int blue;
         public final int alpha;
 
         TerrainTypeColor(int[] vals)
         {
             this.red = vals[0];
             this.green = vals[1];
             this.blue = vals[2];
             this.alpha = vals[3];
         }
 
         @Override
         public String toString()
         {
             return String.format("[%d %d %d %d]", red, green, blue, alpha);
         }
     }
 }
