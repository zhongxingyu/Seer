 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: InstructionWriter.java
  * Author A: YoungHoon Jung <yj2244@columbia.edu>
  * Author B: Akshai Sarma <as4107@columbia.edu>
  * Author C: Younghoon Jeon <yj2231@columbia.edu>
  * Description: JavaSourceWriter
  */
 package edu.columbia.mipl.codegen;
 
 import java.io.*;
 import java.util.*;
 
 import edu.columbia.mipl.builtin.*;
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.conf.*;
 
 public class JavaSourceWriter extends InstructionWriter {
 	Set<String> declarationList;
 
 	Stack<String> stack;
 	Writer out;
 
 	int idxName = 0;
 	int nTab = 0;
 
 	String jobDeclarations = "";
 
 	private void resetDeclarationList() {
 		declarationList = new HashSet<String>();
 	}
 
 	private String indent(String s) {
 		int i;
 		String tab = "";
 
 		if (s.startsWith("}"))
 			nTab--;
 
 		for (i = 0; i < nTab; i++) {
 			tab += "	";
 		}
 
 		if (s.endsWith("{"))
 			nTab++;
 
 		return tab + s + "\n";
 	}
 
 	private void println(String s) {
 		String[] lines = s.split("\n");
 		try {
 			for (String line : lines) {
 				out.write(indent(line));
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	public JavaSourceWriter() {
 	}
 
 	public void init(String path, String filename) {
 
 		stack = new Stack<String>() /* {
 			public String push(String a) {
 				System.out.println("PUSH: " + a);
 				return super.push(a);
 			}
 			public String pop() {
 				String a = super.pop();
 				System.out.println("POP: " + a);
 				return a;
 			}
 		} */;
 
 		resetDeclarationList();
 
 		File file = new File(path + "/" + filename + ".java");
 		try {
 			out = new BufferedWriter(new FileWriter(file));
 
 			println("import java.io.*;");
 			println("import java.util.*;");
 			println("");
 			
 			println("import edu.columbia.mipl.builtin.*;");
 			println("import edu.columbia.mipl.runtime.*;");
 			println("import edu.columbia.mipl.runtime.execute.*;");
 			println("import edu.columbia.mipl.conf.*;");
 			println("");
 			
 			println("import edu.columbia.mipl.datastr.*;");
 			println("");
 			
 			println("public class " + filename + " {");
 			
 			println("public static void main(String[] args) throws MiplRuntimeException {");
 			
 			// set configuration
 			Configuration conf = Configuration.getInstance();
 			List<String> servers = conf.getServers();
 			println("Configuration conf = Configuration.getInstance();");
 			println("conf.setMode(" + conf.getMode() + ");");
 			for (String server :servers)
 				println("conf.addServer(\"" + server + "\");");
 			
 			println("KnowledgeTable knowledgeTable = KnowledgeTableFactory.getKnowledgeTable();");			
 			println("Program program = new Program(new ProgramExecutor());");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	public String getName() {
 		return "JavaSrc";
 	}
 
 	public void createTerm(Term.Type type, double value) {
 		stack.push("new Term(Term.Type.NUMBER, " + value + ")");
 	}
 
 	public void createTerm(Term.Type type, Term term1, Expression expr1) {
 		String t = stack.pop();
 		stack.push("new Term(Term.Type.IS, " + t + ", " + stack.pop() + ")");
 	// Term.Type.IS
 	}
 
 	public void createTerm(Term.Type type, Expression expr1, Expression expr2) {
 		String e = stack.pop();
 		switch (type) {
 			case EQ:
 				stack.push("new Term(Term.Type.EQ, " + e + ", " + stack.pop() + ")");
 				break;
 			case LT:
 				stack.push("new Term(Term.Type.LT, " + e + ", " + stack.pop() + ")");
 				break;
 			case LE:
 				stack.push("new Term(Term.Type.LE, " + e + ", " + stack.pop() + ")");
 				break;
 			case GT:
 				stack.push("new Term(Term.Type.GT, " + e + ", " + stack.pop() + ")");
 				break;
 			case GE:
 				stack.push("new Term(Term.Type.GE, " + e + ", " + stack.pop() + ")");
 				break;
 			case NE:
 				stack.push("new Term(Term.Type.NE, " + e + ", " + stack.pop() + ")");
 				break;
 		}
 
 	}
 
 	public void createTerm(Term.Type type, String name,
 										PrimitiveMatrix<Double> matrix) {
 		int i;
 		int j;
 		String pa = "primitiveArray" + (idxName++);
 		println("PrimitiveDoubleArray " + pa + " = new PrimitiveDoubleArray(" + matrix.getRow() + ", " + matrix.getCol() + ");");
 
 		for (i = 0; i < matrix.getRow(); i++)
 			for (j = 0; j < matrix.getCol(); j++)
 				println(pa + ".setValue(" + i + ", " + j + ", " + matrix.getValue(i, j) + ");");
 
 		stack.push("new Term(Term.Type.MATRIX, \"" + name + "\", new PrimitiveMatrix<Double>(" + pa + "))");
 	}
 
 	public void createTerm(Term.Type type, Term term1, Term term2) {
 		String t = stack.pop();
 
 		switch (type) {
 			case ANDTERMS:
 				stack.push("new Term(Term.Type.ANDTERMS, " + t + ", " + stack.pop() + ")");
 				break;
 			case ORTERMS:
 				stack.push("new Term(Term.Type.ORTERMS, " + t + ", " + stack.pop() + ")");
 				break;
 		}
 	}
 
 	public void createTerm(Term.Type type, Term term1) {
 	// Term.Type.NOTTERM
 		stack.push("new Term(Term.Type.NOTTERM, " + stack.pop() + ")");
 	}
 
 	public void createTerm(Term.Type type, String name, List<Term> arguments) {
 		int i;
 		String argList = "(List<Term>) null";
 
 		if (arguments.size() > 0) {
 			argList = "listArgs" + (idxName++);
 			println("List<Term> " + argList + " = new ArrayList<Term>();");
 		}
 		for (i = 0; i < arguments.size(); i++) {
 			println(argList + ".add(" + stack.pop() + ");");
 		}
 	
 		switch (type) {
 			case REGEXTERM:
 				stack.push("new Term(Term.Type.REGEXTERM, \"" + name + "\", " + argList + ")");
 				break;
 			case TERM:
 				stack.push("new Term(Term.Type.TERM, \"" + name + "\", " + argList + ")");
 				break;
 		}
 	}
 
 	public void createTerm(Term.Type type, String name) {
 		switch (type) {
 			case VARIABLE:
 				declarationList.add(name);
 				stack.push("new Term(Term.Type.VARIABLE, \"" + name + "\")");
 				break;
 			case QUERYALL:
 				stack.push("new Term(Term.Type.QUERYALL, \"" + name + "\")");
 				break;
 			case REGEXQUERYALL:
 				stack.push("new Term(Term.Type.REGEXQUERYALL, \"" + name + "\")");
 				break;
 			case STRING:
 				stack.push("new Term(Term.Type.STRING, \"" + name + "\")");
 				break;
 		}
 	}
 
 	public void createTerm(Term.Type type, Expression expr1) {
 	// Term.Type.EXPRESSION
 		stack.push("new Term(Term.Type.EXPRESSION, " + stack.pop() + ")");
 
 	}
 
 	public void createExpression(Expression.Type type, Term term1) {
 	// Term.Type.Term
 		stack.push("new Expression(Expression.Type.TERM, " + stack.pop() + ")");
 
 	}
 
 	public void createExpression(Expression.Type type, Expression expr1,
 									Expression expr2) {
 
 		String e1 = stack.pop();
 		String e2 = stack.pop();
 
 		switch (type) {
 			case MINUS:
 				stack.push("new Expression(Expression.Type.MINUS, " + e1 + ", " + e2 + ")");
 				break;
 			case PLUS:
 				stack.push("new Expression(Expression.Type.PLUS, " + e1 + ", " + e2 + ")");
 				break;
 			case MULTI:
 				stack.push("new Expression(Expression.Type.MULTI, " + e1 + ", " + e2 + ")");
 				break;
 			case DIVIDE:
 				stack.push("new Expression(Expression.Type.DIVIDE, " + e1 + ", " + e2 + ")");
 				break;
 		}
 	}
 
 	public void createFact(Fact.Type type, Term term) {
 	// Fact.Type.FACT
 
 		println("program.add(new Fact(" + stack.pop() + "));");
 		println("");
 
 		resetDeclarationList();
 	}
 
 	public void createFact(Fact.Type type, String name, List<String> names,
 							List<Term> terms) {
 	// Fact.Type.MATRIXASFACTS
 	// SHOULD CHECK IF THE JOB IS DEFINED BEFORE THIS FACT OR LATER
 		int i;
 		int namesSize = 0;
 		if (names != null)
 			namesSize = names.size();
 
 		String stringArgs = "";
 		for (i = 0; i < terms.size(); i++) {
 			stack.pop(); // throw away
 			if (i != 0)
 				stringArgs += ", ";
 			if (terms.get(i).getType() == Term.Type.TERM) {
 				// SemanticChecker: check if this is a complex term, and then throw an exception
 				// which means .getArguments().size() != 0
 				stringArgs += "knowledgeTable.getFactMatrix(\"" + terms.get(i).getName() + "\")";
 			}
 			else if (terms.get(i).getType() == Term.Type.NUMBER)
 				stringArgs += "new PrimitiveDouble(" + terms.get(i).getValue() + ")";
 			else if (terms.get(i).getType() == Term.Type.STRING)
 				stringArgs += "new PrimitiveString(\"" + terms.get(i).getName() + "\")";
 			else
 				new Exception("Not Implemented!").printStackTrace();
 		}
 
 		String rvName = "returnVal" + (idxName++);
 		if (BuiltinTable.existJob(name)) {
 			println("List<PrimitiveType> " + rvName + " = BuiltinTable.job(\"" + name + "\", " + stringArgs + ");");
 		}
 		else {
 			println("List<PrimitiveType> " + rvName + " = " + name + "(" + stringArgs + ");");
 		}
 		if (namesSize == 0) {
 			println("if (" + rvName + " != null)");
 			println("       throw new UnmatchedNumberOfReturenException();");
 		}
 		else {
 			println("if (" + rvName + ".size() != " + namesSize + ")");
 			println("	throw new UnmatchedNumberOfReturenException();");
 		}
 
 		for (i = 0; i < namesSize; i++)
 			println("program.add(new Fact(new Term(Term.Type.MATRIX, \"" + names.get(i) + "\", " + rvName + ".get(" + i + "))));");
 		println("");
 
 		resetDeclarationList();
 	}
 
 	public void createRule(Term term, Term source) {
 		String t = stack.pop();
 		String s = stack.pop();
 		println("program.add(new Rule(" + t + ", " + s + "));");
 		println("");
 
 		resetDeclarationList();
 	}
 
 	public void createQuery(Term term) {
 		println("program.add(new Query(" + stack.pop() + "));");
 		println("");
 
 		resetDeclarationList();
 	}
 
 	public void createJob(String name, List<Term> args, List<JobStmt> stmts) {
 		int i;
 
 		String argsString = "";
 		for (i = 0; i < args.size(); i++) {
 			stack.pop(); // throw away
 			if (i != 0)
 				argsString += ", ";
 			argsString += "PrimitiveType " + args.get(i).getName();
 			declarationList.remove(args.get(i).getName());
 		}
 
 		jobDeclarations += "public static List<PrimitiveType> " + name + "(" + argsString + ") throws MiplRuntimeException {\n";
 		jobDeclarations += "List<PrimitiveType> returnVal = new ArrayList<PrimitiveType>();\n";
 		for (String decl : declarationList)
 			jobDeclarations += "PrimitiveType " + decl + " = null;\n";
 	
 		for (i = 0; i < stmts.size(); i++)
 			jobDeclarations += stack.pop();
 
 		jobDeclarations += "return returnVal;\n";
 		jobDeclarations += "}\n";
 
 		resetDeclarationList();
 	}
 
 	public void createJobStmt(JobStmt.Type type, JobExpr expr, JobStmt stmt1,
 								JobStmt stmt2) {
 		String s = "";
 
 		switch (type) {
 			case IF:
 				s += "if (" + stack.pop() + ".getData()) {\n";
 				s += stack.pop();
 				if (stmt2 != null) {
 					s += "} else {\n";
 					s += stack.pop();
 				}
 				s += "}\n";
 				break;
 			case WHILE:
 				s += "while (" + stack.pop() + ".getData()) {\n";
 				s += stack.pop();
 				s += "}\n";
 				break;
 			case DOWHILE:
 				String e = stack.pop();
 				s += "do {\n";
 				s += stack.pop();
 				s += "} while(" + e + ".getData());\n";
 				break;
 		}
 		stack.push(s);
 	}
 
 	public void createJobStmt(JobStmt.Type type, List<JobStmt> stmts) {
 	// JobStmt.Type.COMPOUND
 		String s = "";
 		for (JobStmt js : stmts) {
 			s += stack.pop();
 		}
 		stack.push(s);
 	}
 
 	public void createJobStmt(JobStmt.Type type, JobExpr expr) {
 		switch (type) {
 			case RETURN:
 				stack.push("returnVal.add(" + stack.pop() + ");\n");
 				break;
 			case EXPR:
 				stack.push(stack.pop() + ";\n");
 				break;
 		}
 	}
 
 	public void createJobExpr(JobExpr.Type type, String name, JobExpr expr) {
 		declarationList.add(name);
 		switch (type) {
 			case ASSIGN:
 				stack.push(name + " = PrimitiveOperations.assign(" + name + ", " + stack.pop() + ")");
 				break;
 			case MULASSIGN:
 				stack.push(name + " = PrimitiveOperations.assign(" + name + ", PrimitiveOperations.mult(" + name + ", " + stack.pop() + "))");
 //				stack.push("PrimitiveOperations.multiAssign(" + name + ", " + stack.pop() + ")");
 				break;
 			case DIVASSIGN:
 				stack.push(name + " = PrimitiveOperations.assign(" + name + ", PrimitiveOperations.div(" + name + ", " + stack.pop() + "))");
 //				stack.push("PrimitiveOperations.divAssign(" + name + ", " + stack.pop() + ")");
 				break;
 			case MODASSIGN:
 				stack.push(name + " = PrimitiveOperations.assign(" + name + ", PrimitiveOperations.mod(" + name + ", " + stack.pop() + "))");
 //				stack.push("PrimitiveOperations.modAssign(" + name + ", " + stack.pop() + ")");
 				break;
 			case ADDASSIGN:
 				stack.push(name + " = PrimitiveOperations.assign(" + name + ", PrimitiveOperations.add(" + name + ", " + stack.pop() + "))");
 //				stack.push("PrimitiveOperations.addAssign(" + name + ", " + stack.pop() + ")");
 				break;
 			case SUBASSIGN:
 				stack.push(name + " = PrimitiveOperations.assign(" + name + ", PrimitiveOperations.sub(" + name + ", " + stack.pop() + "))");
 //				stack.push("PrimitiveOperations.subAssign(" + name + ", " + stack.pop() + ")");
 				break;
 		}
 	}
 
 	public void createJobExpr(JobExpr.Type type, JobExpr expr1,	JobExpr expr2) {
 		String e = stack.pop();
 		switch (type) {
 			case OR:
 				stack.push("PrimitiveOperations.or(" + e + ", " + stack.pop() + ")");
 				break;
 			case AND:
 				stack.push("PrimitiveOperations.and(" + e + ", " + stack.pop() + ")");
 				break;
 			case EQ:
 				stack.push("PrimitiveOperations.eq(" + e + ", " + stack.pop() + ")");
 				break;
 			case NE:
 				stack.push("PrimitiveOperations.ne(" + e + ", " + stack.pop() + ")");
 				break;
 			case LT:
 				stack.push("PrimitiveOperations.lt(" + e + ", " + stack.pop() + ")");
 				break;
 			case GT:
 				stack.push("PrimitiveOperations.gt(" + e + ", " + stack.pop() + ")");
 				break;
 			case LE:
 				stack.push("PrimitiveOperations.le(" + e + ", " + stack.pop() + ")");
 				break;
 			case GE:
 				stack.push("PrimitiveOperations.ge(" + e + ", " + stack.pop() + ")");
 				break;
 			case ADD:
 				stack.push("PrimitiveOperations.add(" + e + ", " + stack.pop() + ")");
 				break;
 			case SUB:
 				stack.push("PrimitiveOperations.sub(" + e + ", " + stack.pop() + ")");
 				break;
 			case MULT:
 				stack.push("PrimitiveOperations.mult(" + e + ", " + stack.pop() + ")");
 				break;
 			case DIV:
 				stack.push("PrimitiveOperations.div(" + e + ", " + stack.pop() + ")");
 				break;
 			case MOD:
 				stack.push("PrimitiveOperations.mod(" + e + ", " + stack.pop() + ")");
 				break;
 			case MULT_CELL:
 				stack.push("PrimitiveOperations.cellmult(" + e + ", " + stack.pop() + ")");
 				break;
 			case DIV_CELL:
 				stack.push("PrimitiveOperations.celldiv(" + e + ", " + stack.pop() + ")");
 				break;
 			case EXP_CELL:
 				stack.push("PrimitiveOperations.cellexp(" + e + ", " + stack.pop() + ")");
 				break;
 		}
 	}
 
 	public void createJobExpr(JobExpr.Type type, JobExpr expr1) {
 	// JobExpr.Type.NEGATE
 		stack.push("new PrimitiveBool(!((PrimitiveBool)" + stack.pop() + ").getData())");
 	}
 
 	public void createJobExpr(JobExpr.Type type, Term term,
 										List<ArrayIndex> indices1,
 										List<ArrayIndex> indices2) {
 	// JobExpr.Type.ARRAY
 //		stack.push("new PrimitiveDouble((Double) " + stack.pop() + ".getValue(
 
 	}
 
 	public void createJobExpr(JobExpr.Type type, String name,
 										List<JobExpr> exprs) {
 		// JobExpr.Type.JOBCALL
 		// Builtin jobs
 		if (!BuiltinTable.existJob(name)) {
 			// TODO: support nested job call
 			new Exception("No such builtin job! : " + name).printStackTrace();
 			return;
 		}
 		int i;
 		String args = "";
 		for (i = 0; i < exprs.size(); i++) {
 			args += stack.pop();
 			if (i != exprs.size() - 1)
 				args += ", ";
 		}
 		stack.push("BuiltinTable.job(\"" + name + "\", " + args + ").get(0)");
 	}
 
 	public void createJobExpr(JobExpr.Type type, Term term) {
 	// JobExpr.Type.TERM
 		stack.pop(); // throw away
 		if (term.getType() == Term.Type.VARIABLE)
 			stack.push(term.getName());
 		else if (term.getType() == Term.Type.NUMBER)
 			stack.push("new PrimitiveDouble(" + term.getValue() + ")");
 		else if (term.getType() == Term.Type.TERM) {
 			if (term.getArguments().size() == 0) {
 				// Builtin matrix
 				if (!BuiltinTable.existMatrix(term.getName())) {
 					new Exception("No such builtin matrix!").printStackTrace();
 					stack.push("new PrimitiveDouble(0.0)");
 				}
 				stack.push("BuiltinTable.matrix(\"" + term.getName() + "\")");
 			}
 			else {
 				// Nested Job Call
 				new Exception("Nested Job Call" + term.getName() + " is not implemented!").printStackTrace();
 				stack.push("new PrimitiveDouble(0.0)");
 			}
 		}
 		else {
 			new Exception("This Job expr is not implemented!").printStackTrace();
 			stack.push("new PrimitiveDouble(0.0)");
 		}
 	}
 
 	public void finish() {
 		try {
 			println("}");
 			println("");
 			println(jobDeclarations);
 			println("}");
 			out.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 }
