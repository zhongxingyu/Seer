// -----------------------------------------------------------------------------
// Copyright 2011-2012  Harris Corporation, All Rights Reserved
// -----------------------------------------------------------------------------
 package ping.pong.net.server;
 
 import ping.pong.net.client.Client;
 import ping.pong.net.client.io.IoClientImpl;
 import ping.pong.net.connection.ConnectionFactory;
 import ping.pong.net.connection.MessageListener;
 
 public class MyClient
 {
     public static void main(String[] args)
     {
 
         IoClientImpl<String> client = new IoClientImpl<String>(ConnectionFactory.createConnectionConfiguration());
         client.addMessageListener(new MessageListener<Client, String>()
         {
             @Override
             public void messageReceived(Client source, String message)
             {
                 System.out.println("CLient Id: " + source.getId());
                 System.out.println("Message: " + message);
             }
         });
         client.start();
     }
 }
