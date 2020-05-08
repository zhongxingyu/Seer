 package entities;
 
import enums.BeingCollision;
import gui.NewMap;
 import interfaces.Constants;
 import interfaces.PlayerImages;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import javax.swing.JPanel;
 
 public class NewBeing implements Constants, PlayerImages {
 
 	// Constants
 	final double PLAYER_FRAME_DELAY = 0.2;
 	/*--Variables--*/
 	private boolean left, right, up, down, pick;
 	private int x, y, dx, dy;
 	private int chunkX, chunkY;
 	private int xSpeed = 4;
 	private int ySpeed = 8;
 	private int jumpHeight = 6;
 	private boolean isAirborne = false;
 	private double frameNumber = 0;
 	private Block[][] currMap;
 	/*--End Variables--*/
 
 	// Default Constructor
 	public NewBeing(Point spawnPoint, Block[][] currMap) {
 		this.x = spawnPoint.x * TILE_SIZE;
 		this.y = spawnPoint.y * TILE_SIZE;
 		this.currMap = currMap;
 	}
 
 	public void setCurrMap(Block[][] currMap) {
 		this.currMap = currMap;
 	}
 
 	public boolean isLeft() {
 		return left;
 	}
 
 	public void setLeft(boolean left) {
 		if (left == true) {
 			dx = -xSpeed;
 		} else {
 			dx = 0;
 		}
 		this.left = left;
 	}
 
 	public boolean isRight() {
 		return right;
 	}
 
 	public void setRight(boolean right) {
 		if (right == true) {
 			dx = xSpeed;
 		} else {
 			dx = 0;
 		}
 		this.right = right;
 	}
 
 	public boolean isUp() {
 		return up;
 	}
 
 	public void setUp(boolean up) {
 		if (up == true) {
 			dy = -ySpeed;
 		} else {
 			dy = ySpeed;
 		}
 		this.up = up;
 	}
 
 	public boolean isDown() {
 		return down;
 	}
 
 	public void setDown(boolean down) {
 		this.down = down;
 	}
 
 	public boolean isPick() {
 		return pick;
 	}
 
 	public void setPick(boolean pick) {
 		this.pick = pick;
 	}
 
 	public void draw(Graphics2D g2d, JPanel rootPane, Block[][] map) {
 		frameNumber += PLAYER_FRAME_DELAY;
 		if (((int) frameNumber) > 3 && !left && !right) {
 			frameNumber = 0;
 		} else if (left || right) {
 			if (((int) frameNumber) > 4) {
 				frameNumber = 0;
 			}
 		}
 		if (left) {
 			g2d.drawImage(walkLeftImages[(int) frameNumber], x, y, rootPane);
 		} else if (right) {
 			g2d.drawImage(walkRightImages[(int) frameNumber], x, y, rootPane);
 		} else { // Standing, jumping, or falling.
 			g2d.drawImage(standImages[(int) frameNumber], x, y, rootPane);
 		}
 		newMove(dx, dy);
 		// move(map);
 	}
 	/**
 	 * @return the "x" index in the 2D map array.
 	 */
 	public int getXMapIndex() {
 		return x / TILE_SIZE;
 	}
 
 	/**
 	 * @return the "y" index in the 2D map array.
 	 */
 	public int getYMapIndex() {
 		return y / TILE_SIZE;
 	}
 
 	// Holy fuck confusing
 	public void newMove(int xMove, int yMove) {
 		// Notes, if the player passes bounds check while dx == positive ySpeed
 		// then isAirborne == true.
 
 		// Are we still moving?
 		if (xMove != 0 || yMove != 0) {
 			// ----MOVING DOWN
 			if (yMove > 0) {
 				if (xMove != 0) { // Moving down and left/right
 
 					if (xMove > 0) { // Down and Left
 						try {
 							if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid
 									|| currMap[getXMapIndex() - 1][getYMapIndex()].isSolid) {
 								yMove = 0;
 							} else { // No Solid
 								// Move towards 0 wheter pos or neg.
 								// ie -27/abs(-27) -1 vs. -27/-27 = 1;
 								xMove -= (xMove / Math.abs(xMove));
 								x += (xMove / Math.abs(xMove));
 								yMove -= (yMove / Math.abs(yMove));
 								y += (yMove / Math.abs(yMove));
 							}
 						} catch (NullPointerException e) {
 							// Hitting left border
 							if (x < 0) {
 								xMove = 0;
 							}
 						}
 					} else if (xMove < 0) { // Down and Right
 
 					}
 				} else { // Only Down
 					// if the block below is solid STOP
 					if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid) {
 						yMove = 0;
 					} else { // No Solid
 						// Move towards 0 wheter pos or neg.
 						// ie -27/abs(-27) -1 vs. -27/-27 = 1;
 						yMove -= (yMove / Math.abs(yMove));
 						y += (yMove / Math.abs(yMove));
 					}
 				}
 				// ----MOVING UP
 			} else if (yMove < 0) {
 				if (xMove != 0) {// Moving up and left/right
 
 				} else { // Only up
 					if (currMap[getXMapIndex()][getYMapIndex() - 1].isSolid) {
 						yMove = 0;
 					} else { // No Solid
 						// Move towards 0 wheter pos or neg.
 						// ie -27/abs(-27) -1 vs. -27/-27 = 1;
 						yMove -= (yMove / Math.abs(yMove));
 						y += (yMove / Math.abs(yMove));
 					}
 				}
 				// ----MOVING RIGHT ONLY
 			} else if (xMove > 0) {
 				try {
 					if (currMap[getXMapIndex() + 1][getYMapIndex()].isSolid) {
 						xMove = 0;
 					} else { // No Solid
 						// Move towards 0 wheter pos or neg.
 						// ie -27/abs(-27) -1 vs. -27/-27 = 1;
 						xMove -= (xMove / Math.abs(xMove));
 						x += (xMove / Math.abs(xMove));
 					}
 				} catch (NullPointerException e) {
 					// Handle hitting right border
 					if (y + TILE_SIZE >= CHUNK_SIZE * TILE_SIZE) {
 						xMove = 0;
 					}
 				}
 				// ----MOVING LEFT ONLY
 			} else if (xMove < 0) {
 				try {
 					if (currMap[getXMapIndex() - 1][getYMapIndex()].isSolid) {
 						xMove = 0;
 					} else { // No Solid
 						// Move towards 0 wheter pos or neg.
 						// ie -27/abs(-27) -1 vs. -27/-27 = 1;
 						xMove -= (xMove / Math.abs(xMove));
 						x += (xMove / Math.abs(xMove));
 					}
 				} catch (NullPointerException e) {
 					// Handle hitting left border
 					if (x <= 0) {
 						xMove = 0;
 					}
 				}
 
 			} else if (xMove == 0 && yMove == 0) { // Are we finished
 				return;
 			} else { // Not done moving call again!
 				newMove(xMove, yMove);
 			}
 		}
 	}
 
 	// public void move(Block[][] map) {
 	// // bounds check
 	// if (up) {
 	// if (map[chunkX][chunkY - 1].isSolid) {
 	// return;
 	// }
 	// } else if (left) {
 	// if (map[chunkX - 1][chunkY].isSolid) {
 	// return;
 	// }
 	// } else if (right) {
 	// if (map[chunkX + 1][chunkY].isSolid) {
 	// return;
 	// }
 	// } else {
 	// if (map[chunkX][chunkY + 1].isSolid) {
 	// return;
 	// }
 	// }
 	// if (up) {
 	// dy = -ySpeed;
 	// } else if (left) {
 	// dx = -xSpeed;
 	// } else if (right) {
 	// dx = xSpeed;
 	// } else {
 	// dx = 0;
 	// dy = ySpeed;
 	// }
 	// // move
 	// x += dx;
 	// y += dy;
 	// // Location in relation to the map array [y][x]
 	// chunkX = x / TILE_SIZE;
 	// chunkY = y / TILE_SIZE;
 	// }
 }
