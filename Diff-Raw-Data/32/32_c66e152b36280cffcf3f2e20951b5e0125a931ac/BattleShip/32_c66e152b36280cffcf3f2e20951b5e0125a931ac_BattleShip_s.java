 /**
  * 
  */
 package com.jpii.navalbattle.game.entity;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 
 /**
  * @author maximusvladimir
  *
  */
 public class BattleShip extends Entity {
 	public static GridedEntityTileOrientation BATTLESHIP_ID;
 	/**
 	 * @param em
 	 */
 	public BattleShip(EntityManager em) {
 		super(em);
 	}
 
 	/**
 	 * @param em
 	 * @param loc
 	 * @param superId
 	 */
 	public BattleShip(EntityManager em, Location loc, GridedEntityTileOrientation superId,int team) {
 		super(em, loc, superId,team);
 	}
 	
 	public void init() {
 		setWidth(4);
 		setHeight(1);
 	}
 	
 	public void onUpdate(long timePassed) {
 		int r = 1;
 		int c = 1;
 		//if(Game.Settings.isFinishedGenerating && Game.Settings.rand.nextInt(0,20) == 2)
 			//moveTo(r+getLocation().getRow(),c+getLocation().getCol());
 	}
 	public void onMouseMove(int x, int y) {
 		System.out.println("The battleship was hovered over. (" + x + "," + y + ")");
 	}
 	public void onMouseDown(int x, int y, boolean leftbutton) {
 		System.out.println("The battleship was clicked on. Left mouse button? " + leftbutton + " (" + x + "," + y + ")");
 	}
 }
