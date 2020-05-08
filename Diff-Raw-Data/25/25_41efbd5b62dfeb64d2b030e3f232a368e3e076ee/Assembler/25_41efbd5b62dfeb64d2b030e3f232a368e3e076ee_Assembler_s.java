 package org.libj.xquery.compiler;
 
 import static org.libj.xquery.compiler.Constants.*;
 
 import org.libj.xquery.lisp.Cons;
 import org.libj.xquery.namespace.*;
 import static org.libj.xquery.lexer.TokenType.*;
 
 import org.libj.xquery.parser.AST;
 import org.objectweb.asm.*;
 
 public class Assembler implements Opcodes {
     private Cons ast;
     private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
     private String compiledClassName;
     private String[] vars;
     private final String evalMainMethodName = "eval_";
     private final String evalMainMethodSignature;
     private final String evalMethodSignature;
     private final String evalCallbackMethodSignature;
 
     private Namespace namespace;
     private final String XML_FACTORY_IMPLEMENTATION;
 
     private MethodVisitor mv;
 
     public Assembler(String className, Cons ast, String[] vars, Namespace root, Class xmlFactory) {
         this.compiledClassName = className.replace('.', '/');
         this.ast = ast;
         this.vars = vars != null ? vars : new String[0];
         this.namespace = root;
         XML_FACTORY_IMPLEMENTATION = xmlFactory.getName().replace('.', '/');
         String varSignature = "";
         for (int i = 0; i < vars.length; i++) {
             varSignature += "Ljava/lang/Object;";
         }
         evalMainMethodSignature = "(L"+CALLBACK+";L"+ENVIRONMENT+";"+varSignature+")Ljava/lang/Object;";
         evalMethodSignature = "("+varSignature+")Ljava/lang/Object;";
         evalCallbackMethodSignature = "(L"+CALLBACK+";"+varSignature+")V";
         visitClass();
     }
 
     public Assembler(String className, Cons ast, String[] vars) {
         this(className, ast, vars, new DefaultRootNamespace(), DEFAUL_XML_FACTORY_IMPLEMENTATION_CLASS);
     }
 
     public Assembler(String className, Cons ast) {
         this(className, ast, null);
     }
 
     private void visitClass() {
         cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, compiledClassName, null,
                 SUPER_CLASS,
                 QUERY_INTERFACE_CLASS);
         visitInit();
         visitFactory();
 //        visitNamespaces();
         visitEvalMain();
         visitEvalWithEnvironment();
         visitEval();
         visitEvalCallback();
         visitEvalCallbackEnvironment();
 //        visitCallbackToList();
         cw.visitEnd();
     }
 
     private void visitFactory() {
         FieldVisitor fv = cw.visitField(ACC_PRIVATE, "xmlFactory", "L"+ XML_FACTORY +";", null, null);
         fv.visitEnd();
 
         mv = cw.visitMethod(ACC_PRIVATE, "toXML", "(Ljava/lang/String;)L"+ XML_INTERFACE +";", null, null);
         mv.visitCode();
         // if xmlFactory == null
         mv.visitVarInsn(ALOAD, 0);
         mv.visitFieldInsn(GETFIELD, compiledClassName, "xmlFactory", "L"+ XML_FACTORY +";");
         Label endIf = new Label();
         mv.visitJumpInsn(IFNONNULL, endIf);
         // init xmlFactory
         mv.visitVarInsn(ALOAD, 0);
         newObject(XML_FACTORY_IMPLEMENTATION);
         mv.visitFieldInsn(PUTFIELD, compiledClassName, "xmlFactory", "L"+ XML_FACTORY +";");
         // register namespaces
         for (Object declare: Cons.rest(AST.nthAST(ast, 1))) {
            if (AST.getNodeType(AST.nthAST(((Cons) declare), 1)) == NAMESPACE) {
                 String alias = (String) AST.nthAST(((Cons) declare), 2).second();
                 String uri = (String) AST.nthAST(((Cons) declare), 3).second();
                 mv.visitVarInsn(ALOAD, 0);
                 mv.visitFieldInsn(GETFIELD, compiledClassName, "xmlFactory", "L"+ XML_FACTORY +";");
                 pushConst(alias);
                 pushConst(uri);
                 mv.visitMethodInsn(INVOKEINTERFACE, XML_FACTORY, "registerNamespace", "(Ljava/lang/String;Ljava/lang/String;)V");
             }
         }
         mv.visitLabel(endIf);
         // enf if
         mv.visitVarInsn(ALOAD, 0);
         mv.visitFieldInsn(GETFIELD, compiledClassName, "xmlFactory", "L" + XML_FACTORY + ";");
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEINTERFACE, XML_FACTORY, "toXML", "(Ljava/lang/String;)L" + XML_INTERFACE + ";");
         mv.visitInsn(ARETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
     private void visitNamespaces() {
         mv = cw.visitMethod(ACC_PROTECTED, "initNamespaces", "()V", null, null);
         mv.visitCode();
 
         for (Object declare: Cons.rest(AST.nthAST(ast, 1))) {
             if (AST.getNodeType(AST.nthAST(((Cons) declare), 1)) == NAMESPACE) {
                 String alias = (String) AST.nthAST(((Cons) declare), 2).second();
                 String uri = (String) AST.nthAST(((Cons) declare), 3).second();
                 mv.visitVarInsn(ALOAD, 0);
                 pushConst(alias);
                 pushConst(uri);
                 mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, "registerNamespace", "(Ljava/lang/String;Ljava/lang/String;)V");
             }
         }
 
         mv.visitInsn(RETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
     private void visitInit() {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitMethodInsn(INVOKESPECIAL, SUPER_CLASS, "<init>", "()V");
         mv.visitInsn(RETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     //////////////////////////////////////////////////
     /// eval(Callback, Environment, ...)
     //////////////////////////////////////////////////
 
     private void visitEvalMain() {
 //        mv = cw.visitMethod(ACC_PUBLIC, "eval", "(L"+ ENVIRONMENT +";L"+ CALLBACK +";)Ljava/lang/Object;", null, null);
         mv = cw.visitMethod(ACC_PRIVATE, evalMainMethodName, evalMainMethodSignature, null, null);
         mv.visitCode();
         Class returnType = visitAST();
         if (returnType.isPrimitive()) {
             Caster.cast(mv, returnType, Object.class);
             mv.visitInsn(ARETURN);
         }
         else {
             Label returnLabel = new Label();
             mv.visitInsn(DUP);
             mv.visitJumpInsn(IFNONNULL, returnLabel);
             mv.visitInsn(POP);
             pushNilObject();
             mv.visitLabel(returnLabel);
             mv.visitInsn(ARETURN);
         }
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
     private Class visitAST() {
         Cons declares = AST.nthAST(ast, 1);
         Cons code = AST.nthAST(ast, 2);
         visitDeclares(declares);
         return new EvalAssembler(mv, compiledClassName, vars, namespace).visit(code);
     }
 
     private void visitDeclares(Cons declares) {
         for (Object declare: Cons.rest(declares)) {
             visitDeclare((Cons) declare);
         }
     }
 
     private void visitDeclare(Cons declare) {
        if (AST.getNodeType(AST.nthAST(declare, 1)) == NAMESPACE) {
             visitDeclareNamespace(declare);
         }
     }
 
     private void visitDeclareNamespace(Cons declare) {
         String alias = (String) AST.nthAST(declare, 2).second();
         String uri = (String) AST.nthAST(declare, 3).second();
         if (uri.startsWith("class:")) {
             namespace.register(alias, namespace.lookup(uri));
         }
         else if (uri.startsWith("http:")) {
             namespace.register(alias, new URI(uri));
         }
         else {
             throw new RuntimeException("Not Implemented declare namespace: "+uri);
         }
     }
 
 
     //////////////////////////////////////////////////
     /// eval(Environment)
     //////////////////////////////////////////////////
     private void visitEvalWithEnvironment() {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", "(L"+ ENVIRONMENT +";)Ljava/lang/Object;", null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitInsn(ACONST_NULL); // callback
         mv.visitVarInsn(ALOAD, 1); // environment
         for (int i = 0; i < vars.length; i++) {
             mv.visitVarInsn(ALOAD, 1);
             mv.visitLdcInsn("$"+vars[i]);
             mv.visitMethodInsn(INVOKEVIRTUAL, ENVIRONMENT, "getVariable", "(Ljava/lang/String;)Ljava/lang/Object;");
         }
         mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, evalMainMethodName, evalMainMethodSignature);
         mv.visitInsn(ARETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
 
     //////////////////////////////////////////////////
     /// eval
     //////////////////////////////////////////////////
 
     private void visitEval() {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", evalMethodSignature, null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitInsn(ACONST_NULL);
         mv.visitInsn(ACONST_NULL);
         for (int i = 0; i < vars.length; i++) {
             mv.visitVarInsn(ALOAD, i+1);
         }
         mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, evalMainMethodName, evalMainMethodSignature);
         mv.visitInsn(ARETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
     //////////////////////////////////////////////////
     /// eval(Callback)
     //////////////////////////////////////////////////
     private void visitEvalCallback() {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", evalCallbackMethodSignature, null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitVarInsn(ALOAD, 1); // callback
         mv.visitInsn(ACONST_NULL); // env
         for (int i = 0; i < vars.length; i++) {
             mv.visitVarInsn(ALOAD, i+2);
         }
         mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, evalMainMethodName, evalMainMethodSignature);
 //        mv.visitInsn(POP);
         mv.visitInsn(RETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
 
     //////////////////////////////////////////////////
     /// eval(Callback)
     //////////////////////////////////////////////////
     private void visitEvalCallbackEnvironment() {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", "(L"+ CALLBACK +";L"+ ENVIRONMENT +";)V", null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitVarInsn(ALOAD, 1); // callback
         mv.visitVarInsn(ALOAD, 2); // environment
         for (int i = 0; i < vars.length; i++) {
             mv.visitVarInsn(ALOAD, 2);
             mv.visitLdcInsn("$"+vars[i]);
             mv.visitMethodInsn(INVOKEVIRTUAL, ENVIRONMENT, "getVariable", "(Ljava/lang/String;)Ljava/lang/Object;");
         }
         mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, evalMainMethodName, evalMainMethodSignature);
 //        mv.visitInsn(POP);
         mv.visitInsn(RETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
     //////////////////////////////////////////////////
     /// List callbackToList(Callback)
     //////////////////////////////////////////////////
     private void visitCallbackToList() {
         MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "callbackToList", "(L"+ CALLBACK +";)L"+ LIST_INTERFACE +";", null, null);
         mv.visitCode();
         /*
         if (callback == null) {
             return new List();
         }
         if (callback instanceof List) {
             return callback;
         }
         return new CallbackList(callback);
          */
         Label notNull = new Label();
         Label notList = new Label();
         mv.visitVarInsn(ALOAD, 1);
         mv.visitJumpInsn(IFNONNULL, notNull);
         mv.visitTypeInsn(NEW, LIST_IMPLEMENTATION);
         mv.visitInsn(DUP);
         mv.visitMethodInsn(INVOKESPECIAL, LIST_IMPLEMENTATION, "<init>", "()V");
         mv.visitInsn(ARETURN);
         mv.visitLabel(notNull);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitTypeInsn(INSTANCEOF, LIST_INTERFACE);
         mv.visitJumpInsn(IFEQ, notList);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitInsn(ARETURN);
         mv.visitLabel(notList);
         mv.visitTypeInsn(NEW, CALLBACK_LIST);
         mv.visitInsn(DUP);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKESPECIAL, CALLBACK_LIST, "<init>", "(L"+ CALLBACK +";)V");
         mv.visitInsn(ACONST_NULL);
         mv.visitInsn(ARETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
     }
 
     //////////////////////////////////////////////////
     /// helper
     //////////////////////////////////////////////////
 
     private Class cast(Class source, Class target) {
         return Caster.cast(mv, source, target);
     }
 
     private void pushConst(int n) {
         switch (n) {
             case -1:
                 mv.visitInsn(ICONST_M1);
                 return;
             case 0:
                 mv.visitInsn(ICONST_0);
                 return;
             case 1:
                 mv.visitInsn(ICONST_1);
                 return;
             case 2:
                 mv.visitInsn(ICONST_2);
                 return;
             case 3:
                 mv.visitInsn(ICONST_3);
                 return;
             case 4:
                 mv.visitInsn(ICONST_4);
                 return;
             case 5:
                 mv.visitInsn(ICONST_5);
                 return;
         }
         if (-0x80 <= n && n <= 0x7f) {
             mv.visitIntInsn(BIPUSH, n);
         }
         else if (-0x8000 <= n && n <= 0x7fff) {
             mv.visitIntInsn(SIPUSH, n);
         }
         else {
             mv.visitLdcInsn(n);
         }
     }
 
     private void pushConst(Object o) {
         mv.visitLdcInsn(o);
     }
 
     private void newObject(String className) {
         mv.visitTypeInsn(NEW, className);
         mv.visitInsn(DUP);
         mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
     }
 
     private void pushNilObject() {
 //        newObject(NIL);
         mv.visitFieldInsn(GETSTATIC, NIL, "NIL", "L"+NIL+";");
     }
 
     private void log(String message) {
         mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
         mv.visitLdcInsn(message);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
     }
 
     private void log() {
         mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
         mv.visitInsn(SWAP);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
     }
 
     //////////////////////////////////////////////////
     /// public API
     //////////////////////////////////////////////////
 
     public byte[] toByteArray() {
         return cw.toByteArray();
     }
 }
