 package net.diogomarques.similarity;
 
 import java.util.BitSet;
 
 /**
  * A BitSet that returns the size requested at creation instead of the memory
  * actually allocated.
  * <p>
  * The BitSet default implementation of {@link BitSet#BitSet(int)} assures that
  * enough space is allocated to accommodate the requested number of bits, but
  * calling {@link BitSet#size()} will return the length of a vector used
  * internally to keep state, whose size depends on internals.
  */
 public class FixedBitSet extends BitSet {
 
 	private static final long serialVersionUID = 1L;
 
 	private int size;
 
 	/**
 	 * Creates a bit set with the same properties as one created with
 	 * {@link BitSet#BitSet(int)}.
 	 * 
 	 * @param nbits
 	 *            the size of the bit set
 	 */
 	public FixedBitSet(int nbits) {
 		super(nbits);
 		this.size = nbits;
 	}
 
 	/**
 	 * Returns the number of bits requested at creation.
 	 */
 	@Override
 	public int size() {
 		return size;
 	}
 
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++)
			builder.append(get(i) ? 1 : 0);
		return builder.toString();
	}

 	/**
 	 * Proper deep clone. BitSet's default implementation creates a new instance
 	 * but the internal long[] array is a shallow clone.
 	 */
 	@Override
 	public Object clone() {
 		FixedBitSet clone = new FixedBitSet(size);
 		clone.or(this);
		return clone;
 	}
 
 }
