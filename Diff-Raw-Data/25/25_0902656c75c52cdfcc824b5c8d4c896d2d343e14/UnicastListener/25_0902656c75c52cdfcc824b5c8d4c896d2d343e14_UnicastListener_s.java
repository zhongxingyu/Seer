 package pongLobby;
 
 import java.io.IOException;
 import java.lang.Thread;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
import java.net.SocketException;
 import java.net.InetAddress;
 
 
 
 public class UnicastListener extends Thread {
 	UnicastListener(Lobby lobby){
 		_lobby=lobby;
 	}
 	
 	public void run(){
 		_initializeSocket();	
 		
 		while(_lobby.get_state().equals(LobbyState.WAITING))
 		{
 			_readPacket();
 		}
 	}
 	
 	
 	//Méthodes privées
 	//*******************
 	
 	private void _initializeSocket()
 	{
 		
 		try{
 			_socket = new DatagramSocket(_lobby.get_unicastPort());
 		}
 		catch(IOException exception)
 		{
 			System.out.println("Impossible de lancer la socket: " + exception);
 			System.exit(1);
 		}
		try{
		_socket.setSoTimeout(500);
		}
		catch(SocketException ex)
		{
			System.out.println("Impossible de mettre un timeout à la socket: " + ex);
			System.exit(1);
		}
 	}
 	
 	private void _sendString(String msg, InetAddress address){
 		String str=msg;
 		str+="\0";
 		DatagramPacket packet= new DatagramPacket(str.getBytes(), str.length(),
 				address,_lobby.get_unicastPort());
 		try{
 		_socket.send(packet);
 		}
 		catch(IOException exception)
 		{
 			System.out.println("Impossible d'envoyer le paquet dans la socket: "
 					+ exception);
 			System.exit(1);
 		}
 	}
 
 	private void _readPacket(){
 		byte[] data = new byte[200];
 		Player player = null;
 		DatagramPacket packet = new DatagramPacket(data, data.length);
 		try{
 			_socket.receive(packet);
 		}
 		catch(IOException exception)
 		{
 			System.out.println("Impossible de lire le paquet dans la socket: "
 					+ exception);
 		}
 		String str = new String(packet.getData(), 0, packet.getLength());
 		String[] strl= str.split(" ");
 		if(strl[0].equals("ACK"))
 		{
 			long id=0;
 			try{
 				id= Long.parseLong(strl[1]);
 			}
 			catch(NumberFormatException ex)
 			{
 				System.out.println(ex);
 				System.exit(1);
 			}
 			player = _lobby.searchPlayer(id);
 			_sendString("SYNC " + player.get_id(), player.get_ipAddress());
 			player.resetTimer();
 		}
 	}
 	//Attributs privés
 	//****************
 	private Lobby _lobby;
 	private DatagramSocket _socket;
 }
