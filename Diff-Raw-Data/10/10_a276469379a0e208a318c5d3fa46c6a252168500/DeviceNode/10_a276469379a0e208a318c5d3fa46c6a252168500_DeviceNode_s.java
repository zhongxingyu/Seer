 package device;
 
 import javaclient3.PlayerClient;
 import javaclient3.PlayerException;
 import javaclient3.structures.PlayerConstants;
 import javaclient3.structures.PlayerDevAddr;
 import javaclient3.structures.player.PlayerDeviceDevlist;
 
 import java.util.Iterator;
 import java.util.concurrent.*;
 import java.util.logging.Logger;
 
 import data.Host;
 
 /**
  * Client API to the robot server.
  * Whatever the server is, this class is the basic interface
  * onto all other robot devices will be hooked.
  * @author sebastian
  *
  */
 public class DeviceNode extends Device
 {
 	/** Logging support */
     Logger logger = Logger.getLogger (DeviceNode.class.getName ());
 
 	/** A list of all connected robot clients of this node */
 	CopyOnWriteArrayList<PlayerClient> playerClientList = null;
 		
 	/**
 	 * Creates a plain device node with an internal PlayerClient list.
 	 */
 	private DeviceNode()
 	{
 		playerClientList = new CopyOnWriteArrayList<PlayerClient>();
 		
 		/**
 		 * If this DeviceNode needs doing something alter sleep time later.
 		 */
 		setSleepTime(Long.MAX_VALUE);
 	}
 	
 	/**
 	 * @deprecated Use {@link #DeviceNode(Host[])} instead.
 	 * Constructor for a RobotClient.
 	 * @param host The host name where the server is to connect to.
 	 * @param port The port of the server listening for a client.
 	 */
 	public DeviceNode (String host, Integer port) 
 	{
 		this();
 		InitRobotClient( host, new Integer(port) );
 	}
 	/**
 	 * @deprecated Use {@link #DeviceNode(Host[])} instead.
 	 * Accepts a list of following structure:
 	 * String "hostname",Integer port
 	 * All clients on the list will be instantiated on the hosts and ports given.
 	 * @param origin
 	 */
 	public DeviceNode (final Object[] origin)
 	{
 	    this();
 	    if (origin != null)
 	    {
 	        int count = origin.length;
 	        if (count % 2 == 0)
 	        {
 	            for (int i=0; i<count; i+=2)
 	            {
 	                InitRobotClient( (String) origin[i], (Integer) origin[i+1] );
 	            }
 	        }
 	    }
 	}
 	/**
 	 * @deprecated Use {@link #DeviceNode(Host[], Device[])} instead.
 	 * Creates a device node containing all devices found on the given hosts.
 	 * @param hostList The host list to search devices.
 	 */
 	public DeviceNode (Host[] hostList)
 	{
 	    this();
 	    
 	    for (int i=0; i<hostList.length; i++)
 	    {
 	        InitRobotClient(hostList[i].getHostName(), hostList[i].getPortNumber());
 	    }
 	}
 	/**
 	 * @deprecated Use {@link #DeviceNode(Host, Device[])} instead.
 	 * Convenience wrapper for {@link #DeviceNode(Host[])}.
 	 */
 	public DeviceNode(Host host)
 	{
 	    this(new Host[]{host});
 	}
 	/**
 	 * Creates a DeviceNode with the given {@link PlayerClient}.
 	 * @param client The PlayerClient.
 	 */
 	DeviceNode (PlayerClient client)
 	{
 		this();
 		playerClientList.add(client);
 	}
 	/**
 	 * Creates a device node that contains only the devices given (if any of them are found on the given hosts).
 	 * If a found device matches any given device property it will be connected to.
 	 * Note that only the device first found matching the properties will be connected.
 	 * @param hostList The host list to search devices.
 	 * @param devList The device properties to match any devices on the given hosts.
 	 */
 	public DeviceNode (Host[] hostList, Device[] devList)
 	{
 	    this();
 	    
 	    for (int i=0; i<hostList.length; i++)
 	    {
 	        initRobotClientTemplate(hostList[i], devList);
 	    }
     }
 	/** Convenience wrapper for {@link #DeviceNode(Host[], Device[])}. */
 	public DeviceNode (Host host, Device dev)
 	{
 	    this(new Host[]{host}, new Device[]{dev});
 	}
     /** Convenience wrapper for {@link #DeviceNode(Host[], Device[])}. */
 	public DeviceNode (Host host, Device[] devList)
 	{
 	    this(new Host[]{host}, devList); 
 	}
     /** Convenience wrapper for {@link #DeviceNode(Host[], Device[])}. */
 	public DeviceNode (Host[] hostList, Device dev)
 	{
 	    this(hostList, new Device[]{dev});
 	}
 	/**
 	 * @deprecated Use {@link #initRobotClientTemplate(Host, Device[])} instead.
 	 * Connects to the underlying robot service and retrieves a list of all devices.
 	 * Only to be called by root DeviceNode!
 	 * @param host The host this device node runs on.
 	 * @param port The host's port.
 	 * @throws IllegalStateException When connecting to underlying robot client layer fails.
 	 */
 	void InitRobotClient(String host, Integer port) throws IllegalStateException
 	{
 		try
 		{
 			PlayerClient client = new PlayerClient (host, port);
 			/** Do not add a @see PlayerClient as this is a root DeviceNode */
 			
 			/** Create a new DeviceNode with the PlayerClient */
             DeviceNode devNode = new DeviceNode(client);
             devNode.setHost(host);
             devNode.setPort(port);
             /** Push requires no sleep time */
             devNode.setSleepTime(0);
             /** Add it to the internal DeviceNode list */
             getDeviceList().add(devNode);
 
             /** Requires that above call has internally updated device list already! */
 			/** Add devices of that client to internal list */
 			updateDeviceList(client, host, port);
 			client.setNotThreaded();
 
 			/** Get the devices available */
 			client.requestDataDeliveryMode(PlayerConstants.PLAYER_DATAMODE_PUSH);
 		}
 		catch (PlayerException e)
 		{
 		    String log = "Could not connect to PlayerClient at "+host+":"+port;
 		    logger.severe(log);
 		    throw new IllegalStateException(log);
 		}		
 	}
 	/**
      * Connects to the underlying robot service and retrieves a list of all devices.
      * Only devices matching the given template list will be initialized and added.
      * Only to be called by root DeviceNode!
      * @param aHost The host this device node runs on.
      * @param aDevList The device template list.
      * @throws IllegalStateException When connecting to underlying robot client layer fails.
      */
 	void initRobotClientTemplate(Host aHost, Device[] aDevList) throws IllegalStateException
 	{
 	    try
         {
             PlayerClient client = new PlayerClient (aHost.getHostName(), aHost.getPortNumber());
             /** Do not add a @see PlayerClient as this is a root DeviceNode */
             
             /** Create a new DeviceNode with the PlayerClient */
             DeviceNode devNode = new DeviceNode(client);
             devNode.setHost(aHost.getHostName());
             devNode.setPort(aHost.getPortNumber());
             /** Push requires no sleep time */
             devNode.setSleepTime(0);
             /** Add it to the internal DeviceNode list */
             getDeviceList().add(devNode);
 
             /** Requires that above call has internally updated device list already! */
             /** Add devices of that client to internal list */
             updateDeviceListTemplate(client, aHost, aDevList);
             
             client.setNotThreaded();
 
             /** Get the devices available */
             client.requestDataDeliveryMode(PlayerConstants.PLAYER_DATAMODE_PUSH);
         }
         catch (PlayerException e)
         {
             String log = "Could not connect to PlayerClient at "+aHost.getHostName()+":"+aHost.getPortNumber();
             logger.info(log);
 //            throw new IllegalStateException(log);
         }       
 	}
 	/**
 	 * Reads the underlying robot client data when available.
 	 */
 	@Override protected void update()
 	{
 		Iterator<PlayerClient> it = playerClientList.iterator();
 		while (it.hasNext()) { it.next().readAll(); }
 	}
 	/**
 	 * Shutdown robot client and clean up
 	 */
 	@Override public void shutdown()
 	{
 	    /**
 	     * Shutdown devices in device list first,
 	     * as they keep reading from the playerclient(s)
 	     */
 	    super.shutdown();
 	    
 	    /** Shutdown the PlayerClients */
 	    Iterator<PlayerClient> it = playerClientList.iterator();
 	    while (it.hasNext()) { it.next().close(); }
 	}
 
 	/**
 	 * @deprecated Use {@link #updateDeviceListTemplate(PlayerClient, Host, Device[])} instead.
 	 * Connects to known devices of the underlying robot service.
 	 * @param playerClient
 	 * @param host
 	 * @param port
 	 */
 	private void updateDeviceList(PlayerClient playerClient, String host, int port)
 	{
 		PlayerDeviceDevlist pDevList = playerClient.getPDDList();
 		if (pDevList != null) {
 
 			PlayerDevAddr[] pDevListAddr = pDevList.getDevList();
 			if (pDevListAddr != null) {
 
 				int devCount = pDevList.getDeviceCount();
 				for (int i=0; i<devCount; i++)
 				{
                     Device dev = null;
 
 				    /**
 				     * Read device information.
 				     * Host and port are arguments.
 				     */
 					int devId = pDevListAddr[i].getInterf();
 					int devIdx = pDevListAddr[i].getIndex();
 				
 					switch (devId)
 					{
 					case IDevice.DEVICE_POSITION2D_CODE :
 						if (devIdx == 0)
 							dev = new Position2d(this, new Device(devId, host, port, devIdx)); break;
 
 					case IDevice.DEVICE_RANGER_CODE : 
//						dev = new Ranger(this, new Device(devId, host, port, devIdx)); break;
 						
 					case IDevice.DEVICE_BLOBFINDER_CODE :
 						dev = new Blobfinder(this, new Device(devId, host, port, devIdx)); break;
 	
 					case IDevice.DEVICE_GRIPPER_CODE : 
 						dev = new Gripper(this, new Device(devId, host, port, devIdx)); break;
 						
 					case IDevice.DEVICE_SONAR_CODE : 
 						dev = new RangerSonar(this, new Device(devId, host, port, devIdx)); break;
 
 					case IDevice.DEVICE_LASER_CODE :
 					    /** Use legacy laser only when no ranger is present */
 //					    if (getDevice(new Device(IDevice.DEVICE_RANGER_CODE,null,-1,1)) == null)
 					        dev = new RangerLaser(this, new Device(devId, host, port, devIdx));
 					    break;
 
 					case IDevice.DEVICE_LOCALIZE_CODE : 
 						dev = new Localize(this, new Device(devId, host, port, devIdx)); break;
 	
 					case IDevice.DEVICE_SIMULATION_CODE : 
 						dev = new Simulation(this, new Device(devId, host, port, devIdx)); break; 
 
 					case IDevice.DEVICE_PLANNER_CODE :
 						try {
 						dev = new Planner(this, new Device(devId, host, port, devIdx));
 						} catch (IllegalStateException e) {
 							dev = new Planner(this, new Device(devId, host, port, devIdx));
 						}
 						break;
 
 					case IDevice.DEVICE_ACTARRAY_CODE :
 					    dev = new Actarray(this, new Device(devId, host, port, devIdx)); break;
 
 					case IDevice.DEVICE_DIO_CODE :
 					    dev = new Dio(this, new Device(devId, host, port, devIdx)); break;
 
 					default: break;
 					}
 					
 					/**
 					 * Add new device to device list.
 					 */
 					if (dev != null)
 					{
 						getDeviceList().add(dev);
 					}
 				}
 			}
 		}
 	}
 	Device initDevice(Device devInfo)
 	{
 	    Device dev = null;
 	    int devId = devInfo.getName();
 	    String host = devInfo.getHost();
 	    int port = devInfo.getPort();
 	    int devIdx = devInfo.getDeviceNumber();
 	    
 	    switch (devId)
         {
             case IDevice.DEVICE_POSITION2D_CODE :
                 dev = new Position2d(this, new Device(devId, host, port, devIdx)); break;
     
             case IDevice.DEVICE_RANGER_CODE : 
                 dev = new Ranger(this, new Device(devId, host, port, devIdx)); break;
                 
             case IDevice.DEVICE_BLOBFINDER_CODE :
                 dev = new Blobfinder(this, new Device(devId, host, port, devIdx)); break;
     
             case IDevice.DEVICE_GRIPPER_CODE : 
                 dev = new Gripper(this, new Device(devId, host, port, devIdx)); break;
                 
             case IDevice.DEVICE_SONAR_CODE : 
                 dev = new RangerSonar(this, new Device(devId, host, port, devIdx)); break;
 
             case IDevice.DEVICE_LASER_CODE :
                 dev = new RangerLaser(this, new Device(devId, host, port, devIdx)); break;
     
             case IDevice.DEVICE_LOCALIZE_CODE : 
                 dev = new Localize(this, new Device(devId, host, port, devIdx)); break;
     
             case IDevice.DEVICE_SIMULATION_CODE : 
                 dev = new Simulation(this, new Device(devId, host, port, devIdx)); break; 
     
             case IDevice.DEVICE_PLANNER_CODE :
                 try {
                     dev = new Planner(this, new Device(devId, host, port, devIdx));
                 } catch (IllegalStateException e) {
                     /** TODO Debug this */
                     dev = new Planner(this, new Device(devId, host, port, devIdx));
                 }
                 break;
     
             case IDevice.DEVICE_ACTARRAY_CODE :
                 dev = new Actarray(this, new Device(devId, host, port, devIdx)); break;
     
             case IDevice.DEVICE_DIO_CODE :
                 dev = new Dio(this, new Device(devId, host, port, devIdx)); break;
     
             default: break;
         }
 
 	    return dev;
 	}
 	/**
 	 * Finds devices according to the given device templates.
 	 * @param playerClient The {@link PlayerClient} to search for devices.
 	 * @param aHost The host the PlayerClient is running on.
 	 * @param devList The device template list ({@link Device}).
 	 */
 	private void updateDeviceListTemplate(PlayerClient playerClient, Host aHost, Device[] devList)
     {
         PlayerDeviceDevlist pDevList = playerClient.getPDDList();
        
         if (pDevList != null)
         {
             PlayerDevAddr[] pDevListAddr = pDevList.getDevList();
           
             if (pDevListAddr != null) {
 
                 int devCount = pDevList.getDeviceCount();
                
                 for (int i=0; i<devCount; i++)
                 {
                     /**
                      * Read device information.
                      * Host and port are arguments.
                      */
                     int devId = pDevListAddr[i].getInterf();
                     int devIdx = pDevListAddr[i].getIndex();
                 
                     /** Device with following properties found on PlayerClient */
                     Device dev = new Device(devId, aHost.getHostName(), aHost.getPortNumber(), devIdx);
                     
                     if (dev.matchesList(devList) == true)
                     {
                         /**
                          * Add new device to device list
                          * as it matches to any of the given templates.
                          */
                         Device newDev = initDevice(dev);
                         if (newDev != null)
                         {
                             getDeviceList().add( newDev );
                             logger.fine("Added device "+newDev);
                         }
                     }
                 }
             }
         }
     }
 
 	/**
 	 * @return The primary PlayerClient.
 	 */
 	public PlayerClient getPlayerClient()
 	{
 		if (playerClientList.size() > 0)
 		{
 			return playerClientList.get(0);
 		}
 		else
 		{
 			return null;
 		}
 	}
 	/**
 	 * Checks the internal device list for matching DeviceNodes.
 	 * @param host The host address.
 	 * @param port The host port.
 	 * @return The wanted DeviceNode or null if not found.
 	 */
 	public DeviceNode getDeviceNode(String host, int port)
 	{
 	    Iterator<Device> devIt = getDeviceList().iterator();
 	    /** Search all DeviceNodes */
 	    while (devIt.hasNext())
 	    {
 	        Device dev = devIt.next();
 	        if (dev.getClass() == DeviceNode.class)
 	        {
 	            /** Check for wanted DeviceNode */
 	            if (dev.getHost().equals(host) && dev.getPort() == port)
 	            {
 	                return (DeviceNode) dev;
 	            }
 	        }
 	    }
 	    return null;
 	}
 
     /**
      * @return The object's string.
      */
     @Override public String toString()
     {
         return ""+getClass().getName()+"@"+getHost()+":"+getPort();
     }
 }
