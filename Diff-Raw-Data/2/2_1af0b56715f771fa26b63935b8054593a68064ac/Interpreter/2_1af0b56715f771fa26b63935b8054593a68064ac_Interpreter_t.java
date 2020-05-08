 package columbia.plt.tt.interpreter;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
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
 import columbia.plt.tt.constants.TTConstants;
 import columbia.plt.tt.datatype.Calendar;
 import columbia.plt.tt.datatype.Date;
 import columbia.plt.tt.datatype.Task;
 import columbia.plt.tt.datatype.TimeFrame;
 import columbia.plt.tt.typecheck.InterpreterListener;
 
 public class Interpreter {
 
 	TTLexer lexer;
 	TokenStream tokenStream;
 	TTParser parser;
 	// Right now our TTGrammar.g makes each node a CommonTree
 	CommonTree root;
 	SymbolTable symbolTable = new SymbolTable();
 	ArrayList<String> errors = new ArrayList<String>();
 
 	public enum TimeFrameConst {
 		YEAR, YEARS, MONTH, MONTHS, DAY, DAYS, HOUR, HOURS, MINUTE, MINUTES, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY, JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER, WEEKEND, WEEKDAY
 	}
 
 	private Boolean breakCalled = false;
 
 	public InterpreterListener listener = // default response to messages
 	new InterpreterListener() {
 		public void info(String msg) {
 			System.out.println(msg);
 		}
 
 		public void error(String msg) {
 			System.err.println(msg);
 		}
 
 		public void error(String msg, Exception e) {
 			error(msg);
 			e.printStackTrace(System.err);
 		}
 
 		@Override
 		public void error(String msg, org.antlr.runtime.Token t) {
 			error("line " + t.getLine() + ": " + msg);
 
 		}
 	};
 
 	// add interpreterListener to collect errors
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
 
 		/*
 		 * CommonTreeNodeStream nodes = new CommonTreeNodeStream(root);
 		 * nodes.setTokenStream(tokenStream); // pass the tokens from the lexer
 		 * //nodes.setTreeAdaptor(TTAdaptor);
 		 * 
 		 * // Phase 1 - Analyze all method and variable definitions and populate
 		 * the SymbolTable DefinitionAnalyzer def = new
 		 * DefinitionAnalyzer(nodes, symbolTable); def.downup(root); // trigger
 		 * define actions upon certain subtrees
 		 * 
 		 * // // Phase 2 - Analyze expression types and resolve symbols //
 		 * nodes.reset(); // TypeAnalyzer typeComp = new TypeAnalyzer(nodes,
 		 * symbolTable); // typeComp.downup(root); // trigger resolve/type
 		 * computation actions
 		 * 
 		 * // if (errors) // return;
 		 */
 
 		CommonTreeNodeStream nodes = new CommonTreeNodeStream(root);
 		nodes.setTokenStream(tokenStream); // pass the tokens from the lexer
 		// nodes.setTreeAdaptor(TTAdaptor);
 
 		// Phase 1 - Analyze all method and variable definitions and populate
 		// the SymbolTable
 		DefinitionAnalyzer def = new DefinitionAnalyzer(nodes, symbolTable);
 		def.downup(root); // trigger define actions upon certain subtrees
 
 		// // Phase 2 - Analyze expression types and resolve symbols
 		// nodes.reset();
 		// TypeAnalyzer typeComp = new TypeAnalyzer(nodes, symbolTable);
 		// typeComp.downup(root); // trigger resolve/type computation actions
 
 		// if (errors)
 		// return;
 
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
 
 			case TTParser.DECLARE:
 				return declarationEval(t, false);
 
 			case TTParser.DEFINE:
 				defineEval(t, false);
 				break;
 
 			case TTParser.ASSIGN:
 				assign(t);
 				break; // (JL)
 
 			case TTParser.PLUS:
 				return plusEval(t);
 
 			case TTParser.MINUS:
 			case TTParser.DIV:
 			case TTParser.MULT:
 			case TTParser.MOD:
 				return arithmeticEval(t);
 
 			case TTParser.AND:
 			case TTParser.OR:
 				return logicalEval(t);
 			case TTParser.NOT:
 				return "not";
 
 			case TTParser.GT:
 			case TTParser.GTEQ:
 			case TTParser.LT:
 			case TTParser.LTEQ:
 				return relationalEval(t);
 
 			case TTParser.EQUALS:
 			case TTParser.NOTEQUALS:
 				return equalityEval(t);
 
 			case TTParser.UNARY:
 				return unaryExprEval(t);
 
 			case TTParser.STRINGTYPE:
 			case TTParser.NUMBERTYPE:
 			case TTParser.DATETYPE:
 			case TTParser.BOOLEAN:
 			case TTParser.TASKTYPE:
 			case TTParser.TIMEFRAMETYPE:
 			case TTParser.CALENDARTYPE:
 			case TTParser.TIMETYPE:
 				return t.getText();
 
 			case TTParser.DATE_CONSTANT_TOKEN:
 				return dateConstant(t);
 			case TTParser.TIMEFRAME_CONSTANT:
 				return timeFrameConstant(t);
 
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
 			case TTParser.ON:
 				return on(t);// (MA)
 			case TTParser.BREAK: // (MA)
 				breakFunc(t);
 				break;
 			case TTParser.EXIT: // (MA)
 				exitFunc(t);
 				break;
 			case TTParser.CONTINUE: // (MA)
 				break;
 			/*
 			 * case TTParser.TRUE : return true; case TTParser.FALSE : return
 			 * false;
 			 */
 			case TTParser.IDENT_TOKEN:
 				return identity(t);
 			case TTParser.IDENT:
 				return identityValue(t);
 				// (JL)
 			//case TTParser.DOT:
 			//	return fieldAccess(t);
 			case TTParser.NUMBER:
 				return Integer.parseInt(t.getText()); // (JL)
 			case TTParser.STRING_CONSTANT:
 				return t.getText();
 
 			case TTParser.TRUE:
 			case TTParser.FALSE:
 				return Boolean.parseBoolean(t.getText());
 
 			case TTParser.CALL:
 				return call(t);
 			case TTParser.RETURN:
 				return returnStmt(t);
 			case TTParser.READ :
 				return read(t);
 			case TTParser.PRINT:
 				print(t);
 				break; // (PL)
 
 			case TTParser.TIMEFRAME_YEAR:
 				return TimeFrameConst.YEAR;
 			case TTParser.TIMEFRAME_YEARS:
 				return TimeFrameConst.YEARS;
 			case TTParser.TIMEFRAME_MONTH:
 				return TimeFrameConst.MONTH;
 			case TTParser.TIMEFRAME_MONTHS:
 				return TimeFrameConst.MONTHS;
 			case TTParser.TIMEFRAME_DAY:
 				return TimeFrameConst.DAY;
 			case TTParser.TIMEFRAME_DAYS:
 				return TimeFrameConst.DAYS;
 			case TTParser.TIMEFRAME_HOUR:
 				return TimeFrameConst.HOUR;
 			case TTParser.TIMEFRAME_HOURS:
 				return TimeFrameConst.HOURS;
 			case TTParser.TIMEFRAME_MINUTE:
 				return TimeFrameConst.MINUTE;
 			case TTParser.TIMEFRAME_MINUTES:
 				return TimeFrameConst.MINUTES;
 			case TTParser.TIMEFRAME_MONDAY:
 				return TimeFrameConst.MONDAY;
 			case TTParser.TIMEFRAME_TUESDAY:
 				return TimeFrameConst.TUESDAY;
 			case TTParser.TIMEFRAME_WEDNESDAY:
 				return TimeFrameConst.WEDNESDAY;
 			case TTParser.TIMEFRAME_THURSDAY:
 				return TimeFrameConst.THURSDAY;
 			case TTParser.TIMEFRAME_FRIDAY:
 				return TimeFrameConst.FRIDAY;
 			case TTParser.TIMEFRAME_SATURDAY:
 				return TimeFrameConst.SATURDAY;
 			case TTParser.TIMEFRAME_SUNDAY:
 				return TimeFrameConst.SUNDAY;
 			case TTParser.TIMEFRAME_JANUARY:
 				return TimeFrameConst.JANUARY;
 			case TTParser.TIMEFRAME_FEBRUARY:
 				return TimeFrameConst.FEBRUARY;
 			case TTParser.TIMEFRAME_MARCH:
 				return TimeFrameConst.MARCH;
 			case TTParser.TIMEFRAME_APRIL:
 				return TimeFrameConst.APRIL;
 			case TTParser.TIMEFRAME_MAY:
 				return TimeFrameConst.MAY;
 			case TTParser.TIMEFRAME_JUNE:
 				return TimeFrameConst.JUNE;
 			case TTParser.TIMEFRAME_JULY:
 				return TimeFrameConst.JULY;
 			case TTParser.TIMEFRAME_AUGUST:
 				return TimeFrameConst.AUGUST;
 			case TTParser.TIMEFRAME_SEPTEMBER:
 				return TimeFrameConst.SEPTEMBER;
 			case TTParser.TIMEFRAME_OCTOBER:
 				return TimeFrameConst.OCTOBER;
 			case TTParser.TIMEFRAME_NOVEMBER:
 				return TimeFrameConst.NOVEMBER;
 			case TTParser.TIMEFRAME_DECEMBER:
 				return TimeFrameConst.DECEMBER;
 			case TTParser.TIMEFRAME_WEEKEND:
 				return TimeFrameConst.WEEKEND;
 			case TTParser.TIMEFRAME_WEEKDAY:
 				return TimeFrameConst.WEEKDAY;
 
 			default: // catch unhandled node types
 				throw new UnsupportedOperationException("Node " + t.getText()
 						+ "<" + t.getType() + "> not handled");
 			}
 		} catch (Exception e) {
 			listener.error("problem executing " + t.toStringTree(), e);
 		}
 		return null;
 	}
 
 	public void tunit(CommonTree t) {
 		if (t.getType() != TTParser.TUNIT) {
 			listener.error("not a tunit: " + t.toStringTree());
 		}
 		MethodSymbol mainSymbol = (MethodSymbol) symbolTable.getSymbol("main");
 		if (mainSymbol == null) {
 			System.out.println("No main");
 			listener.error("no main method to execute: " + t.toStringTree());
 		} else {
 			exec(mainSymbol.methodBody);
 		}
 
 		// Clear global scope
 		symbolTable.removeScope();
 	}
 
 	public void evalGlobals() {
 		/* eval all global variables */
 		Scope globalScope = symbolTable.getScope(0);
 		for (Iterator<Map.Entry<String, Symbol>> it = globalScope.entrySet().iterator(); 
 				it.hasNext();) {
 			try {
 				Map.Entry<String, Symbol> symbolEntry = it.next();
 				VariableSymbol vs = (VariableSymbol)symbolEntry.getValue();
 				if (vs != null) {
 					if (vs.assignmentExpression == null) {
 						declarationEval(vs.declaration, true);
 					} else {
 						defineEval(vs.assignmentExpression, true);
 					}
 				}
 			} catch (Exception e) {
 				
 			}
 		}
 	}
 	
 	public void imports(CommonTree t) {
 		// System.out.println("Imports");
 	}
 
 	public void mainBlock(CommonTree t) {
 		symbolTable.addScope(); // add a scope for a main block
 		if (t.getType() != TTParser.MAIN) {
 			// Handle error
 			listener.error("not a mainblock: " + t.getType() + " "
 					+ t.toStringTree());
 
 		}
 		CommonTree mainBody = (CommonTree) t.getChild(0);
 		exec((CommonTree) mainBody);
 		symbolTable.removeScope();
 	}
 
 	public Object block(CommonTree t) {
 		// Execute code
 		if (t.getType() != TTParser.SLIST) {
 			// Handle error
 			listener.error("not a block: " + t.toStringTree());
 		}
 		int childrenCount = t.getChildCount();
 		for (int i = 0; i < childrenCount; i++) {
 			CommonTree childNode = (CommonTree) t.getChild(i);
 			Object rv = exec(childNode);
 			if (childNode.getType() == TTParser.BREAK
 					|| childNode.getType() == TTParser.CONTINUE)
 				return null;
 			if (childNode.getType() == TTParser.RETURN)
 				return rv;
 		}
 		return null;
 	}
 
 	
 	public void defineEval(CommonTree t, boolean isGlobal) {
 
 
 		CommonTree lhs = (CommonTree) t.getChild(0);
 		CommonTree expr = (CommonTree) t.getChild(1);
 
 		Object value = exec(expr);
 
 		String ident = null;
 
 
 		if(lhs.getType() == TTParser.DECLARE)
 			ident = declarationEval(lhs, isGlobal);
 
 		else {
 			// throw error
 		}
 		Symbol s = symbolTable.getSymbol(ident);
 
 		if (s == null) {
 			// throw error;
 		}
 		s.setValue(value);
 
 	}
 
 
 
 	public String declarationEval(CommonTree t, boolean isGlobal) {
 		
 		
 		if (t.getType() != TTParser.DECLARE) {
 			listener.error("not a declarition: " + t.toStringTree());
 			return null;
 		}
 
 		else {
 			String ident = null;
 			try {
 
 				System.out.println("Type" + t.getChild(0).getText());
 
 				String dataType = (String) exec((CommonTree) t.getChild(0));
 				ident = t.getChild(1).getText();
 				Object object = null;
 				if (dataType.equals("Calendar") || dataType.equals("Task")
 						|| dataType.equals("TimeFrame")
 						|| dataType.equals("Date")) {
 					dataType = TTConstants.PACKAGE_PREFIX + dataType;
 
 					Class<?> dataTypeClass = Class.forName(dataType);
 					object = dataTypeClass.newInstance();
 				} else if (dataType.equals("Number")) {
 					object = new Integer(0);
 				} else if (dataType.equals("String")) {
 					object = new String();
 				}else if(dataType.equals("Boolean")){
 					object = new Boolean(false);
 				}
 				else {
 					listener.error("Unsupported data type");
 					return null;
 				}
 
 				if (isGlobal) {
 					Symbol s = symbolTable.getSymbol(ident);
 					s.setValue(object);
 					s.setType(dataType);
 
 				} else {
 					symbolTable.addSymbol(ident, dataType, object);
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			return ident;
 		}
 
 	}
 
 	public Symbol identity(CommonTree t) {
 		//System.out.println("identity: "+t+ " "+t.getChild(0));
 		
 		Symbol s;
 		if(t.getChild(0) == null)
 			s = symbolTable.getSymbol(t.getText());
 		else
 			s = symbolTable.getSymbol(t.getChild(0).getText());
 		
 		return s;
 	}
 	
 	public Object identityValue(CommonTree t) {
 		Symbol s = identity(t);
 		return s.getValue();
 	}
 
 	public void assign(CommonTree t) {
 
 		CommonTree lhs = (CommonTree) t.getChild(0);
 		CommonTree expr = (CommonTree) t.getChild(1);
 
 		Object value = exec(expr);
 
 		if (lhs.getType() == TTParser.DOT) {
 			fieldassign(lhs, value);
 			return;
 		}
 
 		String ident = null;
 		if (lhs.getType() == TTParser.DECLARE)
 			ident = declarationEval(t, false);
 		else
 			ident = lhs.getText();
 		Symbol s = symbolTable.getSymbol(ident);
 
 		if (s == null) {
 			// throw error;
 		}
 		s.setValue(value);
 
 	}
 
 	private void fieldassign(CommonTree lhs, Object value) {
 
 		CommonTree o = (CommonTree) lhs.getChild(0);
 		CommonTree f = (CommonTree) lhs.getChild(1);
 		
 		String fieldname = f.getText().trim();
 
 		Symbol symbol = symbolTable.getSymbol(o.getText());
 		if (symbol == null) {
 			listener.error("No symbol in table for "+o.getText()+"."+f.getText());
 			return;
 		}
 
 		String dataType = symbol.getType();
 
 		if (dataType.equals(TTConstants.PACKAGE_PREFIX
 				+ TTConstants.CALENDAR_CLASS)) {
 
 			Calendar c = (Calendar) symbol.getValue();
 			if (fieldname.equals("name"))
 				c.setName((String) value);
 			else if (fieldname.equals("start"))
 				c.setStart((Date) value);
 			else if (fieldname.equals("end"))
 				c.setEnd((Date) value);
 			else {
 				listener.error("No field "+fieldname+" in Calendar");
 				return;
 			}
 
 		}
 
 		else if (dataType.equals(TTConstants.PACKAGE_PREFIX
 				+ TTConstants.DATE_CLASS)) {
 
 			Date d = (Date) symbol.getValue();
 			int val = (Integer) value;
 
 			if (fieldname.equals("year"))
 				d.setYear(val);
 			if (fieldname.equals("month"))
 				d.setMonth(val);
 			if (fieldname.equals("day"))
 				d.setDay(val);
 			if (fieldname.equals("hour"))
 				d.setHour(val);
 			if (fieldname.equals("minute"))
 				d.setMinute(val);
 			else {
 				listener.error("No field "+fieldname+" in Date");
 				return;
 			}
 
 		}
 
 		else if (dataType.equals(TTConstants.PACKAGE_PREFIX
 				+ TTConstants.TASK_CLASS)) {
 
 			Task t = (Task) symbol.getValue();
 			System.out.println(value);
 			if (fieldname.equals("name"))
 				t.setName((String) value);
 
 			if (fieldname.equals("start"))
 				t.setStart((Date) value);
 
 			if (fieldname.equals("end"))
 				t.setEnd((Date) value);
 
 			if (fieldname.equals("description"))
 				t.setDescription((String) value);
 
 			if (fieldname.equals("location"))
 				t.setLocation((String) value);
 
 			else {
 				listener.error("No field "+fieldname+" in Task");
 				return;
 			}
 
 		}
 
 		else if (dataType.equals(TTConstants.PACKAGE_PREFIX
 				+ TTConstants.TIMEFRAME_CLASS)) {
 
 			TimeFrame tf = (TimeFrame) symbol.getValue();
 			int val = (Integer) value;
 
 			if (fieldname.equals("years"))
 				tf.setYears(val);
 
 			if (fieldname.equals("months"))
 				tf.setMonths(val);
 
 			if (fieldname.equals("weeks"))
 				tf.setWeeks(val);
 
 			if (fieldname.equals("days"))
 				tf.setDays(val);
 
 			if (fieldname.equals("hours"))
 				tf.setHours(val);
 
 			if (fieldname.equals("minutes"))
 				tf.setMinutes(val);
 
 			else {
 
 				listener.error("No field "+fieldname+" in TimeFrame");
 				return;
 			}
 
 		}
 
 		else {
 			listener.error("Fields cannot be accessed on primitive types");
 			return;
 		}
 
 	}
 
 	public Object fieldAccess(CommonTree t) {
 
 		CommonTree o = (CommonTree) t.getChild(0);
 		CommonTree f = (CommonTree) t.getChild(1);
 		
		String fieldname = f.getText().trim();
 
 		Symbol symbol = symbolTable.getSymbol(o.getText());
 		if (symbol == null) {
 			listener.error("No symbol in table for "+o.getText()+"."+f.getText());
 		}
 		
 		String dataType = symbol.getType();
 
 		if (dataType.equals(
 				TTConstants.PACKAGE_PREFIX + TTConstants.CALENDAR_CLASS)) {
 
 			Calendar c = (Calendar) symbol.getValue();
 			if (fieldname.equals("name"))
 				return c.getName();
 			if (fieldname.equals("start"))
 				return c.getStart();
 			if (fieldname.equals("end"))
 				return c.getEnd();
 			else {
 				
 				listener.error("No field "+fieldname+" in Calendar");
 				return null;
 			}
 
 		}
 
 		else if (dataType.equals(
 				TTConstants.PACKAGE_PREFIX + TTConstants.DATE_CLASS)) {
 
 			Date d = (Date) symbol.getValue();
 
 			if (fieldname.equals("year"))
 				return d.getYear();
 			if (fieldname.equals("month"))
 				return d.getMonth();
 			if (fieldname.equals("day"))
 				return d.getDay();
 			if (fieldname.equals("hour"))
 				return d.getHour();
 			if (fieldname.equals("minute"))
 				return d.getMinute();
 			else {
 				
 				listener.error("No field "+fieldname+" in Date");
 				return null;
 			}
 
 		}
 
 		else if (dataType.equals(
 				TTConstants.PACKAGE_PREFIX + TTConstants.TASK_CLASS)) {
 
 			Task task = (Task) symbol.getValue();
 
 			if (fieldname.equals("name"))
 				return task.getName();
 
 			if (fieldname.equals("start"))
 				return task.getStart();
 
 			if (fieldname.equals("end"))
 				return task.getEnd();
 
 			if (fieldname.equals("description"))
 				return task.getDescription();
 
 			if (fieldname.equals("location"))
 				return task.getLocation();
 
 			else {
 				
 				listener.error("No field "+fieldname+" in Task");
 				return null;
 			}
 
 		}
 
 		else if (dataType.equals(
 				TTConstants.PACKAGE_PREFIX + TTConstants.TIMEFRAME_CLASS)) {
 
 			TimeFrame tf = (TimeFrame) symbol.getValue();
 
 			if (fieldname.equals("years"))
 				return tf.getYears();
 
 			if (fieldname.equals("months"))
 				return tf.getMonths();
 
 			if (fieldname.equals("weeks"))
 				return tf.getWeeks();
 
 			if (fieldname.equals("days"))
 				return tf.getDays();
 
 			if (fieldname.equals("hours"))
 				return tf.getHours();
 
 			if (fieldname.equals("minutes"))
 				return tf.getMinutes();
 
 			else {
 			
 				listener.error("No field "+fieldname+" in TimeFrame");
 				return null;
 			}
 		}
 		
 		else {
 			listener.error("No fields in primitive types");
 			return null;
 		}
 	}
 
 	
 	public Object plusEval(CommonTree t) {
 
 		System.out.println("" + " Operator Evaluation");
 				
 		Object a = exec((CommonTree) t.getChild(0));
 		Object b = exec((CommonTree) t.getChild(1));
 		
 		
 		if (a instanceof String && b instanceof String)
 			return a.toString() + b.toString();
 		else if (a instanceof Date && b instanceof TimeFrame) {
 			((Date)a).add((TimeFrame)b);
 			return a;
 		}
 		else if (a instanceof TimeFrame && b instanceof Date) {
 			((Date)b).add((TimeFrame)a);
 			return b;
 		}
 		else {
 			return arithmeticEval(t);
 		}
 	}
 
 	public Integer arithmeticEval(CommonTree t) {
 		System.out.println("Arithmetic Evaluation");
 
 		Integer a = (Integer) exec((CommonTree) t.getChild(0));
 		Integer b = (Integer) exec((CommonTree) t.getChild(1));
 
 		switch (t.getType()) {
 
 		case TTParser.PLUS:
 			System.out.println(a + b);
 			return a + b;
 
 		case TTParser.MINUS:
 			return a - b;
 
 		case TTParser.MULT:
 			return a * b;
 
 		case TTParser.DIV: {
 			if (b == 0) {
 				listener.error("invalid operation:" + t.toStringTree());
 			
 			}
 			return a / b;
 
 		}
 
 		case TTParser.MOD:
 			return a % b;
 
 		default:
 			return 0;
 
 		}
 
 	}
 
 	public Boolean logicalEval(CommonTree t) {
 
 		System.out.println("Logical Evaluation");
 
 		Boolean a = (Boolean) exec((CommonTree) t.getChild(0));
 		Boolean b = (Boolean) exec((CommonTree) t.getChild(1));
 
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
 
 	public Boolean equalityEval(CommonTree t) {
 
 		System.out.println("Equality Evaluation");
 		Object a = exec((CommonTree) t.getChild(0));
 		Object b = exec((CommonTree) t.getChild(1));
 
 		switch (t.getType()) {
 
 		case TTParser.EQUALS:
 			return a == b;
 		case TTParser.NOTEQUALS:
 			return a != b;
 		default:
 			return false;
 		}
 	}
 
 	public Boolean relationalEval(CommonTree t) {
 
 		System.out.println("Relational Evaluation " +t.getChild(0).getText()+" " +t.getChild(1));
 
 		Integer a = (Integer) exec((CommonTree) t.getChild(0));
 		System.out.println("a: "+a);
 		Integer b = (Integer) exec((CommonTree) t.getChild(1));
 		System.out.println("b: "+b);
 
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
 
 	public Object unaryExprEval(CommonTree t) {
 
 		System.out.println("UNARY: "+t.getChild(0).getType());
 		Object a = null;
 		
 		
 		if(t.getChild(0).getType() == TTParser.DOT)
 			a = fieldAccess((CommonTree)t.getChild(0));
 		else
 			a = exec((CommonTree) t.getChild(0));
 		
 		Object value = a;
 		
 		if (a == "not") {
 			System.out.println("a == not");
 			Object b = null;
 			if(t.getChild(0).getType() == TTParser.DOT)
 				b = fieldAccess((CommonTree)t.getChild(1));
 			else
 				b = exec((CommonTree) t.getChild(1));
 			System.out.println("b: "+b);
 			value = !(Boolean) b;
 		}
 		return value;
 	}
 
 	public Object call(CommonTree t) {
 		System.out.println("call "+t.getChild(0));
 		
 		if (t.getChild(0).getText().equals("addTask")){
 			//System.out.println("addTask "+t.getChild(1).getText() + " "+t.getChild(2).getText());
 			Symbol s = (Symbol)identity((CommonTree)t.getChild(1));
 			Calendar c = (Calendar)s.getValue();
 			//System.out.println("Calendar: " +c);
 			s = ((Symbol)identity((CommonTree)t.getChild(2)));
 			Task task = (Task)s.getValue();
 			
 			if(c == null)
 				System.out.println("C IS NULL");
 			c.add(task);
 			return null;
 		}
 		
 		String methodName = t.getChild(0).getText();
 		MethodSymbol methodSymbol = (MethodSymbol) symbolTable
 				.getSymbol(methodName);
 		
 		if (methodName == null) {
 			listener.error("no such method " + methodName, t.token);
 			return null;
 		}
 
 		int argCount = t.getChildCount() - 1;
 		// check for argument compatibility
 		ArrayList<Symbol> argsList = methodSymbol.getArgumentList();
 		if (argsList == null && argCount > 0 || argsList != null
 				&& argsList.size() != argCount) {
 			listener.error("method '" + methodName + "' argument list mismatch");
 			return null;
 		}
 		symbolTable.addScope(false);
 		int i = 0;
 		// evaluate and define arguments
 		for (Symbol arg : argsList) {
 			CommonTree ithArg = (CommonTree) t.getChild(i + 1);
 			Object argValue = exec(ithArg);
 			symbolTable.addSymbol(arg.getName(), arg.getType(), argValue);
 			i++;
 		}
 
 		Object result = exec(methodSymbol.methodBody);
 		symbolTable.removeScope();
 		return result;
 	}
 
 	public Object returnStmt(CommonTree t) {
 		int childrenCount = t.getChildCount();
 		if (childrenCount == 0)
 			return null;
 		return exec((CommonTree) t.getChild(0));
 	}
 
 	public void ifStatement(CommonTree t) {
 		System.out.println("IF " + t.getChildCount() + " "+ t.getChild(0).getText());
 		// 0th Child is the expr to evaluate
 		
 		Object o = exec((CommonTree) t.getChild(0));
 		System.out.println("OBJECT HERE: "+o);
 		
 		if ((Boolean) exec((CommonTree) t.getChild(0))) {
 			System.out.println("HERE");
 			// 1st Child is the block
 			exec((CommonTree) t.getChild(1));
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
 		System.out.println("\neveryDate! ");
 		// Declare variables
 		Date start = null;
 		Date end = null;
 		TimeFrame inc = null;
 		CommonTree block = null;
 		Date itterDate = null;
 		String type = null;
 		String name = null;
 
 		// Create a new scope that is a child of parent scope
 		symbolTable.addScope(true);
 		symbolTable.addSymbol(name, type, itterDate);
 
 		for (int i = 0; i < t.getChildCount(); i++) {
 			switch (t.getChild(i).getType()) {
 			case TTParser.FROM:
 				System.out.println("FROM: "+t.getChild(i).getChild(0));
 				start = (Date) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.TO:
 				System.out.println("TO: "+t.getChild(i).getChild(0));
 				end = (Date) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.BY:
 				System.out.println("BY: "+t.getChild(i).getChild(0));
 				inc = (TimeFrame) exec((CommonTree) t.getChild(i));
 				break;
 			case TTParser.SLIST:
 				System.out.println("SLIST: "+t.getChild(i).getChild(0));
 				block = (CommonTree) t.getChild(i);
 				break;
 			default:
 				// Handle "Date"
 				type = ((CommonTree) t.getChild(i)).getText();
 				name = ((CommonTree) t.getChild(i)).getChild(0).getText();
 				symbolTable.addSymbol(name, type, itterDate);
 				break;
 			}
 		}
 
 		if (start == null || end == null) {
 			// End of loop remove the scope
 			System.out.println("NO LOOP");
 			symbolTable.removeScope();
 			return;
 		}
 
 		// Define the itterDate
 		itterDate = start;
 		symbolTable.addSymbol(name, type, itterDate);
 		System.out.println("loop from: "+start+" to "+end);
 		while (itterDate.compareTo(end) <= 0) {
 			// Execute the block
 			exec(block);
 
 			// Check if a break was called in the block
 			if (breakCalled) {
 				breakCalled = false;
 				break;
 			}
 
 			// Increment the itterDate and update symbolTable
 			// itterDate = (Date)symbolTable.getSymbol(name);
 			itterDate.add(inc);
 			symbolTable.addSymbol(name, type, itterDate);
 		}
 
 		// End of loop remove the scope
 		symbolTable.removeScope();
 	}
 
 	public Date dateConstant(CommonTree t) {
 		return new Date(((CommonTree) t.getChild(0)).getText());
 	}
 
 	public Date dateOrIdent(CommonTree t) {
 		if (t.getChild(0).getType() == TTParser.IDENT_TOKEN)
 		{
 			Symbol s = (Symbol)exec((CommonTree)t.getChild(0));
 			return (Date)s.getValue();
 		}
 		return (Date) exec((CommonTree) t.getChild(0));
 	}
 
 	public TimeFrame timeFrameOrIdent(CommonTree t) {
 		if (t.getChild(0).getType() == TTParser.IDENT_TOKEN)
 		{
 			Symbol s = (Symbol)exec((CommonTree)t.getChild(0));
 			return (TimeFrame)s.getValue();
 		}
 		return (TimeFrame) exec((CommonTree) t.getChild(0));
 	}
 
 	public TimeFrame timeFrameConstant(CommonTree t) {
 		String tf = "";
 		for (int i = 0; i < t.getChildCount(); i++) {
 			tf = tf + " " + ((CommonTree) t.getChild(i)).getText();
 		}
 		System.out.println(tf);
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
 				/*
 				 * c = new Calendar("Temp"); c.add(new Task("one", new
 				 * Date("2013.01.01.10"), new Date("2013.01.01.12"), "here", 0,
 				 * "desc")); c.add(new Task("two", new Date("2013.01.01.13"),
 				 * new Date("2013.01.01.13.50"), "here", 0, "desc")); c.add(new
 				 * Task("three", new Date("2013.01.01.14"), new
 				 * Date("2013.01.01.14.10"), "here", 0, "desc")); c.add(new
 				 * Task("four", new Date("2013.01.01.20"), new
 				 * Date("2013.01.01.21"), "here", 0, "desc"));
 				 */
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
 				if (breakCalled) {
 					breakCalled = false;
 					break;
 				}
 
 			}
 		}
 
 	}
 
 	public void breakFunc(CommonTree t) {
 		breakCalled = true;
 	}
 
 	public void exitFunc(CommonTree t) {
 		System.exit(0);
 	}
 
 	public Boolean on(CommonTree t) {
 		//System.out.println("ON " + t.getChild(0));
 		return (Boolean) exec((CommonTree) t.getChild(0));
 	}
 
 	public Calendar in(CommonTree t) {
 
 		//System.out.println("IN "+t.getChild(0)+ " "+t.toString());
 		Symbol s = (Symbol)exec((CommonTree)t.getChild(0));
 		//System.out.println("Calendar: "+ s.getType()+ " "+s.getValue());
 		return (Calendar)s.getValue();
 
 
 	}
 	public Object read(CommonTree t) {
 		Object result = exec((CommonTree) t.getChild(0));
 		System.out.print(result);
 		
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		Object userInput = null;
 		try {
 			userInput = in.readLine();
 		} catch (IOException e) {
 			return userInput;
 		}
 		return userInput;
 	}
 	
 	public void print(CommonTree t) {
 		if (((CommonTree) t.getChild(0)).getType() == TTParser.IDENT_TOKEN) {
 			Symbol s = (Symbol) exec((CommonTree) t.getChild(0));
 			System.out.println(s.getValue());
 		} else
 			System.out.println(t.getChild(0).getText());
 	}
 }
