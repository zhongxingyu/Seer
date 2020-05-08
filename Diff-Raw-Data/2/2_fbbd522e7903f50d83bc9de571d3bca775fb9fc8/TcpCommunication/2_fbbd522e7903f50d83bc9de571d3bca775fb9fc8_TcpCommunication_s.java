 package se.miun.mediasense.disseminationlayer.communication.tcp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import se.miun.mediasense.addinlayer.AddInManager;
 import se.miun.mediasense.disseminationlayer.communication.CommunicationInterface;
 import se.miun.mediasense.disseminationlayer.communication.DestinationNotReachableException;
 import se.miun.mediasense.disseminationlayer.communication.GetMessage;
 import se.miun.mediasense.disseminationlayer.communication.Message;
 import se.miun.mediasense.disseminationlayer.communication.NotifyMessage;
 import se.miun.mediasense.disseminationlayer.communication.SetMessage;
 import se.miun.mediasense.disseminationlayer.communication.serializer.BinaryMessageSerializer;
 import se.miun.mediasense.disseminationlayer.disseminationcore.DisseminationCore;
 
 public class TcpCommunication implements Runnable, CommunicationInterface{
 
 	private DisseminationCore disseminationCore = null;
 	
 	private BinaryMessageSerializer messageSerializer = new BinaryMessageSerializer();
 	
 	private ServerSocket ss;
 	private int communicationPort = 9009;
 	
 	
 	
 	private boolean runCommunication = true;
 	
 	public TcpCommunication(DisseminationCore disseminationCore) {		
 		try {
 			this.disseminationCore = disseminationCore;
 
 			this.ss = new ServerSocket(communicationPort);
 			
 			//Start the Listener!
 			Thread t = new Thread(this);
 			t.start();
 			
 		} catch (Exception e){
 			e.printStackTrace();			
 		}		
 	}
 	
 	@Override
 	public void shutdown() {
 		try {
 			runCommunication = false;
 			ss.close();					
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 	
 
 	@Override
 	public void sendMessage(Message message) throws DestinationNotReachableException {
 		try {
 			Socket s = new Socket(message.getToIp(), communicationPort);
 			
 			byte[] data = messageSerializer.serializeMessage(message);
 	
 			OutputStream os = s.getOutputStream();
 			os.write(data);
 
 			os.flush();
 			os.close();
 			s.close();
 			
 		} catch (IOException e) {
 			throw new DestinationNotReachableException(e.getMessage());
 		}
 	}
 
 	@Override
 	public String getLocalIp() {
 		try {			
 						
 			InetAddress address = InetAddress.getLocalHost();			
 			if(!address.isLoopbackAddress() && !address.isLinkLocalAddress()){
 				return address.getHostAddress();
 			}
 			else {				
 				//Workaround because Linux is stupid...	
 				Socket s = new Socket("www.google.com", 80);
 				String ip = s.getLocalAddress().getHostAddress();
 				s.close();
 				return ip;
 			}
 			
 			
 
 			
 			
 			/*
 	    	Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
 	    	
 	    	while (ni.hasMoreElements()) {
 	    		NetworkInterface networkInterface = ni.nextElement();
 	    			    		
 	    		Enumeration<InetAddress> ias = networkInterface.getInetAddresses();
 		    	while (ias.hasMoreElements()) {
 		    		 InetAddress address = ias.nextElement();
 		    		 
 		    		 if(!address.isLoopbackAddress() && !address.isLinkLocalAddress()){
 		        		return address.getHostAddress();
 		        	}		    		 
 		    	}	    		
 	    	}  
 		*/	
 		} catch (Exception e1) {
 			return "127.0.0.1";
 		}
 		/*
 			try{
 		    	//In windows it is this simple...
 				return 
 			} catch (Exception e2) {
 			}
 		}
 		*/
 		
 	}
 
 	@Override
 	public void run() {
 		while (runCommunication) {
             try {
                  final Socket s = ss.accept();
                                          
                  Thread t = new Thread(new Runnable() {
 					
 					@Override
 					public void run() {
 						handleConnection(s);								
 					}
 					
 				});
 				t.start();
 				
             } catch (IOException e) {
                 //throw new DestinationNotReachableException(e.getMessage());
             }
         }				
 		
 		
 		
 	}
 	
 	
 	//This should be done better... With some managers and stuff. Victor will fix it...
 	private void handleConnection(Socket s) {
 		try {
 
 			byte[] buffer = new byte[1048576];
 
 			InputStream is = s.getInputStream();
 			is.read(buffer);
 
 			//String stringRepresentation = new String(buffer);
 
			Message message = messageSerializer.deserializeMessage(buffer,s.getRemoteSocketAddress().toString(),getLocalIp());
 
 			switch (message.getType()) {
 
 				
 			case Message.GET:				
 				//Fire off the getEvent!
 				GetMessage getMessage = (GetMessage) message;
 				disseminationCore.callGetEventListener(getMessage.getFromIp(), getMessage.uci);				
 				break;
 										
 			case Message.SET:				
 				//Fire off the SetEvent!
 				SetMessage setMessage = (SetMessage) message;
 				disseminationCore.callSetEventListener(setMessage.uci, setMessage.value);				
 				break;
 
 
 			case Message.NOTIFY:
 				//Fire off the getResponseEvent!
 				NotifyMessage notifyMessage = (NotifyMessage) message;
 				disseminationCore.callGetResponseListener(notifyMessage.uci, notifyMessage.value);
 				break;
 				
 				
 			default:
 
 				//This forwards any unknown messages to the lookupService
 				disseminationCore.getLookupServiceInterface().handleMessage(message);		
 
 				//This forwards any unknown messages to the AddInManager and the addIns
 				AddInManager addInManager = disseminationCore.getMediaSensePlatform().getAddInManager();
 				addInManager.forwardMessageToAddIns(message);
 				break;
 			
 			}
 
 			is.close();
 			s.close();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 
 	
 }
