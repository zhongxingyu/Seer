 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: ProgramExecutor.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: ProgramExecutor
  */
 package edu.columbia.mipl.runtime.execute;
 
 import java.util.*;
 
 import edu.columbia.mipl.builtin.*;
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.traverse.*;
 
 public class ProgramExecutor extends RuntimeTraverser {
 
 	KnowledgeTable kt = KnowledgeTableFactory.getKnowledgeTable();
 
 	public Method getMethod() {
 		return Method.POST;
 	}
 
 	public ProgramExecutor() {
 	}
 
 	public boolean reachTerm(Term term) {
 		return true;
 	}
 
 	public boolean reachExpression(Expression expr) {
 		return true;
 	}
 
 	public boolean reachFact(Fact fact) {
 		switch(fact.getType()) {
 			case FACT:
 				kt.put(fact.getName(), fact);
 				break;
 			case MATRIXASFACTS:
 				// Reach here only in interpreter/interactive mode.
 				List<PrimitiveType> args = new ArrayList<PrimitiveType>();
 				for (Term term : fact.getTerms()) {
 					if (term.getType() == Term.Type.TERM)
 						// SemanticChecker: check if this is a complex term, and then throw an exception
 						// which means .getArguments().size() != 0
 						args.add(kt.getFactMatrix(term.getName()));
 					else if (term.getType() == Term.Type.NUMBER)
 						args.add(new PrimitiveDouble(term.getValue()));
 					else if (term.getType() == Term.Type.STRING)
 						args.add(new PrimitiveString(term.getName()));
 					else
 						new Exception("Not Implemented! " + term.getType()).printStackTrace();
 				}
 
 				List<PrimitiveType> results;
 				if (BuiltinTable.existJob(fact.getName())) {
 					try {
						results = BuiltinTable.job(fact.getName(), (PrimitiveType[]) args.toArray());
 					} catch (Exception e) {
 						e.printStackTrace();
 						return false;
 					}
 				}
 				else if (kt.get(fact.getName()) != null)
 					results = new JobExecutor((Job) kt.get(fact.getName()).get(0), args).getResults();
 				else {
 					// SemanticChecker: check this
					new Exception("No such defined or builtin job!").printStackTrace();
 					return false;
 				}
 
 				List<String> names = fact.getNames();
 				if (names.size() != results.size())
 					new Exception("Unmatched Variable Numbers").printStackTrace();
 
 				int i = 0;
 				for (PrimitiveType pt : results) {
 					Fact f = new Fact(new Term(Term.Type.MATRIX, names.get(i), results.get(i)));
 					kt.put(f.getName(), f);
 					i++;
 				}
 				break;
 		}
 		return true;
 	}
 
 	public boolean reachRule(Rule rule) {
 		kt.put(rule.getName(), rule);
 		return true;
 	}
 
 	public boolean reachQuery(Query query) {
 		SolvableBinder sb = new SolvableBinder(query.getTerm());
 		if (sb.bind())
 			System.out.println("success");
 		else
 			System.out.println("fail");
 
 		return true;
 	}
 
 	public boolean reachJob(Job job) {
 		kt.put(job.getName(), job);
 		return true;
 	}
 
 	public boolean reachJobStmt(JobStmt jstmt) {
 		return true;
 	}
 
 	public boolean reachJobExpr(JobExpr jexpr) {
 		return true;
 	}
 
 	public void finish() {
 	}
 }
