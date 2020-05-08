 package IntermediateCodeGeneration.AST;
 
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.Type.Type;
 import IntermediateCodeGeneration.Token;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 
 /**
  * Representacion de un nodo literal
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class LiteralNode extends PrimaryNode {
 
     protected Token literal;
 
     public LiteralNode(SymbolTable symbolTable, Token literal, Type type) {
         super(symbolTable, literal);
         this.literal = literal;
         expressionType = type;
     }
 
     @Override
     public void checkNode() {
     }
 
     @Override
     public void generateCode() throws SemanticException {
 
         if (expressionType.getTypeName().equals("null")) {
             ICG.GEN(".CODE");
             ICG.GEN("PUSH 0", "Apilamos 'null'");
         } else if (expressionType.getTypeName().equals("String")) {
             String label = ICG.generateLabel();
 
             ICG.GEN(".DATA");
             ICG.GEN("lString" + label + "_" + symbolTable.getCurrentService() + "_" + symbolTable.getCurrentClass() + ": DW " + literal.getLexeme() + ", 0");
 
             ICG.GEN(".CODE");
             ICG.GEN("PUSH lString" + label + "_" + symbolTable.getCurrentService() + "_" + symbolTable.getCurrentClass(), "Apilamos el label del String '" + literal.getLexeme() + "'.");
         } else if (expressionType.getTypeName().equals("char")) {
             if (literal.getLexeme().equals("'\n'")) {
                 ICG.GEN("PUSH 10", "Apilo el caracter nl.");
             } else if (literal.getLexeme().equals("'\t'")) {
                 ICG.GEN("PUSH 9", "Apilo el caracter tab.");
             } else {
                 ICG.GEN("PUSH " + (int) literal.getLexeme().charAt(0), "Apilo el caracter " + literal.getLexeme());
             }
         } else if (expressionType.getTypeName().equals("boolean")) {
             if (literal.getLexeme().equals("true")) {
                 ICG.GEN(".CODE");
                 ICG.GEN("PUSH 1", "Apilamos 'true'");
             } else if (literal.getLexeme().equals("false")) {
                 ICG.GEN(".CODE");
                 ICG.GEN("PUSH 0", "Apilamos 'false'");
             }
        } else {
             ICG.GEN(".CODE");
             ICG.GEN("PUSH " + literal.getLexeme());
         }
 
 //        if (expressionType.getTypeName().equals("String")) {
 //            String label = ICG.generateLabel();
 //
 //            ICG.GEN(".DATA");
 //            ICG.GEN("lString" + label + "_" + symbolTable.getCurrentService() + "_" + symbolTable.getCurrentClass() + ": DW " + literal.getLexeme() + ", 0");
 //
 //            ICG.GEN(".CODE");
 //            ICG.GEN("PUSH lString" + label + "_" + symbolTable.getCurrentService() + "_" + symbolTable.getCurrentClass(), "Apilamos el label del String '" + literal.getLexeme() + "'.");
 //        } else if (literal.getLexeme().equals("true")) {
 //            ICG.GEN(".CODE");
 //            ICG.GEN("PUSH 1", "Apilamos 'true'");
 //        } else if (literal.getLexeme().equals("false")) {
 //            ICG.GEN(".CODE");
 //            ICG.GEN("PUSH 0", "Apilamos 'false'");
 //        } else if (literal.getLexeme().equals("null")) {
 //            ICG.GEN(".CODE");
 //            ICG.GEN("PUSH 0", "Apilamos 'null'");
 //        } else {
 //            ICG.GEN(".CODE");
 //            ICG.GEN("PUSH " + literal.getLexeme());
 //        }
 
     }
 }
