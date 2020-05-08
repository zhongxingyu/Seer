 package de.skuzzle.polly.parsing.declarations;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TimerTask;
 import java.util.Map.Entry;
 import java.util.Timer;
 
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.Token;
 import de.skuzzle.polly.parsing.TokenType;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.literals.StringLiteral;
 import de.skuzzle.polly.parsing.tree.operators.BinaryOperatorOverload;
 import de.skuzzle.polly.parsing.tree.operators.TernaryOperatorOverload;
 import de.skuzzle.polly.parsing.tree.operators.UnaryOperatorOverload;
 import de.skuzzle.polly.parsing.util.CopyTool;
 
 
 public class Namespace {
 
     
     
     private final static FileFilter FILE_FILTER = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
             String n = pathname.getName().toLowerCase();
             return !n.startsWith(GLOBAL_NAMESPACE) && n.endsWith(".declaration");
         }
     };
     
     
     
     
     public static void setTempVarLifeTime(int lifeTime) {
         tempVarLifeTime = lifeTime;
     }
     
     
     
     private void scheduleDeletion(Declaration var, Declarations declarations) {
         synchronized (TEMP_VAR_KILLER) {
             TEMP_VAR_KILLER.schedule(new TempVarKillTask(declarations, var),
                 tempVarLifeTime);
         }
     }
     
     
    private static int tempVarLifeTime = 60000 * 5; // 5 min
     private final static Timer TEMP_VAR_KILLER = new Timer("TEMP_VAR_KILLER", true);
     private final static boolean IGNORE_UNKNOWN_IDENTIFIERS = true;
     private final static String GLOBAL_NAMESPACE = "~global";
     
     
     private class TempVarKillTask extends TimerTask {
         private Declarations declarations;
         private Declaration var;
         
         
         public TempVarKillTask(Declarations declarations, Declaration var) {
             this.declarations = declarations;
             this.var = var;
         }
         
         
         
         @Override
         public void run() {
             this.declarations.remove(this.var.getName().getIdentifier());
         }
     }
     
     
     
     private Declarations global;
     private Declarations reserved;
     private FunctionDeclaration forbidden;
     private List<UnaryOperatorOverload> unaryOperators;
     private List<BinaryOperatorOverload> binaryOperators;
     private List<TernaryOperatorOverload> ternaryOperators;
     private Map<String, Declarations> namespaces;
     private String currentNS;
     
     
     
     public Namespace() {
         this.global = new Declarations();
         this.reserved = new Declarations();
         this.unaryOperators = new LinkedList<UnaryOperatorOverload>();
         this.binaryOperators = new LinkedList<BinaryOperatorOverload>();
         this.ternaryOperators = new LinkedList<TernaryOperatorOverload>();
         this.namespaces = new HashMap<String, Declarations>();
         this.namespaces.put(GLOBAL_NAMESPACE, this.global);
     }
     
     
     
     public synchronized Namespace copyFor(String rootNamespace) {
     	Namespace copy = new Namespace();
     	copy.currentNS = rootNamespace;
     	copy.global = this.global;
     	copy.reserved = this.reserved;
     	copy.forbidden = this.forbidden;
     	copy.unaryOperators = this.unaryOperators;
     	copy.binaryOperators = this.binaryOperators;
     	copy.ternaryOperators = this.ternaryOperators;
     	copy.namespaces = this.namespaces;
     	if (!copy.namespaces.containsKey(rootNamespace)) {
     	    copy.namespaces.put(rootNamespace, new Declarations());
     	}
     	return copy;
     }
 
     
     
     public String getRootNS() {
         return this.currentNS;
     }
     
     
     
     public void forbidFunction(FunctionDeclaration function) {
         this.forbidden = function;
     }
     
     
     
     public void allowFunction() {
         this.forbidden = null;
     }
     
     
     
     public Declarations getNamespaceFor(String name) {
         return this.namespaces.get(name);
     }
     
     
     
     public Declarations checkNamespace(String namespace, Position nsPosition) 
             throws ParseException {
         Declarations ns = this.getNamespaceFor(namespace);
         if (ns == null) {
             throw new ParseException("Unbekannter Namespace: " + namespace, nsPosition);
         }
         return ns;
     }
     
     
     
     public synchronized void enter() {
         Declarations declarations = this.namespaces.get(this.currentNS);
         declarations.enter();
     }
     
     
     
     public synchronized void leave() {
         Declarations declarations = this.namespaces.get(this.currentNS);
         declarations.leave();
     }
     
     
     
     public synchronized void reserve(Declaration declaration) {
         this.reserved.add(declaration);
     }
     
     
     
     public synchronized void create(String name) {
         Declarations decl = this.namespaces.get(name);
         if (decl == null) {
             this.namespaces.put(name, new Declarations());
         }
     }
     
     
     
     public void addNormal(Declaration declaration) throws ParseException {
         this.add(declaration, this.currentNS, false);
     }
     
     
     
     public void addRoot(Declaration declaration) throws ParseException {
         this.add(declaration, this.currentNS, true);
     }
     
     
     
     public void add(Declaration declaration, String namespace) throws ParseException {
         this.add(declaration, namespace, false);
     }
     
     
     
     private synchronized void add(Declaration declaration, String namespace, boolean root) 
             throws ParseException {
         
         if (this.reserved.tryResolve(
             new IdentifierLiteral(declaration.getName().getIdentifier()), 
             false) != null) {
                 throw new ParseException("'" + declaration.getName().getIdentifier() + 
                         "' ist ein reservierter Bezeichner", 
                         declaration.getName().getPosition());
         }
     
     
         if (declaration.isGlobal()) {
            System.out.println("added global");
             this.global.add(declaration, true);
             
             if (declaration.isTemp()) {
                 scheduleDeletion(declaration, this.global);
             }
             return;
         }
         
         Declarations ns = this.getNamespaceFor(namespace);
         if (ns == null) {
             // if namespace not yet exists, create one!
             ns = new Declarations();
             this.namespaces.put(namespace, ns);
         }
         
         ns.add(declaration, root);
         if (declaration.isTemp()) {
             scheduleDeletion(declaration, ns);
         }
     }
     
     
     
     public void add(UnaryOperatorOverload operator) {
         this.unaryOperators.add(operator);
     }
     
     
     
     public void add(BinaryOperatorOverload operator) {
         this.binaryOperators.add(operator);
     }
     
     
     
     public void add(TernaryOperatorOverload operator) {
         this.ternaryOperators.add(operator);
     }
     
     
     
     public void remove(String name) throws ParseException {
         this.remove(name, this.currentNS);
     }
     
     
     
     public synchronized void remove(String name, String namespace) throws ParseException {
         Declarations ns = this.checkNamespace(namespace, Position.EMPTY);
         ns.remove(name);
     }
     
     
     
     public synchronized Declaration resolve(IdentifierLiteral id, boolean root) 
                 throws ParseException {
         
         if (this.forbidden != null && 
                 id.getIdentifier().equals(this.forbidden.getName().getIdentifier())) {
             throw new ParseException("Unglter rekursiver Aufruf von " + id, 
                 id.getPosition());
     }
         
         Declarations ns = this.namespaces.get(this.currentNS);
         
         Declaration decl = ns.tryResolve(id, root);
         if (decl == null) {
             decl = this.global.tryResolve(id, false); 
         }
         
         if (decl == null) {
             /* ISSUE 0000085: If identifier doesnt exist, treat it as String
              * 
              * throw new ParseException("Unbekannter Bezeichner: '"
              *   + id.getIdentifier() + "'", id.getPosition());
              */
             if (IGNORE_UNKNOWN_IDENTIFIERS) {
                 return new VarDeclaration(id, new StringLiteral(
                     new Token(TokenType.STRING, id.getPosition(), id.getIdentifier())));
             } else {
                 throw new ParseException("Unbekannter Bezeichner: '"
                    + id.getIdentifier() + "'", id.getPosition());
             }
         }
         return decl;
     }
     
     
     
     public synchronized TypeDeclaration resolveType(IdentifierLiteral id) 
             throws ParseException {
         TypeDeclaration decl = (TypeDeclaration) this.reserved.tryResolve(id, false);
         if (decl != null) {
             return decl;
         }
         
         throw new ParseException("Unbekannter Typ: " + id.getIdentifier(), 
             id.getPosition());
     }
     
 
     
     public UnaryOperatorOverload resolveOperator(TokenType op, Type operandType, 
             Position position) throws ParseException {
         for (UnaryOperatorOverload overload : this.unaryOperators) {
             UnaryOperatorOverload result = overload.match(op, operandType);
             if (result != null) {
                 return CopyTool.copyOf(result);
             }
         }
         throw new ParseException("Operator " + op + " nicht anwendbar auf den Typ " + 
                 operandType, position);
     }
     
     
     
     public BinaryOperatorOverload resolveOperator(TokenType op, Type operandType1,
         Type operandType2, Position position) throws ParseException {
         
         for (BinaryOperatorOverload overload : this.binaryOperators) {
             BinaryOperatorOverload result = overload.match(op, operandType1, operandType2);
             if (result != null) {
                 return CopyTool.copyOf(result);
             }
         }
         throw new ParseException("Operator " + op + " nicht anwendbar auf die Typen " + 
                 operandType1 + " und " + operandType2, position);
     }
     
     
     
     public TernaryOperatorOverload resolveOperator(TokenType op, Type operandType1,
         Type operandType2, Type operandType3, Position position) throws ParseException {
         
         for (TernaryOperatorOverload overload : this.ternaryOperators) {
             TernaryOperatorOverload result = overload.match(op, operandType1, 
                     operandType2, operandType3);
             if (result != null) {
                 return CopyTool.copyOf(result);
             }
         }
         throw new ParseException("Operator " + op + " nicht anwendbar auf die Typen " + 
                 operandType1 + ", " + operandType2 + " und " + operandType3, position);
     }
     
     
     
     public void store(File directory) throws IOException {
         if (!directory.isDirectory()) {
             throw new IOException(directory + " is no directory");
         }
         this.global.store(new File(directory, GLOBAL_NAMESPACE + ".declaration"));
         for (Entry<String, Declarations> ns : this.namespaces.entrySet()) {
             ns.getValue().store(new File(directory, ns.getKey() + ".declaration"));
         }
     }
     
     
     
     public void restore(File directory) throws IOException {
         if (!directory.isDirectory()) {
             throw new IOException(directory + " is no directory");
         }
         this.global = Declarations.restore(
                 new File(directory, GLOBAL_NAMESPACE + "declaration"));
         this.namespaces.put(GLOBAL_NAMESPACE, this.global);
         
         File[] files = directory.listFiles(FILE_FILTER);
         
         for (File file : files) {
             Declarations ns = Declarations.restore(file);
             String name = file.getName().substring(0, 
                     file.getName().length() - ".declaration".length());
             this.namespaces.put(name, ns);
         }
     }
     
     
     
     public String toString() {
         StringBuilder result = new StringBuilder();
         result.append("RESERVED\n");
         result.append(this.reserved.toString());
         result.append("GLOBAL\n");
         result.append(this.global.toString());
         result.append("NAMESPACES\n");
         for (Entry<String, Declarations> ns : this.namespaces.entrySet()) {
             result.append(ns.getKey());
             result.append(":\n");
             result.append(ns.getValue());
         }
         return result.toString();
     }
 }
