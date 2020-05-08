 package stkl.spectropolarisclient;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 
 public class Client extends Thread {
 	private final int SERVER_PORT = 1337;
 	private final int TIMEOUT = 10000;
 	
 	private static Client instance;
 	private Socket skt;
 	private boolean connected;
 	private OutputStream out;
 	private BufferedInputStream in;
 	
 	public Client(String ip, String name, int color) throws UnknownHostException, IOException {
 		skt = new Socket();
 		skt.setTcpNoDelay(true);
 		skt.connect(new InetSocketAddress(ip, SERVER_PORT), TIMEOUT);
 		skt.setSoTimeout(TIMEOUT);
 		connected = true;
 		instance = this;
 		
 		out = skt.getOutputStream();
 		in = new BufferedInputStream(skt.getInputStream());
 		
 		sentInit(name, color);
 	}
 	
 	public void sentInit(String name, int color) {
 		byte[] bName = name.getBytes();
 		
 		ByteBuffer buffer = ByteBuffer.allocate(12 + bName.length);
 		buffer.putInt(Message.NAMECOLOR.value());		
 		buffer.putInt(bName.length);
 		buffer.put(bName);
 		buffer.putInt(color);
 		
 		try {
 			out.write(buffer.array());
 		} catch (IOException e) {
 			System.err.println("Error occured while sending player info");
 			e.printStackTrace();
 			close();
 		}
 	}
 	
 	public static Client getInstance() {
 		return instance;
 	}
 	
 	public void sent(float x, float y, float direction, float speed, int health) {
 		if(!connected)
 			return;
 		
 		try {
 			ByteBuffer buffer = ByteBuffer.allocate(24);
 			
 			buffer.putInt(Message.PLAYER.value());
 			buffer.putFloat(x);
 			buffer.putFloat(y);
 			buffer.putFloat(direction);
 			buffer.putFloat(speed);
 			buffer.putInt(health);
 			
 			out.write(buffer.array());
 		} catch (IOException e) {
 			System.out.println("Connection to server lost");
 			e.printStackTrace();
 			GameActivity.getInstance().finish();
 			close();
 		}
 	}
 	
 	private void receiveCharacters(ByteBuffer intByteBuffer) throws Exception {
 		
 		// Read amount of characters from stream
		in.read(intByteBuffer.array(), 0, 4);
 		int numOfCharacters = intByteBuffer.getInt();
 		intByteBuffer.clear();
 		
 		// Read amount of bullets from stream
 		in.read(intByteBuffer.array(), 0, 4);
  		int numOfBullets = intByteBuffer.getInt();
  		intByteBuffer.clear();
 		
 		// Read amount of pickups from stream
 		int numOfBytes = in.read(intByteBuffer.array(), 0, 4);
  		int numOfPickups = intByteBuffer.getInt();
  		intByteBuffer.clear();
 		
 		//int numOfBytes = 0;
 		// characters
 		//numOfBytes += numOfCharacters * GameCharacter.sendSize();
 		// bullets
 		//numOfBytes += numOfBullets * Bullet.sendSize();
 		// pickups
 		//numOfBytes += numOfPickups * HealthPickup.sendSize();
  		
 		// Prepare byte buffer
 		//ByteBuffer buffer = ByteBuffer.allocate(numOfBytes);	 
 		
 		//numOfBytes = in.read(buffer.array());
 	    
 	    if(numOfBytes < 0)
 	        throw new Exception(new String("Connection to server lost"));
 	    
 	    //if(numOfBytes < buffer.capacity()) {
 	    //    System.err.println("Received only " + numOfBytes + " bytes in receiveCharacters");
 	    //    return;
 	    //}
 	    
 	    GameActivity.getInstance().model().receive(in, numOfCharacters, numOfBullets, numOfPickups);
 	}
 	
 	private void receiveId(ByteBuffer intByteBuffer) throws Exception {
 		
 		// Read next 4 bytes (int, id) from stream
		int numOfBytes = in.read(intByteBuffer.array(), 0, 4);
 		int id = intByteBuffer.getInt();
 		intByteBuffer.clear();
 		
 	    if(numOfBytes < 0)
 	        throw new Exception("Connection to server lost");
 	    
 	    if(numOfBytes < 4) {
 	        System.err.println("Received only " + numOfBytes + " bytes in receiveId");
 	        return;
 	    }
 	    
 	    GameActivity.getInstance().model().player().setId(id);
 	    
 	    System.out.println("id: " + GameActivity.getInstance().model().player().id());
 	}
 	
 	@Override
 	public void run() {
 		
 		byte[] bytes = new byte[4];
 		ByteBuffer intByteBuffer = ByteBuffer.wrap(bytes);
 		
 		while(connected) {
 			if(GameActivity.getInstance() == null)
 				continue;
 			
 			try {
 				// Read first 4 bytes (int, messageType) from stream
				in.read(bytes, 0, 4);
 				int messageType = intByteBuffer.getInt();
 				intByteBuffer.clear(); // Clear buffer so we can read from it again
 				
 				// Receive appropriate message based on received messageType
 			    if(messageType == Message.CHARACTERS.value()) {
 					receiveCharacters(intByteBuffer);
 				} else if(messageType == Message.ID.value()) {
 					receiveId(intByteBuffer);
 				} else {
 					System.err.println("Received message with unknown id: " + messageType);
 				}
 			    
 			} catch (Exception e) {
 				System.out.println("Connection to server lost");
 				e.printStackTrace();
 				close();
 			}
 			
 		}
 	}
 	
 	public void close() {
 		connected = false;
 		try {
 			skt.close();
 		} catch (IOException e) {
 			System.err.println("Socket already closed");
 		}
 
 		if(GameActivity.getInstance() != null){
 			GameActivity.getInstance().finish();
 		}
 
 	}
 }
