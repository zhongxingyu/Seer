 package iterators.scanners;
 
 import primitives.Schema;
 import primitives.Tuple;
 import global.RID;
 import global.SearchKey;
 import heap.HeapFile;
 import index.HashIndex;
 import index.HashScan;
 import iterators.Iterator;
 
 /**
  * Wrapper for hash scan, an index access method.
  */
 public class KeyScan extends Iterator {
 
 	private SearchKey key;
 	private HeapFile file;
 	private HashIndex index;
 	private HashScan scan;
 
 	/**
 	 * Constructs an index scan, given the hash index and schema.
 	 */
 	public KeyScan(Schema schema, HashIndex index, SearchKey key, HeapFile file) {
 		this.setSchema(schema);
 		this.key = key;
 		this.file = file;
 		this.index = index;
		this.scan = index.openScan(key);
 	}
 
 	/**
 	 * Gives a one-line explaination of the iterator, repeats the call on any
 	 * child iterators, and increases the indent depth along the way.
 	 */
 	public void explain(int depth) {
 		for(int i=0; i<depth; i++)
 			System.out.print("\t");
 		System.out.println("KeyScan Iterator");
 	}
 
 	/**
 	 * Restarts the iterator, i.e. as if it were just constructed.
 	 */
 	public void restart() {
 		scan = index.openScan(key);
 	}
 
 	/**
 	 * Returns true if the iterator is open; false otherwise.
 	 */
 	public boolean isOpen() {
 		return scan!=null;
 	}
 
 	/**
 	 * Closes the iterator, releasing any resources (i.e. pinned pages).
 	 */
 	public void close() {
 		this.scan.close();
 	}
 
 	/**
 	 * Returns true if there are more tuples, false otherwise.
 	 */
 	public boolean hasNext() {
 		return this.scan.hasNext();
 	}
 
 	/**
 	 * Gets the next tuple in the iteration.
 	 * 
 	 * @throws IllegalStateException
 	 *             if no more tuples
 	 */
 	public Tuple getNext() {
 		RID rid = scan.getNext();
 		byte[] data = file.selectRecord(rid);
 		return new Tuple(getSchema(), data);
 	}
 
 } // public class KeyScan extends Iterator
