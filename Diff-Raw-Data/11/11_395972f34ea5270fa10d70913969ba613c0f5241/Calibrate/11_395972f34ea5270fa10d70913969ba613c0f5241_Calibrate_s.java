 package balle.strategy;
 
 import java.util.ArrayList;
 
 import balle.controller.Controller;
 import balle.misc.Globals;
 import balle.strategy.planner.AbstractPlanner;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 
 public class Calibrate extends AbstractPlanner {
 
 	private final long ACCEL_TIME = 1000; // time needed to accelerate to the
 											// terminal speed
 	private final int POWER_STEP = 25;
 	private final long SAMPLE_TIME = 4000; // how many angular velocity samples
 											// to
 										// take (Must be greater then 1)
 	private long lastPowerChangeTime = System.currentTimeMillis();
 	private long sampleStartTime;
 	private long sampleEndTime;
 	private int power = 0;
 	private ArrayList<Orientation> samples = new ArrayList<Orientation>();
 	
 	private boolean done = false;
 
 	@FactoryMethod(designator = "Calibrate")
 	public static Calibrate calibrateFactory() {
 		return new Calibrate();
 	}
 
     /**
      * Tell the strategy to do a step (e.g. move forward).
      * @param snapshot TODO
      */
 	public void onStep(Controller controller, Snapshot snapshot) {
		if (true) {
			controller.setWheelSpeeds(500, 500);
			System.out.println(snapshot.getBalle().getVelocity().abs());
			return;
		}
 		if(!done) {
			controller.setWheelSpeeds(power, -power);
 			// if accelerating, just let it accelerate
 			if (System.currentTimeMillis() - lastPowerChangeTime < ACCEL_TIME)
 				return;
 			// else collect the sample if possible
 			boolean isLastSample = false;
 			if (snapshot.getBalle() != null
 					&& snapshot.getBalle().getOrientation() != null) {
 				if (samples.size() == 0) {
 					sampleStartTime = snapshot.getTimestamp();
 				}
 				samples.add(snapshot.getBalle().getOrientation());
 				if (snapshot.getTimestamp() - sampleStartTime > SAMPLE_TIME) {
 					sampleEndTime = snapshot.getTimestamp();
 					isLastSample = true;
 				}
 			}
 
 			// if this is the last sample for the current power
 			if (isLastSample) {
 				// record the results
 				record();
 				// move to the next power (or stop if done)
 				samples.clear();
 				power += POWER_STEP;
 				if(power <= Globals.MAXIMUM_MOTOR_SPEED) {
 					controller.setWheelSpeeds(power, -power);
 					lastPowerChangeTime = System.currentTimeMillis();
 				} else {
 					done = true;
 				}
 			}
 		} else {
 			controller.stop();
 		}
 	}
 	
 	/**
 	 * uses the current samples and power to record a (power, velocity) pair 
 	 */
 	private void record() {
 		// calculate the wheel velocity
 		double deltaA = 0;
 		for (int i = 1; i < samples.size(); i++) {
 			deltaA += samples.get(i - 1).angleToatan2Radians(samples.get(i));
 		}
 		double angVel = Math.abs(deltaA / (sampleEndTime - sampleStartTime));
 		double wheelVelocity = angularVelToWheelSpeed(angVel);
 		
 		// record (just print out for now)
 		System.out.println(power + ";" + wheelVelocity);
 	}
 
 	/**
 	 * assuming the robot is spinning (has no linear velocity) calculate the
 	 * wheel speed (absolute velocity) based on angular velocity
 	 * 
 	 * @return
 	 */
 	private double angularVelToWheelSpeed(double angVel) {
		return angVel * Globals.ROBOT_TRACK_WIDTH / 2;
 	}
 
 }
