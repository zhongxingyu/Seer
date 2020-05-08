 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.sensor.impl;
 
 import org.concord.framework.data.stream.DataStreamDescription;
 import org.concord.framework.data.stream.DataStreamEvent;
 import org.concord.framework.text.UserMessageHandler;
 import org.concord.sensor.ExperimentConfig;
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorConfig;
 import org.concord.sensor.SensorDataProducer;
 import org.concord.sensor.device.DeviceReader;
 import org.concord.sensor.device.SensorDevice;
 
 
 public abstract class SensorDataProducerImpl
 	implements SensorDataProducer, DeviceReader, TickListener
 {
 	public int		startTimer =  0;
 	protected Ticker ticker = null;
 	protected UserMessageHandler messageHandler;
 	
 	public DataStreamDescription dDesc = new DataStreamDescription();
 	public DataStreamEvent	processedDataEvent = new DataStreamEvent();
 
 	protected float [] processedData;
 	private static final int DEFAULT_BUFFERED_SAMPLE_NUM = 1000;
 	
 	int timeWithoutData = 0;
 	protected String [] okOptions;
 	protected String [] continueOptions;	
	public final static int DATA_TIME_OUT = 40;
 	private boolean inDeviceRead;
 	private int totalDataRead;
 	private SensorDevice device;
 	private ExperimentConfig experimentConfig = null;
     protected float dataTimeOffset;
     
 	
 	public SensorDataProducerImpl(SensorDevice device, Ticker t, UserMessageHandler h)
 	{
 		this.device = device;
 		continueOptions = new String [] {"Continue"};
 		okOptions = new String [] {"Ok"};
 		
 		ticker = t;
 		
 		
 		messageHandler = h;
 		
 		processedData = new float[DEFAULT_BUFFERED_SAMPLE_NUM];
 		processedDataEvent.setData(processedData);
 		processedDataEvent.setSource(this);
 		processedDataEvent.setDataDescription(dDesc);
 		dataTimeOffset = 0;
 	}
 
 	public void tick()
 	{
 	    int ret;
 
 	    /*
 		if(messageHandler != null) messageHandler.showOptionMessage(null, "Message test",			
 				continueOptions, continueOptions[0]);
 	    */
 	    
 	    // reset the total data read so we can track data coming from
 	    // flushes
 	    totalDataRead = 0;
 
 		dDesc.setDataOffset(0);
 
 	    // track when we are in the device read so if flush
 	    // is called outside of this we can complain
 	    inDeviceRead = true;
 	    ret = device.read(processedData, 0, 
 	    			dDesc.getChannelsPerSample(), this);
 	    inDeviceRead = false;
 	    
 	    if(ret < 0) {
 			stop();
 			String devError = device.getErrorMessage(ret);
 			if(devError == null) {
 				devError = "unknown";
 			}
 			String message = "Data Read Error: " + devError;
 			if(messageHandler != null) {
 				messageHandler.showOptionMessage(message, "Interface Error",
 						continueOptions, continueOptions[0]);
 			}
 			return;
 	    }
 	    
 	    totalDataRead += ret;
 	    if(totalDataRead == 0) {
 			// we didn't get any data. 
 	    	// keep track of this so we can report there is
 	    	// is a problem.  If this persists too long
 			timeWithoutData++;
 			if(timeWithoutData > DATA_TIME_OUT){
 				stop();
 				if(messageHandler != null) {
 					messageHandler.showOptionMessage("Data Read Error: " +
 							 "possibly no interface " +
 							 "connected", "Interface Error",
 							 continueOptions, continueOptions[0]);					
 				}
 			}
 			return;
 	    }
 	    
 	    // We either got data or there was an error
 		timeWithoutData = 0;
 
 		if(ret > 0){
 			// There was some data that didn't get flushed during the read
 			// so send this out to our listeners.
 			processedDataEvent.setNumSamples(ret);
 			notifyDataListenersReceived(processedDataEvent);				
 		} 	
 	}
 	
 	public void tickStopped()
 	{
 	    deviceStop(false);
 	}
 	
 	/*
 	 * This is a helper method for slow devices.  It be called within deviceRead.
 	 * If the data should be written into the values array passed to deviceRead
 	 * the values read from the offset passed in until offset+numSamples will 
 	 * be attempted to be flushed.
 	 * the method returns the new offset into the data. 
 	 * 
 	 * You don't need to call this, but if your device is going to work on a slow
 	 * computer (for example an older palm) then you will probably have to use
 	 * this method.  Otherwise you will build up too much data to be processed later
 	 * and then while all that data is being processed the serial buffer will overflow.
 	 * 
 	 * Instead this method will partially process the data.  This will give the device
 	 * a better chance to "get ahead" of the serial buffer.  Once the device has gotten
 	 * far enough ahead of the serial buffer it can return from deviceRead the
 	 * data will be fully processed.
 	 */
 	public int flushData(int numSamples)
 	{
 		if(!inDeviceRead) {
 			// flush should only be called inside of a device read
 
 			// error we need an assert here but we are in waba land 
 			// so no exceptions or asserts instead we force
 			// a null pointer exception.  Superwaba supports
 			// some exceptions now so we should refactor this 
 			// to use exceptions
 			Object test = null;
 			test.equals(test);
 		}
 		
 		processedDataEvent.setNumSamples(numSamples);
 		notifyDataListenersReceived(processedDataEvent);
 		dDesc.setDataOffset(dDesc.getDataOffset()+numSamples);
 		
 		totalDataRead += numSamples;
 		
 		return 0;
 	}
 	
 	protected int getBufferedSampleNum()
 	{
 		return DEFAULT_BUFFERED_SAMPLE_NUM;
 	}
 	
 	/**
 	 * This method is called by users of the sensor
 	 * device.  After the producer is created this method
 	 * is called.  In some cases it is called before every
 	 * start().
 	 * 
 	 * It might take a while to return.  It might also fail
 	 * in which case it will return null, or it will return
 	 * a config for which getValid() return false.
 	 */
 	public ExperimentConfig configure(ExperimentRequest request)
 	{
 	    if(ticker.isTicking()) {
 	        ticker.stopTicking(this);
 	    }
 	    
 		ExperimentConfig actualConfig = device.configure(request);
 		if(actualConfig == null || !actualConfig.isValid()) {
 			// prompt the user because the attached sensors do not
 			// match the requested sensors.
 			// It is in this case that we need more error information
 			// from the device.  I suppose one solution is to get a 
 			// listing of the actual sensors and then do the comparision
 			// here in a general way.
 			// That will work if the interface can auto identify sensors
 			// if it can't then how would it know they are incorrect???
 			// I guess in case it would have to check if the returned values
 			// are valid.  Othwise it will just have to trust the student and
 			// the experiments will have to be designed (technical hints) to help
 			// the student figure out what is wrong.
 			// So we will try to tackle the general error cases here :S
 			// But there is now a way for the device to explain why the configuration
 			// is invalid.
 			if(actualConfig == null) {
 				// we don't have any config.  this should mean there was
 				// a more serious error talking to the device.  Either it
 				// isn't there, our communiction channel is messed up, or
 				// the device is messed up.
 				if(messageHandler != null) {
 					// get the error message from the device
 					String devErrStr = device.getErrorMessage(0);
 					if(devErrStr == null) {
 						devErrStr = "unknown";
 					}
 					
 					messageHandler.showMessage("Device error: " + devErrStr, "Alert");
 				}
 			} else {
 				// we have a valid config so that should mean the device
 				// can detect the sensors, but the ones it found didn't 
 				// match the request so it set the config to invalid
 				if(messageHandler != null) {
 					messageHandler.showMessage("Wrong sensors attached", "Alert");
 				}				
 
 				// System.err.println("  device reason: " + actualConfig.getInvalidReason());
 				SensorConfig [] sensorConfigs = actualConfig.getSensorConfigs();
 				// System.err.println("  sensor attached: " + sensorConfigs[0].getType());
 			}
 						
 			// Maybe should be a policy decision somewhere
 			// because maybe you would want to just return the
 			// currently attached setup
 		}
 
 	    experimentConfig = actualConfig;
 		DataStreamDescUtil.setupDescription(dDesc, request, actualConfig);
 
 		DataStreamEvent event = 
 		    new DataStreamEvent(DataStreamEvent.DATA_DESC_CHANGED, null, dDesc);
 		event.setSource(this);
 		notifyDataListenersEvent(event);
 		
 		return actualConfig;
 	}
 	
 	public final void start()
 	{
 	    if(ticker == null) {
 	        throw new RuntimeException("Null ticker object in start");
 	    }
 	    
 	    if(ticker.isTicking()) {
 	        // this is an error some other object is using
 	        // this ticker, or we are trying to start it twice
 	        throw new RuntimeException("Trying to start device twice");
 	    }
 	    
 	    if(device == null) {
 	        throw new RuntimeException("Null device in start");
 	    }
 	    
 		if(!device.start()) {
 			// cannot start device
 			if(messageHandler != null) {
 				String devMessage = device.getErrorMessage(0);
 				if(devMessage == null) {
 					devMessage = "unknown";
 				}
 				
 				messageHandler.showMessage("Can't start device: " + devMessage,
 						"Device Start Error");
 			}
 			return;
 		}
 		
 		timeWithoutData = 0;
 
 		startTimer = ticker.currentTimeMillis();
 		int dataReadMillis = (int)(experimentConfig.getDataReadPeriod()*1000.0);
 		// Check if the data read millis is way below the experiment period
 		// if it is then tick code will time out incorrectly.  So 
 		// we try to correct it so that the read time is no less than
 		// 1/5th of the period.
 		int autoDataReadMillis = (int)(experimentConfig.getPeriod()*1000/5);
 		if(dataReadMillis < autoDataReadMillis){
 		    dataReadMillis = autoDataReadMillis;
 		}
 		ticker.startTicking(dataReadMillis, this);
 
 	}
 	
 	/**
 	 *  This doesn't really need to do anything if
 	 * the sensor isn't storing any cache.
 	 * however for sensors that need to put timestamps
 	 * on the data this method should be used to 
 	 * reset the timestamp
 	 */
 	public final void reset()
 	{	
 	    dataTimeOffset = 0;
 	}
 	
 	public final void stop()
 	{
 		boolean ticking = ticker.isTicking();
 
 		// just to make sure
 		// even if we are not ticking just incase
 		ticker.stopTicking(this);
 
 		deviceStop(ticking);
 	}
 
 	
 	protected void deviceStop(boolean ticking)
 	{
 		device.stop(ticking);
 
 		// FIXME we should get the time the device sends back
 		// instead of using our own time.
 		dataTimeOffset += (ticker.currentTimeMillis() - startTimer) / 1000f;	    
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.SensorDataProducer#isAttached()
 	 */
 	public boolean isAttached()
 	{
 	    if(ticker != null && ticker.isTicking()) {
 	        // this will have the ticker send a tickStopped event 
 	        // which should cause us to stop the device
 	        ticker.stopTicking(null);
 	    }
 	    
 		return device.isAttached();
 	}
 	
 	public boolean isRunning()
 	{
 		if(ticker == null) {
 			return false;
 		}
 		
 		return ticker.isTicking();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.SensorDataProducer#canDetectSensors()
 	 */
 	public boolean canDetectSensors()
 	{
 		// TODO Auto-generated method stub
 		return device.canDetectSensors();
 	}
 	
 	public ExperimentConfig getCurrentConfig()
 	{
 		return device.getCurrentConfig();
 	}
 	
 	public void close()
 	{
 		device.close();
 	}
 	
 	public final DataStreamDescription getDataDescription()
 	{
 		return dDesc;
 	}
 		
 	public abstract void notifyDataListenersEvent(DataStreamEvent e);
 	
 	public abstract void notifyDataListenersReceived(DataStreamEvent e);
 }
