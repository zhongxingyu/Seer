 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 
 class Expr {
 
 	public Expr() {
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return new Number(0);
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
             return null;
 	}
 }
 
 class ValueType extends Expr {
 	public ValueType() {
 		super();
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
             return null;
 	}
 
 	public String toString(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return new String("");
 	}
 }
 
 class Ident extends Expr {
 
 	private String name;
 
 	public Ident(String s) {
 		name = s;
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
              return function.getVariable(name);
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return nametable.get(name);
 	}
 }
 
 class Number extends ValueType {
 
 	private Integer value;
 
 	public Number(int n) {
 		value = new Integer(n);
 	}
 
 	public Number(Integer n) {
 		value = n;
 	}
 
 	public Number(String n) {
 		value = Integer.parseInt(n);
 	}
 
 	public int intValue() {
 		return value.intValue();
 	}
 
 	public String toString(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return value.toString();
 	}
 
 	public static boolean numberp(ValueType vt) {
 		return (vt instanceof Number);
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
             return st.addConstant(value);
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return this;
 	}
 }
 
 class Times extends Expr {
 
 	private Expr expr1, expr2;
 
 	public Times(Expr op1, Expr op2) {
 		expr1 = op1;
 		expr2 = op2;
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
             Symbol a =  expr1.translate(st, ft, function);
             Symbol b =  expr2.translate(st, ft, function);
             function.add(Instruction.Load(a));
             function.add(Instruction.Mul(b));
 	    Symbol c = function.addTemp();
             function.add(Instruction.Store(c));
             return c;
	}            
}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		// FIXME: need to do type checking for other operators (+,-) and write
 		// better exception text
 		ValueType vt1 = expr1.eval(nametable, functiontable);
 		ValueType vt2 = expr2.eval(nametable, functiontable);
 		if (!Number.numberp(vt1)) {
 			throw new RuntimeException("ERROR");
 		} else if (!Number.numberp(vt2)) {
 			throw new RuntimeException("ERROR");
 		}
 		return new Number(((Number) vt1).intValue() * ((Number) vt2).intValue());
 	}
 }
 
 class Not extends Expr {
 
 	private Expr expr1;
 
 	public Not(Expr op1) {
 		expr1 = op1;
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
 /* TODO: This
 */
 return null;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 
 		int value = ((Number) expr1.eval(nametable, functiontable))
 				.intValue();
 		if (value <= 0) {
 			value = 1;
 		} else {
 			value = 0;
 		}
 		return new Number(value);
 	}
 }
 
 class Plus extends Expr {
 
 	private Expr expr1, expr2;
 
 	public Plus(Expr op1, Expr op2) {
 		expr1 = op1;
 		expr2 = op2;
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
             Symbol a =  expr1.translate(st, ft, function);
             Symbol b =  expr2.translate(st, ft, function);
             function.add(Instruction.Load(a));
             function.add(Instruction.Add(b));
 	    Symbol c = function.addTemp();
             function.add(Instruction.Store(c));
             return c;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		Number e1 = ((Number) expr1.eval(nametable, functiontable));
 		Number e2 = ((Number) expr2.eval(nametable, functiontable));
 
 		return new Number(e1.intValue() + e2.intValue());
 	}
 }
 
 class Minus extends Expr {
 
 	private Expr expr1, expr2;
 
 	public Minus(Expr op1, Expr op2) {
 		expr1 = op1;
 		expr2 = op2;
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
             Symbol a =  expr1.translate(st, ft, function);
             Symbol b =  expr2.translate(st, ft, function);
             function.add(Instruction.Load(a));
             function.add(Instruction.Subtract(b));
             Symbol c = function.addTemp();
             function.add(Instruction.Store(c));
             return c;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		Number e1 = ((Number) expr1.eval(nametable, functiontable));
 		Number e2 = ((Number) expr2.eval(nametable, functiontable));
 
 		return new Number(e1.intValue() - e2.intValue());
 	}
 }
 
 // added for 2c
 class FunctionCall extends Expr {
 
 	private String funcid;
 	private ExpressionList explist;
 
 	public FunctionCall(String id, ExpressionList el) {
 		funcid = id;
 		explist = el;
 	}
 
 	public Symbol translate(SymbolTable st, FunctionTable ft, Function function) {
 	    Function callFunction = ft.get(funcid);
 		//TODO: this
             return null; 
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return functiontable.get(funcid).apply(nametable, functiontable, explist);
 	}
 }
 
 abstract class Statement {
 
 	public Statement() {
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) throws Exception {
 	}
 }
 
 // added for 2c
 class DefineStatement extends Statement {
 
 	private String name;
 	private Proc proc;
 	private ParamList paramlist;
 	private StatementList statementlist;
 
 	public DefineStatement(String id, Proc process) {
 		name = id;
 		proc = process;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
 	    Symbol label = st.createLabel();
             Function newFunction = new Function(name, label);
 	    proc.translate(st, ft, newFunction);
 	    ft.addFunction(newFunction);
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functable) {
 		// get the named proc object from the function table.
 		// System.out.println("Adding Process:"+name+" to Functiontable");
 		functable.put(name, proc);
 	}
 }
 
 class ReturnStatement extends Statement {
 
 	private Expr expr;
 
 	public ReturnStatement(Expr e) {
 		expr = e;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
             // TODO
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) throws Exception {
 		// Java can't throw exceptions of numbers, so we'll convert it to a
 		// string
 		// and then on the other end we'll reconvert back to Integer..
 		throw new Exception(expr.eval(nametable, functiontable)
 				.toString(nametable, functiontable));
 	}
 }
 
 class AssignStatement extends Statement {
 
 	private String name;
 	private Expr expr;
 
 	public AssignStatement(String id, Expr e) {
 		name = id;
 		expr = e;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
             Symbol c = function.getVariable(name); 
             function.add(Instruction.Store(c));
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		ValueType vt = expr.eval(nametable, functiontable);
 		nametable.put(name, vt);
 	}
 }
 
 class IfStatement extends Statement {
 
 	private Expr expr;
 	private StatementList stmtlist1, stmtlist2;
 
 	public IfStatement(Expr e, StatementList list1, StatementList list2) {
 		expr = e;
 		stmtlist1 = list1;
 		stmtlist2 = list2;
 	}
 
 	public IfStatement(Expr e, StatementList list) {
 		expr = e;
 		stmtlist1 = list;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
             Symbol c = expr.translate(st, ft, function);
 
             Symbol firstLabel = st.createLabel();
             Symbol secondLabel = st.createLabel();
 
 	    function.add(Instruction.Load(c));
 	    function.add(Instruction.JumpNegative(firstLabel));
 	    function.add(Instruction.JumpZero(firstLabel));
 	    stmtlist1.translate(st, ft, function);
 
 	    function.add(Instruction.Jump(secondLabel));
 	    function.add(firstLabel, Instruction.NOP());
 	    stmtlist2.translate(st, ft, function);
 	    function.add(secondLabel, Instruction.NOP());
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) throws Exception {
 		ValueType result = expr.eval(nametable, functiontable);
 		if (!Number.numberp(result)) {
 			throw new RuntimeException(
 					"Expression in if statement does not evaluate to a Number!.");
 		}
 
 		 if (((Number) result).intValue() > 0) {
 		 stmtlist1.eval(nametable, functiontable);
 		 } else {
 		 stmtlist2.eval(nametable, functiontable);
 		 }
 	}
 }
 
 class WhileStatement extends Statement {
 
 	private Expr expr;
 	private StatementList stmtlist;
 
 	public WhileStatement(Expr e, StatementList list) {
 		expr = e;
 		stmtlist = list;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
             Symbol firstLabel = st.createLabel();
 	    Symbol secondLabel = st.createLabel();
 
 	    Symbol c = expr.translate(st, ft, function);
 	    function.add(firstLabel, Instruction.Load(c));
 
 	    function.add(Instruction.JumpNegative(secondLabel));
 	    function.add(Instruction.JumpZero(secondLabel));
 
 	    stmtlist.translate(st, ft, function);
 
 	    function.add(Instruction.Jump(firstLabel));
 	    function.add(secondLabel, Instruction.NOP());
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) throws Exception {
 
 		ValueType result = expr.eval(nametable, functiontable);
 
 		if (!Number.numberp(result)) {
 			throw new RuntimeException(
 					"Expression in while statement does not evaluate to a Number!.");
 		}
 
 		 while (((Number) expr.eval(nametable, functiontable))
 		 .intValue() > 0) {
 		 stmtlist.eval(nametable, functiontable);
 		 }
 	}
 }
 
 class RepeatStatement extends Statement {
 
 	private Expr expr;
 	private StatementList sl;
 
 	public RepeatStatement(StatementList list, Expr e) {
 		expr = e;
 		sl = list;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
 		// TODO:
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) throws Exception {
 		ValueType result = expr.eval(nametable, functiontable);
 		if (!Number.numberp(result)) {
 			throw new RuntimeException(
 					"Expression in if statement does not evaluate to a Number!.");
 		}
 		do {
 			sl.eval(nametable, functiontable);
 		} while (((Number) expr.eval(nametable, functiontable))
 				.intValue() > 0);
 
 	}
 }
 
 // added for 2c
 class ParamList {
 
 	private LinkedList<String> parameterlist;
 
 	public ParamList() {
 		parameterlist = new LinkedList<String>();
 	}
 
 	public ParamList(String name) {
 		parameterlist = new LinkedList<String>();
 		parameterlist.add(name);
 	}
 
 	public ParamList(String name, ParamList parlist) {
 		parameterlist = parlist.getParamList();
 		parameterlist.add(name);
 	}
 
 	public LinkedList<String> getParamList() {
 		return parameterlist;
 	}
 }
 
 // Added for 2c
 class ExpressionList {
 
 	private LinkedList<Expr> list;
 
 	public ExpressionList(Expr ex) {
 		list = new LinkedList<Expr>();
 		list.add(ex);
 	}
 
 	public ExpressionList(Expr ex, ExpressionList el) {
 		list = new LinkedList<Expr>();
 		// we need ot add the expression to the front of the list
 		list.add(0, ex);
 
 	}
 
 	public LinkedList<Expr> getExpressions() {
 		return list;
 	}
 }
 
 class StatementList {
 
 	private LinkedList<Statement> statementlist;
 
 	public StatementList(Statement statement) {
 		statementlist = new LinkedList<Statement>();
 		statementlist.add(statement);
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
 	    for (Statement stmt : statementlist) {
 	        stmt.translate(st, ft, function);
 	    }
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) throws Exception {
 
 		for (Statement stmt : statementlist) {
 			stmt.eval(nametable, functiontable);
 		}
 	}
 
 	public void insert(Statement s) {
 		// we need to add it to the front of the list
 		statementlist.add(0, s);
 	}
 
 	public LinkedList<Statement> getStatements() {
 		return statementlist;
 	}
 }
 
 class Proc {
 
 	private ParamList parameterlist;
 	private StatementList stmtlist;
 
 	public Proc(ParamList pl, StatementList sl) {
 		parameterlist = pl;
 		stmtlist = sl;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft, Function function) {
 		Iterator<String> p = parameterlist.getParamList().iterator();
 		while(p.hasNext()) {
 			String param = p.next();
 			function.addParameter(param);
 		}
 		stmtlist.translate(st, ft, function);
 	}
 
 	public ValueType apply(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable, ExpressionList expressionlist) {
 		// System.out.println("Executing Proceedure");
 		HashMap<String, ValueType> newnametable = new HashMap<String, ValueType>();
 
 		// bind parameters in new name table
 		// we need to get the underlying List structure that the ParamList
 		// uses...
 		Iterator<String> p = parameterlist.getParamList().iterator();
 		Iterator<Expr> e = expressionlist.getExpressions().iterator();
 
 		if (parameterlist.getParamList().size() != expressionlist
 				.getExpressions().size()) {
 			System.out.println("Param count does not match");
 			System.exit(1);
 		}
 		while (p.hasNext() && e.hasNext()) {
 
 			// assign the evaluation of the expression to the parameter name.
 			newnametable.put(p.next(),
 					e.next().eval(nametable, functiontable));
 			// System.out.println("Loading Nametable for procedure with: "+p+" = "+nametable.get(p));
 
 		}
 		// evaluate function body using new name table and
 		// old function table
 		// eval statement list and catch return
 		// System.out.println("Beginning Proceedure Execution..");
 		try {
 			stmtlist.eval(newnametable, functiontable);
 		} catch (Exception result) {
 			// Note, the result shold contain the proceedure's return value as a
 			// String
 			// System.out.println();
 			// result.printStackTrace();
 			// System.out.println();
 			return new Number(result.getMessage());
 		}
 		System.out.println("Error:  no return value");
 		System.exit(1);
 		// need this or the compiler will complain, but should never
 		// reach this...
 		return null;
 	}
 }
 
 class Program {
 
 	private StatementList stmtlist;
 
         private int startingAddress;
 
 	public Program(StatementList list) {
 		stmtlist = list;
 	}
 
 	public void translate(SymbolTable st, FunctionTable ft) {
 		try {
 			Proc mainProc = new Proc(new ParamList(), stmtlist);
 			DefineStatement main = new DefineStatement("main", mainProc);
 			main.translate(st, ft, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 		}
 	}
 
 	public void eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		try {
 			stmtlist.eval(nametable, functiontable);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 		}
 	}
 
 	public void dump(HashMap<String, ValueType> nametable,
 			 HashMap<String, Proc> functiontable,  SymbolTable st,
 			 Linker t) {
 		// System.out.println(hm.values());
 		// System.out.println("Dumping out all the variables...");
 		// if (nametable != null) {
 		// for (String name : nametable.keySet()) {
 		// System.out.println(name
 		// + "="
 		// + nametable.get(name).toString(nametable,
 		// functiontable, st, t));
 		// }
 		// }
 		// if (functiontable != null) {
 		// for (String name : functiontable.keySet()) {
 		// System.out.println("Function: " + name + " defined...");
 		// }
 		// }
 
 		System.out.println("Dumping out the symbol table...");
 		System.out.println(st);
 
 		//TODO: Have these file names passed is as params?
 		String translatedFile = "trans.out";
 		System.out.println("Dumping out the translated instructions to file " + translatedFile + "...");
 		writeFile(translatedFile, t.toString(st));
 
 		String linkedFile = "linked.out";
 		System.out.println("Dumping out the linked instructions to file " + linkedFile + "...");
 		writeFile(linkedFile, t.toStringLink(st));
 
 		String optimizedFile = "optimized.out";
 		System.out.println("Dumping out Optimized instructions to file " + optimizedFile + "...");
 		writeFile(optimizedFile, t.toStringOpt(st));
 
 		String initialMemory = "initialmemory.out";
 		System.out.println("Dumping out the initial memory to file " + initialMemory + "...");
 		writeFile(initialMemory, t.toStringInitialMemory(st));
 	}
 
         private void writeFile(String filename, String text) {
 	    try{
 		// Create file 
 		FileWriter fstream = new FileWriter(filename);
 		BufferedWriter out = new BufferedWriter(fstream);
 		out.write(text);
 		//Close the output stream
 		out.close();
 	    }catch (Exception e){//Catch exception if any
 		System.err.println("Error: " + e.getMessage());
 	    }
         }
 }
 
 // Assignment 2 (mwa29)
 
 class Sequence {
 	LinkedList<Expr> seq;
 
 	public Sequence(Sequence s, Expr e) {
 		seq = s.expressions();
 		seq.add(0, e);
 	}
 
 	public Sequence(Expr e) {
 		seq = new LinkedList<Expr>();
 		seq.add(e);
 	}
 
 	public Sequence() {
 		seq = new LinkedList<Expr>();
 	}
 
 	public LinkedList<Expr> expressions() {
 		return seq;
 	}
 
 	public Sequence clone() {
 		Sequence temp = new Sequence();
 		for (Expr e : seq) {
 			temp.expressions().add(e);
 		}
 		return temp;
 	}
 }
 
 class List extends ValueType {
 	private Sequence s;
 
 	public List(Sequence seq) {
 		s = seq;
 	}
 
 	public List() {
 		s = new Sequence();
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		return this;
 	}
 
 	public String toString(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		StringBuilder returnString = new StringBuilder("[");
 		boolean flag = false; // Check if the list is empty
 		for (Expr vt : s.seq) {
 			flag = true;
 			returnString.append(vt.eval(nametable, functiontable)
 					.toString(nametable, functiontable));
 			returnString.append(",");
 		}
 		// Remove the trailing ','
 		returnString.deleteCharAt(returnString.length() - 1);
 		returnString.append("]");
 		if (flag == false) // Delete the "]"
 			returnString.deleteCharAt(returnString.length() - 1);
 		return returnString.toString();
 	}
 
 	public List clone() {
 		Sequence clone = s.clone();
 		return new List(clone);
 	}
 
 	public Sequence sequence() {
 		return s;
 	}
 
 	public static boolean listp(ValueType vt) {
 		return (vt instanceof List);
 	}
 
 	public List cons(ValueType e) {
 		s.expressions().add(0, e);
 		return this;
 	}
 
 	public Expr car() {
 		return s.expressions().getFirst();
 	}
 
 	public Expr cdr() {
 		Sequence copy = s.clone();
 		copy.expressions().remove(0);
 		return new List(copy);
 	}
 
 	public void concat(List list2) {
 		for (Expr e : list2.sequence().expressions()) {
 			s.expressions().add(e);
 		}
 	}
 }
 
 class Cons extends Expr {
 
 	private Expr e;
 	private Expr L;
 
 	public Cons(Expr e, Expr L) {
 		this.e = e;
 		this.L = L;
 	}
 
 	// FIXME The parser crashes when it encounters variables passed into this
 	// function that are not surrounded by parentheses. (i.e. y :=
 	// cons(x,[1,2]) fails to parse but y := cons((x),[1,2]) works)
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		ValueType element = e.eval(nametable, functiontable);
 		ValueType list = L.eval(nametable, functiontable);
 
 		if (list instanceof List) {
 			List copy = ((List) list).clone();
 			return copy.cons(element)
 					.eval(nametable, functiontable);
 		} else {
 			// Must pass a list to car. Otherwise, error.
 			throw new RuntimeException("Invalid value type passed to cons: "
 					+ list.getClass());
 		}
 
 	}
 }
 
 class Car extends Expr {
 
 	private Expr L;
 
 	public Car(Expr L) {
 		this.L = L;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		ValueType list = L.eval(nametable, functiontable);
 		// If the list is empty, throw an exception saying so
 		List temp = (List) list;
 		if (temp.sequence().expressions().size() == 0)
 			throw new RuntimeException("Attempting to Car an empty list");
 		else if (list instanceof List) {
 			return ((List) list).car().eval(nametable, functiontable);
 		} else {
 			// Must pass a list to car. Otherwise, error.
 			throw new RuntimeException("Invalid value type passed to car: "
 					+ list.getClass());
 		}
 	}
 }
 
 class Cdr extends Expr {
 
 	private Expr L;
 
 	public Cdr(Expr L) {
 		this.L = L;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 
 		ValueType list = L.eval(nametable, functiontable);
 		// If there's only one element in the list, return an empty list
 		List temp = (List) list;
 		if (temp.sequence().expressions().size() == 1)
 			return new List();
 		else if (list instanceof List) {
 			try { // Check if the list is empty
 				return ((List) list).cdr().eval(nametable, functiontable);
 			} catch (Exception e) {
 				throw new RuntimeException("Attempting to Access Null Value");
 			}
 		} else {
 			// Must pass a list to cdr. Otherwise, error.
 			throw new RuntimeException("Invalid value type passed to cdr: "
 					+ list.getClass());
 		}
 	}
 }
 
 class Nullp extends Expr {
 
 	private Expr L;
 
 	public Nullp(Expr L) {
 		this.L = L;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		ValueType list = L.eval(nametable, functiontable);
 		// Check for empty list as well as null
 		List temp = (List) list;
 		if (list == null || temp.sequence().expressions().size() == 0) {
 			return new Number(1);
 		} else if (list instanceof List) {
 			return new Number(0);
 		} else {
 			throw new RuntimeException("Invalid value type passed to nullp: "
 					+ L.eval(nametable, functiontable).getClass());
 		}
 	}
 }
 
 class Intp extends Expr {
 
 	private Expr e;
 
 	public Intp(Expr e) {
 		this.e = e;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		if (e.eval(nametable, functiontable) instanceof Number) {
 			return new Number(1);
 		} else {
 			return new Number(0);
 		}
 	}
 }
 
 class Listp extends Expr {
 
 	private Expr e;
 
 	public Listp(Expr e) {
 		this.e = e;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		if (e.eval(nametable, functiontable) instanceof List) {
 			return new Number(1);
 		} else {
 			return new Number(0);
 		}
 	}
 }
 
 class Concat extends Expr {
 
 	private Expr l1;
 	private Expr l2;
 
 	public Concat(Expr l1, Expr l2) {
 		this.l1 = l1;
 		this.l2 = l2;
 	}
 
 	public ValueType eval(HashMap<String, ValueType> nametable,
 			HashMap<String, Proc> functiontable) {
 		ValueType list1 = l1.eval(nametable, functiontable);
 		ValueType list2 = l2.eval(nametable, functiontable);
 
 		if (list1 instanceof List && list2 instanceof List) {
 			List copy = ((List) list1).clone();
 			copy.concat((List) list2);
 			return copy;
 		} else {
 			// Must pass a list to car. Otherwise, error.
 			throw new RuntimeException("Invalid value type passed to concat: "
 					+ list1.getClass() + " - " + list2.getClass());
 		}
 
 	}
 }
