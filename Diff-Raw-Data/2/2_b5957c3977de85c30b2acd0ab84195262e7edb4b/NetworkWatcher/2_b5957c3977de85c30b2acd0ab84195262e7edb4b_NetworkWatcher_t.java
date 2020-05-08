 package netproj.controller;
 
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import netproj.hosts.tcp.FastTcpHost;
 import netproj.hosts.tcp.TcpHost;
 import netproj.routers.Router;
 import netproj.skeleton.Device;
 import netproj.skeleton.Link;
 
 /**
  * This class records data files corresponding to the buffer occupancy for
  * the input and output buffers of every device on the network, as well
  * as the throughput for all links between the devices.  It is
  * created by the {@code SimulatorSpecification}; since each simulation
  * uses exactly one {@code SimulatorSpecification}, each simulation should
  * only employ one {@code NetworkWatcher}.  The data files are dumped to the
  * /data/ directory, organized by device addresses.  Note that since the
  * NetworkWatcher does not manage open file connections to all data files
  * during simulation, it instead appends new log information to the end of
  * data files for each iteration of logging.  As a result, new data is
  * appended to the data files; thus, BETWEEN EACH SIMULATION, DATA FILES
  * MUST BE CLEARED.
  * 
  * 
  * @author nathan
  *
  */
 public class NetworkWatcher extends Thread {
 	private List<Device> devices;
 	private List<Router> routers;
 	private List<Link> links;
 	private long timeOffset;
 	
 	private static final Logger logger = Logger.getLogger(NetworkWatcher.class.getCanonicalName());
 	
 	public NetworkWatcher(List<Device> d, List<Router> r, List<Link> l) {
 		devices = d;
 		routers = r;
 		links = l;
 		timeOffset = -1;
 	}
 	
 	@Override
 	public void run() {
 		if (timeOffset == -1)
 			timeOffset = System.currentTimeMillis();
 		while (!this.isInterrupted()) {
 			try {
 				synchronized (this) {
 					wait(900);
 				}
 			} catch (InterruptedException e) {
 				logger.log(Level.WARNING, "NetworkWatcher was interrupted.", e);
 				break;
 			}
 			
 			// Collect buffer occupancy stats on all hosts
 			for (Device d : devices) {
 				try {
 					PrintWriter o1 = new PrintWriter(new FileWriter("data/" +
 							Integer.toHexString(d.getAddress()) + "inbuf.dat", true));
 					o1.println((System.currentTimeMillis() - timeOffset) + " " + d.getInputBuffUsed());
 					o1.close();
 					
 					PrintWriter o2 = new PrintWriter(new FileWriter("data/" +
 							Integer.toHexString(d.getAddress()) + "outbuf.dat", true));
 					o2.println((System.currentTimeMillis() - timeOffset) + " " + d.getOutputBuffUsed());
 					o2.close();
 					
 					if (d instanceof TcpHost) {
 						TcpHost tmp = (TcpHost)d;
 						PrintWriter o3 = new PrintWriter(new FileWriter("data/" +
 							Integer.toHexString(d.getAddress()) + "window.dat", true));
 						o3.println((System.currentTimeMillis() - timeOffset) + " " + tmp.getWindowSize());
 						o3.close();
 						
 						double rtt = tmp.getRtt();
 						if (rtt >= 0) {
 							PrintWriter o4 = new PrintWriter(new FileWriter("data/" +
 									Integer.toHexString(d.getAddress()) + "rtt.dat", true));
 							o4.println((System.currentTimeMillis() - timeOffset) + " " + rtt);
 							o4.close();
 						}
 					}
 					
 					if (d instanceof FastTcpHost) {
 						FastTcpHost tmp = (FastTcpHost)d;
 						long baseRtt = tmp.getBaseRtt();
 						if (baseRtt < Long.MAX_VALUE) {
 							PrintWriter o5 = new PrintWriter(new FileWriter("data/" +
 									Integer.toHexString(d.getAddress()) + "basertt.dat", true));
 							o5.println((System.currentTimeMillis() - timeOffset) + " " + baseRtt);
 							o5.close();
 						}
 					}
 				} catch (Exception e) {
 					logger.severe("Failed to write to log file\n" + e);
 				}
 			}
 			
 			// Collect buffer occupancy stats on all routers
 			for (Router r : routers) {
 				try {
 					PrintWriter o1 = new PrintWriter(new FileWriter("data/" +
 							Integer.toHexString(r.getAddress()) + "inbuf.dat", true));
 					o1.println((System.currentTimeMillis() - timeOffset) + " " + r.getInputBuffUsed());
 					o1.close();
 					
 					PrintWriter o2 = new PrintWriter(new FileWriter("data/" +
 							Integer.toHexString(r.getAddress()) + "outbuf.dat", true));
 					o2.println((System.currentTimeMillis() - timeOffset) + " " + r.getOutputBuffUsed());
 					o2.close();
 				} catch (Exception e) {
 					logger.severe("Failed to write to log file\n" + e);
 				}
 			}
 			
 			// Collect throughput stats on all links
 			for (Link l : links) {
 				long timer = l.getLastReset();
 				long now = System.currentTimeMillis();
 				long bits = l.resetBitsSent();
 				if (timer != 0) {
 					timer = now - timer;
 					try {
 						String linkName = l.toString();
 						linkName = linkName.substring(1, linkName.length() - 1);
 						PrintWriter o = new PrintWriter(new FileWriter("data/" +
 								linkName + "flow.dat", true));
						o.println((now - timeOffset) + " " + (bits * 1000 / timer));
 						o.close();
 					} catch (Exception e) {
 						logger.severe("Failed to write to log file\n" + e);
 					}
 				}
 			}
 		}
 	}
 }
