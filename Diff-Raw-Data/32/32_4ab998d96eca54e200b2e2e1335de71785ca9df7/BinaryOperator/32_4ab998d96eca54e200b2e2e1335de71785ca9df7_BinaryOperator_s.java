 package banjo.parser.ast;
 
 
 public enum BinaryOperator implements Operator {
 	LOOKUP(ParenType.BRACKETS),
 	CALL(ParenType.PARENS),
 	PROJECTION(".", Precedence.SUFFIX, Associativity.RIGHT),
 	MAP_PROJECTION("?.", Precedence.SUFFIX),
 	POW("^", Precedence.MULDIV, "toThePowerOf"),
 	MUL("*", 0x00D7, Precedence.MULDIV, "times"),
 	DIV("/", 0x00F7, Precedence.MULDIV, "dividedBy"),
 	ADD("+", Precedence.ADDSUB, "plus"),
 	SUB("-", Precedence.ADDSUB, "minus"),
 	GT(">", Precedence.ORDERING),
 	GE(">=", 0x2265, Precedence.ORDERING),
 	LT("<", Precedence.ORDERING),
 	LE("<=", 0x2264, Precedence.ORDERING),
 	EQ("==", Precedence.EQUALITY, "eq"),
 	NEQ("!=", 0x2260, Precedence.EQUALITY),
 	CMP("<=>", Precedence.EQUALITY, "cmp"),
 	INTERSECT("&", 0x2229, Precedence.INTERSECT, "intersection"),
 	XOR("><", 0x22BB, Precedence.XOR, "xor"),
 	UNION("|", 0x222A, Precedence.UNION, "union"),
 	LAZY_AND("&&", 0x2227, Precedence.LAZY_AND),
 	LAZY_OR("||", 0x2228, Precedence.LAZY_OR),
 	COMMA(",", Precedence.COMMA),
 	FUNCTION("->", 0x21A6, Precedence.FUNCTION, Associativity.RIGHT),
 	ASSIGNMENT("=", Precedence.ASSIGNMENT, Associativity.RIGHT),
 	PAIR(":", Precedence.COLON, Associativity.RIGHT),
	PAIR2("::", Precedence.ASSIGNMENT, Associativity.RIGHT),
 	COND("=>", 0x21D2, Precedence.COND, Associativity.RIGHT),
 	OR_ELSE("?:", Precedence.COND, Associativity.RIGHT),
 	SEMICOLON(";", Precedence.SEMICOLON),
 	NEWLINE("\\n", Precedence.SEMICOLON),
 	INVALID("~~~INVALID~~~", -1, Precedence.ATOM),
 	MISSING("~~~MISSING~~~", -1, Precedence.ATOM); // Newline and indent
 	
 	public static enum Associativity { LEFT, RIGHT; }
 	private final String op;
 	private final int codePoint; // -1 if no special unicode character
 	private final Precedence precedence;
 	private final ParenType parenType; // nullable
 	private final String methodName; // nullable
 	private final Associativity associativity;
 	
 	BinaryOperator(String op, int codePoint, Precedence p, ParenType parenType, String methodName, Associativity associativity) {
 		this.op = op;
 		this.codePoint = codePoint;
 		this.precedence = p;
 		this.parenType = parenType;
 		this.methodName = methodName;
 		this.associativity = associativity;
 	}
 	
 	BinaryOperator(String op, int codePoint, Precedence p, ParenType parenType, String methodName) {
 		this(op, codePoint, p, parenType, methodName, Associativity.LEFT);
 	}
 	BinaryOperator(String op, int codePoint, Precedence p, String methodName) {
 		this(op, codePoint, p, null, methodName);
 	}
 	
 	BinaryOperator(String op, Precedence p) {
 		this(op, -1, p);
 	}
 	BinaryOperator(String op, Precedence p, Associativity associativity) {
 		this(op, -1, p, associativity);
 	}
 	BinaryOperator(String op, Precedence p, String methodName) {
 		this(op, -1, p, methodName);
 	}
 	BinaryOperator(String op, int codePoint, Precedence p) {
 		this(op, codePoint, p, null, null);
 	}
 	BinaryOperator(String op, int codePoint, Precedence p, Associativity associativity) {
 		this(op, codePoint, p, null, null, associativity);
 	}
 	BinaryOperator(ParenType pt) {
 		this(null, -1, Precedence.SUFFIX, pt, null);
 	}
 
 	public String getOp() {
 		return op;
 	}
 
 	public Precedence getPrecedence() {
 		return precedence;
 	}
 
 	public static BinaryOperator fromOp(String op) {
 		if(op == null)
 			return null;
 		for(BinaryOperator operator : values()) {
 			if(op.equals(operator.op) || (operator.codePoint > 0 && op.length() == 1 && op.codePointAt(0) == operator.codePoint)) {
 				return operator;
 			}
 		}
 		return null;
 	}
 	public static BinaryOperator fromParenType(ParenType pt) {
 		if(pt == null)
 			return null;
 		for(BinaryOperator operator : values()) {
 			if(pt.equals(operator.parenType)) {
 				return operator;
 			}
 		}
 		return null;
 	}
 	public int getCodePoint() {
 		return codePoint;
 	}
 	public ParenType getParenType() {
 		return parenType;
 	}
 	public boolean isParen() {
 		return parenType != null;
 	}
 	public String getMethodName() {
 		return methodName;
 	}
 
 	public Associativity getAssociativity() {
 		return associativity;
 	}
 	
 	public boolean isLeftAssociative() {
 		return associativity == Associativity.LEFT;
 	}
 	
 	public boolean isRightAssociative() {
 		return associativity == Associativity.RIGHT;
 	}
 }
