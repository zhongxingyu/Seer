 package edu.bsu.cs222.warVessels;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * The basic framework for an artificial intelligence that can play War Vessels.
  * This class takes care of most of the implementation. It leaves the
  * randomGuess method implementation up to whatever subclasses this class since
  * this aspect largely determines how "smart" the AI is.
  * 
  * @author Steffan Byrne
  * 
  */
 public abstract class AbstractAI {
 	private Grid ocean;
 	protected Grid radar;
 
 	protected int x;
 	protected int y;
 
 	private int hitsOnShip;
 
 	private String sunk;
 	private String ship;
 
 	private boolean shipFound;
 	private boolean shipVert;
 	private boolean shipHoriz;
 
 	private ArrayList<Integer> xHit;
 	private ArrayList<Integer> yHit;
 	private ArrayList<Integer> choices;
 
 	private int lastHitX;
 	private int lastHitY;
 
 	/**
 	 * Constructs an AI given two grids.
 	 * 
 	 * @param ocean
 	 *            The Grid used as the AI's ocean board
 	 * @param radar
 	 *            The Grid used as the AI's radar board. This should be the
 	 *            opponent's ocean board.
 	 */
 	public AbstractAI(Grid ocean, Grid radar) {
 		this.ocean = ocean;
 		this.radar = radar;
 		shipFound = false;
 		shipHoriz = false;
 		shipVert = false;
 		hitsOnShip = 0;
 
 		xHit = new ArrayList<Integer>();
 		yHit = new ArrayList<Integer>();
 		choices = new ArrayList<Integer>();
 
 	}
 
 	/**
 	 * Places one of every type of ship on the board.
 	 */
 	public void placeAllShips() {
 		for (ShipType s : ShipType.values()) {
 			placeShip(new Ship(s));
 		}
 	}
 
 	/**
 	 * Places a ship on the AI's grid in a random position.
 	 * 
 	 * @param aShip
 	 *            the ship to place on the grid
 	 */
 	public void placeShip(Ship aShip) {
 		Random rand = new Random();
 		do {
 			int i = rand.nextInt(4); // rotate the ship a random number of
 										// times.
 			for (int j = 0; j < i; j++) {
 				aShip.changeBearing();
 			}
 			x = rand.nextInt(10);
 			y = rand.nextInt(10);
 		} while (!ocean.validateLocation(x, y, aShip)); // Ensure ship will be
 														// placed in a legal
 														// location
 
 		ocean.setShip(x, y, aShip);
 
 	}
 
 	/**
 	 * Guesses a location where an opponent's ship might be.
 	 */
 	public void guessLocation() {
 		x = 0;
 		y = 0;
 
 		sunk = "";
 		ship = "";
 
 		if (!shipFound) { // If a ship has not been found then guess randomly.
 			randomGuess();
 		} else {
 			smartGuess();
 		}
 
 		if (radar.checkGuess(x, y)) {
 			ship = radar.getShip(x, y).getType().toString();
 			hitAShip();
 		} else {
 			if (hitsOnShip > 1) {
 				xHit.subList(1, xHit.size()).clear();
 				yHit.subList(1, yHit.size()).clear();
 			}
 		}
 	}
 
 	public String guessToString() {
 		return "Guessed " + x + ", " + y + ": " + radar.getStatus(x, y) + ship
 				+ sunk;
 	}
 
 	/**
 	 * 
 	 * @return the AI's Grid
 	 */
 	public Grid getOcean() {
 		return ocean;
 	}
 
 	public Point getGuess(){
 		return new Point(x,y);
 	}
 	
 	protected abstract void randomGuess();
 
 	/**
 	 * Chooses a location based on previous hits on a ship
 	 */
 	private void smartGuess() {
 		choices = new ArrayList<Integer>();
 
 		lastHitX = xHit.get(xHit.size() - 1);
 		lastHitY = yHit.get(xHit.size() - 1);
 		/*
 		 * If the ship is vertical then check the north and south cell states.If
 		 * at least one of these is open, then guess it. If both are open choose
 		 * randomly
 		 */
 		if (shipVert) {
 
 			checkNSCells();
 			if (choices.size() != 0) {
 				chooseCell();
 			} else {
 				// Two ships near each other made it look like ship was vertical
 				shipVert = false;
 				checkEWCells();
 				if (choices.size() != 0) {
 					chooseCell();
 				} else {
 					randomGuess();
 				}
 			}
 
 		} else if (shipHoriz) {
 
 			checkEWCells();
 			if (choices.size() != 0) {
 				chooseCell();
 			} else {
 				// Two ships near each other made it look like ship was
 				// horizontal
 				shipHoriz = false;
 				checkNSCells();
 				if (choices.size() != 0) {
 					chooseCell();
 				} else {
 					randomGuess();
 				}
 			}
 
 		} else { // check N, E, S, W of the last hit cell
 
 			checkCellsNear();
 		}
 	}
 
 	/**
 	 * Checks the status of the four cells surrounding a guess that hit
 	 */
 	private void checkCellsNear() {
 		checkEWCells();
 		checkNSCells();
 		if (choices.size() != 0) {
 			chooseCell();
 		} else {
 			randomGuess();
 		}
 	}
 
 	/**
 	 * Randomly chooses a cell out of the ones available.
 	 */
 	private void chooseCell() {
 		Random rand = new Random();
 
 		switch (choices.get(rand.nextInt(choices.size()))) {
 		case 0:
 			setGuess(NearbyCell.EAST);
 			break;
 		case 1:
 			setGuess(NearbyCell.WEST);
 			break;
 		case 2:
 			setGuess(NearbyCell.SOUTH);
 			break;
 		case 3:
 			setGuess(NearbyCell.NORTH);
 		}
 	}
 
 	/**
 	 * Check the East and West cells
 	 */
 	private void checkEWCells() {
 		if (lastHitX < 9 && checkCell(NearbyCell.EAST)) {
 			choices.add(0);
 		}
 		if (lastHitX > 0 && checkCell(NearbyCell.WEST)) {
 			choices.add(1);
 		}
 	}
 
 	/**
 	 * Check the North and South cells
 	 */
 	private void checkNSCells() {
 		if (lastHitY < 9 && checkCell(NearbyCell.SOUTH)) {
 			choices.add(2);
 		}
 		if (lastHitY > 0 && checkCell(NearbyCell.NORTH)) {
 			choices.add(3);
 		}
 	}
 
 	/**
 	 * Checks a cell nearby the last hit to see if it has been guessed.
 	 * 
 	 * @param near
 	 *            the relative location of the nearby cell
 	 * @return true if cell has not been guessed, false otherwise
 	 */
 	private boolean checkCell(NearbyCell near) {
 		return radar.getStatus(near.newX(lastHitX), near.newY(lastHitY))
 				.equals(Guess.NOT_GUESSED);
 	}
 
 	/**
 	 * Sets the x and y coordinates for the next guess.
 	 * 
 	 * @param near
 	 * 			the relative location of the cell to guess.
 	 */
 	private void setGuess(NearbyCell near) {
 		x = near.newX(lastHitX);
 		y = near.newY(lastHitY);
 	}
 
 	
 	private void hitAShip() {
 		if (!radar.getShip(x, y).isShipSunk()) {
 			shipFound = true;
 			xHit.add(x);
 			yHit.add(y);
 			hitsOnShip++;
 
 			if (hitsOnShip > 1) { // If the ship has been hit more than once
 									// then determine the orientation
 				if (xHit.get(0) == xHit.get(1)) {
 					shipVert = true;
 				} else {
 					shipHoriz = true;
 				}
 			}
 
 		} else { // Ship is sunk so reset these variables
 			sunk = " Sunk ";
 
 			xHit = new ArrayList<Integer>();
 			yHit = new ArrayList<Integer>();
 			shipFound = false;
 			hitsOnShip = 0;
 			shipVert = false;
 			shipHoriz = false;
 		}
 	}
 
 }
