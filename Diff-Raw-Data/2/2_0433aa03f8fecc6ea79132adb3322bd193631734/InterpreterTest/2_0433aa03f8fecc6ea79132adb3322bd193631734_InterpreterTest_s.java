 package test.interpreter;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import interpreter.*;
 import ast.*;
 
 public class InterpreterTest {
 	private Interpreter interpreter;
 	private static Field interpreterState;
 	private static Field interpreterTempValue;
 	private static Field stateStatement;
 	private static Field stateScope;
 	private static Constructor<State> stateConstructor;
 
 	@BeforeClass
 	public static void init() throws SecurityException, NoSuchFieldException, NoSuchMethodException {
 		interpreterState = Interpreter.class.getDeclaredField("currentState");
 		interpreterState.setAccessible(true);
 		interpreterTempValue = Interpreter.class.getDeclaredField("tempValue");
 		interpreterTempValue.setAccessible(true);
 		stateScope = State.class.getDeclaredField("currentScope");
 		stateScope.setAccessible(true);
 		stateConstructor = State.class.getDeclaredConstructor(new Class[] {});
 		stateConstructor.setAccessible(true);
 		stateStatement = State.class.getDeclaredField("currentStatement");
 		stateStatement.setAccessible(true);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		interpreter = new Interpreter();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	private Scope createEmptyScope() {
 		return new Scope(null, new StatementBlock(new Statement[] {}, null), new Function(null, null, null, null, null,
 				null, new Ensure[] {}));
 	}
 
 	private State createEmptyState() throws IllegalArgumentException, InstantiationException, IllegalAccessException,
 			InvocationTargetException {
 		State emptyState = stateConstructor.newInstance(new Object[] {});
 		stateScope.set(emptyState, createEmptyScope());
 		return emptyState;
 	}
 
 	@Test
 	public void testVisitConditionalTrueBranch() throws IllegalArgumentException, IllegalAccessException,
 			InstantiationException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		Statement trueStatement = new Assertion(null, null);
 		Statement falseStatement = new Assertion(null, null);
 
 		// check if true branch is entered
 		interpreter
 				.visit(new Conditional(null, new BooleanLiteral(null, "true"), new StatementBlock(
 						new Statement[] { trueStatement }, null), new StatementBlock(
 						new Statement[] { falseStatement }, null)));
 		assertSame(trueStatement, s.getCurrentStatement());
 	}
 
 	@Test
 	public void testVisitConditionalFalseBranch() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		Statement trueStatement = new Assertion(null, null);
 		Statement falseStatement = new Assertion(null, null);
 
 		// check if false branch is entered
 		interpreter
 				.visit(new Conditional(null, new BooleanLiteral(null, "false"), new StatementBlock(
 						new Statement[] { trueStatement }, null), new StatementBlock(
 						new Statement[] { falseStatement }, null)));
 		assertSame(falseStatement, s.getCurrentStatement());
 	}
 
 	@Test
 	public void testVisitLoopBody() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
 			InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		Statement loopBodyStatement = new Assertion(null, null);
 
 		// check if loop body is entered if condition is true
 		Loop infiniteLoop = new Loop(null, new BooleanLiteral(null, "true"), new StatementBlock(
 				new Statement[] { loopBodyStatement }, null), new Invariant[] {}, new Ensure[] {});
 		interpreter.visit(infiniteLoop);
 		assertSame(loopBodyStatement, s.getCurrentStatement());
 	}
 
 	@Test
 	public void testVisitLoopAfterBody() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		Statement loopBodyStatement = new Assertion(null, null);
 
 		// check if loop body is not entered if condition is false
 		Loop skipLoop = new Loop(null, new BooleanLiteral(null, "false"), new StatementBlock(
 				new Statement[] { loopBodyStatement }, null), new Invariant[] {}, new Ensure[] {});
 		interpreter.visit(skipLoop);
 		assertNotSame(loopBodyStatement, s.getCurrentStatement());
 	}
 
 	@Test
 	public void testVisitArrayAssignment() throws IllegalArgumentException, IllegalAccessException,
 			InstantiationException, InvocationTargetException {
 		State s = createEmptyState();
 		s.createArray("a", new ArrayType(new IntegerType()), new int[] { 3 });
 		interpreterState.set(interpreter, s);
 		interpreter.visit(new ArrayAssignment(null, new NumericLiteral(null, "7"), new Identifier("a"),
 				new Expression[] { new NumericLiteral(null, "1") }));
 		Value v = ((ArrayValue) ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("a")));
 		ArrayValue expected = new ArrayValue(new ArrayType(new IntegerType()), new int[] { 3 });
 		expected.setValue("7", Arrays.asList(new Integer[] { 1 }));
 		assertEquals(expected, v);
 	}
 
 	@Test
 	public void testVisitArithmeticExpression() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "2"),
 				new Addition()));
 		assertEquals(new IntegerValue("7"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "2"),
 				new Subtraction()));
 		assertEquals(new IntegerValue("3"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "2"),
 				new Multiplication()));
 		assertEquals(new IntegerValue("10"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "2"),
 				new Division()));
 		assertEquals(new IntegerValue("2"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "-2"),
 				new Division()));
 		assertEquals(new IntegerValue("-2"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "-5"), new NumericLiteral(null, "2"),
 				new Division()));
 		assertEquals(new IntegerValue("-3"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "0"),
 				new Division()));
 		assertEquals(new IntegerValue("0"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "2"),
 				new Modulo()));
 		assertEquals(new IntegerValue("1"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "0"),
 				new Modulo()));
 		assertEquals(new IntegerValue("5"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new ArithmeticExpression(null, new NumericLiteral(null, "5"), null, new UnaryMinus()));
 		assertEquals(new IntegerValue("-5"), interpreterTempValue.get(interpreter));
 	}
 
 	@Test
 	public void testVisitNumericLiteral() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 
 		interpreter.visit(new NumericLiteral(null, "0"));
 		assertEquals(new IntegerValue("0"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new NumericLiteral(null, "-10"));
 		assertEquals(new IntegerValue("-10"), interpreterTempValue.get(interpreter));
 	}
 
 	@Test
 	public void testVisitLogicalOperation() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 
 		interpreter.visit(new LogicalExpression(null, new BooleanLiteral(null, "false"), new BooleanLiteral(null,
 				"true"), new Disjunction()));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new BooleanLiteral(null, "false"), new BooleanLiteral(null,
 				"true"), new Conjunction()));
 		assertEquals(new BooleanValue("false"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new BooleanLiteral(null, "false"), null, new Negation()));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new BooleanLiteral(null, "false"), new BooleanLiteral(null,
 				"false"), new Equal()));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new BooleanLiteral(null, "false"), new BooleanLiteral(null,
 				"false"), new NotEqual()));
 		assertEquals(new BooleanValue("false"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "5"),
 				new Equal()));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "5"),
 				new NotEqual()));
 		assertEquals(new BooleanValue("false"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "5"),
 				new Less()));
 		assertEquals(new BooleanValue("false"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new NumericLiteral(null, "3"), new NumericLiteral(null, "5"),
 				new LessEqual()));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new NumericLiteral(null, "3"), new NumericLiteral(null, "5"),
 				new Greater()));
 		assertEquals(new BooleanValue("false"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new LogicalExpression(null, new NumericLiteral(null, "5"), new NumericLiteral(null, "5"),
 				new GreaterEqual()));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 	}
 
 	@Test
 	public void testVisitBooleanLiteral() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 
 		interpreter.visit(new BooleanLiteral(null, "true"));
 		assertEquals(new BooleanValue("true"), interpreterTempValue.get(interpreter));
 
 		interpreter.visit(new BooleanLiteral(null, "false"));
 		assertEquals(new BooleanValue("false"), interpreterTempValue.get(interpreter));
 	}
 
 	@Test
 	public void testVisitVariableRead() throws IllegalArgumentException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		s.createVar("x", "4", new IntegerType());
 		interpreterState.set(interpreter, s);
 
 		interpreter.visit(new VariableRead(null, new Identifier("x")));
 		assertEquals(new IntegerValue("4"), interpreterTempValue.get(interpreter));
 	}
 
 	@Test
 	public void testVisitArrayRead() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		s.createArray("a", new ArrayType(new IntegerType()), new int[] { 4 });
 		s.setArray("a", "-2", Arrays.asList(new Integer[]{ 0 }));
 		s.setArray("a", "3", Arrays.asList(new Integer[]{ 3 }));
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new ArrayRead(null, new Identifier("a"), new Expression[]{new NumericLiteral(null, "3")}));
 		assertEquals(new IntegerValue("3"), interpreterTempValue.get(interpreter));
 		
 		interpreter.visit(new ArrayRead(null, new Identifier("a"), new Expression[]{new NumericLiteral(null, "5")}));
		assertEquals(new IntegerValue("0"), interpreterTempValue.get(interpreter));
 	}
 
 	@Test
 	public void testVisitAssignment() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
 			InvocationTargetException {
 		State s = createEmptyState();
 		s.createVar("x", "0", new IntegerType());
 		interpreterState.set(interpreter, s);
 		interpreter.visit(new Assignment(null, new NumericLiteral(null, "7"), new Identifier("x")));
 		Value v = ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("x"));
 		assertEquals(new IntegerValue("7"), v);
 	}
 	
 	@Test
 	public void testVisitAssertionNoFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Assertion(null, new BooleanLiteral(null, "true")));
 	}
 	
 	@Test(expected=AssertionFailureException.class)
 	public void testVisitAssertionFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Assertion(null, new BooleanLiteral(null, "false")));
 	}
 
 	@Test
 	public void testVisitAssumptionNoFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Assumption(null, new BooleanLiteral(null, "true")));
 	}
 	
 	@Test(expected=AssertionFailureException.class)
 	public void testVisitAssumptionFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Assumption(null, new BooleanLiteral(null, "false")));
 	}
 	
 	@Test
 	public void testVisitAxiom() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Axiom(null, new BooleanLiteral(null, "true")));
 		interpreter.visit(new Axiom(null, new BooleanLiteral(null, "false")));
 	}
 	
 	@Test
 	public void testVisitEnsureNoFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Ensure(null, new BooleanLiteral(null, "true")));
 	}
 	
 	@Test(expected=AssertionFailureException.class)
 	public void testVisitEnsureFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Ensure(null, new BooleanLiteral(null, "false")));
 	}
 	
 	@Test
 	public void testVisitInvariantNoFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Invariant(null, new BooleanLiteral(null, "true")));
 	}
 	
 	@Test(expected=AssertionFailureException.class)
 	public void testVisitInvariantFail() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new Invariant(null, new BooleanLiteral(null, "false")));
 	}
 	
 	@Test
 	public void testVisitVariableDeclaration() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new VariableDeclaration(null, "x", new NumericLiteral(null, "3"), new IntegerType()));
 		Value expected = ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("x"));
 		assertEquals(new IntegerValue("3"), expected);
 		
 
 		interpreter.visit(new VariableDeclaration(null, "y", null, new IntegerType()));
 		expected = ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("y"));
 		assertEquals(new IntegerValue("0"), expected);
 
 		interpreter.visit(new VariableDeclaration(null, "r", new BooleanLiteral(null, "true"), new BooleanType()));
 		expected = ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("r"));
 		assertEquals(new BooleanValue("true"), expected);
 		
 		interpreter.visit(new VariableDeclaration(null, "s", null, new BooleanType()));
 		expected = ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("s"));
 		assertEquals(new BooleanValue("false"), expected);
 	}
 	
 	@Test
 	public void testVisitArrayDeclaration() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		State s = createEmptyState();
 		interpreterState.set(interpreter, s);
 		
 		interpreter.visit(new ArrayDeclaration(null, "a", new ArrayType(new IntegerType()), new Expression[]{new NumericLiteral(null, "4")}));
 		Value aValue = ((ArrayValue) ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("a")));
 		ArrayValue aExpected = new ArrayValue(new ArrayType(new IntegerType()), new int[] { 4 });
 		assertEquals(aExpected, aValue);
 		
 		interpreter.visit(new ArrayDeclaration(null, "b", new ArrayType(new BooleanType()), new Expression[]{new NumericLiteral(null, "4")}));
 		Value bValue = ((ArrayValue) ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("b")));
 		ArrayValue bExpected = new ArrayValue(new ArrayType(new BooleanType()), new int[] { 4 });
 		assertEquals(bExpected, bValue);
 		assertFalse(bValue.equals(aExpected));
 		
 		interpreter.visit(new ArrayDeclaration(null, "c", new ArrayType(new ArrayType(new IntegerType())), new Expression[]{new NumericLiteral(null, "4"), new NumericLiteral(null, "7")}));
 		Value cValue = ((ArrayValue) ((State) interpreterState.get(interpreter)).getVariables().get(new Identifier("c")));
 		ArrayValue cExpected = new ArrayValue(new ArrayType(new ArrayType(new IntegerType())), new int[] { 4, 7 });
 		assertEquals(cExpected, cValue);
 		assertFalse(cValue.equals(aExpected));
 	}
 }
