 package de.javawi.jstun.test.demo;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import de.javawi.jstun.attribute.exception.MessageAttributeException;
 import de.javawi.jstun.header.exception.MessageHeaderParsingException;
 import de.javawi.jstun.test.BindingRequestTest;
 import de.javawi.jstun.util.UtilityException;
 
 public class BindingRequestDemo implements Runnable {
	
	static Logger logger;
 	
 	InetAddress local;
 	String stunServer = "biascica.pipps.net";
 	int port = 3478;
 	
 	public BindingRequestDemo(InetAddress localAddress, String stunServer, int port) {
 		this.local = localAddress;
 		this.stunServer = stunServer;
 		this.port = port;
 	}
 	
 	public void run() {
 		try {
 			BindingRequestTest br = new BindingRequestTest(local, stunServer, port);
 			br.test();
 		} catch (SocketException e) {
//			logger.severe(local + ": Socket Exception: "+e.getMessage()); // TODO or simply e?
 		} catch (UnknownHostException e) {
//			logger.severe(local+": Unknown host: "+e.getMessage());
 		} catch (UtilityException e) {
//			logger.severe(local+": Utility Exception: "+e.getMessage());
 		} catch (MessageHeaderParsingException e) {
//			logger.severe(local+": MessageHeader Parsing Exception: "+e.getMessage());
 		} catch (MessageAttributeException e) {
//			logger.severe(local+": Attribute Parsing Exception: "+e.getMessage());
 		} catch (IOException e) {
//			logger.severe(local+": IO Exception: "+e.getMessage());
 		}
 	}
 	
 	public static void main(String args[]) {
 		try {
 			Handler fh = new FileHandler("bindingreq.log");
 			fh.setFormatter(new SimpleFormatter());
			Logger.getLogger("de.javawi.jstun").addHandler(fh);
 			
 			String stunserver = "biascica.pipps.net";
 			int port = 3478;
 			
 			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
 			
 			while( ifaces.hasMoreElements() ) {
 				NetworkInterface iface = ifaces.nextElement();
 				Enumeration<InetAddress> iadds = iface.getInetAddresses();
 				while (iadds.hasMoreElements()) {
 					InetAddress iadd = iadds.nextElement();
 					if (!iadd.isLoopbackAddress() && !iadd.isLoopbackAddress()) {
 						Thread t = new Thread(new BindingRequestDemo(iadd, stunserver, port));
 						t.start();
 					}
 				}
 			}
 		} catch (SecurityException e) {
 			logger.warning("Warning: no logging permissions");
 		} catch (IOException e) {
 			logger.warning("Warning: IO error prevented logger from starting");
 		}
 	}
 	
 	
 
 }
