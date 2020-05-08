 package it.chalmers.tendu.gamemodel.shapesgame;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.MiniGame;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 
 /**
  * Puzzle game
  * 
  * Every player has n slots (default is 4) on their screen that are initially
  * empty and are to be replaced by shapes. One uses objects (Shape class object)
  * to fill ones' individual slot puzzle. Moreover, every player has an inventory
  * of shapes, the union of all players' inventory contain all shapes needed to
  * fully fill the missing slots.
  * 
  * When beginning the game, you might have to trade shapes without fellow team
  * players until you will be able to solve YOUR puzzle.
  * 
  */
 public class ShapesGame extends MiniGame {
 	
 	public final String TAG = this.getClass().getName();
 
 	private int playerCount;
 	private final static int LOCK_SIZE = 4;
 
 	/**
 	 * All shapes for all players mapped by player number (Integer).
 	 */
 	private Map<Integer, List<Shape>> allInventory;
 
 	/**
 	 * Every players lock, mapped by player number (Integer)
 	 */
 	private Map<Integer, Lock> allLocks;
 
 	/** No args constructor for reflection use */
 	protected ShapesGame() {
 		super();
 	};
 
 	/**
 	 * This will create a ShapesGame. It creates a list of all possible
 	 * combinations of the enums {@link GeometricShape} and link {@link Color}
 	 * and then reduces this randomly to a subset that suffice for the game
 	 * settings (player count and lock seqeuence length)
 	 */
 	public ShapesGame(int addTime, Difficulty difficulty,
 			Map<String, Integer> players) {
 		super(addTime, difficulty, GameId.SHAPES_GAME, players);
 
 		// Get list of all combinations of shapes and colors then shuffle
 		List<Shape> allShapes = Shape.getAllShapes();
 		Collections.shuffle(allShapes);
 
 		playerCount = players.size();
 
 		allInventory = new HashMap<Integer, List<Shape>>(playerCount);
 		allLocks = new HashMap<Integer, Lock>(playerCount);
 
 		// Every player only has an explicit number of slots to fill, so let's
 		// grab the need amount of shapes from our allShapes list
 		List<Shape> gameShapes = new ArrayList<Shape>(LOCK_SIZE * playerCount);
 
 		for (int i = 0; i < LOCK_SIZE * playerCount; i++) {
 			Shape randomShape = allShapes.remove(0);
 			gameShapes.add(randomShape);
 		}
 
 		// Clone gameShapes list, and shuffle the copy. We will use this copy to
 		// randomly distribute the slots.
 		List<Shape> copyOfShapes = new ArrayList<Shape>(gameShapes.size());
 		copyOfShapes.addAll(gameShapes);
 		Collections.shuffle(copyOfShapes);
 
 		// Create inventory and slots lists for all players.
 		for (int p = 0; p < playerCount; p++) {
 			List<Shape> playerInventory = new ArrayList<Shape>();
 			Lock playerLock = new Lock();
 
 			allInventory.put(p, playerInventory);
 			allLocks.put(p, playerLock);
 
 			for (int i = 0; i < LOCK_SIZE; i++) {
 				playerInventory.add(gameShapes.remove(0));
 				playerLock.addSlot(copyOfShapes.remove(0));
 			}
 
 		}
 
 		Gdx.app.log("This is", "Shapes Game!");
 
 	}
 
 	/**
 	 * Will move a referenced shape to the inventory of another player.
 	 * 
 	 * @param shape
 	 *            to move
 	 * @param player
 	 *            that should receive the shape-
 	 * @return <code>-1</code> if that player already owned that shape.
 	 *         <code>0</code> on successful move.
 	 */
 	public int move(Shape shape, int player) {
 		int owner = getOwnerOf(shape);
 		if (owner == player)
 			return -1;
 		else {
 			List<Shape> oldLocation = allInventory.get(owner);
 			List<Shape> newLocation = allInventory.get(player);
 			if (!oldLocation.remove(shape)) // TODO: for debugging.
 				return -2;
 
 			newLocation.add(shape);
 			return 0;
 		}
 	}
 
 	public boolean shapeFitIntoLock(int player, Shape shape, Shape lockShape) {
 		return allLocks.get(player).fitsIntoSlot(shape, lockShape);
 	}
 
 	/**
 	 * @param player
 	 *            that is inserting the shape
 	 * @param shape
 	 *            <<<<<<< HEAD to be inserted into the players ���.
 	 *            ======= to be inserted into the players slot. >>>>>>>
 	 *            refs/heads/ShapesGameGraphics
 	 * @return <code>true</code> if shape and slot fitted.
 	 */
 	public boolean insertShapeIntoSlot(int player, Shape shape, Shape lockShape) {
 		Gdx.app.log(TAG, "In model");
 		Lock lock = this.allLocks.get(player);
 		if (lock.fillSlot(shape, lockShape))
 			return true;
 
 		super.changeTimeWith(-3000);
		
 		return false;
 	}
 
 	/**
 	 * @param shape
 	 *            to search for
 	 * @return the player that owns the searched shape. <code>-1</code> if the
 	 *         shape was not found.
 	 */
 	public int getOwnerOf(Shape shape) {
 		int owner = -1;
 		for (Map.Entry<Integer, List<Shape>> e : allInventory.entrySet()) {
 			for (Shape s : e.getValue()) {
 				if (shape.equals(s)) {
 					owner = e.getKey();
 					return owner;
 				}
 			}
 		}
 		return owner;
 	}
 
 	/**
 	 * @param lock
 	 *            to search owner of
 	 * @return the owner of the lock.
 	 */
 	public int getOwnerOf(Lock lock) {
 		int owner = -1;
 		for (Map.Entry<Integer, Lock> e : allLocks.entrySet()) {
 			if (lock.equals(e.getValue())) {
 				owner = e.getKey();
 			}
 		}
 		return owner;
 	}
 
 	/**
 	 * @param player
 	 *            who is the owner of the lock.
 	 * @return the lock a particular player has. null if there is no such
 	 *         player.
 	 */
 	public Lock getLock(int player) {
 		Lock lock = allLocks.get(player);
 		return lock;
 	}
 
 	/**
 	 * DEBUG METHOD
 	 */
 	void debug_printMembers() {
 		System.out.println("\t ALL INVENTORY \t");
 		for (Map.Entry<Integer, List<Shape>> e : allInventory.entrySet()) {
 			System.out.println("Player" + e.getKey());
 			for (Shape s : e.getValue()) {
 				System.out.println("\t" + s);
 			}
 		}
 		System.out.println("SLOTS TO FILL");
 		for (Map.Entry<Integer, Lock> e : allLocks.entrySet()) {
 			System.out.println("Player" + e.getKey());
 
 			for (Shape s : e.getValue().getLockSequence()) {
 				System.out.println("\t" + s);
 			}
 
 			for (Boolean b : e.getValue().getLockState().values()) {
 				System.out.println("\t " + b);
 			}
 
 		}
 
 	}
 
 	/**
 	 * TODO: DEBUG MAIN and testing.
 	 */
 	public static void main(String[] a) {
 		Lock lock = new Lock();
 
 		List<Shape> allShapes = Shape.getAllShapes();
 		lock.addSlot(allShapes.remove(0));
 		lock.addSlot(allShapes.remove(0));
 
 		Shape myShape = new Shape(Color.GREEN, GeometricShape.CIRCLE);
 		// System.out.println(lock.fillSlot(myShape));
 
 		System.out.println(lock);
 	}
 
 	/**
 	 * Gets all the players shapes.
 	 * 
 	 * @return All the players
 	 */
 	public Map<Integer, List<Shape>> getAllInventory() {
 		return allInventory;
 	}
 
 }
