 package com.buglabs.bug.module.lcd;
 
 import java.io.IOException;
 
 import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleProvider;
 import com.buglabs.bug.module.lcd.pub.IML8953Accelerometer;
 
 /**
 * Exposes sysfs entry for ML3953 devices to Java clients.
  * 
  * @author jconnolly
  * 
  */
 public class ML8953AccelerometerImplementation implements IML8953Accelerometer,
 		IAccelerometerSampleProvider {
 
 	private ML8953Device device;
 
 	public ML8953AccelerometerImplementation() {
 		device = ML8953Device.getInstance();
 	}
 
 	public short readX() throws IOException {
 		String position = device.getPosition();
 		position = position.substring(position.indexOf('(') + 1,
 				position.indexOf(','));
 		return Short.parseShort(position.trim());
 	}
 
 	public short readY() throws IOException {
 		String position = device.getPosition().trim();
 		position = position.substring(position.indexOf(',') + 1);
 		position = position.substring(0, position.indexOf(','));
 		return Short.parseShort(position.trim());
 	}
 
 	public short readZ() throws IOException {
 		String position = device.getPosition();
 		position = position.substring(position.lastIndexOf(',') + 1,
 				position.indexOf(')'));
 		return Short.parseShort(position.trim());
 	}
 
 	private float convert2gs(final short raw) {
 		return raw / 1000F;
 	}
 
 	//
 	// Accelerometer Sample Provider
 	//
 
 	// TODO: should we be doing the -1 on Y or is something else bassackwards?
 	public AccelerometerSample readSample() throws IOException {
 		return new AccelerometerSample(
 				convert2gs(readX()),
 				convert2gs(readY())	* -1F, // is this necessary?
 				convert2gs(readZ()));
 	}
 
 }
