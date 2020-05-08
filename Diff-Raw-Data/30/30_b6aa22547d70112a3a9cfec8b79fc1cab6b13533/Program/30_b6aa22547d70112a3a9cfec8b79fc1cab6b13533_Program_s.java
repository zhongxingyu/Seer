 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Program.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Program
  */
 package edu.columbia.mipl.runtime;
 
 import java.util.*;
 
 import edu.columbia.mipl.runtime.traverse.*;
 
 public final class Program extends Traversable {
 	Traverser traverser = null;
 
 	public Program() {
 	}
 
 	public Program(Traverser traverser) {
 		this.traverser = traverser;
 	}
 
 	public boolean add(Command c) {
 		if (traverser != null)
 			c.traverse(traverser, true);
		return super.add(c);
 	}
 
 	public void finish() {
 		if (traverser != null)
 			traverser.finish();
 	}
 }
