 package com.shivanshusingh.pluginanalyser.analysis;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.Handle;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 import org.objectweb.asm.signature.SignatureReader;
 import org.objectweb.asm.signature.SignatureVisitor;
 
 public class DependencyVisitor extends ClassVisitor {
 	Set<String> packages = new HashSet<String>();
 
 	// mine
 	/*
 	 * this is a list of prefixes of types that are considered Non external in
 	 * nature, e.g. java.* or javax.* These need to be detected to see if the
 	 * urrent type or method owner is to be excluded from the exteral-non-java
 	 * or external - excluded types or methods sets.
 	 */
 	Set<String> excludedExternalTypePrefixes = new HashSet<String>(Arrays.asList(new String[] { "java.", "javax.", "java/",
 			"javax/" }));
 	// mine
 	Set<String> allMyClasses = new HashSet<String>();
 	// mine
 	Set<String> allMyDeprecatedClasses = new HashSet<String>();
 	// mine
 	Set<String> allMyPublicClasses = new HashSet<String>();
 	// mine
 	Set<String> allMyProtectedClasses = new HashSet<String>();
 	// mine
 	Set<String> allInvokations = new HashSet<String>();
 	// mine
 	Set<String> allNonJavaInvokations = new HashSet<String>();
 	// mine
 	Set<String> allDetectedTypes = new HashSet<String>();
 	// mine
 	Set<String> allNonJavaDetectedTypes = new HashSet<String>();
 	// mine
 	Set<String> allMyMethods = new HashSet<String>();
 	// mine
 	Set<String> allMyDeprecatedMethods = new HashSet<String>();
 	// mine
 	Set<String> allMyPublicMethods = new HashSet<String>();
 	// mine
 	Set<String> allMyProtectedMethods = new HashSet<String>();
 
 	// mine
 	Map<String, TypeDependency> allMyTypeDependencies = new HashMap<String, TypeDependency>();
 
 	// mine
 	public Map<String, TypeDependency> getAllMyTypeDependencies() {
 		return allMyTypeDependencies;
 	}
 
 	// mine
 	public Set<String> getAllMyDeprecatedMethods() {
 		return allMyDeprecatedMethods;
 	}
 
 	// mine
 	public Set<String> getAllMyDeprecatedClasses() {
 		return allMyDeprecatedClasses;
 	}
 
 	// mine
 	public Set<String> getAllMyDeprecatedPublicMethods() {
 		Set<String> tempSet = new HashSet<String>();
 		for (Object s : allMyDeprecatedMethods)
 			if (allMyPublicMethods.contains(s))
 				tempSet.add((String) s);
 		return tempSet;
 	}
 
 	// mine
 	public Set<String> getAllMyDeprecatedPublicClasses() {
 		Set<String> tempSet = new HashSet<String>();
 		for (Object s : allMyDeprecatedClasses)
 			if (allMyPublicClasses.contains(s))
 				tempSet.add((String) s);
 		return tempSet;
 
 	}
 
 	// mine
 	public Set<String> getAllMyPublicMethods() {
 		return allMyPublicMethods;
 	}
 
 	// mine
 	public Set<String> getAllMyMethods() {
 		return allMyMethods;
 	}
 
 	// mine
 	public Set<String> getAllInvokations() {
 		return allInvokations;
 	}
 
 	// mine
 	public Set<String> getAllNonJavaInvokations() {
 		return allNonJavaInvokations;
 	}
 
 	// mine
 	public Set<String> getAllDetectedTypes() {
 		return allDetectedTypes;
 	}
 
 	// mine
 	public Set<String> getAllMyClasses() {
 		return allMyClasses;
 	}
 
 	// mine
 	public Set<String> getAllMyPublicClasses() {
 		return allMyPublicClasses;
 	}
 
 	// mine
 	public Set<String> getAllExternalMethodInvokations() {
 		Set<String> difference = new HashSet<String>(allInvokations);
 		difference.removeAll(allMyMethods);
 		return difference;
 	}
 
 	// mine
 	public Set<String> getAllExternalNonJavaMethodInvokations() {
 		Set<String> difference = new HashSet<String>(allNonJavaInvokations);
 		difference.removeAll(allMyMethods);
 		return difference;
 	}
 
 	// mine
 	public Set<String> getAllExternalDetectedTypes() {
 		Set<String> difference = new HashSet<String>(allDetectedTypes);
 		difference.removeAll(allMyClasses);
 		return difference;
 	}
 
 	// mine
 	public Set<String> getAllExternalNonJavaDetectedTypes() {
 		Set<String> difference = new HashSet<String>(allNonJavaDetectedTypes);
 		difference.removeAll(allMyClasses);
 		return difference;
 	}
 
 	// mine current class being visited
 	String currentClass;
 
 	Map<String, Map<String, Integer>> groups = new HashMap<String, Map<String, Integer>>();
 
 	Map<String, Integer> current;
 
 	public Map<String, Map<String, Integer>> getGlobals() {
 		return groups;
 	}
 
 	public Set<String> getPackages() {
 		return packages;
 	}
 
 	public DependencyVisitor() {
 		super(Opcodes.ASM4);
 		// mine
 	}
 
 	// mine
 	private void addMyMethod(int access, String name, String desc) {
 
 		String fnSignature = Type.getReturnType(desc).getClassName() + " "
 				+ Type.getObjectType(currentClass).getClassName() + "." + name + " (";
 		Type[] types = Type.getArgumentTypes(desc);
 		for (int i = 0; i < types.length; i++) {
 			fnSignature += types[i].getClassName() + ",";
 		}
 		fnSignature += ")";
 		allMyMethods.add(fnSignature);
 
 		// if method access is public then this is a personal public function
 		if ((access & Opcodes.ACC_PUBLIC) != 0)
 			allMyPublicMethods.add(fnSignature);
 
 		// if method access is protected then this is a personal protected
 		// function
 		if ((access & Opcodes.ACC_PROTECTED) != 0) {// // for now adding them to
 													// public.... to see
 			allMyPublicMethods.add(fnSignature);
 			allMyProtectedMethods.add(fnSignature);
 		}
 
 		// if method access is anything else than private then, also include it
 		// for now as a public function
 		boolean isPrivate=((access & Opcodes.ACC_PRIVATE) != 0)?true:false;
 		if (!isPrivate) {// // for now adding them to
 													// public.... to see
 			allMyPublicMethods.add(fnSignature);
 		}
 
 		// if method access is deprecated then this is a personal deprecated
 		// function
 		if ((access & Opcodes.ACC_DEPRECATED) != 0)
 			allMyDeprecatedMethods.add(fnSignature);
 
 	}
 
 	// mine
 	private void addInvokation(String owner, String name, String desc) {
 		String ownerclassName = Type.getObjectType(owner).getClassName();
 
 		String fnSignature = Type.getReturnType(desc).getClassName() + " " + ownerclassName + "." + name + " (";
 		Type[] types = Type.getArgumentTypes(desc);
 		for (int i = 0; i < types.length; i++) {
 			fnSignature += types[i].getClassName() + ",";
 		}
 		fnSignature += ")";
 		allInvokations.add(fnSignature);
 
 		// building the list of all external and excluded invokations
 
 		boolean startsWithExcludedType = false;
 		for (String s : excludedExternalTypePrefixes)
 			if (ownerclassName.startsWith(s)) {
 				startsWithExcludedType = true;
 				break;
 			}
 		if (!startsWithExcludedType)
 			allNonJavaInvokations.add(fnSignature);
 
 	}
 
 	// ClassVisitor
 
 	@Override
 	public void visit(final int version, final int access, final String name, final String signature,
 			final String superName, final String[] interfaces) {
 
 		// mine - adding to MyClasses
 		String myFullyQualifiedClassName = Type.getObjectType(name).getClassName();
		
 		allMyClasses.add(myFullyQualifiedClassName);
 		// mine- adding to public classes and no other.
 		if ((access & Opcodes.ACC_PUBLIC) != 0)
 			allMyPublicClasses.add(myFullyQualifiedClassName);
		// mine- adding to protected or unspecified classes and no other.
 		if ((access & Opcodes.ACC_PROTECTED) != 0) {
 			// for now adding to public classes to see ....
			allMyClasses.add(myFullyQualifiedClassName);
 			allMyProtectedClasses.add(myFullyQualifiedClassName);
 		}
 		// if method access is anything else than private then, also include it
 		// for now as a public class
 		boolean isPrivate=((access & Opcodes.ACC_PRIVATE) != 0)?true:false;
 		if (!isPrivate)
		{// // for now adding them to
													// public.... to see
			allMyClasses.add(myFullyQualifiedClassName);
 		}
 		// if class access is deprecated then this is a personal deprecated
 		// class
 		if ((access & Opcodes.ACC_DEPRECATED) != 0)
 			allMyDeprecatedClasses.add(myFullyQualifiedClassName);
 
 		// adding all dependent types (super classes and interfaces)
 		if (null != superName && !"".equalsIgnoreCase(superName)) {
 			// add superclass
 			TypeDependency typeDep = new TypeDependency();
 			if (allMyTypeDependencies.containsKey(myFullyQualifiedClassName))
 				typeDep = allMyTypeDependencies.get(myFullyQualifiedClassName);
 			String superClassFullyQualName=Type.getObjectType(superName).getClassName();
 			typeDep.superClass = (superClassFullyQualName);
 			allMyTypeDependencies.put(myFullyQualifiedClassName, typeDep);
 		}
 
 		if (null != interfaces && 1 <= interfaces.length) {
 			// adding interfaces.
 			TypeDependency typeDep = new TypeDependency();
 			if (allMyTypeDependencies.containsKey(myFullyQualifiedClassName))
 				typeDep = allMyTypeDependencies.get(myFullyQualifiedClassName);
 			for (String s : interfaces) {
 				if (null != s && !"".equalsIgnoreCase(s))
 				{
 					String interfaceFullyQualName = Type.getObjectType(s).getClassName();
 					typeDep.interfaces.add(interfaceFullyQualName);
 				}
 			}
 			allMyTypeDependencies.put(myFullyQualifiedClassName, typeDep);
 
 		}
 
 		String p = getGroupKey(name);
 		current = groups.get(p);
 		currentClass = name;
 		if (current == null) {
 			current = new HashMap<String, Integer>();
 			groups.put(p, current);
 		}
 
 		if (signature == null) {
 			if (superName != null) {
 				addInternalName(superName);
 			}
 			addInternalNames(interfaces);
 		} else {
 			addSignature(signature);
 		}
 	}
 
 	@Override
 	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
 		addDesc(desc);
 		return new AnnotationDependencyVisitor();
 	}
 
 	@Override
 	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
 			final Object value) {
 		if (signature == null) {
 			addDesc(desc);
 		} else {
 			addTypeSignature(signature);
 		}
 		if (value instanceof Type) {
 			addType((Type) value);
 		}
 		return new FieldDependencyVisitor();
 	}
 
 	@Override
 	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
 			final String[] exceptions) {
 		// Log.outln("=======\nThis Class MethodName="+name);
 
 		// mine - getting all
 		/* Building the current method signature */
 		addMyMethod(access, name, desc);
 
 		if (signature == null) {
 			addMethodDesc(desc);
 		} else {
 			addSignature(signature);
 		}
 		addInternalNames(exceptions);
 		// Log.outln("++++");
 		return new MethodDependencyVisitor();
 	}
 
 	class AnnotationDependencyVisitor extends AnnotationVisitor {
 
 		public AnnotationDependencyVisitor() {
 			super(Opcodes.ASM4);
 		}
 
 		@Override
 		public void visit(final String name, final Object value) {
 			if (value instanceof Type) {
 				addType((Type) value);
 			}
 		}
 
 		@Override
 		public void visitEnum(final String name, final String desc, final String value) {
 			addDesc(desc);
 		}
 
 		@Override
 		public AnnotationVisitor visitAnnotation(final String name, final String desc) {
 			addDesc(desc);
 			return this;
 		}
 
 		@Override
 		public AnnotationVisitor visitArray(final String name) {
 			return this;
 		}
 	}
 
 	class FieldDependencyVisitor extends FieldVisitor {
 
 		public FieldDependencyVisitor() {
 			super(Opcodes.ASM4);
 		}
 
 		@Override
 		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
 			addDesc(desc);
 			return new AnnotationDependencyVisitor();
 		}
 	}
 
 	class MethodDependencyVisitor extends MethodVisitor {
 
 		public MethodDependencyVisitor() {
 			super(Opcodes.ASM4);
 		}
 
 		@Override
 		public AnnotationVisitor visitAnnotationDefault() {
 			return new AnnotationDependencyVisitor();
 		}
 
 		@Override
 		public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
 			addDesc(desc);
 			return new AnnotationDependencyVisitor();
 		}
 
 		@Override
 		public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
 			addDesc(desc);
 			return new AnnotationDependencyVisitor();
 		}
 
 		@Override
 		public void visitTypeInsn(final int opcode, final String type) {
 			addType(Type.getObjectType(type));
 		}
 
 		@Override
 		public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
 			addInternalName(owner);
 			addDesc(desc);
 		}
 
 		@Override
 		public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
 
 			// mine //////// This is where we capture External Method Calls
 			// YAYYYYYYYYYYYYYYYYYYYYYYYY ///////////
 			addInvokation(owner, name, desc);
 
 			// //////////////////////////////////////////////
 
 			addInternalName(owner);
 			addMethodDesc(desc);
 
 			// ///////////////////////////////////
 		}
 
 		@Override
 		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
 
 			// ////// This is where we capture External Dynamic Method Calls
 			// YAYYYYYYYYYYYYYYYYYYYYYYYY ///////////
 			// WE dont need to worry about this
 			try {
 				throw new InvokeDynamicException();
 			} catch (InvokeDynamicException e) {
 				e.printStackTrace();
 			}
 			// /////////////////
 			addMethodDesc(desc);
 			addConstant(bsm);
 			for (int i = 0; i < bsmArgs.length; i++) {
 				addConstant(bsmArgs[i]);
 			}
 		}
 
 		@Override
 		public void visitLdcInsn(final Object cst) {
 			addConstant(cst);
 		}
 
 		@Override
 		public void visitMultiANewArrayInsn(final String desc, final int dims) {
 			addDesc(desc);
 		}
 
 		@Override
 		public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
 				final Label end, final int index) {
 			addTypeSignature(signature);
 		}
 
 		@Override
 		public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
 			if (type != null) {
 				addInternalName(type);
 			}
 		}
 	}
 
 	class SignatureDependencyVisitor extends SignatureVisitor {
 
 		String signatureClassName;
 
 		public SignatureDependencyVisitor() {
 			super(Opcodes.ASM4);
 		}
 
 		@Override
 		public void visitClassType(final String name) {
 			signatureClassName = name;
 			addInternalName(name);
 		}
 
 		@Override
 		public void visitInnerClassType(final String name) {
 			signatureClassName = signatureClassName + "$" + name;
 			addInternalName(signatureClassName);
 		}
 	}
 
 	// ---------------------------------------------
 
 	private String getGroupKey(String name) {
 		int n = name.lastIndexOf('/');
 		if (n > -1) {
 			name = name.substring(0, n);
 		}
 		packages.add(name);
 		return name;
 	}
 
 	private void addName(final String name) {
 		if (name == null) {
 			return;
 		}
 
 		// mine - capturing all Types that are dependencies
 		String typeName = Type.getObjectType(name).getClassName();
 		allDetectedTypes.add(typeName);
 		boolean startsWithExcludedType = false;
 		for (String s : excludedExternalTypePrefixes)
 			if (typeName.startsWith(s)) {
 				startsWithExcludedType = true;
 				break;
 			}
 		if (!startsWithExcludedType)
 			allNonJavaDetectedTypes.add(typeName);
 		// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		String p = getGroupKey(name);
 		if (current.containsKey(p)) {
 			current.put(p, current.get(p) + 1);
 		} else {
 			current.put(p, 1);
 		}
 	}
 
 	void addInternalName(final String name) {
 		addType(Type.getObjectType(name));
 	}
 
 	private void addInternalNames(final String[] names) {
 		for (int i = 0; names != null && i < names.length; i++) {
 			addInternalName(names[i]);
 		}
 	}
 
 	void addDesc(final String desc) {
 		addType(Type.getType(desc));
 	}
 
 	void addMethodDesc(final String desc) {
 		/*
 		 * Printig method return tyoe and argument types
 		 */
 		addType(Type.getReturnType(desc));
 
 		// Log.out("\n__retType__="+Type.getReturnType(desc).getClassName()+"{__argType__=");
 		Type[] types = Type.getArgumentTypes(desc);
 		for (int i = 0; i < types.length; i++) {
 			addType(types[i]);
 			// Log.out(types[i].getClassName() +",");
 		}
 		// Log.out("}\n");
 	}
 
 	void addType(final Type t) {
 		switch (t.getSort()) {
 		case Type.ARRAY:
 			addType(t.getElementType());
 			break;
 		case Type.OBJECT:
 			addName(t.getInternalName());
 			break;
 		case Type.METHOD:
 			addMethodDesc(t.getDescriptor());
 			break;
 		}
 	}
 
 	private void addSignature(final String signature) {
 
 		if (signature != null) {
 			new SignatureReader(signature).accept(new SignatureDependencyVisitor());
 		}
 	}
 
 	void addTypeSignature(final String signature) {
 		// Log.outln("__sig="+signature);
 		if (signature != null) {
 			new SignatureReader(signature).acceptType(new SignatureDependencyVisitor());
 		}
 	}
 
 	void addConstant(final Object cst) {
 		if (cst instanceof Type) {
 			addType((Type) cst);
 		} else if (cst instanceof Handle) {
 			Handle h = (Handle) cst;
 			addInternalName(h.getOwner());
 			addMethodDesc(h.getDesc());
 		}
 	}
 }
