 package jp.ac.tsukuba.cs.conclave.earthquake.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import jp.ac.tsukuba.cs.conclave.earthquake.data.DataList;
 import jp.ac.tsukuba.cs.conclave.earthquake.data.DataPoint;
 
 public class EarthQuakeDrawable extends JPanel {
 
 	int height = 200;
 	int width = 200;
 
 	// drawing borders
 	double minlon;
 	double minlat;
 	double maxlon;
 	double maxlat;
 	
 	DataList aftershocks;
 	DataPoint origin;
 	
 	BufferedImage map;
 	double mapsx = 123.693237;
 	double mapsy = 24.246965;
 	double mapex = 145.797729;
 	double mapey = 45.521744;
 	
 	int mapclipsx;
     int mapclipsy;
     int mapclipex;
     int mapclipey;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public EarthQuakeDrawable() {
         // set a preferred size for the custom panel.
         setPreferredSize(new Dimension(width,height));
         
         try {
         	// map = ImageIO.read(new File("japan.jpg"));
             map = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("japan.jpg"));
             // BLACK MAGIC!
         } catch (IOException e) {
         	System.out.println(e.getMessage());
         	System.exit(0);
         }
     }
 
 	
 	public void addAfterShocks(DataList d)
 	{
 		aftershocks = d;
 		if (d != null)
 		{
 			// Calculating borders for the image (5% above of the forecast area to each dir)
 			
             mapclipsx = (int) Math.floor(((d.minlong - mapsx)/(mapex - mapsx))*map.getWidth());
             mapclipsy = (int) Math.floor(((d.minlat - mapsy)/(mapey - mapsy))*map.getHeight());
             mapclipex = (int) Math.floor(((d.maxlong - mapsx)/(mapex - mapsx))*map.getWidth());
             mapclipey = (int) Math.floor(((d.maxlat - mapsy)/(mapey - mapsy))*map.getHeight());
 
 			
 			double delta;
 			delta = (d.maxlat - d.minlat);
 			minlat = d.minlat - delta*0.05;
 			maxlat = d.maxlat + delta*0.05;
 
 			delta = (d.maxlong - d.minlong);
 			minlon = d.minlong - delta*0.05;
 			maxlon = d.maxlong + delta*0.05;
 		}
 	}
 	
 	public void addOrigin(DataPoint d)
 	{
 		origin = d;
 	}
 	
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         g.drawRect(0, 0, width, height);
         g.setClip(0, 0, 200, 200);
         
 
         
         if (aftershocks != null)
         {
             System.out.println(map.getWidth()+"x"+map.getHeight());
             System.out.println(mapclipsx+"x"+mapclipsy+"  "+mapclipex+"x"+mapclipey);
             
             g.drawImage(map, 0, 0, width, height, mapclipsx, mapclipsy, mapclipex, mapclipey, null);
         	
         	
         	Iterator<DataPoint> it = aftershocks.data.iterator();
         	while(it.hasNext())
         	{
         		DataPoint dtmp = it.next();
         		
         		int xpos = (int) Math.floor(((dtmp.longitude - minlon)/(maxlon-minlon))*width);
         		int ypos = (int) Math.floor(((dtmp.latitude - minlat)/(maxlat-minlat))*height);
         		ypos = height - ypos;        		
         		
         		g.drawOval(xpos, ypos, 1, 1);
         	}
         	
         	int xpos = (int) Math.floor(((origin.longitude - minlon)/(maxlon-minlon))*width);
     		int ypos = (int) Math.floor(((origin.latitude - minlat)/(maxlat-minlat))*height);
     		ypos = height - ypos;        		
     		
     		// Drawing Angles
     		// FIXME: this draw straight lines, not great circles!
     		double degang;
     		
     		degang = Math.toRadians((360 + 90 - origin.S[0])%360);
     		g.setColor(Color.BLUE);
     		g.drawLine(xpos + (int) Math.floor(Math.cos(degang)*100), 
     				   ypos - (int) Math.floor(Math.sin(degang)*100), 
     				   xpos - (int) Math.floor(Math.cos(degang)*100),
     				   ypos + (int) Math.floor(Math.sin(degang)*100));
     		
     		degang = Math.toRadians((360 + 90 - origin.S[1])%360);
     		g.setColor(Color.RED);
     		g.drawLine(xpos + (int) Math.floor(Math.cos(degang)*100), 
     				   ypos - (int) Math.floor(Math.sin(degang)*100), 
     				   xpos - (int) Math.floor(Math.cos(degang)*100),
     				   ypos + (int) Math.floor(Math.sin(degang)*100));
     		
     		g.setColor(Color.GREEN);
     		g.drawOval(xpos, ypos, 2, 2);
         	
         }
         else
         	g.drawImage(map, 0, 0, width, height, 0, 0, map.getWidth(), map.getHeight(), null);
     }
 	
 }
