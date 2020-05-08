 package cz.emo4d.zen.remote;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 
 public class RemoteControl {
 
 	private static final int serverPort = 5869;
 	private static final int discoveryPort = 10869;
 	private final static String SERIALIZER_DELIMITER = "&&&&";
 
 	private ArrayList<Thread> clients;
 	private ArrayList<DeviceEvent> events;
 	private ArrayList<PrintStream> outputs;
 	private HashMap<Integer,ClientMove> clientMoves;
 	
 	private ThreadLocal<Integer> clientIdHolder;
 
 	private DeviceEventHandler callback = null;
 	//private PrintStream output;
 
 	public RemoteControl() {
 		clients = new ArrayList<Thread>();
 		events = new ArrayList<DeviceEvent>();
 		clientMoves = new HashMap<Integer,ClientMove>();
 		outputs = new ArrayList<PrintStream>();
 
 		clientIdHolder = new ThreadLocal<Integer>();
 		
 		startTCPServer();
 
 		AutoDiscoveryAsync exe = new AutoDiscoveryAsync();
 		exe.start();
 
 	}
 
 	private void startTCPServer() {
 
 		RemoteControlAsync rca = new RemoteControlAsync();
 		rca.start();
 
 		MessageGetter mg = new MessageGetter();
 		mg.start();
 
 	}
 
 	public void RegisterEventHandler(DeviceEventHandler cb) {
 		callback = cb;
 	}
 
 	private synchronized DeviceEvent getMessage() throws InterruptedException {
 		while (events.size() == 0)
 			wait();
 		DeviceEvent de = events.get(0);
 		events.remove(0);
 		return de;
 	}
 
 	private synchronized void putMessage(DeviceEvent de) {
 		events.add(de);
 		notify();
 	}
 
 	DeviceEvent expandEvent(String s) {
 		int pos = s.indexOf(SERIALIZER_DELIMITER);
 		String first = s.substring(0, pos);
 		String secondThird = s.substring(pos + SERIALIZER_DELIMITER.length());
 
 		pos = secondThird.indexOf(SERIALIZER_DELIMITER);
 		String second = secondThird.substring(0, pos);
 		String third = secondThird.substring(pos
 				+ SERIALIZER_DELIMITER.length());
 
 		DeviceEvent de = new DeviceEvent();
 		de.eventType = Integer.parseInt(first);
 		de.valueX = Float.parseFloat(second);
 		de.valueY = Float.parseFloat(third);
 		de.player = clientIdHolder.get();
 
 		return de;
 	}
 
 	class AutoDiscoveryAsync extends Thread {
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					DatagramSocket socket = new DatagramSocket(discoveryPort);
 					// socket.setBroadcast(true);
 
 					byte[] buf = new byte[1024];
 					DatagramPacket packet = new DatagramPacket(buf, buf.length);
 					Gdx.app.log("", "Discovery waiting");
 					socket.receive(packet);
 
 					InetAddress clientAddr = packet.getAddress();
 
 					InetAddress thisIp = InetAddress.getLocalHost();
 					String ip = thisIp.getHostAddress();
 
 					Gdx.app.log("Discovery", clientAddr.getHostAddress());
 
 					socket.close();
 					
 					Thread.sleep(300);
 
 					DatagramSocket socket2 = new DatagramSocket();
					//socket2.setBroadcast(true);
 
					DatagramPacket packet2 = new DatagramPacket(ip.getBytes(), ip.length(),
 							clientAddr, discoveryPort);
					socket2.send(packet2);
 					socket2.close();
 
 					Gdx.app.log("", "Discovery sent");
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		}
 	}
 
 	class RemoteControlAsync extends Thread {
 
 		@Override
 		public void run() {
 			ServerSocket server = null;
 
 			try {
 				server = new ServerSocket();
 
 				SocketAddress addr = new InetSocketAddress(
 						InetAddress.getByName("0.0.0.0"), serverPort);
 				server.bind(addr);
 
 				while (true) {
 					Socket s = server.accept();
 					ClientThread newclient = new ClientThread(s);
 					clients.add(newclient);
 					newclient.start();
 					Gdx.app.log("AA", "Client connected");
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	class ClientThread extends Thread {
 
 		Socket client = null;
 
 		public ClientThread(Socket s) {
 			client = s;
 		}
 
 		@Override
 		public void run() {
 			
 			Integer clientId = clientIdHolder.get();
 			clientId = clients.size() - 1;
 			clientIdHolder.set(clientId);
 			
 			try {
 				outputs.add(new PrintStream(client.getOutputStream()));
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						client.getInputStream()));
 
 				while (true) {
 					String message = in.readLine();
 					if (message == null)
 						break;
 					//Gdx.app.log("INC", message);
 					putMessage(expandEvent(message));
 				}
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	class MessageGetter extends Thread {
 
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					DeviceEvent de = getMessage();
 //					Gdx.app.log("Incoming",
 //							"Incoming type: " + Integer.toString(de.eventType)
 //									+ ", valX:" + Float.toString(de.valueX)
 //									+ ", valY:" + Float.toString(de.valueY));
 
 					if (de.eventType == DeviceEvent.MOVE) {
 						//Integer clientId = clientIdHolder.get();
 						//Gdx.app.log("UPDATE POSITION", Integer.toString(de.player));
 						ClientMove cm = clientMoves.get(de.player);
 						if (cm == null) cm = new ClientMove();
 						cm.X = de.valueX;
 						cm.Y = de.valueY;
 						clientMoves.put(de.player, cm);
 					}
 					
 					if (callback != null) {
 						callback.acceptEvent(de.eventType, de.player, de.valueX, de.valueY);
 					}
 
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public ClientMove getClientMove(int client) {
 		if (clientMoves.size() >= client) {
 			return clientMoves.get(client);
 		} else {
 			return null;
 		}
 	}
 	
 	
 	
 	/////////////////////////////////////////////////////////
 	
 	
 	public void emitEvent(int client, int event) {
 				
 		//Gdx.app.log("EMIT", "EVENT");
 		
 		SendAsync exe = new SendAsync(client,Integer.toString(event));
 		exe.start();
 		
 	}
 	
 	private class SendAsync extends Thread {
 
 		int client;
 		private String message;
 		
 		public SendAsync (int c,String m) {
 			message = m;
 			client = c;
 		}
 		
 		@Override
 		public void run() {
 			if (outputs.get(client) != null) {
 				outputs.get(client).println(message);
 				outputs.get(client).flush();
 			}
 		}
 
 	}
 
 }
