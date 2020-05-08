 /**
  * 
  */
 package unsw.comp9024.Lock;
 
 /**
  * @author Alexander Whillas (z3446737) <whillas@gmail.com>
  *
  */
 public class Lock {
 
 	private static final int wheelCount = 3;
 	
 	private DriveCam cam;
 	
 	private LockWheel[] wheels;
 	
 	public Lock (int rearPin, int rearFly, int midPin, int midFly, int frontPin, int frontFly) {
 		// Start at the bottom and chain the previous wheel to the one bellow it.
 		wheels[3] = new Wheel(frontPin, frontFly, null);
 		wheels[2] = new Wheel(midPin, midFly, wheels[3]);
 		wheels[1] = new Wheel(rearPin, rearFly, wheels[2]);
 		wheels[0] = new DriveCam(wheels[1]);
 	}
 	
 	/**
 	 * Checks that all the wheels are in the 0 position.
 	 * @return
 	 */
 	public boolean isLocked() {
		for(int i = 0; i < this.wheelCount; i++) {
 			if (this.wheels[i].getNotch() != 0)
 				return false;
 		}
 		return true;
 	}
 	
 	public void turnClockwise(int units) {
 		turn(units);
 	}
 	
 	public void turnAntiClockwise(int units) {
 		turn(-units);
 	}
 	
 	private void turn(int amount) {
 		cam.rotate(amount);
 	}
 }
