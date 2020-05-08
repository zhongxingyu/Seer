 package org.concord.sensor.pasco;
 
 import java.util.ArrayList;
 
 import org.concord.sensor.pasco.datasheet.PasportSensorDataSheet;
 import org.concord.sensor.pasco.datasheet.PasportSensorMeasurement;
 import org.concord.sensor.pasco.jna.PascoChannel;
 import org.concord.sensor.pasco.jna.PascoException;
 
 public class PascoSensorManager {
 	static class SensorInfo {
 		PasportSensorDataSheet dataSheet;
 		PascoChannel channel;
 		
 		// list of measurements to start
 		ArrayList<MeasurementInfo> measurements = new ArrayList<MeasurementInfo>();
 
 		// variables for during data collection
 		public int sampleSize;
 		byte[] readBuffer;
 		byte[] bufferedSamples;
 		int numBufferedSamples;
 		
 		public void bufferSampleData() throws PascoException{
 			// FIXME should see about handling invalid num of bytes read
 			int readBytes = channel.getSampleData(sampleSize, readBuffer, readBuffer.length/sampleSize);
 			System.arraycopy(readBuffer, 0, bufferedSamples, numBufferedSamples*sampleSize, readBytes);
 			numBufferedSamples += readBytes/sampleSize;
 		}
 		
 		public void processSamples(int numSamples, float[] values, int offset, int nextSampleOffset) {
 			for(int i=0; i<numSamples; i++){
 				for(MeasurementInfo measurement: measurements){
 					values[offset + i*nextSampleOffset + measurement.index] = 
 						measurement.measurement.readSample(bufferedSamples, i*sampleSize);
 				}				
 			}			
 
 			// reduce the samples buffered after they are processed
 			numBufferedSamples -= numSamples;
 		}
 
 		public void start(int msPeriod) throws PascoException {
 			sampleSize = channel.startContinuousSampling(msPeriod);
 			readBuffer = new byte[sampleSize*32];
 			bufferedSamples = new byte[sampleSize*64];
 			numBufferedSamples = 0;
 		}
 	}
 	
 	static class MeasurementInfo {
 		PasportSensorMeasurement measurement;
 		SensorInfo sensor;
 		public int index;
 	}
 	
 	ArrayList<SensorInfo> sensors = new ArrayList<SensorInfo>();
 	ArrayList<SensorInfo> sensorsToStart = new ArrayList<SensorInfo>();
 
 	int measurementCount = 0;
 	
 	public void addSensor(PasportSensorDataSheet dataSheet, PascoChannel channel) {
 		SensorInfo sensorInfo = new SensorInfo();
 		sensorInfo.dataSheet = dataSheet;
 		sensorInfo.channel = channel;
 		sensors.add(sensorInfo);
 	}
 	
 	/**
 	 * This will be the smallest period that is valid for all sensors.
 	 * 
 	 * @return maximum of minPeriods of all sensors
 	 */
 	public float getMinPeriod() {
 		float minPeriod = Float.NEGATIVE_INFINITY;
 		for(SensorInfo sensor: sensors){
 			if(minPeriod < sensor.dataSheet.getMinPeriod()){
 				minPeriod = sensor.dataSheet.getMinPeriod();
 			}
 		}
 		return minPeriod;
 	}
 	
 	/**
 	 * This will be the largest period that is valid for all sensors
 	 * 
 	 * @return minimum of maxPeriods of all sensors
 	 */
 	public float getMaxPeriod(){
 		float maxPeriod = Float.POSITIVE_INFINITY;
 		for(SensorInfo sensor: sensors){
 			if(maxPeriod > sensor.dataSheet.getMaxPeriod()){
 				maxPeriod = sensor.dataSheet.getMaxPeriod();
 			}
 		}
 		return maxPeriod;
 	}
 
 	/**
 	 * Figure out a valid period that is smallest of the default periods for each sensor
 	 * 
 	 * It is possible that some sensors are not compatible to be collected with a single sampling rate
 	 * if one sensor requires a long time to sample and another isn't useful unless it samples fast
 	 * then there will be a problem  In that case this returns NaN 
 	 * 
 	 * @return
 	 */
 	public float getMinDefaultPeriod(){
 		float minValidPeriod = getMinPeriod();
 		float maxValidPeriod = getMaxPeriod();
 
 		// make sure there is a possible value
 		if(minValidPeriod > maxValidPeriod){
 			return Float.NaN;
 		}
 		
 		float minDefaultPeriod = Float.POSITIVE_INFINITY;
 		for(SensorInfo sensor: sensors){
 			if(minDefaultPeriod > sensor.dataSheet.getDefaultPeriod()){
 				minDefaultPeriod = sensor.dataSheet.getDefaultPeriod();
 			}
 		}
 		
 		// make sure this isn't too small for any of the sensors
 		if(minDefaultPeriod < minValidPeriod){
 			minDefaultPeriod = minValidPeriod;
 		}
 		
 		// make sure it isn't too big for any of the sensors
 		if(minDefaultPeriod > maxValidPeriod){
 			minDefaultPeriod = maxValidPeriod;
 		}
 		
 		return minDefaultPeriod;
 	}
 
 	public void clearSensors() {
 		sensors.clear();		
 	}
 
 	private SensorInfo findSensor(PascoChannel channel) {
 		for(SensorInfo sensor: sensors){
 			if(sensor.channel == channel){
 				return sensor;
 			}
 		}
 
 		return null;
 	}
 	
 	public void startChannels(float period) throws PascoException {
 		int msPeriod = (int)(period * 1000);
 		for(SensorInfo sensor: sensorsToStart){
 			sensor.start(msPeriod);
 		}
 	}
 
 	public void stopChannels() {
 		for(SensorInfo sensor: sensorsToStart) {
 			try {
 				sensor.channel.stopContinuousSampling();
 				sensor.readBuffer = null;
 			} catch (PascoException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public int read(float[] values, int offset, int nextSampleOffset) throws PascoException {
 		for(SensorInfo sensor: sensorsToStart){
 			sensor.bufferSampleData();
 		}
 		
 		int minSamplesBuffered = Integer.MAX_VALUE;
 		for(SensorInfo sensor: sensorsToStart){
 			// find the min number of samples read
 			if(sensor.numBufferedSamples < minSamplesBuffered){
 				minSamplesBuffered = sensor.numBufferedSamples;
 			}
 		}	
 		
 		for(SensorInfo sensor: sensorsToStart){
 			sensor.processSamples(minSamplesBuffered, values, offset, nextSampleOffset);
 		}
 
 		
 		return minSamplesBuffered;
 	}
 
 	/**
 	 * This adds the measurement to be collected.  The corresponding channel will be
 	 * started when startChannels is called
 	 * and when read is called this measurement will be added to the value array
 	 * 
 	 * The order of the calls to this method matters, that order is used for inserting
 	 * the measurements into the value array  
 	 * 
 	 * @param measurement
 	 * @param channel
 	 */
 	public void addMeasurement(PasportSensorMeasurement measurement,
 			PascoChannel channel) {		
 		MeasurementInfo info = new MeasurementInfo();
 		info.measurement = measurement;
 		info.sensor = findSensor(channel);
 		info.index = measurementCount++;
 		info.sensor.measurements.add(info);
 
 		if(!sensorsToStart.contains(info.sensor)){
 			sensorsToStart.add(info.sensor);
 		}
 	}
 
 	public void clearMeasurements() {
 		sensorsToStart.clear();
 		
 		// we could just go through the sensorsToStart but instead we run through all of them
 		for(SensorInfo sensor: sensors){
 			sensor.measurements.clear();
 		}
 	}
 }
