 package org.bitducks.spoofing.util.gateway;
 
 import java.io.IOException;
 import java.net.InetAddress;
 
 import org.bitducks.spoofing.util.os.*;
 
 import jpcap.NetworkInterface;
 
 public abstract class GatewayFinder {
 	
 	static public InetAddress find( NetworkInterface device ) throws IOException {
 		
 		Os os = OsDiscovery.discover();
 		
 		if( os == Os.UNKNOWN ) {
			throw new RuntimeException("Could not detect OS.");
 		}
 		
 		GatewayParser parser = null;
 		
 		if( os == Os.LINUX ) { 
 			parser = new LinuxGatewayParser();
 		}
 		
 		InetAddress address = parser.findAndParse(device);
 		if( address == null ) {
			throw new RuntimeException("Could not parse routing table when looking up gateway");
 		}
 		
 		return address;
 
 	}
 
 }
