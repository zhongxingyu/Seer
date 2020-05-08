 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: RuntimeTraverser.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: RuntimeTraverser
  */
 package edu.columbia.mipl.runtime.traverse;
 
 import java.util.*;
 
 import edu.columbia.mipl.runtime.*;
 
 public abstract class RuntimeTraverser implements Traverser {
 	public boolean reach(Traversable t) {
 		if (t instanceof Term) {
 			return reachTerm((Term) t);
 		}
 		else if (t instanceof Expression) {
 			return reachExpression((Expression) t);
 		}
 		else if (t instanceof Fact) {
 			return reachFact((Fact) t);
 		}
 		else if (t instanceof Rule) {
 			return reachRule((Rule) t);
 		}
 		else if (t instanceof Query) {
 			return reachQuery((Query) t);
 		}
 		else if (t instanceof Job) {
 			return reachJob((Job) t);
 		}
 		else if (t instanceof JobStmt) {
 			return reachJobStmt((JobStmt) t);
 		}
 		else if (t instanceof JobExpr) {
 			return reachJobExpr((JobExpr) t);
 		}
 
 		// TODO: print error log
 		return false;
 	}
 
 	public abstract boolean reachTerm(Term term);
 	public abstract boolean reachExpression(Expression expr);
 	public abstract boolean reachFact(Fact fact);
 	public abstract boolean reachRule(Rule rule);
 	public abstract boolean reachQuery(Query query);
 	public abstract boolean reachJob(Job job);
 	public abstract boolean reachJobStmt(JobStmt jstmt);
 	public abstract boolean reachJobExpr(JobExpr jexpr);
 }
