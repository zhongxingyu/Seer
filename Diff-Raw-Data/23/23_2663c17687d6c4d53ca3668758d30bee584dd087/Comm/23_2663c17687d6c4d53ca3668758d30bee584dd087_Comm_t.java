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
 	
 	private Socket clientToServer;
 	private Socket meToOther;
 
 	private OutputStream ServerOS;
 	private InputStream ServerIS;
 	private ObjectOutputStream ServerOOS;
 	private ObjectInputStream ServerOIS;
 	
 	private OutputStream ClientOS;
 	private InputStream ClientIS;
 	private ObjectOutputStream ClientOOS;
 	private ObjectInputStream ClientOIS;
 	
 	public Comm( String username ) throws NoInternetException{
 		try {
 			SERVER = InetAddress.getByName("192.168.1.107");
 			startServerSocket();
 			ServerPacket signingOn = new ServerPacket( Client_Driver.getCurrentUser().getUsername(), Status.online );
 			sendServerPacket( signingOn );
 			//stopServerSocket();
 		} catch (UnknownHostException e) {
 			throw new NoInternetException("Cannot find server");
 		}
 	}
 	
 	private void startServerSocket() throws NoInternetException{
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
 	
 	private void stopServerSocket() throws NoInternetException{
 		try {
 			clientToServer.close();
 			ServerOS.close();
 			ServerIS.close();
 			ServerOOS.close();
 			ServerOIS.close();
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't close socket" );
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
 			}
 			
 			sendIMPacket( toSend );
 			Client_Driver.getCurrentUser().addSentMessage( 
 					toSend.getDestUsername(), toSend.getData() );
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void run(){
 		while( true ){
 			try {
				if( ClientIS.available() > 0 ){
					IMPacket received = receiveIMPacket();
 					Client_Driver.getCurrentUser().addReceivedMessage( 
 							received.getSrcUsername(), received.getData() );
 				}
 				if( ServerIS.available() > 0 ){
					ServerPacket received = receiveServerPacket();
 					Client_Driver.updateBuddyStatus( received.getUsername(), received.getStatus() );	
 				}
 			} catch (IOException e) {
 				System.err.println( e.getMessage() );
 			}
 		}
 	}
 	
 	public void sendServerPacket( ServerPacket sp ) throws NoInternetException{
 		try {
 			if( ServerOOS == null ){
				ServerOOS = new ObjectOutputStream( ServerOS );
 			}
 			ServerOOS.reset();
 			ServerOOS.writeObject( sp );
 
 		} catch (IOException e) {
 			throw new NoInternetException( "Can't send server packet" );
 		}
 	}
 	
	private ServerPacket receiveServerPacket() throws NoInternetException{
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
 	
	private IMPacket receiveIMPacket() throws NoInternetException{
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
