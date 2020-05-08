 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: UnboundColMatrix.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Built-in Unbound Cols Matrix 
  */
 package edu.columbia.mipl.builtin.matrix;
 
 import java.util.*;
 import java.lang.reflect.*;
 
 import edu.columbia.mipl.datastr.*;
 
 public class UnboundColMatrix extends UnboundMatrix {
 
 	public UnboundColMatrix(PrimitiveMatrix<Double> m) {
		if (m.getCol() != 1) {
			System.out.println("Error: ucol() only takes a matrix which has a single column (Nx1)");
 			assert (false);
		}
 
 		setData(m.getData());
 	}
 
 	public PrimitiveArray getData() {
 		return null;
 	}
 
 	public int getRow() {
 		return super.getData().getRow();
 	}
 
 	public  String getName() {
 		return "ucol";
 	}
 
 	public Double getValue(int row, int col) {
 		return (Double) super.getData().getValue(row, 0);
 	}
 }
