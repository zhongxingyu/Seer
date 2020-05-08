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
 import java.awt.event.MouseWheelEvent;
 
 import com.jpii.navalbattle.game.entity.*;
 import com.jpii.navalbattle.game.gui.HUD;
 import com.jpii.navalbattle.game.gui.PauseWindow;
 import com.jpii.navalbattle.pavo.*;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.gui.MessageBox;
 import com.jpii.navalbattle.pavo.gui.MessageBoxIcon;
 import com.jpii.navalbattle.pavo.gui.OmniMap;
 import com.jpii.navalbattle.pavo.gui.TestWindowWithNewAPI;
 import com.jpii.navalbattle.renderer.weather.WeatherMode;
 
 /**
  * The game file.
  */
 public class NavalGame extends Game{
 	private static final long serialVersionUID = 1L;
 	/*PlayerProfileWindow ppw;
 	ShipInfoWindow siw;
 	StatusBar sb;*/
 	NavalManager nm;
 	HUD hud;
 	OmniMap omnimap;
 	
 	TestWindowWithNewAPI twwna;
 	PauseWindow pw;
 	
 	//GridWindow test;
 	
 	float airStrike = -1;
 	
 	public NavalGame() {
 		super();
 		twwna = new TestWindowWithNewAPI(getWindows());
 		twwna.setLoc(200,200);
 		twwna.repaint();
 		twwna.setVisible(false);
 		pw = new PauseWindow(getWindows());
 		nm = new NavalManager(getWorld());
 		hud = new HUD(getWindows(),nm.getTurnManager(),0,Settings.currentHeight-150,Settings.currentWidth, 150);
 		getWorld().setEntityManager(nm);
 		omnimap = new OmniMap(getWorld());
 //		ppw = new PlayerProfileWindow();
 //		sb = new StatusBar(this);
 		//test = new GridWindow();
 		//test.setGridLocation(10,10);
 //		ppw.setLoc(200,200);
 //		siw = new ShipInfoWindow();
 //		siw.setLoc(350,350);
 		getWorld().getWeather().setWeather(WeatherMode.Sunny);
 //		getWinMan().add(ppw);
 //		getWinMan().add(siw);
 //		getWinMan().add(sb);
 //		getWinMan().add(test);
 		getWindows().add(twwna);
 		getWindows().add(hud);
 		getWindows().add(pw);
 		//MessageBox.show("Warning", "This is a message box!!!");
 		MessageBox.show("Hey there!","Could not connect to RocketGamer servers.\n\nTrying again in 10 seconds.",
 				MessageBoxIcon.Notify, false);
 
 	}
 	public NavalGame(PavoOpenState pos, String flags) {
 		super(pos,flags);
 		nm = new NavalManager(getWorld());
 		hud = new HUD(getWindows(),nm.getTurnManager(),0,Settings.currentHeight-150,Settings.currentWidth, 150);
 		getWorld().setEntityManager(nm);
 		pw = new PauseWindow(getWindows());
 		omnimap = new OmniMap(getWorld());
 		twwna = new TestWindowWithNewAPI(getWindows());
 		twwna.setLoc(200,200);
 		twwna.repaint();
 		//ppw = new PlayerProfileWindow();
 		//sb = new StatusBar(this);
 		//test = new GridWindow();
 		//test.setGridLocation(10,10);
 		//ppw.setLoc(200,200);
 		//siw = new ShipInfoWindow();
 		//siw.setLoc(350,350);
 		getWorld().getWeather().setWeather(WeatherMode.Sunny);
 		getWindows().add(twwna);
 		getWindows().add(hud);
 		getWindows().add(pw);
 //		getWinMan().add(ppw);
 //		getWinMan().add(siw);
 //		getWinMan().add(sb);
 //		getWinMan().add(test);
 		//MessageBox.show("Warning", "This is a message box!!!");
 		MessageBox.show("Hey there!","Could not connect to RocketGamer servers.\n\nTrying again in 10 seconds.",
 				MessageBoxIcon.Notify, false);
 	}
 	int hw = 0, hh = 0;
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
 		
 		if (hw != Game.Settings.currentWidth || hh != Game.Settings.currentHeight) {
 			hw = Game.Settings.currentWidth;
 			hh = Game.Settings.currentHeight;
 			hud.setLocY(hh-150);
 			hud.repaint();
 		}
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
 		
 		if (guiUsedMouseDrag)
 			return;
 		/*if (me.getX() - twwna.getLocX() >= 0 && me.getX() - twwna.getLocX() < twwna.getWidth()
 				&& me.getY() - twwna.getLocY() >= 0 && me.getY() - twwna.getLocY() < twwna.getHeight())
 			twwna.onMouseDrag(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY());
 		if (me.getX() - hud.getLocX() >= 0 && me.getX() - hud.getLocX() < hud.getWidth()
 				&& me.getY() - hud.getLocY() >= 0 && me.getY() - hud.getLocY() < hud.getHeight())
 			hud.onMouseDrag(me.getX()-hud.getLocX(), me.getY()-hud.getLocY());
 		*/
 		//if (getWinMan().mouseDragged(me))
 			//return;
 		//if (getWindows().mouseDragged(me))
 			//return;
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
 		if ((getWorld().getScreenX() != fgax || getWorld().getScreenY() != fgaz) && !isAClient()) {
 			getSelfServer().send("bounds:"+fgax+","+fgaz);
 		}
 		if ((getWorld().getScreenX() != fgax || getWorld().getScreenY() != fgaz) && isAClient()) {
 			getSelfClient().send("bounds:"+fgax+","+fgaz);
 		}
 		getWorld().setLoc(fgax, fgaz);
 		//forceUpdate(); // SEE WARNING IN DESCRIPTION!!! THIS METHOD IS NOT ACTUALLY DECREPATED!!!
 	}
 	public void mouseWheelChange(MouseWheelEvent mwe) {
 		super.mouseWheelChange(mwe);
 		
		getWorld().setLoc(getWorld().getScreenX(),getWorld().getScreenY()+(-mwe.getWheelRotation() * 50));
 	}
 	public OmniMap getMap() {
 		return omnimap;
 	}
 	/**
 	 * Button1 == Left
 	 * Button2 == Middle?
 	 * Button3 == Right?
 	 */
 	public void mouseDown(MouseEvent me) {
 		super.mouseDown(me);
 		
 		if (guiUsedMouseDown)
 			return;
 		
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		Tile current = nm.getTile(chy,chx);
 		
 		twwna.updateLocation(getWorld().getEntityManager(), chx, chy);
 		
 		if ((omnimap.mouseDown(me)))
 			return;
 		
 		else if(hud.moveShip(chx,chy)){
 			
 		}
 		
 		else if(!isAClient() && me.getButton() != MouseEvent.BUTTON1){
 			getHud().setEntity(null);
 		}
 		
 //		else if (Game.Settings.isFinishedGenerating && getWorld().getEntityManager().getTilePercentLand(chy,chx) <= 5){
 //			Entity ent = getWorld().getEntityManager().findEntity(chy,chx);
 //			if(current==null){
 //				if(!isAClient() && me.getButton() == MouseEvent.BUTTON1){
 //					if(Game.Settings.isFinishedGenerating){
 //						if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, chy, chx, 4)){
 //							nm.getTurnManager().addEntity(new BattleShip(this.getWorld().getEntityManager(),new Location(chy,chx),BattleShip.BATTLESHIP_ID,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT,Game.Settings.rand.nextInt(0,3)));
 //						}
 //						else if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, chy, chx, 4)){
 //							nm.getTurnManager().addEntity(new BattleShip(this.getWorld().getEntityManager(),new Location(chy,chx),BattleShip.BATTLESHIP_ID,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM,Game.Settings.rand.nextInt(0,3)));
 //						}
 //					}
 //				}
 //				if(!isAClient() && me.getButton() == MouseEvent.BUTTON3){
 //					if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, chy, chx, 2)){
 //						new Submarine(this.getWorld().getEntityManager(),new Location(chy,chx),Submarine.SUBMARINE_ID,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT,Game.Settings.rand.nextInt(0,3));
 //					}
 //					else if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, chy, chx, 2)){
 //						new Submarine(this.getWorld().getEntityManager(),new Location(chy-1,chx),Submarine.SUBMARINE_ID,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM,Game.Settings.rand.nextInt(0,3));
 //					}	
 //				}
 //			}
 //		}
 		
 		//airStrike = 0;
 	}
 	public void mouseUp(MouseEvent me) {
 		super.mouseUp(me);
 		
 		if (guiUsedMouseUp)
 			return;
 		/*if (me.getX() - twwna.getLocX() >= 0 && me.getX() - twwna.getLocX() < twwna.getWidth()
 				&& me.getY() - twwna.getLocY() >= 0 && me.getY() - twwna.getLocY() < twwna.getHeight())
 			twwna.onMouseUp(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY(),me.getButton());
 		if (me.getX() - hud.getLocX() >= 0 && me.getX() - hud.getLocX() < hud.getWidth()
 				&& me.getY() - hud.getLocY() >= 0 && me.getY() - hud.getLocY() < hud.getHeight())
 			hud.onMouseUp(me.getX()-hud.getLocX(), me.getY()-hud.getLocY(),me.getButton());
 		*/
 	}
 	public void mouseMove(MouseEvent me) {
 		super.mouseMove(me);
 		
 		//if (guiUsedMouse)
 		//if (getWindows().mouseMove(me)) {
 		//	return;
 		//}
 		/*if (me.getX() - twwna.getLocX() >= 0 && me.getX() - twwna.getLocX() < twwna.getWidth()
 				&& me.getY() - twwna.getLocY() >= 0 && me.getY() - twwna.getLocY() < twwna.getHeight())
 			twwna.onMouseHover(me.getX()-twwna.getLocX(), me.getY()-twwna.getLocY());
 		if (me.getX() - hud.getLocX() >= 0 && me.getX() - hud.getLocX() < hud.getWidth()
 				&& me.getY() - hud.getLocY() >= 0 && me.getY() - hud.getLocY() < hud.getHeight())
 			hud.onMouseHover(me.getX()-hud.getLocX(), me.getY()-hud.getLocY());*/
 		omnimap.mouseMoved(me);
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		//sb.setMouseTileLocation(chx,chy);
 		//test.setGridLocation(chy, chx);
 		//test.render();
 		//getWorld().getEntityManager().getEntity(0).moveTo(
 			//	PavoHelper.convertWorldSpaceToGridLocation(PavoHelper.convertScreenToWorldSpace(getWorld(), me.getPoint())));
 		//System.out.println("f"+getWorld().getEntityManager().getEntity(0).getWidth());
 		if(hud.isShowingMove()){
 			hud.showMoveShip(chx,chy);
 		}
 	}
 	BoxBlurFilter bbf = new BoxBlurFilter();
 	public void render() {
 		super.render();
 		//if (getWorld().getTimeManager().getCurrentMinutes() == 0)
 			//sb.render();
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
 		//g.drawImage(twwna.getBuffer(),twwna.getLocX(),twwna.getLocY(),null);
 		//g.drawImage(hud.getBuffer(),hud.getLocX(),hud.getLocY(),null);
 		g.dispose();
 	}
 	
 	public HUD getHud(){
 		return hud;
 	}
 }
