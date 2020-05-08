 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: JobExecutor.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: JobExecutor
  */
 package edu.columbia.mipl.runtime.execute;
 
 import java.util.*;
 
 import edu.columbia.mipl.builtin.*;
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.traverse.*;
 
 public class JobExecutor {
 	Map<String, PrimitiveType> variableTable;
 	List<PrimitiveType> results;
 
 	public JobExecutor(Job job, List<PrimitiveType> args) {
 		results = new ArrayList<PrimitiveType>();
 
 		variableTable = new HashMap<String, PrimitiveType>();
 
 		List<Term> terms = job.getArgs();
 		if (terms.size() != args.size())
 			throw new RuntimeException();
 
 		int i = 0;
 		for (Term term : terms)
 			variableTable.put(term.getName(), args.get(i++));
 
 		evaluateJob(job);
 	}
 
 	public List<PrimitiveType> getResults() {
 		return results;
 	}
 
 	public void evaluateJob(Job job) {
 		for (JobStmt jobStmt : job.getStmts())
 			evaluateJobStmt(jobStmt);
 	}
 
 	public void evaluateJobStmt(JobStmt jstmt) {
 		PrimitiveBool expr;
 		switch (jstmt.getType()) {
 			case IF:
 				expr = (PrimitiveBool) evaluateJobExpr(jstmt.getExpr());
 				if (expr.getData())
 					evaluateJobStmt(jstmt.getStmt1());
 				else if (jstmt.getStmt2() != null)
 					evaluateJobStmt(jstmt.getStmt2());
 				break;
 			case WHILE:
 				expr = (PrimitiveBool) evaluateJobExpr(jstmt.getExpr());
 				while (expr.getData()) {
 					evaluateJobStmt(jstmt.getStmt1());
 					expr = (PrimitiveBool) evaluateJobExpr(jstmt.getExpr());
 				}
 				break;
 			case DOWHILE:
 				do {
 					evaluateJobStmt(jstmt.getStmt1());
 					expr = (PrimitiveBool) evaluateJobExpr(jstmt.getExpr());
 				} while (expr.getData());
 				break;
 			case COMPOUND:
 				for (JobStmt jobStmt : jstmt.getStmts())
 					evaluateJobStmt(jobStmt);
 				break;
 			case EXPR:
 				evaluateJobExpr(jstmt.getExpr());
 				break;
 			case RETURN:
 				results.add(evaluateJobExpr(jstmt.getExpr()));
 				break;
 		}
 	}
 
 	public PrimitiveType evaluateJobExpr(JobExpr jexpr) {
 		String name;
 		PrimitiveType result;
 		switch (jexpr.getType()) {
 			case ASSIGN:
 				name = jexpr.getName();
 				result = PrimitiveOperations.assign(variableTable.get(name), evaluateJobExpr(jexpr.getExpr1()));
 				variableTable.put(name, result);
 				return result;
 			case MULASSIGN:
 				name = jexpr.getName();
 				result = PrimitiveOperations.mult(variableTable.get(name), evaluateJobExpr(jexpr.getExpr1()));
 				variableTable.put(name, result);
 				return result;
 			case DIVASSIGN:
 				name = jexpr.getName();
 				result = PrimitiveOperations.div(variableTable.get(name), evaluateJobExpr(jexpr.getExpr1()));
 				variableTable.put(name, result);
 				return result;
 			case MODASSIGN:
 				name = jexpr.getName();
 				result = PrimitiveOperations.mod(variableTable.get(name), evaluateJobExpr(jexpr.getExpr1()));
 				variableTable.put(name, result);
 				return result;
 			case ADDASSIGN:
 				name = jexpr.getName();
 				result = PrimitiveOperations.add(variableTable.get(name), evaluateJobExpr(jexpr.getExpr1()));
 				variableTable.put(name, result);
 				return result;
 			case SUBASSIGN:
 				name = jexpr.getName();
 				result = PrimitiveOperations.sub(variableTable.get(name), evaluateJobExpr(jexpr.getExpr1()));
 				variableTable.put(name, result);
 				return result;
 			case OR:
 				return PrimitiveOperations.or((PrimitiveBool) evaluateJobExpr(jexpr.getExpr1()), (PrimitiveBool) evaluateJobExpr(jexpr.getExpr2()));
 			case AND:
 				return PrimitiveOperations.and((PrimitiveBool) evaluateJobExpr(jexpr.getExpr1()), (PrimitiveBool) evaluateJobExpr(jexpr.getExpr2()));
 			case EQ:
 				return PrimitiveOperations.eq(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case NE:
 				return PrimitiveOperations.ne(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case LT:
 				return PrimitiveOperations.lt(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case GT:
 				return PrimitiveOperations.gt(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case LE:
 				return PrimitiveOperations.le(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case GE:
 				return PrimitiveOperations.ge(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case ADD:
 				return PrimitiveOperations.add(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case SUB:
 				return PrimitiveOperations.sub(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case MULT:
 				return PrimitiveOperations.mult(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case DIV:
 				return PrimitiveOperations.div(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case MOD:
 				return PrimitiveOperations.mod(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case MULT_CELL:
 				return PrimitiveOperations.cellmult(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case DIV_CELL:
 				return PrimitiveOperations.celldiv(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 //			case EXP_CELL:
 //				return PrimitiveOperations.cellexp(evaluateJobExpr(jexpr.getExpr1()), evaluateJobExpr(jexpr.getExpr2()));
 			case NEGATE:
 				return new PrimitiveBool(!((PrimitiveBool) evaluateJobExpr(jexpr.getExpr1())).getData());
 			case ARRAY:
 				new Exception("Not Implemented (obsolete)").printStackTrace();
 				return null;
 			case JOBCALL:
 				if (!BuiltinTable.existJob(jexpr.getName())) {
 					// TODO: support nested job call
 					new Exception("No such builtin job! : " + jexpr.getName()).printStackTrace();
 					return null;
 				}
 				List<PrimitiveType> args = new ArrayList<PrimitiveType>();
 				for (JobExpr expr : jexpr.getExprs())
 					args.add(evaluateJobExpr(expr));
 
 				try {
					return BuiltinTable.job(jexpr.getName(), (PrimitiveType[]) args.toArray()).get(0);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return null;
 			case TERM:
 				Term term = jexpr.getTerm();
 				if (term.getType() == Term.Type.VARIABLE)
 					return variableTable.get(term.getName());
 				else if (term.getType() == Term.Type.NUMBER)
 					return new PrimitiveDouble(term.getValue());
 				else if (term.getType() == Term.Type.TERM) {
 					if (term.getArguments().size() == 0) {
 						// Builtin matrix
 						if (!BuiltinTable.existMatrix(term.getName())) {
 							new Exception("No such builtin matrix!").printStackTrace();
 							return new PrimitiveDouble(0.0);
 						}
 						return BuiltinTable.matrix(term.getName());
 					}
 					else {
 						// Nested Job Call
 						new Exception("Nested Job Call" + term.getName() + " is not implemented!").printStackTrace();
 						return new PrimitiveDouble(0.0);
 					}
 				}
 				else {
 					new Exception("This Job expr is not implemented! " + term.getType()).printStackTrace();
 					return new PrimitiveDouble(0.0);
 				}
 			default:
 				return null;
 		}
 	}
 
 	public void finish() {
 	}
 }
