 package sensors;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorManager;
 
 /**
 * Class that contains the devices MagneticField-sensor 
  * and simplifies usage of it.
  * @author Pontus Pall & Gustav Alm Rosenblad
  *
  */
 public class MagneticFieldSensor implements SpangSensor {
 	public static final int SENSOR_TYPE = Sensor.TYPE_MAGNETIC_FIELD;
 	public static final byte ENCODE_ID = 0x04;
 	public static final int VALUES_LENGTH = 3;
 	public static final int ENCODED_LENGTH = VALUES_LENGTH * 4 + 1;
 
 	private float[] values = new float[3];
 	private int accuracy;
 	private boolean isActive;
 
 	private SensorManager sensorManager;
 	private Sensor sensor;
 
 	/**
	 * Doesn't start listening to the sensor. Only gets the light-sensor from the device.
	 * If the device has no MagneticField sensor, a NoSensorException is thrown.
 	 * @param context
 	 */
 	public MagneticFieldSensor(Context context) {
 		this.sensorManager = (SensorManager) context
 				.getSystemService(Context.SENSOR_SERVICE);
 		this.sensor = sensorManager
 				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 
 		if (this.sensor == null) {
 			throw new NoSensorException("Device has no MagneticField-sensor");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void start() {
 		sensorManager.registerListener(this, sensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		this.isActive = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stop() {
 		sensorManager.unregisterListener(this);
 		this.isActive = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return the MagneticField-sensor value. 
 	 * Return value occupies only 3 positions in the array.
 	 */
 	@Override
 	public float[] getValues() {
 		return values;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onAccuracyChanged(Sensor unused, int newAccuracy) {
 		accuracy = newAccuracy;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		values = event.values.clone();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getAccuracy() {
 		return this.accuracy;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getSensorID() {
 		return SENSOR_TYPE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isRunning() {
 		return isActive;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public byte[] encode() {
 		byte[] encodedValue = new byte[13];
 		encodedValue[0] = ENCODE_ID;
 
 		int float0 = Float.floatToRawIntBits(getValues()[0]);
 		int float1 = Float.floatToRawIntBits(getValues()[1]);
 		int float2 = Float.floatToRawIntBits(getValues()[2]);
 
 		encodedValue[1] = (byte) ((float0 >> 24) & 0xff);
 		encodedValue[2] = (byte) ((float0 >> 16) & 0xff);
 		encodedValue[3] = (byte) ((float0 >> 8) & 0xff);
 		encodedValue[4] = (byte) (float0 & 0xff);
 
 		encodedValue[5] = (byte) ((float1 >> 24) & 0xff);
 		encodedValue[6] = (byte) ((float1 >> 16) & 0xff);
 		encodedValue[7] = (byte) ((float1 >> 8) & 0xff);
 		encodedValue[8] = (byte) (float1 & 0xff);
 
 		encodedValue[9] = (byte) ((float2 >> 24) & 0xff);
 		encodedValue[10] = (byte) ((float2 >> 16) & 0xff);
 		encodedValue[11] = (byte) ((float2 >> 8) & 0xff);
 		encodedValue[12] = (byte) (float2 & 0xff);
 
 		return encodedValue;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getValuesLength() {
 		return VALUES_LENGTH;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getEncodedLength() {
 		return ENCODED_LENGTH;
 	}
 }
