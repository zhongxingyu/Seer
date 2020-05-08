 package model;
 
 import java.io.*;
 
 import javax.swing.event.EventListenerList;
 
 import org.zeromq.*;
 
 import controller.ConnectionListener;
 //import model.CompetitionLogging;
 import model.Logging;
 
 public class TaskServer implements Runnable{
 
 	private ZMQ.Socket refereeSocket;
 	private EventListenerList listOfConnectionListeners = new EventListenerList();
 	private Logging logg;
 	private Thread serverThread;
 	private String teamName;
 	private String commLogID= "Communication";
 	private boolean activeConnection;
 	
 	public TaskServer() {
 		logg = Logging.getInstance();
 		teamName = "";
 		activeConnection = false;
 	}
 
 	public void createServerSocket(String ServerIP, String ServerPort){
 		// Prepare context and socket
 		if(activeConnection == false){
 			ZMQ.Context context = ZMQ.context(1);
 			refereeSocket = context.socket(ZMQ.REP);
 			try {
 				refereeSocket.bind("tcp://" + ServerIP + ":" + ServerPort);
 			}
 			catch(Exception e) {
 				System.out.println("An exception occured the application will be terminated." + "\n" + "Exception: " + e);
 				File file = new File(Logging.logFileName);
 				if (!file.delete()) {
 					System.out.println("Deletion of file >" + Logging.logFileName + "< failed.");
 				}
 				//System.exit(1);
 			} 
 			System.out.println("Server socket created: " + refereeSocket
 					+ " ipAddress: " + ServerIP + " port: " + ServerPort);
 			logg.globalLogging(commLogID, 
 					"Server socket created: " + " ipAddress: " + ServerIP + " port: " + ServerPort);
 		}
 	}
 	
 	public void listenForConnection() {
 		if(activeConnection == false){
 			activeConnection = true;
			teamName = new String("");
			logg.setTeamName(teamName);
 			serverThread = new Thread(this, "Task Server Thread");
 			serverThread.start();
 			System.out.println("Server thread started... ");
 			logg.globalLogging(commLogID, "Server thread started... ");
 		}
 	}
 
 	public void run() {
 		System.out.println("Waiting for Client Requests on socket... "
 				+ refereeSocket);
 		logg.globalLogging(commLogID, "Waiting for Client Requests on socket... ");
 		refereeSocket.setReceiveTimeOut(-1);
 		byte bytes[] = refereeSocket.recv(0);
 		teamName = new String(bytes);
 		System.out.println("Received message: " + teamName + " from client.");
 		logg.setTeamName(teamName);
 		logg.globalLogging(commLogID, "Received message: " + teamName + " from client.");
 		notifyTeamConnected();
 	}
 
 	public ZMQ.Socket getrefereeSocket(){
 		return refereeSocket;
 	}
 	
 	public boolean sendTaskSpecToClient(String tSpec) {
 		// Send task specification
 		if(activeConnection == false){
 			return false;
 		}
 		refereeSocket.send(tSpec.getBytes(), 0);
 		System.out.println("String sent to client: "+ tSpec);
 		logg.globalLogging(commLogID, "String sent to client: "+ tSpec);
 		logg.competitionLogging(commLogID, "String sent to client: "+ tSpec);
 		refereeSocket.setReceiveTimeOut(1000);
 		String tripletAcknowledge = "";
 		byte bytes[] = refereeSocket.recv(0);
 
 		if(!(bytes==null)) {
 			tripletAcknowledge = new String(bytes);
 		}
 
 		if(!tripletAcknowledge.equals("ACK")){
 			System.out.println("Could not send the task specification to the team: " + teamName);
 			logg.globalLogging(commLogID, "Could not send the task specification to the team: " + teamName);
 			logg.competitionLogging(commLogID, "Could not send the task specification to the team: " + teamName);
 			return false;
 		}else{
 			System.out.println("Message from " + teamName + ": " + tripletAcknowledge);
 			logg.globalLogging(commLogID, "Message from " + teamName + ": " + tripletAcknowledge);
 			logg.competitionLogging(commLogID, "Message from " + teamName + ": " + tripletAcknowledge);
 			notifyTaskSpecSent();
 			return true;
 		}
 
 	}
 
 
 	public boolean disconnectClient() {
 		try{
 			
 			if(!teamName.equals("")){
 				System.out.println("teamName " + teamName);
 				refereeSocket.close();
 				activeConnection = false;
 				teamName = "";
 				System.out.println("Client Disconnected");
 				logg.globalLogging(commLogID, "Client Disconnected");
 				notifyTeamDisconnected();
 				return true;
 			}else{
 				return false;
 			}
 			
 		}
 		catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public void addConnectionListener(ConnectionListener cL) {
 		listOfConnectionListeners.add(ConnectionListener.class, cL);
 	}
 
 	public void removeConenctionListener(ConnectionListener cL) {
 		listOfConnectionListeners.remove(ConnectionListener.class, cL);
 	}
 
 	private void notifyTeamConnected() {
 		Object[] listeners = listOfConnectionListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == ConnectionListener.class) {
 				((ConnectionListener) listeners[i + 1]).teamConnected(teamName);
 			}
 		}
 	}
 
 	private void notifyTeamDisconnected() {
 		Object[] listeners = listOfConnectionListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == ConnectionListener.class) {
 				((ConnectionListener) listeners[i + 1]).teamDisconnected();
 			}
 		}
 	}
 
 	private void notifyTaskSpecSent() {
 		Object[] listeners = listOfConnectionListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == ConnectionListener.class) {
 				((ConnectionListener) listeners[i + 1]).taskSpecSent();
 			}
 		}
 	}
 	
 	
 	public String getTeamName() {
 		return teamName;
 	}
 	
 }
