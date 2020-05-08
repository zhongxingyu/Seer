 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.game.entity.PortEntity;
 import com.jpii.navalbattle.game.entity.Submarine;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.gui.NewWindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.Control;
 import com.jpii.navalbattle.pavo.gui.controls.PButton;
 import com.jpii.navalbattle.pavo.gui.controls.PImage;
 import com.jpii.navalbattle.pavo.gui.events.PMouseEvent;
 import com.jpii.navalbattle.game.turn.PortShop;
 import com.jpii.navalbattle.game.turn.ShipShop;
 import com.jpii.navalbattle.game.turn.TurnManager;
 import com.jpii.navalbattle.util.FileUtils;
 
 public class MidHud{
 	
 	PImage missile;
 	PImage bullet;
 	PImage move;
 	PImage diplomacy;
 	PImage shop;
 	PImage elevation;
 	PImage airstrike;
 	
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
 	NewWindowManager parent;
 	
 	int width,height;
 	String secondary = "Missiles";
 	
 	TurnManager tm;
 	
 	public MidHud(Control c, TurnManager tm,NewWindowManager pare){
 		initButtons(c);
 		width = c.getWidth();
 		height = c.getHeight();
 		parent = pare;
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
 					if(display.isPrimaryTileBeingShown()){
 						display.togglePrimaryRange();
 					}
 					if(display.isSecondaryTileBeingShown()){
 						display.toggleSecondaryRange();
 					}
 				}
 			}
 		}
 		display = e;
 	}
 
 	private void drawText(Graphics2D g){
 		g.setColor(Color.black);
 		Font temp = g.getFont();
 		Font perks = new Font("Arial",0,10);
 		g.setFont(perks);
 		g.drawString("Shop",(width/2)-148,height-62);
 		g.drawString(secondary,(width/2)-91,height-62);
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
 		airstrike.setVisible(false);
 		secondary = "Missile";
 		
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
 				if(moveE.getHandle()==21){
					airstrike.setVisible(true);
 					missile.setVisible(false);
 					secondary = "Airstrike";
 				}
 			}
 			if(display.getHandle()%10 == 2){
 				shop.setVisible(true);
 				shopB.setVisible(true);
 			}
 		}
 	}
 	
 	private void initButtons(Control c){		
 		c.addControl(shopB = new PButton(c,(c.getWidth()/2)-150,c.getHeight()-60,32,31));
 		c.addControl(missileB = new PButton(c,(c.getWidth()/2)-90,c.getHeight()-60,32,31));
 		c.addControl(bulletB = new PButton(c,(c.getWidth()/2)-30,c.getHeight()-60,32,31));
 		c.addControl(diplomacyB = new PButton(c,(c.getWidth()/2)+30,c.getHeight()-60,32,31));
 		c.addControl(moveB = new PButton(c,(c.getWidth()/2)+90,c.getHeight()-60,32,31));
 		c.addControl(elevationB = new PButton(c,(c.getWidth()/2)+150,c.getHeight()-60,32,31));
 		
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
 		airstrike = new PImage(c);
 		
 		shop.setLoc((c.getWidth()/2)-150,c.getHeight()-60);
 		missile.setLoc((c.getWidth()/2)-90,c.getHeight()-60);
 		bullet.setLoc((c.getWidth()/2)-30,c.getHeight()-60);
 		diplomacy.setLoc((c.getWidth()/2)+30,c.getHeight()-60);
 		move.setLoc((c.getWidth()/2)+90,c.getHeight()-60);
 		elevation.setLoc((c.getWidth()/2)+150,c.getHeight()-60);
 		airstrike.setLoc((c.getWidth()/2)-90,c.getHeight()-60);
 		
 		shop.setSize(30,30);
 		missile.setSize(30,30);
 		bullet.setSize(30,30);
 		diplomacy.setSize(30,30);
 		move.setSize(30,30);
 		elevation.setSize(30,30);
 		airstrike.setSize(30,30);
 		
 		shop.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Shop.png")));
 		missile.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Missile.png")));
 		bullet.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Bullet.png")));
 		diplomacy.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Diplomacy.png")));
 		move.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Move.png")));
 		elevation.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Elevation.png")));
 		airstrike.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Airplane.png")));
 		
 		shop.repaint();
 		missile.repaint();
 		bullet.repaint();
 		diplomacy.repaint();
 		move.repaint();
 		elevation.repaint();
 		airstrike.repaint();
 		
 		c.addControl(shop);
 		c.addControl(missile);
 		c.addControl(bullet);
 		c.addControl(diplomacy);
 		c.addControl(move);
 		c.addControl(elevation);
 		c.addControl(airstrike);
 		
 		moveB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				moveAction();
 			}
 		});
 		
 		nextMove.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				turnAction();
 			}
 		});
 		
 		bulletB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				primaryAction();
 			}
 		});
 		
 		missileB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				secondaryAction();
 			}
 		});
 		
 		elevationB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				submergeAction();
 			}
 		});
 		
 		shopB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				shopAction();					
 			}
 		});
 		
 		nextEntity.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				nextAction();
 			}
 		});
 
 		c.repaint();
 	}
 	
 	public void moveAction(){
 		if(move.isVisible()){
 			if(moveE!=null){
 				if(moveE.isSecondaryTileBeingShown())
 					moveE.toggleSecondaryRange();
 				if(moveE.isPrimaryTileBeingShown())	
 					moveE.togglePrimaryRange();	
 				moveE.toggleMovable();
 			}
 		}
 		update();
 	}
 	
 	public void turnAction(){
 		if(nextMove.isVisible()){
 			final TurnManager tm2 = tm;
 			tm2.nextTurn();
 		}
 		update();
 	}
 	
 	public void primaryAction(){
 		if(bullet.isVisible()){
 			if(moveE!=null){
 				if(moveE.isMovableTileBeingShown())
 					moveE.toggleMovable();
 				if(moveE.isSecondaryTileBeingShown())
 					moveE.toggleSecondaryRange();	
 				moveE.togglePrimaryRange();	
 			}
 		}
 		update();
 	}
 	
 	public void secondaryAction(){
 		if(missile.isVisible()||airstrike.isVisible()){
 			if(moveE!=null){
 				if(moveE.isMovableTileBeingShown())
 					moveE.toggleMovable();
 				if(moveE.isPrimaryTileBeingShown())
 					moveE.togglePrimaryRange();
 				moveE.toggleSecondaryRange();	
 			}
 		}
 		update();
 	}
 	
 	public void submergeAction(){
 		if(elevation.isVisible()){
 			Submarine sub = (Submarine)display;
 			if(!sub.isSumberged()){
 				sub.toggleElevation();
 				sub.usePrimary();
 				sub.useSecondary();
 			}
 		}
 		update();
 	}
 	
 	public void shopAction(){
 		if(display!=null&&display.getHandle()%10 == 2){
 			new PortShop(parent,(PortEntity)display);
 			update();
 		}
 		else if(moveE!=null){
 			new ShipShop(parent,moveE);
 			update();
 		}
 	}
 	
 	public void nextAction(){
 		NavalGame.getManager().getTurnManager().getTurn().getPlayer().nextEntity(display);
 	}
 
 }
