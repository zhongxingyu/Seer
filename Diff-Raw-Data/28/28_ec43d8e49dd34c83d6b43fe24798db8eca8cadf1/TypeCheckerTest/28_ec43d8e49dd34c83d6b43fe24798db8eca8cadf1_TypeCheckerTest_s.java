 package test.parser;
 
 import ast.*;
 import org.antlr.runtime.RecognitionException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import parser.FunctionCallNotAllowedException;
 import parser.IllegalTypeException;
 import parser.ParserInterface;
 import parser.TypeChecker;
 
 import static org.junit.Assert.*;
 
 public class TypeCheckerTest {
     private ParserInterface parserInterface;
     private TypeChecker typeChecker;
     private Program program;
     private final Position p = new Position();
 
     @Before
     public void setUp() {
         parserInterface = new ParserInterface();
         typeChecker = new TypeChecker();
     }
 
     @After
     public void tearDown() {
         parserInterface = null;
         typeChecker = null;
     }
 
     @Test
     public void testCorrectAssume() {
         program = new Program(p, new Function[0], new Function(
                 p, null, "main", new FunctionParameter[0], new StatementBlock(
                     new Statement[0], p), new Assumption[]{
                         new Assumption(p, new BooleanLiteral(p, "true"))},
                     new Ensure[0]),
                 new Axiom[0]);
         boolean success = true;
         try {
             typeChecker.checkTypes(program);
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectNegation() {
         program = new Program(p, new Function[0], new Function(
                 p, null, "main", new FunctionParameter[0], new StatementBlock(
                     new Statement[]{new VariableDeclaration(
                             p, "i", new LogicalExpression(
                             p, new BooleanLiteral(p, "true"), null, new Negation()
 
                     ), new BooleanType()
                     )}, p), new Assumption[0],
                     new Ensure[0]),
                 new Axiom[0]);
         boolean success = true;
         try {
             typeChecker.checkTypes(program);
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectFunctions() {
         boolean success = true;
         try {
             parserInterface.parseProgram("int f(){return 0;}" +
                     "int g(){return 0;}" +
                     "int h(){return 0;}" +
                     "int i(){return 0;}" +
                     "main(){}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectAssertFuncCall() {
         boolean success = true;
         try {
             parserInterface.parseProgram("bool f(){return true;}" +
                     "main(){assert f();}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectFunctionEnsures() {
         boolean success = true;
         try {
             parserInterface.parseProgram("int f(){return 0;}" +
                     "ensure{true;false;}" +
                     "main(){}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectEnsure() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main(){} ensure false;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectInvariant() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main(){" +
                     "while(true)invariant true;{}}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectAxiom() {
         boolean success = true;
         try {
             parserInterface.parseProgram("axiom false; main(){}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectAssert() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main(){assert true;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectLength() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {int[] s = array[2];}" +
                     "ensure length(s)!=1;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectFuncCall() {
         boolean success = true;
         try {
             parserInterface.parseProgram("int m(int a, bool c)" +
                     "{return 2;}" +
                     "main() {int w = m(2,true);}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectArrayFuncCall() {
         boolean success = true;
         try {
             parserInterface.parseProgram("int m(int[][] d)" +
                     "{return 2;}" +
                     "main() {int[][][] s = array[2][1][2]; int w = m(s[0]); }");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectLengthSubArray() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {" +
                     "int[][][] s = array[2][1][2];} ensure length(s[1])!=1;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectQuantifier() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {int a; int b = 7;" +
                     "} ensure exists x(a,b) x != 2;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectIf() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main(){if (true) {}}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectWhile() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main(){bool c; while (c) {}}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectArrayRead() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main(){" +
                     "int[][] s = array[2][1]; int a = 1;" +
                     "a = s[s[1][1]][a];" +
                     "} ensure length(s)!=1;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectArrayAssign() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {int a;" +
                     "int[][][] s = array[2][1][2];" +
                     "s[1][1][a] = 2;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectEqualBasicTypes() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {" +
                     "bool c; int a; int b; c = c == (a==b);}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectEqualArrays() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {" +
                     "int[] a = array[0]; int[] b = array[0]; bool c = a==b;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectNotEqualBasicTypes() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {" +
                     "bool c; int a; int b; c = c != (a!=b);}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectNotEqualArrays() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {" +
                     "int[] a = array[0]; int[] b = array[0]; bool c = a!=b;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectLess() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {bool a = 2<2;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectOr() {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {bool c = true | false;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectExistsDivZero() throws RecognitionException {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {} ensure exists x(1,2) 1/x==0;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectExistsUnbounded() throws RecognitionException {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {} ensure exists x() true;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectForAllUnbounded() throws RecognitionException {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {} ensure forall x() true;");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test
     public void testCorrectScoping() throws RecognitionException {
         boolean success = true;
         try {
             parserInterface.parseProgram("main() {bool i;if(i){" +
                     "i=true;int i;i=9;}i=false;}");
         } catch (RecognitionException e) {
             success = false;
         } catch (IllegalTypeException e) {
             success = false;
         }
         assertTrue(success);
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWhileConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() {int c; while (c){}}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testIfConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() {int c; if (c){}}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testAssertConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() {assert 1;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testInvariantConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() {while(true)invariant 1;{}}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testAssumeConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() assume 1;{}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testAxiomConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("axiom 1; main() {}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testEnsureConditionNotBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() {}ensure 1;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testBooleanOnIntegerOperator() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = 1*true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testBooleanOnIntegerOperator2() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = true*1;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testIntegerOnBooleanOperator() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = 1|true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testIntegerOnBooleanOperator2() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = true|1;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongMinus() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = -true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongNegation() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = !1;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testAssignBooleanToInteger() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testAssignBooleanToInteger2() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = 1; i = true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testAssignIntegerToBoolean() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i = 42;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testEqualsDifferentTypes() throws RecognitionException {
         parserInterface.parseProgram("main() {int[][] c = array[3][2];" +
                 "int[] d = array[2]; bool i = c==d;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayNotCorrectlyDeclared1() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayNotCorrectlyDeclared2() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[true];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayNotCorrectlyDeclared3() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[2][3];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayNotCorrectlyDeclared4() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[][] i = array[2];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testVariableDeclaredAsArray() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i = array[6];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayNotFullyIndexed() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[][] i = array[6][1];i[1]=true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testIllegalNumberOfParameters() throws RecognitionException {
         parserInterface.parseProgram("bool f(){return true;}" +
                                     "main() {bool i = f(true);}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testVariableAlreadyDeclared() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i; bool i;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayAlreadyDeclared() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i; bool[] i = array[7];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayWrongBaseType() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[7];i[1]=1;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testLengthNoArray() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i; int g = length(i);}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testLengthNoRead() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i; int g = length(3);}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testLengthWrongParameterNumber() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i; int g = length(i, i);}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongArrayIndexType1() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[true];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongArrayIndexType2() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[7];" +
                                      "i[true] = true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongArrayIndexType3() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[7];" +
                                      "bool j = i[i];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongArrayIndexNumber1() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[7];" +
                                      "assert i[5][6];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongArrayIndexNumber2() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[7];" +
                                      "i[5][6] = true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongArrayIndexNumber3() throws RecognitionException {
         parserInterface.parseProgram("main() {bool[] i = array[5];" +
                                      "i = true;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongExistsRange1() throws RecognitionException {
         parserInterface.parseProgram("main() {}ensure exists x(true,2) true;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongExistsRange2() throws RecognitionException {
         parserInterface.parseProgram("main() {}ensure exists x(2,true) true;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongExistsExpr() throws RecognitionException {
         parserInterface.parseProgram("main() {} ensure exists x(2, 3) 1;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongForAllRange1() throws RecognitionException {
         parserInterface.parseProgram("main() {}ensure forall x(true,2) true;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongForAllRange2() throws RecognitionException {
         parserInterface.parseProgram("main() {}ensure forall x(2,true) true;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongForAllExpr() throws RecognitionException {
         parserInterface.parseProgram("main() {} ensure forall x(2, 3) 1;");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testNoSuchFunction() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = f(a);}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testFunctionOverloaded() throws RecognitionException {
         parserInterface.parseProgram("int f(){return 0;}" +
                     "int f(){return 0;}" +
                     "main(){}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongParamType() throws RecognitionException {
         parserInterface.parseProgram("int f(bool a){return 0;}main() {int i = f(1);}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayReturn() throws RecognitionException {
         parserInterface.parseProgram("int[] f(){return 0;}main() {}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongReturn() throws RecognitionException {
         parserInterface.parseProgram("int f(){return true;}main() {}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testVarNotDeclared() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = w;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testArrayNotDeclared() throws RecognitionException {
         parserInterface.parseProgram("main() {int i = w[1];}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongScoping1() throws RecognitionException {
         parserInterface.parseProgram("main() {if(true){}else{int i;} i=2;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongScoping2() throws RecognitionException {
         parserInterface.parseProgram("main() {bool i;if(i){int i;i=true;}}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testWrongScoping3() throws RecognitionException {
         parserInterface.parseProgram("main() {" +
                 "if(true){}else{int[] i=array[1];} i[1]=2;}");
     }
 
     @Test (expected = IllegalTypeException.class)
     public void testVarReadInAxiom() throws RecognitionException {
         parserInterface.parseProgram("axiom i;" +
                 "main() {}");
     }
 
     @Test (expected = FunctionCallNotAllowedException.class)
     public void testFunctionCallNotAllowed() throws RecognitionException {
         parserInterface.parseProgram("int g(int x){return 0;}" +
                 "main() {} ensure g(1);");
     }
 }
