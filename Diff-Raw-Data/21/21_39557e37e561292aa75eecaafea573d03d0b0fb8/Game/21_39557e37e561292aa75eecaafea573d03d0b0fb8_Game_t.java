 package net.codegames.towerninja;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Vector;
 
 import net.codegames.towerninja.AppletRenderer;
 import net.codegames.towerninja.Bat;
 import net.codegames.towerninja.Bomb;
 import net.codegames.towerninja.Brick;
 import net.codegames.towerninja.Main;
 import net.codegames.towerninja.Player;
 import net.codegames.towerninja.Score;
 import net.codegames.towerninja.Scoreboard;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PImage;
 
 /**
  * @author chameleon
  * 
  *         This class contains a tower data structure. The method createStone
  *         allows creating new stones for free spots within the tower.
  */
 public class Game {
 
 	private final long NEW_STONE_DELAY = 500L;
 	private final float MIN_SPEED = 30;
 	private PApplet mApplet;
 	private AppletRenderer mRenderer;
 	private long mLastTimeStamp = System.currentTimeMillis();
 	private long mStartTimeStamp = System.currentTimeMillis();
 	public Scoreboard scoreboard = new Scoreboard();
 	public Score score;
 	private boolean startScreen = true;
 
 	public boolean isStartScreen() {
 		return startScreen;
 	}
 
 	public void setStartScreen(boolean startScreen) {
 		this.startScreen = startScreen;
 	}
 
 	public boolean gameover = false;
 	public boolean restart = false;
 	PImage bubble;
 	private boolean once = true;
 	private boolean firstGameStart = true;
 	private static final int TOWER_HEIGHT = 8;
 	private static final int TOWER_WIDTH = 5;
 	/**
 	 * A tower represented by a 2-dimensional data strucure. the first dimension
 	 * describes the towers width. The second its height. When a new stone is
 	 * added to a certain location, it must not actually be located at that
 	 * position already. It will fly towards that position though.
 	 */
 	private Vector<Brick[]> tower = new Vector<Brick[]>();
 	private int diffCounter = 0;
 	
 	PFont katana, myriad;
 
 	/**
 	 * Game constructor
 	 * 
 	 * @param applet
 	 *            <code>Main</code> instance
 	 */
 	public Game(PApplet applet) {
 		this.mApplet = applet;
 		init();
 	}
 
 	private void init() {
 		gameover = false;
 		restart = false;
 		this.mRenderer = new AppletRenderer(mApplet);
 		scoreboard.load("scores.txt");
 		score = new Score();
 		scoreboard.addScore(score);
 		for (int i = 0; i < TOWER_HEIGHT; i++) {
 			tower.add(new Brick[TOWER_WIDTH]);
 		}
 
 		bubble = mApplet.loadImage(mApplet.getCodeBase()
 				+ "../resources/speechbubble.png");
 		
 		katana = mApplet.loadFont(mApplet.getCodeBase()
 				+ "../resources/Katana-54.vlw");
 		myriad = mApplet.loadFont(mApplet.getCodeBase()
 				+ "../resources/MyriadPro-32.vlw");
 		mApplet.textFont(katana);
 	}
 
 	private boolean existsCompleteStoneLine3() {
 		boolean isComplete = true;
 		for (int i = 0; i < TOWER_WIDTH; i++) {
 			if (tower.get(4)[i] == null || !tower.get(4)[i].isOnTower()) {
 				isComplete = false;
 			}
 		}
 		return isComplete;
 	}
 
 	private void removeStoneLine0() {
		if (tower.get(0)[0] != null && !tower.get(0)[0].isDestroyed()) {
			System.out.println("distreud");
			for (int i = 0; i < TOWER_WIDTH; i++) {
				tower.get(0)[i].setDestroyed(true);
			}
		}
		else if (tower.get(0)[0].isDestroyed() && tower.get(0)[0].getDestroyTimer() == 0) {
			System.out.println("riemuf");
 			tower.remove(0);
 			diffCounter ++;
 			tower.add(new Brick[TOWER_WIDTH]);
 			for (int i = 0; i < TOWER_HEIGHT; i++) {
 				for (int j = 0; j < TOWER_WIDTH; j++) {
 					if (tower.get(i)[j] != null) {
 						tower.get(i)[j].setDestination(i, j);
 						if (tower.get(i)[j].isOnTower())
 							tower.get(i)[j].setyLocation(tower.get(i)[j]
 									.exactYDestination());
 					}
 				}
 			}
 		}
 
 	}
 
 	public void update(Vector<Player> players) {
 		// startscreen
 		if (startScreen) {
 			drawStartScreen(players);
 			drawPlayerHands(players);
 			detectSlices(players);
 			removeStones();
 			drawStones();
 			once = true;
 
 		} else {
 			// Game running
 			if (!gameover) {
 				if (firstGameStart) {
 
 					if (gameover)
 						restart = true;
 					else
 						restart = false;
 					gameover = false;
 					mStartTimeStamp = System.currentTimeMillis();
 					tower.get(0)[0] = new Brick(50, 5, 0, 0);
 					tower.get(0)[0].putOnTower();
 					firstGameStart = false;
 					once = true;
 				}
 				if (System.currentTimeMillis() - mLastTimeStamp > NEW_STONE_DELAY) {
 					mLastTimeStamp = System.currentTimeMillis();
 					createStone();
 				}
 				drawPlayerHands(players);
 				detectSlices(players);
 				moveStones();
 				if (existsCompleteStoneLine3())
 					// System.out.println("3rd line complete");
 					removeStoneLine0();
 
				removeStones();
 				drawStones();
 				displayScore();
 				displayTime();
 				checkGameover();
 			}
 			// gameover
 			else {
 				drawScoreboard(players);
 				if (once) {
 					scoreboard.save("scores.txt");
 					firstGameStart = true;
 					once = false;
 				}
 
 				drawPlayerHands(players);
 				detectSlices(players);
 				removeStones();
 				drawStones();
 
 			}
 		}
 	}
 
 	/**
 	 * Moves all stones for the passed number of frames.
 	 */
 	private void moveStones() {
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(i).length; j++) {
 				if (tower.get(i)[j] != null) {
 					if (!tower.get(i)[j].isOnTower()) {
 						tower.get(i)[j].moveToDestination(mApplet.frameRate);
 						if (tower.get(i)[j].isOnTower()) {
 							score.addScore(tower.get(i)[j].getPoints());
 
 							if (tower.get(i)[j] instanceof Bomb) {
 								handleExplosion(i, j);
 							}
 						}
 					}
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handles the explosion of a bomb for a given position in the tower and
 	 * triggers the removal of all stones.
 	 * 
 	 * @param i
 	 *            row in the tower
 	 * @param j
 	 *            column in the tower
 	 */
 	private void handleExplosion(int i, int j) {
 		int rowStart = (i - 1) < 0 ? 0 : (i - 1);
 		int rowEnd = (i + 1) >= TOWER_HEIGHT ? TOWER_HEIGHT - 1 : (i + 1);
 		int columnStart = (j - 1) < 0 ? 0 : (j - 1);
 		int columnEnd = (j + 1) >= TOWER_WIDTH ? TOWER_WIDTH - 1 : (j + 1);
 
 		for (int x = rowStart; x <= rowEnd; x++) {
 			for (int y = columnStart; y <= columnEnd; y++) {
 				if (tower.get(x)[y] != null && tower.get(x)[y].isOnTower()) {
 					destroyStone(x, y);
 				}
 			}
 		}
 		updateDestinations();
 	}
 
 	/**
 	 * Checks for all flying stones if they need a new destination position, so
 	 * that stones don't land "floating in the air".
 	 */
 	private void updateDestinations() {
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(i).length; j++) {
 				if (tower.get(i)[j] != null && !tower.get(i)[j].isOnTower()) {
 					int minRow = Integer.MAX_VALUE;
 					for (int k = i - 1; k >= 0; k--) {
 						if (tower.get(k)[j] == null) {
 							minRow = k;
 						}
 					}
 					if (minRow < Integer.MAX_VALUE) {
 						tower.get(minRow)[j] = tower.get(i)[j];
 						tower.get(minRow)[j].updatePathWithLastPosition(minRow,
 								j);
 						tower.get(i)[j] = null;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method loops through the tower data structure and finds the lowest
 	 * leftmost free spot. It then creates a stone for that spot and returns its
 	 * reference
 	 * 
 	 * @return a reference to the next {@link Brick}
 	 */
 	private Brick createStone() {
 		updateDestinations();
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(i).length; j++) {
 				if (tower.get(i)[j] == null) {
 					double rand = Math.random();
 					if (rand < 0.9d - diffCounter/20.0) {
 						tower.get(i)[j] = new Brick(randomXPosition(), 5, i, j);
 					} else {
 						tower.get(i)[j] = new Bomb(randomXPosition(), 5, i, j);
 					}
 					return tower.get(i)[j];
 				}
 			}
 		}
 		return null;
 	}
 
 	private int randomXPosition() {
 		return (int) ((Math.round(Math.random()) * Main.width)
 				- (Math.random()-0.5) * 120) - 42;
 	}
 
 	/**
 	 * draws every stone
 	 */
 	private void drawStones() {
 		mApplet.fill(64);
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(0).length; j++) {
 				if (tower.get(i)[j] != null) {
 					tower.get(i)[j].draw(mRenderer);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws the hand positions of every player.
 	 * 
 	 * @param players
 	 *            vector of all {@link Player}s
 	 */
 	private void drawPlayerHands(Vector<Player> players) {
 		for (int p = 0; p < players.size(); p++) {
 			Player currentPlayer = players.get(p);
 			int color = currentPlayer.getColor();
 
 			mApplet.noFill();
 			mApplet.strokeWeight(20);
 			mApplet.strokeJoin(mApplet.ROUND);
 			mApplet.strokeCap(mApplet.ROUND);
 			mApplet.stroke(color, 32);
 			// left hand
 			if (currentPlayer.getLeftSpeed() > MIN_SPEED) {
 				// draw 6 traces of different length over each other
 				for (int i = 0; i < 6; i++) {
 					mApplet.beginShape();
 					for (int j = 0; j < currentPlayer.getLeft().size()
 							- (i + 3); j++) {
 						mApplet.vertex(currentPlayer.getLeft().get(j)[0],
 								currentPlayer.getLeft().get(j)[1]);
 					}
 					mApplet.endShape();
 				}
 			}
 			// right hand
 			if (currentPlayer.getRightSpeed() > MIN_SPEED) {
 				// draw 6 traces of different length over each other
 				for (int i = 0; i < 6; i++) {
 					mApplet.beginShape();
 					for (int j = 0; j < currentPlayer.getRight().size()
 							- (i + 3); j++) {
 						mApplet.vertex(currentPlayer.getRight().get(j)[0],
 								currentPlayer.getRight().get(j)[1]);
 					}
 					mApplet.endShape();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks for all flying stones if a player currently slices them.
 	 * 
 	 * @param players
 	 *            vector of all {@link Player}s
 	 */
 	private void detectSlices(Vector<Player> players) {
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(0).length; j++) {
 				if (tower.get(i)[j] != null) {
 					for (int p = 0; p < players.size(); p++) {
 						Player currentPlayer = players.get(p);
 						// left hand detection
 						if (currentPlayer.getLeftSpeed() > MIN_SPEED
 								&& tower.get(i)[j] != null) {
 							if (tower.get(i)[j].contains(
 									currentPlayer.getLeftX(),
 									currentPlayer.getLeftY(),
 									currentPlayer.getLastLeftX(),
 									currentPlayer.getLastLeftY())
 									&& !tower.get(i)[j].isOnTower()) {
 								destroyStone(i, j);
 								updateDestinations();
 							}
 						}
 						// right hand detection
 						if (currentPlayer.getRightSpeed() > MIN_SPEED
 								&& tower.get(i)[j] != null) {
 							if (tower.get(i)[j].contains(
 									currentPlayer.getRightX(),
 									currentPlayer.getRightY(),
 									currentPlayer.getLastRightX(),
 									currentPlayer.getLastRightY())
 									&& !tower.get(i)[j].isOnTower()) {
 								destroyStone(i, j);
 								updateDestinations();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets sliced or exploded stones as destroyed.
 	 * 
 	 * @param i
 	 *            tower row
 	 * @param j
 	 *            tower column
 	 */
 	private void destroyStone(int i, int j) {
 		score.addScore(-1 * tower.get(i)[j].getPoints());
 		tower.get(i)[j].setDestroyed(true);
 		if (startScreen)
 			startScreen = false;
 		if (gameover) {
 			restart = true;
 		}
 
 		// tower.get(i)[j] = null;
 	}
 
 	/**
 	 * Loops through the tower and removes all stones with finished destroy
 	 * animation.
 	 */
 	private void removeStones() {
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(0).length; j++) {
 				if (tower.get(i)[j] != null
 						&& (tower.get(i)[j].isDestroyed() || gameover)) {
 					if (tower.get(i)[j].getDestroyTimer() == 0) {
 						tower.get(i)[j] = null;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Loops through the tower and removes all stones with finished destroy
 	 * animation.
 	 */
 	private void removeAllStones() {
 		for (int i = 0; i < tower.size(); i++) {
 			for (int j = 0; j < tower.get(0).length; j++) {
 				tower.get(i)[j] = null;
 			}
 		}
 	}
 
 	/**
 	 * Displays the Score in game
 	 * 
 	 */
 	private void displayScore() {
 		mApplet.textAlign(mApplet.LEFT, mApplet.TOP);
 		mApplet.textSize(44);
 		mApplet.fill(64);
 		mApplet.text("SCORE: " + this.score.getScore(), 10, 10);
 	}
 
 	/**
 	 * Displays the Score in game
 	 * 
 	 */
 
 	private void displayTime() {
 
 		long elapsedTime = System.currentTimeMillis() - mStartTimeStamp;
 		String time = new SimpleDateFormat("mm:ss")
 				.format(new Date(elapsedTime));
 		score.setTime(time);
 
 		mApplet.textSize(44);
 		mApplet.fill(64);
 		mApplet.textAlign(mApplet.RIGHT, mApplet.TOP);
 		mApplet.text("" + time, Main.width - 10, 10);
 	}
 
 	/**
 	 * Draw the Scoreboard
 	 * 
 	 */
 	private void drawScoreboard(Vector<Player> players) {
 		Score[] scoreboardArray = this.scoreboard.giveScoreboard();
 		mApplet.textSize(36);
 		mApplet.textAlign(mApplet.LEFT);
 		mApplet.text("PLACE", 110, 50);
 		mApplet.text("DATE", 220, 50);
 		mApplet.text("POINTS", 620, 50);
 		mApplet.text("TIME", 750, 50);
 		
 		mApplet.textFont(myriad);
 		mApplet.textSize(22);
 		for (int i = 0; i < scoreboardArray.length && i <= 9; i++) {
 			mApplet.text(i + 1, 110, 80 + i * 30);
 			mApplet.text(scoreboardArray[i].getName(), 220, 80 + i * 30);
 			mApplet.text(scoreboardArray[i].getScore(), 620, 80 + i * 30);
 			mApplet.text(scoreboardArray[i].getTime(), 750, 80 + i * 30);
 		}
 		mApplet.textFont(katana);
 
 		drawStartScreen(players);
 	}
 
 	/**
 	 * Draw the start screen.
 	 */
 	private void drawStartScreen(Vector<Player> players) {
 		tower.get(0)[0] = new Bat(Main.width / 2 - 112, Main.height / 2, 400,
 				500);
 
 		if (!players.isEmpty()) {
 			mApplet.image(bubble, Main.width / 2 - bubble.width / 2,
 					Main.height / 2 - bubble.height);
 			// mApplet.text("Slice the Bat to start the game!", 500,
 			// 480-bubble.height/2);
 		}
 	}
 
 	private boolean checkGameover() {
		for (int i = 0; i < 2 /* tower.size() */; i++) {
 			for (int j = 0; j < tower.get(0).length; j++) {
 				if (tower.get(i)[j] != null && tower.get(i)[j].isOnTower()) {
 					return false;
 				}
 			}
 		}
 		gameover = true;
 		removeAllStones();
 		return true;
 	}
 
 }
