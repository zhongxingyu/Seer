 package device;
 
 //import java.util.Collections;
 import java.util.Iterator;
 //import java.util.LinkedList;
 //import java.util.List;
 import java.util.concurrent.*;
 
 import core.Logger;
 
 public class Device implements IDevice, Runnable
 {
 	protected ConcurrentLinkedQueue<Device> deviceList = null;
 	
 	int name = -1;
 	String host = "0";
 	int port = 0;
 	int deviceNumber = 0;
 	
 	// Every class of this type has it's own thread
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
 		
 		Logger.logDeviceActivity(false, "Running", this);
 	}
 
 	@Override
 	public void run()
 	{
 	    isRunning = true;
 		while ( ! thread.isInterrupted() && isThreaded == true)
 		{
 			update();
 			
 			if (SLEEPTIME == 0)
 			{
 				Thread.yield();
 			}
 			else
 			{
 				try { Thread.sleep ( SLEEPTIME ); }
 				catch (InterruptedException e) { thread.interrupt(); }
 			}
 		}
 		isRunning = false;    // sync with setNotThreaded
 		Logger.logDeviceActivity(false, "Shutdown", this);
 	}
 	public synchronized void shutdown()
 	{
 		isThreaded = false;
         
 		while (isRunning == true)
 		{// wait to exit run thread
             try { Thread.sleep (10); } catch (Exception e) { }
 		}
 		
 		thread.interrupt();
 		while (thread.isAlive());
 		
 		// Stop all devices
 		if (deviceList.size() > 0) {
 			Iterator<Device> deviceIterator = deviceList.iterator();
 			while (deviceIterator.hasNext()) {
 				Device device = deviceIterator.next();
 				
 				// Stop device
 				device.thread.interrupt();
 				while (device.thread.isAlive());
 			}
 			// empty device list
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
 	
 	public synchronized final void addToDeviceList(Device dev) {
 
 		// Check if it has other devices linked
 		Iterator<Device> iter = dev.getDeviceIterator(); 
 		if (iter != null) {
 			while (iter.hasNext()) {
 				// Add the device to internal list	
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
 	public void setSleepTime(int time) {
 		this.SLEEPTIME = time;
 	}
 }
