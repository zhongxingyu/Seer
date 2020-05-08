 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.countertimer;
 
 import gda.device.ContinuousParameters;
 import gda.device.DeviceException;
 import gda.device.detector.BufferedDetector;
 import gda.device.detector.DAServer;
 
 import java.util.HashMap;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Adapter for the TfgScalerWithLogValues class so that it may be used in ContinuousScans.
  * <p>
  * Assumes that the TTL signal driving the TFG time-frames is connected to TFG socket TTL INPUT 0.
  */
 public class BufferedScaler extends TfgScalerWithLogValues implements BufferedDetector {
 
 	private static final Logger logger = LoggerFactory.getLogger(BufferedScaler.class);
 
 	private ContinuousParameters parameters;
 	private boolean continuousMode;
 	private double overrunTime = 0.1;
 	private DAServer daserver;
 	private int ttlSocket = 0; // the TTL Trig In socket [0-3] default is 0
 	private Boolean returnCountRates = false;
 
 	private double[][] framesRead;
 	
 	public BufferedScaler(){
 		try {
 			framesRead = new double[getNumberFrames()][5];
 		} catch (DeviceException e) {
 			logger.error("Cannot construct BufferedScaler, something wrong with TFG", e);
 		}
 	}
 	
 	public double[][] getFramesRead() {
 		return framesRead;
 	}
 	
 	public void clearFramesRead(){
 		framesRead = null;
 	}
 
 	@Override
 	public void collectData() throws DeviceException {
 		// do nothing when in continuous mode
 		if (continuousMode){
 			return;
 		}
 		super.collectData();
 	}	
 	
 	@Override
 	public Object[] readAllFrames() throws DeviceException {
 		Integer lastFrame = getNumberFrames() - 1;
 		return readFrames(0, lastFrame);
 	}
 	
 	@Override
 	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
 		
 		double[][] frame = readData(startFrame, finalFrame);
 		framesRead=frame;
 		return frame;
 	}
 	
 	@Override
 	public double[] readout() throws DeviceException {
 		if (continuousMode){
 			Integer numFrames = getNumberFrames();
 			// if nothing collected, so 0 frames, then view the contents of the first frame anyway
 			if (numFrames == 0) {
 				return readData(0, 0)[0];
 			}
 			return readData(numFrames -1 , numFrames -1)[0];
 
 		}
 		return readData(0,0)[0];
 	}
 
 	/**
 	 * @param startFrame - as known by TFG i.e. first frame is 0
 	 * @param finalFrame
 	 * @return double[][]
 	 * @throws DeviceException
 	 */
 	private double[][] readData(int startFrame, int finalFrame) throws DeviceException {
 
 		// readout everything for those frames
 		int numberOfFrames = (finalFrame - startFrame) + 1;
 		double[] scalerReadings;
 		if (numChannelsToRead == null){
 			scalerReadings = scaler.read(0, 0, startFrame, scaler.getDimension()[0], 1, numberOfFrames);
 		} else {
 			scalerReadings = scaler.read(0, 0, startFrame, numChannelsToRead, 1, numberOfFrames);
 		}
 		int numberOfValuesPerFrame = this.getExtraNames().length; //assuming the instance is properly setup
 		
 		// loop over frames, extract each frame and add log values to the end
 		double[][] output = new double[numberOfFrames][];
 		for (int i = 0; i < numberOfFrames; i++) {
 			
 			// get the slice from the readings array
 			int numScalers = numChannelsToRead == null ?scaler.getDimension()[0]:numChannelsToRead;
 			int entryNumber = i;
 			double[] slice = new double[numScalers];
 			for (int scaNum = 0; scaNum < numScalers; scaNum++){
 				slice[scaNum] = scalerReadings[entryNumber];
 				entryNumber += numberOfFrames;
 			}
 //			int startSlice = i * scaler.getDimension()[0];
 //			int endSlice = startSlice + scaler.getDimension()[0];
 //			double[] slice = ArrayUtils.subarray(scalerReadings, startSlice, endSlice);
 
 			double[] thisFrame = new double[numberOfValuesPerFrame];
 			if (isTFGv2 && !timeChannelRequired) {
 				// for TFGv2 always get the number of live time clock counts as the first scaler item
 				thisFrame = ArrayUtils.subarray(slice, 1, slice.length);
 			} else if (isTFGv2 && timeChannelRequired) {
 				// convert the live time clock counts into seconds (TFG has a 100MHz clock cycle)
 				thisFrame = ArrayUtils.subarray(slice, 0, slice.length);
 				thisFrame[0] = thisFrame[0] / 100000000;
 				
 				// convert all values to rates
 				if (this.returnCountRates && thisFrame[0] > 0.0) {
 					for (int scaNum = 1; scaNum < numScalers; scaNum++) {
 						thisFrame[scaNum] /= thisFrame[0];
 					}
 				}
 				
 			} else if (!isTFGv2 && timeChannelRequired) {
 				throw new DeviceException("Invalid parameter options for " + getName()
 						+ ": cannot add a time channel when using TFGv1! Set timeChannelRequired to false");
 			} else {
 				thisFrame = slice;
 			}
 			if(isOutputLogValues())
 				thisFrame = appendLogValues(thisFrame);
 			output[i] = thisFrame;
 		}
 
 		return output;
 	}
 
 	@Override
 	public void setContinuousMode(boolean on) throws DeviceException {
 		this.continuousMode = on;
 		if (on) {
 			setTimeFrames();
 		} else {
 			switchOffExtTrigger();
 		}
 	}
 
 	private void setTimeFrames() throws DeviceException {
 
 		if (parameters == null) {
 			throw new DeviceException(getName()
 					+ " could not set time frames for continuous scans as parameters not supplied!");
 		}
 
 		// tfg setup-trig
		switchOnExtTrigger();
		
 		//Send as a single command. Otherwise DAServer reply timeouts are seen and the 3 commands take about 10s!
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("tfg setup-groups ext-start cycles 1"+"\n");
		buffer.append(parameters.getNumberDataPoints() + " 0.000001 0.00000001 0 0 0 " + (ttlSocket + 8)+"\n");
 		buffer.append("-1 0 0 0 0 0 0");
 		daserver.sendCommand(buffer.toString());
		daserver.sendCommand("tfg arm");
 		/*daserver.sendCommand("tfg setup-groups ext-start cycles 1");
 		daserver.sendCommand(parameters.getNumberDataPoints() + " 0.000001 0.00000001 0 0 0 " + ttlSocket + 8);
 		daserver.sendCommand("-1 0 0 0 0 0 0");*/
 	}
 
 	@Override
 	public boolean isContinuousMode() {
 		return continuousMode;
 	}
 
 	@Override
 	public ContinuousParameters getContinuousParameters() {
 		return parameters;
 	}
 
 	@Override
 	public void setContinuousParameters(ContinuousParameters parameters) {
 		this.parameters = parameters;
 	}
 
 	@Override
 	public void clearMemory() throws DeviceException {
 		scaler.clear();
 		scaler.start(); 
 	}
 
 	/**
 	 * switch off external triggering by the TTL0 input
 	 * @throws DeviceException 
 	 */
 	private void switchOffExtTrigger() throws DeviceException {
 		daserver.sendCommand("tfg setup-trig start");
 	}
 
 	/**
 	 * switch on external triggering by the TTL0 input
 	 * @throws DeviceException 
 	 */
 	private void switchOnExtTrigger() throws DeviceException {
		Object test = daserver.sendCommand("tfg setup-trig start ttl" + ttlSocket);
		System.out.println(test);;
 	}
 
 	@Override
 	public int getNumberFrames() throws DeviceException {
 		if (!continuousMode){ 
 			return 0;
 		}
 		
 		String[] cmds = new String[]{"status show-armed","progress","status","full","lap","frame"};
 		HashMap <String,String> currentVals = new HashMap<String,String>();
 		for (String cmd : cmds){
 			currentVals.put(cmd, daserver.sendCommand("tfg read " + cmd).toString());
 			logger.info("tfg read "+ cmd + ": " + currentVals.get(cmd));
 		}
 		
 		if (currentVals.isEmpty()){
 			return 0;
 		}
 		
 		
 		// else either scan not started (return -1) or has finished (return continuousParameters.getNumberDataPoints())
 		
 		// if started but nothing collected yet
 		if (currentVals.get("status show-armed").equals("EXT-ARMED") /*&& currentVals.get("status").equals("IDLE")*/ ){
 			return 0;
 		}
 
 		// if frame is non-0 then work out the current frame
 		if (!currentVals.get("frame").equals("0")){
 			String numFrames = currentVals.get("frame");
 			try{
 			return extractCurrentFrame(Integer.parseInt(numFrames));
 			}
 			catch(NumberFormatException e){
 				throw new DeviceException(numFrames);
 			}
 		}
 
 		return parameters.getNumberDataPoints();
 	}
 	
 	private int extractCurrentFrame(int frameValue){
 		if (isEven(frameValue)) {
 			Integer numFrames = frameValue / 2;
 			return numFrames;
 		}
 		Integer numFrames = (frameValue - 1) / 2;
 		return numFrames;
 	}
 
 	private boolean isEven(int x) {
 		return (x % 2) == 0;
 	}
 
 	public void setOverrunTime(double overrunTime) {
 		this.overrunTime = overrunTime;
 	}
 
 	/**
 	 * The excess time to collect data for above the movement time. This ensures all motor encoder data has been read.
 	 * <p>
 	 * Default is 0.1;
 	 * 
 	 * @return double time in seconds
 	 */
 	public double getOverrunTime() {
 		return overrunTime;
 	}
 
 	public DAServer getDaserver() {
 		return daserver;
 	}
 
 	public void setDaserver(DAServer daserver) {
 		this.daserver = daserver;
 	}
 
 	public void setReturnCountRates(Boolean returnCountRates) {
 		
 		if (!timeChannelRequired && returnCountRates){
 			timeChannelRequired = true;
 			this.extraNames = (String[]) ArrayUtils.addAll(new String[]{"time"}, this.extraNames);
 			this.outputFormat = (String[]) ArrayUtils.addAll(new String[]{this.outputFormat[0]}, this.outputFormat);
 		} else if (timeChannelRequired && this.returnCountRates && !returnCountRates){
 			timeChannelRequired = false;
 			this.extraNames = (String[]) ArrayUtils.remove(this.extraNames, 0);
 			this.outputFormat = (String[]) ArrayUtils.remove(this.outputFormat, 0);
 		}
 		
 		this.returnCountRates = returnCountRates;
 	}
 
 	public Boolean getReturnCountRates() {
 		return returnCountRates;
 	}
 
 	@Override
 	public int maximumReadFrames() throws DeviceException {
 		// as this is only a few integers per frame, its unlikely to ever cause memory issues.
 		return 99999;
 	}
 
	public int getTtlSocket() {
		return ttlSocket;
	}

	public void setTtlSocket(int ttlSocket) {
		this.ttlSocket = ttlSocket;
	}

	
	
 //	public double getDeadTimeInSeconds() {
 //		return deadTimeInSeconds;
 //	}
 //
 //	/**
 //	 * Set the deadtime this system should use after recieving a TTL pulse to trigger the next frame.
 //	 * William Helsby has recommended that this time should be >= 5* the cable delay (at 5ns/m)
 //	 * e.g. for a 20m cable, signal will take 100ns so should have a >500ns deadtime to be safe.
 //	 * <p>
 //	 * default is 500ns.
 //	 * 
 //	 * @param deadTimeInSeconds
 //	 */
 //	public void setDeadTimeInSeconds(double deadTimeInSeconds) {
 //		this.deadTimeInSeconds = deadTimeInSeconds;
 //	}
 
 }
