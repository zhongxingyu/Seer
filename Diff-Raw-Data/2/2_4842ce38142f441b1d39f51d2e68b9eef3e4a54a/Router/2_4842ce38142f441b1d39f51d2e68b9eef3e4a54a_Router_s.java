 package org.th3falc0n.nn2;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 
 import org.th3falc0n.nn2.packets.Packet;
 import org.th3falc0n.nn2.packets.handler.HandlerHandshake;
 import org.th3falc0n.nn2.packets.handler.HandlerPing;
 import org.th3falc0n.nn2.packets.handler.HandlerRouting;
 import org.th3falc0n.nn2.packets.handler.HandlerStreaming;
 import org.th3falc0n.nn2.packets.handler.PacketHandler;
 
 public class Router {
 	public static Router $Instance = new Router();
 	
 	public static void main(String[] args) throws IOException {
 		try {
 			$Instance.start();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
 
 		//TODO: No console here.
 		while(true) {
 			String input = sysin.readLine();
 			
 			String[] command = input.split(" ");
 			
 			if(command[0].equalsIgnoreCase("add")) {
 				Socket sock = new Socket(command[1], Integer.parseInt(command[2]));
 				
 				Port port = new Port(sock);
 				$Instance.addPort(port, false);
 			}
 			
 			if(command[0].equalsIgnoreCase("ping")) {
 				$Instance.routePacket(HandlerPing.getPingPacket(new Address(command[1])));
 			}
 			
 			if(command[0].equalsIgnoreCase("str")) {
 				new HandlerStreaming.NNSocket(16, 0, new Address(command[1])).getOutputStream().write(new String("Hallo da drauen dies muss ein langer String werden :)").getBytes());
 			}
 			
 			if(command[0].equalsIgnoreCase("strs")) {
 				System.out.println("Awaiting stream");
 				byte[] test = new byte[32];
 				new HandlerStreaming.NNServerSocket(0).accept().getInputStream().read(test);
 				System.out.println("Accepted stream: " + new String(test));
 			}
 		}
 	}
 	
 	volatile List<Port> ports;
 	volatile Map<Port, Map<String, Integer>> routes = new HashMap<Port, Map<String, Integer>>();
 	
 	volatile Address address;
 	
 	public Router() {
 		ports = new Vector<Port>(0,1);
 	}
 	
 	public void start() throws IOException {
 		PacketHandler.init();
 		
 		ConnectionListener listener = new ConnectionListener();
 		listener.startListening();
 	}
 	
 	public void routePacket(Packet packet) {
 		for(Port p : ports) {
 			if(!p.isAddressOptimized(packet.getDestination())) {
 				p.enqueuePacket(HandlerRouting.getRequestHopCountForAddressPacket(p.remoteAddress, packet.getDestination()));
 				p.setAddressOptimized(packet.getDestination());
 			}			
 			if(packet.getDestination().toString().equals(p.remoteAddress.toString())) {
 				p.log("DC-Routed packet from " + packet.getSource().toString());
 				p.enqueuePacket(packet);
 				return;
 			}
 		}
 
 		Port ideal = getIdealPortForAddress(packet.getDestination());
 		if(ideal != null) {
 			ideal.log("HP-Routed packet from " + packet.getSource().toString());
 			ideal.enqueuePacket(packet);
 			return;
 		}
 
 		Port rnd = ports.get(new Random().nextInt(ports.size()));
 		
 		rnd.log("RND-Routed packet from " + packet.getSource().toString());
 		rnd.enqueuePacket(packet);
 	}
 	
 	public Port getIdealPortForAddress(Address addr) {
 		String address = addr.toString();
 		
 		Port port = null;
 		int hops = Integer.MAX_VALUE;
 		
 		for(Port p : ports) {
 			if(getRoutes().get(p).containsKey(address)) {
 				if(getRoutes().get(p).get(address) < hops) {
 					hops = getRoutes().get(p).get(address);
 					port = p;
 				}
 			}
 		}
 		
 		return port;
 	}
 	
 	public int getIdealHopsForAddress(Address addr) {
 		String address = addr.toString();
 		
 		int hops = Integer.MAX_VALUE;
 		
 		for(Port p : ports) {
 			if(getRoutes().get(p).containsKey(address)) {
 				if(getRoutes().get(p).get(address) < hops) {
 					hops = getRoutes().get(p).get(address);
 				}
 			}
 		}
 		
 		return hops;
 	}
 	
 	public Address getAddress() {
 		return address;
 	}
 	
 	int portID = 0;
 	
 	public void addPort(Port port, boolean server) {
 		port.init(server, portID++);
 		ports.add(port);
 		getRoutes().put(port, new HashMap<String, Integer>());
 		
 		if(!server) {
 			port.queue.add(HandlerHandshake.getRequestPacket(Address.getStraightcastAddress()));
 		}
 	}
 
 	public void learnRoute(Port port, String addr, int hops) {
 		if(!getRoutes().get(port).containsKey(addr)) {
 			getRoutes().get(port).put(addr, hops);
 		}
 	}
 
 	public Map<Port, Map<String, Integer>> getRoutes() {
 		return routes;
 	}
 
 	public void setRoutes(Map<Port, Map<String, Integer>> routes) {
 		this.routes = routes;
 	}
 }
