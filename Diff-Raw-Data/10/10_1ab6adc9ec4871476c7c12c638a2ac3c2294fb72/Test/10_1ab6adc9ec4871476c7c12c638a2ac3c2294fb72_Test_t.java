 package com.jpii.dagen.test;
 
 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RadialGradientPaint;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.dagen.IslandEngine;
 import com.jpii.dagen.MapType;
 
 @SuppressWarnings("serial")
 public class Test extends Applet{
 	
 	IslandEngine eng;
 	
 	int WIDTH = 200;
 	int HEIGHT = 200;
 	
 	BufferedImage shadowOuter;
 	
 	public void init() {
 		eng = new IslandEngine(WIDTH,HEIGHT);
 		setSize(WIDTH * 3, HEIGHT * 3);
 		//eng.setSmoothFactor(3);
 		eng.setIslandRadius(getWidth() / 10);
 		eng.generate(MapType.Hills, (int)(Math.random() * 4000), 1);
 		
 		shadowOuter = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
 		Graphics g = shadowOuter.getGraphics();
 		Graphics2D g2 = (Graphics2D)g;
 		Point2D center = new Point2D.Float(getWidth()/2, getHeight()/2);
         float radius = getWidth();
         Point2D focus = new Point2D.Float(getWidth()/2, getHeight()/2);
         float[] dist = {0.0f,0.3f, 1.0f};
         Color[] colors = {new Color(0,0,0,0),new Color(0,0,0,0), new Color(0,0,0,255)};
         RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.NO_CYCLE);
         g2.setPaint(p);
         g2.fillRect(0, 0, getWidth(), getHeight());
 		
 		repaint();
 	}
 	
 	public int snap(double d) {
 		if (d > 1.0)
 			d = 1.0;
 		if (d < 0.0)
 			d = 0.0;
 		return (int)(d * 255);
 	}
 	
 	public void paint(Graphics g) {
 		if (eng != null) {	
 			double[][] points = eng.getPoints();
 			@SuppressWarnings("unused")
 			double[][] water = eng.getWaterPoints();
 			g.setColor(Color.black);
 			g.fillRect(0, 0, getWidth(), getHeight());
 			for (int x = 0; x < WIDTH; x++) {
 				for (int y = 0; y < HEIGHT; y++) {
 					if (points[x][y] < 0.3){
 						g.setColor(new Color(50 - eng.r(-10,10), 100 - eng.r(-10, 0),150-  eng.r(-10, 10)));
 						g.fillRect(x * 3, y * 3, 3,3);
 					}
 					else {
 						boolean flag0 = false;
 						if (x > 3 & y > 3 && x < eng.getWidth() - 3 && y < eng.getHeight() - 3) {
 							if (points[x-1][y] < 0.3 || points[x-1][y-1] < 0.3
 									|| points[x][y-1] < 0.3 || points[x+1][y+1] < 0.3
 									|| points[x+1][y] < 0.3 || points[x][y+1] < 0.3) {
 								int reduce = (int)(points[x][y] * 119);
 								int cr = 164 - reduce + eng.r(10, 30);
 								int cg = 149 - reduce + eng.r(10, 30);
 								int cb = 125 - reduce + eng.r(10, 30);
 								@SuppressWarnings("unused")
 								int rgb = snap(points[x][y]);
 								g.setColor(new Color(cr, cg,cb));
 								flag0 = true;
 							}	
 						}
 						if (!flag0) {
 							int reduce = (int)(points[x][y] * 6);
 							int cr = 64 - reduce;//eng.r(-5,5) - reduce;
 							int cg = 128 - reduce + eng.r(-5, 5);//eng.r(-5, 5) - reduce;
 							int cb = 80 - reduce + eng.r(-5, 5); //eng.r(-5, 5) - reduce;
 							@SuppressWarnings("unused")
 							int rgb = snap(points[x][y]);
 							g.setColor(new Color(cr, cg,cb));//new Color(rgb,rgb,rgb));
 						}
 						g.fillRect(x * 3, y * 3, 3,3);
 					}
 				}
 			}
 		}
 		g.setColor(new Color(127,127,127,127));
 		for (int gridx = 0; gridx < getWidth(); gridx+=20) {
 			g.drawLine(gridx, 0, gridx, getHeight());
 		}
 		for (int gridy = 0; gridy < getHeight(); gridy+=20) {
 			g.drawLine(0, gridy, getWidth(), gridy);
 		}
 		g.drawImage(shadowOuter, 0, 0, null);
 	}
 	
 }
