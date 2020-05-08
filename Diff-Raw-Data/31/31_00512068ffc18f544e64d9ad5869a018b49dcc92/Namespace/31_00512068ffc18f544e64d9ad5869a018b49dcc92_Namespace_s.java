 package de.skuzzle.polly.parsing.ast.declarations;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 import de.skuzzle.polly.parsing.ast.Identifier;
 import de.skuzzle.polly.parsing.ast.ResolvableIdentifier;
 import de.skuzzle.polly.parsing.ast.expressions.Braced;
 import de.skuzzle.polly.parsing.ast.lang.Cast;
 import de.skuzzle.polly.parsing.ast.lang.Operator.OpType;
 import de.skuzzle.polly.parsing.ast.lang.functions.FoldLeft;
 import de.skuzzle.polly.parsing.ast.lang.operators.BinaryArithmetic;
 import de.skuzzle.polly.parsing.ast.lang.operators.BinaryDotDot;
 import de.skuzzle.polly.parsing.ast.lang.operators.Conditional;
 import de.skuzzle.polly.parsing.ast.lang.operators.DateArithmetic;
 import de.skuzzle.polly.parsing.ast.lang.operators.DateTimespanArithmetic;
 import de.skuzzle.polly.parsing.ast.lang.operators.ListIndex;
 import de.skuzzle.polly.parsing.ast.lang.operators.RandomListIndex;
 import de.skuzzle.polly.parsing.ast.lang.operators.Relational;
 import de.skuzzle.polly.parsing.ast.lang.operators.TernaryDotDot;
 import de.skuzzle.polly.parsing.ast.lang.operators.TimespanArithmetic;
 import de.skuzzle.polly.parsing.ast.lang.operators.UnaryArithmetic;
 import de.skuzzle.polly.parsing.ast.lang.operators.UnaryList;
 import de.skuzzle.polly.parsing.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.parsing.ast.visitor.Unparser;
 import de.skuzzle.polly.parsing.types.FunctionType;
 import de.skuzzle.polly.parsing.types.Type;
 import de.skuzzle.polly.tools.streams.CopyTool;
 import de.skuzzle.polly.tools.strings.StringUtils;
 
 
 public class Namespace {
     
     /** 
      * Percentage of word length that will be used as levenshtein thresold in
      * {@link #findSimilar(String, Namespace)}.
      */
     private final static float LEVENSHTEIN_THRESHOLD_PERCENT = 0.6f;
     
     
     public static File declarationFolder;
     public static synchronized void setDeclarationFolder(File declarationFolder) {
         Namespace.declarationFolder = declarationFolder;
     }
     
     
     
     
     
     
     private final static class StorableNamespace extends Namespace {
 
         private String fileName;
         
         
         
         public StorableNamespace(String fileName, Namespace parent) {
             super(parent);
             this.fileName = fileName;
         }
         
         
         
         @Override
         public synchronized void declare(Declaration decl) throws ASTTraversalException {
             super.declare(decl);
             if (!(decl instanceof VarDeclaration)) {
                 return;
             }
             final VarDeclaration vd = (VarDeclaration) decl;
             if (vd.isPublic() || vd.isTemp() || vd.isNative()) {
                 return;
             }
             
             try {
                 this.store();
             } catch (IOException ignore) {}
         }
         
         
         
         @Override
         public int delete(Identifier id) {
             final int i = super.delete(id);
             
             if (i != 0) {
                 try {
                     this.store();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             return i;
         }
         
         
         
         public void store() throws IOException {
             if (declarationFolder == null) {
                 throw new IOException("declaration folder has not been set");
             }
             
             PrintWriter pw = null;
             try {
                 pw = new PrintWriter(new File(declarationFolder, this.fileName));
                 
                 final Unparser up = new Unparser(pw);
                 for (final Declaration decl : this.decls) {
                     if (!(decl instanceof VarDeclaration)) {
                         continue;
                     }
                     final VarDeclaration vd = (VarDeclaration) decl;
                     new Braced(vd.getExpression()).visit(up);
                     pw.print("->");
                     pw.println(vd.getName().getId());
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
     
     
     
     
     
     
     
     private final static Namespace TYPES = new Namespace(null);
     static {
         try {
         TYPES.declare(new TypeDeclaration(Type.ANY.getTypeName(), Type.ANY));
         TYPES.declare(new TypeDeclaration(Type.BOOLEAN.getTypeName(), Type.BOOLEAN));
         TYPES.declare(new TypeDeclaration(Type.CHANNEL.getTypeName(), Type.CHANNEL));
         TYPES.declare(new TypeDeclaration(Type.STRING.getTypeName(), Type.STRING));
         TYPES.declare(new TypeDeclaration(Type.COMMAND.getTypeName(), Type.COMMAND));
         TYPES.declare(new TypeDeclaration(Type.DATE.getTypeName(), Type.DATE));
         TYPES.declare(new TypeDeclaration(Type.TIMESPAN.getTypeName(), Type.TIMESPAN));
         TYPES.declare(new TypeDeclaration(Type.HELP.getTypeName(), Type.HELP));
         TYPES.declare(new TypeDeclaration(Type.LIST.getTypeName(), Type.LIST));
         TYPES.declare(new TypeDeclaration(Type.USER.getTypeName(), Type.USER));
         TYPES.declare(new TypeDeclaration(Type.NUMBER.getTypeName(), Type.NUMBER));
         } catch (ASTTraversalException e) {
             throw new RuntimeException("impossibru", e);
         }
     }
     
     
     
     /**
      * Resolves a type declaration and stores the {@link TypeDeclaration} instance
      * in the {@link ResolvableIdentifier}'s declaration attribute.
      * 
      * @param name Name of the type to resolve. Refers to {@link Type#getTypeName()}.
      * @return The resolved {@link TypeDeclaration}
      * @throws DeclarationException If no such type exists.
      */
     public final static TypeDeclaration resolveType(ResolvableIdentifier name) 
             throws DeclarationException {
         final Declaration decl = TYPES.tryResolve(name);
         if (decl == null) {
             final List<String> similar = findSimilar(name.getId(), TYPES);
             
             throw new DeclarationException(name.getPosition(), 
                "Unbekannter Typ: '" + name + "'.", similar);
         } else if (!(decl instanceof TypeDeclaration)) {
             throw new IllegalStateException(
                 "Namespace 'TYPES' must only contain instances of TypeDeclaration.");
         }
         return (TypeDeclaration) decl;
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
             for (final Declaration decl : space.decls) {
                 
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
      * This is the root namespace of all user namespaces. It contains all operator
      * declarations as well as public variable and function declarations made by users.
      */
     private final static Namespace GLOBAL = new StorableNamespace("~global.decl", null);
     static {
         try {
             // casting ops
             GLOBAL.declare(new Cast(OpType.STRING, Type.STRING).createDeclaration());
             GLOBAL.declare(new Cast(OpType.NUMBER, Type.NUMBER).createDeclaration());
             
             // relational ops
             GLOBAL.declare(new Relational(OpType.EQ).createDeclaration());
             GLOBAL.declare(new Relational(OpType.NEQ).createDeclaration());
             GLOBAL.declare(new Relational(OpType.LT).createDeclaration());
             GLOBAL.declare(new Relational(OpType.ELT).createDeclaration());
             GLOBAL.declare(new Relational(OpType.GT).createDeclaration());
             GLOBAL.declare(new Relational(OpType.EGT).createDeclaration());
             
             // Arithmetic binary ops
             GLOBAL.declare(new BinaryArithmetic(OpType.ADD).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.SUB).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.MUL).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.DIV).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.INTDIV).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.INT_AND).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.INT_OR).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.MOD).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.POWER).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.LEFT_SHIFT).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.RIGHT_SHIFT).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.URIGHT_SHIFT).createDeclaration());
             GLOBAL.declare(new BinaryArithmetic(OpType.RADIX).createDeclaration());
             
             // Arithmetic timespan and date binary ops
             GLOBAL.declare(new TimespanArithmetic(OpType.ADD).createDeclaration());
             GLOBAL.declare(new TimespanArithmetic(OpType.SUB).createDeclaration());
             GLOBAL.declare(new DateTimespanArithmetic(OpType.ADD).createDeclaration());
             GLOBAL.declare(new DateTimespanArithmetic(OpType.SUB).createDeclaration());
             GLOBAL.declare(new DateArithmetic(OpType.SUB).createDeclaration());
             
             // Arithmetic unary ops
             GLOBAL.declare(new UnaryArithmetic(OpType.SUB).createDeclaration());
             
             // list unary op
             GLOBAL.declare(new UnaryList(OpType.EXCLAMATION).createDeclaration());
             GLOBAL.declare(new ListIndex(OpType.INDEX).createDeclaration());
             GLOBAL.declare(new RandomListIndex(OpType.QUEST_EXCL).createDeclaration());
             GLOBAL.declare(new RandomListIndex(OpType.QUESTION).createDeclaration());
             
             // DOTDOT
             GLOBAL.declare(new TernaryDotDot().createDeclaration());
             GLOBAL.declare(new BinaryDotDot().createDeclaration());
             
             // ternary ops
             GLOBAL.declare(new Conditional(OpType.IF).createDeclaration());
             
             
             // FUNCTIONS
             GLOBAL.declare(new FoldLeft().createDeclaration());
             GLOBAL.declare(new de.skuzzle.polly.parsing.ast.lang.functions.Map().createDeclaration());
         } catch (ASTTraversalException e) {
             e.printStackTrace();
         }
     }
     
     
     
     /** Contains all user namespaces mapped to their user name */
     private final static Map<String, Namespace> ROOTS = new HashMap<String, Namespace>();
 
     
     
     /**
      * Gets the toplevel namespace with the given name. If no namespace with that name
      * exists, it is created.
      * 
      * @param name The name of the namespace to retrieve.
      * @return The namespace.
      */
     public final static Namespace forName(String name) {
         if (name.equals("~global")) {
             return GLOBAL;
         }
         Namespace check = ROOTS.get(name);
         if (check == null) {
             check = new StorableNamespace(name + ".decl", GLOBAL);
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
     
     
 
     protected final Collection<Declaration> decls;
     protected final Namespace parent;
     protected boolean local;
     
     
     
     /**
      * Creates a new namespace which parent namespace is the given one.
      * 
      * @param parent Parent namespace.
      */
     public Namespace(Namespace parent) {
         this.parent = parent;
         this.decls = new ArrayList<Declaration>();
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
         result.decls.addAll(this.decls);
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
         final Namespace result = new Namespace(parent);
         result.decls.addAll(this.decls);
         return result;
     }
     
     
     
     /**
      * Creates a new sub namespace of this one and returns it.
      * 
      * @param local Whether the new namespace is used for local function parameters. 
      * @return The new namespace.
      */
     public Namespace enter(boolean local) {
         final Namespace ns = new Namespace(this);
         ns.local = local;
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
      * Declares a new variable in this namespace.
      * 
      * @param decl The function to declare.
      * @throws ASTTraversalException If a function with the same name and signature
      *          already exists in this namespace.
      */
     public void declare(Declaration decl) throws ASTTraversalException {
         final Collection<Declaration> decls = decl.isPublic() ? GLOBAL.decls : this.decls;
         // check if declaration exists in current namespace
         final Iterator<Declaration> it = decls.iterator();
         while (it.hasNext()) {
             final Declaration d = it.next();
             
             if (d.getName().equals(decl.getName())) {// && d.getType().check(decl.getType())) {
                 if (!(d.getType() instanceof FunctionType) && !(decl.getType() instanceof FunctionType) || d.getType().check(decl.getType())) {
                     if (!this.local) {
                         if (d.isNative()) {
                             throw new ASTTraversalException(decl.getPosition(), 
                                 "Du kannst keine nativen Deklarationen " +
                                 "berschreiben. Deklaration '" + d.getName() + 
                                 "' existiert bereits");
                         }
                         it.remove();
                     }
                 }
             }
         }
         decls.add(decl);
     }
     
     
     
     /**
      * Removes all declarations in this namespace with the given name.
      * 
      * @param id Name of the declaration to delete.
      * @return How many declarations have been removed.
      */
     public int delete(Identifier id) {
         final Iterator<Declaration> it = this.decls.iterator();
         int i = 0;
         while (it.hasNext()) {
             final Declaration d = it.next();
             
             if (d.getName().equals(id) && !d.isNative()) {
                 it.remove();
                 ++i;
             }
         }
         return i;
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
      * @param signature The signature of the variable to resolve. Use {@link Type#ANY} 
      *          if the signature should be ignored.
      * @return The resolved declaration or <code>null</code> if non was found.
      */
     public Declaration tryResolve(ResolvableIdentifier name, Type signature) {
         for(Namespace space = this; space != null; space = space.parent) {
             for (Declaration decl : space.decls) {
                 
                 if (decl.getName().equals(name) && decl.getType().check(signature)) {
                     if (decl.mustCopy()) {
                         decl = CopyTool.copyOf(decl);
                     }
                     name.setDeclaration(decl);
                     return decl;
                 }
             }
         }
         return null;
     }
     
     
     
     /**
      * Tries to resolve a declaration, disregarding the type of the declaration. This
      * will resolve the first matching declaration in this or any parent namespace. This 
      * method is equivalent to calling <code>tryResolve(name, Type.ANY)</code>.
      * 
      * @param name The name of the function to resolve.
      * @return The resolved declaration or <code>null</code> if non was found.
      * @see #tryResolve(ResolvableIdentifier, Type)
      */
     public Declaration tryResolve(ResolvableIdentifier name) {
         return this.tryResolve(name, Type.ANY);
     }
     
     
     
     public VarDeclaration resolveVar(ResolvableIdentifier name, Type signature) 
             throws ASTTraversalException {
         final Declaration check = this.tryResolve(name, signature);
         if (check == null && signature instanceof FunctionType) {
             final FunctionType ft = (FunctionType) signature;
             
             throw new ASTTraversalException(name.getPosition(), 
                 "Keine berladung von '" + name + "' mit der Signatur " + 
                     ft.toString(false) + " gefunden");
         } else if (check == null) {
             final List<String> similar = findSimilar(name.getId(), this);
             
             throw new DeclarationException(name.getPosition(), 
                "Unbekannte Variable: '" + name + "'.", similar);
         } else if (check instanceof VarDeclaration) {
             return (VarDeclaration) check;
         }
         throw new ASTTraversalException(name.getPosition(), name.getId() + 
             " ist keine Variable");
     }
     
     
     
     @Override
     public String toString() {
         final StringBuilder b = new StringBuilder();
         int level = 0;
         for(Namespace space = this; space != null; space = space.parent) {
             final List<Declaration> copy = new ArrayList<Declaration>(space.decls);
             Collections.sort(copy);
             
             b.append("Level: ");
             b.append(level++);
             b.append("\n");
             for (final Declaration decl : copy) {
                 b.append("    '");
                 b.append(decl.getName());
                 b.append("'");
                 if (decl instanceof VarDeclaration) {
                     final VarDeclaration vd = (VarDeclaration) decl;
                     b.append(" = ");
                     b.append(Unparser.toString(vd.getExpression()));
                 }
                 b.append(" [Type: ");
                 b.append(decl.getType());
                 b.append("]\n");
             }
         }
         return b.toString();
     }
 }
