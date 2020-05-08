 /**
  * MIPL: Mining Integrated Programming Language
  *
  * File: InterpreterTest.java
  * Author: Akshai Sarma <as4107@columbia.edu>
  * Reviewer: Wonjoon Song <dws2127@columbia.edu>
  * Description: Tests for the interpreter mode
  */
 
 package edu.columbia.mipl.interpreter;
 
 import java.io.*;
 import java.util.*;
 import java.lang.Runtime;
 
 import junit.framework.TestCase;
 
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.execute.*;
 
 public class InterpreterTest extends TestCase {
 
 	static String testInputPath = "test/input/";
 	static String miplMainCommand = "java -ea -esa -cp build:./lib/bcel-5.2.jar edu.columbia.mipl.Main ";
 	static Runtime runtime;
 
 	public static void main(String args[]) {
 		junit.textui.TestRunner.run(InterpreterTest.class);
 	}
 
 	@Override
 	protected void setUp() {
 		this.runtime = Runtime.getRuntime();
 	}
 
 	public void testExecutionSuccess() throws java.io.IOException, java.lang.InterruptedException {
 		String output;
 		boolean success = true;
 		DataInputStream outputEater;
 
 		String[] inputFiles = new File(testInputPath).list();
 
 		for (int i = 0; i < inputFiles.length; i++) {
			if (inputFiles[i].startsWith(".") || 
				inputFiles[i].startsWith("pagerank.mipl") ||
				inputFiles[i].startsWith("multi_jobs.mipl") ||
				inputFiles[i].startsWith("simple_matrix_op.mipl") ||
				inputFiles[i].startsWith("matrix.mipl") ||
				inputFiles[i].startsWith("classification.mipl"))
 				continue;
 			Process mainOfMIPL = runtime.exec(miplMainCommand + testInputPath + inputFiles[i]);
 			outputEater = new DataInputStream(mainOfMIPL.getInputStream());
 
 			while ((output = outputEater.readLine()) != null)
 				System.out.println(output);
 
			success &= (mainOfMIPL.waitFor() == 0);
			System.out.println(success);
 		}
 		assertTrue(success);
 	}
 }
