 package IntermediateCodeGeneration.AST;
 
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.ClassEntry;
 import IntermediateCodeGeneration.SymbolTable.Type.Type;
 import IntermediateCodeGeneration.Token;
 import IntermediateCodeGeneration.SymbolTable.MethodEntry;
 import IntermediateCodeGeneration.SymbolTable.ParameterEntry;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 import java.util.Collection;
 import java.util.LinkedList;
 
 /**
  * Representacion de un nodo llamada a metodo e.g. id(), id(1,2,c),
  * id1().id2(1,2,c)
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class MethodCallNode extends PrimaryNode {
 
     protected Token id;
     protected Type idType;
     protected LinkedList<ExpressionNode> actualArgs;
     protected LinkedList<CallNode> callList;
 
     public MethodCallNode(SymbolTable symbolTable, Token id, LinkedList<ExpressionNode> actualArgs, LinkedList<CallNode> callList, Token token) {
         super(symbolTable, token);
         this.id = id;
         this.actualArgs = actualArgs;
         this.callList = callList;   // Puede ser vacia - Caso 1
     }
 
     /**
      * Casos a controlar
      *
      * Caso 1: X es solo parte de una llamada a metodo, entonces X debe ser un
      * metodo de la clase C (e.g., X(2)).
      *
      * Como la herencia implementada es por copia - i.e. todos los metodos
      * heredados de una clase padre se copian a la clase hijo - simplemente
      * deberemos buscar en la tabla de la clase donde se esta usando el nombre.
      *
      * Caso 2: X aparece a la derecha de una expresion punto y es parte de una
      * llamada a metodo, entonces X debe ser un metodo de la clase del tipo de
      * retorno del metodo de la izquierda (e.g., g().X(2)).
      *
      * Primero es necesario determinar la clase en la cual hay que buscar el
      * metodo. Esta clase se determina por el tipo (estatico) de la parte
      * izquierda de la expresion punto. El tipo de la izquierda debe ser una
      * clase (no un tipo primitivo o void), o de lo contrario se reportara un
      * error semantico. Una vez identificada la clase de la izquierda se buscara
      * el metodo de manera similar al caso del inciso (1) pero en esa clase.
      *
      * @throws SemanticException
      */
     @Override
     public void checkNode() throws SemanticException {
         // control de nombre
 
         checkId();
 
         // control de argumentos
         for (ExpressionNode arg : actualArgs) {
             arg.checkNode();
         }
 
         controlFormalArgs();
 
         // control de llamadas
 
         Type callerType = idType;
         for (CallNode call : callList) {
             call.setCallerType(callerType);
             call.checkNode();
             callerType = call.getCallReturnType();
         }
 
         controlReturnType();
     }
 
     /**
      * Controla que sea un metodo de la clase actual Si es un constructor o no
      * esta definido, ocurrira un error
      *
      * @throws SemanticException
      */
     private void checkId() throws SemanticException {
         // o bien es un metodo de la clase actual
         // o bien es un metodo de alguna clase ancestro de la clase actual
         // si es un constructor, ocurrira un error
         String currentClass = symbolTable.getCurrentClass();
         String currentService = symbolTable.getCurrentService();
         String idName = id.getLexeme();
 
         if (currentClass.equals(idName)) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: No puede realizarse una llamada a un constructor.");
         }
 
         MethodEntry methodEntry = symbolTable.getClassEntry(currentClass).getMethodEntry(idName);
         MethodEntry currentMethodEntry = symbolTable.getClassEntry(currentClass).getMethodEntry(currentService);
 
         if (methodEntry == null) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: No existe el metodo '" + idName + "' en la clase " + currentClass + ".");
         } else if (methodEntry.getModifier().equals("dynamic") && currentMethodEntry != null && currentMethodEntry.getModifier().equals("static")) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: No puede hacerse una invocacion al metodo dinamico '" + idName + "' en la clase " + currentClass + " en el contexto del metodo estatico '" + currentService + "'.");
         } else {
             idType = symbolTable.getClassEntry(currentClass).getMethodEntry(idName).getReturnType();
             setExpressionType(idType);
         }
     }
 
     /**
      * Control de conformidad de tipos entre argumentos formales y argumentos
      * actuales de un metodo
      *
      * @throws SemanticException
      */
     private void controlFormalArgs() throws SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         Collection<ParameterEntry> formalArgs = symbolTable.getClassEntry(currentClass).getMethodEntry(id.getLexeme()).getParameters().values();
        
 
         if (formalArgs.size() != actualArgs.size()) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: Las listas de argumentos actuales y formales para el metodo " + id.getLexeme() + " de la clase " + currentClass + " tienen diferente longitud.");
         }
        int index = 0;
         for (ParameterEntry formalArg : formalArgs) {
             actualArgs.get(index).checkNode();
             if (!formalArg.getType().checkConformity(actualArgs.get(index).getExpressionType())) {
                int position = index + 1;
                throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: En la llamada al metodo '" + id.getLexeme() + "' el tipo del argumento actual en la posicion (" + position + ") no conforma con el tipo del argumento formal."
                        + " El tipo del argumento actual es " + actualArgs.get(index).getExpressionType().getTypeName() + " y el tipo del argumento formal es " + formalArg.getType().getTypeName() + ".");
             }
             index++;
         }
     }
 
     /**
      * Control del tipo de retorno de la expresion llamadora E.g. para un caso
      * como g().h() tenemos que asegurarnos que se pueda mandar el mensaje h()
      * al retorno de g(). Es decir, el retorno de g() debe ser de un tipo de
      * clase C tal que exista un metodo M en C.
      */
     private void controlReturnType() throws SemanticException {
         Type currentType = getExpressionType();
         Type nextType = currentType;
         String nextId;
 
         for (CallNode nextCall : callList) {
             nextType = nextCall.getExpressionType();
             nextId = nextCall.getId().getLexeme();
 
             if (symbolTable.getClassEntry(currentType.getTypeName()).getMethodEntry(nextId) == null) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El metodo '" + nextId + "' no es un metodo de la clase '" + currentType.getTypeName() + "'.");
             }
 
             nextType.checkConformity(currentType);
             currentType = nextType;
         }
 
         // si no surge ningun error durante el control de conformidad de tipos
         // se le asigna al nodo actual el tipo del ultimo callnode en la lista
 
         this.setExpressionType(nextType);
 
     }
 
     @Override
     public void generateCode() throws SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         String currentMethod = id.getLexeme();
 
         ICG.GEN(".CODE");
         ICG.GEN("LOAD", 3, "Apilamos el THIS para invocar al metodo '" + currentMethod + "'");
 
         Type returnType = symbolTable.getClassEntry(currentClass).getMethodEntry(currentMethod).getReturnType();
         String returnTypeName = returnType.getTypeName();
 
         if (!returnTypeName.equals("void")) {
             ICG.GEN("RMEM", 1, "Reservamos una locacion de memoria para el resultado del metodo '" + currentMethod + "' de la clase '" + currentClass + "'.");
             ICG.GEN("SWAP", "Acomodamos el THIS haciendo un SWAP con RETVAL.");
         }
 
         for (ExpressionNode actualArg : actualArgs) {
             actualArg.setICG(ICG);
             actualArg.generateCode();
             ICG.GEN("SWAP", "Acomodamos el THIS cada vez que generamos el codigo para un parametro.");
         }
 
         if (!currentClass.equals(id.getLexeme()) && symbolTable.getClassEntry(currentClass).getMethodEntry(id.getLexeme()).getModifier().equals("static")) {
             ClassEntry classEntry = symbolTable.getClassEntry(currentClass);
             ICG.GEN("PUSH VT_" + currentClass + classEntry.getClassNumber());
         } else {
             ICG.GEN("DUP", "MethodCallNode. Duplicamos la referencia al CIR para utilizarla en el LOADREF al asociar la VT para invocar al metodo '" + currentMethod + "'.");
             ICG.GEN("LOADREF", 0, "MethodCallNode. El offset de la VT en el CIR es siempre 0. Accedemos a la VT.");
         }
 
         int offsetId = symbolTable.getClassEntry(currentClass).getMethodEntry(id.getLexeme()).getOffset();
 
         ICG.GEN("LOADREF", offsetId, "MethodCallNode. Recuperamos la direccion del metodo '" + currentMethod + "'.");
         ICG.GEN("CALL", "MethodCallNode. Llamamos al metodo '" + currentMethod + "'.");
 
         Type callerType = returnType;
         for (CallNode call : callList) {
             call.setCallerType(callerType);
             call.setICG(ICG);
             call.generateCode();
             callerType = call.getCallReturnType();
         }
     }
 }
