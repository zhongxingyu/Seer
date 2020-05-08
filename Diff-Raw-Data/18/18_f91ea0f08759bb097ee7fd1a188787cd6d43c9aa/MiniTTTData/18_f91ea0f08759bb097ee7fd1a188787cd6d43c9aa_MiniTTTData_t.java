 package com.trio.triotictactoe.model;
 
 import com.trio.triotictactoe.utils.CellState;
 
 /**
  * Data corresponding to one normal tic tac toe
  * @author ajith
  *
  */
 public class MiniTTTData {
 	private CellState[][] data = new CellState[3][3];
 	private boolean userSelectedData;
 	private int userClickedRowIndex;
 	private int userClickedColumnIndex;
 
	public MiniTTTData() {
		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 2; j++) {
				data[i][j] = CellState.EMPTY;
			}
		}
	}

 	public CellState getCell(int row, int column) {
 		checkInput(row, column);
 		return data[row][column];
 	}
 
 	public void populateData(int row, int column, CellState state) {
 		checkInput(row, column);
 		data[row][column] = state;
 	}
 
 	public boolean hasUserSelectedData() {
 		return userSelectedData;
 	}
 
 	public void setHasUserSelectedData(boolean hasUserSelectedData) {
 		this.userSelectedData = hasUserSelectedData;
 	}
 
 	public int getUserClickedRowIndex() {
 		return userClickedRowIndex;
 	}
 
 	public void setUserClickedRowIndex(int userClickedRowIndex) {
 		this.userClickedRowIndex = userClickedRowIndex;
 	}
 
 	public int getUserClickedColumnIndex() {
 		return userClickedColumnIndex;
 	}
 
 	public void setUserClickedColumnIndex(int userClickedColumnIndex) {
 		this.userClickedColumnIndex = userClickedColumnIndex;
 	}
 
 	/**
 	 * TODO: Add proper exception and remove this stupidity
 	 * @param row
 	 * @param column
 	 */
 	private void checkInput(int row, int column) {
 		if (row < 0 || row > 2 || column < 0 || column > 2) {
 			throw new RuntimeException("Invalid input, row: " + row + ", column: " + column);
 		}
 		return;
 	}
 
 	public void setData(CellState[][] data) {
 		this.data = data;
 	}
 
 }
