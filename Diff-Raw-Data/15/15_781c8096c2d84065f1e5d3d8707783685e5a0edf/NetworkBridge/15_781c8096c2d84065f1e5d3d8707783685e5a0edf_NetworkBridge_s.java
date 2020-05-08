 /*
 ** Author: Nikhil Handyal
 ** Date: 10/31/12
 ** Project: Cs200-Factory
 ** Description: Provides a simple to use asynchronous network communication protocol
 **
 ** Pre-Conditions: None
 */
 
 package factory.global.network;
 
 //Java packages
 import java.io.*;
 import java.net.*;
 import java.util.TreeMap;
 import java.util.ArrayList;
 
 //user packages
 import factory.global.data.*;
 
 public class NetworkBridge{
 		ObjectOutputStream oos;
 		InputStreamListener isl;
 		int id = -1;
 		Socket s = null;
 		
 		// ---------------------------------------------------------------------------------------------------------- //
 		// ----------------------------------------------CONSTRUCTORS------------------------------------------------ //
 		// ---------------------------------------------------------------------------------------------------------- //
 		
 		// Client Side Constructor
 		public NetworkBridge(NetworkManager parent, String remoteURL, int port, int cID){
 				try{
 						s = new Socket(remoteURL,port);
 				}catch(Exception e){
 						System.out.println("Fatal Error: Server Connection Failed");
 						System.exit(1);
 				}
 				
 				establishStreams(parent, s);
 				registerClient(cID);
 		}
 		
 		// Server Side Constructor
 		public NetworkBridge(NetworkManager parent, Socket s){
 				this.s = s;
 				establishStreams(parent, s);
 		}
 		
 		// ---------------------------------------------------------------------------------------------------------- //
 		// ----------------------------------------------PRIVATE METHODS--------------------------------------------- //
 		// ---------------------------------------------------------------------------------------------------------- //
 		
 		void establishStreams(NetworkManager parent, Socket s){
 				ObjectInputStream ois;
 				
 				try{
 						s.setSoTimeout(500);
 						
 						oos = new ObjectOutputStream(s.getOutputStream());			// create output stream
 						ois = new ObjectInputStream(s.getInputStream());				// create input stream - this will be passed to the threaded listener
 						
 						isl = new InputStreamListener(parent, ois, this);
 						isl.start();
 						
 						System.out.println("OOS and OIS initialized. ISL started.");
 				}catch(IOException i){
 						i.printStackTrace();
 						System.exit(0);
 				}
 		}
 		
 		void registerClient(int cID){
 				try{
 						Instruction instr = new Instruction("register-client",cID);
 						oos.writeObject(instr);
 				}catch(IOException ie){
 						ie.printStackTrace();
 				}
 		}
 		
 		// ---------------------------------------------------------------------------------------------------------- //
 		// ----------------------------------------------PUBLIC METHODS---------------------------------------------- //
 		// ---------------------------------------------------------------------------------------------------------- //
 		
 		public void writeData(Object data){
 				try{
 						oos.writeObject(data);
 				}catch(IOException ie){
 						ie.printStackTrace();
 				}
 		}
 		
 		// method to send parts data to server
 		public void sendPartData(Object partData){
 				Instruction instr = new Instruction("UPD");						// UPD == update part data
 				writeData(instr);
 				writeData(partData);
 		}
 		
 		public void sync(){
 				Instruction instr = new Instruction("SYNC");
 				writeData(instr);
 		}
 		
 		public void setBridgeID(int id){
 				this.id = id;
 		}
 		
 		public int getBridgeID(){
 				return id;
 		}
 		
 		public void close(){
 				try{
 						s.close();
 						isl.kill();
 				}catch(IOException ie){
 						ie.printStackTrace();
 				}
 		}
 }
 
 // ------------------------------------- END CLASS NETWORK BRIDGE ---------------------------------//
 
 
 class InputStreamListener extends Thread{
 		private NetworkManager parent;
 		private NetworkBridge nb;
 		private ObjectInputStream ois;
 		private boolean killThread;
 		
 		
 		InputStreamListener(NetworkManager parent, ObjectInputStream ois, NetworkBridge nb){
 				this.parent = parent;
 				this.ois = ois;
 				this.nb = nb;
 				killThread = false;
 		}
 		
 		public void run(){
 				Instruction instr = null;
 				
 				// listen for a request from the client
 				while(true){
 						if(killThread)
 								return;
 						
 						try{
 								instr = (Instruction)ois.readObject();
 										parseInstruction(instr);
 						}catch(IOException i){
 								String message = i.toString();
 								if(message.equals("java.io.EOFException")){
 										System.out.println("Connection lost");
 										parent.closeNetworkBridge(nb.getBridgeID());
 								}
 								else if(message.equals("java.net.SocketTimeoutException: Read timed out")){}
 								else{
										i.printStackTrace();		
 								}
 						}catch(ClassNotFoundException c){
 								c.printStackTrace();
 						}
 				}
 		}
 		
 		public void kill(){
 				killThread = true;
 		}
 		
 		// ----------------------------- PRIVATE METHODS ------------------------------- //
 		
 		void parseInstruction(Instruction instr){
 				String instruction = instr.instruction;
 				int x = instr.x;
 				if(instruction.equals("register-client")){		// client -- > server register client
 						int cID = instr.x;
 						parent.registerClientListener(nb, cID);
 						nb.setBridgeID(cID);											// set the bridge id to the registered client
 				}
 				else if(instruction.equals("UAD")){						// server --> client update animation data
 						readAnimationData(x);
 				}
 				else if(instruction.equals("SAD")){						// server --> client sync animation data
 						syncAnimationData(x);
 				}
 				else if(instruction.equals("SYNC")){					// client --> server request sync animation data
 						parent.syncFrame(nb.getBridgeID());
 				}
 				else if(instruction.equals("UPD")){						// client --> server update part data
 						readPartData();
 				}
 		}
 		
 		void readAnimationData(int expectedPackets){
 				// array lists to store the transfered data from the server
 				ArrayList<TreeMap<Integer, Boolean>> mapArray = new ArrayList<TreeMap<Integer, Boolean>>();
 				ArrayList<TreeMap<Integer, FactoryObject>> dataArray = new ArrayList<TreeMap<Integer, FactoryObject>>();
 				
 				for(int i = 0; i < expectedPackets; i++){
 						try{
 								NetworkTransferObject frameData = (NetworkTransferObject)ois.readObject();
 								mapArray.add(frameData.changeMap);
 								dataArray.add(frameData.changeData);
 						}catch(IOException ie){
 								String message = ie.toString();
 								if(message.equals("java.io.EOFException")){
 										System.out.println("Connection lost");
 										parent.closeNetworkBridge(nb.getBridgeID());
 								}
 								else if(message.equals("java.net.SocketTimeoutException: Read timed out")){}
 								else{
 										ie.printStackTrace();		
 								}
 						}catch(ClassNotFoundException c){
 								c.printStackTrace();
 						}
 				}
 				parent.mergeChanges(mapArray, dataArray);
 		}
 		
 		void syncAnimationData(int expectedPackets){
 				ArrayList<TreeMap<Integer, FactoryObject>> dataArray = new ArrayList<TreeMap<Integer, FactoryObject>>();
 				for(int i = 0; i < expectedPackets; i++){
 						try{
								TreeMap<Integer, FactoryObject> changeData = (TreeMap<Integer, FactoryObject>)ois.readObject();
 								dataArray.add(changeData);
 						}catch(IOException ie){
 								String message = ie.toString();
 								if(message.equals("java.io.EOFException")){
 										System.out.println("Connection lost");
 										parent.closeNetworkBridge(nb.getBridgeID());
 								}
 								else if(message.equals("java.net.SocketTimeoutException: Read timed out")){}
 								else{
 										ie.printStackTrace();		
 								}
 						}catch(ClassNotFoundException c){
 								c.printStackTrace();
 						}
 				}
 				parent.syncChanges(dataArray);
 		}
 		
 		void readPartData(){
 				TreeMap<Integer, Parts> partData = null;
 				try{
 						partData = (TreeMap<Integer, Parts>)ois.readObject();
 				}catch(IOException ie){
 						String message = ie.toString();
 						if(message.equals("java.io.EOFException")){
 								System.out.println("Connection lost");
 								parent.closeNetworkBridge(nb.getBridgeID());
 						}
 						else if(message.equals("java.net.SocketTimeoutException: Read timed out")){}
 						else{
 								ie.printStackTrace();		
 						}
 				}catch(ClassNotFoundException c){
 						c.printStackTrace();
 				}
 				parent.updatePartData(partData);
 		}
 		
 }
