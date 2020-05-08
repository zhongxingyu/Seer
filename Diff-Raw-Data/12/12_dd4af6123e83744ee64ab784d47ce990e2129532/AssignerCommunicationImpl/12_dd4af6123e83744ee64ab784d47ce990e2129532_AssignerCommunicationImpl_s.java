 package ro.iasi.communication.impl;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import ro.iasi.communication.api.AssignerCommunication;
 import ro.iasi.communication.api.LinksCallback;
 import ro.iasi.communication.api.LinksDTO;
 import ro.iasi.communication.enums.Operation;
 
 public class AssignerCommunicationImpl implements AssignerCommunication {
 	
 	public static final int PORT = 6668;
 	
 	private ServerSocket serverSocket;
 	private LinksCallback listener;
 	
 	public AssignerCommunicationImpl() throws IOException {
 		this.serverSocket = new ServerSocket();
 	}
 
 	@Override
 	public void startListening() throws UnknownHostException, IOException, ClassNotFoundException {
 		while (true) {
			serverSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), AssignerCommunicationImpl.PORT));
 			Socket socket = serverSocket.accept();
 			
 			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
 			Operation operation = (Operation) objectInputStream.readObject();
 			
 			if (operation == Operation.SEND) {
 				listener.sendLinks((LinksDTO) objectInputStream.readObject());
 			} else if (operation == Operation.GET) {
 				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
 				objectOutputStream.writeObject(listener.getLinks());
 				objectOutputStream.close();
 			}
 			
 			objectInputStream.close();
 			socket.close();
 		}
 	}
 
 	@Override
 	public void registerListener(LinksCallback linksCallback) {
 		listener = linksCallback;
 	}
 
 }
