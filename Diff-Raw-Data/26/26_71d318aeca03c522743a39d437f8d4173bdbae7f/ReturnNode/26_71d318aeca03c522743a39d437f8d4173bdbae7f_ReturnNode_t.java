 package IntermediateCodeGeneration.AST;
 
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.ClassEntry;
 import IntermediateCodeGeneration.SymbolTable.MethodEntry;
 import IntermediateCodeGeneration.SymbolTable.Type.Type;
 import IntermediateCodeGeneration.SymbolTable.Type.VoidType;
 import IntermediateCodeGeneration.Token;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 
 /**
  * Representacion de un nodo return
  *
  * @author Ramiro Agis
  * @author Victoria Martinez de la Cruz
  */
 public class ReturnNode extends SentenceNode {
 
     protected Token returnToken;
 
     public ReturnNode(SymbolTable systemTable, Token token) {
         super(systemTable, token);
         returnToken = token;
     }
 
     @Override
     public void checkNode() throws SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         String currentMethod = symbolTable.getCurrentMethod();
 
         String returnType = symbolTable.getClassEntry(currentClass).getMethodEntry(currentMethod).getReturnType().getTypeName();
 
         if (!returnType.equals("void")) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: Se esperaba retornar una expresion de tipo " + returnType + ".");
         }
 
         Type aType = new VoidType();
         this.setSentenceType(aType);
     }
 
     @Override
     public void generateCode() throws SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         String currentMethod = symbolTable.getCurrentMethod();
         ClassEntry currentClassEntry = symbolTable.getClassEntry(currentClass);
         MethodEntry currentMethodEntry = currentClassEntry.getMethodEntry(currentMethod);
 
         int parametersCount = currentMethodEntry.getParameters().size();
         int localVariablesCount = currentMethodEntry.getLocalVariables().size();
 
         ICG.GEN(".CODE");
         ICG.GEN("; Retorno vacio del metodo '" + currentMethod + "' de la clase '" + currentClass + "'");
 
         if (localVariablesCount > 0) {
             // El metodo tiene variables locales
            ICG.GEN("FMEM", localVariablesCount, "Liberamos el espacio usado por las variables locales del metodo '" + currentMethod + "' de la clase '" + currentClass + "'.");
         }
 
         ICG.GEN("STOREFP", "Actualizamos el FP para que apunte al RA del llamador");
         ICG.GEN("RET", parametersCount + 1, "Retornamos de la unidad liberando el espacio que ocupaban los parametros y el THIS del metodo '" + currentMethod + "' de la clase '" + currentClass + "'.");
     }
 }
