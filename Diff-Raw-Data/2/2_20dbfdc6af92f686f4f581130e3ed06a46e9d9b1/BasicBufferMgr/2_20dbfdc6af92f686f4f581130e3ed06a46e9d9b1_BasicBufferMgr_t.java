 package simpledb.buffer;
 
 import simpledb.file.*;
 import java.util.ArrayList;
 
 /**
  * Manages the pinning and unpinning of buffers to blocks.
  * @author Edward Sciore
  *
  */
 class BasicBufferMgr {
    private Buffer[] bufferpool;
    private ArrayList<Buffer> freeList;
    private ArrayList<Buffer> aOne;
    private ArrayList<Buffer> aEm;
    private int threshold;
    private int numAvailable;
    
    /**
     * Creates a buffer manager having the specified number 
     * of buffer slots.
     * This constructor depends on both the {@link FileMgr} and
     * {@link simpledb.log.LogMgr LogMgr} objects 
     * that it gets from the class
     * {@link simpledb.server.SimpleDB}.
     * Those objects are created during system initialization.
     * Thus this constructor cannot be called until 
     * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
     * is called first.
     * @param numbuffs the number of buffer slots to allocate
     */
    BasicBufferMgr(int numbuffs) {
       bufferpool = new Buffer[numbuffs];
       freeList = new ArrayList<Buffer>(numbuffs);
       aOne = new ArrayList<Buffer>(numbuffs);
       aEm = new ArrayList<Buffer>(numbuffs);
       threshold = 3;
       numAvailable = numbuffs;
       for (int i=0; i<numbuffs; i++) {
          bufferpool[i] = new Buffer();
 	 freeList.add(bufferpool[i]);
       }
    }
    
    /**
     * Flushes the dirty buffers modified by the specified transaction.
     * @param txnum the transaction's id number
     */
    synchronized void flushAll(int txnum) {
       for (Buffer buff : bufferpool)
          if (buff.isModifiedBy(txnum))
          buff.flush();
    }
    
    /**
     * Pins a buffer to the specified block. 
     * If there is already a buffer assigned to that block
     * then that buffer is used;  
     * otherwise, an unpinned buffer from the pool is chosen.
     * Returns a null value if there are no available buffers.
     * @param blk a reference to a disk block
     * @return the pinned buffer
     */
    synchronized Buffer pin(Block blk) {
       Buffer buff = findExistingBuffer(blk);
       if (buff == null) {
          buff = chooseUnpinnedBuffer();
          if (buff == null)
             return null;
          buff.assignToBlock(blk);
       }
       if (!buff.isPinned())
          numAvailable--;
       buff.pin();
       return buff;
    }
    
    /**
     * Allocates a new block in the specified file, and
     * pins a buffer to it. 
     * Returns null (without allocating the block) if 
     * there are no available buffers.
     * @param filename the name of the file
     * @param fmtr a pageformatter object, used to format the new block
     * @return the pinned buffer
     */
    synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
       Buffer buff = chooseUnpinnedBuffer();
       if (buff == null)
          return null;
       buff.assignToNew(filename, fmtr);
       numAvailable--;
       buff.pin();
       return buff;
    }
    
    /**
     * Unpins the specified buffer.
     * @param buff the buffer to be unpinned
     */
    synchronized void unpin(Buffer buff) {
       buff.unpin();
       if (aEm.contains(buff)) {
       	aEm.add(0,buff);
       } else if (aOne.contains(buff)) {
       	aEm.add(0,buff);
       } else {
       	aOne.add(0,buff);
       }
       if (!buff.isPinned())
          numAvailable++;
    }
    
    /**
     * Returns the number of available (i.e. unpinned) buffers.
     * @return the number of available buffers
     */
    int available() {
       return numAvailable;
    }
    
    private Buffer findExistingBuffer(Block blk) {
       for (Buffer buff : bufferpool) {
          Block b = buff.block();
          if (b != null && b.equals(blk))
             return buff;
       }
       return null;
    }
    
    private Buffer chooseUnpinnedBuffer() {
      //This has been changed
      for (Buffer buff : freeList) {
      	return buff;
      }
      if (aOne.size() > threshold || aEm.isEmpty()) {
      	return aOne.remove(aOne.size()-1);
      } else {
      	return aEm.remove(aEm.size()-1);
      }
      /* Old Code
       for (Buffer buff : bufferpool)
          if (!buff.isPinned())
          return buff;
      */
    }
 
    public int getThreshold() {
    	return threshold;
    }
 
    public void setThreshold(int t) {
    	threshold = t;
    }
 
    public String toString() {
    	String ret = "";
    	for (Buffer buff : bufferpool) {
		ret += "\t" + ((buff.block() != null) ? buff.block().number() : -2) + " " + ((buff.isPinned()) ? "p" : "u");
 	}
 	return ret;
    }
 }
