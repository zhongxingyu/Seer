 package wind;
 
 public class WindMatrix {
 	private WindCell[][] _cells;
 
 	public WindMatrix(int cols, int rows) {
 		_cells = new WindCell[cols][rows];
 		for(int c=0; c<cols; c++)
 			for (int r=0; r<rows; r++)
 				_cells[c][r] = new WindCell(c, r);
 	}
 	
 	public WindCell[][] getCells() {
 		return _cells;
 	}
 
 	public void setCells(WindCell[][] _cells) {
 		this._cells = _cells;
 	}
 
 	public WindCell getCell(int col, int row) {
 		if (indexInBounds(col, row)) return _cells[col][row];
 		return new WindCell(col, row);
 	}
 
 	public boolean indexInBounds(int col, int row) {
 		if ((col < 0) || (col >= _cells.length) || (row < 0) || (row >= _cells[0].length)) return false;
 		return true;
 	}
 
 	public WindCell getLeft(int col, int row) {
 		return getCell(col-1,row);
 	}
 
 	public WindCell getRight(int col, int row) {
 		return getCell(col+1,row);
 	}
 
 	public void replaceCell(int col, int row, WindCell cell) {
 		_cells[col][row] = cell;
 	}
 
 	public void setCell(int col, int row, float x, float y) {
 		_cells[col][row].get_wind().x = x;
 		_cells[col][row].get_wind().y = y;
 	}
 
 	public float energy() {
 		float energy = 0.0f;
		for(WindCell[] col : _cells)
			for(WindCell cell : col)
 				energy = energy + cell.get_wind().mag();
 		return energy;
 	}
 
 }
