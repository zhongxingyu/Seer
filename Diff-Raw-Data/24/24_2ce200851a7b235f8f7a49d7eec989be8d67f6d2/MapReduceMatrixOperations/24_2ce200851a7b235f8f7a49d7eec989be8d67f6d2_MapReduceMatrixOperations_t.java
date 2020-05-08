 /**
  * MIPL: Mining Integrated Programming Language
  *
  * File: MapReduceMatrixOperations.java
  * Author: 
  * Reviewer: 
  * Description: Matrix Operations Implementations with OpenCL
  *
  */
 package edu.columbia.mipl.matops;
 
 import java.io.File;
 
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.mapreduce.MapReduceProxy;
 
 public class MapReduceMatrixOperations extends ClMatrixOperations {
 
 	static {
 		/* Initializations */
 	}
 
 	boolean checkDimensionSame(final PrimitiveArray arg1, final PrimitiveArray arg2) {
 		return (arg1.getRow() == arg2.getRow() && arg1.getCol() == arg2.getCol());
 	}
 
 	public PrimitiveMatrix add(PrimitiveMatrix arg1,  PrimitiveMatrix arg2) {
 
 		MapReduceProxy mapred = new MapReduceProxy();
 
 		mapred.add(arg1.getURI(), arg2.getURI(), "temp");
 
 
 		PrimitiveMatrix matrix = new PrimitiveMatrix("temp/part-00000", true);
 		
 		matrix.moveMatrix();
 
 		cleanup();
 		
 		return matrix;
 		//mapred.add(arg1., inputPath2, outputPath)
 
 		//		mapred.
 		//		return null;
 		//return result;
 	}
 	
	public void cleanup() {
 		File folder = new File("temp");
 		if (folder.exists()) {
 			for (File c : folder.listFiles())
 				c.delete();
 			folder.delete();
 		}
 	}
 
 	/*
 	public PrimitiveMatrix add(final PrimitiveMatrix arg1, double arg2) {
 		return new PrimitiveMatrix(add(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix sub(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		return new PrimitiveMatrix(sub(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix sub(final PrimitiveMatrix arg1, double arg2) {
 		return new PrimitiveMatrix(sub(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix cellmult(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		return new PrimitiveMatrix(cellmult(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix mult(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		return new PrimitiveMatrix(mult(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix mult(final PrimitiveMatrix arg1, final double arg2) {
 		return new PrimitiveMatrix(mult(arg1.getData(), arg2));
 	}
 
 	public PrimitiveMatrix celldiv(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		return new PrimitiveMatrix(celldiv(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix div(final PrimitiveMatrix arg1, final PrimitiveMatrix arg2) {
 		return new PrimitiveMatrix(div(arg1.getData(), arg2.getData()));
 	}
 
 	public PrimitiveMatrix div(final PrimitiveMatrix arg1, final double arg2) {
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
 	*/
 	
 //	PrimitiveArray add(final PrimitiveArray arg1, double arg2);
 
 //	PrimitiveArray sub(final PrimitiveArray arg1, final PrimitiveArray arg2);
 
 //	PrimitiveArray sub(final PrimitiveArray arg1, double arg2);
 
 //	PrimitiveArray cellmult(final PrimitiveArray arg1, final PrimitiveArray arg2);
 //	PrimitiveArray mult(final PrimitiveArray arg1, final PrimitiveArray arg2);
 //	PrimitiveArray mult(final PrimitiveArray arg1, final double arg2);
 
 //	PrimitiveArray celldiv(final PrimitiveArray arg1, final PrimitiveArray arg2);
 //	PrimitiveArray div(final PrimitiveArray arg1, final PrimitiveArray arg2);
 //	PrimitiveArray div(final PrimitiveArray arg1, final double arg2);
 
 //	void assign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void addassign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void addassign(PrimitiveArray arg1, double arg2);
 //	void subassign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void subassign(PrimitiveArray arg1, double arg2);
 
 //	void cellmultassign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void multassign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void multassign(PrimitiveArray arg1, double arg2);
 //	void celldivassign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void divassign(PrimitiveArray arg1, final PrimitiveArray arg2);
 //	void divassign(PrimitiveArray arg1, double arg2);
 
 //	PrimitiveArray transpose(final PrimitiveArray arg1);
 //	PrimitiveArray inverse(final PrimitiveArray arg1);
 
 //	double sum(final PrimitiveArray arg1);
 //	double mean(final PrimitiveArray arg1);
 //	PrimitiveArray rowsum(final PrimitiveArray arg1);
 //	PrimitiveArray rowmean(final PrimitiveArray arg1);
 }
