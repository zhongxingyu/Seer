 package unipi.p2p.chord.util.console;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.net.Socket;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 class Nodes{
 	private String ipAddr;
 	private int port;
 	private int proxyPort;
 	public Nodes(String ipAddr, int port, int proxyPort){
 		this.ipAddr = ipAddr;
 		this.port = port;
 		this.proxyPort = proxyPort;
 	}
 	public String getIpAddr(){
 		return ipAddr;
 	}
 	public int getPort(){
 		return port;
 	}
 	public int getProxyPort(){
 		return proxyPort;
 	}
 }
 class Proxies{
 	private String ipAddr;
 	private int port;
 	public Proxies(String ipAddr, int port){
 		this.ipAddr = ipAddr;
 		this.port = port;
 	}
 	public String getIpAddr(){
 		return ipAddr;
 	}
 	public int getPort(){
 		return port;
 	}
 }
 /*
  * Class which implements the console interface
  */
 public class ConsoleClient {
 	static InetAddress iAddr = null;
 	static Socket socket = null;
 	static PrintWriter pw = null;
 	
 	private static void connect(String ipAddress, int port){
 		try{
 			iAddr = InetAddress.getByName(ipAddress);
 			socket = new Socket(iAddr, port);
 			pw = new PrintWriter(socket.getOutputStream(), true);
 		}catch (UnknownHostException e0){
 			e0.printStackTrace();
 		}catch (IOException e1){
 			e1.printStackTrace();
 		}
 	}
 	
 	private static void disconnect(){
 		if(pw != null)
 			pw.close();
 		try{
 			if(socket != null)
 				socket.close();
 		}catch (IOException e0){
 			e0.printStackTrace();
 		}
 	}
 	private static String help(){
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("+------------+").append("\n");
 		sb.append("|Help Section|").append("\n");
 		sb.append("+------------+").append("\n").append("\n");
 		sb.append(">Available commands:").append("\n");
 		
 		sb.append(">node create IP_ADDR[:PORT] [OPTIONS] - creates a new chord node")
 		.append("\n");
 		sb.append(">\t").append("IP_ADDR[:PORT] - ip address of chord's proxy.");
 		sb.append("\n>\t\t");
 		sb.append("If proxy is running on a non standard port you should define");
 		sb.append("\n>\t\t");
 		sb.append("it with the PORT option.").append("\n");
 		sb.append(">\t").append("-chordport PORT - if you want to run a chord");
 		sb.append("\n>\t\t").append("node on a non standard port you should define it");
 		sb.append("\n>\t\t").append("with this option. Default chord port is 6788");
 		sb.append("\n>\t").append("-config CONFIG - If the chord node should use");
 		sb.append("\n>\t\t").append("a non standard configuration file, define it here");
 		sb.append("\n>\t\t").append("Default configuration file is: config/chord.properties");
 		sb.append("\n").append(">\n");
 		
 		sb.append(">node create -f FILE - creates chord nodes from a comma " +
 				"separated file").append("\n");
 		sb.append(">\t").append("FILE - comma separated file with the following " +
 				"format: proxyip,");
 		sb.append("\n>\t\t").append("proxyport,chordport,chord config file");
 		sb.append("\n").append(">\n");
 		
 		sb.append(">node stop IP_ADDR[:PORT] [OPTIONS] - Stops an already running")
 		.append(" chord node").append("\n");
 		sb.append(">\t").append("IP_ADDR[:PORT] - ip address of chord's proxy.");
 		sb.append("\n>\t\t");
 		sb.append("If proxy is running on a non standard port you should define");
 		sb.append("\n>\t\t").append("it with the PORT option.").append("\n");
 		sb.append(">\t").append("-chordport PORT - If chord node is running on a");
 		sb.append("\n>\t\t").append("non standard port you should define it here.");
 		sb.append("\n").append(">\n");
 		
 		sb.append(">node stop -f FILE - stops all chord nodes listed in a comma " +
 				"separated file").append("\n");
 		sb.append(">\t").append("FILE - comma separated file with the following " +
 				"format: proxyip,");
 		sb.append("\n>\t\t").append("proxyport,chordport,chord config file");
 		sb.append("\n").append(">\n");
 		
 		sb.append(">node list - Lists all running chord nodes.");
 		sb.append("\n").append(">\n");
 		
 		sb.append(">exit - exits from administration console.");
 		
 		return sb.toString();
 	}
 	public static void main(String[] args) {
 		boolean consoleUp = true;
 
 		System.out.println("Welcome to ScorpioFS administration console");
 		System.out.println("Type help for more info");
 		Scanner in = new Scanner(System.in);
 		LinkedList<Nodes> nodesList = new LinkedList<Nodes>();
 		LinkedList<Proxies> proxies = new LinkedList<Proxies>();
 		ExecutorService exec = Executors.newCachedThreadPool();
 		ConsoleClientReceiver consoleRec = new ConsoleClientReceiver(nodesList);
 		exec.execute(consoleRec);
 		
 		while(consoleUp){
 			System.out.print("$>");
 			String command = in.nextLine();
 			String[] tokens = command.split("[ ]");
 
 			//Chord node block
 			if(tokens[0].equals("node")){
 				/*
 				 * Create a chord node
 				 */
 				if(tokens[1].equals("create")){
 					if(tokens.length < 3){
 						System.err.println("Usage: node create IP_ADDR[:port]" +
 								" -chordport PORT -config CONFIG");
 					}else if((tokens.length == 4) && tokens[2].equals("-f")){
 						//load from file
 						try{
 							FileInputStream istream = new FileInputStream(tokens[3]);
 							DataInputStream distream = new DataInputStream(istream);
 							BufferedReader br = new BufferedReader(
 									new InputStreamReader(distream));
 							String line;
 							while ((line = br.readLine()) != null){
 								if(line.startsWith("#"))
 									continue;
 								String ftokens[] = line.split(",");
 								String proxyIp = ftokens[0];
 								int proxyPort = Integer.parseInt(ftokens[1]);
 								int chordPort = Integer.parseInt(ftokens[2]);
 								String chordConfig = ftokens[3];
 								
 								connect(proxyIp, proxyPort);
 								
 								Iterator<Proxies> proxiesIt = proxies.iterator();
 								Proxies prox = null;
 								boolean found = false;
 								
 								while(proxiesIt.hasNext()){
 									prox = proxiesIt.next();
 									if(prox.getIpAddr().equals(proxyIp) && 
 											prox.getPort() == proxyPort){
 										found = true;
 									}
 								}
 								if(!found){
 									Proxies tmpProxy = new Proxies(proxyIp, 
 											proxyPort);
 									proxies.add(tmpProxy);
 								}
 								
 								pw.println(ConsoleProtocol.NODE_CREATE);
 								pw.println(chordPort);
 								pw.println(chordConfig);
 								disconnect();
 								//wait for bootstrap node to get ready
 								while(nodesList.size() < 1){
 									try{
 										TimeUnit.SECONDS.sleep(2);
 									}catch(InterruptedException ex){
 										ex.printStackTrace();
 									}
 								}
 							}
 							br.close();
 							distream.close();
 							istream.close();
 						}catch(FileNotFoundException e){
 							e.printStackTrace();
 						}catch(IOException e){
 							e.printStackTrace();
 						}
 					}else{
 						int chordPort = -1;
 						String chordConfig = null;
 						int i;
 						int chPortIndex = -1;
 						int configIndex = -1;
 						for(i = 0; i < tokens.length; i++){
 							if(tokens[i].equals("-chordport"))
 								chPortIndex = i;
 							if(tokens[i].equals("-config"))
 								configIndex = i;
 						}
 						
 						//Create chord node on a non default port
 						if(chPortIndex != -1){
 							chordPort = Integer.parseInt(tokens[chPortIndex + 1]);
 						}else{
 							chordPort = ConsoleProtocol.CHORD_PORT;
 						}
 						
 						//Explicit define chord properties file
 						if(configIndex != -1){
 							chordConfig = tokens[configIndex + 1];
 						}else{
 							chordConfig = "config/chord.properties";
 						}
 						
 						Iterator<Proxies> proxiesIt = proxies.iterator();
 						Proxies prox = null;
 						boolean found = false;
 						
 						//Proxy running on a non default port
 						if(tokens[2].contains(":")){
 							String ipPort[] = tokens[2].split(":");
 							connect(ipPort[0], Integer.parseInt(ipPort[1]));
 							while(proxiesIt.hasNext()){
 								prox = proxiesIt.next();
 								if(prox.getIpAddr().equals(ipPort[0]) && 
 										prox.getPort() == Integer.parseInt(
 												ipPort[1])){
 									found = true;
 								}
 							}
 							if(!found){
 								Proxies tmpProxy = new Proxies(ipPort[0], Integer.
 										parseInt(ipPort[1]));
 								proxies.add(tmpProxy);
 							}
 							pw.println(ConsoleProtocol.NODE_CREATE);
 							pw.println(chordPort);
 							pw.println(chordConfig);
 						}else{
 							//Proxy running on default port
 							connect(tokens[2], ConsoleProtocol.PROXY_PORT);
 							while(proxiesIt.hasNext()){
 								prox = proxiesIt.next();
 								if(prox.getIpAddr().equals(tokens[2]) && 
 										prox.getPort() == ConsoleProtocol.PROXY_PORT){
 									found = true;
 								}
 							}
 							if(!found){
 								Proxies tmpProxy = new Proxies(tokens[2], 
 										ConsoleProtocol.PROXY_PORT);
 								proxies.add(tmpProxy);
 							}
 							pw.println(ConsoleProtocol.NODE_CREATE);
 							pw.println(chordPort);
 							pw.println(chordConfig);
 						}
 						disconnect();
 					}
 					/*
 					 * Stop a chord node
 					 */
 				}else if(tokens[1].equals("stop")){
 					if(tokens.length < 3){
 						System.err.println("Usage: node stop IP_ADDR[:port]"+
 								" -chordport PORT");
 					}else if((tokens.length == 4) && tokens[2].equals("-f")){
 						System.out.println("Load from file!");
 						//load from file
 						try{
 							FileInputStream istream = new FileInputStream(tokens[3]);
 							DataInputStream distream = new DataInputStream(istream);
 							BufferedReader br = new BufferedReader(
 									new InputStreamReader(distream));
 							String line;
 							while ((line = br.readLine()) != null){
								if (line.startsWith("#"))
									continue;
 								String ftokens[] = line.split(",");
 								String proxyIp = ftokens[0];
 								int proxyPort = Integer.parseInt(ftokens[1]);
 								int chordPort = Integer.parseInt(ftokens[2]);
 								String chordConfig = ftokens[3];
 								
 								connect(proxyIp, proxyPort);
 								
 								Iterator<Proxies> proxiesIt = proxies.iterator();
 								Proxies prox = null;
 								int index = 0;
 								
 								while(proxiesIt.hasNext()){
 									prox = proxiesIt.next();
 									if(prox.getIpAddr().equals(proxyIp) && 
 											prox.getPort() == proxyPort){
 										break;
 									}
 									index++;
 								}
 								proxies.remove(index);
 								
 								pw.println(ConsoleProtocol.NODE_STOP);
 								pw.println(chordPort);
 								pw.println(chordConfig);
 								disconnect();
 							}
 							br.close();
 							distream.close();
 							istream.close();
 						}catch(FileNotFoundException e){
 							e.printStackTrace();
 						}catch(IOException e){
 							e.printStackTrace();
 						}
 					}else{
 						String config = null;
 						int chordPort = -1;
 						int chPortIndex = -1;
 						int i;
 						for(i = 0; i < tokens.length; i++){
 							if(tokens[i].equals("-chordport"))
 								chPortIndex = i;
 						}
 						
 						if(chPortIndex != -1){
 							chordPort = Integer.parseInt(tokens[chPortIndex + 1]);
 						}else{
 							chordPort = ConsoleProtocol.CHORD_PORT;
 						}
 						
 						Iterator<Proxies> proxiesIt = proxies.iterator();
 						int index = 0;
 						Proxies prox =null;
 						
 						//Proxy running on a non default port
 						if(tokens[2].contains(":")){
 							String ipPort[] = tokens[2].split(":");
 							connect(ipPort[0], Integer.parseInt(ipPort[1]));
 							while(proxiesIt.hasNext()){
 								prox = proxiesIt.next();
 								if(prox.getIpAddr().equals(ipPort[0]) && 
 										prox.getPort() == Integer.parseInt(
 												ipPort[1])){
 									break;
 								}
 								index++;
 							}
 							proxies.remove(index);
 							pw.println(ConsoleProtocol.NODE_STOP);
 							pw.println(chordPort);
 							pw.println(config);
 						}else{
 							//Proxy running on default port
 							connect(tokens[2], ConsoleProtocol.PROXY_PORT);
 							while(proxiesIt.hasNext()){
 								prox = proxiesIt.next();
 								if(prox.getIpAddr().equals(tokens[2]) && 
 										prox.getPort() == ConsoleProtocol.PROXY_PORT){
 									break;
 								}
 								index++;
 							}
 							proxies.remove(index);
 							pw.println(ConsoleProtocol.NODE_STOP);
 							pw.println(chordPort);
 							pw.println(config);
 						}
 						disconnect();
 					}
 					/*
 					 * Get statistics from a chord node
 					 */
 				}else if(tokens[1].equals("stat")){
 					if(tokens.length < 3){
 						System.out.println("Usage: node stat PROXY_ADDR[:port]");
 					}else{
 						System.out.println("Go ahead!");
 						int chordPort = -1;
 						String config = "dontcare";
						Iterator<Proxies> proxiesIt = proxies.iterator();
						Proxies tmpProxy = null;
						
						while (proxiesIt.hasNext()){
							tmpProxy = proxiesIt.next();
							connect(tmpProxy.getIpAddr(), tmpProxy.getPort());
 							pw.println(ConsoleProtocol.NODE_STAT);
 							pw.println(chordPort);
 							pw.println(config);
							disconnect();
 						}
 					}
 					//pw.println(ConsoleProtocol.NODE_STAT);
 				}else if(tokens[1].equals("alive")){
 					pw.println(ConsoleProtocol.NODE_ALIVE);
 					/*
 					 * List all alive chord nodes
 					 */
 				}else if(tokens[1].equals("list")){
 					if(nodesList.size() == 0){
 						System.out.print("$>");
 						System.out.println("--> There are no working chord nodes");
 					}else{
 						Nodes tmpNode = null;
 						Iterator<Nodes> nodesIt = nodesList.iterator();
 						System.out.print("$>");
 						int size = nodesList.size();
 						int i = 1;
 						while(nodesIt.hasNext()){
 							tmpNode = nodesIt.next();
 							System.out.println("--> Chord node on ip "+tmpNode
 									.getIpAddr()+" and on port "+tmpNode
 									.getPort()+" is up");
 							if(i < size)
 								System.out.print("$>");
 							i++;
 						}
 					}
 				}else if(tokens.length < 2){
 					System.out.println("Command not found!");
 				}else{
 					System.out.println("Command not found!");
 				}
 				//ScorpioFS block
 			}else if(tokens[0].equals("scorpiofs")){
 				if(tokens[1].equals("mount")){
 					//Call ScorpioFS
 				}else if(tokens[1].equals("unmount")){
 					//System process (fusermount -u)
 				}else if(tokens.length < 2){
 					System.out.println("Command not found!");
 				}else{
 					System.out.println("Command not found!");
 				}
 				/*
 				 * Print help
 				 */
 			}else if(tokens[0].equals("help")){
 				System.out.println(help());
 				/*
 				 * Exit from console
 				 */
 			}else if(tokens[0].equals("exit")){
 				disconnect();
 				consoleRec.stopRunning();
 				consoleUp = false;
 			}else{
 				System.out.println("Command not found!");
 			}
 		}
 		System.out.println("GoodBye!");
 		System.exit(1);
 	}
 
 }
