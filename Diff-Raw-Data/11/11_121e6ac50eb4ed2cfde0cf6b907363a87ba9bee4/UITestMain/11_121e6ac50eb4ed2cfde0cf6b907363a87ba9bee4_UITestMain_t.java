 package ui.isometric.mock;
 
 import ui.isometric.IsoInterface;
 import util.*;
 import client.model.*;
 
 public class UITestMain {
 	private static game.GameModel sgm = new game.GameModel();
 
 	public static void main(String[] args) {
 		game.GameThing tile = new game.things.GroundTile(sgm);
 		sgm.level(0).put(new Position(5, 0), tile);
 		sgm.level(0).put(new Position(5, 1), new game.things.GroundTile(sgm, "ground_grey_water_two_sides"));
 		//sgm.level(0).put(new Position(5, 2), new game.things.Wall(sgm, "wallx"));
//		Position pos51 = new Position(5,1);
 //		sgm.level(0).put(pos51, new game.things.Player(sgm, pos51, Direction.SOUTH, "character_cordi_empty"));
 		IsoInterface view = new IsoInterface("IsoTest", Conversions.fromServerGameModel(sgm), new IsoGameLogicMock());
 		view.show();
 	}
 }
