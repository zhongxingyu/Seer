 package mikera.matrixx.impl;
 
 import mikera.matrixx.AMatrix;
 import mikera.vectorz.AVector;
 import mikera.vectorz.Op;
 
 /**
  * Abstract base class for matrices that use a collection of Vectors 
  * as storage for the matrix rows.
  * 
  * Vector matrices support appending with new rows - this functionality can be useful
  * e.g. when building a matrix to represent a data set.
  * 
  * @author Mike
  *
  */
 public abstract class AVectorMatrix<T extends AVector> extends AMatrix {
 	/* ================================
 	 * Abstract interface
 	 */
 	
 	public abstract void appendRow(AVector row);
 	
 	/**
 	 * Gets a row of the matrix. Should be guaranteed to be an existing vector by all
 	 * descendents of VectorMatrix.
 	 */
 	@Override
 	public abstract T getRow(int row);
 
 	@Override
 	public double get(int row, int column) {
 		return getRow(row).get(column);
 	}
 	
 	@Override
 	public double unsafeGet(int row, int column) {
 		return getRow(row).unsafeGet(column);
 	}
 	
	@Override
	public boolean isFullyMutable() {
		int rc=rowCount();
		for (int i=0; i<rc; i++) {
			if (!getRow(i).isFullyMutable()) return false;
		}
		return true;
	}
	
 	@Override 
 	public void set(double value) {
 		int rc=rowCount();
 		for (int i=0; i<rc; i++) {
 			getRow(i).set(value);
 		}
 	}
 
 	@Override
 	public void set(int row, int column, double value) {
 		getRow(row).set(column,value);
 	}
 	
 	@Override
 	public void unsafeSet(int row, int column, double value) {
 		getRow(row).unsafeSet(column,value);
 	}
 	
 	@Override
 	public void transform(AVector source, AVector dest) {
 		int rc=rowCount();
 		for (int i=0; i<rc; i++) {
 			dest.set(i,getRow(i).dotProduct(source));
 		}
 	}
 	
 	@Override
 	public double calculateElement(int i, AVector inputVector) {
 		T row=getRow(i);
 		return row.dotProduct(inputVector);
 	}
 	
 	@Override
 	public void applyOp(Op op) {
 		int rc = rowCount();
 		for (int i = 0; i < rc; i++) {
 			getRow(i).applyOp(op);
 		}
 	}
 	
 	@Override
 	public boolean isView() {
 		return true;
 	}
 	
 	@Override
 	public double elementSum() {
 		int rc=rowCount();
 		double result=0.0;
 		for (int i=0; i<rc; i++) {
 			result+=getRow(i).elementSum();
 		}
 		return result;
 	}
 	
 	@Override
 	public double elementSquaredSum() {
 		int rc=rowCount();
 		double result=0.0;
 		for (int i=0; i<rc; i++) {
 			result+=getRow(i).elementSquaredSum();
 		}
 		return result;
 	}
 	
 	@Override
 	public long nonZeroCount() {
 		int rc=rowCount();
 		long result=0;
 		for (int i=0; i<rc; i++) {
 			result+=getRow(i).nonZeroCount();
 		}
 		return result;
 	}	
 	
 	@Override
 	public AVectorMatrix<?> clone() {
 		AVectorMatrix<?> avm=(AVectorMatrix<?>) super.clone();
 		return avm;
 	}
 }
