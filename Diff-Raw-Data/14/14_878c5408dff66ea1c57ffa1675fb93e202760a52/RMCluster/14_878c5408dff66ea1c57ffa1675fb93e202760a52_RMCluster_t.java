 package ResImpl;
 
 import ResInterface.*;
 
 import java.util.*;
 import java.rmi.*;
 
 import java.rmi.registry.Registry;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 public class RMCluster extends ArrayList<ResourceManager>
 implements ResourceManager
 {
     public synchronized boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.addFlight(id, flightNum, flightSeats, flightPrice);
             } catch (RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized void onError(int[] i)
     {
         remove(i[0]--);
     }
 
     public synchronized boolean addCars(int id, String location, int numCars, int price)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.addCars(id, location, numCars, price);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean addRooms(int id, String location, int numRooms, int price)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.addRooms(id, location, numRooms, price);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean deleteItem(int id, String key)
     throws RemoteException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.deleteItem(id, key);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean newCustomer(int id, int cid)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.newCustomer(id, cid);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean deleteFlight(int id, int flightNum)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.deleteFlight(id, flightNum);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean deleteCars(int id, String location)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.deleteCars(id, location);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean deleteRooms(int id, String location)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.deleteRooms(id, location);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized boolean deleteCustomer(int id,int customer)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.deleteCustomer(id, customer);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized int queryFlight(int id, int flightNumber)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryFlight(id, flightNumber);
                 else if (toR != r.queryFlight(id, flightNumber))
                     Trace.info("Mismatched information for flight");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized int queryCars(int id, String location)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryCars(id, location);
                 else if (toR != r.queryCars(id, location))
                     Trace.info("Mismatched information for cars");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized int queryRooms(int id, String location)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryRooms(id, location);
                 else if (toR != r.queryRooms(id, location))
                     Trace.info("Mismatched information for rooms");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized String queryCustomerInfo(int id,int customer)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         String toR = null;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == null)
                     toR = r.queryCustomerInfo(id, customer);
                else if (!r.queryCustomerInfo(id, customer).equals(toR))
                     Trace.info("Mismatched information for customer info");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized int queryFlightPrice(int id, int flightNumber)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryFlightPrice(id, flightNumber);
                 else if(toR != r.queryFlightPrice(id, flightNumber))
                     Trace.info("Mismatched information for flight price.");
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized int queryCarsPrice(int id, String location)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryCarsPrice(id, location);
                 else if (toR != r.queryCarsPrice(id, location))
                     Trace.info("Mismatched information for car price");
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized RMHashtable getCustomerReservations(int id, int customerID)
     throws RemoteException
     {
         int[] i = {0};
         RMHashtable toR = null;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == null)
                     toR = r.getCustomerReservations(id, customerID);
                 else if (toR.equals(r.getCustomerReservations(id, customerID)))
                     Trace.info("Mismatched information for customer reservations");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     /* return the num */
     public synchronized int queryNum(int id, String location)
     throws RemoteException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryNum(id, location);
                 else if (toR != r.queryNum(id, location))
                     Trace.info("Mismatched information for number");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     /* return the price  */
     public synchronized int queryPrice(int id, String location)
     throws RemoteException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryPrice(id, location);
                 else if (toR != r.queryPrice(id, location))
                     Trace.info("Mismatched information for price");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     /* return the price of a room at a location */
     public synchronized int queryRoomsPrice(int id, String location)
     throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         int toR = -1;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (toR == -1)
                     toR = r.queryRoomsPrice(id, location);
                 else if (toR != r.queryRoomsPrice(id, location))
                     Trace.info("Mismatched information for room price");
 
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     /* Reserve a seat on this flight*/
     public synchronized boolean reserveFlight(int id, int customer, int flightNumber)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.reserveFlight(id, customer, flightNumber);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     /* reserve a car at this location */
     public synchronized boolean reserveCar(int id, int customer, String location)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.reserveCar(id, customer, location);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     /* reserve a room certain at this location */
     public synchronized boolean reserveRoom(int id, int customer, String location)
     throws RemoteException, TransactionAbortedException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.reserveRoom(id, customer, location);
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 
     public synchronized void incrementItem(int id, String key, int change)
     throws RemoteException
     {
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 r.incrementItem(id, key, change);
             } catch(RemoteException re)
                 {onError(i);}
         }
     }
 
     public synchronized void decrementItem(int id, String key)
     throws RemoteException
     {
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 r.decrementItem(id, key);
             } catch(RemoteException re)
                 {onError(i);}
         }
     }
 
     public synchronized int start() throws RemoteException
     {
         int[] i = {0};
         int trxnId = -1;
         boolean die = false;
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 if (trxnId == -1)
                     trxnId = r.start();
                 else if (trxnId != r.start())
                 {
                     die = true;
                     break;
                 }
             } catch(RemoteException re)
                 {onError(i);}
         }
         if (die)
             throw new RemoteException();
         return trxnId;
     }
 
     public synchronized void commit(int transactionId) throws RemoteException, TransactionAbortedException
     {
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 r.commit(transactionId);
             } catch(RemoteException re)
                 {onError(i);}
         }
     }
 
     public synchronized void abort(int transactionId) throws RemoteException
     {
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 r.abort(transactionId);
             } catch(RemoteException re)
                 {onError(i);}
         }
     }
 
     public synchronized void enlist(int trxnId) throws RemoteException
     {
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 r.enlist(trxnId);
             } catch(RemoteException re)
                 {onError(i);}
         }
     }
 
     public synchronized boolean shutdown() throws RemoteException
     {
         boolean toR = false;
         int[] i = {0};
         for(; i[0] < size(); i[0]++) {
             ResourceManager r = get(i[0]);
             try {
                 toR = r.shutdown();
             } catch(RemoteException re)
                 {onError(i);}
         }
         return toR;
     }
 }
