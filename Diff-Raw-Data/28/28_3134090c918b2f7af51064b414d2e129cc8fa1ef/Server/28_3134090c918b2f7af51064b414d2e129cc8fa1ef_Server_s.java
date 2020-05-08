 package me.timellis.quack.network;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class Server {
 
 	static ArrayList<IncomingServerThread> incomingThreads = new ArrayList<IncomingServerThread>();
 	static ArrayList<OutgoingServerObject> outgoingStreams = new ArrayList<OutgoingServerObject>();
 
 	public static void main(String[] args) {
 
 		System.out.println("SpaceServer v1.0");
 		System.out.println();
 		System.out.println("Waiting for players..");
 		try {
 			ServerSocket s = new ServerSocket(8189);
 			int i = 0;
 			while (true) {
 				Socket incoming = s.accept();
 				System.out.println("Player " + i + " connected.");
				ObjectOutputStream oos = new ObjectOutputStream(
						incoming.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(
						incoming.getInputStream());
				IncomingServerThread it = new IncomingServerThread(ois, i,
						outgoingStreams);
 				it.start();
 				incomingThreads.add(it);
 				outgoingStreams.add(new OutgoingServerObject(oos, i));
 				i++;
 			}
 		} catch (Exception e) {
 			System.out.println("Server error!");
 		}
 	}
 }
 
 class IncomingServerThread extends Thread {
 	private int playerNo;
 	private ObjectInputStream ois;
 	private ArrayList<OutgoingServerObject> outgoingStreams;
 
	public IncomingServerThread(ObjectInputStream ois, int c,
			ArrayList<OutgoingServerObject> outgoingStreams) {
 		playerNo = c;
 		this.outgoingStreams = outgoingStreams;
 		this.ois = ois;
 	}
 
 	public void run() {
 		try {
 			while (true) {
 				MultiplayerData md = (MultiplayerData) ois.readObject();
 				if (md != null) {
					// System.out.println("Rec'd from player " + playerNo + ": "
					// + md);
 					md.playerNo = playerNo;
 					for (int i = 0; i < outgoingStreams.size(); i++) {
						// System.out.println("sending" +
						// outgoingStreams.get(i).playerNo + ", " + playerNo);
 						if (outgoingStreams.get(i).playerNo != playerNo)
 							outgoingStreams.get(i).send(md);
 					}
 				}
 			}
 		} catch (Exception e) {
 			System.out.println("Player " + playerNo + " disconnected.");
 			for (int i = 0; i < outgoingStreams.size(); i++) {
 				if (outgoingStreams.get(i).playerNo == playerNo) {
 					outgoingStreams.remove(i);
 					break;
 				}
 			}
 		}
 	}
 }
 
 class OutgoingServerObject {
 	public int playerNo;
 	private ObjectOutputStream oos;
 
 	public OutgoingServerObject(ObjectOutputStream oos, int c) {
 		playerNo = c;
 		this.oos = oos;
 	}
 
 	public void send(MultiplayerData md) {
 		try {
 			// System.out.println("Sending");
 			oos.writeObject(md);
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 }
