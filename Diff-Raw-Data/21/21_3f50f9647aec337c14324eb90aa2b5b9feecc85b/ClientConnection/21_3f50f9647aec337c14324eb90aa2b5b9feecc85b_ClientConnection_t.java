 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 
 public class ClientConnection implements Runnable {
     InetAddress addr;
    int multicastPort;
     InetAddress serverAddr;
     int port;
     String name;
     ArrayList<ClientInfo> participants = new ArrayList(); //List containing information about other participants
 
     Socket socket;
     OutputStream os;
 	ObjectOutputStream out;
 	InputStream is;
 	ObjectInputStream in;
 
    public ClientConnection (InetAddress addr, InetAddress serverAddr, int port, String name, int multicastPort) {
 	    this.addr = addr;
 	    this.port = port;
 	    this.serverAddr = serverAddr;
 	    this.name = name;
	    this.multicastPort = multicastPort;
     }
 
     public synchronized ArrayList getParticpants () {
 		return participants;
 	}
 
 	public synchronized ClientInfo getParticipant (int i) {
 		return participants.get(i);
 	}
 
 	/*public synchronized void addParticipant(String ip, String name, int port) {
 		try {
 		    ClientInfo outMessage = new ClientInfo(ip, name, port);
 
 		    out.writeObject(outMessage);
 	    } catch (Exception e) {
 			e.printStackTrace();
 		}
 	}*/
 
 	public void addParticipantToList(ClientInfo c) {
 		participants.add(c);
 	}
 
 	public void wipeParticipants () {
 		participants = new ArrayList();
 	}
 
 	public synchronized void processMessage(ObjectOutputStream out, ObjectInputStream in) {
 		try {
 		    ClientInfo inMessage;
 
 		    while (true) {
 				out.writeObject(new ClientInfo("QUERY", "", 0)); //Query the server (this gives readObject something to do and prevents blocking)
 
                 inMessage = (ClientInfo)in.readObject();
 
 		        if (inMessage.ip.equals("NONE")) {
 					break;
 				} else if (inMessage.ip.equals("END")) { //If server is done sending
 
 		            break;
 		        } else if (inMessage.ip.equals("NEW")) {
 			        this.wipeParticipants();
 
 		        } else if (inMessage.ip != null) {  //If new participant
 			        this.addParticipantToList(inMessage);
 			        System.out.println(inMessage.name);
 		        }
 	        }
 
 	    } catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
     public void run() {
 	    socket = null;
 
 		os = null;
 		out = null;
 		is = null;
 		in = null;
 
 		try{
 
 
 					      socket = new Socket(serverAddr, port);
 
 					      os = socket.getOutputStream();
 					      out = new ObjectOutputStream(os);
 
 					      is = socket.getInputStream();
 					      in = new ObjectInputStream(is);
 
					      out.writeObject(new ClientInfo(addr.toString().substring(1), name, multicastPort));
					      System.out.println("Sending: " + addr.toString().substring(1) + " " +  name + " " +  multicastPort);
 
 					      while (true) {
 
 							  if (Thread.interrupted()) { //Thread has been told to close
							      ClientInfo end = new ClientInfo(addr.toString().substring(1), name, multicastPort);
 							      end.removeThis = true;
 							      out.writeObject(end);
 
 								  //in.close();
 								  //out.close();
 								  //socket.close();
 
 								  ClientInfo inMessage;
 
 								  while (true) {
 
                                       inMessage = (ClientInfo)in.readObject();
 								      if (inMessage.ip.equals("CONNECTIONEND")) {
 
 									      return;
 								      }
 							      }
 							  }
 							  processMessage(out, in);
 					      }
 
 		} catch (Exception e) {
 					      System.out.println("Exception");
 					      e.printStackTrace();
 	    }
     }
 }
 
