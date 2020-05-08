 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
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
 
 import gda.device.DeviceException;
 import gda.device.continuouscontroller.HardwareTriggerProvider;
 import gda.device.detector.DAServer;
 import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
 import gda.device.scannable.PositionCallableProvider;
 import gda.device.scannable.PositionInputStream;
 import gda.device.scannable.PositionStreamIndexer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.concurrent.Callable;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 //TODO change class name as it wrongly suggests that it's generic
 public class TFGTriggeredScaler extends TfgScalerWithLogValues implements HardwareTriggeredDetector, 
 PositionCallableProvider<double[]> {
 	static final Logger logger = LoggerFactory.getLogger(TFGTriggeredScaler.class);
 	private int ttlSocket = 0; // the TTL Trig In socket [0-3] default is 0
 	private int numberScanPointsToCollect = 1;
 	private boolean returnCountRates = false;
 	private boolean integrateBetweenPoints;
 	private boolean busy;
 	private AtomicBoolean readingOut = new AtomicBoolean(false);
 	private PositionStreamIndexer<double[]> indexer;
 	private DAServer daserver;
 	private HardwareTriggerProvider triggerProvider;
 
 	public void setHardwareTriggerProvider(HardwareTriggerProvider triggerProvider) {
 		this.triggerProvider = triggerProvider;
 	}
 	
 	@Override
 	public HardwareTriggerProvider getHardwareTriggerProvider() {
 		return triggerProvider;
 	}
 
 	public void setIntegrateBetweenPoints(boolean integrateBetweenPoints) {
 		this.integrateBetweenPoints = integrateBetweenPoints;
 	}
 
 	private void setTimeFrames() throws DeviceException {
 		// tfg setup-trig
 		daserver.sendCommand("tfg setup-trig start ttl" + ttlSocket);				
 		
 		//Send as a single command. Otherwise DAServer reply timeouts are seen and the 3 commands take about 10s!
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("tfg setup-groups ext-start cycles 1"+"\n");
 		buffer.append(numberScanPointsToCollect + " 0.000001 0.00000001 0 0 0 " + (ttlSocket + 8)+"\n");
 		
 		buffer.append("-1 0 0 0 0 0 0");
 		daserver.sendCommand(buffer.toString());
 		daserver.sendCommand("tfg arm");
 	}
 
 	public void clearMemory() throws DeviceException {
 		scaler.clear();
 		scaler.start(); 
 	}
 	
 	public int getNumberFrames() throws DeviceException {
 		String[] cmds = new String[]{"status show-armed","progress","status","full","lap","frame"};
 		HashMap <String,String> currentVals = new HashMap<String,String>();
 		for (String cmd : cmds){
 			currentVals.put(cmd, daserver.sendCommand("tfg read " + cmd).toString());
 			logger.info("tfg read "+ cmd + ": " + currentVals.get(cmd));
 		}
 		
 		if (currentVals.isEmpty())
 			return 0;
 
 		// if started but nothing collected yet
 		if (currentVals.get("status show-armed").equals("EXT-ARMED") /*&& currentVals.get("status").equals("IDLE")*/ )
 			return 0;
 
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
 		return triggerProvider.getNumberTriggers();
 	}
 
 	private boolean isEven(int x) {
 		return (x % 2) == 0;
 	}
 
 	private int extractCurrentFrame(int frameValue){
 		if (isEven(frameValue)) {
 			Integer numFrames = frameValue / 2;
 			return numFrames;
 		}
 		Integer numFrames = (frameValue - 1) / 2;
 		return numFrames;
 	}
 
 	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
 		double[][] frame = readData(startFrame, finalFrame);
 		return readData(startFrame, finalFrame);
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
 	
 	class TfgInputStream implements PositionInputStream<double[]>
 	{
 		private int readOutFromHardwareSoFar = 0;
 		
 		@Override
 		public List<double[]> read(int maxToRead) throws NoSuchElementException, InterruptedException,
 				DeviceException {
 
 			int totalCollectedByHarwdare =  getNumberFrames();
 			double millisToCollectEntireLine = getCollectionTime() * TFGTriggeredScaler.this.numberScanPointsToCollect * 1000;
 			double waitedSoFarMilliSeconds = 0;
 			int waitTime = 1000;
 			
 			// Waits for *first* frame to become available in hardware or millisToCollectEntireLine to have passed
 			while(totalCollectedByHarwdare == 0 && waitedSoFarMilliSeconds <=  millisToCollectEntireLine)
 			{
 				Thread.sleep(waitTime);
 				waitedSoFarMilliSeconds += waitTime;
 				totalCollectedByHarwdare =  getNumberFrames();
 			}
 			//log waitedSoFarMilliSeconds totalCollectedByHarwdare
 			logger.info("waitedSoFarMilliSeconds=" + waitedSoFarMilliSeconds + ", totalCollectedByHarwdare="+totalCollectedByHarwdare);
 			List<double[]> container = new ArrayList<double[]>();
 			//if there is no data to read then treat as an error and thro
 			if(totalCollectedByHarwdare <= 0){
 				logger.info("Nothing collected so returning");
 				return container;
 			}
 			int startFrame = readOutFromHardwareSoFar;
 			int finalFrame = totalCollectedByHarwdare - 1;
 			double dataRead[][] = readData(startFrame, finalFrame); // readData must block until the range is available
 			for(int i = 0  ; i <  dataRead.length; i++) 
 			{
 				setReadingOut(true);
 				container.add(dataRead[i]);
 			}
 			//add log on 	totalCollectedByHarwdare and 	readOutFromHardwareSoFar
 			logger.info("readOutFromHardwareSoFar=" + readOutFromHardwareSoFar + ", totalCollectedByHarwdare="+totalCollectedByHarwdare);
 
 			readOutFromHardwareSoFar = totalCollectedByHarwdare;
 			if(readOutFromHardwareSoFar >= numberScanPointsToCollect)
 				setReadingOut(false);
 			return container;
 		}
 	}
 
 	//TODO what does this do?
 	private void setReadingOut(boolean readingOut) {
 		synchronized (this.readingOut) {
 			this.readingOut.set(readingOut);
 			this.readingOut.notifyAll();
 		}
 	}
 
 	public void waitForReadoutCompletion(){
 		synchronized (readingOut) {
 			while (readingOut.get()) {
 				try{
 				readingOut.wait();
 				}
 				catch (InterruptedException e) {
 					setReadingOut(false);
 				}
 			}
 		}
 	}
 
 	@Override
 	public double[] readout() throws DeviceException {
 		throw new RuntimeException("readout not currently supported");
 	}
 
 	@Override
 	public boolean isBusy()  throws DeviceException{
 		if(busy)
 			return false;
 		return super.isBusy();
 	}
 
 	@Override
 	public Callable<double[]> getPositionCallable() throws DeviceException {
 		return indexer.getPositionCallable();
 	}
 
 	@Override
 	public boolean integratesBetweenPoints() {
 		return integrateBetweenPoints;
 	}
 
 	@Override
 	public void setNumberImagesToCollect(int numberImagesToCollect) {
 		this.numberScanPointsToCollect = numberImagesToCollect;
 	}
 	
 	@Override
 	public void collectData() throws DeviceException {
 		clearMemory();
 		setTimeFrames();
 	}
 	
 	@Override
 	public void atScanLineStart()throws DeviceException
 	{
 		waitForReadoutCompletion();
 		try {
 			clearMemory();
 			setTimeFrames();	
 
 			this.indexer  = new PositionStreamIndexer<double[]>(new TfgInputStream());
 			busy = true;
 		} catch (Exception e) {
 			busy = false;
 			logger.error("error While setting up detector for hardware triggering", e);
 			throw new DeviceException("error While setting up detector for hardware triggering" , e);
 		}
 	}
 
 	public DAServer getDaserver() {
 		return daserver;
 	}
 
 	public void setDaserver(DAServer daserver) {
 		this.daserver = daserver;
 	}
 
 	public int getTtlSocket() {
 		return ttlSocket;
 	}
 
 	public void setTtlSocket(int ttlSocket) {
 		this.ttlSocket = ttlSocket;
 	}
 }
