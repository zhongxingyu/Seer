 package datapath;
 
 import guiFX.Browser;
 import guiFX.FlowBuffer;
 
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import javafx.application.Platform;
 import topo.ConnectionList;
 import topo.GuiConnection;
 import topo.NodeList;
 
 public class TrafficWatch implements Runnable{
 
 	public ConnectionList connections;
 	public NodeList nodes;
 	public Browser browser;
 	public ArrayList<String> devices;
 	public ArrayList<NodePuppet> puppetList;
 	private String 				newLine 				= System.getProperty("line.separator");
 	
 	//FlowBuffer buffer;
 	FlowBuffer buffer;
 	public Boolean run;
 	
 	private int protocol;
 	private String sourceIP;
 	private int sourcePort;
 	private String destIP;
 	private int destPort;
 	
 	public TrafficWatch(NodeList nodes,
 						ConnectionList connections, 
 					    Browser browser, 
 					    ArrayList<String> devices, 
 					    String _protocol,
 					    String _sourceIP,
 					    int _sourcePort,
 					    String _destIP,
 					    int		_destPort){
 		
 		System.out.println("Hello from Traffic Watch");
 		
 		this.browser = browser;
 		this.connections = connections;
 		this.nodes = nodes;
 		this.devices = devices;
 		this.puppetList = new ArrayList<NodePuppet>();
 		this.run = true;
 		this.sourceIP = _sourceIP;
 		this.sourcePort = _sourcePort;
 		this.destIP = _destIP;
 		this.destPort = _destPort;
 		this.buffer = new FlowBuffer(connections);
 		String proto = _protocol.toLowerCase();
 		//Protocol
 		switch(proto){
 		case "icmp" : this.protocol = 1; this.sourcePort = 0; this.destPort = 0; break;
 		case "igmp" : this.protocol = 2; break;
 		case "tcp" : this.protocol = 6; break;
 		case "egp" : this.protocol = 8; break;
 		case "igrp" : this.protocol = 9; break;
 		case "udp" : this.protocol = 17; break;
 		case "rsvp" : this.protocol = 46; break;
 		case "gre" : this.protocol = 47; break;
 		case "ah" : this.protocol = 51; break;
 		default : this.protocol = 256; break;
 		
 
 		}
 		
 	}
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		System.out.println("Starting Traffic Watch");
 		System.out.println(this.devices);
 
 		for(String node : devices){
 			System.out.println("Adding " + node);
 			puppetList.add(new NodePuppet(node, "cisco", "cisco", protocol, sourceIP, sourcePort, destIP, destPort, buffer));
 		}
 		
 
 			try {
 				for(NodePuppet puppet: puppetList){
 					new Thread(puppet).start();
 					Thread.sleep(2000);
 				}
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			while(this.run){
 				
 				try {
 					Thread.sleep(10000);
 				/*	//FlowBuffer.getItem();
 					//ArrayList<String> contents = FlowBuffer.checkLoss();
 					//System.out.println("Buffer is ");
 					//for(String packet: contents){
 						//System.out.println(packet);
 						//String[] parts = packet.split(",");
 						//System.out.println("Lost packet from " + parts[0] +" on interface " + parts[1]);
 					}
 					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + newLine +
 									   "~~~~~~~~~~~~Current Gui Connections~~~~~~~~~~~~" + newLine +
 									   "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 					for(GuiConnection connect : this.connections){
 						System.out.println(connect.getInfo());
 					}*/
 					//System.out.println(nodes.printNodes() + newLine + connections.printConnections());
 					//System.out.println(connections.printConnections());
					System.out.println("~~~~~~~~~~~~~Current Loss~~~~~~~~~~~~~");
 					connections.printLoss();
 					//browser.loadTopo(nodes.printNodes() + newLine + connections.printConnections());
 					Platform.runLater(new Runnable(){
 						@Override
 						public void run(){
 							try {
 								browser.loadTopo(nodes.printNodes() + newLine + connections.printConnections());
							
								//System.out.println(connections.printConnections());
 								//browser.loadTopo(getJsonTopo());
 							} catch (MalformedURLException | FileNotFoundException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					});
 					
 				} catch (InterruptedException  e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		
 	}
 
 }
