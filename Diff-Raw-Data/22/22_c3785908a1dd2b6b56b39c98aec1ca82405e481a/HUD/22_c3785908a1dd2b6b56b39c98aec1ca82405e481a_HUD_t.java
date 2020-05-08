 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.gui.WindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 
 public class HUD extends PWindow{
 	
 	Entity display;
 	Rand ran;
 	GradientPaint gp;
 	
 	public HUD(WindowManager parent,int x, int y, int width, int height){
 		super(parent, x, y, width, height);
 		display = null;
 		ran = Game.Settings.rand;
<<<<<<< HEAD
 		gp = new GradientPaint(0,0,new Color(0,0,255),0,height,new Color(0,0,0));
=======
		gp = new GradientPaint(0,0,getBlue(),0,height,getBlack());
	}
	public HUD(WindowManager parent){
		super(parent);
		display = null;
		ran = Game.Settings.rand;
		gp = new GradientPaint(0,0,getBlue(),0,height,getBlack());
>>>>>>> Fixed Anthony.
 	}
 	
 	public void paint(Graphics2D g) {
 		super.paint(g);
		
		
		// This is all you have to do.
		g.setPaint(gp);
		
		g.fillRect(0,0,getWidth(),getHeight());
		//System.out.println("see anything?"); You definitly don't need this line.
 	}
 	
 	public void setEntity(Entity e){
 		display = e;
 		System.out.println("Entity was clicked");
 		update();
 	}
 	
 	public void update(){
 		if(display != null){
 			drawMenu();
 		}
 	}
 	
 	private void drawMenu(){
 		repaint();
 	}
 	
 	private Color getBlue(){
 		int r = ran.nextInt(85,115);
 		int g = ran.nextInt(0,255/2);
 		int b = ran.nextInt((255/2)+g/2,255);
 		return new Color(r,g,b);
 	}
 	
 	private Color getBlack(){
 		int r = 0;
 		int b = ran.nextInt(50,(255/3));
 		return new Color(r,r,b);
 	}
 	
 	private Color getBrown(){
 		int r = ran.nextInt(85,115);
 		int g = ran.nextInt(0,255/2);
 		int b = ran.nextInt(0,255/3);
 		return new Color(r,g,b);
 	}
 	
 }
