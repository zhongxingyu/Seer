 package es.deusto.ingenieria.ssdd.chat.client.controller;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observer;
 
 import Thread.MessageProcesorClient;
 
 import es.deusto.ingenieria.ssdd.util.observer.local.LocalObservable;
 import es.deusto.ingenieria.ssdd.chat.data.User;
 
 public class ChatClientController {
 	private String serverIP;
 	private int serverPort;
 	private User connectedUser;
 	private User chatReceiver;
 	private LocalObservable observable;
 	private static final int MESSAGE_MAX_LENGTH=1024;
 	public String incompletedListUsers=null;
 	public String incompletedMessage=null;
	private DatagramSocket udpSocket;
 	public ChatClientController() {
 		this.observable = new LocalObservable();
 		this.serverIP = null;
 		this.serverPort = -1;
 	}
 	
 	public String getConnectedUser() {
 		if (this.connectedUser != null) {
 			return this.connectedUser.getNick();
 		} else {
 			return null;
 		}
 	}
 	
 	public String getChatReceiver() {
 		if (this.chatReceiver != null) {
 			return this.chatReceiver.getNick();
 		} else {
 			return null;
 		}
 	}
 	
 	public String getServerIP() {
 		return this.serverIP;
 	}
 	
 	public int gerServerPort() {
 		return this.serverPort;
 	}
 	
 	public boolean isConnected() {
 		return this.connectedUser != null;
 	}
 	
 	public boolean isChatSessionOpened() {
 		return this.chatReceiver != null;
 	}
 	
 	public void addLocalObserver(Observer observer) {
 		this.observable.addObserver(observer);
 	}
 	
 	public void deleteLocalObserver(Observer observer) {
 		this.observable.deleteObserver(observer);
 	}
 	
 	private int calculateNumberOfMessages (String message){
 		int numberOfMessages=1;
 		String[] messageFields = message.split("&");
 		int bytesToRest = messageFields[0].getBytes().length + messageFields[1].getBytes().length +"&&&".getBytes().length;
 		int messageLength = messageFields[2].getBytes().length;
 		if (messageLength > MESSAGE_MAX_LENGTH - bytesToRest){
 			
 			numberOfMessages=messageLength / (MESSAGE_MAX_LENGTH - bytesToRest);
 			
 			if (messageLength % (MESSAGE_MAX_LENGTH - bytesToRest) != 0)
 			{						
 				numberOfMessages++;
 			}
 		}
 		return numberOfMessages;
 	}
 	
 	private ArrayList <byte []> divideList (ArrayList <User> connectedUsers) throws IOException{
 		ArrayList <byte []> aMessages = new ArrayList<byte []>();
 		aMessages.add(new byte [0]);
 		int cont =0;
 		int arrayPosition=0;
 		String nick;
 		for (int i=0; i< connectedUsers.size(); i++){
 			nick= connectedUsers.get(i).getNick().concat("&");
 			cont += nick.getBytes().length;
 			
 			if(cont> MESSAGE_MAX_LENGTH){
 				arrayPosition++;
 				aMessages.add(nick.getBytes());
 				
 				cont=connectedUsers.get(i).toString().getBytes().length;
 			}
 			else
 			{
 				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
 				outputStream.write( aMessages.get(arrayPosition) );
 				outputStream.write( nick.getBytes() );
 								
 				aMessages.set(arrayPosition, outputStream.toByteArray( ));
 			}
 						
 		}
 			
 		return aMessages;
 	}
 	
 	
 	private ArrayList<byte[]> divideMessage (String completeMessage){
 		ArrayList<byte[]> aMessages = new ArrayList<byte[]>();
 		int numberOfMessages = calculateNumberOfMessages(completeMessage);
 		int a= completeMessage.length()/numberOfMessages;
 		String message;
 		for (int i=0; i<numberOfMessages;i++){
 			message= completeMessage.substring(a*i, a*(i+1));
 			if(i!=numberOfMessages){
 				message.concat("&");
 			}
 			aMessages.add(message.getBytes());
 		}
 		return aMessages;		
 	}
 	
 	//Mtodo que enva un paquete
 	public void sendDatagramPacket(String message){
		try  {
 			InetAddress serverHost = InetAddress.getByName(this.serverIP);	
 			byte[] byteMsg = message.getBytes();
 			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, this.serverPort);
 			udpSocket.send(request);
 		} catch (SocketException e) {
 			System.err.println("# UDPClient Socket error: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("# UDPClient IO error: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * This method is used to receive a datagramPacket
 	 */
 	public DatagramPacket receiveDatagramPacket(){
		try  {
 			
 			byte[] buffer = new byte[1024];
 			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			System.out.println("entra en receive");
			udpSocket.receive(reply);	
			System.out.println("recibidooo");
 			
 			return reply;
 		} catch (SocketException e) {
 			System.err.println("# UDPClient Socket error: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("# UDPClient IO error: " + e.getMessage());
 		}
 		return null;
 	}
 	
 public boolean connect(String ip, int port, String nick) {
 		
 		String message = "101&"+nick;
 		String returnMessage ="";
 		//ENTER YOUR CODE TO CONNECT
 		this.serverIP = ip;
 		this.serverPort = port;
		try {
			this.udpSocket= new DatagramSocket();
			sendDatagramPacket(message);
			System.out.println("pakete enviado: "+message);
			DatagramPacket receivedPacket= receiveDatagramPacket();
			returnMessage=receivedPacket.getData().toString();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		System.out.println("pakete recibido"+returnMessage);
 		if (returnMessage.split("&")[0].equals("201")){
 			this.connectedUser = new User();
 			this.connectedUser.setNick(nick);
			incompletedListUsers = returnMessage.substring(4);
 			MessageProcesorClient messageProcesor = new MessageProcesorClient();
 			messageProcesor.start();
 			return true;
 		}
 		else{
 			return false;
 		}
 		
 		
 	}
 	
 	public boolean disconnect() {
 		
 		String message;
 		//ENTER YOUR CODE TO DISCONNECT
 		if (isChatSessionOpened()){
 			sendChatClosure();
 			 
 		}
 		
 		message= "106";
 		sendDatagramPacket(message);
 		
 		this.connectedUser = null;
 		this.chatReceiver = null;
 		
 		return true;
 	}
 	
 	public ArrayList<String> getConnectedUsers() {
 		ArrayList<String> connectedUsers = new ArrayList<>();
 		String message="107";
 		sendDatagramPacket(message);
 		
 		//connectedUsers.add("Default");
 		
 		return connectedUsers;
 	}
 	
 	public boolean sendMessage(String message) {
 		
 		//ENTER YOUR CODE TO SEND A MESSAGE
 //		ArrayList<byte[]> messagesToSend = divideMessage(message);
 //		String singleMessage;
 //		for (int i=0; i< messagesToSend.size(); i++){
 //			singleMessage= "108&"+this.chatReceiver.getNick()+"&"+messagesToSend.get(i)
 //		}
 //		String messageToSend= "108&"+this.chatReceiver.getNick()+"&"+message;
 		
 		return true;
 	}
 	
 	public void receiveMessage() {
 		
 		//ENTER YOUR CODE TO RECEIVE A MESSAGE
 		
 		String message = "Received message";		
 		
 		//Notify the received message to the GUI
 		this.observable.notifyObservers(message);
 	}	
 	
 	public boolean sendChatRequest(String to) {
 		
 		String message= "102&"+to;
 		sendDatagramPacket(message);
 		this.chatReceiver = new User();
 		this.chatReceiver.setNick(to);
 		
 		return true;
 	}	
 	
 	public void receiveChatRequest() {
 		
 		//ENTER YOUR CODE TO RECEIVE A CHAT REQUEST
 		
 		String message = "Chat request details";
 		
 		//Notify the chat request details to the GUI
 		this.observable.notifyObservers(message);
 	}
 	
 	public boolean acceptChatRequest() {
 		
 		//ENTER YOUR CODE TO ACCEPT A CHAT REQUEST
 		String message= "103&"+this.connectedUser.getNick();
 		sendDatagramPacket(message);
 		return true;
 	}
 	
 	public boolean refuseChatRequest() {
 		
 		//ENTER YOUR CODE TO REFUSE A CHAT REQUEST
 		String message= "104&"+this.connectedUser.getNick();
 		sendDatagramPacket(message);
 		return true;
 	}	
 	
 	public boolean sendChatClosure() {
 		
 		//ENTER YOUR CODE TO SEND A CHAT CLOSURE
 		String message="105&"+this.chatReceiver.getNick();
 		sendDatagramPacket(message);
 		this.chatReceiver = null;
 		
 		return true;
 	}
 	
 	public void receiveChatClosure() {
 		
 		//ENTER YOUR CODE TO RECEIVE A CHAT REQUEST
 		
 		String message = "Chat request details";
 		
 		//Notify the chat request details to the GUI
 		this.observable.notifyObservers(message);
 	}
 }
