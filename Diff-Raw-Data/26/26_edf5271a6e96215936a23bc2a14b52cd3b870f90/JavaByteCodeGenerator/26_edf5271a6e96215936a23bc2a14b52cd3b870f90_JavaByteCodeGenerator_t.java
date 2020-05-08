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
 import java.util.List;
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
 
 // GreenTea Generator should be written in each language.
 
 class GtClassNode implements Opcodes {
 	final String					name;
 	final String					superClass;
 	final Map<String, MethodNode>	methods	= new HashMap<String, MethodNode>();
 	final Map<String, FieldNode>	fields	= new HashMap<String, FieldNode>();
 
 	public GtClassNode(String name, String superClass) {
 		this.name = name;
 		this.superClass = superClass;
 	}
 
 	public void accept(ClassVisitor cv) {
 		cv.visit(V1_6, ACC_PUBLIC, this.name, null, this.superClass, null);
 		for(FieldNode f : this.fields.values()) {
 			f.accept(cv);
 		}
 		for(MethodNode m : this.methods.values()) {
 			m.accept(cv);
 		}
 	}
 
 	public MethodNode getMethodNode(String name) {
 		return this.methods.get(name);
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
 		byte[] b = this.Gen.generateBytecode(name);
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
 
 class JVMLocal {
 	public String		Name;
 	public GtType		TypeInfo;
 	public int			Index;
 
 	public JVMLocal(int Index, GtType TypeInfo, String Name) {
 		this.Index = Index;
 		this.TypeInfo = TypeInfo;
 		this.Name = Name;
 	}
 }
 
 class JVMBuilder implements Opcodes {
 
 	ArrayList<JVMLocal>                LocalVals;
 	GtFunc                        FuncInfo;
 
 	MethodVisitor                   methodVisitor;
 	Stack<Type>                     typeStack;
 	LabelStack                      LabelStack;
 	GtNameSpace                     NameSpace;
 	JVMTypeResolver                    TypeResolver;
 
 	public JVMBuilder(GtFunc method, MethodVisitor mv, JVMTypeResolver TypeResolver, GtNameSpace NameSpace) {
 		this.LocalVals = new ArrayList<JVMLocal>();
 		this.FuncInfo = method;
 
 		this.methodVisitor = mv;
 		this.typeStack = new Stack<Type>();
 		this.LabelStack = new LabelStack();
 		this.TypeResolver = TypeResolver;
 		this.NameSpace = NameSpace;
 	}
 
 	void LoadLocal(JVMLocal local) {
 		GtType gtype = local.TypeInfo;
 		Type type = this.TypeResolver.GetAsmType(gtype);
 		this.typeStack.push(type);
 		this.methodVisitor.visitVarInsn(type.getOpcode(ILOAD), local.Index);
 	}
 
 	void StoreLocal(JVMLocal local) {
 		GtType gtype = local.TypeInfo;
 		Type type = this.TypeResolver.GetAsmType(gtype);
 		this.typeStack.pop(); //TODO: check cast
 		this.methodVisitor.visitVarInsn(type.getOpcode(ISTORE), local.Index);
 	}
 
 	public void boxingReturn() {
 		if(this.typeStack.empty()) {
 			this.methodVisitor.visitInsn(ACONST_NULL);
 		}
 		else {
 			this.box();
 		}
 		this.methodVisitor.visitInsn(ARETURN);
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
 			this.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
 			this.typeStack.push(Type.getType(Integer.class));
 		}
 		else if(type.equals(Type.DOUBLE_TYPE)) {
 			this.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
 			this.typeStack.push(Type.getType(Double.class));
 		}
 		else if(type.equals(Type.BOOLEAN_TYPE)) {
 			this.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
 			this.typeStack.push(Type.getType(Boolean.class));
 		}
 		else if(type.equals(Type.VOID_TYPE)) {
 			this.methodVisitor.visitInsn(ACONST_NULL);//FIXME: return void
 			this.typeStack.push(Type.getType(Void.class));
 		}
 		else {
 			this.typeStack.push(type);
 		}
 	}
 
 	void unbox(Type type) {
 		if(type.equals(Type.INT_TYPE)) {
 			this.methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Integer");
 			this.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
 			this.typeStack.push(Type.INT_TYPE);
 		}
 		else if(type.equals(Type.DOUBLE_TYPE)) {
 			this.methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Double");
 			this.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleValue", "()D");
 			this.typeStack.push(Type.DOUBLE_TYPE);
 		}
 		else if(type.equals(Type.BOOLEAN_TYPE)) {
 			this.methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
 			this.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "booleanValue", "()Z");
 			this.typeStack.push(Type.BOOLEAN_TYPE);
 		}
 		else {
 			this.typeStack.push(type);
 		}
 	}
 }
 
 class JVMTypeResolver {
 	private final Map<String, GtClassNode>	classMap			= new HashMap<String, GtClassNode>();
 	private final Map<String, String>		typeDescriptorMap	= new HashMap<String, String>();
 
 	public JVMTypeResolver(GtClassContext Context) {
 		this.typeDescriptorMap.put(Context.VoidType.ShortClassName, Type.getType(void.class).getDescriptor());
 		this.typeDescriptorMap.put(Context.BooleanType.ShortClassName, Type.getType(boolean.class).getDescriptor());
 		this.typeDescriptorMap.put(Context.IntType.ShortClassName, Type.getType(int.class).getDescriptor());
 		//this.typeDescriptorMap.put(Context.ObjectType.ShortClassName, Type.getType(Object.class).getDescriptor());
 		this.typeDescriptorMap.put(Context.StringType.ShortClassName, Type.getType(String.class).getDescriptor());
 		// TODO: other class
 	}
 
 	// FIXME
 	public String GetJavaTypeDescriptor(GtType type) {
 		String descriptor = this.typeDescriptorMap.get(type.ShortClassName);
 		if(descriptor != null) {
 			return descriptor;
 		}
 		if(type.NativeSpec != null) { //HostedClassInfo -> NativeSpec
 			if(type.NativeSpec instanceof GtClassField) {
 				return Type.getDescriptor(Object.class);
 			}
 			return Type.getDescriptor((Class<?>) type.NativeSpec); //HostedClassInfo -> NativeSpec
 		}
 		else {
 			return "L" + type.ShortClassName + ";";//FIXME
 		}
 	}
 
 	public String GetJavaFuncDescriptor(GtFunc method) {
 		GtType returnType = method.GetReturnType();
 		//paramTypes.remove(0);
 		StringBuilder signature = new StringBuilder();
 		signature.append("(");
 //		if(method.GetRecvType().ShortClassName.equals("global")) {
 //			signature.append(globalType);
 //		}
 		for(int i = 0; i < method.GetFuncParamSize(); i++) {
 			GtType ParamType = method.GetFuncParamType(i);
 			signature.append(this.GetJavaTypeDescriptor(ParamType));
 		}
 		signature.append(")");
 		if(method.FuncName.equals("New")) {
 			signature.append(Type.VOID_TYPE.getDescriptor());
 		}
 		else {
 			signature.append(this.GetJavaTypeDescriptor(returnType));
 		}
 		return signature.toString();
 	}
 
 	public GtClassNode FindClassNode(String className) {
 		return this.classMap.get(className);
 	}
 
 	public void StoreClassNode(GtClassNode cn) {
 		this.classMap.put(cn.name, cn);
 	}
 
 	public Type GetAsmType(Class<?> klass) {
 		return Type.getType(klass);
 	}
 
 	public Type GetAsmType(GtType type) {
 		return Type.getType(GetJavaTypeDescriptor(type));
 	}
 }
 
 class JVMConstPool {
 	private final List<Object> constValues = new ArrayList<Object>();
 
 	public int add(Object o) {
 		int id = constValues.indexOf(o);
 		if(id != -1) {
 			return id;
 		}
 		else {
 			constValues.add(o);
 			return constValues.size() - 1;
 		}
 	}
 
 	public Object get(int id) {
 		return constValues.get(id);
 	}
 }
 
 public class JavaByteCodeGenerator extends GtGenerator implements Opcodes {
 	private static JVMConstPool constPool = new JVMConstPool();
 	private JVMTypeResolver TypeResolver;
 	private JVMBuilder Builder;
 	private final String defaultClassName = "Global";
 	private GtClassNode globalNode = new GtClassNode(defaultClassName, "java/lang/Object") {{
 		// create default methods
 		{
 		MethodNode p = new MethodNode(ACC_PUBLIC | ACC_STATIC, "println", "(I)V", null, null);
 		p.visitVarInsn(ILOAD, 0);
 		p.visitMethodInsn(INVOKESTATIC, "JVM/StaticMethods", "println", "(I)V");
 		p.visitInsn(RETURN);
 		this.methods.put(p.name, p);
 		}
 		{
 		MethodNode p = new MethodNode(ACC_PUBLIC | ACC_STATIC, "assert", "(Z)V", null, null);
 		p.visitVarInsn(ILOAD, 0);
 		p.visitMethodInsn(INVOKESTATIC, "JVM/StaticMethods", "assert_", "(Z)V");
 		p.visitInsn(RETURN);
 		this.methods.put(p.name, p);
 		}
 	}};
 
 	public JavaByteCodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.TypeResolver = null;
 	}
 
 	void LoadConst(Object o) {
 		Type type;
 		boolean unsupportType = false;
 		if(o instanceof Integer) {
 			type = Type.INT_TYPE;
 		}
 		else if(o instanceof Boolean) {
 			type = Type.BOOLEAN_TYPE;
 		}
 		else if(o instanceof String) {
 			type = this.TypeResolver.GetAsmType(o.getClass());
 		}
 		else {
 			unsupportType = true;
 			type = this.TypeResolver.GetAsmType(o.getClass());
 		}
 		this.Builder.typeStack.push(type);
 		if(unsupportType) {
 			int id = constPool.add(o);
 			String owner = Type.getInternalName(this.getClass());
 			String methodName = "getConstValue";
 			String methodDesc = "(I)Ljava/lang/Object;";
 			this.Builder.methodVisitor.visitLdcInsn(id);
 			this.Builder.methodVisitor.visitMethodInsn(INVOKESTATIC, owner, methodName, methodDesc);
 			this.Builder.methodVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(o.getClass()));
 		}
 		else {
 			this.Builder.methodVisitor.visitLdcInsn(o);
 		}
 	}
 
 	public static Object getConstValue(int id) {
 		return constPool.get(id);
 	}
 
 	public void OutputClassFile(String className, String dir) throws IOException {
 		byte[] ba = this.generateBytecode(className);
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
 
 	public byte[] generateBytecode(String className) {
 		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
 		GtClassNode CNode = this.TypeResolver.FindClassNode(className);
 		CNode.accept(classWriter);
 		classWriter.visitEnd();
 		return classWriter.toByteArray();
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		int acc = ACC_PUBLIC | ACC_STATIC;
 		String methodName = "__eval";
 		String methodDesc = "()Ljava/lang/Object;";
 		MethodNode mn = new MethodNode(acc, methodName, methodDesc, null, null);
 		GtClassNode c = globalNode;
 		c.methods.put(methodName, mn);
 		TypeResolver.StoreClassNode(c);
 
 		this.Builder = new JVMBuilder(null, mn, TypeResolver, null);
 		this.VisitBlock(Node);
 		this.Builder.boxingReturn();
 		try {
 			this.OutputClassFile(defaultClassName, ".");
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		//execute
 		try {
 			GtClassLoader loader = new GtClassLoader(this);
 			Class<?> klass = loader.loadClass(defaultClassName);
 			Object res = klass.getMethod(methodName).invoke(null);
 			return res;
 		} catch(ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			if(cause instanceof Error) {
				throw (Error)cause;
			}
			cause.printStackTrace();
 		} catch(IllegalAccessException e) {
 			e.printStackTrace();
 		} catch(NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> NameList, GtNode Body) {
 		int acc = ACC_PUBLIC;
 		//if(!Func.Is(ExportFunc)) {
 			acc |= ACC_STATIC;
 		//}
 		Type retType = TypeResolver.GetAsmType(Func.GetReturnType());
 		ArrayList<Type> argTypes = new ArrayList<Type>();
 		ArrayList<JVMLocal> locals  = new ArrayList<JVMLocal>();
 		for(int i=0; i<NameList.size(); i++) {
 			GtType type = Func.GetFuncParamType(i);
 			String name = NameList.get(i);
 			argTypes.add(TypeResolver.GetAsmType(type));
 			locals.add(new JVMLocal(i, type, name));
 		}
 
 		String methodName = Func.FuncName;
 		String methodDesc = Type.getMethodDescriptor(retType, argTypes.toArray(new Type[0]));
 
 		MethodNode mn = new MethodNode(acc, methodName, methodDesc, null, null);
 		GtClassNode c = globalNode;
 		c.methods.put(methodName, mn);
 		TypeResolver.StoreClassNode(c);
 
 		this.Builder = new JVMBuilder(Func, mn, TypeResolver, null);
 		this.Builder.LocalVals.addAll(locals);
 		this.VisitBlock(Body);
 		if(retType.equals(Type.VOID_TYPE)) {
 			this.Builder.methodVisitor.visitInsn(RETURN);//FIXME
 		}
 
 		try {
 			this.OutputClassFile(defaultClassName, ".");
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		String className = Type.ShortClassName;
 		GtClassNode superClassNode = TypeResolver.FindClassNode(Type.SuperType.ShortClassName);
 		String superClassName = superClassNode != null ? superClassNode.name : "java/lang/Object";
 		GtClassNode classNode = new GtClassNode(className, superClassName);
 		TypeResolver.StoreClassNode(classNode);
 		// generate field
 		for(GtFieldInfo field : ClassField.FieldList) {
 			int access = ACC_PUBLIC;
 			String fieldName = field.NativeName;
 			Type fieldType = TypeResolver.GetAsmType(field.Type);
 			FieldNode node = new FieldNode(access, fieldName, fieldType.getDescriptor(), null, null);
 			classNode.fields.put(fieldName, node);
 		}
 		// generate default constructor (for jvm)
 		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
 		constructor.visitVarInsn(ALOAD, 0);
 		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V");
 		constructor.visitInsn(RETURN);
 		classNode.methods.put("<init>", constructor);
 		try {
 			this.OutputClassFile(className, ".");
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 //	@Override public void AddClass(GtType Type) {
 //	}
 
 	@Override public void InitContext(GtClassContext Context) {
 		this.TypeResolver = new JVMTypeResolver(Context);
 		InitEmbeddedFunc();
 		super.InitContext(Context);
 	}
 
 	private void InitEmbeddedFunc() {
 //		new GtIntDef(Context.RootNameSpace, NMMap).MakeDefinition();
 //		new GtStringDef(Context.RootNameSpace, NMMap).MakeDefinition();
 //		new GtSystemDef(Context.RootNameSpace, NMMap).MakeDefinition();
 	}
 
 	void Call(int opcode, GtFunc Func) {
 		String owner = defaultClassName;//FIXME
 		String methodName = Func.FuncName;
 		String methodDescriptor = this.TypeResolver.GetJavaFuncDescriptor(Func);
 		this.Builder.methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDescriptor);
 		this.Builder.typeStack.push(this.TypeResolver.GetAsmType(Func.GetReturnType()));
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		Object constValue = Node.ConstValue;
 		this.LoadConst(constValue);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		Type type = Type.getType(Node.Func.Types[0].ShortClassName);
 		String owner = type.getInternalName();
 		this.Builder.methodVisitor.visitTypeInsn(NEW, owner);
 		this.Builder.methodVisitor.visitInsn(DUP);
 		this.Builder.methodVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "()V");
 		this.Builder.typeStack.push(type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		GtType TypeInfo = Node.Type;
 		if(TypeInfo.DefaultNullValue != null) {
 			this.LoadConst(TypeInfo.DefaultNullValue);
 		}
 		else {
 			// FIXME support primitive type (e.g. int)
 			this.Builder.typeStack.push(this.TypeResolver.GetAsmType(Object.class));
 			this.Builder.methodVisitor.visitInsn(ACONST_NULL);
 		}
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		String FieldName = Node.NativeName;
 		JVMLocal local;
 		if((local = this.Builder.FindLocalVariable(FieldName)) == null) {
 			throw new RuntimeException("local variable not found:" + FieldName);
 		}
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
 		Type ty = TypeResolver.GetAsmType(Node.Func.Types[2]);//FIXME
 		Node.Expr.Evaluate(this);
 		this.Builder.methodVisitor.visitLdcInsn(name);
 		this.Builder.methodVisitor.visitMethodInsn(INVOKESTATIC,
 				"JavaByteCodeGenerator", "getter", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
 		this.Builder.unbox(ty);
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
		//FIXME -n
		if(Node.Params.size() == 1 && Node.Token.EqualsText("-")) {
			Node.Params.get(0).Evaluate(this);
			this.Builder.methodVisitor.visitInsn(INEG);
			return;
		}
 		GtFunc Func = Node.Func;
 		for(int i = 1; i < Node.Params.size(); i++) {
 			GtNode Param = Node.Params.get(i);
 			Param.Evaluate(this);
 			Type requireType = this.TypeResolver.GetAsmType(Func.GetFuncParamType(i - 1));
 			Type foundType = this.Builder.typeStack.peek();
 			if(requireType.equals(Type.getType(Object.class)) && this.Builder.isPrimitiveType(foundType)) {
 				// boxing
 				this.Builder.box();
 			}
 			else {
 				this.Builder.typeStack.pop();
 			}
 		}
 		if(Func.FuncName.equals("New")) {
 			Type type = this.TypeResolver.GetAsmType(Func.GetReturnType());
 			String owner = type.getInternalName();
 			String methodName = "<init>";
 			String methodDesc = TypeResolver.GetJavaFuncDescriptor(Func);//"()V";//Node.Params;
 			this.Builder.methodVisitor.visitMethodInsn(INVOKESPECIAL, owner, methodName, methodDesc);
 			this.Builder.typeStack.push(type);
 		}
 		else {
 			int opcode = INVOKEVIRTUAL;
 			//if(Node.Func.Is(KonohaConst.StaticFunc)) {
 			opcode = INVOKESTATIC;
 			//}
 			this.Call(opcode, Func);
 		}
 	}
 
 	Map<String, Integer> opInstMap = new HashMap<String, Integer>();
 	Map<String, Integer> relInstMap = new HashMap<String, Integer>();
 	{
 		opInstMap.put("+", IADD);
 		opInstMap.put("-", ISUB);
 		opInstMap.put("*", IMUL);
 		opInstMap.put("/", IDIV);
 		opInstMap.put("%", IREM);
 		opInstMap.put("<<", ISHL);
 		opInstMap.put(">>", ISHR);
 		relInstMap.put("==", IF_ICMPEQ);
 		relInstMap.put("!=", IF_ICMPNE);
 		relInstMap.put("<" , IF_ICMPLT);
 		relInstMap.put("<=", IF_ICMPLE);
 		relInstMap.put(">" , IF_ICMPGT);
 		relInstMap.put(">=", IF_ICMPGE);
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		Node.LeftNode.Evaluate(this);
 		Node.RightNode.Evaluate(this);
 
 		String methodName = Node.Token.ParsedText;
 		Integer inst = opInstMap.get(methodName);
 		if(inst != null) {
 			this.Builder.methodVisitor.visitInsn(inst);
 			this.Builder.typeStack.pop();
 			this.Builder.typeStack.push(Type.INT_TYPE);
 			return;
 		}
 		inst = relInstMap.get(methodName);
 		if(inst != null) {
 			Type t = this.Builder.typeStack.pop();
 			if(t.getDescriptor().startsWith("L")) {
 				if(inst == IF_ICMPEQ) inst = IF_ACMPEQ;
 				if(inst == IF_ICMPNE) inst = IF_ACMPNE;
 			}
 			this.Builder.typeStack.pop();
 			this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 
 			Label thenLabel = new Label();
 			Label mergeLabel = new Label();
 			this.Builder.methodVisitor.visitJumpInsn(inst, thenLabel);
 			this.Builder.methodVisitor.visitInsn(ICONST_0);
 			this.Builder.methodVisitor.visitJumpInsn(GOTO, mergeLabel);
 			this.Builder.methodVisitor.visitLabel(thenLabel);
 			this.Builder.methodVisitor.visitInsn(ICONST_1);
 			this.Builder.methodVisitor.visitLabel(mergeLabel);
 			return;
 		}
 		throw new RuntimeException("unsupport binary operator: " + methodName);
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		ApplyNode applyNode = new ApplyNode(Node.Type, Node.Token, Node.Func);
 		applyNode.Append(Node.Expr);
 
 		VisitApplyNode(applyNode);
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Label elseLabel = new Label();
 		Label mergeLabel = new Label();
 		Node.LeftNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.methodVisitor.visitJumpInsn(IFEQ, elseLabel);
 
 		Node.RightNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.methodVisitor.visitJumpInsn(IFEQ, elseLabel);
 
 		this.Builder.methodVisitor.visitLdcInsn(true);
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.methodVisitor.visitLabel(elseLabel);
 		this.Builder.methodVisitor.visitLdcInsn(false);
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.methodVisitor.visitLabel(mergeLabel);
 		this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Label thenLabel = new Label();
 		Label mergeLabel = new Label();
 		Node.LeftNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.methodVisitor.visitJumpInsn(IFNE, thenLabel);
 
 		Node.RightNode.Evaluate(this);
 		this.Builder.typeStack.pop();
 		this.Builder.methodVisitor.visitJumpInsn(IFNE, thenLabel);
 
 		this.Builder.methodVisitor.visitLdcInsn(false);
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.methodVisitor.visitLabel(thenLabel);
 		this.Builder.methodVisitor.visitLdcInsn(true);
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.Builder.methodVisitor.visitLabel(mergeLabel);
 		this.Builder.typeStack.push(Type.BOOLEAN_TYPE);
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		if(Node.LeftNode instanceof GetterNode) {
 			GetterNode left = (GetterNode) Node.LeftNode;
 			String name = left.Func.FuncName;
 			Type ty = TypeResolver.GetAsmType(left.Func.Types[0]);//FIXME
 
 			left.Expr.Evaluate(this);
 			this.Builder.methodVisitor.visitLdcInsn(name);
 			Node.RightNode.Evaluate(this);
 			this.Builder.box();
 			this.Builder.methodVisitor.visitMethodInsn(INVOKESTATIC,
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
 
 	@Override public void VisitLetNode(LetNode Node) {
 		JVMLocal local = this.Builder.AddLocal(Node.Type, Node.VariableName);
 		Node.InitNode.Evaluate(this);
 		this.Builder.StoreLocal(local);
 		this.VisitBlock(Node.BlockNode);
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Label ELSE = new Label();
 		Label END = new Label();
 		MethodVisitor mv = this.Builder.methodVisitor;
 		Node.CondExpr.Evaluate(this);
 		this.Builder.typeStack.pop(); //TODO: check cast
 		mv.visitJumpInsn(IFEQ, ELSE);
 
 		// Then
 		if(Node.ThenNode != null) {
 			Node.ThenNode.Evaluate(this);
 		}
 		mv.visitJumpInsn(GOTO, END);
 
 		// Else
 		mv.visitLabel(ELSE);
 		if(Node.ElseNode != null) {
 			Node.ElseNode.Evaluate(this);
 			mv.visitJumpInsn(GOTO, END);
 		}
 
 		// End
 		mv.visitLabel(END);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) { //FIXME
 //		Node.CondExpr.Evaluate(this);
 //		for(int i = 0; i < Node.Blocks.size(); i++) {
 //			TypedNode Block = (TypedNode) Node.Blocks.get(i);
 //			this.VisitBlock(Block);
 //		}
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		MethodVisitor mv = this.Builder.methodVisitor;
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
 			this.Builder.methodVisitor.visitInsn(type.getOpcode(IRETURN));
 		}
 		else {
 			this.Builder.methodVisitor.visitInsn(RETURN);
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
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, label);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		Label l = this.Builder.LabelStack.FindLabel(Node.Label);
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		Label l = this.Builder.LabelStack.FindLabel(Node.Label);
 		this.Builder.methodVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) { //FIXME
 		int catchSize = 1;
 		MethodVisitor mv = this.Builder.methodVisitor;
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
 
 	public static class WrappedThrowable extends Throwable {
 		public final Object object;
 		public WrappedThrowable(Object object) {
 			this.object = object;
 		}
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		// use wrapper
 		String name = Type.getInternalName(WrappedThrowable.class);
 		this.Builder.methodVisitor.visitTypeInsn(NEW, name);
 		this.Builder.methodVisitor.visitInsn(DUP);
 		Node.Expr.Evaluate(this);
 		this.Builder.box();
 		this.Builder.typeStack.pop();
 		this.Builder.methodVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
 		this.Builder.methodVisitor.visitInsn(ATHROW);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) { //FIXME
 		String ps = Type.getDescriptor(System.err.getClass());
 		this.Builder.methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "err", ps);
 //		this.Builder.methodVisitor.visitLdcInsn(Node.ErrorMessage); // FIXME
 		this.Builder.methodVisitor.visitLdcInsn("(ErrorNode)");
 		this.Builder.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		//TODO: call GtSubProc#ExecCommandString, ExecCommandBool, ExecCommandVoid,
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		try {
 			GtClassLoader loader = new GtClassLoader(this);
 			Class<?> klass = loader.loadClass(defaultClassName);
 			Method m = klass.getMethod("main");
 			if(m != null) {
 				m.invoke(null);
 			}
 		} catch(ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			if(cause instanceof Error) {
				throw (Error)cause;
			}
			cause.printStackTrace();
 		} catch(IllegalAccessException e) {
 			e.printStackTrace();
 		} catch(NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void VisitEnd() {
 		this.Builder.methodVisitor.visitEnd();
 	}
 }
 
 class EmbeddedFuncDef extends GtStatic {
 	private GtNameSpace NameSpace;
 
 	// Embedded GtType
 	final GtType VoidType;
 	//final GtType ObjectType;
 	final GtType BooleanType;
 	final GtType IntType;
 	final GtType StringType;
 	final GtType VarType;
 	final GtType AnyType;
 
 	public static Method LookupFunc(Object Callee, String FuncName) {
 		if(FuncName != null) {
 			// LibGreenTea.DebugP("looking up method : " + Callee.getClass().getSimpleName() + "." + FuncName);
 			Method[] methods = Callee.getClass().getMethods();
 			for(int i = 0; i < methods.length; i++) {
 				if(FuncName.equals(methods[i].getName())) {
 					return methods[i];
 				}
 			}
 			LibGreenTea.DebugP("method not found: " + Callee.getClass().getSimpleName() + "." + FuncName);
 		}
 		return null;
 		/*throw new KonohaParserException("method not found: " + callee.getClass().getName() + "." + methodName);*/
 	}
 
 	public static ArrayList<GtType> MakeParamTypeList(GtType ReturnType, GtType RecvType, GtType...ParamTypes) {
 		ArrayList<GtType> paramTypeList = new ArrayList<GtType>();
 		paramTypeList.add(ReturnType);
 		paramTypeList.add(RecvType);
 		for(int i = 0; i < ParamTypes.length; i++) {
 			paramTypeList.add(ParamTypes[i]);
 		}
 		return paramTypeList;
 	}
 
 	public EmbeddedFuncDef(GtNameSpace NameSpace) {
 		this.NameSpace = NameSpace;
 
 		this.VoidType = NameSpace.Context.VoidType;
 		//this.ObjectType = NameSpace.Context.ObjectType;
 		this.BooleanType = NameSpace.Context.BooleanType;
 		this.IntType = NameSpace.Context.IntType;
 		this.StringType = NameSpace.Context.StringType;
 		this.VarType = NameSpace.Context.VarType;
 		this.AnyType = NameSpace.Context.AnyType;
 	}
 
 	public void MakeDefinition() {
 
 	}
 
 	void RegisterFunc(int FuncFlag, String FuncName, ArrayList<GtType> ParamTypeList, Object Callee, String LocalName) {
 		// Code generator does not need to regeister any functions (all things are controlled in parser)
 		// Add native method to GtFunc that is generated in the parser
 //		GtFunc newFunc = new GtFunc(FuncFlag | NativeFunc, FuncName, 0, ParamTypeList);
 //		GtType[] paramTypes = LangDeps.CompactTypeList(0, ParamTypeList);
 //		Method mtd = LookupFunc(Callee, LocalName);
 //		NMMap.PutFuncInvoker(newFunc, new JavaMethodInvoker(paramTypes, mtd));
 		//Code generator does not need to regeister any functions (all things are controlled in parser)
 		//NameSpace.AppendFunc(newFunc);
 	}
 
 	GtType RegisterClass(int ClassFlag, String ClassName, Object DefaultNullValue) {
 		GtType newClass = new GtType(NameSpace.Context, ClassFlag, ClassName, DefaultNullValue, null);
 		NameSpace.AppendTypeName(newClass);
 		return newClass;
 	}
 }
