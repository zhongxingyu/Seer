 import java.io.*;
 import java.util.*;
 import org.objectweb.asm.*;
 //import org.objectweb.asm.attrs.*;
 public class IndyDump implements Opcodes {
 
 public static byte[] dump () throws Exception {
 
 ClassWriter cw = new ClassWriter(0);
 FieldVisitor fv;
 MethodVisitor mv;
 AnnotationVisitor av0;
 
 cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "Hello", null, "org/python/core/IndyFunctionTable", new String[] { "org/python/core/PyRunnable" });
 
 {
 av0 = cw.visitAnnotation("Lorg/python/compiler/APIVersion;", true);
 av0.visit("value", new Integer(26));
 av0.visitEnd();
 }
 {
 av0 = cw.visitAnnotation("Lorg/python/compiler/MTime;", true);
 av0.visit("value", new Long(1253627083000L));
 av0.visitEnd();
 }
 {
 fv = cw.visitField(ACC_FINAL + ACC_STATIC, "self", "LHello;", null, null);
 fv.visitEnd();
 }
 {
 fv = cw.visitField(ACC_FINAL + ACC_STATIC, "_2", "Lorg/python/core/PyString;", null, null);
 fv.visitEnd();
 }
 {
 fv = cw.visitField(ACC_FINAL + ACC_STATIC, "_1", "Lorg/python/core/PyString;", null, null);
 fv.visitEnd();
 }
 {
 fv = cw.visitField(ACC_FINAL + ACC_STATIC, "_0", "Lorg/python/core/PyString;", null, null);
 fv.visitEnd();
 }
 {
 fv = cw.visitField(ACC_FINAL + ACC_STATIC, "f$0", "Lorg/python/core/PyCode;", null, null);
 fv.visitEnd();
 }
 {
 fv = cw.visitField(ACC_FINAL + ACC_STATIC, "greet$1", "Lorg/python/core/PyCode;", null, null);
 fv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC, "f$0", "(Lorg/python/core/PyFrame;Lorg/python/core/ThreadState;)Lorg/python/core/PyObject;", null, null);
 mv.visitCode();
 mv.visitVarInsn(ALOAD, 1);
 mv.visitInsn(ICONST_1);
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyFrame", "setline", "(I)V");
 mv.visitFieldInsn(GETSTATIC, "org/python/core/Py", "EmptyObjects", "[Lorg/python/core/PyObject;");
 mv.visitVarInsn(ASTORE, 3);
 mv.visitTypeInsn(NEW, "org/python/core/PyFunction");
 mv.visitInsn(DUP);
 mv.visitVarInsn(ALOAD, 1);
 mv.visitFieldInsn(GETFIELD, "org/python/core/PyFrame", "f_globals", "Lorg/python/core/PyObject;");
 mv.visitVarInsn(ALOAD, 3);
 mv.visitFieldInsn(GETSTATIC, "Hello", "greet$1", "Lorg/python/core/PyCode;");
 mv.visitInsn(ACONST_NULL);
 mv.visitMethodInsn(INVOKESPECIAL, "org/python/core/PyFunction", "<init>", "(Lorg/python/core/PyObject;[Lorg/python/core/PyObject;Lorg/python/core/PyCode;Lorg/python/core/PyObject;)V");
 mv.visitVarInsn(ASTORE, 3);
 mv.visitVarInsn(ALOAD, 1);
 mv.visitLdcInsn("greet");
 mv.visitVarInsn(ALOAD, 3);
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyFrame", "setlocal", "(Ljava/lang/String;Lorg/python/core/PyObject;)V");
 mv.visitInsn(ACONST_NULL);
 mv.visitVarInsn(ASTORE, 3);
 mv.visitVarInsn(ALOAD, 1);
 mv.visitInsn(ICONST_4);
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyFrame", "setline", "(I)V");
 mv.visitVarInsn(ALOAD, 1);
 mv.visitLdcInsn("greet");
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyFrame", "getname", "(Ljava/lang/String;)Lorg/python/core/PyObject;");
 mv.visitVarInsn(ALOAD, 2);
 mv.visitFieldInsn(GETSTATIC, "Hello", "_2", "Lorg/python/core/PyString;");
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyObject", "__call__", "(Lorg/python/core/ThreadState;Lorg/python/core/PyObject;)Lorg/python/core/PyObject;");
 mv.visitInsn(POP);
 mv.visitVarInsn(ALOAD, 1);
 mv.visitInsn(ICONST_M1);
 mv.visitFieldInsn(PUTFIELD, "org/python/core/PyFrame", "f_lasti", "I");
 mv.visitFieldInsn(GETSTATIC, "org/python/core/Py", "None", "Lorg/python/core/PyObject;");
 mv.visitInsn(ARETURN);
 mv.visitMaxs(6, 4);
 mv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC, "greet$1", "(Lorg/python/core/PyFrame;Lorg/python/core/ThreadState;)Lorg/python/core/PyObject;", null, null);
 mv.visitCode();
 mv.visitVarInsn(ALOAD, 1);
 mv.visitInsn(ICONST_2);
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyFrame", "setline", "(I)V");
 mv.visitFieldInsn(GETSTATIC, "Hello", "_1", "Lorg/python/core/PyString;");
 mv.visitVarInsn(ALOAD, 1);
 mv.visitInsn(ICONST_0);
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyFrame", "getlocal", "(I)Lorg/python/core/PyObject;");
 mv.visitMethodInsn(INVOKEVIRTUAL, "org/python/core/PyObject", "_mod", "(Lorg/python/core/PyObject;)Lorg/python/core/PyObject;");
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/Py", "println", "(Lorg/python/core/PyObject;)V");
 mv.visitVarInsn(ALOAD, 1);
 mv.visitInsn(ICONST_M1);
 mv.visitFieldInsn(PUTFIELD, "org/python/core/PyFrame", "f_lasti", "I");
 mv.visitFieldInsn(GETSTATIC, "org/python/core/Py", "None", "Lorg/python/core/PyObject;");
 mv.visitInsn(ARETURN);
 mv.visitMaxs(3, 3);
 mv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null);
 mv.visitCode();
 mv.visitVarInsn(ALOAD, 0);
 mv.visitMethodInsn(INVOKESPECIAL, "org/python/core/IndyFunctionTable", "<init>", "()V");
 mv.visitVarInsn(ALOAD, 0);
 mv.visitFieldInsn(PUTSTATIC, "Hello", "self", "LHello;");
 mv.visitLdcInsn("Frank");
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/PyString", "fromInterned", "(Ljava/lang/String;)Lorg/python/core/PyString;");
 mv.visitFieldInsn(PUTSTATIC, "Hello", "_2", "Lorg/python/core/PyString;");
 mv.visitLdcInsn("hello, %s");
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/PyString", "fromInterned", "(Ljava/lang/String;)Lorg/python/core/PyString;");
 mv.visitFieldInsn(PUTSTATIC, "Hello", "_1", "Lorg/python/core/PyString;");
 mv.visitLdcInsn("/Users/frank/svn/jython/trunk/sandbox/wierzbicki/indy_jython/./hello.py");
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/PyString", "fromInterned", "(Ljava/lang/String;)Lorg/python/core/PyString;");
 mv.visitFieldInsn(PUTSTATIC, "Hello", "_0", "Lorg/python/core/PyString;");
 mv.visitInsn(ICONST_0);
 mv.visitInsn(ICONST_0);
 mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
 mv.visitVarInsn(ASTORE, 2);
 mv.visitVarInsn(ALOAD, 2);
 mv.visitVarInsn(ALOAD, 1);
 mv.visitLdcInsn("<module>");
 mv.visitInsn(ICONST_0);
 mv.visitInsn(ICONST_0);
 mv.visitInsn(ICONST_0);
 mv.visitFieldInsn(GETSTATIC, "Hello", "self", "LHello;");
 
 ////MethodHandle goes here
 ////mv.visitInsn(ACONST_NULL);
 mv.visitMethodInsn(INVOKESTATIC, "java/dyn/MethodHandles", "lookup", "()Ljava/dyn/MethodHandles$Lookup;");
 mv.visitLdcInsn(Type.getType("LHello;"));
 mv.visitLdcInsn("f$0");
 mv.visitLdcInsn(Type.getType("Lorg/python/core/PyObject;"));
 mv.visitLdcInsn(Type.getType("Lorg/python/core/PyFrame;"));
 mv.visitInsn(ICONST_1);
 mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
 mv.visitInsn(DUP);
 mv.visitInsn(ICONST_0);
 mv.visitLdcInsn(Type.getType("Lorg/python/core/ThreadState;"));
 mv.visitInsn(AASTORE);
 mv.visitMethodInsn(INVOKESTATIC, "java/dyn/MethodType", "make", "(Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/dyn/MethodType;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/dyn/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/dyn/MethodType;)Ljava/dyn/MethodHandle;");
 
 mv.visitInsn(ACONST_NULL);
 mv.visitInsn(ACONST_NULL);
 mv.visitInsn(ICONST_0);
 mv.visitIntInsn(SIPUSH, 4096);
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/Py", "newCode", "(I[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZZLorg/python/core/IndyFunctionTable;Ljava/dyn/MethodHandle;[Ljava/lang/String;[Ljava/lang/String;II)Lorg/python/core/PyCode;");
 mv.visitFieldInsn(PUTSTATIC, "Hello", "f$0", "Lorg/python/core/PyCode;");
 mv.visitInsn(ICONST_1);
 mv.visitInsn(ICONST_1);
 mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
 mv.visitVarInsn(ASTORE, 2);
 mv.visitVarInsn(ALOAD, 2);
 mv.visitInsn(ICONST_0);
 mv.visitLdcInsn("name");
 mv.visitInsn(AASTORE);
 mv.visitVarInsn(ALOAD, 2);
 mv.visitVarInsn(ALOAD, 1);
 mv.visitLdcInsn("greet");
 mv.visitInsn(ICONST_1);
 mv.visitInsn(ICONST_0);
 mv.visitInsn(ICONST_0);
 mv.visitFieldInsn(GETSTATIC, "Hello", "self", "LHello;");
 
 ////MethodHandle goes here
 ////mv.visitInsn(ACONST_NULL);
 mv.visitMethodInsn(INVOKESTATIC, "java/dyn/MethodHandles", "lookup", "()Ljava/dyn/MethodHandles$Lookup;");
 mv.visitLdcInsn(Type.getType("LHello;"));
 mv.visitLdcInsn("greet$1");
 mv.visitLdcInsn(Type.getType("Lorg/python/core/PyObject;"));
 mv.visitLdcInsn(Type.getType("Lorg/python/core/PyFrame;"));
 mv.visitInsn(ICONST_1);
 mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
 mv.visitInsn(DUP);
 mv.visitInsn(ICONST_0);
 mv.visitLdcInsn(Type.getType("Lorg/python/core/ThreadState;"));
 mv.visitInsn(AASTORE);
 mv.visitMethodInsn(INVOKESTATIC, "java/dyn/MethodType", "make", "(Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/dyn/MethodType;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/dyn/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/dyn/MethodType;)Ljava/dyn/MethodHandle;");
 
 mv.visitInsn(ACONST_NULL);
 mv.visitInsn(ACONST_NULL);
 mv.visitInsn(ICONST_0);
 mv.visitIntInsn(SIPUSH, 4097);
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/Py", "newCode", "(I[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZZLorg/python/core/IndyFunctionTable;Ljava/dyn/MethodHandle;[Ljava/lang/String;[Ljava/lang/String;II)Lorg/python/core/PyCode;");
 mv.visitFieldInsn(PUTSTATIC, "Hello", "greet$1", "Lorg/python/core/PyCode;");
 mv.visitInsn(RETURN);
mv.visitMaxs(17, 3);
 mv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC, "getMain", "()Lorg/python/core/PyCode;", null, null);
 mv.visitCode();
 mv.visitFieldInsn(GETSTATIC, "Hello", "f$0", "Lorg/python/core/PyCode;");
 mv.visitInsn(ARETURN);
 mv.visitMaxs(1, 1);
 mv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
 mv.visitCode();
 mv.visitTypeInsn(NEW, "Hello");
 mv.visitInsn(DUP);
 mv.visitLdcInsn("Hello");
 mv.visitMethodInsn(INVOKESPECIAL, "Hello", "<init>", "(Ljava/lang/String;)V");
 mv.visitMethodInsn(INVOKEVIRTUAL, "Hello", "getMain", "()Lorg/python/core/PyCode;");
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/CodeLoader", "createSimpleBootstrap", "(Lorg/python/core/PyCode;)Lorg/python/core/CodeBootstrap;");
 mv.visitVarInsn(ALOAD, 0);
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/Py", "runMain", "(Lorg/python/core/CodeBootstrap;[Ljava/lang/String;)V");
 mv.visitInsn(RETURN);
 mv.visitMaxs(3, 1);
 mv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "getCodeBootstrap", "()Lorg/python/core/CodeBootstrap;", null, null);
 mv.visitCode();
 mv.visitLdcInsn(Type.getType("LHello;"));
 mv.visitMethodInsn(INVOKESTATIC, "org/python/core/PyRunnableBootstrap", "getFilenameConstructorReflectionBootstrap", "(Ljava/lang/Class;)Lorg/python/core/CodeBootstrap;");
 mv.visitInsn(ARETURN);
 mv.visitMaxs(1, 0);
 mv.visitEnd();
 }
 {
 mv = cw.visitMethod(ACC_PUBLIC, "call_function", "(Ljava/dyn/MethodHandle;Lorg/python/core/PyFrame;Lorg/python/core/ThreadState;)Lorg/python/core/PyObject;", null, null);
 mv.visitCode();
 mv.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
mv.visitVarInsn(ALOAD, 1);
 mv.visitVarInsn(ALOAD, 0);
 mv.visitVarInsn(ALOAD, 2);
 mv.visitVarInsn(ALOAD, 3);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/dyn/MethodHandle", "invoke", "(LHello;Lorg/python/core/PyFrame;Lorg/python/core/ThreadState;)Lorg/python/core/PyObject;");
 
 /*
 Label l0 = new Label();
 Label l1 = new Label();
 Label l2 = new Label();
 mv.visitTableSwitchInsn(0, 1, l2, new Label[] { l0, l1 });
 mv.visitLabel(l0);
 mv.visitFrame(Opcodes.F_NEW, 4, new Object[] {"Hello", Opcodes.INTEGER, "org/python/core/PyFrame", "org/python/core/ThreadState"}, 3, new Object[] {"Hello", "org/python/core/PyFrame", "org/python/core/ThreadState"});
 mv.visitMethodInsn(INVOKEVIRTUAL, "Hello", "f$0", "(Lorg/python/core/PyFrame;Lorg/python/core/ThreadState;)Lorg/python/core/PyObject;");
 mv.visitInsn(ARETURN);
 mv.visitLabel(l1);
 mv.visitFrame(Opcodes.F_NEW, 4, new Object[] {"Hello", Opcodes.INTEGER, "org/python/core/PyFrame", "org/python/core/ThreadState"}, 3, new Object[] {"Hello", "org/python/core/PyFrame", "org/python/core/ThreadState"});
 mv.visitMethodInsn(INVOKEVIRTUAL, "Hello", "greet$1", "(Lorg/python/core/PyFrame;Lorg/python/core/ThreadState;)Lorg/python/core/PyObject;");
 mv.visitInsn(ARETURN);
 mv.visitLabel(l2);
 mv.visitFrame(Opcodes.F_NEW, 4, new Object[] {"Hello", Opcodes.INTEGER, "org/python/core/PyFrame", "org/python/core/ThreadState"}, 3, new Object[] {"Hello", "org/python/core/PyFrame", "org/python/core/ThreadState"});
 */
 mv.visitInsn(ACONST_NULL);
 mv.visitInsn(ARETURN);
 mv.visitMaxs(5, 4);
 mv.visitEnd();
 }
 cw.visitEnd();
 
 return cw.toByteArray();
 }
 
 public static void main(String args[]) throws Exception {
     byte[] ba = dump();
     File file = new File("Hello.class");
     try {
         FileOutputStream o = new FileOutputStream(file);
         o.write(ba);
         o.close();
     } catch (Throwable t) {
         t.printStackTrace();
     }
 
 }
 }
