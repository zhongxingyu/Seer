 package com.jpii.dagen.test;
 
 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RadialGradientPaint;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.dagen.EngineStatistics;
 import com.jpii.dagen.IslandEngine;
 import com.jpii.dagen.MapType;
 
 @SuppressWarnings("serial")
 public class Test extends Applet implements KeyListener{
 	
 	IslandEngine eng;
 	
 	int WIDTH = 200;
 	int HEIGHT = 200;
 	int PIXEL = 3;
 	
 	BufferedImage shadowOuter;
 	BufferedImage grid;
 	
 	public void init() {
 		eng = new IslandEngine(WIDTH,HEIGHT);
		setSize(WIDTH * PIXEL, HEIGHT * PIXEL);
 		eng.setSmoothFactor(3);
 		eng.setIslandRadius(getWidth() / 8);
 		eng.generate(MapType.Hills, (int)(Math.random() * 4000000), 1);
 		
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
 		
         grid = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
 		g = grid.getGraphics();
 		g.setColor(new Color(127,127,127,127));
 		for (int gridx = 0; gridx < getWidth(); gridx+=20) {
 			g.drawLine(gridx, 0, gridx, getHeight());
 		}
 		for (int gridy = 0; gridy < getHeight(); gridy+=20) {
 			g.drawLine(0, gridy, getWidth(), gridy);
 		}
         
 		addKeyListener(this);
 		
 		repaint();
 	}
 	
 	public double snap(double d) {
 		if (d > 1.0)
 			d = 1.0;
 		if (d < 0.0)
 			d = 0.0;
 		return (d);
 	}
 	
 	public void paint(Graphics g) {
 		if (eng != null) {
 			double[][] points = eng.getPoints();
 			g.setColor(Color.black);
 			g.fillRect(0, 0, getWidth(), getHeight());
 			for (int x = 0; x < WIDTH; x++) {
 				for (int y = 0; y < HEIGHT; y++) {
 					double wamount = 0.70;
 					if (points[x][y] < wamount){
 						int reduce = (int)(snap(points[x][y]) * 49.0);
 						g.setColor(new Color(50 - reduce,
 								100 - reduce,150 - reduce));
 						g.fillRect(x * PIXEL, y * PIXEL, PIXEL,PIXEL);
 					}
 					else {
 						boolean flag0 = false;
 						if (x > 3 & y > 3 && x < eng.getWidth() - 3 && y < eng.getHeight() - 3) {
 							if (points[x-1][y] < wamount || points[x-1][y-1] < wamount
 									|| points[x][y-1] < wamount || points[x+1][y+1] < wamount
 									|| points[x+1][y] < wamount || points[x][y+1] < wamount) {
 								int reduce = 30;//(int)(snap(points[x][y]) * 120);
 								int cr = 164 - reduce + eng.r(-20, 10);
 								int cg = 149 - reduce + eng.r(-20, 10);
 								int cb = 125 - reduce + eng.r(-20, 10);
 								g.setColor(new Color(cr, cg,cb));
 								flag0 = true;
 							}	
 						}
 						if (!flag0) {
 							int reduce = (int)(snap(points[x][y]) * 63);
 							int cr = 64 - reduce;//eng.r(-5,5) - reduce;
 							int cg = 128 - reduce + eng.r(-5, 5);//eng.r(-5, 5) - reduce;
 							int cb = 80 - reduce + eng.r(-5, 5); //eng.r(-5, 5) - reduce;
 							g.setColor(new Color(cr, cg,cb));//new Color(rgb,rgb,rgb));
 						}
 						g.fillRect(x * PIXEL, y * PIXEL, PIXEL,PIXEL);
 					}
 				}
 			}
 		}
 		g.drawImage(grid, 0, 0, null);
 		
 		g.drawImage(shadowOuter, 0, 0, null);
 		
 		EngineStatistics stats = new EngineStatistics(eng);
 		int amountWater = stats.getPercentWater();
 		
 		if (amountWater < 70 || amountWater > 90) {
 			eng.generate(MapType.Hills, (int)(Math.random() * 4000000), 1);
 			repaint();
 		}
 		//int amountLand = 100 - amountWater;
 		
 		g.setFont(new Font("Segoe UI Light", Font.PLAIN, (int)(getWidth() / (PIXEL * 4.0)) ));
 		g.setColor(new Color(255,255,255,100));
 		g.drawString("Seed:" + eng.getSeed(), 0, 40);
 		g.drawString("Gens:" + eng.getCycles(), 0, 80);
 		g.drawString("Press <space> to regen", 0, 120);
 		g.drawString("Percent water:" + amountWater, 0, 160);
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
 			eng.generate(MapType.Hills, (int)(Math.random() * 100000), 1);
 			repaint();
 		}
 	}
 	public void keyReleased(KeyEvent arg0) {
 	}
 	public void keyTyped(KeyEvent arg0) {
 	}
 	
 }
