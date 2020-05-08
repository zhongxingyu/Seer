 package IntermediateCodeGeneration.SymbolTable;
 
 import IntermediateCodeGeneration.ICGenerator;
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.Type.*;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Set;
 
 /**
  * Representacion de la tabla de simbolos para la generacion de codigo
  * intermedio
  *
  * @author Ramiro Agis
  * @author Victoria Martinez de la Cruz
  */
 public class SymbolTable {
 
     private LinkedHashMap<String, ClassEntry> classTable;
     private LinkedHashMap<String, ClassEntry> controlledClasses;
     private ICGenerator ICG;
     private String currentClass, currentMethod, mainClass;
 
     /**
      * Constructor de la tabla de simbolos Crea las estructuras e inicializa los
      * atributos
      */
     public SymbolTable() {
         classTable = new LinkedHashMap<>();
         controlledClasses = new LinkedHashMap<>();
         currentClass = null;
         currentMethod = null;
     }
 
     // Inicializacion
     /**
      * Inicializa la tabla de simbolos
      */
     public void initSymbolTable() {
         addClassObject();
         addClassSystem();
     }
 
     /**
      * Agrega la clase "Object" a la tabla de simbolos
      */
     private void addClassObject() {
         ClassEntry Object = new ClassEntry("Object", this, 0);
         Object.setParent(null); // El padre de Object es null
 
         classTable.put("Object", Object);
 
         controlledClasses.put("Object", Object);
     }
 
     /**
      * Agrega la clase "System" y sus metodos a la tabla de simbolos
      */
     private void addClassSystem() {
         ClassEntry System = new ClassEntry("System", null, 0);
         System.setParent("Object");
 
         classTable.put("System", System);
 
 
         System.addMethodEntry("read", new IntegerType(), "static", 0);
 
         System.addMethodEntry("printB", new VoidType(), "static", 0);
         System.getMethodEntry("printB").addParameterEntry("b", new BooleanType(), 0);
 
         System.addMethodEntry("printC", new VoidType(), "static", 0);
         System.getMethodEntry("printC").addParameterEntry("c", new CharType(), 0);
 
         System.addMethodEntry("printI", new VoidType(), "static", 0);
         System.getMethodEntry("printI").addParameterEntry("i", new IntegerType(), 0);
 
         System.addMethodEntry("printS", new VoidType(), "static", 0);
         System.getMethodEntry("printS").addParameterEntry("s", new StringType(), 0);
 
         System.addMethodEntry("println", new VoidType(), "static", 0);
 
         System.addMethodEntry("printBln", new VoidType(), "static", 0);
         System.getMethodEntry("printBln").addParameterEntry("b", new BooleanType(), 0);
 
         System.addMethodEntry("printCln", new VoidType(), "static", 0);
         System.getMethodEntry("printCln").addParameterEntry("c", new CharType(), 0);
 
         System.addMethodEntry("printIln", new VoidType(), "static", 0);
         System.getMethodEntry("printIln").addParameterEntry("i", new IntegerType(), 0);
 
         System.addMethodEntry("printSln", new VoidType(), "static", 0);
         System.getMethodEntry("printSln").addParameterEntry("s", new StringType(), 0);
 
         controlledClasses.put("System", System);
     }
 
     // Triviales
     /**
      * Retorna la entrada de clase de una clase deseada
      *
      * @param className nombre de la entrada de clase a recuperar
      * @return ClassEntry si la clase de nombre className se encuentra en el
      * diccionario de clases, null en caso contrario
      */
     public ClassEntry getClassEntry(String className) {
         return classTable.get(className);
     }
 
     /**
      * Retorna el conjunto de clases presentes en la tabla de simbolos
      *
      * @return classTable
      */
     public LinkedHashMap getClasses() {
         return classTable;
     }
 
     /**
      * Crea e inserta una nueva entrada de clase en la tabla de simbolos
      *
      * @param className nombre de la entrada de clase a agregar
      */
     public void addClassEntry(String className, int lineNumber) {
         ClassEntry aClass = new ClassEntry(className, this, lineNumber);
         classTable.put(className, aClass);
     }
 
     /**
      * Establece la clase actual
      *
      * @param className nombre de la clase actual
      */
     public void setCurrentClass(String className) {
         currentClass = className;
     }
 
     /**
      * Retorna la clase actual
      *
      * @return clase actual
      */
     public String getCurrentClass() {
         return currentClass;
     }
 
     /**
      * Establece el metodo actual
      *
      * @param methodName nombre del metodo actual
      */
     public void setCurrentMethod(String methodName) {
         currentMethod = methodName;
     }
 
     /**
      * Retorna el metodo actual
      *
      * @return
      */
     public String getCurrentMethod() {
         return currentMethod;
     }
 
     // Controles   
     /**
      * Control de declaraciones: Herencia
      *
      * @throws SemanticException
      */
     public void declarationCheckInheritance() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
 
             if (!aClass.getName().equals("Object") && !aClass.getName().equals("System")) {
                 // control de existencia de las clases padre
                 String parent = aClass.getParent();
                 if (classTable.get(parent) == null) {
                     throw new SemanticException("Error semantico: La clase '" + aClass.getParent() + "', padre de la clase '" + aClass.getName() + "', no existe.");
                 }
             }
         }
     }
 
     /**
      * Consolidacion de herencia Se efectua, de ser posible, la herencia por
      * copia entre las clases que tienen una relacion "es un"
      *
      * @throws SemanticException
      */
     public void consolidateInheritance() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
 
             if (!aClass.getName().equals("Object") && !aClass.getName().equals("System")) {
                 // control de herencia
                 // consolidacion de herencia
                 controlInheritance(aClass.getName());
             }
         }
     }
 
     /**
      * Consolidacion de constructores Se inicializan los offsets de los
      * parametros y variables locales de los constructores
      */
     public void consolidateConstructors() {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
 
             if (!aClass.getName().equals("Object") && !aClass.getName().equals("System")) {
                 // consolidacion de constructores (solo offsets)
                 aClass.setConstructorOffset();
             }
         }
     }
 
     /**
      * Controla que no exista herencia circular entre las clases Para eso se
      * busca a si misma en la lista de ancestros
      *
      * @param className
      */
     public void controlCircularInheritance(ClassEntry aClass) throws SemanticException {
         LinkedList<String> parents = aClass.getParentList();
         String className = aClass.getName();
 
         if (parents.contains(className) == true) {
             // Una clase se tiene a si misma en la lista de ancestros.
             throw new SemanticException("Error semantico: Herencia circular. La clase " + className + " no puede heredar de si misma.");
         }
     }
 
     /**
      * Consolidacion de herencia Si la clase padre no fue consolidada, entonces
      * se invoca recursivamente a la consolidacion para la clase padre En caso
      * contrario, se delega el control de consolidacion a la entrada de clase de
      * la clase hijo
      *
      * @param className
      * @throws SemanticException
      */
     private void controlInheritance(String className) throws SemanticException {
         ClassEntry aClass = classTable.get(className);
         String parent = aClass.getParent();
 
         if (controlledClasses.get(parent) == null) {
             controlInheritance(parent);
         }
 
         aClass.controlInheritedMethods();
         controlledClasses.put(className, aClass);
     }
 
     /**
      * Control de declaraciones: Tipo de retorno de los metodos
      *
      * @throws SemanticException
      */
     public void declarationCheckReturnType() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
             if (!aClass.getName().equals("Object") || !aClass.getName().equals("System")) {
                 Collection<MethodEntry> methods = aClass.getMethods().values();
 
                 for (MethodEntry aMethod : methods) {
                     Type returnType = aMethod.getReturnType();
                     if (!typeExists(returnType)) {
                         throw new SemanticException("Linea: " + aMethod.getLineNumber() + " - Error semantico: El tipo de retorno " + returnType.getTypeName() + " del metodo " + aMethod.getName() + " no existe.");
                     }
                 }
             }
 
         }
     }
 
     /**
      * Control de declaraciones: Variables
      *
      * @throws SemanticException
      */
     public void declarationCheckVariables() throws SemanticException {
        declarationCheckLocalVars();
        declarationCheckParameters();
         declarationCheckInstanceVariables();
     }
 
     /**
      * Controla que exista un metodo principal sin retorno (void) y estatico
      * (static) en alguna de las clases presentes en el conjunto de clases
      */
     public void declarationCheckMainExistence() throws SemanticException {
         Set<String> classes = classTable.keySet();
         for (String aClass : classes) {
             if (getClassEntry(aClass).hasMain()) {
                 mainClass = aClass;
                 return;
             }
         }
         throw new SemanticException("Error semantico: El metodo 'main' no fue declarado en ninguna de las clases.");
     }
 
     /**
      * Control de declaraciones: Variables de instancias
      *
      * @throws SemanticException
      */
     private void declarationCheckInstanceVariables() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
             Collection<InstanceVariableEntry> instanceVariables = aClass.getInstanceVariables().values();
 
             for (InstanceVariableEntry anInstanceVar : instanceVariables) {
                 Type aType = anInstanceVar.getType();
                 if (!typeExists(aType)) {
                     throw new SemanticException("Linea: " + anInstanceVar.getLineNumber() + " - Error semantico: El tipo " + aType.getTypeName() + " de la variable de instancia " + anInstanceVar.getVariableName() + " no existe.");
                 }
             }
         }
     }
 
     /**
      * Control de declaraciones: Parametros
      *
      * @throws SemanticException
      */
     private void declarationCheckParameters() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
 
             if (!aClass.getName().equals("Object") && !aClass.getName().equals("System")) {
                 ConstructorEntry aConstructor = aClass.getConstructorEntry();
 
                 Collection<ParameterEntry> parameters = aConstructor.getParameters().values();
 
                 for (ParameterEntry aParameter : parameters) {
                     Type aType = aParameter.getType();
                     if (!typeExists(aType)) {
                         throw new SemanticException("Linea: " + aParameter.getLineNumber() + " - Error semantico: El tipo " + aType.getTypeName() + " del parametro " + aParameter.getVariableName() + " en el constructor '" + aConstructor.getName() + "' no existe.");
                     }
                 }
 
 
                 Collection<MethodEntry> methods = aClass.getMethods().values();
 
                 for (MethodEntry aMethod : methods) {
                     parameters = aMethod.getParameters().values();
 
                     for (ParameterEntry aParameter : parameters) {
                         Type aType = aParameter.getType();
                         if (!typeExists(aType)) {
                             throw new SemanticException("Linea: " + aParameter.getLineNumber() + " - Error semantico: El tipo " + aType.getTypeName() + " del parametro " + aParameter.getVariableName() + " en el metodo '" + aMethod.getName() + "' no existe.");
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Control de declaraciones: Variables locales
      *
      * @throws SemanticException
      */
     private void declarationCheckLocalVars() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
 
             if (!aClass.getName().equals("Object") && !aClass.getName().equals("System")) {
                 ConstructorEntry aConstructor = aClass.getConstructorEntry();
 
                 Collection<LocalVariableEntry> localVariables = aConstructor.getLocalVariables().values();
 
                 for (LocalVariableEntry aLocalVariable : localVariables) {
                     Type aType = aLocalVariable.getType();
                     if (!typeExists(aType)) {
                         throw new SemanticException("Linea: " + aLocalVariable.getLineNumber() + " - Error semantico: El tipo " + aType.getTypeName() + " de la variable local " + aLocalVariable.getVariableName() + " en el constructor '" + aConstructor.getName() + "' no existe.");
                     }
                 }
 
                 Collection<MethodEntry> methods = aClass.getMethods().values();
 
                 for (MethodEntry aMethod : methods) {
                     localVariables = aMethod.getLocalVariables().values();
 
                     for (LocalVariableEntry aLocalVariable : localVariables) {
                         Type aType = aLocalVariable.getType();
                         if (!typeExists(aType)) {
                             throw new SemanticException("Linea: " + aLocalVariable.getLineNumber() + " - Error semantico: El tipo " + aType.getTypeName() + " de la variable local " + aLocalVariable.getVariableName() + " en el metodo '" + aMethod.getName() + "' no existe.");
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Control de sentencias
      */
     public void sentenceCheck() throws SemanticException {
         Collection<ClassEntry> classes = classTable.values();
 
         for (ClassEntry aClass : classes) {
             if (!aClass.getName().equals("Object") && !aClass.getName().equals("System")) {
                 currentClass = aClass.getName();
                 aClass.setICG(ICG);
                 aClass.checkClass();
             }
         }
     }
 
     // Auxiliares
     public String isConstructor(String id) {
         Set<String> classes = classTable.keySet();
 
         for (String aClass : classes) {
             if (aClass.equals(id)) {
                 return aClass;
             }
         }
 
         return null;
     }
 
     public boolean isMethodInClass(String aClass, String aMethod) {
         if (classTable.get(aClass) != null) {
             if (classTable.get(aClass).getMethodEntry(aMethod) != null) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean typeExists(Type type) {
         return Type.isPrimitiveType(type) || classTable.get(type.getTypeName()) != null;
     }
 
     /**
      * Retorna el nombre de la clase que contiene el metodo principal * Por
      * convencion, se elige la primer clase con el metodo main
      *
      * @return mainClass
      */
     public String getMainClass() {
         return mainClass;
     }
 
     /**
      * Establece el handler de generacion de codigo intermedio
      *
      * @param ICG
      */
     public void setICG(ICGenerator ICG) {
         this.ICG = ICG;
     }
 }
