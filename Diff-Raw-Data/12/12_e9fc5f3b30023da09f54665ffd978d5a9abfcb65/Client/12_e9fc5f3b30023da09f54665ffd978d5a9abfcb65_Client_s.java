 package com.objet.chat_rmi;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  * A client
  */
 public class Client
 {
     /**
      * Entry point
      * 
      * @param args (0: server host, 1: server port) all optional
      * @throws UnknownHostException
      * @throws MalformedURLException
      * @throws RemoteException
      * @throws NotBoundException
      */
     public static void main(String[] args) throws UnknownHostException,
             MalformedURLException, RemoteException, NotBoundException
     {
         boolean stop = false;
         String host;
         int port;
 
         try
         {
             host = args[0];
         }
         catch (Exception e)
         {
             host = InetAddress.getLocalHost().getHostName();
         }
 
         try
         {
             port = Integer.parseInt(args[1]);
         }
         catch (Exception e)
         {
             port = 4000;
         }
 
         String URL = "//" + host + ":" + port + "/server_chat_rmi";
         IChat server = (IChat)Naming.lookup(URL);
         Client client = new Client(server);
 
         Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine() && !stop)
         {
             String line = sc.nextLine();
 
             if (line.equals("connect"))
             {
                 System.out.print("Enter the pseudonym please: ");
                 client.connect(sc.nextLine());
 
                 client.enableUpdate();
             }
             else if (line.equals("send"))
             {
                 client.disableUpdate();
 
                 System.out.print("Enter the message please: ");
                 client.send(sc.nextLine());
 
                 client.enableUpdate();
             }
             else if (line.equals("who"))
             {
                 client.disableUpdate();
 
                 System.out.println("Users list:");
                 ArrayList<User> listUsers = client.who();
                 for (User user : listUsers)
                 {
                     System.out.println(user.getPseudo());
                 }
 
                 client.enableUpdate();
             }
             else if (line.equals("bye"))
             {
                 client.disableUpdate();
                 client.bye();
             }
             else if (line.equals("quit"))
             {
                 if (client.isConnected())
                 {
                     client.disableUpdate();
                     client.bye();
                 }
                 stop = true;
             }
         }
     }
 
     /**
      * Identifier of the client
      */
     private int m_id;
 
     /**
      * Identifier of the last received message
      */
     private int m_lastMsgId;
 
     /**
      * THE server
      */
     private IChat m_server;
 
     /**
      * Thread managing the receptions
      */
     private UpdateThread m_thread;
 
     /**
      * Constructs a new client
      * 
      * @param server
      */
     public Client(IChat server)
     {
         m_server = server;
         m_id = -1;
         m_lastMsgId = -1;
        m_thread = new UpdateThread(this);
     }
 
     /**
      * Connection to the server
      * 
      * @param pseudo
      * @throws RemoteException
      */
     public void connect(String pseudo) throws RemoteException
     {
         m_id = m_server.connect(pseudo);
     }
 
     /**
      * Sends a message to the server
      * 
      * @param msg
      * @throws RemoteException
      */
     public void send(String msg) throws RemoteException
     {
         m_server.send(m_id, msg);
     }
 
     /**
      * Receives the non read messages
      * 
      * @return List of non read messages
      * @throws RemoteException
      */
     public ArrayList<Message> receive() throws RemoteException
     {
         ArrayList<Message> msg = m_server.receive(m_id, m_lastMsgId);
         if (msg.size() > 0)
         {
             m_lastMsgId = msg.get(msg.size() - 1).getId();
         }
 
         return msg;
     }
 
     /**
      * Receives the connected users
      * 
      * @return List of connected users
      * @throws RemoteException
      */
     public ArrayList<User> who() throws RemoteException
     {
         return m_server.who(m_id);
     }
 
     /**
      * Logout
      * 
      * @throws RemoteException
      */
     public void bye() throws RemoteException
     {
         m_server.bye(m_id);
         m_id = -1;
     }
 
     /**
      * Enables the thread
      */
     public void enableUpdate()
     {
         m_thread.enable();
     }
 
     /**
      * Disables the thread
      */
     public void disableUpdate()
     {
         m_thread.disable();
     }
 
     /**
      * @return True if the client is connected
      */
     public boolean isConnected()
     {
         return !(m_id < 0);
     }
 }
