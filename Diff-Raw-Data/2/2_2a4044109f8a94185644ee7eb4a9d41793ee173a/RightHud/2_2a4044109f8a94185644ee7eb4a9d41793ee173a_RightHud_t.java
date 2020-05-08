 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.game.entity.PortEntity;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.gui.controls.Control;
 import com.jpii.navalbattle.util.FileUtils;
 
 public class RightHud {
 	BufferedImage entityImg;
 	int boxwidth,boxheight,boxx,boxy;
 	int centerx,centery;
 	int imgx,imgy;
 	String location = new String("");
 	String health = new String("");
 	String movement = new String("");
 	String missiles = new String("");
 	
 	Entity display;
 	MoveableEntity move;
 	
 	public RightHud(Control parent,int width, int height){
 		centerx = width-210;
 		centery = height/2;
 	}
 	
 	public void draw(Graphics2D g){
 		drawFrame(g, boxx, boxy, boxwidth, boxheight);
 		if(display!=null){
 			g.drawImage(entityImg,boxx+50,boxy+50,null);
 			drawString(g,location, centerx, centery+60);
 			drawString(g,health, centerx, centery-45);
 			drawString(g,movement, centerx, centery+40);
 			drawString(g,missiles, centerx, centery-25);
 		}
 	}
 	
 	public void setEntity(Entity e,MoveableEntity me){
 		display = e;
 		move = me;
 	}
 	
 	private void drawFrame(Graphics2D g,int x, int y, int width, int height) {
 		g.setColor(new Color(126,105,65));
 		g.fillRect(x,y,width,height);
 		g.setColor(new Color(65,54,33));
 		for (int x22 = x+8; x22 < (x+width)-8; x22 += 8) {
 			g.drawLine(x22+x-50,y,x+x22+4-50,y+8);
 		}
 		for (int x22 = x+8; x22 < (x+width)-8; x22 += 8) {
 			g.drawLine(x22+4+x-50,y+height-9,x22+x-50,y+height);
 		}
 		for (int y22 = 8+y; y22 < (y+height)-8; y22 += 8) {
 			g.drawLine(x,y22+y,8+x,y+y22+4);
 		}
 		for (int y22 = 8+y; y22 < (y+height)-8; y22 += 8) {
 			g.drawLine(x+height-9,y+y22+4,x+width,y+y22);
 		}
 		g.setColor(new Color(169,140,86));
 		g.fillRect(8+x,y+8,width-16,height-16);
 		g.setColor(Color.black);
 		g.drawRect(x,y,width-1,height-1);
 		g.drawRect(8+x,8+y,width-16,height-16);
 	}
 	
 	private void drawString(Graphics2D g,String s,int x, int y){
 		if(s!=null){
 			g.drawString(s, x-(getWidth(g,s))/2, y);
 		}
 	}
 	
 	private int getWidth(Graphics2D g,String s){
 		return 4+g.getFontMetrics().stringWidth(s);
 	}
 	
 	public void update(){
 		boxx = boxy = boxheight = boxwidth = 0;
 		missiles = health = movement = "";
 		if(display!=null){
 			if (display.getHandle()%10 == 2) {
 				PortEntity display = (PortEntity)this.display;
 				entityImg = display.getIcon();
 			}
 			else
 				entityImg = FileUtils.getImage(display.imgLocation);
 			int tempwidth = entityImg.getWidth();
 			int tempheight = entityImg.getHeight();
 			boxx = centerx - (tempwidth/2) - 50;
 			boxy = centery - (tempheight/2) - 50;
 			boxwidth = tempwidth+100;
 			boxheight = tempheight+100;
			location = ("[R:"+display.getLocation().getRow()+" C:"+display.getLocation().getCol()+"]");
 			if(move!=null){
 				health = ("Health: "+move.getPercentHealth()+"%");
 				movement = ("Movement Left: "+(move.getMaxMovement()-move.getMoved())+" out of "+move.getMaxMovement());
 				if(move.getMissileCount()>0){
 					if(move.getHandle()!=21){
 						missiles="Missiles Left: "+move.getMissileCount();
 					}
 				}
 			}
 			else if(display.getHandle()%10 == 2){
 				PortEntity temp = (PortEntity) display;
 				health = ("Health: "+temp.getPercentHealth()+"%");
 			}
 		}
 	}
 }
