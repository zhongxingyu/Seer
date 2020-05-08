 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: PrimitiveMatrix.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Primitive Matrix
  */
 package edu.columbia.mipl.datastr;
 
 import java.util.*;
 
 public class PrimitiveDoubleArray extends PrimitiveArray {
 	double data[];
 
 	public double[] getData() {
 		return data;
 	}
 
 	public PrimitiveDoubleArray(int row, int col) {
 		this(row, col, null);
 	}
 
 	/* Reference Type */
 	PrimitiveDoubleArray(PrimitiveDoubleArray ref) {
 		this.row = ref.row;
 		this.col = ref.col;
 		paddedRow = getPaddedLength(row);
 		paddedCol = getPaddedLength(col);
 		this.data = ref.data;
 	}
 
 	public PrimitiveDoubleArray(int row, int col, double data[]) /* throws UnalignedMatrixSizeException */ {
 		this(row, col, data, false);
 	}
 
 	private void newPrimitiveDoubleArrayWrapper(int row, int col, double data[], boolean padded) {
 		this.row = row;
 		this.col = col;
 
 		paddedRow = getPaddedLength(row);
 		paddedCol = getPaddedLength(col);
 
 		if (!padded) {
//			if (data != null && data.length != row * col)
//				assert (false);
 		}
 		else if (data.length != paddedRow * paddedCol) {
 			assert (false);
 		}
 
 		if (data == null)
 			data = new double[paddedRow * paddedCol];
 		else if (!padded) {
 			int i;
 			int j;
 			double[] newData = new double[paddedRow * paddedCol];
 			for (i = 0; i < row; i++) {
 				for (j = 0; j < col; j++) {
 					newData[i * paddedCol + j] = data[i * col + j];
 				}
 			}
 			data = newData;
 		}
 
 		this.data = data;
 	}
 
 	public PrimitiveDoubleArray(int row, int col, double data[], boolean padded) /* throws UnalignedMatrixSizeException */ {
 		this.newPrimitiveDoubleArrayWrapper(row, col, data, padded);
 	}
 
 	public void reallocateSizeWithNewData(int row, int col, double newData[]) {
 		this.newPrimitiveDoubleArrayWrapper(row, col, newData, true);
 	}
 
 	public void reallocateSize() {
 		data = Arrays.copyOf(data, paddedCol * paddedRow);
 	}
 
 	public void setValue(int row, int col, Object value) {
 		data[flattenIndex(row, col)] = (double) (Double) value;
 	}
 
 	public Object getValue(int row, int col) {
 		return (Object) (Double) data[flattenIndex(row, col)];
 	}
 
 	public void copyRange(PrimitiveArray src, int srcRow, int srcCol, int dstRow, int dstCol, int nRows, int nCols) {
 		PrimitiveDoubleArray source = (PrimitiveDoubleArray) src;
 
 		assert (nRows + dstRow <= row);
 		assert (nCols + dstCol <= col);
 		assert (nRows + srcRow <= source.getRow());
 		assert (nCols + srcCol <= source.getCol());
 
 		int i;
 		int j;
 
 		double[] srcData = source.getData();
 		int srcPaddedCol = source.getPaddedCol();
 
 		if (paddedCol == srcPaddedCol && srcCol == 0 && dstRow == 0 &&
 				nCols == source.getCol() && nCols == col) {
 			System.arraycopy(srcData, srcRow * srcPaddedCol, data, dstRow * paddedCol, paddedCol * nRows);
 			return;
 		}
 
 		for (i = 0; i < nRows; i++) {
 			for (j = 0; j < nCols; j++) {
 				data[(dstRow + i) * paddedCol + dstCol + j] = srcData[(srcRow + i) * srcPaddedCol + srcCol + j];
 			}
 		}
 	}
 
 	public boolean equalsDimensionally(final PrimitiveArray arg) {
 		if (!(arg instanceof PrimitiveDoubleArray))
 			return false;
 
 		return (row == arg.getRow() && col == arg.getCol());
 	}
 
 	public boolean equalsSemantically(final PrimitiveArray arg) {
 		if (!equalsDimensionally(arg))
 			return false;
 
 		PrimitiveDoubleArray a = (PrimitiveDoubleArray) arg;
 
 		double[] data1 = a.getData();
 		int pos;
 		int offset = 0;
 
 		for (int i = 0; i < row; ++i) {
 			pos = offset;
 			for (int j = 0; j < col; ++j) {
 				if (data[pos] != data1[pos])
 					return false;
 				pos++;
 			}
 			offset += paddedRow;
 		}
 
 		return true;
 	}
 
 	public void printMatrix() {
 		int pos;
 		int offset = 0;
 
//		System.out.println("printMatrix : " + row + "  " + col + " " + paddedRow + " " + paddedCol);
		
		System.out.println("data");
		for (int i = 0; i < data.length; i++) {
			System.out.print(data[i] + " " );
			if ( (i+1) % paddedCol == 0) System.out.println();
		}
		
		System.out.println();
 		for (int i = 0; i < row; ++i) {
 			pos = offset;
 			for (int j = 0; j < col; ++j) {
 				System.out.print(data[pos] + " ");
 				pos++;
 			}
//			offset += paddedRow;
			offset += paddedCol;
 			System.out.println("|");
 		}
 		System.out.println("----------------------------");
 	}
 }
