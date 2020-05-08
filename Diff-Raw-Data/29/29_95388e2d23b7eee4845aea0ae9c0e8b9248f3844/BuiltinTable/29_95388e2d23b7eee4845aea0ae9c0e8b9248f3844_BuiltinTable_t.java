 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: BuiltinTable.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: A Class for Managing Builtin Jobs and Matrices
  */
 package edu.columbia.mipl.builtin;
 
 import java.util.*;
 import java.lang.reflect.*;
 
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.builtin.job.*;
 import edu.columbia.mipl.builtin.matrix.*;
 import edu.columbia.mipl.runtime.execute.*;
 
 public class BuiltinTable {
 	static Map<String, BuiltinJob> jobTable;
 	static Map<String, PrimitiveMatrix<Double>> matrixTable;
 
 	static {
 		// TODO: read directory name from Configuration
 		jobTable = new HashMap<String, BuiltinJob>();
 		matrixTable = new HashMap<String, PrimitiveMatrix<Double>>();
 
 		// TODO: and remove below hard codede loadings
 		jobTable.put("load", new BuiltinLoad());
 		jobTable.put("save", new BuiltinSave());
 		jobTable.put("urow", new BuiltinURow());
 		jobTable.put("ucol", new BuiltinUCol());
 		jobTable.put("abs", new BuiltinAbs());
 		jobTable.put("rowsum", new BuiltinRowSum());
 		jobTable.put("transpose", new BuiltinTranspose());
 
 		matrixTable.put("ones", new UnboundOnes());
		matrixTable.put("zeros", new UnboundZeros());
		matrixTable.put("eye", new UnboundEye());
 	}
 
 	public static boolean existJob(String name) {
 		return (jobTable.get(name) != null);
 	}
 
 	public static List<PrimitiveType> job(String name, PrimitiveType ... args) throws MiplRuntimeException {
 		if (!existJob(name)) {
 			new Exception("No such job!").printStackTrace();
 			return null;
 		}
 
 		return jobTable.get(name).jobImplementation(args);
 	}
 
 	public static boolean existMatrix(String name) {
 		return (matrixTable.get(name) != null);
 	}
 
 	public static PrimitiveMatrix<Double> matrix(String name) {
 		if (!existMatrix(name)) {
 			new Exception("No such matrix!").printStackTrace();
 			return null;
 		}
 
 		return matrixTable.get(name);
 	}
 }
