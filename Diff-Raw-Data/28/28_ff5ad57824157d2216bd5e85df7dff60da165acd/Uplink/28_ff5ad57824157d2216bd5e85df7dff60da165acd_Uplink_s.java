 package ch.nksa.pu.robotics.libs;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import ch.nksa.pu.robotics.libs.IncomingRequest;
 
 import lejos.nxt.LCD;
 import lejos.nxt.LCDOutputStream;
 import lejos.nxt.comm.BTConnection;
 import lejos.nxt.comm.Bluetooth;
 
 public class Uplink {
 	protected BTConnection uplink;
 	protected DataInputStream dis;
 	protected DataOutputStream dos;
 	protected Thread receivingThread;
 	protected Thread sendingThread;
 	protected static Uplink instance;
 	protected ArrayList<IncomingRequestHelper> listeners = new ArrayList<IncomingRequestHelper>();
 	protected ArrayList<byte[][]> rawIncoming = new ArrayList<byte[][]>();
 	protected ArrayList<IncomingRequest> incomingRequests = new ArrayList<IncomingRequest>();
 	protected ArrayList<OutgoingRequest> outgoingRequests = new ArrayList<OutgoingRequest>();
 	protected int requestsSent = 0;
 	/**
 	 * Holds a 2d byte array with the latest incoming request
 	 */
 	protected RequestStruct requestStruct;
 	
 	public Uplink(boolean connect_now){
 		if(connect_now){
 			connect(10 * 1000);
 		}
 	}
 	
 	//Singleton Patterns
 	public static Uplink getInstance(){
 		return getInstance(false);
 	}
 	
 	public static Uplink getInstance(boolean connect_now){
 		if(instance == null){
 			return new Uplink(connect_now);
 		}
 		return instance;
 	}
 	//end Singleton
 	
 	public boolean connect(int timeout){
 		System.out.println("Waiting for connection...");
 		uplink = Bluetooth.waitForConnection();
 		dis = uplink.openDataInputStream();
 		dos = uplink.openDataOutputStream();
 		System.out.println("Connection established.");
 		sendingThread = new Thread(){
 			public void run(){
 				sendRequests();
 			}
 		};
 		sendingThread.start();
 		
 		
 		receivingThread = new Thread(){
 			public void run(){
 				getRequests();
 			}
 		};
 		receivingThread.start();
 		return false;
 	}
 	
 	public void registerRequest(OutgoingRequest req){
 		req.id = outgoingRequests.size();
 		outgoingRequests.add(req);
 	}
 	
 	public void registerListener(IncomingRequestHelper l){
 		this.listeners.add(l);
 	}
 	
 	public OutgoingRequest getOutgoingRequest(int id){
 		if(outgoingRequests.size() > id){
 			return outgoingRequests.get(id);
 		}
 		return null;
 	}
 	
 	public IncomingRequest getIncomingRequest(int id){
 		if(incomingRequests.size() > id){
 			return incomingRequests.get(id);
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * must NOT be invoked manually!
 	 */
 	protected void sendRequests(){
 		while(true){
 			if(outgoingRequests.size() > requestsSent){
 				OutgoingRequest req = outgoingRequests.get(requestsSent);
 				try {
 					/**
 					 * Request Protocol Format
 					 * 
 					 *id
 					 * RequestMode
 					 * ReferenceId
 					 * SenderLength
 					 * Sender
 					 * NickLength
    					 * Nick
 					 * SubjectLength
 					 * Subject
 					 */
 					byte[][] header;
 					header = req.getHeader();
 					byte[][] data;
 					data = req.getData();
 					dos.writeInt(header.length + data.length);
 					for(byte[] b: header){
 						dos.writeInt(b.length);
 						dos.write(b);
 					}
 					
 					for(byte[] b: data){
 						dos.writeInt(b.length);
 						dos.write(b);
 					}
 					dos.flush();
 					req.hasBeenSent = true;
 					requestsSent ++;
 				} catch (IOException e) {}
 				
 			}
 			else{
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {}
 			}
 		}
 	}
 	
 	
 	
 	/**
 	 * must NOT be invoked manually!
 	 */
 	protected void getRequests(){
 		System.out.println("Listening...");
 		Thread waiting;
 		boolean did_break = false;
 		while(true){
 			try {
				int length = 0;
 				did_break = false;
				
 				int lines = dis.readInt();
 				byte[][] incoming = new byte[lines][];
 				for(byte[] b: incoming){
 					length = dis.readInt();
 					b = new byte[length];
 					dis.readFully(b, 0, length);
 				}
 				
 				if(!IncomingRequest.headerIsValid(incoming)){
 					//Add bogus object in order to keep index and id in sync
 					incomingRequests.add(null);
 					System.out.println(">Unexpected behaviour may follow!");
 					continue;
 				}
 				this.requestStruct = new RequestStruct(incoming);
 				
 				for(final IncomingRequestHelper l: listeners){
 					l.last = false;
 					waiting = l.makeActive();
 					try {
 						waiting.join();
 					} catch (InterruptedException e) {}
 					if(l.last){
 						did_break = true;
 						break;
 					}
 					if(!did_break){
 						BasicIncomingRequest req =  BasicIncomingRequest.validate(incoming);
 						incomingRequests.add(req);
 					}
 				}
 			} catch (IOException e) {
 				LCD.drawString("Error!", 0, 5);
 				break;
 			}
 			
 		}
 	}
 }
