 package org.jackie.asmtools;
 
 import org.jackie.utils.Assert;
 import static org.jackie.utils.JavaHelper.FALSE;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Field;
 
 /**
  * @author Patrik Beno
  */
 public abstract class CodeBlock implements Opcodes {
 
 	protected MethodInfo methodInfo;
 	protected MethodVisitor mv;
 
 	protected Label lstart;
 	protected Label lend;
 
 	{
 		lstart = new Label();
 		lend = new Label();
 	}
 
 	protected CodeBlock() {
 	}
 
	protected CodeBlock(CodeBlock codeblock) {
		this.methodInfo = codeblock.methodInfo;
		this.mv = codeblock.mv;
	}



 	public void execute() {
 		label(lstart);
 		body();
 		label(lend);
 	}
 
 	protected abstract void body();
 	
 
 	protected Variable declareVariable(String name, Class type) {
 		return methodInfo.variables.declare(name, type, lstart, lend, false, false);
 	}
 
 	protected Variable declareParameter(String name, Class type) {
 		return methodInfo.variables.declare(name, type, lstart, lend, true, false);
 	}
 
 	protected Variable $this() {
 		return methodInfo.isStatic() ? null : methodInfo.variables.get(0);
 	}
 
 	protected Field field(Class cls, String name) {
 		try {
 			return cls.getDeclaredField(name);
 		} catch (NoSuchFieldException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 	protected Method method(Class cls, String name, Class... args) {
 		try {
 			return cls.getMethod(name, args);
 		} catch (NoSuchMethodException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 	protected void label(Label label) {
 		mv.visitLabel(label);
 	}
 
 	protected void push(Object object) {
 		mv.visitLdcInsn(object);
 	}
 
 	protected void push(Class cls) {
 		mv.visitLdcInsn(Type.getType(cls));
 	}
 
 	protected void load(Variable var) {
 		int opcode = Type.getType(var.type).getOpcode(IALOAD);
 		mv.visitIntInsn(opcode, var.index);
 	}
 
 	protected void store(Variable var) {
 		int opcode = Type.getType(var.type).getOpcode(IASTORE);
 		mv.visitIntInsn(opcode, var.index);
 	}
 
 	protected void newInstance(Constructor constructor) {
 		mv.visitTypeInsn(NEW, Type.getDescriptor(constructor.getDeclaringClass()));
 		mv.visitMethodInsn(
 				INVOKESPECIAL,
 				Type.getDescriptor(constructor.getDeclaringClass()),
 				"<init>",
 				Type.getConstructorDescriptor(constructor)
 		);
 	}
 
 	protected void get(Field field) {
 		mv.visitFieldInsn(
 				Modifier.isStatic(field.getModifiers()) ? GETSTATIC : GETFIELD,
 				Type.getInternalName(field.getDeclaringClass()),
 				field.getName(),
 				Type.getDescriptor(field.getType()));
 	}
 
 	protected void invoke(Method method) {
 		int opcode
 				= Modifier.isStatic(method.getModifiers()) ? INVOKESTATIC
 				: method.getDeclaringClass().isInterface() ? INVOKEINTERFACE
 				: INVOKEVIRTUAL;
 
 		mv.visitMethodInsn(opcode,
 								 Type.getInternalName(method.getDeclaringClass()),
 								 method.getName(),
 								 Type.getMethodDescriptor(method));
 	}
 
 	protected void invokeVoid(Method method) {
 		invoke(method);
 		if (FALSE(method.getReturnType().equals(void.class))) {
 			pop();
 		}
 	}
 
 	protected void pop() {
 		mv.visitInsn(POP);
 	}
 
 	protected void dup() {
 		mv.visitInsn(DUP);
 	}
 
 	protected void swap() {
 		mv.visitInsn(SWAP);
 	}
 
 	protected void doreturn() {
 		Type rtype = Type.getType(methodInfo.type);
 		mv.visitInsn(rtype.getOpcode(IRETURN));
 	}
 
 	protected void jump(Label label) {
 		mv.visitJumpInsn(GOTO, label);
 	}
 
 	protected void jumpif(boolean isTrue, Label label) {
 		mv.visitJumpInsn((isTrue ? IFNE : IFEQ), label);
 	}
 
 }
