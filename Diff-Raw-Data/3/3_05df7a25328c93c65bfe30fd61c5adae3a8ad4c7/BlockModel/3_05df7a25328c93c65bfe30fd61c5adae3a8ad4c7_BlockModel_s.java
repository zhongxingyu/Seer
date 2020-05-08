 package demo.client.shared;
 
 import java.util.Iterator;
 
 /*
  * The base class for a falling block in a Block Drop BoardModel.
  */
 public class BlockModel {
 	
 	/*
 	 * For iterating through the positions of squares in a single block.
 	 */
 	public class SquareIterator implements Iterator<Integer[]> {
 
 		/* Index of next position to return. */
 		private int next;
 		
 		private int row;
 		private int col;
 		
 		/*
 		 * Create a SquareIterator.
 		 * 
 		 * @param offsets The offsets of each square to iterate through.
 		 */
 		public SquareIterator(int row, int col) {
 			super();
 			this.row = row;
 			this.col = col;
 			next = 0;
 		}
 		
 		/*
 		 * (non-Javadoc)
 		 * @see java.util.Iterator#hasNext()
 		 */
 		@Override
 		public boolean hasNext() {
 			return next < offsets.length;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see java.util.Iterator#next()
 		 */
 		@Override
 		public Integer[] next() {
 			Integer[] res = new Integer[] {
 					new Integer(offsets[next][0] + row),
 					new Integer(offsets[next][1] + col)};
 			next++;
 			return res;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see java.util.Iterator#remove()
 		 */
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	public static final int BASIC_CODE = 1;
 	
 	private static int idGen = 1;
 
 	/* 
 	 * An array of pairs. Represents the offset positions of each tile in this block
 	 * from the central position.
 	 */
 	private int[][] offsets;
 	/* A unique id for identifying this block. */
 	private int id;
 	
 	/*
 	 * Create a basic BlockModel consisting of one square.
	 * 
	 * @param rowNum The number of rows in the board (used to determine starting position).
	 * @param colNum The number of columns in the board (used to determine starting position).
 	 */
 	public BlockModel() {
 		// Creates array {{0,0}}, so single square with no offset.
 		offsets = new int[1][2];
 		
 		id = generateId();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object other) {
 		return other.getClass() == BlockModel.class && this.getId() == ((BlockModel) other).getId();
 	}
 	
 	/*
 	 * Get the id of this BlockModel.
 	 * 
 	 * @return The id of this BlockModel.
 	 */
 	public int getId() {
 		return id;
 	}
 
 	private static int generateId() {
 		return idGen++;
 	}
 
 	/*
 	 * Get the integer representing this type of block on the board.
 	 * 
 	 * @return An integer representing this type of block on the board.
 	 */
 	public int getCode() {
 		return BASIC_CODE;
 	}
 	
 	public Iterable<Integer[]> getIterator(final int row, final int col) {
 		return new Iterable<Integer[]>() {
 			@Override
 			public Iterator<Integer[]> iterator() {
 				return new SquareIterator(row, col);
 			}
 		};
 	}
 	
 	public void rotateClockwise() {
 		//TODO: Implement clockwise rotation.
 	}
 	
 	public void rotateCounterclockwise() {
 		//TODO: Implement counter-clockwise rotation.
 	}
 }
