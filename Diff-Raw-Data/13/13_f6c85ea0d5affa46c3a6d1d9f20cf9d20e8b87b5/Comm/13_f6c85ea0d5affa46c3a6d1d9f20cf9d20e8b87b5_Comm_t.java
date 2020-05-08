 package client;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.*;
 
 import common.Status;
 import common.ServerPacket;
 
 public class Comm extends Thread{
 
 	public static final int SERVER_PORT = 8010;
 	public static final int COMM_PORT = 8011;
 	
 	public static InetAddress SERVER;
 	
 	private static Socket clientToServer;
 
 	private static OutputStream ServerOS;
 	private static InputStream ServerIS;
 	private static ObjectOutputStream ServerOOS;
 	private static ObjectInputStream ServerOIS;
 	
 	public static void initComm() throws NoInternetException{
 		try {
 			SERVER = InetAddress.getByName("192.168.1.105");
 			startServerSocket();
 			ServerPacket signingOn = new ServerPacket( Client_Driver.getCurrentUser().getUsername(), Status.online );
 			sendServerPacket( signingOn );
 			//stopServerSocket();
 		} catch (UnknownHostException e) {
 			throw new NoInternetException("Cannot find server");
 		}
 	}
 	
 	private static void startServerSocket() throws NoInternetException{
 		try{
 			clientToServer = new Socket( SERVER, SERVER_PORT );
 			clientToServer.setKeepAlive( true );
 			ServerOS = clientToServer.getOutputStream();
 			ServerIS = clientToServer.getInputStream();
 		} catch (UnknownHostException e) {
 			throw new NoInternetException("Cannot find server");
 		} catch (IOException io ){
 			io.printStackTrace();
 		}
 	}
 	
 	public static void stopServerSocket() throws NoInternetException{
 		try {
 			ServerPacket signingOff = new ServerPacket( Client_Driver.getCurrentUser().getUsername(), Status.offline );
 			sendServerPacket( signingOff );
 			
 			if( clientToServer != null && !clientToServer.isClosed() ){
 				clientToServer.close();
 				if( ServerOS != null ){
 					ServerOS.close();
 					if( ServerOOS != null ){
 						ServerOOS.close();					
 					}
 				}
 				if( ServerIS != null ){
 					ServerIS.close();
 					if( ServerOIS != null ){
 						ServerOIS.close();					
 					}
 				}				
 			}
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't close socket" );
 		}
 		
 	}
 	
 	public static void sendServerPacket( ServerPacket sp ) throws NoInternetException{
 		try {
 			if( ServerOOS == null ){
 				ServerOOS = new ObjectOutputStream( ServerOS );
 			}
 			ServerOOS.writeObject( sp );
 
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't send server packet" );
 		}
 	}
 	
 	private static ServerPacket receiveServerPacket() throws NoInternetException{
 		ServerPacket sp = null;
 		try{
 			if( ServerOIS == null ){
 				ServerOIS = new ObjectInputStream( ServerIS );				
 			}
 			sp = (ServerPacket) ServerOIS.readObject();
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't receive server packet" );
 		} catch (ClassNotFoundException e) {
 			throw new NoInternetException( "Can't receive server packet" );
 		}
 		return sp;
 	}
 
 
 	private Socket meToOther;
 	
 	private OutputStream ClientOS;
 	private InputStream ClientIS;
 	private ObjectOutputStream ClientOOS;
 	private ObjectInputStream ClientOIS;
 	
 	private boolean started;
 	
 	public Comm() throws NoInternetException{
 		started = false;
 		
 	}
 	
 	public Comm( Socket received ) throws NoInternetException{
 		this();
 		meToOther = received;
 		startClientStreams();
 	}
 	
 	private void startClientStreams(){
 		try{
 			ClientOS = meToOther.getOutputStream();
 			ClientIS = meToOther.getInputStream();
 		} catch (IOException io ){
 			io.printStackTrace();
 		}
 	}
 
 	public void sendMessage( IMPacket toSend ) throws NoInternetException{
 		try {
 			if( meToOther == null ){
 			
 				String curBuddy = toSend.getDestUsername();
 				ServerPacket senderIP = new ServerPacket( curBuddy );
 				sendServerPacket( senderIP );
 				senderIP = receiveServerPacket();
 				InetAddress dest = senderIP.getIP();
 				
 				meToOther = new Socket( dest, COMM_PORT );
 				startClientStreams();
 			}
 			
 			sendIMPacket( toSend );
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean hasStarted(){
 		return started;
 	}
 	
 	public void run(){
 		started = true;
 		
 		while( true ){
 			try {
				IMPacket received = receiveIMPacket();
				Client_Driver.getCurrentUser().addReceivedMessage( 
						received.getSrcUsername(), received.getData() );
				/*if( ServerIS.available() > 0 ){
 					ServerPacket received = receiveServerPacket();
 					Client_Driver.updateBuddyStatus( received.getUsername(), received.getStatus() );	
				}*/
 			} catch (IOException e) {
 				System.err.println( e.getMessage() );
 			}
 		}
 	}
 	
 	
 	
 	private void sendIMPacket( IMPacket i ) throws NoInternetException{
 		try {
 			if( ClientOOS == null ){
 				ClientOOS = new ObjectOutputStream( ClientOS );
 			}
 			ClientOOS.writeObject( i );
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't send IM" );
 		}
 	}
 	
 	public IMPacket receiveIMPacket() throws NoInternetException{
 		IMPacket im = null;
 		try{
 			if( ClientOIS == null ){
 				ClientOIS = new ObjectInputStream( ClientIS );
 			}
 			im = (IMPacket) ClientOIS.readObject();
 			
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't receive IM packet" );
 		} catch (ClassNotFoundException e) {
 			throw new NoInternetException( "Can't receive IM packet" );
 		}
 		return im;
 	}
 
 }
