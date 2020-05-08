 package prj.httpApplication.agent;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import prj.cyclo.Agent;
 import prj.cyclo.TCPReactor;
 import prj.httpApplication.app.WebApp;
 import prj.httpApplication.connection.HTTPSocket;
 import prj.httpApplication.connection.HTTPSocketFactory;
 import prj.httpApplication.utils.ConcurrencyUtils;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 
 public class HTTPAgent extends Agent
 {
     private int _port = 8080;
     private HTTPSocketFactory _socketFactory;
     private Map<Socket, HTTPSocket> _sockets;
     private Logger _logger;
     private WebApp _application;
 
     public HTTPAgent(TCPReactor reactor, WebApp application, int port)
     {
         super(reactor);
         init(application, port);
     }
 
     public HTTPAgent(TCPReactor reactor, WebApp application, int port, ScheduledThreadPoolExecutor executor)
     {
         super(reactor, executor);
         init(application, port);
     }
 
     @Override
     public void connectionMade(Socket socket)
     {
         HTTPSocket httpSocket = _socketFactory.newSocket(socket);
         _sockets.put(socket, httpSocket);
         _application.clientConnected(httpSocket);
     }
 
     @Override
     public boolean isServer()
     {
         return true;
     }
 
     @Override
     public InetSocketAddress getSocketAddress() throws UnknownHostException
     {
         return new InetSocketAddress("0.0.0.0", _port);
     }
 
     @Override
     public void receive(Socket socket, byte[] incomingData)
     {
         HTTPSocket httpSocket = _sockets.get(socket);
         if (httpSocket == null)
         {
             _logger.error("no http socket to handle info from {}", socket);
         }
         else
         {
             httpSocket.parse(new String(incomingData));
         }
     }
 
     @Override
     public void registrationFailed(IOException e)
     {
         _logger.error("registration failed for httpAgent on port " + _port);
     }
 
     @Override
     public void close(Socket socket)
     {
         super.close(socket);
         _sockets.remove(socket);
     }
 
     @Override
     public void onClose(Socket socket)
     {
         HTTPSocket httpSocket = _sockets.remove(socket);
         if (httpSocket != null)
         {
             httpSocket.close();
         }
     }
 
     private void init(WebApp application, int port)
     {
         _port = port;
         _socketFactory = new HTTPSocketFactory(this);
         _sockets = new HashMap<>();
         _application = application;
         _logger = LoggerFactory.getLogger(HTTPAgent.class.getSimpleName());
         ConcurrencyUtils.initialize(_agency);
     }
 
//    @Override
//    public void onShutdown()
//    {
//        //TODO: Handle Server Shutdown
//    }
 }
