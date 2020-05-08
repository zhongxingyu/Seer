 package iterators.scanners;
 
 import iterators.Iterator;
 import primitives.Schema;
 import primitives.Tuple;
 import global.Convert;
 import global.RID;
 import heap.HeapFile;
 import heap.HeapScan;
 
 /**
  * Wrapper for heap file scan, the most basic access method. This "iterator"
  * version takes schema into consideration and generates real tuples.
  */
 public class FileScan extends Iterator {
 
 	private HeapFile file;
 	private HeapScan scan;
 	private Tuple tuple;
 	private RID rid;
 	/**
 	 * Constructs a file scan, given the schema and heap file.
 	 */
 	public FileScan(Schema schema, HeapFile file) {
 		this.setSchema(schema);
 		this.file = file;
 		this.tuple = new Tuple(schema);
 	}
 
 	/**
 	 * Gives a one-line explaination of the iterator, repeats the call on any
 	 * child iterators, and increases the indent depth along the way.
 	 */
 	public void explain(int depth) {
 		for(int i=0; i<depth; i++)
 			System.out.print("\t");
 		System.out.println("FileScan Iterator");
 	}
 
 	/**
 	 * Restarts the iterator, i.e. as if it were just constructed.
 	 */
 	public void restart() {
 		scan = file.openScan();
 	}
 
 	/**
 	 * Returns true if the iterator is open; false otherwise.
 	 */
 	public boolean isOpen() {
 		return scan != null;
 	}
 
 	/**
 	 * Closes the iterator, releasing any resources (i.e. pinned pages).
 	 */
 	public void close() {
 		scan.close();
 	}
 
 	/**
 	 * Returns true if there are more tuples, false otherwise.
 	 */
 	public boolean hasNext() {
 		return scan.hasNext();
 	}
 
 	/**
 	 * Gets the next tuple in the iteration.
 	 * 
 	 * @throws IllegalStateException
 	 *             if no more tuples
 	 */
 	public Tuple getNext() {
 		byte[] data = scan.getNext(rid);
 		tuple = new Tuple(getSchema(), data);
 		return tuple;
 	}
 
 	/**
 	 * Gets the RID of the last tuple returned.
 	 */
 	public RID getLastRID() {
 		return rid;
 	}
 
 } // public class FileScan extends Iterator
