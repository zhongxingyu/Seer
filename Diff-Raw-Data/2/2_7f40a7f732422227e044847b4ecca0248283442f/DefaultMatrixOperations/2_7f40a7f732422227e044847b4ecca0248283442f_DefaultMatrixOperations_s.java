 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: DefaultMatrixOperations.java
  * Author: Jin Hyung Park <jp2105@columbia.edu>
  * Reviewer: Young Hoon Jung <yj2244@columbia.edu>
  * Description: Matrix Operations Default Implementations
  *
  */
 package edu.columbia.mipl.matops;
 
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.log.*;
 
 public class DefaultMatrixOperations implements MatrixOperations {
 	private Log log;
 
 	public DefaultMatrixOperations() {
 		log = Log.getInstance();
 	}
 
 	boolean checkDimensionMutipliable(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (arg1.getCol() == -1 && arg2.getRow() == -1)
 			return false;
 		if (arg1.getCol() != -1 && arg2.getRow() != -1 && arg1.getCol() != arg2.getRow())
 			return false;
 		if (arg1.getRow() == -1 || arg2.getCol() == -1)
 			return false;
 		return true;
 	}
 
 	boolean checkDimension(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if ((arg1.getCol() == -1 && arg2.getCol() == -1) ||
 			(arg1.getRow() == -1 && arg2.getRow() == -1))
 			return false;
 		if (arg1.getCol() != -1 && arg2.getCol() != -1 && arg1.getCol() != arg2.getCol())
 			return false;
 		if (arg1.getRow() != -1 && arg2.getRow() != -1 && arg1.getRow() != arg2.getRow())
 			return false;
 		return true;
 	}
 
 	boolean containsUnboundMatrix(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		return arg1.getRow() == -1 || arg1.getCol() == -1 ||
			arg2.getRow() == 1 || arg2.getCol() == -1;
 	}
 
 	boolean containsUnboundMatrix(final PrimitiveMatrix arg1) {
 		return arg1.getRow() == -1 || arg1.getCol() == -1;
 	}
 
 	int getRow(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (arg1.getRow() == -1)
 			return arg2.getRow();
 		return arg1.getRow();
 	}
 
 	int getCol(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (arg1.getCol() == -1)
 			return arg2.getCol();
 		return arg1.getCol();
 	}
 
 	public PrimitiveMatrix add(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (!checkDimension(arg1, arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 
 		if (containsUnboundMatrix(arg1, arg2)) {
 			int i, j;
 			int row = getRow(arg1, arg2);
 			int col = getCol(arg1, arg2);
 
 			PrimitiveMatrix result = new PrimitiveMatrix<Double>(new PrimitiveDoubleArray(row, col));
 
 			for (i = 0; i < row; i++) {
 				for (j = 0; j < col; j++) {
 					result.setValue(i, j, (Double) arg1.getValue(i, j) + (Double) arg2.getValue(i, j));
 				}
 			}
 			return result;
 		}
 
 		return new PrimitiveMatrix(add(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix add(final PrimitiveMatrix arg1, double arg2) {
 		if (containsUnboundMatrix(arg1)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Cannot add a value to a unbound matrix.");
 		}
 
 		return new PrimitiveMatrix(add(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix sub(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (!checkDimension(arg1, arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 
 		if (containsUnboundMatrix(arg1, arg2)) {
 			int i, j;
 			int row = getRow(arg1, arg2);
 			int col = getCol(arg1, arg2);
 
 			PrimitiveMatrix result = new PrimitiveMatrix<Double>(new PrimitiveDoubleArray(row, col));
 
 			for (i = 0; i < row; i++) {
 				for (j = 0; j < col; j++) {
 					result.setValue(i, j, (Double) arg1.getValue(i, j) - (Double) arg2.getValue(i, j));
 				}
 			}
 			return result;
 		}
 
 		return new PrimitiveMatrix(sub(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix sub(final PrimitiveMatrix arg1, double arg2) {
 		if (containsUnboundMatrix(arg1)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Cannot subtract a value from a unbound matrix.");
 		}
 
 		return new PrimitiveMatrix(sub(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix cellmult(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (!checkDimension(arg1, arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 
 		if (containsUnboundMatrix(arg1, arg2)) {
 			int i, j;
 			int row = getRow(arg1, arg2);
 			int col = getCol(arg1, arg2);
 
 			PrimitiveMatrix result = new PrimitiveMatrix<Double>(new PrimitiveDoubleArray(row, col));
 
 			for (i = 0; i < row; i++) {
 				for (j = 0; j < col; j++) {
 					result.setValue(i, j, (Double) arg1.getValue(i, j) * (Double) arg2.getValue(i, j));
 				}
 			}
 			return result;
 		}
 
 		return new PrimitiveMatrix(cellmult(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix mult(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (!checkDimensionMutipliable(arg1, arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("To multiply, two input matrices should have same dimensions and squares.");
 		}
 
 		if (containsUnboundMatrix(arg1, arg2)) {
 			int i, j, k;
 			int len = arg1.getCol();
 			if (len == -1)
 				len = arg2.getRow();
 
 			PrimitiveMatrix result = new PrimitiveMatrix<Double>(new PrimitiveDoubleArray(arg1.getRow(), arg2.getCol()));
 
 			for (i = 0; i < arg1.getRow(); i++) {
 				for (j = 0; j < arg2.getCol(); j++) {
 					double sum = 0;
 					for (k = 0; k < len; k++)
 						sum += (Double) arg1.getValue(i, k) * (Double) arg2.getValue(k, j);
 					result.setValue(i, j, sum);
 				}
 			}
 			return result;
 		}
 
 		return new PrimitiveMatrix(mult(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix mult(final PrimitiveMatrix arg1, final double arg2) {
 		if (!containsUnboundMatrix(arg1)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Cannot multiply a value to a unbound matrix.");
 		}
 
 		return new PrimitiveMatrix(mult(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix celldiv(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (!checkDimension(arg1, arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 
 		if (containsUnboundMatrix(arg1, arg2)) {
 			int i, j;
 			int row = getRow(arg1, arg2);
 			int col = getCol(arg1, arg2);
 
 			PrimitiveMatrix result = new PrimitiveMatrix<Double>(new PrimitiveDoubleArray(row, col));
 
 			for (i = 0; i < row; i++) {
 				for (j = 0; j < col; j++) {
 					result.setValue(i, j, (Double) arg1.getValue(i, j) / (Double) arg2.getValue(i, j));
 				}
 			}
 			return result;
 		}
 
 		return new PrimitiveMatrix(celldiv(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix div(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		if (!checkDimensionMutipliable(arg1, arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("To multiply, two input matrices should have same dimensions and squares.");
 		}
 
 		/*
 		if (containsUnboundMatrix(arg1, arg2)) {
 			int i, j, k;
 			int len = arg1.getCol();
 			if (len == -1)
 				len = arg2.getRow();
 
 			PrimitiveMatrix result = new PrimitiveMatrix<Double>(new PrimitiveDoubleArray(arg1.getRow(), arg2.getCol()));
 
 			for (i = 0; i < arg1.getRow(); i++) {
 				for (j = 0; j < arg2.getCol(); j++) {
 					double sum = 0;
 					for (k = 0; k < len; k++)
 						sum += (Double) arg1.getValue(i, k) * (Double) arg2.getValue(k, j);
 					result.setValue(i, j, sum);
 				}
 			}
 			return result;
 		}
 		*/
 
 		return new PrimitiveMatrix(div(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix div(final PrimitiveMatrix arg1, final double arg2) {
 		if (!containsUnboundMatrix(arg1)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Cannot divide a unbound matrix by a value.");
 		}
 
 		return new PrimitiveMatrix(div(arg1.getData(), arg2));
 	}
 
 	public void assign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		assign(arg1.getData(), arg2.getData());
 	}
 
 	public void assign(PrimitiveMatrix arg1, double arg2) {
 		assign(arg1.getData(), arg2);
 	}
 
 	public void addassign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		addassign(arg1.getData(), arg2.getData());
 	}
 
 	public void addassign(PrimitiveMatrix arg1, double arg2) {
 		addassign(arg1.getData(), arg2);
 	}
 
 	public void subassign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		subassign(arg1.getData(), arg2.getData());
 	}
 
 	public void subassign(PrimitiveMatrix arg1, double arg2) {
 		subassign(arg1.getData(), arg2);
 	}
 
 	public void cellmultassign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		cellmultassign(arg1.getData(), arg2.getData());
 	}
 
 	public void multassign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		multassign(arg1.getData(), arg2.getData());
 	}
 
 	public void multassign(PrimitiveMatrix arg1, double arg2) {
 		multassign(arg1.getData(), arg2);
 	}
 
 	public void celldivassign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		celldivassign(arg1.getData(), arg2.getData());
 	}
 
 	public void divassign(PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		divassign(arg1.getData(), arg2.getData());
 	}
 
 	public void divassign(PrimitiveMatrix arg1, double arg2) {
 		divassign(arg1.getData(), arg2);
 	}
 
 
 	public PrimitiveMatrix transpose(final PrimitiveMatrix arg1) {
 		return new PrimitiveMatrix(transpose(arg1.getData()));
 	}
 
 	public PrimitiveMatrix inverse(final PrimitiveMatrix arg1) {
 		return new PrimitiveMatrix(inverse(arg1.getData()));
 	}
 
 
 	public PrimitiveMatrix mod(final PrimitiveMatrix arg1, double arg2) {
 		return new PrimitiveMatrix(mod(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix mod(final PrimitiveMatrix arg1, PrimitiveMatrix arg2) {
 		return new PrimitiveMatrix(mod(arg1.getData(), arg2.getData()));
 	}
 
 	public double sum(final PrimitiveMatrix arg1) {
 		return sum(arg1.getData());
 	}
 
 	public double mean(final PrimitiveMatrix arg1) {
 		return mean(arg1.getData());
 	}
 
 	public PrimitiveMatrix rowsum(final PrimitiveMatrix arg1) {
 		return new PrimitiveMatrix(rowsum(arg1.getData()));
 	}
 
 	public PrimitiveMatrix rowmean(final PrimitiveMatrix arg1) {
 		return new PrimitiveMatrix(rowmean(arg1.getData()));
 	}
 
 	public PrimitiveMatrix abs(final PrimitiveMatrix arg1) {
 		return new PrimitiveMatrix(abs(arg1.getData()));
 	}
 
 	
 	public PrimitiveArray add(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		double[] data1 = a1.getData();
 		double[] data2 = a2.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] + data2[pos];
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 
 		return result;
 	}
 
 	public PrimitiveArray add(final PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		double[] data1 = a1.getData();
 		double[] data = result.getData();
 
 		int i, j, pos = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] + arg2;
 				pos++;
 			}
 		}
 
 		return result;
 	}
 
 	public PrimitiveArray sub(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		if (!arg1.equalsDimensionally(arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		double[] data1 = a1.getData();
 		double[] data2 = a2.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] - data2[pos];
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return result;
 	}
 
 	public PrimitiveArray sub(final PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		double[] data1 = a1.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] - arg2;
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return result;
 	}
 
 	public PrimitiveArray cellmult(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		if (!arg1.equalsDimensionally(arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg2.getCol());
 		double[] data1 = a1.getData();
 		double[] data2 = a2.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] * data2[pos];
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 
 		return result;
 	}
 
 	public PrimitiveArray mult(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg2.getCol());
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < result.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < result.getCol(); j++) {
 				double res = 0;
 				for (int k = 0; k < arg1.getCol(); k++) {
 					double a1val = (Double) a1.getValue(i, k);
 					double a2val = (Double) a2.getValue(k, j);
 					res += a1val * a2val;
 				}
 				data[pos] = res;
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return result;
 	}
 	public PrimitiveArray mult(final PrimitiveArray arg1, final double arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		double[] data1 = a1.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] * arg2;
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return result;
 	}
 
 	public PrimitiveArray celldiv(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		if (!arg1.equalsDimensionally(arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg2.getCol());
 		double[] data1 = a1.getData();
 		double[] data2 = a2.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				if (data2[pos] == 0) {
 					/* throw new divideByZeroException() */
 					log.error("The matrix cannot be divided by zero.");
 					return null;
 				}
 				data[pos] = data1[pos] / data2[pos];
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 
 		return result;
 	}
 
 	public PrimitiveArray div(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		if (!arg1.equalsDimensionally(arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 		if (arg1.getRow() != arg1.getCol()) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("To divide, the column of the matrix should be same the row of the other matrix.");
 		}
 
 		PrimitiveDoubleArray invArg2 = (PrimitiveDoubleArray) this.inverse(arg2);
 		PrimitiveDoubleArray result = 
 			(PrimitiveDoubleArray) this.mult(arg1, (PrimitiveArray) invArg2);
 
 		return result;
 	}
 
 	public PrimitiveArray div(final PrimitiveArray arg1, final double arg2) {
 		if (arg2 == 0) {
 			/* throw new divideByZeroException() */;
 			log.error("The matrix cannot be divided by zero.");
 		}
 
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		double[] data1 = a1.getData();
 		double[] data = result.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				data[pos] = data1[pos] / arg2;
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return result;
 	}
 
 	public void assign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		a1.reallocateSizeWithNewData(a2.getRow(), a2.getCol(), a2.getData());
 	}
 
 	public void assign(PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		for (int i = 0; i < arg1.getRow(); ++i)
 			for (int j = 0; j < arg1.getCol(); ++j)
 				a1.setValue(i, j, arg2);
 	}
 
 	public void addassign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.add(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void addassign(PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.add(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void subassign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.sub(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void subassign(PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.sub(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void cellmultassign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.cellmult(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void multassign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.mult(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void multassign(PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.mult(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void celldivassign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.celldiv(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void divassign(PrimitiveArray arg1, final PrimitiveArray arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.div(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public void divassign(PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray result = (PrimitiveDoubleArray) this.div(arg1, arg2);
 		this.assign(arg1, result);
 	}
 
 	public PrimitiveArray transpose(final PrimitiveArray arg1) {
 		int pos, offset = 0;
 		int row = arg1.getRow();
 		int col = arg1.getCol();
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		double data[] = a1.getData();
 		double dataT[] = new double[data.length];
 		PrimitiveDoubleArray matT = new PrimitiveDoubleArray(col, row, dataT);
 		for (int i = 0; i < row; ++i) {
 			pos = offset;
 			for (int j = 0; j < col; ++j) {
 				matT.setValue(j, i, (Double) data[pos]);
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return matT;
 	}
 	
 	/* Private Functions for calculating inverse matrix */
 	public double determinant(final PrimitiveArray arg1) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		int row = arg1.getRow();
 		int col = arg1.getCol();
 		double[][] temp = new double[row][row];
 
 		for (int i = 0; i < row; i++)
 			for (int j = 0; j < row; j++)
 				temp[i][j] = (Double) a1.getValue(i, j);
 
 		for (int n = 0; n < temp.length; n++) {
 			if (temp[n][n] == 0) {
 				for (int a = n; a < temp.length; a++) {
 					if (temp[a][n] != 0) {
 						for (int b = n; b < temp.length; b++) {
 							temp[n][b] += temp[a][b];
 						}
 						break;
 					}
 				}
 				if (temp[n][n] == 0) {
 					System.out.println(n + "ss");
 					return 0;
 				}
 			}
 			for (int i = n + 1; i < temp.length; i++) {
 				for (int j = temp[0].length - 1; j >= n; j--) {
 					temp[i][j] = temp[i][j] - temp[i][n] * temp[n][j] / temp[n][n];
 				}
 			}
 		}
 		double sum = 1;
 		for (int i = 0; i < temp.length; i++) {
 			sum *= temp[i][i];
 		}
 		return sum;
 	}
 
 	public PrimitiveArray minor(final PrimitiveArray arg1, int removeRow, int removeCol) {
 		int row = arg1.getRow();
 		int col = arg1.getCol();
 		int ar = 0, ac = 0, pos, offset = 0;
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		double data[] = a1.getData();
 		double dataMinor[] = new double[(row - 1) * (col - 1)];
 		PrimitiveDoubleArray coMat = new PrimitiveDoubleArray(row - 1, col - 1, dataMinor);
 		for (int r = 0; r < row; r++) {
 			pos = offset;
 			for (int c = 0; c < col; c++) {
 				if (!(r == removeRow || c == removeCol)) {
 					coMat.setValue(ar, ac, (Double) data[pos]);
 					if (++ac >= col - 1) {
 						if (++ar < row - 1)
 							ac = 0;
 					}
 				}
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 		return coMat;
 	}
 
 	public double cofactor(PrimitiveArray arg1, int row, int col) {
 		PrimitiveArray matI = this.minor(arg1, row, col);
 		return this.determinant(matI) * Math.pow((-1), (row + col));
 	}
 
 	public PrimitiveArray unitMatrix(int k) {
 		PrimitiveDoubleArray r = new PrimitiveDoubleArray(k, k);
 		double[] data = r.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < r.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < r.getCol(); j++) {
 				data[pos] = 1;
 				pos++;
 			}
 			offset += r.getPaddedRow();
 		}
 
 		return r;
 	}
 
 	/* We do not use determinant to calculate inverse.
 	 * this inverse function uses the Gaussian elimination. */
 	public PrimitiveArray inverse(final PrimitiveArray arg1) {
 		int row = arg1.getRow();
 		int col = arg1.getCol();
 		double[][] temp = new double[row][row];
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 
 		for (int i = 0; i < row; i++) {
 			for (int j = 0; j < row; j++) {
 				temp[i][j] = (Double) a1.getValue(i, j);
 			}
 		}
 
 		//PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) this.unitMatrix(col);
 		double[][] comp = new double[col][col];
 		for (int i = 0; i < col; i++) {
 			for (int j = 0; j < col; j++) {
 				if (i == j)
 					comp[i][j] = 1;
 				else
 					comp[i][j] = 0;
 			}
 		}
 
 		for (int n = 0; n < col; n++) {
 			if (temp[n][n] == 0) {
 				for (int k = n; k < col; k++) {
 					if (temp[k][n] != 0) {
 						for (int a = 0; a < col; a++) {
 							comp[n][a] = comp[n][a] + comp[k][a];
 							temp[n][a] = temp[n][a] + temp[k][a];
 						}
 						break;
 					}
 				}
 			}
 			double p = temp[n][n];
 			for (int a = 0; a < col; a++) {
 				comp[n][a] /= p;
 				temp[n][a] /= p;
 			}
 			for (int i = 0; i < col; i++) {
 				if (i != n) {
 					double p1 = temp[i][n];
 					for (int j = col - 1; j >= 0; j--) {
 						comp[i][j] -= p1 * comp[n][j];
 						temp[i][j] -= p1 * temp[n][j];
 					}
 				}
 			}
 		}
 
 		PrimitiveDoubleArray r = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		for (int i = 0; i < arg1.getRow(); ++i)
 			for (int j = 0; j < arg1.getCol(); ++j)
 				r.setValue(i, j, (Double) comp[i][j]);
 
 		return r;
 	}
 
 	public PrimitiveArray mod(final PrimitiveArray arg1, PrimitiveArray arg2) {
 		if (!arg1.equalsDimensionally(arg2)) {
 			/* throw new UncompatiableMatrixDimensionException() */;
 			log.error("Two input matrices should have same dimensions.");
 		}
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray a2 = (PrimitiveDoubleArray) arg2;
 		PrimitiveDoubleArray r = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		for (int i = 0; i < arg1.getRow(); ++i)
 			for (int j = 0; j < arg1.getCol(); ++j) {
 				int v = (int) ((Double) a1.getValue(i, j) / (Double) a2.getValue(i, j));
 				r.setValue(i, j, (Double) a1.getValue(i, j) - (v * (Double) a2.getValue(i, j)));
 			}
 		return r;
 	}
 
 	public PrimitiveArray mod(final PrimitiveArray arg1, double arg2) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray r = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		for (int i = 0; i < arg1.getRow(); ++i)
 			for (int j = 0; j < arg1.getCol(); ++j) {
 				int v = (int) ((Double) a1.getValue(i, j) / arg2);
 				r.setValue(i, j, (Double) a1.getValue(i, j) - (v * arg2));
 			}
 		return r;
 	}
 
 	public double sum(final PrimitiveArray arg1) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		double[] data1 = a1.getData();
 		double result = 0;
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				result += data1[pos];
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 
 		return result;
 	}
 
 	public double mean(final PrimitiveArray arg1) {
 		double sum = this.sum(arg1);
 		return sum / (arg1.getRow() * arg1.getCol());
 	}
 
 	public PrimitiveArray rowsum(final PrimitiveArray arg1) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(1, arg1.getCol());
 		double[] data1 = a1.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				double val = (Double) result.getValue(0, j);
 				val = val + data1[pos];
 				result.setValue(0, j, (Double) val);
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 
 		return result;
 	}
 
 	public PrimitiveArray rowmean(final PrimitiveArray arg1) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(1, arg1.getCol());
 		double[] data1 = a1.getData();
 
 		int i, j, pos, offset = 0;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			pos = offset;
 			for (j = 0; j < arg1.getCol(); j++) {
 				double val = (Double) result.getValue(0, j);
 				val = val + data1[pos];
 				result.setValue(0, j, (Double) val);
 				pos++;
 			}
 			offset += arg1.getPaddedRow();
 		}
 
 		for (j = 0; j < arg1.getCol(); j++) {
 			double val = (Double) result.getValue(0, j);
 			val = val / arg1.getRow();
 			result.setValue(0, j, (Double) val);
 		}
 
 		return result;
 	}
 
 	public PrimitiveArray abs(final PrimitiveArray arg1) {
 		PrimitiveDoubleArray a1 = (PrimitiveDoubleArray) arg1;
 		PrimitiveDoubleArray result = new PrimitiveDoubleArray(arg1.getRow(), arg1.getCol());
 		int i, j;
 
 		for (i = 0; i < arg1.getRow(); i++) {
 			for (j = 0; j < arg1.getCol(); j++) {
 				Double value = (Double) arg1.getValue(i, j);
 				if (value >= 0)
 					result.setValue(i, j, value);
 				else
 					result.setValue(i, j, -value);
 			}
 		}
 
 		return result;
 	}
 }
