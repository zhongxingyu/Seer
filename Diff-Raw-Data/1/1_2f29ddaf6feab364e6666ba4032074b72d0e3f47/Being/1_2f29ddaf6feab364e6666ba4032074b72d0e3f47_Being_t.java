 package entities;
 
 import interfaces.Constants;
 import interfaces.PlayerImages;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import javax.swing.JPanel;
 
 import enums.BlockIDString;
 
 public class Being implements Constants, PlayerImages {
 
 	// Constants
 	final double PLAYER_FRAME_DELAY = 0.2;
 	// How close does the player need to be to the block?
 	private final int MAX_PICK_DISTANCE = 4;
 	/*--Variables--*/
 	private boolean left, right, up, down;
 	private boolean isHoldingObject = false;
 	private int x, y, dx;
 	private int xSpeed = 4;
 	private int ySpeed = 6;
 	private int dy = ySpeed;
 	private int jumpMulti = 6; // Jump height == jumpMulti * -ySpeed
 	private double frameNumber = 0;
 	private Block[][] currMap;
 	// TODO Held "Object" Dont limit to blocks.
 	private Block heldBlock;
 	/*--End Variables--*/
 
 	// Default Constructor
 	public Being(Point spawnPoint, Block[][] currMap) {
 		this.x = spawnPoint.x * TILE_SIZE;
 		this.y = spawnPoint.y * TILE_SIZE;
 		this.currMap = currMap;
 	}
 
 	public boolean isHoldingObject() {
 		return isHoldingObject;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public int getDx() {
 		return dx;
 	}
 
 	public int getDy() {
 		return dy;
 	}
 
 	public int getxSpeed() {
 		return xSpeed;
 	}
 
 	public int getySpeed() {
 		return ySpeed;
 	}
 
 	public double getFrameNumber() {
 		return frameNumber;
 	}
 
 	public void setCurrMap(Block[][] currMap) {
 		this.currMap = currMap;
 	}
 
 	public boolean isLeft() {
 		return left;
 	}
 
 	// TODO Change the way dx and dy are set.
 	public void setLeft(boolean left) {
 		if (left == true) {
 			dx = -xSpeed;
 		} else if (!right) {
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
 		} else if (!left) {
 			dx = 0;
 		}
 		this.right = right;
 	}
 
 	public boolean isUp() {
 		return up;
 	}
 
 	public void setUp(boolean up) {
 		if (up == true) {
 			dy = jumpMulti * -ySpeed;
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
 
 	public boolean isAirborne() {
 		if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid) {
 			return false;
 		}
 		return true;
 	}
 
 	public void draw(Graphics2D g2d, JPanel rootPane, Block[][] map) {
 		frameNumber += PLAYER_FRAME_DELAY;
 		if (((int) frameNumber) > 3 && !left && !right) {
 			frameNumber = 0;
 		} else if (left || right) {
 			if (isHoldingObject) {
 				if ((int) frameNumber > 1) {
 					frameNumber = 0;
 				}
 			} else if (((int) frameNumber) > 4) {
 				frameNumber = 0;
 			}
 		}
 		//FIXME Drawing walkLeftImages doesnt work
 		move(dx, dy);
 		if (left && isHoldingObject) {
 			g2d.drawImage(walkLeftHoldImages[(int) frameNumber], x, y, rootPane);
 		} else if (right && isHoldingObject) {
 			g2d.drawImage(walkRightHoldImages[(int) frameNumber], x, y, rootPane);
 		} else if (left) {
 			g2d.drawImage(walkLeftImages[(int) frameNumber], x, y, rootPane);
 		} else if (right) {
 			g2d.drawImage(walkRightImages[(int) frameNumber], x, y, rootPane);
 		} else { // Standing, jumping, or falling.
 			g2d.drawImage(standImages[(int) frameNumber], x, y, rootPane);
 		}
 		// If we are holding a block draw it.
 		if (isHoldingObject) {
 			// Draw the block above the player
 			g2d.drawImage(heldBlock.img, x, y - TILE_SIZE, rootPane);
 		}
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
 
 	// TODO Implement things like sand (things that collapse)
 	public void pickUpObject() {
 		if (!isHoldingObject) {
 			if (left) { // Facing Left
 				if (x % TILE_SIZE <= MAX_PICK_DISTANCE) {
 					if (currMap[getXMapIndex() - 1][getYMapIndex()].isPickable) {
 						isHoldingObject = true;
 						// Set the players held block to this block
 						heldBlock = new Block(
 								currMap[getXMapIndex() - 1][getYMapIndex()].getTypeID());
 						// Change the block the player picked to sky.
 						currMap[getXMapIndex() - 1][getYMapIndex()].changeType(BlockIDString.SKY);
 					}
 				}
 			} else if (right) { // Facing Right
 				if ((x + TILE_SIZE) % TILE_SIZE <= TILE_SIZE - MAX_PICK_DISTANCE) {
 					if (currMap[getXMapIndex() + 1][getYMapIndex()].isPickable) {
 						isHoldingObject = true;
 						// Set the players held block to this block
 						heldBlock = new Block(
 								currMap[getXMapIndex() + 1][getYMapIndex()].getTypeID());
 						// Change the block the player picked to sky.
 						currMap[getXMapIndex() + 1][getYMapIndex()].changeType(BlockIDString.SKY);
 					}
 				}
 			}
 		} else if (isHoldingObject) {
 		}
 	}
 
 	private int jump(int yMove) {
 		setUp(false);
 		// Are we below two blocks?
 		if (x % TILE_SIZE != 0) {
 			// Are either solid?
 			try {
 				if (currMap[getXMapIndex()][getYMapIndex() - 1].isSolid
 						|| currMap[getXMapIndex() + 1][getYMapIndex() - 1].isSolid) {
 					yMove = 0;
 				} else { // Neither are solid
 					y += yMove / Math.abs(yMove);
 					yMove -= yMove / Math.abs(yMove);
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				if (getYMapIndex() - 1 < 0 && y >= 0) {
 					y += yMove / Math.abs(yMove);
 					yMove -= yMove / Math.abs(yMove);
 				} else {
 					yMove = 0;
 				}
 			}
 		} else { // below only one block.
 			try {
 				if (currMap[getXMapIndex()][getYMapIndex() - 1].isSolid) {
 					yMove = 0;
 				} else { // Block is not solid.
 					y += yMove / Math.abs(yMove);
 					yMove -= yMove / Math.abs(yMove);
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				if (getYMapIndex() - 1 < 0 && y >= 0) {
 					y += yMove / Math.abs(yMove);
 					yMove -= yMove / Math.abs(yMove);
 				} else {
 					yMove = 0;
 				}
 			}
 		}
 		if (yMove != 0) {
 			jump(yMove);
 		} else {
 			return yMove;
 		}
 		return 0;
 	}
 
 	// FIXME Character gets stuck when hitting left border, handle x == -1
 	// FIXME Character on the right side of screen causes
 	// ArrayIndexOOBExceptions
 	public void move(int xMove, int yMove) {
 		// Are we moving up or down?
 		if (yMove != 0) { // Move up and down
 			/*----DOWN----*/
 			if (yMove > 0) {
 				// Are we above two blocks?
 				if (x % TILE_SIZE != 0) {
 					// Are either solid?
 					if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid
 							|| currMap[getXMapIndex() + 1][getYMapIndex() + 1].isSolid
 							&& getYMapIndex() + 1 <= NUM_CHUNKS) {
 						yMove = 0;
 					} else { // Neither are solid
 						y += yMove / Math.abs(yMove);
 						yMove -= yMove / Math.abs(yMove);
 					}
 				} else { // Above only one block.
 					if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid
 							&& getYMapIndex() + 1 <= NUM_CHUNKS) {
 						yMove = 0;
 					} else { // Block is not solid.
 						y += yMove / Math.abs(yMove);
 						yMove -= yMove / Math.abs(yMove);
 					}
 				}
 				/*----UP----*/
 			} else if (yMove < 0) { // Up
 				yMove = jump(yMove);
 				// setUp(false);
 				yMove = 0;
 			}
 		}
 		// Moving Left or Right
 		if (xMove != 0) {
 			// Checking left and right border.
 			if (x >= 0 && x <= (CHUNK_SIZE - 1) * TILE_SIZE) {
 				/*----RIGHT----*/
 				if (xMove > 0) {
 					if (getXMapIndex() + 1 < CHUNK_SIZE
 							&& currMap[getXMapIndex() + 1][getYMapIndex()].isSolid) {
 						xMove = 0;
 					} else { // Neither are solid
 						x += xMove / Math.abs(xMove);
 						xMove -= xMove / Math.abs(xMove);
 					}
 					/*----LEFT----*/
 				} else if (xMove < 0) {
 					if (getXMapIndex() - 1 >= 0 && currMap[getXMapIndex()][getYMapIndex()].isSolid) {
 						xMove = 0;
 						// Fixes bug that allowed player to climb up a block if
 						// pushed up against it.
 						if (x % TILE_SIZE == TILE_SIZE - 1) {
 							x++; // Move player right one pixel
 						}
 					} else { // Neither are solid
 						x += xMove / Math.abs(xMove);
 						xMove -= xMove / Math.abs(xMove);
 					}
 				}
 			} else { // Hitting Border
 				// TODO Implement a new map when walking far right/left
 				xMove = 0; // Stop moving left or right.
 			}
 		}
 		// Are we still moving?
 		if (xMove != 0 || yMove != 0) {
 			move(xMove, yMove);
 		} else {
 			// Temporary fix for bug where player would get stuck at
 			// the left border
 			if (x == -1) {
 				x++;
 			}
 			return; // Done Moving.
 		}
 	}
 }
