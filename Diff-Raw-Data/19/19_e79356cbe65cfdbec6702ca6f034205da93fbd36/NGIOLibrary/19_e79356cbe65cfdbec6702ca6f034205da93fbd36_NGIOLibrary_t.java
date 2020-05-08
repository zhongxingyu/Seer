 package org.concord.sensor.labquest.jna;
 
 import com.sun.jna.Library;
 import com.sun.jna.Pointer;
 import com.sun.jna.Structure;
 import com.sun.jna.ptr.ByteByReference;
 import com.sun.jna.ptr.DoubleByReference;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.ShortByReference;
 
 public interface NGIOLibrary extends Library {
 	public final static int STRUCTURE_ALIGNMENT = Structure.ALIGN_NONE; 
 	
 	public final static int COMM_TRANSPORT_USB = 1;
 	public final static int COMM_TRANSPORT_SERIAL = 2;
 	public final static int COMM_TRANSPORT_BLUETOOTH = 3;
 	public final static int COMM_TRANSPORT_BLUETOOTH_ALT1 = 4;
 	public final static int COMM_TRANSPORT_WIRELESS_80211_X = 5;
 	public final static int COMM_TRANSPORT_HARDWIRE_LAN = 6;
 
 	public final static int DEVTYPE_LABPRO = 1;
 	public final static int DEVTYPE_GO_TEMP = 2;
 	public final static int DEVTYPE_GO_LINK = 3;
 	public final static int DEVTYPE_GO_MOTION = 4;
 	public final static int DEVTYPE_LABPRO2 = 5;
 	public final static int DEVTYPE_WDSS = 6;
 	public final static int DEVTYPE_NI_SENSORDAQ = 7;
 	public final static int DEVTYPE_LABPRO2_FILE_SERVER = 8;
 	public final static int DEVTYPE_LABPRO2_AUDIO = 9;
 	public final static int DEVTYPE_DAISYCHAIN_TEST = 10;
 
 	public final static int MAX_SIZE_DEVICE_NAME = 220;
 	public final static int GRAB_DAQ_TIMEOUT = 12000;
 	
 	public final static int TCPIP_PORT_LABPRO2 = 0x9500;
 
 	public final static int TIMEOUT_MS_DEFAULT = 2000;
 	public final static int TIMEOUT_MS_READ_DDSMEMBLOCK = 2000;
 	public final static int TIMEOUT_MS_WRITE_DDSMEMBLOCK = 4000;
 	
 	public final static int DEVICE_STATUS_MASK_OPEN = 1;
 	public final static int DEVICE_STATUS_MASK_OPENED_BY_THIS_CLIENT = 2;
 	
	public final static int TRACE_SEVERITY_LOWEST = 1;
	public final static int TRACE_SEVERITY_LOW = 10;
	public final static int TRACE_SEVERITY_MEDIUM = 50;
	public final static int TRACE_SEVERITY_HIGH = 100;
	
 	public final static byte FALSE = (byte)0;
 	public final static byte TRUE = (byte)1;
 	
 	Pointer init();
 	int uninit(Pointer libHandle); 
 	int getDLLVersion(Pointer libHandle, ShortByReference majorVersion, 
 			ShortByReference minorVersion);
 	
 	int searchForDevices(Pointer handle, int deviceType, 
 			int commTransportId, Pointer params, IntByReference deviceListSignature);
 	
 	Pointer openDeviceListSnapshot(Pointer handle,int deviceType,
 			IntByReference numDevices, IntByReference deviceListSignature);
 	
 	int closeDeviceListSnapshot(Pointer handle);
 	
 	int deviceListSnapshot_GetNthEntry(Pointer hDeviceList,
 			int n, byte [] devNameBuf, int bufSize,
 			IntByReference pDeviceStatusMask);
 
 	Pointer device_Open(Pointer libHandle, String deviceName,
 			byte bDemandExclusiveOwnership);
 	
 	int device_Close(Pointer hDevice);
 	
 	// TIMEOUT_MS_DEFAULT is recommened
 	// this appears to always return false even if the labquest is showing data.
 	int device_IsRemoteCollectionActive(Pointer hDevice, 
 			ByteByReference remoteCollectionActive, int timeoutMs);
 
 	// GRAB_DAQ_TIMEOUT is recommended.
 	int device_AcquireExclusiveOwnership(Pointer hDevice, int timeoutMs);
 	
 	// this isn't supported by the current library 
 	// int isDeviceExclusivelyOurs(Pointer hDevice, 
 	//		ByteByReference exclusiveOwnershipFlag);
 	
 	// untested
 	int device_GetOpenDeviceName(Pointer hDevice, byte [] pDeviceName, int bufSize);
 
 	// untested
 	int device_Lock(Pointer hDevice, int timeoutMs);
 	
 	// untested
 	int device_Unlock(Pointer hDevice);
 	
 	// untested
 	int device_ClearIO(Pointer hDevice, byte channel);
 
 	// A Pointer is returned instead of a Structure, because this method can return
 	// a different struct if there is an error.
 	int device_SendCmdAndGetResponse(Pointer hDevice, byte cmd, Structure pParams,
 			int nParamBytes, Pointer pRespBuf, IntByReference pnRespBytes, int timeoutMs); 
 
 	// untested
 	// need to possibly add in the NGIO_PTR structure for pParams
 	int device_SendCmd(Pointer hDevice,	byte cmd, Pointer pParms, int nParamBytes,
 			IntByReference pSignature);
 	
 	// untested
 	int device_GetNextResponse(Pointer hDevice,	byte [] pRespBuf, IntByReference pnRespBytes,
 			ByteByReference pCmd, ByteByReference pErrRespFlag, IntByReference pSignature,
 			int nTimeoutMs);
 	
 	// untested
 	double device_GetMeasurementTick(Pointer hDevice, byte channel);
 	
 	// untested
 	double device_GetMinimumMeasurementPeriod(Pointer hDevice, byte channel);
 	
 	// untested 
 	double device_GetMaximumMeasurementPeriod(Pointer hDevice, byte channel);
 	
 	// untested
 	int device_SetMeasurementPeriod(Pointer hDevice, byte channel,
 			double desiredPeriod, int timeoutMs);
 
 	// untested
 	int device_GetMeasurementPeriod(Pointer hDevice, byte channel,
 			DoubleByReference pPeriod, int timeoutMs); 
 	
 	// untested
 	int device_GetNumMeasurementsAvailable(Pointer hDevice, byte channel);
 	
 	//Photogates return the following raw measurements:
 	public final static int MEAS_PHOTOGATE_BLOCKED = 1;
 	public final static int MEAS_PHOTOGATE_UNBLOCKED = 0;
 
 	//Motion Detectors return the following raw measurements:
 	public final static int MEAS_MOTION_DETECTOR_PING = 0;
 	public final static int MEAS_MOTION_DETECTOR_ECHO = 1;
 
 	//A false echo is reported if no echo is detected after a ping.
 	public final static int MEAS_MOTION_DETECTOR_FALSE_ECHO = 2;
 	
 	int device_ReadRawMeasurements(Pointer hDevice, byte channel,
 			int [] pMeasurementsBuf, long [] pTimeStamps,
 			int maxCount);
 	
 	// untested
 	int device_GetLatestRawMeasurement(Pointer hDevice, byte channel);
 	
 	// untested
 	float device_ConvertToVoltage(Pointer hDevice, byte channel,
 			int rawMeasurement,  int probeType);
 	
 	// untested	
 	float device_CalibrateData(Pointer hDevice, byte channel,
 			float volts);
 	
 	// untested
 	float device_CalibrateData2(Pointer hDevice, byte channel,
 			int rawMeasurement);
 	
 	// untested
 	int device_GetProbeType(Pointer hDevice, byte channel);
 	
 	// untested
 	int device_DDSMem_ReadRecord(Pointer hDevice, byte channel,
 			byte strictDDSValidationFlag, int timeoutMs);
 	
 	// untested
 	// need to implement DDSMem struct for pRec
 	int device_DDSMem_SetRecord(Pointer hDevice, byte channel,
 			Pointer pRec);
 	
 	// untested
 	int device_DDSMem_GetRecord(Pointer hDevice, byte channel,
 			GSensorDDSMem sensorDDSMem);
 
 	// untested
 	int device_DDSMem_CalculateChecksum(Pointer hDevice, byte channel,
 			ByteByReference pChecksum);
 	
	// untested
	int diags_SetDebugTraceThreshold(Pointer hLibrary, int threshold);
 	
 }
