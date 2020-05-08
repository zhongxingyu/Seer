 package edu.berkeley.gamesman.database;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.Random;
 
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 
 /** 
  * Test DataBase for GamesCrafters Java. 
  * Right now it just writes BigIntegers to memory, without byte padding.
  *
  *	
  * @author Alex Trofimov
  * @version 1.3
  * 
  * Changelog:
  * 1.3 With data sizes < 58 bits, longs are used instead of BigInts, 20% speedup.
  * 1.2 Slight speedup for operating on small data (< 8 bits); ensureCapacity() added.
  * 1.1 Switched to a byte[] instead of ArrayList<Byte> for internal storage.
  * 1.0 Initial Version.
  */
 public class MemoryDatabase extends Database{
 	
 	/* Class Variables */
 	private byte[] memoryStorage;		// byte array to store the data
 	
 	/* DB Variables */
 	protected long capacity;			// the size of the data
 	protected boolean open;				// whether this database is initialized 
 										// and not closed.
 	
 	/**
 	 * Null Constructor, used primarily for testing.
 	 * It doesn't set anything
 	 * 
 	 * @author Alex Trofimov
 	 */
 	public MemoryDatabase() {	}
 	
 	/**
 	 * Get the size of this DB in bytes.
 	 * @return size of the DB in bytes.
 	 */
 	public long getSize() {
 		return this.capacity;
 	}
 	
 	/** 
 	 * Ensure that this database can store at least this number of bytes.
 	 * @param numBytes - number of bytes this DB should be able to fit (at least).
 	 */
 	protected void ensureCapacity(long numBytes) {
 		int newCapacity = 2;
 		while (newCapacity <= numBytes) newCapacity = newCapacity << 2;
 		if (newCapacity > this.capacity) {
 			this.capacity = newCapacity;
 			byte[] temp = this.memoryStorage;
 			this.memoryStorage = new byte[(int) this.capacity];
 			for (int i = 0; i < temp.length; i ++) {
 				this.memoryStorage[i] = temp[i];	}
 			temp = null;
 		}
 	}
 	
 	@Override
 	public Record getRecord(long recIndex) {
 		return getRecord(recIndex, new Record(this.conf));
 	}
 	
 	@Override 
 	public Record getRecord(long recordIndex, Record recordToReturn) {
 		/* Defining Variables */
 		int recordSize = recordToReturn.bitlength();
 		/* Getting the Record */
 		if (recordSize > 57)
 			recordToReturn.loadBigInteger(getBitsAsBigInteger(recordIndex, recordSize));
 		else
 			recordToReturn.loadLong(getBitsAsLong(recordIndex, recordSize));
 		return recordToReturn;	
 	}
 	
 	/** 
 	 * Read a record from DB specified by recIndex.
 	 * Reads byte-array from DB byte-by-byte, truncates it
 	 * into a BigInteger.
 	 * 
 	 * @author Alex Trofimov
 	 * @param index sequential index of this segment in DB.
 	 * @param bitSize the size in bits of the segment.
 	 * 		  Assumes that all previous segments are of this size.
 	 * @return Record in BigInteger representation
 	 */
 	protected BigInteger getBitsAsBigInteger(long index, long bitSize) {
 		
 		/* Setting up Variables */
 		long bitBegin = bitSize * index;
 		long bitEnd = bitSize * (index + 1) - 1;
 		long byteIndexStart = (bitBegin) >> 3;
 		long byteIndexEnd = (bitEnd) >> 3; 
 		int byteSize = (int) (byteIndexEnd - byteIndexStart + 1);
 		int unSign = 1; // Padding left, to make it unsigned
 		byte[] downloadData = new byte[byteSize + unSign];
 		int rightPad = (int) (7 - (bitEnd % 8));
 		int leftPad = (int) bitBegin % 8;
 		
 		/* Getting data from DB */
 		for (int i = 0; i < byteSize; i ++) {
 			downloadData[i + unSign] = getByte(i + byteIndexStart); }		
 		
 		/* Truncating data and returning */		
 		if (leftPad > 0) 
 			downloadData[unSign] &= ((-1 & 0xFF) >>> leftPad); // off by one :/
 		BigInteger bigIntData = new BigInteger(downloadData);
 		if (rightPad > 0) 
 			bigIntData = bigIntData.shiftRight(rightPad);
 		return bigIntData;
 	}
 
 	/** 
 	 * Read a record from DB specified by recIndex.
 	 * Reads byte-array from DB byte-by-byte, truncates it
 	 * into a long.
 	 * 
 	 * @author Alex Trofimov
 	 * @param index sequential index of this segment in DB.
 	 * @param bitSize the size in bits of the segment.
 	 * 		  Assumes that all previous segments are of this size.
 	 * @return Record in long representation
 	 */
 	protected long getBitsAsLong(long index, long bitSize) {
 		
 		/* Setting up Variables */
 		long bitBegin = bitSize * index;
 		long bitEnd = bitSize * (index + 1) - 1;
 		long byteIndexStart = (bitBegin) >> 3;
 		long byteIndexEnd = (bitEnd) >> 3; 
 		int byteSize = (int) (byteIndexEnd - byteIndexStart + 1);
 		int rightPad = (int) (7 - (bitEnd % 8));
 		int leftPad = (int) bitBegin % 8;
 		
 		/* Getting data from DB */
 		long data = 0;
 		long temp;
 		for (int i = 0; i < byteSize; i ++) {
 			temp = (getByte(i + byteIndexStart) & 0xFF);
 			if (i == 0) temp &=  (-1l >>> (leftPad + 56));				
 			data += temp << (8 * (byteSize - 1 - i));
 		}
 		data = data >>> rightPad;
 
 		/* returning */
 		return data;
 	}
 	
 	@Override
 	public void initialize(String locations) {
 		this.initialize();
 	}
 	
 	/**
 	 * Null Constructor for testing the database outside of
 	 * Gamesman environment.
 	 * Initializes the internal storage.
 	 * 
 	 * @author Alex Trofimov
 	 */
 	private void initialize() {		
 		this.capacity = 2;
 		this.memoryStorage = new byte[(int) this.capacity];
 		this.open = true;
 		
 	}
 	
 	@Override
 	public void putRecord(long recordIndex, Record value) {
 		int bitSize = value.bitlength();
 		if (bitSize > 57) {
 			BigInteger data = value.toBigInteger();
 			putBitsAsBigInteger(recordIndex, bitSize, data);
 		} else {
 			long data = value.toLong();
 			putBitsAsLong(recordIndex, bitSize, data);
 		}
 	}
 	
 	/**
 	 * Takes a BigInteger, then aligns it to bytes, 
 	 * filling the gaps with data from actual DB (memory reads are cheap).
 	 * Then puts data into the DB byte-by-byte.
 	 * 
 	 * @author Alex Trofimov
 	 * @param index is the sequential number of the segment in DB.
 	 * @param bitSize size of the segment.
 	 *		  Assumes all previous segments are of this size.
 	 * @param data are the bits that need to be stored compacted as BigInteger.
 	 */
 	protected void putBitsAsBigInteger(long index, int bitSize, BigInteger data) {
 		
 		/* Setting up Variables */		
 		long bitBegin = bitSize * index;
 		long bitEnd = bitSize * (index + 1) - 1;
 		long byteIndexStart = (bitBegin) >> 3;
 		long byteIndexEnd = (bitEnd) >> 3; 
 		int byteSize = (int) (byteIndexEnd - byteIndexStart + 1);
 		byte[] updateData = new byte[byteSize];
 		int rightPad = (int) (7 - (bitEnd % 8));
 		
 		/* Padding the data to be written */
 		if (rightPad > 0)
 			data = data.shiftLeft(rightPad);
 		byte[] byteData = data.toByteArray();
 		int dataByteOffset = byteSize - byteData.length;
 		int signOffset = (dataByteOffset < 0) ? 1 : 0; // Skip first byte if it's a negative num
 		for (int i = signOffset; i < byteData.length; i ++) {
 				updateData[i + dataByteOffset] = byteData[i]; }
 		
 		/* Writing the data to DataBase */
 		for(int i = 0; i < byteSize; i ++) {
 			if (i == 0 || i == byteSize - 1)
 				putByte(byteIndexStart + i, updateData[i], true); // Ends should preserve bits
 			else putByte(byteIndexStart + i, updateData[i], false); 
 			}
 	}
 	
 	/**
 	 * Takes a long, then aligns it, filling the gaps with data 
 	 * from actual DB (memory reads are cheap).
 	 * Then puts data into the DB byte-by-byte.
 	 * 
 	 * @author Alex Trofimov
 	 * @param index is the sequential number of the segment in DB.
 	 * @param bitSize size of the segment.
 	 *		  Assumes all previous segments are of this size.
 	 * @param data are the bits that need to be stored compacted as BigInteger.
 	 */
 	protected void putBitsAsLong(long index, int bitSize, long data) {
 		assert bitSize <= 64; // otherwise the BigInteger should be used
 		
 		/* Setting up Variables */		
 		long bitBegin = bitSize * index;
 		long bitEnd = bitSize * (index + 1) - 1;
 		long byteIndexStart = (bitBegin) >> 3;
 		long byteIndexEnd = (bitEnd) >> 3; 
 		int byteSize = (int) (byteIndexEnd - byteIndexStart + 1);
 		int rightPad = (int) (7 - (bitEnd % 8));
 		
 		/* Putting padded data into a byte array */
 		byte[] byteData = new byte[byteSize];
 		for (int i = byteSize - 1; i >= 0; i --) {
 			byteData[i] = (byte) (((data << rightPad) >> ((byteSize - i - 1) * 8)) & 0xFF);
 		}
 		
 		/* Writing the data to DataBase */
 		for(int i = 0; i < byteSize; i ++) {
 			if (i == 0 || i == byteSize - 1)
 				putByte(byteIndexStart + i, byteData[i], true); // Ends should preserve bits
 			else putByte(byteIndexStart + i, byteData[i], false); 
 		}
 	}
 
 	@Override
 	public void flush() {
 		assert Util.debug(DebugFacility.DATABASE, "Flushing Memory DataBase. Does Nothing.");
 	}
 	
 	@Override
 	public void close() {
 		this.open = false;
 		flush();
 		assert Util.debug(DebugFacility.DATABASE, "Closing Memory DataBase. Does Nothing.");
 	}
 	
 	/**
 	 * Get a byte from the database.
 	 * @author Alex Trofimov
 	 * @param index - sequential number of this byte in DB.
 	 * @return - one byte at specified byte index.
 	 */
 	protected byte getByte(long index) {
 		assert !open;
 		if (this.capacity <= index || index < 0)
 			return 0x0;
 		try {
 			return this.memoryStorage[(int) index];
 		} catch (Exception e) {
 			Util.fatalError("Read from DB failed. Capacity: " + this.capacity, e);
 			return (byte) 0x0; // Not Reached.
 		}
 	}
 	
 	/**
 	 * Write a byte into the database. Assumes that space is already allocated.
 	 * @author Alex Trofimov
 	 * @param index - sequential number of byte in DB.
 	 * @param data - byte that needs to be written.
 	 * @param preserve - set to true if you want data to be ORed with existing byte.
 	 */
 	protected void putByte(long index, byte data, boolean preserve) {
 		assert !open;
 		try {
 			ensureCapacity((int) index + 1); // Ensure that the data can be written.
 			if (preserve) this.memoryStorage[(int) index] |= data;
 			else this.memoryStorage[(int) index] = data;
 		} catch (Exception e) {
 			Util.fatalError("Write to DB failed. Byte #" + index, e);
 		}
 	}
 	
 	
 	/** 
 	 * For testing purposes only. 
 	 * This tests this class for correctness (more or less).
 	 * It generates random BigIntegers of fixed size, stores
 	 * them into the DB, then reads them and compares to 
 	 * the originals.
 	 * 
 	 * @author Alex Trofimov
 	 * @param args Will be ignored.
 	 */
 	public static void main(String[] args) {
 		
 		/* Defining Variables */
 		long startTime = (new Date()).getTime();
 		Random random = new Random();
 		int testSize = 800;
 		BigInteger[] BigInts = new BigInteger[testSize];
 		MemoryDatabase DB = new MemoryDatabase();
 		java.util.TreeSet<Integer> visited = new java.util.TreeSet<Integer>();
 		BigInteger temp;
 		long longTemp;
 		
 		/* Testing different length storage */
 		for (int bitSize = 1; bitSize < 100; bitSize ++) {
 			/* Resetting variables */
 			DB.initialize("");
 			visited.clear();
 			
 			/* Generating Random Numbers */
 			int bc;
 			for (int i = 0; i < testSize; i ++) {
 				BigInts[i] = new BigInteger(bitSize, random);
 				bc = BigInts[i].bitLength();
 				if (bc < bitSize)
 					BigInts[i] = BigInts[i].shiftLeft(bitSize - bc); }
 			
 			/* Writing numbers to database in random order */
 			int index;
 			for (int i = 0; i < testSize; i ++) {
 				index = random.nextInt(testSize);
 				while (visited.contains(index))
 					index = (index + 1) % testSize;
 				if (bitSize > 57) DB.putBitsAsBigInteger(index, bitSize, BigInts[index]);
 				else DB.putBitsAsLong(index, bitSize, BigInts[index].longValue());
 				visited.add(index);
 			}
 			
 			/* Testing that it was written correctly */
 			int pass = 0;
 			int fail = 0;
 			for (int i = 0; i < testSize; i ++) {
 				if (bitSize > 57) {
 					temp = DB.getBitsAsBigInteger(i, bitSize);
 					if (temp.toString(2).equals(BigInts[i].toString(2))) pass ++;
 					else fail ++;
 				} else {
 					longTemp = DB.getBitsAsLong(i, bitSize);
 					if (longTemp == BigInts[i].longValue()) pass ++;
 					else fail ++;
 				}
 				
 			}
 			
 			/* Finshing this test round */
 			System.out.printf("%d tests passed. %d tests failed with Bitsize = %d.\n", pass, fail, bitSize);
 			DB.close();
 		}
 		
 		long endTime = (new Date()).getTime() - startTime;
 		System.out.printf("Testing Complete in %d milli-seconds.\n", endTime);
 		System.out.printf("Datbase size: %d bytes.\n", DB.getSize());
 		
 	}
}
