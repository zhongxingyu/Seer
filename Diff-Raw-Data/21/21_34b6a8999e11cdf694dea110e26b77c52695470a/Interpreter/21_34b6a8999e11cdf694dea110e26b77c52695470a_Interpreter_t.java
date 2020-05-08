 package columbia.plt.tt.interpreter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 import antlr.RecognitionException;
 import columbia.plt.tt.DefinitionAnalyzer;
 import columbia.plt.tt.TTLexer;
 import columbia.plt.tt.TTParser;
 import columbia.plt.tt.TypeAnalyzer;
 import columbia.plt.tt.datatype.Calendar;
 import columbia.plt.tt.datatype.Date;
 import columbia.plt.tt.datatype.Task;
 import columbia.plt.tt.datatype.TimeFrame;
 
 public class Interpreter {
 
 	TTLexer lexer;
 	TokenStream tokenStream;
 	TTParser parser;
 	// Right now our TTGrammar.g makes each node a CommonTree
 	CommonTree root;
 	SymbolTable symbolTable = new SymbolTable();
 	ArrayList<String> errors = new ArrayList<String>();
 
 	public void interp(InputStream input) throws RecognitionException,
 			IOException, org.antlr.runtime.RecognitionException {
 		// Lexical and Syntax Analysis
 		CharStream stream = new ANTLRInputStream(input);
 		lexer = new TTLexer(stream);
 		tokenStream = new CommonTokenStream(lexer);
 		parser = new TTParser(tokenStream);
 		// Do we have a tree adaptor??
 		// parser.setTreeAdaptor(InterPie.pieAdaptor);
 
 		// Show any lexer/parser errors
 		for (int i = 0; i < lexer.getErrors().size(); i++) {
 			System.out.println(lexer.getErrors().get(i));
 		}
 		for (int i = 0; i < parser.getErrors().size(); i++) {
 			System.out.println(parser.getErrors().get(i));
 		}
 		TTParser.translationUnit_return r = parser.translationUnit();
		root = r.getTree();
		System.out.println("tree: " + root.toStringTree());
 		// If Syntax errors exit
 		if (parser.getNumberOfSyntaxErrors() != 0)
 			return;

 		// Semantic Analysis
 		CommonTreeNodeStream nodes = new CommonTreeNodeStream(root);
 		nodes.setTokenStream(tokenStream); // pass the tokens from the lexer
 		//nodes.setTreeAdaptor(TTAdaptor);
 		
 		// Phase 1 - Analyze all method and variable definitions and populate the SymbolTable
 		DefinitionAnalyzer def = new DefinitionAnalyzer(nodes, symbolTable);
 		def.downup(root); // trigger define actions upon certain subtrees
 		
 //		// Phase 2 - Analyze expression types and resolve symbols
 //		nodes.reset();
 //		TypeAnalyzer typeComp = new TypeAnalyzer(nodes, symbolTable);
 //		typeComp.downup(root); // trigger resolve/type computation actions
 
 //		if (errors)
 //			return;
 		
 		// Run program it is correct
 		tunit(root);
 	}
 
 	/** visitor dispatch according to node token type */
 	public Object exec(CommonTree t) {
 
 		// System.out.println(t.getType());
 		try {
 			switch (t.getType()) {
 			case TTParser.TUNIT:
 				tunit(t);
 				break; // (PL)
 			case TTParser.IMPORTS:
 				imports(t);
 				break; // (PL)
 			// case TTParser.IMPORT : imports(t); break; // (PL) is this same
 			// with IMPORTS?
 			case TTParser.MAIN:
 				mainBlock(t);
 				break; // (PL)
 			case TTParser.SLIST:
 				block(t);
 				break; // (PL)
 			// case TTParser.STRINGTYPE: (AA)
 			case TTParser.NUMBERTYPE:
 				numberType(t);
 				break; // (AA)
 			// case TTParser.DATETYPE : (AA)
 			// case TTParser.TASKTYPE : (AA)
 			// case TTParser.TIMEFRAMETYPE : (AA)
 			// case TTParser.CALENDARTYPE : (AA)
 			// case TTParser.TIMETYPE : (AA)
 			case TTParser.IF:
 				ifStatement(t);
 				break;// (MA)
 			case TTParser.ELSE:
 				elseStatement(t);
 				break;// (MA)
 			case TTParser.EVERYDATE:
 				everyDate(t);
 				break; // (MA)
 			case TTParser.EVERYTASK:
 				everyTask(t);
 				break; // (MA)
 			case TTParser.FROM:
 				return dateOrIdent(t); // (MA)
 			case TTParser.TO:
 				return dateOrIdent(t); // (MA)
 			case TTParser.BY:
 				return timeFrameOrIdent(t); // (MA)
 			case TTParser.IN:
 				return in(t);// (MA)
 				// case TTParser.ON : (MA)
 				// case TTParser.BREAK : (MA)
 				// case TTParser.EXIT : (MA)
 				// case TTParser.CONTINUE : (MA)
 				// case TTParser.TRUE : (JL)
 				// case TTParser.FALSE : (JL)
 			case TTParser.IDENT:
 				identity(t);
 				break; // (JL)
 			case TTParser.ASSIGN:
 				assign(t);
 				break; // (JL)
 			case TTParser.NUMBER:
 				return Integer.parseInt(t.getText()); // (JL)
 			case TTParser.PLUS:
 				return plusEval(t);
 
 			case TTParser.MINUS:
 			case TTParser.DIV:
 			case TTParser.MULT:
 			case TTParser.MOD:
 				return arithmeticEval(t);
 			case TTParser.AND:
 			case TTParser.OR:
 			case TTParser.NOT:
 				return logicalEval(t);
 
 			case TTParser.GT:
 			case TTParser.GTEQ:
 			case TTParser.LT:
 			case TTParser.LTEQ:
 				return relationalEval(t);
 
 			case TTParser.EQUALS:
 			case TTParser.NOTEQUALS:
 				return equalityEval(t); // (AA)
 				// case TTParser.CALL : call(t); break; (PL)
 				// case TTParser.RETURN : (PL)
 				// case TTParser.READ : (PL)
 			case TTParser.PRINT:
 				print(t);
 				break; // (PL)
 
 			// case TTParser.TIMEFRAME_YEAR //(JL)
 			// case TTParser.TIMEFRAME_YEARS //(JL)
 			// case TTParser.TIMEFRAME_MONTH //(JL)
 			// case TTParser.TIMEFRAME_MONTHS //(JL)
 			// case TTParser.TIMEFRAME_DAY //(JL)
 			// case TTParser.TIMEFRAME_DAYS //(JL)
 			// case TTParser.TIMEFRAME_HOUR //(JL)
 			// case TTParser.TIMEFRAME_HOURS //(JL)
 			// case TTParser.TIMEFRAME_MINUTE //(JL)
 			// case TTParser.TIMEFRAME_MINUTES //(JL)
 			// case TTParser.TIMEFRAME_MONDAY //(JL)
 			// case TTParser.TIMEFRAME_TUESDAY //(JL)
 			// case TTParser.TIMEFRAME_WEDNESDAY //(JL)
 			// case TTParser.TIMEFRAME_THURSDAY //(JL)
 			// case TTParser.TIMEFRAME_FRIDAY //(JL)
 			// case TTParser.TIMEFRAME_SATURDAY //(JL)
 			// case TTParser.TIMEFRAME_SUNDAY //(JL)
 			// case TTParser.TIMEFRAME_JANUARY //(JL)
 			// case TTParser.TIMEFRAME_FEBRUARY //(JL)
 			// case TTParser.TIMEFRAME_MARCH //(JL)
 			// case TTParser.TIMEFRAME_APRIL //(JL)
 			// case TTParser.TIMEFRAME_MAY //(JL)
 			// case TTParser.TIMEFRAME_JUNE //(JL)
 			// case TTParser.TIMEFRAME_JULY //(JL)
 			// case TTParser.TIMEFRAME_AUGUST //(JL)
 			// case TTParser.TIMEFRAME_SEPTEMBER //(JL)
 			// case TTParser.TIMEFRAME_OCTOBER //(JL)
 			// case TTParser.TIMEFRAME_NOVEMBER //(JL)
 			// case TTParser.TIMEFRAME_DECEMBER //(JL)
 			// case TTParser.TIMEFRAME_WEEKEND //(JL)
 			// case TTParser.TIMEFRAME_WEEKDAY //(JL)
 
 			/*
 			 * case PieParser.BLOCK : block(t); break; case PieParser.ASSIGN :
 			 * assign(t); break; case PieParser.RETURN : ret(t); break; case
 			 * PieParser.PRINT : print(t); break; case PieParser.IF : ifstat(t);
 			 * break; case PieParser.WHILE : whileloop(t); break; case
 			 * PieParser.CALL : return call(t); case PieParser.NEW : return
 			 * instance(t); case PieParser.ADD : return add(t); case
 			 * PieParser.SUB : return op(t); case PieParser.MUL : return op(t);
 			 * case PieParser.EQ : return eq(t); case PieParser.LT : return
 			 * lt(t); case PieParser.INT : return Integer.parseInt(t.getText());
 			 * case PieParser.CHAR : return new
 			 * Character(t.getText().charAt(1)); case PieParser.FLOAT : return
 			 * Float.parseFloat(t.getText()); case PieParser.STRING : String s =
 			 * t.getText(); return s.substring(1,s.length()-1); case
 			 * PieParser.DOT : case PieParser.ID : return load(t);
 			 */
 			default: // catch unhandled node types
 				throw new UnsupportedOperationException("Node " + t.getText()
 						+ "<" + t.getType() + "> not handled");
 			}
 		} catch (Exception e) {
 		}
 		return null;
 	}
 
 	public void tunit(CommonTree t) {
 		if (t.getType() != TTParser.TUNIT) {
 			System.out.println("NOT a TUNIT");
 		}
 		// Execute code
 		List<CommonTree> stats = null;
 		for (int i = 0; i < t.getChildCount(); i++) {
 			exec((CommonTree) t.getChild(i));
 		}
 	}
 
 	public void imports(CommonTree t) {
 		// System.out.println("Imports");
 	}
 
 	public void mainBlock(CommonTree t) {
 		// System.out.println("main");
 
 		symbolTable.addScope(); // add a scope for a main block
 
 		if (t.getType() != TTParser.MAIN) {
 			// Handle error
 			// listener.error("not a block: "+t.toStringTree());
 		}
 		// Execute code
 		List<CommonTree> stats = null;
 		for (int i = 0; i < t.getChildCount(); i++) {
 			// exec((CommonTree)t.getChild(i));
 			exec((CommonTree) t.getChild(i));
 		}
 
 		System.out.println("a: "
 				+ symbolTable.getCurrentScope().get("a").getType() + ", "
 				+ symbolTable.getCurrentScope().get("a").getValue());
 		System.out.println("b: "
 				+ symbolTable.getCurrentScope().get("b").getType() + ", "
 				+ symbolTable.getCurrentScope().get("b").getValue());
 		System.out.println("c: "
 				+ symbolTable.getCurrentScope().get("c").getType() + ", "
 				+ symbolTable.getCurrentScope().get("c").getValue());
 		// System.out.println("d: " +
 		// symbolTable.getCurrentScope().get("d").getType() + ", " +
 		// symbolTable.getCurrentScope().get("d").getValue());
 	}
 
 	public void block(CommonTree t) {
 		// System.out.println("block");
 		// Execute code
 		List<CommonTree> stats = null;
 		for (int i = 0; i < t.getChildCount(); i++) {
 			exec((CommonTree) t.getChild(i));
 		}
 	}
 
 	public void numberType(CommonTree t) {
 		System.out.println("number");
 		System.out.println(t.getChild(0).getText());
 
 		for (int i = 0; i < t.getChildCount(); i++) {
 			symbolTable.addSymbol(t.getChild(i).getText(), t.getText(), null); // Number
 																				// a;
 			exec((CommonTree) t.getChild(i));
 		}
 
 	}
 
 	public void identity(CommonTree t) {
 		// System.out.println("identity");
 
 		for (int i = 0; i < t.getChildCount(); i++) {
 			// System.out.println(((CommonTree)t.getChild(i)).getText());
 			// System.out.println("here1 " + exec((CommonTree)t.getChild(i)));
 			exec((CommonTree) t.getChild(i));
 		}
 	}
 
 	public void assign(CommonTree t) {
 		System.out.println("assign");
 		// System.out.println(t.getChild(0).getText());
 		symbolTable.setValue(t.getChild(0).getText(),
 				exec((CommonTree) t.getChild(1)));
 
 		/*
 		 * for (int i = 0; i < t.getChildCount(); i++) {
 		 * System.out.println(((CommonTree)t.getChild(i)).getText());
 		 * //System.out.println("here2 " + exec((CommonTree)t.getChild(i)));
 		 * Object o = exec((CommonTree)t.getChild(i)); }
 		 */
 	}
 
 	public Object plusEval(CommonTree t) {
 
 		System.out.println("Plus Operator Evaluation");
 		exec((CommonTree) t.getChild(0));
 		exec((CommonTree) t.getChild(1));
 
 		Object a = symbolTable.getValue(t.getChild(0).getText());
 		Object b = symbolTable.getValue(t.getChild(1).getText());
 
 		if (a instanceof String && b instanceof String)
 			return a.toString() + b.toString();
 		else {
 			return arithmeticEval(t);
 		}
 
 	}
 
 	public int arithmeticEval(CommonTree t) {
 		System.out.println("Arithmetic Evaluation");
 		exec((CommonTree) t.getChild(0));
 		exec((CommonTree) t.getChild(1));
 
 		int a = (int) symbolTable.getValue(t.getChild(0).getText());
 		int b = (int) symbolTable.getValue(t.getChild(1).getText());
 
 		switch (t.getType()) {
 
 		case TTParser.PLUS:
 			return a + b;
 
 		case TTParser.MINUS:
 			return a - b;
 
 		case TTParser.MULT:
 			return a * b;
 
 		case TTParser.DIV:
 			return a / b; // to do throw error on divide by zero
 
 		case TTParser.MOD:
 			return a % b;
 
 		default:
 			return 0;
 
 		}
 
 	}
 
 	public boolean logicalEval(CommonTree t) {
 
 		System.out.println("Logical Evaluation");
 		exec((CommonTree) t.getChild(0));
 		exec((CommonTree) t.getChild(1));
 
 		boolean a = (boolean) symbolTable.getValue(t.getChild(0).getText());
 		boolean b = (boolean) symbolTable.getValue(t.getChild(1).getText());
 
 		switch (t.getType()) {
 		case TTParser.AND:
 			return a && b;
 		case TTParser.OR:
 			return a || b;
 		case TTParser.NOT:
 			return !a;
 		default:
 			return false;
 		}
 
 	}
 
 	public boolean equalityEval(CommonTree t) {
 
 		System.out.println("Equality Evaluation");
 		exec((CommonTree) t.getChild(0));
 		exec((CommonTree) t.getChild(1));
 
 		Object a = symbolTable.getValue(t.getChild(0).getText());
 		Object b = symbolTable.getValue(t.getChild(1).getText());
 
 		switch (t.getType()) {
 
 		case TTParser.EQUALS:
 			return a == b;
 		case TTParser.NOTEQUALS:
 			return a != b;
 		default:
 			return false;
 		}
 	}
 
 	public boolean relationalEval(CommonTree t) {
 
 		System.out.println("Relational Evaluation");
 		exec((CommonTree) t.getChild(0));
 		exec((CommonTree) t.getChild(1));
 
 		int a = (int) symbolTable.getValue(t.getChild(0).getText());
 		int b = (int) symbolTable.getValue(t.getChild(1).getText());
 
 		switch (t.getType()) {
 
 		case TTParser.GTEQ:
 			return a >= b;
 		case TTParser.LTEQ:
 			return a <= b;
 		case TTParser.GT:
 			return a > b;
 		case TTParser.LT:
 			return a < b;
 
 		default:
 			return false;
 		}
 
 	}
 
 	public void call(CommonTree t) {
 		System.out.println("call");
 		symbolTable.addScope();
 
 		for (int i = 0; i < t.getChildCount(); i++) {
 			System.out.println(((CommonTree) t.getChild(i)).getText());
 			// System.out.println("here2 " + exec((CommonTree)t.getChild(i)));
 			exec((CommonTree) t.getChild(i));
 		}
 
 		symbolTable.removeScope();
 	}
 
 	public void ifStatement(CommonTree t) {
 		System.out.println("IF" + t.getChildCount());
 		// 0th Child is the expr to evaluate
 		if ((Boolean) exec((CommonTree) t.getChild(0))) {
 			System.out.println("HERE");
 			// 1st Child is the block
 			exec((CommonTree) t.getChild(0));
 		} else if (t.getChildCount() >= 3) {
 			System.out.println("HERE");
 			exec((CommonTree) t.getChild(2));
 			System.out.println(((CommonTree) t.getChild(2)).getText());
 		}
 
 	}
 
 	public void elseStatement(CommonTree t) {
 		for (int i = 0; i < t.getChildCount(); i++) {
 			exec((CommonTree) t.getChild(i));
 		}
 	}
 
 	public void everyDate(CommonTree t) {
 		System.out.println("\neveryDate!");
 		// Declare variables
 		Date start = null;
 		Date end = null;
 		TimeFrame inc = null;
 		CommonTree block = null;
 		Date itterDate = null;
 		String type = null;
 		String name = null;
 		symbolTable.addSymbol(name, type, itterDate);
 
 		for (int i = 0; i < t.getChildCount(); i++) {
 			switch (t.getChild(i).getType()) {
 			case TTParser.FROM:
 				start = (Date) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.TO:
 				end = (Date) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.BY:
 				inc = (TimeFrame) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.SLIST:
 				block = (CommonTree) t.getChild(i);
 				break;
 			default:
 				// Handle "Date"
 				type = ((CommonTree) t.getChild(i)).getText();
 				name = ((CommonTree) t.getChild(i)).getChild(0).getText();
 				symbolTable.addSymbol(name, type, itterDate);
 				System.out.println("Found task");
 				break;
 			}
 		}
 
 		if (start == null || end == null)
 			return;
 
 		// Define the itterDate
 		itterDate = start;
 		symbolTable.addSymbol(name, type, itterDate);
 
 		System.out.println("itterDate: " + itterDate.toString() + " end: "
 				+ end.toString() + "compare " + itterDate.compareTo(end));
 		while (itterDate.compareTo(end) <= 0) {
 			System.out.println("LOOP: " + itterDate.toString());
 			// nth Child is the Block to execute every loop
 			exec((CommonTree) t.getChild(4));
 
 			// Increment the itterDate and update symbolTable
 			itterDate.add(inc);
 			symbolTable.addSymbol(name, type, itterDate);
 		}
 	}
 
 	public Date dateOrIdent(CommonTree t) {
 		// This only handles DATE_CONSTANT
 		System.out.println("dateOrIdent");
 		return new Date(((CommonTree) t.getChild(0)).getText());
 	}
 
 	public TimeFrame timeFrameOrIdent(CommonTree t) {
 		// This only handles timeFrameConstant
 		System.out.println("timeFrameOrIdent");
 		String tf = "";
 		for (int i = 0; i < t.getChildCount(); i++) {
 			tf = tf + " " + ((CommonTree) t.getChild(i)).getText();
 		}
 		System.out.println("by: " + tf);
 		return new TimeFrame(tf);
 	}
 
 	public void everyTask(CommonTree t) {
 		System.out.println("\neveryTask!");
 
 		String type = null;
 		String name = null;
 
 		Date start = null;
 		Date end = null;
 
 		CommonTree on = null;
 		Calendar c = null;
 
 		CommonTree block = null;
 		Task itterTask = null;
 
 		for (int i = 0; i < t.getChildCount(); i++) {
 			switch (t.getChild(i).getType()) {
 			case TTParser.IN:
 				c = (Calendar) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.FROM:
 				start = (Date) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.TO:
 				end = (Date) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.ON:
 				on = (CommonTree) t.getChild(i);
 				break;
 			case TTParser.SLIST:
 				block = (CommonTree) t.getChild(i);
 				break;
 			default:
 				// Handle "Task"
 				type = ((CommonTree) t.getChild(i)).getText();
 				name = ((CommonTree) t.getChild(i)).getChild(0).getText();
 				symbolTable.addSymbol(name, type, itterTask);
 				break;
 			}
 		}
 
 		// Get the subset of tasks from Calendar
 		ArrayList<Task> taskList = null;
 		if (start != null && end != null) {
 			taskList = c.getTasksWithinRange(start, end);
 		} else {
 			taskList = c;
 		}
 
 		for (Task task : taskList) {
 			// If there is an on expression evaluate it for each loop
 			if (on == null || (Boolean) exec(on)) {
 				// Update the symbol table
 				itterTask = task;
 				symbolTable.addSymbol(name, type, itterTask);
 
 				// execute the block of code
 				exec(block);
 			}
 		}
 
 	}
 
 	public Calendar in(CommonTree t) {
 		return new Calendar("Temp");
 	}
 
 	public void print(CommonTree t) {
 		System.out.println(t.getChild(0).getText());
 	}
 }
