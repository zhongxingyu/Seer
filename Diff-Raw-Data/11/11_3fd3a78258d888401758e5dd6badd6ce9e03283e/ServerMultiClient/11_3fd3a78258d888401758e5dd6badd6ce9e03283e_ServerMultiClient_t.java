 package ping.pong.net.server;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import ping.pong.net.client.Client;
 import ping.pong.net.client.io.IoClientImpl;
 import ping.pong.net.connection.Connection;
 import ping.pong.net.connection.ConnectionFactory;
 import ping.pong.net.connection.Envelope;
 import ping.pong.net.connection.MessageListener;
 import ping.pong.net.server.io.IoServerImpl;
 
 public class ServerMultiClient
 {
     public static void main(String[] args)
     {
         final IoServerImpl<String> server = new IoServerImpl<String>(ConnectionFactory.createConnectionConfiguration("localhost", 5011, 5012, false));
         server.addConnectionListener(new ServerConnectionListener()
         {
             @Override
             public void connectionAdded(Server server, Connection conn)
             {
                 System.out.println("Connection Added");
                 server.broadcast(new Envelope<String>()
                 {
                     @Override
                     public boolean isReliable()
                     {
                         return true;
                     }
 
                     @Override
                     public String getMessage()
                     {
                         return "Test";
                     }
                 });
             }
 
             @Override
             public void connectionRemoved(Server server, Connection conn)
             {
                 System.out.println("Connection removed was " + conn.getConnectionId());
             }
         });
 
         server.start();
         Timer timer = new Timer("HeatBeat", true);
         timer.schedule(new TimerTask()
         {
             @Override
             public void run()
             {
                 server.broadcast(new Envelope<String>()
                 {
                     @Override
                     public boolean isReliable()
                     {
                         return true;
                     }
 
                     @Override
                     public String getMessage()
                     {
                         return "HeartBeat";
                     }
                 });
             }
         }, 500, 1000);
 
         for (int i = 0; i < 5; i++)
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
 }
