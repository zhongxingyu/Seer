 package lejos.nxt.addon;
 
 import lejos.nxt.I2CPort;
 import lejos.nxt.I2CSensor;
 import lejos.robotics.DirectionFinder;
 
 /**
  * HiTechnic IRSeekerV2 sensor - untested. www.hitechnic.com
  * 
  */
 public class IRSeekerV2 extends I2CSensor implements DirectionFinder
 {
 	public static enum Mode {
 		AC, DC
 	};
 
 	public static final byte	address	= 0x08;
 	byte[]						buf		= new byte[1];
 	public static final	float	noAngle = Float.NaN;
 
 	private Mode				mode;
 
 	/**
 	 * Set the mode of the sensor
 	 */
 	public void setMode(Mode mode)
 	{
 		this.mode = mode;
 	}
 
 	public IRSeekerV2(I2CPort port, Mode mode)
 	{
 		super(port, I2CPort.STANDARD_MODE);
 		setMode(mode);
 		setAddress(address);
 	}
 
 	/**
 	 * Returns the direction of the target (1 to 9) or 0 if no target.
 	 * 
 	 * @return direction
 	 */
 	public int getDirection()
 	{
 		int register = 0;
 		if(mode == Mode.AC)
 		{
 			register = 0x49;
 		}
 		else if(mode == Mode.DC)
 		{
 			register = 0x42;
 		}
 		int ret = getData(register, buf, 1);
 		if(ret != 0)
 			return -1;
 		return (0xFF & buf[0]);
 	}
 
 	/**
 	 * Returns the angle of the target (-180 to 180) or NaN.
 	 * 
 	 * @return direction
 	 */
 	public float getAngle(boolean blocking)
 	{
 		while(true)
 		{
 			int dir = getDirection();
			if(dir == 0 && !blocking)
 				return Float.NaN;
 			else
 				return (dir - 5) * 30;
 		}
 	}
 	public float getAngle()
 	{
 		return getAngle(false);
 	}
 
 	public boolean hasDirection()
 	{
 		return getDirection() != 0;
 	}
 
 	/**
 	 * Returns value of sensor 1 - 5.
 	 * 
 	 * @param id
 	 *            The id of the sensor to read
 	 * @return sensor value (0 to 255).
 	 */
 	public int getSensorValue(int id)
 	{
 		int register = 0;
 		if(mode == Mode.AC)
 		{
 			register = 0x4A;
 		}
 		else if(mode == Mode.DC)
 		{
 			register = 0x43;
 		}
 		if(id < 1 || id > 5)
 			throw new IllegalArgumentException(
 					"The argument 'id' must be between 1 and 5");
 		int ret = getData(register + (id - 1), buf, 1);
 		if(ret != 0)
 			return -1;
 		return (0xFF & buf[0]);
 	}
 
 	/**
 	 * Gets the values of each sensor, returning them in an array.
 	 * 
 	 * @return Array of sensor values (0 to 255).
 	 */
 	public int[] getSensorValues()
 	{
 		int[] values = new int[5];
 		for(int i = 0; i < 5; i++)
 		{
 			values[i] = getSensorValue(i + 1);
 		}
 		return values;
 	}
 
 	/**
 	 * Returns the average sensor reading (DC Only)
 	 * 
 	 * @return sensor value (0 to 255).
 	 */
 	public int getAverage(int id)
 	{
 		if(mode == Mode.DC)
 		{
 			if(id <= 0 || id > 5)
 				return -1;
 			int ret = getData(0x48, buf, 1);
 			if(ret != 0)
 				return -1;
 			return (0xFF & buf[0]);
 		}
 		else
 		{
 			return -1;
 		}
 	}
 
 	/**
 	 * Returns a string representation of the strengths
 	 */
 	public String toString()
 	{
 		return "(" + getSensorValue(1) + "," + getSensorValue(2) + ","
 				+ getSensorValue(3) + "," + getSensorValue(4) + ","
 				+ getSensorValue(5) + ")";
 	}
 
 	@Override
     public float getDegreesCartesian()
     {
 	    return getAngle();
     }
 
 	@Override
     public void resetCartesianZero()
     {	    
     }
 
 	@Override
     public void startCalibration()
     {
     }
 
 	@Override
     public void stopCalibration()
     {
     }
 }
