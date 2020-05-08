 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 public class Ranger {
 	private static String nodeName;
 	private static String port;
 	private static FileWriter fileStream;
 	private static ServerThread serverThread;
 	private static Clock clock;
 
 	private static Socket pentagonSocket;
 	private static ObjectInputStream pentagonInput;
 	private static ObjectOutputStream pentagonOutput;
 
 	private static OtherClient otherClientList[] = new OtherClient[2];
 	private static OtherClientThread otherClientThreadList[] = new OtherClientThread[2];
 	private static Request requestQueue[] = new Request[3];
 
 	public static void main(String[] args) throws Exception {
		if(args.length!=2) {
			System.out.println("Usage: Ranger <codename> <port>");
 			System.exit(1);
 		}
 		nodeName = args[0];
		port = args[1];
 
 		clock = new Clock();
 		serverThread = new ServerThread(port);
 
		pentagonSocket = new Socket("localhost", 5000);
 		pentagonOutput = new ObjectOutputStream(pentagonSocket.getOutputStream());
 		pentagonInput = new ObjectInputStream(pentagonSocket.getInputStream());
 
 		pentagonOutput.writeObject("Ranger " + nodeName + " ready to proceed.");
 		pentagonOutput.writeObject(port);
 
 		for(int i=0; i<2; i++)
 			otherClientList[i] = new OtherClient();
 		for(int i=0; i<2; i++)
 			otherClientThreadList[i] = new OtherClientThread(otherClientList[i]);
 
 		for(int i=0; i<2; i++)
 			otherClientThreadList[i].thread.join();
 		serverThread.thread.join();
 
 		pentagonOutput.writeObject("Ranger " + nodeName + " reporting, Mission Accomplished.");
 		pentagonInput.readObject();
 
 		for(int i=0; i<2; i++)
 			serverThread.clientList[i].closeStreams();
 		serverThread.server.close();
 
 		pentagonInput.close();
 		pentagonOutput.close();
 		pentagonSocket.close();
 	}
 
 	private static void report(String msg) throws Exception {
 		fileStream = new FileWriter("report", true);
 		fileStream.write("Ranger " + nodeName + " → " + msg + "\n");
 		fileStream.close();
 		Thread.sleep(2000);
 	}
 
 	private static void requestQueueInsert(Request r) {
 		Request tmp[] = new Request[3];
 		boolean done = false;
 		for(int i=0, j=0; i<3; i++) {
 			if(!done) {
 				if(requestQueue[i]!=null && ((requestQueue[i].timeStamp<r.timeStamp) || (requestQueue[i].timeStamp==r.timeStamp && requestQueue[i].nodePort<r.nodePort))) {
 					tmp[j++] = requestQueue[i];
 				} else {
 					tmp[j++] = r;
 					done = true;
 					i--;
 				}
 			} else {
 				if(requestQueue[i]!=null)
 					tmp[j++] = requestQueue[i];
 			}
 		}
 		requestQueue = tmp;
 	}
 
 //	private static void printRequestQueue() {
 //		System.out.println("RQ:");
 //		for(int i=0; i<3; i++) {
 //			if(requestQueue[i]!=null) {
 //				System.out.println("\t"+i+" "+requestQueue[i].nodePort+" "+requestQueue[i].timeStamp);
 //			}
 //		}
 //	}
 
 	private static void requestQueueRelease(int n) {
 		Request tmp[] = new Request[3];
 		for(int i=0, j=0; i<3; i++) {
 			if(requestQueue[i]!=null && requestQueue[i].nodePort!=n) {
 				tmp[j++] = requestQueue[i];
 			}
 		}
 		requestQueue = tmp;
 	}
 
 	private static class Request {
 		public int timeStamp;
 		public int nodePort;
 
 		public Request(String timeStamp, String nodePort) {
 			this.timeStamp = Integer.parseInt(timeStamp);
 			this.nodePort = Integer.parseInt(nodePort);
 		}
 	}
 
 	private static class Clock {
 		public int timeStamp;
 
 		public Clock() throws Exception {
 			timeStamp = 0;
 		}
 
 		synchronized public void executeCriticalSection() throws Exception {
 			int portNumber = Integer.parseInt(port);
 			while(requestQueue[0].nodePort!=portNumber) {
 				wait();
 			}
 			report("Initialising mission SuperSecret!");
 			report("Hacking the uber super computer!");
 			report("Finishing mission SuperSecret!");
 		}
 
 		synchronized public String event(String msg, ObjectOutputStream out) throws Exception {
 			String tmp[] = msg.split(" ");
 			if(tmp[0].equals("request")) {
 				requestQueueInsert(new Request(tmp[1], tmp[2]));
 				syncTimeStamp(tmp[1]);
 				out.writeObject("ack " + timeStamp + " " + port);
 				return "";
 			} else if(tmp[0].equals("release")) {
 				requestQueueRelease(Integer.parseInt(tmp[2]));
 				syncTimeStamp(tmp[1]);
 				notify();
 				return "";
 			} else if(tmp[0].equals("ask")) {
 				requestQueueInsert(new Request(""+timeStamp, port));
 				timeStamp++;
 				return "request " + (timeStamp-1) + " " + port;
 			} else if(tmp[0].equals("ack")) {
 				syncTimeStamp(tmp[1]);
 				return "release " + timeStamp + " " + port;
 			}
 			return "";
 		}
 
 		private void syncTimeStamp(String time) {
 			int newTimeStamp = Integer.parseInt(time);
 			if(newTimeStamp > timeStamp)
 				timeStamp = newTimeStamp;
 			timeStamp++;
 		}
 	}
 
 	private static class OtherClient {
 		private Socket client;
 		private ObjectInputStream in;
 		private ObjectOutputStream out;
 
 		public OtherClient() throws Exception {
 			String string = (String)pentagonInput.readObject();
 			String tmp[] = string.split(" ");
 			client = new Socket(tmp[0], Integer.parseInt(tmp[1]));
 			out = new ObjectOutputStream(client.getOutputStream());
 			in = new ObjectInputStream(client.getInputStream());
 		}
 	}
 
 	private static class Client {
 		public Socket client;
 		public ObjectInputStream in;
 		public ObjectOutputStream out;
 
 		public Client(Socket client) throws Exception {
 			this.client = client;
 			in = new ObjectInputStream(client.getInputStream());
 			out = new ObjectOutputStream(client.getOutputStream());
 		}
 
 		public void closeStreams() throws Exception {
 			in.close();
 			out.close();
 			client.close();
 		}
 	}
 
 	private static class OtherClientThread implements Runnable {
 		public OtherClient client;
 		public Thread thread;
 
 		public OtherClientThread(OtherClient client) throws Exception {
 			this.client = client;
 			thread = new Thread(this);
 			thread.start();
 		}
 
 		public void run() {
 			try {
 				for(int i=0; i<2; i++)
 					clock.event((String)client.in.readObject(), client.out);
 				closeStreams();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		private void closeStreams() throws Exception {
 			client.in.close();
 			client.out.close();
 			client.client.close();
 		}
 	}
 
 	private static class ServerThread implements Runnable {
 		private ServerSocket server;
 		public Thread thread;
 		public Client clientList[] = new Client[2];
 
 		public ServerThread(String port) throws Exception {
 			server = new ServerSocket(Integer.parseInt(port));
 			thread = new Thread(this);
 			thread.start();
 		}
 
 		public void run() {
 			Socket client;
 			try {
 				for(int i=0; i<2; i++) {
 					client = server.accept();
 					clientList[i] = new Client(client);
 				}
 
 				String message = clock.event("ask", clientList[0].out);
 				for(int i=0; i<2; i++)
 					clientList[i].out.writeObject(message);
 				for(int i=0; i<2; i++)
 					message = clock.event((String)clientList[i].in.readObject(), clientList[0].out);
 				clock.executeCriticalSection();
 				requestQueueRelease(Integer.parseInt(port));
 				for(int i=0; i<2; i++)
 					clientList[i].out.writeObject(message);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
