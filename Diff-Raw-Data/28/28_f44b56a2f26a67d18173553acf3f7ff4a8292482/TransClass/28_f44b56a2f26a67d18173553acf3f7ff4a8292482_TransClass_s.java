 package de.unifr.acp.trafo;
 
 import static de.unifr.acp.trafo.TranslationHelper.filterClassesToTransform;
 import static de.unifr.acp.trafo.TranslationHelper.parameterCountOf;
 
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtBehavior;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMember;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.NotFoundException;
 import javassist.bytecode.SyntheticAttribute;
 import javassist.expr.Cast;
 import javassist.expr.ConstructorCall;
 import javassist.expr.ExprEditor;
 import javassist.expr.FieldAccess;
 import javassist.expr.Handler;
 import javassist.expr.Instanceof;
 import javassist.expr.MethodCall;
 import javassist.expr.NewArray;
 import javassist.expr.NewExpr;
 import de.unifr.acp.runtime.annot.Grant;
 import de.unifr.acp.runtime.nfa.NFA;
 import de.unifr.acp.runtime.nfa.NFARunner;
 
 // TODO: consider fully qualified field names
 
 public class TransClass {
     static {
         try {
             logger = Logger.getLogger("de.unifr.acp.trafo.TransClass");
             fh = new FileHandler("mylog.txt");
             TransClass.logger.addHandler(TransClass.fh);
             TransClass.logger.setLevel(Level.ALL);
         } catch (SecurityException | IOException e) {
             throw new RuntimeException(e);
         }
     }
     private static final String AUTOMATON_CLASS_NAME = NFA.class
             .getCanonicalName();
     private static final String RUNNER_CLASS_NAME = NFARunner.class
             .getCanonicalName();
     private static final String TRAVERSAL_TARGET_CLASS_NAME = de.unifr.acp.runtime.TraversalTarget__.class
             .getCanonicalName();
     private static final String VISITOR_CLASS_NAME = de.unifr.acp.runtime.TraversalImpl.class
             .getCanonicalName();
     private static final String GLOBAL_CLASS_NAME = de.unifr.acp.runtime.Global.class
             .getCanonicalName();
 
     private static Logger logger;
     private static FileHandler fh;
     private static final String FST_CACHE_FIELD_NAME = "$fstMap";
      private final CtClass objectClass =
      ClassPool.getDefault().get(Object.class.getName());
     // public final String FILTER_TRANSFORM_REGEX_DEFAULT =
     // "(java\\..*)|(de\\.unifr\\.acp\\.runtime\\..*)";
     public final String FILTER_TRANSFORM_REGEX_DEFAULT = "(java\\..*)";
     // public final String FILTER_TRANSFORM_REGEX_DEFAULT =
     // "(java\\..*)|(de\\.unifr\\.acp\\.(runtime|templates|contracts|util)\\..*)";
     private String filterTransformRegex = FILTER_TRANSFORM_REGEX_DEFAULT;
     // public final String FILTER_VISIT_REGEX_DEFAULT =
     // "(java\\..*)|(de\\.unif\\.acp\\.runtime\\..*)";
     public final String FILTER_VISIT_REGEX_DEFAULT = "(java\\..*)|(de\\.unifr\\.acp\\.(runtime|templates|contracts|util)\\..*)";
     private String filterVisitRegex = FILTER_VISIT_REGEX_DEFAULT;
     private ClassPool cp;
     private final boolean convertExceptions2Warnings;
     private Config config = null;
 
     // private final Map<CtClass, Boolean> visited = new HashMap<CtClass,
     // Boolean>();
     // private final HashSet<CtClass> visited = new HashSet<CtClass>();
     // private final HashSet<CtClass> transformed = new HashSet<CtClass>();
     // private final Queue<CtClass> pending;
 
     // protected Set<CtClass> getVisited() {
     // return Collections.unmodifiableSet(visited);
     // }
 
     // protected Collection<CtClass> getPending() {
     // return Collections.unmodifiableCollection(pending);
     // }
 
     /**
      * Transformer class capable of statically adding heap traversal code to
      * application classes. The instrumentation uses the specified class pool.
      * 
      * @param cp
      *            the class pool to use
      * @param classname
      *            the name of the class forming the starting point for the
      *            reachable classes
      * @param convertExceptions2Warnings
      *            the instrumentation prints warnings instead of throwing
      *            exceptions
      * @throws NotFoundException
      */
     protected TransClass(ClassPool cp, boolean convertExceptions2Warnings)
             throws NotFoundException {
         this.cp = cp;
         this.convertExceptions2Warnings = convertExceptions2Warnings;
     }
 
     /**
      * Transformer class capable of statically adding heap traversal code to
      * application classes. The instrumentation uses the default class pool.
      * 
      * @param classname
      *            the name of the class forming the starting point for the
      *            reachable classes
      * @param convertExceptions2Warnings
      *            the instrumentation prints warnings instead of throwing
      *            exceptions
      * @throws NotFoundException
      */
     protected TransClass(boolean convertExceptions2Warnings)
             throws NotFoundException {
         this(ClassPool.getDefault(), convertExceptions2Warnings);
     }
 
     /**
      * Convenience constructor. Calls TransClass(ClassPool.getDefault(), true).
      * 
      * @throws NotFoundException
      */
     protected TransClass() throws NotFoundException {
         this(ClassPool.getDefault(), true);
     }
 
     /**
      * Convenience constructor. Calls TransClass(cp, true).
      * 
      * @param cp
      *            the class pool to use
      * @throws NotFoundException
      */
     protected TransClass(ClassPool cp) throws NotFoundException {
         this(cp, true);
     }
 
     /**
      * Transforms all classes that are reachable from the class corresponding to
      * the specified class name.
      * 
      * @param cp
      *            the class pool to look up the class name in
      * @param className
      *            the class name of the class spanning a reachable classes tree.
      * @return the set of transformed classes.
      * @param convertExceptions2Warnings
      *            the instrumentation prints warnings instead of throwing
      *            exceptions
      * @throws ClassNotFoundException
      */
     public static Set<CtClass> transformReachableClasses(ClassPool cp,
             String className, boolean convertExceptions2Warnings)
             throws NotFoundException, IOException, CannotCompileException,
             ClassNotFoundException {
         TransClass tc = new TransClass(cp, convertExceptions2Warnings);
         Set<CtClass> reachable = tc.computeReachableClasses(cp.get(className),
                 false);
         Set<CtClass> transformed = tc.transformClasses(reachable);
         return transformed;
     }
 
     public static void defrostReachableClasses(ClassPool cp, String className)
             throws NotFoundException, IOException, CannotCompileException {
         TransClass tc = new TransClass(cp);
         Set<CtClass> reachable = tc.computeReachableClasses(cp.get(className),
                 true);
         System.out.println("Reachable classes:");
         System.out.println(reachable);
         for (CtClass clazz : reachable) {
             if (clazz.isFrozen() && !clazz.isArray())
                 clazz.defrost();
         }
     }
 
     /**
      * Transforms a single class of the specified class name. Superclasses are
      * not transformed. No filtering.
      * 
      * @param cp
      *            the class pool to look up the class name in
      * @param className
      *            the class name of the class to transform.
      * @return the set of transformed classes.
      * 
      * @throws ClassNotFoundException
      */
     public static void transformSingleClass(ClassPool cp, String className,
             boolean convertExceptions2Warnings) throws NotFoundException,
             ClassNotFoundException, IOException, CannotCompileException {
         TransClass tc = new TransClass(cp, convertExceptions2Warnings);
         CtClass target = cp.get(className);
         tc.transformClasses(Collections.singleton(target));
     }
 
     /**
      * Transforms all classes that are reachable from the class corresponding to
      * the specified class name and flushes the resulting classes to the
      * specified output directory.
      * 
      * @param cp
      *            the class pool to look up the class name in
      * 
      * @param className
      *            the class name of the class spanning a reachable classes tree
      * @param outputDir
      *            the relative output directory
      * @param convertExceptions2Warnings
      *            the instrumentation prints warnings instead of throwing
      *            exceptions
      * @throws ClassNotFoundException
      */
     public static void transformAndFlushHierarchy(ClassPool cp,
             String className, String outputDir,
             boolean convertExceptions2Warnings) throws NotFoundException,
             IOException, CannotCompileException, ClassNotFoundException {
         final Set<CtClass> transformed = transformReachableClasses(cp,
                 className, convertExceptions2Warnings);
         TransClass.flushClassesToDir(transformed, outputDir);
     }
 
     /*
      * Computes the set of reachable classes (types referred to in the constant
      * pool).
      */
     protected Set<CtClass> computeReachableClasses(CtClass root,
             boolean doDefrost) throws NotFoundException, IOException,
             CannotCompileException {
         final Queue<CtClass> pending = new LinkedList<CtClass>();
         final HashSet<CtClass> visited = new HashSet<CtClass>();
 
         pending.add(root);
         while (!pending.isEmpty()) {
             CtClass clazz = pending.remove();
             if (!visited.contains(clazz)) {
                 doTraverse(clazz, pending, visited, doDefrost);
             }
         }
         return visited;
     }
 
     /*
      * Helper method for <code>computeReachableClasses</code>. Traverses the
      * specified target class and adds it to the list of visited classes. Adds
      * all classes the class' fields to queue of pending classes, if not already
      * visited.
      */
     @Deprecated
     private void doTraverseOld(CtClass target, Queue<CtClass> pending,
             Set<CtClass> visited, boolean doDefrost) throws NotFoundException {
         visited.add(target);
 
         // collect all types this type refers to in this set
         final Set<CtClass> referredTypes = new HashSet<CtClass>();
         CtClass superclazz = target.getSuperclass();
         if (superclazz != null) {
             referredTypes.add(superclazz);
         }
 
         // for (CtClass clazz : target.getInterfaces()) {
         // referredTypes.add(clazz);
         // }
 
         CtField[] fs = target.getDeclaredFields();
         for (CtField f : fs) {
             CtClass ft = f.getType();
             if (ft.isPrimitive())
                 continue;
             referredTypes.add(ft);
         }
 
         if (target.isArray()) {
             CtClass componentType = target.getComponentType();
             referredTypes.add(componentType);
         }
 
         for (CtMethod method : target.getMethods()) {
             referredTypes.add(method.getReturnType());
         }
 
         for (CtBehavior methodOrCtor : target.getDeclaredBehaviors()) {
             List<CtClass> exceptionTypes = Arrays.asList(methodOrCtor
                     .getExceptionTypes());
             referredTypes.addAll(exceptionTypes);
             List<CtClass> paramTypes = Arrays.asList(methodOrCtor
                     .getParameterTypes());
             referredTypes.addAll(paramTypes);
 
             try {
                 final List<NotFoundException> notFoundexceptions = new ArrayList<NotFoundException>(
                         1);
                 if (!target.isArray() && doDefrost)
                     target.defrost();
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(NewExpr expr)
                             throws CannotCompileException {
                         try {
                             CtClass type = expr.getConstructor()
                                     .getDeclaringClass();
                             logger.finer("Reference to instantiated type "
                                     + type.getName() + " at "
                                     + expr.getFileName() + ":"
                                     + expr.getLineNumber());
                             referredTypes.add(type);
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(Instanceof expr)
                             throws CannotCompileException {
                         try {
                             CtClass type = expr.getType();
                             logger.finer("Reference to instanceof right-hand side type "
                                     + type.getName()
                                     + " at "
                                     + expr.getFileName()
                                     + ":"
                                     + expr.getLineNumber());
                             referredTypes.add(type);
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(NewArray expr)
                             throws CannotCompileException {
                         try {
                             CtClass type = expr.getComponentType();
                             logger.finer("Reference to array component type "
                                     + type.getName() + " at "
                                     + expr.getFileName() + ":"
                                     + expr.getLineNumber());
                             referredTypes.add(type);
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(MethodCall expr)
                             throws CannotCompileException {
                         try {
                             CtClass type = expr.getMethod().getDeclaringClass();
                             logger.finer("Reference to method-declaring type "
                                     + type.getName() + " at "
                                     + expr.getFileName() + ":"
                                     + expr.getLineNumber());
                             referredTypes.add(type);
 
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(Handler expr)
                             throws CannotCompileException {
                         try {
                             CtClass type = expr.getType();
                             logger.finer("Reference to handler type "
                                     + ((type != null) ? type.getName() : type)
                                     + " at " + expr.getFileName() + ":"
                                     + expr.getLineNumber());
                             // type can be null in case of synchronized blocks
                             // which are compiled to handler for type 'any'
                             if (type != null) {
                                 referredTypes.add(type);
                             }
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(FieldAccess expr)
                             throws CannotCompileException {
                         try {
                             CtClass type = expr.getField().getDeclaringClass();
                             logger.finer("Reference to field-declaring type "
                                     + type.getName() + " at "
                                     + expr.getFileName() + ":"
                                     + expr.getLineNumber());
                             referredTypes.add(type);
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(Cast expr) throws CannotCompileException {
                         try {
                             CtClass type = expr.getType();
                             logger.finer("Reference to cast target type "
                                     + type.getName() + " at "
                                     + expr.getFileName() + ":"
                                     + expr.getLineNumber());
                             referredTypes.add(type);
                         } catch (NotFoundException e) {
                             notFoundexceptions.add(e);
                         }
                     }
                 });
                 if (!notFoundexceptions.isEmpty()) {
                     throw notFoundexceptions.get(0);
                 }
             } catch (CannotCompileException e) {
                 // we do not compile and therefore expect no such exception
                 assert (false);
             }
         }
 
         // basic filtering of referred types
         for (CtClass type : referredTypes) {
             if (type.isPrimitive())
                 continue;
             if (type.getName().matches(filterVisitRegex))
                 continue;
             enter(type, pending, visited);
         }
     }
 
     /*
      * Helper method for <code>computeReachableClasses</code>. Traverses the
      * specified target class and adds it to the list of visited classes. Adds
      * all referred classes of the target class' fields to queue of pending
      * classes, if not already visited.
      * 
      * @param target the target class (class, interface, or array)
      * 
      * @param pending the queue of pending classes
      */
     private void doTraverse(CtClass target, Queue<CtClass> pending,
             Set<CtClass> visited, boolean doDefrost) throws NotFoundException {
         visited.add(target);
 
         // collect all types this type refers to in this set
         final Set<CtClass> referredTypes = new HashSet<CtClass>(1);
 
         if (target.isArray()) {
             CtClass componentType = target.getComponentType();
             if (!componentType.isPrimitive()) {
                 referredTypes.add(componentType);
             }
             // if (type.isPrimitive())
             // continue;
         } else {
 
             @SuppressWarnings({ "unchecked" })
             Set<String> classNames = target.getClassFile2().getConstPool()
                     .getClassNames();
             for (String className : classNames) {
                 referredTypes.add(cp.get(className.replace('/', '.')));
             }
         }
 
         // basic filtering of referred types
         for (CtClass type : referredTypes) {
             // if (type.isPrimitive())
             // continue;
             String typeName = type.getName();
             if (typeName.matches(filterVisitRegex))
                 continue;
             enter(type, pending, visited);
         }
     }
 
     /*
      * Helper method for <code>computeReachableClasses</code>. Adds the
      * specified class to queue of pending classes, if not already visited.
      */
     private void enter(CtClass clazz, Queue<CtClass> pending,
             Set<CtClass> visited) {
         logger.entering(TransClass.class.getSimpleName(), "enter",
                 (clazz != null) ? clazz.getName() : clazz);
         if (!visited.contains(clazz)) {
             pending.add(clazz);
         }
         logger.exiting("TransClass", "enter");
     }
 
     /**
      * Transforms the specified set of classes.
      * 
      * @param the
      *            set of classes to be transformed.
      * @return the transformed set of classes.
      */
     protected Set<CtClass> transformClasses(Set<CtClass> classes)
             throws NotFoundException, IOException, CannotCompileException,
             ClassNotFoundException {
         cp.importPackage("java.util");
 
         Set<CtClass> toTransform = filterClassesToTransform(classes,
                 filterTransformRegex);
 
         if (logger.isLoggable(Level.FINEST)) {
             StringBuilder sb = new StringBuilder();
             for (CtClass visitedClazz : toTransform) {
                 sb.append(visitedClazz.getName() + "\n");
             }
             logger.finest("Classes to transform:\n" + sb.toString());
         }
 
         final HashSet<CtClass> transformed = new HashSet<CtClass>();
         for (CtClass clazz : toTransform) {
             Deque<CtClass> stack = new ArrayDeque<CtClass>();
             CtClass current = clazz;
             do {
                 stack.push(current);
                 current = current.getSuperclass();
             } while (toTransform.contains(current));
             while (!stack.isEmpty()) {
                 CtClass superclass = stack.pop();
 
                 // if this does not hold we might miss some superclass fields
                 assert (transformed.contains(superclass.getSuperclass()) == toTransform
                         .contains(superclass.getSuperclass())) : toTransform
                         .contains(superclass.getSuperclass());
                 transformClass(superclass,
                         transformed.contains(superclass.getSuperclass()),
                         toTransform);
                 transformed.add(superclass);
             }
         }
         if (logger.isLoggable(Level.FINEST)) {
             StringBuilder sb = new StringBuilder();
             for (CtClass transformedClazz : transformed) {
                 sb.append(transformedClazz.getName() + "\n");
             }
             logger.finest("Transformed types:\n" + sb.toString());
         }
         return transformed;
     }
 
     private static Set<CtField> getStaticFields(Iterable<CtClass> classes) {
         Set<CtField> staticFields = new HashSet<>();
         for (CtClass clazz : classes) {
             CtField[] fields = clazz.getDeclaredFields();
             for (CtField field : fields) {
                 int modifiers = field.getModifiers();
                 if ((modifiers & javassist.Modifier.STATIC) != 0) {
                     staticFields.add(field);
                 }
             }
         }
         return staticFields;
     }
 
     /**
      * Returns the sublist of members of the specified list of members that
      * match one or more of the specified modifiers.
      * 
      * @param members
      *            the sublist of members that match one or more of the specified
      *            modifiers
      * @param modifiers
      *            the modifiers to match
      * @return the list of members matching one or more of the specified
      *         modifiers
      */
     private static <T extends CtMember> Set<T> membersMatchingOneModifer(
             Iterable<T> members, int modifiers) {
         Set<T> matchingMembers = new HashSet<>();
         for (T member : members) {
             if ((member.getModifiers() & modifiers) != 0) {
                 matchingMembers.add(member);
             }
         }
         return matchingMembers;
     }
 
     /**
      * Returns the sublist of members of the specified list of members that
      * match all of the specified modifiers. Sets do not work presumably due to
      * broken field equality.
      * 
      * @param members
      *            the sublist of members that match all of the specified
      *            modifiers
      * @param modifiers
      *            the modifiers to match
      * @return the list of members matching all of the specified modifiers
      */
     private static <T extends CtMember> Set<T> membersMatchingAllModifers(
             Iterable<T> members, int modifiers) {
         Set<T> matchingMembers = new HashSet<>();
         for (T member : members) {
             if ((member.getModifiers() & modifiers) == modifiers) {
                 matchingMembers.add(member);
             }
         }
         return matchingMembers;
     }
 
     /**
      * Returns the sublist of members of the specified list of members that do
      * not match all of the specified modifiers. NOT (AND_members) Sets do not
      * work presumably due to broken field equality.
      * 
      * @param members
      *            the sublist of members that do not match all of the specified
      *            modifiers
      * @param modifiers
      *            the modifiers to dismatch
      * @return the list of members not matching all of the specified modifiers
      * 
      */
     private static <T extends CtField> List<T> membersNotMatchingAllModifers(
             Iterable<T> members, int modifiers) {
         List<T> matchingMembers = new ArrayList<>();
         for (T member : members) {
             int memberModifiers = member.getModifiers();
             if (((memberModifiers & modifiers) != modifiers)) {
                 matchingMembers.add(member);
             }
         }
         return matchingMembers;
     }
 
     /**
      * Returns the sublist of members of the specified list of members that
      * match none of the specified modifiers. NOT (OR_members) Sets do not work
      * presumably due to broken field equality.
      * 
      * @param members
      *            the sublist of members that match none of the specified
      *            modifiers
      * @param modifiers
      *            the modifiers to match
      * @return the list of members matching none of the specified modifiers
      */
     private static <T extends CtField> List<T> membersMatchingNoModifer(
             Iterable<T> members, int modifiers) {
         List<T> matchingMembers = new ArrayList<>();
         for (T member : members) {
             if ((member.getModifiers() & modifiers) == 0) {
                 matchingMembers.add(member);
             }
         }
         return matchingMembers;
     }
 
     /**
      * Flushes the specified set of classes back to disk.
      * 
      * @param a
      *            set of classes
      * @param outputDir
      *            the relative output directory
      */
     protected static void flushClassesToDir(Set<CtClass> classes,
             String outputDir) throws NotFoundException, IOException,
             CannotCompileException {
         for (CtClass tc : classes) {
             if (!tc.isArray()) {
                 tc.writeFile(outputDir);
                 for (CtClass inner : tc.getNestedClasses()) {
                     inner.writeFile(outputDir);
                 }
             }
         }
         // String[] libClassNames = {
         // "de.unifr.acp.runtime.TraversalTarget__",
         // "de.unifr.acp.runtime.TraversalImpl",
         // "de.unifr.acp.runtime.Traversal__",
         // "de.unifr.acp.runtime.Global", "de.unifr.acp.runtime.fst.Permission"
         // };
         // CtClass[] libClasses = this.cp.get(libClassNames);
         // for (CtClass libClass : libClasses) {
         // libClass.writeFile(outputDir);
         // }
     }
 
     /**
      * Transforms a single class if not already done.
      * 
      * @param target
      *            the class to transform
      * @param hasTransformedSuperclass
      *            the target has an already transformed superclass
      * @param toTransform
      *            all classes to be transformed
      * @throws NotFoundException
      * @throws IOException
      * @throws CannotCompileException
      * @throws ClassNotFoundException
      */
     public void transformClass(CtClass target,
             boolean hasTransformedSuperclass, Set<CtClass> toTransform)
             throws NotFoundException, IOException, CannotCompileException,
             ClassNotFoundException {
         Object[] params4Logging = new Object[2];
         params4Logging[0] = (target != null) ? target.getName() : target;
         params4Logging[1] = hasTransformedSuperclass;
         logger.entering("TransClass", "doTransform", params4Logging);
         if (target.isArray() || target.isInterface()) {
             return;
         }
         this.cp.importPackage("java.util");
 
         // add: implements TRAVERSALTARGET
         List<CtClass> targetIfs = Arrays.asList(target.getInterfaces());
 
         // NOTE: Given the equality semantics of CtClass the condition is only
         // valid if the CtClass instance representing the traversal interface is
         // never detached from the ClassPool as this would result in a new
         // CtClass instance (representing the traversal interface) to be
         // generated
         // by a call to ClassPool.get(...) not being equal to the old instance.
 
         // only generate implementation of traversal interface if not yet
         // present
         // use traversal interface as marker for availability of other
         // instrumentation
         CtClass traversalTargetIf = this.cp.get(TRAVERSAL_TARGET_CLASS_NAME);
         if (!targetIfs.contains(traversalTargetIf)) {
 
             // change methods carrying contracts
             // 1. Find all methods carrying contracts
             // 2. For each annotated method
             // 1. Get method annotation
             // 2. Generate code to generate automaton for contract
             // (generate code to get contract string and call into automation
             // generation library)
             // 3. Use insertBefore() and insertAfter() to insert permission
             // installation/deinstallation code
 
             // according to tutorial there's no support for generics in
             // Javassist, thus we use raw types
             CtField f = CtField.make("private static java.util.HashMap "
                     + FST_CACHE_FIELD_NAME + " = new java.util.HashMap();",
                     target);
             target.addField(f);
 
             // List<CtBehavior> methodsAndCtors = Arrays.asList(target
             // .getDeclaredBehaviors());
 
             // instrument constructors
             for (CtConstructor ctor : target.getDeclaredConstructors()) {
                 instrumentNew(ctor);
             }
 
             // instrument methods and constructors declared in this very class
             for (CtBehavior methodOrCtor : target.getDeclaredBehaviors()) {
                 instrumentNewArray(methodOrCtor);
 
                 instrumentFieldAccess(methodOrCtor);
             }
 
             // instrument methods
             for (CtBehavior methodOrCtor : target.getDeclaredBehaviors()) {
                 instrumentMethodOrCtor(methodOrCtor, toTransform);
             }
 
             // add traversal target interface
             addInterface(target, traversalTargetIf);
 
             // add: method traverse__
             // TODO: create body before adding new technically required fields)
             String traverseBody = createTraverseBody(target,
                     hasTransformedSuperclass);
             target.addMethod(CtNewMethod.make(traverseBody, target));
             String traverseStaticsBody = createTraverseStaticsBody(target,
                     hasTransformedSuperclass);
             target.addMethod(CtNewMethod.make(traverseStaticsBody, target));
         }
         logger.exiting("TransClass", "doTransform");
     }
 
     private void instrumentMethodOrCtor(final CtBehavior methodOrCtor,
             Set<CtClass> toTransform) throws ClassNotFoundException,
             NotFoundException, CannotCompileException {
         logger.fine("Consider adding traversal to behavior: "
                 + methodOrCtor.getLongName());
         if ((methodOrCtor.getModifiers() & Modifier.ABSTRACT) != 0) {
             return;
         }
 
         // instrumentFieldAccess(methodOrCtor);
 
         if (hasMethodGrantAnnotations(methodOrCtor)) {
 
             // uniquely identifies a method globally
             final String longName = methodOrCtor.getLongName();
 
             logger.fine("Add traversal to behavior: " + longName);
 
             /* generate header and footer */
 
             // filter synthetic methods
             if (methodOrCtor.getMethodInfo().getAttribute(
                     SyntheticAttribute.tag) != null) {
                 return;
             }
 
             // generate method header
 
             /*
              * We keep method contract and parameter contracts separate.
              */
 
             StringBuilder sb = new StringBuilder();
 
             // check if automata for this method already exist
 
             // FSTs indexed by parameter position (0: FST for this &
             // type-anchored
             // contracts, 1 to n: FTSs for implicitly anchored parameter
             // contracts)
 
             // alternatively generate one automata cache field per method
             sb.append(AUTOMATON_CLASS_NAME + "[] automata;");
             sb.append("  automata = ((" + AUTOMATON_CLASS_NAME + "[])"
                     + FST_CACHE_FIELD_NAME + ".get(\"" + longName + "\"));");
 
             sb.append("if (automata == null) {");
 
             // build array of FSTs indexed by parameter
             sb.append("  automata = new " + AUTOMATON_CLASS_NAME + "["
                     + (parameterCountOf(methodOrCtor) + 1) + "];");
             for (int i = 0; i < parameterCountOf(methodOrCtor) + 1; i++) {
                 Grant grant = grantAnno(methodOrCtor, i);
                 if (grant != null) {
                     sb.append("    automata[" + i + "] = new "
                             + AUTOMATON_CLASS_NAME + "(\"" + grant.value()
                             + "\");");
                 }
             }
 
             // cache generated automata indexed by long method name
             sb.append("  " + FST_CACHE_FIELD_NAME + ".put(\"" + longName
                     + "\", automata);");
             sb.append("}");
 
             // now we expect to have all FSTs available and cached
 
             sb.append("  Map allLocPerms = new de.unifr.acp.runtime.util.WeakIdentityHashMap();");
 
             final boolean isMethodOrCtorStatic = isStatic(methodOrCtor);
 
             // 0 represents this, 1 to n the n parameters
             int i = (isMethodOrCtorStatic ? 1 : 0);
             int limit = parameterCountOf(methodOrCtor) + 1;
             for (; i < limit; i++) {
 
                 // only grant-annotated methods/parameters require any
                 // action
                 if (grantAnno(methodOrCtor, i) == null) {
                     continue;
                 }
 
                 if (!mightBeReferenceParameter(methodOrCtor, i)) {
                     continue;
                 }
 
                 // TODO: factor out this code in external class,
                 // parameterize over i and allPermissions
                 // a location permission is a Map<Object, Map<String,
                 // Permission>>
                 sb.append("{");
                 // sb.append("System.out.println(\"start of traversal ...\");");
                 // sb.append("  " + AUTOMATON_CLASS_NAME
                 // + " nfa = automata[" + i + "];");
                 // sb.append("System.out.println(\"got FST ...\");");
                 sb.append("  " + RUNNER_CLASS_NAME + " runner = new "
                         + RUNNER_CLASS_NAME + "(automata[" + i + "]);");
                 // sb.append("System.out.println(\"got runner ...\");");
 
                 // step to reach FST runner state that corresponds to
                 // anchor object
                 // for explicitly anchored contracts
                 if (i == 0) {
                     sb.append("  runner.step(\"this\");");
                     // sb.append("System.out.println(\"after reset of runner ...\");");
                 }
 
                 // here the runner should be in synch with the parameter
                 // object
                 // (as far as non-static fields are concerned), the
                 // visitor implicitly joins locPerms
 
                 // parameter types indexed by local variable index
                 CtClass[] paramTypes = methodOrCtor.getParameterTypes();
                 if (true/* i == 0 || !paramTypes[i - 1].isArray() */) {
                     // sb.append("System.out.println(\"found traversal target ...\");");
                     sb.append("  " + VISITOR_CLASS_NAME + " visitor = new "
                             + VISITOR_CLASS_NAME + "(runner, allLocPerms);");
                     // sb.append("System.out.println(\"got visitor ...\");");
                     sb.append("  try {");
                     sb.append("    ((" + TRAVERSAL_TARGET_CLASS_NAME + ")$" + i
                             + ").traverse__(visitor);");
                     sb.append("  } catch(java.lang.ClassCastException e) {}");
                     // sb.append("System.out.println(\"traversal ...\");");
                 }
 
                 if (i == 0) {
                     for (CtClass clazz : toTransform) {
                         sb.append("  runner.resetAndStep(\""
                                 + clazz.getSimpleName() + "\");");
                         sb.append("  " + VISITOR_CLASS_NAME
                                 + " visitorStatics = new " + VISITOR_CLASS_NAME
                                 + "(runner, allLocPerms);");
                         sb.append("  try {");
                         sb.append("    ((" + TRAVERSAL_TARGET_CLASS_NAME + ")$"
                                 + i + ").traverseStatics__(visitorStatics);");
                         sb.append("  } catch(java.lang.ClassCastException e) {}");
                     }
                 }
                 // TODO: explicit representation of locations and
                 // location permissions (supporting join)
                 // (currently it's all generic maps and implicit joins
                 // in visitor similar to Maxine implementation)
                 // sb.append("System.out.println(\"end of traversal ...\");");
                 sb.append("}");
             }
 
             // install allLocPerms and push new objects set on (current
             // thread's) stack
             // sb.append("System.out.println(\"locPermStack: \"+de.unifr.acp.runtime.Global.locPermStack);");
             // sb.append("System.out.println(\"locPermStack.peek(): \"+de.unifr.acp.runtime.Global.locPermStack.peek());");
             // sb.append("System.out.println(\"before push ...\");");
             sb.append(GLOBAL_CLASS_NAME + ".installPermissions(allLocPerms);");
             // sb.append("de.unifr.acp.runtime.Global.newObjectsStack.push(Collections.newSetFromMap(new de.unifr.acp.util.WeakIdentityHashMap()));");
             // sb.append("System.out.println(\"Push in "
             // + methodOrCtor.getLongName() + "\");");
 
             // TODO: figure out how to instrument thread start/end and
             // field access
 
             final String header = sb.toString();
 
             if (methodOrCtor instanceof CtConstructor) {
                 // insert contract installation instrumentation
                 // in constructor before existing new instrumentation
                 // thus accesses to constructor fields are allowed
                 ((CtConstructor) methodOrCtor).insertBeforeBody(header);
             } else {
                 methodOrCtor.insertBefore(header);
             }
 
             // generate method footer
             sb = new StringBuilder();
 
             // pop location permissions and new locations entry from
             // (current thread's) stack
             // sb.append("System.out.println(\"locPermStack: \"+de.unifr.acp.runtime.Global.locPermStack);");
             // sb.append("System.out.println(\"locPermStack.peek(): \"+de.unifr.acp.runtime.Global.locPermStack.peek());");
             // sb.append("System.out.println(\"Pop in "
             // + methodOrCtor.getLongName() + "\");");
             sb.append(GLOBAL_CLASS_NAME + ".uninstallPermissions();");
             String footer = sb.toString();
 
             // make sure all method exits are covered (exceptions,
             // multiple returns)
             methodOrCtor.insertAfter(footer, true);
 
         } // end if (hasMethodGrantAnnotations(methodOrCtor))
     }
 
     private void instrumentFieldAccess(final CtBehavior methodOrCtor)
             throws CannotCompileException {
         // if (!isInstrumented.get()) {
         // methodOrCtor.getMethodInfo2().
         if ((methodOrCtor.getModifiers() & Modifier.ABSTRACT) != 0) {
             return;
         }
         methodOrCtor.instrument(new ExprEditor() {
             public void edit(FieldAccess expr) throws CannotCompileException {
                 if (true/* !expr.isStatic() */) {
                     try {
                         CtField field = expr.getField();
 
                         // String qualifiedFieldName = expr.getClassName() + "."
                         // + expr.getFieldName();
                         String qualifiedFieldName = field.getDeclaringClass()
                                 .getName() + "." + field.getName();
 
                         // exclude standard API (to be factored out)
                         if (!(qualifiedFieldName.startsWith("java") || qualifiedFieldName
                                 .startsWith("javax"))) {
                             StringBuilder code = new StringBuilder();
                             code.append("{");
 
                             // get active permission for location to access
                             code.append("if (!" + GLOBAL_CLASS_NAME
                                     + ".newObjectsStack.isEmpty()) {");
 
                             code.append("de.unifr.acp.runtime.fst.Permission effectivePerm = "
                                     + GLOBAL_CLASS_NAME
                                     + ".effectivePermissionStackNotEmpty("
                                     + (!expr.isStatic() ? "$0" : "null")
                                     + ", \"" + qualifiedFieldName + "\");");
 
                             // get permission needed for this access
                             code.append("de.unifr.acp.runtime.fst.Permission accessPerm = de.unifr.acp.runtime.fst.Permission."
                                     + (expr.isReader() ? "READ_ONLY"
                                             : "WRITE_ONLY") + ";");
                             // code.append("de.unifr.acp.runtime.fst.Permission accessPerm = de.unifr.acp.runtime.fst.Permission.values()["
                             // + (expr.isReader() ? "1" : "2")
                             // + "];");
 
                             // code.append("if (!effectivePerm.containsAll(accessPerm)) {");
                             code.append("if (!de.unifr.acp.runtime.fst.Permission.containsAll(effectivePerm, accessPerm)) {");
                             code.append("  " + GLOBAL_CLASS_NAME
                                     + ".throwOrPrintViolation($0, \""
                                     + qualifiedFieldName + "\", \""
                                     + methodOrCtor.getLongName()
                                     + "\",effectivePerm, accessPerm,"
                                     + convertExceptions2Warnings + ");");
                             code.append("}");
                             code.append("}");
 
                             if (expr.isReader()) {
                                 code.append("  $_ = $proceed();");
                             } else {
                                 code.append("  $proceed($$);");
                             }
                             code.append("}");
 
                             expr.replace(code.toString());
                         }
                     } catch (NotFoundException e) {
                         throw new CannotCompileException(e);
                     }
                 }
             }
         });
         // }
     }
 
     private static CtField resolveFieldFromAccess(FieldAccess expr)
             throws NotFoundException {
         return expr.getField();
     }
 
     /*
      * Instruments constructors to such that the constructed object is added to
      * the new objects stack's top entry.
      */
     private static void instrumentNew(CtConstructor ctor)
             throws CannotCompileException {
 
         // Apparently Javassist does not support instrumentation between new
         // bytecode and constructor using the expression editor on new
         // expressions (in AspectJ this might work using Initialization Pointcut
         // Designators). Hence, we go for instrumenting the constructor, but
         // we need to make sure that the object is a valid by the time we
         // add it to the new objects (after this() or super() call).
 
         ctor.instrument(new ExprEditor() {
             public void edit(ConstructorCall expr)
                     throws CannotCompileException {
                 StringBuilder code = new StringBuilder();
 
                 // TODO: could be done with insertBeforeBody
                 // TODO: instrumentation only needed if super() call?!
                 code.append("{");
                 code.append("  $_ = $proceed($$);");
                 code.append("  " + GLOBAL_CLASS_NAME + ".addNewObject($0);");
                 code.append("}");
                 expr.replace(code.toString());
             }
         });
 
     }
 
     /*
      * Instrument array creation such that the new array is added to the new
      * objects stack's top entry.
      */
     private static void instrumentNewArray(CtBehavior methodOrCtor)
             throws CannotCompileException {
         methodOrCtor.instrument(new ExprEditor() {
             public void edit(NewArray expr) throws CannotCompileException {
                 StringBuilder code = new StringBuilder();
                 code.append("{");
                 code.append("  $_ = $proceed($$);");
                 code.append("  " + GLOBAL_CLASS_NAME + ".addNewObject($_);");
                 code.append("}");
 
                 expr.replace(code.toString());
             }
         });
     }
 
     private static boolean isStatic(CtMember member) {
         return ((member.getModifiers() & Modifier.STATIC) != 0);
     }
 
     private static void addInterface(CtClass clazz, CtClass interf)
             throws NotFoundException {
         List<CtClass> clazzIfs = Arrays.asList(clazz.getInterfaces());
         HashSet<CtClass> newClazzIfs = new HashSet<CtClass>(clazzIfs);
         newClazzIfs.add(interf);
         clazz.setInterfaces(newClazzIfs.toArray(new CtClass[0]));
     }
 
     /**
      * Currently is an under-approximation (might return false for primitive
      * parameters)
      * 
      * @param methodOrCtor
      * @param index
      *            the parameter index
      * @return false if non-primitive or primitive, true if primitive
      */
     private static boolean mightBeReferenceParameter(CtBehavior methodOrCtor,
             int index) {
         if (index == 0) {
             return true;
         } else {
             try {
                 return !(methodOrCtor.getParameterTypes()[index - 1]
                         .isPrimitive());
             } catch (NotFoundException e) {
                 // TODO: fix this
                 return true;
             }
         }
     }
 
     /**
      * Return the grant annotation for the specified parameter (1 to n) or
      * behavior (0) if available.
      * 
      * @param methodOrCtor
      *            the behavior
      * @param index
      *            1 to n refers to a parameter index, 0 refers to the behavior
      *            itself
      * @return the grant annotation if available, <code>null</code> otherwise
      * @throws ClassNotFoundException
      */
     private static Grant grantAnno(CtBehavior methodOrCtor, int index)
             throws ClassNotFoundException {
         if (index == 0) {
             Grant methodGrantAnnot = ((Grant) methodOrCtor
                     .getAnnotation(Grant.class));
             return methodGrantAnnot;
         } else {
 
             // optional parameter types annotations indexed by parameter
             // position
             Object[][] availParamAnnot = methodOrCtor
                     .getAvailableParameterAnnotations();
 
             final Object[] oa = availParamAnnot[index - 1];
             for (Object o : oa) {
                 if (o instanceof Grant) {
                     // there's one grant annotation per parameter only
                     return (Grant) o;
                 }
             }
             return null;
         }
     }
 
     /**
      * Returns true if the specified method/constructor or one of its parameters
      * has a Grant annotation.
      * 
      * @param methodOrCtor
      *            the method/constructor to check
      * @return true if Grant annotation exists, false otherwise.
      * @throws NotFoundException
      * @throws CannotCompileException
      */
     private static boolean hasMethodGrantAnnotations(CtBehavior methodOrCtor)
             throws NotFoundException, CannotCompileException {
         if (methodOrCtor.hasAnnotation(Grant.class))
             return true;
         CtClass[] parameterTypes = methodOrCtor.getParameterTypes();
         int i = 0;
         for (Object[] oa : methodOrCtor.getAvailableParameterAnnotations()) {
             CtClass paramType = parameterTypes[i++];
 
             // we can savely ignore grant annotations on primitive formal
             // parameters
             if (!paramType.isPrimitive()) {
                 for (Object o : oa) {
                     if (o instanceof Grant) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     protected static String createTraverseBody(CtClass target,
             boolean hasTransformedSuperclass) throws NotFoundException {
         StringBuilder sb = new StringBuilder();
         sb.append("public void traverse__(de.unifr.acp.runtime.Traversal__ t) {\n");
         for (CtField f : membersMatchingNoModifer(
                 Arrays.asList(target.getDeclaredFields()),
                 javassist.Modifier.STATIC)) {
             CtClass tf = f.getType();
             String fname = f.getName();
             if (!fname.equals(FST_CACHE_FIELD_NAME)/*
                                                     * && !f.getType ( ).isArray(
                                                     * )
                                                     */) {
                 // sb.append("System.out.println(\"Before visitor calls for field "+fname
                 // +"\");");
                 appendVisitorCalls(sb, target, tf, fname, false);
                 // sb.append("System.out.println(\"After visitor calls for field "+fname
                 // +"\");");
             }
         }
         if (hasTransformedSuperclass) {
             sb.append("super.traverse__(t);\n");
         }
         sb.append('}');
         return sb.toString();
     }
 
     protected static String createTraverseStaticsBody(CtClass target,
             boolean hasTransformedSuperclass) throws NotFoundException {
         StringBuilder sb = new StringBuilder();
         sb.append("public void traverseStatics__(de.unifr.acp.runtime.Traversal__ t) {\n");
         for (CtField f : membersMatchingAllModifers(
                 Arrays.asList(target.getDeclaredFields()),
                 javassist.Modifier.STATIC)) {// getStaticFields(Collections.singleton(target)))
                                              // {
             CtClass tf = f.getType();
             String fname = f.getName();
             if (!fname.equals(FST_CACHE_FIELD_NAME)/* && !f.getType().isArray() */) {
                 // sb.append("System.out.println(\"Before visitor calls for field "+fname
                 // +"\");");
                 appendVisitorCalls(sb, target, tf, fname, true);
                 // sb.append("System.out.println(\"After visitor calls for field "+fname
                 // +"\");");
             }
         }
         sb.append('}');
         return sb.toString();
     }
 
     protected static void appendVisitorCalls(StringBuilder sb, CtClass target,
             CtClass tf, String fname, boolean isStatic)
             throws NotFoundException {
         // int nesting = 0;
         // String index = "";
 
         // final boolean isArray = tf.isArray();
         final CtClass innerComponentType = innerComponentTypeOf(tf);
         final boolean isNonPrimitiveArray = tf.isArray()
                && !innerComponentType.isPrimitive();
 
         // // we might care about the reference values during traversal
         // if (!innerComponentType.isPrimitive()) {
         // // some array might not be traversed by this code as every Object's
         // // dynamic type could be an array type
         // while (tf.isArray()) {
         // String var = "i" + nesting;
         //
         // /* generate for header */
         // sb.append("for (int " + var + " = 0; ");
         //
         // // static type of implicit 'this' corresponds to field's
         // // declaring class,
         // // no cast needed, static fields can be accessed using the same
         // // syntax
         // sb.append(var + "<((" + /* (isStatic ? "" : "this.") + */fname
         // + index + " != null) ? " + /*
         // * (isStatic ? "" : "this.")
         // * +
         // */fname + index
         // + ".length : 0); ");
         // sb.append(var + "++");
         // sb.append(")\n");
         // index = index + "[" + var + "]";
         // nesting++;
         // tf = tf.getComponentType();
         // }
         // } else {
         // tf = innerComponentType;
         // }
         sb.append("{\n");
         // sb.append("System.out.println(\"Visit field (element) " + fname
         // + index.replace("[", "[\"+").replace("]", "+\"]") + "\");\n");
         // if (!tf.isPrimitive()) {
         // sb.append("System.out.println(\"\"+System.identityHashCode(this."
         // + fname + index + "));");
         // }
 
         if (tf.isPrimitive()) {
             sb.append("t.visitPrimitiveField__(");
         } else if (isNonPrimitiveArray) {
             sb.append("t.visitArrayField__(");
         } else if (tf.equals(ClassPool.getDefault().get(Object.class.getName()))) { 
             sb.append("t.visitPotentialRefArrayField__(");
         } else {
             sb.append("t.visitField__(");
         }
         sb.append((isStatic ? "null" : "this") + ", ");
         sb.append('"');
         sb.append(target.getName());
         sb.append('.');
         sb.append(fname);
         sb.append('"');
         if (!tf.isPrimitive()) {
             // static type of 'this' corresponds to field's declaring class, no
             // cast needed
             sb.append(", "/* + (isStatic ? "" : "this.") */);
             sb.append(fname/* + index */);
         }
         sb.append(");\n");
         sb.append("}\n");
     }
 
     /**
      * Returns innermost component type.
      * 
      * @param clazz
      *            the type to return the innermost component type for
      * @return the innermost component type if the specified type is an array,
      *         the specified type itself otherwise
      * @throws NotFoundException
      */
     private static CtClass innerComponentTypeOf(CtClass clazz)
             throws NotFoundException {
         CtClass component = clazz;
         while (component.isArray()) {
             component = component.getComponentType();
         }
         return component;
     }
 
     @Deprecated
     private static boolean isClassNonPrimitiveArray(CtClass clazz)
             throws NotFoundException {
         CtClass component = clazz;
         if (!clazz.isArray()) {
             return false;
         } else {
             do {
                 component = component.getComponentType();
             } while (component.isArray());
             return !component.isPrimitive();
         }
     }
 
 }
