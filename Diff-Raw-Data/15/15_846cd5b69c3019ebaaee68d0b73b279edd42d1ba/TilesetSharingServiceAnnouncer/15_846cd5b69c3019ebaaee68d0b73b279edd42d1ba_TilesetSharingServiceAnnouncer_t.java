 /**
  * 
  */
 package net.niconomicon.tile.source.app.sharing.server;
 
 import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceInfo;
 
 import net.niconomicon.tile.source.app.Ref;
 
 /**
  * @author niko
  * 
  */
 public class TilesetSharingServiceAnnouncer {
 
 	private int servicePort;
 	JmDNS jmdns;
 
 	static TilesetSharingServiceAnnouncer service;
 	boolean shouldExit = false;
 	boolean shouldUnregister = false;
 
 	public static TilesetSharingServiceAnnouncer getInstance() {
 		synchronized (Ref.sharing_serviceName) {
 			if (service == null) {
 				service = new TilesetSharingServiceAnnouncer();
 			}
 		}
 		return service;
 	}
 
 	private TilesetSharingServiceAnnouncer() {}
 
 	public void startSharing(int port) {
 		shouldUnregister = false;
 		servicePort = port;
 		// System.out.println("Running the Thread");
 		try {
 			if (jmdns != null) {
 				jmdns.unregisterAllServices();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			System.out.println("Opening JmDNS");
 			jmdns = JmDNS.create();
 			System.out.println("Opened JmDNS. Registering the service...");
 
 			try {
				Map<String,String> m = new HashMap<String, String>();
				m.put("path","json");

				ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort,1,1,m);
 				jmdns.registerService(info);
 				System.out.println("\nRegistered Service as " + info);
 				if (shouldUnregister) {
 					jmdns.unregisterAllServices();
 					jmdns.close();
 				}
 				if (shouldExit) {
 					jmdns.unregisterAllServices();
 					jmdns.close();
 					System.exit(0);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void stopSharing() {
 		stopService();
 	}
 
 	public void stopService() {
 		shouldUnregister = true;
 		if (null != jmdns) {
 			try {
 				System.out.println("unregistering services");
 				jmdns.unregisterAllServices();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			try {
 				System.out.println("closing the jmdns service");
 				jmdns.close();
 				System.out.println("closed the jmdns service");
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 }
