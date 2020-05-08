 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 //import java.util.Timer;
 
 /**
  * Class for the board of the game.
  * 
  * Adapted from zetcode.com
  * 
  * @author Rodney Earl
  * @version 1.0
  */
 public class Board extends JPanel implements Commons {
 
 	/**
 	 * Player object.
 	 */
 	private Player player;
 	/**
 	 * Boolean to check of the game is finished or not.
 	 */
 	private boolean finished = false;
 	/**
 	 * Arraylist containing the trees of the game.
 	 */
 	private ArrayList<Tree> trees = new ArrayList<Tree>();
 	/**
 	 * Arraylist containing the ships of the game.
 	 */
 	private ArrayList<Ship> ships = new ArrayList<Ship>();
 	/**
 	 * Arraylist containing the water blocks in the game.
 	 */
 	private ArrayList<Water> water = new ArrayList<Water>();
 	/**
 	 * Arraylist containing the chests of the game.
 	 */
 	private ArrayList<Treasure> chests = new ArrayList<Treasure>();
 	/**
 	 * Arraylist containing the goal zones of the game.
 	 */
 	private ArrayList<Goal> goals = new ArrayList<Goal>();
 
 	private SimpleDateFormat sdf;
 
 	private Timer timer;
 
 	private long timeRemaining;
 
 	private boolean outOfTime = false;
 
 	private int currentScore;
 
 	long start;
 
 	long current;
 
 	long end;
 
 	long previous;
 
 	/**
 	 * Constructor for the board.
 	 */
 	public Board() {
 		addKeyListener(new MyKeyAdapter());
 		setFocusable(true);
 		sdf = new SimpleDateFormat("mm : ss");
 		initWorld(levelOne);
 	}
 
 	/**
 	 * Initializer for a level.
 	 * 
 	 * @param level
 	 *            String containing the level layout.
 	 */
 	public void initWorld(String level) {
 
 		int x = OFFSET;
 		int y = OFFSET;
 
 		Tree tree;
 		Ship ship;
 		Water waterBlock;
 		Treasure chest;
 		Goal goal;
 
 		// Parse the level string and initialize objects where they need to go.
 		for (int pos = 0; pos < level.length(); pos++) {
 
 			char item = level.charAt(pos);
 
 			// New line string, reset x position and move down.
 			if (item == '\n') {
 				y += SPRITE_WIDTH;
 				x = OFFSET;
 			}
 			// T specifies a tree at this position.
 			else if (item == 'T') {
 				tree = new Tree(x, y);
 				trees.add(tree);
 				x += SPRITE_WIDTH;
 			}
 			// W specifies general water at this position.
 			else if (item == 'W') {
 				waterBlock = new Water(x, y);
 				water.add(waterBlock);
 				x += SPRITE_WIDTH;
 			}
 			// S specifies a ship at this position.
 			else if (item == 'S') {
 				ship = new Ship(x, y);
 				ships.add(ship);
 				x += SPRITE_WIDTH;
 			}
 			// $ specifies treasure at this position.
 			else if (item == '$') {
 				chest = new Treasure(x, y);
 				chests.add(chest);
 				x += SPRITE_WIDTH;
 			}
 			// . specifies a goal spot at this position.
 			else if (item == '.') {
 				goal = new Goal(x, y);
 				goals.add(goal);
 				x += SPRITE_WIDTH;
 			}
 			// @ specifies player starting position.
 			else if (item == '@') {
 				player = new Player(x, y);
 				x += SPRITE_WIDTH;
 			}
 			// A blank means nothing is here.
 			else if (item == ' ') {
 				x += SPRITE_WIDTH;
 			}
 		}
 
 		// Initialize the score for the level
 		currentScore = 0;
 		// Initialize and start the timer. It will be called every one second.
 		timeRemaining = GAME_TIME;
 		timer = new Timer(1000, new CDT());
 		timer.start();
 	}
 
 	/**
 	 * Paint method for java guis.
 	 * 
 	 * @param g
 	 *            Graphic object being painted.
 	 */
 	public void paint(Graphics g) {
 		super.paint(g);
 		// Set background colour.
 		g.setColor(new Color(250, 240, 170));
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 
 		// Add all the objects to an array to loop over.
 		ArrayList<Sprite> world = new ArrayList<Sprite>();
 		world.addAll(trees);
 		world.addAll(ships);
 		world.addAll(water);
 		world.addAll(goals);
 		world.addAll(chests);
 		world.add(player);
 
 		// Loop over all objects in this world.
 		for (int i = 0; i < world.size(); i++) {
 
 			Sprite item = world.get(i);
 
 			if ((item instanceof Player) || (item instanceof Treasure)) {
 				g.drawImage(item.getImage(), item.getX() + 2, item.getY() + 2,
 						this);
 			} else {
 				g.drawImage(item.getImage(), item.getX(), item.getY(), this);
 			}
 
 			// If the player is out of time, they are also finished.
 			if (outOfTime)
 				finished = true;
 
 			// Add text that will be shown all the time.
 			Graphics2D g2d = (Graphics2D) g;
 			g2d.setColor(Color.BLACK);
 			g2d.setFont(new Font("Verdana", Font.BOLD, 24));
 			g2d.drawString(sdf.format(new Date(timeRemaining)),
 					BOARD_WIDTH - 150, OFFSET * 8);
 
 			g2d.drawString(currentScore + "/" + goals.size(),
 					BOARD_WIDTH / 2 - 25, OFFSET * 8);
 			g2d.drawString("Press R to restart.", 700, BOARD_HEIGHT - 40);
 
 			// Extra text and commands to do if the player has finished.
 			if (finished) {
 				// Stop the song and timer.
 				SoundEffect.SONG.stoploop();
 				timer.stop();
 
 				// End game text.
 				g2d.setColor(Color.BLACK);
 				g2d.setFont(new Font("Verdana", Font.BOLD, 48));
 				g2d.drawString("Game Over", 150, BOARD_HEIGHT / 2);
 				// Extra text for out of time message.
 				if (outOfTime)
 					g2d.drawString("Out of Time", 150, BOARD_HEIGHT / 2 + 50);
 
 				// Recognition.
 				g2d.setColor(Color.BLACK);
 				g2d.setFont(new Font("Verdana", Font.BOLD, 24));
 				g2d.drawString("Game made by Bowen Hui and Rodney Earl",
 						OFFSET, BOARD_HEIGHT - 160);
 				g2d.drawString("Images by Rodney Earl", OFFSET,
 						BOARD_HEIGHT - 130);
 				g2d.drawString("and Duncan Szarmes", OFFSET, BOARD_HEIGHT - 100);
 				g2d.drawString("Music from Kevin MacLeod", OFFSET,
 						BOARD_HEIGHT - 70);
 				g2d.drawString("Sound effects from MediaCollege.com", OFFSET,
 						BOARD_HEIGHT - 40);
 			}
 
 		}
 	}
 
 	/**
 	 * Custom key adapter class to act upon certain key actions.
 	 * 
 	 * @author Rodney Earl
 	 * @version 1.0
 	 */
 	private class MyKeyAdapter extends KeyAdapter {
 
 		/**
 		 * Method called whenever a key is pressed on the keyboard.
 		 * 
 		 * @param e
 		 *            Key pressed.
 		 */
 		public void keyPressed(KeyEvent e) {
 
 			if (finished) {
 				return;
 			}
 
 			int key = e.getKeyCode();
 
 			// Player has pressed the up key.
 			if (key == KeyEvent.VK_UP) {
 
 				// Check for collisions above the player.
 				if (checkTreeCollision(TOP_COLLISION, player)) {
 					return;
 				}
 				if (checkWaterCollision(TOP_COLLISION, player)) {
 					return;
 				}
 				if (checkShipCollision(TOP_COLLISION, player)) {
 					return;
 				}
 
 				if (checkChestCollision(TOP_COLLISION)) {
 					return;
 				}
 				// If no collision, move the player.
 				player.move(0, -SPRITE_WIDTH);
 			}
 			// Player has pressed the right key.
 			else if (key == KeyEvent.VK_RIGHT) {
 				// Check for collisions to the right of the player.
 				if (checkTreeCollision(RIGHT_COLLISION, player)) {
 					return;
 				}
 				if (checkWaterCollision(RIGHT_COLLISION, player)) {
 					return;
 				}
 				if (checkShipCollision(RIGHT_COLLISION, player)) {
 					return;
 				}
 
 				if (checkChestCollision(RIGHT_COLLISION)) {
 					return;
 				}
 				// If no collision, move the player.
 				player.move(SPRITE_WIDTH, 0);
 			}
 			// Player has pressed the down key.
 			else if (key == KeyEvent.VK_DOWN) {
 				// Check for collisions below the player.
 				if (checkTreeCollision(BOTTOM_COLLISION, player)) {
 					return;
 				}
 				if (checkWaterCollision(BOTTOM_COLLISION, player)) {
 					return;
 				}
 				if (checkShipCollision(BOTTOM_COLLISION, player)) {
 					return;
 				}
 
 				if (checkChestCollision(BOTTOM_COLLISION)) {
 					return;
 				}
 				// If no collision, move the player.
 				player.move(0, SPRITE_WIDTH);
 			}
 			// Player has pressed the left key.
 			else if (key == KeyEvent.VK_LEFT) {
 				// Check for collisions to the left of the player.
 				if (checkTreeCollision(LEFT_COLLISION, player)) {
 					return;
 				}
 				if (checkWaterCollision(LEFT_COLLISION, player)) {
 					return;
 				}
 				if (checkShipCollision(LEFT_COLLISION, player)) {
 					return;
 				}
 
 				if (checkChestCollision(LEFT_COLLISION)) {
 					return;
 				}
 				// If no collision, move the player.
 				player.move(-SPRITE_WIDTH, 0);
 			}
 			// Player has pressed the R key.
 			else if (key == KeyEvent.VK_R) {
 				restartLevel();
 			}
 
 			repaint();
 		}
 
 		/**
 		 * Method to check if an object is going to collide with a tree.
 		 * 
 		 * @param type
 		 *            What type of collision; top, right, bottom, or left.
 		 * @param object
 		 *            Object that is being checked if it will collide with a
 		 *            tree.
 		 * @return True if there is a collision, false otherwise.
 		 */
 		private boolean checkTreeCollision(int type, Sprite object) {
 			/*
 			 * Check which of the four collisions is happening, and then check
 			 * if if the given object will collide with a tree.
 			 */
 			if (type == TOP_COLLISION) {
 				for (int index = 0; index < trees.size(); index++) {
 					Tree tree = trees.get(index);
 					if (object.isTopCollision(tree))
 						return true;
 				}
 				return false;
 			} else if (type == RIGHT_COLLISION) {
 				for (int index = 0; index < trees.size(); index++) {
 					Tree tree = trees.get(index);
 					if (object.isRightCollision(tree))
 						return true;
 				}
 				return false;
 			} else if (type == BOTTOM_COLLISION) {
 				for (int index = 0; index < trees.size(); index++) {
 					Tree tree = trees.get(index);
 					if (object.isBottomCollision(tree))
 						return true;
 				}
 				return false;
 			} else if (type == LEFT_COLLISION) {
 				for (int index = 0; index < trees.size(); index++) {
 					Tree tree = trees.get(index);
 					if (object.isLeftCollision(tree))
 						return true;
 				}
 				return false;
 			}
 			return false;
 		}
 
 		/**
 		 * Method to check if an object is going to collide with a ship.
 		 * 
 		 * @param type
 		 *            What type of collision; top, right, bottom, or left.
 		 * @param object
 		 *            Object that is being checked if it will collide with a
 		 *            ship.
 		 * @return True if there is a collision, false otherwise.
 		 */
 		private boolean checkShipCollision(int type, Sprite object) {
 			/*
 			 * Check which of the four collisions is happening, and then check
 			 * if if the given object will collide with a ship.
 			 */
 			if (type == TOP_COLLISION) {
 				for (int index = 0; index < ships.size(); index++) {
 					Ship ship = ships.get(index);
 					if (object.isTopCollision(ship))
 						return true;
 				}
 				return false;
 			} else if (type == RIGHT_COLLISION) {
 				for (int index = 0; index < ships.size(); index++) {
 					Ship ship = ships.get(index);
 					if (object.isRightCollision(ship))
 						return true;
 				}
 				return false;
 			} else if (type == BOTTOM_COLLISION) {
 				for (int index = 0; index < ships.size(); index++) {
 					Ship ship = ships.get(index);
 					if (object.isBottomCollision(ship))
 						return true;
 				}
 				return false;
 			} else if (type == LEFT_COLLISION) {
 				for (int index = 0; index < ships.size(); index++) {
 					Ship ship = ships.get(index);
 					if (object.isLeftCollision(ship))
 						return true;
 				}
 				return false;
 			}
 			return false;
 		}
 
 		/**
 		 * Method to check if an object is going to collide with water.
 		 * 
 		 * @param type
 		 *            What type of collision; top, right, bottom, or left.
 		 * @param object
 		 *            Object that is being checked if it will collide with
 		 *            water.
 		 * @return True if there is a collision, false otherwise.
 		 */
 		private boolean checkWaterCollision(int type, Sprite object) {
 			/*
 			 * Check which of the four collisions is happening, and then check
 			 * if if the given object will collide with any water.
 			 */
 			if (type == TOP_COLLISION) {
 				for (int index = 0; index < water.size(); index++) {
 					Water waterBlock = water.get(index);
 					if (object.isTopCollision(waterBlock))
 						return true;
 				}
 				return false;
 			} else if (type == RIGHT_COLLISION) {
 				for (int index = 0; index < water.size(); index++) {
 					Water waterBlock = water.get(index);
 					if (object.isRightCollision(waterBlock))
 						return true;
 				}
 				return false;
 			} else if (type == BOTTOM_COLLISION) {
 				for (int index = 0; index < water.size(); index++) {
 					Water waterBlock = water.get(index);
 					if (object.isBottomCollision(waterBlock))
 						return true;
 				}
 				return false;
 			} else if (type == LEFT_COLLISION) {
 				for (int index = 0; index < water.size(); index++) {
 					Water waterBlock = water.get(index);
 					if (object.isLeftCollision(waterBlock))
 						return true;
 				}
 				return false;
 			}
 			return false;
 		}
 
 		/**
 		 * Method to check if a chest will collide with an object.
 		 * 
 		 * @param type
 		 *            What type of collision; top, right, bottom, or left.
 		 * @return True if there is a collision, false otherwise.
 		 */
 		private boolean checkChestCollision(int type) {
 			/*
 			 * Check which of the four collisions is happening, and then check
 			 * if if the given object will collide with a chest. Also need to
 			 * check if the chest will collide with trees, water, ships or other
 			 * chests. If not, then move the chest and check if the player has
 			 * finished.
 			 */
 			if (type == TOP_COLLISION) {
 				for (int index = 0; index < chests.size(); index++) {
 					Treasure chest = chests.get(index);
 					if (player.isTopCollision(chest)) {
 
 						if (checkTreeCollision(TOP_COLLISION, chest))
 							return true;
 						if (checkShipCollision(TOP_COLLISION, chest))
 							return true;
 						if (checkWaterCollision(TOP_COLLISION, chest))
 							return true;
 
 						for (int chestIndex = 0; chestIndex < chests.size(); chestIndex++) {
 							Treasure otherChest = chests.get(chestIndex);
 							if (!chest.equals(otherChest)) {
 								if (chest.isTopCollision(otherChest))
 									return true;
 							}
 						}
 						SoundEffect.PUSH.play();
 						chest.move(0, -SPRITE_WIDTH);
 						checkEndState();
 					}
 				}
 
 				return false;
 			} else if (type == RIGHT_COLLISION) {
 				for (int index = 0; index < chests.size(); index++) {
 					Treasure chest = chests.get(index);
 					if (player.isRightCollision(chest)) {
 
 						if (checkTreeCollision(RIGHT_COLLISION, chest))
 							return true;
 						if (checkShipCollision(RIGHT_COLLISION, chest))
 							return true;
 						if (checkWaterCollision(RIGHT_COLLISION, chest))
 							return true;
 
 						for (int chestIndex = 0; chestIndex < chests.size(); chestIndex++) {
 							Treasure otherChest = chests.get(chestIndex);
 							if (!chest.equals(otherChest)) {
 								if (chest.isRightCollision(otherChest))
 									return true;
 							}
 						}
 						SoundEffect.PUSH.play();
 						chest.move(SPRITE_WIDTH, 0);
 						checkEndState();
 					}
 				}
 				return false;
 			} else if (type == BOTTOM_COLLISION) {
 				for (int index = 0; index < chests.size(); index++) {
 					Treasure chest = chests.get(index);
 					if (player.isBottomCollision(chest)) {
 
 						if (checkTreeCollision(BOTTOM_COLLISION, chest))
 							return true;
 						if (checkShipCollision(BOTTOM_COLLISION, chest))
 							return true;
 						if (checkWaterCollision(BOTTOM_COLLISION, chest))
 							return true;
 
 						for (int chestIndex = 0; chestIndex < chests.size(); chestIndex++) {
 							Treasure otherChest = chests.get(chestIndex);
 							if (!chest.equals(otherChest)) {
 								if (chest.isBottomCollision(otherChest))
 									return true;
 							}
 						}
 						SoundEffect.PUSH.play();
 						chest.move(0, SPRITE_WIDTH);
 						checkEndState();
 					}
 				}
 
 				return false;
 			} else if (type == LEFT_COLLISION) {
 				for (int index = 0; index < chests.size(); index++) {
 					Treasure chest = chests.get(index);
 					if (player.isLeftCollision(chest)) {
 
 						if (checkTreeCollision(LEFT_COLLISION, chest))
 							return true;
 						if (checkShipCollision(LEFT_COLLISION, chest))
 							return true;
 						if (checkWaterCollision(LEFT_COLLISION, chest))
 							return true;
 
 						for (int chestIndex = 0; chestIndex < chests.size(); chestIndex++) {
 							Treasure otherChest = chests.get(chestIndex);
 							if (!chest.equals(otherChest)) {
 								if (chest.isLeftCollision(otherChest))
 									return true;
 							}
 						}
 						SoundEffect.PUSH.play();
 						chest.move(-SPRITE_WIDTH, 0);
 						checkEndState();
 					}
 				}
 				return false;
 			}
 			return false;
 		}
 
 		/**
 		 * Check if the level is completed. Called once a chest is moved, as a
 		 * level will be completed after a chest has moved into the goal.
 		 */
 		public void checkEndState() {
 
 			int completed = 0;
 			// Check if the chests are in the goal squares.
 			for (int chestIndex = 0; chestIndex < chests.size(); chestIndex++) {
 				Treasure chest = (Treasure) chests.get(chestIndex);
 				for (int goalIndex = 0; goalIndex < goals.size(); goalIndex++) {
 					Goal goal = goals.get(goalIndex);
 					if (chest.getX() == goal.getX()
 							&& chest.getY() == goal.getY())
 						completed++;
 				}
 			}
 			// Current score is the number of chests in goal squares.
 			currentScore = completed;
 			// If all goal squares are filled, then the player is finished.
 			if (completed == goals.size()) {
 				finished = true;
 				repaint();
 			}
 		}
 
 		/**
 		 * Method to restart the level.
 		 */
 		public void restartLevel() {
 
 			goals.clear();
 			chests.clear();
 			trees.clear();
 			initWorld(levelOne);
 			if (finished) {
 				finished = false;
 			}
 		}
 	}
 
 	/**
 	 * Custom class for the timer.
 	 * 
 	 * @author Rodney Earl
 	 */
 	private class CDT implements ActionListener {
 
 		/**
 		 * Method called after actions are performed or after specified
 		 * intervals.
 		 * 
 		 * @param ae
 		 *            An action event. Not used in this custom timer.
 		 */
 		public void actionPerformed(ActionEvent ae) {
 			// Decrement the time remaining by one second.
 			timeRemaining -= 1000;
 			// If time remaining is 0, then the player is out of time.
 			if (timeRemaining == 0)
 				outOfTime = true;
 			repaint();
 		}
 	}
 }
