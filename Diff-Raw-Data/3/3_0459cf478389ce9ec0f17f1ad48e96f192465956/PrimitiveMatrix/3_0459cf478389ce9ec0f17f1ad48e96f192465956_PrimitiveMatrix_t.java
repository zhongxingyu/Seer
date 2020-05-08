 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: PrimitiveMatrix.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Primitive Matrix
  */
 package edu.columbia.mipl.ds;
 
 import java.util.*;
 import java.lang.reflect.*;
 
 public class PrimitiveMatrix<T> extends PrimitiveType {
 	int row;
 	int col;
 
 	PrimitiveArray data;
 
 	public enum Status {
 		PM_STATUS_INVALID,
 		PM_STATUS_URI_LOCAL,
 		PM_STATUS_URI_REMOTE,
 		PM_STATUS_LOADED_FULL,
 		PM_STATUS_LOADED_SPARSE,
 	};
 	Status status;
 	String uri;
 
 	public static void main(String args[]) {
 		/* Unit Tests */
 		PrimitiveMatrix<Double> pm = new PrimitiveMatrix<Double>(1, 1, true);
 		PrimitiveMatrix<Integer> pm_int = new PrimitiveMatrix<Integer>(100, 1, false);
 		pm_int.setValue(10, 0, 300);
 	}
 
 	Map<Integer, T> sparseList;
 
 	PrimitiveMatrix(int row, int col) {
 		this(row, col, true);
 	}
 
 	PrimitiveMatrix(int row, int col, boolean isSparse) {
 		this.row = row;
 		this.col = col;
 		if (isSparse) {
 			sparseList = new HashMap<Integer, T>();
 			status = Status.PM_STATUS_LOADED_SPARSE;
 		}
 		else {
 			/* Lazy Allocation */
 			status = Status.PM_STATUS_LOADED_FULL;
 		}
 	}
 
 	PrimitiveMatrix(String uri) {
 		this(uri, true);
 	}
 
 	PrimitiveMatrix(String uri, boolean isLocal) {
 		this.uri = uri;
 		if (isLocal) {
 			status = Status.PM_STATUS_URI_LOCAL;
 		}
 		else {
 			status = Status.PM_STATUS_URI_REMOTE;
 		}
 	}
 
 	PrimitiveMatrix(int row, int col, PrimitiveArray data) {
 		setData(row, col, data);
 	}
 
 	int makeHashKey(int row, int col) {
 		return flattenIndex(row, col);
 	}
 
 	int flattenIndex(int row, int col) {
 		return this.col * row + col;
 	}
 
 	boolean checkOutOfBound(int row, int col)
 	{
 		if (row >= this.row || col >= this.col)
 			return true;
 		return false;
 	}
 
 	void setData(int row, int col, PrimitiveArray data) {
 		this.data = data;
 		status = Status.PM_STATUS_LOADED_FULL;
 	}
 
 	void loadMatrix() {
 		if (status == Status.PM_STATUS_URI_LOCAL) {
 			// matrixLoader = MatrixLoaderFactory.getInstance().getMatrixLoader(MatrixType);
 			// data = matrixLoader.loadMatrix(uri);
 			// or
 			// sparseList = matrixLoader.loadMatrix(uri);
 		}
 		// status == REMOTE : similar to LOCAL or MatrixFactory returns a remote matrix loader;
 	}
 
 	void allocateMatrix(T t) {
		if (data != null)
			return;

 		if (t instanceof Double) {
 			data = new PrimitiveDoubleArray(row * col);
 		}
 		else if (t instanceof Integer) {
 			data = new PrimitiveIntArray(row * col);
 		}
 		// throw new UnsupportedTypeException();
 	}
 
 	void setValue(int row, int col, T value) /* throws OutOfBoundExcpetion */ {
 		if (checkOutOfBound(row, col)) {
 			// throw new OutOfBoundException();
 		}
 
 		loadMatrix();
 
 		if (status == Status.PM_STATUS_LOADED_SPARSE) {
 			sparseList.put(makeHashKey(row, col), value);
 		}
 		else if (status == Status.PM_STATUS_LOADED_FULL) {
 			allocateMatrix(value);
 			data.setValue(flattenIndex(row, col), (Object) value);
 		}
 	}
 
 	public T getValue(int row, int col) /* throws OutOfBoundExcpetion */ {
 		if (checkOutOfBound(row, col)) {
 			// throw new OutOfBoundException();
 		}
 
 		loadMatrix();
 
 		if (status == Status.PM_STATUS_LOADED_SPARSE) {
 			return sparseList.get(makeHashKey(row, col));
 		}
 		else if (status == Status.PM_STATUS_LOADED_FULL) {
 			if (data == null)
 				return (T) (Object) 0;
 
 			return (T) data.getValue(flattenIndex(row, col));
 		}
 		// throw new InvalidStatusException();
 		return null;
 	}
 }
