 package ph.sm.boxgame;
 
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 import android.graphics.Color;
 import android.graphics.Point;
 import ch.aplu.android.Actor;
 import ch.aplu.android.GGActorTouchListener;
 import ch.aplu.android.GGStatusBar;
 import ch.aplu.android.GGTouch;
 import ch.aplu.android.GameGrid;
 import ch.aplu.android.Location;
 
 
 
 public class BoxGame extends GameGrid implements GGActorTouchListener{
 
 
 	/**
 	 * If set to true, an arbitrary grid size can be chosen at the start of the game.
 	 */
 	private static final boolean customizableGrid = false;
 	/**
 	 * For every location of the valid grid, the strokes surrounding it are saved in this hashtable.
 	 * It is then used for look up after a new stroke is drawn.
 	 */
 	Hashtable<Location, LinkedList<Stroke>> BoxMap = new Hashtable<Location, LinkedList<Stroke>>();
 	private Player currentPlayer;
 	private Player[] players = new Player[2];
 	private GGStatusBar status;
 	private static int playerCounter = 0;
	public static final int INIT_CELL_SIZE = 75;
 	
 	public BoxGame() {
		super(3 + 2, 3 + 2, cellZoom(INIT_CELL_SIZE), Color.WHITE);
 		status = addStatusBar(30);
 	}
 		
 	public void main() {		
 		players[0] = new Player(Color.BLUE, "Blue");
 		players[1] = new Player(Color.RED, "Red");
 		currentPlayer = players[0]; //blue begins;
 		getBg().clear(Color.WHITE);
 		refresh(); //so user doesn't sit in front of black screen
 		for (int x = 1; x < getNbHorzCells(); x++) {
 			for (int y = 1; y < getNbVertCells(); y++) {
 				Location loc = new Location(x, y);
 				BoxMap.put(loc, new LinkedList<Stroke>());
 				for (StrokeDirection d: StrokeDirection.values()) {
 					//prevent loop from drawing unnecessary strokes
 					if (y == getNbVertCells() - 1 && d == StrokeDirection.VERTICAL
 							|| x == getNbHorzCells() - 1 && d == StrokeDirection.HORIZONTAL)
 						continue;
 					Stroke s = new Stroke(this, d);
 					addActorNoRefresh(s, new Location(x,y));
 					s.addActorTouchListener(this, GGTouch.click);
 					for (Location l: s.getPossibleFillLocations())
 						BoxMap.get(l).add(s);
 				}
 			}
 		}
 		status.setText("Click on an edge to start");
 		setTitle("The box game -- www.java-online.ch"); 
 		refresh();
 	}
 	
 	
 	@Override
 	public void actorTouched(Actor actor, GGTouch mouse, Point spot) {
 		Stroke s = (Stroke) actor;
 		if (s.isDrawn()) 
 			return;
 		switch (mouse.getEvent()) {
 			case GGTouch.click:
 				s.draw(currentPlayer.id);
 				boolean nextPlayer = true;
 				for (Location loc: s.getPossibleFillLocations()) {
 					if (players[currentPlayer.id].tryToFillBoxes(loc))
 						nextPlayer = false;
 				}
 				if (nextPlayer)
 					currentPlayer = currentPlayer.nextPlayer();
 				updateStatusText();
 				break;
 		}
 		refresh();
 	}
 
 	private void updateStatusText() {
 		String msg = players[0].getLabelledScore() + " vs " + players[1].getLabelledScore();
 		if (Stroke.allDrawn())
 			msg = "Final Score -- " + msg;
 		else msg = msg + ", current Player is " + currentPlayer;
 		status.setText(msg);
 	}
 
 	private boolean outOfValidGrid(Location loc) {
 		return loc.y >= getNbVertCells() - 1 || loc.x >= getNbHorzCells() -1 
 				|| loc.y < 1 || loc.x < 1;
 	}
 	
 	class Player {
 		private int id;
 		private int color;
 		private int score;
 		private String name;
 		
 		public Player(int blue, String name) {
 			this.name = name;
 			this.id = playerCounter++;
 			this.color = blue;
 			this.score = 0;
 		}
 		
 		public String toString() {
 			return name;
 		}
 		
 		public Player nextPlayer() {
 			return players[(id + 1) % playerCounter];
 		}
 		public String getLabelledScore() {
 			return name + ": " + score;
 		}
 		
 		/**
 		 * Player tries to fill out the given location with own color, but first checks if it's surrounded
 		 * by strokes. 
 		 * @param loc
 		 * @return true if the given location was filled
 		 */
 		private boolean tryToFillBoxes(Location loc) {
 			if (outOfValidGrid(loc))
 				return false;
 			for (Stroke s: BoxMap.get(loc))
 				if (!s.isDrawn())
 					return false;
 			getPanel().fillCell(loc, color);
 			score++;
 			return true;
 		}
 	}
 }
