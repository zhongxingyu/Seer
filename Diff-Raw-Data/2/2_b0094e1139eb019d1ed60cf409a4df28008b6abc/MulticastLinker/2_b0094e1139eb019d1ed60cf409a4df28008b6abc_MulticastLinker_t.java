 package zeroconf.linker;
 
 import java.util.*;
 import java.io.*;
 
 import zeroconf.msg.Msg;
 
 
 public class MulticastLinker {
 	private PrintWriter[] dataOut;
 	private BufferedReader[] dataIn;
 	private BufferedReader dIn;
 	private int myId;
 
 	public MulticastLinker(int id) throws Exception {
 		myId = id;
 		// TODO Creo que _NO_ deberiamos necesitar un Buffer reader por cada cliente
 		// dataIn = new BufferedReader[numProc];
 		// dataOut = new PrintWriter[numProc];
 	}
 
 	public void sendMsg(int destId, String tag, String msg) {
 		dataOut[destId].println(myId + " " + destId + " " + tag + " " + msg
 				+ "#");
 		dataOut[destId].flush();
 	}
 
 	public void sendMsg(int destId, String tag) {
 		sendMsg(destId, tag, " 0 ");
 	}
 
 	public void multicast(LinkedList<Integer> destIds, String tag, String msg) {
 		for (int i = 0; i < destIds.size(); i++) {
 			sendMsg(destIds.get(i), tag, msg);
 		}
 	}
 
 	public Msg receiveMsg(int fromId) throws IOException {
 		String getline = dataIn[fromId].readLine();
 		StringTokenizer st = new StringTokenizer(getline);
 		int srcId = Integer.parseInt(st.nextToken());
 		int destId = Integer.parseInt(st.nextToken());
 		String tag = st.nextToken();
 		String msg = st.nextToken("#");
 		return new Msg(srcId, destId, tag, msg);
 	}
 
 	public int getMyId() {
 		return myId;
 	}
 
 	public void close() {
		//connector.closeSockets();
 	}
 }
