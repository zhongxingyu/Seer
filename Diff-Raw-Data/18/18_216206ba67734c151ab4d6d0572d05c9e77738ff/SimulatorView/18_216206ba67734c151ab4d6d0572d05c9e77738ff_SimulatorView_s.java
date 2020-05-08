 package guiAntGame;
 
 import java.util.ArrayList;
 
 import antgame.core.Cell;
 import antgame.core.Map;
 import antgame.core.World;
 import processing.core.*;
 import org.gicentre.utils.move.*;
 import controlP5.*;
 
 public class SimulatorView extends PApplet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Hexagon[][] board;
 	private PImage rock, food, antB, antR, antHR, antHB, clear,
 			scoreboard_round, scoreboard_red, scoreboard_black;
 	private int xSize, ySize;
 	private ObserverAntWorld obiwan;
 	private Map curMap;
 	private World w;
 	private ZoomPan zoomer;
 	private int round = 0;
 	private PFont f, f2;
 	private ControlP5 cp5;
 	private float frameRateInt = 60;
 	private boolean didthegamefinallyend = false;
 	
 	public void setWorld(World w){
 		this.w = w;
 	}
 	
 	public void setup() {
 		smooth();
 		noStroke();
 
 		size(800, 700);
 
 		zoomer = new ZoomPan(this);
		zoomer.allowZoomButton(false);
 
 		this.obiwan = w.getObserver();
 		curMap = w.getMap();
 		this.xSize = curMap.getXSize();
 		this.ySize = curMap.getYSize();
 		rock = loadImage("rock.png");
 		food = loadImage("food.png");
 		antHR = loadImage("redH.png");
 		antHB = loadImage("blackH.png");
 		clear = loadImage("clear.png");
 		antB = loadImage("blackAnt.png");
 		antR = loadImage("redAnt.png");
 
 		scoreboard_round = loadImage("scoreboard_round.png");
 		scoreboard_red = loadImage("scoreboard_red.png");
 		scoreboard_black = loadImage("scoreboard_black.png");
 
 		board = new Hexagon[xSize][ySize];
 		Cell cCell;
 		for (int y = 0; y < ySize; y++) {
 			for (int x = 0; x < xSize; x++) {
 				cCell = curMap.getCell(y, x);
 				if (cCell.containsRock()) {
 					board[x][y] = new Hexagon(this, rock);
 				}
 
 				else if (cCell.containsBlackAnt()) {
 					board[x][y] = new Hexagon(this, antB);
 				} else if (cCell.containsRedAnt()) {
 					board[x][y] = new Hexagon(this, antR);
 				} else if (cCell.isContainsFood()) {
 					board[x][y] = new Hexagon(this, food);
 				} else if (cCell.containsBlackAntHill()) {
 					board[x][y] = new Hexagon(this, antHB);
 				} else if (cCell.containsRedAntHill()) {
 					board[x][y] = new Hexagon(this, antHR);
 				} else if (cCell.isClear()) {
 					board[x][y] = new Hexagon(this, clear);
 				}
 
 			}
 		}
 
 		f = createFont("Arial", 20, true);
 		f2 = createFont("Arial", 34, true);
 
 		cp5 = new ControlP5(this);
 
 		cp5.addSlider("speed").setPosition(10, 640).setSize(150, 20)
 				.setRange(30, 500).setValue(60);
 		cp5.addFrameRate().setInterval(10).setPosition(130, 645);
 		cp5.addButton("endgame").setPosition(10, 670).setSize(150, 20);
 
 	}
 
 	public void draw() {
 		frameRate(frameRateInt);
 		background(170);
 		pushMatrix();
 		zoomer.transform();
 
 		for (int x = 0; x < xSize; x++) {
 			for (int y = 0; y < ySize; y++) {
 
 				if (x % 2 == 0) {
 					if (!board[x][y].toCompare(clear)) {
 						board[x][y].display(40 * y, x * 35, 40, 47);
 					}
 
 				} else {
 					if (!board[x][y].toCompare(clear)) {
 						board[x][y].display(40 * y + 20, x * 35, 40, 47);
 					}
 				}
 			}
 		}
 		popMatrix();
 		if (didthegamefinallyend) {
 			// Displayed when the game ends
 			// End the game
 			finishGame();
 			updateAllBoard();
 			showTheWinner();
 
 		}
 		if (round <= 300000 && w.getStats().getBlackAlive() > 0
 				&& w.getStats().getRedAlive() > 0) {
 			// Game simulation display
 			w.step();
 			updateBoard(obiwan.getToUpdate());
 			obiwan.clearList();
 			round++;
 
 			// ScoreBoard
 			fill(40);
 			rect(0, 0, width, 70);
 			image(scoreboard_round, width / 2 - 75, 0);
 			image(scoreboard_red, 0, 0);
 			image(scoreboard_black, width - 189, 0);
 			textAlign(CENTER);
 			fill(200);
 			textFont(f);
 			text(round, width / 2, 65);
 			textAlign(RIGHT);
 			text("Food: " + obiwan.getWorld().getStats().getFoodUnitsRedHill(),
 					width / 2 - 90, 32);
 			text("Alive: " + obiwan.getWorld().getStats().getRedAlive(),
 					width / 2 - 90, 57);
 			textAlign(LEFT);
 			text("Food: "
 					+ obiwan.getWorld().getStats().getFoodUnitsBlackHill(),
 					width / 2 + 90, 32);
 			text("Alive: " + obiwan.getWorld().getStats().getBlackAlive(),
 					width / 2 + 90, 57);
 
 			image(scoreboard_red, 0, 0);
 			image(scoreboard_black, width - 189, 0);
 
 		} else if (w.getStats().getBlackAlive() == 0
 				|| w.getStats().getRedAlive() == 0) {
 			// Displayed when one of the teams wins prior 300000 rounds
 			showTheWinner();
 		} else if (!didthegamefinallyend)
 		// Displayed when EndGame is clicked
 		{
 			fill(20, 230);
 			rect(width / 2 - 300, height / 2 - 200, 600, 400);
 			fill(255);
 			textAlign(CENTER);
 			textFont(f);
 
 			text("Please wait, ants are busy having a war.", width / 2,
 					height / 2 - 100);
 
 			didthegamefinallyend = true;
 		}
 
 	}
 
 	public void updateBoard(ArrayList<Cell> toUpdate) {
 		for (Cell cCell : toUpdate) {
 			if (cCell.containsBlackAnt()) {
 				board[cCell.getPos()[1]][cCell.getPos()[0]].setIcon(antB);
 			} else if (cCell.containsRedAnt()) {
 				board[cCell.getPos()[1]][cCell.getPos()[0]].setIcon(antR);
 			} else if (cCell.isContainsFood()) {
 				board[cCell.getPos()[1]][cCell.getPos()[0]].setIcon(food);
 			} else if (cCell.containsBlackAntHill()) {
 				board[cCell.getPos()[1]][cCell.getPos()[0]].setIcon(antHB);
 			} else if (cCell.containsRedAntHill()) {
 				board[cCell.getPos()[1]][cCell.getPos()[0]].setIcon(antHR);
 			} else if (cCell.isClear()) {
 				board[cCell.getPos()[1]][cCell.getPos()[0]].setIcon(clear);
 			}
 		}
 	}
 
 	public void updateAllBoard() {
 		Cell cCell;
 		for (int y = 0; y < ySize; y++) {
 			for (int x = 0; x < xSize; x++) {
 				cCell = curMap.getCell(y, x);
 				if (cCell.containsRock()) {
 					board[x][y] = new Hexagon(this, rock);
 				} else if (cCell.containsBlackAnt()) {
 					board[x][y] = new Hexagon(this, antB);
 				} else if (cCell.containsRedAnt()) {
 					board[x][y] = new Hexagon(this, antR);
 				} else if (cCell.isContainsFood()) {
 					board[x][y] = new Hexagon(this, food);
 				} else if (cCell.containsBlackAntHill()) {
 					board[x][y] = new Hexagon(this, antHB);
 				} else if (cCell.containsRedAntHill()) {
 					board[x][y] = new Hexagon(this, antHR);
 				} else if (cCell.isClear()) {
 					board[x][y] = new Hexagon(this, clear);
 				}
 
 			}
 		}
 	}
 
 	void speed(float rate) {
 		frameRateInt = rate;
 	}
 	
 	private void showTheWinner(){
 		frameRateInt = 30;
 		// Create a rectable box
 		fill(20, 230);
 		rect(width / 2 - 300, height / 2 - 200, 600, 300);
 		fill(225);
 
 		// Styling for the text
 		textAlign(CENTER);
 		textFont(f2);
 		String winner;
 		// Check who won
 		if (w.whoWon() != null) {
 			winner = w.whoWon().toString();
 		} else {
 			winner = "TIE";
 		}
 
 		// Display the winner
 		text("Result: " + winner, width / 2, height / 2 - 150);
 		textFont(f);
 		text("Rounds: " + obiwan.getWorld().getStats().getRound(),
 				width / 2, height / 2 - 130);
 		textFont(f2);
 		// Add some stats:
 		textAlign(RIGHT);
 		// Red Team
 		text("Red:", width / 2 - 100, height / 2 - 60);
 		textFont(f);
 		text("Food: " + obiwan.getWorld().getStats().getFoodUnitsRedHill(),
 				width / 2 - 100, height / 2 - 30);
 		text("Alive: " + obiwan.getWorld().getStats().getRedAlive(),
 				width / 2 - 100, height / 2);
 		text("Dead: " + obiwan.getWorld().getStats().getRedAntsdead(),
 				width / 2 - 100, height / 2 + 30);
 
 		textAlign(LEFT);
 		// Black Team
 		textFont(f2);
 		text("Black:", width / 2 + 100, height / 2 - 60);
 
 		textFont(f);
 		text("Food: "
 				+ obiwan.getWorld().getStats().getFoodUnitsBlackHill(),
 				width / 2 + 100, height / 2 - 30);
 		text("Alive: " + obiwan.getWorld().getStats().getBlackAlive(),
 				width / 2 + 100, height / 2);
 		text("Dead: " + obiwan.getWorld().getStats().getBlackAntsDead(),
 				width / 2 + 100, height / 2 + 30);
 	}
 
 	public void endgame() {
 		if (round < 300000) {
 			round = 300000;
 		}
 
 	}
 
 	public void finishGame() {
 		int curR = w.getStats().getRound();
 		while (curR < 300000 && w.getStats().getBlackAlive() > 0
 				&& w.getStats().getRedAlive() > 0) {
 			w.step();
 			curR++;
 		}
 	}
 }
