 /*
  * DiskUnit.java
  *
  * Quick tests of Disk
  *
  * You must follow the coding standards distributed
  * on the class web page.
  *
  * (C) 2007,2010, 2011 Mike Dahlin
  *
  */
 import java.util.concurrent.locks.Condition;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class DiskUnit implements DiskCallback{
 
 	private static final int NUM = 100;
 
 	boolean anyHaveError;
 	private static final int INPROGRESS = 234;
 	private static final int DONE_OK = 9234;
 	private static final int DONE_ERROR = 22842;
 
 	private static final boolean verbose = true;
 	private static final boolean vverbose = false;
 
 
 	int status[];
 	int doneCount;
 
 	int barrierPlace = -1;
 
 	SimpleLock lock;
 	Condition resultAvailable;
 
 	//-------------------------------------------------------
 	// main() -- java DiskUnit to run this test
 	//-------------------------------------------------------
 	public static void main(String args[]) throws IllegalArgumentException, IOException
 	{
 		/*testWrites(true, (float)0.0, -1);
     System.out.println("Wrote 0 to all");
     testWrites(true, (float)0.0, 50);
     System.out.println("Wrote 0 to all");
     testWrites(false, (float)0.0, -1);
     System.out.println("Wrote data to all");
     testWrites(false, (float)0.0, 50);
     System.out.println("Wrote data to all");
     testReads(true, -1);
     System.out.println("Read data from all");
     testWrites(true, (float)0.0, -1);
     System.out.println("Wrote 0 to all");
     testWrites(true, (float)0.0, 50);
     System.out.println("Wrote 0 to all");
     testWrites(false,  (float)0.1, -1);
     System.out.println("Wrote data to some");
     testReads(false, -1);    
     System.out.println("Read data from some");
     System.exit(0);*/
 		Tester t = new Tester();
 		/*System.out.println("Testing TranID");
 		TransID id = new TransID();
 		id.unit(t);
 		
 		System.out.println("Testing Write");
 		Write w = new Write(0, new byte[0]);
 		w.unit(t);
 		
 		System.out.println("Testing Transaction");
 		Transaction tran = new Transaction();
 		tran.unit(t);
 		
 		System.out.println("Testing ActiveTransactionList");
 		ActiveTransactionList atl = new ActiveTransactionList();
 		atl.unit(t);
 		
 		System.out.println("Testing WrtieBackList");
 		WriteBackList.unit(t);
 		
 		System.out.println("Testing LogStatus");
 		LogStatus.unit(t);
 		
 		System.out.println("Testing CallbackTracker");
 		CallbackTracker.unit(t);
 		
 		System.out.println("Testing ADisk");
 		ADisk.unit(t, true);
 		//for(int i = 0; i < 1000; i++)
 			//ADisk.unit(t, false);
 			 * */
		/*System.out.println("Testing BitMap");
 		BitMap.unit(t);
 		System.out.println("Testing TNode");
 		TNode.unit(t);
 		System.out.println("Testing Internal Node");
		InternalNode.unit(t);*/
 		System.out.println("Testing PTree");
 		PTree.unit(t);
 		
 		System.out.println();
 		t.close();
 		System.exit(0);
 	}
 
 
 	//-------------------------------------------------------
 	// Issue a one write to each of a bunch of disk sectors
 	// Expect all of the writes to have completed properly...
 	//-------------------------------------------------------
 	private static void testWrites(boolean writeZero, float errorPct, int barrierWhere)
 	{
 
 		DiskUnit du;
 		Disk d = null;
 		int ii;
 
 		assert(NUM < Disk.NUM_OF_SECTORS);
 		int ok;
 
 		du = new DiskUnit(barrierWhere);
 		try{
 			d = new Disk(du);
 		}
 		catch(FileNotFoundException fnf){
 			System.out.println("Unable to open disk file");
 			System.exit(-1);
 		}
 		du.doClear();
 		for(ii = 0; ii < NUM; ii++){
 			//
 			// Remember: Do not re-use buffer until
 			// request completes!
 			//
 			byte b[] = new byte[Disk.SECTOR_SIZE];
 			if(writeZero){
 				setBuffer((byte)0, b);
 			}
 			else{
 				setBuffer((byte)ii, b);
 			}
 			try{
 				d.startRequest(Disk.WRITE, ii, ii, b);
 				if(ii == barrierWhere){
 					d.addBarrier();
 				}
 			}
 			catch(Exception e){
 				System.out.println("Unexpected exception in startReq write " 
 						+ e.toString() + " ii=" + ii);
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		}
 
 		if(errorPct > 0.0){
 			d.setFailProb(0, errorPct);
 		}
 
 		if(verbose){
 			System.out.println("Writes started");
 		}
 		ok = du.check(); // All writes done
 		if(verbose){
 			System.out.println("Writes done");
 		}
 		if(ok < NUM && errorPct == 0){
 			System.out.println("Fewer writes than expected " + ok);
 			return;
 		}
 		if(ok == NUM && errorPct > 0){ // Make sure expected num writes nonzero
 			System.out.println("More writes than expected " + ok);
 			return;
 		}
 	}
 
 	//-------------------------------------------------------
 	// Issue a one read to each of a bunch of disk sectors
 	//-------------------------------------------------------
 	private static void testReads(boolean expectAll, int barrierWhere)
 	{
 
 		DiskUnit du;
 		Disk d = null;
 		int ii;
 
 		assert(NUM < Disk.NUM_OF_SECTORS);
 		int ok;
 
 		du = new DiskUnit(barrierWhere);
 		try{
 			d = new Disk(du);
 		}
 		catch(FileNotFoundException fnf){
 			System.out.println("Unable to open disk file");
 			System.exit(-1);
 		}
 
 		du.doClear();
 		for(ii = 0; ii < NUM; ii++){
 			//
 			// Remember: Do not re-use buffer until
 			// request completes!
 			//
 			byte b[] = new byte[Disk.SECTOR_SIZE];
 			setBuffer((byte)0, b);
 			try{
 				d.startRequest(Disk.READ, ii, ii, b);
 				if(ii == barrierWhere){
 					d.addBarrier();
 				}
 			}
 			catch(Exception f){
 				System.out.println("Unexpected exception in startReq read");
 				System.exit(-1);
 			}
 		}
 		if(verbose){
 			System.out.println("Reads started");
 		}
 		ok = du.check(); // All reads done
 		if(verbose){
 			System.out.println("Reads done");
 		}
 		if(expectAll && ok < NUM){
 			System.out.println("Fewer reads than expected " + ok);
 			System.exit(-1);
 			return;
 		}
 		if(!expectAll && ok == NUM){
 			System.out.println("More reads than expected " + ok);
 			return;
 		}
 		return;
 	}
 
 	//-------------------------------------------------------
 	// set Disk.SECTOR_SIZE bytes to specified value
 	//-------------------------------------------------------
 	private static void setBuffer(byte value, byte b[])
 	{
 		int ii;
 		for(ii = 0; ii < Disk.SECTOR_SIZE; ii++){
 			b[ii] = value;
 		}
 		return;
 	}
 
 	//-------------------------------------------------------
 	// DiskUnit
 	//-------------------------------------------------------
 	private DiskUnit(int barrierWhere)
 	{
 		status = new int[NUM];
 		this.lock = new SimpleLock();
 		this.resultAvailable = lock.newCondition();
 		this.barrierPlace = barrierWhere;
 		doClear();
 	}
 
 	//-------------------------------------------------------
 	// clear
 	//-------------------------------------------------------
 	public void doClear()
 	{
 		int ii;
 		try{
 			lock.lock();
 			for(ii = 0; ii < NUM; ii++){
 				status[ii] = INPROGRESS;
 			}
 			anyHaveError = false;
 			doneCount = 0;
 		}
 		finally{
 			lock.unlock();
 		}
 	}
 
 
 
 	//-------------------------------------------------------
 	// check
 	//-------------------------------------------------------
 	public int check()
 	{
 		try{
 			lock.lock();
 			while(!anyHaveError && doneCount < NUM){
 				resultAvailable.awaitUninterruptibly();
 			}
 			return doneCount;
 		}
 		finally{
 			lock.unlock();
 		}
 	}
 
 
 	//-------------------------------------------------------
 	// requesDone -- callback -- check writes for status
 	// and check reads for 
 	//-------------------------------------------------------
 	public void requestDone(DiskResult result)
 	{
 		int sec;
 		try{
 			lock.lock();
 
 			//
 			// For this simple test, we have at most one outstanding request
 			// per sector, so we can use sector number to know which
 			// request this reply finishes.
 			// Could use tag to match requests with replies instead
 			//
 			sec = result.getSectorNum();
 
 			if(result.getOperation() == Disk.WRITE 
 					&& this.barrierPlace >= 0 
 					&& this.barrierPlace < sec){
 				// Assumes all pre-barrier operations were WRITEs
 				int ii;
 				for(ii = 0; ii <= barrierPlace; ii++){
 					if(status[ii] == INPROGRESS){
 						System.out.println("ERROR: Request " + result.getSectorNum() + " barrier error");
 						System.out.println("ERROR:      barrier: " + this.barrierPlace);
 						System.out.println("ERROR:      ii: " + ii);
 						System.out.println("ERROR:      done: " + sec);
 						System.exit(-1);
 					}
 				}
 			}
 			if((result.getStatus() == DiskResult.FAKE_ERROR) || (status[sec] == INPROGRESS)){
 				if(result.getStatus() == DiskResult.OK){
 					if(vverbose){
 						System.out.println("Request " + result.getSectorNum() + " done");
 					}
 					if(checkOK(result)){
 						doneCount++;
 						status[sec] = DONE_OK;
 						if(doneCount == NUM){
 							resultAvailable.signal();
 						}
 						return;
 					}
 					else{
 						//
 						// Expected case if writes did not complete
 						//
 						if(vverbose){
 							System.out.println("Request " + result.getSectorNum() + " missing data");
 						}
 						status[sec] = DONE_ERROR;
 						anyHaveError = true;
 						resultAvailable.signal();
 						return;
 					}
 				}
 				else if(result.getStatus() == DiskResult.REAL_ERROR){
 					System.out.println("UNEXPECTED ERROR: real IO error");
 					System.exit(-1);
 				}
 				else{
 					//
 					// Fake error. Expected case
 					//
 					if(vverbose){
 						System.out.println("Request " + result.getSectorNum() + " fake error");
 					}
 					status[sec] = DONE_ERROR;
 					anyHaveError = true;
 					resultAvailable.signal();
 					return;
 				}
 			}
 			System.out.println("UNEXPECTED ERROR: multiple returns for same entry " + result.toString());
 			System.exit(-1);
 			return;
 		}
 		finally{
 			lock.unlock();
 		}
 	}
 
 
 	//-------------------------------------------------------
 	// check -- check for expected data in buffer if this is a read
 	//-------------------------------------------------------
 	private static boolean checkOK(DiskResult r)
 	{
 		int ii;
 		int secNum;
 		byte b[];
 
 		if(r.getOperation() == Disk.WRITE){
 			return true;
 		}
 		secNum = r.getSectorNum();
 		b = r.getBuf();
 		for(ii = 0; ii < Disk.SECTOR_SIZE; ii++){
 			if(b[ii] != (byte)secNum){
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 }
