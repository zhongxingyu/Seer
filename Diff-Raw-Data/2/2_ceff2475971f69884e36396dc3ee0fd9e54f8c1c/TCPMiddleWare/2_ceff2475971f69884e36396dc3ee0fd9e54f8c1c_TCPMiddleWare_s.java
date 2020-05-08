 package comp512;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class TCPMiddleWare {
     private Map<String, HostPort> backends;
     
     public TCPMiddleWare() {
         backends = new HashMap<String, HostPort>();
     }
     
     public static void main(String[] args) {
         if (args.length != 5) {
             System.err.println("Usage: comp512.TCPMiddleWare <port> car=<host>:<port> " +
             		"flight=<host>:<port> hotel=<host>:<port> customer=<host>:<port>");
             System.exit(1);
         }
         
         ExecutorService executor = Executors.newFixedThreadPool(16);
         TCPMiddleWare server = new TCPMiddleWare();
         int port = Integer.parseInt(args[0]);
         server.populateBackends(args);
         ServerSocket serverSocket;
         
         try {
             serverSocket = new ServerSocket(port);
             while (true) {
                 // Accept connection from a new client
                 final Socket connection;
                 try {
                     connection = serverSocket.accept();
                     System.out.println("Connection from "
                         + connection.getRemoteSocketAddress());
                 }
                 catch (IOException e) {
                     System.err.println("Error in server.accept(): "
                         + e.getMessage());
                     continue;
                 }
 
                 ArrayList<String> msg = (ArrayList<String>)Comm.recvObject(connection);
                 System.out.println(msg);
 
                 // The following steps happen here:
                 // 1. An asynchronous request is send to the dispatcher.
                 // 2. While the dispatcher is doing its work, a new asynchronous
                 //    task is started that'll wait for the result of the dispatcher
                 //    and then send the result back to the client.
                 // 3. While these two threads are running, the middleware listens
                 //    for new connections.
                 final Future<Result> resultFuture = executor.submit(
                     new BackendDispatcher(msg, server.backends));
                 executor.execute(new Runnable() {
                     @Override
                     public void run() {
                         Result result;
                         try {
                             result = resultFuture.get();
                             Comm.sendObject(connection, result);
                             connection.close();
                         }
                         catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         catch (ExecutionException e) {
                             e.printStackTrace();
                         }
                         catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 });
             }
         }
         catch (IOException e) {
             System.err.println(e.getMessage());
            System.err.println("Cannot create a new socket on port 5566.");
             System.exit(1);
         }
     }
 
     /**
      * Add the backends with their associated host/port information
      * to the backends map.
      * @param args array of mappings of the form: backend=host:port
      */
     public void populateBackends(String[] args) {
         for (int i = 1; i < args.length; ++i) {
             String arg = args[i];
             String[] keyValue = arg.split("=");
             String[] hostPort = keyValue[1].split(":");
             backends.put(keyValue[0], 
                 new HostPort(hostPort[0], Integer.parseInt(hostPort[1])));
         }
     }
     
 }
 
 
 class BackendDispatcher implements Callable<Result> {
     private ArrayList<String> msg;
     private Map<String, HostPort> backends;
     
     public BackendDispatcher(ArrayList<String> msg, Map<String, HostPort> backends) {
         this.msg = msg;
         this.backends = backends;
     }
     
     public Result call() {
         String cmd = this.msg.get(0);
         Socket socket;
         HostPort hp = null;
         
 
         if (cmd.contains("flight")) {
             hp = this.backends.get("flight");
             
         }
         else if (cmd.contains("car")) {
             hp = this.backends.get("car");
         }
         else if (cmd.contains("room")) {
             hp = this.backends.get("hotel");
 
         }
         else if (cmd.contains("customer")) {
             hp = this.backends.get("customer");
 
         }
         else if (cmd.contains("itinerary")) {
             // TODO: should we lock the customer to make sure he's not deleted
             // in the middle of booking his itinerary?
             
             HostPort flightHp = backends.get("flight");
             HostPort carHp = backends.get("car");
             HostPort hotelHp = backends.get("hotel");
             String sessionId = msg.get(1);
             String customerId = msg.get(2);
 
             // Book flights
             for (int i = 3; i < msg.size()-3; ++i) {
                 ArrayList<String> flightMsg = new ArrayList<String>();
                 flightMsg.add("reserveflight");
                 flightMsg.add(sessionId); // Reuse session id
                 flightMsg.add(customerId);
                 flightMsg.add(msg.get(i));
                 this.sendCommand(flightHp, flightMsg);
             }
 
             // Book car
             ArrayList<String> carMsg = new ArrayList<String>();
             carMsg.add("reservecar");
             carMsg.add(sessionId);
             carMsg.add(customerId);
             carMsg.add(msg.get(msg.size() - 3));
             carMsg.add(msg.get(msg.size() - 2));
             this.sendCommand(carHp, carMsg);
             
             // Book hotel
             ArrayList<String> hotelMsg = new ArrayList<String>();
             hotelMsg.add("reserveroom");
             hotelMsg.add(sessionId);
             hotelMsg.add(customerId);
             hotelMsg.add(msg.get(msg.size() - 3));
             hotelMsg.add(msg.get(msg.size() - 1));
             this.sendCommand(hotelHp, hotelMsg);
             
             Result res = new Result();
             res.boolResult = true;
             return res;
             
         }
         else {
             Result res = new Result();
             res.boolResult = false;
             return res;
         }
         
         return sendCommand(hp, this.msg);
     }
 
     private Result sendCommand(HostPort hp, ArrayList<String> msg) {
         Socket socket;
         try {
             
             socket = new Socket(hp.host, hp.port);
             Comm.sendObject(socket, msg);
             Result res = (Result)Comm.recvObject(socket);
             socket.close();
             
             // We got a result back from a reservation command,
             // we now need to send it to the Customer backend.
             // Message format:
             // <"reservation", customerId, reservationKey, location, price>
             if (res.reservationResult != null) {
                 ArrayList<String> reservationMsg = new ArrayList<String>();
                 reservationMsg.add("reservation");
                 reservationMsg.add(msg.get(2));
                 reservationMsg.add(res.reservationResult.getKey());
                 reservationMsg.add(res.reservationResult.getLocation());
                 reservationMsg.add(res.reservationResult.getPrice() + "");
                 hp = this.backends.get("customer");
                 socket = new Socket(hp.host, hp.port);
                 Comm.sendObject(socket, reservationMsg);
             }
             
             return res;
         }
         catch (IOException e) {
             return null;
         }
     }
 }
