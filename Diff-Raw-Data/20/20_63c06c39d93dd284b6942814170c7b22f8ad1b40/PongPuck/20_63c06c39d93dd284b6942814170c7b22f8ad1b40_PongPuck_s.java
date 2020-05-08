 package pong;
 
 import java.util.Random;
 
 import jgame.Context;
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.ImageCache;
 import jgame.SoundManager;
 import jgame.controller.ConstantMovementController;
 import jgame.listener.BoundaryRemovalListener;
 import jgame.listener.HitTestListener;
 import jgame.listener.ParentBoundsListener;
 
 public class PongPuck extends GSprite {
 	ConstantMovementController cmc;
 
 	public PongPuck() {
 		super(ImageCache.forClass(Pong.class).get("puck.png"));
 		cmc = new ConstantMovementController(
 		           new Random().nextBoolean() ? 5 : -5, 0);	
 		addController(cmc);
 		addListener(new BoundaryRemovalListener());
 		setPrimitive(PrimitiveShape.CIRCLE);
 		
 
 		ParentBoundsListener bounce = new ParentBoundsListener() {
 			@Override
 			public void invoke(GObject target, Context context) {
 				cmc.setVelocityY(-cmc.getVelocityY());
 				double vx = cmc.getVelocityX();
 
 				// Test the sign.
 				if (vx > 0) {
 				    vx += 0.01;
 				} else if (vx < 0) {
 				    vx -= 0.01;
 				} else {
 				    // It's zero; do nothing.
 				}
 
 				// Set the velocity.
 				cmc.setVelocityX(vx);
 			}
 		};
 
 		bounce.setValidateHorizontal(false);
 		addListener(bounce);
 	
 		HitTestListener htl = new HitTestListener(PongPaddle.class) {
 			@Override
 			public void invoke(GObject target, Context context) {
 				flip();
				PongPaddle paddle = context.hitTestClass(PongPaddle.class).get(0);
 				double offset = getY() - paddle.getY();
				cmc.setVelocityY(cmc.getVelocityY() + offset * 0.1);
 			}
 		};
 		addListener(htl);
 		HitTestListener htlTwo = new HitTestListener(PongPaddleTwo.class) {
 			
 			@Override
 			public void invoke(GObject target, Context context) {
 				
 				flip();
 				PongPaddleTwo paddle = context.hitTestClass(PongPaddleTwo.class).get(0);
 				double offset = getY() - paddle.getY();
				cmc.setVelocityY(cmc.getVelocityY() + offset * 0.1);
 			}
 		};
 		addListener(htlTwo);
 	}
 
 	public void flip() {
 		double vx = cmc.getVelocityX();
 		vx = -vx;
 		cmc.setVelocityX(vx);
 	}
 
 }
