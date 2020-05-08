 /**
  * 
  */
 package com.jpii.navalbattle.pavo.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.io.File;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import sun.reflect.Reflection;
 
 import com.jpii.navalbattle.game.entity.BattleShip;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.grid.Location;
 
 /**
  * @author maximusvladimir
  *
  */
 public class GridWindow extends GameWindow {
 	Location local;
 	double dconstraint = 900;
 	/**
 	 * 
 	 */
 	public GridWindow() {
 		super();
 		
 		setSize(200,200);
 		setGridLocation(0,0);
		setLoc(0,25);
 	}
 	public void setGridLocation(int r, int c) {
 		local = new Location(r,c);
 	}
 	public void setGridLocation(Location loc) {
 		local = loc;
 	}
 	public Location getGridLocation() {
 		return local;
 	}
 	public void setDistanceConstraint(double d) {
 		dconstraint = d;
 	}
 	public double getDistanceConstraint() {
 		return dconstraint;
 	}
 	public void render() {
 		super.render();
 		Game game = getWinMan().getGame();
 		if (game == null)
 			return;
 		Graphics2D g = PavoHelper.createGraphics(getBuffer());
 		g.setColor(Color.black);
 		g.drawString("Info:", 10, 40);
 		if (local == null)
 			return;
 		//System.out.println("local not null");
 		if (game.getWorld() == null)
 			return;
 		//System.out.println("world not null");
 		if (game.getWorld().getEntityManager() == null)
 			return;
 		if (Location.validate(local)) {
 		if (game.getWorld().getEntityManager().isTileFilledWithWater(local.getRow(),local.getCol())) {
 			g.drawString("Tile has water.", 10, 60);
 		}
 		else {
 			g.drawString("Tile doesn't have water.", 10, 60);
 		}
 		if (game.getWorld().getEntityManager().getTile(local) != null && game.getWorld().getEntityManager().getTile(local).getEntity() instanceof BattleShip) {
 			g.drawString("Tile is part of a battleship.", 10, 80);
 		}
 		g.drawString("Percent land " + game.getWorld().getEntityManager().getTilePercentLand(local.getRow(),local.getCol()) + "%", 10, 100);
 		g.drawString("Precent water " + (100 - game.getWorld().getEntityManager().getTilePercentLand(local.getRow(),local.getCol())) + "%", 10, 120);
 		}
 	}
 }
