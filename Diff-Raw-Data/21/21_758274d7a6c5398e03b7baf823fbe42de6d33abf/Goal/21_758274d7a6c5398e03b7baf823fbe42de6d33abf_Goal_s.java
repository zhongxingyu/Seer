 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Goal.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Goal
  */
 package edu.columbia.mipl.runtime.execute;
 
 import java.util.*;
 
 import edu.columbia.mipl.runtime.*;
 
 public class Goal extends Stack<Term> {
 	Map<String, Term> initialVariableMap;
 
 	Goal(Term initialGoal) {
 		push(initialGoal);
 	}
 
 	Goal(Goal clone) {
 		addAll(clone);
 		initialVariableMap = clone.initialVariableMap;
 	}
 
 	void setInitialVariableMap(Map<String, Term> map) {
 		initialVariableMap = map;
 	}
 }
