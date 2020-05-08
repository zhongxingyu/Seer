 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Main.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Main
  */
 package edu.columbia.mipl;
 
 import java.io.*;
 import java.util.*;
 
 import edu.columbia.mipl.syntax.*;
 import edu.columbia.mipl.codegen.*;
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.traverse.*;
 
 public class Main {
 	public static void main(String[] args) {
		Program program = new Program(new CodeGenerator());
		Parser parser = new Parser("test/input/multireturn.mipl", program);
 		if (parser.getNumError() != 0) {
 			System.out.println("Error on parsing input!");
 			return;
 		}
 	}
 }
