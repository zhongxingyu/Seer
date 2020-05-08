 package IntermediateCodeGeneration.AST;
 
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 import IntermediateCodeGeneration.SymbolTable.Type.*;
 import IntermediateCodeGeneration.Token;
 
 /**
  * Representacion de una expresion binaria
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class BinaryExpressionNode extends ExpressionNode {
 
     protected Token operator;
     protected ExpressionNode left;
     protected ExpressionNode right;
 
     public BinaryExpressionNode(SymbolTable symbolTable, Token op, ExpressionNode exp1, ExpressionNode exp2, Token token) {
         super(symbolTable, token);
         operator = op;
         left = exp1;
         right = exp2;
     }
 
     @Override
     public void checkNode() throws SemanticException {
         left.checkNode();
         right.checkNode();
 
         String operatorLexeme = operator.getLexeme();
         if (operatorLexeme.equals("+") || operatorLexeme.equals("-") || operatorLexeme.equals("*") || operatorLexeme.equals("/") || operatorLexeme.equals("%")) {
             if (!left.getExpressionType().getTypeName().equals("int")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El operador binario '" + operatorLexeme + "' no puede aplicarse a una subexpresion de tipo " + left.getExpressionType().getTypeName() + ".\nSe esperaba una subexpresion de tipo entero.");
             } else if (!right.getExpressionType().getTypeName().equals("int")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El operador binario '" + operatorLexeme + "' no puede aplicarse a una subexpresion de tipo " + right.getExpressionType().getTypeName() + ".\nSe esperaba una subexpresion de tipo entero.");
             } else {
                 Type type = new IntegerType();
                 this.setExpressionType(type);
             }
         } else if (operatorLexeme.equals("&&") || operatorLexeme.equals("||")) {
             if (!left.getExpressionType().getTypeName().equals("boolean")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El operador binario '" + operatorLexeme + "' no puede aplicarse a una subexpresion de tipo " + left.getExpressionType().getTypeName() + ".\nSe esperaba una subexpresion de tipo boolean.");
             } else if (!right.getExpressionType().getTypeName().equals("boolean")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El operador binario '" + operatorLexeme + "' no puede aplicarse a una subexpresion de tipo " + right.getExpressionType().getTypeName() + ".\nSe esperaba una subexpresion de tipo boolean.");
             } else {
                 Type type = new BooleanType();
                 this.setExpressionType(type);
             }
         } else if (operatorLexeme.equals("==") || operatorLexeme.equals("!=")) {
             if (!left.getExpressionType().checkConformity(right.getExpressionType()) && !right.getExpressionType().checkConformity(left.getExpressionType())) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: Los tipos de las subexpresiones no son conformantes.\nLa subexpresion de la izquierda es de tipo " + left.getExpressionType().getTypeName() + " y la subexpresion de la derecha es de tipo " + right.getExpressionType().getTypeName() + ".");
             } else {
                 Type type = new BooleanType();
                 this.setExpressionType(type);
             }
         } else if (operatorLexeme.equals(">") || operatorLexeme.equals(">=") || operatorLexeme.equals("<=") || operatorLexeme.equals("<")) {
             if (!left.getExpressionType().getTypeName().equals("int")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El operador binario '" + operatorLexeme + "' no puede aplicarse a la subexpresion de tipo " + left.getExpressionType().getTypeName() + ".\nSe esperaba una subexpresion de tipo entero.");
             } else if (!right.getExpressionType().getTypeName().equals("int")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El operador binario '" + operatorLexeme + "' no puede aplicarse a la subexpresion de tipo " + right.getExpressionType().getTypeName() + ".\nSe esperaba una subexpresion de tipo entero.");
             } else {
                 Type type = new BooleanType();
                 this.setExpressionType(type);
             }
         }
     }
 
     @Override
     public void generateCode() throws SemanticException {
         left.setICG(ICG);
         left.generateCode();
 
         right.setICG(ICG);
         right.generateCode();
 
         ICG.GEN(".CODE");
 
         if (operator.getLexeme().equals("+")) {
             ICG.GEN("ADD");
         } else if (operator.getLexeme().equals("-")) {
             ICG.GEN("SUB");
         } else if (operator.getLexeme().equals("*")) {
             ICG.GEN("MUL");
         } else if (operator.getLexeme().equals("/")) {
             ICG.GEN("DIV");
         } else if (operator.getLexeme().equals("%")) {
             ICG.GEN("MOD");
         } else if (operator.getLexeme().equals("&&")) {
             ICG.GEN("AND");
         } else if (operator.getLexeme().equals("||")) {
             ICG.GEN("OR");
         } else if (operator.getLexeme().equals("==")) {
             ICG.GEN("EQ");
         } else if (operator.getLexeme().equals("<")) {
             ICG.GEN("LT");
         } else if (operator.getLexeme().equals(">")) {
             ICG.GEN("GT");
         } else if (operator.getLexeme().equals("<=")) {
             ICG.GEN("LE");
         } else if (operator.getLexeme().equals(">=")) {
             ICG.GEN("GE");
         }
 
     }
 }
