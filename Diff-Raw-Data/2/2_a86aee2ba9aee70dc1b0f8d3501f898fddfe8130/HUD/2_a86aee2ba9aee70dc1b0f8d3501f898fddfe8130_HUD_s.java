 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.gui.WindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 
 public class HUD extends PWindow{
 	
 	GradientPaint gp;
 	EntityBox entityBox;
 	
 	public HUD(WindowManager parent,int x, int y, int width, int height){
 		super(parent, x, y, width, height);
 		gp = new GradientPaint(0,0,new Color(96,116,190),0,height,new Color(0,0,54));
 		setTitleVisiblity(false);
 		setVisible(false);
 		entityBox = new EntityBox(this,width-200,height/2);
 		//x and y passed here are the center of the Frame/Image!!!
 		addControl(entityBox);
 	}
 	
 	public void paint(Graphics2D g) {
 		super.paint(g);
 		g.setPaint(gp);
 		g.fillRect(0,0,getWidth(),getHeight());
 	}
 	
 	public void paintAfter(Graphics2D g){
 		super.paintAfter(g);
 	}
 	
 	public void setEntity(Entity e){		
 		update(e);
 		entityBox.setEntity(e);
 	}
 	
 	public void update(Entity e){
 		if(e != null){
			setChildVisible(false);
 		}
 		else{
 			setChildVisible(false);
 		}
 		repaint();
 	}
 	
 	public void setVisible(boolean value){
 		super.setVisible(value);
 	}
 	
 	private void setChildVisible(boolean value){
 		super.setVisible(value);
 		for(int k = 0;k<getTotalControls();k++){
 			getControl(k).setVisible(value);
 		}
 	}
 	
 }
