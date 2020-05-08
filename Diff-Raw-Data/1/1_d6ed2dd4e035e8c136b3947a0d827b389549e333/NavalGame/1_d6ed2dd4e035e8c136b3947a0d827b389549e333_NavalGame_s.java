 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.game;
 
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.game.entity.*;
 import com.jpii.navalbattle.game.gui.HUD;
 import com.jpii.navalbattle.game.gui.PlayerProfileWindow;
 import com.jpii.navalbattle.game.gui.ShipInfoWindow;
 import com.jpii.navalbattle.game.gui.StatusBar;
 import com.jpii.navalbattle.pavo.*;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.gui.GridWindow;
 import com.jpii.navalbattle.pavo.gui.MessageBox;
 import com.jpii.navalbattle.pavo.gui.MessageBoxIcon;
 import com.jpii.navalbattle.pavo.gui.OmniMap;
 import com.jpii.navalbattle.pavo.gui.TestWindowWithNewAPI;
 import com.jpii.navalbattle.renderer.weather.WeatherMode;
 
 /**
  * @author MKirkby
  * The game file.
  */
 public class NavalGame extends Game{
 	PlayerProfileWindow ppw;
 	ShipInfoWindow siw;
 	OmniMap omnimap;
 	StatusBar sb;
 	NavalManager nm;
 	HUD hud;
 	
 	TestWindowWithNewAPI twwna;
 	
 	GridWindow test;
 	
 	float airStrike = -1;
 	
 	public NavalGame() {
 		super();
 		hud = new HUD();
 		nm = new NavalManager(getWorld());
 		getWorld().setEntityManager(nm);
 		omnimap = new OmniMap(getWorld());
 		ppw = new PlayerProfileWindow();
 		sb = new StatusBar(this);
 		test = new GridWindow();
 		test.setGridLocation(10,10);
 		ppw.setLoc(200,200);
 		siw = new ShipInfoWindow();
 		siw.setLoc(350,350);
 		getWorld().getWeather().setWeather(WeatherMode.Sunny);
 		getWinMan().add(ppw);
 		getWinMan().add(siw);
 		getWinMan().add(sb);
 		getWinMan().add(test);
 		//MessageBox.show("Warning", "This is a message box!!!");
 		MessageBox.show("Hey there!","Could not connect to RocketGamer servers.\n\nTrying again in 10 seconds.",
 				MessageBoxIcon.Notify, false);
 		
 		twwna = new TestWindowWithNewAPI(getWinMan());
 		twwna.setLoc(200,200);
 		twwna.repaint();
 	}
 	/**
 	 * Mulithreaded updator.
 	 */
 	public void update() {
 		if (getNumUpdates() % 750 != 0) {
 			return;
 		}
 		long updatecode = getNumUpdates();
 		int ccall = 0;
 		if (omnimap == null)
 			omnimap = new OmniMap(getWorld());
 		omnimap.render();
 	}
 	/**
 	 * Called right when sunset starts.
 	 */
 	public void becomingSunset() {
 		
 	}
 	
 	public void doSync() {
 		
 	}
 	
 	/**
 	 * Called right when sunrise starts.
 	 */
 	public void becomingSunrise() {
 		
 	}
 	/**
 	 * Called right when nighttime starts.
 	 */
 	public void becomingNight() {
 		
 	}
 	/**
 	 * Called right when daytime starts.
 	 */
 	public void becomingDay() {
 		/*
 		 * No longer used:
 		 * 
 		 for (int r = 0; r < PavoHelper.getGameWidth(getWorld().getWorldSize())*2; r++) {
 			for (int c = 0; c < PavoHelper.getGameHeight(getWorld().getWorldSize())*2; c++) {
 				Entity ent = getWorld().getEntityManager().getEntity(r,c);
 				if (ent != null) {
 					//ent.setImage(null); // The daytime image would go here.
 				}
 			}
 		}*/
 	}
 	/**
 	 * Called... all the time.
 	 */
 	public void becomingDave() {
 		// Just kidding.
 	}
 	public void mouseHeldDown(MouseEvent me) {
 		super.mouseHeldDown(me);
 		//mouseDragged(me);
 	}
 	public void mouseDragged(MouseEvent me) {
 		super.mouseDragged(me);
 		if (me.getX() - twwna.getLocX() >= 0 && me.getX() - twwna.getLocX() < twwna.getWidth()
 				&& me.getY() - twwna.getLocY() >= 0 && me.getY() - twwna.getLocY() < twwna.getHeight())
 			twwna.onMouseDrag(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY());
 		if (getWinMan().mouseDragged(me))
 			return;
 		if (omnimap.mouseDragged(me))
 			return;
 		int mx = me.getX();
 		int my = me.getY();
 		int mzx = 0;
 		int mzy = 0;
 		int ww = (Game.Settings.currentWidth/2);
 		int wh = (Game.Settings.currentHeight/2);
 		int ad = 24;
 		if (mx < ww) {
 			mzx = (ww - mx)/ad;
 		}
 		else
 			mzx = -((mx-ww))/ad;
 		if (my < wh) {
 			mzy = (wh - my)/ad;
 		}
 		else
 			mzy = -((my-wh))/ad;
 		int fgax = getWorld().getScreenX()+mzx;
 		int fgaz = getWorld().getScreenY()+mzy;
 		if (fgax > 200)
 			fgax = 200;
 		if (fgaz > 200)
 			fgaz = 200;
 		if (fgax < -((PavoHelper.getGameWidth(getWorld().getWorldSize()) * 100)-100))
 			fgax = -((PavoHelper.getGameWidth(getWorld().getWorldSize()) * 100)-100);
 		if (fgaz < -((PavoHelper.getGameHeight(getWorld().getWorldSize()) * 100)-100))
 			fgaz = -((PavoHelper.getGameHeight(getWorld().getWorldSize()) * 100)-100);
 		getWorld().setLoc(fgax, fgaz);
 		//forceUpdate(); // SEE WARNING IN DESCRIPTION!!! THIS METHOD IS NOT ACTUALLY DECREPATED!!!
 	}
 	/**
 	 * Button1 == Left
 	 * Button2 == Middle?
 	 * Button3 == Right?
 	 */
 	public void mouseDown(MouseEvent me) {
 		super.mouseDown(me);
 		
 		if (me.getX() >= twwna.getLocX() && me.getX() < twwna.getWidth() + twwna.getLocX()
 				&& me.getY() >= twwna.getLocY() && me.getY() < twwna.getHeight() + twwna.getLocY())
 			twwna.onMouseDown(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY(),me.getButton());
 		
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		Tile current = nm.getTile(chy,chx);
 		
 		if (getWinMan().mouseDown(me)||(omnimap.mouseDown(me)))
 			return;
 		
 		else if (Game.Settings.isFinishedGenerating && getWorld().getEntityManager().getTilePercentLand(chy,chx) <= 5){
 			if(current==null){
 				if(me.getButton() == MouseEvent.BUTTON1){
 					if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, chy, chx, 4)){
 						new BattleShip(this.getWorld().getEntityManager(),new Location(chy,chx),BattleShip.BATTLESHIP_ID,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT,Game.Settings.rand.nextInt(0,3));
 					}
 					else if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, chy, chx, 4)){
 						BattleShip b = new BattleShip(this.getWorld().getEntityManager(),new Location(chy,chx),BattleShip.BATTLESHIP_ID,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM,Game.Settings.rand.nextInt(0,3));
 					}	
 				}
 				if(me.getButton() == MouseEvent.BUTTON2){
 					//new Whale(this.getWorld().getEntityManager(),new Location(chy,chx),
 						//	Game.Settings.rand.nextInt(0,3),NavalManager.w1,NavalManager.w2,NavalManager.w3);
 				}
 			}
 		}
 		
 		//airStrike = 0;
 	}
 	public void mouseUp(MouseEvent me) {
 		super.mouseUp(me);
 		if (me.getX() - twwna.getLocX() >= 0 && me.getX() - twwna.getLocX() < twwna.getWidth()
 				&& me.getY() - twwna.getLocY() >= 0 && me.getY() - twwna.getLocY() < twwna.getHeight())
 			twwna.onMouseUp(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY(),me.getButton());
 	}
 	public void mouseMove(MouseEvent me) {
 		super.mouseMove(me);
 		if (me.getX() - twwna.getLocX() >= 0 && me.getX() - twwna.getLocX() < twwna.getWidth()
 				&& me.getY() - twwna.getLocY() >= 0 && me.getY() - twwna.getLocY() < twwna.getHeight())
 			twwna.onMouseHover(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY());
 		omnimap.mouseMoved(me);
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		sb.setMouseTileLocation(chx,chy);
 		test.setGridLocation(chy, chx);
 		test.render();
 		//getWorld().getEntityManager().getEntity(0).moveTo(
 			//	PavoHelper.convertWorldSpaceToGridLocation(PavoHelper.convertScreenToWorldSpace(getWorld(), me.getPoint())));
 		//System.out.println("f"+getWorld().getEntityManager().getEntity(0).getWidth());
 	}
 	BoxBlurFilter bbf = new BoxBlurFilter();
 	public void render() {
 		super.render();
 		if (airStrike >= 0 && airStrike < 40) {
 			airStrike += 1.4f;
 			int f = (int)airStrike;
 			if  (f > 12)
 				f = 12;
 			bbf.setHRadius(f);
 			bbf.setIterations(1);
 			bbf.filter(getBuffer(), getBuffer());
 			if (airStrike >= 40)
 				airStrike = -1;
 		}
 		Graphics2D g = PavoHelper.createGraphics(getBuffer());
 		g.drawImage(omnimap.getBuffer(), Game.Settings.currentWidth-158, 40, null);
 		g.drawImage(twwna.getBuffer(),twwna.getLocX(),twwna.getLocY(),null);
 		g.dispose();
 	}
 	
 	public HUD getHud(){
 		return hud;
 	}
 }
