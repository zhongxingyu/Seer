 package de.tud.kitchen.android;
 
 import android.hardware.SensorEvent;
 import android.hardware.SensorManager;
 
 public class SensorData {
 	/**
 	 * private variables for storing the sensor data
 	 */
 	private long timestamp = 0;
 	private float data_x_axes = 0;
 	private float data_y_axes = 0;
 	private float data_z_axes = 0;
 
 	/**
 	 * Storing the delta time between sender and receiver.
 	 */
 	private int time_delta = 0;
 
 	/**
 	 * Constructor without start value.
 	 */
 	public SensorData() {
 	}
 
 	/**
 	 * Constructor with start values.
 	 * 
 	 * @param event
 	 *            The SensorEvent with the data to use as start value.
 	 */
 	public SensorData(SensorEvent event) {
 		setData(event);
 	}
 
 	/**
 	 * 
 	 * @param event
 	 *            The SensorEvent with the data to use as start value.
 	 * @param scale_factor
 	 *            The scale factor for normalization of the given sensor data.
 	 */
 	public SensorData(SensorEvent event, float scale_factor) {
 		setData(event, scale_factor);
 	}
 
 	/**
 	 * Sets the stored sensor data values to the given event values.
 	 * 
 	 * @param event
 	 *            The SensorEvent with the data to use.
 	 */
 	public void setData(SensorEvent event) {
 		setData(event, 1);
 	}
 
 	/**
 	 * Sets the time difference in ms.
 	 * 
 	 * @param delta
 	 *            The time difference in ms.
 	 */
 	public void setTimeDelta(int delta) {
 		time_delta = delta;
 	}
 
 	/**
 	 * Sets the stored sensor data values to the normalization of the given event values.
 	 * 
 	 * @param event
 	 *            The SensorEvent with the data to use.
 	 * @param scale_factor
 	 *            The scale factor for normalization of the given sensor data.
 	 */
 	public void setData(SensorEvent event, float scale_factor) {
		timestamp = event.timestamp;
 		data_x_axes = event.values[SensorManager.DATA_X] / scale_factor;
 		data_y_axes = event.values[SensorManager.DATA_Y] / scale_factor;
 		data_z_axes = event.values[SensorManager.DATA_Z] / scale_factor;
 	}
 
 	/**
 	 * Returns the timestamp.
 	 * 
 	 * @return The stored timestamp value of the sensor data.
 	 */
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	/**
 	 * Returns the x axes value.
 	 * 
 	 * @return The stored x axes data.
 	 */
 	public float getXAxes() {
 		return data_x_axes;
 	}
 
 	/**
 	 * Returns the y axes value.
 	 * 
 	 * @return The stored y axes data.
 	 */
 	public float getYAxes() {
 		return data_y_axes;
 	}
 
 	/**
 	 * Returns the z axes value.
 	 * 
 	 * @return The stored z axes data.
 	 */
 	public float getZAxes() {
 		return data_z_axes;
 	}
 
 	/**
 	 * Returns the complete sensor data as an object array for OSC transmission.
 	 * 
 	 * @return The sensor data as an OSC object array.
 	 */
 	public Object[] toOSCArray() {
 		Object[] osc_data = new Object[4];
 
		osc_data[0] = new Float(data_x_axes);
		osc_data[1] = new Float(data_y_axes);
		osc_data[2] = new Float(data_z_axes);
		// send timestamp as 1/1000 second (= ms) instead as nanosecond
		osc_data[3] = new Integer((int) (timestamp / 1000000) + time_delta);
 
 		return osc_data;
 	}
 
 	@Override
 	public String toString() {
 		String output = new String();
 
 		output = output.concat(Long.toString(timestamp) + String.format("|%15.12f", data_x_axes) + String.format("|%15.12f", data_y_axes)
 				+ String.format("|%15.12f", data_z_axes));
 
 		return output;
 	}
 }
