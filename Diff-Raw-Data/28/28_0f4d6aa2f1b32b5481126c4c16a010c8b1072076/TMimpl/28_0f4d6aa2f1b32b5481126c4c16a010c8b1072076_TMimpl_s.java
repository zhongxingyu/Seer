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
         Trace.info("TM::start() succeeded. Providing transaction id " + tid );
 		return tid;
 	}
 
 	@Override
 	public int start(boolean autocommit) throws RemoteException {
 		// Generate a globally unique ID for the new transaction
         int tid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                    String.valueOf( Math.round( Math.random() * 100 + 1 )));
         writeData(tid, new Transaction(tid, autocommit));
         Trace.info("TM::autocommit() succeeded. Providing transaction id " + tid );
 		return tid;
 	}
 	
 	@Override
 	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
 		Transaction t = readData(transactionID);
 		if(t == null)
 			throw new InvalidTransactionException();
 		t.timeoutReset();
 		if(t.isEnlisted(RMType.CAR)){
 			PrepareThread th = new PrepareThread(RMType.CAR, t);
 			th.start();
 		}
 		if(t.isEnlisted(RMType.FLIGHT)){
 			PrepareThread th = new PrepareThread(RMType.FLIGHT, t);
 			th.start();
 		}
 		if(t.isEnlisted(RMType.HOTEL)){
 			PrepareThread th = new PrepareThread(RMType.HOTEL, t);
 			th.start();
 		}
 		if(t.isEnlisted(RMType.CUSTOMER)){
 			PrepareThread th = new PrepareThread(RMType.CUSTOMER, t);
 			th.start();
 		}
 		
 		try {
 			t.timeoutStart();
 			while(!t.isReady()){
 				if(t.isTimedOut()){
 					abort(transactionID);
 					return false;
 				}
 				wait();
 			}
 			Trace.info("TM::commit(" + transactionID + ") all replied.");
 			if(t.voteResult()){
 				Trace.info("TM::commit(" + transactionID + ") vote passed.");
 				if(t.isEnlisted(RMType.CAR))
 					rmCar.commit(transactionID);
 				if(t.isEnlisted(RMType.FLIGHT))
 					rmFlight.commit(transactionID);
 				if(t.isEnlisted(RMType.HOTEL))
 					rmHotel.commit(transactionID);
 				if(t.isEnlisted(RMType.CUSTOMER))
 					rmCustomer.commit(transactionID);
 				if(t.isAutoCommitting())
 					t.commit();
 				else{
 					t.cancelTTL();
 					removeData(transactionID);
 				}
 			} else { 
 				Trace.info("TM::commit(" + transactionID + ") vote was rejected.");
 				abort(transactionID);
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransactionAbortedException e) {
 			Trace.info("TM::commit(" + transactionID + ") An RM returned an error. Transaction is aborted.");
 			abort(transactionID);
 		}
 		
 		Trace.info("TM::commit(" + transactionID + ") succeeded.");
 		return true;
 	}
 
 	@Override
 	public void abort(int transactionID) throws RemoteException, InvalidTransactionException {
 		Transaction t = readData(transactionID);
 		if(t == null)
 			throw new InvalidTransactionException();
 		if(t.isEnlisted(RMType.CAR))
 			rmCar.abort(transactionID);
 		if(t.isEnlisted(RMType.FLIGHT))
 			rmFlight.abort(transactionID);
 		if(t.isEnlisted(RMType.HOTEL))
 			rmHotel.abort(transactionID);
 		if(t.isEnlisted(RMType.CUSTOMER))
 			rmCustomer.abort(transactionID);
 		if(t.isAutoCommitting())
 			t.commit();//Does not actually performs commit but clears the enlisted properties.
 		else{
 			t.cancelTTL();
 			removeData(transactionID);
 		}
 		Trace.info("TM::abort(" + transactionID + ") succeeded.");
 	}
 
 	@Override
 	public void enlist(int transactionID, RMType rm) throws InvalidTransactionException {
 		Transaction t = readData(transactionID);
 		if(t == null)
 			throw new InvalidTransactionException();
 		try {
 			t.enlist(rm);
 			switch(rm){
 			case CAR:
 				rmCar.enlist(transactionID);
 				break;
 			case FLIGHT:
 				rmFlight.enlist(transactionID);
 				break;
 			case HOTEL:
 				rmHotel.enlist(transactionID);
 				break;
 			case CUSTOMER:
 				rmCustomer.enlist(transactionID);
 				break;
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		Trace.info("TM::enlist(" + transactionID + ", " + rm.toString() + ") succeeded.");
 	}
 	
 	public boolean shutdown() throws RemoteException {
 		for(Enumeration<Transaction> i = transactionHT.elements(); i.hasMoreElements(); ){
     		Transaction tr = i.nextElement();
     		try {
 				abort(tr.getID());
 				lock.UnlockAll(tr.getID());
 			} catch (RemoteException e) {
 				// Should not happen
 				e.printStackTrace();
 			} catch (InvalidTransactionException e) {
 				// Should not happen
 				e.printStackTrace();
 			}
 		}
 		try{
 		rmCar.shutdown();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		rmFlight.shutdown();
 		rmHotel.shutdown();
 		rmCustomer.shutdown();
 		Trace.info("TM::shutdown() succeeded.");
 		return true;
 	}
 	
 	public boolean lives(int id) throws InvalidTransactionException{
 		Transaction tr = readData(id);
 		if(tr == null)
 			throw new InvalidTransactionException();
 		Trace.info("TM::lives(" + id + ") succeeded.");
 		return tr.resetTTL();
 	}
 	
 	
 	private class PrepareThread extends Thread {
 		
 		private RMType rm;
 		private Transaction tr;
 		
 		PrepareThread(RMType tp, Transaction tr){
 			rm = tp;
 			this.tr = tr;
 		}
 
 		public void run(){
 			boolean b;
 			try {
 				switch(rm){
 				case CAR:
 					b = rmCar.prepare(tr.id);
 					if(!tr.isTimedOut())
 						tr.prepared(b, rm);
 					break;
 				case HOTEL:
 					b = rmHotel.prepare(tr.id);
 					if(!tr.isTimedOut())
 						tr.prepared(b, rm);
 					break;
 				case FLIGHT:
 					b = rmFlight.prepare(tr.id);
 					if(!tr.isTimedOut())
 						tr.prepared(b, rm);
 					break;
 				case CUSTOMER:
 					b = rmCustomer.prepare(tr.id);
 					if(!tr.isTimedOut())
 						tr.prepared(b, rm);
 					break;
 				}
 			} catch (TransactionAbortedException e) {
 				tr.markAborted();
 			} catch (RemoteException e){
 				tr.markAborted();
 			} catch (InvalidTransactionException e) {
 				tr.markAborted();
 			}
 		}
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
 
 		private EnlistedRM car;
 		private EnlistedRM hotel;
 		private EnlistedRM flight;
 		private EnlistedRM customer;
 		private boolean timeout = false;
 		private boolean aborted = false;
 		private boolean autocommit = false;
 		private TimeToLive ttl;
 		private Timeout tout;
 		private int id;
 		
 		public Transaction (int id){
 			this.id = id;
 			ttl = new TimeToLive();
 		}
 
 		public Transaction (int id, boolean autocommit){
 			this.autocommit = autocommit;
 			this.id = id;
 			//If autocommit is on, then transaction will not expire
 			if(!autocommit)
 				ttl = new TimeToLive();
 		}
 			
 		public int getID (){
 			return id;
 		}
 		
 		public boolean isAutoCommitting(){
 			return autocommit;
 		}
 		
 		class TimeToLive {
 
 			private Timer timer;
 			private final long life = 120; //in seconds; 2 min.
 
 			TimeToLive() {
 				timer = new Timer();
 				timer.schedule(new RemindTask(), life*1000);
 			}
 
 			class RemindTask extends TimerTask {
 				public void run() {
 					try {
 						abort(id);
 						lock.UnlockAll(id);
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
 			
 			void cancel(){
 				timer.cancel();
 			}
 		}
 
 		//reset does not occur if autocommit since there is nothing to have expire.
 		boolean resetTTL(){
 			if(!autocommit)
 				ttl.reset();
 			return !autocommit;
 		}
 		
 		boolean cancelTTL(){
 			if(!autocommit)
 				ttl.cancel();
 			return true;
 		}
 		
 		
 		/**
 		 * Tells whether a particular RM is enlisted
 		 */
 		public boolean isEnlisted(RMType rm) {
 			switch(rm){
 			case CAR:
 				return car != null;
 			case FLIGHT:
 				return flight != null;
 			case HOTEL:
 				return hotel != null;
 			case CUSTOMER:
 				return customer != null;
 			}
 			return false;
 		}
 		
 		/**
 		 * Adds RMCar to the list of enlisted RMs
 		 */
 		public void enlist(RMType rm) {
 			switch(rm){
 			case CAR:
 				car = new EnlistedRM();
 				break;
 			case FLIGHT:
 				flight = new EnlistedRM();
 				break;
 			case HOTEL:
 				hotel = new EnlistedRM();
 				break;
 			case CUSTOMER:
 				customer = new EnlistedRM();
 				break;
 			}
 		}
 		
 		public void prepared(boolean t, RMType rm){
 			if(t){
 				acceptsCommit(rm);
 			} else {
 				refusesCommit(rm);
 			}
 		}
 
 		private void acceptsCommit(RMType rm) {
 			switch(rm){
 			case CAR:
 				if(car != null)
 					car.accepted();
 				break;
 			case FLIGHT:
 				if(flight != null)
 					flight.accepted();
 				break;
 			case HOTEL:
 				if(hotel != null)
 					hotel.accepted();
 				break;
 			case CUSTOMER:
 				if(customer != null)
 					customer.accepted();
 				break;
 			}
 		}
 		
 		private void refusesCommit(RMType rm) {
 			switch(rm){
 			case CAR:
 				if(car != null)
 					car.refused();
 				break;
 			case FLIGHT:
 				if(flight != null)
 					flight.refused();
 				break;
 			case HOTEL:
 				if(hotel != null)
 					hotel.refused();
 				break;
 			case CUSTOMER:
 				if(customer != null)
 					customer.refused();
 				break;
 			}
 		}
 		
 		public boolean isTimedOut() {
 			return timeout;
 		}
 
 		public void timeIsOut() {
 			this.timeout = true;
 		}
 		
 		public void timeoutReset(){
 			this.timeout = false;
 		}
 		
 		public void timeoutStart() {
 			tout = new Timeout();
 		}
 		
 		class Timeout {
 
 			private Timer timer;
 			private final long life = 60; //in seconds; 1 min.
 
 			Timeout() {
 				timer = new Timer();
 				timer.schedule(new RemindTask(), life*1000);
 			}
 
 			class RemindTask extends TimerTask {
 				public void run() {
 					timeIsOut();
 					timer.cancel(); //Terminate the timer thread
 					tout = null; //sends Timeout object to garbage collection
 				}
 			}	
 		}
 
 		public void markAborted() {
 			aborted = true;
 		}
 		
 		public boolean isReady() throws TransactionAbortedException {
 			if(aborted)
 				throw new TransactionAbortedException(id);
 			if (flight != null)
				if(flight.hasReplied())
 					return false;
 			if (car != null)
				if(car.hasReplied())
 					return false;
 			if (hotel != null)
				if(hotel.hasReplied())
 					return false;
 			if (customer != null)
				if(customer.hasReplied())
 					return false;
 			return true;
 			
 		}
 		
 		/**
 		 * Will simply reset the enlisted RMs.
 		 * Is only used for autocommit transactions.
 		 */
 		public void commit() {
 			car = null;
 			flight = null;
 			hotel = null;
 			customer = null;
 			aborted = false;
 		}
 		
 		private class EnlistedRM {
 			
 			private boolean repliedPrepared = false;
 			private boolean acceptedPrepared = false;
 			
 			public void accepted(){
 				repliedPrepared = true;
 				acceptedPrepared = true;
 			}
 			
 			public void refused(){
 				repliedPrepared = true;
 				acceptedPrepared = false;
 			}
 			
 			public boolean hasReplied(){
 				return repliedPrepared;
 			}
 			
 			public boolean hasAccepted(){
 				return acceptedPrepared;
 			}
 		}
 	}	
 }
