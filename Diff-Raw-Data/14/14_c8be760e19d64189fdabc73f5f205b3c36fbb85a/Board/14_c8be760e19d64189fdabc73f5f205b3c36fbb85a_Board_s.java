 package bomberman.game;
 
 public class Board {
 
 	final private int height;
 	final private int width;
 	final private int[][] field;
 
 	/**
 	 * Draw the board, initialize collisions
 	 * 
 	 * @param height
 	 *            height of the board.
 	 * @param width
 	 *            width of the board.
 	 * 
	 *            (both in pixel)
 	 */
 
 	public Board(final int[][] arr) {
 		this.height = arr.length;
 		this.width = arr[0].length;
 		this.field = arr;
 	}
 
 	public Board(final int height, final int width) {
 		if (height < 2 || width < 2)
 			throw new IllegalArgumentException();
 
 		// if height or width is an even number, make it uneven.
 		// So no wall-blocks get stuck at the border
 
 		this.height = (height % 2 == 1) ? height : height + 1;
 		this.width = (width % 2 == 1) ? width : width + 1;
 		this.field = new int[this.height][this.width];
 
 		initializeCollisions();
 	}
 
 	/**
 	 * Collisions: Bomberman can not pass walls Currently sets non walkable
 	 * cells with 1: The core of every wall is unwalkable...
 	 */
 	private void initializeCollisions() {
 		for (int i = 0; i < height; i++) {
 			for (int j = 0; j < width; j++) {
 				if (i == 0 && j == 0 || i == 0 && j == 1 || i == 1 && j == 0)
 					continue;
 
 				if (Math.random() < 0.2)
 					field[i][j] = 2;
 			}
 		}
 
 		for (int i = 1; i < height; i += 2) {
 			for (int j = 1; j < width; j += 2) {
 				field[i][j] = 1;
 			}
 		}
 	}
 
 	/**
 	 * get height of field
 	 * 
	 * @return height in px(int)
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * get width of field
 	 * 
	 * @return width in px (int)
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * get field
 	 * 
 	 * @return a 2D int array with collisions set...
 	 */
 	public int[][] getField() {
 		return field;
 	}
 
 }
