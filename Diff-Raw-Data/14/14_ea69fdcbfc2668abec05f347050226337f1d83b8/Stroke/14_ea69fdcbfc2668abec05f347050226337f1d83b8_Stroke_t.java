 package ph.sm.boxgame;
 
 
 import java.util.LinkedList;
 
 import android.graphics.Point;
 import ch.aplu.android.Actor;
 import ch.aplu.android.GGVector;
import ch.aplu.android.L;
 import ch.aplu.android.Location;
 
 
 public class Stroke extends Actor {
 
 	private BoxGame gg;
 	private StrokeDirection direction;
 	private boolean drawn;
 	private static int drawnStrokes;
 	private static int strokeCounter = 0;
 	
 	/**
 	 * The sprite id corresponds to the player the stroke belongs to -1
 	 * @param gg
 	 * @param d
 	 */
 	public Stroke(BoxGame gg, StrokeDirection d) {
 		super(true, "strokeboarder", 3);
 		strokeCounter++;
 		this.gg = gg;
 		this.direction = d;
 		this.drawn = false;
 	}
 	
 	public void reset() {
 		this.turn(direction.ordinal()*90);
 		this.setLocationOffset(scaleOffset(direction.getOffset()));
		L.d("Cell size: " + gg.getCellSize());
		this.setActorTouchCircle(new Point(0,0), BoxGame.INIT_CELL_SIZE/3);
 	}
 
 	private Point scaleOffset(GGVector offset) {
 		int scaleFactor = gg.getCellSize()/2;
 		return new Point((int) (offset.x * scaleFactor), (int) (offset.y * scaleFactor));
 	}
 	
 	public LinkedList<Location> getPossibleFillLocations() {
 		LinkedList<Location> fillLocs = new LinkedList<Location>();
 		Location loc = getLocation();
 		fillLocs.add(loc);
 		if (loc.y != 1 && direction == StrokeDirection.HORIZONTAL) 
 			fillLocs.add(new Location(loc.x, loc.y - 1));
 		if (loc.x != 1 && direction == StrokeDirection.VERTICAL)
 			fillLocs.add(new Location(loc.x - 1, loc.y));
 		return fillLocs;
 	}
 	
 	public StrokeDirection getStrokeDirection() {
 		return direction;
 	}
 	
 	public void draw(int playerId) {
 		drawnStrokes++;
 		drawn = true;
 		show(1 + playerId);
 	}
 
 	public boolean isDrawn() {
 		return drawn;
 	}
 
 	public static boolean allDrawn() {
 		return strokeCounter == drawnStrokes;
 	}
 	
 }
