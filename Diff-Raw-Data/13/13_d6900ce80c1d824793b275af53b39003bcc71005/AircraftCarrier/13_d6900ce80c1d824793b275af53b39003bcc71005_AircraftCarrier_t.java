 package com.jpii.navalbattle.game.entity;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 
 public class AircraftCarrier extends MoveableEntity {
 	private static final long serialVersionUID = 1L;
 	public static GridedEntityTileOrientation AIRCRAFTCARRIER_ID;
 
 	public AircraftCarrier(EntityManager em, Location loc,byte orientation) {
 		super(em, loc, AIRCRAFTCARRIER_ID,orientation);
 		imgLocation="drawable-game/aircraftcarrier/aircraftcarrier.png";
 		Game g = em.getWorld().getGame();
 		if (!g.isAClient()) {
 			g.getSelfServer().send("aircraftcarrier:"+loc.getCol()+","+loc.getRow());
 		}
 		handle=21;
 		moved=0;
 		maxMovement = 3;
 		maxHealth = 2000;
 		currentHealth = maxHealth;
 		primaryRange = 5;
 		secondaryRange = 9;
 		gunsAttackOption = true;
 		planeAttackOption = true;
 	}
 	
 	public void init() {
 		setWidth(5);
 	}
 	
 	public boolean moveTo(Location loc, boolean override) {
 		return super.moveTo(loc, override);
 	}
 	
 	public void onUpdate(long timePassed) {
 	}
 	
 	public void onMouseMove(int x, int y) {
 	}
 	
 	public void onMouseDown(int x, int y, boolean leftbutton) {
 		super.onMouseDown(x, y, leftbutton);
 	}
 	
 	public boolean getUsedMissiles(){
 		missileCount=1;
 		return super.getUsedMissiles();
 	}
	
	public void useSecondary(){
		usedMissiles=true;
	}
 }
