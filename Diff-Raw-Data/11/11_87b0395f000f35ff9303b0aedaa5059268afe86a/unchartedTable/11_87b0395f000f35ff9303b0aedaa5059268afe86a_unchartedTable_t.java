 public class unchartedTable {
 	int[][] uncharted;
 	Ants game;
 	updateTable update;
 	/**
 	 * 
 	 * @return uncharted contains the information about how many turns it has
 	 *         been when the tile was last visible
 	 */
 	public int[][] getUncharted() {
 		return uncharted;
 	}
 	/**
 	 * initializes uncharted
 	 * 
 	 * @param gameField
 	 *            is the map of current game
 	 */
 	public void initializeUncharted(Ilk[][] gameField) {
 		uncharted = new int[gameField.length][gameField[0].length];
 		for (int i = 0; i < gameField.length; i++) {
 			for (int j = 0; j < gameField[0].length; j++) {
 				if (gameField[i][j] != Ilk.WATER) {
 					uncharted[i][j] = 30;
 				} else {
 					uncharted[i][j] = -1;
 				}
 			}
 		}
 	}
 	/**
 	 * tells if tile is visible or not
 	 * 
 	 * @param row
 	 *            vertical location of a tile
 	 * @param col
	 *            horizontal location of a tile
 	 * @return true if tile is visible false if invisible
 	 */
 	public boolean Visible(int row, int col) {
 		Tile brick = new Tile(row, col);
 		return game.isVisible(brick);
 	}
 
 	/**
 	 * establishes new view area
 	 */
 	public void setViewArea(int vision, Tile MyAnt) {
 		int[][] easternLine = update.getEastTable();
 		int[][] westernLine = update.getWestTable();
 		int row = MyAnt.getRow();
 		int col = MyAnt.getCol();
 		for (int i = 0; i < easternLine.length; i++) {
 			col += easternLine[i][1];
 			for (int j = easternLine[i][0] + 1; j < westernLine[i][0]; j++) {
 				row += j;
 				uncharted[col][row] = 0;
 			}
 		}
 	}
 	/**
 	 * goes through uncharted and raises the value of areas that are unseen by
 	 * own ants
 	 */
 	public void raiseUncharted() {
 		for (int i = 0; i < uncharted.length; i++) {
 			for (int j = 0; j < uncharted[0].length; j++) {
 				if ((uncharted[i][j] < 30 && uncharted[i][j] > -1) && !Visible(i, j)) {
 					uncharted[i][j] += 1;
 				}
 			}
 		}
 	}
 	/**
	 * checks if value of horizontal or vertical coordinate goes over the map
 	 * and returns value that is within the border
 	 * 
 	 * @param value
	 *            horizontal or vertical coordinate
 	 * @param max
	 *            the border of horizontal or vertical line on the map
 	 * @return value contains either given value if within borders or the amount
 	 *         that it goes over the top
 	 */
 	public int isOverBoard(int value, int max) {
 		if (value >= max) {
 			return value - max;
 		}
 		return value;
 	}
 	/**
 	 * counts how much ant would gain from moving to the direction given in
 	 * table
 	 * 
 	 * @param MyAnt
 	 *            the current location of ant
 	 * @param table
 	 *            contains some directions coordinates that would be visible
 	 *            after ants moves should ant move to this direction
 	 * @return value is the amount gain from moving to this direction
 	 */
 	public int getMoveValue(Tile MyAnt, int table[][]) {
 		int row;
 		int col;
 		int value = 0;
 		for (int i = 0; i < table.length; i++) {
 			row = MyAnt.getRow() + table[i][0];
 			col = MyAnt.getCol() + table[i][1];
 			row = isOverBoard(row, game.getRows());
 			col = isOverBoard(col, game.getCols());
 			value += uncharted[row][col];
 		}
 		return value;
 	}
 	/**
 	 * sets the new area covered by the view radius to zero.
 	 * 
 	 * @param MyAnt
 	 *            the current location of ant
 	 * @param table
 	 *            contains coordinates of the new areas that are to be seen
 	 */
 	public void setTable(Tile MyAnt, int table[][]) {
 		int row;
 		int col;
 		for (int i = 0; i < table.length; i++) {
 			row = MyAnt.getRow() + table[i][0];
 			col = MyAnt.getCol() + table[i][1];
 			row = isOverBoard(row, game.getRows());
 			col = isOverBoard(col, game.getCols());
 			uncharted[row][col] = 0;
 		}
 	}
 }
