 package dinaBOT.navigation;
 
 import java.lang.Math;
 
 import lejos.robotics.Encoder;
 import lejos.nxt.LCD;
 
 import dinaBOT.sensor.LineDetector;
 
 /**
  * The ArcOdometer is our odometer implementation. It is based primarly on the theory presented in the tutorial slides prepared by Patrick Diez for the odometer lab. It conforms to the specification of the odometer interface. In addition to that theory, this odometer also implements a "grid snapping" system, which uses line cross events detected by the two lightsensors (located on the left and right of the robot in line with the axel) to compute correction for the x, y, theta position of the robot. This effectivly places an upper bound on the robot's positional error. This should make the robot capable of indefinetly moving around the course without ever increasing error. More detail about the grid snapping algorithm can be found in the project documentation.
  * <p>
  * This odometer implentation assumes a two wheeled robot (whos dimensions are imported from the {@link dinaBOT.mech.MechConstants}) which can rotate in place about the center of it's axel.
  * <p>
  * Angles are in radians, x-y coordinates in cm.
  *
  * @author Gabriel Olteanu, Severin Smith, Vinh Phong Buu
  * @see Odometer
  * @see Navigation
  * @see Movement
  * @version 3
 */
 public class ArcOdometer implements Odometer {
 
 	/* -- Class Variables -- */
 
 	/* Odometer */
 
 	Encoder left_encoder, right_encoder;
 
 	double tacho_left, tacho_right;
 
 	double[] position;
 
 	/* Grid snapping */
 
 	boolean snap_enable;
 
 	int snap_status;
 
 	int snap_tacho_count;
 	LineDetector snap_detector;
 
 	double snap_prev_direction;
 
 	final int snap_saftey = 2;
 
 	/* Debug */
 
 	boolean debug, safe;
 
 	/**
 	 * Creates a new ArcOdometer.
 	 *
 	 * @param left_encoder the tacho encoder for the left wheel of the robot
 	 * @param right_encoder the tacho encoder for the right wheel of the robot
 	*/
 	public ArcOdometer(Encoder left_encoder, Encoder right_encoder) {
 		//Setup the odometer
 		this.left_encoder = left_encoder;
 		this.right_encoder = right_encoder;
 
 		left_encoder.resetTachoCount();
 		right_encoder.resetTachoCount();
 
 		position = new double[3];
 
 		//Register with the line detectors for grid snapping
 		LineDetector.left.registerListener(this);
 		LineDetector.right.registerListener(this);
 		snap_enable = false; //Grid snapping is currently disable by default until it is 100% tested
 
 		//Start the odometer thread last
 		Thread odometer_thread = new Thread(this);
 		odometer_thread.setDaemon(true);
 		odometer_thread.start();
 	}
 
 	/**
 	 * The run method contiuously polls the tacho counter and computes the updated position of the robot as a function of the change in the tacho counts of the left and right wheels
 	 *
 	*/
 	public void run() {
 		while(true) { //Forever
 			//Compute the change in tacho count (in radians)
 			double d_tacho_left = (double)left_encoder.getTachoCount()/360*(2*Math.PI)-tacho_left; 
 			double d_tacho_right = (double)right_encoder.getTachoCount()/360*(2*Math.PI)-tacho_right;
 
 			double dC = (d_tacho_right*WHEEL_RADIUS+d_tacho_left*WHEEL_RADIUS)/2; //Compute the arc length travelled
 			double dTheta = (d_tacho_right*WHEEL_RADIUS-d_tacho_left*WHEEL_RADIUS)/WHEEL_BASE; //Compute the change in angle
 
 			//We are going to recursively modify the position of the robot, we don't want it to be modified while we're doing so
 			//So enter a synchronized block
 			synchronized(this) {
 				position[0] += dC*cos(position[2]+dTheta/2); //Compute the new X position
 				position[1] += dC*sin(position[2]+dTheta/2); //Compute the new Y position
 				position[2] += dTheta; //Compute the new angle
 			}
 
 			//Update the latest left and right tacho counts
 			tacho_left += d_tacho_left;
 			tacho_right += d_tacho_right;
 		}
 	}
 
 	/**
 	 * Returns the current position as a double array with a length of three. It will be filled with the x, y and theta components in that order respectively.
 	 * <p>
 	 * Angles are in radians, x-y coordinates in cm.
 	 * <p>
 	 * Please note that this method is <b>not</b> synchronized by design. There is a tiny 
 	 * risk that the position array will get updated while arraycopy
 	 * is running (this is HIGHLY unlikley given that array copy is
 	 * a native call, not a java call, so it probably executes in a
 	 * single sweep everytime). But the normal updates from the run
 	 * method make such tiny increments to the position array that it
 	 * won't make much difference if a partially updated position array
 	 * is returned . In contrast, if the method was synchronized
 	 * we risk creating a serious bottle neck between this method and
 	 * the run method should this method be repeatedly polled
 	 * (which will probably happen). Such a bottle neck would significantly
 	 * reduce the speed and therefor the accuracy of our odometer.
 	 *
 	 * @return position the array with the current position values
 	 * @see #setPosition(double[] position, boolean[] update)
 	*/
 	public double[] getPosition() {
 		double[] tmp_array = new double[3]; //Create a new array to return
 		try {
 			System.arraycopy(position, 0, tmp_array, 0, 3); //Copy the current position into the array
 		} catch(Exception e) {
 			System.out.println("Exception, ArcOdometer getPosition()");
 		}
 		return tmp_array; //And return
 	}
 
 	/**
 	 * Updates the components of the odometer's position using the values in the <code>position</code> array if and only if the corresponding entry in the masking array <code>update</code> is true.
 	 * <p>
 	 * Angles are in radians, x-y coordinates in cm.
 	 * <p>
 	 * TODO Currently if you are performing a turnTo and you update the position of the odometer simulaneously you can enter an endless loop. This should be addressed
 	 *
 	 * @param position the array of update position values
 	 * @param update the masking array for updating position values
 	 * @see #getPosition()
 	*/
 	public synchronized void setPosition(double[] position, boolean[] update) {
 		try {
 			//Make sure the position and update arrays are of accepatable sizes
 			if(this.position.length == position.length && this.position.length == update.length) {
 				//Update x and y
 				if(update[0]) this.position[0] = position[0];
 				if(update[1]) this.position[1] = position[1];
 				if(update[2]) {
 					//Make the new theta as close as possible to the old theta for continuity
 					while(position[2] < (this.position[2] - Math.PI)) position[2] += 2*Math.PI;
 					while(position[2] > (this.position[2] + Math.PI)) position[2] -= 2*Math.PI;
 					//Update theta
 					this.position[2] = position[2];
 				}
 			}
 		} catch(Exception e) {
 			System.out.println("Exception, ArcOdometer getPosition()");
 		}
 	}
 
 
 	/**
 	 * Accepts notification of a new line cross event and performs grid snapping accordingly.
 	 * <p>
 	 * TODO Should perform small adjustement eg keep the theta in the same range it was previously + limit maximum correction (correct as average of odometer + snapping?)
 	 *
 	 * @param detector indicates the detector which is calling this method
 	*/
 	public synchronized void lineDetected(LineDetector detector) {
		if(!snap_enable) return;
 		if(detector == LineDetector.left) System.out.println("L"+snap_status);
 		else System.out.println("R"+snap_status);
 		
 		//Compute the current heading between [0, 2*PI]
 		double current_heading = position[2]%(Math.PI*2);
 		if(current_heading < 0) current_heading += Math.PI*2;
 
 		//Compute the current direction 0 = pos X, 1 = pos Y, 2 = neg X, 3 = neg Y
 		int current_direction = (int)Math.round(current_heading/(2*Math.PI)*4)%4;
 
 		//If we have rotated to a new quadrant reset the snapping system so we don't carry line crosses from one quadrant into another
 		if(current_direction != snap_prev_direction) snap_status = 0;
 		snap_prev_direction = current_direction;
 
 		//Compute the actual position of the left and right sensor
 		double l_sensor = 0;
 		double r_sensor = 0;
 
 		if(current_direction%2 == 0) { //pos or neg X
 			l_sensor = (cos(position[2])*LIGHT_SENSOR_BASE/2+position[1])%UNIT_TILE;
 			r_sensor = (-cos(position[2])*LIGHT_SENSOR_BASE/2+position[1])%UNIT_TILE;
 		} else { //pos or neg Y
 			l_sensor = (sin(position[2])*LIGHT_SENSOR_BASE/2+position[0])%UNIT_TILE;
 			r_sensor = (-sin(position[2])*LIGHT_SENSOR_BASE/2+position[0])%UNIT_TILE;
 		}
 		//If the sensor are in an unsafe area (where they could make erroneous readings, return
 		if(l_sensor < snap_saftey || l_sensor > UNIT_TILE-snap_saftey || r_sensor < snap_saftey || r_sensor > UNIT_TILE-snap_saftey) {
 			safe = false;
 			return;
 		} else {
 			safe = true;
 		}
 
 		//If we haven't seen a line yet
 		if(snap_status == 0) {
 			snap_status = 1; //Update status to reflect a first line cross
 			snap_detector = detector; //Remember which sensor saw the first line cross
 			//And the tacho count of the cross
 			if(detector == LineDetector.left) snap_tacho_count = left_encoder.getTachoCount();
 			else snap_tacho_count = right_encoder.getTachoCount();
 		} else if(snap_status == 1) { //If we have already seen a line
 			if(snap_detector == detector) { //If it was the same one
 				snap_status = 1; //Make this new cross our first line cross
 				snap_detector = detector; //Remember the detector of our new first cross
 				//And the tacho count of the cross
 				if(detector == LineDetector.left) snap_tacho_count = left_encoder.getTachoCount();
 				else snap_tacho_count = right_encoder.getTachoCount();
 			} else { //If it's a different line
 				//Compute the change in tacho count of the first sensor to cross
 				int dtacho = -snap_tacho_count;
 				if(snap_detector == LineDetector.left) dtacho += left_encoder.getTachoCount();
 				else dtacho += right_encoder.getTachoCount();
 
 				//And compute the real distance travelled
 				double distance_travelled = (double)dtacho/360.0*2.0*Math.PI*WHEEL_RADIUS;
 
 				if(distance_travelled > 15) { //If the distance is unusually large
 					snap_status = 1; //Make this new cross our first line cross
 					snap_detector = detector; //Remember the detector of our new first cross
 					//And the tacho count of the cross
 					if(detector == LineDetector.left) snap_tacho_count = left_encoder.getTachoCount();
 					else snap_tacho_count = right_encoder.getTachoCount();
 				} else { //If the distance is reasonable
 					//Compute the new angle and x or y coordinate and perform the odometer correction
 
 					double theta = 0;
 					double offset_angle = Math.atan(distance_travelled/LIGHT_SENSOR_BASE);
 					if(snap_detector == LineDetector.left) theta = current_direction*Math.PI/2-offset_angle;
 					else theta = current_direction*Math.PI/2+offset_angle;
 
 					if(current_direction%2 == 0) { //pos or neg X
 						double x = Math.round(position[0]/UNIT_TILE)*UNIT_TILE+(distance_travelled-LIGHT_SENSOR_OFFSET)/2*Math.cos(theta);
 						setPosition(new double[] {x, 0, theta}, new boolean[] {true, false, true}); //Use setPosition to avoid synchronization problems
 					} else { //pos or neg Y
 						double y = Math.round(position[1]/UNIT_TILE)*UNIT_TILE+(distance_travelled-LIGHT_SENSOR_OFFSET)/2*Math.sin(theta);
 						setPosition(new double[] {0, y, theta}, new boolean[] {false, true, true}); //Use setPosition to avoid synchronization problems
 					}
 					System.out.println("Corrected");
 					System.out.println("---");
 					snap_status = 0; //Reset our state to 0: no lines seen
 				}
 			}
 		}
 	}
 
 	/**
 	 * Perform initial localization. Must be called adjacent to a corner (two walls).
 	 *
 	*/
 	public void localize() {
 
 	}
 
 	/**
 	 * Enable or disable grid snapping (auto correction of the odometer with grid lines).
 	 *
 	 * @param enable enables grid snapping if set to true, disables it otherwise
 	*/
 	public void enableSnapping(boolean enable) {
 		//snap_status = 0;
 		snap_enable = enable;
 	}
 
 	/**
 	 * Set the state onscreen debug functionality of the odometer. This feature print the current odometer position to the screen of the NXT brick
 	 *
 	 * @param state activates the debuging if and only if state is true, deactivates it otherwise
 	*/
 	public void setDebug(boolean state) {
 		//Toggle the odometer printout thread for debugging
 		if(!debug && state) { //If it's not already running and we want to activate it
 			debug = true; //Set it's state to true
 			//Instantiate a new thread whose target is an annonymous class implementing Runnable
 			Thread debug_thred = new Thread(
 				new Runnable() {
 					public void run() {
 						while(debug) { //As long as the debug state is true
 							LCD.clear(); //Clear the screen
 							//Print X,Y and Theta
 							LCD.drawString("x = "+((Float)(float)position[0]).toString(),0,3);
 							LCD.drawString("y = "+((Float)(float)position[1]).toString(),0,4);
 							LCD.drawString("t = "+((Float)(float)(position[2]%(Math.PI*2)/(Math.PI*2)*360)).toString(),0,5);
 							LCD.drawString("snap = "+snap_enable,0,6);
 							LCD.drawString("safe = "+safe,0,7);
 
 							try {
 								Thread.sleep(500); //Pause for 0.5 seconds
 							} catch(Exception e) {
 
 							}
 						}
 					}
 				}
 			);
 			debug_thred.setDaemon(true); //Configure it as a daemon thread
 			debug_thred.start(); //Start the thread
 		} else { //If we want to deactivate it
 			debug = false; //Set it's state to false
 			//This will cause the run method of any running debug threads instantiated by this instance of ArcOdometer to return
 		}
 	}
 
 	/**
 	 * Return the cos of an angle in radians.
 	 *
 	*/
 	double cos(double angle) {
 		return Math.cos(angle);
 	}
 
 	/**
 	 * Return the sin of an angle in radians.
 	 *
 	*/
 	double sin(double angle) {
 		return Math.sin(angle);
 	}
 }
