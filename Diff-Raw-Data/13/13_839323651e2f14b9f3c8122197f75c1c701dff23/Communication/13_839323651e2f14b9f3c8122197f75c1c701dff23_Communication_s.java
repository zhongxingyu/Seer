 package se.kth.ssvl.tslab.wsn.service.bpf;
 
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 
 import android.os.StrictMode;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFCommunication;
 
 public class Communication implements BPFCommunication {
 
 	@Override
 	public InetAddress getBroadcastAddress() {
 		InetAddress found_bcast_address = null;
 		System.setProperty("java.net.preferIPv4Stack", "true");
 		try {
 			Enumeration<NetworkInterface> niEnum = NetworkInterface
 					.getNetworkInterfaces();
 			while (niEnum.hasMoreElements()) // while all interfaces are counted
 			{
 				NetworkInterface ni = niEnum.nextElement(); // ni receive each
 															// time the name of
 															// interface, for
 															// example eth0
 				if (!ni.isLoopback()) { // if ni-interface is not loopback, then
 										// variable interfaceAddress receive
 										// address of this interface
 					for (InterfaceAddress interfaceAddress : ni
 							.getInterfaceAddresses()) {
 						found_bcast_address = interfaceAddress.getBroadcast();
 					}
 				}
 			}
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 
 		return found_bcast_address;
 	}
 
 	@Override
 	public InetAddress getDeviceIP() {
 		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
 		
 		InetAddress DeviceAddress = null;
 		try {
 	        for (Enumeration<NetworkInterface> en = NetworkInterface
 	                .getNetworkInterfaces(); en.hasMoreElements();) {
 	            NetworkInterface intf = en.nextElement();
 	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 	                InetAddress inetAddress = enumIpAddr.nextElement();
 	                if (!inetAddress.isLoopbackAddress()) {
 	                	DeviceAddress = 
 	                		    InetAddress.getLocalHost();
 	                }
 	            }
 	        }
 	    } catch (UnknownHostException ex) {
 	    	ex.printStackTrace();
 	    } catch (SocketException e) {
 			e.printStackTrace();
 		}
 		return DeviceAddress;
 	}
 
 }
