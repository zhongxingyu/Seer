 package com.hexcore.cas.utilities;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageProducer;
 import java.awt.image.PixelGrabber;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import jogamp.opengl.glu.mipmap.ScaleInternal;
 
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.HexagonGrid;
 import com.hexcore.cas.model.RectangleGrid;
 import com.hexcore.cas.model.TriangleGrid;
 
 /**
  * 
  * @author Karl Zoller
  *
  */
 public class HeightMapConverter
 {
 	private static final String TAG = "HeightMapConverter";
 	public boolean loadHeightMap(String fileName, Grid grid, int propertyIndex)
 	{
 		int width = grid.getWidth();
 		int height = grid.getHeight();
 		
 		int[] pixels = new int[width*height];
 		BufferedImage heightMap = null;
 		int imgWidth = 0;
 		int imgHeight = 0;
 		
 		//Read file
 		try
 		{
 			heightMap = ImageIO.read(new File(fileName));
 			imgWidth = heightMap.getWidth(null);
 			imgHeight = heightMap.getHeight(null);
 			
 			if(width != imgWidth || height != imgHeight)
 			{
 				Log.information("HeightMapConverter", "Scaling Image of: " + imgWidth + " x " + imgHeight + " to " + width + " x " + height);
				heightMap = (BufferedImage) heightMap.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
 			}
 		} 
 		catch (FileNotFoundException e)
 		{
 			Log.warning(TAG, "Could not find file");
 			return false;
 		}
 		catch (IOException e)
 		{
 			Log.error(TAG, "Error reading file");
 			return false;
 		}	
 		
 		for(int r = 0; r < height; r++)
 		{
 			for(int c = 0; c < width; c++)
 			{
 				int val = heightMap.getRGB(c, r);
 				double cs = (double)(val & 0xff) / 255.0;
 				double cl = 0.0;
 				
 				if(cs <= 0.04045)
 					cl = cs / 12.92;
 				else cl = Math.pow(((cs + 0.055)/1.055), 2.4);
 				
 				double finalVal = Math.floor(cl * 255 + 0.5);
 				grid.getCell(c, r).setValue(propertyIndex, finalVal);				
 				
 				System.out.println("Value: " + finalVal);
 			}
 		}		
 		
 		return true;
 	}
 }
