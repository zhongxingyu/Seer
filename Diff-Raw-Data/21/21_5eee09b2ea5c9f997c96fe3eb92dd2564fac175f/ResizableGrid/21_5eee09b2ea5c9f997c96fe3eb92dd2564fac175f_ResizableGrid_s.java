 package utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ResizableGrid<T>{
 
 	private static final int Width_Height = 10;
 	private static final float DefaultWidthGrowthFactor = 2.0f;
 	private static final float DefaultHeightGrowthFactor = 1.1f;
 	
 	private final float widthGrowtFactor;
 	private final float heightGrowtFactor;
 	
 	private T[][] internalGrid;
 	
 	public int gridWidth() {
 		return internalGrid[0].length;
 	}
 	
 	public int gridHeight() {
 		return internalGrid.length;
 	}
 	
 	public ResizableGrid() {
 		this(Width_Height,Width_Height);
 	}
 	
 	public ResizableGrid(int initialGridHeight, int initialGridWidth) {
 		this(initialGridHeight, initialGridWidth,
 						DefaultWidthGrowthFactor,
 						DefaultHeightGrowthFactor);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ResizableGrid(int initialGridHeight, int initialGridWidth,
 						 float heightGrowthFactor,
 						 float widthGrowthFactor) {
 		this.heightGrowtFactor = heightGrowthFactor;
 		this.widthGrowtFactor = widthGrowthFactor;
 		
 		this.internalGrid = (T[][])new Object[initialGridHeight][initialGridWidth];
 	}
 	
 	public T get(int row, int collumn) {
 		return this.internalGrid[row][collumn];
 	}
 	
 	public void set(int row, int collumn, T item) {
 		if(row >= this.gridHeight() ||
 		   collumn >= this.gridWidth()) {
 			grow(row, collumn);
 		}
 		
 		this.internalGrid[row][collumn] = item;
 	}
 
 	public List<T> getRow(int row) {
 		List<T> list = new ArrayList<T>(this.gridWidth());
 		for (int column = 0; column < this.gridWidth(); column++) {
 			list.add(this.internalGrid[row][column]);
 		}
 		return list;
 	}
 	
 	public List<T> getCollumn(int column) {
		List<T> list = new ArrayList<T>(this.gridWidth());
		for (int row = 0; row < this.gridWidth(); row++) {
 			list.add(this.internalGrid[row][column]);
 		}
 		return list;
 		
 	}
 	
 	private void grow(int row, int column) {
 		
 		//Calculate the new amount of rows in the grid.
 		int newRowLength = (int) ((row >= this.gridHeight() * this.heightGrowtFactor)
 								? row + 1: this.gridHeight() * this.heightGrowtFactor);
 		
 		//Calculate the new amount of columns in the grid.
 		int newCollumnLength = (int) ((column >= this.gridWidth() * this.widthGrowtFactor)
 								? column + 1: this.gridWidth() * this.widthGrowtFactor);
 		
 		@SuppressWarnings("unchecked") //Java generic arrays are weird... sigh.
 		T[][] grownGrid = (T[][])new Object[newRowLength][newCollumnLength];
 		copyGrid(grownGrid);
 		
 		this.internalGrid = grownGrid;
 	}
 
 	private void copyGrid(T[][] grownGrid) {
 		for (int rowIndex = 0; rowIndex < this.gridHeight(); rowIndex++) {
 			for (int columnIndex = 0; columnIndex < this.gridWidth(); columnIndex++) {
 				grownGrid[rowIndex][columnIndex] = this.internalGrid[rowIndex][columnIndex];
 			}
 		}
 	}
 
 	public void ensureLargeEnough(int columnLength, int rowLength) {
 		if(columnLength >= this.gridHeight() ||
 		   rowLength >= this.gridWidth()) {
 			this.grow(columnLength, rowLength);
 		}
 	}
 
 	public void clearRow(int row) {
 		for (int column = 0; column < this.gridWidth(); column++) {
 			this.internalGrid[row][column] = null;
 		}
 	}
 	
 	public void clearColumn(int column) {
 		for (int row = 0; row < this.gridHeight(); row++) {
 			this.internalGrid[row][column] = null;
 		}
 	}
 	
 	public void clear() {
 		for (int row = 0; row < this.gridHeight(); row++) {
 			for (int column = 0; column < this.gridWidth(); column++) {
 				this.internalGrid[row][column] = null;
 			}
 		}
 		
 	}
 
 }
