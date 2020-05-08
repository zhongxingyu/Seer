 package ResImpl;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import comp512.Comm;
 import comp512.Result;
 
 // This backend reuses the methods inside ResourceManagerImpl.
 
 public class CustomerManagerImpl {
     public static void main(String[] args) throws RemoteException {
         // Figure out where server is running
         int port = 5005;
         ResourceManagerImpl obj = new ResourceManagerImpl();
 
         if (args.length == 0) {
             System.err.println("Usage: java ResImpl.CustomerManagerImpl tcp [<port>]");
             System.exit(1);
         }
         else if (args.length >= 1 && args[0].equals("tcp")) {
             try {
                 tcpServer(obj, args.length >= 2 ? Integer.parseInt(args[1]) : port);
                 System.exit(0);
             }
             catch (IOException e) {
                 System.err.println(e.getMessage());
                 System.exit(1);
             }
         }
         else if (args.length >= 1 && args[0].equals("rmi")) {
             System.err.println("CustomerManagerImpl cannot be used as a RMI backend.");
             System.exit(1);
         }
     }
     
     public static void tcpServer(final ResourceManagerImpl obj, int port) throws IOException {
         ServerSocket server = new ServerSocket(port);
         ExecutorService executor = Executors.newFixedThreadPool(16);
         while (true) {
             final Socket connection = server.accept();
             System.out.println("Accepting connection: " + connection.toString());
 
             executor.execute(new Runnable() {
                 public void run() {
                     Result res = new Result();
 
                     try {
                         ArrayList<String> msg = (ArrayList<String>) Comm.recvObject(connection);
 
                         if (msg.get(0).equalsIgnoreCase("newcustomer")) {
                             if (msg.size() == 3) {
                                 res.boolResult = obj.newCustomer(
                                     Integer.parseInt(msg.get(1)),
                                     Integer.parseInt(msg.get(2))
                                     );
                             }
                             else if (msg.size() == 2) {
                                 res.intResult = obj.newCustomer(
                                     Integer.parseInt(msg.get(1))
                                     );
                             }
                             else {
                                 res.boolResult = false;
                             }
                         }
                         else if (msg.get(0).equalsIgnoreCase("deletecustomer")) {
                             res.boolResult = 
                                 obj.deleteCustomer(
                                     Integer.parseInt(msg.get(1)), 
                                     Integer.parseInt(msg.get(2))); 
                         }
                         else if (msg.get(0).equalsIgnoreCase("querycustomer")) {
                             res.stringResult =
                                 obj.queryCustomerInfo(
                                     Integer.parseInt(msg.get(1)), 
                                     Integer.parseInt(msg.get(2)));
                         }
                         // Not an actual client command, just used for message passing.
                         else if (msg.get(0).equalsIgnoreCase("reservation")) {
                             Customer cust = obj.getCustomer(Integer.parseInt(msg.get(1)));
                             if (cust != null) {
                                 cust.reserve(msg.get(2), msg.get(3), Integer.parseInt(msg.get(4)));
                                 res.boolResult = true;
                             }
                             else {
                                 res.boolResult = false;
                             }
                         }
                         else {
                             res.boolResult = false;
                         }
 
                         Comm.sendObject(connection, res);
                         connection.close();
                     }
                     catch (NumberFormatException e) {
                         res.boolResult = false;
                         Comm.sendObject(connection, res);
                     }
                     catch (RemoteException e) {
                         res.boolResult = false;
                         Comm.sendObject(connection, res);
                     }
                     catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             });
         }
     }
 }
