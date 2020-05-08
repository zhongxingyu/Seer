 package os;
 
 import java.io.*;
 import java.util.concurrent.locks.*;
 
 /**
  * os/Pipe.java
  * <br><br>
  * This class is the glue between files / shells and or jobs. Some methods may 
  * be blockable. It makes some synchronization and prevents data corruption.
  * 
  * <li>Every shell has to provide std IN/OUT for jobs itself.
  * 
  * <li>Two jobs pushing in ONE pipe is possible now: for this use PermPipe.
  * 
  * <li>Jobs now provides their PID, so the reciever can tell what is what.
  * 
  * @author Pavel Čurda
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška  radekp25@students.zcu.cz
  * 
  * @version 1.5
  */
 public class Pipe {
 	
 	/** Input of the pipe to file. */
 	protected BufferedReader fileIn;
 	/** Output of the pipe to file. */
 	protected BufferedWriter fileOut;
 	
 	/** Buffer of the pipe when from Job to Job. */
 	private char[] buffer;
 	/** Index of the buffer */
 	private int buffIndex;
 	/** PID of the last job which pushed data IN */
 	private Integer pid;
 	
 	/** Size of buffer. */
 	public static final int BUF_SIZE = 13;
 	
 	/** Synchronization of the pipe */
 	protected ReentrantLock lock;
 	protected Condition lFull;
 	protected Condition lEmpty;
 	
 	/**
 	 * Type of the pipe:
 	 * <br> 00 from job to job -> LOCKS ready
 	 * <br> 10 from file to job
 	 * <br> 01 from job to file
 	 * <br> -1 broken pipe
 	 */
 	protected int type = 0;
 	
 	/**
 	 * Status of the pipe:
 	 * <br> 1 is alive
 	 * <br> 0 closed
 	 */
 	protected int status = 1;
 	
 	
 	
 	/**
 	 * If the input is null, the pipe will initialize synchronization locks and 
 	 * will be PASSIVELY waiting for ANY JOB to give its INput. Same for the
 	 * OUTput.
 	 * <br><br>
 	 * <b>You can't join two files together!</b>
 	 * 
 	 * @param input You will give data to pipe.
 	 * @param output Where the data will flow from the pipe.
 	 */
 	public Pipe(FileReader input, FileWriter output) {
 
 		this.lock = new ReentrantLock();
 		this.lEmpty = this.lock.newCondition();
 		this.lFull = this.lock.newCondition();
 		
 		
 		if ((input != null) && (output != null)){
 			// from file to file? HERESY
 			this.type = -1;
 			this.status = 0;
 			System.err.println("TRIED TO CREATE PIPE: file to file!");
 			return;
 		}
 		
 		if ((input != null) && (output == null)){
 			// from file to JOB
 			this.type = 10;
 			
 			this.fileIn = new BufferedReader(input);
 			this.fileOut = null;
 
 			return;
 		}
 		
 		if ((input == null) && (output != null)){
 			// from JOB to file
 			this.type = 01;
 			
 			this.fileIn = null;
 			this.fileOut = new BufferedWriter(output);
 
 			return;
 		}
 		
 		if ((input == null) && (output == null)){
 			// from JOB to JOB
 			this.type = 00;
 			
 			this.fileIn = null;
 			this.fileOut = null;
 			
 			
 			
 			this.buffer = new char [BUF_SIZE];
 			this.buffIndex = 0;
 			
 			return;
 	
 		}
 		
 		System.err.println("ERROR 001 in Pipe.java");
 	}
 	
 	
 	
 	/**
 	 * Push your data to mighty Pipe! You can push data of any length.
 	 * 
 	 * @param dataIn you want to push in.
 	 * @param off offset, where to start.
 	 * @param len how many data to write.
 	 * @param PID of the process which writes to it.
 	 * 
 	 * @throws IOException many reasons why it can fail.
 	 * <b>Can throw IOException: Pipe closed</b>
 	 * 
 	 * @throws InterruptedException SIG_KILL came while thread was waiting for
 	 * 	empty buffer to write in.		
 	 */
 	public void pushData(char [] dataIn, int off, int len, Integer PID) 
 			throws IOException, InterruptedException{
 		
 		if(this.status == 0){
 			throw new IOException("Pipe closed!");
 		}
 		
 		if(this.type == 01){
 			/** writing to file */
 			this.fileOut.write(dataIn, off, len);
 			return;
 		} 
 		
 		/** writing to buffer */
 
 		int kolikrat = (len / BUF_SIZE);
 		int pom = off;
 		
 		
 			
 		
 		
 		for (int i = 0; i < kolikrat; i++){
 			
 			copyArrayPush(dataIn, this.buffer, pom, pom + BUF_SIZE, PID);
 			pom += BUF_SIZE;
 		}
 		
 		int zbytek = len - (kolikrat * BUF_SIZE);
 		if (zbytek != 0){
 			copyArrayPush(dataIn, this.buffer, pom, pom + zbytek, PID);
 		}
 		
 		
 			
 		return;
 	}
 	
 	/**
 	 * <li>Get some data from this Pipe!
 	 * 
 	 * @param destination where the data will be copied to. 
 	 * <b>Size has to be BUF_SIZE or bigger.</b>
 	 * 
 	 * @return Number of successfuly written data to your destination. <b>-1: EOF.</b>
 	 * 
 	 * @throws IOException Many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while <b>your thread</b> was 
 	 * waiting for full buffer to read from.
 	 */
 	public int getData(char [] destination) 
 			throws IOException, InterruptedException{
 		
 		return(this.getData(destination, null, null));
 		
 	}
 	
 	/**
 	 * <li>Get some data from this Pipe!
 	 * 
 	 * <li>And PID of it's sender (may be null).
 	 * 
 	 * @param destination where the data will be copied to. 
 	 * <b>Size has to be BUF_SIZE or bigger.</b>
 	 * @param PID destination where the PID of the sender will be written (pointer).
 	 * 
 	 * @return Number of successfuly written data to your destination. <b>-1: EOF.</b>
 	 * 
 	 * @throws IOException Many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while <b>your thread</b> was 
 	 * waiting for full buffer to read from.
 	 */
 	public int getData(char [] destination, Integer [] PID) 
 			throws IOException, InterruptedException{
 		
 		return(this.getData(destination, PID, null));
 
 	}
 	
 	
 
 	/**
 	 * <li>Get some data from this Pipe!
 	 * 
 	 * <li>When you got data from this pipe, it will lock your lock1.
 	 * 
 	 * @param destination where the data will be copied to. 
 	 * <b>Size has to be BUF_SIZE or bigger.</b>
 	 * @param lock1 to be locked when you got the some data.
 	 * 
 	 * @return Number of successfuly written data to your destination. <b>-1: EOF.</b>
 	 * 
 	 * @throws IOException Many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while <b>your thread</b> was 
 	 * waiting for full buffer to read from.
 	 */
 	public int getData(char [] destination, Lock lock1) 
 			throws IOException, InterruptedException{
 		
 		return(this.getData(destination, null, lock1));
 		
 	}
 	
 	/**
 	 * <li>Get some data from this Pipe!
 	 * 
 	 * <li>And PID of it's sender (may be null).
 	 * 
 	 * <li>When you got data from this pipe, it will lock your lock1.
 	 * 
 	 * @param destination where the data will be copied to. 
 	 * <b>Size has to be BUF_SIZE or bigger.</b>
 	 * @param PID destination where the PID of the sender will be written (pointer).
 	 * @param lock1 to be locked when you got the some data.
 	 * 
 	 * @return Number of successfuly written data to your destination. <b>-1: EOF.</b>
 	 * 
 	 * @throws IOException Many reasons why it can fail.
 	 * @throws InterruptedException SIG_KILL came while <b>your thread</b> was 
 	 * waiting for full buffer to read from.
 	 */
 	public int getData(char [] destination, Integer [] PID, Lock lock1) 
 			throws IOException, InterruptedException{
 		
 		int index;
 		
 		if(this.type == 10){
 			/** reading file */
 			index = this.fileIn.read(destination);
 			return (index);
 		}
 			
 
 		/** reading from buffer */
 		this.lock.lockInterruptibly();
 		
 			
 		while(this.buffIndex == 0){
 			
 			if(this.status == 0){
 				/** buffer is empty and pipe is closed */
 				System.err.println("waiting getData at closed pipe.");
 				this.lock.unlock();
 				return(-1);
 			}
 			
 			this.lEmpty.await();
 			
 		}
 		
 		index = this.buffIndex;
 		
 		/** the buffer has some data */
 		copyArray(this.buffer, destination, index);
 		this.buffIndex = 0;
 		
 		if(PID != null){
 			PID[0] = this.pid;
 		}
 		
 		if(lock1 != null){
 			if(Thread.currentThread().isInterrupted()){
 				System.err.println("INTERUPT - dont lock;");
 			} else {
 				//Super locking when we got some data
 				lock1.lockInterruptibly();
 			}
 
 		}
 			
 		
 		this.lFull.signalAll();
 		this.lock.unlock();
 		return(index);
 		
 	}
 	
 	
 	
 	/**
 	 * Flushes any buffer, close streams and unlocks any threads waiting here.
 	 * <br><br>
 	 * It is possible to "close" one pipe multiple times. It is not cool, but
 	 * still possible. Nothing wrong should happen if you do so.
 	 * 
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 */
 	public void close() throws IOException, InterruptedException{
 		
 		this.lock.lockInterruptibly();
 		
 		
 		if (this.status == 0){
 			//FIXME this is not cool.
 			//throw new IOException("Closing one PIPE twice or more times.");
 			
 		} else if (this.type == 01){
 			
 			this.fileOut.close();
 			this.fileOut = null;
 
 			
 		} else if (this.type == 10){
 			
 			this.fileIn.close();
 			this.fileIn = null;
 
 		} else if (this.type == 00){
 			
 			this.lEmpty.signalAll();
 			this.lFull.signalAll();
 			
 		}
 		
 		this.status = 0;
 		this.lock.unlock();
 		return;
 		
 	}
 	
 
 	
 	/**
 	 * Copy one array to another. SAME LENGTH. from 0 to index.
 	 * 
 	 * @param from source
 	 * @param to target
 	 * @param index from 0 to index
 	 */
 	private static void copyArray(char [] from, char [] to, int index){
 		
 		for (int i = 0; i < index; i++){
 			to[i] = from[i];
 		}
 		return;
 	}
 	
 
 
 	/**
 	 * Makes copy of array FROM, and place the copy to array TO.
 	 * It starts on index start and stops on number STOP.
 	 * <br><br>
 	 * to.length >= start - stop
 	 * <br><br>
 	 * Make sure that you wont call this method if start - stop = 0!
 	 * 
 	 * @param from source
 	 * @param to target
 	 * @param start index (source)
 	 * @param stop index (source)
 	 * @param PID of the sender.
 	 * @throws InterruptedException 
 	 * @throws IOException 
 	 * @throws PipeException 
 	 */
 	private void copyArrayPush(char [] from, char [] to, int start, int stop, Integer PID) 
 			throws InterruptedException, IOException{
 
 		this.lock.lockInterruptibly();
 		try{
 
 			while (this.buffIndex != 0){
 				this.lFull.await();
 
 				if (this.status == 0){
 					/** 
 					 * We cant push any data anymore, go away.
 					 * (Other side of pipe is dead)
 					 */
 					System.err.println("PUSHING IN CLOSED PIPE!");
 					throw new IOException("The PIPE is closed.");
 				}
 			}
 
 			/** the buffer is free */
 			this.pid = PID;
 			int index = 0;
 
 			for (int i = start; i < stop; i++){
 
 				to[index] = from[i];
 				index++;
 			}
 
 			this.buffIndex = index;
 
 		} finally{
 
 			this.lEmpty.signalAll();
 			this.lock.unlock();
 		}
 		return;
 	}
 	
 	/**
 	 * Blocking operation: Waits until this pipe will be empy: If you push some
 	 * data in, call this method to be sure that the reciever has already got 
 	 * the data.
 	 * <br><br>
 	 * Waits only on opened pipes between two Jobs.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void waitForEmpty() throws InterruptedException{
 		
 		this.lock.lockInterruptibly();
 		try{
 			
 			while (this.buffIndex != 0){
 				this.lFull.await();
 			}
 			
 		} finally {
 			// The pipe has empthy buffer.
 			this.lock.unlock();
 		}
 		
 		return;
 	}
 	
 	@Override
 	public String toString() {
 		
 		String lo = "LOCKED";
 		
 		int waitE = -1;
 		int waitF = -1;
 		
 	
 		
 		if(this.lock.tryLock()){
 			lo = "unlocked";
 			
 			waitE = this.lock.getWaitQueueLength(this.lEmpty);
 			waitF = this.lock.getWaitQueueLength(this.lFull);
 			
 			this.lock.unlock();
 		}
 		
 		String text = this.getClass().getName() + ", status=" + this.status + 
 				", type=" + this.type + ", buff:" + this.buffIndex + 
 				", " + lo + ", held:" + this.lock.isLocked()
 				+", waitF:" + waitF + ", waitE:" + waitE;
 		
 		
 		return (text);
 	}
 	
 
 
 	
 }
