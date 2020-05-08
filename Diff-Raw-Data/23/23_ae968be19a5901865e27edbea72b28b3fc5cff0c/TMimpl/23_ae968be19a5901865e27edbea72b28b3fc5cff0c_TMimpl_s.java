 package serversrc.resImpl;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import LockManager.LockManager;
 import serversrc.resInterface.*;
 
 public class TMimpl implements TransactionManager {
 	
 	private RMCar rmCar;
 	private RMFlight rmFlight;
 	private RMHotel rmHotel;
 	private RMCustomer rmCustomer;
 	private RMHashtable transactionHT;
 	//lock is passed to the TM so that the TimeToLive can 
 	//unlock when aborting an idle transaction
 	private LockManager lock;
 	
     // Reads a data item
     private Transaction readData(int id)
     {
         synchronized(transactionHT) {
             return (Transaction) transactionHT.get(id);
         }
     }
 
     // Writes a data item
     private void writeData( int id, Transaction value )
     {
         synchronized(transactionHT) {
             transactionHT.put(id, value);
         }
     }
     
     // Remove the item out of storage
     protected Transaction removeData(int id) {
         synchronized(transactionHT) {
             return (Transaction)transactionHT.remove(id);
         }
     }
 	
 	public TMimpl(RMCar Car, RMFlight Flight, RMHotel Hotel, RMCustomer Customer, LockManager lock){
 		rmCar = Car;
 		rmFlight = Flight;
 		rmHotel = Hotel;
 		rmCustomer = Customer;
 		this.lock = lock;
 		transactionHT = new RMHashtable();
 	}
 
 	@Override
 	public int start() throws RemoteException {
 		// Generate a globally unique ID for the new transaction
         int tid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                    String.valueOf( Math.round( Math.random() * 100 + 1 )));
         writeData(tid, new Transaction(tid));
 		return tid;
 	}
 
 	@Override
 	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
 		Transaction t = removeData(transactionID);
 		if(t == null)
 			throw new InvalidTransactionException();
 		if(t.isCarEnlisted())
 			rmCar.commit(transactionID);
 		if(t.isFlightEnlisted())
 			rmFlight.commit(transactionID);
 		if(t.isHotelEnlisted())
 			rmHotel.commit(transactionID);
 		if(t.isCustomerEnlisted())
 			rmCustomer.commit(transactionID);
 		return true;
 	}
 
 	@Override
 	public void abort(int transactionID) throws RemoteException, InvalidTransactionException {
 		Transaction t = removeData(transactionID);
 		if(t == null)
 			throw new InvalidTransactionException();
 		if(t.isCarEnlisted())
 			rmCar.abort(transactionID);
 		if(t.isFlightEnlisted())
 			rmFlight.abort(transactionID);
 		if(t.isHotelEnlisted())
 			rmHotel.abort(transactionID);
 		if(t.isCustomerEnlisted())
 			rmCustomer.abort(transactionID);
 		
 	}
 
 	@Override
	public void enlist(int transactionID, RMType rm) {
 		Transaction t = readData(transactionID);
 		switch(rm){
 		case CAR:
 			t.enlistCar();
 			break;
 		case FLIGHT:
 			t.enlistFlight();
 			break;
 		case HOTEL:
 			t.enlistHotel();
 			break;
 		case CUSTOMER:
 			t.enlistCustomer();
 			break;
 		}
 	}
 	
 	public boolean shutdown() throws RemoteException {
 		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
 		for (Thread s : threadSet) {
 			try {
 				s.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		for(Enumeration<Transaction> i = transactionHT.elements(); i.hasMoreElements(); ){
     		Transaction tr = i.nextElement();
     		try {
 				abort(tr.getID());
 			} catch (RemoteException e) {
 				// Should not happen
 				e.printStackTrace();
 			} catch (InvalidTransactionException e) {
 				// Should not happen
 				e.printStackTrace();
 			}
 		}
 		rmCar.shutdown();
 		rmFlight.shutdown();
 		rmHotel.shutdown();
 		rmCustomer.shutdown();
 		return true;
 	}
 	
	public void lives(int id){
 		Transaction tr = readData(id);
 		tr.resetTTL();
 	}
 	
 	
 	/**
 	 * Defines the type of transactions as kept in
 	 * the hashtable. This class lives as a subclass to
 	 * be able to have the TTL as a field and have the
 	 * TTL call abort when delay is passed to abort the
 	 * transaction.
 	 */
 	@SuppressWarnings("serial")
 	public class Transaction implements Serializable {
 
 		private boolean carEnlisted = false;
 		private boolean hotelEnlisted = false;
 		private boolean flightEnlisted = false;
 		private boolean customerEnlisted = false;
 		private TimeToLive ttl;
 		private int id;
 		
 		public Transaction (int id){
 			this.id = id;
 			ttl = new TimeToLive();
 		}
 		
 		public int getID (){
 			return id;
 		}
 		
 		class TimeToLive {
 
 			private Timer timer;
 			private final long life = 600; //in seconds; 10 min.
 
 			TimeToLive() {
 				timer = new Timer();
 				timer.schedule(new RemindTask(), life*1000);
 			}
 
 			class RemindTask extends TimerTask {
 				public void run() {
 					try {
 						lock.UnlockAll(id);
 						abort(id);
 					} catch (RemoteException e) {
 						// Should not happen
 						e.printStackTrace();
 					} catch (InvalidTransactionException e) {
 						// Should not happen
 						e.printStackTrace();
 					}
 					timer.cancel(); //Terminate the timer thread
 				}
 			}	
 			
 			void reset(){
 				timer.cancel();
 				timer = new Timer();
 				timer.schedule(new RemindTask(), life*1000);
 			}
 		}
 
 		void resetTTL(){
 			ttl.reset();
 		}
 		
 		
 		/**
 		 * Tells whether RMCar is enlisted
 		 */
 		public boolean isCarEnlisted() {
 			return carEnlisted;
 		}
 		
 		/**
 		 * Adds RMCar to the list of enlisted RMs
 		 */
 		public void enlistCar() {
 			this.carEnlisted = true;
 		}
 
 		/**
 		 * Tells whether RMHotel is enlisted
 		 */
 		public boolean isHotelEnlisted() {
 			return hotelEnlisted;
 		}
 
 		/**
 		 * Adds RMHotel to the list of enlisted RMs
 		 */
 		public void enlistHotel() {
 			this.hotelEnlisted = true;
 		}
 
 		/**
 		 * Tells whether RMFlight is enlisted
 		 */
 		public boolean isFlightEnlisted() {
 			return flightEnlisted;
 		}
 
 		/**
 		 * Adds RMFlight to the list of enlisted RMs
 		 */
 		public void enlistFlight() {
 			this.flightEnlisted = true;
 		}
 
 		/**
 		 * Tells whether RMCustomer is enlisted
 		 */
 		public boolean isCustomerEnlisted() {
 			return customerEnlisted;
 		}
 
 		/**
 		 * Adds RMCustomer to the list of enlisted RMs
 		 */
 		public void enlistCustomer() {
 			this.customerEnlisted = true;
 		}
 
 	}
 
 	
 	
 	
 }
