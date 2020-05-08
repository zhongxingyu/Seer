 package simulation;
 
 import java.awt.Color;
 import java.awt.Dimension;
 
 /**
  * XXX.
  * 
  * @author Ross Cahoon
  */
 
 public class FixedMass extends Mass {
 	private static int DEFAULT_MASS = 0;
 	
 	public FixedMass(double x, double y) {
         super(x,y,DEFAULT_MASS);
 	}
     @Override
     public void update (double elapsedTime, Dimension bounds) {
    	setVelocity(0,0);
        super.update(elapsedTime, bounds);
     }
 
 }
