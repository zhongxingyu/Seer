 package main;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class Client extends Thread {
 
 	private Socket socket;
 	private Main main;
 	private InputStreamReader in;
 	PrintWriter out;
 	private long lastPinged = System.currentTimeMillis();
 	@SuppressWarnings("unused")
 	private long lastPing = 0;
 	public int id;
 
 	public Client(Socket socket, Main main) {
 		this.socket = socket;
 		this.main = main;
 	}
 
 	public void init(int id) {
 		this.id = id;
 		System.out.println("Client connected: " + socket.getInetAddress() + ":" + socket.getPort());
 		try {
 			in = new InputStreamReader(socket.getInputStream());
 			out = new PrintWriter(socket.getOutputStream(), true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void run() {
 		FPS fps = new FPS();
 		while (main.isRunning() && main.clients.containsKey(this)) {
 			if (System.currentTimeMillis() - lastPinged >= 10000) {
 				System.out.println("Client timed out");
 				main.removeClient(this);
 			}
 			try {
 				if (in.ready()) {
 					String string = "";
 					int chr = in.read();
 					while (chr != -1 && chr != 10) {
 						string += Character.toString((char) chr);
 						chr = in.read();
 					}
 
 					String[] stringArray = (string.replaceAll("\\r|\\n", "")).split("\\|");
 					if (stringArray[0] != "") {
 						if (!((Integer.parseInt(stringArray[0])) + "").equals(stringArray[0])) {
 							System.out.println("Received unreadable message: " + string);
 						} else {
 							int messageId = Integer.parseInt(stringArray[0]);
 							if (Integer.parseInt(stringArray[1]) == 1) {
 								switch (messageId) {
									case 1: // pinged
										out.println(string);
 										out.flush();
 										lastPinged = System.currentTimeMillis();
										lastPing = System.currentTimeMillis() - Long.parseLong(stringArray[2]);
 									break;
 
 									case 2:// New player joined, reply with ID
 										System.out.println("Sending ID: " + id);
 										out.println("2|" + id);
 										out.flush();
 										for (Client currentClient : main.clients.keySet()) {
 											if (currentClient != this) {
 												out.println("3|" + currentClient.id);
 												out.flush();
 											}
 										}
 									break;
 
 									case 4:
 									break;
 
 									default:
 										System.out.println("Received unreadable message: " + string);
 									break;
 								}
 							} else {
 								for (Client currentClient : main.clients.keySet()) {
 									if (currentClient != this) {
 										currentClient.out.println(string);
 										out.flush();
 									}
 								}
 							}
 						}
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			fps.updateFPS();
//			try {
//				Thread.sleep(fps.getSleepmillis());
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
 		}
 	}
 }
