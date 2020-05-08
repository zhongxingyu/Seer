 package com.jpii.navalbattle.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JComponent;
import javax.swing.Timer;
 
 public class MenuBackground extends JComponent{
 	int width, height, pixel;
 	BufferedImage buffer;
 	
 	int boatx;
 	int boaty;
 	
 	double theta = 0.0;
 	int x,y;
 	public MenuBackground(int width, int height, int pixelSize) {
 		this.width = width;
 		this.height = height;
 		this.pixel = pixelSize;
 		
 		boatx = width;
 		
 		buffer = new BufferedImage(width*pixel,height*pixel,BufferedImage.TYPE_INT_ARGB);
 		tick();
 	}
 
 	
 	public void tick() {
 		theta += 0.5;
 		boatx--;
 		if (boatx < -50)
 			boatx = width;
 		
 		Graphics g = buffer.getGraphics();
 		g.setColor(new Color(198,225,241));
 		g.fillRect(0, 0, width,height);
 		
 		g.setColor(Color.black);
 		g.fillRect(boatx + 50, boaty+((height/3) * 2)-40, 10, 40);
 		
 		g.setColor(new Color(125,116,81));
 		g.fillRect(boatx, boaty+((height/3) * 2)-25, 100, 50);
 		Polygon p = new Polygon();
 		p.addPoint(boatx-25, boaty+((height/3) * 2)-25);
 		p.addPoint(boatx, boaty+((height/3) * 2)+50);
 		p.addPoint(boatx, boaty+((height/3) * 2)-25);
 		g.fillPolygon(p);
 		
 		g.setColor(new Color(78,131,209));
 		int minx = -1;
 		for (int x = 0; x < width; x++) {
 			double toil = Math.sin((x/5.0)+theta)*3.5;
 			int nx = (int)toil;
 			int add = 0;
 			if (toil < 0) {
				//add = (int) Math.abs(toil) * 2;
 			}
			g.drawLine(x, ((height/3) * 2) + nx, x, ((height/3) * 2) + nx +add);
 			
 			if (minx < nx && nx != 0)
 				minx = nx;
 			
 			if (boatx == x) {
 				boaty = nx;
 			}
 		}
 		g.fillRect(0,((height/3) * 2)+(minx), width, ((height/3)));
 	}
 	
 	public BufferedImage getImage() {
 		return buffer;
 	}
 	
 	public void paintComponent(Graphics g) {
 		g.drawImage(buffer, 0, 0, null);
 	}
 }
