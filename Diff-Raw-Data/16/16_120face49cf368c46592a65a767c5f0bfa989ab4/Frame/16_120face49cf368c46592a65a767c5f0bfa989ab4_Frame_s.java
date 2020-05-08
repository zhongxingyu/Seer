 /*========== Frame.java ==========
   Wrapper class for java's built in BufferedImage class.
   Allows use of java's DrawLine and image saving methods
 
   =========================*/
 
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import javax.imageio.*;
 
 public class Frame
 {
     public static final int XRES = 500;
     public static final int YRES = 500;
     public static final int COLOR_VALUE = 255;
     
     private int maxx, maxy, maxcolor;
     private BufferedImage bi;
     
     public Frame()
     {
 	this(XRES, YRES);
     }
     
     public Frame(int xres, int yres)
     {
 	maxx = xres;
 	maxy = yres;
 	maxcolor = COLOR_VALUE;
 	bi = new BufferedImage(maxx,maxy,BufferedImage.TYPE_BYTE_INDEXED);
     }
     
     /*======== public void drawLines() ==========
       Inputs:  EdgeMatrix em
       Color c 
       Returns: 
       calls drawLine so that it draws all the lines within PointMatrix pm
       ====================*/
     public void drawLines(EdgeMatrix em, Color c)
     {
 	for(int i = 0; i < em.getLastCol() / 2; i++)
 	{
 	    drawLine(em.getX(2*i), em.getY(2*i), em.getX(2*i+1), em.getY(2*i+1), c);
 	}
     }
     
     /*======== public void drawLine() ==========
       Inputs:  int x0
       int y0
       int x1
       int y1
       Color c 
       Returns: 
       Wrapper for java's built in drawLine routine
       ====================*/
     public void drawLine(int x0, int y0, int x1, int y1, Color c)
     {
 	Graphics2D g = bi.createGraphics();
 	g.setColor(c);
 	g.drawLine(x0,y0,x1,y1);
     }	
  
    
     /*======== public void save() ==========
       Inputs:  String filename 
       Returns: 
       saves the bufferedImage as a png file with the given filename
       ====================*/
     
     public void save(String filename)
     {
 	try
 	{
 	    File fn = new File(filename);
	    ImageIO.write(bi, filename.substring(filename.lastIndexOf('.') + 1), fn);
 	}
	catch (IOException e)
 	{
	    try
	    {
		File fn = new File(filename);
		ImageIO.write(bi,"png",fn);
	    }
	    catch (IOException e2)
	    {
		
	    }
 	}
     }
 }
