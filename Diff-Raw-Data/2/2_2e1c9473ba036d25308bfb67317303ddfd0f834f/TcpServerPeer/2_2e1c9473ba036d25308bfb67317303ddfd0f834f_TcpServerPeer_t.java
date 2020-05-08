 package ch.hszt.mdp.chatplus.logic.concrete;
 
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import ch.hszt.mdp.chatplus.logic.contract.message.IClientMessage;
 import ch.hszt.mdp.chatplus.logic.contract.peer.IServerPeer;
 
 public class TcpServerPeer implements IServerPeer, Runnable {
 
 	private String serverIP = "";
 	private int serverPort;
 
 	public String getServerIP() {
 		return serverIP;
 	}
 
 	public void setServerIP(String serverIP) {
 		this.serverIP = serverIP;
 	}
 
 	public int getServerPort() {
 		return serverPort;
 	}
 
 	public void setServerPort(int serverPort) {
 		this.serverPort = serverPort;
 	}
 
 	private Socket server;
 	private final Queue<IClientMessage> threadSafeMessageQueue = new LinkedList<IClientMessage>();
 	private final Object lock = new Object();
 
 	private boolean isInterrupted = false;
 
 	@Override
 	public void send(IClientMessage message) {
 
 		synchronized (lock) {
 			threadSafeMessageQueue.add(message);
 			lock.notify();
 		}
 	}
 
 	public void Init() throws UnknownHostException, IOException {
 		server = new Socket(serverIP, serverPort);
 	}
 
 	public void Stop() {
 		isInterrupted = true;
 		lock.notify();
 	}
 
 	public static void main(String[] args) throws UnknownHostException,
 			IOException, InterruptedException {
 		/*
 		 * Socket server = new Socket("192.168.1.55",9999); PrintWriter out =
 		 * new PrintWriter(server.getOutputStream(), true);
 		 * 
 		 * out.println("Hoi Pascal");
 		 */
 
 		TcpServerPeer peer = new TcpServerPeer();
 		peer.setServerIP("192.168.1.55");
 		peer.setServerPort(9999);
 		peer.Init();
 
 		SimpleMessage msg = new SimpleMessage();
 		msg.setSender("Sven");
 		msg.setMessage("Hallo Pascal!");
 
 		Thread t = new Thread(peer);
 		t.start();
 
 		peer.send(msg);
 
 		Thread.sleep(5000);
 	}
 
 	@Override
 	public void run() {
 
 		System.out.println("Starting.");
 		while (!isInterrupted) {
 			System.out.println("Looping.");
 			IClientMessage msg;
 
 			synchronized (lock) {
 				System.out.println("Locked.");
 				msg = threadSafeMessageQueue.poll();
 
 				while (msg != null) {
 					System.out.println("Msg != null.");
 					try {
 						XMLEncoder encoder = new XMLEncoder(
 								new BufferedOutputStream(server
 										.getOutputStream()));
 						encoder.writeObject(msg);
 						encoder.close();
 
 						msg = threadSafeMessageQueue.poll();
 					} catch (Exception ex) {
 						System.out.println("Ex:" + ex.getMessage());
 					}
 				}
 
 				try {
 					System.out.println("Waiting");
 					lock.wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		System.out.println("Dying");
 	}
 	/*
 	 * public void write(Object o, String filename){ try{ XMLEncoder encoder =
 	 * new XMLEncoder( new BufferedOutputStream( new
 	 * FileOutputStream(filename))); encoder.writeObject(o); encoder.close();
 	 * }catch(IOException e){ e.printStackTrace(); } }
 	 * 
 	 * 
 	 * public Object read(String filename){ try{ XMLDecoder decoder = new
 	 * XMLDecoder( new BufferedInputStream( new FileInputStream(filename)));
 	 * Object o = decoder.readObject(); decoder.close(); return o; }catch
 	 * (FileNotFoundException e){ e.printStackTrace(); } return null; }
 	 */
 
 }
