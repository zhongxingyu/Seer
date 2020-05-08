 package ResImpl;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import comp512.Comm;
 import comp512.Result;
 import comp512.HostPort;
 import ResInterface.*;
 
 // This backend reuses the methods inside ResourceManagerImpl.
 
 public class CustomerManagerImpl {
     protected RMHashtable customerTable = new RMHashtable();
     
     public static void main(String[] args) throws RemoteException {
         // Figure out where server is running
         CustomerManagerImpl obj = new CustomerManagerImpl();
 
         if (args.length != 3) {
             System.err.println("Usage: java ResImpl.CustomerManagerImpl tcp <port> <middlewarehost:port>");
             System.exit(1);
         }
         else if (!args[0].equals("tcp")) {
             System.err.println("CustomerManagerImpl cannot be used as a RMI backend.");
             System.exit(1);
         }
         else {
             try {
                 int localPort = Integer.parseInt(args[1]);
                 String[] middleware = args[2].split(":");
                 HostPort hp = new HostPort(middleware[0], Integer.parseInt(middleware[1]));
                 obj.tcpServer(localPort, hp);
                 System.exit(0);
             }
             catch (IOException e) {
                 System.err.println(e.getMessage());
                 System.exit(1);
             }
         }
     }
     
     public void tcpServer(int port, final HostPort hp) throws IOException {
         ServerSocket server = new ServerSocket(port);
         ExecutorService executor = Executors.newFixedThreadPool(16);
         final CustomerManagerImpl copy = this;
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
                                 res.boolResult = copy.newCustomer(
                                     Integer.parseInt(msg.get(1)),
                                     Integer.parseInt(msg.get(2))
                                     );
                             }
                             else if (msg.size() == 2) {
                                 res.intResult = copy.newCustomer(
                                     Integer.parseInt(msg.get(1))
                                     );
                             }
                             else {
                                 res.boolResult = false;
                             }
                         }
                         else if (msg.get(0).equalsIgnoreCase("deletecustomer")) {
                             res.boolResult = 
                                 copy.deleteCustomer(
                                     Integer.parseInt(msg.get(1)), 
                                     Integer.parseInt(msg.get(2)),
                                     hp); 
                         }
                         else if (msg.get(0).equalsIgnoreCase("querycustomer")) {
                             String queryResult = 
                                 copy.queryCustomerInfo(
                                     Integer.parseInt(msg.get(1)), 
                                     Integer.parseInt(msg.get(2)));
                             if (queryResult.isEmpty())
                                 res.boolResult = false;
                             else
                                 res.stringResult = queryResult;
                         }
                         else if (msg.get(0).equalsIgnoreCase("cancelcustomer")) {
                            Customer cust = (Customer)copy.customerTable.get("customer-" + msg.get(2));
                            System.out.println(cust);
                            System.out.flush();
                            if (cust != null) {
                                cust.cancelReservation(msg.get(3));
                                res.boolResult = true;
                             }
                             else {
                                res.boolResult = false;
                             }
                         }
                         // Not an actual client command, just used for message passing.
                         else if (msg.get(0).equalsIgnoreCase("reservation")) {
                             Customer cust = copy.getCustomer(Integer.parseInt(msg.get(1)));
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
                     catch (Exception e) {
                         res.boolResult = false;
                         Comm.sendObject(connection, res);
                     }
                 }
             });
         }
     }
 
     protected Integer newCustomer(int sessionId) {
         int cid;
         while (true) {
             cid = Integer.parseInt(String.valueOf(sessionId)
                 + String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND))
                 + String.valueOf(Math.round(Math.random() * 100 + 1)));
              if (newCustomer(sessionId, cid))
                  break;
         }
         return cid;
     }
 
     protected Boolean newCustomer(int sessionId, int customerId) {
         Trace.info("INFO: RM::newCustomer(" + sessionId + ", " + customerId
             + ") called");
         Customer cust = getCustomer(customerId);
         if (cust == null) {
             cust = new Customer(customerId);
             putCustomer(sessionId, cust.getKey(), cust);
             Trace.info("INFO: RM::newCustomer(" + sessionId + ", " + customerId
                 + ") created a new customer");
             return true;
         }
         else {
             Trace.info("INFO: RM::newCustomer(" + sessionId + ", " + customerId
                 + ") failed--customer already exists");
             return false;
         } 
     }
 
     protected Customer getCustomer(int customerId) {
         Customer cust = fetchCustomer(0, Customer.getKey(customerId));
         return cust;    
     }
 
     protected String queryCustomerInfo(int sessionId, int customerId) {
         Trace.info("RM::queryCustomerInfo(" + sessionId + ", " + customerId
             + ") called");
         Customer cust = getCustomer(customerId);
         if (cust == null) {
             Trace.warn("RM::queryCustomerInfo(" + sessionId + ", " + customerId
                 + ") failed--customer doesn't exist");
             return "";
         }
         else {
             String s = cust.printBill();
             Trace.info("RM::queryCustomerInfo(" + sessionId + ", " + customerId
                 + "), bill follows...");
             System.out.println(s);
             return s;
         } 
     }
 
     protected Boolean deleteCustomer(int sessionId, int customerId, HostPort hp) {
         Trace.info("RM::deleteCustomer(" + sessionId + ", " + customerId + ") called");
         Customer cust = getCustomer(customerId);
         if (cust == null) {
             Trace.warn("RM::deleteCustomer(" + sessionId + ", " + customerId
                 + ") failed--customer doesn't exist");
             return false;
         }
         else {
             RMHashtable reservations = cust.getReservations();
             deleteCustomer(sessionId, cust.getKey());
             for (Enumeration e = reservations.keys(); e.hasMoreElements();) {
                 String reservedkey = (String) (e.nextElement());
                 ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                 Trace.info("RM::deleteCustomer(" + sessionId + ", " + customerId
                     + ") has reserved " + reserveditem.getKey() + " "
                     + reserveditem.getCount() + " times");
                 
                 Socket sock = null;
                 try {
                     sock = new Socket(hp.host, hp.port);
                     ArrayList<String> cancelMsg = new ArrayList<String>();
                     String itemType = reserveditem.getKey().split("-")[0];
                     cancelMsg.add("cancel" + itemType);
                     cancelMsg.add(sessionId + "");
                     cancelMsg.add(reserveditem.getKey());
                     cancelMsg.add(reserveditem.getCount() + "");
                     Comm.sendObject(sock, cancelMsg);
                 }
                 catch (IOException ex) {
                     System.err.println("Error during deleteCustomer: " + ex.getMessage());
                 }
                 finally {
                     try {
                         sock.close();
                     }
                     catch (IOException ex) {
                         System.err.println("Cannot close socket");
                     }
                 }
                 
             }
             Trace.info("RM::deleteCustomer(" + sessionId + ", " + customerId
                 + ") succeeded");
             return true;
         }
     }
 
     private Customer fetchCustomer(int id, String itemId) {
         synchronized (customerTable) {
             return (Customer)customerTable.get(itemId);
         }
     }
     
     private void putCustomer(int id, String itemId, Customer Customer) {
         synchronized (customerTable) {
             customerTable.put(itemId, Customer);            
         }
     }
     
     private void deleteCustomer(int id, String itemId) {
         synchronized (customerTable) {
             customerTable.remove(itemId);
         }
     }
 
 }
