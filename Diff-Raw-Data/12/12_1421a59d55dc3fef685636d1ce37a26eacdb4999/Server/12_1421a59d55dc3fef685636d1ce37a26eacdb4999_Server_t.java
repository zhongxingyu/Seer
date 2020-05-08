package server;
 
 import headers.classes.HandShakeHeader;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import connection.Connection;
 
 import transfer.classes.HandShakeMessage;
 
 public class Server  {
 
 	public static final int DEFAULT_PORT = 21000;
 	
 	private int listenPort;
 	private ServerSocket svSock;
 	private ArrayList<Connection> cList;
 	
 	public Server(int listenPort) throws IOException
 	{
 		this.listenPort = listenPort;
 		this.svSock = new ServerSocket(this.listenPort);
 		this.cList = new ArrayList<Connection>();
 		
 	}
 	
 	public Server() throws IOException
 	{
 		this(DEFAULT_PORT);
 	}
 //-----------------------------------------------------------------------------------------------------------------------
 	public void start() {
 		while(true)
 		{
 			Socket sock=null;
 			
 			try {
 				 sock = svSock.accept();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				continue;
 			}
 			
 			ObjectInputStream ois=null;
 			ObjectOutputStream oos = null;
 			System.out.println("trying to get streams");
 			try {
 				oos = new ObjectOutputStream(sock.getOutputStream());
 				ois = new ObjectInputStream(sock.getInputStream());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			System.out.println("trying to safe connection");
 			Connection c = new Connection(sock,oos,ois);
 			c.setConHandler(new ServerConnectionHandler(c));
 			if(doHandShake(c))
 			{
 				addNewConnection(c);
 				System.out.println("HANDSHAKE SUCCESS");
 			}
 			else
 			{
 				System.out.println("HANDSHAKE FAILED");
				try {
					sock.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 				continue;
 			}
 			
 			new Thread(c).start();
 			
 		}
 	}
 //-----------------------------------------------------------------------------------------------------------------------------
 	private void addNewConnection(Connection c)
 	{
 		cList.add(c);
 	}
 	
 	private boolean doHandShake(Connection c)
 	{
		// Nur ein Beispiel, ginge natÃ¼rlich viel effizienter ohne die ganzen Objekte, aber OOP wills halt so extrem
 		HandShakeMessage h = null;
 		try {
 			System.out.println("try sending handshake");
 
 			c.send(new HandShakeMessage());
 			System.out.println("handshake sent, try receiving handshake");
 
 			h = (HandShakeMessage) c.receive();
 			System.out.println("handshake received");
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		
 		if( 
 			(h != null) 
 			&& 
 			(h.getHeader().getId() == HandShakeHeader.ID) 
 		)
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 //##################################################################
 	public static void main(String args[])
 	{
 		
 		try {
 			Server sv = new Server();
 			sv.start();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 }
