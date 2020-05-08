 package org.concord.sensor.vernier.goio;
 
 import org.concord.sensor.ExperimentConfig;
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorConfig;
 import org.concord.sensor.device.DeviceReader;
 import org.concord.sensor.device.impl.AbstractSensorDevice;
 import org.concord.sensor.device.impl.SerialPortParams;
 import org.concord.sensor.goio.jna.GoIOLibrary;
 import org.concord.sensor.goio.jna.GoIOSensor;
 import org.concord.sensor.impl.ExperimentConfigImpl;
 import org.concord.sensor.vernier.VernierSensor;
 import org.concord.sensor.vernier.VernierSensorDevice;
 
 public class GoIOSensorDevice extends AbstractSensorDevice implements
 		VernierSensorDevice 
 {
 	GoIOLibrary goio;
 	String errorMessage;
 	GoIOSensor currentGoDevice;
 		
 	public GoIOSensorDevice() {
     	deviceLabel = "GIO";
     	goio = new GoIOLibrary();
     	
     	try {
     		goio.initLibrary();
     	} catch (Throwable t) {
     		errorMessage = "Can't load goio native library";
     		goio = null;
     		t.printStackTrace();
     	}
 	}
 	
 	@Override
 	public void log(String message) {
 		super.log(message);
 	}
 	
 	@Override
 	protected SerialPortParams getSerialPortParams() {
 		return null;
 	}
 
 	@Override
 	protected boolean initializeOpenPort(String portName) {
 		return false;
 	}
 
 	/**
 	 * Because we are using a api that abstracts the usb and/or serial port connection
 	 * we override this to initialize the api instead of opening the port. 
 	 * 
 	 * 
 	 * @see org.concord.sensor.device.impl.AbstractSensorDevice#openPort()
 	 */
 	@Override
 	public void open(String portParams) {
 		if(goio == null){
 			return;
 		}
 		
 		if(goio.init() != 0) {
 			errorMessage = "Can't init go_io, You have another program using the Go device\n";
 		}		
 	}
 
 	/**
 	 * We don't have a port, but openPort is called by the auto configure code.
 	 * 
 	 * @see org.concord.sensor.device.impl.AbstractSensorDevice#openPort()
 	 */
 	protected boolean openPort()
 	{
 		return goio != null;
 	}
 		
 	public void close()
 	{
 		// already closed
 		if(goio == null){
 			return;
 		}
 
 		if(currentGoDevice != null){
 			currentGoDevice.close();
 			currentGoDevice = null;
 		}
 		
 		goio.uninit();
 		goio = null;
 	}
 
 	
 	public boolean canDetectSensors() {		
 		return true;
 	}
 
 	@Override
 	protected boolean hasNonAutoIdSensors() {
 		// FIXME there are non autoid sensors that can be used with the golink
 		//   and old projects used them.  However the current AbstractSensorDevice code doesn't
 		//   handle this case particularly well, so for now we will return false here
 		return false;
 	}
 	
 	public ExperimentConfig configure(ExperimentRequest request) {
 		// FIXME this should reject raw* requests if that don't work with the attached device
 		//   for example raw_*2 shouldn't work with the goTemp or goMotion 
 
 		ExperimentConfig experimentConfig = autoIdConfigure(request);
 		
 		// Because the supported measurement period by the device 
 		// might be different than the requested period the measurement period is set
 		// on the device here
 		currentGoDevice.setMeasurementPeriod(experimentConfig.getPeriod());
 		((ExperimentConfigImpl)experimentConfig).setPeriod((float) currentGoDevice.getMeasurementPeriod());
 
 		
 		return experimentConfig;
 	}
 
 	public ExperimentConfig getCurrentConfig() {
 		if(currentGoDevice != null){
 			currentGoDevice.close();
 			currentGoDevice = null;
 		}
 		
 		currentGoDevice = openGoDevice();
 
 		if(currentGoDevice == null) {
 			// Currently the only way to indicate errors in loading the library or opening the device
 			// is to return null here.  In that case getErrorMessage is called. 
 			return null;
 		}
 
 		ExperimentConfigImpl expConfig = new ExperimentConfigImpl();
 		
 		expConfig.setDeviceName(currentGoDevice.getDeviceLabel());
 
 		expConfig.setExactPeriod(true);
 		expConfig.setPeriod((float)currentGoDevice.getMeasurementPeriod());
 		expConfig.setDataReadPeriod(expConfig.getPeriod());
 
		// TODO we should set the period range since it is known by the device
		//   expConfig.getPeriodRange();
		
 		SensorConfig [] sensorConfigs = new SensorConfig[1];
 		expConfig.setSensorConfigs(sensorConfigs);
 
 		int channelType = VernierSensor.CHANNEL_TYPE_ANALOG;
 		if(currentGoDevice.isGoMotion()) {
 			channelType = VernierSensor.CHANNEL_TYPE_DIGITAL;
 		}
 		VernierSensor sensor = new VernierSensor(this, devService, 0, channelType);
 
		// FIXME if the sensor isn't a known sensor then sensorConfig should return
		// an unknown type of sensor.  It isn't clear if this should return a valid experiment config or not.
 		sensor.setupSensor(currentGoDevice.getAttachedSensorId(), null);
 		sensorConfigs[0] = sensor;
 		
 		expConfig.setValid(true);
 		
 		return expConfig;
 	}
 
 	protected GoIOSensor openGoDevice() {
 		if(goio == null){
 			return null;
 		}
 		GoIOSensor gSensor = goio.getFirstSensor(); 
 		
 		if(gSensor == null){
 			// Set the error message here because returning null here
 			// ought to trigger an error printout, however if isAttached was called
 			// first then this shouldn't happen because that ought to return false
 			// in this case
 			errorMessage = "Cannot find an attached Go IO device";
 			return null;			
 		}
 		
 		// This will lock the device to this thread
 		// In the sensor-native code we then unlock it.  It isn't clear why, 
 		// perhaps so any thread can access it.  A more safe approach would be to use
 		// the a single thread delegator to force all access on one thread.
 		// or to synchronize the access to the gSensor and have it lock it and unlock it 
 		gSensor.open();
 		
 		return gSensor;
 	}
 	
 	public String getErrorMessage(int error) {
 		if(errorMessage == null){
 			return "Unkown Error";
 		}
 		
 		return errorMessage;
 	}
 
 	public String getVendorName() {
 		return "Vernier";		
 	}
 
 	public String getDeviceName() {
 		if(currentGoDevice != null){
 			return currentGoDevice.getDeviceLabel();
 		}
 		
 		return "GoIO";
 	}
 
 	public boolean start() {
 		currentGoDevice.clearIO();
 		
 		// check if a raw_*_2 sensor type was requested
 		// that should select the 10V channel on the go link
 		SensorConfig[] sensorConfigs = currentConfig.getSensorConfigs();
 		if(sensorConfigs != null && sensorConfigs.length > 0){
 			if(((VernierSensor)sensorConfigs[0]).getVernierProbeType() ==
 				VernierSensor.kProbeTypeAnalog10V){
 				currentGoDevice.setAnalogInputChannel(GoIOSensor.ANALOG_CHANNEL_10V);
 			}
 		}
 		
 		currentGoDevice.startMeasurements();		
 		return true;
 	}
 
 	public void stop(boolean wasRunning) {
 		currentGoDevice.stopMeasurements();
 	}
 
 	@Override
 	public boolean isAttached() {
 		if(goio == null){
 			return false;
 		}
 		return goio.isGoDeviceAttached();
 	}
 
 	@Override
 	protected SensorConfig createSensorConfig(int type, int requestPort) 
 	{
 		VernierSensor config = 
 			new VernierSensor(this, devService, 0,
 					VernierSensor.CHANNEL_TYPE_ANALOG);
     	config.setType(type);
     	return config;
 	}
 
 	int [] rawBuffer = new int [200];
 
 	public int read(float[] values, int offset, int nextSampleOffset,
 			DeviceReader reader) {
 		int numMeasurements = currentGoDevice.readRawMeasurements(rawBuffer);
 		if(numMeasurements < 0){
 			errorMessage = "error reading measurements";
 			return -1;
 		}
 
 		SensorConfig [] sensors = currentConfig.getSensorConfigs();
 		VernierSensor sensorConfig = (VernierSensor) sensors[0];
 		int type = sensorConfig.getType();
 
 		// To support multiple devices this should be in a loop over the 
 		// devices and sensorIndex should be incremented
 		int sensorIndex = 0;
 
 		for(int i=0; i<numMeasurements; i++){
 
 			float calibratedData = Float.NaN;
 
 			if(type == SensorConfig.QUANTITY_RAW_DATA_1 ||
 					type == SensorConfig.QUANTITY_RAW_DATA_2){
 				calibratedData = rawBuffer[i];
 			} else {
 				// convert to voltage
 				float voltage = (float) currentGoDevice.convertToVoltage(rawBuffer[i]);
 
 				// scytacki: I would think the GoIO sdk would automatically handle calibration for autoid non smart sensors
 				//   but it doesn't.  Instead we use the calibration code that is in VernierSensor class.
 				if(sensorConfig.getCalibration() != null){
 					calibratedData = sensorConfig.getCalibration().calibrate(voltage);
 				} else {
 					// ask goio sdk to convert value
 					calibratedData = (float) currentGoDevice.calibrateData(voltage);
 				}
 			}
 			values[offset + sensorIndex + i*nextSampleOffset] = calibratedData;
 		}
 
 		return numMeasurements;
 	}
 }
