 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 public class Agent {
 	private static String nodeName;
	private static String port;
 	private static FileWriter fileStream;
 
 	private static Socket pentagonSocket;
 	private static ObjectOutputStream pentagonOutput;
 	private static ObjectInputStream pentagonInput;
 
 	private static OtherClient otherClientList[] = new OtherClient[2];
 
 	public static void main(String[] args) throws Exception {
		if(args.length!=3) {
			System.out.println("Usage: Agent <codename> <host> <port>");
 			System.exit(1);
 		}
 		nodeName = args[0];
		port = args[2];
 
		pentagonSocket = new Socket(args[1], 5000);
 		pentagonOutput = new ObjectOutputStream(pentagonSocket.getOutputStream());
 		pentagonInput = new ObjectInputStream(pentagonSocket.getInputStream());
 
 		pentagonOutput.writeObject("Agent " + nodeName + " ready to proceed.");
		pentagonOutput.writeObject(port);
 
 		for(int i=0; i<2; i++)
 			otherClientList[i] = new OtherClient();
 
 		report("Initialising mission SuperSecret!");
 		report("Hacking the uber super computer!");
 		report("Finishing mission SuperSecret!");
 
 		pentagonOutput.writeObject("Agent " + nodeName + " reporting, Mission Accomplished.");
 		pentagonInput.readObject();
 
 		pentagonInput.close();
 		pentagonOutput.close();
 		pentagonSocket.close();
 	}
 
 	private static void report(String msg) throws Exception {
 		fileStream = new FileWriter("report", true);
 		fileStream.write("Agent " + nodeName + " → " + msg + "\n");
 		fileStream.close();
 		Thread.sleep(2000);
 	}
 
 	private static class OtherClient {
 		private String string;
 
 		public OtherClient() throws Exception {
 			string = (String)pentagonInput.readObject();
 		}
 	}
 }
