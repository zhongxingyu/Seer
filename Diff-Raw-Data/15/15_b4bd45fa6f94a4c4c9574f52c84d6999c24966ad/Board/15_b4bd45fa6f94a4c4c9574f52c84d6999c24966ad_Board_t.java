 /**
  * This is free and unencumbered software released into the public domain.
  * 
  * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
  * this software, either in source code form or as a compiled binary, for any
  * purpose, commercial or non-commercial, and by any means.
  * 
  * In jurisdictions that recognize copyright laws, the author or authors of this
  * software dedicate any and all copyright interest in the software to the
  * public domain. We make this dedication for the benefit of the public at large
  * and to the detriment of our heirs and successors. We intend this dedication
  * to be an overt act of relinquishment in perpetuity of all present and future
  * rights to this software under copyright law.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * For more information, please refer to [http://unlicense.org]
  */
 package ch.bfh.bti7301.w2013.battleship.game;
 
 import java.util.ArrayList;
 
 import ch.bfh.bti7301.w2013.battleship.game.players.GenericPlayer.PlayerState;
 
 /**
  * @author Simon Krenger <simon@krenger.ch>
  * 
  */
 public class Board {
 
 	/**
 	 * Default board size
 	 */
 	private static int DEFAULT_BOARD_SIZE = 10;
 
 	/**
 	 * Size of the board (n*n)
 	 */
 	private int size;
 
 	/**
 	 * Player this board belongs to
 	 */
 	private Player owner;
 
 	/**
 	 * List of placed ships on this board
 	 */
 	private ArrayList<Ship> placedShips = new ArrayList<Ship>();
 
 	/**
 	 * List of placed missiles on this board
 	 */
 	private ArrayList<Missile> placedMissiles = new ArrayList<Missile>();
 
 	/**
 	 * Setup object. Needed for Board setup
 	 */
 	private BoardSetup setup = new BoardSetup();
 
 	private ArrayList<BoardListener> listeners = new ArrayList<BoardListener>();
 
 	public Board(Player p) {
 		this(p, DEFAULT_BOARD_SIZE);
 	}
 
 	public Board(Player p, int size) {
 		this.size = size;
 		this.owner = p;
 	}
 
 	public int getBoardSize() {
 		return this.size;
 	}
 
 	/**
 	 * Function to place a missile on the opponents board
 	 * 
 	 * @param m
 	 */
 	public void placeMissile(Missile m) {
 
 		// This operation can only be made on the opponents board
 		if (owner == Game.getInstance().getOpponent()) {
 			// Check if its the players turn
 			if (Game.getInstance().getLocalPlayer().getPlayerState() == PlayerState.PLAYING) {
 
 				// Check if coordinates of missile were already used
 				for (Missile placed : placedMissiles) {
 					if (placed.getCoordinates().equals(m.getCoordinates())) {
 						throw new RuntimeException(
 								"Missile coordinates were already used!");
 					}
 				}
 				placedMissiles.add(m);
 				owner.sendMissile(m);
 			} else {
 				throw new RuntimeException("Player" + owner + " is in state "
 						+ owner.getPlayerState()
 						+ ", cannot place missile just yet!");
 			}
		}
		// Notify listeners
		for (BoardListener l : listeners) {
			l.stateChanged(m);
 		}
 	}
 
 	public void updateMissile(Missile m) {
 		for (Missile placed : placedMissiles) {
 			if (placed.getCoordinates().equals(m.getCoordinates())) {
 				placed.setMissileState(m.getMissileState());
 
 				// Notify listeners
 				for (BoardListener l : listeners) {
 					l.stateChanged(m);
 				}
 				return;
 			}
 		}
 		throw new RuntimeException("Missile " + m
 				+ " not found on board! placedMissiles: " + placedMissiles);
 	}
 
 	public ArrayList<Ship> getPlacedShips() {
 		return this.placedShips;
 	}
 
 	public ArrayList<Missile> getPlacedMissiles() {
 		return this.placedMissiles;
 	}
 
 	public boolean withinBoard(Coordinates c) {
 		return ((c.x <= size) && (c.y <= size)) && ((c.x > 0) && (c.y > 0));
 	}
 
 	public boolean checkAllShipsSunk() {
 		for (Ship s : getPlacedShips()) {
 			if (!s.isSunk()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public BoardSetup getBoardSetup() {
 		if (this.setup == null) {
 			throw new RuntimeException(
 					"You called done(), so no more BoardSetup for you!");
 		}
 		return this.setup;
 	}
 
 	public class BoardSetup {
 		private BoardSetup() {
 
 		}
 
 		public void moveShip(Ship s, Coordinates newStartCoordinates,
 				Direction d) {
 			s.setCoordinates(this, newStartCoordinates, d);
 		}
 
 		public void moveShip(Ship s, Coordinates newStartCoordinates,
 				Coordinates newEndCoordinates) {
 			s.setCoordinates(this, newStartCoordinates, newEndCoordinates);
 		}
 
 		public void placeShip(Ship s) {
 
 			// Check boundaries of board
 			if (withinBoard(s.getStartCoordinates())
 					&& withinBoard(s.getEndCoordinates())) {
 
 				// Check if coordinates are already used
 				for (Ship placed : placedShips) {
 					for (Coordinates d : s.getExtrapolatedCoordinates()) {
 						for (Coordinates c : placed.getCoordinatesForShip()) {
 							if (c.equals(d)) {
 								throw new RuntimeException(
 										"Conflicting coordinates for ship "
 												+ placed + " and " + s);
 							}
 						}
 					}
 				}
 
 				placedShips.add(s);
 			} else {
 				throw new RuntimeException("Coordinates not within board!");
 			}
 		}
 
 		public void done() {
 			setup = null;
 		}
 	}
 
 	public void addBoardListener(BoardListener bl) {
 		listeners.add(bl);
 	}
 
 	/**
 	 * Enumeration for direction. This direction can be used to place ships
 	 * 
 	 * @author simon
 	 * 
 	 */
 	public static enum Direction {
 		NORTH, SOUTH, WEST, EAST
 	}
 }
