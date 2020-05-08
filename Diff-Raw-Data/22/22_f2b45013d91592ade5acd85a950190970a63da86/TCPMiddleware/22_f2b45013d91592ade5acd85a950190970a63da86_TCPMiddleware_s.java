 // -------------------------------
 // adapated from Kevin T. Manley
 // CSE 593
 //
 package ResImpl;
 
 import ResInterface.*;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 import java.io.IOException;
 
 public class TCPMiddleware implements TCPResourceManager
 {    
     protected RMHashtable m_itemHT = new RMHashtable();
 
     static Socket sock_c = null;
     static int portc = 9897;
     static DataInputStream in_c;
     static DataOutputStream out_c;
 
     static Socket sock_p = null;
     static int portp = 9898;
     static DataInputStream in_p;
     static DataOutputStream out_p;
 
     static Socket sock_h = null;
     static int porth = 9899;
     static DataInputStream in_h;
     static DataOutputStream out_h;
 
 
     public static void main(String args[]) {
         // Figure out where server is running
         String server_c = "mimi.cs.mcgill.ca";
         String server_p = server_c;
         String server_h = server_c;
         int port = 9988;
 
         if (args.length == 7)  // mw_port, (server, port) * 3
         {
             port = Integer.parseInt(args[0]);
             server_c = args[1];
             server_p = args[3];
             server_h = args[5];
             portc = Integer.parseInt(args[2]);
             portp = Integer.parseInt(args[4]);
             porth = Integer.parseInt(args[6]);
         }
         else if (args.length == 5) // mw_port, allserver, (port, ) * 3
         {
             port = Integer.parseInt(args[0]);
             server_c = args[1];
             server_p = args[1];
             server_h = args[1];
             portc = Integer.parseInt(args[2]);
             portp = Integer.parseInt(args[3]);
             porth = Integer.parseInt(args[4]);
         }
         else if (args.length == 2) // mw_port, allserver
         {
             port = Integer.parseInt(args[0]);
             server_c = args[1];
             server_p = args[1];
             server_h = args[1];
         }
         else if (args.length == 1)
             port = Integer.parseInt(args[0]);
         else if (args.length != 0) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.TCPMiddleware");
             System.out.println("  OR : java ResImpl.TCPMiddleware middleware_port");
             System.out.println("  OR : java ResImpl.TCPMiddleware mw_port rm_server");
             System.out.println("  OR : java ResImpl.TCPMiddleware mw_port rm_server carport hotelport flightport");
             System.out.println("  OR : java ResImpl.TCPMiddleware mw_port carserver carport hotelserver hotelport flightserver flightport");
             System.exit(1);
         }
 
         try 
         {
            sock_c = new Socket(server, portc);
             in_c = new DataInputStream(sock_c.getInputStream());
             out_c = new DataOutputStream(sock_c.getOutputStream());
 
            sock_p = new Socket(server, portp);
             in_p = new DataInputStream(sock_p.getInputStream());
             out_p = new DataOutputStream(sock_p.getOutputStream());
 
            sock_h = new Socket(server, porth);
             in_h = new DataInputStream(sock_h.getInputStream());
             out_h = new DataOutputStream(sock_h.getOutputStream());
 
             // set up port for client connections
             ServerSocket listenSocket = new ServerSocket(port);
             System.err.println("Server ready");
             TCPMiddleware customerServer = new TCPMiddleware();
             while (true) {
                 Socket clientSocket = listenSocket.accept();
                 Connection c = new Connection(clientSocket, customerServer);
             }
 
         } 
         catch (Exception e) 
         {    
             System.err.println("TCPMiddleware exception: " + e.toString());
             e.printStackTrace();
         }
     }
 
     public TCPMiddleware() {
     }
 
 
     // TODO remove these methods as they will not be called here
     // where these are all the private and many of the protected methods
 
     // Reads a data item
     private RMItem readData( int id, String key )
     {
         synchronized(m_itemHT){
             return (RMItem) m_itemHT.get(key);
         }
     }
 
     // Writes a data item
     private void writeData( int id, String key, RMItem value )
     {
         synchronized(m_itemHT){
             m_itemHT.put(key, value);
         }
     }
 
     // Remove the item out of storage
     protected RMItem removeData(int id, String key){
         synchronized(m_itemHT){
             return (RMItem)m_itemHT.remove(key);
         }
     }
 
 
     // deletes the entire item
     protected boolean deleteItem(int id, String key)
     {
         Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key );
         // Check if there is such an item in the storage
         if( curObj == null ) {
             Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
             return false;
         } else {
             if(curObj.getReserved()==0){
                 removeData(id, curObj.getKey());
                 Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                 return true;
             }
             else{
                 Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                 return false;
             }
         } // if
     }
 
 
     // query the number of available seats/rooms/cars
     public int queryNum(int id, String key) {
         Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key);
         int value = 0;  
         if( curObj != null ) {
             value = curObj.getCount();
         } // else
         Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
         return value;
     }    
 
     // query the price of an item
     public int queryPrice(int id, String key){
         Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key);
         int value = 0; 
         if( curObj != null ) {
             value = curObj.getPrice();
         } // else
         Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
         return value;        
     }
 
     public void decrementItem(int id, String key) {
         ReservableItem curObj = (ReservableItem) readData(id, key);
         curObj.setCount(curObj.getCount() - 1);
         curObj.setReserved(curObj.getReserved() + 1);
     }
 
     public void incrementItem(int id, String key, int change) {
         ReservableItem curObj = (ReservableItem) readData(id, key);
         curObj.setCount(curObj.getCount() + change);
         curObj.setReserved(curObj.getReserved() - change);
     }   
 
 
 
     // reserve an item
     protected boolean reserveItem(int id, int customerID, String key, String location){
         Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
         // Read customer object if it exists (and read lock it)
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
         if( cust == null ) {
             Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         } 
 
         // check if the item is available
         ReservableItem item = (ReservableItem)readData(id, key);
         if(item==null){
             Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         }else if(item.getCount()==0){
             Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
             return false;
         }else{            
             cust.reserve( key, location, item.getPrice());        
             writeData( id, cust.getKey(), cust );
 
             // decrease the number of available items in the storage
             item.setCount(item.getCount() - 1);
             item.setReserved(item.getReserved()+1);
 
             Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }        
     }
 
     // Create a new flight, or add seats to existing flight
     //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
     public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
         throws IOException
         {
             out_p.writeUTF("NEWFLI,"+id+","+flightNum+","+flightSeats+","+flightPrice);
             return in_p.readUTF().equals("TRUE");
         }
 
 
 
     public boolean deleteFlight(int id, int flightNum)
         throws IOException
         {
             out_p.writeUTF("DELFLI," + id + "," + flightNum);
             return in_p.readUTF().equals("TRUE");
         }
 
 
 
     // Create a new room location or add rooms to an existing location
     //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
     public boolean addRooms(int id, String location, int count, int price)
         throws IOException
         {
             out_h.writeUTF("NEWROO,"+id+","+location+","+count+","+price);
             return in_h.readUTF().equals("TRUE");
         }
 
     // Delete rooms from a location
     public boolean deleteRooms(int id, String location)
         throws IOException
         {
             out_h.writeUTF("DELROO,"+id+","+location);
             return in_h.readUTF().equals("TRUE");
         }
 
     // Create a new car location or add cars to an existing location
     //  NOTE: if price <= 0 and the location already exists, it maintains its current price
     public boolean addCars(int id, String location, int count, int price)
         throws IOException
         {
             out_c.writeUTF("NEWCAR,"+id+","+location+","+count+","+price);
             return in_c.readUTF().equals("TRUE");
         }
 
 
     // Delete cars from a location
     public boolean deleteCars(int id, String location)
         throws IOException
         {
             out_c.writeUTF("DELCAR," + id + "," +location);
             return in_c.readUTF().equals("TRUE");
         }
 
 
 
     // Returns the number of empty seats on this flight
     public int queryFlight(int id, int flightNum)
         throws IOException
         {
             out_p.writeUTF("QUEFLI," + id + "," + flightNum);
             return Integer.parseInt(in_p.readUTF());
         }
 
 
     // Returns price of this flight
     public int queryFlightPrice(int id, int flightNum )
         throws IOException
         {
             out_p.writeUTF("PRIFLI," + id + "," + flightNum);
             return Integer.parseInt(in_p.readUTF());
         }
 
 
     // Returns the number of rooms available at a location
     public int queryRooms(int id, String location)
         throws IOException
         {
             out_h.writeUTF("QUEROO," + id + "," + location);
             return Integer.parseInt(in_h.readUTF());
         }
 
 
 
 
     // Returns room price at this location
     public int queryRoomsPrice(int id, String location)
         throws IOException
         {
             out_h.writeUTF("PRIROO," + id + "," + location);
             return Integer.parseInt(in_h.readUTF());
         }
 
 
     // Returns the number of cars available at a location
     public int queryCars(int id, String location)
         throws IOException
         {
             out_c.writeUTF("QUECAR," + id + "," + location);
             return Integer.parseInt(in_c.readUTF());
         }
 
 
     // Returns price of cars at this location
     public int queryCarsPrice(int id, String location)
         throws IOException
         {
             out_c.writeUTF("PRICAR," + id + "," + location);
             return Integer.parseInt(in_c.readUTF());
         }
 
     // Returns data structure containing customer reservation info. Returns null if the
     //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
     //  reservations.
     public RMHashtable getCustomerReservations(int id, int customerID)
         {
             Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
             Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
             if( cust == null ) {
                 Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                 return null;
             } else {
                 return cust.getReservations();
             } // if
         }
 
     // return a bill
     public String queryCustomerInfo(int id, int customerID)
         throws IOException
         {
             Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
             Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
             if( cust == null ) {
                 Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                 return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
             } else {
                 String s = "Bill for customer " + customerID + "\n";
                 String key = null;
                 RMHashtable m_Reservations = cust.getReservations();
                 DataOutputStream whom;
                 for (Enumeration e = m_Reservations.keys(); e.hasMoreElements(); ) {
                     key = (String) e.nextElement();
                     if(key.startsWith("car-"))
                         s += queryCars(id, key.substring(4)) + " " + key + " $" + queryCarsPrice(id, key.substring(4)) + "\n";
                     else if(key.startsWith("flight-"))
                         s += queryFlight(id, Integer.parseInt(key.substring(7))) + " " + key + " $" + queryFlightPrice(id, Integer.parseInt(key.substring(7))) + "\n";
                     else if(key.startsWith("room-"))
                         s += queryRooms(id, key.substring(5)) + " " + key + " $" + queryRoomsPrice(id, key.substring(5))+ "\n";
                 }
                 Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                 System.out.println( s );
                 return s;
             } // if
         }
 
     // customer functions
     // new customer just returns a unique customer identifier
 
     public int newCustomer(int id)
         throws IOException
         {
             Trace.info("INFO: RM::newCustomer(" + id + ") called" );
             // Generate a globally unique ID for the new customer
             int cid = Integer.parseInt( String.valueOf(id) +
                     String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                     String.valueOf( Math.round( Math.random() * 100 + 1 )));
             Customer cust = new Customer( cid );
             writeData( id, cust.getKey(), cust );
             Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
             return cid;
         }
 
     // I opted to pass in customerID instead. This makes testing easier
     public boolean newCustomer(int id, int customerID )
         throws IOException
         {
             Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
             Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
             if( cust == null ) {
                 cust = new Customer(customerID);
                 writeData( id, cust.getKey(), cust );
                 Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
                 return true;
             } else {
                 Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
                 return false;
             } // else
         }
 
 
     // Deletes customer from the database. 
     public boolean deleteCustomer(int id, int customerID)
         throws IOException
         {
             Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
             Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
             if( cust == null ) {
                 Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                 return false;
             } else {            
                 // Increase the reserved numbers of all reservable items which the customer reserved. 
                 RMHashtable reservationHT = cust.getReservations();
                 for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){        
                     String reservedkey = (String) (e.nextElement());
                     ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                     Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
 
                     DataOutputStream sendto = sendToWhom(reservedkey);
                     sendto.writeUTF("INCITE,"+id+","+reservedkey+","+reserveditem.getCount());
                     //sendto.incrementItem(id, reservedkey, reserveditem.getCount());
                     // TODO: the trace is bad now
                     //Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
                 }
 
                 // remove the customer from the storage
                 removeData(id, cust.getKey());
 
                 Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
                 return true;
             } // if
         }
 
 
     /* public DataInputStream recvFWrhom(String key)
     {
         if(key.startsWith("car-"))
             return in_c;
         else if(key.startsWith("flight-"))
             return in_p;
         else if(key.startsWith("room-"))
             return in_h;
         return null;
     } */
 
     public DataOutputStream sendToWhom(String key)
     {
         if(key.startsWith("car-"))
             return out_c;
         else if(key.startsWith("flight-"))
             return out_p;
         else if(key.startsWith("room-"))
             return out_h;
         return null;
     }
 
     // Adds car reservation to this customer. 
     public boolean reserveCar(int id, int customerID, String location)
         throws IOException
         {
         String key = Car.getKey(location);
         Trace.info("RM::reserveCar( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
         // Read customer object if it exists (and read lock it)
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
         if( cust == null ) {
             Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         } 
 
         // MAKE THE CAR SERVER DO THE THINGS
         int num = queryCars(id, location);
         if (num == 0) {
             Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         } else {            
             cust.reserve( key, location, queryCarsPrice(id, location));
             writeData( id, cust.getKey(), cust );
 
             out_c.writeUTF("DECITE," + id + "," + key);
 
             Trace.info("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }        
         }
 
 
     // Adds room reservation to this customer. 
     public boolean reserveRoom(int id, int customerID, String location)
         throws IOException
         {
         String key = Hotel.getKey(location);
         Trace.info("RM::reserveHotel( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
         // Read customer object if it exists (and read lock it)
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
         if( cust == null ) {
             Trace.warn("RM::reserveHotel( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         } 
 
         // MAKE THE HOTEL SERVER DO THE THINGS
         int num = queryRooms(id, location);
         if (num == 0) {
             Trace.warn("RM::reserveHotel( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         } else {            
             cust.reserve( key, location, queryRoomsPrice(id, location));        
             writeData( id, cust.getKey(), cust );
 
             out_h.writeUTF("DECITE," + id + "," + key);
 
             Trace.info("RM::reserveHotel( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }        
         }
 
     // Adds flight reservation to this customer.  
     public boolean reserveFlight(int id, int customerID, int flightNum)
         throws IOException
         {
         String key = Flight.getKey(flightNum);
         Trace.info("RM::reservePlane( " + id + ", customer=" + customerID + ", " +key+ ", "+flightNum+" ) called" );        
         // Read customer object if it exists (and read lock it)
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
         if( cust == null ) {
             Trace.warn("RM::reservePlane( " + id + ", " + customerID + ", " + key + ", "+flightNum+")  failed--customer doesn't exist" );
             return false;
         } 
 
         // MAKE THE PLANE SERVER DO THE THINGS
         int num = queryFlight(id, flightNum);
         if (num == 0) {
             Trace.warn("RM::reservePlane( " + id + ", " + customerID + ", " + key+", " +flightNum+") failed--item doesn't exist" );
             return false;
         } else {            
             cust.reserve( key, String.valueOf(flightNum), queryFlightPrice(id, flightNum));        
             writeData( id, cust.getKey(), cust );
 
             out_p.writeUTF("DECITE," + id + "," + key);
 
             Trace.info("RM::reservePlane( " + id + ", " + customerID + ", " + key + ", " +flightNum+") succeeded" );
             return true;
         }        
         }
 
     /* reserve an itinerary */
     public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
         throws IOException
         {
             ListIterator itr = flightNumbers.listIterator();
             while (itr.hasNext()) {
                 reserveFlight(id, customer, Integer.parseInt(String.valueOf(itr.next())));
             }
             if (Car)
                 reserveCar(id, customer, location);
 
             if (Room)
                 reserveRoom(id, customer, location);
 
             return true;
         }
 }
 
 class Connection extends Thread {
     DataInputStream in;
     DataOutputStream out;
     Socket clientSocket;
     TCPResourceManager master;
     public Connection (Socket aClientSocket, TCPResourceManager mast) {
         master = mast;
         try {
             clientSocket = aClientSocket;
             in = new DataInputStream( clientSocket.getInputStream());
             out =new DataOutputStream( clientSocket.getOutputStream());
             this.start();
         } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
     }
     public void run(){
         try {
             while(true)
             {
                 String data = in.readUTF();
                 String[] splat = data.split(",");
                 
                 if(splat[0].equals("NEWFLI"))
                     if (master.addFlight(Integer.parseInt(splat[1]), Integer.parseInt(splat[2]),
                             Integer.parseInt(splat[3]), Integer.parseInt(splat[4])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("NEWCAR"))
                     if (master.addCars(Integer.parseInt(splat[1]), splat[2],
                             Integer.parseInt(splat[3]), Integer.parseInt(splat[4])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("NEWROO"))
                     if (master.addRooms(Integer.parseInt(splat[1]), splat[2],
                             Integer.parseInt(splat[3]), Integer.parseInt(splat[4])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("NEWCUS"))
                     out.writeUTF("" + master.newCustomer(Integer.parseInt(splat[1])));
 
                 else if(splat[0].equals("DELFLI"))
                     if(master.deleteFlight(Integer.parseInt(splat[1]),
                             Integer.parseInt(splat[2])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("DELCAR"))
                     if(master.deleteCars(Integer.parseInt(splat[1]),
                             splat[2]))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("DELROO"))
                     if(master.deleteRooms(Integer.parseInt(splat[1]),
                             splat[2]))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("DELCUS"))
                     if(master.deleteCustomer(Integer.parseInt(splat[1]),
                             Integer.parseInt(splat[2])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("QUEFLI"))
                     out.writeUTF("" + master.queryFlight(Integer.parseInt(splat[1]),
                         Integer.parseInt(splat[2])));
 
                 else if(splat[0].equals("QUECAR"))
                     out.writeUTF("" + master.queryCars(Integer.parseInt(splat[1]),
                         splat[2]));
 
                 else if(splat[0].equals("QUEROO"))
                     out.writeUTF("" + master.queryRooms(Integer.parseInt(splat[1]),
                         splat[2]));
 
                 else if(splat[0].equals("QUECUS"))
                     out.writeUTF(master.queryCustomerInfo(Integer.parseInt(splat[1]),
                         Integer.parseInt(splat[2])));
 
                 else if(splat[0].equals("PRIFLI"))
                     out.writeUTF("" + master.queryFlightPrice(Integer.parseInt(splat[1]),
                         Integer.parseInt(splat[2])));
 
                 else if(splat[0].equals("PRICAR"))
                     out.writeUTF("" + master.queryCarsPrice(Integer.parseInt(splat[1]),
                         splat[2]));
 
                 else if(splat[0].equals("PRIROO"))
                     out.writeUTF("" + master.queryRoomsPrice(Integer.parseInt(splat[1]),
                         splat[2]));
 
                 else if(splat[0].equals("RESFLI"))
                     if(master.reserveFlight(Integer.parseInt(splat[1]),
                             Integer.parseInt(splat[2]), Integer.parseInt(splat[3])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("RESCAR"))
                     if(master.reserveCar(Integer.parseInt(splat[1]),
                             Integer.parseInt(splat[2]), splat[3]))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("RESROO"))
                     if(master.reserveRoom(Integer.parseInt(splat[1]),
                             Integer.parseInt(splat[2]), splat[3]))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("RESITI"))
                 {
                     int veclen = splat.length - 6;
                     Vector sendme = new Vector();
                     for(int i=0; i<veclen; i++)
                         sendme.add(Integer.parseInt(splat[i + 3]));
                     if(master.itinerary(Integer.parseInt(splat[1]), Integer.parseInt(splat[2]),
                             sendme, splat[splat.length - 3], splat[splat.length - 2].equals("true"),
                             splat[splat.length - 1].equals("true")))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
                 }
                 else if(splat[0].equals("QUICLI"))
                     break;
 
                 else if(splat[0].equals("NEWCU2"))
                     if(master.newCustomer(Integer.parseInt(splat[1]),
                             Integer.parseInt(splat[2])))
                         out.writeUTF("TRUE");
                     else
                         out.writeUTF("FALSE");
 
                 else if(splat[0].equals("INCITE"))
                     master.incrementItem(Integer.parseInt(splat[1]),
                             splat[2], Integer.parseInt(splat[3]));
 
                 else if(splat[0].equals("DECITE"))
                     master.decrementItem(Integer.parseInt(splat[1]),
                         splat[2]);
             }
             //out.writeUTF(data);
         } catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
         } catch(IOException e) {System.out.println("IO:"+e.getMessage());
         } finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
     }
 }
