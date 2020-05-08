 package IntermediateCodeGeneration;
 
 import IntermediateCodeGeneration.AST.*;
 import IntermediateCodeGeneration.SymbolTable.*;
 import IntermediateCodeGeneration.SymbolTable.Type.*;
 import java.util.LinkedList;
 
 /**
  * Analizador sintáctico. Transforma su entrada en un arbol de derivación.
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class Parser {
 
     private Tokenizer tokenizer;
     private Token lookAhead, currentToken;
     public SymbolTable symbolTable;
 
     // analizador sintactico
     /**
      * Constructor de la clase Parser
      *
      * Inicializa las estructuras de datos utilizadas.
      *
      * @param filename Path del archivo con el código fuente
      */
     public Parser(String filename) {
         tokenizer = new Tokenizer(filename);
         symbolTable = new SymbolTable();
     }
 
     /**
      * Inicia el proceso de análisis obteniendo el primer token y procesando la
      * regla inicial de la gramática.
      *
      * @throws SyntacticException
      * @throws LexicalException
      */
     public void analize() throws SyntacticException, LexicalException, SemanticException {
         lookAhead = tokenizer.getToken();
         Inicial();
     }
 
     /**
      * Compara el token encontrado con lo que se esperaba de acuerdo a la
      * gramatica. En caso de encontrar un token no esperado o el fin de linea
      * durante el proceso de analisis, se procedera a detenerlo y a notificar el
      * error.
      *
      * @param token
      * @throws LexicalException
      * @throws SyntacticException
      */
     public void match(String token) throws LexicalException, SyntacticException {
         if (lookAhead.getToken().equals(token)) {
             if (!token.equals("EOF")) {
                 currentToken = lookAhead;
                 lookAhead = tokenizer.getToken();
             } else {
                 System.err.println("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se alcanzo el fin de archivo durante el analisis sintactico.");
             }
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba: '" + token + "'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     // Todos los metodos que siguen corresponden a las reglas de la gramatica de MiniJava
     // Existe un metodo por no-terminal de la gramatica, y el proceso se lleva adelante siguiendo el flujo de ejecucion normal
     private void Inicial() throws LexicalException, SyntacticException, SemanticException {
         symbolTable.initSymbolTable();
         Clase();
         ListaClases();
     }
 
     private void ListaClases() throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals("class")) {
             Clase();
             ListaClases();
         } else if (lookAhead.equals("EOF")) {
             // El analizador semantico termino exitosamente
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba la palabra reservada 'class'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private void Clase() throws LexicalException, SyntacticException, SemanticException {
         match("class");
         if (!lookAhead.equals("id")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el nombre de la clase. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         match("id");
         String className = currentToken.getLexeme();
         int lineNumber = currentToken.getLineNumber();
         if (symbolTable.getClassEntry(className) == null) {
             symbolTable.addClassEntry(className, lineNumber);
             symbolTable.setCurrentClass(className);
         } else {
             throw new SemanticException("Linea: " + currentToken.getLineNumber() + " - Error semantico: Ya existe una clase declarada con el nombre " + className);
         }
         Herencia();
         // el control de { lo hace Herencia()
         match("{");
         ListaMiembros("class");
 
         // Si la clase fue declarada sin constructor,
         // el compilador le asignará un constructor sin argumentos y con cuerpo vacío.
         symbolTable.getClassEntry(className).controlDefaultConstructor();
         // el control de } lo hace ListaMiembros()
         match("}");
     }
 
     private void Herencia() throws LexicalException, SyntacticException, SemanticException {
         String className = symbolTable.getCurrentClass();
         ClassEntry classEntry = symbolTable.getClassEntry(className);
         if (lookAhead.equals("extends")) {
             match("extends");
             match("id");
             String parent = currentToken.getLexeme();
             classEntry.setParent(parent);
             classEntry.setParentList(parent);
             symbolTable.controlCircularInheritance(classEntry);
         } else if (lookAhead.equals("{")) {
             // Herencia -> lambda
             // No hay herencia
             classEntry.setParent("Object");
             classEntry.setParentList("Object");
             symbolTable.controlCircularInheritance(classEntry);
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el comienzo de la clase '{' o la especificacion de herencia. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private void ListaMiembros(String from) throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals("}")) {
             // ListaMiembros -> lambda
             // No hay mas miembros
         } else {
             Miembro(from);
             ListaMiembros(from);
             // Para no duplicar codigo dejamos que el control
             // de lo haga Miembro()
         }
     }
 
     private void Miembro(String from) throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals("var")) {
             Atributo(from);
         } else if (lookAhead.equals("id")) {
             Ctor();
         } else if (lookAhead.equals("static") || lookAhead.equals("dynamic")) {
             Metodo();
         } else if (isType(lookAhead)) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: No se encontro el modificador del metodo.");
         } else if (lookAhead.equals("EOF")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el cierre de la clase '}'. Se encontro: '" + lookAhead.getToken() + "'.");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba la definicion de atributos, constructores o metodos. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private void Atributo(String from) throws LexicalException, SyntacticException, SemanticException {
         match("var");
         Type type = Tipo();
         ListaDecVars(from, type);
         if (!lookAhead.equals(";")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el terminador de la lista de variables ';'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         match(";");
     }
 
     private void Metodo() throws LexicalException, SyntacticException, SemanticException {
         String modificator = ModMetodo();
         Type type = TipoMetodo();
 
         match("id");
 
         String methodName = currentToken.getLexeme();
         symbolTable.setCurrentService(methodName);
         String currentClass = symbolTable.getCurrentClass();
         ClassEntry classEntry = symbolTable.getClassEntry(currentClass);
 
 
         if (methodName.equals(currentClass)) {
             throw new SemanticException("Linea: " + lookAhead.getLineNumber() + " - Error semantico: El metodo " + methodName + " no puede tener el mismo nombre que su clase.");
         } else if (classEntry.getMethodEntry(methodName) != null) {
             throw new SemanticException("Linea: " + lookAhead.getLineNumber() + " - Error semantico: Ya existe un metodo " + methodName + " declarado en la clase " + currentClass);
         } else {
             classEntry.addMethodEntry(methodName, type, modificator, lookAhead.getLineNumber());
         }
 
         ArgsFormales();
         VarsLocales("method");
         BlockNode body = Bloque();
         classEntry.getMethodEntry(methodName).setBody(body);
     }
 
     private void Ctor() throws LexicalException, SyntacticException, SemanticException {
         match("id");
         String constructorName = currentToken.getLexeme();
         symbolTable.setCurrentService(constructorName);
         String currentClass = symbolTable.getCurrentClass();
         ClassEntry classEntry = symbolTable.getClassEntry(currentClass);
         if (!currentClass.equals(constructorName)) {
             throw new SemanticException("Linea: " + lookAhead.getLineNumber() + " - Error semantico: El nombre del constructor " + constructorName + " no corresponde al nombre de la clase.");
         } else if (classEntry.getConstructorEntry() != null) {
             throw new SemanticException("Linea: " + lookAhead.getLineNumber() + " - Error semantico: Ya existe un constructor en la clase " + currentClass + ".");
         } else {
             classEntry.setConstructorEntry(constructorName, lookAhead.getLineNumber());
         }
         ArgsFormales();
         VarsLocales("constructor");
         BlockNode body = Bloque();
         classEntry.getConstructorEntry().setBody(body);
     }
 
     private void ArgsFormales() throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals("(")) {
             match("(");
             ArgsFormales_();
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba la apertura de una lista de argumentos formales '('. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private void ArgsFormales_() throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals(")")) {
             // ArgsFormales_ -> )
             // No hay mas argumentos formales
             match(")");
         } else {
             ListaArgsFormales();
             if (lookAhead.equals(")")) {
                 match(")");
             } else {
                 throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el cierre de la lista de argumentos formales ')'. Se encontro: '" + lookAhead.getToken() + "'.");
             }
         }
     }
 
     private void ListaArgsFormales() throws LexicalException, SyntacticException, SemanticException {
         ArgFormal();
         ListaArgsFormales_();
     }
 
     private void ListaArgsFormales_() throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals(")")) {
             // ListaArgsFormales_ -> lambda
             // No hay mas argumentos formales
         } else if (lookAhead.equals(",")) {
             match(",");
             ListaArgsFormales();
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba un nuevo argumento formal o el cierre de la lista de argumentos formales ')'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private void ArgFormal() throws LexicalException, SyntacticException, SemanticException {
         Type type = Tipo();
         match("id");
         String parameterName = currentToken.getLexeme();
         String currentClass = symbolTable.getCurrentClass();
         String currentMethod = symbolTable.getCurrentService();
         ServiceEntry serviceEntry; // Can be a constructor or a method. 
 
         if (currentMethod.equals(currentClass)) {
             // This is the constructor.
             serviceEntry = symbolTable.getClassEntry(currentClass).getConstructorEntry();
 
         } else {
             // This is a method.
             serviceEntry = symbolTable.getClassEntry(currentClass).getMethodEntry(currentMethod);
         }
 
         if (serviceEntry.getParameterEntry(parameterName) != null) {
             throw new SemanticException("Linea: " + lookAhead.getLineNumber() + " - Error semantico: Ya existe un argumento formal con el nombre " + parameterName + " en el servicio " + currentMethod + " de la clase " + currentClass);
         } else {
             serviceEntry.addParameterEntry(parameterName, type, currentToken.getLineNumber());
         }
     }
 
     private void VarsLocales(String from) throws LexicalException, SyntacticException, SemanticException {
         ListaAtributos(from);
     }
 
     private void ListaAtributos(String from) throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals("{")) {
             // ListaAtributos -> lambda
             // Bloque
             // No hay mas atributos
         } else if (lookAhead.equals("var")) {
             Atributo(from);
             ListaAtributos(from);
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el comienzo de bloque del metodo '{' o la definicion de variables locales. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private String ModMetodo() throws LexicalException, SyntacticException {
         if (lookAhead.equals("static")) {
             match("static");
             return "static";
         } else {
             match("dynamic");
             return "dynamic";
         }
     }
 
     private Type TipoMetodo() throws LexicalException, SyntacticException {
         Type type;
         if (lookAhead.equals("void")) {
             match("void");
             type = createType("void");
         } else {
             type = Tipo();
         }
         return type;
     }
 
     private Type Tipo() throws LexicalException, SyntacticException {
         Type type;
         if (lookAhead.equals("id")) {
             match("id");
             type = new ClassType(currentToken.getLexeme(), symbolTable);
         } else {
             type = TipoPrimitivo();
         }
         return type;
     }
 
     private Type TipoPrimitivo() throws LexicalException, SyntacticException {
         Type type;
         if (lookAhead.equals("boolean")) {
             match("boolean");
             type = createType("boolean");
         } else if (lookAhead.equals("char")) {
             match("char");
             type = createType("char");
         } else if (lookAhead.equals("int")) {
             match("int");
             type = createType("int");
         } else if (lookAhead.equals("String")) {
             match("String");
             type = createType("String");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el tipo de la variable. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         return type;
     }
 
     private void ListaDecVars(String from, Type type) throws LexicalException, SyntacticException, SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         ClassEntry classEntry = symbolTable.getClassEntry(currentClass);
 
         if (lookAhead.equals("id")) {
             match("id");
             String variableName = currentToken.getLexeme();
             int lineNumber = currentToken.getLineNumber();
             ListaDecVars_(from, type);
             if (from.equals("class")) {
                 // Declaración de variable de instancia.
                 if (classEntry.getInstanceVariableEntry(variableName) == null) {
                     // La variable de instancia no existe. Se crea.
                     classEntry.addInstanceVariableEntry(variableName, type, lineNumber);
                 } else {
                     throw new SemanticException("Linea: " + lineNumber + " - Error semantico: Existe mas de una variable de instancia con el nombre " + variableName + " en la clase " + currentClass);
                 }
             } else if (from.equals("method")) {
                 // Declaración de variable local.
                 String currentMethod = symbolTable.getCurrentService();
                 ServiceEntry serviceEntry = classEntry.getMethodEntry(currentMethod);
                 if (serviceEntry.getParameterEntry(variableName) != null) {
                     throw new SemanticException("Linea: " + lineNumber + " - Error semantico: Existe mas de un parametro con el nombre " + variableName + " en el metodo " + currentMethod + " de la clase " + currentClass);
                 } else if (serviceEntry.getLocalVariableEntry(variableName) != null) {
                     throw new SemanticException("Linea: " + lineNumber + " - Error semantico: Existe mas de una variable local con el nombre " + variableName + " en el metodo " + currentMethod + " de la clase " + currentClass);
                 } else {
                     // La variable local no existe. Se crea.
                     serviceEntry.addLocalVariableEntry(variableName, type, lineNumber);
                 }
 
             } else if (from.equals("constructor")) {
                 // Declaración de variable local.
                 String currentConstructor = symbolTable.getCurrentService();
                 ServiceEntry serviceEntry = classEntry.getConstructorEntry();
                 if (serviceEntry.getParameterEntry(variableName) != null) {
                     throw new SemanticException("Linea: " + lineNumber + " - Error semantico: Existe mas de un parametro con el nombre " + variableName + " en el constructor " + currentConstructor + " de la clase " + currentClass);
                 } else if (serviceEntry.getLocalVariableEntry(variableName) != null) {
                     throw new SemanticException("Linea: " + lineNumber + " - Error semantico: Existe mas de una variable local con el nombre " + variableName + " en el constructor " + currentConstructor + " de la clase " + currentClass);
                 } else {
                     // La variable local no existe. Se crea.
                     serviceEntry.addLocalVariableEntry(variableName, type, lineNumber);
                 }
             }
         } else if (lookAhead.equals(",")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Falta especificar el tipo de las variables declaradas."); // caso: var v1, v2;                                                                                                                                                 // v1 se toma como tipo  
         }
     }
 
     private void ListaDecVars_(String from, Type type) throws LexicalException, SyntacticException, SemanticException {
         if (lookAhead.equals(";")) {
             // ListaDecVars_ -> lambda
             // No hay mas variables declaradas
         } else if (lookAhead.equals(",")) {
             match(",");
             ListaDecVars(from, type);
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba una variable. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private BlockNode Bloque() throws LexicalException, SyntacticException {
         if (lookAhead.equals("{")) {
             match("{");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba la apertura de un bloque '}'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         LinkedList<SentenceNode> sentenceList = ListaSentencias();
         if (lookAhead.equals("}")) {
             match("}");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el cierre de un bloque '{'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         BlockNode block = new BlockNode(symbolTable, sentenceList, currentToken);
         return block;
     }
 
     private LinkedList<SentenceNode> ListaSentencias() throws LexicalException, SyntacticException {
         if (lookAhead.equals("}")) {
             // ListaSentencias -> lambda
             // No hay mas sentencias
             return new LinkedList<>();
         } else {
             SentenceNode sentence = Sentencia();
             // Delego el control de terminales o no-terminales a Sentencia()
             LinkedList<SentenceNode> sentenceList = ListaSentencias();
             sentenceList.addFirst(sentence);
             return sentenceList;
         }
     }
 
     private SentenceNode Sentencia() throws LexicalException, SyntacticException {
         if (lookAhead.equals(";")) {
             Token current = currentToken;
             match(";");
             return new SeparatorNode(symbolTable, current);
         } else if (lookAhead.equals("id")) {
             AssignNode assign = Asignacion();
             match(";");
             return assign;
         } else if (lookAhead.equals("(")) {
             SimpleSentenceNode simpleSentence = SentenciaSimple();
             match(";");
             return simpleSentence;
         } else if (lookAhead.equals("if")) {
             match("if");
            Token current = currentToken; 
             match("(");
             ExpressionNode condition = Expresion();
             match(")");
             SentenceNode sentenceIf = Sentencia();
            return Sentencia_(condition, sentenceIf, current);
         } else if (lookAhead.equals("while")) {
             match("while");
            Token current = currentToken;
             match("(");
             ExpressionNode condition = Expresion();
             match(")");
             SentenceNode sentence = Sentencia();
             return new WhileNode(symbolTable, condition, sentence, current);
         } else if (lookAhead.equals("for")) {
             match("for");
            Token current = currentToken;
             match("(");
             AssignNode init = Asignacion();
             match(";");
             ExpressionNode condition = Expresion();
             match(";");
             ExpressionNode increment = Expresion();
             match(")");
             SentenceNode sentence = Sentencia();
             return new ForNode(symbolTable, init, condition, increment, sentence, current);
         } else if (lookAhead.equals("{")) {
             return Bloque();
         } else if (lookAhead.equals("return")) {
             match("return");
             Token current = currentToken;
             ExpressionNode expression = Sentencia__();
             match(";");
             if (expression == null) {
                 // return;
                 return new ReturnNode(symbolTable, current);
             } else {
                 // return <Expresion>;
                 return new ReturnExpNode(symbolTable, expression, current);
             }
         } else if (lookAhead.equals("var")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: No pueden declararse variables dentro de un bloque.");
         } else if (lookAhead.equals("=")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Falta el lado izquierdo de la asignacion.");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba una sentencia o el cierre de bloque.");
         }
     }
 
    private SentenceNode Sentencia_(ExpressionNode condition, SentenceNode sentenceIf, Token current) throws LexicalException, SyntacticException {
         if (lookAhead.equals("else")) {
             match("else");
             SentenceNode sentenceElse = Sentencia();
            return new IfThenElseNode(symbolTable, condition, sentenceIf, sentenceElse, current);
         } else {
             // Sentencia_ -> lambda
             // if-then sin else
            return new IfThenNode(symbolTable, condition, sentenceIf, current);
 
         }
     }
 
     private ExpressionNode Sentencia__() throws LexicalException, SyntacticException {
         if (lookAhead.equals(";")) {
             // Sentencia__ -> lambda
             return null;
         } else {
             return Expresion();
         }
     }
 
     private AssignNode Asignacion() throws LexicalException, SyntacticException {
 
         match("id");
         Token id = currentToken;
         match("=");
         Token current = currentToken;
         ExpressionNode expression = Expresion();
         return new AssignNode(symbolTable, id, expression, current);
     }
 
     private SimpleSentenceNode SentenciaSimple() throws LexicalException, SyntacticException {
         Token current = currentToken;
         if (!lookAhead.equals("(")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba la apertura de una expresion parentizada '('. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         match("(");
         ExpressionNode expression = Expresion();
         if (!lookAhead.equals(")")) {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el cierre de una expresion parentizada ')'. Se encontro: '" + lookAhead.getToken() + "'.");
         }
         match(")");
         return new SimpleSentenceNode(symbolTable, expression, current);
     }
 
     private ExpressionNode Expresion() throws LexicalException, SyntacticException {
         return Expresion6();
     }
 
     private ExpressionNode Expresion6() throws LexicalException, SyntacticException {
         ExpressionNode left = Expresion5();
         ExpressionNode right = Expresion6_(left);
         return right;
     }
 
     private ExpressionNode Expresion6_(ExpressionNode left) throws LexicalException, SyntacticException {
         if (lookAhead.equals("||")) {
             Token current = currentToken;
             match("||");
             Token operator = currentToken;
             ExpressionNode right = Expresion5();
             ExpressionNode binaryExpression = new BinaryExpressionNode(symbolTable, operator, left, right, current);
             return Expresion6_(binaryExpression);
         } else {
             // Expresion6_ -> lambda
             return left;
         }
     }
 
     private ExpressionNode Expresion5() throws LexicalException, SyntacticException {
         ExpressionNode left = Expresion4();
         ExpressionNode right = Expresion5_(left);
         return right;
     }
 
     private ExpressionNode Expresion5_(ExpressionNode left) throws LexicalException, SyntacticException {
         if (lookAhead.equals("&&")) {
             Token current = currentToken;
             match("&&");
             Token operator = currentToken;
             ExpressionNode right = Expresion4();
             ExpressionNode binaryExpression = new BinaryExpressionNode(symbolTable, operator, left, right, current);
             return Expresion5_(binaryExpression);
         } else {
             // Expresion5_ -> lambda
             return left;
         }
     }
 
     private ExpressionNode Expresion4() throws LexicalException, SyntacticException {
         ExpressionNode left = Expresion3();
         ExpressionNode right = Expresion4_(left);
         return right;
     }
 
     private ExpressionNode Expresion4_(ExpressionNode left) throws LexicalException, SyntacticException {
         if (lookAhead.equals("==") || lookAhead.equals("!=")) {
             Token current = currentToken;
             Token operator = Operador4();
             ExpressionNode right = Expresion3();
             ExpressionNode binaryExpression = new BinaryExpressionNode(symbolTable, operator, left, right, current);
             return Expresion4_(binaryExpression);
         } else {
             // Expresion4_ -> lambda
             return left;
         }
     }
 
     private ExpressionNode Expresion3() throws LexicalException, SyntacticException {
         ExpressionNode left = Expresion2();
         ExpressionNode right = Expresion3_(left);
         return right;
     }
 
     private ExpressionNode Expresion3_(ExpressionNode left) throws LexicalException, SyntacticException {
         if (lookAhead.equals("<") || lookAhead.equals(">") || lookAhead.equals(">=") || lookAhead.equals("<=")) {
             Token current = currentToken;
             Token operator = Operador3();
             ExpressionNode right = Expresion2();
             ExpressionNode binaryExpression = new BinaryExpressionNode(symbolTable, operator, left, right, current);
             return Expresion3_(binaryExpression);
         } else {
             // Expresion3_ -> lambda
             return left;
         }
     }
 
     private ExpressionNode Expresion2() throws LexicalException, SyntacticException {
         ExpressionNode left = Expresion1();
         ExpressionNode right = Expresion2_(left);
         return right;
     }
 
     private ExpressionNode Expresion2_(ExpressionNode left) throws LexicalException, SyntacticException {
         if (lookAhead.equals("+") || lookAhead.equals("-")) {
             Token current = currentToken;
             Token operator = Operador2();
             ExpressionNode right = Expresion1();
             ExpressionNode binaryExpression = new BinaryExpressionNode(symbolTable, operator, left, right, current);
             return Expresion2_(binaryExpression);
         } else {
             // Expresion2_ -> lambda
             return left;
         }
     }
 
     private ExpressionNode Expresion1() throws LexicalException, SyntacticException {
         ExpressionNode left = Expresion0();
         ExpressionNode right = Expresion1_(left);
         return right;
     }
 
     private ExpressionNode Expresion1_(ExpressionNode left) throws LexicalException, SyntacticException {
         if (lookAhead.equals("*") || lookAhead.equals("/") || lookAhead.equals("%")) {
             Token current = currentToken;
             Token operator = Operador1();
             ExpressionNode right = Expresion0();
             ExpressionNode binaryExpression = new BinaryExpressionNode(symbolTable, operator, left, right, current);
             return Expresion1_(binaryExpression);
         } else {
             // Expresion1_ -> lambda
             return left;
         }
     }
 
     private ExpressionNode Expresion0() throws LexicalException, SyntacticException {
         if (lookAhead.equals("!") || lookAhead.equals("+") || lookAhead.equals("-")) {
             Token current = currentToken;
             return new UnaryExpressionNode(symbolTable, OperadorUnario(), Expresion0(), current);
         } else {
             return Primario();
         }
     }
 
     private Token Operador4() throws LexicalException, SyntacticException {
         if (lookAhead.equals("==")) {
             match("==");
         } else if (lookAhead.equals("!=")) {
             match("!=");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba == o != . Se encontro: '" + lookAhead.getToken() + "'.");
         }
         return currentToken;
     }
 
     private Token Operador3() throws LexicalException, SyntacticException {
         if (lookAhead.equals("<")) {
             match("<");
         } else if (lookAhead.equals(">")) {
             match(">");
         } else if (lookAhead.equals(">=")) {
             match(">=");
         } else if (lookAhead.equals("<=")) {
             match("<=");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba <, >, <=, >= . Se encontro: '" + lookAhead.getToken() + "'.");
         }
         return currentToken;
     }
 
     private Token Operador2() throws LexicalException, SyntacticException {
         if (lookAhead.equals("+")) {
             match("+");
         } else if (lookAhead.equals("-")) {
             match("-");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba + o - . Se encontro: '" + lookAhead.getToken() + "'.");
         }
         return currentToken;
     }
 
     private Token Operador1() throws LexicalException, SyntacticException {
         if (lookAhead.equals("*")) {
             match("*");
         } else if (lookAhead.equals("/")) {
             match("/");
         } else if (lookAhead.equals("%")) {
             match("%");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba *, / o % . Se encontro: '" + lookAhead.getToken() + "'.");
         }
         return currentToken;
     }
 
     private Token OperadorUnario() throws LexicalException, SyntacticException {
         if (lookAhead.equals("!")) {
             match("!");
         } else if (lookAhead.equals("+")) {
             match("+");
         } else if (lookAhead.equals("-")) {
             match("-");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba !, + o - . Se encontro: '" + lookAhead.getToken() + "'.");
         }
         return currentToken;
     }
 
     private ExpressionNode Primario() throws LexicalException, SyntacticException {
         if (lookAhead.equals("this")) {
             match("this");
             return new ThisNode(symbolTable, currentToken);
         } else if (lookAhead.equals("(")) {
             Token current = currentToken;
             match("(");
             ExpressionNode expression = Expresion();
             match(")");
             LinkedList<CallNode> callList = ListaLlamadas();
             return new ExpressionCallNode(symbolTable, expression, callList, current);
         } else if (lookAhead.equals("id")) {
             match("id");
             return ListaLlamadas_(currentToken);
         } else if (lookAhead.equals("new")) {
             match("new");
             Token newToken = currentToken;
             match("id");
             Token idToken = currentToken;
             LinkedList<ExpressionNode> actualArgs = ArgsActuales();
             LinkedList<CallNode> callList = ListaLlamadas();
             return new NewNode(symbolTable, idToken, actualArgs, callList, newToken);
         } else {
             return Literal();
         }
     }
 
     private LinkedList<CallNode> ListaLlamadas() throws LexicalException, SyntacticException {
 
         if (lookAhead.equals(".")) {
             CallNode call = Llamada();
             LinkedList<CallNode> callList = ListaLlamadas();
             callList.addFirst(call);
             return callList;
 
         } else {
             // ListaLlamadas -> lambda
             return new LinkedList<>();
         }
     }
 
     private PrimaryNode ListaLlamadas_(Token id) throws LexicalException, SyntacticException {
         Token current = currentToken;
 
         if (lookAhead.equals("(")) {
             LinkedList<ExpressionNode> actualArgs = ArgsActuales();
             return new MethodCallNode(symbolTable, current, actualArgs, ListaLlamadas(), current);
         } else {
             // Si la lista de llamadas es vacia, se trata de un Id
             return new IdMethodCallNode(symbolTable, current, ListaLlamadas(), current);
         }
     }
 
     private CallNode Llamada() throws LexicalException, SyntacticException {
         Token current = currentToken;
         match(".");
         match("id");
         return new CallNode(symbolTable, currentToken, ArgsActuales(), current);
 
     }
 
     private LiteralNode Literal() throws LexicalException, SyntacticException {
         Type type;
         if (lookAhead.equals("null")) {
             match("null");
             type = createType("null");
         } else if (lookAhead.equals("true")) {
             match("true");
             type = createType("boolean");
         } else if (lookAhead.equals("false")) {
             match("false");
             type = createType("boolean");
         } else if (lookAhead.equals("intLiteral")) {
             match("intLiteral");
             type = createType("int");
         } else if (lookAhead.equals("charLiteral")) {
             match("charLiteral");
             type = createType("char");
         } else if (lookAhead.equals("stringLiteral")) {
             match("stringLiteral");
             type = createType("String");
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba un literal. Se encontro: '" + lookAhead.getToken() + "'.");
         }
 
         return new LiteralNode(symbolTable, currentToken, type);
     }
 
     private LinkedList<ExpressionNode> ArgsActuales() throws LexicalException, SyntacticException {
         if (lookAhead.equals("(")) {
             match("(");
             return ArgsActuales_();
         } else {
             throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba la apertura de una lista de argumentos actuales '('. Se encontro: '" + lookAhead.getToken() + "'.");
         }
     }
 
     private LinkedList<ExpressionNode> ArgsActuales_() throws LexicalException, SyntacticException {
         if (lookAhead.equals(")")) {
             // ArgsActuales_ -> )
             // No hay mas argumentos actuales
             match(")");
             return new LinkedList<>();
         } else {
 
             // Usamos el argumento para guardar las expresiones porque es una recursión usada complicada y es más cómodo crear la lista desde afuera.
             LinkedList<ExpressionNode> args = new LinkedList<>();
             ListaExps(args);
             if (lookAhead.equals(")")) {
                 match(")");
                 return args;
             } else {
                 throw new SyntacticException("Linea: " + lookAhead.getLineNumber() + " - Error sintactico: Se esperaba el cierre de la lista de argumentos actuales ')'. Se encontro: '" + lookAhead.getToken() + "'.");
             }
         }
     }
 
     private void ListaExps(LinkedList<ExpressionNode> expressionList) throws LexicalException, SyntacticException {
         ExpressionNode e = Expresion();
         expressionList.addLast(e);
         ListaExps_(expressionList);
     }
 
     private void ListaExps_(LinkedList<ExpressionNode> expressionList) throws LexicalException, SyntacticException {
         if (lookAhead.equals(",")) {
             match(",");
             ListaExps(expressionList);
         } else {
             // ListaExps_ -> lambda
         }
     }
 
     /**
      * Control de declaracion de metodos
      *
      * @param lookAhead
      * @return true si se trata de un tipo, false en caso contrario
      */
     private boolean isType(Token lookAhead) {
         return lookAhead.equals("void") || lookAhead.equals("boolean") || lookAhead.equals("char") || lookAhead.equals("int") || lookAhead.equals("String") || lookAhead.equals("id");
     }
 
     /**
      * Crea un objeto Type para el manejo de tipos
      *
      * @param type
      * @return objeto Type
      */
     private Type createType(String type) {
         Type aType;
 
         if (type.equals("int")) {
             aType = new IntegerType();
         } else if (type.equals("char")) {
             aType = new CharType();
         } else if (type.equals("boolean")) {
             aType = new BooleanType();
         } else if (type.equals("String")) {
             aType = new StringType();
         } else if (type.equals("void")) {
             aType = new VoidType();
         } else {
             aType = new ClassType(type, symbolTable);
         }
 
         return aType;
     }
 
     protected SymbolTable getSymbolTable() {
         return symbolTable;
     }
 }
