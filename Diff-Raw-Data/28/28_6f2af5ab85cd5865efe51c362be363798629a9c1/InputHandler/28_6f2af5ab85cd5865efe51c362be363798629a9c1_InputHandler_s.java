 package game.airportcontrol.gamesettings;
 
 import game.airportcontrol.moveables.AircraftBase;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Input;
 
 public class InputHandler {
 
 	public static void inputHandler(Input input) {
 		if (input.isMouseButtonDown(0)) {
 			recordTrack(input.getAbsoluteMouseX(), input.getAbsoluteMouseY());
 		}
 		else {
 			saveTrack();
 		}
 	}
 
 	public static void saveTrack() {
 		if (Game.path.size() > 0) {
 			if (Game.pathTo != null)
 				Game.pathTo.setWayPoints(Game.path);
 			Game.path = new ArrayList<Point>();
 		}
 	}
 
 	public static void recordTrack(int x, int y) {
 		if (Game.path.isEmpty()) {
 			for (AircraftBase a : Game.airplanes) {
 				if (a.getPosition().distance(new Point(x, y)) < 20) {
 					Game.pathTo = a;
 				}
 			}
 		}
		if(Game.path.size()>0) {
		if (Game.path.get(Game.path.size()-1).distance(new Point(x,y)) >= Game.pathTo.getRequiredDistanceToWaypoint()/2)
			Game.path.add(new Point(x, y));
 		}
		else
			Game.path.add(new Point(x,y));
 		Game.c++;
 		Game.c = Game.c % 5;
 	}
 }
