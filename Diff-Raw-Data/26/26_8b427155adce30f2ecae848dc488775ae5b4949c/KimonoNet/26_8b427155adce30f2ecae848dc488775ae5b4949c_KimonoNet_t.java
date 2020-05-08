 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoVelocity;
 import kimononet.net.Packet;
 import kimononet.net.beacon.BeaconPacket;
 import kimononet.net.p2p.Connection;
 import kimononet.net.p2p.MulticastConnection;
 import kimononet.net.p2p.port.PortConfiguration;
 import kimononet.net.parcel.Parcel;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAddress;
 import kimononet.peer.PeerAgent;
 
 
public class KimonoNet {
 
 	public static void main(String args[]){
 		System.out.println("Starting Simulation");
 		
 		PeerAddress addressA = new PeerAddress("12:00:00:00:00:00");
 		GeoLocation locationA = new GeoLocation(1.0, 2.0, 3.0f);
 		GeoVelocity velocityA = new GeoVelocity(locationA);
 		
 		PeerAgent agentA = new PeerAgent(new Peer(addressA, locationA, velocityA)); 
 		
 		//packetTest(agentA);
 	
 		
 		agentA.startServices();
 		
 		
 		
 		//geoLocationTest();
 		//peerAddressTest();
 		//parcelTest();
 		
 		/* TESTS MULTICAST CONNECTIONS:
 		SimulationPortConfigurationProvider confProvider = new SimulationPortConfigurationProvider();
 		connectionTest(confProvider.getPortConfiguration(null));
 		connectionTest(confProvider.getPortConfiguration(null));
 		*/
		
 	}
 	
 	public static void parcelTest(){
 		
 		Parcel parcel = new Parcel(16);
 		
 		System.out.println(parcel);
 		int a = 32;
 		int b = 64;
 		
 		parcel.add(a);
 		parcel.add(b);
 		
 		parcel.rewind();
 		
 		System.out.println(parcel.getInt());
 		System.out.println(parcel.getInt());
 
 	}
 	
 	public static void packetTest(PeerAgent agent){
 		Packet packet = new BeaconPacket(agent);
 		
 		System.out.println(packet);
 		
 		Parcel parcel = packet.toParcel();
 		
 		System.out.println("PACKET PARCEL: \t " + parcel);
 	
 		System.out.println("--PARSED PACKET--");
 		System.out.println(new BeaconPacket(parcel));
 		
 	}
 	
 	public static void geoLocationTest(){
 		
 		GeoLocation location1 = new GeoLocation(1.0, 2.0, 3.0f);		
 		GeoLocation location2 = new GeoLocation(4.0, 5.0, 6.0f);
 		
 		System.out.println(location2);
 		location2.setLocation(location1.toParcel());
 		System.out.println(location2);
 	}
 	
 	public static void peerAddressTest(){
 		
 		PeerAddress address = new PeerAddress("12:00:00:00:00:00");
 		
 		System.out.println(address);
 	}
 	
 	public static void connectionTest(PortConfiguration conf){
 		
 		final int serverPort = conf.getDataSendingServicePort();
 		final int clientPort = conf.getDataReceivingServicePort();
 		
 		final String serverAddress = conf.getAddress();
 		final String clientAddress = conf.getAddress();
 	
 		
 		Thread server = new Thread(){
 			public void run(){
 				
 				Connection server = new MulticastConnection(serverPort, serverAddress);
 				
 				if(!server.connect()){
 					System.out.println("Server socket could not connect!");
 					return;
 				}
 				
 				while(true){
 					
 					try {
 						
 						GeoLocation location = new GeoLocation(1.0, 2.0, 3.0f);		
 						
 						System.out.println("Sending data...");
 						server.send(location.toParcel().toByteArray(), clientPort,serverAddress);
 						System.out.println("Sent data!");
 						
 						sleep(1000);
 				
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 						break;
 						
 					}
 					
 					
 				}
 				
 			}
 		};
 		
 		Thread client = new Thread(){
 			
 			public void run(){
 				
 			
 				Connection client = new MulticastConnection(clientPort, clientAddress);
 			
 				client.connect();
 				client.setBlocking(true);
 				byte[] buffer;
 				while(true){
 				
 					try{
 						
 						System.out.println("Receiving data...");
 						buffer = client.receive();
 						
 						//Check to see if the data was received.
 						if(buffer != null){
							System.out.println("Data Received! \t" + new GeoLocation(new Parcel(buffer)));
 						}else{
 							System.out.println("Data not received!");
 						}
 						
 						sleep(1000);
 						
 					}catch (InterruptedException e) {
 						e.printStackTrace();
 						break;
 					}
 				
 				}
 				
 				
 				
 				
 			}
 		};
 		
 	server.start();
 	client.start();
 		
 		
 		
 	}
 }
