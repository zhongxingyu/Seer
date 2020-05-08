 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 
 public class Transaction implements Iterable<Write>{
 	
 	public static final Sector COMMIT = new Sector("Commit".getBytes());
 
 	private ArrayList<Write> writes;
 
 	public Transaction() {
 		this.writes = new ArrayList<Write>();
 	}
 
 	public void add(int sectorNum, byte[] buffer) throws IndexOutOfBoundsException, IllegalArgumentException{
 		if (writes.size() == Common.MAX_WRITES_PER_TRANSACTION)
 			throw new ResourceException();
 		writes.add(new Write(sectorNum, buffer));
 	}
 
 	public Iterator<Write> iterator() {
 		return writes.iterator();
 	}
 	
 	//Return the size of the transaction, in sectors
 	public int size() {
 		return this.writes.size() + 2;
 	}
 	
 	public ArrayList<byte[]>getSectors() throws IOException {
 		ArrayList<byte[]> bytes = new ArrayList<byte[]>();
 		ByteArrayOutputStream buff = new ByteArrayOutputStream(Disk.SECTOR_SIZE);
 		ObjectOutputStream oos = new ObjectOutputStream(buff);		
 		ArrayList<Integer> sectorNums = new ArrayList<Integer>();
 		
 		for (Write w : this)
 			sectorNums.add(w.sectorNum);
 		oos.writeObject(sectorNums);
		bytes.add(buff.toByteArray());
 
 		for (Write w : this)
 			bytes.add(w.buffer);
 		bytes.add(Transaction.COMMIT.array);
 		return bytes;
 	}
 }
 
 class Write {
 	public int sectorNum;
 	public byte[] buffer;
 	
 	Write(int sectorNum, byte buffer[]) throws IllegalArgumentException, IndexOutOfBoundsException{
 		if (sectorNum < ADisk.REDO_LOG_SECTORS + 1)
 			throw new IndexOutOfBoundsException();
 		
 		if (buffer.length != Disk.SECTOR_SIZE)
 			  throw new IllegalArgumentException();
 		
 		if (sectorNum < ADisk.REDO_LOG_SECTORS || sectorNum >= Disk.NUM_OF_SECTORS)
 			throw new IndexOutOfBoundsException();
 		this.sectorNum = sectorNum;
 		this.buffer = buffer;
 	}
 }
