 package org.bitducks.spoofing.main;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 
 import javax.swing.WindowConstants;
 
 import jpcap.JpcapCaptor;
 import jpcap.JpcapSender;
 import jpcap.NetworkInterface;
 import jpcap.NetworkInterfaceAddress;
 import jpcap.packet.ARPPacket;
 import jpcap.packet.Packet;
 
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Layout;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.gateway.GatewayFindService;
 import org.bitducks.spoofing.gui.DeviceSelection;
 import org.bitducks.spoofing.gui.Gui;
 import org.bitducks.spoofing.gui.LogView;
 //import org.bitducks.spoofing.scan.ArpService;
 import org.bitducks.spoofing.packet.PacketGenerator;
 import org.bitducks.spoofing.scan.ArpCache;
 import org.bitducks.spoofing.scan.ArpRecieveService;
 import org.bitducks.spoofing.scan.ArpScanService;
 import org.bitducks.spoofing.scan.IpRange;
 import org.bitducks.spoofing.test.CustomAppender;
 import org.bitducks.spoofing.test.DummyService;
 import org.bitducks.spoofing.util.Constants;
 import org.bitducks.spoofing.util.IpUtil;
 import org.bitducks.spoofing.util.gateway.GatewayFinder;
 
 public class Tests {
 	
 	final static private int NB_DEVICE = 0;
 	
 	/**
 	 * @param args
 	 * @throws UnknownHostException 
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		greeting();
 		//testIpUtils();
 		//testIpMask();
 		//testIpRange();
 		//testArpService();
 		//testArpRequestResponse();
 		//testDummyService();
 		//testCache();
 		//testGui();
 		//testGatewayFinder();
 		//testLogging();
 		testLogView();
 		
 
 	}
 	
 	private static void testLogView() throws Exception {
 		
 		BasicConfigurator.configure();
 		
 		Server.createInstance( getDevice() );
 		Server server = Server.getInstance();
 		
 		DummyService dummy = new DummyService();
 		Server.getInstance().addService(dummy);
 		
 		server.start();
 		
		Gui gui = new Gui( getDevice() );
 		gui.setVisible(true);
 		gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		
 	}
 
 	private static Layout defaultLayout() {
 		return new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN);
 	}
 	
 	private static void testLogging() {
 		
 		//BasicConfigurator.configure();
 		CustomAppender appender = new CustomAppender( defaultLayout() );
 		
 		
 		Logger logger = Logger.getLogger( Tests.class );
 		logger.addAppender(appender);
 		
 		logger.info("HELLO WORLD");
 		
 	}
 
 	private static void testGatewayFinder() throws Exception {
 		
 		Server.createInstance( getDevice() );
 		Server server = Server.getInstance();
 		server.start();
 		Thread.sleep(1000);
 		
 		System.out.println("adding service");
 		GatewayFindService finder = new GatewayFindService();
 		server.addService(finder);
 		
 		byte[] address = finder.getMacAddress();
 		
 		System.out.println("mac found: " + IpUtil.prettyPrintMac( address ) );
 		
 		
 		
 	}
 
 	public static void printDevices() {
 		
 		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
 		
 		//for each network interface
 		for (int i = 0; i < devices.length; i++) {
 		  //print out its name and description
 		  System.out.println(i+": "+devices[i].name + "(" + devices[i].description+")");
 
 		  //print out its datalink name and description
 		  System.out.println(" datalink: "+devices[i].datalink_name + "(" + devices[i].datalink_description+")");
 
 		  //print out its MAC address
 		  System.out.print(" MAC address:");
 		  for (byte b : devices[i].mac_address)
 		    System.out.print(Integer.toHexString(b&0xff) + ":");
 		  System.out.println();
 
 		  //print out its IP address, subnet mask and broadcast address
 		  for (NetworkInterfaceAddress a : devices[i].addresses) {
 		    System.out.println(" address:"+a.address + " " + a.subnet + " "+ a.broadcast);
 		  }
 		}
 		
 	}
 	
 	public static void greeting() {
 		
 		NetworkInterface device = getDevice();
 		System.out.println( device.addresses[0].address );
 		
 	}
 	
 	public static NetworkInterface getDevice() {
 		
 		NetworkInterface device = JpcapCaptor.getDeviceList()[ Tests.NB_DEVICE ];
 		return device;
 		
 	}
 
 
 	private static void testIpUtils() {
 		
 		NetworkInterface device = getDevice();
 		NetworkInterfaceAddress deviceAddress = device.addresses[0];
 		
 		InetAddress iAddress = device.addresses[0].address;
 		InetAddress iMask = device.addresses[0].subnet;
 		InetAddress iBroadcast = device.addresses[0].broadcast;
 		
 		InetAddress network = IpUtil.network(iAddress, iMask);
 		InetAddress last = IpUtil.lastIpInNetwork(iBroadcast);
 		
 		System.out.println(network);
 		System.out.println(last);
 		System.out.println( IpUtil.network(deviceAddress) );
 		System.out.println( IpUtil.lastIpInNetwork(deviceAddress) );
 		
 	}
 
 	private static void testIpMask() {
 		
 		NetworkInterface device = getDevice();
 		
 		InetAddress iAddress = device.addresses[0].address;
 		InetAddress iMask = device.addresses[0].subnet;
 		InetAddress iBroadcast = device.addresses[0].broadcast;
 		
 		int address = ByteBuffer.wrap(iAddress.getAddress()).getInt(0);
 		int mask = ByteBuffer.wrap(iMask.getAddress()).getInt(0);
 		
 		int network = address & mask;
 		
 		ByteBuffer buffer = ByteBuffer.allocate(4);
 		buffer.putInt(network);
 				
 		byte[] bufNetwork = new byte[4];
 		for( int i = 0; i < 4; i++ ) {
 			bufNetwork[i] = buffer.get(i);
 		}
 		
 		InetAddress iNetwork = null;
 		try {
 			iNetwork = InetAddress.getByAddress(bufNetwork);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println(iNetwork);
 		
 	}
 
 	public static void testIpRange() throws Exception {
 		
 		InetAddress start = InetAddress.getByName("192.168.0.1");
 		InetAddress end = InetAddress.getByName("192.168.0.255");
 		
 		IpRange range = new IpRange(start.getAddress(), end.getAddress());
 		
 		for( InetAddress a: range) {
 			System.out.println(a);
 		}
 		
 	}
 	
 	public static void testArpRequestResponse() throws Exception {
 		
 		JpcapCaptor captor = JpcapCaptor.openDevice( getDevice(), 2000, false, 20 );
 		JpcapSender sender = JpcapSender.openDevice( getDevice() );
 		
 		InetAddress start = InetAddress.getByName("192.168.1.221");
 		InetAddress end = InetAddress.getByName("192.168.1.222");
 		
 		IpRange range = new IpRange( start, end );
 		
 		PacketGenerator generator = new PacketGenerator( getDevice() );
 		
 		for( InetAddress a: range ) {
 			ARPPacket request = generator.arpRequest( a );
 			System.out.println("sending " + request.toString() );
 			sender.sendPacket(request);
 		}
 		
 		while( true ) {
 			Packet packet = captor.getPacket();
 			if( packet != null ) {
 				System.out.println(packet);
 			}
 		}
 		
 	}
 	
 	public static void testDummyService() throws Exception {
 		
 		NetworkInterface i = JpcapCaptor.getDeviceList() [ Tests.NB_DEVICE ];
 		Server.createInstance(i);
 		
 		DummyService dummy = new DummyService();
 		
 		Server.getInstance().addService(dummy);
 		
 		Server.getInstance().start();
 		
 		// NEVER END !!
 		Server.getInstance().join();
 		
 	}
 	
 	public static void testArpService() throws Exception {
 		
 		Server.createInstance( getDevice() );
 		
 		Server server = Server.getInstance();
 		
 		ArpRecieveService reciever = new ArpRecieveService();
 		ArpScanService scanner = new ArpScanService();
 		
 		server.addService(reciever);
 		
 		server.start();
 		
 		Thread.sleep(2000);
 		
 		server.addService(scanner);
 		scanner.runNetworkScan();
 		
 		server.join();
 		
 		
 	}
 	
 	public static void testCache() throws Exception {
 		
 		ArpCache cache = new ArpCache();
 		
 		InetAddress address = InetAddress.getByName("192.168.1.1");
 		byte[] mac = Constants.BROADCAST;
 		
 		System.out.println("waiting");
 		cache.add(address, mac);
 		Thread.sleep(2000);
 		
 		System.out.println( cache.hasAddress(address,1000) );
 		
 	}
 	
 	public static void testGui() throws Exception {
 		
 		NetworkInterface device = DeviceSelection.getSelectedDevice();
 		System.out.println(device);
 		System.out.println("done");
 		
 	}
 
 }
