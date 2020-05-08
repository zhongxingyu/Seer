 package bpf;
 
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFCommunication;
 
 public class Communication implements BPFCommunication {
 
 	private static final String TAG = "Communication";
 
 	public InetAddress getBroadcastAddress(String interfaceName) {
 		System.setProperty("java.net.preferIPv4Stack", "true");
 		try {
 			Enumeration<NetworkInterface> niEnum = NetworkInterface
 					.getNetworkInterfaces();
 			while (niEnum.hasMoreElements()) {
 				NetworkInterface ni = niEnum.nextElement();
 				if (!ni.isLoopback()) {
 					for (InterfaceAddress interfaceAddress : ni
 							.getInterfaceAddresses()) {
 						if (interfaceAddress.getBroadcast() != null
 								&& ni.getName().equals(interfaceName)) {
 							BPF.getInstance()
 									.getBPFLogger()
 									.debug(TAG,
 											"getBroadcastAddress from interface "
 													+ interfaceName
 													+ " is: "
 													+ interfaceAddress
 															.getBroadcast());
 							return interfaceAddress.getBroadcast();
 						}
 					}
 				}
 			}
 		} catch (SocketException e) {
 			BPF.getInstance().getBPFLogger()
 					.error(TAG, "Exception while getting broadcast address.");
 		}
 
 		BPF.getInstance()
 				.getBPFLogger()
				.error(
 						TAG,
 						"Called getBroadcastAddress but couldn't find the interface: "
 								+ interfaceName);
 		return null;
 	}
 
 	public InetAddress getDeviceIP() {
 		try {
 			Enumeration<NetworkInterface> ifaces = NetworkInterface
 					.getNetworkInterfaces();
 			while (ifaces.hasMoreElements()) {
 				NetworkInterface iface = ifaces.nextElement();
 				Enumeration<InetAddress> addresses = iface.getInetAddresses();
 
 				while (addresses.hasMoreElements()) {
 					InetAddress addr = addresses.nextElement();
 					if (addr instanceof Inet4Address
 							&& !addr.isLoopbackAddress()) {
 						BPF.getInstance().getBPFLogger()
 								.debug(TAG, "getDeviceIP returning " + addr);
 						return addr;
 					}
 				}
 			}
 		} catch (SocketException e) {
 			BPF.getInstance().getBPFLogger()
 					.error(TAG, "Exception while getting device address.");
 		}
 		return null;
 	}
 
 }
