 package riskyspace.view.opengl.impl;
 
 import javax.media.opengl.GLAutoDrawable;
 
 import riskyspace.logic.Path;
 import riskyspace.model.Position;
 import riskyspace.view.opengl.GLRenderAble;
 import riskyspace.view.opengl.Rectangle;
 
 /**
  * Animation that rotates moves and rotates on GLSprite to an adjacent Position
  * @author Alexander Hederstaf
  *
  */
 public class GLFleetAnimation implements GLRenderAble {
 
 	private final double moveRotation;
 	private final double endRotation;
 	private final double dRotation;
 	private double rotation;
 	
 	private GLSprite sprite;
 	
 	private GLSprite[] flames;
 	private GLSprite flame;
 	
 	private Rectangle rect;
 	
 	private int maxTime;
 	private long startTime;
 	
 	private int startX;
 	private int startY;
 	private int squareSize;
 	private int dX, dY;
 	
 	public GLFleetAnimation(GLSprite sprite, GLSprite[] flames, Rectangle startRect, int maxTime, int squareSize, Position[] steps) {
 		this.sprite = sprite;
 		this.flames = flames;
 		this.rect = new Rectangle(startRect);
 		this.maxTime = maxTime;
 		this.squareSize = squareSize;
 		this.startX = rect.getX();
 		this.startY = rect.getY();
 		double mR = Math.toDegrees(Path.getRotation(null, steps[0], steps[1]));
 		double eR;
 		if (steps[2] != null) {
 			eR = Math.toDegrees(Path.getRotation(null, steps[1], steps[2]));
 		} else {
 			eR = 0;
 		}
 		moveRotation = mR % 360;
 		endRotation = eR % 360;
 		dRotation = getDeltaRotation(moveRotation, endRotation);
 		rotation = moveRotation;
 		dX = steps[1].getCol() - steps[0].getCol();
 		dY = steps[0].getRow() - steps[1].getRow();
 	}
 	
 	/**
 	 * Get the angle in degrees between to angles.
 	 * Positive value for clockwise rotation
 	 * Negative value for counter-clockwise rotation
 	 * @return double value x; (-180 < x < 180)
 	 */
 	private double getDeltaRotation(double from, double to) {
 		/*
 		 * Angle between from and to
 		 */
 		double a = Math.abs((from - to) % 360);
 		if (a > 180) {
 			a -= 180;
 		}
 		if (a % 180 != 0) {
 			if (from % 180 == 0) {
 				a = (Math.cos(Math.toRadians(from)) * Math.sin(Math.toRadians(to))) * a;
 			} else if (from % 90 == 0) {
 				a = -(Math.cos(Math.toRadians(to)) * Math.sin(Math.toRadians(from))) * a;
 			}
 		}
 		return a;
 	}
 
 	private void update() {
 		if (startTime == 0) {
 			startTime = System.currentTimeMillis();
 		}
 		float pDone = ((float) (System.currentTimeMillis() - startTime)) / maxTime;
 		if (moveRotation != endRotation) {
 			if (pDone > 0.7f) {
 				rotation = moveRotation + dRotation * ((pDone - 0.7f) / 0.3f);
 			} else {
 				float mDone = pDone / 0.7f;
 				rect.setX(startX + (int) (dX * squareSize * mDone));
 				rect.setY(startY + (int) (dY * squareSize * mDone));
 			}
 		} else {
 			rect.setX(startX + (int) (dX * squareSize * pDone));
 			rect.setY(startY + (int) (dY * squareSize * pDone));
 		}
 		if (flames != null && flames.length > 0) {
			int index = (maxTime/40) % (flames.length + 2);
 			if (index == flames.length) {
 				index = index - 2;
 			} else if (index == flames.length + 1) {
 				index = index - 4;
 			}
 			flame = flames[index];
 		}
 	}
 	
 	@Override
 	public Rectangle getBounds() {
 		return rect;
 	}
 	
 	@Override
 	public void draw(GLAutoDrawable drawable, Rectangle objectRect,	Rectangle targetArea, int zIndex) {
 		update();
 		sprite.setRotation(rotation);
 		sprite.draw(drawable, rect, targetArea, zIndex + 1);
 		if (flame != null) {
 			flame.setRotation(rotation);
 			flame.draw(drawable, rect, targetArea, zIndex);
 			flame.setRotation(0);
 		}
 		sprite.setRotation(0);
 	}
 }
