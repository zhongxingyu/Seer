 package uk.ac.cam.jk510.part2project.protocol;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import android.widget.TextView;
 
 import uk.ac.cam.jk510.part2project.graphics.MapDrawer;
 import uk.ac.cam.jk510.part2project.gui.MapDisplayScreen;
 import uk.ac.cam.jk510.part2project.network.DataConnectionManager;
 import uk.ac.cam.jk510.part2project.network.DeviceConnection;
 import uk.ac.cam.jk510.part2project.network.TCPConnection;
 import uk.ac.cam.jk510.part2project.session.Device;
 import uk.ac.cam.jk510.part2project.session.Session;
 import uk.ac.cam.jk510.part2project.settings.Config;
 import uk.ac.cam.jk510.part2project.store.Coords;
 import uk.ac.cam.jk510.part2project.store.CoordsTXYA;
 import uk.ac.cam.jk510.part2project.store.PositionStore;
 import uk.ac.cam.jk510.part2project.store.Response;
 import uk.ac.cam.jk510.part2project.session.DeviceHandleIP;;
 
 public abstract class ProtocolManager {
 
 	protected LinkedList<Coords>[] coordsToSend;	//one linkedlist for each device to send to. Client server so only one.
 	protected long[] timeOfLastSend;
 	private long timeSinceastMissingCheck = 0;
 	private long pointsSinceLastMissingCheck = 0;
 	protected boolean checkedMissingSinceLastReceipt = false;
 	protected LinkedList<Integer>[] requestArray;
 	protected DeviceConnection[] connections;
 	private static boolean networking;	//is the current protocol using the network? (for testing)
 
 	private static ProtocolManager instance;
 	private static boolean alive = true;
 	public static TextView debugInfo;
 
 	public synchronized static ProtocolManager initialiseProtocolManager(Session session) throws Exception {
 		if(instance == null) {
 			alive = true;
			System.out.println("I AM ALIVE 1: "+alive);	//debug
 			networking = (session.getDevice(0).getHandle() instanceof DeviceHandleIP);
 			System.out.println("Networking enabled: "+networking);
 			instance = newProtocolManager();
 			if(instance instanceof ProtocolManagerClientServer) {
 				instance.coordsToSend = new LinkedList[1];
 				instance.coordsToSend[0] = new LinkedList<Coords>();
 			} else if(instance instanceof ProtocolManagerP2P) {
 				instance.coordsToSend = new LinkedList[session.numDevices()];	//TODO make this one size less so none for self.
 				System.out.println("Initialising ProtocolManager and coordsToSend[0] is "+instance.coordsToSend[0]);	//debug
 				for(int device=0; device < session.numDevices(); device++) {
 					instance.coordsToSend[device] = new LinkedList<Coords>();
 				}
 			}
 			if(networking) {
				System.out.println("I AM ALIVE 2: "+alive);	//debug
 				instance.spawnReceivingThread();
 				instance.spawnMissingCheckTimerThread();
 			}
 		}
 		return instance;
 	}
 
 	//TODO when sending to server, must attach the device number thats sending it so server knows which its coming from, not just which it's about. (e.g. for swicthing port numbers)
 
 	public static void spawnRandomGPSThread() {
 		new Thread(new Runnable() {
 			long timeOfLastGenerate = 0;
 			int index = 0;
 			public void run() {
 				while(alive) {
 					if(timeOfLastGenerate + Config.fakeGPSPeriod() <= System.currentTimeMillis()) {
 						testInputData(Session.getThisDevice(), index);
 						index++;
 						timeOfLastGenerate = System.currentTimeMillis();
 					}
 					try {
 						Thread.sleep(Config.fakeGPSPeriod());
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 	}
 
 	public static void testInputData() {
 		//		for (int dev=0; dev<Session.getSession().numDevices(); dev++) {
 		//			testInputData(dev);
 		//		}
 		new Thread(new Runnable() {
 			public void run() {
 				int thisDeviceNumber = Session.getThisDevice().getDeviceID();
 				testInputData(thisDeviceNumber);	//changed from above as needed
 			}
 		}).start();
 
 	}
 
 	public static void testInputData(int device) {
 		//TODO remove the following test data
 		//adds some random data for test
 		Device deviceObject = Session.getDevice(device);
 		for(int i=0; i<1; i++) {
 			int index = (int) (Math.random()*100);
 			Coords coords = new CoordsTXYA(device, index, (int) (Math.random()*100)+Config.getSampleX(), (int) (Math.random()*100)+Config.getSampleY(), (int) (Math.random()*100));
 			testInputData(deviceObject, index);
 
 		}
 	}
 
 	public static void testInputData(Device deviceObject, int index) {
 		Coords coords = new CoordsTXYA(deviceObject.getDeviceID(), index, (int) (Math.random()*100)+Config.getSampleX(), (int) (Math.random()*100)+Config.getSampleY(), (int) (Math.random()*100));
 		System.err.println("PM: now generating test index: "+coords.getLClock()+" to device "+deviceObject.getDeviceID());	//debug
 		insertOriginalDataPoint(deviceObject, coords);
 		System.err.println("Finished inputting test data");	//debug
 	}
 
 	private static ProtocolManager newProtocolManager() throws Exception {
 		Proto protocol = Config.getProtocol();
 		switch(protocol) {
 		case singleUser: instance = new ProtocolManagerSingleUser(); break;
 		case clientServer: instance = new ProtocolManagerClientServer(); break;
 		case p2p: instance = new ProtocolManagerP2P(); break;
 		default: throw new Exception();
 		}
 
 		return instance;
 	}
 
 	public static ProtocolManager getProtocolManager() {
 		return instance;
 	}
 
 	/* This method is specifically for original data points.
 	 * Distinction made so that on insert, it can also notify the network module. This doesnt have to be done
 	 * for network received data points, (un-original ones).
 	 */
 	public static void insertOriginalDataPoint(final Device device, final Coords coords) {
 		//if(decision logic) {	what decision logic?
 		//should sending decision be made here or there, probably here because its ProtocolManager.
 		//}
 		new Thread(new Runnable() {
 			public void run() {
 				// TODO Auto-generated method stub
 				instance.giveToNetwork(coords);
 			}
 		}).start();
 
 		//tell Logger
 		Logger.generatedPoint(coords.getLClock());
 
 		PositionStore.insert(device.getDeviceID(), coords);
 	}
 
 	//sync'd because increments counter
 	public static synchronized void insertNetworkDataPoint(int fromDevice, Coords coords) {
 		PositionStore.insert(fromDevice, coords);
 
 		//the following code makes the system reckeck for missing points on a number of points basis as opposed to amount of time, may be more logical
 		//for actual checking, this is better since only check when enough new points come in, but for sending requests?
 		//How about, in the timer thread, instead of using a boolean, having a received count, and the threshold gets smaller as time gap gets bigger? TODO
 		//		instance.pointsSinceLastMissingCheck++;
 		//		//TODO ideally should be done at the end of processing the whole packet, not each point in the packet
 		//		//also consider the case where the recieved packet is full of missing data, this will increment counter and trigger a missingCheck. Probably desirable.
 		//		if(instance.pointsSinceLastMissingCheck >= Config.missingDataCheckThreshold()) {
 		//			instance.missingCheck();
 		//			instance.sendMissingRequest();
 		//		}
 	}
 
 
 
 	protected abstract void giveToNetwork(Coords coords);
 
	public static synchronized void destroy() {
 		ProtocolManager.stopReceivingThread();
 		if(instance != null) {
 			instance.protocolSpecificDestroy();
 		}
 		instance = null;
 	}
 	protected static void stopReceivingThread() {
 		alive = false;
 	}
 	protected boolean readyToSend(int deviceNumber) {
 		if (coordsToSend[deviceNumber].size() >= Config.getMinCoordsPerPacket()) {
 			return true;
 		} else {
 			if(Config.sendOnTimeout()) {
 				if(coordsToSend[deviceNumber].size()>0 && DataConnectionManager.timeOfLastSend() + Config.getSendTimeout() <= System.currentTimeMillis()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	protected int updateRequestArray() {
 		requestArray = new LinkedList[Session.getSession().numDevices()];
 		int size = 0;
 		for(Device d: Session.getSession().getDevices()) {
 			//requestArray[d.getDeviceID()] = d.getAbsentList(false);
 			requestArray[d.getDeviceID()] = new LinkedList<Integer>();
 			requestArray[d.getDeviceID()].addAll(d.getHistory().getAbsentList()); //changed from above to use snooping
 			
 			
  			size += requestArray[d.getDeviceID()].size();
 		}
 		return size;
 	}
 
 	public void spawnMissingCheckTimerThread() {
 		//TODO should ideally be called after MapDrawer is constructed
 		new Thread(new Runnable() {
 			public void run() {
 				while(alive) {
 					if(!MapDrawer.initialised()) {	//missingCheck relies on objects constructed when MapDrawer is inflated.
 						try {
 							Thread.sleep(Config.missingCheckTimer());
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					} else {
 						int size = 0;
 
 						if(timeSinceastMissingCheck + Config.missingCheckTimer() <= System.currentTimeMillis()) {
 							//Only need to recheck if any new points have arrived, because thats the only way
 							//new gaps can occur.
 							if(!checkedMissingSinceLastReceipt) {
 								size = missingCheck();
 							}
 							Logger.sendingRequest(size);
 							size = 0;
 							sendMissingRequest();
 						}
 						try {
 							Thread.sleep(Config.missingCheckTimer());
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 					}
 				}
 			}
 		}).start();
 	}
 
 	public void spawnSendingTimeoutThread() {
 		new Thread(new Runnable() {
 			public void run() {
 				while(alive) {
 					for(int i=0; i<Session.getSession().numDevices(); i++) {
 						if(readyToSend(i)) {
 							flushToNetwork(i);
 						}
 					}
 					try {
 						Thread.sleep(Config.getSendTimeout());
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				}
 			}
 		}).start();
 	}
 
 	public static void serviceRequestAsClient(int fromID, LinkedList<Integer>[] requestArray) {
 		instance.serviceRequest(fromID, requestArray);
 	}
 	private void serviceRequest(int fromID, LinkedList<Integer>[] requestArray) {
 		Logger.receivedRequest();
 		Response[] responses = PositionStore.fulfillRequest(requestArray);
 		List<Coords> coordsList = Response.getCoordsList(responses);
 		if(willRespondToThisRequest(requestArray, coordsList)) {
 			Logger.respondedToRequest();
 			respondToNetwork(fromID, coordsList);
 		}
 	}
 	private boolean willRespondToThisRequest(List<Integer>[] requestArray, List<Coords> coordsList) {
 
 		/*
 		 * ratio = responseSize / requestSize
 		 * if responseRatio ~0:	dont want to reply
 		 * if responseRatio ~1:	may want to reply (depending on liklihood of others also having ratio ~1.
 		 * 						this liklehood decreases with the size of the request.)
 		 */
 		int requestSize = 0;
 		for(List<Integer> list: requestArray) {
 			requestSize += list.size();
 		}
 		int responseSize = coordsList.size();
 		float responseRatio = Float.valueOf(responseSize)/Float.valueOf(requestSize);
 
 		switch(Config.responseDecider()) {
 		case always: 					return true; 
 		case probability: 				return Math.random() <= 1.0/Session.getSession().numDevices();
 		case responseRatioThresh: 		return responseRatio<=0.5;
 		case responseRatioProbability:	return Math.random() <= responseRatio;
 		case youAreRequestee:		return requestArray[Session.getThisDevice().getDeviceID()].size() > 0;
 		case youAreLargestRequestee:		int mySize = requestArray[Session.getThisDevice().getDeviceID()].size();
 		for(int i=0; i<requestArray.length; i++) {
 			if(requestArray[i].size() > mySize) {
 				return false;
 			}
 		} return true;
 		case never:	return false;
 		default: System.err.println("ResponseDecider not catered for: "+Config.responseDecider().name()); return false;
 		}	
 	}
 
 	protected int missingCheck() {
 		return updateRequestArray();
 	}
 	protected abstract void connectToPeers() throws UnknownHostException, IOException;
 	protected abstract void flushToNetwork(int device);
 	protected abstract void respondToNetwork(int requester, List<Coords> coordsList);
 	protected abstract List<Device> requestablePeers();
 	protected abstract List<Device> relientPeers();
 	public abstract void spawnReceivingThread();
 	protected abstract void sendMissingRequest();
 	protected abstract void protocolSpecificDestroy();
 	public abstract void distributeSession(Session session) throws UnknownHostException, IOException;
 	public abstract void sendKeepAliveMessage(int index);
 
	public synchronized static boolean isAlive() {
 		return alive;
 	}
 
 
 
 
 }
