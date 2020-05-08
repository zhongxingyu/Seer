 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.game.entity.Submarine;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.gui.controls.Control;
 import com.jpii.navalbattle.pavo.gui.controls.PButton;
 import com.jpii.navalbattle.pavo.gui.controls.PImage;
 import com.jpii.navalbattle.pavo.gui.events.PMouseEvent;
 import com.jpii.navalbattle.turn.TurnManager;
 import com.jpii.navalbattle.util.FileUtils;
 
 public class MidHud{
 
 	boolean attackGuns = false;
 	boolean attackMissiles = false;
 	
 	PImage missile;
 	PImage bullet;
 	PImage move;
 	PImage diplomacy;
 	PImage shop;
 	PImage elevation;
 	
 	PButton missileB;
 	PButton bulletB;
 	PButton moveB;
 	PButton diplomacyB;
 	PButton shopB;
 	PButton elevationB;
 	
 	PButton nextMove;
 	PButton nextEntity;
 	
 	Entity display;
 	MoveableEntity moveE;
 	
 	int width,height;
 	
 	TurnManager tm;
 	
 	public MidHud(Control c, TurnManager tm){
 		initButtons(c);
 		width = c.getWidth();
 		height = c.getHeight();
 		this.tm = tm;
 	}
 
 	public void draw(Graphics2D g){
 		drawText(g);
 	}
 	
 	public void setEntity(Entity e,MoveableEntity me){
 		moveE = me;
 		if(display!=null){
 			if(moveE==null || !display.equals(moveE)){
 				if(display.getHandle()%10 == 1){
 					MoveableEntity display = (MoveableEntity)this.display;
 					if(display.isMovableTileBeingShown()){
 						display.toggleMovable();
 					}
 					if(display.isAttackTileBeingShown()){
 						display.toggleAttackRange();
 					}
 				}
 			}
 		}
 		display = e;
 	}
 
 	private void drawText(Graphics2D g){
 		g.setColor(Color.green);
 		Font temp = g.getFont();
 		Font perks = new Font("Arial",0,10);
 		g.setFont(perks);
 		g.drawString("Shop",(width/2)-148,height-62);
 		g.drawString("Missile",(width/2)-91,height-62);
 		g.drawString("Guns",(width/2)-28,height-62);
 		g.drawString("Diplomacy",(width/2)+18,height-62);
 		g.drawString("Move",(width/2)+92,height-62);
 		g.drawString("Submerge",(width/2)+142,height-62);
 		g.setFont(temp);
 	}
 	
 	public void update(){
 		move.setVisible(false);
 		moveB.setVisible(false);
 		missile.setVisible(false);
 		missileB.setVisible(false);
 		bullet.setVisible(false);
 		bulletB.setVisible(false);
 		diplomacy.setVisible(false);
 		diplomacyB.setVisible(false);
 		shop.setVisible(false);
 		shopB.setVisible(false);
 		elevation.setVisible(false);
 		elevationB.setVisible(false);
 		
 		if(display!=null){
 			diplomacy.setVisible(true);
 			diplomacyB.setVisible(true);
 			if(moveE!=null){
 				if(moveE.getHandle()==11){
 					Submarine sub = (Submarine)moveE;
 					elevationB.setVisible(true);
 					if(!sub.isSumberged()&&tm.getTurn().getPlayer().myEntity(sub))
 						elevation.setVisible(true);
 				}
 				if(moveE.getMaxMovement()!=moveE.getMoved())
 					move.setVisible(true);
 				if(!moveE.getUsedGuns())
 					bullet.setVisible(true);
 				if(!moveE.getUsedMissiles())
 					missile.setVisible(true);
 				if(tm.getTurn().getPlayer().myEntity(moveE)){
 					diplomacy.setVisible(false);
 					diplomacyB.setVisible(false);
 					shop.setVisible(true);
 					shopB.setVisible(true);
 				}
 				moveB.setVisible(true);
 				missileB.setVisible(true);
 				bulletB.setVisible(true);
 			}
 		}
 	}
 	
 	private void initButtons(Control c){
		c.addControl(shopB = new PButton(c,(c.getWidth()/2)-150,c.getHeight()-60,30,30));
		c.addControl(missileB = new PButton(c,(c.getWidth()/2)-90,c.getHeight()-60,30,30));
		c.addControl(bulletB = new PButton(c,(c.getWidth()/2)-30,c.getHeight()-60,30,30));
		c.addControl(diplomacyB = new PButton(c,(c.getWidth()/2)+30,c.getHeight()-60,30,30));
		c.addControl(moveB = new PButton(c,(c.getWidth()/2)+90,c.getHeight()-60,30,30));
		c.addControl(elevationB = new PButton(c,(c.getWidth()/2)+150,c.getHeight()-60,30,30));
 		
 		c.addControl(nextMove = new PButton(c,"End Turn",(c.getWidth()/2)-60,c.getHeight()-130,150,40));
 		c.addControl(nextEntity = new PButton(c,"Next Ship",(c.getWidth()/2)+120,c.getHeight()-130,70,20));
 		
 		nextMove.setFont(new Font("Arial",0,35));	
 		nextEntity.setFont(new Font("Arial",0,15));
 		
 		missile = new PImage(c);
 		bullet = new PImage(c);
 		move = new PImage(c);
 		diplomacy = new PImage(c);
 		shop = new PImage(c);
 		elevation = new PImage(c);
 		
 		shop.setLoc((c.getWidth()/2)-150,c.getHeight()-60);
 		missile.setLoc((c.getWidth()/2)-90,c.getHeight()-60);
 		bullet.setLoc((c.getWidth()/2)-30,c.getHeight()-60);
 		diplomacy.setLoc((c.getWidth()/2)+30,c.getHeight()-60);
 		move.setLoc((c.getWidth()/2)+90,c.getHeight()-60);
 		elevation.setLoc((c.getWidth()/2)+150,c.getHeight()-60);
 		
 		shop.setSize(30,30);
 		missile.setSize(30,30);
 		bullet.setSize(30,30);
 		diplomacy.setSize(30,30);
 		move.setSize(30,30);
 		elevation.setSize(30,30);
 		
 		shop.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Shop.png")));
 		missile.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Missile.png")));
 		bullet.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Bullet.png")));
 		diplomacy.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Diplomacy.png")));
 		move.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Move.png")));
 		elevation.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Elevation.png")));
 		
 		shop.repaint();
 		missile.repaint();
 		bullet.repaint();
 		diplomacy.repaint();
 		move.repaint();
 		elevation.repaint();
 		
 		c.addControl(shop);
 		c.addControl(missile);
 		c.addControl(bullet);
 		c.addControl(diplomacy);
 		c.addControl(move);
 		c.addControl(elevation);
 		
 		moveB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(move.isVisible()){
 					if(moveE!=null){
 						if(moveE.isAttackTileBeingShown())
 							moveE.toggleAttackRange();
 						moveE.toggleMovable();
 					}
 				}
 				update();
 			}
 		});
 		
 		nextMove.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(nextMove.isVisible()){
 					final TurnManager tm2 = tm;
 					tm2.nextTurn();
 				}
 				update();
 			}
 		});
 		
 		bulletB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(bullet.isVisible()){
 					if(moveE!=null){
 						if(moveE.isMovableTileBeingShown())
 							moveE.toggleMovable();
 						if(moveE.isAttackTileBeingShown()&&attackMissiles){
 						}
 						else	
 							moveE.toggleAttackRange();
 						attackGuns = true;	
 						attackMissiles = false;	
 					}
 				}
 				update();
 			}
 		});
 		
 		missileB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(missile.isVisible()){
 					if(moveE!=null){
 						if(moveE.isMovableTileBeingShown())
 							moveE.toggleMovable();
 						if(moveE.isAttackTileBeingShown()&&attackGuns){
 						}
 						else	
 							moveE.toggleAttackRange();
 						attackMissiles = true;
 						attackGuns = false;	
 					}
 				}
 				update();
 			}
 		});
 		
 		elevationB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(elevation.isVisible()){
 						Submarine sub = (Submarine)display;
 						if(!sub.isSumberged()){
 							sub.toggleElevation();
 							sub.useGuns();
 							sub.useMissiles();
 						}
 				}
 				update();
 			}
 		});
 
 		c.repaint();
 	}
 
 }
