 package uk.ac.cam.jk510.part2project.protocol;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.ac.cam.jk510.part2project.network.DataConnectionManager;
 import uk.ac.cam.jk510.part2project.network.ClientMessage;
 import uk.ac.cam.jk510.part2project.network.DeviceConnection;
 import uk.ac.cam.jk510.part2project.network.DroppedPacketException;
 import uk.ac.cam.jk510.part2project.session.Device;
 import uk.ac.cam.jk510.part2project.session.Session;
 import uk.ac.cam.jk510.part2project.settings.Config;
 import uk.ac.cam.jk510.part2project.store.Coords;
 
 public class ProtocolManagerClientServer extends ProtocolManager {
 
 	//private DatagramSocket socket;
 	private InetSocketAddress serverSocketAddress;
 	private static boolean alive = true;
 
 	@Override
 	protected synchronized void giveToNetwork(Coords coords) {
 		checkInit();
 
 		coordsToSend[0].add(coords);
 		if(readyToSend(0)) {
 			flushToNetwork(0);	//0 is server
 		}
 	}
 
 	protected void flushToNetwork(int device) {
 		sendCoordsToServer(coordsToSend[device]);
 		coordsToSend[device].clear();
 	}
 
 	protected synchronized void respondToNetwork(int requester, List<Coords> response) {
 		checkInit();
 
 		coordsToSend[0].addAll(response);
 		if(readyToSend(0)) {
 			sendCoordsToServer(coordsToSend[0]);
 			coordsToSend[0].clear();
 		}
 	}
 
 	@Override
 	public void spawnReceivingThread() {
		
		System.out.println("Spawning receiver thread");	//debug
 
 		new Thread(new Runnable() {
 			public void run() {
				System.out.println("Receiver thread spawned alive="+alive);	//debug
 				checkInit();
 				byte[] receivingData = new byte[1024];
 				//DatagramPacket datagram = new DatagramPacket(receivingData, receivingData.length);
				while(isAlive()) {
					System.out.println("in receiver loop");	//debug
 					try {
 						//socket.receive(datagram);
						System.out.println("Waiting for next packet");	//debug
 						ByteBuffer bb = DataConnectionManager.receive(connections[0], receivingData);	//this is a destructive method on the datagram object
 						System.out.println("Recieved datagram");
 						ClientMessage.processData(bb);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (DroppedPacketException e) {
 						Logger.droppingPacket();
 					}
 					//TODO this in a new thread, so that it can receive while processing it.
 					
 				}
 			}
 		}).start();
 	}
 
 	public void spawnKeepAliveThread() {
 		new Thread(new Runnable() {
 			public void run() {
 				while(alive) {
 					DataConnectionManager.keepAlive();
 					try {
 						Thread.sleep(Config.getKeepAlivePeriod());
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				}
 			}
 		}).start();
 	}
 
 	//this method was based on Server -> NetworkInterface.sendCoordsToDevice
 	private void sendCoordsToServer(List<Coords> coordsList) {
 
 		checkInit();
 		DataConnectionManager.sendCoordsToDevice(connections[0], coordsList);
 
 	}
 
 	private void checkInit() {
 		try {
 			connectToPeers();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(serverSocketAddress == null) {
 			serverSocketAddress = new InetSocketAddress(Config.getServerIP(), Config.getServerPort());
 		}
 	}
 	public void connectToPeers() throws UnknownHostException, IOException {
 		if(connections == null) {
 			connections = new DeviceConnection[1];	//just one connection for server
 			//TODO make it ProtocolManager.numConnections instead or make it do it or something for server and all.
 						connections[0] = DeviceConnection.newConnection(null);
 
 		}
 	}
 
 	@Override
 	protected void protocolSpecificDestroy() {
 		alive = false;	//stop receiving thread TODO warning: thread may be blocking on network so wont actually stop until next packet arrives.
 	}
 
 	@Override
 	public void distributeSession(Session session) throws UnknownHostException, IOException {
 		DataConnectionManager.sendSessionToServer(session);
 	}
 
 	@Override
 	public void sendKeepAliveMessage(int device) {
 		//TODO send it.
 	}
 
 	@Override
 	protected void sendMissingRequest() {
 		try {
 			checkInit();
 			//TODO shouldnt be usig socketAddress here
 			byte[] data = DataConnectionManager.createRequestMessageWithAddress(serverSocketAddress, requestArray);
 			if(data != null) {
 				DataConnectionManager.send(data, connections[0]);
 			}
 		} catch (SocketException e) {
 			//TODO 
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	//TODO these arent strictly correct since in strict c/s protocol, only peer is the server.
 	@Override
 	protected List<Device> requestablePeers() {
 		return Session.getSession().getDevices();
 	}
 	@Override
 	protected List<Device> relientPeers() {
 		return Session.getSession().getDevices();
 	}
 }
