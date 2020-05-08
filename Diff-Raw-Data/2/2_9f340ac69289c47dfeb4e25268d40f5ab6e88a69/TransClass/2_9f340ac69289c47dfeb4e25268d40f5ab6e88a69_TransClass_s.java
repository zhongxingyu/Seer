 package de.unifr.acp.trafo;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtBehavior;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
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
 
 import de.unifr.acp.annot.Grant;
 import de.unifr.acp.templates.TraversalTarget__;
 
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
     private static Logger logger;
     private static FileHandler fh;
     private static final String TRAVERSAL_TARGET = "de.unifr.acp.templates.TraversalTarget__";
     private static final String FST_CACHE_FIELD_NAME = "$fstMap";
     private final CtClass objectClass;
 
     private final Map<CtClass, Boolean> visited = new HashMap<CtClass, Boolean>();
     private final Queue<CtClass> pending;
 
     protected Map<CtClass, Boolean> getVisited() {
         return Collections.unmodifiableMap(visited);
     }
 
     protected Collection<CtClass> getPending() {
         return Collections.unmodifiableCollection(pending);
     }
 
     /**
      * Transformer class capable of statically adding heap traversal code to a
      * set of reachable classes.
      * 
      * @param classname
      *            the name of the class forming the starting point for the
      *            reachable classes
      * @throws NotFoundException
      */
     protected TransClass(String classname) throws NotFoundException {
         CtClass clazz = ClassPool.getDefault().get(classname);
         ClassPool.getDefault().importPackage("java.util");
     	pending = new LinkedList<CtClass>();
     	pending.add(clazz);
         objectClass = ClassPool.getDefault().get(Object.class.getName());
     }
 
     /**
      * Transforms all classes that are reachable from the class corresponding
      * to the specified class name.
      * @param className the class name of the class spanning a reachable classes tree 
      * @throws ClassNotFoundException 
      */
     public static TransClass transformHierarchy(String className)
             throws NotFoundException, IOException, CannotCompileException, ClassNotFoundException {
         TransClass tc = new TransClass(className);
         tc.computeReachableClasses();
         tc.performTransform();
         return tc;
     }
 
     /**
      * Transforms all classes that are reachable from the class corresponding
      * to the specified class name and flushes the resulting classes to the
      * specified output directory.
      * @param className the class name of the class spanning a reachable classes tree
      * @param outputDir the relative output directory 
      * @throws ClassNotFoundException 
      */
     public static void transformAndFlushHierarchy(String className, String outputDir)
             throws NotFoundException, IOException, CannotCompileException, ClassNotFoundException {
         TransClass tc = transformHierarchy(className);
         tc.flushTransform(outputDir);
     }
 
     protected void computeReachableClasses() throws NotFoundException,
             IOException, CannotCompileException {
         while (!pending.isEmpty()) {
             CtClass clazz = pending.remove();
             if (!visited.containsKey(clazz)) {
                 doTraverse(clazz);
             }
         }
     }
     
 //    public boolean hasSuperClassOtherThan(CtClass target, CtClass standard) throws NotFoundException {
 //        CtClass superclazz = target.getSuperclass();
 //        boolean hasSuperclass = !superclazz.equals(standard);
 //        return hasSuperclass;
 //    }
     
     /*
      * Helper method for <code>computeReachableClasses</code>. Traverses the
      * specified target class and adds it to the list of visited classes. Adds
      * all classes the class' fields to queue of pending classes, if not already
      * visited.
      */
     private void doTraverse(CtClass target) throws NotFoundException {
         CtClass superclazz = target.getSuperclass();
         boolean hasSuperclass = !superclazz.equals(objectClass);
         visited.put(target, hasSuperclass);
         if (hasSuperclass) {
             enter(superclazz);
         }
         
         final List<CtClass> referredTypes = new ArrayList<CtClass>();
         CtField[] fs = target.getDeclaredFields();
         for (CtField f : fs) {
             CtClass ft = f.getType();
             if (ft.isPrimitive())
                 continue;
             referredTypes.add(ft);
         }
 
         List<CtMethod> methods = Arrays.asList(target.getMethods());
         for (CtMethod method : methods) {
             List<CtClass> returnType = Arrays.asList(method.getReturnType());
             referredTypes.addAll(returnType);
         }
         List<CtConstructor> ctors = Arrays.asList(target.getConstructors());
         List<CtBehavior> methodsAndCtors = new ArrayList<CtBehavior>();
         methodsAndCtors.addAll(methods);
         methodsAndCtors.addAll(ctors);
         for (CtBehavior methodOrCtor : methodsAndCtors) {
             List<CtClass> exceptionTypes = Arrays.asList(methodOrCtor.getExceptionTypes());
             referredTypes.addAll(exceptionTypes);
             List<CtClass> paramTypes = Arrays.asList(methodOrCtor.getParameterTypes());
             referredTypes.addAll(paramTypes);
             
             try {
                 final List<NotFoundException> notFoundexceptions = new ArrayList<NotFoundException>(1);
                 methodOrCtor.instrument(new ExprEditor() {
                     public void edit(NewExpr expr)
                         throws CannotCompileException {
                             try {
                                 CtClass type = expr.getConstructor().getDeclaringClass();
                                 logger.fine("Reference to instantiated type "
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
                                 logger.fine("Reference to instanceof right-hand side type "
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
                     public void edit(NewArray expr)
                         throws CannotCompileException {
                             try {
                                 CtClass type = expr.getComponentType();
                                 logger.fine("Reference to array component type "
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
                             logger.fine("Reference to method-declaring type "
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
                             logger.fine("Reference to handler type "
                                    + type.getName() + " at "
                                     + expr.getFileName() + ":"
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
                             CtClass type = expr.getField().getType();
                             logger.fine("Reference to field-declaring type "
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
                             logger.fine("Reference to cast target type "
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
                 assert(false);
             }
         }
         
         for (CtClass type : referredTypes) {
             if (type.isPrimitive())
                 continue;
             if (type.getName().matches("java\\..*"))
                 continue;
             enter(type); 
         }
     }
 
     /*
      * Helper method for <code>computeReachableClasses</code>. Adds the
      * specified class to queue of pending classes, if not already visited.
      */
     private void enter(CtClass clazz) {
         if (!visited.containsKey(clazz)) {
             pending.add(clazz);
         }
     }
 
     /*
      * Transforms all reachable classes.
      */
     protected void performTransform() throws NotFoundException, IOException,
             CannotCompileException, ClassNotFoundException {
         for (Entry<CtClass, Boolean> entry : visited.entrySet()) {
             doTransform(entry.getKey(), entry.getValue());
         }
     }
     
     /**
      * Flushes all reachable classes back to disk.
      * @param outputDir the relative output directory
      */
     protected void flushTransform(String outputDir) throws NotFoundException, IOException,
             CannotCompileException {
         for (CtClass tc : visited.keySet()) {
             if (!tc.isArray()) {
                 tc.writeFile(outputDir);
             }
         }
     }
 
     /**
      * 
      * @param target
      * @param hasSuperclass
      * @throws NotFoundException
      * @throws IOException
      * @throws CannotCompileException
      * @throws ClassNotFoundException
      */
     public static void doTransform(CtClass target, boolean hasSuperclass)
             throws NotFoundException, IOException, CannotCompileException, ClassNotFoundException {
         if (target.isArray() || target.isInterface()) {
             return;
         }
         ClassPool.getDefault().importPackage("java.util");
         
         // add: implements TRAVERSALTARGET
         List<CtClass> targetIfs = Arrays.asList(target.getInterfaces());
         Collection<CtClass> newTargetIfs = new HashSet<CtClass>(targetIfs);
         
         // NOTE: Given the equality semantics of CtClass the condition is only
         // valid if the CtClass instance representing the traversal interface is
         // never detached from the ClassPool as this would result in a new
         // CtClass instance (representing the traversal interface) to be generated
         // by a call to ClassPool.get(...) not being equal to the old instance.
         
         // only generate implementation of traversal interface if not yet present
         // use traversal interface as marker for availability of other instrumentation
         CtClass traversalTargetInterface = ClassPool.getDefault().get(TRAVERSAL_TARGET);
         if (!newTargetIfs.contains(traversalTargetInterface)) {
             newTargetIfs.add(traversalTargetInterface);
             target.setInterfaces(newTargetIfs.toArray(new CtClass[0]));
 
             // add: method traverse__ (create body before adding new technically required fields)
             String methodbody = createBody(target, hasSuperclass);
             CtMethod m = CtNewMethod.make(methodbody, target);
             target.addMethod(m);
             
             // change methods carrying contracts
             // 1. Find all methods carrying contracts
             // 2. For each annotated method
             //   1. Get method annotation
             //   2. Generate code to generate automaton for contract
             //      (generate code to get contract string and call into automation generation library)
             //   3. Use insertBefore() and insertAfter() to insert permission installation/deinstallation code
             
             // according to tutorial there's no support for generics in Javassist, thus we use raw types
             CtField f = CtField.make("private java.util.HashMap "+FST_CACHE_FIELD_NAME+" = new java.util.HashMap();", target);
             target.addField(f);
             
             // collect all methods and constructors
             List<CtMethod> methods = Arrays.asList(target.getMethods());
             List<CtConstructor> ctors = Arrays.asList(target.getConstructors());
             List<CtBehavior> methodsAndCtors = new ArrayList<CtBehavior>();
             methodsAndCtors.addAll(methods);
             methodsAndCtors.addAll(ctors);
             
             for (CtConstructor ctor : ctors) {
                 instrumentNew(ctor);
             }
             for (CtBehavior methodOrCtor : methodsAndCtors) {
                 
                 instrumentFieldAccess(methodOrCtor);
                 
                 if (hasMethodGrantAnnotations(methodOrCtor)) {
                     
                     /* generate header and footer */
                     
                     // filter synthetic methods
                     if (methodOrCtor.getMethodInfo().getAttribute(SyntheticAttribute.tag) != null) {
                         continue;
                     }
                                         
                     // generate method header
                     
                     // optional method grant annotation (can be null)
                     // NOTE: method contracts include 'this' and type names as anchors
                     Grant methodGrantAnnot = ((Grant)methodOrCtor.getAnnotation(Grant.class));
                     
                     // optional parameter types annotations indexed by parameter position
                     Object[][] availParamAnnot = methodOrCtor.getAvailableParameterAnnotations();
                     
                     // optional parameter (1 to n) grant annotations indexed by parameter position minus one
                     // NOTE: parameter contracts exclude anchors (formal parameter names)
                     Grant[] paramGrantAnnots = new Grant[availParamAnnot.length];
                     final CtClass[] parameterTypes = methodOrCtor.getParameterTypes();
                     
                     for (int i = 0; i < availParamAnnot.length; i++) {
                         final Object[] oa = availParamAnnot[i];
                         final CtClass paramType = parameterTypes[i];
                         
                         // we can savely ignore grant annotations on primitive formal parameters
                         if (!paramType.isPrimitive()) {
                             for (Object o : oa) {
                                 if (o instanceof Grant) {
                                     paramGrantAnnots[i] = (Grant)o;
                                     break; // there's one grant annotation per parameter only
                                 }
                             }
                         }
                     }
                     
                     /*
                      * We keep method contract and all parameter contracts separate.
                      */
                     
                     StringBuilder sb = new StringBuilder();
                     
                     // uniquely identifies a method globally
                     String longName = methodOrCtor.getLongName();
                     
                     // check if automata for this method already exist
                     sb.append("String longName = \"" + longName + "\";");
                     
                     // FSTs indexed  by parameter position (0: FST for this & type-anchored
                     // contracts, 1 to n: FTSs for unanchored parameter contracts)
                     sb.append("de.unifr.acp.fst.FST[] fSTs;");
                     sb.append("if ("+FST_CACHE_FIELD_NAME+".containsKey(longName)) {");
                     sb.append("  fSTs = ((de.unifr.acp.fst.FST[])"+FST_CACHE_FIELD_NAME+".get(longName));");
                     sb.append("}");
                     sb.append("else {");
                     
                     // build array of FSTs indexed by parameter
                     sb.append("  fSTs = new de.unifr.acp.fst.FST["+(getParameterCount(methodOrCtor)+1)+"];");
                     for (int i=0; i<getParameterCount(methodOrCtor)+1; i++) {
                         Grant grant = grantAnno(methodOrCtor, i);
                         if (grant != null) {
                             sb.append("    fSTs[" + i
                                     + "] = new de.unifr.acp.fst.FST(\""
                                     + grant.value() + "\");");
                         }
                     }
                     
                     // cache generated automata indexed by long method name
                     sb.append("  "+FST_CACHE_FIELD_NAME+".put(longName, fSTs);");
                     sb.append("}");
                     
                     // now we expect to have all FSTs available and cached
                     
                     sb.append("  Map allLocPerms = new de.unifr.acp.util.WeakIdentityHashMap();");
                     for (int i=0; i<getParameterCount(methodOrCtor)+1; i++) {
                         
                         // only grant-annotated methods/parameters require any action
                         if (grantAnno(methodOrCtor, i) == null) {
                             continue;
                         }
                         
                         if (!mightBeReferenceParameter(methodOrCtor, i)) {
                             continue;
                         }
                         
                         // TODO: factor out this code in external class, parameterize over i and allPermissions
                         // a location permission is a Map<Object, Map<String, Permission>>
                         sb.append("{");
                         sb.append("  de.unifr.acp.fst.FST fst = fSTs["+i+"];");
                         sb.append("  de.unifr.acp.fst.FSTRunner runner = new de.unifr.acp.fst.FSTRunner(fst);");
                         
                         // step to reach FST runner state that corresponds to anchor object
                         // for explicitly anchored contracts
                         if (i == 0) {
                             sb.append("  runner.resetAndStep(\"this\");");
                         }
                         
                         // here the runner should be in synch with the parameter object
                         // (as far as non-static fields are concerned), the visitor implicitly joins locPerms
                         sb.append("  if ($"+i+" instanceof de.unifr.acp.templates.TraversalTarget__) {");
                         sb.append("    de.unifr.acp.templates.TraversalImpl visitor = new de.unifr.acp.templates.TraversalImpl(runner,allLocPerms);");
                         sb.append("    ((de.unifr.acp.templates.TraversalTarget__)$"+i+").traverse__(visitor);");
 
                         // Map<Object, Map<String, de.unifr.acp.fst.Permission>>
                         sb.append("    Map allLocPerms = visitor.getLocationPermissions();");
                         //sb.append("    System.out.println(\"allLocPerms: \"+allLocPerms);");
                         sb.append("  }");
                         // TODO: explicit representation of locations and location permissions (supporting join)
                         // (currently it's all generic maps and implicit joins in visitor similar to Maxine implementation)
                         
                         sb.append("}");
                     }
                     
                     // install allLocPerms and push new objects set on (current thread's) stack
                     //sb.append("System.out.println(\"locPermStack: \"+de.unifr.acp.templates.Global.locPermStack);");
                     //sb.append("System.out.println(\"locPermStack.peek(): \"+de.unifr.acp.templates.Global.locPermStack.peek());");
                     sb.append("de.unifr.acp.templates.Global.locPermStack.push(allLocPerms);");
                     sb.append("de.unifr.acp.templates.Global.newObjectsStack.push(Collections.newSetFromMap(new de.unifr.acp.util.WeakIdentityHashMap()));");
                     
                     // TODO: figure out how to instrument thread start/end and field access
                     
                     String header = sb.toString();
                     methodOrCtor.insertBefore(header);
                     
                     // generate method footer
                     sb = new StringBuilder();
 
                     // pop location permissions and new locations entry from
                     // (current thread's) stack
                     //sb.append("System.out.println(\"locPermStack: \"+de.unifr.acp.templates.Global.locPermStack);");
                     //sb.append("System.out.println(\"locPermStack.peek(): \"+de.unifr.acp.templates.Global.locPermStack.peek());");
                     sb.append("de.unifr.acp.templates.Global.locPermStack.pop();");
                     sb.append("de.unifr.acp.templates.Global.newObjectsStack.pop();");
                     String footer = sb.toString();
                     
                     // make sure all method exits are covered (exceptions, multiple returns)
                     methodOrCtor.insertAfter(footer, true);
 
                 } // end if (hasMethodGrantAnnotations(methodOrCtor))
             }
         }
     }
     
     private static void instrumentFieldAccess(CtBehavior methodOrCtor)
             throws CannotCompileException {
 //        if (!isInstrumented.get()) {
             methodOrCtor.instrument(new ExprEditor() {
                 public void edit(FieldAccess expr)
                         throws CannotCompileException {
                     String qualifiedFieldName = expr.getClassName() + "."
                             + expr.getFieldName();
 
                     // exclude standard API (to be factored out)
                     if (!(qualifiedFieldName.startsWith("java") || qualifiedFieldName
                             .startsWith("javax"))) {
                         StringBuilder code = new StringBuilder();
                         code.append("{");
 
                         // get active permission for location to access
                         code.append("if (!de.unifr.acp.templates.Global.newObjectsStack.isEmpty()) {");
                         
                         code.append("de.unifr.acp.fst.Permission effectivePerm = de.unifr.acp.templates.Global.installedPermissionStackNotEmpty($0, \""
                                 + qualifiedFieldName + "\");");
 
                         // get permission needed for this access
                         code.append("de.unifr.acp.fst.Permission accessPerm = de.unifr.acp.fst.Permission."
                                 + (expr.isReader() ? "READ_ONLY" : "WRITE_ONLY")
                                 + ";");
 //                        code.append("de.unifr.acp.fst.Permission accessPerm = de.unifr.acp.fst.Permission.values()["
 //                                + (expr.isReader() ? "1" : "2")
 //                                + "];");
 
                         //code.append("if (!effectivePerm.containsAll(accessPerm)) {");
                         code.append("if (!de.unifr.acp.fst.Permission.containsAll(effectivePerm, accessPerm)) {");
                         code.append("  System.out.println(\"ACCESS VIOLATION:\");");
                         code.append("  System.out.println($0);");
                         code.append("  System.out.println(\""
                                 + qualifiedFieldName + "\");");
                         code.append("  System.out.println(\"effectivePerm: \"+effectivePerm);");
                         code.append("  System.out.println(de.unifr.acp.templates.Global.locPermStack);");
                         code.append("  System.out.println();");
                         code.append("  System.out.println(\"requiredPerm: \"+accessPerm);");
                         code.append("  System.out.println();");
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
                 }
             });
 //        }
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
                 code.append("{");
                 code.append("  $_ = $proceed($$);");
                 code.append("  de.unifr.acp.templates.Global.addNewObject($0);");
                 code.append("}");
                 expr.replace(code.toString());
             }
         });
 
     }
     
     /*
      * Instrument array creation such that the new array is added to the new
      * objects stack's top entry.
      */
     private static void instrumentNewArray(CtBehavior methodOrCtor) throws CannotCompileException {
         methodOrCtor.instrument(new ExprEditor() {
             public void edit(NewArray expr) throws CannotCompileException {
                 StringBuilder code = new StringBuilder();
                 code.append("{");
                 code.append("  $_ = $proceed($$);");
                 code.append("  de.unifr.acp.templates.Global.addNewObject($0);");
                 code.append("}");
                 
                 expr.replace(code.toString());
               } 
         });
     }
     
     private static int getParameterCount(CtBehavior methodOrCtor) {
         return methodOrCtor.getAvailableParameterAnnotations().length;
     }
     
     /**
      * Currently is an under-approximation (might return false for primitive parameters)
      * @param methodOrCtor
      * @param index the parameter index
      * @return false if non-primitive or primitive, true if primitive
      */
     private static boolean mightBeReferenceParameter(CtBehavior methodOrCtor, int index) {
         if (index == 0) {
             return true;
         } else {
             try {
                 return !(methodOrCtor.getParameterTypes()[index-1].isPrimitive());
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
 
             // optional parameter types annotations indexed by parameter position
             Object[][] availParamAnnot = methodOrCtor
                     .getAvailableParameterAnnotations();
 
             final Object[] oa = availParamAnnot[index-1];
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
      * @deprecated
      */
     private String concateSingleContracts(Grant methodGrantAnnot,
             Grant[] paramGrantAnnots) {
         String compositeContract;
         // list of single contracts to form the composite contract
         final ArrayList<String> singleContracts = new ArrayList<String>();
         
         // add method contract if available
         if (methodGrantAnnot != null) {
             singleContracts.add(methodGrantAnnot.value());
         }
         
         // add anchor-prefixed parameter contracts
         for (int i = 0; i < paramGrantAnnots.length; i++) {
             Grant grant = paramGrantAnnots[i];
             String[] singleParamContracts = grant.value().split(",");
             for (String contract : singleParamContracts) {
                 singleContracts.add((i + 1) + "." + contract); // add anchor prefix
             }
         }
         
         // append comma-separated single contracts 
         final StringBuilder compositeBuilder = new StringBuilder();
         if (singleContracts.size() > 0) {
             for (int i = 0; i < singleContracts.size() - 1; i++) {
                 String contract = singleContracts.get(i);
                 compositeBuilder.append(contract + ",");
             }
             compositeBuilder.append(singleContracts.get(singleContracts.size() -1));
         }
         compositeContract = compositeBuilder.toString();
         return compositeContract;
     }
     
     /**
      * Returns true if the specified method/constructor or one of its parameters
      * has a Grant annotation.
      * @param methodOrCtor the method/constructor to check
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
 
     protected static String createBody(CtClass target, boolean hasSuperclass)
             throws NotFoundException {
         StringBuilder sb = new StringBuilder();
         sb.append("public void traverse__(de.unifr.acp.templates.Traversal__ t) {\n");
         for (CtField f : target.getDeclaredFields()) {
             CtClass tf = f.getType();
             String fname = f.getName();
             if (!fname.equals(FST_CACHE_FIELD_NAME)) {
                 appendVisitorCalls(sb, target, tf, fname);
             }
         }
         if (hasSuperclass) {
             sb.append("super.traverse__(t);\n");
         }
         sb.append('}');
         return sb.toString();
     }
 
     protected static void appendVisitorCalls(StringBuilder sb, CtClass target,
             CtClass tf, String fname) throws NotFoundException {
         int nesting = 0;
         String index = "";
         while (tf.isArray()) {
             String var = "i" + nesting;
             
             /* generate for header */
             sb.append("for (int " + var + " = 0; ");
             
             // static type of 'this' corresponds to field's declaring class, no cast needed
             sb.append(var + "<this."+fname+index+".length; ");
             sb.append(var + "++");
             sb.append(")\n");
             index = index + "[" + var + "]";
             nesting++;
             tf = tf.getComponentType();
         }
         if (tf.isPrimitive()) {
             sb.append("t.visitPrimitive__(");
         } else {
             sb.append("t.visit__(");
         }
         sb.append("this, ");
         sb.append('"');
         sb.append(target.getName());
         sb.append('.');
         sb.append(fname);
         sb.append('"');
         if (!tf.isPrimitive()) {
             // static type of 'this' corresponds to field's declaring class, no cast needed
             sb.append(", this."); 
             sb.append(fname + index);
         }
         sb.append(");\n");
     }
 
 }
