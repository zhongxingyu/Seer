 package edu.kit.curiosity.behaviors.tapefollow;
 
import lejos.nxt.Button;
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.subsumption.Behavior;
 import edu.kit.curiosity.Settings;
 
 public class TapeFollow implements Behavior {
 
 	static DifferentialPilot pilot = Settings.PILOT;
 	static LightSensor light = Settings.LIGHT;
 
 	private boolean suppressed = false;
 
 	/**
 	 * Light threshold.
 	 */
 	static final int blackWhiteThreshold = 50;
 
 	/**
 	 * Thread sleep time.
 	 */
 	static final int sleep = 10;
 
 	/**
 	 * Number of steps before making turn steeper.
 	 */
 	static final int out = 150;
 
 	/**
 	 * Number of steps.
 	 */
 	private static int i = 0;
 
 	/**
 	 * Number of consecutive left turns.
 	 */
 	private static int l = 0;
 
 	/**
 	 * Number of consecutive right turns.
 	 */
 	private static int r = 0;
 
 	/**
 	 * Turn rate.
 	 */
 	private static int tr = 40;
 
 	private static int tacho = 0;
 
 	@Override
 	public boolean takeControl() {
 		return true;
 	}
 
 	@Override
 	public void action() {
 
 		suppressed = false;
 		tacho = Motor.B.getTachoCount();
 
 		while (!suppressed) {
 			if (light.getLightValue() > blackWhiteThreshold) {
 				// On white, turn right
 				l = 0;
 				// System.out.print("right ");
 				pilot.steer(-tr, -10, true);
 				r++;
 				tacho = Motor.B.getTachoCount();
 			} else {
 				// On black, turn left
 				r = 0;
 				// System.out.print("left ");
 				pilot.steer(tr, 10, true);
 				r = 1;
 				l++;
 			}
 			// between out and 2 * out
 			if (i > out && i <= 2 * out) {
 				// last out turns were in same direction
 				if (r > out || l > out) {
 					// make turn steeper
 					tr = 150;
 				}
 			} else if (i > 2 * out) { // more than 2 * out
 				if (r > 2 * out || l > 2 * out) {
 
 					// multiplier
 					int m = 1;
 
 					if (r > 2 * out)
 						m = -1;
 					// pilot.setTravelSpeed(pilot.getMaxTravelSpeed());
 
 					// travel back until line found
 
 					while (i >= 0
 							&& light.getLightValue() < blackWhiteThreshold) { // travel
 																				// back
 						i--;
 						try {
 							Thread.sleep(sleep);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					System.out.println("GAP?");
 					Button.waitForAnyPress();
 
 					// Invert multiplier
 					pilot.stop();
 					m *= -1;
 					// Travels forward arc in the other direction
 					while (i < 2 * out
 							&& light.getLightValue() < blackWhiteThreshold) {
 						pilot.steer(m * tr, m * 10, true); // travel back
 						i++;
 						try {
 							Thread.sleep(sleep);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					pilot.stop();
 
 					// Travels backward arc in the other direction
 					while (i >= 0
 							&& light.getLightValue() < blackWhiteThreshold) {
 						pilot.steer(m * tr, (-1) * m * 10, true); // travel
 																	// back
 						i--;
 						try {
 							Thread.sleep(sleep);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					if (!suppressed
 							&& light.getLightValue() < blackWhiteThreshold)
 						Settings.gap = true;
 				}
 				// Set values to defaults.
 				i = 0;
 				l = 0;
 				r = 0;
 				tr = 90;
 			}
 			i++;
 			try {
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		pilot.stop();
 
 	}
 
 	@Override
 	public void suppress() {
 		suppressed = true;
 
 	}
 }
