 package IntermediateCodeGeneration.SymbolTable;
 
 import IntermediateCodeGeneration.AST.BlockNode;
 import IntermediateCodeGeneration.AST.SentenceNode;
 import IntermediateCodeGeneration.ICGenerator;
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.Type.Type;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 
 /**
  * Representacion de la entrada de clase
  *
  * @author Ramiro Agis
  * @author Victoria Martinez de la Cruz
  */
 public class ClassEntry {
 
     private String className;
     private int lineNumber;
     private boolean inheritanceControl;
     private String parent;
     private LinkedList<String> parentList;
     private ConstructorEntry constructor;
     private LinkedHashMap<String, InstanceVariableEntry> instanceVariablesTable;
     private LinkedHashMap<String, MethodEntry> methodsTable;
     private SymbolTable symbolTable;
     private ICGenerator ICG;
     private int instanceVariablesCount; // cantidad de variables de instancia en total (contando heredadas)
 
     public ClassEntry(String className, SymbolTable symbolTable, int lineNumber) {
         this.className = className;
         this.lineNumber = lineNumber;
         this.symbolTable = symbolTable;
         inheritanceControl = false;
         parent = null;
         parentList = new LinkedList<>();
         constructor = null;
         instanceVariablesTable = new LinkedHashMap<>();
         methodsTable = new LinkedHashMap<>();
         instanceVariablesCount = 0;
     }
 
     /**
      * Retorna el nombre de la clase asociada a la entrada de clase
      *
      * @return name
      */
     public String getName() {
         return className;
     }
 
     /**
      * Retorna el numero de linea donde se declaro la clase
      *
      * @return lineNumber
      */
     public int getLineNumber() {
         return lineNumber;
     }
 
     /**
      * Establece el ancestro directo de la entrada de clase
      *
      * @param parent
      */
     public void setParent(String parent) {
         this.parent = parent;
     }
 
     /**
      * Retorna el ancestro directo de la entrada de clase
      */
     public String getParent() {
         return parent;
     }
 
     /**
      * Agrega el ancestro identificado con el nombre pasado por parametro al
      * final de la lista de ancestros
      *
      * @param parent nombre del ancestro a agregar a la lista de ancestros
      */
     public void setParentList(String aParent) {
         ClassEntry parentEntry = symbolTable.getClassEntry(parent);
 
         if (parentEntry != null) {
             LinkedList<String> ancestors = parentEntry.getParentList();
             for (String ancestor : ancestors) {
                 parentList.addLast(ancestor);
             }
             parentList.addLast(aParent);
         } else {
             parentList.addLast(aParent);
         }
     }
 
     /**
      * Retorna la jerarquia de herencia de la entrada de clase
      *
      * @return lista con los ancestros de la clase
      */
     public LinkedList<String> getParentList() {
         return parentList;
     }
 
     /**
      * Retorna el constructor de la entrada de clase
      *
      * @return constructor
      */
     public ConstructorEntry getConstructorEntry() {
         return constructor;
     }
 
     /**
      * Establece el constructor pasado por parametro como constructor de la
      * clase
      *
      * @param constructor
      */
     public void setConstructorEntry(String constructorName, int lineNumber) {
         ConstructorEntry constructorEntry = new ConstructorEntry(constructorName, className, symbolTable, lineNumber);
         this.constructor = constructorEntry;
     }
 
     /**
      * Controla si existe un constructor. En caso de no existir, agrega un
      * constructor default
      */
     public void controlDefaultConstructor() {
         if (getConstructorEntry() == null) {
             constructor = new ConstructorEntry(className, className, symbolTable, 0);
             constructor.setBody(new BlockNode(symbolTable, new LinkedList<SentenceNode>(), null));
         }
     }
 
     /**
      * Crea e inserta una nueva entrada de metodo en la tabla de metodos
      *
      * @param methodName nombre del metodo a insertar
      * @param type tipo del metodo a insertar
      * @param modificator modificador del metodo a insertar
      */
     public void addMethodEntry(String methodName, Type returnType, String modificator, int lineNumber) {
         MethodEntry method = new MethodEntry(methodName, className, modificator, returnType, symbolTable, lineNumber);
         methodsTable.put(methodName, method);
     }
 
     /**
      * Retorna la tabla de metodos
      *
      * @return methodsTable
      */
     public LinkedHashMap<String, MethodEntry> getMethods() {
         return methodsTable;
     }
 
     /**
      * Retorna la entrada de metodo de un metodo deseado
      *
      * @param methodName
      */
     public MethodEntry getMethodEntry(String methodName) {
         return methodsTable.get(methodName);
     }
 
     /**
      * Crea e inserta una nueva entrada de variable de instancia en la tabla de
      * variables de instancia
      *
      * @param instanceVariableName nombre de la variable de instancia a insertar
      * @param type tipo de la variable de instancia a insertar
      * @param lineNumber numero de linea
      */
     public void addInstanceVariableEntry(String instanceVariableName, Type instanceVariableType, int lineNumber) {
         InstanceVariableEntry instanceVariable = new InstanceVariableEntry(instanceVariableName, instanceVariableType, lineNumber);
         instanceVariablesTable.put(instanceVariableName, instanceVariable);
     }
 
     /**
      * Retorna la tabla de variables de instancia
      *
      * @return instanceVariablesTable
      */
     public LinkedHashMap<String, InstanceVariableEntry> getInstanceVariables() {
         return instanceVariablesTable;
     }
 
     /**
      * Retorna la entrada de variable de instancia de una variable de instancia
      * deseada
      *
      * @param instanceVariableName
      * @param lineNumber
      * @return instanceVariable
      */
     public InstanceVariableEntry getInstanceVariableEntry(String instanceVariableName) {
         return instanceVariablesTable.get(instanceVariableName);
     }
 
     /**
      * Controla que exista un metodo principal sin retorno (void) y estatico
      * (static)
      *
      * @return true si la clase tiene un metodo main void y static, false en
      * caso contrario
      */
     public boolean hasMain() throws SemanticException {
         MethodEntry main = methodsTable.get("main");
         if (main != null) {
             if (!main.getModifier().equals("static")) {
                 throw new SemanticException("Linea: " + main.getLineNumber() + " - Error semantico: El metodo main debe ser estatico");
             }
 
             if (!main.getParameters().isEmpty()) {
                 throw new SemanticException("Linea: " + main.getLineNumber() + " - Error semantico: El metodo main no debe tener parametros");
             }
         } else {
             return false;
         }
         return true;
     }
 
     /**
      * De ser posible, copia los metodos heredados de la clase padre (herencia
      * por copia)
      *
      * @throws SemanticException
      */
     public void controlInheritedMethods() throws SemanticException {
 
         if (parent.equals("Object") && !inheritanceControl) {
             // el padre es Object, por lo que no hay nada que heredar
             // simplemente establecemos los offsets correspondientes
 
             // offsets de los metodos
             setMethodOffset(0);
 
             // offsets de las variables de instancia
             instanceVariablesCount = instanceVariablesTable.size();
             setInstanceVariablesOffset(0);
 
             inheritanceControl = true;
         } else if (!parent.equals("Object") && !inheritanceControl) {
             // Las variables de instancia no se heredan
             // Ver documentacoin
             inheritMethods();
             inheritanceControl = true;
 
             int offsetBaseMethods = symbolTable.getClassEntry(parent).getMethods().size() - 1;
             setMethodOffset(offsetBaseMethods);
 
             int offsetBaseVariables = symbolTable.getClassEntry(parent).getInstanceVariablesCount();
             setInstanceVariablesOffset(offsetBaseVariables);
 
         }
     }
 
     /**
      * Copia metodos heredados y controla los metodos redefinidos
      *
      * @throws SemanticException
      */
     private void inheritMethods() throws SemanticException {
         Collection<MethodEntry> inheritedMethods = symbolTable.getClassEntry(parent).getMethods().values();
 
         for (MethodEntry parentMethod : inheritedMethods) {
             String parentMethodName = parentMethod.getName();
 
             if (parentMethodName.equals(className)) {
                 throw new SemanticException("Linea: " + getLineNumber() + " - Error semantico: La clase " + parent + " tiene un metodo con el mismo nombre que su subclase " + className + ".");
             }
 
             if (methodsTable.get(parentMethodName) != null) {
                 // Se encontro el nombre    
                 // Controlar si el metodo fue redefinido
                 MethodEntry redefinedMethod = methodsTable.get(parentMethodName);
                 redefinedMethod.compareModifier(parentMethod);
                 redefinedMethod.compareReturnType(parentMethod);
                 redefinedMethod.compareParameters(parentMethod);
 
                 redefinedMethod.setOffset(parentMethod.getOffset());
                 redefinedMethod.setParametersOffset();
                 redefinedMethod.setLocalVariablesOffset();
             } else {
                 // Se hereda el metodo
                 addInheritedMethod(parentMethod);
                 MethodEntry inheritedMethod = methodsTable.get(parentMethodName);
                 inheritedMethod.setOffset(parentMethod.getOffset());
             }
         }
     }
 
     /**
      * Agrega el metodo heredado a la tabla de metodos de la clase actual
      * Previamente se controla de que el metodo agregado no colisione con una
      * variable de instancia
      *
      * @param parentMethod
      * @throws SemanticException
      */
     private void addInheritedMethod(MethodEntry parentMethod) throws SemanticException {
         String parentMethodName = parentMethod.getName();
 
         if (instanceVariablesTable.get(parentMethodName) != null) {
             throw new SemanticException("Linea: " + getLineNumber() + " - Error semantico: La clase " + parent + " tiene un metodo con el mismo nombre que la variable de instancia " + parentMethodName + " de su subclase " + className + ".");
         }
 
         methodsTable.put(parentMethodName, parentMethod);
     }
 
     /**
      * Establece el handler de generacion de codigo intermedio
      *
      * @param ICG
      */
     public void setICG(ICGenerator ICG) {
         this.ICG = ICG;
     }
 
     /**
      * Control de sentencias de la clase
      *
      * @param symbolTable
      */
     public void checkClass() throws SemanticException {
         initVT();

         constructor.setICG(ICG);
         constructor.checkService();
 
         for (MethodEntry method : methodsTable.values()) {
             symbolTable.setCurrentMethod(method.getName());
             method.setICG(ICG);
             method.checkService();
         }
     }
 
     public void setMethodOffset(int baseOffset) {
         int offset = 0;
         for (MethodEntry aMethod : methodsTable.values()) {
             if (aMethod.getOffset() == -1) {    // no es un metodo heredado (controlado en su respectiva clase)
                 aMethod.setOffset(baseOffset + offset);
                 aMethod.setLocalVariablesOffset();
                 aMethod.setParametersOffset();
                 offset++;
             }
         }
     }
 
     public void setInstanceVariablesOffset(int baseOffset) {
         int offset = 1;
         for (InstanceVariableEntry anInstanceVariable : instanceVariablesTable.values()) {
             anInstanceVariable.setOffset(baseOffset + offset);
             offset++;
         }
         instanceVariablesCount = baseOffset + instanceVariablesTable.size();
     }
 
     public void setConstructorOffset() {
         constructor.setLocalVariablesOffset();
         constructor.setParametersOffset();
     }
 
     /**
      * Retorna el total de variables de instancia propias
      *
      * @return
      */
     public int getInstanceVariablesCount() {
         return instanceVariablesCount;
     }
 
     private void initVT() {
         ICG.GEN(".DATA");
         if (methodsTable.isEmpty()) {
             ICG.GEN("VT_" + className + ": NOP");
         } else {
             String DWInstruction = "VT_" + className + ": DW ";
             Collection<MethodEntry> methods = methodsTable.values();
             for (MethodEntry aMethod : methods) {
                 DWInstruction += "L_" + aMethod.getClassName() + "_" + aMethod.getName() + " ,";
             }
 
             DWInstruction = DWInstruction.substring(0, DWInstruction.length() - 1); // Comma removal
             ICG.GEN(DWInstruction);
         }
     }
 }
