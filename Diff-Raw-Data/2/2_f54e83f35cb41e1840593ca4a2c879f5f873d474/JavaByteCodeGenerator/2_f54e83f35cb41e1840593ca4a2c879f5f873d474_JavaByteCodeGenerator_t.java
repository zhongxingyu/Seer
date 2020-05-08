 // ***************************************************************************
 // Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // *  Redistributions of source code must retain the above copyright notice,
 //    this list of conditions and the following disclaimer.
 // *  Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the distribution.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 // TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 // EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 // PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 // OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 // OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 // **************************************************************************
 
 package org.GreenTeaScript;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import org.GreenTeaScript.JVM.GtSubProc;
 import org.GreenTeaScript.JVM.GtThrowableWrapper;
 import org.GreenTeaScript.JVM.JVMConstPool;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 import org.objectweb.asm.tree.FieldNode;
 import org.objectweb.asm.tree.MethodNode;
 
 import static org.objectweb.asm.Opcodes.*;
 
 // GreenTea Generator should be written in each language.
 
 class MethodHolderClass implements Opcodes {
 	final String					name;
 	final String					superClass;
 	final ArrayList<MethodNode>	methods	= new ArrayList<MethodNode>();
 	final Map<String, FieldNode>	fields	= new HashMap<String, FieldNode>();
 
 	public MethodHolderClass(String name, String superClass) {
 		this.name = name;
 		this.superClass = superClass;
 	}
 
 	public void accept(ClassVisitor cv) {
 		cv.visit(V1_6, ACC_PUBLIC, this.name, null, this.superClass, null);
 		for(FieldNode f : this.fields.values()) {
 			f.accept(cv);
 		}
 		for(MethodNode m : this.methods) {
 			m.accept(cv);
 		}
 	}
 
 	public void addMethodNode(MethodNode m) {
 		for(int i=0; i<methods.size(); i++) {
 			MethodNode node = this.methods.get(i);
 			if(node.name.equals(m.name) && node.desc.equals(m.desc)) {
 				this.methods.set(i, m);
 				return;
 			}
 		}
 		this.methods.add(m);
 	}
 
 	public FieldNode getFieldNode(String name) {
 		return this.fields.get(name);
 	}
 }
 
 class GtClassLoader extends ClassLoader {
 	JavaByteCodeGenerator Gen;
 
 	public GtClassLoader(JavaByteCodeGenerator Gen) {
 		this.Gen = Gen;
 	}
 
 	@Override protected Class<?> findClass(String name) {
 		byte[] b = this.Gen.GenerateBytecode(name);
 		return this.defineClass(name, b, 0, b.length);
 	}
 }
 
 final class JVMLocal {
 	public String Name;
 	public Type   TypeInfo;
 	public int    Index;
 
 	public JVMLocal(int Index, Type TypeInfo, String Name) {
 		this.Index = Index;
 		this.TypeInfo = TypeInfo;
 		this.Name = Name;
 	}
 }
 
 class JVMBuilder {
 	MethodVisitor                 AsmMethodVisitor;
 	ArrayList<JVMLocal>           LocalVals;
 	int                           LocalSize;
 	Stack<Type>                   typeStack;
 	Stack<Label>                  BreakLabelStack;
 	Stack<Label>                  ContinueLabelStack;
 
 	public JVMBuilder(MethodVisitor AsmMethodVisitor) {
 		this.AsmMethodVisitor = AsmMethodVisitor;
 		this.LocalVals = new ArrayList<JVMLocal>();
 		this.LocalSize = 0;
 		this.typeStack = new Stack<Type>();
 		this.BreakLabelStack = new Stack<Label>();
 		this.ContinueLabelStack = new Stack<Label>();
 	}
 
 	void LoadConst(Object o) {
 		Type type = null;
 		boolean unsupportType = false;
 		// JVM supports only boolean, int, long, String, float, double, java.lang.Class
 		if(o instanceof Long) {
 			type = Type.LONG_TYPE;
 		}
 		else if(o instanceof Double) {
 			type = Type.DOUBLE_TYPE;
 		}
 		else if(o instanceof Boolean) {
 			type = Type.BOOLEAN_TYPE;
 		}
 		else if(o instanceof String) {
 			type = Type.getType(o.getClass());
 		}
 		else {
 			unsupportType = true;
 			type = Type.getType(o.getClass());
 		}
 		this.typeStack.push(type);
 		if(unsupportType) {
 			int id = JVMConstPool.add(o);
 			String owner = Type.getInternalName(JVMConstPool.class);
 			String methodName = "get";
 			String methodDesc = "(I)Ljava/lang/Object;";
 			this.AsmMethodVisitor.visitLdcInsn(id);
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, methodName, methodDesc);
 			this.AsmMethodVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(o.getClass()));
 		}
 		else {
 			this.AsmMethodVisitor.visitLdcInsn(o);
 		}
 	}
 
 	void LoadLocal(JVMLocal local) {
 		Type type = local.TypeInfo;
 		this.typeStack.push(type);
 		this.AsmMethodVisitor.visitVarInsn(type.getOpcode(ILOAD), local.Index);
 	}
 
 	void StoreLocal(JVMLocal local) {
 		Type type = local.TypeInfo;
 		this.typeStack.pop(); //TODO: check cast
 		this.AsmMethodVisitor.visitVarInsn(type.getOpcode(ISTORE), local.Index);
 	}
 
 	public JVMLocal FindLocalVariable(String Name) {
 		for(int i = 0; i < this.LocalVals.size(); i++) {
 			JVMLocal l = this.LocalVals.get(i);
 			if(l.Name.compareTo(Name) == 0) {
 				return l;
 			}
 		}
 		return null;
 	}
 
 	public JVMLocal AddLocal(Type LocalType, String Name) {
 		JVMLocal local = new JVMLocal(this.LocalSize, LocalType, Name);
 		this.LocalVals.add(local);
 		this.LocalSize += LocalType.getSize();
 		return local;
 	}
 
 	boolean isPrimitiveType(Type type) {
 		return !type.getDescriptor().startsWith("L");
 	}
 
 	void box() {
 		Type type = this.typeStack.pop();
 		if(type.equals(Type.INT_TYPE)) {
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
 			this.typeStack.push(Type.getType(Integer.class));
 		}
 		else if(type.equals(Type.LONG_TYPE)) {
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
 			this.typeStack.push(Type.getType(Long.class));
 		}
 		else if(type.equals(Type.DOUBLE_TYPE)) {
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
 			this.typeStack.push(Type.getType(Double.class));
 		}
 		else if(type.equals(Type.BOOLEAN_TYPE)) {
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
 			this.typeStack.push(Type.getType(Boolean.class));
 		}
 		else if(type.equals(Type.VOID_TYPE)) {
 			this.AsmMethodVisitor.visitInsn(ACONST_NULL);//FIXME: return void
 			this.typeStack.push(Type.getType(Void.class));
 		}
 		else {
 			this.typeStack.push(type);
 		}
 	}
 
 	void unbox(Type type) {
 		if(type.equals(Type.INT_TYPE)) {
 			this.AsmMethodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Integer");
 			this.AsmMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
 			this.typeStack.push(Type.INT_TYPE);
 		}
 		else if(type.equals(Type.LONG_TYPE)) {
 			this.AsmMethodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Long");
 			this.AsmMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
 			this.typeStack.push(Type.LONG_TYPE);
 		}
 		else if(type.equals(Type.DOUBLE_TYPE)) {
 			this.AsmMethodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Double");
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleValue", "()D");
 			this.typeStack.push(Type.DOUBLE_TYPE);
 		}
 		else if(type.equals(Type.BOOLEAN_TYPE)) {
 			this.AsmMethodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
 			this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "booleanValue", "()Z");
 			this.typeStack.push(Type.BOOLEAN_TYPE);
 		}
 		else {
 			this.AsmMethodVisitor.visitTypeInsn(CHECKCAST, type.getInternalName());
 			this.typeStack.push(type);
 		}
 	}
 
 	void Call(Method method) {
 		String owner = Type.getInternalName(method.getDeclaringClass());
 		this.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, method.getName(), Type.getMethodDescriptor(method));
 		this.typeStack.push(Type.getReturnType(method));
 	}
 }
 
 public class JavaByteCodeGenerator extends GtGenerator {
 	private JVMBuilder Builder;
 	private final String defaultClassName = "Global";
 	private final Map<String, MethodHolderClass> classMap = new HashMap<String, MethodHolderClass>();
 	private final Map<String, Type> typeDescriptorMap = new HashMap<String, Type>();
 	private MethodHolderClass DefaultHolderClass = new MethodHolderClass(defaultClassName, "java/lang/Object");
 	private Map<String, Method> methodMap;
 
 	public JavaByteCodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super("java", OutputFile, GeneratorFlag);
 	}
 
 	@Override public void InitContext(GtParserContext Context) {
 		super.InitContext(Context);
 		this.typeDescriptorMap.put(Context.VoidType.ShortName, Type.VOID_TYPE);
 		this.typeDescriptorMap.put(Context.BooleanType.ShortName, Type.BOOLEAN_TYPE);
 		this.typeDescriptorMap.put(Context.IntType.ShortName, Type.LONG_TYPE);
 		this.typeDescriptorMap.put(Context.FloatType.ShortName, Type.DOUBLE_TYPE);
 		this.typeDescriptorMap.put(Context.AnyType.ShortName, Type.getType(Object.class));
 		this.typeDescriptorMap.put(Context.StringType.ShortName, Type.getType(String.class));
 		this.methodMap = GreenTeaRuntime.getAllStaticMethods();
 	}
 
 	//-----------------------------------------------------
 
 	Type ToAsmType(GtType GivenType) {
 		Type type = this.typeDescriptorMap.get(GivenType.ShortName);
 		if(type != null) {
 			return type;
 		}
 		if(GivenType.TypeBody != null && GivenType.TypeBody instanceof Class<?>) {
 			return Type.getType((Class<?>) GivenType.TypeBody);
 		}
 		return Type.getType("L" + GivenType.ShortName + ";");
 	}
 
 	Type ToAsmMethodType(GtFunc method) {
 		Type returnType = this.ToAsmType(method.GetReturnType());
 		Type[] argTypes = new Type[method.GetFuncParamSize()];
 		for(int i = 0; i < argTypes.length; i++) {
 			GtType ParamType = method.GetFuncParamType(i);
 			argTypes[i] = this.ToAsmType(ParamType);
 		}
 		return Type.getMethodType(returnType, argTypes);
 	}
 
 	public Class<?> ToClass(Type type) throws ClassNotFoundException {
		if(type.equals(Type.BOOLEAN_TYPE)) {
 			return boolean.class;
 		}
 		else if(type.equals(Type.LONG_TYPE)) {
 			return long.class;
 		}
 		else if(type.equals(Type.DOUBLE_TYPE)) {
 			return double.class;
 		}
 		else {
 			return Class.forName(type.getClassName());
 		}
 	}
 
 	//-----------------------------------------------------
 
 	void OutputClassFile(String className, String dir) throws IOException {
 		byte[] ba = this.GenerateBytecode(className);
 		File file = new File(dir, className + ".class");
 		FileOutputStream fos = null;
 		try {
 			fos = new FileOutputStream(file);
 			fos.write(ba);
 		} finally {
 			if(fos != null) {
 				fos.close();
 			}
 		}
 	}
 
 	byte[] GenerateBytecode(String className) {
 		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
 		MethodHolderClass CNode = this.classMap.get(className);
 		assert CNode != null;
 		CNode.accept(classWriter);
 		classWriter.visitEnd();
 		return classWriter.toByteArray();
 	}
 
 	//-----------------------------------------------------
 
 	@Override public Object Eval(GtNode Node) {
 		int acc = ACC_PUBLIC | ACC_STATIC;
 		String methodName = "__eval";
 		String methodDesc = "()Ljava/lang/Object;";
 		MethodNode mn = new MethodNode(acc, methodName, methodDesc, null, null);
 		MethodHolderClass c = DefaultHolderClass;
 		c.addMethodNode(mn);
 		this.classMap.put(c.name, c);
 
 		this.Builder = new JVMBuilder(mn);
 		this.VisitBlock(Node);
 
 		// boxing and return
 		if(this.Builder.typeStack.empty()) {
 			this.Builder.AsmMethodVisitor.visitInsn(ACONST_NULL);
 		}
 		else {
 			this.Builder.box();
 		}
 		this.Builder.AsmMethodVisitor.visitInsn(ARETURN);
 
 		if(LibGreenTea.DebugMode) {
 			try {
 				this.OutputClassFile(defaultClassName, ".");
 			} catch(IOException e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 		//execute
 		try {
 			GtClassLoader loader = new GtClassLoader(this);
 			Class<?> klass = loader.loadClass(defaultClassName);
 			Object res = klass.getMethod(methodName).invoke(null);
 			return res;
 		} catch(ClassNotFoundException e) {
 			LibGreenTea.VerboseException(e);
 		} catch(InvocationTargetException e) {
 			LibGreenTea.VerboseException(e);
 		} catch(IllegalAccessException e) {
 			LibGreenTea.VerboseException(e);
 		} catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 		}
 		return null;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> NameList, GtNode Body) {
 		int acc = ACC_PUBLIC | ACC_STATIC;
 		Type ReturnType = this.ToAsmType(Func.GetReturnType());
 
 		ArrayList<Type> argTypes = new ArrayList<Type>();
 		for(int i=0; i<NameList.size(); i++) {
 			GtType type = Func.GetFuncParamType(i);
 			argTypes.add(this.ToAsmType(type));
 		}
 		String MethodName = Func.GetNativeFuncName();
 		String MethodDesc = Type.getMethodDescriptor(ReturnType, argTypes.toArray(new Type[0]));
 
 		MethodNode AsmMethodNode = new MethodNode(acc, MethodName, MethodDesc, null, null);
 		MethodHolderClass c = DefaultHolderClass;
 		c.addMethodNode(AsmMethodNode);
 		this.classMap.put(c.name, c);
 
 		this.Builder = new JVMBuilder(AsmMethodNode);
 		for(int i=0; i<NameList.size(); i++) {
 			String Name = NameList.get(i);
 			this.Builder.AddLocal(argTypes.get(i), Name);
 		}
 		this.VisitBlock(Body);
 
 		// JVM always needs return;
 		if(ReturnType.equals(Type.VOID_TYPE)) {
 			this.Builder.AsmMethodVisitor.visitInsn(RETURN);//FIXME
 		}
 
 		// for debug purpose
 		if(LibGreenTea.DebugMode) {
 			try {
 				this.OutputClassFile(defaultClassName, ".");
 			} catch(IOException e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 		try {
 			ClassLoader loader = new GtClassLoader(this);
 			Class<?>[] args = new Class<?>[argTypes.size()];
 			for(int i=0; i<args.length; i++) {
 				args[i] = ToClass(argTypes.get(i));
 			}
 			Method m = loader.loadClass(c.name).getMethod(MethodName, args);
 			Func.SetNativeMethod(0, m);
 		} catch(Exception e) {
 			LibGreenTea.VerboseException(e);
 		}
 	}
 
 	@Override public void OpenClassField(GtType ClassType, GtClassField ClassField) {
 		String className = ClassType.ShortName;
 		MethodHolderClass superClassNode = this.classMap.get(ClassType.SuperType.ShortName);
 		String superClassName = superClassNode != null ?
 				superClassNode.name : Type.getInternalName(GreenTeaTopObject.class);
 		MethodHolderClass classNode = new MethodHolderClass(className, superClassName);
 		this.classMap.put(classNode.name, classNode);
 		// generate field
 		for(GtFieldInfo field : ClassField.FieldList) {
 			if(field.FieldIndex >= ClassField.ThisClassIndex) {
 				int access = ACC_PUBLIC;
 				String fieldName = field.NativeName;
 				Type fieldType = this.ToAsmType(field.Type);
 				FieldNode node = new FieldNode(access, fieldName, fieldType.getDescriptor(), null, null);
 				classNode.fields.put(fieldName, node);
 			}
 		}
 		// generate default constructor (for jvm)
 		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "(Lorg/GreenTeaScript/GtType;)V", null, null);
 		constructor.visitVarInsn(ALOAD, 0);
 		constructor.visitVarInsn(ALOAD, 1);
 		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "(Lorg/GreenTeaScript/GtType;)V");
 		for(GtFieldInfo field : ClassField.FieldList) {
 			if(field.FieldIndex >= ClassField.ThisClassIndex && field.InitValue != null) {
 				String name = field.NativeName;
 				String desc = this.ToAsmType(field.Type).getDescriptor();
 				constructor.visitVarInsn(ALOAD, 0);
 				constructor.visitLdcInsn(field.InitValue);
 				constructor.visitFieldInsn(PUTFIELD, className, name, desc);
 			}
 		}
 		constructor.visitInsn(RETURN);
 		classNode.addMethodNode(constructor);
 		if(LibGreenTea.DebugMode) {
 			try {
 				this.OutputClassFile(className, ".");
 			} catch(IOException e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 		try {
 			ClassLoader loader = new GtClassLoader(this);
 			ClassType.TypeBody = loader.loadClass(className);
 		} catch(Exception e) {
 			LibGreenTea.VerboseException(e);
 		}
 	}
 
 	//-----------------------------------------------------
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		Object constValue = Node.ConstValue;
 		if(constValue == null) { // FIXME(ide)
 			this.Builder.typeStack.push(this.ToAsmType(Node.Type));
 			this.Builder.AsmMethodVisitor.visitInsn(ACONST_NULL);
 		}
 		else {
 			this.Builder.LoadConst(constValue);
 		}
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		Type type = this.ToAsmType(Node.Type);
 		String owner = type.getInternalName();
 		this.Builder.AsmMethodVisitor.visitTypeInsn(NEW, owner);
 		this.Builder.AsmMethodVisitor.visitInsn(DUP);
 		if(!Node.Type.IsNative()) {
 			this.Builder.AsmMethodVisitor.visitInsn(ACONST_NULL);//FIXME: push type
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "(Lorg/GreenTeaScript/GtType;)V");
 		} else {
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "()V");
 		}
 		this.Builder.typeStack.push(type);
 	}
 
 	public void VisitConstructorNode(ConstructorNode Node) {
 		Type type = this.ToAsmType(Node.Type);
 		for(int i=1; i<Node.ParamList.size(); i++) {
 			Node.ParamList.get(i).Evaluate(this);
 			this.Builder.typeStack.pop();
 		}
 		GtFunc Func = Node.Func;
 		Method m = null;
 		if(Func.NativeRef instanceof Method) {
 			m = (Method) Func.NativeRef;
 		}
 		else {
 			m = this.methodMap.get(Func.FuncName);
 		}
 		if(m != null) {
 			String owner = Type.getDescriptor(m.getDeclaringClass());
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, m.getName(), Type.getMethodDescriptor(m));
 		}
 		else {
 			int opcode = INVOKESTATIC;
 			String owner = this.defaultClassName;
 			String methodName = Func.GetNativeFuncName();
 			String methodDescriptor = this.ToAsmMethodType(Func).getDescriptor();
 			this.Builder.AsmMethodVisitor.visitMethodInsn(opcode, owner, methodName, methodDescriptor);
 		}
 		this.Builder.typeStack.push(type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.Builder.typeStack.push(this.ToAsmType(Node.Type));
 		this.Builder.AsmMethodVisitor.visitInsn(ACONST_NULL);
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		JVMLocal local = this.Builder.FindLocalVariable(Node.NativeName);
 		this.Builder.LoadLocal(local);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		String name = Node.Func.FuncName;
 		Type ty = this.ToAsmType(Node.Type);
 		Node.Expr.Evaluate(this);
 		this.Builder.AsmMethodVisitor.visitLdcInsn(name);
 		this.Builder.Call(this.methodMap.get("$getter"));
 		this.Builder.unbox(ty);
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		GtFunc Func = Node.Func;
 		for(int i = 1; i < Node.NodeList.size(); i++) {
 			GtNode ParamNode = Node.NodeList.get(i);
 			ParamNode.Evaluate(this);
 			Type requireType = this.ToAsmType(Func.GetFuncParamType(i - 1));
 			Type foundType = this.Builder.typeStack.peek();
 			if(requireType.equals(Type.getType(Object.class)) && this.Builder.isPrimitiveType(foundType)) {
 				// boxing
 				this.Builder.box();
 			}
 			else {
 				this.Builder.typeStack.pop();
 			}
 		}
 		Method m = null;
 		if(Func.NativeRef instanceof Method) {
 			m = (Method) Func.NativeRef;
 		}
 		else {
 			m = this.methodMap.get(Func.FuncName);
 		}
 		if(m != null) {
 			String owner = Type.getInternalName(m.getDeclaringClass());
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, m.getName(), Type.getMethodDescriptor(m));
 			this.Builder.typeStack.push(Type.getReturnType(m));
 		}
 		else {
 //			int opcode = Node.Func.Is(NativeStaticFunc) ? INVOKESTATIC : INVOKEVIRTUAL;
 			int opcode = INVOKESTATIC;
 			String owner = defaultClassName;//FIXME
 			String methodName = Func.GetNativeFuncName();  // IMSORRY
 			String methodDescriptor = this.ToAsmMethodType(Func).getDescriptor();
 			this.Builder.AsmMethodVisitor.visitMethodInsn(opcode, owner, methodName, methodDescriptor);
 			this.Builder.typeStack.push(this.ToAsmType(Func.GetReturnType()));
 		}
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		Node.LeftNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		Node.RightNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		Method m = (Method)Node.Func.NativeRef;
 		if(m != null) {
 			this.Builder.Call(m);
 		}
 		else {
 			throw new RuntimeException("unsupport binary operator: " + Node.Func.FuncName);
 		}
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		Node.Expr.Evaluate(this);
 		this.Builder.typeStack.pop();
 		Method m = (Method)Node.Func.NativeRef;
 		if(m != null) {
 			this.Builder.Call(m);
 		}
 		else {
 			throw new RuntimeException("unsupport unary operator: " + Node.Func.FuncName);
 		}
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Label elseLabel = new Label();
 		Label mergeLabel = new Label();
 		Node.LeftNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, elseLabel);
 
 		Node.RightNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, elseLabel);
 
 		this.Builder.AsmMethodVisitor.visitLdcInsn(true);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(elseLabel);
 		this.Builder.AsmMethodVisitor.visitLdcInsn(false);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(mergeLabel);
 		this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Label thenLabel = new Label();
 		Label mergeLabel = new Label();
 		Node.LeftNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFNE, thenLabel);
 
 		Node.RightNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFNE, thenLabel);
 
 		this.Builder.AsmMethodVisitor.visitLdcInsn(false);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(thenLabel);
 		this.Builder.AsmMethodVisitor.visitLdcInsn(true);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(mergeLabel);
 		this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		if(Node.LeftNode instanceof GetterNode) {
 			GetterNode left = (GetterNode) Node.LeftNode;
 			String name = left.Func.FuncName;
 			Type ty = this.ToAsmType(left.Func.Types[0]);//FIXME
 
 			left.Expr.Evaluate(this);
 			this.Builder.AsmMethodVisitor.visitLdcInsn(name);
 			Node.RightNode.Evaluate(this);
 			this.Builder.box();
 			this.Builder.Call(this.methodMap.get("$setter"));
 			this.Builder.typeStack.pop();
 			this.Builder.typeStack.push(ty);
 		}
 		else {
 			assert (Node.LeftNode instanceof LocalNode);
 			LocalNode Left = (LocalNode) Node.LeftNode;
 			JVMLocal local = this.Builder.FindLocalVariable(Left.NativeName);
 			Node.RightNode.Evaluate(this);
 			this.Builder.StoreLocal(local);
 		}
 	}
 
 	public void VisitSelfAssignNode(SelfAssignNode Node) {
 		if(Node.LeftNode instanceof LocalNode) {
 			LocalNode Left = (LocalNode)Node.LeftNode;
 			JVMLocal local = this.Builder.FindLocalVariable(Left.NativeName);
 			Node.LeftNode.Evaluate(this);
 			this.Builder.typeStack.pop();
 			Node.RightNode.Evaluate(this);
 			this.Builder.typeStack.pop();
 			this.Builder.Call((Method)Node.Func.NativeRef);
 			this.Builder.StoreLocal(local);
 		}
 		else {
 			LibGreenTea.TODO("selfAssign");
 		}
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		JVMLocal local = this.Builder.AddLocal(this.ToAsmType(Node.Type), Node.NativeName);
 		Node.InitNode.Evaluate(this);
 		this.Builder.StoreLocal(local);
 		this.VisitBlock(Node.BlockNode);
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Label ElseLabel = new Label();
 		Label EndLabel = new Label();
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop(); //TODO: check cast
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, ElseLabel);
 		// Then
 		this.VisitBlock(Node.ThenNode);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, EndLabel);
 		// Else
 		this.Builder.AsmMethodVisitor.visitLabel(ElseLabel);
 		if(Node.ElseNode != null) {
 			this.VisitBlock(Node.ElseNode);
 			this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, EndLabel);
 		}
 		// End
 		this.Builder.AsmMethodVisitor.visitLabel(EndLabel);
 	}
 
 	public void VisitTrinaryNode(TrinaryNode Node) {
 		Label ElseLabel = new Label();
 		Label EndLabel = new Label();
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, ElseLabel);
 		// Then
 		this.VisitBlock(Node.ThenExpr);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, EndLabel);
 		// Else
 		this.Builder.AsmMethodVisitor.visitLabel(ElseLabel);
 		this.VisitBlock(Node.ElseExpr);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, EndLabel);
 		// End
 		this.Builder.AsmMethodVisitor.visitLabel(EndLabel);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		int cases = Node.CaseList.size() / 2;
 		int[] keys = new int[cases];
 		Label[] caseLabels = new Label[cases];
 		Label defaultLabel = new Label();
 		Label breakLabel = new Label();
 		for(int i=0; i<cases; i++) {
 			keys[i] = ((Number)((ConstNode)Node.CaseList.get(i*2)).ConstValue).intValue();
 			caseLabels[i] = new Label();
 		}
 		Node.MatchNode.Evaluate(this);
 		this.Builder.AsmMethodVisitor.visitInsn(L2I);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitLookupSwitchInsn(defaultLabel, keys, caseLabels);
 		for(int i=0; i<cases; i++) {
 			this.Builder.BreakLabelStack.push(breakLabel);
 			this.Builder.AsmMethodVisitor.visitLabel(caseLabels[i]);
 			this.VisitBlock(Node.CaseList.get(i*2+1));
 			this.Builder.BreakLabelStack.pop();
 		}
 		this.Builder.AsmMethodVisitor.visitLabel(defaultLabel);
 		this.VisitBlock(Node.DefaultBlock);
 		this.Builder.AsmMethodVisitor.visitLabel(breakLabel);
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Label continueLabel = new Label();
 		Label breakLabel = new Label();
 		this.Builder.BreakLabelStack.push(breakLabel);
 		this.Builder.ContinueLabelStack.push(continueLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(continueLabel);
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
 		this.VisitBlock(Node.LoopBody);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, continueLabel);
 		this.Builder.AsmMethodVisitor.visitLabel(breakLabel);
 
 		this.Builder.BreakLabelStack.pop();
 		this.Builder.ContinueLabelStack.pop();
 	}
 
 	public void VisitDoWhileNode(DoWhileNode Node) {
 		Label headLabel = new Label();
 		Label continueLabel = new Label();
 		Label breakLabel = new Label();
 		this.Builder.BreakLabelStack.push(breakLabel);
 		this.Builder.ContinueLabelStack.push(continueLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(headLabel);
 		this.VisitBlock(Node.LoopBody);
 		this.Builder.AsmMethodVisitor.visitLabel(continueLabel);
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, headLabel);
 		this.Builder.AsmMethodVisitor.visitLabel(breakLabel);
 
 		this.Builder.BreakLabelStack.pop();
 		this.Builder.ContinueLabelStack.pop();
 	}
 
 	public void VisitForNode(ForNode Node) {
 		Label headLabel = new Label();
 		Label continueLabel = new Label();
 		Label breakLabel = new Label();
 		this.Builder.BreakLabelStack.push(breakLabel);
 		this.Builder.ContinueLabelStack.push(continueLabel);
 
 		this.Builder.AsmMethodVisitor.visitLabel(headLabel);
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
 		this.VisitBlock(Node.LoopBody);
 		this.Builder.AsmMethodVisitor.visitLabel(continueLabel);
 		Node.IterExpr.Evaluate(this);
 		//this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, headLabel);
 		this.Builder.AsmMethodVisitor.visitLabel(breakLabel);
 
 		this.Builder.BreakLabelStack.pop();
 		this.Builder.ContinueLabelStack.pop();
 	}
 
 	public void VisitForEachNode(ForEachNode Node) {
 		/*extension*/
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			Type type = this.Builder.typeStack.pop();
 			this.Builder.AsmMethodVisitor.visitInsn(type.getOpcode(IRETURN));
 		}
 		else {
 			this.Builder.AsmMethodVisitor.visitInsn(RETURN);
 		}
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		Label l = this.Builder.BreakLabelStack.peek();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		Label l = this.Builder.ContinueLabelStack.peek();
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) { //FIXME
 		int catchSize = 1;
 		MethodVisitor mv = this.Builder.AsmMethodVisitor;
 		Label beginTryLabel = new Label();
 		Label endTryLabel = new Label();
 		Label finallyLabel = new Label();
 		Label catchLabel[] = new Label[catchSize];
 		String throwType = this.ToAsmType(Node.CatchExpr.Type).getInternalName();
 
 		// try block
 		mv.visitLabel(beginTryLabel);
 		this.VisitBlock(Node.TryBlock);
 		mv.visitLabel(endTryLabel);
 		mv.visitJumpInsn(GOTO, finallyLabel);
 
 		// prepare
 		for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
 			catchLabel[i] = new Label();
 			mv.visitTryCatchBlock(beginTryLabel, endTryLabel, catchLabel[i], throwType);
 		}
 
 		// catch block
 		{ //for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
 			int i = 0;
 			GtNode block = Node.CatchBlock;
 			mv.visitLabel(catchLabel[i]);
 			this.VisitBlock(block);
 			mv.visitJumpInsn(GOTO, finallyLabel);
 		}
 
 		// finally block
 		mv.visitLabel(finallyLabel);
 		this.VisitBlock(Node.FinallyBlock);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		// use wrapper
 		String name = Type.getInternalName(GtThrowableWrapper.class);
 		this.Builder.AsmMethodVisitor.visitTypeInsn(NEW, name);
 		this.Builder.AsmMethodVisitor.visitInsn(DUP);
 		Node.Expr.Evaluate(this);
 		this.Builder.box();
 		this.Builder.typeStack.pop();
 		this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
 		this.Builder.AsmMethodVisitor.visitInsn(ATHROW);
 	}
 
 	@Override public void VisitInstanceOfNode(InstanceOfNode Node) {
 		Type type = this.ToAsmType(Node.TypeInfo);
 		Node.ExprNode.Evaluate(this);
 		Type foundType = this.Builder.typeStack.pop();
 		if(type.equals(foundType)) {
 			this.Builder.AsmMethodVisitor.visitLdcInsn(true);//FIXME: primitive type
 		}
 		else {
 			this.Builder.AsmMethodVisitor.visitTypeInsn(INSTANCEOF, type.getInternalName());
 		}
 		this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		this.Builder.AsmMethodVisitor.visitLdcInsn("(ErrorNode)");
 		this.Builder.Call(this.methodMap.get("$error_node"));
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		ArrayList<ArrayList<GtNode>> Args = new ArrayList<ArrayList<GtNode>>();
 		CommandNode node = Node;
 		while(node != null) {
 			Args.add(node.Params);
 			node = (CommandNode) node.PipedNextNode;
 		}
 		// new String[][n]
 		this.Builder.AsmMethodVisitor.visitLdcInsn(Args.size());
 		this.Builder.AsmMethodVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String[].class));
 		for(int i=0; i<Args.size(); i++) {
 			// new String[m];
 			ArrayList<GtNode> Arg = Args.get(i);
 			this.Builder.AsmMethodVisitor.visitInsn(DUP);
 			this.Builder.AsmMethodVisitor.visitLdcInsn(i);
 			this.Builder.AsmMethodVisitor.visitLdcInsn(Arg.size());
 			this.Builder.AsmMethodVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String.class));
 			for(int j=0; j<Arg.size(); j++) {
 				this.Builder.AsmMethodVisitor.visitInsn(DUP);
 				this.Builder.AsmMethodVisitor.visitLdcInsn(j);
 				Arg.get(j).Evaluate(this);
 				this.Builder.typeStack.pop();
 				this.Builder.AsmMethodVisitor.visitInsn(AASTORE);
 			}
 			this.Builder.AsmMethodVisitor.visitInsn(AASTORE);
 		}
 		String name, desc;
 		if(Node.Type.IsBooleanType()) {
 			name = "ExecCommandBool";
 			desc = "([[Ljava/lang/String;)Z";
 			this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 		}
 		else if(Node.Type.IsStringType()) {
 			name = "ExecCommandString";
 			desc = "([[Ljava/lang/String;)Ljava/lang/String;";
 			this.Builder.typeStack.push(Type.getType(String.class));
 		}
 		else if(Node.Type.IsVoidType()) {
 			name = "ExecCommandVoid";
 			desc = "([[Ljava/lang/String;)V";
 		}
 		else {
 			name = "ExecCommand";
 			desc = "([[Ljava/lang/String;)" + Type.getType(GtSubProc.class).getDescriptor();
 			this.Builder.typeStack.push(Type.getType(GtSubProc.class));
 		}
 		this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, Type.getInternalName(GtSubProc.class), name, desc);
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		try {
 			GtClassLoader loader = new GtClassLoader(this);
 			Class<?> klass = loader.loadClass(defaultClassName);
 			Method m = klass.getMethod(MainFuncName);
 			if(m != null) {
 				m.invoke(null);
 			}
 		} catch(ClassNotFoundException e) {
 			LibGreenTea.VerboseException(e);
 		} catch(InvocationTargetException e) {
 			LibGreenTea.VerboseException(e);
 		} catch(IllegalAccessException e) {
 			LibGreenTea.VerboseException(e);
 		} catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 		}
 	}
 }
