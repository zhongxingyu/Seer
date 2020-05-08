 package dblockcache;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import virtualdisk.VirtualDisk;
 import virtualdisk.VirtualDiskSingleton;
 
 import common.Constants;
 
 public class DBuffer {
 	
 	private Lock _busyLock = new ReentrantLock();
 	private Lock _validLock = new ReentrantLock();
 	private Lock _dirtyLock = new ReentrantLock();
 	private Condition _isValidCV = _validLock.newCondition();
 	private Condition _isDirtyCV = _dirtyLock.newCondition();
 	
 	private int _blockID;
 	private byte[] _buffer;
 	private int _busy;
 	private boolean _dirty;
 	private boolean _valid;
 	
 	public DBuffer(int blockID) {
 		_blockID = blockID;
 		_buffer = new byte[Constants.BLOCK_SIZE];
 		_busy = 0;
 		_dirty = false;
 		_valid = true;
 	}
 	
 	/* Start an asynchronous fetch of associated block from the volume */
 	public void startFetch() {
 		VirtualDisk vd = VirtualDiskSingleton.getInstance();
 		vd.startRequest(this, Constants.DiskOperationType.READ);
 		_validLock.lock();
 		_valid = true;
 		_isValidCV.notifyAll();
 		_validLock.unlock();
 	}
 	
 	/* Start an asynchronous write of buffer contents to block on volume */
 	public void startPush() {
 		// TODO: verify what async should mean...
 		VirtualDisk vd = VirtualDiskSingleton.getInstance();
 		vd.startRequest(this, Constants.DiskOperationType.WRITE);
 		_dirtyLock.lock();
 		_dirty = false;
 		_isDirtyCV.notifyAll();
 		_dirtyLock.unlock();
 	}
 	
 	/* Check whether the buffer has valid data */ 
 	public boolean checkValid() {
 		// TODO: verify what "valid" is
 		return _valid;
 	}
 	
 	/* Wait until the buffer is free */
 	public boolean waitValid() throws InterruptedException {
 		_validLock.lock();
 		while (!_valid) {
 			_isValidCV.await();
 		}
 		_validLock.unlock();
 		return _valid;
 	}
 	
 	/* Check whether the buffer is dirty, i.e., has modified data to be written back */
 	public boolean checkClean() {
 		return !_dirty;
 	}
 	
 	/* Wait until the buffer is clean, i.e., until a push operation completes */
 	public boolean waitClean() throws InterruptedException {
 		_dirtyLock.lock();
 		while (_dirty) {
 			_isDirtyCV.await();
 		}
 		_dirtyLock.unlock();
 		return !_dirty;
 	}
 	
 	/* Check if buffer is evictable: not evictable if I/O in progress, or buffer is held */
 	public boolean isBusy() {
 		return _busy > 0;
 	}
 
 	/*
 	 * reads into the buffer[] array from the contents of the DBuffer. Check
 	 * first that the DBuffer has a valid copy of the data! startOffset and
 	 * count are for the buffer array, not the DBuffer. Upon an error, it should
 	 * return -1, otherwise return number of bytes read.
 	 */
 	public int read(byte[] buffer, int startOffset, int count) {
 		// Validity and bound checking
 		if (!_valid) return -1;
 		if (startOffset + count > buffer.length) return -1;
 		// Read into buffer
		return buffcopy(_buffer, buffer, startOffset, count);
 	}
 
 	/*
 	 * writes into the DBuffer from the contents of buffer[] array. startOffset
 	 * and count are for the buffer array, not the DBuffer. Mark buffer dirty!
 	 * Upon an error, it should return -1, otherwise return number of bytes
 	 * written.
 	 */
 	public int write(byte[] buffer, int startOffset, int count) {
 		// Boundary check for if blocks are trying to be written past buff size
 		if (startOffset + count > Constants.BLOCK_SIZE) return -1;
 		// Otherwise write blocks (stop if we hit end of buffer or count)
		int blocksWritten = buffcopy(buffer, _buffer, startOffset, count);
 		_dirty = true;
		return blocksWritten;
	}
	
	/*
	 * Helper method for copying from one buffer to another.
	 */
	private int buffcopy(byte[] src, byte[] dest, int offset, int count) {
 		int i = 0;
		for (i = 0; i <  count && i < dest.length + offset && i < src.length; i++) {
			dest[i + offset] = src[i];
 		}
 		return i;
 	}
 	
 	/* An upcall from VirtualDisk layer to inform the completion of an IO operation */
 	public synchronized void ioComplete() {
 		_busyLock.lock();
 		_busy--;
 		_busyLock.unlock();
 	}
 	
 	/* An upcall from VirtualDisk layer to fetch the blockID associated with a startRequest operation */
 	public int getBlockID() {
 		return _blockID;
 	}
 	
 	/* An upcall from VirtualDisk layer to fetch the buffer associated with DBuffer object*/
 	public byte[] getBuffer() {
 		_busyLock.lock();
 		_busy++;
 		_busyLock.unlock();
 		return _buffer;
 	}
 }
