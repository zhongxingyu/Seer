 package kinematics;
 
 import lejos.nxt.Motor;
 
 public class Kinematics {
 	static final int GEAR_RATIO_A = 7;
 	static final int GEAR_RATIO_B = 1;
 	static final int DEFAULT_SPEED = 40;
 	public static final int	MAX_A = 45;
 	public static final int	MIN_A = -45;
 	public static final int	MAX_B = 30;
 	public static final int	MIN_B = -30;
 	
 	
 	/**
 	 * Constructor
 	 */
 	public Kinematics() {
 		Motor.A.setSpeed(DEFAULT_SPEED*GEAR_RATIO_A);
 		Motor.B.setSpeed(DEFAULT_SPEED*GEAR_RATIO_B);
 		reset();
 	}
 	
 	
 	/**
 	 * Resets the Position back to 0/0
 	 */
 	public void reset() {
 		Motor.A.rotateTo(0);
 		Motor.B.rotateTo(0);
		Motor.A.resetTachoCount();
		Motor.B.resetTachoCount();
 	}
 	
 	/**
 	 * Rotates both Motor to given degrees
 	 * @param a	Position for motor a
 	 * @param b Position for motor b
 	 * @param immediateReturn
 	 * @throws BadPositionException 
 	 */
 	public void rotateTo(int a, int b, boolean immediateReturn) throws BadPositionException {
 		rotateATo(a, immediateReturn);
 		rotateBTo(b, immediateReturn);
 	}
 	
 	/**
 	 * @param position
 	 * @param immediateReturn
 	 * @throws BadPositionException 
 	 */
 	public void rotateATo(int position, boolean immediateReturn) throws BadPositionException {
 		if (position > MAX_A || position < MIN_A) throw new BadPositionException();
 		Motor.A.rotateTo(position*GEAR_RATIO_A, immediateReturn);
 	}
 	
 	/**
 	 * @param position
 	 * @param immediateReturn
 	 * @throws BadPositionException 
 	 */
 	public void rotateBTo(int position,  boolean immediateReturn) throws BadPositionException {
 		if (position > MAX_B || position < MIN_B) throw new BadPositionException();
 		Motor.B.rotateTo(position*GEAR_RATIO_B, immediateReturn);
 	}
 	
 	/**
 	 * Returns the Position of Engine A.
 	 * @return Position (Degree) of Engine A.
 	 */
 	public int getPositionA() {
 		return Motor.A.getPosition()/GEAR_RATIO_A;
 	}
 	
 	/**
 	 * Returns the Position of Engine B.
 	 * @return Position (Degree) of Engine B.
 	 */
 	public int getPositionB() {
 		return Motor.B.getPosition()/GEAR_RATIO_B;
 	}
 }
