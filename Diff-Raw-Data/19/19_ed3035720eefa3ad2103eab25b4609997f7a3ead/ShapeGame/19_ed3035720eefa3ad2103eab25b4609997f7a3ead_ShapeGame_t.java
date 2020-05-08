 package it.chalmers.tendu.gamemodel.shapesgame;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.GameResult;
 import it.chalmers.tendu.gamemodel.GameState;
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
 public class ShapeGame extends MiniGame {
 
 	public final String TAG = this.getClass().getName();
 
 	private int playerCount;
 	private int lockSize;
 
 	/**
 	 * Holds every persons latest received shape. Replaces the value when a new
 	 * shape has been received. <Integer(Receiver), <Integer(Sender),
 	 * Shape(Received Shape)>
 	 */
 	public Map<Integer, Map<Integer, Shape>> latestReceivedShapes; //TODO private
 
 	/**
 	 * Holds every persons last sent shape <Integer(Receiver), <Integer(Sender),
 	 * Shape(Received Shape)>
 	 */
 	private Map<Integer, List<Shape>> latestSentShapes;
 
 	/**
 	 * All shapes for all players mapped by player number (Integer).
 	 */
 	private Map<Integer, List<Shape>> allInventory;
 
 	/**
 	 * Every players lock, mapped by player number (Integer)
 	 */
 	private Map<Integer, Lock> allLocks;
 
 	/** No args constructor for reflection use */
 	protected ShapeGame() {
 		super();
 	};
 
 	/**
 	 * This will create a ShapesGame. It creates a list of all possible
 	 * combinations of the enums {@link GeometricShape} and link {@link ShapeColor}
 	 * and then reduces this randomly to a subset that suffice for the game
 	 * settings (player count and lock seqeuence length)
 	 */
 	public ShapeGame(long extraTime, Difficulty difficulty,
 			Map<String, Integer> players) {
 		super(difficulty, GameId.SHAPE_GAME, players);
 
 		switch (difficulty) {
 		case ONE:
			this.setGameTime(30000, extraTime);
			lockSize = 2;
 			break;
 		case TWO:
			this.setGameTime(25000, extraTime);
 			lockSize = 3;
 			break;
 		case THREE:
			this.setGameTime(20000, extraTime);
 			lockSize = 4;
 			break;
 		case FOUR:
			this.setGameTime(20000, extraTime);
 			lockSize = 5;
 			break;
 		case FIVE:
			this.setGameTime(12000, extraTime);
 			lockSize = 5;
 			break;
 		default:
 			break;
 		}
 
 		// Get list of all combinations of shapes and colors then shuffle
 		List<Shape> allShapes = Shape.getAllShapes();
 		Collections.shuffle(allShapes);
 
 		playerCount = players.size();
 
 		latestReceivedShapes = new HashMap<Integer, Map<Integer, Shape>>();
 		// Map initiation
 		for (int i = 0; i < playerCount; i++) {
 			latestReceivedShapes.put(i, new HashMap<Integer, Shape>());
 		}
 
 		latestSentShapes = new HashMap<Integer, List<Shape>>();
 		// Map initiation
 		for (int i = 0; i < playerCount; i++) {
 			latestSentShapes.put(i, new ArrayList<Shape>());
 		}
 
 		allInventory = new HashMap<Integer, List<Shape>>(playerCount);
 		allLocks = new HashMap<Integer, Lock>(playerCount);
 
 		// Every player only has an explicit number of slots to fill, so let's
 		// grab the need amount of shapes from our allShapes list
 		List<Shape> gameShapes = new ArrayList<Shape>(lockSize * playerCount);
 
 		for (int i = 0; i < lockSize * playerCount; i++) {
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
 
 			for (int i = 0; i < lockSize; i++) {
 				playerInventory.add(gameShapes.remove(0));
 				playerLock.addSlot(copyOfShapes.remove(0));
 			}
 
 		}
 		Gdx.app.log("This is", "Shapes Game!");
 
 	}
 	
 	public void startGame() {
 		super.startGame();
 	}
 
 	/**
 	 * Will move a referenced shape to the inventory of another player.
 	 * 
 	 * @param shape
 	 *            to move
 	 * @param recipiant
 	 *            that should receive the shape-
 	 * @return <code>-1</code> if that player already owned that shape.
 	 *         <code>sender</code> on successful move.
 	 */
 	public int move(Shape shape, int recipiant) {
 		int sender = getOwnerOf(shape);
 		if (sender == recipiant)
 			return -1;
 		else {
 			List<Shape> oldLocation = allInventory.get(sender);
 			List<Shape> newLocation = allInventory.get(recipiant);
 			if (!oldLocation.remove(shape)) // TODO: for debugging.
 				return -2;
 
 			// Added to the new owners inventory
 			newLocation.add(shape);
 
 			// Adds shape to the list of sent shapes
 			latestSentShapes.get(sender).add(shape);
 			Gdx.app.log(TAG, "" + latestSentShapes.get(super.getplayerNbr()));
 
 			// Added to new owners latestReceivedShape
 			Map<Integer, Shape> senderShapePack = new HashMap<Integer, Shape>();
 
 			Gdx.app.log(TAG, "Added to latestSentShapes: from " + sender + " " + shape);
 			senderShapePack.put(sender, shape);
 			latestReceivedShapes.put(recipiant, senderShapePack);
 			return sender;
 		}
 	}
 
 	public boolean shapeFitIntoLock(int player, Shape shape, Shape lockShape) {
 		return allLocks.get(player).fitsIntoSlot(shape, lockShape);
 	}
 
 	/**
 	 * @param player
 	 *            that is inserting the shape
 	 * @param shape
 <<<<<<< HEAD
 =======
 	 *            to be inserted into the players slot. >>>>>>> branch
 	 *            'Majormerge' of https://github.com/tortal/IT2-LP1-Tendu.git
 >>>>>>> refs/heads/Majormerge
 	 * @return <code>true</code> if shape and slot fitted.
 	 */
 	public boolean insertShapeIntoSlot(int player, Shape shape, Shape lockShape) {
 		Lock lock = this.allLocks.get(player);
 		if (lock.fillSlot(shape, lockShape)) {
 			Gdx.app.log(TAG, "" + this.checkGameState());
 			return true;
 		}
		//super.changeTime(-3000);
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
 
 		Shape myShape = new Shape(ShapeColor.GREEN, GeometricShape.CIRCLE);
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
 
 	@Override
 	public GameResult getGameResult() {
 		if (checkGameState() == GameState.WON
 				|| checkGameState() == GameState.LOST) {
 			long spentTime = (getGameTime() - getRemainingTime());
 			GameResult result = new GameResult(getGameId(), spentTime,
 					getRemainingTime(), checkGameState());
 			return result;
 		}
 
 		return null;
 
 	}
 
 	@Override
 	public GameState checkGameState() {
 		if(checkIfGameWon()) {
 			return GameState.WON;
 		}else if (timerIsDone()) {
 			return GameState.LOST;
 		}
 		return GameState.RUNNING;
 	}
 
 	public Map<Integer, Shape> getLatestReceivedShape(int player) {
 		return latestReceivedShapes.get(player);
 	}
 
 	public List<Shape> getLatestSentShapes(int player) {
 		return latestSentShapes.get(player);
 	}
 
 	/**
 	 * Checks all the players locks and returns true if all locks are filled and
 	 * the game is won.
 	 */
 	private boolean checkIfGameWon() {
 		for (int i = 0; i < allLocks.size(); i++) {
 			if (!(getLock(i).isAllSlotsFilled()))
 				return false;
 		}
 		return true;
 	}
 }
