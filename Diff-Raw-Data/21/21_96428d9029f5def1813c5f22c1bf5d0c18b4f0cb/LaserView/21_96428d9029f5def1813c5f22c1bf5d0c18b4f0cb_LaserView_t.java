 package se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard;
 
 import se.chalmers.dryleafsoftware.androidrally.sharred.MapBuilder;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 /**
  * This view class only displays the laser beam. For proper collision when drawing, use
  * <code>setCollisionMatrix()</code>.
  * 
  * @author
  *
  */
 public class LaserView extends AnimatedImage {
 	
 	private boolean[][][] collisionMatrix;
 	private int length;
 	private boolean hasCalc = false;
 	
 	/**
 	 * Creates a new laser which will use the specified texture.
 	 * @param texture The texture of the laser beam.
 	 * @param x The X-coordinate. (In array).
 	 * @param y The Y-coordinate. (In array).
 	 * @param dir The direction to shot.
 	 */
 	public LaserView(TextureRegion texture, int dir) {
 		super(texture);
 		setRotation(-dir * 90);
 		setVisible(false);
 		setPhaseMask(5);
 	}
 	
 	/**
 	 * Set to use proper collision.
 	 * @param collisionMatrix Array of booleans describing collision.
 	 */
 	public void setCollisionMatrix(boolean[][][] collisionMatrix) {
 		this.collisionMatrix = collisionMatrix;
 		hasCalc = false;
 	}
 	
 	@Override
 	public void enable(int phase) {
 		super.enable(phase);
 		setVisible(isEnabled());
 	}
 	
 	@Override
 	public void disable() {
 		super.disable();
 		setVisible(false);
 	}
 
 	@Override
 	public void animate(float timeDelta) {
 		if(!hasCalc) {
 			int x = (int) (getX() / 40);
 			int y = 15 - (int) (getY() / 40);
 			switch(((int)(getRotation() / -90) + 4) % 4) {
 			case MapBuilder.DIR_NORTH:
 				length = 40 * (y+1);
 				for(int i = 0; i <= y; i++) {
 					if(collisionMatrix[x][y - i][0]) {
 						length = 40 * (i+1);
 						break;
 					}
 				}
 				break;
 			case MapBuilder.DIR_SOUTH:
 				length = 40 * (16-y);
				for(int i = y + 1; i < 16; i++) {
 					if(collisionMatrix[x][i][0]) {
 						length = 40 * (i-y);
 						break;
 					}
 				}
 				break;
 			case MapBuilder.DIR_WEST:
 				length = 40 * (x+1);
 				for(int i = 0; i <= x; i++) {
 					if(collisionMatrix[x - i][y][1]) {
 						length = 40 * (i+1);
 						break;
 					}
 				}
 				break;
 			case MapBuilder.DIR_EAST:
 				length = 40 * (12-x);
				for(int i = x + 1; i < 12; i++) {
 					if(collisionMatrix[i][y][1]) {
 						length = 40 * (i-x);
 						break;
 					}
 				}
 				break;
 			}
 			setHeight(length);
 			layout();
 			hasCalc = true;
 		}
 	}
 }
