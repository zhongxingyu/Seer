 package ph.sm.balance;
 
 import android.graphics.Point;
 import ch.aplu.android.Actor;
 
 public class Marble extends Actor{
 	
 	private static final float FACTOR = 0.005f;
 	float xVelocity, yVelocity;
 	float xPos, yPos;
 	private Balance gg;
 	
 	public Marble(Balance gg) {
 		super("marble");
 		this.gg = gg;
 	}
 	
 	public void reset() {
 		xPos = getPixelLocation().x;
 		yPos = getPixelLocation().y;
 		xVelocity = 0;
 		yVelocity = 0;
 	}
 	
 	public void act() {
 		xVelocity += FACTOR*gg.getXSlope();
 		yVelocity += FACTOR*gg.getYSlope();
 		xPos += xVelocity;
 		yPos += yVelocity;
		setPixelLocation(new Point(Math.round(xPos),Math.round(yPos)));
 		if (!isInGrid())
 			gg.gameOver();
 	}
 }
