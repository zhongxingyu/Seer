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
 
 	private int size;
 
 	private Player owner;
 
 	private ArrayList<Ship> placedShips = new ArrayList<Ship>();
 	private ArrayList<Missile> placedMissiles = new ArrayList<Missile>();
 
 	private BoardSetup setup = new BoardSetup();
 
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
				owner.placeMissile(m);
 				System.out.println("placeMissile() called with " + m);
 				// TODO: Notify oberserver pattern
 
 			} else {
 				throw new RuntimeException("Player" + owner + " is in state "
 						+ owner.getPlayerState()
 						+ ", cannot place missile just yet!");
 			}
 		} else {
 			throw new RuntimeException(
 					"placeMissile() can only be called on the opponents board!");
 		}
 	}
 
 	public void updateMissile(Missile m) {
 		for (Missile placed : placedMissiles) {
 			if (placed.getCoordinates().equals(m.getCoordinates())) {
 				placed.setMissileState(m.getMissileState());
 				
 				System.out.println("Missile UPDATED! Missile: "+ m);
 				// TODO: Notify observer pattern
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
 		return this.setup;
 	}
 
 	public class BoardSetup {
 		private BoardSetup() {
 
 		}
 
 		public void moveShip(Ship s, Coordinates newStartCoordinates,
 				Direction d) {
 
 		}
 
 		public void moveShip(Ship s, Coordinates newStartCoordinates,
 				Coordinates newEndCoordinates) {
 
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
 
 	/**
 	 * Class to store coordinates (X and Y)
 	 * 
 	 * @author simon
 	 * 
 	 */
 	public static class Coordinates {
 
 		/**
 		 * X coordinates
 		 */
 		public int x;
 
 		/**
 		 * Y coordinates
 		 */
 		public int y;
 
 		public Coordinates(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public String toString() {
 			// Stolen from here:
 			// http://stackoverflow.com/questions/10813154/converting-number-to-letter
 			String alpha = x > 0 && x < 27 ? String
 					.valueOf((char) (x + 'A' - 1)) : null;
 			return alpha + y;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Coordinates other = (Coordinates) obj;
 			if (x != other.x)
 				return false;
 			if (y != other.y)
 				return false;
 			return true;
 		}
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
