 /**
  * 
  */
 package com.jpii.navalbattle.game;
 
 import com.jpii.navalbattle.game.entity.AircraftCarrier;
 import com.jpii.navalbattle.game.entity.BattleShip;
 import com.jpii.navalbattle.game.entity.Submarine;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.World;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.util.FileUtils;
 
 /**
  * The entity manager specified for NavalThing.
  */
 public class NavalManager extends EntityManager {
 	private static final long serialVersionUID = 1L;
 	public static GridedEntityTileOrientation w1, w2, w3;
 	TurnManager tm;
 	
 	/**
 	 * Creates a new instance of the NavalManager.
 	 * @param w The world to create it from.
 	 */
 	public NavalManager(World w) {
 		super(w);
 		tm = new TurnManager(new PlayerManager(new Player()));
 		battleShipId = new GridedEntityTileOrientation();
 		battleShipId.setLeftToRightImage(registerEntity(PavoHelper.imgUtilOutline(
 				FileUtils.getImage("drawable-game/battleship/battleship.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT));
 		battleShipId.setTopToBottomImage(registerEntity(PavoHelper.imgUtilOutline(
 				FileUtils.getImage("drawable-game/battleship/battleship_S.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM));
 		
 		acarrierId = new GridedEntityTileOrientation();
 		acarrierId.setLeftToRightImage(registerEntity(PavoHelper.imgUtilOutline(
 				FileUtils.getImage("drawable-game/aircraftcarrier/aircraftcarrier.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT));
 		acarrierId.setTopToBottomImage(registerEntity(PavoHelper.imgUtilOutline(
 				FileUtils.getImage("drawable-game/aircraftcarrier/aircraftcarrier_S.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM));
 		
 		submarineId = new GridedEntityTileOrientation();
 		submarineId.setLeftToRightImage(registerEntity(PavoHelper.imgUtilOutline(
 				FileUtils.getImage("drawable-game/submarine/submarine.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT));
 		submarineId.setTopToBottomImage(registerEntity(PavoHelper.imgUtilOutline(
 				FileUtils.getImage("drawable-game/submarine/submarine_S.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM));
 		
 		if (battleShipId != null) {
 			BattleShip.BATTLESHIP_ID = battleShipId;
 		}
 		if (acarrierId != null) {
 			AircraftCarrier.AIRCRAFTCARRIER_ID = acarrierId;
 		}
 		if (submarineId != null) {
 			Submarine.SUBMARINE_ID = submarineId;
 		}
 		else {
 			System.out.println("not a battleship");
 		}
 		gh = new GridHelper(this);
 	}
 	GridHelper gh;
 	
 	public void gameDoneGenerating() {
 		int w1_ = registerEntity(PavoHelper.imgUtilOutline(FileUtils.getImage("drawable-game/other/whaleleft.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT);
 		int w2_ = registerEntity(PavoHelper.imgUtilOutline(FileUtils.getImage("drawable-game/other/whalecenter.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT);
 		int w3_ = registerEntity(PavoHelper.imgUtilOutline(FileUtils.getImage("drawable-game/other/whaleright.png"),Game.Settings.GridColor),GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT);
 		w1 = new GridedEntityTileOrientation();
 		w1.setLeftToRightImage(w1_);
 		w1.setTopToBottomImage(w1_);
 		w2 = new GridedEntityTileOrientation();
 		w1.setLeftToRightImage(w2_);
 		w1.setTopToBottomImage(w2_);
 		w3 = new GridedEntityTileOrientation();
 		w1.setLeftToRightImage(w3_);
 		w1.setTopToBottomImage(w3_);
 		
		for (int c = 0; c < 10; c++) {
 			Location poll = gh.pollNextWaterTile();
 			while((!(GridHelper.canPlaceInGrid(this,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, poll.getRow(), poll.getCol(), 4)) && !(GridHelper.canPlaceInGrid(this,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, poll.getRow(), poll.getCol(), 4)))){
 				poll = gh.pollNextWaterTile(25);
 			}
 			if(GridHelper.canPlaceInGrid(this,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, poll.getRow(), poll.getCol(), 4))
 				tm.addEntity(new BattleShip(this, poll, BattleShip.BATTLESHIP_ID,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT,Game.Settings.rand.nextInt(0,3)));
 			else if(GridHelper.canPlaceInGrid(this,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, poll.getRow(), poll.getCol(), 4))
 				tm.addEntity(new BattleShip(this, poll, BattleShip.BATTLESHIP_ID,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM,Game.Settings.rand.nextInt(0,3)));
 			else
 				c--;
 	}
 		System.out.println("Let me play you the song of my people.");
 	}
 	
 	public void update(long ticksPassed) {
 		for (int c = 0; c < this.getTotalEntities(); c++) {
 			Entity e = getEntity(c);
 			if (e != null){ //&& e instanceof BattleShip) {
 				e.onUpdate(ticksPassed);
 			}
 		}
 	}
 }
