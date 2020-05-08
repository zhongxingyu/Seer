 package relop;
 
 /**
  * The projection operator extracts columns from a relation; unlike in
  * relational algebra, this operator does NOT eliminate duplicate tuples.
  */
 public class Projection extends Iterator {
 
 	/**
 	 * Constructs a projection, given the underlying iterator and field numbers.
 	 */
 	private boolean isOpen;
 	private Iterator itr;
 
 	public Projection(Iterator iter, Integer... fields) {
 		Schema oldSchema = iter.getSchema();
 		Schema schema = new Schema(fields.length);
 		for (int i = 0; i < fields.length; i++) {
 			int k = fields[i];
 			schema.initField(i, oldSchema.fieldType(k),
 					oldSchema.fieldLength(k), oldSchema.fieldName(k));
 		}
 		setSchema(schema);
 		itr = iter;
 		isOpen = true;
 	}
 
 	/**
 	 * Gives a one-line explanation of the iterator, repeats the call on any
 	 * child iterators, and increases the indent depth along the way.
 	 */
 	public void explain(int depth) {
 		System.out.println("Projection " + depth);
 		itr.explain(depth + 1);
 	}
 
 	/**
 	 * Restarts the iterator, i.e. as if it were just constructed.
 	 */
 	public void restart() {
 		isOpen = true;
 		itr.restart();
 	}
 
 	/**
 	 * Returns true if the iterator is open; false otherwise.
 	 */
 	public boolean isOpen() {
 		return isOpen;
 	}
 
 	/**
 	 * Closes the iterator, releasing any resources (i.e. pinned pages).
 	 */
 	public void close() {
 		itr.close();
 		isOpen = false;
 	}
 
 	/**
 	 * Returns true if there are more tuples, false otherwise.
 	 */
 	public boolean hasNext() {
 		return itr.hasNext();
 	}
 
 	/**
 	 * Gets the next tuple in the iteration.
 	 * 
 	 * @throws IllegalStateException
 	 *             if no more tuples
 	 */
 	public Tuple getNext() {
		byte[] data = itr.getNext().getData();
		Tuple tup = new Tuple(getSchema(), data);
 		return tup;
 	}
 
 } // public class Projection extends Iterator
