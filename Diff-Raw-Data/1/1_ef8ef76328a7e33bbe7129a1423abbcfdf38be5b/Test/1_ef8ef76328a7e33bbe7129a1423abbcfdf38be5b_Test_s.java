 package com.jpii.dagen.test;
 
 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Graphics;
 
 import com.jpii.dagen.IslandEngine;
 import com.jpii.dagen.MapType;
 
 public class Test extends Applet{
 	
 	IslandEngine eng;
 	
 	int WIDTH = 100;
 	int HEIGHT = 100;
 	
 	public void init() {
 		eng = new IslandEngine(WIDTH,HEIGHT);
 		setSize(WIDTH * 2, HEIGHT * 2);
 		eng.generate(MapType.Hills, (int)(Math.random() * 4000), 1);
 		repaint();
 	}
 	
 	public void paint(Graphics g) {
 		if (eng != null) {	
 			double[][] points = eng.getPoints();
 			double[][] water = eng.getWaterPoints();
 			g.setColor(Color.black);
 			g.fillRect(0, 0, getWidth(), getHeight());
 			for (int x = 0; x < WIDTH; x++) {
 				for (int y = 0; y < HEIGHT; y++) {
 					int rgb = (int)(points[x][y] * 255);
 					if (water[x][y] != 0)
 						g.setColor(new Color(rgb,rgb,rgb));
 					else
 						g.setColor(new Color(0,0,rgb));
 					g.fillRect(x * 3, y * 3, 3,3);
 				}
 			}
 		}
 	}
 	
 }
