 package device;
 
 import java.util.Iterator;
 import java.util.concurrent.*;
 import java.util.logging.Logger;
 
 public class Device implements IDevice, Runnable
 {
 	/** Logging support */
     Logger logger = Logger.getLogger (Device.class.getName ());
 
 	protected ConcurrentLinkedQueue<Device> deviceList = null;
 	
 	int name = -1;
 	String host = "0";
 	int port = 0;
 	int deviceNumber = 0;
 	
 	/** Every class of this type has it's own thread */
 	protected Thread thread = new Thread ( this );
 
 	long SLEEPTIME = 100;
 	private boolean isRunning = false;
 	private boolean isThreaded = false;
 	
 	public Device()
 	{
 		deviceList = new ConcurrentLinkedQueue<Device>();
 	}
 	/**
      * This constructor is used to create a data device object
      * and has no internal devicelist or thread
      *
 	 * @param name The Device id.
 	 * @param host The host to which this Device is connected.
 	 * @param port The port to which this Device is connected.
 	 * @param devNum The Device number (aka index) of the Device.
 	 */
 	public Device (int name, String host, int port, int devNum)
 	{
 		this();
 		this.name = name;
 		if (host != null)
 			this.host = host;
 		this.port = port;
 		deviceNumber = devNum;
 	}
 	/**
 	 * 
 	 * @param device A device template to create a new device
 	 */
 	public Device (Device device)
 	{
 		this(device.getName(), device.getHost(), device.getPort(), device.getDeviceNumber());
 	}
 	/**
 	 * @depreciated
 	 * This constructor adds all devices of the devices in the list
 	 * to its internal device list.
 	 * @param aDeviceList The list of devices that contain devices.
 	 */
 	public Device (Device[] aDeviceList) {
 		this();
 		if (aDeviceList != null) {
 			for (int i=0; i<aDeviceList.length; i++) {
 				addDevicesOf(aDeviceList[i]);
 			}
 		}
 	}
 	/**
 	 * Adds all devices of the given device (if any and without itself)
 	 * to the internal device list of this device.
 	 * @param yad A device containing other devices.
 	 */
 	public void addDevicesOf ( Device yad ) {
 		if (yad != null && deviceList != null) {
 			Iterator<Device> devIt = yad.getDeviceIterator();
 			while (devIt.hasNext()) {
 				deviceList.add(devIt.next());
 			}
 		}
 	}
 	/**
 	 * Might be to be implemented by subclass to do something
 	 */
 	protected void update() {}
 
 	public synchronized void runThreaded()
 	{
 		if (thread.isAlive() != true) {
 			// Start all devices
 			if (deviceList != null && deviceList.size() > 0)
 			{
 				Iterator<Device> deviceIterator = deviceList.iterator();
 
 				while (deviceIterator.hasNext())
 				{
 					Device device = deviceIterator.next();
 
 					// Start device
 					device.runThreaded();
 					while (device.thread.isAlive() == false);
 				}
 			}
 
 			isThreaded  = true;
 
 			thread.start();
 			while (thread.isAlive() == false);
 
 			logger.info("Running "+this.getClass().toString()+" in "+thread.getName());
 		}
 	}
 
 	@Override
 	public void run()
 	{
 	    isRunning = true;
 		while ( ! thread.isInterrupted() && isThreaded == true)
 		{
 			/** Do sub-class specific stuff */
 			update();
 			
 			if (SLEEPTIME > 0)
 			{
 				try { Thread.sleep ( SLEEPTIME ); }
 				catch (InterruptedException e) {
 					isThreaded = false;
 				}
 			}
 			else
 				if (SLEEPTIME == 0)
 				{
 					Thread.yield();
 				}
 				// else if (SLEEPTIME < 0) do nothing				
 		}
 		isRunning = false;    /** sync with setNotThreaded */
 		
 		logger.info("Shutdown "+this.getClass().toString()+" in "+thread.getName());
 	}
 	public synchronized void shutdown()
 	{
 		long delayCount = 0;
 		
 		isThreaded = false;
         
 		while (isRunning == true) /** wait to exit run loop */
 		{
 			delayCount += 1;
 			if (delayCount > 2)
 				logger.fine("Shutdown delayed " + this.getClass().getName());
 			
             try { Thread.sleep (100); } catch (Exception e) { }
 		}
 		
 		/** Stop all devices */
 		if (deviceList.size() > 0)
 		{
		    /**
		     * Loop through Device List in reverse order
		     */
			Object[] devList = deviceList.toArray();
			for (int i=devList.length-1; i>=0; i--)
 			{
			    Device device = (Device)devList[i];
 				
 				/** Stop device */
 				device.shutdown();
 			}
 			/** empty device list */
 			deviceList.clear();
 		}
 	}
 	/**
 	 * Returns a list of devices that this robot client provides
 	 * @return Device list
 	 */
 	public final ConcurrentLinkedQueue<Device> getDeviceList() {
 		return deviceList;
 	}
 	
 	/**
      * @param deviceList the deviceList to set
      */
     protected final void setDeviceList(ConcurrentLinkedQueue<Device> deviceList) {
         this.deviceList = deviceList;
     }
     public synchronized final void addToDeviceList(Device dev) {
 
 		/** Check if it has other devices linked */
 		Iterator<Device> iter = dev.getDeviceIterator(); 
 		if (iter != null) {
 			while (iter.hasNext()) {
 				/** Add the device to internal list */	
 				addToDeviceList(dev);
 			}
 		}
 		deviceList.add(dev);
 	}
 	
 	public final Iterator<Device> getDeviceIterator() {
 		if (deviceList != null) {
 			return (Iterator<Device>) deviceList.iterator();
 		} else {
 			return null;
 		}
 	}
 	/**
 	 * 
 	 * @param dev Device template (0 or null for args not to take care of).
 	 * @return A device matching the given template when found in list, null otherwise.
 	 */
 	public final Device getDevice (Device dev) {
 		int name = dev.getName();
 		int number = dev.getDeviceNumber();
 		Device found = null;
 		
 		Iterator<Device> it = getDeviceIterator();
 		while (it.hasNext())
 		{
 			Device curDev = it.next();
 			if (curDev.getName() == name)
 			{
 				if (number != -1)
 				{
 					if (curDev.getDeviceNumber() == number)
 					{
 						found = curDev;
 					}
 				} else {
 					found = curDev;
 				}
 			}
 		}
 		return found;
 	}
 	public String getHost() {
 		return host;
 	}
 	public void setHost(String host) {
 		this.host = host;
 	}
 	public int getPort() {
 		return port;
 	}
 	public void setPort(int port) {
 		this.port = port;
 	}
 	public int getName() {
 		return name;
 	}
 	public void setName(int name) {
 		this.name = name;
 	}
 
 	public int getDeviceNumber() {
 		return deviceNumber;
 	}
 	public String getThreadName() {
 		if (thread != null) {
 			return thread.getName();
 		} else {
 			return null;
 		}
 	}
 	public void setSleepTime(long time) {
 		this.SLEEPTIME = time;
 	}
 	public long getSleepTime() {
 		return SLEEPTIME;
 	}
 	public boolean isRunning() {
 		return isRunning;
 	}
 	public boolean isThreaded() {
 		return isThreaded;
 	}
     /**
      * @return the logger
      */
     public Logger getLogger() {
         return logger;
     }
 }
