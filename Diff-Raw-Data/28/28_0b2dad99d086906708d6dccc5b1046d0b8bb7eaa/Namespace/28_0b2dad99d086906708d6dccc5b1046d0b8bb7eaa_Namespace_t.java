 package de.skuzzle.polly.core.parser.ast.declarations;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 import de.skuzzle.polly.core.parser.ParseException;
 import de.skuzzle.polly.core.parser.ParserProperties;
 import de.skuzzle.polly.core.parser.Position;
 import de.skuzzle.polly.core.parser.ast.Identifier;
 import de.skuzzle.polly.core.parser.ast.ResolvableIdentifier;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Substitution;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 import de.skuzzle.polly.core.parser.ast.expressions.Braced;
 import de.skuzzle.polly.core.parser.ast.expressions.VarAccess;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.NumberLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.StringLiteral;
 import de.skuzzle.polly.core.parser.ast.lang.Cast;
 import de.skuzzle.polly.core.parser.ast.lang.Operator.OpType;
 import de.skuzzle.polly.core.parser.ast.lang.functions.Comp;
 import de.skuzzle.polly.core.parser.ast.lang.functions.Day;
 import de.skuzzle.polly.core.parser.ast.lang.functions.FoldLeft;
 import de.skuzzle.polly.core.parser.ast.lang.functions.Id;
 import de.skuzzle.polly.core.parser.ast.lang.functions.Sort;
 import de.skuzzle.polly.core.parser.ast.lang.operators.BinaryArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.BinaryBooleanArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.BinaryStringArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.Conditional;
 import de.skuzzle.polly.core.parser.ast.lang.operators.DateArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.DateTimespanArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.ListIndex;
 import de.skuzzle.polly.core.parser.ast.lang.operators.RandomListIndex;
 import de.skuzzle.polly.core.parser.ast.lang.operators.Relational;
 import de.skuzzle.polly.core.parser.ast.lang.operators.ReverseList;
 import de.skuzzle.polly.core.parser.ast.lang.operators.ReverseString;
 import de.skuzzle.polly.core.parser.ast.lang.operators.StringIndex;
 import de.skuzzle.polly.core.parser.ast.lang.operators.TernaryDotDot;
 import de.skuzzle.polly.core.parser.ast.lang.operators.TimespanArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.UnaryArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.UnaryBooleanArithmetic;
 import de.skuzzle.polly.core.parser.ast.lang.operators.UnaryList;
 import de.skuzzle.polly.core.parser.ast.lang.operators.UnaryTimespan;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.parser.ast.visitor.Unparser;
 import de.skuzzle.polly.core.parser.problems.ProblemReporter;
 import de.skuzzle.polly.core.parser.problems.Problems;
 import de.skuzzle.polly.tools.strings.StringUtils;
 
 
 
 /**
  * <p>This class represents a hierarchical store for variable declarations. Every 
  * namespace has a parent namespace, except the NATIVE namespace. Namespaces that are not 
  * created with a parent will automatically have the PUBLIC namespace as parent. The 
  * NATIVE space contains all native operator and constant declarations that can be
  * used, the PUBLIC space will contain declarations declared public.</p>
  * 
  * <p>For each variable name can be stored a list of declarations. Declarations with
  * the same name must have distinct unique types. Resolving of a variable name will first
  * search the current namespace and then all parent namespaces.</p>
  * 
  * <p>Namespaces can be obtained using any of the <code>forName</code> methods. The
  * returned namespace instance will store all declared variables into a file with the
  * name of the namespace.</p> 
  * 
  * @author Simon Taddiken
  */
 public class Namespace {
     
     /** 
      * Percentage of word length that will be used as levenshtein threshold in
      * {@link #findSimilar(String, Namespace)}.
      */
     private final static float LEVENSHTEIN_THRESHOLD_PERCENT = 0.6f;
     
     
     /** Name of the public namespace */
     public final static String PUBLIC_NAMESPACE_NAME = "~public";
     
     
     
     public static File declarationFolder;
     public static synchronized void setDeclarationFolder(File declarationFolder) {
         Namespace.declarationFolder = declarationFolder;
     }
     
     
     
     
     
     /**
      * Private namespace extension that will store the namespace's contents to file 
      * whenever a new variable is declared or deleted.
      * 
      * @author Simon Taddiken
      */
     private final static class StorableNamespace extends Namespace {
 
         private String fileName;
         
         
         
         public StorableNamespace(String fileName, Namespace parent) {
             super(parent);
             this.fileName = fileName;
         }
         
         
         
         @Override
         public synchronized void declare(Declaration decl) 
                 throws ASTTraversalException {
             super.declare(decl);
             if (decl.isNative()) {
                 return;
             }
             
             try {
                 this.store();
             } catch (IOException ignore) {
                 throw new ASTTraversalException(decl.getPosition(), 
                     "Deklaration konnte nicht gespeichert werden");
             }
         }
         
         
         
         @Override
         protected void delete(Iterator<Declaration> it) {
             super.delete(it);
             try {
                 this.store();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         
         
         
         /**
          * Stores the content of this namespace (no parent namespaces) to a file
          * in the folder set by {@link Namespace#setDeclarationFolder(File)}.
          * 
          * @throws IOException If storing of the namespace fails.
          */
         public void store() throws IOException {
             if (declarationFolder == null) {
                 throw new IOException("declaration folder has not been set");
             }
             
             PrintWriter pw = null;
             try {
                 pw = new PrintWriter(new File(declarationFolder, this.fileName));
                 
                 final Unparser up = new Unparser(pw);
                 for (final List<Declaration> decls : this.decls.values()) {
                     for (final Declaration decl : decls) {
                         new Braced(decl.getExpression().getPosition(), 
                             decl.getExpression()).visit(up);
                         pw.print("->");
                        if (decl.isPublic()) {
                            pw.print("public ");
                        }
                         if (decl.getName().wasEscaped()) {
                             pw.print("\\");
                         }
                         pw.println(decl.getName().getId());
                     }
                 }
             } catch (ASTTraversalException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (pw != null) {
                     pw.close();
                 }
             }
         }
     }
     
     
     
     
     
     
     
 
     
     
     
     /**
      * Helper class that encapsulates the result of a Levenshtein comparison: the distance
      * and the actual found string.
      * @author Simon Taddiken
      */
     private final static class LevenshteinResult implements 
                 Comparable<LevenshteinResult> {
         
         private int distance;
         public String string;
         
         public LevenshteinResult(int distance, String string) {
             super();
             this.distance = distance;
             this.string = string;
         }
         
         
         
         @Override
         public int compareTo(LevenshteinResult o) {
             return Integer.compare(this.distance, o.distance);
         }
     }
     
     
     
     /**
      * <p>Tries to find declarations that have a name similar to the given string. All
      * declarations in all parent namespaces, beginning at the given one are searched.
      * All declarations that have a levenshtein distance lower than 
      * {@link #LEVENSHTEIN_THRESHOLD_PERCENT} of the given string's length will be 
      * returned. If no such declaration was found, the resulting list will be empty.</p>
      * 
      * <p>The resulting list will be sorted by the distance. The declaration with the 
      * lowest distance to the given string will be on first place and so on.</p>
      * 
      * @param given The string to compare with all declarations.
      * @param nspace The root namespace to search. All parent namespaces are searched
      *          as well.
      * @return A collection of similar declaration names.
      * @see StringUtils
      */
     private final static List<String> findSimilar(String given, Namespace nspace) {
         final int threshold = 
             Math.max(1, (int) Math.round(given.length() * LEVENSHTEIN_THRESHOLD_PERCENT));
         final List<LevenshteinResult> results = 
                 new ArrayList<Namespace.LevenshteinResult>();
         
         for(Namespace space = nspace; space != null; space = space.parent) {
             final List<Declaration> decls = space.decls.get(given);
             if (decls == null) {
                 continue;
             }
             for (final Declaration decl : decls) {
                 
                 final String name = decl.getName().getId();
                 
                 // only take valid identifiers into account (this will skip operator
                 // declarations.
                 if (!Character.isJavaIdentifierPart(name.charAt(0))) {
                     continue;
                 }
                 
                 final int dist = StringUtils.getLevenshteinDistance(name, given);
                 if (dist <= threshold) {
                     results.add(new LevenshteinResult(dist, decl.getName().getId()));
                 }
             }
         }
         Collections.sort(results);
         final List<String> stringResults = new ArrayList<String>(results.size());
         for (final LevenshteinResult lr : results) {
             stringResults.add(lr.string);
         }
         return stringResults;
     }
     
     
     /**
      * This is the root namespace of all user namespaces. It contains all operator and
      * native function declarations.
      */
     private final static Namespace NATIVE = new Namespace(null) {
         
         @Override
         public void declare(Declaration decl) throws ASTTraversalException {
             if (!decl.isNative()) {
                 throw new IllegalArgumentException(
                     "NATIVE namespace must only contain native declarations.");
             }
             super.declare(decl);
         }
         
         
         
         @Override
         protected void delete(Iterator<Declaration> it) {
             throw new UnsupportedOperationException(
                 "can not delete from native namespace.");
         }
     };
     
     // initialize native declarations
     static {
         try {
             
             final Declaration pi = new Declaration(Position.NONE, 
                 new Identifier(Position.NONE, "pi"), 
                 new NumberLiteral(Position.NONE, Math.PI));
             pi.setNative(true);
             final Declaration e = new Declaration(Position.NONE, 
                 new Identifier(Position.NONE, "e"), 
                 new NumberLiteral(Position.NONE, Math.E));
             e.setNative(true);
             
             NATIVE.declare(pi);
             NATIVE.declare(e);
             
             // casting ops
             NATIVE.declare(new Cast(OpType.STRING, Type.STRING).createDeclaration());
             NATIVE.declare(new Cast(OpType.NUMBER, Type.NUM).createDeclaration());
             NATIVE.declare(new Cast(OpType.DATE, Type.DATE).createDeclaration());
             NATIVE.declare(new Cast(OpType.TIMESPAN, Type.TIMESPAN).createDeclaration());
             NATIVE.declare(new Cast(OpType.CHANNEL, Type.CHANNEL).createDeclaration());
             NATIVE.declare(new Cast(OpType.USER, Type.USER).createDeclaration());
             
             // relational ops
             NATIVE.declare(new Relational(OpType.EQ).createDeclaration());
             NATIVE.declare(new Relational(OpType.NEQ).createDeclaration());
             NATIVE.declare(new Relational(OpType.LT).createDeclaration());
             NATIVE.declare(new Relational(OpType.ELT).createDeclaration());
             NATIVE.declare(new Relational(OpType.GT).createDeclaration());
             NATIVE.declare(new Relational(OpType.EGT).createDeclaration());
             
             // Arithmetic binary ops
             NATIVE.declare(new BinaryArithmetic(OpType.ADD).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.SUB).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.MUL).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.DIV).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.INTDIV).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.INT_AND).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.INT_OR).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.MOD).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.POWER).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.LEFT_SHIFT).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.RIGHT_SHIFT).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.URIGHT_SHIFT).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.RADIX).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.MIN).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.MAX).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.XOR).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.ATAN2).createDeclaration());
             NATIVE.declare(new BinaryArithmetic(OpType.HYPOT).createDeclaration());
             
             // boolean arithmetic
             NATIVE.declare(new BinaryBooleanArithmetic(OpType.BOOLEAN_AND).createDeclaration());
             NATIVE.declare(new BinaryBooleanArithmetic(OpType.XOR).createDeclaration());
             NATIVE.declare(new BinaryBooleanArithmetic(OpType.BOOLEAN_OR).createDeclaration());
             
             // boolean unary
             NATIVE.declare(new UnaryBooleanArithmetic(OpType.EXCLAMATION).createDeclaration());
             
             // Arithmetic timespan and date binary ops
             NATIVE.declare(new TimespanArithmetic(OpType.ADD).createDeclaration());
             NATIVE.declare(new TimespanArithmetic(OpType.SUB).createDeclaration());
             NATIVE.declare(new DateTimespanArithmetic(OpType.ADD).createDeclaration());
             NATIVE.declare(new DateTimespanArithmetic(OpType.SUB).createDeclaration());
             NATIVE.declare(new DateArithmetic(OpType.SUB).createDeclaration());
             NATIVE.declare(new UnaryTimespan().createDeclaration());
             
             // String operators
             NATIVE.declare(new BinaryStringArithmetic(OpType.ADD).createDeclaration());
             NATIVE.declare(new StringIndex().createDeclaration());
             NATIVE.declare(new ReverseString().createDeclaration());
             
             // Arithmetic unary ops
             NATIVE.declare(new UnaryArithmetic(OpType.EXCLAMATION).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.SUB).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.LOG).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.LN).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.SQRT).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.CEIL).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.FLOOR).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.ROUND).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.SIG).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.COS).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.SIN).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.TAN).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.ACOS).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.ATAN).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.ASIN).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.ABS).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.TO_DEGREES).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.TO_RADIANS).createDeclaration());
             NATIVE.declare(new UnaryArithmetic(OpType.EXP).createDeclaration());
             
             // list unary op
             NATIVE.declare(new UnaryList(OpType.EXCLAMATION).createDeclaration());
             NATIVE.declare(new ListIndex(OpType.INDEX).createDeclaration());
             NATIVE.declare(new RandomListIndex(OpType.QUEST_EXCL).createDeclaration());
             NATIVE.declare(new RandomListIndex(OpType.QUESTION).createDeclaration());
             NATIVE.declare(new ReverseList().createDeclaration());
             
             // DOTDOT
             NATIVE.declare(new TernaryDotDot().createDeclaration());
             
             // ternary ops
             NATIVE.declare(new Conditional(OpType.IF).createDeclaration());
             
             // FUNCTIONS
             NATIVE.declare(new FoldLeft().createDeclaration());
             NATIVE.declare(new Id().createDeclaration());
             NATIVE.declare(new de.skuzzle.polly.core.parser.ast.lang.functions.Map().createDeclaration());
             NATIVE.declare(new Comp().createDeclaration());
             NATIVE.declare(new Sort().createDeclaration());
             NATIVE.declare(new Day().createDeclaration());
             
         } catch (ASTTraversalException e) {
             throw new ExceptionInInitializerError(e);
         }
     }
     
     
     
     /** Contains all user namespaces mapped to their user name */
     private final static Map<String, Namespace> ROOTS = new HashMap<String, Namespace>();
 
     
     /** Namespace for public declarations. */
     private final static Namespace PUBLIC = new StorableNamespace(PUBLIC_NAMESPACE_NAME + 
         ".decl", NATIVE);
     
     static {
         ROOTS.put(PUBLIC_NAMESPACE_NAME, PUBLIC);
     }
     
     
     /**
      * Declares a variable in PUBLIC namespace.
      * 
      * @param decl The declaration to add.
      * @throws ASTTraversalException If declaring fails.
      */
     public final static void declarePublic(Declaration decl) 
             throws ASTTraversalException {
         PUBLIC.declare(decl);
     }
     
     
     
     /**
      * Deletes a declaration from PUBLIC namespace.
      * 
      * @param id Name of the declaration to delete.
      * @return Number of deleted declarations.
      */
     public final static int deletePublic(Identifier id) {
         return PUBLIC.delete(id);
     }
     
     
     
     /**
      * Gets the toplevel namespace with the given name. If no namespace with that name
      * exists, it is created.
      * 
      * @param name The name of the namespace to retrieve.
      * @return The namespace.
      */
     public final static Namespace forName(String name) {
         Namespace check = ROOTS.get(name);
         if (check == null) {
             check = new StorableNamespace(name + ".decl", PUBLIC);
             ROOTS.put(name, check);
         }
         return check;
     }
     
     
     
     /**
      * Gets the toplevel namespace with the given name. If no namespace with that name
      * exists, it is created.
      * 
      * @param name The name of the namespace to retrieve.
      * @return The namespace.
      */
     public final static Namespace forName(Identifier name) {
         return forName(name.getId());
     }
     
     
     
     /**
      * Checks whether a root namespace with given name exists.
      *  
      * @param name name to check.
      * @return <code>true</code> if a namespace with that name exists.
      */
     public final static boolean exists(String name) {
         return ROOTS.containsKey(name);
     }
     
     
     
     /**
      * Checks whether a root namespace with given name exists.
      *  
      * @param name name to check.
      * @return <code>true</code> if a namespace with that name exists.
      */
     public final static boolean exists(Identifier name) {
         return exists(name.getId());
     }
     
     
 
     /** Declarations by name in this namespace */
     protected final Map<String, List<Declaration>> decls;
     
     /** Parent of this namespace. <code>null</code> for root namespace. */
     protected final Namespace parent;
     
     
     
     /**
      * Creates a new namespace which parent namespace is the given one.
      * 
      * @param parent Parent namespace.
      */
     public Namespace(Namespace parent) {
         this.parent = parent;
         this.decls = new HashMap<String, List<Declaration>>();
     }
     
     
     
     /**
      * Creates a new Namespace that contains the same declarations as this one. Each
      * parent namespace is copied as well. That means any changes made to the derived 
      * namespace are <b>not</b> reflected to the original space. This excludes 
      * modifications made to the stored declarations, as they are the same!
      * 
      * @return A new {@link Namespace}.
      */
     public Namespace derive() {
         if (this.parent == null) {
             return null;
         }
         final Namespace result = new Namespace(this.parent.derive());
         result.decls.putAll(this.decls);
         return result;
     }
     
     
     
     /**
      * Creates a new Namespace which contains the same declarations as this one. The
      * new namespace will have the given namespace as parent.
      * 
      * @param parent The parent namespace for the new namespace.
      * @return A new {@link Namespace}.
      */
     public Namespace derive(Namespace parent) {
         if (parent == this) {
             throw new IllegalArgumentException("namespace can not be its own parent");
         }
         final Namespace result = new Namespace(parent);
         result.decls.putAll(this.decls);
         return result;
     }
     
     
     
     /**
      * Creates a new sub namespace of this one and returns it.
      * 
      * @return The new namespace.
      */
     public Namespace enter() {
         final Namespace ns = new Namespace(this);
         return ns;
     }
     
     
     
     /**
      * Gets the parent namespace of this namespace. Returns <code>null</code> for the
      * root namespace.
      * 
      * @return The parent namespace.
      */
     public Namespace getParent() {
         return this.parent;
     }
 
     
     
     /**
      * Gets all declarations of the current level.
      * 
      * @return Map of declarations.
      */
     public Map<String, List<Declaration>> getDeclarations() {
         return this.decls;
     }
 
     
     
     /**
      * Declares a new variable in this namespace.
      * 
      * @param decl The function to declare.
      * @throws ASTTraversalException If a function with the same name and signature
      *          already exists in this namespace.
      */
     public void declare(Declaration decl) throws ASTTraversalException {
         /*if (decl.getType() == Type.UNKNOWN) {
             throw new IllegalStateException(
                 "cannot declare variable with unresolved type: " + decl);
         }*/
         
         List<Declaration> d = this.decls.get(decl.getName().getId());
         if (d == null) {
             d = new ArrayList<Declaration>();
             this.decls.put(decl.getName().getId(), d);
         }
         
         // overloading is only allowed for native declarations, so check if decl with that
         // name already exists
         final Iterator<Declaration> it = d.iterator();
         while (it.hasNext()) {
             final Declaration existing = it.next();
             
             if (!decl.isNative()) {
                 if (existing.getName().getId().equals(decl.getName().getId())) {
                     this.delete(it);
                 }
             } else {
                 // native declarations can be overloaded, but must have distinct types
                 /*if (Type.tryUnify(decl.getType(), existing.getType())) {
                     throw new ASTTraversalException(decl.getPosition(), 
                         "Deklaration mit identischem Typ existiert bereits.");
                 }*/
             }
         }
                 
         d.add(decl);
     }
     
     
     
     protected void delete(Iterator<Declaration> it) {
         it.remove();
     }
     
     
     
     /**
      * Removes all declarations in this namespace with the given name.
      * 
      * @param id Name of the declaration to delete.
      * @return How many declarations have been removed.
      */
     public int delete(Identifier id) {
         List<Declaration> decls = this.decls.get(id.getId());
         if (decls == null) {
             return 0;
         }
         
         final Iterator<Declaration> it = decls.iterator();
         int i = 0;
         while (it.hasNext()) {
             final Declaration d = it.next();
             
             if (!d.isNative()) {
                 this.delete(it);
                 ++i;
             }
         }
         return i;
     }
     
     
     
     /**
      * Resolves the first declaration with the given name that is found. This will search
      * all parent name spaces too and return when the first matching declaration was 
      * found.
      * 
      * @param name Name of declaration to resolved.
      * @return The resolved declaration.
      * @throws ASTTraversalException If declaration does not exist or multiple 
      *          declarations with that name exists in current level.
      */
     public Declaration resolveFirst(ResolvableIdentifier name) 
             throws ASTTraversalException {
         
         for (Namespace space = this; space != null; space = space.parent) {
             final List<Declaration> decls = space.decls.get(name.getId());
         
             if (decls == null || decls.isEmpty()) {
                 continue;
             } else if (decls.size() != 1) {
                 throw new ASTTraversalException(name.getPosition(), "Ambiguous");
             }
             return decls.get(0);
         }
         
         if (ParserProperties.should(ParserProperties.REPORT_UNKNOWN_VARIABLES)) {
             throw new ASTTraversalException(name.getPosition(), 
                     "Unbekannt: " + name.getId());
         } else {
             return new Declaration(name.getPosition(), name, 
                 new StringLiteral(name.getPosition(), name.getId()));
         }
     }
     
     
     
     /**
      * <p>Tries to resolve a declaration with the given name and type. If 
      * none was found, <code>null</code> is returned. This will return the first
      * matching declaration in this or any of the parent namespaces.</p>
      * 
      * <p>If a matching declaration was found, it will, before being returned, stored
      * in the given identifiers declaration attribute.</p>
      * 
      * @param name The name of the variable to resolve.
      * @param signature The signature of the variable to resolve.
      * @return The resolved declaration or <code>null</code> if non was found.
      */
     public Declaration tryResolve(ResolvableIdentifier name, Type signature) {
         for(Namespace space = this; space != null; space = space.parent) {
             final List<Declaration> decls = space.decls.get(name.getId());
             if (decls == null) {
                 continue;
             }
             for (Declaration decl : decls) {
                 if (Type.tryUnify(signature, decl.getType())) {
                     name.setDeclaration(decl);
                     return decl;
                 }
             }
         }
         if (ParserProperties.should(ParserProperties.REPORT_UNKNOWN_VARIABLES)) {
             return null;
         } else {
             final Declaration result = new Declaration(name.getPosition(), name, 
                 new StringLiteral(name.getPosition(), name.getId()));
             name.setDeclaration(result);
             return result;
         }
     }
     
     
     
     /**
      * Looks up all declarations with the given name in this and all parent namespaces.
      * 
      * @param name The name to lookup.
      * @return Collection of matching declarations.
      */
     public Collection<Declaration> lookupAll(ResolvableIdentifier name) {
         final List<Declaration> result = new ArrayList<Declaration>();
         
         for(Namespace space = this; space != null; space = space.parent) {
             final List<Declaration> decls = space.decls.get(name.getId());
             if (decls == null) {
                 continue;
             }
             result.addAll(decls);
         }
         return result;
     }
     
     
     
     
     /**
      * Looks up all possible declared types for the given identifier. Declarations on a 
      * higher level are ignored if a declaration with identical type is found on a lower
      * level. Additionally, this method replaces all type variables with fresh ones for 
      * all declarations that are no formal parameter. 
      * 
      * @param access VarAccess to resolve.
      * @param reporter Problem reporter to use when variable does not exist.
      * @return A set of all declarations with matching names.
      * @throws DeclarationException If no variable with matching name was found.
      * @throws ParseException May be thrown by problem reporter.
      */
     public Set<Type> lookupFresh(VarAccess access, ProblemReporter reporter) 
             throws DeclarationException, ParseException {
         final ResolvableIdentifier name = access.getIdentifier();
         final Set<Type> result = new HashSet<Type>();
         for(Namespace space = this; space != null; space = space.parent) {
             final List<Declaration> decls = space.decls.get(name.getId());
             if (decls == null) {
                 continue;
             }
             
             for (Declaration decl : decls) {
                 if (decl.getName().equals(name)) {
                     
                     // check if we already found a declaration with the same type on a 
                     // lower declaration level. If so, the current declaration will be
                     // ignored. That will have the effect that declarations on lower 
                     // levels override those on a higher level.
                     /*for (final Type alreadyFound : result) {
                         if (Type.tryUnify(alreadyFound, decl.getType())) {
                             // continue with next declaration and ignore this one
                             //continue ignore;
                         }
                     }*/
                     final Type fresh = decl.getType().subst(Substitution.fresh());
                     result.add(fresh);
                     name.addDeclaration(decl);
                     decl.onLookup(access);
                 }
             }
         }
         
         if (result.isEmpty()) {
             if (ParserProperties.should(ParserProperties.REPORT_UNKNOWN_VARIABLES)) {
                 reporter.semanticProblem(Problems.UNKNOWN_VAR, access.getPosition(), 
                     access.getIdentifier().getId());
             } else {
                 return Collections.singleton(Type.STRING);
             }
         }
         return result;
     }
     
     
     
     @Override
     public String toString() {
         final StringBuilder b = new StringBuilder();
         int level = 0;
         for(Namespace space = this; space != null; space = space.parent) {
             b.append("Level: ");
             b.append(level++);
             b.append("\n");
             
             for (final List<Declaration> decls : space.decls.values()) {
                 final List<Declaration> copy = new ArrayList<Declaration>(decls);
                 Collections.sort(copy);
                 
                 for (final Declaration decl : copy) {
                     b.append("    '");
                     b.append(decl.getName());
                     b.append("'");
                     b.append(" = ");
                     if (!decl.isNative()) {
                         b.append(Unparser.toString(decl.getExpression()));
                         b.append(" ");
                     }
                     b.append(decl.getType());
                     b.append("\n");
                 }
             }
         }
         return b.toString();
     }
 }
