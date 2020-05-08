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
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 import org.objectweb.asm.tree.FieldNode;
 import org.objectweb.asm.tree.MethodNode;
 import static org.objectweb.asm.Opcodes.*;
 
 import JVM.GtSubProc;
 import JVM.GtThrowableWrapper;
 import JVM.JVMConstPool;
 import JVM.StaticMethods;
 
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
 
 class LabelStack {
 	ArrayList<String>	LabelNames;
 	ArrayList<Label>	Labels;
 
 	LabelStack() {
 		this.LabelNames = new ArrayList<String>();
 		this.Labels = new ArrayList<Label>();
 	}
 
 	void AddLabel(String Name, Label Label) {
 		this.LabelNames.add(Name);
 		this.Labels.add(Label);
 	}
 
 	Label FindLabel(String Name) {
 		for(int i = this.LabelNames.size() - 1; i >= 0; i--) {
 			String LName = this.LabelNames.get(i);
 			if(LName.equals(Name)) {
 				return this.Labels.get(i);
 			}
 		}
 		return null;
 	}
 
 	void RemoveLabel(String Name) {
 		for(int i = this.LabelNames.size() - 1; i >= 0; i--) {
 			String LName = this.LabelNames.get(i);
 			if(LName.equals(Name)) {
 				this.LabelNames.remove(i);
 				this.Labels.remove(i);
 			}
 		}
 	}
 }
 
 final class JVMLocal {
 	public String		Name;
 	public GtType		TypeInfo;
 	public int			Index;
 
 	public JVMLocal(int Index, GtType TypeInfo, String Name) {
 		this.Index = Index;
 		this.TypeInfo = TypeInfo;
 		this.Name = Name;
 	}
 }
 
 class JVMBuilder {
 	MethodVisitor                 AsmMethodVisitor;
 	ArrayList<JVMLocal>           LocalVals;
 	Stack<Type>                   typeStack;
 	LabelStack                    LabelStack;
 	JavaByteCodeGenerator         Gen;
 
 	public JVMBuilder(JavaByteCodeGenerator Gen, MethodVisitor AsmMethodVisitor) {
 		this.Gen = Gen;
 		this.AsmMethodVisitor = AsmMethodVisitor;
 		this.LocalVals = new ArrayList<JVMLocal>();
 		this.typeStack = new Stack<Type>();
 		this.LabelStack = new LabelStack();
 	}
 
 	void LoadConst(Object o) {
 		Type type;
 		boolean unsupportType = false;
 		// JVM supports only boolean, int, long, String, float, double, java.lang.Class
 		if(o instanceof Integer) {
 			type = Type.INT_TYPE;
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
 			String owner = Type.getInternalName(this.getClass());
 			String methodName = "getConstValue";
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
 		GtType gtype = local.TypeInfo;
 		Type type = this.Gen.GetAsmType(gtype);
 		this.typeStack.push(type);
 		this.AsmMethodVisitor.visitVarInsn(type.getOpcode(ILOAD), local.Index);
 	}
 
 	void StoreLocal(JVMLocal local) {
 		GtType gtype = local.TypeInfo;
 		Type type = this.Gen.GetAsmType(gtype);
 		this.typeStack.pop(); //TODO: check cast
 		this.AsmMethodVisitor.visitVarInsn(type.getOpcode(ISTORE), local.Index);
 	}
 
 	public void boxingReturn() {
 		if(this.typeStack.empty()) {
 			this.AsmMethodVisitor.visitInsn(ACONST_NULL);
 		}
 		else {
 			this.box();
 		}
 		this.AsmMethodVisitor.visitInsn(ARETURN);
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
 
 	public JVMLocal AddLocal(GtType Type, String Name) {
 		JVMLocal local = new JVMLocal(this.LocalVals.size(), Type, Name);
 		this.LocalVals.add(local);
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
 			this.AsmMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()L");  // ?
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
 			this.typeStack.push(type);
 		}
 	}
 }
 
 public class JavaByteCodeGenerator extends GtGenerator {
 	private boolean debug_mode = false;
 	private JVMBuilder Builder;
 	private final String defaultClassName = "Global";
 	private final Map<String, MethodHolderClass> classMap = new HashMap<String, MethodHolderClass>();
 	private final Map<String, Type> typeDescriptorMap = new HashMap<String, Type>();
 	private MethodHolderClass DefaultHolderClass = new MethodHolderClass(defaultClassName, "java/lang/Object");
 	private Map<String, Method> methodMap;
 
 	public JavaByteCodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 	}
 
 	@Override public void InitContext(GtContext Context) {
 		super.InitContext(Context);
 		this.typeDescriptorMap.put(Context.VoidType.ShortClassName, Type.VOID_TYPE);
 		this.typeDescriptorMap.put(Context.BooleanType.ShortClassName, Type.BOOLEAN_TYPE);
 		this.typeDescriptorMap.put(Context.IntType.ShortClassName, Type.INT_TYPE);
		//this.typeDescriptorMap.put(Context.ObjectType.ShortClassName, Type.getType(Object.class).getDescriptor());
		this.typeDescriptorMap.put("Object", Type.getType(Object.class));
 		this.typeDescriptorMap.put(Context.StringType.ShortClassName, Type.getType(String.class));
 		this.methodMap = StaticMethods.getAllStaticMethods();
 	}
 
 	//-----------------------------------------------------
 
 	Type GetAsmType(GtType GivenType) {
 		Type type = this.typeDescriptorMap.get(GivenType.ShortClassName);
 		if(type != null) {
 			return type;
 		}
 		return Type.getType(GivenType.ShortClassName);
 	}
 
 	Type GetAsmMethodType(GtFunc method) {
 		Type returnType = this.GetAsmType(method.GetReturnType());
 		Type[] argTypes = new Type[method.GetFuncParamSize()];
 		for(int i = 0; i < argTypes.length; i++) {
 			GtType ParamType = method.GetFuncParamType(i);
 			argTypes[i] = this.GetAsmType(ParamType);
 		}
 		return Type.getMethodType(returnType, argTypes);
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
 
 		this.Builder = new JVMBuilder(this, mn);
 		this.VisitBlock(Node);
 		this.Builder.boxingReturn();
 		if(debug_mode) {
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
 		Type ReturnType = this.GetAsmType(Func.GetReturnType());
 
 		ArrayList<Type> argTypes = new ArrayList<Type>();
 		ArrayList<JVMLocal> locals  = new ArrayList<JVMLocal>();
 		for(int i=0; i<NameList.size(); i++) {
 			GtType type = Func.GetFuncParamType(i);
 			String name = NameList.get(i);
 			argTypes.add(this.GetAsmType(type));
 			locals.add(new JVMLocal(i, type, name));
 		}
 
 		String MethodName = Func.GetNativeFuncName();
 		String MethodDesc = Type.getMethodDescriptor(ReturnType, argTypes.toArray(new Type[0]));
 
 		MethodNode AsmMethodNode = new MethodNode(acc, MethodName, MethodDesc, null, null);
 
 		MethodHolderClass c = DefaultHolderClass;
 		c.addMethodNode(AsmMethodNode);
 		this.classMap.put(c.name, c);
 
 		this.Builder = new JVMBuilder(this, AsmMethodNode);
 		this.Builder.LocalVals.addAll(locals);
 		this.VisitBlock(Body);
 
 		// JVM always needs return;
 		if(ReturnType.equals(Type.VOID_TYPE)) {
 			this.Builder.AsmMethodVisitor.visitInsn(RETURN);//FIXME
 		}
 
 		// for debug purpose
 		if(debug_mode) {
 			try {
 				this.OutputClassFile(defaultClassName, ".");
 			} catch(IOException e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		String className = Type.ShortClassName;
 		MethodHolderClass superClassNode = this.classMap.get(Type.SuperType.ShortClassName);
 		String superClassName = superClassNode != null ? superClassNode.name : "java/lang/Object";
 		MethodHolderClass classNode = new MethodHolderClass(className, superClassName);
 		this.classMap.put(classNode.name, classNode);
 		// generate field
 		for(GtFieldInfo field : ClassField.FieldList) {
 			int access = ACC_PUBLIC;
 			String fieldName = field.NativeName;
 			Type fieldType = this.GetAsmType(field.Type);
 			FieldNode node = new FieldNode(access, fieldName, fieldType.getDescriptor(), null, null);
 			classNode.fields.put(fieldName, node);
 		}
 		// generate default constructor (for jvm)
 		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
 		constructor.visitVarInsn(ALOAD, 0);
 		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V");
 		constructor.visitInsn(RETURN);
 		classNode.addMethodNode(constructor);
 		if(debug_mode) {
 			try {
 				this.OutputClassFile(className, ".");
 			} catch(IOException e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 	}
 
 	//-----------------------------------------------------
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		Object constValue = Node.ConstValue;
 		//FIXME
 		if(constValue instanceof Long) {
 			this.Builder.LoadConst(((Long)constValue).intValue());
 		}
 		else {
 			this.Builder.LoadConst(constValue);
 		}
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		Type type = Type.getType(Node.Func.Types[0].ShortClassName);
 		String owner = type.getInternalName();
 		this.Builder.AsmMethodVisitor.visitTypeInsn(NEW, owner);
 		this.Builder.AsmMethodVisitor.visitInsn(DUP);
 		this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "()V");
 		this.Builder.typeStack.push(type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.Builder.typeStack.push(this.GetAsmType(Node.Type));
 		this.Builder.AsmMethodVisitor.visitInsn(ACONST_NULL);
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		JVMLocal local = this.Builder.FindLocalVariable(Node.NativeName);
 		this.Builder.LoadLocal(local);
 	}
 
 	public static Object getter(Object o, String name) throws Exception {
 		return o.getClass().getField(name).get(o);
 	}
 
 	public static void setter(Object o, String name, Object n) throws Exception {
 		o.getClass().getField(name).set(o, n);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		String name = Node.Func.FuncName;
 		Type ty = this.GetAsmType(Node.Func.Types[2]);//FIXME
 		Node.Expr.Evaluate(this);
 		this.Builder.AsmMethodVisitor.visitLdcInsn(name);
 		this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC,
 				"JavaByteCodeGenerator", "getter", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
 		this.Builder.unbox(ty);
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		GtFunc Func = Node.Func;
 		for(int i = 1; i < Node.Params.size(); i++) {
 			GtNode ParamNode = Node.Params.get(i);
 			ParamNode.Evaluate(this);
 			Type requireType = this.GetAsmType(Func.GetFuncParamType(i - 1));
 			Type foundType = this.Builder.typeStack.peek();
 			if(requireType.equals(Type.getType(Object.class)) && this.Builder.isPrimitiveType(foundType)) {
 				// boxing
 				this.Builder.box();
 			}
 			else {
 				this.Builder.typeStack.pop();
 			}
 		}
 //		if(Func.FuncName.equals("New")) {
 //			Type type = this.TypeResolver.GetAsmType(Func.GetReturnType());
 //			String owner = type.getInternalName();
 //			String methodName = "<init>";
 //			String methodDesc = TypeResolver.GetJavaFuncDescriptor(Func);//"()V";//Node.Params;
 //			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESPECIAL, owner, methodName, methodDesc);
 //			this.Builder.typeStack.push(type);
 //		}
 //		else {
 		Method m = this.methodMap.get(Func.FuncName);
 		if(m != null) {
 			String owner = Type.getInternalName(m.getDeclaringClass());
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, m.getName(), Type.getMethodDescriptor(m));
 			this.Builder.typeStack.push(Type.getReturnType(m));
 		} else {
 //			int opcode = Node.Func.Is(NativeStaticFunc) ? INVOKESTATIC : INVOKEVIRTUAL;
 			int opcode = INVOKESTATIC;
 			String owner = defaultClassName;//FIXME
 			String methodName = Func.GetNativeFuncName();  // IMSORRY
 			String methodDescriptor = this.GetAsmMethodType(Func).getDescriptor();
 			this.Builder.AsmMethodVisitor.visitMethodInsn(opcode, owner, methodName, methodDescriptor);
 			this.Builder.typeStack.push(this.GetAsmType(Func.GetReturnType()));
 		}
 	}
 
 	private char GetOpDesc(Type type) {
 		// int->I, boolean->Z, Object->L
 		return type.getDescriptor().charAt(0);
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		Node.LeftNode.Evaluate(this);
 		Type leftType = this.Builder.typeStack.pop();
 		Node.RightNode.Evaluate(this);
 		Type rightType = this.Builder.typeStack.pop();
 		String FuncName = Node.Func.FuncName;
 		String OperatorFuncName = "binary_" + FuncName + "_" + GetOpDesc(leftType) + GetOpDesc(rightType);
 		Method m = this.methodMap.get(OperatorFuncName);
 		if(m != null) {
 			String owner = Type.getInternalName(m.getDeclaringClass());
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, m.getName(), Type.getMethodDescriptor(m));
 			this.Builder.typeStack.push(Type.getReturnType(m));
 			return;
 		} else {
 			throw new RuntimeException("unsupport binary operator: " + FuncName);
 		}
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		Node.Expr.Evaluate(this);
 		Type type = this.Builder.typeStack.pop();
 		String FuncName = Node.Func.FuncName;
 		String OperatorFuncName = "unary_" + FuncName + "_" + GetOpDesc(type);
 		Method m = this.methodMap.get(OperatorFuncName);
 		if(m != null) {
 			String owner = Type.getInternalName(m.getDeclaringClass());
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, owner, m.getName(), Type.getMethodDescriptor(m));
 			this.Builder.typeStack.push(Type.getReturnType(m));
 		} else {
 			throw new RuntimeException("unsupport unary operator: " + FuncName);
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
 		Node.RightNode.Evaluate(this);
 		if(Node.LeftNode instanceof GetterNode) {
 			GetterNode left = (GetterNode) Node.LeftNode;
 			String name = left.Func.FuncName;
 			Type ty = this.GetAsmType(left.Func.Types[0]);//FIXME
 
 			left.Expr.Evaluate(this);
 			this.Builder.AsmMethodVisitor.visitLdcInsn(name);
 			Node.RightNode.Evaluate(this);
 			this.Builder.box();
 			this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC,
 					"JavaByteCodeGenerator", "setter", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V");
 			this.Builder.typeStack.pop();
 			this.Builder.typeStack.push(ty);
 		}
 		else {
 			assert (Node.LeftNode instanceof LocalNode);
 			LocalNode Left = (LocalNode) Node.LeftNode;
 			String Name = Left.NativeName;
 			JVMLocal local = this.Builder.FindLocalVariable(Name);
 			if(local == null) {
 				throw new RuntimeException("local variable " + Name + " is not found in this context");
 			}
 			this.Builder.StoreLocal(local);
 		}
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		JVMLocal local = this.Builder.AddLocal(Node.Type, Node.VariableName);
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
 
 	@Override public void VisitSwitchNode(SwitchNode Node) { //FIXME
 //		Node.CondExpr.Evaluate(this);
 //		for(int i = 0; i < Node.Blocks.size(); i++) {
 //			TypedNode Block = (TypedNode) Node.Blocks.get(i);
 //			this.VisitBlock(Block);
 //		}
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		MethodVisitor mv = this.Builder.AsmMethodVisitor;
 		Label HEAD = new Label();
 		Label END = new Label();
 		this.Builder.LabelStack.AddLabel("break", END);
 		this.Builder.LabelStack.AddLabel("continue", HEAD);
 		mv.visitLabel(HEAD);
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop();
 		mv.visitJumpInsn(IFEQ, END); // condition
 		this.VisitBlock(Node.LoopBody);
 
 		mv.visitJumpInsn(GOTO, HEAD);
 		mv.visitLabel(END);
 		this.Builder.LabelStack.RemoveLabel("break");
 		this.Builder.LabelStack.RemoveLabel("continue");
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
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 		String LabelName = Node.Label;
 		Label Label = new Label();
 		this.Builder.LabelStack.AddLabel(LabelName, Label);
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
 		String LabelName = Node.Label;
 		Label label = this.Builder.LabelStack.FindLabel(LabelName);
 		if(label == null) {
 			throw new RuntimeException("Cannot find " + LabelName + " label.");
 		}
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, label);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		Label l = this.Builder.LabelStack.FindLabel(Node.Label);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		Label l = this.Builder.LabelStack.FindLabel(Node.Label);
 		this.Builder.AsmMethodVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) { //FIXME
 		int catchSize = 1;
 		MethodVisitor mv = this.Builder.AsmMethodVisitor;
 		Label beginTryLabel = new Label();
 		Label endTryLabel = new Label();
 		Label finallyLabel = new Label();
 		Label catchLabel[] = new Label[catchSize];
 		String throwType = Type.getInternalName(Throwable.class);
 
 		// prepare
 		for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
 			catchLabel[i] = new Label();
 			mv.visitTryCatchBlock(beginTryLabel, endTryLabel, catchLabel[i], throwType);
 		}
 
 		// try block
 		mv.visitLabel(beginTryLabel);
 		this.VisitBlock(Node.TryBlock);
 		mv.visitLabel(endTryLabel);
 		mv.visitJumpInsn(GOTO, finallyLabel);
 
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
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) { //FIXME
 		String ps = Type.getDescriptor(System.err.getClass());
 		this.Builder.AsmMethodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "err", ps);
 //		this.Builder.methodVisitor.visitLdcInsn(Node.ErrorMessage); // FIXME
 		this.Builder.AsmMethodVisitor.visitLdcInsn("(ErrorNode)");
 		this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		ArrayList<GtNode> Args = new ArrayList<GtNode>();
 		CommandNode node = Node;
 		while(node != null) {
 			Args.addAll(node.Params);
 			node = (CommandNode) node.PipedNextNode;
 		}
 		this.Builder.AsmMethodVisitor.visitLdcInsn(Args.size());
 		this.Builder.AsmMethodVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String.class));
 		for(int i=0; i<Args.size(); i++) {
 			GtNode Arg = Args.get(i);
 			this.Builder.AsmMethodVisitor.visitInsn(DUP);
 			this.Builder.AsmMethodVisitor.visitLdcInsn(i);
 			Arg.Evaluate(this);
 			this.Builder.typeStack.pop();
 			this.Builder.AsmMethodVisitor.visitInsn(AASTORE);
 		}
 		this.Builder.AsmMethodVisitor.visitMethodInsn(INVOKESTATIC, Type.getInternalName(GtSubProc.class),
 				"ExecCommandBool", "([Ljava/lang/String;)Z");
 		this.Builder.AsmMethodVisitor.visitInsn(POP);
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
 
 	public void VisitEnd() {
 		this.Builder.AsmMethodVisitor.visitEnd();
 	}
 }
