 package grouppractical.client.commands;
 
 import grouppractical.utils.map.Position;
 
 import static grouppractical.utils.StandardOps.convert;
 
 /**
  * <p>Immutable class representing the status of the robot.</p>
  * <p><i>Note, the certainty and occupied values of the position are ignored by this command</i></p>
  * @author jay
  *
  */
 public class RStatusCommand implements Command {
 	public static final int MIN_X = -16384, MIN_Y = -16384, MAX_X = 16384, MAX_Y = 16384;
 	public static final float MIN_BATT = 0f, MAX_BATT = 12f;
 	
 	/** Minimum rotation in degrees, radians and serialized integer */
	public static final double MIN_DEGREES = -359.98901, MIN_RADIANS = -6.28299;
 	public static final short MIN_INT = Short.MIN_VALUE + 1;
 	/** Maximum rotation in degrees, radians and serialized integer */
 	public static final double MAX_DEGREES = 359.98901, MAX_RADIANS = 6.28299;
 	public static final short MAX_INT = Short.MAX_VALUE;
 	
 	private final Position position;
 	private final float voltage;
 	private final short angle;
 	
 	/**
 	 * Constructs a new Status Command
 	 * @param position Position of the robot
 	 * @param voltage Battery Level of robot in volts
 	 * @param angle Angle the robot is facing, as a serialized integer
 	 */
 	public RStatusCommand(Position position, float voltage, short angle) {
 		this.position = position;
 		voltage = voltage > MAX_BATT ? MAX_BATT : voltage;
 		voltage = voltage < MIN_BATT ? MIN_BATT : voltage;
 		this.voltage = voltage; 
 		this.angle = angle;
 	}
 	/**
 	 * Creates a new Status Command, using an angle in radians
 	 * @param position Position of the robot
 	 * @param voltage Battery Level of robot in volts
 	 * @param angle Angle the robot is facing, in radians
 	 * @return New RStatus Command
 	 */
 	public static RStatusCommand constructFromRadians(Position position, float voltage, double angle) {
 		short s = (short) convert(angle,MIN_RADIANS,MAX_RADIANS,MIN_INT,MAX_INT);
 		return new RStatusCommand(position, voltage, s);
 	}
 	/**
 	 * Creates a new Status Command, using an angle in degrees
 	 * @param position Position of the robot
 	 * @param voltage Battery Level of robot in volts
 	 * @param angle Angle the robot is facing, in degrees
 	 * @return New RStatus Command
 	 */
 	public static RStatusCommand constructFromDegrees(Position position, float voltage, double angle) {
 		short s = (short) convert(angle,MIN_DEGREES,MAX_DEGREES,MIN_INT,MAX_INT);
 		return new RStatusCommand(position, voltage, s);
 	}
 	
 	@Override
 	public CommandType getCommandType() {
 		return CommandType.RSTATUS;
 	}
 
 	@Override
 	public char[] serialize() {
 		char[] bytes = new char[8];
 		//Command Type
 		bytes[0] = getCommandType().toChar();
 		
 		int x = Math.abs(position.getX()), y = Math.abs(position.getY());
 		boolean x_negative = position.getX() < 0, y_negative = position.getY() < 0;
 		
 		float v = voltage > MAX_BATT ? MAX_BATT : (voltage < MIN_BATT ? MIN_BATT : voltage);
 		v = v * 20;
 		
 		int serial = getAngleSerial();
 		
 		//X coordinate
 		bytes[1] = (char) ((0xFE & (x >> 7)) ^ (x_negative ? 0x01 : 0x00));
 		bytes[2] = (char) (0xFF & x);
 		//Y coordinate
 		bytes[3] = (char) ((0xFE & (y >> 7)) ^ (y_negative ? 0x01 : 0x00));
 		bytes[4] = (char) (0xFF & y);
 		//Battery Voltage
 		bytes[5] = (char) (0xFF & (int)v);
 		//MSB of distance
 		bytes[6] = (char)((Math.abs(serial) & 0x7F80) >> 7);
 		//LSB of distance
 		bytes[7] = (char)((Math.abs(serial) & 0x007F) << 1);
 		//Sets bit 8 of the LSB to be the direction
 		if (serial > 0) bytes[7] = (char) (bytes[7] | 0x01);
 		return bytes;
 	}
 	
 	/**
 	 * Gets the position of the robot
 	 * @return Position of robot
 	 */
 	public Position getPosition() {
 		return position;
 	}
 	
 	/**
 	 * Gets the voltage of the robot
 	 * @return Battery voltage of robot
 	 */
 	public float getVoltage() {
 		return voltage;
 	}
 	
 	/**
 	 * Gets the angle of robot in degrees, where 0 is North
 	 * @return Double (between >= 0 and < 360) representing the clockwise angle of rotation
 	 */
 	public double getAngleDegrees() { return convert(angle,MIN_INT,MAX_INT,MIN_DEGREES,MAX_DEGREES); }
 	/**
 	 * Gets the angle of robot in radians, where 0 is North
 	 * @return Double (between >= 0 and < 2pi) representing the clockwise angle of rotation
 	 */
 	public double getAngleRadians() { return convert(angle,MIN_INT,MAX_INT,MIN_RADIANS,MAX_RADIANS); }
 	
 	/**
 	 * Gets the angle of robot as a serialized integer, where 0 is North
 	 * @return 16-bit integer between 0 and 65535
 	 */
 	public short getAngleSerial() { return angle; }
 	
 	@Override
 	public String toString() {
 		return String.format("RSTATUS(x: %d, y: %d, a: %f, %fV)", position.getX(), position.getY(),
 				getAngleDegrees(), voltage); }
 	
 	@Override
 	public boolean equals(Object that) {
 		if (this == that) return true;
 		if (that == null || !that.getClass().equals(RStatusCommand.class)) return false;
 		RStatusCommand rthat = (RStatusCommand)that;
 		return rthat.getPosition().equals(this.getPosition()) && rthat.getVoltage() == this.getVoltage()
 				&& rthat.getAngleSerial() == this.getAngleSerial();
 	}
 	
 	@Override
 	public int hashCode() {
 		return getPosition().hashCode();
 	}
 }
