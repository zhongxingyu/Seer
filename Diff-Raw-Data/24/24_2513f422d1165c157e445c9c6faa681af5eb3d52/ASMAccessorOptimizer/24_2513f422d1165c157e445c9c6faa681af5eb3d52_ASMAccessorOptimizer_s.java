 /**
  * MVEL (The MVFLEX Expression Language)
  *
  * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.mvel.optimizers.impl.asm;
 
 import static org.mvel.util.ArrayTools.findFirst;
 import org.mvel.*;
 import org.mvel.integration.VariableResolverFactory;
 import org.mvel.optimizers.AbstractOptimizer;
 import org.mvel.optimizers.AccessorOptimizer;
 import org.mvel.optimizers.OptimizationNotSupported;
 import org.mvel.optimizers.impl.refl.Union;
 import org.mvel.util.*;
 import static org.mvel.util.ParseTools.*;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import static org.objectweb.asm.Opcodes.*;
 import static org.objectweb.asm.Type.*;
 
 import java.lang.reflect.*;
 import java.util.*;
 
 /**
  * Implementation of the MVEL Just-in-Time (JIT) compiler for Property Accessors using the ASM bytecode
  * engineering library.
  */
 public class ASMAccessorOptimizer extends AbstractOptimizer implements AccessorOptimizer {
    private static final String MAP_IMPL = "org/mvel/util/FastMap";
     private static final String LIST_IMPL = "org/mvel/util/FastList";
 
     private static final int OPCODES_VERSION;
 
     static {
         String javaVersion = System.getProperty("java.version");
         if (javaVersion.startsWith("1.4"))
             OPCODES_VERSION = Opcodes.V1_4;
         else if (javaVersion.startsWith("1.5"))
             OPCODES_VERSION = Opcodes.V1_5;
         else if (javaVersion.startsWith("1.6") || javaVersion.startsWith("1.7"))
             OPCODES_VERSION = Opcodes.V1_6;
         else
             OPCODES_VERSION = Opcodes.V1_2;
     }
 
     private Object ctx;
     private Object thisRef;
 
     private VariableResolverFactory variableFactory;
 
     private static final Object[] EMPTYARG = new Object[0];
 
     private boolean first = true;
 
     private String className;
     private ClassWriter cw;
     private MethodVisitor mv;
 
     private Object val;
     private int stacksize = 1;
     private long time;
 
     private int inputs;
 
     private ArrayList<ExecutableStatement> compiledInputs;
 
     private Class returnType;
 
 
     public ASMAccessorOptimizer() {
         //do this to confirm we're running the correct version
         //otherwise should create a verification error in VM
         new ClassWriter(ClassWriter.COMPUTE_MAXS);
     }
 
     /**
      * Does all the boilerplate for initiating the JIT.
      */
     private void _initJIT() {
         cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
         cw.visit(OPCODES_VERSION, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className = "ASMAccessorImpl_" + String.valueOf(cw.hashCode()).replaceAll("\\-", "_") + (System.currentTimeMillis() / 1000),
                 null, "java/lang/Object", new String[]{"org/mvel/Accessor"});
 
         MethodVisitor m = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
         m.visitCode();
         m.visitVarInsn(Opcodes.ALOAD, 0);
         m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                 "<init>", "()V");
         m.visitInsn(Opcodes.RETURN);
 
         m.visitMaxs(1, 1);
         m.visitEnd();
 
         mv = cw.visitMethod(ACC_PUBLIC, "getValue",
                 "(Ljava/lang/Object;Ljava/lang/Object;Lorg/mvel/integration/VariableResolverFactory;)Ljava/lang/Object;", null, null);
         mv.visitCode();
     }
 
 
     public Accessor optimize(char[] property, Object staticContext, Object thisRef, VariableResolverFactory factory, boolean root) {
         time = System.currentTimeMillis();
 
         inputs = 0;
         compiledInputs = new ArrayList<ExecutableStatement>();
 
         start = cursor = 0;
 
         this.first = true;
         this.val = null;
 
         this.length = property.length;
         this.expr = property;
         this.ctx = staticContext;
         this.thisRef = thisRef;
         this.variableFactory = factory;
 
         _initJIT();
 
         return compileAccessor();
     }
 
     private void _finishJIT() {
         if (returnType != null && returnType.isPrimitive()) {
             //noinspection unchecked
             wrapPrimitive(returnType);
         }
 
         if (returnType == void.class) {
             debug("ACONST_NULL");
             mv.visitInsn(ACONST_NULL);
         }
 
         debug("ARETURN");
 
         mv.visitInsn(ARETURN);
 
         debug("\n{METHOD STATS (maxstack=" + stacksize + ")}\n");
         mv.visitMaxs(stacksize, 1);
 
         mv.visitEnd();
 
         buildInputs();
 
         cw.visitEnd();
     }
 
     private Accessor _initializeAccessor() throws Exception {
         Class cls = loadClass(className, cw.toByteArray());
 
         debug("[MVEL JIT Completed Optimization <<" + new String(expr) + ">>]::" + cls + " (time: " + (System.currentTimeMillis() - time) + "ms)");
 
         Object o;
 
         if (inputs == 0) {
             o = cls.newInstance();
         }
         else {
             Class[] parms = new Class[inputs];
             for (int i = 0; i < inputs; i++) {
                 parms[i] = ExecutableStatement.class;
             }
             o = cls.getConstructor(parms).newInstance(compiledInputs.toArray(new ExecutableStatement[compiledInputs.size()]));
         }
 
         if (!(o instanceof Accessor)) {
             throw new RuntimeException("Classloader problem detected. JIT Class is not subclass of org.mvel.Accessor.");
         }
 
         return (Accessor) o;
     }
 
     private Accessor compileAccessor() {
         debug("\n{Initiate Compile: " + new String(expr) + "}\n");
 
         Object curr = ctx;
 
         try {
             while (cursor < length) {
                 switch (nextSubToken()) {
                     case BEAN:
                         curr = getBeanProperty(curr, capture());
                         break;
                     case METH:
                         curr = getMethod(curr, capture());
                         break;
                     case COL:
                         curr = getCollectionProperty(curr, capture());
                         break;
                 }
 
                 first = false;
             }
 
             val = curr;
 
             _finishJIT();
 
             return _initializeAccessor();
         }
         catch (InvocationTargetException e) {
             throw new PropertyAccessException("could not access property", e);
         }
         catch (IllegalAccessException e) {
             throw new PropertyAccessException("could not access property", e);
         }
         catch (IndexOutOfBoundsException e) {
             throw new PropertyAccessException("array or collections index out of bounds (property: " + new String(expr) + ")", e);
         }
         catch (PropertyAccessException e) {
             throw new PropertyAccessException("failed to access property: <<" + new String(expr) + ">> in: " + (ctx != null ? ctx.getClass() : null), e);
         }
         catch (CompileException e) {
             throw e;
         }
         catch (NullPointerException e) {
             throw new PropertyAccessException("null pointer exception in property: " + new String(expr), e);
         }
         catch (OptimizationNotSupported e) {
             throw e;
         }
         catch (Exception e) {
             throw new PropertyAccessException("unknown exception in expression: " + new String(expr), e);
         }
     }
 
 
     private Object getBeanProperty(Object ctx, String property)
             throws IllegalAccessException, InvocationTargetException {
 
         debug("{bean: " + property + "}");
 
         Class cls = (ctx instanceof Class ? ((Class) ctx) : ctx != null ? ctx.getClass() : null);
         Member member = cls != null ? PropertyTools.getFieldOrAccessor(cls, property) : null;
 
 
         if (first && variableFactory != null && variableFactory.isResolveable(property)) {
             try {
                 debug("ALOAD 3");
                 mv.visitVarInsn(ALOAD, 3);
 
                 debug("LDC :" + property);
                 mv.visitLdcInsn(property);
 
                 debug("INVOKEINTERFACE org/mvel/integration/VariableResolverFactory.getVariableResolver");
                 mv.visitMethodInsn(INVOKEINTERFACE, "org/mvel/integration/VariableResolverFactory",
                         "getVariableResolver", "(Ljava/lang/String;)Lorg/mvel/integration/VariableResolver;");
 
                 debug("INVOKEINTERFACE org/mvel/integration/VariableResolver.getValue");
                 mv.visitMethodInsn(INVOKEINTERFACE, "org/mvel/integration/VariableResolver",
                         "getValue", "()Ljava/lang/Object;");
             }
             catch (Exception e) {
                 throw new OptimizationFailure("critical error in JIT", e);
             }
 
 
             return variableFactory.getVariableResolver(property).getValue();
         }
         else if (member instanceof Field) {
             Object o = ((Field) member).get(ctx);
 
             if (first) {
                 debug("ALOAD 2");
                 mv.visitVarInsn(ALOAD, 2);
             }
 
             if (((member.getModifiers() & Modifier.STATIC) != 0)) {
                 debug("GETSTATIC " + getDescriptor(member.getDeclaringClass()) + "."
                         + member.getName() + "::" + getDescriptor(((Field) member).getType()));
 
                 mv.visitFieldInsn(GETSTATIC, getDescriptor(member.getDeclaringClass()),
                         member.getName(), getDescriptor(returnType = ((Field) member).getType()));
             }
             else {
                 debug("CHECKCAST " + getInternalName(cls));
                 mv.visitTypeInsn(CHECKCAST, getInternalName(cls));
 
                 debug("GETFIELD " + property + ":" + getDescriptor(((Field) member).getType()));
                 mv.visitFieldInsn(GETFIELD, getInternalName(cls), property, getDescriptor(((Field) member).getType()));
             }
 
             returnType = ((Field) member).getType();
 
             return o;
         }
         else if (member != null) {
 
             Object o;
 
             if (first) {
                 debug("ALOAD 2");
                 mv.visitVarInsn(ALOAD, 2);
             }
 
             try {
                 o = ((Method) member).invoke(ctx, EMPTYARG);
 
                 debug("CHECKCAST " + getInternalName(member.getDeclaringClass()));
                 mv.visitTypeInsn(CHECKCAST, getInternalName(member.getDeclaringClass()));
 
                 returnType = ((Method) member).getReturnType();
 
                 debug("INVOKEVIRTUAL " + member.getName() + ":" + returnType);
                 mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(member.getDeclaringClass()), member.getName(),
                         getMethodDescriptor((Method) member));
 
             }
             catch (IllegalAccessException e) {
                 Method iFaceMeth = determineActualTargetMethod((Method) member);
 
                 debug("CHECKCAST " + getInternalName(iFaceMeth.getDeclaringClass()));
                 mv.visitTypeInsn(CHECKCAST, getInternalName(iFaceMeth.getDeclaringClass()));
 
                 returnType = iFaceMeth.getReturnType();
 
                 debug("INVOKEINTERFACE " + member.getName() + ":" + returnType);
                 mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(iFaceMeth.getDeclaringClass()), member.getName(),
                         getMethodDescriptor((Method) member));
 
                 o = iFaceMeth.invoke(ctx, EMPTYARG);
             }
             return o;
 
         }
         else if (ctx instanceof Map && ((Map) ctx).containsKey(property)) {
             debug("CHECKCAST java/util/Map");
             mv.visitTypeInsn(CHECKCAST, "java/util/Map");
 
             debug("LDC: \"" + property + "\"");
             mv.visitLdcInsn(property);
 
             debug("INVOKEINTERFACE: get");
             mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
             return ((Map) ctx).get(property);
         }
         else if ("this".equals(property)) {
             debug("ALOAD 2");
             mv.visitVarInsn(ALOAD, 2); // load the thisRef value.
 
             return this.thisRef;
         }
         else if (Token.LITERALS.containsKey(property)) {
             Object lit = Token.LITERALS.get(property);
 
             if (lit instanceof Class) {
                 debug("LDC " + getDescriptor((Class) lit));
                 mv.visitLdcInsn(getType(getDescriptor((Class) lit)));
             }
 
             return Token.LITERALS.get(property);
         }
         else {
             Object ts = tryStaticAccess();
 
             if (ts != null) {
                 if (ts instanceof Class) {
                     debug("LDC " + getDescriptor((Class) ts));
                     mv.visitLdcInsn(getType(getDescriptor((Class) ts)));
                     return ts;
                 }
                 else {
                     debug("GETSTATIC " + getDescriptor(((Field) ts).getDeclaringClass()) + "."
                             + ((Field) ts).getName() + "::" + getDescriptor(((Field) ts).getType()));
 
                     mv.visitFieldInsn(GETSTATIC, getDescriptor(((Field) ts).getDeclaringClass()),
                             ((Field) ts).getName(), getDescriptor(returnType = ((Field) ts).getType()));
 
 
                     return ((Field) ts).get(null);
                 }
 
             }
             else
                 throw new PropertyAccessException("could not access property (" + property + ")");
         }
     }
 
     /**
      * Handle accessing a property embedded in a collections, map, or array
      *
      * @param ctx  -
      * @param prop -
      * @return -
      * @throws Exception -
      */
     private Object getCollectionProperty(Object ctx, String prop)
             throws IllegalAccessException, InvocationTargetException {
         if (prop.length() > 0) ctx = getBeanProperty(ctx, prop);
 
         debug("{collections: " + prop + "} ctx=" + ctx);
 
         int start = ++cursor;
 
         whiteSpaceSkip();
 
         if (cursor == length)
             throw new PropertyAccessException("unterminated '['");
 
         String item;
 
         if (expr[cursor] == '\'' || expr[cursor] == '"') {
             start++;
 
             int end;
 
             if (!scanTo(']'))
                 throw new PropertyAccessException("unterminated '['");
             if ((end = containsStringLiteralTermination()) == -1)
                 throw new PropertyAccessException("unterminated string literal in collections accessor");
 
             item = new String(expr, start, end - start);
         }
         else {
             if (!scanTo(']'))
                 throw new PropertyAccessException("unterminated '['");
 
             item = new String(expr, start, cursor - start);
         }
 
         ++cursor;
 
         if (ctx instanceof Map) {
             debug("CHECKCAST java/util/Map");
             mv.visitTypeInsn(CHECKCAST, "java/util/Map");
 
             debug("LDC: \"" + item + "\"");
             mv.visitLdcInsn(item);
 
             debug("INVOKEINTERFACE: get");
             mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
 
             return ((Map) ctx).get(item);
         }
         else if (ctx instanceof List) {
             int index = Integer.parseInt(item);
 
             debug("CHECKCAST java/util/List");
             mv.visitTypeInsn(CHECKCAST, "java/util/List");
 
             intPush(index);
 
             debug("INVOKEINTERFACE: java/util/List.get");
             mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;");
 
             return ((List) ctx).get(index);
         }
         else if (ctx instanceof Collection) {
             int count = Integer.parseInt(item);
             if (count > ((Collection) ctx).size())
                 throw new PropertyAccessException("index [" + count + "] out of bounds on collections");
 
             Iterator iter = ((Collection) ctx).iterator();
             for (int i = 0; i < count; i++) iter.next();
             return iter.next();
         }
         else if (ctx instanceof Object[]) {
             int index = Integer.parseInt(item);
 
             debug("CHECKCAST [Ljava/lang/Object;");
             mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
 
             intPush(index);
 
             mv.visitInsn(AALOAD);
 
             return ((Object[]) ctx)[index];
         }
         else if (ctx instanceof CharSequence) {
             int index = Integer.parseInt(item);
 
             intPush(index);
 
             mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/CharSequence", "charAt", "(I)C");
 
             return ((CharSequence) ctx).charAt(index);
         }
         else {
             throw new PropertyAccessException("illegal use of []: unknown type: " + (ctx == null ? null : ctx.getClass().getName()));
         }
     }
 
 
     /**
      * Find an appropriate method, execute it, and return it's response.
      *
      * @param ctx  -
      * @param name -
      * @return -
      * @throws Exception -
      */
     @SuppressWarnings({"unchecked"})
     private Object getMethod(Object ctx, String name)
             throws IllegalAccessException, InvocationTargetException {
         debug("{method: " + name + "}");
 
 
         int st = cursor;
 
         int depth = 1;
 
         while (cursor++ < length - 1 && depth != 0) {
             switch (expr[cursor]) {
                 case'(':
                     depth++;
                     continue;
                 case')':
                     depth--;
 
             }
         }
         cursor--;
 
         String tk = (cursor - st) > 1 ? new String(expr, st + 1, cursor - st - 1) : "";
 
         cursor++;
 
         Object[] preConvArgs;
         Object[] args;
         ExecutableStatement[] es;
 
         if (tk.length() == 0) {
             //noinspection ZeroLengthArrayAllocation
             args = new Object[0];
 
             //noinspection ZeroLengthArrayAllocation
             preConvArgs = new Object[0];
             es = null;
         }
         else {
             String[] subtokens = parseParameterList(tk.toCharArray(), 0, -1);
 
             es = new ExecutableStatement[subtokens.length];
             args = new Object[subtokens.length];
             preConvArgs = new Object[es.length];
 
             for (int i = 0; i < subtokens.length; i++) {
                 preConvArgs[i] = args[i] = (es[i] = (ExecutableStatement) MVEL.compileExpression(subtokens[i])).getValue(this.ctx, variableFactory);
             }
         }
 
         if (es != null) {
             for (ExecutableStatement e : es)
                 compiledInputs.add(e);
         }
         /**
          * If the target object is an instance of java.lang.Class itself then do not
          * adjust the Class scope target.
          */
         Class cls = ctx instanceof Class ? (Class) ctx : ctx.getClass();
 
         Method m;
         Class[] parameterTypes = null;
 
         /**
          * If we have not cached the method then we need to go ahead and try to resolve it.
          */
         /**
          * Try to find an instance method from the class target.
          */
 
         if ((m = ParseTools.getBestCanadidate(args, name, cls.getMethods())) != null) {
             parameterTypes = m.getParameterTypes();
         }
 
         if (m == null) {
             /**
              * If we didn't find anything, maybe we're looking for the actual java.lang.Class methods.
              */
             if ((m = ParseTools.getBestCanadidate(args, name, cls.getClass().getDeclaredMethods())) != null) {
                 parameterTypes = m.getParameterTypes();
             }
         }
 
 
         if (m == null) {
             StringAppender errorBuild = new StringAppender();
             for (int i = 0; i < args.length; i++) {
                 errorBuild.append(parameterTypes[i] != null ? parameterTypes[i].getClass().getName() : null);
                 if (i < args.length - 1) errorBuild.append(", ");
             }
 
             throw new PropertyAccessException("unable to resolve method: " + cls.getName() + "." + name + "(" + errorBuild.toString() + ") [arglength=" + args.length + "]");
         }
         else {
             if (es != null) {
                 ExecutableStatement cExpr;
                 for (int i = 0; i < es.length; i++) {
                     if ((cExpr = es[i]).getKnownIngressType() == null) {
                         cExpr.setKnownIngressType(parameterTypes[i]);
                         cExpr.computeTypeConversionRule();
                     }
                     if (!cExpr.isConvertableIngressEgress()) {
                         args[i] = DataConversion.convert(args[i], parameterTypes[i]);
                     }
                 }
             }
             else {
                 /**
                  * Coerce any types if required.
                  */
                 for (int i = 0; i < args.length; i++)
                     args[i] = DataConversion.convert(args[i], parameterTypes[i]);
             }
 
 
             if (first) {
                 debug("ALOAD 1");
                 mv.visitVarInsn(ALOAD, 1);
             }
 
             if (m.getParameterTypes().length == 0) {
                 if ((m.getModifiers() & Modifier.STATIC) != 0) {
                     debug("INVOKESTATIC " + m.getName());
                     mv.visitMethodInsn(INVOKESTATIC, getInternalName(m.getDeclaringClass()), m.getName(), getMethodDescriptor(m));
                 }
                 else {
                     debug("CHECKCAST " + getInternalName(m.getDeclaringClass()));
                     mv.visitTypeInsn(CHECKCAST, getInternalName(m.getDeclaringClass()));
 
                     debug("INVOKEVIRTUAL " + m.getName());
                     mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(m.getDeclaringClass()), m.getName(),
                             getMethodDescriptor(m));
                 }
 
                 returnType = m.getReturnType();
 
                 stacksize++;
             }
             else {
                 if ((m.getModifiers() & Modifier.STATIC) == 0) {
                     debug("CHECKCAST " + getInternalName(cls));
                     mv.visitTypeInsn(CHECKCAST, getInternalName(cls));
                 }
 
                 for (int i = 0; i < es.length; i++) {
                     debug("ALOAD 0");
                     mv.visitVarInsn(ALOAD, 0);
 
                     debug("GETFIELD p" + inputs++);
                     mv.visitFieldInsn(GETFIELD, className, "p" + (inputs - 1), "Lorg/mvel/ExecutableStatement;");
 
                     debug("ALOAD 2");
                     mv.visitVarInsn(ALOAD, 2);
 
                     debug("ALOAD 3");
                     mv.visitVarInsn(ALOAD, 3);
 
                     debug("INVOKEINTERFACE ExecutableStatement.getValue");
                     mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(ExecutableStatement.class), "getValue",
                             "(Ljava/lang/Object;Lorg/mvel/integration/VariableResolverFactory;)Ljava/lang/Object;");
 
                     if (parameterTypes[i].isPrimitive()) {
                         unwrapPrimitive(parameterTypes[i]);
                     }
                     else if (preConvArgs[i] == null ||
                             (parameterTypes[i] != String.class &&
                                     !parameterTypes[i].isAssignableFrom(preConvArgs[i].getClass()))) {
 
                         debug("LDC " + getType(parameterTypes[i]));
                         mv.visitLdcInsn(getType(parameterTypes[i]));
 
                         debug("INVOKESTATIC DataConversion.convert");
                         mv.visitMethodInsn(INVOKESTATIC, "org/mvel/DataConversion", "convert",
                                 "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
 
                         debug("CHECKCAST " + getInternalName(parameterTypes[i]));
                         mv.visitTypeInsn(CHECKCAST, getInternalName(parameterTypes[i]));
                     }
                     else if (parameterTypes[i] == String.class) {
                         debug("<<<DYNAMIC TYPE OPTIMIZATION STRING>>");
                         mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
                     }
                     else {
                         debug("<<<DYNAMIC TYPING BYPASS>>>");
                         debug("<<<OPT. JUSTIFICATION " + parameterTypes[i] + "=" + preConvArgs[i].getClass() + ">>>");
 
                         debug("CHECKCAST " + getInternalName(parameterTypes[i]));
                         mv.visitTypeInsn(CHECKCAST, getInternalName(parameterTypes[i]));
                     }
 
                 }
 
                 if ((m.getModifiers() & Modifier.STATIC) != 0) {
                     debug("INVOKESTATIC: " + m.getName());
                     mv.visitMethodInsn(INVOKESTATIC, getInternalName(m.getDeclaringClass()), m.getName(), getMethodDescriptor(m));
                 }
                 else {
                     if (m.getDeclaringClass() != cls && m.getDeclaringClass().isInterface()) {
                         debug("INVOKEINTERFACE: " + getInternalName(m.getDeclaringClass()) + "." + m.getName());
                         mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(m.getDeclaringClass()), m.getName(),
                                 getMethodDescriptor(m));
                     }
                     else {
                         debug("INVOKEVIRTUAL: " + getInternalName(cls) + "." + m.getName());
                         mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(cls), m.getName(),
                                 getMethodDescriptor(m));
                     }
                 }
 
                 returnType = m.getReturnType();
 
                 stacksize++;
             }
 
             //    debug(">>" + m + " :: " + ctx);
 
             return m.invoke(ctx, args);
         }
     }
 
     private static final Method defineClass;
 
     static {
         try {
             defineClass =
                     Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass",
                             new Class[]{String.class, byte[].class, int.class, int.class});
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
 
     }
 
     private static java.lang.Class loadClass(String className, byte[] b)
             throws IllegalAccessException, InvocationTargetException {
         defineClass.setAccessible(true);
         try {
             //noinspection RedundantArrayCreation
             return (Class) defineClass.invoke(ASMAccessorOptimizer.class.getClassLoader(), new Object[]{className, b, 0, (b.length)});
         }
         finally {
             defineClass.setAccessible(false);
         }
     }
 
 
     private static void debug(String instruction) {
         assert ParseTools.debug(instruction);
     }
 
     @SuppressWarnings({"SameReturnValue"})
     public String getName() {
         return "ASM";
     }
 
     public Object getResultOptPass() {
         return val;
     }
 
     private Class getWrapperClass(Class cls) {
         if (cls == boolean.class) {
             return Boolean.class;
         }
         else if (cls == int.class) {
             return Integer.class;
         }
         else if (cls == float.class) {
             return Float.class;
         }
         else if (cls == double.class) {
             return Double.class;
         }
         else if (cls == short.class) {
             return Short.class;
         }
         else if (cls == long.class) {
             return Long.class;
         }
         else if (cls == byte.class) {
             return Byte.class;
         }
         else if (cls == char.class) {
             return Character.class;
         }
 
         return null;
     }
 
     private void unwrapPrimitive(Class cls) {
         if (cls == boolean.class) {
             debug("CHECKCAST java/lang/Boolean");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
             debug("INVOKEVIRTUAL java/lang/Boolean.booleanValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
         }
         else if (cls == int.class) {
             debug("CHECKCAST java/lang/Integer");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
             debug("INVOKEVIRTUAL java/lang/Integer.intValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
         }
         else if (cls == float.class) {
             debug("CHECKCAST java/lang/Float");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
             debug("INVOKEVIRTUAL java/lang/Float.floatValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
         }
         else if (cls == double.class) {
             debug("CHECKCAST java/lang/Double");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
             debug("INVOKEVIRTUAL java/lang/Double.doubleValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
         }
         else if (cls == short.class) {
             debug("CHECKCAST java/lang/Short");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
             debug("INVOKEVIRTUAL java/lang/Short.shortValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
         }
         else if (cls == long.class) {
             debug("CHECKCAST java/lang/Long");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
             debug("INVOKEVIRTUAL java/lang/Long.longValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
         }
         else if (cls == byte.class) {
             debug("CHECKCAST java/lang/Byte");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
             debug("INVOKEVIRTUAL java/lang/Byte.byteValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
         }
         else if (cls == char.class) {
             debug("CHECKCAST java/lang/Character");
             mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
             debug("INVOKEVIRTUAL java/lang/Character.charValue");
             mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
         }
 
     }
 
 
     private void wrapPrimitive(Class<? extends Object> cls) {
         if (cls == boolean.class) {
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
         }
         else if (cls == int.class) {
             debug("INVOKESTATIC java/lang/Integer.valueOf");
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
         }
         else if (cls == float.class) {
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
         }
         else if (cls == double.class) {
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
         }
         else if (cls == short.class) {
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
         }
         else if (cls == long.class) {
             debug("INVOKESTATIC java/lang/Long.valueOf");
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
         }
         else if (cls == byte.class) {
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
         }
         else if (cls == char.class) {
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
         }
     }
 
 
     private void buildInputs() {
         if (inputs == 0) return;
 
         debug("\n{SETTING UP MEMBERS...}\n");
 
         StringAppender constSig = new StringAppender("(");
         int size = inputs;
 
         for (int i = 0; i < size; i++) {
             debug("ACC_PRIVATE p" + i);
             FieldVisitor fv = cw.visitField(ACC_PRIVATE, "p" + i, "Lorg/mvel/ExecutableStatement;", null, null);
             fv.visitEnd();
 
             constSig.append("Lorg/mvel/ExecutableStatement;");
         }
         constSig.append(")V");
 
 
         debug("\n{CREATING INJECTION CONSTRUCTOR}\n");
 
         MethodVisitor cv = cw.visitMethod(ACC_PUBLIC, "<init>", constSig.toString(), null, null);
         cv.visitCode();
         debug("ALOAD 0");
         cv.visitVarInsn(ALOAD, 0);
         debug("INVOKESPECIAL java/lang/Object.<init>");
         cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
 
         for (int i = 0; i < size; i++) {
             debug("ALOAD 0");
             cv.visitVarInsn(ALOAD, 0);
             debug("ALOAD " + (i + 1));
             cv.visitVarInsn(ALOAD, i + 1);
             debug("PUTFIELD p" + i);
             cv.visitFieldInsn(PUTFIELD, className, "p" + i, "Lorg/mvel/ExecutableStatement;");
         }
         debug("RETURN");
         cv.visitInsn(RETURN);
         cv.visitMaxs(0, 0);
         cv.visitEnd();
 
         debug("}");
     }
 
     private int cnum = 3;
     private int lastCnum = 0;
 
     private static final int ARRAY = 0;
     private static final int LIST = 1;
     private static final int MAP = 2;
     private static final int VAL = 3;
 
 
     private int _getAccessor(Object o, int type, int register, int index) {
         int c;
 
         if (o instanceof List) {
             debug("NEW " + LIST_IMPL);
             mv.visitTypeInsn(NEW, LIST_IMPL);
 
             debug("DUP");
             mv.visitInsn(DUP);
 
             intPush(((List) o).size());
             debug("INVOKESPECIAL " + LIST_IMPL + ".<init>");
             mv.visitMethodInsn(INVOKESPECIAL, LIST_IMPL, "<init>", "(I)V");
             debug("ASTORE " + (cnum + 1));
             mv.visitVarInsn(ASTORE, c = ++cnum);
 
             for (Object item : (List) o) {
                 if (_getAccessor(item, LIST, c, 0) != VAL) {
                     debug("ALOAD " + c);
                     mv.visitVarInsn(ALOAD, c);
 
                     debug("ALOAD " + lastCnum);
                     mv.visitVarInsn(ALOAD, lastCnum);
                 }
 
                 debug("INVOKEINTERFACE java/util/List.add");
                 mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
 
                 debug("POP");
                 mv.visitInsn(POP);
             }
 
             lastCnum = c;
             return LIST;
         }
         else if (o instanceof Map) {
             debug("NEW " + MAP_IMPL);
             mv.visitTypeInsn(NEW, MAP_IMPL);
 
             debug("DUP");
             mv.visitInsn(DUP);
 
             intPush(((Map) o).size());
 
             debug("INVOKESPECIAL " + MAP_IMPL + ".<init>");
             mv.visitMethodInsn(INVOKESPECIAL, MAP_IMPL, "<init>", "(I)V");
 
             debug("ASTORE " + (cnum + 1));
             mv.visitVarInsn(ASTORE, c = ++cnum);
 
             int firstParm;
 
             for (Object item : ((Map) o).keySet()) {
                 _getAccessor(item, MAP, c, 0);
                 firstParm = lastCnum;
 
                 _getAccessor(((Map) o).get(item), MAP, c, 0);
                 debug("ALOAD " + c);
                 mv.visitVarInsn(ALOAD, c);
 
                 debug("ALOAD " + firstParm + " (" + ((Map) o).get(item) + ")");
                 mv.visitVarInsn(ALOAD, firstParm);
 
                 debug("ALOAD " + lastCnum + " (" + item + ")");
                 mv.visitVarInsn(ALOAD, lastCnum);
 
 
                 debug("INVOKEINTERFACE java/util/Map.put");
                 mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
 
                 debug("POP");
                 mv.visitInsn(POP);
             }
 
             lastCnum = c;
             return MAP;
         }
         else if (o instanceof Object[]) {
             intPush(((Object[]) o).length);
 
             debug("ANEWARRAY (" + o.hashCode() + ")");
             mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
 
             debug("ASTORE " + (cnum + 1));
             mv.visitVarInsn(ASTORE, c = ++cnum);
 
             int i = 0;
             for (Object item : (Object[]) o) {
                 if (_getAccessor(item, ARRAY, c, i) != VAL) {
                     debug("ALOAD " + c);
                     mv.visitVarInsn(ALOAD, c);
                     intPush(i);
                     debug("ALOAD + " + lastCnum);
                     mv.visitVarInsn(ALOAD, lastCnum);
                 }
 
                 debug("AASTORE (" + o.hashCode() + ")");
                 mv.visitInsn(AASTORE);
                 i++;
             }
 
             lastCnum = c;
             return ARRAY;
         }
         else {
             compiledInputs.add((ExecutableStatement) MVEL.compileExpression((String) o));
 
             switch (type) {
                 case ARRAY:
                     debug("ALOAD " + register);
                     mv.visitVarInsn(ALOAD, register);
                     intPush(index);
                     break;
 
                 case LIST:
                     debug("ALOAD " + register);
                     mv.visitVarInsn(ALOAD, register);
                     break;
 
             }
 
             debug("ALOAD 0");
             mv.visitVarInsn(ALOAD, 0);
 
             debug("GETFIELD p" + inputs++);
             mv.visitFieldInsn(GETFIELD, className, "p" + (inputs - 1), "Lorg/mvel/ExecutableStatement;");
 
             debug("ALOAD 2");
             mv.visitVarInsn(ALOAD, 2);
 
             debug("ALOAD 3");
             mv.visitVarInsn(ALOAD, 3);
 
             debug("INVOKEINTERFACE ExecutableStatement.getValue");
             mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(ExecutableStatement.class), "getValue",
                     "(Ljava/lang/Object;Lorg/mvel/integration/VariableResolverFactory;)Ljava/lang/Object;");
 
 
             switch (type) {
                 case MAP:
                     debug("ASTORE " + (cnum + 1) + "(" + o + ")");
                     mv.visitVarInsn(ASTORE, lastCnum = ++cnum);
             }
 
             return VAL;
         }
     }
 
 
     public Accessor optimizeCollection(char[] property, Object ctx, Object thisRef, VariableResolverFactory factory) {
         this.cursor = 0;
         this.length = (this.expr = property).length;
         this.compiledInputs = new ArrayList<ExecutableStatement>();
 
         this.ctx = ctx;
         this.thisRef = thisRef;
         this.variableFactory = factory;
 
         CollectionParser parser = new CollectionParser();
         Object o = ((List) parser.parseCollection(property)).get(0);
 
         _initJIT();
 
         _getAccessor(o, LIST, 0, 0);
 
         debug("ALOAD 4");
         mv.visitVarInsn(ALOAD, 4);
 
         _finishJIT();
 
         int end = parser.getEnd() + 2;
         try {
             Accessor compiledAccessor = _initializeAccessor();
 
             if (end < property.length) {
                 return new Union(compiledAccessor, subset(property, end));
             }
             else {
                 return compiledAccessor;
             }
 
         }
         catch (Exception e) {
             throw new OptimizationFailure("could not optimize collection", e);
         }
     }
 
     private void intPush(int index) {
         if (index < 6) {
             switch (index) {
                 case 0:
                     debug("ICONST_0");
                     mv.visitInsn(ICONST_0);
                     break;
                 case 1:
                     debug("ICONST_1");
                     mv.visitInsn(ICONST_1);
                     break;
                 case 2:
                     debug("ICONST_2");
                     mv.visitInsn(ICONST_2);
                     break;
                 case 3:
                     debug("ICONST_3");
                     mv.visitInsn(ICONST_3);
                     break;
                 case 4:
                     debug("ICONST_4");
                     mv.visitInsn(ICONST_4);
                     break;
                 case 5:
                     debug("ICONST_5");
                     mv.visitInsn(ICONST_5);
                     break;
             }
         }
         else {
             debug("BIPUSH " + index);
             mv.visitIntInsn(BIPUSH, index);
         }
     }
 
     public Accessor optimizeAssignment(char[] property, Object ctx, Object thisRef, VariableResolverFactory factory) {
         this.length = (this.expr = property).length;
         this.ctx = ctx;
         this.thisRef = thisRef;
         this.variableFactory = factory;
         compiledInputs = new ArrayList<ExecutableStatement>();
 
         _initJIT();
 
         greedy = false;
         String varName = nextToken().getName();
 
         if (!nextToken().isOperator(Operator.ASSIGN))
             throw new OptimizationFailure("expected assignment operator");
      //   greedy = true;
 
         Token valTk = captureTokenToEOS();
         
         ExecutableStatement value;
         if (valTk.isLiteral()) {
             value = new ExecutableLiteral(valTk.getLiteralValue());
         }
         else if (valTk.isNewObject()) {
             value = new ExecutableAccessor(valTk, false);
         }
         else {
             value = (ExecutableStatement) MVEL.compileExpression(valTk.getNameAsArray());
         }
 
         inputs++;
         compiledInputs.add(value);
 
 
         debug("ALOAD 3");
         mv.visitVarInsn(ALOAD, 3);
 
         debug("INVOKESTATIC org/mvel/util/ParseTools.findLocalVariableFactory");
         mv.visitMethodInsn(INVOKESTATIC, "org/mvel/util/ParseTools", "finalLocalVariableFactory", "(Lorg/mvel/integration/VariableResolverFactory;)Lorg/mvel/integration/VariableResolverFactory;");
 
         debug("LDC '" + varName + "'");
         mv.visitLdcInsn(varName);
 
         debug("ALOAD 0: this");
         mv.visitVarInsn(ALOAD, 0);
 
         debug("GETFIELD p0");
         mv.visitFieldInsn(GETFIELD, className, "p0", "Lorg/mvel/ExecutableStatement;");
 
         debug("ALOAD 2");
         mv.visitVarInsn(ALOAD, 2);
 
         debug("ALOAD 3");
         mv.visitVarInsn(ALOAD, 3);
 
         debug("INVOKEINTERFACE org/mvel/ExecutableStatement.getValue");
         mv.visitMethodInsn(INVOKEINTERFACE, "org/mvel/ExecutableStatement", "getValue", "(Ljava/lang/Object;Lorg/mvel/integration/VariableResolverFactory;)Ljava/lang/Object;");
 
         debug("DUP");
         mv.visitInsn(DUP);
 
         debug("ASTORE 4");
         mv.visitVarInsn(ASTORE, 4);
 
         debug("INVOKEINTERFACE org/mvel/integration/VariableResolverFactory.createVariable");
         mv.visitMethodInsn(INVOKEINTERFACE, "org/mvel/integration/VariableResolverFactory", "createVariable", "(Ljava/lang/String;Ljava/lang/Object;)Lorg/mvel/integration/VariableResolver;");
 
         debug("POP");
         mv.visitInsn(POP);
 
         debug("ALOAD 4");
         mv.visitVarInsn(ALOAD, 4);
 
         returnType = Object.class;
 
         try {
             _finishJIT();
             return _initializeAccessor();
         }
         catch (Exception e) {
             throw new OptimizationFailure("could not create assignment", e);
         }
     }
 
     public Accessor optimizeObjectCreation(char[] property, Object ctx, Object thisRef, VariableResolverFactory factory) {
         _initJIT();
 
         compiledInputs = new ArrayList<ExecutableStatement>();
         this.length = (this.expr = property).length;
         this.ctx = ctx;
         this.thisRef = thisRef;
         this.variableFactory = factory;
 
         String[] cnsRes = captureContructorAndResidual(property);
         String[] constructorParms = parseMethodOrConstructor(cnsRes[0].toCharArray());
 
         try {
             if (constructorParms != null) {
                 for (String constructorParm : constructorParms) {
                     compiledInputs.add((ExecutableStatement) MVEL.compileExpression(constructorParm));
                 }
 
                 String s;
 
                 Class cls = Token.LITERALS.containsKey(s = new String(subset(property, 0, findFirst('(', property)))) ?
                         ((Class) Token.LITERALS.get(s)) : ParseTools.createClass(s);
 
                 debug("NEW " + getDescriptor(cls));
                 mv.visitTypeInsn(NEW, getDescriptor(cls));
                 debug("DUP");
                 mv.visitInsn(DUP);
 
                 inputs = constructorParms.length;
 
                 Object[] parms = new Object[constructorParms.length];
 
                 int i = 0;
                 for (ExecutableStatement es : compiledInputs) {
                     parms[i++] = es.getValue(ctx, factory);
                 }
 
                 Constructor cns = getBestConstructorCanadidate(parms, cls);
 
                 if (cns == null)
                     throw new CompileException("unable to find constructor for: " + cls.getName());
 
                 Class tg;
                 for (i = 0; i < constructorParms.length; i++) {
                     debug("ALOAD 0");
                     mv.visitVarInsn(ALOAD, 0);
                     debug("GETFIELD p" + i);
                     mv.visitFieldInsn(GETFIELD, className, "p" + i, "Lorg/mvel/ExecutableStatement;");
                     debug("ALOAD 2");
                     mv.visitVarInsn(ALOAD, 2);
                     debug("ALOAD 3");
                     mv.visitVarInsn(ALOAD, 3);
                     debug("INVOKEINTERFACE org/mvel/ExecutableStatement.getValue");
                     mv.visitMethodInsn(INVOKEINTERFACE, "org/mvel/ExecutableStatement", "getValue", "(Ljava/lang/Object;Lorg/mvel/integration/VariableResolverFactory;)Ljava/lang/Object;");
 
                     tg = cns.getParameterTypes()[i].isPrimitive()
                             ? getWrapperClass(cns.getParameterTypes()[i]) : cns.getParameterTypes()[i];
 
                     if (!parms[i].getClass().isAssignableFrom(cns.getParameterTypes()[i])) {
                         debug("LDC " + getType(tg));
                         mv.visitLdcInsn(getType(tg));
                         debug("INVOKESTATIC org/mvel/DataConversion.convert");
                         mv.visitMethodInsn(INVOKESTATIC, "org/mvel/DataConversion", "convert", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
 
                         if (cns.getParameterTypes()[i].isPrimitive()) {
                             unwrapPrimitive(cns.getParameterTypes()[i]);
                         }
                         else {
                             debug("CHECKCAST " + getInternalName(tg));
                             mv.visitTypeInsn(CHECKCAST, getInternalName(tg));
                         }
 
                     }
                     else {
                         debug("CHECKCAST " + getInternalName(cns.getParameterTypes()[i]));
                         mv.visitTypeInsn(CHECKCAST, getInternalName(cns.getParameterTypes()[i]));
                     }
 
                 }
 
                 debug("INVOKESPECIAL " + getDescriptor(cls) + ".<init> : " + getConstructorDescriptor(cns));
                 mv.visitMethodInsn(INVOKESPECIAL, getDescriptor(cls), "<init>", getConstructorDescriptor(cns));
 
                 _finishJIT();
                 Accessor acc = _initializeAccessor();
 
                 if (cnsRes.length > 1 && cnsRes[1] != null && !cnsRes[1].trim().equals("")) {
                     return new Union(acc, cnsRes[1].toCharArray());
                 }
 
                 return acc;
             }
             else {
                 Class cls = Class.forName(new String(property));
 
                 debug("NEW " + getDescriptor(cls));
                 mv.visitTypeInsn(NEW, getDescriptor(cls));
                 debug("DUP");
                 mv.visitInsn(DUP);
 
                 Constructor cns = cls.getConstructor();
 
                 debug("INVOKESPECIAL <init>");
 
                 mv.visitMethodInsn(INVOKESPECIAL, getDescriptor(cls), "<init>", getConstructorDescriptor(cns));
 
                 _finishJIT();
                 Accessor acc = _initializeAccessor();
 
                 if (cnsRes.length > 1 && cnsRes[1] != null && !cnsRes[1].trim().equals("")) {
                     return new Union(acc, cnsRes[1].toCharArray());
                 }
 
                 return acc;
             }
         }
         catch (Exception e) {
             throw new OptimizationFailure("could not optimize construtor", e);
         }
     }
 
 
     public Accessor optimizeFold(char[] property, Object ctx, Object thisRef, VariableResolverFactory factory) {
         throw new OptimizationNotSupported("JIT does not yet support fold operations.");
     }
 }
