 package dblockcache;
 
 import common.Constants;
 import common.Constants.DiskOperationType;
 import virtualdisk.VirtualDisk;
 
 
 public class DBuffer
 {
     private int myBlockId;
     private VirtualDisk myVirtualDisk;
     private int myIoOperations;
     private boolean myValid, myClean, myHeld;
     private byte[] myBuffer;
 
 
     public DBuffer(int blockId, VirtualDisk virtualDisk)
     {
         myBlockId = blockId;
         myVirtualDisk = virtualDisk;
         myIoOperations = 0;
         myValid = false;
         myClean = true;
         myHeld = false;
         myBuffer = new byte[Constants.BLOCK_SIZE];
     }
 
 	/* Start an asynchronous fetch of associated block from the volume */
 	public synchronized void startFetch()
     {
         myIoOperations++;
         myVirtualDisk.startRequest(this, DiskOperationType.READ);
     }
 
 	/* Start an asynchronous write of buffer contents to block on volume */
 	public synchronized void startPush()
     {
         myIoOperations++;
         myVirtualDisk.startRequest(this, DiskOperationType.WRITE);
     }
 
 	/* Check whether the buffer has valid data */ 
 	public boolean checkValid()
     {
         return myValid;
     }
 
 	/* Wait until the buffer is free */
 	public synchronized void waitValid()
     {
         while (!myValid) {
             try {
                 wait();
             }
             catch (InterruptedException e) {}
         }
     }
 
 	/* Check whether the buffer is dirty, i.e., has modified data to be written back */
 	public boolean checkClean()
     {
         return myClean;
     }
 
 	/* Wait until the buffer is clean, i.e., until a push operation completes */
 	public synchronized void waitClean()
     {
         while (!myClean) {
             try {
                 wait();
             }
             catch (InterruptedException e) {}
         }
     }
 
 	/* Check if buffer is evictable: not evictable if I/O in progress, or buffer is held */
 	public boolean isBusy()
     {
         return myIoOperations > 0;
     }
 
 	/*
 	 * reads into the buffer[] array from the contents of the DBuffer. Check
 	 * first that the DBuffer has a valid copy of the data! startOffset and
 	 * count are for the buffer array, not the DBuffer. Upon an error, it should
 	 * return -1, otherwise return number of bytes read.
 	 */
 	public synchronized int read(byte[] buffer, int startOffset, int count)
     {
         if (!checkValid())
             return -1;
        for (int i = 0; i < count; i++) {
            buffer[i] = myBuffer[startOffset + i];
         }
        return count;
     }
 
 	/*
 	 * writes into the DBuffer from the contents of buffer[] array. startOffset
 	 * and count are for the buffer array, not the DBuffer. Mark buffer dirty!
 	 * Upon an error, it should return -1, otherwise return number of bytes
 	 * written.
 	 */
 	public synchronized int write(byte[] buffer, int startOffset, int count)
     {
         myClean = false;
        for (int i = 0; i < count; i++) {
            myBuffer[startOffset + i] = buffer[i];
         }
        return count;
     }
 
 	/* An upcall from VirtualDisk layer to inform the completion of an IO operation */
 	public synchronized void ioComplete()
     {
         myIoOperations--;
         notifyAll();
     }
 
 	/* An upcall from VirtualDisk layer to fetch the blockID associated with a startRequest operation */
 	public int getBlockID()
     {
         return myBlockId;
     }
 
 	/* An upcall from VirtualDisk layer to fetch the buffer associated with DBuffer object*/
 	public byte[] getBuffer()
     {
         return myBuffer;
     }
 
     public synchronized void acquire()
     {
         while (myHeld) {
             try {
                 wait();
             }
             catch (InterruptedException e) {}
         }
         myHeld = true;
     }
 
     public boolean isHeld()
     {
         return myHeld;
     }
 
     public synchronized void release()
     {
         myHeld = false;
         notify();
     }
 }
