 package ru.spbstu.telematics.flowgen.cloud;
 
 
 import org.json.JSONException;
 import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.AttachmentPoint;
 import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Host;
 import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
 import ru.spbstu.telematics.flowgen.utils.StringUtils;
 
 
 public class ControllerHostConnector implements Runnable {
 
 	// Attempting every 5 seconds for 2 minutes
 	private static final int ATTEMPTS = 25;
 	private static final int INTERVAL_MILLIS = 5000;
 
 	private ICloud cloud;
 	private Action action;
 	private String ip;
 
 
 	public ControllerHostConnector(ICloud cloud, String ip, Action action) {
 		if (cloud == null) {
 			throw new NullPointerException("Cloud is null");
 		}
 		if (StringUtils.isNullOrEmpty(ip)) {
 			throw new IllegalArgumentException("Wrong IP (null or empty string)");
 		}
 
 		this.cloud = cloud;
 		this.action = action;
 		this.ip = ip;
 	}
 
 
 	@Override
 	public void run() {
 
 		boolean done = false;
		String dpid = "not_found";
 		int port = -1;
 
 		int i;
 		for (i = 1; i <= ATTEMPTS; i++) {
 
 			// Request controller for list of known hosts and parse it
 			Hosts knownHosts;
 			try {
 				knownHosts = Hosts.parse(cloud.getFloodlightClient().getAllKnownHosts());
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return;
 			}
 
 			// Search for host by IP
 			for (Host host : knownHosts.getAllHosts()) {
 				if (ip.equals(host.getIpv4())) {
 					// Found!
 
 					AttachmentPoint ap = host.getAttachmentPoint();
 					if (action == Action.Connect) {
						cloud.launchHost(host.getMac(), ap.getDpid(), ap.getPort());
 					} else {
						cloud.stopHost(host.getMac());
 					}
 
 					done = true;
 					break;
 				}
 			}
 			if (done) {
 				break;
 			}
 
 			// Wait for time interval
 			try {
 				synchronized (this) {
 					this.wait(INTERVAL_MILLIS);
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 
 		if (done) {
 			if (action == Action.Connect) {
 				System.out.println("[INFO] [+] VM with IP (" + ip +
 						") connected by connector on port (" + port +
 						") of DPID (" + dpid + ") in attempt #" + i);
 			} else {
 				System.out.println("[INFO] [-] VM with IP (" + ip +
 						") disconnected by connector from port (" + port +
 						") of DPID (" + dpid + ") in attempt #" + i);
 			}
 		} else {
 			System.out.println("[ERROR] " + (action == Action.Connect ? "[+]" : "[-]") +
					" VM with IP (" + ip + ") not found in list of known hosts");
 		}
 
 	}
 
 	/***** Inner Classes *****/
 
 	public static enum Action {
 		Connect,
 		Disconnect
 	}
 }
