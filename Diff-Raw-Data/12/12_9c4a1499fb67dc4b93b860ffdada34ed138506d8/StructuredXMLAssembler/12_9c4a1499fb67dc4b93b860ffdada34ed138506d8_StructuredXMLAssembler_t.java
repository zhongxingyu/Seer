 package org.libj.xquery.compiler;
 
 import org.libj.xquery.lexer.TokenType;
 import org.libj.xquery.lisp.Cons;
 import org.libj.xquery.lisp.Fn;
 import org.libj.xquery.lisp.Pred;
 import org.libj.xquery.parser.AST;
 import org.libj.xquery.xml.NilXML;
 import org.libj.xquery.xml.XMLUtils;
 import org.libj.xquery.xml.str.SharedNamespace;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static org.libj.xquery.compiler.Constants.*;
 import static org.libj.xquery.lisp.Cons.*;
 
 public class StructuredXMLAssembler implements Opcodes {
     private EvalAssembler outer;
     private MethodVisitor mv;
     private String compiledClassName;
     private ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>();
     private int classIndex;
     private int nodeID = 0;
     private HashMap<Integer, ClassInfo> nodeClass = new HashMap<Integer, ClassInfo>();
 
     StructuredXMLAssembler(EvalAssembler outer, MethodVisitor mv, String compiledClassName) {
         this.outer = outer;
         this.mv = mv;
         this.compiledClassName = compiledClassName;
         this.classIndex = outer.getInnerClasses().size() + 1;
     }
 
     public ArrayList<ClassInfo> getClasses() {
         return classes;
     }
 
     void visitElement(final Cons expr) {
         Cons nexpr = replaceFields(annotateNamespace(expr));
         ClassInfo c = compileToClass(nexpr);
         ArrayList fields = collectFields(expr);
         if (fields.isEmpty()) {
             mv.visitFieldInsn(GETSTATIC, c.getClassName(), "INSTANCE", "L" + XML_INTERFACE + ";");
         }
         else {
             mv.visitTypeInsn(NEW, c.getClassName());
             mv.visitInsn(DUP);
             StringBuilder args = new StringBuilder();
             for (Object x: fields) {
                 Class t = outer.visitExpr((Cons) x);
                 Caster.cast(mv, t, Object.class);
                 args.append("Ljava/lang/Object;");
             }
             mv.visitMethodInsn(INVOKESPECIAL, c.getClassName(), "<init>", "("+args.toString()+")V");
         }
     }
 
     private Cons annotateNamespace(Cons expr) {
         return annotateNamespace(expr, null);
     }
 
     private Cons annotateNamespace(Cons expr, SharedNamespace parent) {
         final SharedNamespace namespace = new SharedNamespace(parent);
         Cons attrs = (Cons) expr.third();
         if (attrs != null) {
             for (Object x: attrs) {
                 Cons a = (Cons) x;
                 String k = (String) a.first();
                 Cons values = (Cons) a.second();
                 if (values.size() == 1 && values.first() instanceof String) {
                     String v = (String) values.first();
                     if (k.equals("xmlns")) {
                         namespace.setDefaultNamespace(v);
                     }
                     else if (k.startsWith("xmlns:")) {
                         k = k.substring(6);
                         namespace.put(k, v);
                     }
                 }
             }
         }
 
         String tagName = (String) expr.second();
         String prefix = null;
         int at = tagName.indexOf(':');
         if (at != -1) {
             prefix = tagName.substring(0, at);
             tagName = tagName.substring(at+1);
         }
         String ns = namespace.getDefaultNamespace();
         if (prefix != null) {
             ns = namespace.get(prefix);
         }
         Cons contents = (Cons) expr.nth(3);
         contents = map(new Fn() {
             public Object call(Object x) {
                 if (isElement(x)) {
                     return annotateNamespace((Cons) x, namespace);
                 }
                 else {
                     return x;
                 }
             }
         }, contents);
         return concat(expr.assoc(3, contents), list(tagName, ns, nodeID++));
     }
 
     private static Cons replaceFields(Cons expr) {
         return replaceFields(expr, new AtomicInteger(0));
     }
 
     private static Cons replaceFields(Cons expr, final AtomicInteger index) {
         Cons attrs = (Cons) expr.third();
         if (attrs != null) {
             Cons newAttrs = null;
             for (Object x: attrs) {
                 Cons a = (Cons) x;
                 Cons values = (Cons) a.second();
                 Cons newValues = null;
                 for (Object v: values) {
                     newValues = cons(v instanceof String ? v : index.getAndIncrement(), newValues);
                 }
                 newValues = reverse(newValues);
                 newAttrs = cons(list(a.first(), newValues), newAttrs);
             }
            attrs = reverse(newAttrs);
         }
 
         Cons contents = (Cons) expr.nth(3);
         if (contents != null) {
 
         }
         contents = Cons.map(new Fn() {
             public Object call(Object x) {
                 if (isElement(x)) {
                     return replaceFields((Cons) x, index);
                 } else if (x instanceof Cons) {
                     return index.getAndIncrement();
                 } else {
                     return x;
                 }
             }
         }, contents);
         return expr.assoc(2, attrs).assoc(3, contents);
     }
 
     private static Cons resetFields(Cons expr) {
         ArrayList<Integer> fields = collectFields(expr);
         if (fields.isEmpty()) {
             return expr;
         }
         int start = fields.get(0);
         return resetFields(expr, start);
     }
 
     private static Cons resetFields(Cons expr, final int start) {
         Cons attrs = (Cons) expr.third();
         if (attrs != null) {
             Cons newAttrs = null;
             for (Object x: attrs) {
                 Cons a = (Cons) x;
                 Cons values = (Cons) a.second();
                 Cons newValues = null;
                 for (Object v: values) {
                     newValues = cons(v instanceof String ? v : (Integer)v - start, newValues);
                 }
                 newValues = reverse(newValues);
                 newAttrs = cons(list(a.first(), newValues), newAttrs);
             }
            attrs = reverse(newAttrs);
         }
 
         Cons contents = (Cons) expr.nth(3);
         if (contents != null) {
 
         }
         contents = Cons.map(new Fn() {
             public Object call(Object x) {
                 if (isElement(x)) {
                     return resetFields((Cons) x, start);
                 } else if (x instanceof Cons) {
                     return (Integer)x - start;
                 } else {
                     return x;
                 }
             }
         }, contents);
         return expr.assoc(2, attrs).assoc(3, contents);
     }
 
     private static int countFields(Cons expr) {
         Cons attrs = (Cons) expr.nth(2);
         Cons contents = (Cons) expr.nth(3);
         int count = 0;
         if (attrs != null) {
             for (Object x: attrs) {
                 count += countFieldsInValues((Cons) ((Cons)x).second());
             }
         }
         count += countFieldsInValues(contents);
         return count;
     }
 
     private static int countFieldsInValues(Cons values) {
         if (values == null) {
             return 0;
         }
         int count = 0;
         for (Object x: values) {
             if (isElement(x)) {
                 count += countFields((Cons) x);
             }
             else if (x instanceof Cons) {
                 count++;
             }
             else if (x instanceof Integer) {
                 count++;
             }
         }
         return count;
     }
 
     private static ArrayList collectFields(Cons expr) {
         ArrayList fields = new ArrayList();
         collectFields(expr, fields);
         return fields;
     }
 
     private static void collectFields(Cons expr, ArrayList fields) {
         Cons attrs = (Cons) expr.nth(2);
         Cons contents = (Cons) expr.nth(3);
         if (attrs != null) {
             for (Object x: attrs) {
                 collectFieldsInValues((Cons) ((Cons) x).second(), fields);
             }
         }
         collectFieldsInValues(contents, fields);
     }
 
     private static void collectFieldsInValues(Cons values, ArrayList fields) {
         if (values == null) {
             return;
         }
         for (Object x: values) {
             if (isElement(x)) {
                 collectFields((Cons) x, fields);
             }
             else if (x instanceof Cons) {
                 fields.add(x);
             }
             else if (x instanceof Integer) {
                 fields.add(x);
             }
             else if (x instanceof String) {
                 // pass
             }
             else {
                 throw new RuntimeException("Not Implemented!");
             }
         }
     }
 
     private ClassInfo compileToClass(Cons expr) {
         expr = resetFields(expr);
         ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
         String className = compiledClassName + "$" + (classIndex++);
         int fieldsNumber = countFields(expr);
         visitInit(cw, className, fieldsNumber);
         visitEval(cw);
         visitAttr(cw, className, (Cons) expr.third());
         visitTags(cw, className, (Cons) expr.nth(3));
         visitText(cw, className, (Cons) expr.nth(3));
         visitToString(cw, className, expr);
         if (fieldsNumber == 0) {
             visitInstance(cw, className);
         }
 
         cw.visitEnd();
         ClassInfo c = new ClassInfo(className, cw.toByteArray());
         classes.add(c);
         int node = (Integer)expr.nth(6);
         if (nodeClass.get(node) != null) {
             throw new RuntimeException("Coding error!");
         }
         nodeClass.put(node, c);
         return c;
     }
 
     private static void visitInit(ClassWriter cw, String className, int fieldsNumber) {
         cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className, null,
                 classToSignature(Object.class),
                 new String[]{XML_INTERFACE});
 
         StringBuilder init = new StringBuilder();
         for (int i = 0; i < fieldsNumber; i++) {
             cw.visitField(ACC_PRIVATE, "field"+i, "Ljava/lang/Object;", null, null);
             init.append("Ljava/lang/Object;");
         }
 
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + init + ")V", null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
 
         for (int i = 0; i < fieldsNumber; i++) {
             mv.visitVarInsn(ALOAD, 0);
             mv.visitVarInsn(ALOAD, i + 1);
             mv.visitFieldInsn(PUTFIELD, className, "field" + i, "Ljava/lang/Object;");
         }
         mv.visitInsn(RETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     private void visitEval(ClassWriter cw) {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", "(Ljava/lang/String;)L"+XML_INTERFACE+";", null, null);
         mv.visitInsn(ACONST_NULL);
         mv.visitInsn(ARETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     private void visitAttr(ClassWriter cw, String className, Cons attrs) {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getAttribute", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
         mv.visitCode();
         if (attrs != null) {
             for (Object x: attrs) {
                 Cons a = (Cons) x;
                 String name = (String) a.first();
                 mv.visitLdcInsn(name);
                 mv.visitVarInsn(ALOAD, 1);
                 mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                 Label next = new Label();
                 mv.visitJumpInsn(IFEQ, next);
                 // find attr!
                 Cons values = (Cons) a.second();
                 if (values.size() == 1) {
                     Object v = values.first();
                     if (v instanceof String) {
                         mv.visitLdcInsn(v);
                     }
                     else {
                         if (!(v instanceof Integer)) {
                             throw new RuntimeException("Not Implemented!");
                         }
                         mv.visitVarInsn(ALOAD, 0);
                         mv.visitFieldInsn(GETFIELD, className, "field"+v, "Ljava/lang/Object;");
                         Label nonNull = new Label();
                         mv.visitJumpInsn(IFNONNULL, nonNull);
                         mv.visitLdcInsn("");
                         mv.visitInsn(ARETURN);
                         mv.visitLabel(nonNull);
                         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                         mv.visitVarInsn(ALOAD, 0);
                         mv.visitFieldInsn(GETFIELD, className, "field" + v, "Ljava/lang/Object;");
                         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
                     }
                 }
                 else {
                     throw new RuntimeException("Not Implemented: a='x{1}x'");
                 }
                 mv.visitInsn(ARETURN);
                 mv.visitLabel(next);
                 mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
             }
         }
         mv.visitInsn(ACONST_NULL);
         mv.visitInsn(ARETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     private void visitText(ClassWriter cw, String className, Cons contents) {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "text", "()Ljava/lang/String;", null, null);
         mv.visitCode();
         if (contents != null) {
             if (contents.size() == 1) {
                 Object v = contents.first();
                 if (v instanceof String) {
                     mv.visitLdcInsn(XMLUtils.unescapeXML((String) v));
                 }
                 else if (v instanceof Integer) {
                     mv.visitVarInsn(ALOAD, 0);
                     mv.visitFieldInsn(GETFIELD, className, "field"+v, "Ljava/lang/Object;");
                     Label nonNull = new Label();
                     mv.visitJumpInsn(IFNONNULL, nonNull);
                     mv.visitLdcInsn("");
                     mv.visitInsn(ARETURN);
                     mv.visitLabel(nonNull);
                     mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                     mv.visitVarInsn(ALOAD, 0);
                     mv.visitFieldInsn(GETFIELD, className, "field" + v, "Ljava/lang/Object;");
                     // XXX: should I use x.toString(), or Fn.string(x)?
                     mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
                 } else if (isElement(v)) {
                     Cons element = (Cons) v;
                     int node = (Integer)element.nth(6);
                     ClassInfo c = nodeClass.get(node);
                     newXML(mv, className, c, element);
                     mv.visitMethodInsn(INVOKEINTERFACE, XML_INTERFACE, "text", "()Ljava/lang/String;");
                 } else {
                     throw new RuntimeException("Not Implemented: "+v);
                 }
             }
             else {
                 StringXMLAssembler.newStringBuilder(mv, "java/lang/StringBuilder");
                 for (Object x: contents) {
                     if (x instanceof String) {
                         mv.visitLdcInsn(XMLUtils.unescapeXML((String) x));
                         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                     }
                     else if (isElement(x)) {
                         Cons element = (Cons) x;
                         int node = (Integer)element.nth(6);
                         ClassInfo c = nodeClass.get(node);
                         newXML(mv, className, c, element);
                         mv.visitMethodInsn(INVOKEINTERFACE, XML_INTERFACE, "text", "()Ljava/lang/String;");
                         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                     }
                     else {
                         int index = (Integer) x;
                         mv.visitVarInsn(ALOAD, 0);
                         mv.visitFieldInsn(GETFIELD, className, "field" + index, "Ljava/lang/Object;");
                         // XXX: should I use x.toString(), or Fn.string(x)?
                         StringXMLAssembler.appendStringBuilder(mv, Object.class);
                     }
                 }
                 StringXMLAssembler.stringBuilderToString(mv);
             }
         }
         else {
             mv.visitLdcInsn("");
         }
         mv.visitInsn(ARETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     private static boolean isElement(Object v) {
         return v instanceof Cons && AST.getNodeType((Cons) v) == TokenType.ELEMENT;
     }
 
     private void visitTags(ClassWriter cw, String className, Cons contents) {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getElementsByTagNameNS", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", null, null);
         mv.visitCode();
 
         contents = filter(new Pred() {
             public boolean call(Object x) {
                 return isElement(x);
             }
         }, contents);
 
         if (contents != null ) {
             for (Object x: contents) {
                 Cons element = (Cons) x;
                 String tag = (String) element.nth(4);
                 String ns = (String) element.nth(5);
                 // compare
                 Label next = new Label();
                 mv.visitLdcInsn(tag);
                 mv.visitVarInsn(ALOAD, 2);
                 mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                 mv.visitJumpInsn(IFEQ, next);
                 if (ns == null) {
                     mv.visitVarInsn(ALOAD, 1);
                     mv.visitJumpInsn(IFNONNULL, next);
                 }
                 else {
                     mv.visitLdcInsn(ns);
                     mv.visitVarInsn(ALOAD, 1);
                     mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                     mv.visitJumpInsn(IFEQ, next);
                 }
                 // find tag!
                 ClassInfo c = compileToClass(element);
                 newXML(mv, className, c, element);
                 
                 mv.visitInsn(ARETURN);
                 mv.visitLabel(next);
                 mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
             }
         }
 
         mv.visitFieldInsn(GETSTATIC, classToSignature(NilXML.class), "NIL", "L" + classToSignature(NilXML.class) + ";");
         mv.visitInsn(ARETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     private static void newXML(MethodVisitor mv, String outerXMLClassName, ClassInfo c, Cons element) {
         ArrayList<Integer> fields = collectFields(element);
         if (fields.isEmpty()) {
             mv.visitFieldInsn(GETSTATIC, c.getClassName(), "INSTANCE", "L"+XML_INTERFACE+";");
         }
         else {
             mv.visitTypeInsn(NEW, c.getClassName());
             mv.visitInsn(DUP);
             StringBuilder args = new StringBuilder();
             for (int i: fields) {
                 mv.visitVarInsn(ALOAD, 0);
                 mv.visitFieldInsn(GETFIELD, outerXMLClassName, "field"+i, "Ljava/lang/Object;");
                 args.append("Ljava/lang/Object;");
             }
             mv.visitMethodInsn(INVOKESPECIAL, c.getClassName(), "<init>", "("+args.toString()+")V");
         }
     }
 
     private void visitToString(ClassWriter cw, String className, Cons expr) {
         MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
         mv.visitCode();
 
         ArrayList subs = StringXMLAssembler.toList(expr);
         if (subs.size() == 1) {
             String singleton = (String) subs.get(0);
             mv.visitLdcInsn(singleton);
         }
         else {
             StringXMLAssembler.newStringBuilder(mv, "java/lang/StringBuilder");
             for (Object x: subs) {
                 if (x instanceof String) {
                     mv.visitLdcInsn(x);
                     mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                 }
                 else {
                     int index = (Integer) x;
                     mv.visitVarInsn(ALOAD, 0);
                     mv.visitFieldInsn(GETFIELD, className, "field"+index, "Ljava/lang/Object;");
                     StringXMLAssembler.appendStringBuilder(mv, Object.class);
                 }
             }
             StringXMLAssembler.stringBuilderToString(mv);
         }
 
         mv.visitInsn(ARETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
     private void visitInstance(ClassWriter cw, String className) {
         cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "INSTANCE", "L"+XML_INTERFACE+";", null, null).visitEnd();
         MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
         mv.visitCode();
         mv.visitTypeInsn(NEW, className);
         mv.visitInsn(DUP);
         mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
         mv.visitFieldInsn(PUTSTATIC, className, "INSTANCE", "L"+XML_INTERFACE+";");
         mv.visitInsn(RETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
     }
 
 }
