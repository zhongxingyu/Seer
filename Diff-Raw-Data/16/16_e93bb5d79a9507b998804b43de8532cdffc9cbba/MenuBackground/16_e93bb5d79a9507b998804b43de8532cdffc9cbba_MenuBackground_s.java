 package com.jpii.navalbattle.gui;
 
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
 import java.awt.event.*;
import java.awt.image.BufferedImage;

 import javax.swing.JComponent;
 
import com.jpii.navalbattle.renderer.Helper;

@SuppressWarnings({ "serial", "unused" })
 public class MenuBackground extends JComponent implements MouseListener{
 	int width, height, pixel;
 	BufferedImage buffer;
 	
 	double boatx;
 	int boaty;
 	
 	double theta = 0.0;
 	double theta2 = 0.0;
 	double whalex = 0;
 	int whaley = 0;
 	double whaletheta = 0.0;
	boolean happy = true;//int x,y;
 	
 	public MenuBackground(int width, int height, int pixelSize) {
 		addMouseListener(this);
 		this.width = width;
 		this.height = height;
 		this.pixel = pixelSize;
 		
 		boatx = width;
 		whalex = width - 120;
 		
 		buffer = new BufferedImage(width*pixel,height*pixel,BufferedImage.TYPE_INT_ARGB);
 		tick();
 	}
 
 	
 	public void tick() {
 		theta += 0.5;
 		theta2 += 1.1;
 		if (happy) {
 			whalex -= 1.25;
 		}
 		else {
 			whalex -= 4;
 		}
 		boatx -= 0.75;
 		if (boatx < -125)
 			boatx = width;
 		if (whalex < -200)
 			whalex = width;
 		
 		Graphics g = buffer.getGraphics();
 		g.setColor(new Color(198,225,241));
 		g.fillRect(0, 0, width,height);
 		
 		if (happy) {
 			g.setColor(new Color(110,137,145));
 		}
 		else {
 			g.setColor(new Color(172,83,85));
 		}
 		int yey2 = boaty+((height/3) * 2) + whaley - 40;
 		g.fillArc((int)whalex,yey2, 40, 50, -90, -180);
 		g.fillRect((int)whalex+20, yey2, 50, 50);
 		g.fillArc((int)whalex+30,yey2,75,50,-90,180);
 		Polygon p2 = new Polygon();
 		p2.addPoint((int)whalex+90, yey2+5);
 		p2.addPoint((int)whalex+120, yey2+30);
 		p2.addPoint((int)whalex+140, yey2+20);
 		p2.addPoint((int)whalex+120, yey2+50);
 		p2.addPoint((int)whalex+90, yey2+50);
 		g.fillPolygon(p2);
 		
 		g.setColor(Color.black);
 		if (happy) {
 			g.drawArc((int)whalex-17, yey2+25, 40,10, 0, -90);
 			g.setColor(Color.white);
 			g.fillOval((int)whalex+15, yey2+5, 15,15);
 			g.setColor(Color.black);
 			g.fillOval((int)whalex+18, yey2+8, 7,7);
 		}
 		else {
 			g.drawArc((int)whalex-17, yey2+25, 40,10, 0, 90);
 			g.drawLine((int)whalex+20, yey2, (int)whalex+10, yey2 + 10);
 			g.fillOval((int)whalex+15, yey2+5, 5,5);
 		}
 		
 		
 		g.setColor(new Color(48,101,169));
 		for (int x = 0; x < width; x++) {
 			double toil = Math.sin((x/5.0)+theta2)*4.0;
 			int nx = (int)toil;
 			g.drawLine(x, ((height/3) * 2) + nx, x, ((height/3) * 2) + nx+5);
 		}
 		
 		
 		g.setColor(Color.black);
 		g.fillRect((int)boatx + 60, boaty+((height/3) * 2)-40, 10, 40);
 		g.fillRect((int)boatx + 40, boaty+((height/3) * 2)-40, 10, 40);
 		g.fillRect((int)boatx + 20, boaty+((height/3) * 2)-40, 10, 40);
 		g.setColor(Color.white);
 		g.fillRect((int)boatx + 60, boaty+((height/3) * 2)-38, 10, 5);
 		g.fillRect((int)boatx + 40, boaty+((height/3) * 2)-38, 10, 5);
 		g.fillRect((int)boatx + 20, boaty+((height/3) * 2)-38, 10, 5);
 		
 		g.setColor(new Color(125,116,81));
 		g.fillRect((int)boatx, boaty+((height/3) * 2)-26, 100, 50);
 		g.fillArc((int)boatx+85, boaty+((height/3) * 2)-26, 30,50,-90,180);
 		Polygon p = new Polygon();
 		p.addPoint((int)boatx-25, boaty+((height/3) * 2)-26);
 		p.addPoint((int)boatx, boaty+((height/3) * 2)+49);
 		p.addPoint((int)boatx, boaty+((height/3) * 2)-26);
 		g.fillPolygon(p);
 		
 		g.setColor(new Color(78,131,209));
 		int minx = -1;
 		for (int x = 0; x < width; x++) {
 			double toil = Math.sin((x/5.0)+theta)*3.5;
 			int nx = (int)toil;
 			int add = 0;
 			if (toil < 0) {
 				add = (int) Math.abs(toil) * 2;
 			}
 			if (nx == 0) {
 				add = (int)(toil * 3.0);
 			}
 			add = (int)(5.05);
 			g.drawLine(x, ((height/3) * 2) + nx, x, ((height/3) * 2) + nx + add);
 			
 			if (minx < nx && nx != 0)
 				minx = nx;
 			
 			if ((int)boatx == x) {
 				boaty = nx;
 			}
 			
 			if ((int)whalex == x) {
 				whaley = nx/2;
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
 
 
 	public void mouseClicked(MouseEvent arg0) {	
 		int mx = arg0.getX();
 		int my = arg0.getY();
 		int yey2 = boaty+((height/3) * 2) + whaley - 40;
 		if (mx >= whalex && mx <= whalex + 140 && my > yey2 - 25 && my < yey2 + 60) {
 			happy = !happy;
 		}
 	}
 	public void mouseEntered(MouseEvent arg0) {	
 	}
 	public void mouseExited(MouseEvent arg0) {
 	}
 	public void mousePressed(MouseEvent arg0) {
 	}
 	public void mouseReleased(MouseEvent arg0) {
 	}
 }
