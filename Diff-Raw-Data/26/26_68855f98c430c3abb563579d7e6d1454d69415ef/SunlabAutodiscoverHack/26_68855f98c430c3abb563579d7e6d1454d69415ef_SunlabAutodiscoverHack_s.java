 package src.net;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 /**
  * Helps set up connections for games in the SunLab.
  */
 public class SunlabAutodiscoverHack {
 	private static ArrayList<InetAddress> addresses;
 
 	public static ArrayList<InetAddress> getSunlabAddresses() {
 		if (addresses == null) {
 			addresses = new ArrayList<InetAddress>();
 
 			try {
				addresses.add(InetAddress.getByName("cslab8c"));

 				addresses.add(InetAddress.getByName("cslab3a"));
 				addresses.add(InetAddress.getByName("cslab3b"));
 				addresses.add(InetAddress.getByName("cslab3c"));
 				addresses.add(InetAddress.getByName("cslab3d"));
 				addresses.add(InetAddress.getByName("cslab3e"));
 				addresses.add(InetAddress.getByName("cslab3f"));
 				addresses.add(InetAddress.getByName("cslab3g"));
 				addresses.add(InetAddress.getByName("cslab3h"));
 				addresses.add(InetAddress.getByName("cslab2a"));
 				addresses.add(InetAddress.getByName("cslab2b"));
 				addresses.add(InetAddress.getByName("cslab2c"));
 				addresses.add(InetAddress.getByName("cslab2d"));
 				addresses.add(InetAddress.getByName("cslab2e"));
 				addresses.add(InetAddress.getByName("cslab2f"));
 				addresses.add(InetAddress.getByName("cslab2g"));
 				addresses.add(InetAddress.getByName("cslab2h"));
 				addresses.add(InetAddress.getByName("cslab1a"));
 				addresses.add(InetAddress.getByName("cslab1b"));
 				addresses.add(InetAddress.getByName("cslab1c"));
 				addresses.add(InetAddress.getByName("cslab1d"));
 				addresses.add(InetAddress.getByName("cslab1e"));
 				addresses.add(InetAddress.getByName("cslab1f"));
 				addresses.add(InetAddress.getByName("cslab1g"));
 				addresses.add(InetAddress.getByName("cslab1h"));
 				addresses.add(InetAddress.getByName("cslab0a"));
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return addresses;
 	}
 }
