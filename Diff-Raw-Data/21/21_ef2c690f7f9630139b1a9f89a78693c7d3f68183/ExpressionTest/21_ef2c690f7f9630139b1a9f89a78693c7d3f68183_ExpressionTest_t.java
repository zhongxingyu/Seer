 package titocc.compiler;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import titocc.compiler.Symbol.Category;
 import titocc.compiler.elements.Expression;
 import titocc.compiler.types.ArrayType;
 import titocc.compiler.types.CType;
 import titocc.compiler.types.FunctionType;
 import titocc.compiler.types.PointerType;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 import titocc.tokenizer.Tokenizer;
 
 public class ExpressionTest
 {
 	private Scope scope;
 
 	private String parse(String code) throws IOException, SyntaxException
 	{
 		Tokenizer tokenizer = new Tokenizer(new StringReader(code));
 		Expression expr = Expression.parse(new TokenStream(tokenizer.tokenize()));
 		return expr.toString();
 	}
 
 	private void testType(String s, CType expectedType) throws IOException, SyntaxException
 	{
 		Tokenizer tokenizer = new Tokenizer(new StringReader(s));
 		Expression expr = Expression.parse(new TokenStream(tokenizer.tokenize()));
 		assertEquals(expectedType, expr.getType(scope));
 	}
 
 	@Before
 	public void setUp()
 	{
 		scope = new Scope(null, "");
 		scope.add(new Symbol("v", CType.VOID, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("c", CType.CHAR, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("uc", CType.UCHAR, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("sc", CType.SCHAR, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("s", CType.SHORT, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("us", CType.USHORT, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("i", CType.INT, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("u", CType.UINT, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("l", CType.LONG, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("ul", CType.ULONG, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("ll", CType.LLONG, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("ull", CType.ULLONG, Category.GlobalVariable, null, false));
 		scope.add(new Symbol("fv", new FunctionType(CType.VOID, new ArrayList<CType>()),
 				Category.GlobalVariable, null, false));
 		scope.add(new Symbol("fi", new FunctionType(CType.INT, new ArrayList<CType>()),
 				Category.GlobalVariable, null, false));
 		scope.add(new Symbol("a", new ArrayType(CType.INT, 2), Category.GlobalVariable, null,
 				false));
 		scope.add(new Symbol("a2", new ArrayType(new ArrayType(CType.INT, 2), 3),
 				Category.GlobalVariable, null, false));
 		scope.add(new Symbol("pi", new PointerType(CType.INT), Category.GlobalVariable, null,
 				false));
 		scope.add(new Symbol("pv", new PointerType(CType.VOID), Category.GlobalVariable, null,
 				false));
 		scope.add(new Symbol("pfv", new PointerType(new FunctionType(CType.VOID,
 				new ArrayList<CType>())), Category.GlobalVariable, null, false));
 	}
 
 	// Tests the precedence of operators.
 	//TODO test operator associativity
 	@Test
 	public void commaExpressionParsedAfterAssignmentExpression()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(CE (ASGN_EXPR = (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a = b, c"));
 	}
 
 	@Test
 	public void assignmentExpressionParsedAfterBinaryExpression1()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(ASGN_EXPR = (BIN_EXPR || (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a || b = c"));
 	}
 
 	@Test
 	public void binaryExpression1ParsedAfterBinaryExpression2()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR || (BIN_EXPR && (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a && b || c"));
 	}
 
 	@Test
 	public void binaryExpression2ParsedAfterBinaryExpression3()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR && (BIN_EXPR | (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a | b && c"));
 	}
 
 	@Test
 	public void binaryExpression3ParsedAfterBinaryExpression4()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR | (BIN_EXPR & (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a & b | c"));
 	}
 
 	@Test
 	public void binaryExpression4ParsedAfterBinaryExpression5()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR & (BIN_EXPR == (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a == b & c"));
 	}
 
 	@Test
 	public void binaryExpression5ParsedAfterBinaryExpression6()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR == (BIN_EXPR != (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a != b == c"));
 	}
 
 	@Test
 	public void binaryExpression6ParsedAfterBinaryExpression7()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR != (BIN_EXPR < (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a < b != c"));
 	}
 
 	@Test
 	public void binaryExpression7ParsedAfterBinaryExpression8()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR < (BIN_EXPR << (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a << b < c"));
 	}
 
 	@Test
 	public void binaryExpression8ParsedAfterBinaryExpression9()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR << (BIN_EXPR + (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a + b << c"));
 	}
 
 	@Test
 	public void binaryExpression9ParsedAfterBinaryExpression10()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR + (BIN_EXPR * (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a * b + c"));
 	}
 
 	@Test
 	public void binaryExpression10ParsedAfterBinaryExpression11()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR + (BIN_EXPR * (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("a * b + c"));
 	}
 
 	@Test
 	public void binaryExpression11ParsedAfterPrefixExpression()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(BIN_EXPR * (PRE_EXPR ++ (ID_EXPR b)) (ID_EXPR a))",
 				parse("++b * a"));
 	}
 
 	@Test
 	public void prefixExpressionParsedAfterPostfixExpression()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(PRE_EXPR ++ (POST_EXPR ++ (ID_EXPR a)))",
 				parse("++a++"));
 	}
 
 	@Test
 	public void postfixExpressionParsedAfterOtherExpressions()
 			throws IOException, SyntaxException
 	{
 		assertEquals("(POST_EXPR ++ (BIN_EXPR + (ID_EXPR a) (ID_EXPR b)))",
 				parse("(a + b)++"));
 		assertEquals("(BIN_EXPR * (BIN_EXPR + (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
 				parse("(a + b) * c"));
 	}
 
 	@Test
 	public void literalExpressionType() throws IOException, SyntaxException
 	{
 		testType("0", CType.INT);
 		testType("-1", CType.INT);
 		testType("2147483647", CType.INT);
 		testType("-2147483648", CType.INT);
 	}
 
 	@Test
 	public void identifierExpressionType() throws IOException, SyntaxException
 	{
 		testType("c", CType.CHAR);
 		testType("sc", CType.SCHAR);
 		testType("i", CType.INT);
 		testType("l", CType.LONG);
 		testType("ull", CType.ULLONG);
 		testType("fv", new FunctionType(CType.VOID, new ArrayList<CType>()));
 		testType("a", new ArrayType(CType.INT, 2));
 	}
 
 	@Test
 	public void postfixIncrementDecrementExpressionType() throws IOException, SyntaxException
 	{
 		testType("c++", CType.CHAR);
 		testType("sc--", CType.SCHAR);
 		testType("s++", CType.SHORT);
 		testType("i--", CType.INT);
 		testType("ull++", CType.ULLONG);
 		testType("pi--", new PointerType(CType.INT));
 	}
 
 	@Test
 	public void prefixIncrementAndDecrementExpressionType() throws IOException, SyntaxException
 	{
 		testType("++c", CType.CHAR);
 		testType("--sc", CType.SCHAR);
 		testType("++s", CType.SHORT);
 		testType("--i", CType.INT);
 		testType("++ull", CType.ULLONG);
 		testType("--pi", new PointerType(CType.INT));
 	}
 
 	@Test
 	public void assignmentExpressionType() throws IOException, SyntaxException
 	{
 		testType("c = ull", CType.CHAR);
 		testType("sc *= i", CType.SCHAR);
 		testType("uc /= sc", CType.UCHAR);
 		testType("s %= c", CType.SHORT);
 		testType("us &= ui", CType.USHORT);
 		testType("i |= ul", CType.INT);
 		testType("u ^= s", CType.UINT);
 		testType("l -= uc", CType.LONG);
 		testType("ll <<= ll", CType.LLONG);
 		testType("ull >>= us", CType.ULLONG);
 		testType("pi += c", new PointerType(CType.INT));
 	}
 
 	@Test
 	public void functionCallExpressionType() throws IOException, SyntaxException
 	{
 		testType("fv()", CType.VOID);
 		testType("fi()", CType.INT);
 		testType("pfv()", CType.VOID);
 	}
 
 	@Test
 	public void intrinsicCallExpressionType() throws IOException, SyntaxException
 	{
 		testType("in()", CType.INT);
 		testType("out(c)", CType.VOID);
 	}
 
 	@Test
 	public void logicalExpressionType() throws IOException, SyntaxException
 	{
 		testType("c && c", CType.INT);
 		testType("pfv || i", CType.INT);
 		testType("a && s", CType.INT);
 		testType("i || i", CType.INT);
 		testType("pi && pi", CType.INT);
 		testType("s || pv", CType.INT);
 		testType("l && l", CType.INT);
 		testType("ull || ll", CType.INT);
 	}
 
 	@Test
 	public void equalityExpressionType() throws IOException, SyntaxException
 	{
 		testType("c == c", CType.INT);
 		testType("pfv != pfv", CType.INT);
 		testType("pi == pv", CType.INT);
 		testType("i != c", CType.INT);
 		testType("i == i", CType.INT);
 		testType("pi != a", CType.INT);
 		testType("l == l", CType.INT);
 		testType("ull != ll", CType.INT);
 	}
 
 	@Test
 	public void relationalExpressionType() throws IOException, SyntaxException
 	{
 		testType("c < c", CType.INT);
 		testType("pi <= pi", CType.INT);
 		testType("pi > a", CType.INT);
 		testType("i >= i", CType.INT);
 		testType("s < i", CType.INT);
 		testType("l <= l", CType.INT);
 		testType("ull > ll", CType.INT);
 	}
 
 	@Test
 	public void additionExpressionType() throws IOException, SyntaxException
 	{
 		testType("c + c", CType.INT);
 		testType("c + i", CType.INT);
 		testType("s + s", CType.INT);
 		testType("i + i", CType.INT);
 		testType("c + u", CType.UINT);
 		testType("i + l", CType.LONG);
 		testType("l + u", CType.ULONG);
 		testType("ul + ll", CType.LLONG);
 		testType("ll + ull", CType.ULLONG);
 		testType("a + c", new PointerType(CType.INT));
 		testType("i + pi", new PointerType(CType.INT));
 	}
 
 	@Test
 	public void subtractExpressionType() throws IOException, SyntaxException
 	{
 		testType("c - c", CType.INT);
 		testType("c - i", CType.INT);
 		testType("s - s", CType.INT);
 		testType("i - i", CType.INT);
 		testType("c - u", CType.UINT);
 		testType("i - l", CType.LONG);
 		testType("l - u", CType.ULONG);
 		testType("ul - ll", CType.LLONG);
 		testType("ll - ull", CType.ULLONG);
 		testType("a - c", new PointerType(CType.INT));
 		testType("pi - i", new PointerType(CType.INT));
 	}
 
 	@Test
 	public void bitwiseExpressionType() throws IOException, SyntaxException
 	{
 		testType("c & c", CType.INT);
 		testType("c | i", CType.INT);
 		testType("s ^ s", CType.INT);
 		testType("i & i", CType.INT);
 		testType("c | u", CType.UINT);
 		testType("i ^ l", CType.LONG);
 		testType("l & u", CType.ULONG);
 		testType("ul | ll", CType.LLONG);
 		testType("ll ^ ull", CType.ULLONG);
 	}
 
 	@Test
 	public void shiftExpressionType() throws IOException, SyntaxException
 	{
 		testType("c << c", CType.INT);
 		testType("uc >> i", CType.UINT); // !
 		testType("s << s", CType.INT);
 		testType("s >> i", CType.INT);
 		testType("us << ull", CType.UINT); // !
 		testType("i >> i", CType.INT);
 		testType("u << c", CType.UINT);
 		testType("u >> l", CType.UINT);
 		testType("l << c", CType.LONG);
 		testType("ll >> ull", CType.LLONG);
 	}
 
 	@Test
	public void multiplicativeExpressionType() throws IOException, SyntaxException
	{
		testType("c * c", CType.INT);
		testType("c / i", CType.INT);
		testType("s % s", CType.INT);
		testType("i * i", CType.INT);
		testType("c / u", CType.UINT);
		testType("i % l", CType.LONG);
		testType("l * u", CType.ULONG);
		testType("ul / ll", CType.LLONG);
		testType("ll * ull", CType.ULLONG);
	}

	@Test
 	public void parenthesizedExpressionType() throws IOException, SyntaxException
 	{
 		testType("(c)", CType.CHAR);
 		testType("(sc)", CType.SCHAR);
 		testType("(i)", CType.INT);
 		testType("(l)", CType.LONG);
 		testType("(ull)", CType.ULLONG);
 		testType("(fv)", new FunctionType(CType.VOID, new ArrayList<CType>()));
 		testType("(a)", new ArrayType(CType.INT, 2));
 	}
 
 	@Test
 	public void commaExpressionType() throws IOException, SyntaxException
 	{
 		testType("1, c", CType.CHAR);
 		testType("l++, sc", CType.SCHAR);
 		testType("s=1, i", CType.INT);
 		testType("4, l", CType.LONG);
 		testType("fv(), 3, ull", CType.ULLONG);
 		testType("i/=2, fv", new PointerType(new FunctionType(CType.VOID, new ArrayList<CType>())));
 		testType("a[0]=1, a", new PointerType(CType.INT));
 	}
 
 	@Test
 	public void unaryPlusMinusExpressionType() throws IOException, SyntaxException
 	{
 		testType("-c", CType.INT);
 		testType("-uc", CType.UINT); // !
 		testType("+s", CType.INT);
 		testType("+us", CType.UINT); // !
 		testType("-i", CType.INT);
 		testType("-u", CType.UINT);
 		testType("+l", CType.LONG);
 		testType("-ull", CType.ULLONG);
 	}
 
 	@Test
 	public void addressOfExpressionType() throws IOException, SyntaxException
 	{
 		testType("&c", new PointerType(CType.CHAR));
 		testType("&ul", new PointerType(CType.ULONG));
 		testType("&pi", new PointerType(new PointerType(CType.INT)));
 		testType("&*pv", new PointerType(CType.VOID));
 		testType("&a", new PointerType(new ArrayType(CType.INT, 2)));
 		testType("&a2[0]", new PointerType(new ArrayType(CType.INT, 2)));
 		testType("&fv", new PointerType(new FunctionType(CType.VOID, new ArrayList<CType>())));
 	}
 
 	@Test
 	public void dereferenceExpressionType() throws IOException, SyntaxException
 	{
 		testType("*pi", CType.INT);
 		testType("*pv", CType.VOID);
 		testType("*a", CType.INT);
 		testType("*a2", new ArrayType(CType.INT, 2));
 		testType("*fi", new FunctionType(CType.INT, new ArrayList<CType>()));
 		testType("*pfv", new FunctionType(CType.VOID, new ArrayList<CType>()));
 	}
 
 	@Test
 	public void logicalNegationExpressionType() throws IOException, SyntaxException
 	{
 		testType("!a", CType.INT);
 		testType("!c", CType.INT);
 		testType("!i", CType.INT);
 		testType("!ull", CType.INT);
 		testType("!a2", CType.INT);
 		testType("!fi", CType.INT);
 		testType("!pi", CType.INT);
 	}
 
 	@Test
 	public void bitwiseNegationExpressionType() throws IOException, SyntaxException
 	{
 		testType("~c", CType.INT);
 		testType("~uc", CType.UINT); // !
 		testType("~s", CType.INT);
 		testType("~us", CType.UINT); // !
 		testType("~i", CType.INT);
 		testType("~u", CType.UINT);
 		testType("~l", CType.LONG);
 		testType("~ull", CType.ULLONG);
 	}
 
 	@Test
 	public void subscriptExpressionType() throws IOException, SyntaxException
 	{
 		testType("pi[i]", CType.INT);
 		testType("c[a2]", new ArrayType(CType.INT, 2));
 		testType("pi[ull]", CType.INT);
 	}
 }
