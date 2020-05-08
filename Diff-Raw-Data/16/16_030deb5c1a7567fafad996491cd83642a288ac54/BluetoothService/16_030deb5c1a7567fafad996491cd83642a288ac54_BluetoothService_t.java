 /**
  * 
  */
 package com.daisyworks.service;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.bluetooth.BluetoothStateException;
 import javax.bluetooth.DataElement;
 import javax.bluetooth.DeviceClass;
 import javax.bluetooth.DiscoveryAgent;
 import javax.bluetooth.DiscoveryListener;
 import javax.bluetooth.LocalDevice;
 import javax.bluetooth.RemoteDevice;
 import javax.bluetooth.ServiceRecord;
 import javax.bluetooth.UUID;
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.flex.messaging.MessageTemplate;
 import org.springframework.flex.remoting.RemotingDestination;
 import org.springframework.flex.remoting.RemotingInclude;
 import org.springframework.stereotype.Service;
 
 import com.daisyworks.common.intelhex.BufferedIntelHexReader;
 import com.daisyworks.common.stk500.io.AsyncInputStreamThread;
 import com.daisyworks.common.stk500.STK500Event;
 import com.daisyworks.common.stk500.STK500EventListener;
 import com.daisyworks.common.stk500.STK500v1;
 import com.daisyworks.exception.ExceptionUtil;
 import com.daisyworks.model.Device;
 
 /**
  * Handles all the Bluetooth stuff
  */
 @Service
 @RemotingDestination(channels = { "my-amf" })
 public final class BluetoothService implements DiscoveryListener {
 
 	private static final Logger LOGGER = Logger
 			.getLogger(BluetoothService.class);
 
 	// object used for waiting
 	private static Object deviceLock = new Object();
 	private static Object serviceLock = new Object();
 
 	// map of discovered devices; key = device address
 	private final ConcurrentMap<String, RemoteDevice> deviceMap = new ConcurrentHashMap<String, RemoteDevice>();
 	// map of service urls; key = device address
 	private final ConcurrentMap<String, String> serviceMap = new ConcurrentHashMap<String, String>();
 	// map of device address to connection
 	private final ConcurrentMap<String, StreamConnection> connectionMap = new ConcurrentHashMap<String, StreamConnection>();
 
 	private DeviceDiscoveryResult lastDeviceDiscoveryResult;
 	private ServiceDiscoveryResult lastServiceDiscoveryResult;
 
 	// our main entry point into Java Bluetooth
 	private DiscoveryAgent agent;
 
 	// for reading from the UART
 	private InputStream input;
 	// for posting to the UART
 	private DataOutputStream output;
 
 	// reader background thread that publishes to the AMF channel
 	private ConsoleReadThread consoleReadThread;
 
 	// Spring template for AMF pub/sub
 	private MessageTemplate template;
 
 	private static final UUID[] uuidSet = new UUID[] {
 	// new UUID(0x0001), // SDP
 	new UUID(0x0003) // RFCOMM
 	// new UUID(0x0008), // OBEX
 	// new UUID(0x000C), // HTTP
 	// new UUID(0x0100) // L2CAP
 	// new UUID(0x000F), // BNEP
 	// new UUID(0x1101), // Serial Port
 	// new UUID(0x1000), // ServiceDiscoveryServerServiceClassID
 	// new UUID(0x1001), // BrowseGroupDescriptorServiceClassID
 	// new UUID(0x1002), // PublicBrowseGroup
 	// new UUID(0x1105), // OBEX Oject Push
 	// new UUID(0x1106), // OBEX File Transfer
 	// new UUID(0x1115), // PAN
 	// new UUID(0x1116), // Network Access Point
 	// new UUID(0x1117), // Group Network
 	};
 
 	private static int[] attrSet = new int[] { 0x0100 // Service name
 	};
 
 	/**
 	 * Constructor
 	 */
 	public BluetoothService() {
 		LOGGER.debug("Bluetooth Service initialized");
 	}
 
 	/**
 	 * Spring injected {@link MessageTemplate}
 	 * 
 	 * @param template
 	 */
 	@Autowired
 	public void setTemplate(MessageTemplate template) {
 		this.template = template;
 	}
 
 	/**
 	 * Get the Local Device information
 	 * 
 	 * @return a {@link Device} that represents our local adapter
 	 */
 	@RemotingInclude
 	public Device getLocalDevice() {
 		try {
 			final LocalDevice localDevice = LocalDevice.getLocalDevice();
 			final Device device = new Device(localDevice.getFriendlyName(),
 					localDevice.getBluetoothAddress());
 			return device;
 		} catch (Exception e) {
 			LOGGER.error(e);
 			throw new BluetoothServiceException(e);
 		}
 	}
 
 	/**
 	 * Discover any Bluetooth devices in range
 	 * 
 	 * @return a set of discovered {@link Device}s
 	 */
 	@RemotingInclude
 	public Set<Device> findDevices() throws Exception {
 		// clear the map
 		deviceMap.clear();
 		try {
 			final LocalDevice localDevice = LocalDevice.getLocalDevice();
 			LOGGER.info("Local Bluetooth Name/Addres: "
 					+ localDevice.getFriendlyName() + "/"
 					+ localDevice.getBluetoothAddress());
 
 			agent = localDevice.getDiscoveryAgent();
 
 			LOGGER.info("Starting Device Inquiry");  
 			agent.startInquiry(DiscoveryAgent.GIAC, this);
 			try {
 				synchronized (deviceLock) {
 					deviceLock.wait();
 				}
 			} catch (InterruptedException e) {
 				LOGGER.error(e);
 				throw new RuntimeException(e);
 			}
 			LOGGER.debug("Device Inquiry Complete => "
 					+ lastDeviceDiscoveryResult);
 			if (!DeviceDiscoveryResult.INQUIRY_COMPLETED
 					.equals(lastDeviceDiscoveryResult)) {
 				// something went wrong
 				throw new BluetoothServiceException(
 						lastDeviceDiscoveryResult.asString());
 			}
 
 			if (deviceMap.size() == 0) {
 				LOGGER.warn("No Bluetooth devices were found");
 				return new HashSet<Device>(0);
 			} else {
 				LOGGER.info("Discovered " + deviceMap.size() + " devices");
 			}
 			return toDeviceSet(deviceMap);
 		} catch (BluetoothServiceException e) {
			LOGGER.error("Exception scanning for devices "+e.getMessage());
 			throw e;
 		} catch (Exception e) {
 			LOGGER.error(e);
 			throw new BluetoothServiceException(e);
 		}
 	}
 	
 	/**
 	 * Returns a list of cached devices that the bluetooth stack knows about.  
 	 * @return
 	 * @throws Exception
 	 */
 	@RemotingInclude
 	public Set<Device> findCachedDevices() throws Exception {
 		Set<Device> set = new HashSet<Device>();
 		try {
 			final LocalDevice localDevice = LocalDevice.getLocalDevice();
 			LOGGER.info("Local Bluetooth Name/Addres: "
 					+ localDevice.getFriendlyName() + "/"
 					+ localDevice.getBluetoothAddress());
 
 			agent = localDevice.getDiscoveryAgent();
 
 			LOGGER.info("Returning a list of cached devices");
 			RemoteDevice[] devices = agent.retrieveDevices(DiscoveryAgent.CACHED);
 			
 			if(devices == null) { return new HashSet<Device>(0); }
 
 			for(RemoteDevice rd : devices) {
 				try {
 					if(rd.getBluetoothAddress().startsWith("0006")) {
 						set.add(toDevice(rd));
 						deviceMap.put(rd.getBluetoothAddress(), rd);
 					}
 				} catch(Exception e) {
 					LOGGER.error(e.getMessage());
 				}
 				
 			}
 			return set;
 		} catch(Exception e) {
 			LOGGER.error(e);
 			throw new BluetoothServiceException(e);
 		}
 	}
 
 	/**
 	 * Find the RFCOMM service for the given address; the address is cached so
 	 * when you call connect, the service URL will be used that this method
 	 * looked up
 	 * 
 	 * @param address
 	 *            the Bluetooth address of the device
 	 * @throws Exception
 	 */
 	@RemotingInclude
 	public void findServices(final String address) throws Exception {
 		LOGGER.info("Searching for services on remote device " + address);
 		final RemoteDevice remoteDevice = deviceMap.get(address);
 		if (remoteDevice == null) {
 			throw new BluetoothServiceException("Unknown device address: "
 					+ address);
 		}
 
 		synchronized (serviceLock) {
 			try {
 				agent.searchServices(attrSet, uuidSet, remoteDevice, this);
 			} catch (BluetoothStateException e) {
 				LOGGER.error("Exception searching for services on remote device address: "
 						+ address
 						+ " \n"
 						+ ExceptionUtil.getStackTraceAsString(e));
 				throw new RuntimeException(e);
 			} finally {
 				try {
 					serviceLock.wait();
 				} catch (InterruptedException e) {
 					LOGGER.error("We've got threading issues <service search> \n"
 							+ ExceptionUtil.getStackTraceAsString(e));
 					throw new BluetoothServiceException(
 							"Connection was not successful.  Please try again.");
 				}
 				// anything but SERVICE_SEARCH_COMPLETED means we had a problem
 				if (!ServiceDiscoveryResult.SERVICE_SEARCH_COMPLETED
 						.equals(lastServiceDiscoveryResult)) {
 					throw new BluetoothServiceException(
 							lastServiceDiscoveryResult.asString());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Connect to the SPP on the given address
 	 * 
 	 * @param address
 	 *            the Bluetooth address
 	 * @throws Exception
 	 */
 	@RemotingInclude
 	public void connectRFComm(String address) throws Exception {
 		LOGGER.info("attempting to connect to " + address);
 		StreamConnection connection = null;
 		if (connectionMap.containsKey(address)) {
 			connection = connectionMap.get(address);
 		} else {
 			connection = (StreamConnection) Connector.open(serviceMap
 					.get(address));
 			connectionMap.put(address, connection);
 		}
 		input = connection.openInputStream();
 		output = connection.openDataOutputStream();
 		if (consoleReadThread == null) {
 			consoleReadThread = new ConsoleReadThread(input, template);
 		}
 		consoleReadThread.start();
 	}
 
 	/**
 	 * Disconnect from the RFComm service
 	 * 
 	 * @param address
 	 * @throws Exception
 	 */
 	@SuppressWarnings("deprecation")
 	@RemotingInclude
 	public void disconnectRFComm(String address) {
 		LOGGER.debug("disconnecting " + address);
 		StreamConnection connection = connectionMap.get(address);
 		try {
 			// stop the thread
 			if (consoleReadThread != null) {
 				/*
 				 * This is deprecated, but the Bluecove implementation of
 				 * InputStream provides no possible way to interrupt a thread
 				 * that is doing a blocking read call; This big hammer approach
 				 * is the only sane way to deal with this.
 				 */
 				consoleReadThread.stop();
 			}
 			if (input != null) {
 				input.close();
 			}
 			if (output != null) {
 				output.close();
 			}
 			if (connection != null) {
 				connection.close();
 			}
 			connectionMap.remove(address);
 		} catch (Exception e) {
 			LOGGER.error("Exception trying to disconnect from RFCOMM \n"
 					+ ExceptionUtil.getStackTraceAsString(e));
 		} finally {
 			consoleReadThread = null;
 		}
 	}
 
 	/**
 	 * Sends a command via Bluetooth to the UART to the micro to be processed by
 	 * the shell program.
 	 * 
 	 * @param command
 	 *            the command to send
 	 * @throws Exception
 	 */
 	@RemotingInclude
 	public void send(String command) throws Exception {
 		if (output != null) {
 			output.writeBytes(command);
 			output.flush();
 		}
 	}
 
 	/**
 	 * Set the device friendly name
 	 * 
 	 * @param name
 	 *            must be at least one character and not more than 20 characters
 	 * @return true if successful, false otherwise
 	 * @throws Exception
 	 */
 	@SuppressWarnings("deprecation")
 	@RemotingInclude
 	public boolean rename(String address, String name) throws Exception {
 		if (name == null || name.isEmpty()) {
 			throw new BluetoothServiceException("Name cannot be null or empty");
 		} else if (name.length() > 20) {
 			throw new BluetoothServiceException(
 					"Name cannot exceed 20 characters");
 		}
 		try {
 			// stop the read thread
 			if (consoleReadThread != null) {
 				consoleReadThread.stop();
 			}
 
 			// switch to command mode
 			LOGGER.debug("switching to bluetooth command mode");
 			output.writeBytes("$$$");
 			output.flush();
 			Thread.sleep(200);
 			// clear the serialized friendly name
 			LOGGER.debug("erasing serialized friendly name");
 			output.writeBytes("s-,\r");
 			output.flush();
 			Thread.sleep(1000);
 			// set the friendly name
 			LOGGER.debug("setting new name to " + name);
 			output.writeBytes("sn," + name + "\r");
 			output.flush();
 			Thread.sleep(1000);
 			// now we need to reset the modem
 			LOGGER.debug("resetting the modem...");
 			output.writeBytes("r,1\r");
 			output.flush();
 			Thread.sleep(1000);
 
 			// tear down the connection
 			disconnectRFComm(address);
 		} catch (Exception e) {
 			LOGGER.error("Exception trying to rename the device \n"
 					+ ExceptionUtil.getStackTraceAsString(e));
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Update the firmware on the device
 	 * 
 	 * @param filePath
 	 */
 	@SuppressWarnings("deprecation")
 	@RemotingInclude
 	public void updateFirmware(String filePath) {
 		
 		final File file = new File(filePath);
 		if (!file.exists()) {
 			throw new BluetoothServiceException("The file " + filePath
 					+ " does not exist");
 		}
 		final AsyncInputStreamThread asyncIn = new AsyncInputStreamThread(consoleReadThread.inputStream);
 	    asyncIn.setDaemon(true);
 	    asyncIn.start();
 		
 		final STK500v1 programmer = new STK500v1(output, asyncIn, new ProgramEventListener(template), true);
 		
 		if (consoleReadThread == null || !consoleReadThread.isAlive()) {
 			throw new BluetoothServiceException(
 					"Can't update firmware because you are not conencted");
 		} else {
 			// suspend the regular console read thread
 			consoleReadThread.suspend();
 		}
 		try {
 			// switch to command mode
 			LOGGER.debug("switching to bluetooth command mode");
 			output.writeBytes("$$$");
 			output.flush();
 			Thread.sleep(200);
 			LOGGER.debug("resetting micro");
 			// set pin 10 high
 			output.writeBytes("S*,0404\n");
 			output.flush();
 			Thread.sleep(100);
 			// set pin 10 low
 			output.writeBytes("S*,0400\n");
 			output.flush();
 			Thread.sleep(100);
 			// set pin 10 high
 			output.writeBytes("S*,0404\n");
 			output.flush();
 			Thread.sleep(100);
 			LOGGER.debug("exit bluetooth command mode");
 			output.writeBytes("---\n");
 			output.flush();
 			
 			LOGGER.debug("sleeping to let device initialize");
 			Thread.sleep(3000);
 			
 			LOGGER.info("Going to update firmware with "+filePath);
 			final BufferedIntelHexReader reader = new BufferedIntelHexReader(new BufferedReader(new FileReader(file)));
 
 			programmer.updateFirmware(reader, file.length());
 			
 		} catch (Exception e) {
 			LOGGER.error("Failed to update firmware \n"
 					+ ExceptionUtil.getStackTraceAsString(e));
 			throw new BluetoothServiceException(
 					"Failed to update firmware because " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Converts the map into a set of {@link Device} objects that can be
 	 * serialized easily through AMF.
 	 * 
 	 * @param map
 	 * @return
 	 * @throws IOException
 	 */
 	private Set<Device> toDeviceSet(final Map<String, RemoteDevice> map)
 			throws IOException {
 		final Set<Device> set = new HashSet<Device>();
 		for (final String key : map.keySet()) {
 			Device d = toDevice(map.get(key));
 			if(d != null) {
 				set.add(toDevice(map.get(key)));
 			}
 			
 		}
 		return set;
 	}
 
 	private Device toDevice(final RemoteDevice remoteDevice) throws IOException {
 		try {
			final Device device = new Device(remoteDevice.getFriendlyName(false),
 					remoteDevice.getBluetoothAddress());
 			device.setAuthenticated(remoteDevice.isAuthenticated());
 			device.setEncrypted(remoteDevice.isEncrypted());
 			device.setTrusted(remoteDevice.isTrustedDevice());
 			LOGGER.debug("discovered: " + device);
 			return device;
 		} catch(Exception e) {
 			LOGGER.error("Exception resolving device friendly name"+e.getMessage());
 			return null;
 		}
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.
 	 * RemoteDevice, javax.bluetooth.DeviceClass)
 	 */
 	@Override
 	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
 		if(btDevice.getBluetoothAddress().startsWith("0006")) {
 			deviceMap.put(btDevice.getBluetoothAddress(), btDevice);
		} else {
			LOGGER.debug(String.format("Ignoring device with Bluetooth address %s b/c it is not a Daisy", btDevice.getBluetoothAddress()));
 		}
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int)
 	 */
 	@Override
 	public void inquiryCompleted(int discType) {
		
 		switch (discType) {
 		case DiscoveryListener.INQUIRY_COMPLETED:
 			lastDeviceDiscoveryResult = DeviceDiscoveryResult.INQUIRY_COMPLETED;
			LOGGER.debug("Device Inquiry Completed with: " + DeviceDiscoveryResult.INQUIRY_COMPLETED);
 			break;
 		case DiscoveryListener.INQUIRY_TERMINATED:
 			lastDeviceDiscoveryResult = DeviceDiscoveryResult.INQUIRY_TERMINATED;
			LOGGER.debug("Device Inquiry Completed with: " + DeviceDiscoveryResult.INQUIRY_TERMINATED);
 			break;
 		case DiscoveryListener.INQUIRY_ERROR:
 			lastDeviceDiscoveryResult = DeviceDiscoveryResult.INQUIRY_ERROR;
			LOGGER.debug("Device Inquiry Completed with: " + DeviceDiscoveryResult.INQUIRY_ERROR);
 			break;
 		default:
 			LOGGER.error("\tresponse code: Unknown Response Code =>" + discType);
 			lastDeviceDiscoveryResult = DeviceDiscoveryResult.UNKNOWN;
 			break;
 		}
 		synchronized (deviceLock) {
 			deviceLock.notify();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int,
 	 * javax.bluetooth.ServiceRecord[])
 	 */
 	public void servicesDiscovered(int transID, ServiceRecord[] records) {
 		for (int i = 0; i < records.length; i++) {
 			String url = records[i].getConnectionURL(
 					ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
 			if (url == null) {
 				LOGGER.error("\tService url was null for "
 						+ records[i].toString());
 			}
 			serviceMap.put(records[i].getHostDevice().getBluetoothAddress(),
 					url);
 			DataElement serviceName = records[i].getAttributeValue(0x0100);
 			if (serviceName != null) {
 				LOGGER.info("\tService " + serviceName.getValue() + " found "
 						+ url);
 			} else {
 				LOGGER.info("\tService found " + url);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int)
 	 */
 	public void serviceSearchCompleted(int transID, int respCode) {
 		LOGGER.info("Service search complete for transaction " + transID);
 		switch (respCode) {
 		case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
 			lastServiceDiscoveryResult = ServiceDiscoveryResult.SERVICE_SEARCH_COMPLETED;
 			break;
 		case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
 			lastServiceDiscoveryResult = ServiceDiscoveryResult.SERVICE_SEARCH_DEVICE_NOT_REACHABLE;
 			break;
 		case DiscoveryListener.SERVICE_SEARCH_ERROR:
 			lastServiceDiscoveryResult = ServiceDiscoveryResult.SERVICE_SEARCH_ERROR;
 			break;
 		case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
 			lastServiceDiscoveryResult = ServiceDiscoveryResult.SERVICE_SEARCH_NO_RECORDS;
 			break;
 		case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
 			lastServiceDiscoveryResult = ServiceDiscoveryResult.SERVICE_SEARCH_TERMINATED;
 			break;
 		default:
 			LOGGER.error("\tUnknown Response Code: " + respCode);
 			lastServiceDiscoveryResult = ServiceDiscoveryResult.UNKNOWN;
 		}
 		synchronized (serviceLock) {
 			serviceLock.notifyAll();
 		}
 	}
 
 	/**
 	 * Background thread for reading from the UART and posting on message bus
 	 * for UI
 	 */
 	private final class ConsoleReadThread extends Thread {
 
 		private final BufferedReader reader;
 		private final InputStream inputStream;
 		private final MessageTemplate template;
 
 		public ConsoleReadThread(InputStream input, MessageTemplate template)
 				throws IOException {
 			this.inputStream = input;
 			this.reader = new BufferedReader(new InputStreamReader(inputStream));
 			this.template = template;
 		}
 
 		/**
 		 * {@link BufferedReader#readLine()} and all read methods are blocking
 		 * I/O that can't be interrupted. Even if I wrap it with NIO, the
 		 * underlying input stream is of type
 		 * com.intel.bluetooth.BluetoothRFCommInputStream which can't be
 		 * interrupted. The only way to stop this is to use
 		 * {@link Thread#stop()}
 		 */
 		@Override
 		public void run() {
 			LOGGER.info("Background thread listening for serial port updates...");
 			try {
 				while (true) {
 					template.send("serialPort", reader.readLine());
 				}
 			} catch (IOException e) {
 				LOGGER.error(e);
 			}
 		}
 	}
 
 	private final class ProgramEventListener implements STK500EventListener {
 
 		private final MessageTemplate template;
 		
 		public ProgramEventListener(MessageTemplate template) {
 			this.template = template;
 		}
 		
 		@SuppressWarnings("deprecation")
 		@Override
 		public void notify(final STK500Event event) {
 			LOGGER.info(event);
 			if(STK500Event.COMPLETE.equals(event)) {
 				// restart the background read thread
 				if(consoleReadThread != null) { consoleReadThread.resume(); }
 			}
 			template.send("serialPort", "FOTA,"+event.toString());
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		public void notify(final STK500Event event, final int progress) {
 			LOGGER.info(event + ": " + progress + "%");
 			if(STK500Event.COMPLETE.equals(event)) {
 				// restart the background read thread
 				if(consoleReadThread != null) { consoleReadThread.resume(); }
 			}
 			template.send("serialPort", "FOTA,"+event.toString()+","+progress);
 		}
 
 	}
 }
