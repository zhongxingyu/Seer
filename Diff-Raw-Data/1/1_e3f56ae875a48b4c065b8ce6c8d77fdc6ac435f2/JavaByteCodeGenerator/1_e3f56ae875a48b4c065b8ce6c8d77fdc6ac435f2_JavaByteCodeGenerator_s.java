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
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Stack;
 
 import org.GreenTeaScript.DShell.DShellProcess;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Type;
 import org.objectweb.asm.tree.FieldNode;
 import org.objectweb.asm.tree.MethodNode;
 
 import static org.objectweb.asm.Opcodes.*;
 
 // GreenTea Generator should be written in each language.
 
 class JClassBuilder /*implements Opcodes */{
 	final String SourceFile;
 	final String ClassName;
 	final String SuperClassName;
 	final ArrayList<MethodNode> MethodList = new ArrayList<MethodNode>();
 	final ArrayList<FieldNode> FieldList = new ArrayList<FieldNode>();
 
 	JClassBuilder(String SourceFile, String ClassName, String SuperClass) {
 		this.SourceFile = SourceFile;
 		this.ClassName = ClassName;
 		this.SuperClassName = SuperClass;
 	}
 	
 	void AddMethod(MethodNode m) {
 		for(int i=0; i<MethodList.size(); i++) {
 			MethodNode node = this.MethodList.get(i);
 			if(node.name.equals(m.name) && node.desc.equals(m.desc)) {
 				this.MethodList.set(i, m);
 				return;
 			}
 		}
 		this.MethodList.add(m);
 	}
 
 	void AddField(FieldNode m) {
 		this.FieldList.add(m);
 	}
 	
 	byte[] GenerateBytecode() {
 		ClassWriter Visitor = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
 		Visitor.visit(V1_6, ACC_PUBLIC|ACC_FINAL, this.ClassName, null, this.SuperClassName, null);
 		Visitor.visitSource(this.SourceFile, null);
 		for(FieldNode f : this.FieldList) {
 			f.accept(Visitor);
 		}
 		for(MethodNode m : this.MethodList) {
 			m.accept(Visitor);
 		}
 		Visitor.visitEnd();
 		return Visitor.toByteArray();
 	}
 
 	void OutputClassFile(String className, String dir) {
 		byte[] ba = this.GenerateBytecode();
 		File file = new File(dir, this.ClassName + ".class");
 		try {
 			FileOutputStream fos = new FileOutputStream(file);
 			fos.write(ba);
 			fos.close();
 		}
 		catch(IOException e) {
 			LibGreenTea.VerboseException(e);
 		} 
 	}
 
 }
 
 class GreenTeaClassLoader extends ClassLoader {
 	final GtParserContext Context;
 	final HashMap<String,JClassBuilder> ByteCodeMap;
 	final String GlobalStaticClassName;
 	final String ContextFieldName;
 	final String GontextDescripter;
 	
 	public GreenTeaClassLoader(GtParserContext Context) {
 		this.Context = Context;
 		this.ByteCodeMap = new HashMap<String,JClassBuilder>();
 		
 		this.GlobalStaticClassName = "Global$" + Context.ParserId;
 		JClassBuilder GlobalClass = new JClassBuilder(null, this.GlobalStaticClassName, "java/lang/Object");
 		FieldNode fn = new FieldNode(ACC_STATIC, "ParserContext", Type.getDescriptor(GtParserContext.class), null, null);
 		this.ContextFieldName = fn.name;
 		this.GontextDescripter = fn.desc;
 		GlobalClass.AddField(fn);
 		
 		// static init
 		MethodNode mn = new MethodNode(ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V", null, null);
 		JMethodBuilder MethodBuilder = new JMethodBuilder(this, mn);
 		MethodBuilder.LoadConst(Context);
 		MethodBuilder.AsmVisitor.visitFieldInsn(PUTSTATIC, this.GlobalStaticClassName, this.ContextFieldName, this.GontextDescripter);
 		MethodBuilder.AsmVisitor.visitInsn(RETURN);
 		GlobalClass.AddMethod(mn);
 		byte[] b = GlobalClass.GenerateBytecode();
 		this.defineClass(this.GlobalStaticClassName, b, 0, b.length);
 	}
 
 	private void AddClassBuilder(JClassBuilder ClassBuilder) {
 		this.ByteCodeMap.put(ClassBuilder.ClassName, ClassBuilder);
 	}
 
 	JClassBuilder NewBuilder(String SourceFile, String ClassName, String SuperClassName) {
 		JClassBuilder cb = new JClassBuilder(SourceFile, ClassName, SuperClassName);
 		this.AddClassBuilder(cb);
 		return cb;
 	}
 	
 	JClassBuilder GenerateMethodHolderClass(String SourceFile, String FuncName, MethodNode AsmMethodNode) {
 		JClassBuilder HolderClass = new JClassBuilder(SourceFile, JLib.GetHolderClassName(Context, FuncName), "java/lang/Object");
 		this.AddClassBuilder(HolderClass);
 		HolderClass.AddMethod(AsmMethodNode);
 		return HolderClass;
 	}
 	
 	@Override protected Class<?> findClass(String name) {
 		JClassBuilder cb = this.ByteCodeMap.get(name);
 		if(cb != null) {
 			byte[] b = cb.GenerateBytecode();
 			this.ByteCodeMap.remove(name);
 			return this.defineClass(name, b, 0, b.length);
 		}
 		return null;
 	}
 
 }
 
 
 class JLib {
 	static HashMap<String, Type> TypeMap = new HashMap<String, Type>();
 	static Method GetConstPool;
 	static Method GetTypeById;
 	static Method GetFuncById;
 	static Method GetNameSpaceById;
 	static Method DynamicGetter;
 	static Method DynamicSetter;
 	static Method BoxBooleanValue;
 	static Method BoxIntValue;
 	static Method BoxFloatValue;
 	static Method UnboxBooleanValue;
 	static Method UnboxIntValue;
 	static Method UnboxFloatValue;
 	static Method GreenCastOperator;
 	static Method GreenInstanceOfOperator;
 	static Method NewArrayLiteral;
 	static Method NewArray;
 	static Method InvokeFunc;
 	static Method InvokeOverridedFunc;
 	static Method InvokeDynamicFunc;
 	static Method InvokeDynamicMethod;
 	
 	static Method ExecCommandVoid;
 	static Method ExecCommandBool;
 	static Method ExecCommandString;
 	
 	static {
 		TypeMap.put("void", Type.VOID_TYPE);
 		TypeMap.put("boolean", Type.BOOLEAN_TYPE);
 		TypeMap.put("int", Type.LONG_TYPE);
 		TypeMap.put("float", Type.DOUBLE_TYPE);
 		TypeMap.put("any", Type.getType(Object.class));
 		TypeMap.put("String", Type.getType(String.class));
 		TypeMap.put("Array", Type.getType(GreenTeaArray.class));
 		TypeMap.put("Func", Type.getType(GtFunc.class));
 
 		try {
 			GetConstPool = GtStaticTable.class.getMethod("GetConstPool", int.class);
 			GetTypeById = GtStaticTable.class.getMethod("GetTypeById", int.class);
 			GetFuncById = GtStaticTable.class.getMethod("GetFuncById", int.class);
 			DynamicGetter = LibGreenTea.class.getMethod("DynamicGetter", Object.class, String.class);
 			DynamicSetter = LibGreenTea.class.getMethod("DynamicSetter", Object.class, String.class, Object.class);
 			InvokeFunc = LibGreenTea.class.getMethod("InvokeFunc", GtFunc.class, Object[].class);
 			InvokeOverridedFunc = LibGreenTea.class.getMethod("InvokeOverridedMethod", long.class, GtNameSpace.class, GtFunc.class, Object[].class);
 			InvokeDynamicFunc = LibGreenTea.class.getMethod("InvokeDynamicFunc", long.class, GtType.class, GtNameSpace.class, String.class, Object[].class);
 			InvokeDynamicMethod = LibGreenTea.class.getMethod("InvokeDynamicMethod", long.class, GtType.class, GtNameSpace.class, String.class, Object[].class);
 			
 			BoxBooleanValue = Boolean.class.getMethod("valueOf", boolean.class);
 			BoxIntValue = Long.class.getMethod("valueOf", long.class);
 			BoxFloatValue = Double.class.getMethod("valueOf", double.class);
 			UnboxBooleanValue = Boolean.class.getMethod("booleanValue");
 			UnboxIntValue = Long.class.getMethod("longValue");
 			UnboxFloatValue = Double.class.getMethod("doubleValue");
 
 			GreenCastOperator = LibGreenTea.class.getMethod("DynamicCast", GtType.class, Object.class);
 			GreenInstanceOfOperator = LibGreenTea.class.getMethod("DynamicInstanceOf", Object.class, GtType.class);
 			NewArrayLiteral = LibGreenTea.class.getMethod("NewArrayLiteral", GtType.class, Object[].class);
 			NewArray = LibGreenTea.class.getMethod("NewArray", GtType.class, Object[].class);
 			ExecCommandVoid = DShellProcess.class.getMethod("ExecCommandVoid", String[][].class);
 			ExecCommandBool = DShellProcess.class.getMethod("ExecCommandBool", String[][].class);
 			ExecCommandString = DShellProcess.class.getMethod("ExecCommandString", String[][].class);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 			LibGreenTea.Exit(1, "load error");
 		}
 	}
 	
 	public static String GetHolderClassName(GtParserContext Context, String FuncName) {
 		return "FuncHolder" + FuncName + "$" + Context.ParserId;
 	}
 	
 	static Type GetAsmType(GtType GreenType) {
 		Type type = TypeMap.get(GreenType.ShortName);
 		if(type != null) {
 			return type;
 		}
 		if(GreenType.TypeBody != null && GreenType.TypeBody instanceof Class<?>) {
 			return Type.getType((Class<?>) GreenType.TypeBody);
 		}
 		if(GreenType.IsTypeVariable()) {
 			return Type.getType(Object.class);
 		}
 		if(GreenType.IsGenericType()) {
 			return GetAsmType(GreenType.BaseType);
 		}
 		return Type.getType("L" + GreenType.GetNativeName() + ";");
 	}
 
 	static String GetMethodDescriptor(GtFunc Func) {
 		Type ReturnType = GetAsmType(Func.GetReturnType());
 		Type[] argTypes = new Type[Func.GetFuncParamSize()];
 		for(int i = 0; i < argTypes.length; i++) {
 			GtType ParamType = Func.GetFuncParamType(i);
 			argTypes[i] = GetAsmType(ParamType);
 		}
 		return Type.getMethodDescriptor(ReturnType, argTypes);
 	}
 }
 
 final class JLocalVarStack {
 	public final String Name;
 	public final Type   TypeInfo;
 	public final int    Index;
 
 	public JLocalVarStack(int Index, Type TypeInfo, String Name) {
 		this.Index = Index;
 		this.TypeInfo = TypeInfo;
 		this.Name = Name;
 	}
 }
 
 class JMethodBuilder {
 	GreenTeaClassLoader           LocalClassLoader;
 	MethodVisitor                 AsmVisitor;
 	ArrayList<JLocalVarStack>     LocalVals;
 	int                           LocalSize;
 	Stack<Label>                  BreakLabelStack;
 	Stack<Label>                  ContinueLabelStack;
 	int PreviousLine;
 
 	public JMethodBuilder(GreenTeaClassLoader ClassLoader, MethodVisitor AsmVisitor) {
 		this.LocalClassLoader = ClassLoader;
 		this.AsmVisitor = AsmVisitor;
 		this.LocalVals = new ArrayList<JLocalVarStack>();
 		this.LocalSize = 0;
 		this.BreakLabelStack = new Stack<Label>();
 		this.ContinueLabelStack = new Stack<Label>();
 		this.PreviousLine = 0;
 	}
 
 	void SetLineNumber(long FileLine) {
 		if(FileLine != 0) {
 			int Line = GtStaticTable.GetFileLineNumber(FileLine);
 			if(Line != this.PreviousLine) {
 				Label LineLabel = new Label();
 				this.AsmVisitor.visitLineNumber(Line, LineLabel);
 				this.PreviousLine = Line;
 			}
 		}
 	}
 
 	void SetLineNumber(GtNode Node) {
 		this.SetLineNumber(Node.Token.FileLine);
 	}
 	
 	void LoadLocal(JLocalVarStack local) {
 		Type type = local.TypeInfo;
 		this.AsmVisitor.visitVarInsn(type.getOpcode(ILOAD), local.Index);
 	}
 
 	void StoreLocal(JLocalVarStack local) {
 		Type type = local.TypeInfo;
 		this.AsmVisitor.visitVarInsn(type.getOpcode(ISTORE), local.Index);
 	}
 
 	public JLocalVarStack FindLocalVariable(String Name) {
 		for(int i = 0; i < this.LocalVals.size(); i++) {
 			JLocalVarStack l = this.LocalVals.get(i);
 			if(l.Name.equals(Name)) {
 				return l;
 			}
 		}
 		return null;
 	}
 
 	public JLocalVarStack AddLocal(GtType GreenType, String Name) {
 		Type LocalType =  JLib.GetAsmType(GreenType);
 		JLocalVarStack local = new JLocalVarStack(this.LocalSize, LocalType, Name);
 		this.LocalVals.add(local);
 		this.LocalSize += LocalType.getSize();
 		return local;
 	}
 	
 	void LoadConst(Object Value) {
 		if(Value instanceof Boolean || Value instanceof Long || Value instanceof Double || Value instanceof String) {
 			this.AsmVisitor.visitLdcInsn(Value);
 			return;
 		}
 		if(Value instanceof GtParserContext) {
 			this.AsmVisitor.visitFieldInsn(GETSTATIC, this.LocalClassLoader.GlobalStaticClassName, this.LocalClassLoader.ContextFieldName, this.LocalClassLoader.GontextDescripter);
 			return;
 		}
 		if(Value instanceof GtType) {
 			int id = ((GtType)Value).TypeId;
 			this.AsmVisitor.visitLdcInsn(id);
 			this.InvokeMethodCall(GtType.class, JLib.GetTypeById);
 			return;
 		}
 		else if(Value instanceof GtFunc) {
 			int id = ((GtFunc)Value).FuncId;
 			this.AsmVisitor.visitLdcInsn(id);
 			this.InvokeMethodCall(GtFunc.class, JLib.GetFuncById);
 			return;
 		}
 		int id = GtStaticTable.AddConstPool(Value);
 		this.AsmVisitor.visitLdcInsn(id);
 		this.InvokeMethodCall(Value.getClass(), JLib.GetConstPool);
 	}
 
 	void LoadNewArray(GtGenerator Visitor, int StartIdx, ArrayList<GtNode> NodeList) {
 		this.AsmVisitor.visitLdcInsn(NodeList.size() - StartIdx);
 		this.AsmVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
 		//System.err.println("** arraysize = " + (NodeList.size() - StartIdx));
 		for(int i = StartIdx; i < NodeList.size(); i++) {
 			this.AsmVisitor.visitInsn(DUP);
 			this.AsmVisitor.visitLdcInsn(i);
 			NodeList.get(i).Evaluate(Visitor);
 			this.CheckCast(Object.class, NodeList.get(i).Type);
 			this.AsmVisitor.visitInsn(AASTORE);
 		}
 	}
 	
 	void CheckCast(Class<?> RequiredType, Class<?> GivenType) {
 		//System.err.println("giventype = " + GivenType + ", requested = " + RequiredType);
 		if(RequiredType == void.class || RequiredType == GivenType ) {
 			return;
 		}
 		if(RequiredType == long.class) {
 			if(GivenType == Object.class) {
 				this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
 				this.InvokeMethodCall(long.class, JLib.UnboxIntValue);
 				return;
 			}
 		}
 		if(RequiredType == double.class) {
 			if(GivenType == Object.class) {
 				this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
 				this.InvokeMethodCall(double.class, JLib.UnboxFloatValue);
 				return;
 			}
 		}
 		if(RequiredType == boolean.class) {
 			if(GivenType == Object.class) {
 				this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
 				this.InvokeMethodCall(boolean.class, JLib.UnboxBooleanValue);
 				return;
 			}
 		}
 		if(GivenType == long.class) {
 			if(RequiredType == Object.class) {
 				this.InvokeMethodCall(Long.class, JLib.BoxIntValue);
 				return;
 			}
 		}
 		if(GivenType == double.class) {
 			if(RequiredType == Object.class) {
 				this.InvokeMethodCall(Double.class, JLib.BoxFloatValue);
 				return;
 			}
 		}
 		if(GivenType == long.class) {
 			if(RequiredType == Object.class) {
 				this.InvokeMethodCall(Boolean.class, JLib.BoxBooleanValue);
 				return;
 			}
 		}
 		this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(RequiredType));
 	}
 
 	void CheckCast(Class<?> RequiredType, GtType GivenType) {
 		if(GivenType != null) {
 			this.CheckCast(RequiredType, GivenType.GetNativeType(false));
 		}
 //		else {
 //			System.err.println("cannot check cast given = " + GivenType + " RequiredType="+RequiredType);
 //		}
 	}
 
 	void CheckCast(GtType RequiredType, GtType GivenType) {
 		this.CheckCast(RequiredType.GetNativeType(false), GivenType);
 	}
 
 	void Call(Constructor<?> method) {
 		String owner = Type.getInternalName(method.getDeclaringClass());
 		this.AsmVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", Type.getConstructorDescriptor(method));
 	}
 
 	void InvokeMethodCall(Method method) {
 //		System.err.println("giventype = " + method);
 		InvokeMethodCall(void.class, method);
 	}
 
 	void InvokeMethodCall(GtType RequiredType, Method method) {
 		Class<?> RequiredNativeType = Object.class;
 		if(RequiredType != null) {
 			RequiredNativeType = RequiredType.GetNativeType(false);
 		}
 		InvokeMethodCall(RequiredNativeType, method);
 	}
 
 	void InvokeMethodCall(Class<?> RequiredType, Method method) {
 		int inst;
 		if(Modifier.isStatic(method.getModifiers())) {
 			inst = INVOKESTATIC;
 		}
 		else if(Modifier.isInterface(method.getModifiers())) {
 			inst = INVOKEINTERFACE;
 		}
 		else {
 			inst = INVOKEVIRTUAL;
 		}
 		String owner = Type.getInternalName(method.getDeclaringClass());
 		this.AsmVisitor.visitMethodInsn(inst, owner, method.getName(), Type.getMethodDescriptor(method));
 		//System.err.println("ReturnType="+RequiredType+", " + method);
 		this.CheckCast(RequiredType, method.getReturnType());
 	}
 
 	public void CheckReturn(GtType NodeType, GtType ReturnType) {
 		if(NodeType.IsVoidType() && !ReturnType.IsVoidType()) {
 			// Kimio: this must be necessary to remove unused return value 
 			this.AsmVisitor.visitInsn(POP);
 		}
 	}
 
 	public void PushEvaluatedNode(GtGenerator Generator, GtType RequestedType, GtNode ParamNode) {
 		//System.err.println("requested=" + RequestedType + ", given="+ParamNode.Type);
 		ParamNode.Evaluate(Generator);
 		this.CheckCast(RequestedType, ParamNode.Type);
 	}
 	
 }
 
 public class JavaByteCodeGenerator extends GtGenerator {
 	GreenTeaClassLoader ClassGenerator;
 	JMethodBuilder VisitingBuilder;
 
 	public JavaByteCodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super("java", OutputFile, GeneratorFlag);
 	}
 
 	@Override public void InitContext(GtParserContext Context) {
 		super.InitContext(Context);
 		this.ClassGenerator = new GreenTeaClassLoader(Context);
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> NameList, GtNode Body) {
 		String MethodName = Func.GetNativeFuncName();
 		String MethodDesc = JLib.GetMethodDescriptor(Func);
 		MethodNode AsmMethodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC, MethodName, MethodDesc, null, null);
 		JClassBuilder ClassHolder = this.ClassGenerator.GenerateMethodHolderClass(GtStaticTable.GetSourceFileName(Body.Token.FileLine), MethodName, AsmMethodNode);
 
 		JMethodBuilder LocalBuilder = new JMethodBuilder(this.ClassGenerator, AsmMethodNode);
 		for(int i = 0; i < NameList.size(); i++) {
 			String Name = NameList.get(i);
 			LocalBuilder.AddLocal(Func.GetFuncParamType(i), Name);
 		}
 		JMethodBuilder PushedBuilder = this.VisitingBuilder;
 		this.VisitingBuilder = LocalBuilder;
 		this.VisitBlock(Body);
 		this.VisitingBuilder = PushedBuilder;
 		if(Func.GetReturnType().IsVoidType()) {
 			// JVM always needs return;
 			LocalBuilder.AsmVisitor.visitInsn(RETURN);
 		}
 		try {
 			if(LibGreenTea.DebugMode) {
 				ClassHolder.OutputClassFile(ClassHolder.ClassName, ".");
 			}
 			Class<?> DefinedClass = this.ClassGenerator.loadClass(ClassHolder.ClassName);
 			Method[] DefinedMethods = DefinedClass.getMethods();
 			for(Method m : DefinedMethods) {
 				if(m.getName().equals(Func.GetNativeFuncName())) {
 					Func.SetNativeMethod(0, m);
 					break;
 				}
 			}
 		} catch(Exception e) {
 			LibGreenTea.VerboseException(e);
 		}
 	}
 
 	@Override public void OpenClassField(GtType ClassType, GtClassField ClassField) {
 		String ClassName = ClassType.GetNativeName();
 		String superClassName = ClassType.SuperType.GetNativeName();
 		//System.err.println("class name = " + ClassName + " extends " + superClassName);
 		JClassBuilder ClassBuilder = this.ClassGenerator.NewBuilder(null/*FIXME*/, ClassName, superClassName);
 		// generate field
 		for(GtFieldInfo field : ClassField.FieldList) {
 			if(field.FieldIndex >= ClassField.ThisClassIndex) {
 				String fieldName = field.NativeName;
 				Type fieldType = JLib.GetAsmType(field.Type);
 				FieldNode node = new FieldNode(ACC_PUBLIC, fieldName, fieldType.getDescriptor(), null, null);
 				ClassBuilder.AddField(node);
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
 				String desc = JLib.GetAsmType(field.Type).getDescriptor();
 				constructor.visitVarInsn(ALOAD, 0);
 				constructor.visitLdcInsn(field.InitValue);
 				constructor.visitFieldInsn(PUTFIELD, ClassName, name, desc);
 			}
 		}
 		constructor.visitInsn(RETURN);
 		ClassBuilder.AddMethod(constructor);
 		try {
 			ClassType.TypeBody = this.ClassGenerator.loadClass(ClassName);
 			LibGreenTea.Assert(ClassType.TypeBody != null);
 		}
 		catch (Exception e) {
 			LibGreenTea.VerboseException(e);
 		}
 	}
 
 	//-----------------------------------------------------
 
 	@Override public void VisitConstNode(GtConstNode Node) {
 		Object constValue = Node.ConstValue;
 		LibGreenTea.Assert(Node.ConstValue != null);  // Added by kimio
 		this.VisitingBuilder.LoadConst(constValue);
 	}
 
 	@Override public void VisitNewNode(GtNewNode Node) {
 		Type type = JLib.GetAsmType(Node.Type);
 		String owner = type.getInternalName();
 		this.VisitingBuilder.AsmVisitor.visitTypeInsn(NEW, owner);
 		this.VisitingBuilder.AsmVisitor.visitInsn(DUP);
 		if(!Node.Type.IsNativeType()) {
 			this.VisitingBuilder.LoadConst(Node.Type);
 			this.VisitingBuilder.AsmVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "(Lorg/GreenTeaScript/GtType;)V");
 		} else {
 			this.VisitingBuilder.AsmVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "()V");
 		}
 	}
 
 	@Override public void VisitNullNode(GtNullNode Node) {
 		this.VisitingBuilder.AsmVisitor.visitInsn(ACONST_NULL);
 	}
 
 	@Override public void VisitLocalNode(GtLocalNode Node) {
 		JLocalVarStack local = this.VisitingBuilder.FindLocalVariable(Node.NativeName);
 		this.VisitingBuilder.LoadLocal(local);
 	}
 
 	@Override public void VisitConstructorNode(GtConstructorNode Node) {
 		if(Node.Type.TypeBody instanceof Class<?>) {
 			// native class
 			Class<?> klass = (Class<?>) Node.Type.TypeBody;
 //			Type type = Type.getType(klass);
 			this.VisitingBuilder.AsmVisitor.visitTypeInsn(NEW, Type.getInternalName(klass));
 			this.VisitingBuilder.AsmVisitor.visitInsn(DUP);
 			for(int i = 0; i<Node.ParamList.size(); i++) {
 				GtNode ParamNode = Node.ParamList.get(i);
 				ParamNode.Evaluate(this);
 				this.VisitingBuilder.CheckCast(Node.Func.GetFuncParamType(i), ParamNode.Type);
 			}
 			this.VisitingBuilder.Call((Constructor<?>) Node.Func.FuncBody);
 		} else {
 			LibGreenTea.TODO("TypeBody is not Class<?>");
 		}
 	}
 
 	@Override public void VisitGetterNode(GtGetterNode Node) {
 		String name = Node.Func.FuncName;
 		Type fieldType = JLib.GetAsmType(Node.Func.GetReturnType());
 		Type ownerType = JLib.GetAsmType(Node.Func.GetFuncParamType(0));
 		Node.RecvNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitFieldInsn(GETFIELD, ownerType.getInternalName(), name, fieldType.getDescriptor());
 	}
 
 	@Override public void VisitDyGetterNode(GtDyGetterNode Node) {
 		Node.RecvNode.Evaluate(this);
 		this.VisitingBuilder.LoadConst(Node.FieldName);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.DynamicGetter);
 	}
 	
 	@Override public void VisitSetterNode(GtSetterNode Node) {
 		String name = Node.Func.FuncName;
 		Type fieldType = JLib.GetAsmType(Node.Func.GetFuncParamType(1));
 		Type ownerType = JLib.GetAsmType(Node.Func.GetFuncParamType(0));
 		Node.RecvNode.Evaluate(this);
 		Node.ValueNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitFieldInsn(PUTFIELD, ownerType.getInternalName(), name, fieldType.getDescriptor());
 	}
 
 	@Override public void VisitDySetterNode(GtDySetterNode Node) {
 		Node.RecvNode.Evaluate(this);
 		this.VisitingBuilder.LoadConst(Node.FieldName);
 		Node.ValueNode.Evaluate(this);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.DynamicSetter);						
 	}
 	
 	@Override public void VisitApplyNode(GtApplyNode Node) {
 		GtFunc Func = Node.Func;
 		for(int i = 1; i < Node.NodeList.size(); i++) {
 			GtNode ParamNode = Node.NodeList.get(i);
 			ParamNode.Evaluate(this);
 			this.VisitingBuilder.CheckCast(Func.GetFuncParamType(i - 1), ParamNode.Type);
 		}
 		Method m = null;
 		if(Func.FuncBody instanceof Method) {
 			m = (Method) Func.FuncBody;
 		}
 		if(m != null) {
 			this.VisitingBuilder.InvokeMethodCall(Node.Type, m);
 		}
 		else {
 			String MethodName = Func.GetNativeFuncName(); 
 			String Owner = JLib.GetHolderClassName(this.Context, MethodName);
 			String MethodDescriptor = JLib.GetMethodDescriptor(Func);
 			this.VisitingBuilder.AsmVisitor.visitMethodInsn(INVOKESTATIC, Owner, MethodName, MethodDescriptor);
 		}
 	}
 
 	@Override public void VisitStaticApplyNode(GtStaticApplyNode Node) {
 		GtFunc Func = Node.Func;
 		this.VisitingBuilder.SetLineNumber(Node);
 		for(int i = 0; i < Node.ParamList.size(); i++) {
 			GtNode ParamNode = Node.ParamList.get(i);
 			ParamNode.Evaluate(this);
 			this.VisitingBuilder.CheckCast(Func.GetFuncParamType(i), ParamNode.Type);
 		}
 		if(Func.FuncBody instanceof Method) {
 			this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method) Func.FuncBody);
 		}
 		else {
 			String MethodName = Func.GetNativeFuncName(); 
 			String Owner = JLib.GetHolderClassName(this.Context, MethodName);
 			String MethodDescriptor = JLib.GetMethodDescriptor(Func);
 			this.VisitingBuilder.AsmVisitor.visitMethodInsn(INVOKESTATIC, Owner, MethodName, MethodDescriptor);
 		}
 		this.VisitingBuilder.CheckReturn(Node.Type, Func.GetReturnType());
 	}
 
 	@Override public void VisitApplyFuncNode(GtApplyFuncNode Node) {
 		Node.FuncNode.Evaluate(this);
 		this.VisitingBuilder.LoadNewArray(this, 0, Node.ParamList);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.InvokeFunc);
 		this.VisitingBuilder.CheckReturn(Node.Type, Node.FuncNode.Type.TypeParams[0]);		
 	}
 
 	@Override public void VisitApplyOverridedMethodNode(GtApplyOverridedMethodNode Node) {
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn((long)Node.Token.FileLine);
 		this.VisitingBuilder.LoadConst(Node.NameSpace);
 		this.VisitingBuilder.LoadConst(Node.Func);
 		this.VisitingBuilder.LoadNewArray(this, 0, Node.ParamList);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.InvokeOverridedFunc);		
 		this.VisitingBuilder.CheckReturn(Node.Type, Node.Func.GetReturnType());
 	}
 	
 	@Override public void VisitApplyDynamicFuncNode(GtApplyDynamicFuncNode Node) {
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn((long)Node.Token.FileLine);
 		this.VisitingBuilder.LoadConst(Node.Type);
 		this.VisitingBuilder.LoadConst(Node.NameSpace);
 		this.VisitingBuilder.LoadConst(Node.FuncName);		
 		this.VisitingBuilder.LoadNewArray(this, 0, Node.ParamList);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.InvokeDynamicFunc);				
 		this.VisitingBuilder.CheckReturn(Node.Type, GtStaticTable.AnyType);
 	}
 
 	@Override public void VisitApplyDynamicMethodNode(GtApplyDynamicMethodNode Node) {
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn((long)Node.Token.FileLine);
 		this.VisitingBuilder.LoadConst(Node.Type);
 		this.VisitingBuilder.LoadConst(Node.NameSpace);
 		this.VisitingBuilder.LoadConst(Node.FuncName);		
 		this.VisitingBuilder.LoadNewArray(this, 0, Node.ParamList);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.InvokeDynamicMethod);				
 		this.VisitingBuilder.CheckReturn(Node.Type, GtStaticTable.AnyType);
 	}
 	
 	@Override public void VisitUnaryNode(GtUnaryNode Node) {
 		LibGreenTea.Assert(Node.Func.FuncBody instanceof Method);
 		Node.Expr.Evaluate(this);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method)Node.Func.FuncBody);
 	}
 
 	@Override public void VisitBinaryNode(GtBinaryNode Node) {
 		LibGreenTea.Assert(Node.Func.FuncBody instanceof Method);
 		Node.LeftNode.Evaluate(this);
 		Node.RightNode.Evaluate(this);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method)Node.Func.FuncBody);
 	}
 
 	@Override public void VisitIndexerNode(GtIndexerNode Node) {
 		ArrayList<GtNode> NodeList = Node.NodeList;
 		Node.Expr.Evaluate(this);
 		for(int i=0; i < NodeList.size(); i++) {
 			GtNode ParamNode = NodeList.get(i);
 //			ParamNode.Evaluate(this);
 //			this.VisitingBuilder.CheckCast(Node.Func.GetFuncParamType(i), ParamNode.Type);
 			this.VisitingBuilder.PushEvaluatedNode(this, Node.Func.GetFuncParamType(i+1), ParamNode);
 		}
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method) Node.Func.FuncBody);
 	}
 
 	@Override public void VisitArrayNode(GtArrayNode Node) {
 		this.VisitingBuilder.LoadConst(Node.Type);
 		this.VisitingBuilder.LoadNewArray(this, 0, Node.NodeList);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.NewArrayLiteral);
 	}
 
 	public void VisitNewArrayNode(GtNewArrayNode Node) {
 		this.VisitingBuilder.LoadConst(Node.Type);
 		this.VisitingBuilder.LoadNewArray(this, 0, Node.NodeList);
 		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.NewArray);
 	}
 
 	@Override public void VisitAndNode(GtAndNode Node) {
 		Label elseLabel = new Label();
 		Label mergeLabel = new Label();
 		Node.LeftNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, elseLabel);
 
 		Node.RightNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, elseLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn(true);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(elseLabel);
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn(false);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(mergeLabel);
 	}
 
 	@Override public void VisitOrNode(GtOrNode Node) {
 		Label thenLabel = new Label();
 		Label mergeLabel = new Label();
 		Node.LeftNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFNE, thenLabel);
 
 		Node.RightNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFNE, thenLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn(false);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(thenLabel);
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn(true);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(mergeLabel);
 	}
 
 	@Override public void VisitAssignNode(GtAssignNode Node) {
 		assert (Node.LeftNode instanceof GtLocalNode);
 		GtLocalNode Left = (GtLocalNode) Node.LeftNode;
 		JLocalVarStack local = this.VisitingBuilder.FindLocalVariable(Left.NativeName);
 		Node.RightNode.Evaluate(this);
 		this.VisitingBuilder.StoreLocal(local);
 	}
 
 	@Override public void VisitSelfAssignNode(GtSelfAssignNode Node) {
 		if(Node.LeftNode instanceof GtLocalNode) {
 			GtLocalNode Left = (GtLocalNode)Node.LeftNode;
 			JLocalVarStack local = this.VisitingBuilder.FindLocalVariable(Left.NativeName);
 			Node.LeftNode.Evaluate(this);
 			Node.RightNode.Evaluate(this);
 			this.VisitingBuilder.InvokeMethodCall((Method)Node.Func.FuncBody);
 			this.VisitingBuilder.StoreLocal(local);
 		}
 		else {
 			LibGreenTea.TODO("selfAssign");
 		}
 	}
 
 	@Override public void VisitVarNode(GtVarNode Node) {
 		JLocalVarStack local = this.VisitingBuilder.AddLocal(Node.Type, Node.NativeName);
 		Node.InitNode.Evaluate(this);
 		this.VisitingBuilder.StoreLocal(local);
 		this.VisitBlock(Node.BlockNode);
 	}
 
 	@Override public void VisitIfNode(GtIfNode Node) {
 		Label ElseLabel = new Label();
 		Label EndLabel = new Label();
 		Node.CondExpr.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, ElseLabel);
 		// Then
 		this.VisitBlock(Node.ThenNode);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
 		// Else
 		this.VisitingBuilder.AsmVisitor.visitLabel(ElseLabel);
 		if(Node.ElseNode != null) {
 			this.VisitBlock(Node.ElseNode);
 			this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
 		}
 		// End
 		this.VisitingBuilder.AsmVisitor.visitLabel(EndLabel);
 	}
 
 	@Override public void VisitTrinaryNode(GtTrinaryNode Node) {
 		Label ElseLabel = new Label();
 		Label EndLabel = new Label();
 		Node.ConditionNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, ElseLabel);
 		// Then
 		this.VisitBlock(Node.ThenNode);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
 		// Else
 		this.VisitingBuilder.AsmVisitor.visitLabel(ElseLabel);
 		this.VisitBlock(Node.ElseNode);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
 		// End
 		this.VisitingBuilder.AsmVisitor.visitLabel(EndLabel);
 	}
 
 	@Override public void VisitSwitchNode(GtSwitchNode Node) {
 		int cases = Node.CaseList.size() / 2;
 		int[] keys = new int[cases];
 		Label[] caseLabels = new Label[cases];
 		Label defaultLabel = new Label();
 		Label breakLabel = new Label();
 		for(int i=0; i<cases; i++) {
 			keys[i] = ((Number)((GtConstNode)Node.CaseList.get(i*2)).ConstValue).intValue();
 			caseLabels[i] = new Label();
 		}
 		Node.MatchNode.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitInsn(L2I);
 		this.VisitingBuilder.AsmVisitor.visitLookupSwitchInsn(defaultLabel, keys, caseLabels);
 		for(int i=0; i<cases; i++) {
 			this.VisitingBuilder.BreakLabelStack.push(breakLabel);
 			this.VisitingBuilder.AsmVisitor.visitLabel(caseLabels[i]);
 			this.VisitBlock(Node.CaseList.get(i*2+1));
 			this.VisitingBuilder.BreakLabelStack.pop();
 		}
 		this.VisitingBuilder.AsmVisitor.visitLabel(defaultLabel);
 		this.VisitBlock(Node.DefaultBlock);
 		this.VisitingBuilder.AsmVisitor.visitLabel(breakLabel);
 	}
 
 	@Override public void VisitWhileNode(GtWhileNode Node) {
 		Label continueLabel = new Label();
 		Label breakLabel = new Label();
 		this.VisitingBuilder.BreakLabelStack.push(breakLabel);
 		this.VisitingBuilder.ContinueLabelStack.push(continueLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(continueLabel);
 		Node.CondExpr.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
 		this.VisitBlock(Node.LoopBody);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, continueLabel);
 		this.VisitingBuilder.AsmVisitor.visitLabel(breakLabel);
 
 		this.VisitingBuilder.BreakLabelStack.pop();
 		this.VisitingBuilder.ContinueLabelStack.pop();
 	}
 
 	@Override public void VisitDoWhileNode(GtDoWhileNode Node) {
 		Label headLabel = new Label();
 		Label continueLabel = new Label();
 		Label breakLabel = new Label();
 		this.VisitingBuilder.BreakLabelStack.push(breakLabel);
 		this.VisitingBuilder.ContinueLabelStack.push(continueLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(headLabel);
 		this.VisitBlock(Node.LoopBody);
 		this.VisitingBuilder.AsmVisitor.visitLabel(continueLabel);
 		Node.CondExpr.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, headLabel);
 		this.VisitingBuilder.AsmVisitor.visitLabel(breakLabel);
 
 		this.VisitingBuilder.BreakLabelStack.pop();
 		this.VisitingBuilder.ContinueLabelStack.pop();
 	}
 
 	@Override public void VisitForNode(GtForNode Node) {
 		Label headLabel = new Label();
 		Label continueLabel = new Label();
 		Label breakLabel = new Label();
 		this.VisitingBuilder.BreakLabelStack.push(breakLabel);
 		this.VisitingBuilder.ContinueLabelStack.push(continueLabel);
 
 		this.VisitingBuilder.AsmVisitor.visitLabel(headLabel);
 		Node.CondExpr.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
 		this.VisitBlock(Node.LoopBody);
 		this.VisitingBuilder.AsmVisitor.visitLabel(continueLabel);
 		Node.IterExpr.Evaluate(this);
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, headLabel);
 		this.VisitingBuilder.AsmVisitor.visitLabel(breakLabel);
 
 		this.VisitingBuilder.BreakLabelStack.pop();
 		this.VisitingBuilder.ContinueLabelStack.pop();
 	}
 
 	@Override public void VisitForEachNode(GtForEachNode Node) {
 		LibGreenTea.TODO("ForEach");
 	}
 
 	@Override public void VisitReturnNode(GtReturnNode Node) {
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			Type type = JLib.GetAsmType(Node.Expr.Type);
 			this.VisitingBuilder.AsmVisitor.visitInsn(type.getOpcode(IRETURN));
 		}
 		else {
 			this.VisitingBuilder.AsmVisitor.visitInsn(RETURN);
 		}
 	}
 
 	@Override public void VisitBreakNode(GtBreakNode Node) {
 		Label l = this.VisitingBuilder.BreakLabelStack.peek();
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitContinueNode(GtContinueNode Node) {
 		Label l = this.VisitingBuilder.ContinueLabelStack.peek();
 		this.VisitingBuilder.AsmVisitor.visitJumpInsn(GOTO, l);
 	}
 
 	@Override public void VisitTryNode(GtTryNode Node) { //FIXME
 		int catchSize = Node.CatchBlock != null ? 1 : 0;
 		MethodVisitor mv = this.VisitingBuilder.AsmVisitor;
 		Label beginTryLabel = new Label();
 		Label endTryLabel = new Label();
 		Label finallyLabel = new Label();
 		Label catchLabel[] = new Label[catchSize];
 
 		// try block
 		mv.visitLabel(beginTryLabel);
 		this.VisitBlock(Node.TryBlock);
 		mv.visitLabel(endTryLabel);
 		mv.visitJumpInsn(GOTO, finallyLabel);
 
 		// prepare
 		for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
 			catchLabel[i] = new Label();
 			String throwType = JLib.GetAsmType(Node.CatchExpr.Type).getInternalName();
 			mv.visitTryCatchBlock(beginTryLabel, endTryLabel, catchLabel[i], throwType);
 		}
 
 		// catch block
 		for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
 			GtNode block = Node.CatchBlock;
 			mv.visitLabel(catchLabel[i]);
 			this.VisitBlock(block);
 			mv.visitJumpInsn(GOTO, finallyLabel);
 		}
 
 		// finally block
 		mv.visitLabel(finallyLabel);
 		this.VisitBlock(Node.FinallyBlock);
 	}
 
 	@Override public void VisitThrowNode(GtThrowNode Node) {
 		// use wrapper
 		//String name = Type.getInternalName(GtThrowableWrapper.class);
 		//this.VisitingBuilder.MethodVisitor.visitTypeInsn(NEW, name);
 		//this.VisitingBuilder.MethodVisitor.visitInsn(DUP);
 		//Node.Expr.Evaluate(this);
 		//this.box();
 //		//this.VisitingBuilder.typeStack.pop();
 		//this.VisitingBuilder.MethodVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
 		//this.VisitingBuilder.MethodVisitor.visitInsn(ATHROW);
 	}
 
 	@Override public void VisitInstanceOfNode(GtInstanceOfNode Node) {
 		if(Node.TypeInfo.IsGenericType() || Node.TypeInfo.IsVirtualType()) {
 			Node.ExprNode.Evaluate(this);
 			this.VisitingBuilder.LoadConst(Node.TypeInfo);
 			this.VisitingBuilder.InvokeMethodCall(boolean.class, JLib.GreenInstanceOfOperator);
 		}
 		else {
 			Node.ExprNode.Evaluate(this);
 			this.VisitingBuilder.CheckCast(Object.class, Node.ExprNode.Type);
 			Class<?> NativeType = Node.TypeInfo.GetNativeType(true);
 			this.VisitingBuilder.AsmVisitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(NativeType));
 		}
 	}
 
 	@Override public void VisitCastNode(GtCastNode Node) {
 		this.VisitingBuilder.LoadConst(Node.CastType);
 		Node.Expr.Evaluate(this);
 		this.VisitingBuilder.CheckCast(Object.class, Node.Expr.Type);
 		this.VisitingBuilder.InvokeMethodCall(Node.CastType, JLib.GreenCastOperator);
 	}
 
 	@Override public void VisitFunctionNode(GtFunctionNode Node) {
 		LibGreenTea.TODO("FunctionNode");
 	}
 
 	@Override public void VisitErrorNode(GtErrorNode Node) {
 		String name = Type.getInternalName(SoftwareFaultException.class);
 		this.VisitingBuilder.SetLineNumber(Node);
 		this.VisitingBuilder.AsmVisitor.visitTypeInsn(NEW, name);
 		this.VisitingBuilder.AsmVisitor.visitInsn(DUP);
 		this.VisitingBuilder.LoadConst(Node.Token.GetErrorMessage());
 		this.VisitingBuilder.AsmVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
 		this.VisitingBuilder.AsmVisitor.visitInsn(ATHROW);
 	}
 
 	@Override public void VisitCommandNode(GtCommandNode Node) {
 		ArrayList<ArrayList<GtNode>> Args = new ArrayList<ArrayList<GtNode>>();
 		GtCommandNode node = Node;
 		while(node != null) {
 			Args.add(node.ArgumentList);
 			node = (GtCommandNode) node.PipedNextNode;
 		}
 		// new String[][n]
 		this.VisitingBuilder.AsmVisitor.visitLdcInsn(Args.size());
 		this.VisitingBuilder.AsmVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String[].class));
 		for(int i=0; i<Args.size(); i++) {
 			// new String[m];
 			ArrayList<GtNode> Arg = Args.get(i);
 			this.VisitingBuilder.AsmVisitor.visitInsn(DUP);
 			this.VisitingBuilder.AsmVisitor.visitLdcInsn(i);
 			this.VisitingBuilder.AsmVisitor.visitLdcInsn(Arg.size());
 			this.VisitingBuilder.AsmVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String.class));
 			for(int j=0; j<Arg.size(); j++) {
 				this.VisitingBuilder.AsmVisitor.visitInsn(DUP);
 				this.VisitingBuilder.AsmVisitor.visitLdcInsn(j);
 				Arg.get(j).Evaluate(this);
 				this.VisitingBuilder.AsmVisitor.visitInsn(AASTORE);
 			}
 			this.VisitingBuilder.AsmVisitor.visitInsn(AASTORE);
 		}
 		if(Node.Type.IsBooleanType()) {
 			this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.ExecCommandBool);
 		}
 		else if(Node.Type.IsStringType()) {
 			this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.ExecCommandString);
 		}
 		else {
 			this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.ExecCommandVoid);
 		}
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		try {
 			Class<?> MainClass = Class.forName(JLib.GetHolderClassName(this.Context, MainFuncName), false, this.ClassGenerator);
 			Method m = MainClass.getMethod(MainFuncName);
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
