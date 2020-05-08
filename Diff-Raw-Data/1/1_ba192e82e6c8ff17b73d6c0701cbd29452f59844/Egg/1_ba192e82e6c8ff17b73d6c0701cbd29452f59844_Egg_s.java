 package com.az;
 
 import java.awt.*;
 import java.util.Random;
 
 public class Egg {
 	private Point p = new Point();
 	private static int w = Yard.CELL_SIZE;
 	private static int h = w;
 	private static Random r = new Random();
 	
 	Egg(Point p) {
 		this.p = p;
 	}
 	
 	Egg() {
 		this(nextPos());
 	}
 	
 	
 	static int x, y;
 	private static Point nextPos() {
 		x = r.nextInt(Yard.COLS-1);
 		y = r.nextInt(Yard.ROWS-1);
 		while(x <1 || y*Yard.CELL_SIZE < 30) {
 			y = r.nextInt(Yard.ROWS-1); 
 		}
 		
 		return new Point(x, y);
 	}
 
 	
 	public void draw(Graphics g) {
 		Color c = g.getColor();
 		g.setColor(Color.ORANGE);
 		g.fillOval(Yard.CELL_SIZE*p.x, Yard.CELL_SIZE*p.y, w, h);
 		g.setColor(c);
 	}
 
 	
 	public Rectangle getRect() {
 		return new Rectangle(Yard.CELL_SIZE*p.x, Yard.CELL_SIZE*p.y, w, h);
 	}
 	
 	public void reset() {
 		p = nextPos();
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
