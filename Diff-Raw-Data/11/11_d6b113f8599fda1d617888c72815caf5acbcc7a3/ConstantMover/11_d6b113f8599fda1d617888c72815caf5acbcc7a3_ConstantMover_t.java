 package indaprojekt;
 
 import java.awt.geom.Rectangle2D;
 
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public abstract class ConstantMover extends Mover
 {
 	protected float dx, dy;
 	protected float friction;
 
 	public ConstantMover(float x, float y, Rectangle2D.Float hitBox, float initialDX, float initialDY,
 						 float friction) 
 	{
 		super(x, y, hitBox);
 		dx = initialDX;
 		dy = initialDY;
 		this.friction = friction;
 	}
 
 	@Override
 	public void doLogic(Input input, int delta) throws SlickException
 	{
		if(!(dx  == 0 && dy == 0)) {
 			move(dx*delta, dy*delta);
		}
 		//OBS!! should change according to delta
 		dx *= friction;
 		dy *= friction;
 	}
 
 }
