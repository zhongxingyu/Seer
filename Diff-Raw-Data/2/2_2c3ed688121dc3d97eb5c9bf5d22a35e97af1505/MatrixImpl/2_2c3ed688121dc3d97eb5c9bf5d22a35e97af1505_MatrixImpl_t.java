 package de.echox.hacklace.pix0lat0r.data;
 
 public class MatrixImpl implements Matrix {
 
 	private boolean[][] matrix;
 	
 	public MatrixImpl(int width, int height) {
 		this.matrix = new boolean[width][height];
 	}
 	
 	public MatrixImpl(Matrix matrix) {
		this.matrix = matrix.getData().clone();
 	}
 	
 	@Override
 	public void setPixel(int x, int y) {
 		matrix[x][y] = true;
 	}
 
 	@Override
 	public void unsetPixel(int x, int y) {
 		matrix[x][y] = false;
 	}
 	
 	@Override
 	public boolean getPixel(int x, int y) {
 		return matrix[x][y];
 	}
 
 	@Override
 	public void togglePixel(int x, int y) {
 		this.matrix[x][y] = (!getPixel(x, y));
 	}
 
 	@Override
 	public boolean[][] getData() {
 		return this.matrix;
 	}
 	
 	//TODO implement real cloning
 	public Matrix fullClone() {
 		return new MatrixImpl(this);
 	}
 
 }
