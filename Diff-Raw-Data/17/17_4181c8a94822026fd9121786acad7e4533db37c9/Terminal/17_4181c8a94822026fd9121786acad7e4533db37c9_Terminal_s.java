 package netproj.controller;
 
 import java.util.Scanner;
 import java.util.logging.*;
 
 import netproj.routers.Router;
 import netproj.skeleton.Device;
 import netproj.skeleton.Link;
 import netproj.skeleton.Packet;
 
 public class Terminal {
 	public static Logger log;
 	public static SimulatorSpecification ss;
 	public static ConsoleHandler ch;
 	public static void main(String args[]) {
 		Logger log = Logger.getLogger("netproj");
 		log.setLevel(Level.FINEST);
 		ch = new ConsoleHandler();
 		ch.setLevel(Level.FINEST);
 		log.addHandler(ch);
		ss = SimulatorSpecification.loadXML("test2.xml");
 //		// this is a hack because idk how to encode the routing table in the xml or if I should
 //		// since we won't often want to hard code this
 //		// Give R1 a routing table
 //		((StaticRouter) ss.getRouters().get(0)).addRoutingTableEntry(ss.getDevices().get(0).getAddress(), 32, 1);
 //		((StaticRouter) ss.getRouters().get(0)).addRoutingTableEntry(ss.getDevices().get(1).getAddress(), 32, 2);
 //		((StaticRouter) ss.getRouters().get(0)).addRoutingTableEntry(0, 0, 0);
 //		
 //		// Give R2 a routing table
 //		((StaticRouter) ss.getRouters().get(1)).addRoutingTableEntry(ss.getDevices().get(0).getAddress(), 24, 0);
 //		((StaticRouter) ss.getRouters().get(1)).addRoutingTableEntry(ss.getDevices().get(2).getAddress(), 32, 2);
 //		((StaticRouter) ss.getRouters().get(1)).addRoutingTableEntry(0, 0, 1);
 //		
 //		// Give R3 a routing table
 //		((StaticRouter) ss.getRouters().get(2)).addRoutingTableEntry(0, 0, 0);
 //		((StaticRouter) ss.getRouters().get(2)).addRoutingTableEntry(ss.getDevices().get(3).getAddress(), 32, 1);
 //		((StaticRouter) ss.getRouters().get(2)).addRoutingTableEntry(ss.getDevices().get(4).getAddress(), 32, 2);
 //		((StaticRouter) ss.getRouters().get(2)).addRoutingTableEntry(ss.getDevices().get(5).getAddress(), 32, 3);
 
 		terminal();
 	}
 	
 	public static void terminal(){
 		String input = new String();
 		Scanner sc = new Scanner(System.in);
 		boolean exit = false;
 		while(!exit){
 			System.out.println("Enter a command (or see help)");
 			input = sc.nextLine();
 			if (input.equals("exit") || input.equals("q")) {
 				exit = true;
 			} else if(input.equals("help")) {
 				System.out.println("Commands include: exit, help, set-level, list, start, terminal ");
 			} else if (input.equals("start") || (input.equals("g"))) {
 				ss.start();
 			} else if (input.startsWith("set-level") || input.startsWith("sl")) {
 				ch.setLevel(Level.parse(input.substring(input.indexOf(' ')+1)));
 			} else if (input.startsWith("terminal")) {
 				ss.getDevice(Integer.decode(input.trim().split(" ")[1])).terminal(sc);
 			} else if (input.equals("list") || input.equals("l")) {
 				System.out.println("Routers: ");
 				for(Router r : ss.getRouters()) {
 					System.out.println("\t" + Integer.toHexString(r.getAddress()));
 				}
 				System.out.println("Hosts: ");
 				for(Device d : ss.getDevices()) {
 					System.out.println("\t" + Integer.toHexString(d.getAddress()));
 				}
 				System.out.println("Links: ");
 				for(Link l : ss.getLinks()) {
 					System.out.print("\t-");
 					for (Device d : l.getDevices()) {
 						System.out.print(Integer.toHexString(d.getAddress()) + "-");	
 					}
 					System.out.println();
 				}
 			} else if (input.startsWith("send ") || input.startsWith("s ")) {
 				// send data between hosts
 				// syntax: host ip, dest ip, size
 				input = input.substring(input.indexOf(' ')+1);
 				String[] options = input.split(" ");
 				int from_ip = Integer.decode(options[0]);
 				int to_ip = Integer.decode(options[1]);
 				int size = Integer.decode(options[2]);
 				Device from = null;
 				boolean fromFound = false;
 				for(Device d : ss.getDevices()) {
 					if(d.getAddress() == from_ip) {
 						from = d;
 						fromFound = true;
 					}
 				}
 				if(fromFound){
 					from.broadcastPacket(new Packet(from_ip,to_ip,size));
 				} else {
 					System.out.println("Invalid from IP");
 				}
 			} else {
 				System.out.println("Unknown command.");
 			}
 		}
 	}
 }
