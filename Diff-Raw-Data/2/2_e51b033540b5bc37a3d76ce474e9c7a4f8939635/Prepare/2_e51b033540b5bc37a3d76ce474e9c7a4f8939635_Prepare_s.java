 package de.skuzzle.polly.parsing;
 
 import de.skuzzle.polly.parsing.declarations.Namespace;
 import de.skuzzle.polly.parsing.declarations.TypeDeclaration;
 import de.skuzzle.polly.parsing.declarations.VarDeclaration;
 import de.skuzzle.polly.parsing.tree.functions.BinomialFunction;
 import de.skuzzle.polly.parsing.tree.functions.ContainsFunction;
 import de.skuzzle.polly.parsing.tree.functions.DefaultMathFunction;
 import de.skuzzle.polly.parsing.tree.functions.Functions;
 import de.skuzzle.polly.parsing.tree.functions.AggregateListFunction;
 import de.skuzzle.polly.parsing.tree.functions.ListLengthFunction;
 import de.skuzzle.polly.parsing.tree.functions.MatrixToMatrixFunction;
 import de.skuzzle.polly.parsing.tree.functions.MatrixToMatrixModPFunction;
 import de.skuzzle.polly.parsing.tree.functions.RandomFunction;
 import de.skuzzle.polly.parsing.tree.functions.Functions.MatrixType;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.literals.NumberLiteral;
 import de.skuzzle.polly.parsing.tree.operators.BinaryOperators;
 import de.skuzzle.polly.parsing.tree.operators.TernaryDotDotOperator;
 import de.skuzzle.polly.parsing.tree.operators.UnaryOperators;
 import de.skuzzle.polly.parsing.types.Type;
 
 
 
 public final class Prepare {
 
     public static void operators(Namespace dest) {
         
         // these two operators must probably be added before the other list 
         // operators as they operate on special types of lists.
         dest.add(new BinaryOperators.MatrixArithmeticOperator(TokenType.ADD));
         dest.add(new BinaryOperators.MatrixArithmeticOperator(TokenType.MUL));
         
         dest.add(new BinaryOperators.IndexListOperator());
         dest.add(new BinaryOperators.ListArithmeticOperator(TokenType.ADD));
         dest.add(new BinaryOperators.ListArithmeticOperator(TokenType.SUB));
         dest.add(new BinaryOperators.ListArithmeticOperator(TokenType.WAVE));
         dest.add(new BinaryOperators.ListArithmeticOperator(TokenType.ADDWAVE));
         
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.ADD));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.SUB));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.MUL));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.DIV));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.INTDIV));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.INT_AND));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.INT_XOR));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.INT_OR));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.POWER));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.MOD));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.LEFT_SHIFT));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.RIGHT_SHIFT));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.URIGHT_SHIFT));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.CHOOSE));
         dest.add(new BinaryOperators.ArithmeticOperator(TokenType.RADIX));
         
         //dest.add(new BinaryOperators.ArithmeticDateOperator(TokenType.ADD));
         dest.add(new BinaryOperators.ArithmeticDateOperator(TokenType.SUB));
         dest.add(new BinaryOperators.ArithemticDateTimespanOperator(TokenType.SUB));
         dest.add(new BinaryOperators.ArithemticDateTimespanOperator(TokenType.ADD));
         
         dest.add(new BinaryOperators.ArithmeticTimespanOperator(TokenType.ADD));
         dest.add(new BinaryOperators.ArithmeticTimespanOperator(TokenType.SUB));
                 
         dest.add(new BinaryOperators.ConcatStringOperator());
         dest.add(new BinaryOperators.IndexStringOperator());
 
         dest.add(new BinaryOperators.BooleanOperator(TokenType.BOOLEAN_AND));
         dest.add(new BinaryOperators.BooleanOperator(TokenType.BOOLEAN_OR));
         dest.add(new BinaryOperators.BooleanOperator(TokenType.XOR));
         
         dest.add(new BinaryOperators.EqualityOperator(TokenType.EQ));
         dest.add(new BinaryOperators.EqualityOperator(TokenType.NEQ));
         
         dest.add(new BinaryOperators.RelationalDateOperator(TokenType.LT));
         dest.add(new BinaryOperators.RelationalDateOperator(TokenType.ELT));
         dest.add(new BinaryOperators.RelationalDateOperator(TokenType.GT));
         dest.add(new BinaryOperators.RelationalDateOperator(TokenType.EGT));
         
         dest.add(new BinaryOperators.RelationalNumberOperator(TokenType.LT));
         dest.add(new BinaryOperators.RelationalNumberOperator(TokenType.ELT));
         dest.add(new BinaryOperators.RelationalNumberOperator(TokenType.GT));
         dest.add(new BinaryOperators.RelationalNumberOperator(TokenType.EGT));
         
         dest.add(new BinaryOperators.RelationalStringOperator(TokenType.LT));
         dest.add(new BinaryOperators.RelationalStringOperator(TokenType.ELT));
         dest.add(new BinaryOperators.RelationalStringOperator(TokenType.GT));
         dest.add(new BinaryOperators.RelationalStringOperator(TokenType.EGT));
 
         dest.add(new BinaryOperators.RelationalListOperator(TokenType.LT));
         dest.add(new BinaryOperators.RelationalListOperator(TokenType.ELT));
         dest.add(new BinaryOperators.RelationalListOperator(TokenType.GT));
         dest.add(new BinaryOperators.RelationalListOperator(TokenType.EGT));
         
         
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.ADD));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.SUB));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.MUL));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.DIV));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.MOD));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.BOOLEAN_AND));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.BOOLEAN_OR));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.INT_AND));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.INT_OR));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.INT_XOR));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.URIGHT_SHIFT));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.RIGHT_SHIFT));
         dest.add(new BinaryOperators.ListScalarOperator(TokenType.LEFT_SHIFT));
         
         dest.add(new UnaryOperators.ArithmeticOperator(TokenType.SUB));
         dest.add(new UnaryOperators.BooleanOperator(TokenType.EXCLAMATION));
         dest.add(new UnaryOperators.StringOperator(TokenType.EXCLAMATION));
         dest.add(new UnaryOperators.ListOperator(TokenType.EXCLAMATION));
         dest.add(new UnaryOperators.RandomListIndexOperator());
         dest.add(new UnaryOperators.TimespanOperator());
         
         dest.add(new TernaryDotDotOperator());
     }
     
     
     
     public static void namespaces(Namespace dest) {
         try {
             // reserve type identifiers
             dest.reserve(new TypeDeclaration(Type.ANY));
             dest.reserve(new TypeDeclaration(Type.BOOLEAN));
             dest.reserve(new TypeDeclaration(Type.CHANNEL));
             dest.reserve(new TypeDeclaration(Type.COMMAND));
             dest.reserve(new TypeDeclaration(Type.DATE));
             dest.reserve(new TypeDeclaration(Type.LIST));
             dest.reserve(new TypeDeclaration(Type.NUMBER));
             dest.reserve(new TypeDeclaration(Type.STRING));
             dest.reserve(new TypeDeclaration(Type.TIMESPAN));
             dest.reserve(new TypeDeclaration(Type.USER));
             
             
             // add default functions/constants
             dest.add(new ListLengthFunction(), "util");
             dest.add(new ContainsFunction(), "util");
             
             
             dest.add(new VarDeclaration(new IdentifierLiteral("pi"), 
                     new NumberLiteral(Math.PI)), "math");
             dest.add(new VarDeclaration(new IdentifierLiteral("tau"), 
                 new NumberLiteral(2.0 * Math.PI)), "math");
             dest.add(new VarDeclaration(new IdentifierLiteral("e"), 
                     new NumberLiteral(Math.E)), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.COS), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.SIN), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.TAN), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.ABS), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.SQRT), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.CEIL), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.FLOOR), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.LOG), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.ROUND), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.ATAN), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.ASIN), "math");
             dest.add(new DefaultMathFunction(Functions.MathType.ACOS), "math");
             
             dest.add(new RandomFunction(), "math");
             dest.add(new BinomialFunction(), "math");
             
             dest.add(new AggregateListFunction(Functions.ListType.SUM), "math");
             dest.add(new AggregateListFunction(Functions.ListType.AVG), "math");
             
             dest.add(new MatrixToMatrixFunction("gaussElim", MatrixType.GAUSS), "matrix");
             dest.add(new MatrixToMatrixModPFunction("gaussElimModP", MatrixType.GAUSS), "matrix");
             dest.add(new MatrixToMatrixFunction("invert", MatrixType.GAUSS), "matrix");
             dest.add(new MatrixToMatrixModPFunction("invertModP", MatrixType.INVERT), "matrix");
             
             dest.add(new MatrixToMatrixFunction("rank", new Functions.MatrixToScalarFunction(MatrixType.RANK)), "matrix");
             dest.add(new MatrixToMatrixModPFunction("rankModP", new Functions.MatrixToScalarModPFunction(MatrixType.RANK)), "matrix");
             dest.add(new MatrixToMatrixFunction("det", new Functions.MatrixToScalarFunction(MatrixType.RANK)), "matrix");
            dest.add(new MatrixToMatrixModPFunction("detModP", new Functions.MatrixToScalarModPFunction(MatrixType.RANK)), "matrix");
         } catch (ParseException ignore) {
             // can not happen
         }
     }
     
     private Prepare() {}
 }
