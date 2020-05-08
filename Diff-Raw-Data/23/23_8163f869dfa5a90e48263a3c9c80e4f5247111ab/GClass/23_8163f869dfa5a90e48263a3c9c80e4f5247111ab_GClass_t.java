 package org.effortless.gen;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.groovy.ast.AnnotatedNode;
 import org.codehaus.groovy.ast.AnnotationNode;
 import org.codehaus.groovy.ast.ClassHelper;
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.ast.ConstructorNode;
 import org.codehaus.groovy.ast.FieldNode;
 import org.codehaus.groovy.ast.GenericsType;
 import org.codehaus.groovy.ast.InnerClassNode;
 import org.codehaus.groovy.ast.MethodNode;
 import org.codehaus.groovy.ast.expr.ConstantExpression;
 import org.codehaus.groovy.ast.stmt.BlockStatement;
 //import org.codehaus.groovy.ast.expr.Expression;
 import org.codehaus.groovy.ast.stmt.EmptyStatement;
 import org.codehaus.groovy.control.SourceUnit;
 import org.effortless.ann.Module;
 import org.effortless.ann.NoTransform;
 import org.effortless.core.ClassNodeHelper;
 import org.effortless.core.Collections;
 import org.effortless.core.StringUtils;
 import org.effortless.model.Entity;
 import org.objectweb.asm.Opcodes;
 
 public class GClass extends AbstractNode<GClass> implements GNode {
 
 	protected GClass () {
 		super();
 	}
 	
 	protected void initiate () {
 		super.initiate();
 		this.clazz = null;
 	}
 	
 	public GClass (String name, SourceUnit sourceUnit) {
 		this();
 		this.clazz = new ClassNode(name, Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
 		sourceUnit.getAST().addClass(this.clazz);
 		this.sourceUnit = sourceUnit;
 	}
 	
 	public GClass (ClassNode clazz) {
 		this();
 		this.clazz = clazz;
 	}
 	
 	public GClass (ClassNode clazz, SourceUnit sourceUnit) {
 		this();
 		this.clazz = clazz;
 		this.sourceUnit = sourceUnit;
 	}
 	
 	protected GApplication application;
 	
 	protected void initiateApplication () {
 		this.application = null;
 	}
 	
 	public GApplication getApplication () {
 		return this.application;
 	}
 	
 	public void setApplication (GApplication newValue) {
 //		if (newValue != null) {
 //			newValue.addClass(this);
 //		}
 		this.application = newValue;
 	}
 	
 	protected SourceUnit sourceUnit;
 	
 	public SourceUnit getSourceUnit () {
 		return this.sourceUnit;
 	}
 	
 	protected ClassNode clazz;
 
 	public ClassNode getClassNode () {
 		return this.clazz;
 	}
 	
 	public GField addField (Class<?> type, String name) {
 		return addField(ClassNodeHelper.toClassNode(type), name);
 	}
 	
 	public GField addField (ClassNode type, String name) {
 		GField result = null;
 		FieldNode node = new FieldNode(name, Opcodes.ACC_PROTECTED, type, this.clazz, ConstantExpression.NULL);
 		this.clazz.addField(node);
 		result = new GField(this, node);
 //		FieldExpression fieldExpression = new FieldExpression(field);
 		return result;
 	}
 
 	public void removeField (String name) {
 		if (this.clazz != null) {
 			FieldNode field = this.clazz.getField(name);
 			if (field != null) {
 				this.clazz.removeField(name);
 			}
 		}
 	}
 
 	public ClassNode createGenericType (Class<?> clazz, Class<?>[] types) {
 		return createGenericType(ClassNodeHelper.toClassNode(clazz), ClassNodeHelper.toClassNodes(types));
 	}
 	
 	public ClassNode createGenericType (Class<?> clazz, Class<?> type) {
 		return createGenericType(ClassNodeHelper.toClassNode(clazz), ClassNodeHelper.toClassNodes(type));
 	}
 	
 	public ClassNode createGenericType (ClassNode clazz, Class<?>[] types) {
 		return createGenericType(clazz, ClassNodeHelper.toClassNodes(types));
 	}
 	
 	public ClassNode createGenericType (Class<?> clazz, ClassNode type) {
 		return createGenericType(ClassNodeHelper.toClassNode(clazz), ClassNodeHelper.toClassNodes(type));
 	}
 	
 	public ClassNode createGenericType (ClassNode clazz, ClassNode[] types) {
 		ClassNode result = null;
 //		GenericsUtils.
 		result = clazz.getPlainNodeReference();
 		result.setGenericsPlaceHolder(true);
 //		result = new ClassNode(clazz.getTypeClass());
 //		result.setRedirect(clazz);
 //		result = clazz;
 //		result.setUsingGenerics(true);
 		if (types != null) {
 			GenericsType[] genericsTypes = new GenericsType[types.length];
 			int idx = 0;
 			for (ClassNode type : types) {
 				GenericsType item = new GenericsType(type);
 				genericsTypes[idx] = item;
 				idx += 1;
 			}
 			result.setGenericsTypes(genericsTypes);
 		}
 		return result;
 	}
 	
 	public ClassNode createGenericType (ClassNode clazz, ClassNode type) {
 		return createGenericType(clazz, ClassNodeHelper.toClassNodes(type));
 	}
 	
 	
 	
 	public GClass setSuperClass (Class<?> superClass) {
 		return setSuperClass(ClassNodeHelper.toClassNode(superClass));
 	}
 	
 	public GClass setSuperClass (ClassNode superClass) {
 		if (superClass != null) {
 			this.clazz.setSuperClass(superClass);
 		}
 		return this;
 	}
 
 	public GClass addInterface (Class<?> iface) {
 		return addInterface(ClassNodeHelper.toClassNode(iface));
 	}
 	
 	public GClass addInterface (ClassNode iface) {
 		if (iface != null) {
 			ClassNode[] interfaces = this.clazz.getInterfaces();
 			List<ClassNode> list = new ArrayList<ClassNode>();
 			if (interfaces != null && interfaces.length > 0) {
 				for (ClassNode clazz : interfaces) {
 					list.add(clazz);
 				}
 			}
 			list.add(iface);
 			interfaces = list.toArray(new ClassNode[0]);
 			this.clazz.setInterfaces(interfaces);
 		}
 		return this;
 	}
 
 	public GClass addInterfaces (Class<?>... ifaces) {
 		return addInterfaces(ClassNodeHelper.toClassNodes(ifaces));
 	}
 	
 	public GClass addInterfaces (ClassNode... ifaces) {
 		if (ifaces != null) {
 			ClassNode[] interfaces = this.clazz.getInterfaces();
 			List<ClassNode> list = new ArrayList<ClassNode>();
 			if (interfaces != null && interfaces.length > 0) {
 				for (ClassNode clazz : interfaces) {
 					list.add(clazz);
 				}
 			}
 			for (ClassNode iface : ifaces) {
 				list.add(iface);
 			}
 			interfaces = list.toArray(new ClassNode[0]);
 			this.clazz.setInterfaces(interfaces);
 		}
 		return this;
 	}
 	
 	public GClass addInnerClass (String name) {
 		GClass result = null;
 		InnerClassNode inner = new InnerClassNode(this.clazz, name, Opcodes.ACC_PUBLIC, ClassNodeHelper.toClassNode(Object.class));
 		result = new GClass(inner, this.sourceUnit);
 		this.sourceUnit.getAST().addClass(inner);
 		return result;
 	}
 
 	public int getInnerClassesSize () {
 		int result = 0;
 		Iterator<InnerClassNode> inners = this.clazz.getInnerClasses();
 		if (inners != null) {
 			while (inners.hasNext()) {
 				inners.next();
 				result += 1;
 			}
 		}
 		return result;
 	}
 	
 //	public ClassGen addAnonymousClass (Class<?> superClass) {
 //		return addAnonymousClass(ClassNodeHelper.toClassNode(superClass));
 //	}
 //	
 //	public ClassGen addAnonymousClass (ClassNode superClass) {
 //		ClassGen result = null;
 //		String suffix = "" + (getInnerClassesSize() + 1);
 ////		String suffix = System.currentTimeMillis();
 ////		String name = this.clazz.getName() + "$" + suffix;
 ////		String name = suffix;
 //		String name = this.clazz.getNameWithoutPackage() + "$" + suffix;
 //		InnerClassNode inner = new InnerClassNode(this.clazz, name, Opcodes.ACC_PUBLIC, superClass);
 //		inner.setAnonymous(true);
 //		result = new ClassGen(inner, this.sourceUnit);
 //		this.sourceUnit.getAST().addClass(inner);
 //		return result;
 //	}
 //	
 //	public GMethod addSimpleGetter (String name) {
 //		GMethod result = null;
 //		if (true) {
 //			String getterName = BaseFields.getGetterName(name);
 //			FieldNode field = this.clazz.getField(name);
 //			GMethod mg = this.addMethod(getterName).setReturnType(field.getType()).setPublic(true);
 //			String fName = StringUtils.uncapFirst(name);
 //			mg.gPrintln(mg.call("getClass"));
 //			mg.gPrintln("printing get filter");
 //			mg.gPrintln(mg.field(fName));
 //			mg.add(mg.callStatic(DebugUtils.class, "inspect", mg.field(fName)));
 //			mg.addReturn(mg.field(fName));
 //			
 //			result = mg;
 //		}
 //		else {
 //			FieldNode field = this.clazz.getField(name);
 //			FieldExpression fieldExpression = new FieldExpression(field);
 //
 //			ReturnStatement getterCode = new ReturnStatement(fieldExpression);
 //			String getterName = BaseFields.getGetterName(field);
 //			MethodNode getter = new MethodNode(getterName, Opcodes.ACC_PUBLIC, field.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterCode);
 //			this.clazz.addMethod(getter);
 //		}
 //		
 //		return result;
 //	}
 //	
 //	public GClass addSimpleSetter (String name) {
 //		
 //		if (true) {
 //			FieldNode field = this.clazz.getField(name);
 //			String setterName = BaseFields.getSetterName(name);
 //			GMethod mg = this.addMethod(setterName).setPublic(true).addParameter(field.getType(), "newValue");
 //			mg.gPrintln(mg.var("newValue"));
 //			mg.add(mg.assign(mg.field(name), mg.var("newValue")));
 //		}
 //		else {
 //			FieldNode field = this.clazz.getField(name);
 //			FieldExpression fieldExpression = new FieldExpression(field);
 //	
 //			VariableExpression varNewValue = new VariableExpression("newValue", field.getType());
 //			ExpressionStatement setterCode = new ExpressionStatement(new BinaryExpression(fieldExpression, Token.newSymbol(Types.ASSIGN, -1, -1), varNewValue));
 //			String setterName = BaseFields.getSetterName(field);
 //			MethodNode setter = new MethodNode(setterName, Opcodes.ACC_PUBLIC, ClassHelper.VOID_TYPE, new Parameter[] {new Parameter(field.getType(), "newValue")}, ClassNode.EMPTY_ARRAY, setterCode);
 //			this.clazz.addMethod(setter);
 //		}
 //		
 //		return this;
 //	}
 
 	public GMethod addConstructor () {
 		GMethod result = null;
//		BlockStatement code = new BlockStatement();
//		code.addStatement(EmptyStatement.INSTANCE);
		ConstructorNode constructor = new ConstructorNode(Opcodes.ACC_PUBLIC, EmptyStatement.INSTANCE);
 		this.clazz.addConstructor(constructor);
//		result = new GMethod(constructor, this, code);
		result = new GMethod(constructor, this);
 		return result;
 	}
 	
 	public GMethod addMethod (String name) {
 		GMethod result = null;
 		result = new GMethod(name, this);
 		return result;
 	}
 
 //	public GMethod addSimpleProperty (Class<?> type, String name) {
 //		return addSimpleProperty(ClassNodeHelper.toClassNode(type), name);
 //	}
 //	
 //	public GMethod addSimpleProperty (ClassNode type, String name) {
 //		return addSimpleProperty(type, name, false);
 //	}
 //
 //	public GMethod addSimpleProperty (Class<?> type, String name, boolean readonly) {
 //		return addSimpleProperty(ClassNodeHelper.toClassNode(type), name, readonly);
 //	}
 	
 //	public GMethod addSimpleProperty (ClassNode type, String name, boolean readonly) {
 //		GMethod result = null;
 //		this.addField(type, name);
 //		result = this.addSimpleGetter(name);
 //		if (!readonly) {
 //			this.addSimpleSetter(name);
 //		}
 //		return result;
 //	}
 
 
 	public String queryAnnotation(Class<?> annotation, String member) {
 		return queryAnnotation(this.clazz, ClassNodeHelper.toClassNode(annotation), member, null);
 	}
 	
 	public String queryAnnotation(ClassNode annotation, String member) {
 		return queryAnnotation(this.clazz, annotation, member, null);
 	}
 	
 	public String queryAnnotation(Class<?> annotation, String member, String defaultValue) {
 		return queryAnnotation(this.clazz, ClassNodeHelper.toClassNode(annotation), member, defaultValue);
 	}
 
 	public String queryAnnotation(ClassNode annotation, String member, String defaultValue) {
 		return queryAnnotation(this.clazz, annotation, member, defaultValue);
 	}
 
 	public String queryAnnotation(ClassNode clazz, Class<?> annotation, String member) {
 		return queryAnnotation(clazz, ClassNodeHelper.toClassNode(annotation), member, null);
 	}
 
 	public String queryAnnotation(ClassNode clazz, ClassNode annotation, String member) {
 		return queryAnnotation(clazz, annotation, member, null);
 	}
 
 	public String queryAnnotation(ClassNode clazz, Class<?> annotation, String member, String defaultValue) {
 		return queryAnnotation(clazz, ClassNodeHelper.toClassNode(annotation), member, defaultValue);
 	}
 	
 	public String queryAnnotation(ClassNode clazz, ClassNode annotation, String member, String defaultValue) {
 		String result = defaultValue;
 		
 		List<AnnotationNode> annotations = clazz.getAnnotations(annotation);
 		if (annotations != null && annotations.size() > 0) {
 			AnnotationNode ann = annotations.get(0);
 			result = ann.getMember(member).getText();
 		}
 		
 		return result;
 	}
 
 //	public GClass addAnnotation (Class<?> annotation) {
 //		return addAnnotation(ClassNodeHelper.toClassNode(annotation));
 //	}
 //	
 //	
 //	public GClass addAnnotation (ClassNode annotation) {
 //		AnnotationNode ann = new AnnotationNode(annotation);
 //		this.clazz.addAnnotation(ann);
 //		return this;
 //	}
 //
 //	public GClass addAnnotation (AnnotationNode annotation) {
 //		this.clazz.addAnnotation(annotation);
 //		return this;
 //	}
 //
 //	public GClass addAnnotation (Class<?> annotation, String value) {
 //		return addAnnotation(ClassNodeHelper.toClassNode(annotation), "value", value);
 //	}
 //	
 //	public GClass addAnnotation (ClassNode annotation, String value) {
 //		return addAnnotation(annotation, "value", value);
 //	}
 //
 //	public GClass addAnnotation (Class<?> annotation, String property, String value) {
 //		return addAnnotation(ClassNodeHelper.toClassNode(annotation), property, value);
 //	}
 //
 //	public GClass addAnnotation (Class<?> annotation, String property, boolean value) {
 //		return addAnnotation(ClassNodeHelper.toClassNode(annotation), property, (value ? ConstantExpression.PRIM_TRUE : ConstantExpression.PRIM_FALSE));
 //	}
 //
 //	
 //	
 //	public GClass addAnnotation (ClassNode annotation, String property, String value) {
 //		return addAnnotation(annotation, property, new ConstantExpression(value));
 //	}
 //
 //	public GClass addAnnotation (ClassNode annotation, String property, Expression expr) {
 //		AnnotationNode ann = new AnnotationNode(annotation);
 //		ann.setMember(property, expr);
 //		this.clazz.addAnnotation(ann);
 //		return this;
 //	}
 //
 //	
 //	
 //	
 //	
 //	
 //	
 //	
 //	
 //	
 //	
 //	
 //	public AnnotationNode createAnnotation (Class<?> annotation) {
 //		return createAnnotation(ClassNodeHelper.toClassNode(annotation));
 //	}
 //	
 //	public AnnotationNode createAnnotation (ClassNode annotation) {
 //		AnnotationNode result = new AnnotationNode(annotation);
 //		return result;
 //	}
 //
 //	public AnnotationNode createAnnotation (Class<?> annotation, String value) {
 //		return createAnnotation(ClassNodeHelper.toClassNode(annotation), "value", value);
 //	}
 //	
 //	public AnnotationNode createAnnotation (Class<?> annotation, Expression value) {
 //		return createAnnotation(ClassNodeHelper.toClassNode(annotation), "value", value);
 //	}
 //	
 //	public AnnotationNode createAnnotation (ClassNode annotation, String value) {
 //		return createAnnotation(annotation, "value", value);
 //	}
 //
 //	public AnnotationNode createAnnotation (Class<?> annotation, String property, String value) {
 //		return createAnnotation(ClassNodeHelper.toClassNode(annotation), property, value);
 //	}
 //
 //	public AnnotationNode createAnnotation (ClassNode annotation, String property, String value) {
 //		return createAnnotation(annotation, property, new ConstantExpression(value));
 //	}
 //
 //	public AnnotationNode createAnnotation (Class<?> annotation, String property, Expression value) {
 //		return createAnnotation(ClassNodeHelper.toClassNode(annotation), property, value);
 //	}
 //	
 //	public AnnotationNode createAnnotation (ClassNode annotation, String property, Expression value) {
 //		AnnotationNode result = new AnnotationNode(annotation);
 //		result.setMember(property, value);
 //		return result;
 //	}
 //	
 //	public AnnotationNode createAnnotation (Class<?> annotation, String[] properties, Expression... values) {
 //		return createAnnotation(ClassNodeHelper.toClassNode(annotation), properties, values);
 //	}
 //	
 //	public AnnotationNode createAnnotation (ClassNode annotation, String[] properties, Expression... values) {
 //		AnnotationNode result = new AnnotationNode(annotation);
 //		int length = (properties != null ? properties.length : 0);
 //		for (int i = 0; i < length; i++) {
 //			result.setMember(properties[i], values[i]);
 //		}
 //		return result;
 //	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public GClass addCte(Class<?> type, String name, Object value) {
 		return addCte(ClassNodeHelper.toClassNode(type), name, value);
 	}
 	
 	
 	/*
 
 				protected static final String MODULE_SORTEOS = "sorteos";
 
 
 	 */
 	public GClass addCte(ClassNode type, String name, Object value) {
 		FieldNode field = new FieldNode(name, Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, type, this.clazz, new ConstantExpression(value));
 		this.clazz.addField(field);
 		return this;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static final boolean ONE_PACKAGE = true;
 
 	
 	public boolean checkEntityValid () {
 		boolean result = false;
 		ClassNode clazz = getClassNode();
 		if (clazz != null) {
 			ClassNode superClass = clazz.getSuperClass();
 			String superClassName = (superClass != null ? superClass.getName() : null);
 			result = !"groovy.lang.Script".equals(superClassName);
 			if (result) {
 				List<AnnotationNode> annotations = clazz.getAnnotations(NO_ENTITY_CLAZZ);
 				result = (!(annotations != null && annotations.size() > 0));
 			}
 //			result = result && !"java.lang.Enum".equals(superClassName);
 		}
 		return result;
 	}
 
 	public static final ClassNode NO_ENTITY_CLAZZ = ClassNodeHelper.toClassNode(NoTransform.class);
 
 	public boolean checkEnum () {
 		boolean result = false;
 		ClassNode clazz = getClassNode();
 		if (clazz != null) {
 			ClassNode superClass = clazz.getSuperClass();
 			String superClassName = (superClass != null ? superClass.getName() : null);
 			result = "java.lang.Enum".equals(superClassName);
 		}
 		return result;
 	}
 
 	public String getNameWithoutPackage() {
 		return (this.clazz != null ? this.clazz.getNameWithoutPackage() : null);
 	}
 
 	public String getPackageName() {
 		return (this.clazz != null ? this.clazz.getPackageName() : null);
 	}
 
 	public String getName() {
 		return (this.clazz != null ? this.clazz.getName() : null);
 	}
 
 	public List<GField> getAllFields() {
 		List<GField> result = null;
 		result = new ArrayList<GField>();
 		ClassNode node = this.clazz;
 		do {
 			List<GField> fields = getFields(node);
 			Collections.addAll(result, fields);
 			node = (node != null ? node.getSuperClass() : null);
 		} while (node != null);
 		return result;
 	}
 
 	protected List<GField> getFields (ClassNode clazz) {
 		List<GField> result = null;
 		List<FieldNode> nodes = (clazz != null ? clazz.getFields() : null);
 		if (nodes != null) {
 			result = new ArrayList<GField>();
 			for (FieldNode node : nodes) {
 				GField gField = new GField(this, node);
 				if (gField != null && !gField.isStatic() && !gField.isFinal()) {
 					result.add(gField);
 				}
 			}
 		}
 		return result;
 	}
 	
 	public List<GField> getFields() {
 		List<GField> result = null;
 		result = getFields(this.clazz);
 		return result;
 	}
 
 	public GField getField(String name) {
 		GField result = null;
 		FieldNode node = (this.clazz != null && name != null ? this.clazz.getField(name) : null);
 		result = (node != null ? new GField(this, node) : null);
 		return result;
 	}
 
 	public boolean hasAnnotation (Class<?> clazz) {
 		boolean result = false;
 		if (clazz != null) {
 			ClassNode cNode = ClassNodeHelper.toClassNode(clazz);
 			result = hasAnnotation(cNode);
 		}
 		return result;
 	}
 
 	public boolean hasAnnotation (ClassNode clazz) {
 		boolean result = false;
 		if (clazz != null) {
 			List<AnnotationNode> annotations = (this.clazz != null ? this.clazz.getAnnotations(clazz) : null);
 			result = (annotations != null && annotations.size() > 0);
 		}
 		return result;
 	}
 
 	public GField getProperty (String name) {
 		GField result = null;
 		name = (name != null ? name.trim() : "");
 		if (name.length() > 0) {
 			FieldNode field = this.clazz.getField(name);
 			result = new GField(this, field);
 		}
 		return result;
 	}
 	
 	public List<GField> getProperties(String[] names) {
 		List<GField> result = null;
 		
 		if (names != null && names.length > 0) {
 			result = new ArrayList<GField>();
 			for (String name : names) {
 				GField property = getProperty(name);
 				if (property != null) {
 					result.add(property);
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	public GMethod getMethod (String name) {
 		GMethod result = null;
 		List<MethodNode> methods = (this.clazz != null ? this.clazz.getMethods(name) : null);
 		if (methods != null && methods.size() == 1) {
 			MethodNode method = methods.get(0);
 			result = toGMethod(method);
 		}
 		return result;
 	}
 
 	public List<GMethod> getListMethods (String name) {
 		List<GMethod> result = null;
 		result = new ArrayList<GMethod>();
 		List<MethodNode> methods = _listMethods(name);
 		if (methods != null && methods.size() > 0) {
 			for (MethodNode method : methods) {
 				GMethod gMethod = toGMethod(method);
 				if (gMethod != null) {
 					result.add(gMethod);
 				}
 			}
 		}
 		return result;
 	}
 	
 	protected List<MethodNode> _listMethods (String name) {
 		List<MethodNode> result = null;
 		result = new ArrayList<MethodNode>();
 		ClassNode node = this.clazz;
 		if (name != null && node != null) {
 			do {
 				List<MethodNode> methods = (node != null ? node.getMethods(name) : null);
 				Collections.addAll(result, methods);
 				node = (node != null ? node.getSuperClass() : null);
 			} while (node != null);
 //			List<MethodNode> methods = (this.clazz != null ? this.clazz.getMethods(name) : null);
 			List<MethodNode> methods = (this.clazz != null ? this.clazz.getAllDeclaredMethods() : null);
 			if (methods != null && methods.size() > 0) {
 				for (MethodNode method : methods) {
 					if (method != null && name.equals(method.getName())) {
 						result.add(method);
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	public List<GMethod> getAllDeclaredMethods() {
 		List<GMethod> result = null;
 		List<MethodNode> methods = (this.clazz != null ? this.clazz.getAllDeclaredMethods() : null);
 		result = toMethods(methods);
 		return result;
 	}
 
 	public List<GMethod> getMethods() {
 		List<GMethod> result = null;
 		List<MethodNode> methods = (this.clazz != null ? this.clazz.getMethods() : null);
 		result = toMethods(methods);
 		return result;
 	}
 	
 	protected GMethod toGMethod (MethodNode node) {
 		GMethod result = null;
 		result = new GMethod(node, this);
 		return result;
 	}
 	
 	protected List<GMethod> toMethods (List<MethodNode> methods) {
 		List<GMethod> result = null;
 		if (methods != null) {
 			result = new ArrayList<GMethod>();
 			for (MethodNode node : methods) {
 				GMethod newItem = toGMethod(node);
 				result.add(newItem);
 			}
 		}
 		return result;
 	}
 
 	public String toString () {
 		return getName();
 	}
 
 	public boolean equals (Object o) {
 		boolean result = false;
 		GClass obj = null;
 		try { obj = (GClass)o; } catch (ClassCastException e) {}
 		if (obj != null && this.clazz != null) {
 			result = this.clazz.equals(obj.clazz);
 		}
 		return result;
 	}
 	
 	public List<GField> listRefFields() {
 		return listFields(Entity.class);
 	}
 	
 	public List<GField> listFields (Class<?> type) {
 		List<GField> result = null;
 		result = new ArrayList<GField>();
 		if (type != null) {
 			List<GField> fields = getAllFields();
 			for (GField field : fields) {
 				if (field != null && field.isType(type)) {
 					result.add(field);
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	public List<GField> getProperties() {
 		List<GField> result = null;
 		List<GField> fields = getAllFields();
 		result = new ArrayList<GField>();
 		if (fields != null && fields.size() > 0) {
 			for (GField field : fields) {
 				if (field.isProperty()) {
 					result.add(field);
 				}
 			}
 		}
 		return result;
 	}
 
 	public List<GMethod> getPublicMethods() {
 		List<GMethod> result = null;
 		result = new ArrayList<GMethod>();
 		List<GMethod> methods = getMethods();
 		for (GMethod method : methods) {
 			if (method != null && method.isPublic()) {
 				result.add(method);
 			}
 		}
 		return result;
 	}
 
 	public String getModuleName () {
 		String result = null;
 		GAnnotation ann = getAnnotation(Module.class);
 		result = StringUtils.forceNotNull((ann != null ? ann.getValue() : null));
 		if (result.length() <= 0) {
 			SourceUnit sourceUnit = this.getSourceUnit();
 			String name = (sourceUnit != null ? sourceUnit.getName() : result);
 			int idx = (name != null ? name.lastIndexOf(File.separator) : -1);
 			name = (idx >= 0 ? name.substring(idx + 1) : name);
 			idx = (name != null ? name.indexOf(".gy") : -1);
 			name = (name != null ? name.substring(0, idx) : name);
 			result = name;
 		}
 		result = (result.length() > 0 ? result : null);
 		return result;
 	}
 
 	protected ClassNode _getType () {
 		return this.clazz;
 	}
 
 	@Override
 	protected AnnotatedNode _getAnnotatedNode() {
 		return this.clazz;
 	}
 
 	
 //	public GClass setPublic (boolean newValue) {
 //		if (newValue) {
 //			this.clazz.setModifiers(Opcodes.ACC_PUBLIC);
 //		}
 //		return this;
 //	}
 //	
 //	public GClass setProtected (boolean newValue) {
 //		if (newValue) {
 //			this.clazz.setModifiers(Opcodes.ACC_PROTECTED);
 //		}
 //		return this;
 //	}
 //	
 //	public GClass setPrivate (boolean newValue) {
 //		if (newValue) {
 //			this.clazz.setModifiers(Opcodes.ACC_PRIVATE);
 //		}
 //		return this;
 //	}
 	
 	
 	
 	public GClass setPublic (boolean newValue) {
 		if (newValue) {
 			setProtected(false);
 			setPrivate(false);
 		}
 		return _addRemoteModifier(Opcodes.ACC_PUBLIC, newValue);
 	}
 	
 	public GClass setProtected (boolean newValue) {
 		if (newValue) {
 			setPublic(false);
 			setPrivate(false);
 		}
 		return _addRemoteModifier(Opcodes.ACC_PROTECTED, newValue);
 	}
 
 	public GClass setPrivate (boolean newValue) {
 		if (newValue) {
 			setProtected(false);
 			setPublic(false);
 		}
 		return _addRemoteModifier(Opcodes.ACC_PRIVATE, newValue);
 	}
 	
 	public GClass setStatic(boolean newValue) {
 		return _addRemoteModifier(Opcodes.ACC_STATIC, newValue);
 	}
 	
 	public GClass setFinal(boolean newValue) {
 		return _addRemoteModifier(Opcodes.ACC_FINAL, newValue);
 	}
 	
 	protected GClass _addRemoteModifier(int value, boolean add) {
 		int modifiers = this.clazz.getModifiers();
 		int newModifiers = (add ? modifiers | value : modifiers & (~value));
 		this.clazz.setModifiers(newModifiers);
 		return this;
 	}
 	
 	
 	
 }
