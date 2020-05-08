 package com.jpii.navalbattle.game.entity;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 
 public class BattleShip extends MoveableEntity {
 	private static final long serialVersionUID = 1L;
 	static{
 		maxMovement=4;
 		moved=0;
 	}
 	public static GridedEntityTileOrientation BATTLESHIP_ID;
 	/**
 	 * @param em
 	 */
 	public BattleShip(EntityManager em) {
 		super(em);
 		imgLocation="drawable-game/battleship/battleship.png";
 	}
 
 	/**
 	 * @param em
 	 * @param loc
 	 * @param superId
 	 */
 	public BattleShip(EntityManager em, Location loc, GridedEntityTileOrientation superId,byte orientation,int team) {
 		super(em, loc, superId,orientation,team);
 		imgLocation="drawable-game/battleship/battleship.png";
 		Game g = em.getWorld().getGame();
 		if (!g.isAClient()) {
 			g.getSelfServer().send("battleship:"+loc.getCol()+","+loc.getRow());
 		}
 	}
 	
 	public void init() {
 		setWidth(4);
 		setHeight(1);
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
 		if(!leftbutton){
 			byte t = getCurrentOrientation();
 			if (t == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT)
 				rotateTo(GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM);
 			else
 				rotateTo(GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT);
 		}
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
 //			getManager().setTileOverlay(temp.getRow(),temp.getCol(),(short)0x2f1d);
 //			getManager().setTileOverlay(temp.getRow(),temp.getCol(),(short)0x001);
 		}
 	
 		return temp;
 	}
 }
