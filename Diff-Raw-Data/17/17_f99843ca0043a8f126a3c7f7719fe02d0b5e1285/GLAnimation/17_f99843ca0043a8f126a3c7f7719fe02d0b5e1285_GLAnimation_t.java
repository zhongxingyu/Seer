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
 public class GLAnimation implements GLRenderAble {
 
 	private final double moveRotation;
 	private final double endRotation;
 	private double rotation;
 	
 	private GLSprite sprite;
 	private Rectangle rect;
 	
 	private int maxTime;
 	private long startTime;
 	
 	private int startX;
 	private int startY;
 	private int squareSize;
 	private int dX, dY;
 	
 	public GLAnimation(GLSprite sprite, Rectangle startRect, int maxTime, int squareSize, Position[] steps) {
 		this.sprite = sprite;
 		this.rect = new Rectangle(startRect);
 		this.maxTime = maxTime;
 		this.squareSize = squareSize;
 		this.startX = rect.getX();
 		this.startY = rect.getY();
		moveRotation = Path.getRotation(null, steps[0], steps[1]) - 90.0;
		endRotation = Path.getRotation(null, steps[1], steps[2]) - 90.0;
		rotation = moveRotation;
		dX = steps[1].getCol() - steps[0].getCol();
		dY = steps[0].getRow() - steps[1].getRow();
 	}
 	
 	private void update() {
 		if (startTime == 0) {
 			startTime = System.currentTimeMillis();
 		}
		float pDone = ((float) (System.currentTimeMillis() - startTime)) / maxTime;
 		if (pDone > 0.85f) {
 			rotation = moveRotation + (moveRotation - endRotation)*((pDone - 0.85f)/0.15f);
 		} else {
 			float mDone = pDone / 0.85f;
 			rect.setX(startX + (int) (dX*squareSize*mDone));
 			rect.setY(startY + (int) (dY*squareSize*mDone));
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
 		sprite.draw(drawable, rect, targetArea, zIndex);
 		sprite.setRotation(0);
 	}
 }
