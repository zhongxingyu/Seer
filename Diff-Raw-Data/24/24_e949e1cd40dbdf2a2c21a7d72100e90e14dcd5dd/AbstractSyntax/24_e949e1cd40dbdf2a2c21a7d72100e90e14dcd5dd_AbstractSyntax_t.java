 // Abstract syntax for the language C++Lite,
 // exactly as it appears in Appendix B.
 import java.util.*;
 
 class Program {
     // Program = Declarations globals ; Block functions
 
     private static final String TAB = "  ";	
 
     Declarations globals;
     Functions functions;
 
     Program (Declarations g, Functions f) {
         globals = g;
         functions = f;
     }
     
     void display() {
 	String prefix = TAB + "Globals:\n";
 	String declars;
 	if (globals.size() < 1) 
 		declars = TAB + TAB + "None\n";
 	else {
 		declars = TAB + TAB + "{";
 			for (int i=0; i<globals.size();i++) {
 				declars += globals.get(i) + ", ";
 			}
 		declars = declars.substring(0,declars.length()-2) + "}\n";
 	}
 	System.out.print(prefix + declars + inner_display( TAB, TAB, functions));
     } 
 
     String inner_display(String spc, String spcing, Object node) {
 	if (node instanceof Unary) {
 		Unary u_node = (Unary) node;
 		String prefix = spcing + "Unary:\n";
 		String branch1 = inner_display(spc, spc + spcing, u_node.op);
 		String branch2 = inner_display(spc, spc + spcing, u_node.term);
 		return prefix + branch1 + branch2; 
 		
 	} if (node instanceof Binary) {
 		Binary b_node = (Binary) node;
 		String prefix = spcing + "Binary:\n";
 		String branch1 = inner_display(spc, spc + spcing, b_node.op);
 		String branch2 = inner_display(spc, spc + spcing, b_node.term1);
 		String branch3 = inner_display(spc, spc + spcing, b_node.term2);
 		return prefix + branch1 + branch2 + branch3;
 	} if (node instanceof Assignment) {
 		Assignment a_node = (Assignment) node;
 		String prefix = spcing + "Assignment:\n";
 		String branch1 = inner_display(spc, spc + spcing, a_node.target);
 		String branch2 = inner_display(spc, spc + spcing, a_node.source);
 		return prefix + branch1 + branch2;	
 	} if (node instanceof Block) {
 		Block blk_node = (Block) node;
 		String prefix = spcing + "Block:\n";
 		String blk_string = "";
 		for (int i=0; i<blk_node.members.size(); i++) {
 			blk_string += inner_display(spc, spc + spcing, blk_node.members.get(i));
 		}
 		return prefix + blk_string;
 	} if (node instanceof Conditional) {
 		Conditional con_node = (Conditional) node;
 		String prefix = spcing + "Conditional:\n";
 		String test = inner_display(spc, spc + spcing, con_node.test);
 		String then_branch = spcing + "then:\n" + inner_display(spc, spc + spcing, con_node.thenbranch);
 		String else_branch = spcing +  "else:\n " + inner_display(spc, spc + spcing, con_node.elsebranch);
 		return prefix + test + then_branch + else_branch;
 	} if (node instanceof Loop) {
 		Loop l_node = (Loop) node;
 		String prefix = spcing + "Loop:\n";
 		String test = inner_display(spc, spc + spcing, l_node.test);
 		String body = inner_display(spc, spc + spcing, l_node.body); 	
 		return prefix + test + body;
 	} if (node instanceof Functions) {
 		Functions f_node = (Functions) node;
 		String prefix = spcing + "Functions:\n";
 		String f_string = "";
 		if (f_node.size() < 1)
 			return spcing + spc + "None\n";
 		for (int i=0; i<f_node.size(); i++) {
 			f_string += inner_display(spc, spc + spcing, f_node.get(i));
 		}
 		return prefix + f_string;
 	} if (node instanceof Declarations) {
 		Declarations d_node = (Declarations) node;
 		String declars = spcing + spc + "{";
 		if (d_node.size() < 1) 
 			return spcing + spc + "None\n";
 		for (int i=0; i<d_node.size();i++) {
 			declars += d_node.get(i) + ", ";
 		}
 		declars = declars.substring(0,declars.length()-2) + "}\n";
 		return declars;
 	} if (node instanceof Function) { 
 		Function f_node = (Function) node;
 		//String prefix = spcing + "Function:\n";
 		String title_and_type = spcing + "Function Name: " + f_node.id + " Type: " + f_node.t + "\n";
 		String params = spc + spcing + "Parameters:\n" + inner_display(spc, spc + spcing, f_node.params);
 		String locals = spc + spcing + "Locals:\n" + inner_display(spc, spc + spcing, f_node.locals);
 		String body = inner_display(spc, spc + spcing, f_node.body);
 		return title_and_type + params + locals + body;
 	} if (node instanceof Return) {
 		Return r_node = (Return) node;
 		String prefix = spcing + "Return:\n";
 		String target = inner_display(spc, spc + spcing, r_node.target);
 		String result = inner_display(spc, spc + spcing, r_node.result);
 		return prefix + target + result;
 	} if (node instanceof CallStatement) {
 		CallStatement c_node = (CallStatement) node;
 		String prefix = spcing + "Call:\n";
 		String name = spc + spcing + "Name: " + c_node.name + "\n"; 
 		String args = inner_display(spc, spc + spcing, c_node.args);	
 		return prefix + name + args;
 	} if (node instanceof CallExpression) { 
 		CallExpression c_node = (CallExpression) node;
 		String prefix = spcing + "Call:\n";
 		String name = spc + spcing + "Name: " + c_node.name + "\n"; 
 		String args = inner_display(spc, spc + spcing, c_node.args);	
 		return prefix + name + args;
 	} if (node instanceof Expressions) {
 		Expressions e_node = (Expressions) node;
 		String prefix = spcing + "Expressions:\n";
 		String e_string = "";
 		for (int i=0;i<e_node.size();i++)
 			e_string += inner_display(spc, spc + spcing, e_node.get(i));
 		return prefix + e_string;
 	} if (node instanceof ArrayRef) {
 		ArrayRef a_node = (ArrayRef) node;
 		String prefix = spcing + "ArrayRef:\n";
 		String index = spcing + spc + "id: " + a_node.id + "\n";
 		String contents = inner_display(spc, spc + spcing, a_node.index);
 		return prefix + index + contents;
 	} if (node instanceof Print) {
 		Print p_node = (Print) node;
 		String prefix = spcing + "Print:\n";
 		String expr = inner_display(spc, spc + spcing, p_node.to_print);
 		return prefix + expr;
 	} else {
 		String prefix = spcing + (node.getClass() + "").substring(6) + ": ";
 		String value = node + "\n"; 
 		return prefix + value; 
 	}
     }
 }
 
 class Functions extends ArrayList<Function> {
 	// Functions = Function*
 	// (a list of functions f1, f2, ..., fn)
 
 	public Function get(String name) {
 		for (Function fi : this) 
 			if (fi.id.equals(name)) 
 				return fi;
 		throw new IllegalArgumentException("no func '" + name + "' has been defined");
 	}
 
 }
 
 class Function {
 	// Type t; String id; Declarations params, locals; Block body;
 
 	Type t;
 	String id;
 	Declarations params, locals;
 	Block body;
 
 	Function (Type t, String id, Declarations params, Declarations locals, Block body) {
 		this.t = t; this.id = id; this.locals = locals; this.params = params; this.body = body;
 	}
 }
 
 class Declarations extends ArrayList<Declaration> {
     // Declarations = Declaration*
     // (a list of declarations d1, d2, ..., dn)
 
 }
 
 abstract class Declaration {
 // Declaration = VariableDecl | ArrayDecl
 	Variable v;
 	Type t;
 
 }
 
 class VariableDecl extends Declaration {
 // VariableDecl = Variable V; Type t
 
     VariableDecl (Variable var, Type type) {
         super.v = var; super.t = type;
     } // declaration */
 
     public String toString( ) {
 	return "<" + v + ", " + t + ">";
     }
 }
 
 
 class ArrayDecl extends Declaration {
 // ArrayDecl = Variable v; Type t; Integer size
 
 	IntValue size;
 	
 	ArrayDecl (Variable var, Type type, IntValue alloc) {
 	   super.v = var; super.t = type; size = alloc;
 	}	
 	
 	public String toString( ) {
 	   return "<" + v + ", " + t + ", size: " + size + ">";
 	}
 }
 
 class Type {
     // Type = int | bool | char | float | void
     final static Type INT = new Type("int");
     final static Type BOOL = new Type("bool");
     final static Type CHAR = new Type("char");
     final static Type FLOAT = new Type("float");
     final static Type VOID = new Type("void");
     // final static Type UNDEFINED = new Type("undef");
     
     private String id;
 
     private Type (String t) { id = t; }
 
     public String toString ( ) { return id; }
 
     // returns the equivaled Jasmin type descriptor
     public String to_jasmin() {
 	if(this.equals(Type.INT) || this.equals(Type.BOOL) || this.equals(Type.CHAR))
 		return "I";
 	else if (this.equals(Type.FLOAT))
 		return "F";
 	else if (this.equals(Type.VOID))
 		return "V";
 	else
 		throw new IllegalArgumentException("No such type");
     }
 }
 
 abstract class Statement {
     // Statement = Skip | Block | Assignment | Conditional | Loop | Call | Return
 
 	boolean hasReturn() {
 		return false;
 	}
 
 }
 
 class Skip extends Statement {
 }
 
 class Block extends Statement {
     // Block = Statement*
     //         (a Vector of members)
     public ArrayList<Statement> members = new ArrayList<Statement>();
 
 	boolean hasReturn() {
 		for (Statement s : members) {
 			boolean saw_ret = s.hasReturn();
 			if (saw_ret)
 				return true;
 		}
 		return false;
 	}
 
 	/*
 	public void display() {
 		for (Statement s : members) {
 			s.display();
 		}
 	}
 	*/
 }
 
 class Assignment extends Statement {
     // Assignment = VariableRef target; Expression source
     VariableRef target;
     Expression source;
 
     Assignment (VariableRef t, Expression e) {
         target = t;
         source = e;
     }
 }
 
 class Conditional extends Statement {
 // Conditional = Expression test; Statement thenbranch, elsebranch
     Expression test;
     Statement thenbranch, elsebranch;
     // elsebranch == null means "if... then"
     
     Conditional (Expression t, Statement tp) {
         test = t; thenbranch = tp; elsebranch = new Skip( );
     }
     
     Conditional (Expression t, Statement tp, Statement ep) {
         test = t; thenbranch = tp; elsebranch = ep;
     }
 
 	boolean hasReturn() {
 		return (thenbranch.hasReturn() || elsebranch.hasReturn());
 	}
 
 	boolean mustReturn() {
 		return (thenbranch.hasReturn() && elsebranch.hasReturn());
 	}
 
 	/*
 	public void display() {
 		System.out.println("Conditional: ");
 		System.out.print("\t"); test.display();
 		System.out.println("\t"); thenbranch.display();	
 		System.out.println("\t"); elsebranch.display();
    	} 
 	*/
 }
 
 class Loop extends Statement {
 // Loop = Expression test; Statement body
     Expression test;
     Statement body;
 
     Loop (Expression t, Statement b) {
         test = t; body = b;
     }
 
 	boolean hasReturn() {
 		return body.hasReturn();
 	}	
     
 }
 
 class CallStatement extends Statement {
 	String name;
 	Expressions args;
 
 	CallStatement (String name, Expressions args) {
 		this.name = name; this.args = args;
 	}
 }
 
 class Return extends Statement {
 	VariableRef target;
 	Expression result;
 
 	Return (VariableRef target, Expression result) {
 		this.target = target; this.result = result;
 	}
 
 	boolean hasReturn() {
 		return true;
 	}
 }
 	
 class Expressions extends ArrayList<Expression> {
 	// Expressions = Expression*
 }
 
 abstract class Expression {
     // Expression = VariableRef | Value | Binary | Unary | Call
 }
 
 abstract class VariableRef extends Expression {
    // VariableRef = Variable | ArrayRef
 	String id;
 
 	public boolean equals (Object obj) {
 	    String v = ((VariableRef) obj).id;
 	    System.out.println("VarRef equals called");
 	    return id.equals(v);
 	}
 }
 
 class Variable extends VariableRef {
     // Variable = String id
 
     Variable (String s) { super.id = s; }
 
     public String toString( ) { return id; }
     
     public boolean equals (Object obj) {
     	try {
         	String s = ((Variable) obj).id;
         	return id.equals(s); // case-sensitive identifiers
 	}
 	catch (ClassCastException e) {
 		return false;
 	}
     }
     
     public int hashCode ( ) { return id.hashCode( ); }
 }
 
 class ArrayRef extends VariableRef {
     // ArrayRef = String id; Expression index
 	
     Expression index;
 
     ArrayRef (String s, Expression e) {
 	super.id = s; index = e;
     }
 
     public String toString( ) { return id + "[" + index + "]"; }
 
     public boolean equals (Object obj) {
     	try {
 	    ArrayRef a = (ArrayRef) obj;
 	    String aid = a.id;
 	    Expression aindex = a.index;
 	    return id.equals(aid) && index.equals(aindex); 
         }
 	catch (ClassCastException e) {
 		return false; 
 	}
     }
 
     public int hashCode( ) {
     	IntValue ival = (IntValue) index;
     	return id.hashCode() + ival.intValue(); 
     }
     // I think the reason the ArrayRefs are not being matched. Is because I have not implemented the function hashCode, for ArrayRef
 }
 	
 abstract class Value extends Expression {
     // Value = IntValue | BoolValue |
     //         CharValue | FloatValue
     protected Type type;
     protected boolean undef = true;
 
     int intValue ( ) {
         assert false : "should never reach here";
         return 0;
     }
     
     boolean boolValue ( ) {
         assert false : "should never reach here";
         return false;
     }
     
     char charValue ( ) {
         assert false : "should never reach here";
         return ' ';
     }
     
     float floatValue ( ) {
         assert false : "should never reach here";
         return 0.0f;
     }
 
     boolean isUndef( ) { return undef; }
 
     Type type ( ) { return type; }
 
     static Value mkValue (Type type) {
         if (type == Type.INT) return new IntValue( );
         if (type == Type.BOOL) return new BoolValue( );
         if (type == Type.CHAR) return new CharValue( );
         if (type == Type.FLOAT) return new FloatValue( );
         throw new IllegalArgumentException("Illegal type in mkValue");
     }
 }
 
 class IntValue extends Value {
     private int value = 0;
 
     IntValue ( ) { type = Type.INT; }
 
     IntValue (int v) { this( ); value = v; undef = false; }
 
     int intValue ( ) {
         assert !undef : "reference to undefined int value";
         return value;
     }
 
     public String toString( ) {
         if (undef)  return "undef";
         return "" + value;
     }
 
     public boolean equals(Object obj) {
     	IntValue iv = (IntValue) obj;
 	return iv.value == this.value;
     }
 
 }
 
 class BoolValue extends Value {
     private boolean value = false;
 
     BoolValue ( ) { type = Type.BOOL; }
 
     BoolValue (boolean v) { this( ); value = v; undef = false; }
 
     boolean boolValue ( ) {
         assert !undef : "reference to undefined bool value";
         return value;
     }
 
     int intValue ( ) {
         assert !undef : "reference to undefined bool value";
         return value ? 1 : 0;
     }
 
     public String toString( ) {
         if (undef)  return "undef";
         return "" + value;
     }
 
 }
 
 class CharValue extends Value {
     private char value = ' ';
 
     CharValue ( ) { type = Type.CHAR; }
 
     CharValue (char v) { this( ); value = v; undef = false; }
 
     char charValue ( ) {
         assert !undef : "reference to undefined char value";
         return value;
     }
 
     public String toString( ) {
         if (undef)  return "undef";
         return "" + value;
     }
 
 }
 
 class FloatValue extends Value {
     private float value = 0;
 
     FloatValue ( ) { type = Type.FLOAT; }
 
     FloatValue (float v) { this( ); value = v; undef = false; }
 
     float floatValue ( ) {
         assert !undef : "reference to undefined float value";
         return value;
     }
 
     public String toString( ) {
         if (undef)  return "undef";
         return "" + value;
     }
 
 }
 
 class Binary extends Expression {
 // Binary = Operator op; Expression term1, term2
     Operator op;
     Expression term1, term2;
 
     Binary (Operator o, Expression l, Expression r) {
         op = o; term1 = l; term2 = r;
     } // binary
 	
 }
 
 class Unary extends Expression {
     // Unary = Operator op; Expression term
     Operator op;
     Expression term;
 
     Unary (Operator o, Expression e) {
         op = o; term = e;
     } // unary
 
 }
 
 class CallExpression extends Expression {
 	// Call = String name; Expressions args
 
 	String name;
 	Expressions args;
 
 	CallExpression (String name, Expressions args) {
 		this.name = name; this.args = args;
 	}
 
 	public String toString() {
 		String prefix = name;
 		String acc = "";
 		if (args.size() < 1) 
 			return prefix + "()";
 		for(int i=0; i<args.size();i++) {
 			acc += args.get(i) +", ";	
 		}
 		return prefix + "(" + acc.substring(0, acc.length()-2) + ")";
 	}
 }
 
 class Operator {
     // Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
     // BooleanOp = && | ||
     final static String AND = "&&";
     final static String OR = "||";
     // RelationalOp = < | <= | == | != | >= | >
     final static String LT = "<";
     final static String LE = "<=";
     final static String EQ = "==";
     final static String NE = "!=";
     final static String GT = ">";
     final static String GE = ">=";
     // ArithmeticOp = + | - | * | /
     final static String PLUS = "+";
     final static String MINUS = "-";
     final static String TIMES = "*";
     final static String DIV = "/";
     // UnaryOp = !    
     final static String NOT = "!";
     final static String NEG = "-NEG"; //This has been changed from "-" to "-NEG" to avoid ambiguity with MINUS
     // CastOp = int | float | char
     final static String INT = "int";
     final static String FLOAT = "float";
     final static String CHAR = "char";
     // Typed Operators
     // RelationalOp = < | <= | == | != | >= | >
     final static String INT_LT = "INT<";
     final static String INT_LE = "INT<=";
     final static String INT_EQ = "INT==";
     final static String INT_NE = "INT!=";
     final static String INT_GT = "INT>";
     final static String INT_GE = "INT>=";
     // ArithmeticOp = + | - | * | /
     final static String INT_PLUS = "INT+";
     final static String INT_MINUS = "INT-";
     final static String INT_TIMES = "INT*";
     final static String INT_DIV = "INT/";
     // UnaryOp = !    
     final static String INT_NEG = "INT-NEG";
     // RelationalOp = < | <= | == | != | >= | >
     final static String FLOAT_LT = "FLOAT<";
     final static String FLOAT_LE = "FLOAT<=";
     final static String FLOAT_EQ = "FLOAT==";
     final static String FLOAT_NE = "FLOAT!=";
     final static String FLOAT_GT = "FLOAT>";
     final static String FLOAT_GE = "FLOAT>=";
     // ArithmeticOp = + | - | * | /
     final static String FLOAT_PLUS = "FLOAT+";
     final static String FLOAT_MINUS = "FLOAT-";
     final static String FLOAT_TIMES = "FLOAT*";
     final static String FLOAT_DIV = "FLOAT/";
     // UnaryOp = !    
     final static String FLOAT_NEG = "FLOAT-NEG";
     // RelationalOp = < | <= | == | != | >= | >
     final static String CHAR_LT = "CHAR<";
     final static String CHAR_LE = "CHAR<=";
     final static String CHAR_EQ = "CHAR==";
     final static String CHAR_NE = "CHAR!=";
     final static String CHAR_GT = "CHAR>";
     final static String CHAR_GE = "CHAR>=";
     // RelationalOp = < | <= | == | != | >= | >
     final static String BOOL_LT = "BOOL<";
     final static String BOOL_LE = "BOOL<=";
     final static String BOOL_EQ = "BOOL==";
     final static String BOOL_NE = "BOOL!=";
     final static String BOOL_GT = "BOOL>";
     final static String BOOL_GE = "BOOL>=";
     // Type specific cast
     final static String I2F = "I2F";
     final static String F2I = "F2I";
     final static String C2I = "C2I";
     final static String I2C = "I2C";
     
     String val;
     
     Operator (String s) { val = s; }
 
     public String toString( ) { return val; }
     public boolean equals(Object obj) { return val.equals(obj); }
     
     boolean BooleanOp ( ) { return val.equals(AND) || val.equals(OR); }
     boolean RelationalOp ( ) {
         return val.equals(LT) || val.equals(LE) || val.equals(EQ)
             || val.equals(NE) || val.equals(GT) || val.equals(GE);
     }
     boolean ArithmeticOp ( ) {
         return val.equals(PLUS) || val.equals(MINUS)
             || val.equals(TIMES) || val.equals(DIV)
             || val.equals(INT_PLUS) || val.equals(INT_MINUS)
             || val.equals(INT_TIMES) || val.equals(INT_DIV)
             || val.equals(FLOAT_PLUS) || val.equals(FLOAT_MINUS)
             || val.equals(FLOAT_TIMES) || val.equals(FLOAT_DIV);
     }
     boolean NotOp ( ) { return val.equals(NOT) ; }
    boolean NegateOp ( ) { return (val.equals(NEG) || val.equals(INT_NEG) || 
				   val.equals(FLOAT_NEG)); }
     boolean intOp ( ) { return val.equals(INT); }
     boolean floatOp ( ) { return val.equals(FLOAT); }
     boolean charOp ( ) { return val.equals(CHAR); }
 
     final static String intMap[ ] [ ] = {
         {PLUS, INT_PLUS}, {MINUS, INT_MINUS},
         {TIMES, INT_TIMES}, {DIV, INT_DIV},
         {EQ, INT_EQ}, {NE, INT_NE}, {LT, INT_LT},
         {LE, INT_LE}, {GT, INT_GT}, {GE, INT_GE},
         {NEG, INT_NEG}, {FLOAT, I2F}, {CHAR, I2C}
     };
 
     final static String floatMap[ ] [ ] = {
         {PLUS, FLOAT_PLUS}, {MINUS, FLOAT_MINUS},
         {TIMES, FLOAT_TIMES}, {DIV, FLOAT_DIV},
         {EQ, FLOAT_EQ}, {NE, FLOAT_NE}, {LT, FLOAT_LT},
         {LE, FLOAT_LE}, {GT, FLOAT_GT}, {GE, FLOAT_GE},
         {NEG, FLOAT_NEG}, {INT, F2I}
     };
 
     final static String charMap[ ] [ ] = {
         {EQ, CHAR_EQ}, {NE, CHAR_NE}, {LT, CHAR_LT},
         {LE, CHAR_LE}, {GT, CHAR_GT}, {GE, CHAR_GE},
         {INT, C2I}
     };
 
     final static String boolMap[ ] [ ] = {
         {EQ, BOOL_EQ}, {NE, BOOL_NE}, {LT, BOOL_LT},
         {LE, BOOL_LE}, {GT, BOOL_GT}, {GE, BOOL_GE},
 	{OR, OR}, {AND, AND}, {NOT, NOT}
     };
 
     final static private Operator map (String[][] tmap, String op) {
         for (int i = 0; i < tmap.length; i++)
             if (tmap[i][0].equals(op))
                 return new Operator(tmap[i][1]);
         assert false : "should never reach here";
         return null;
     }
 
     final static public Operator intMap (String op) {
         return map (intMap, op);
     }
 
     final static public Operator floatMap (String op) {
         return map (floatMap, op);
     }
 
     final static public Operator charMap (String op) {
         return map (charMap, op);
     }
 
     final static public Operator boolMap (String op) {
         return map (boolMap, op);
     }
 
 }
