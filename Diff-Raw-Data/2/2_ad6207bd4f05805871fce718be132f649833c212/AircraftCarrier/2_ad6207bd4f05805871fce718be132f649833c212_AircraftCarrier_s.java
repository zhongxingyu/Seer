 package com.jpii.navalbattle.game.entity;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 
 public class AircraftCarrier extends MoveableEntity {
 	private static final long serialVersionUID = 1L;
 	public static GridedEntityTileOrientation AIRCRAFTCARRIER_ID;
 
 	public AircraftCarrier(EntityManager em) {
 		super(em);
 		imgLocation="drawable-game/aircraftcarrier/aircraftcarrier.png";
 	}
 
 	public AircraftCarrier(EntityManager em, Location loc,byte orientation,int team) {
 		super(em, loc, AIRCRAFTCARRIER_ID,orientation,team);
 		imgLocation="drawable-game/aircraftcarrier/aircraftcarrier.png";
 		Game g = em.getWorld().getGame();
 		if (!g.isAClient()) {
 			g.getSelfServer().send("aircraftcarrier:"+loc.getCol()+","+loc.getRow());
 		}
 		moved=0;
 		maxMovement = 5;
 		attackRange = 7;
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
 	
 	public void rotateTo(byte rotateto) {
 		boolean flag = GridHelper.canRotate(getManager(), this, rotateto, getELocation().getRow(), getELocation().getCol(), getWidth());
 		if (flag)
 			super.rotateTo(rotateto);
 	}
 	
 	/**
 	 * Gets the bow of the ship.
 	 * @return The location. Could be "Unknown", if the Entity is not in the Grid.
 	 */
 	public Location getELocation(){
 		Location temp = super.getLocation();
 		if(getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT){
 			return temp;
 		}
 		else if(getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM){
 			temp = new Location(temp.getRow()+(getWidth()-1),temp.getCol());
 			if(startpos)
 				temp = new Location(temp.getRow()+(getWidth()-1),temp.getCol());
 		}
 	
 		return temp;
 	}
 }
