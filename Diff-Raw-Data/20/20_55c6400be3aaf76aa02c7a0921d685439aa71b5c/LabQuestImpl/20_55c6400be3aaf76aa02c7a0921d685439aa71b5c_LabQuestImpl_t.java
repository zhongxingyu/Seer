 package org.concord.sensor.labquest.jna;
 
 import org.concord.sensor.labquest.jna.NGIOSourceCmds.NGIOGetSensorIdCmdResponsePayload;
 import org.concord.sensor.labquest.jna.NGIOSourceCmds.NGIOGetSensorIdParams;
 import org.concord.sensor.labquest.jna.NGIOSourceCmds.NGIOSetSamplingModeParams;
 import org.concord.sensor.labquest.jna.NGIOSourceCmds.NGIOSetSensorChannelEnableMaskParams;
 
 import com.sun.jna.Memory;
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 import com.sun.jna.Structure;
 import com.sun.jna.ptr.ByteByReference;
 import com.sun.jna.ptr.IntByReference;
 
 public class LabQuestImpl implements LabQuest 
 {
 
 	private NGIOLibrary ngio;
 	private Pointer hDevice;
 	private int deviceType;
 
 	LabQuestImpl(NGIOLibrary ngio) {
 		this.ngio = ngio;
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#open(java.lang.String, com.sun.jna.Pointer)
 	 */
 	public void open(String deviceName, Pointer hLibrary) throws LabQuestException
 	{
 		// the demandExclusive ownership is not currently supported
 		hDevice = ngio.device_Open(hLibrary, deviceName, NGIOLibrary.FALSE);
 		if(hDevice == null){
 			throw new LabQuestException();
 		}
 		
 		IntByReference pDeviceType = new IntByReference();		
 		int ret = ngio.getDeviceTypeFromDeviceName(deviceName, pDeviceType);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 		deviceType = pDeviceType.getValue();
 	}
 
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#close()
 	 */
 	public void close() throws LabQuestException
 	{
 		System.err.println("LabQuest: closing");
 		int ret = ngio.device_Close(hDevice);
 		System.err.println("LabQuest: called close");
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 		System.err.println("LabQuest: claimed to close");
 
 	}
 	
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#isRemoteCollectionActive()
 	 */
 	public boolean isRemoteCollectionActive() throws LabQuestException
 	{
 		if(deviceType == NGIOLibrary.DEVTYPE_LABQUEST_MINI){
 			return false;
 		}
 		
 		ByteByReference remoteCollectionActive = new ByteByReference();
 		int ret = ngio.device_IsRemoteCollectionActive(hDevice, remoteCollectionActive, 
 				NGIOLibrary.TIMEOUT_MS_DEFAULT);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 		return remoteCollectionActive.getValue() != 0;
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#acquireExclusiveOwnership()
 	 */
 	public void acquireExclusiveOwnership() throws LabQuestException
 	{
 		if(deviceType == NGIOLibrary.DEVTYPE_LABQUEST_MINI){
 			return;
 		}
 		
 		int ret = ngio.device_AcquireExclusiveOwnership(hDevice, NGIOLibrary.GRAB_DAQ_TIMEOUT);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 	}
 
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#getSensorName()
 	 */
 	public String getSensorName(byte channel) throws LabQuestException {
 		int sensorId = getSensorId(channel);
 		System.out.println("found sensor: " + sensorId);			
 
 		if(sensorId >= 20){
 			ddsMemReadRecord(channel, false);
 			GSensorDDSMem sensorDDSMem = ddsMemGetRecord(channel);
 
 			String name = Native.toString(sensorDDSMem.SensorLongName);
 			return name;
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#getSensorId(byte)
 	 */
 	public int getSensorId(byte channel) throws LabQuestException
 	{
 		NGIOGetSensorIdParams sensorIdParams = new NGIOSourceCmds.NGIOGetSensorIdParams();
 		NGIOGetSensorIdCmdResponsePayload sensorIdCmdResponsePayload = 
 			new NGIOSourceCmds.NGIOGetSensorIdCmdResponsePayload();
 		
 		sensorIdParams.channel = channel;
 		try {
 			sendCmdAndGetResponse(NGIOSourceCmds.CMD_ID_GET_SENSOR_ID, sensorIdParams, 
 					sensorIdCmdResponsePayload);
 		} catch (NGIOException e) {
 			throw new LabQuestException(e);
 		}
 		
 		byte[] sensorId = sensorIdCmdResponsePayload.sensorId;
 		return intFromBytes(sensorId);					
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#ddsMemReadRecord(byte, boolean)
 	 */
 	public void ddsMemReadRecord(byte channel, boolean strict) throws LabQuestException
 	{
 		byte strictByte = strict ? NGIOLibrary.TRUE : NGIOLibrary.FALSE;
 		int ret = ngio.device_DDSMem_ReadRecord(hDevice, channel, 
 				strictByte, NGIOLibrary.TIMEOUT_MS_READ_DDSMEMBLOCK);
 		if(ret != 0){
			throw new LabQuestException("ddsMemReadRecord returned: " + ret);
 		}
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#ddsMemGetRecord(byte)
 	 */
 	public GSensorDDSMem ddsMemGetRecord(byte channel) throws LabQuestException
 	{
 		GSensorDDSMem sensorDDSMem = new GSensorDDSMem();
 		
 		int ret = ngio.device_DDSMem_GetRecord(hDevice, NGIOSourceCmds.CHANNEL_ID_ANALOG1, 
 				sensorDDSMem);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 		return sensorDDSMem;
 	}
 
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#ddsMemCalculateChecksum(byte)
 	 */
 	public byte ddsMemCalculateChecksum(byte channel)
 	{
 		ByteByReference pChecksum = new ByteByReference();
 		int ret = ngio.device_DDSMem_CalculateChecksum(hDevice, channel, pChecksum);
 		if(ret != 0){
 			return 0;
 			// throw new LabQuestException();
 		}
 		return pChecksum.getValue();
 	}
 
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#setMeasurementPeriod(byte, double)
 	 */
 	public void setMeasurementPeriod(byte channel, double desiredPeriod) throws LabQuestException
 	{
 		int ret = ngio.device_SetMeasurementPeriod(hDevice, channel, desiredPeriod, 
 				NGIOLibrary.TIMEOUT_MS_DEFAULT);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#setSensorChannelEnableMask(int)
 	 */
 	public void setSensorChannelEnableMask(int mask) throws LabQuestException
 	{
 		NGIOSetSensorChannelEnableMaskParams channelEnableMaskParams = 
 			new NGIOSourceCmds.NGIOSetSensorChannelEnableMaskParams();
 		bytesFromInt(mask, channelEnableMaskParams.enableSensorChannels);
 		try {
 			sendCmdAndGetResponse(NGIOSourceCmds.CMD_ID_SET_SENSOR_CHANNEL_ENABLE_MASK, 
 					channelEnableMaskParams, null);
 		} catch (NGIOException e) {
 			throw new LabQuestException(e);
 		}		
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#clearIO(byte)
 	 */
 	public void clearIO(byte channel) throws LabQuestException
 	{
 		int ret = ngio.device_ClearIO(hDevice, channel);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#startMeasurements()
 	 */
 	public void startMeasurements() throws LabQuestException
 	{
 		try {
 			sendCmdAndGetResponse(NGIOSourceCmds.CMD_ID_START_MEASUREMENTS, 
 					null, null);
 		} catch (NGIOException e) {
 			throw new LabQuestException(e);
 		}		
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#stopMeasurements()
 	 */
 	public void stopMeasurements() throws LabQuestException
 	{
 		try {
 			sendCmdAndGetResponse(NGIOSourceCmds.CMD_ID_STOP_MEASUREMENTS, 
 					null, null);
 		} catch (NGIOException e) {
 			throw new LabQuestException(e);
 		}
 
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#getNumberOfMeasurementsAvailable(byte)
 	 */
 	public int getNumberOfMeasurementsAvailable(byte channel) throws LabQuestException
 	{
 		
 		int ret = ngio.device_GetNumMeasurementsAvailable(hDevice, channel);
 		if(ret < 0){
 			throw new LabQuestException();			
 		}
 		return ret;
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#readRawMeasurementsAnalog(byte, int[], int)
 	 */
 	public int readRawMeasurementsAnalog(byte channel, int [] pMeasurementsBuf, int maxCount) 
 		throws LabQuestException
 	{
 		int numMeasurements = ngio.device_ReadRawMeasurements(hDevice, channel, 
 				pMeasurementsBuf, null, maxCount);
 		if(numMeasurements < 0){
 			throw new LabQuestException();
 		}
 		return numMeasurements;
 		
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#readRawMeasurementsMotion(byte, int[], long[], int)
 	 */
 	public int readRawMeasurementsMotion(byte channel, int [] pMeasurementsBuf, 
 			long [] timestampBuf, int maxCount) 
 	throws LabQuestException
 	{
 		int numMeasurements = ngio.device_ReadRawMeasurements(hDevice, channel, 
 				pMeasurementsBuf, timestampBuf, maxCount);
 		if(numMeasurements < 0){
 			throw new LabQuestException();
 		}
 		return numMeasurements;
 
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#convertToVoltage(byte, int, int)
 	 */
 	public float convertToVoltage(byte channel, int rawMeasurement, int probeType)
 	{
 		return ngio.device_ConvertToVoltage(hDevice, channel,
 				rawMeasurement,  probeType);
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#setAnalogInput(byte, byte)
 	 */
 	public void setAnalogInput(byte channel, byte analogInput) throws LabQuestException
 	{
 		NGIOSourceCmds.NGIOSetAnalogInputParams setAnalogInputParams =
 			new NGIOSourceCmds.NGIOSetAnalogInputParams();
 		setAnalogInputParams.channel = channel;
 		setAnalogInputParams.analogInput = analogInput;
 		try {
 			sendCmdAndGetResponse(NGIOSourceCmds.CMD_ID_SET_ANALOG_INPUT, 
 					setAnalogInputParams, null);
 		} catch (NGIOException e) {
 			throw new LabQuestException(e);
 		}
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#setSamplingMode(byte, byte)
 	 */
 	public void setSamplingMode(byte channel, byte samplingMode) throws LabQuestException
 	{
 		NGIOSetSamplingModeParams setSamplingModeParams = 
 			new NGIOSourceCmds.NGIOSetSamplingModeParams();
 		setSamplingModeParams.channel = channel;
 		setSamplingModeParams.samplingMode = samplingMode;
 		try {
 			sendCmdAndGetResponse(NGIOSourceCmds.CMD_ID_SET_SAMPLING_MODE, 
 					setSamplingModeParams, null);
 		} catch (NGIOException e) {
 			throw new LabQuestException(e);
 		}
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#calibrateData2(byte, int)
 	 */
 	public float calibrateData2(byte channel, int rawValue)
 	{
 		return ngio.device_CalibrateData2(hDevice, channel, rawValue);
 	}
 	
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#printAttachedSensors()
 	 */
 	public void printAttachedSensors() throws LabQuestException {
 		for(byte i=1; i<7; i++){
 			int sensorId = getSensorId(i);
 			System.out.println("found sensor: " + sensorId);			
 
 			if(sensorId >= 20){
 				ddsMemReadRecord(i, false);
 				GSensorDDSMem sensorDDSMem = ddsMemGetRecord(i);
 
 				String name = Native.toString(sensorDDSMem.SensorLongName);
 				System.out.println("  name: " + name);
 				System.out.println("  operationType: " + sensorDDSMem.OperationType);
 			}
 		}
 	}
 
 	/**
 	 * @see org.concord.sensor.labquest.jna.LabQuest#sendCmdAndGetResponse(byte, com.sun.jna.Structure, com.sun.jna.Structure)
 	 */
 	public void sendCmdAndGetResponse(byte cmd, Structure params, Structure response) 
 	throws NGIOException
 	{		
 		int paramsSize = 0;
 		if(params != null){
 			paramsSize = params.size();
 		}
 
 		Pointer responsePtr = null;
 		IntByReference responseSizeReference = new IntByReference();		
 		if(response == null){
 			responsePtr = new Memory(1);
 			responseSizeReference.setValue(1);
 		} else {
 			// this write isn't completely necessary but it makes sure the memory is allocated
 			// for the reponse structure.
 			response.write();
 			responsePtr = response.getPointer();
 			responseSizeReference.setValue(response.size());
 		}
 
 		int ret = ngio.device_SendCmdAndGetResponse(hDevice, cmd, 
 				params, paramsSize, responsePtr, 
 				responseSizeReference, NGIOLibrary.TIMEOUT_MS_DEFAULT);
 		// if there is an error then the returned structure is NGIODefaultCmdResponse which 
 		// has one byte which indicates the status
 		if(ret != 0){
 			if(responseSizeReference.getValue() == 1){
 				byte status = responsePtr.getByte(0);			
 				System.err.println("error sending command: " + status);
 				throw new NGIOException(status);
 			}
 			
 			throw new NGIOException((byte) -1);
 		}
 
 		if(response != null){
 			response.read();
 		}		
 	}
 	
 	private static int intFromBytes(byte [] buf)
 	{
 		return buf[0] | (buf[1] << 8) | (buf[2] << 16) | (buf[3] << 24);
 	}
 	
 	private static void bytesFromInt(int val, byte [] buf)
 	{
 		buf[0] = (byte)(val & 0xFF);
 		buf[1] = (byte)((val & 0xFF00) >> 8);
 		buf[2] = (byte)((val & 0xFF0000) >> 16);
 		buf[3] = (byte)((val & 0xFF000000) >>> 24);
 	}
 }
