 package pt.up.fe.pt.lpoo.bombermen;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.Socket;
 import java.net.SocketException;
 
 import pt.up.fe.pt.lpoo.bombermen.messages.Message;
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_PING;
 
 public class ClientHandler
 {
     @Override
     protected void finalize() throws Throwable
     {
         ClientReceiver.Finish();
         super.finalize();
     }
 
     public final int Guid;
     private Socket _socket;
     private final BombermenServer _server;
     private boolean _stillConnected = true;
     private int _timer = 0;
 
     private long _timePingSent;
     private long _ping = 0;
     
     public String GetIp()
     {
         return _socket.getInetAddress().getHostAddress();
     }
     
     public final Sender<Message> ClientSender;
 
     public class ServerReceiver extends Receiver<Message>
     {
         public ServerReceiver(Socket socket)
         {
             super(socket);
         }
 
         @Override
         public void run()
         {
             try
             {
                 ObjectInputStream in = new ObjectInputStream(_socket.getInputStream());
 
                 while (!_done)
                 {
                     try
                     {
                         Message msg = (Message) in.readObject();
                         if (_done) break;
                         if (msg == null) continue;
 
                         _server.PushMessage(Guid, msg);
 
                     }
                     catch (ClassNotFoundException e)
                     {
                         e.printStackTrace();
                     }
                     catch (EOFException e)
                     {
                     }
 
                 }
 
                 in.close();
 
             }
             catch (SocketException e)
             {
             }
             catch (IOException e1)
             {
                 e1.printStackTrace();
             }
         }
 
     };
 
     public final Receiver<Message> ClientReceiver;
 
     public ClientHandler(int guid, Socket socket, BombermenServer server)
     {
         Guid = guid;
         _socket = socket;
         _server = server;
         ClientSender = new Sender<Message>(_socket);
         ClientReceiver = new ServerReceiver(_socket);
     }
 
     public boolean IsStillConnected()
     {
         return _stillConnected;
     }
 
     public long GetPing()
     {
         return _ping;
     }
     
     public void OnPingSent()
     {
         _timePingSent = System.currentTimeMillis();
     }
     
     public void OnPingReceived()
     {
         _ping = System.currentTimeMillis() - _timePingSent;
        _ping /= 2;
     }
     
     public void Update(int diff)
     {
         _timer += diff;
         
         if (_timer >= 1000)
         {
             ClientSender.Send(new SMSG_PING());
             OnPingSent();
             
             _timer = 0;
             try
             {
                 ClientSender.TrySend(null);
                 _stillConnected = true;
             }
             catch (IOException e)
             {
                 _stillConnected = false;
             }
         }
     }
 }
