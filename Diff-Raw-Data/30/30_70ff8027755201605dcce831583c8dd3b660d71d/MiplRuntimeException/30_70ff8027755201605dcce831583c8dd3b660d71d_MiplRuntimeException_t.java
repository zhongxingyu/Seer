 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: MiplRuntimeException.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: MiplRuntimeException
  */
 package edu.columbia.mipl.runtime.execute;
 
 import java.util.*;
 
 public class MiplRuntimeException extends Exception {
	String name;

	MiplRuntimeException() {
		name = "MiplRuntimeException";
	}

	MiplRuntimeException(String msg) {
		name = msg;
	}

	String getName() {
		return name;
	}
 }
