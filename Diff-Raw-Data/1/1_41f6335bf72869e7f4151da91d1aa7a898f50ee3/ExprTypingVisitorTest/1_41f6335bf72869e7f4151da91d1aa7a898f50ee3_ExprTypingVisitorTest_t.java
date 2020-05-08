 package cd.semantic;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import cd.exceptions.SemanticFailure;
 import cd.ir.ast.BinaryOp;
 import cd.ir.ast.BinaryOp.BOp;
 import cd.ir.ast.BooleanConst;
 import cd.ir.ast.BuiltInRead;
 import cd.ir.ast.BuiltInReadFloat;
 import cd.ir.ast.Cast;
 import cd.ir.ast.Expr;
 import cd.ir.ast.Field;
 import cd.ir.ast.FloatConst;
 import cd.ir.ast.Index;
 import cd.ir.ast.IntConst;
 import cd.ir.ast.MethodCallExpr;
 import cd.ir.ast.NewObject;
 import cd.ir.ast.NullConst;
 import cd.ir.ast.ThisRef;
 import cd.ir.ast.UnaryOp;
 import cd.ir.ast.UnaryOp.UOp;
 import cd.ir.ast.Var;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 
 /**
  * Tests {@link ExprTypingVisitor}.
  * 
  */
 public class ExprTypingVisitorTest {
 
 	private TypeSymbolTable types;
 	private ClassSymbol xClass;
 	private ClassSymbol zClass;
 	private MethodSymbol xClassMethod;
 
 	private ExprTypingVisitor visitor;
 	private SymbolTable<VariableSymbol> scope;
 	private SymbolTable<VariableSymbol> methodScope;
 
 	private VariableSymbol intVariable;
 	private VariableSymbol floatVariable;
 	private VariableSymbol booleanVariable;
 	private VariableSymbol topVariable;
 	private VariableSymbol bottomVariable;
 	private VariableSymbol objectVariable;
 	private VariableSymbol xVariable;
 	private VariableSymbol zVariable;
 	private VariableSymbol arrayVariable;
 	private VariableSymbol xClassField;
 
 	@Before
 	public void setUp() {
 		types = new TypeSymbolTable();
 
 		xClass = new ClassSymbol("X", types.getObjectType());
 		zClass = new ClassSymbol("Z", types.getObjectType());
 
 		xClassMethod = new MethodSymbol("m", xClass);
 		methodScope = xClassMethod.getScope();
 
 		types.add(xClass);
 		types.add(zClass);
 
 		visitor = new ExprTypingVisitor(types);
 		scope = new SymbolTable<>();
 
 		intVariable = addVariableSymbol("i", types.getIntType());
 		floatVariable = addVariableSymbol("f", types.getFloatType());
 		booleanVariable = addVariableSymbol("b", types.getBooleanType());
 		topVariable = addVariableSymbol("top", types.getTopType());
 		bottomVariable = addVariableSymbol("bottom", types.getBottomType());
 		objectVariable = addVariableSymbol("o", types.getObjectType());
 		xVariable = addVariableSymbol("x", xClass);
 		zVariable = addVariableSymbol("z", zClass);
 		arrayVariable = addVariableSymbol("array",
 				types.getArrayTypeSymbol(types.getObjectType()));
 
 		xClassField = new VariableSymbol("field", types.getIntType());
 		xClass.addField(xClassField);
 		xClass.addMethod(xClassMethod);
 		xClassMethod.addParameter(xVariable);
 		xClassMethod.returnType = types.getIntType();
 	}
 
 	private Var makeIntVar() {
 		return Var.withSym(intVariable);
 	}
 
 	private Var makeFloatVar() {
 		return Var.withSym(floatVariable);
 	}
 
 	private Var makeBoolVar() {
 		return Var.withSym(booleanVariable);
 	}
 
 	private Var makeTopVar() {
 		return Var.withSym(topVariable);
 	}
 
 	private Var makeBottomVar() {
 		return Var.withSym(bottomVariable);
 	}
 
 	private Var makeObjectVar() {
 		return Var.withSym(objectVariable);
 	}
 
 	private Var makeXVar() {
 		return Var.withSym(xVariable);
 	}
 
 	private Var makeZVar() {
 		return Var.withSym(zVariable);
 	}
 
 	private Var makeArrayVar() {
 		return Var.withSym(arrayVariable);
 	}
 
 	private void assertType(TypeSymbol expectedType, Expr expr) {
 		TypeSymbol actualType = type(expr);
 		Assert.assertEquals(expectedType, actualType);
 		Assert.assertEquals(expectedType, expr.getType());
 	}
 
 	private void assertTypeInScope(TypeSymbol expectedType, Expr expr,
 			SymbolTable<VariableSymbol> currentScope) {
 		TypeSymbol actualType = typeWithScope(expr, currentScope);
 		Assert.assertEquals(expectedType, actualType);
 		Assert.assertEquals(expectedType, expr.getType());
 	}
 
 	private void assertIntType(Expr expr) {
 		assertType(types.getIntType(), expr);
 	}
 
 	private void assertFloatType(Expr expr) {
 		assertType(types.getFloatType(), expr);
 	}
 
 	private void assertBooleanType(Expr expr) {
 		assertType(types.getBooleanType(), expr);
 	}
 
 	private void assertBottomType(Expr expr) {
 		assertType(types.getBottomType(), expr);
 	}
 
 	private void assertObjectType(Expr expr) {
 		assertType(types.getObjectType(), expr);
 	}
 
 	@Test
 	public void testBinaryOp() {
 		for (BOp op : new BOp[] { BOp.B_TIMES, BOp.B_DIV, BOp.B_MOD,
 				BOp.B_PLUS, BOp.B_MINUS }) {
 			assertIntType(new BinaryOp(makeIntVar(), op, makeIntVar()));
 
 			if (!op.equals(BOp.B_MOD)) {
 				assertFloatType(new BinaryOp(makeFloatVar(), op, makeFloatVar()));
 			}
 
 			assertIntType(new BinaryOp(makeIntVar(), op, makeBottomVar()));
 			assertBottomType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
 		}
 
 		for (BOp op : new BOp[] { BOp.B_AND, BOp.B_OR }) {
 			assertBooleanType(new BinaryOp(makeBoolVar(), op, makeBoolVar()));
 			assertBooleanType(new BinaryOp(makeBoolVar(), op, makeBottomVar()));
 			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
 		}
 
 		for (BOp op : new BOp[] { BOp.B_EQUAL, BOp.B_NOT_EQUAL }) {
 			assertBooleanType(new BinaryOp(makeIntVar(), op, makeIntVar()));
 			assertBooleanType(new BinaryOp(makeBoolVar(), op, makeBoolVar()));
 			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
 			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeIntVar()));
 			assertBooleanType(new BinaryOp(makeObjectVar(), op, makeObjectVar()));
 			assertBooleanType(new BinaryOp(makeXVar(), op, makeXVar()));
 			assertBooleanType(new BinaryOp(makeXVar(), op, makeObjectVar()));
 		}
 
 		for (BOp op : new BOp[] { BOp.B_LESS_THAN, BOp.B_LESS_OR_EQUAL,
 				BOp.B_GREATER_THAN, BOp.B_GREATER_OR_EQUAL }) {
 			assertBooleanType(new BinaryOp(makeIntVar(), op, makeIntVar()));
 			assertBooleanType(new BinaryOp(makeIntVar(), op, makeBottomVar()));
 			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
 		}
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpModFloat() {
 		type(new BinaryOp(makeFloatVar(), BOp.B_MOD, makeFloatVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpNumericMixedTypes() {
 		type(new BinaryOp(makeIntVar(), BOp.B_PLUS, makeFloatVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpNumericWithBooleans() {
 		type(new BinaryOp(makeBoolVar(), BOp.B_PLUS, makeBoolVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpNumericWithTop() {
 		type(new BinaryOp(makeTopVar(), BOp.B_PLUS, makeIntVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpLogical() {
 		type(new BinaryOp(makeIntVar(), BOp.B_AND, makeBoolVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpEqualityUnrelatedPrimitiveTypes() {
 		type(new BinaryOp(makeIntVar(), BOp.B_EQUAL, makeFloatVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpEqualityUnrelatedReferenceTypes() {
 		type(new BinaryOp(makeXVar(), BOp.B_EQUAL, makeZVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectBinaryOpInequalityUnrelatedTypes() {
 		type(new BinaryOp(makeIntVar(), BOp.B_LESS_OR_EQUAL, makeFloatVar()));
 	}
 
 	@Test
 	public void testBooleanConst() {
 		assertBooleanType(new BooleanConst(true));
 	}
 
 	@Test
 	public void testBuiltInRead() {
 		assertIntType(new BuiltInRead());
 	}
 
 	@Test
 	public void testBuiltInReadFloat() {
 		assertFloatType(new BuiltInReadFloat());
 	}
 
 	@Test
 	public void testCast() {
 		assertObjectType(new Cast(makeObjectVar(), types.getObjectType().name));
 		assertType(xClass, new Cast(makeObjectVar(), xClass.name));
 		assertObjectType(new Cast(makeXVar(), types.getObjectType().name));
 		assertObjectType(new Cast(makeBottomVar(), types.getObjectType().name));
 		assertIntType(new Cast(makeIntVar(), types.getIntType().name));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectReferenceCast() {
 		type(new Cast(makeZVar(), xClass.name));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectPrimitiveCast() {
 		type(new Cast(makeIntVar(), types.getFloatType().name));
 	}
 
 	@Test
 	public void testField() {
 		assertIntType(new Field(makeXVar(), xClassField.name));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testFieldUnknownName() {
 		type(new Field(makeXVar(), "unknown"));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testFieldPrimitiveArgType() {
 		type(new Field(makeIntVar(), "unknown"));
 	}
 
 	@Test
 	public void testIndex() {
 		for (ArrayTypeSymbol arrayType : types.getArrayTypeSymbols()) {
 			TypeSymbol elementType = arrayType.elementType;
 			arrayVariable.setType(arrayType);
 
 			assertType(elementType, new Index(makeArrayVar(), makeIntVar()));
 			assertType(elementType, new Index(makeArrayVar(), makeBottomVar()));
 		}
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectIndexArg() {
 		type(new Index(makeArrayVar(), makeFloatVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectIndexType() {
 		type(new Index(makeFloatVar(), makeFloatVar()));
 	}
 
 	@Test
 	public void testThisRef() {
 		assertTypeInScope(xClass, new ThisRef(), methodScope);
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testInvalidMethodReceiver() {
 		type(new MethodCallExpr(makeZVar(), xClassMethod.name,
 				Collections.<Expr> emptyList()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testInvalidMethodArgumentsNumber() {
 		type(new MethodCallExpr(makeXVar(), xClassMethod.name, Arrays.asList(
 				makeXVar(), makeZVar())));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testInvalidMethodArgumentType() {
 		type(new MethodCallExpr(makeXVar(), xClassMethod.name,
 				Arrays.asList(makeZVar())));
 	}
 
 	public void testMethodCall() {
 		assertIntType(new MethodCallExpr(makeXVar(), xClassMethod.name,
 				Arrays.asList(makeXVar())));
 	}
 
 	@Test
 	public void testIndexBottomType() {
 		assertBottomType(new Index(makeBottomVar(), makeIntVar()));
 	}
 
 	@Test
 	public void testIntConst() {
 		assertIntType(new IntConst(42));
 	}
 
 	@Test
 	public void testFloatConst() {
 		assertFloatType(new FloatConst(42.0f));
 	}
 
 	@Test
 	public void testNewObject() {
 		assertObjectType(new NewObject(types.getObjectType().name));
 		assertType(xClass, new NewObject(xClass.name));
 	}
 
 	@Test
 	public void testNullConst() {
 		assertType(types.getNullType(), new NullConst());
 	}
 
 	@Test
 	public void testUnaryOp() {
 		assertBooleanType(new UnaryOp(UOp.U_BOOL_NOT, makeBoolVar()));
 		assertIntType(new UnaryOp(UOp.U_PLUS, makeIntVar()));
 		assertFloatType(new UnaryOp(UOp.U_MINUS, makeFloatVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectUnaryOpBoolNot() {
 		type(new UnaryOp(UOp.U_BOOL_NOT, makeFloatVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectUnaryOpBoolNotTop() {
 		type(new UnaryOp(UOp.U_BOOL_NOT, makeTopVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectUnaryOpPlus() {
 		type(new UnaryOp(UOp.U_PLUS, makeBoolVar()));
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testIncorrectUnaryOpMinus() {
 		type(new UnaryOp(UOp.U_MINUS, makeTopVar()));
 	}
 
 	@Test
 	public void testUnaryOpBottom() {
 		assertBooleanType(new UnaryOp(UOp.U_BOOL_NOT, makeBottomVar()));
 		assertBottomType(new UnaryOp(UOp.U_MINUS, makeBottomVar()));
 		assertBottomType(new UnaryOp(UOp.U_PLUS, makeBottomVar()));
 	}
 
 	@Test
 	public void testVar() {
 		assertIntType(makeIntVar());
 	}
 
 	@Test(expected = SemanticFailure.class)
 	public void testUnknownVar() {
 		type(new Var("unknown"));
 	}
 
 	private VariableSymbol addVariableSymbol(String name, TypeSymbol type) {
 		VariableSymbol symbol = new VariableSymbol(name, type);
 		scope.add(symbol);
 		return symbol;
 	}
 
 	private TypeSymbol type(Expr expr) {
 		return visitor.type(expr, scope);
 	}
 
 	private TypeSymbol typeWithScope(Expr expr,
 			SymbolTable<VariableSymbol> currentScope) {
 		return visitor.type(expr, currentScope);
 	}
 
 }
