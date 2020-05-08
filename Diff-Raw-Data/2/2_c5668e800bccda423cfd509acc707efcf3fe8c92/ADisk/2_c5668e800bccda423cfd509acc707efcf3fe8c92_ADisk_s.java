 /*
  * ADiskI.java
  *
  * Interface to ADisk
  *
  * You must follow the coding standards distributed
  * on the class web page.
  *
  * (C) 2007, 2010 Mike Dahlin
  *
  */
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.locks.Condition;
 
 public class ADisk implements DiskCallback{
 
 	//-------------------------------------------------------
 	// The size of the redo log in sectors
 	//-------------------------------------------------------
 	public static final int REDO_LOG_SECTORS = 1024;
	private static final int PTR_SECTOR = ADisk.REDO_LOG_SECTORS + 1;
 	private Integer logHead = 0;
 	private Integer logTail = 0;
 	private Disk disk;
 	private SimpleLock lock;
 	
 	private Condition diskDone;
 	private Condition commit;
 //	private Condition writeQueue;
 	private boolean commitInProgress = false;
 	//This holds the only references to Transactions, allowing controlled garbage collection
 	private HashMap<TransID, Transaction> transactions;
 	private ArrayList<Transaction> writeBackQueue;
 	
 	private Thread writerThread;
 	
 	private int lastCompletedAction;
 
 
 	//-------------------------------------------------------
 	//
 	// Allocate an ADisk that stores its data using
 	// a Disk.
 	//
 	// If format is true, wipe the current disk
 	// and initialize data structures for an empty 
 	// disk.
 	//
 	// Otherwise, initialize internal state, read the log, 
 	// redo any committed transactions, and reset the log.
 	//
 	//-------------------------------------------------------
 	public ADisk(boolean format) {
 		this.lock = new SimpleLock();
 		this.diskDone = lock.newCondition();
 		this.commit = lock.newCondition();
 		this.writeBackQueue = new ArrayList<Transaction>();
 		this.transactions = new HashMap<TransID, Transaction>();
 		this.writerThread = new Thread(new WriteBack());
 		this.writerThread.start();
 		try {
 			this.disk = new Disk(this, 0);
 			if (format) {
 				File hdd = new File("DISK.dat");
 				hdd.delete();
 				this.disk = new Disk(this, 0);
 			}
 			else {
 				this.readLog();
 			}
 			this.writePtrs();
 		}catch (FileNotFoundException e) {
 			System.out.println(e.toString());
 			System.exit(-1);
 		}
 	}
 
 
 
 
 
 
 
 	//-------------------------------------------------------
 	//
 	// Return the total number of data sectors that
 	// can be used *not including space reseved for
 	// the log or other data sructures*. This
 	// number will be smaller than Disk.NUM_OF_SECTORS.
 	//
 	//-------------------------------------------------------
 	public int getNSectors()
 	{
 		return Disk.NUM_OF_SECTORS - ADisk.REDO_LOG_SECTORS - 1;  // Can change if we add other data structures
 	} 
 
 	//-------------------------------------------------------
 	//
 	// Begin a new transaction and return a transaction ID
 	//
 	//-------------------------------------------------------
 	public TransID beginTransaction()
 	{
 		try {
 			lock.lock();
 			TransID tid = new TransID();
 			Transaction trans = new Transaction();
 			Transaction tmp;
 			tmp = this.transactions.put(tid, trans);
 			assert(tmp == null);
 			assert(this.isActive(tid));
 			return tid;
 		}finally {
 			lock.unlock();
 		}
 	}
 
 	//-------------------------------------------------------
 	//
 	// First issue writes to put all of the transaction's
 	// writes in the log.
 	//
 	// Then wait until all of those writes writes are 
 	// safely on disk (in the log.)
 	//
 	// Then, mark the log to indicate that the specified
 	// transaction has been committed. 
 	//
 	// Then wait until the "commit" is safely on disk
 	// (in the log).
 	//
 	// Then take some action to make sure that eventually
 	// the updates in the log make it to their final
 	// location on disk. Do not wait for these writes
 	// to occur. These writes should be asynchronous.
 	//
 	// Note: You must ensure that (a) all writes in
 	// the transaction are in the log *before* the
 	// commit record is in the log and (b) the commit
 	// record is in the log before this method returns.
 	//
 	// Throws 
 	// IOException if the disk fails to complete
 	// the commit or the log is full.
 	//
 	// IllegalArgumentException if tid does not refer
 	// to an active transaction.
 	// 
 	//-------------------------------------------------------
 	public void commitTransaction(TransID tid) throws IOException, IllegalArgumentException {
 		try {
 			lock.lock();
 			while(this.commitInProgress)
 				this.commit.awaitUninterruptibly();
 			this.commitInProgress = true;
 			Transaction t = this.transactions.get(tid);
 			if (t == null)
 				throw new IllegalArgumentException();
 			
 			if (t.size() >= ADisk.REDO_LOG_SECTORS)
 				throw new IOException();
 			int tmp = logHead + t.size();
 			if (logHead < logTail && tmp >= logTail)
 				throw new IOException();
 			if (logHead > logTail && tmp >= ADisk.REDO_LOG_SECTORS && tmp % ADisk.REDO_LOG_SECTORS >= logTail)
 				throw new IOException();
 					
 			for (byte[] b : t.getSectors()) {
 				this.aTrans(logHead, b, Disk.WRITE);
 				logHead = logHead + 1 % Disk.ADISK_REDO_LOG_SECTORS;
 			}
 			this.writeBackQueue.add(t);
 			this.commitInProgress = false;
 			this.commit.signal();
 			this.transactions.put(tid, null);  // Committed transactions are no longer active
 			return;
 		}finally {
 			lock.unlock();
 		}
 	}
 
 
 
 	//-------------------------------------------------------
 	//
 	// Free up the resources for this transaction without
 	// committing any of the writes.
 	//
 	// Throws 
 	// IllegalArgumentException if tid does not refer
 	// to an active transaction.
 	// 
 	//-------------------------------------------------------
 	public void abortTransaction(TransID tid) throws IllegalArgumentException {
 		try {
 			lock.lock();
 			if (this.transactions.put(tid, null) == null)
 				throw new IllegalArgumentException();
 		}finally {
 			lock.unlock();
 		}
 	}
 
 
 	//-------------------------------------------------------
 	//
 	// Read the disk sector numbered sectorNum and place
 	// the result in buffer. Note: the result of a read of a
 	// sector must reflect the results of all previously
 	// committed writes as well as any uncommitted writes
 	// from the transaction tid. The read must not
 	// reflect any writes from other active transactions
 	// or writes from aborted transactions.
 	//
 	// Throws 
 	// IOException if the disk fails to complete
 	// the read.
 	//
 	// IllegalArgumentException if tid does not refer
 	// to an active transaction or buffer is too small
 	// to hold a sector.
 	// 
 	// IndexOutOfBoundsException if sectorNum is not
 	// a valid sector number
 	//
 	//-------------------------------------------------------
 	public void readSector(TransID tid, int sectorNum, byte buffer[])
 	throws IOException, IllegalArgumentException, 
 	IndexOutOfBoundsException
 	{
 		Sector result = new Sector();
 		try {
 			lock.lock();
 			if (buffer.length < Disk.SECTOR_SIZE)
 				throw new IllegalArgumentException();
 			if (sectorNum < ADisk.REDO_LOG_SECTORS || sectorNum >= Disk.NUM_OF_SECTORS)
 				throw new IndexOutOfBoundsException();
 			if (!this.isActive(tid))
 				throw new IllegalArgumentException();
 
 			// Check the current transaction
 			Transaction t = this.transactions.get(tid);
 			boolean found = false;
 			for (Write w : t)
 				if (w.sectorNum == sectorNum) {
 					result.update(w.buffer);
 					found = true;
 				}
 			if(found){
 				System.out.println("Found in Transaction");  //TODO: remove
 				return;
 			}
 			//Check committed but not written writes.
 			for (Transaction trans : this.writeBackQueue)
 				for (Write w : trans)
 					if (w.sectorNum == sectorNum) {
 						result.update(w.buffer);
 						found = true;
 					}
 			if(found){
 				System.out.println("Found in Writeback"); //TODO: remove
 				return;
 			}
 			this.aTrans(sectorNum, result.array, Disk.READ);
 			System.out.println("Found on Disk");  //TODO: remove
 			return;
 		}finally {
 			result.fill(buffer);
 			lock.unlock();
 		}
 	}
 
 	//-------------------------------------------------------
 	//
 	// Buffer the specified update as part of the in-memory
 	// state of the specified transaction. Don't write
 	// anything to disk yet.
 	//  
 	// Concurrency: The final value of a sector
 	// must be the value written by the transaction that
 	// commits the latest.
 	//
 	// Throws 
 	// IllegalArgumentException if tid does not refer
 	// to an active transaction or buffer is too small
 	// to hold a sector.
 	// 
 	// IndexOutOfBoundsException if sectorNum is not
 	// a valid sector number
 	//
 	//-------------------------------------------------------
 	public void writeSector(TransID tid, int sectorNum, byte buffer[])
 	throws IllegalArgumentException, 
 	IndexOutOfBoundsException 
 	{
 		try {
 			lock.lock();
 			if(!this.isActive(tid)) 
 				throw new IllegalArgumentException();
 			
 			this.transactions.get(tid).add(sectorNum, buffer);
 		}finally {
 			lock.unlock();
 		}
 	}
 
 
 	public void requestDone(DiskResult r) {
 		try{
 			lock.lock();
 			this.lastCompletedAction = r.getTag();
 			this.diskDone.signalAll();
 		}finally {
 			lock.unlock();
 		}
 	}
 	// Return first head of writeback queue
 	// Uses proper locks.
 	public Transaction queueFront() {
 		try{
 			lock.lock();
 			if (this.writeBackQueue.isEmpty())
 				return null;
 			return this.writeBackQueue.get(0);
 		}finally {
 			lock.unlock();
 		}
 	}
 	
 	// Remove and return first head of writeback queue
 	// Uses proper locks.
 	public Transaction queuePop() {
 		try{
 			lock.lock();
 			if (this.writeBackQueue.isEmpty())
 				return null;
 			return this.writeBackQueue.remove(0);
 		}finally {
 			lock.unlock();
 		}
 	}
 	
 	//Read the log and shove any unfinished transactions into the write back queue.
 	@SuppressWarnings("unchecked")
 	private void readLog() {
 		try{
 			lock.lock();
 			this.readPtrs();
 			TransID tid;
 			while(logTail != logHead){
 				tid = this.beginTransaction();
 				Sector meta = new Sector();
 				this.aTrans(logTail, meta.array, Disk.READ);
 				ByteArrayInputStream in = new ByteArrayInputStream(meta.array);
 				ObjectInputStream ois = new ObjectInputStream(in);
 				ArrayList<Integer> secList  = (ArrayList<Integer>)ois.readObject();  //Pretty sure this works
 				Sector buff = new Sector();
 				for (int i = 0; i < secList.size(); i++){
 					this.aTrans(logTail + i + 1, buff.array, Disk.READ);
 					this.writeSector(tid, secList.get(i), buff.array);
 				}
 				this.aTrans(logTail + secList.size() + 1, buff.array, Disk.READ);
 				if (!buff.equals(Transaction.COMMIT)) {
 					this.abortTransaction(tid);
 					this.logHead = this.logTail;  //Will end loop and update log.
 				}
 				else {
 					this.logTail += secList.size() + 2;
 					this.writeBackQueue.add(this.transactions.get(tid));
 				}
 			}
 			this.writePtrs();
 		} catch (IllegalArgumentException e) {
 			System.out.println("Bad Log Pointer");
 			System.exit(-1);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			// Disk Error
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			System.out.println("Bad Log Pointer");
 			System.exit(-1);
 		}finally {
 			lock.unlock();
 		}
 	}
 	
 	//Write the pointers to disk.
 	private void writePtrs() {
 		try {
 			lock.lock();
 			ByteArrayOutputStream buff = new ByteArrayOutputStream(Disk.SECTOR_SIZE);
 			ObjectOutputStream oos = new ObjectOutputStream(buff);
 			oos.writeObject(logTail);
 			oos.writeObject(logHead);
 			
 			Sector ptrsector = new Sector(buff.toByteArray());
 			
 			this.aTrans(ADisk.PTR_SECTOR, ptrsector.array, Disk.WRITE);
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			// Disk Error
 			e.printStackTrace();
 		}finally {
 			lock.unlock();
 		}
 		
 	}
 	
 	//Read the pointers off of disk
 	private void readPtrs() {
 		try {
 			lock.lock();
 			Sector buff = new Sector();
 			this.aTrans(ADisk.PTR_SECTOR, buff.array, Disk.READ);
 			ByteArrayInputStream in = new ByteArrayInputStream(buff.array);
 			ObjectInputStream ois = new ObjectInputStream(in);
 			this.logTail = (Integer)ois.readObject();
 			this.logHead = (Integer)ois.readObject();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			// Disk Error
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			System.out.println("Bad Log Pointer");
 			System.exit(-1);
 		}finally {
 			lock.unlock();
 		}
 	}
 	
 	private static int actiontag = Integer.MIN_VALUE;
 	private static int actionTag() {
 		if (actiontag == -1) // prevent 0 from being a tag.
 			actiontag++;
 		if (actiontag == Integer.MAX_VALUE-1) //reserve max int.
 			actiontag++;
 		return actiontag++;  //Assuming that we won't cycle through all ints and still have active requests
 	}
 	
 	//See if the given tid is an active transaction
 	public boolean isActive(TransID tid) {
 		try {
 			lock.lock();
 			if (this.transactions.get(tid) == null)
 				return false;
 			return true;
 		}finally {
 			lock.unlock();
 		}
 	}
 	
 	//Atomic transaction.  Start a disk action, then wait for it to complete, then return.
 	private void aTrans(int sectorNum, byte[] buffer, int type) throws IllegalArgumentException, IOException {
 		try {
 			lock.lock();
 			assert (type == Disk.READ || type == Disk.WRITE);
 			int tag = ADisk.actionTag();
 			this.disk.startRequest(type, tag, sectorNum, buffer);
 			while(this.lastCompletedAction != tag)
 				this.diskDone.awaitUninterruptibly();
 			return;
 		}finally {
 			lock.unlock();
 		}
 	}
 	
 	//A runnable object that infinitely loops in its own thread, consuming the write-back queue.
 	class WriteBack implements Runnable {
 		@Override
 		public void run() {
 			Transaction t;
 			while (true) {
 				while ((t = queueFront()) != null) {
 					for (Write w : t)
 						try {
 							aTrans(w.sectorNum, w.buffer, Disk.WRITE);
 							queuePop();
 						} catch (IllegalArgumentException e) {
 							System.out.println("Bad Log Pointer");
 							System.exit(-1);
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							// Disk fail.
 							e.printStackTrace();
 						}
 					logTail += t.size();
 					writePtrs();
 				}
 						
 			}
 		}
 	}
 	
 }
