 package archimate.codegen;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.EnhancedForStatement;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ForStatement;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.IPackageBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.IVariableBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.Javadoc;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.ParameterizedType;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.TagElement;
 import org.eclipse.jdt.core.dom.TextElement;
 import org.eclipse.jdt.core.dom.ThisExpression;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 
 import archimate.patterns.Pattern;
 import archimate.uml.UMLAdapter;
 import archimate.util.JavaClass;
 import archimate.util.JavaMethod;
 import archimate.util.TagNode;
 
 /**
  * This utility class provides methods for accessing and editing source code
  * parsed by the {@link ASTParser}
  * 
  * @author Samuel Esposito
  * 
  */
 public class JavaHelper {
 
 	/**
 	 * TagName for the archiMateTag
 	 */
 	public static final String ARCHIMATETAG = "@archiMateTag";
 	// The current pattern
 	private String pattern;
 	// The status
 	private MultiStatus status;
 
 	/**
 	 * Creates a new {@link JavaHelper}
 	 * 
 	 * @param status
 	 *            the {@link MultiStatus}
 	 * @param currentPattern
 	 *            the pattern currently processed
 	 */
 	public JavaHelper(MultiStatus status, String currentPattern) {
 		this.status = status;
 		pattern = currentPattern;
 	}
 
 	/**
 	 * Searches for an archiMateTag in the javadoc of the given node.
 	 * 
 	 * @param node
 	 *            {@link ASTNode} to be searched
 	 * @return The found archiMateTag
 	 */
 	public String getArchiMateTag(BodyDeclaration node) {
 		if (node.getJavadoc() != null) {
 			List<TagElement> tags = node.getJavadoc().tags();
 			for (Iterator<TagElement> iter = tags.iterator(); iter.hasNext();) {
 				TagElement tag = iter.next();
 				if (tag.getTagName() != null
 						&& tag.getTagName().equals(ARCHIMATETAG)) {
 					List<TextElement> fragments = tag.fragments();
 					String archiMateTag = "";
 					for (Iterator<TextElement> ite2 = fragments.iterator(); ite2
 							.hasNext();) {
 						TextElement name = ite2.next();
 						if (name.getText().length() > 0)
 							archiMateTag += name.getText().substring(1);
 					}
 					return archiMateTag;
 				}
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Returns the compilation unit the {@link ASTNode} is defined in
 	 * 
 	 * @param node
 	 *            the {@link ASTNode}
 	 * @return The compilation unit the {@link ASTNode} is defined in
 	 */
 	public CompilationUnit compilationUnit(ASTNode node) {
 		if (node instanceof CompilationUnit)
 			return (CompilationUnit) node;
 		while (node.getParent() != null) {
 			node = node.getParent();
 			if (node instanceof CompilationUnit) {
 				return (CompilationUnit) node;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@link TypeDeclaration} the {@link ASTNode} is defined in
 	 * 
 	 * @param node
 	 *            the {@link ASTNode}
 	 * @return The {@link TypeDeclaration} the {@link ASTNode} is defined in
 	 */
 	public TypeDeclaration typeDeclaration(ASTNode node) {
 		if (node instanceof TypeDeclaration)
 			return (TypeDeclaration) node;
 		while (node.getParent() != null) {
 			node = node.getParent();
 			if (node instanceof TypeDeclaration) {
 				return (TypeDeclaration) node;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the package of the class the {@link ASTNode} is defined in
 	 * 
 	 * @param node
 	 *            the {@link ASTNode}
 	 * @return The package of the class the {@link ASTNode} is defined in
 	 */
 	public String getPackage(ASTNode node) {
 		CompilationUnit unit = compilationUnit(node);
 		if (unit != null)
 			return unit.getPackage().getName().getFullyQualifiedName();
 		return "";
 	}
 
 	/**
 	 * Returns the imports of the class the {@link ASTNode} is defined in
 	 * 
 	 * @param node
 	 *            the {@link ASTNode}
 	 * @return The imports of the class the {@link ASTNode} is defined in
 	 */
 	public ArrayList<String> getImports(ASTNode node) {
 		ArrayList<String> imports = new ArrayList<String>();
 		CompilationUnit unit = compilationUnit(node);
 		if (unit != null) {
 			for (Object importDec : unit.imports()) {
 				if (importDec instanceof ImportDeclaration)
 					imports.add(((ImportDeclaration) importDec).getName()
 							.getFullyQualifiedName());
 			}
 		}
 		return imports;
 	}
 
 	/**
 	 * Returns the interfaces the class the {@link ASTNode} is defined in
 	 * implements
 	 * 
 	 * @param node
 	 *            the {@link ASTNode}
 	 * @return The interfaces of the class the {@link ASTNode} is defined in
 	 *         implements
 	 */
 	public ArrayList<String> getInterfaces(ASTNode node) {
 		ArrayList<String> interfaces = new ArrayList<String>();
 		TypeDeclaration declaration = typeDeclaration(node);
 		if (declaration != null) {
 			for (Object interfaceType : declaration.superInterfaceTypes()) {
 				if (interfaceType instanceof SimpleType)
 					interfaces.add(((SimpleType) interfaceType).getName()
 							.getFullyQualifiedName());
 			}
 		}
 		return interfaces;
 	}
 
 	/**
 	 * Returns a JavaClass object for every interface the class the
 	 * {@link ASTNode} is defined in implements
 	 * 
 	 * @param node
 	 *            the {@link ASTNode}
 	 * @return The JavaClass objects
 	 */
 	public ArrayList<JavaClass> getInterfaceClasses(ASTNode node) {
 		ArrayList<JavaClass> interfaces = new ArrayList<JavaClass>();
 		TypeDeclaration declaration = typeDeclaration(node);
 		if (declaration != null) {
 			for (Object interfaceType : declaration.superInterfaceTypes()) {
 				if (interfaceType instanceof SimpleType) {
 					ITypeBinding type = ((SimpleType) interfaceType)
 							.resolveBinding();
					if (type != null)
						interfaces.add(new JavaClass(type.getPackage()
								.getName(), type.getName(), "", ""));
 				}
 			}
 		}
 		return interfaces;
 	}
 
 	/**
 	 * Returns the name of the {@link TypeDeclaration} the node is in
 	 * 
 	 * @param node
 	 *            an ASTNode
 	 * @return The name of the {@link TypeDeclaration} the node is in
 	 */
 	public String getClassName(ASTNode node) {
 		if (node instanceof TypeDeclaration)
 			return ((TypeDeclaration) node).getName().getFullyQualifiedName();
 		while (node.getParent() != null) {
 			node = node.getParent();
 			if (node instanceof TypeDeclaration) {
 				return ((TypeDeclaration) node).getName()
 						.getFullyQualifiedName();
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Returns the name of the {@link TypeDeclaration}
 	 * 
 	 * @param node
 	 *            a {@link TypeDeclaration}
 	 * @return The name of the {@link TypeDeclaration}
 	 */
 	public String getName(TypeDeclaration node) {
 		return node.getName().getIdentifier();
 	}
 
 	/**
 	 * Returns a list of all method names in the class
 	 * 
 	 * @param node
 	 *            the class
 	 * @return A list of all method names in the class
 	 */
 	public ArrayList<String> methodNames(TypeDeclaration node) {
 		ArrayList<String> names = new ArrayList<String>();
 		for (Object element : node.bodyDeclarations()) {
 			if (element instanceof MethodDeclaration) {
 				MethodDeclaration declaration = (MethodDeclaration) element;
 				names.add(declaration.getName().getFullyQualifiedName());
 			}
 		}
 		return names;
 	}
 
 	/**
 	 * Returns the name of the {@link MethodDeclaration}
 	 * 
 	 * @param node
 	 *            a {@link MethodDeclaration}
 	 * @return The name of the {@link MethodDeclaration}
 	 */
 	public String getName(MethodDeclaration node) {
 		return node.getName().getIdentifier();
 	}
 
 	/**
 	 * Adds the given imports to the given {@link CompilationUnit} if it doesn't
 	 * already contain them.
 	 * 
 	 * @param unit
 	 *            The {@link CompilationUnit} to add the imports to.
 	 * @param imports
 	 *            A list of import names to be added
 	 */
 	public void addImports(CompilationUnit unit, ArrayList<String> imports) {
 		for (String importName : imports) {
 			addImport(unit, importName);
 		}
 	}
 
 	// Adds one import to the compilation unit if it doesn't already contain it
 	private void addImport(CompilationUnit unit, String importName) {
 		AST ast = unit.getAST();
 		if (!samePackage(unit, importName) && !hasImport(unit, importName)) {
 			ImportDeclaration importDeclaration = ast.newImportDeclaration();
 			importDeclaration.setName(ast.newName(getSimpleNames(importName)));
 			if (importName.indexOf("*") > 0)
 				importDeclaration.setOnDemand(true);
 			else
 				importDeclaration.setOnDemand(false);
 			unit.imports().add(importDeclaration);
 		}
 	}
 
 	// Checks whether the unit and the import are in the same package
 	private boolean samePackage(CompilationUnit unit, String importName) {
 		String packageName = getPackage(unit);
 		String[] importParts = importName.split("\\.");
 		String importPackage = "";
 		for (int index = 0; index < importParts.length - 1; ++index) {
 			importPackage += importParts[index];
 			if (index < importParts.length - 2)
 				importPackage += ".";
 		}
 		return packageName.equals(importPackage);
 	}
 
 	// Checks whether the unit has already got an import
 	private boolean hasImport(CompilationUnit unit, String importName) {
 		for (Iterator<ImportDeclaration> iter = unit.imports().iterator(); iter
 				.hasNext();) {
 			ImportDeclaration importDeclaration = iter.next();
 			String name = importDeclaration.getName().getFullyQualifiedName();
 			if (importDeclaration.isOnDemand()) {
 				name += ".*";
 			}
 			if (name.equals(importName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds a class declaration to the {@link CompilationUnit}.
 	 * 
 	 * @param unit
 	 *            The {@link CompilationUnit} to add the class declaration to
 	 * @param javaClass
 	 *            The {@link JavaClass} defining the class to create
 	 */
 	public void addClass(CompilationUnit unit, JavaClass javaClass) {
 		// add package declaration
 		AST ast = unit.getAST();
 		PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
 		unit.setPackage(packageDeclaration);
 		packageDeclaration.setName(ast.newName(javaClass.packageName()));
 		// add imports
 		addImports(unit, javaClass.imports());
 		// add class declaration
 		TypeDeclaration classType = ast.newTypeDeclaration();
 		classType.setInterface(javaClass.isInterface());
 		setModifier(classType, Modifier.PUBLIC);
 		if (javaClass.isAbstract())
 			setModifier(classType, Modifier.ABSTRACT);
 		classType.setName(ast.newSimpleName(javaClass.className()));
 		// add superclass
 		if (javaClass.hasSuperClass()) {
 			classType.setSuperclassType(ast.newSimpleType(ast
 					.newSimpleName(javaClass.superClass().intendedName())));
 		}
 		// add implemented interfaces
 		if (!javaClass.isInterface()) {
 			for (Iterator<JavaClass> iter = javaClass.interfaces().iterator(); iter
 					.hasNext();) {
 				classType.superInterfaceTypes().add(
 						ast.newSimpleType(ast.newSimpleName(iter.next()
 								.intendedName())));
 			}
 		}
 		unit.types().add(classType);
 		// add javadoc
 		addJavaDoc(classType, javaClass);
 	}
 
 	/**
 	 * Adds methods defined by the {@link TagNode}s {@link ICodeElement}s to the
 	 * {@link TypeDeclaration} node
 	 * 
 	 * @param node
 	 *            {@link TypeDeclaration} node to add the methods to
 	 * @param tagnode
 	 *            {@link TagNode} with a list of {@link ICodeElement}s
 	 */
 	public void addMethods(TypeDeclaration node, ICodeElement code,
 			TagNode tagnode) {
 		for (Iterator<ICodeElement> iter = tagnode.source().iterator(); iter
 				.hasNext();) {
 			ICodeElement element = iter.next();
 			if (!element.visited()) {
 				if (element instanceof JavaMethod
 						&& code.children().contains(element)) {
 					JavaMethod method = (JavaMethod) element;
 					addMethod(node, method);
 					createStatus(method, tagnode);
 				}
 			}
 		}
 	}
 
 	// Adds a status with info to the multistatus object
 	private void createStatus(JavaMethod method, TagNode tagnode) {
 		String container = "";
 		TagNode parent = tagnode.parent();
 		if (parent.source().size() == 1) {
 			ICodeElement parentElement = parent.source().get(0);
 			if (parentElement instanceof JavaClass) {
 				JavaClass javaClass = (JavaClass) parentElement;
 				container = " in the \"" + javaClass.className() + "\" "
 						+ (javaClass.isInterface() ? "interface" : "class");
 			}
 		}
 		status.add(new Status(IStatus.INFO, status.getPlugin(), 1, pattern
 				+ ": Method " + method.type() + " added for the \""
 				+ method.name() + "()\" method" + container
 				+ ".                          ", null));
 	}
 
 	/**
 	 * Adds a method to a {@link TypeDeclaration} based on the settings of the
 	 * {@link JavaMethod}. If the {@link JavaMethod}s type is a
 	 * {@link JavaMethod#DECLARATION}, a method declaration is added. If the
 	 * {@link JavaMethod}s type is a {@link JavaMethod#IMPLEMENTATION}, a method
 	 * with a method block is added. If the {@link JavaMethod}s type is a
 	 * {@link JavaMethod#INVOCATION}, a method with a method invocation in its
 	 * block is added.
 	 * 
 	 * @param node
 	 *            {@link TypeDeclaration} node to add the methods to
 	 * @param method
 	 *            {@link JavaMethod} object containing all the settings for the
 	 *            new method
 	 */
 	public void addMethod(TypeDeclaration node, JavaMethod method) {
 		AST ast = node.getAST();
 		// Add the method declaration
 		MethodDeclaration md;
 		md = ast.newMethodDeclaration();
 		md.setConstructor(false);
 		setModifier(md, Modifier.PUBLIC);
 		if (method.type().equals(JavaMethod.INVOCATION)
 				|| method.type().equals(JavaMethod.CALLBACK_INV)) {
 			// Avoid collision in names of methods invoking other methods
 			String name = method.invocationMethod();
 			int count = 2;
 			while (methodNames(node).contains(name)) {
 				name = method.invocationMethod() + count;
 				++count;
 			}
 			md.setName(ast.newSimpleName(name));
 		} else {
 			md.setName(ast.newSimpleName(method.name()));
 		}
 		node.bodyDeclarations().add(md);
 		// Add method block
 		addMethodBlock(md, method);
 		// Add the JavaDoc
 		addJavaDoc(md, method);
 	}
 
 	// Adds the method block to the method
 	private void addMethodBlock(MethodDeclaration md, JavaMethod method) {
 		AST ast = md.getAST();
 		if (!method.type().equals(JavaMethod.DECLARATION)) {
 			Block methodBlock = ast.newBlock();
 			md.setBody(methodBlock);
 			// Add method invocation
 			if (method.type().equals(JavaMethod.INVOCATION)) {
 				addInvocation(methodBlock, method);
 			}
 			// Add callback invocation
 			if (method.type().equals(JavaMethod.CALLBACK_INV)) {
 				addCallback(methodBlock, method);
 			}
 			// Add callback implementation
 			if (method.type().equals(JavaMethod.CALLBACK_IMPL)) {
 				if (method.argumentsDefined()) {
 					// Add the method arguments
 					ArrayList<String> argNames = addMethodArguments(md, method);
 					addObjectListImpl(methodBlock, method, argNames);
 				}
 			}
 		} else if (method.argumentsDefined()) {
 			addMethodArguments(md, method);
 		}
 	}
 
 	// Adds the list of arguments to the method
 	private ArrayList<String> addMethodArguments(MethodDeclaration md,
 			JavaMethod method) {
 		AST ast = md.getAST();
 		ArrayList<String> names = new ArrayList<String>();
 		for (JavaClass argument : method.arguments()) {
 			String name = camelize(argument.className());
 			SingleVariableDeclaration variableDeclaration = ast
 					.newSingleVariableDeclaration();
 			variableDeclaration.setType(ast.newSimpleType(ast
 					.newSimpleName(argument.className())));
 			variableDeclaration.setName(ast.newSimpleName(name));
 			md.parameters().add(variableDeclaration);
 			names.add(name);
 		}
 		return names;
 	}
 
 	// Adds a method invocation on an object
 	private void addInvocation(Block methodBlock, JavaMethod method) {
 		String objectClass = method.className();
 		String objectName = camelize(method.objectType().className());
 		addObject(methodBlock, objectClass, objectName, new ArrayList<String>());
 		addMethodInvocation(methodBlock, objectName, method);
 	}
 
 	// Adds an object list to the containing typedeclaration
 	private void addObjectListImpl(Block methodBlock, JavaMethod method,
 			ArrayList<String> argNames) {
 		JavaClass argument = method.arguments().get(0);
 		CompilationUnit unit = compilationUnit(methodBlock);
 		addImport(unit, "java.util.ArrayList");
 		String listName = addObjectList(methodBlock, argument.className(),
 				camelize(argument.className()));
 		addObjectToList(methodBlock, listName, argNames);
 	}
 
 	// Adds a callback on an object
 	private void addCallback(Block methodBlock, JavaMethod method) {
 		if (method.objectType().interfacesDefined()) {
 			JavaClass interfaceClass = method.objectType().interfaces().get(0);
 			String className = interfaceClass.className();
 			String objectName = camelize(className);
 			String objectListName = objectName + "List";
 			addForLoop(methodBlock, className, objectName, objectListName,
 					method);
 		}
 	}
 
 	/**
 	 * Adds JavaDoc to a {@link BodyDeclaration} based on the settings of the
 	 * {@link ICodeElement}
 	 * 
 	 * @param body
 	 *            the {@link BodyDeclaration} to add the JavaDoc to
 	 * @param element
 	 *            the {@link ICodeElement} to derive the documentation from
 	 */
 	public void addJavaDoc(BodyDeclaration body, ICodeElement element) {
 		AST ast = body.getAST();
 		Javadoc jc = ast.newJavadoc();
 		TagElement tag;
 		TextElement te;
 		if (element.commentDefined()) {
 			tag = ast.newTagElement();
 			te = ast.newTextElement();
 			tag.fragments().add(te);
 			te.setText(element.comment());
 			jc.tags().add(tag);
 			tag = ast.newTagElement();
 			te = ast.newTextElement();
 			tag.fragments().add(te);
 			te.setText("");
 			jc.tags().add(tag);
 		}
 		if (element.archiMateTagsDefined()) {
 			tag = ast.newTagElement();
 			te = ast.newTextElement();
 			tag.setTagName(ARCHIMATETAG);
 			te = ast.newTextElement();
 			tag.fragments().add(te);
 			te.setText(element.archiMateTag());
 			jc.tags().add(tag);
 		}
 		body.setJavadoc(jc);
 	}
 
 	// Adds a statement that initializes an object
 	private void addObject(Block methodBlock, String type, String name,
 			ArrayList<String> arglist) {
 		AST ast = methodBlock.getAST();
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(name));
 		VariableDeclarationStatement vds = ast
 				.newVariableDeclarationStatement(vdf);
 		methodBlock.statements().add(vds);
 		vds.setType(ast.newSimpleType(ast.newSimpleName(type)));
 
 		ClassInstanceCreation cc = ast.newClassInstanceCreation();
 		cc.setType(ast.newSimpleType(ast.newSimpleName(type)));
 		for (Iterator<String> iter = arglist.iterator(); iter.hasNext();) {
 			cc.arguments().add(ast.newSimpleName(iter.next()));
 		}
 		vdf.setInitializer(cc);
 	}
 
 	// Adds a statement that initializes an arraylist of objects to a {@link
 	// TypeDeclaration}.
 	private String addObjectList(Block methodBlock, String type, String name) {
 		String listName = name + "List";
 		AST ast = methodBlock.getAST();
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(listName));
 		FieldDeclaration fd = ast.newFieldDeclaration(vdf);
 		setModifier(fd, Modifier.PRIVATE);
 		setModifier(fd, Modifier.STATIC);
 		ParameterizedType paramType = ast.newParameterizedType(ast
 				.newSimpleType(ast.newSimpleName("ArrayList")));
 		paramType.typeArguments().add(
 				ast.newSimpleType(ast.newSimpleName(type)));
 		fd.setType(paramType);
 		TypeDeclaration declaration = typeDeclaration(methodBlock);
 		declaration.bodyDeclarations().add(0, fd);
 
 		ClassInstanceCreation cc = ast.newClassInstanceCreation();
 		paramType = ast.newParameterizedType(ast.newSimpleType(ast
 				.newSimpleName("ArrayList")));
 		paramType.typeArguments().add(
 				ast.newSimpleType(ast.newSimpleName(type)));
 		cc.setType(paramType);
 		vdf.setInitializer(cc);
 		return listName;
 	}
 
 	// Adds a statement that adds an object to an objectlist
 	private void addObjectToList(Block methodBlock, String listName,
 			ArrayList<String> argNames) {
 		AST ast = methodBlock.getAST();
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(listName));
 		mi.setName(ast.newSimpleName("add"));
 		for (String argName : argNames)
 			mi.arguments().add(ast.newSimpleName(argName));
 		methodBlock.statements().add(ast.newExpressionStatement(mi));
 	}
 
 	// Adds a for loop to the methodblock
 	private void addForLoop(Block methodBlock, String objectClass,
 			String objectName, String objectListName, JavaMethod method) {
 		AST ast = methodBlock.getAST();
 		EnhancedForStatement forLoop = ast.newEnhancedForStatement();
 		SingleVariableDeclaration declaration = ast
 				.newSingleVariableDeclaration();
 		declaration.setName(ast.newSimpleName(objectName));
 		declaration.setType(ast.newSimpleType(ast.newSimpleName(objectClass)));
 		forLoop.setParameter(declaration);
 		forLoop.setExpression(ast.newSimpleName(objectListName));
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(objectName));
 		mi.setName(ast.newSimpleName(method.name()));
 		forLoop.setBody(ast.newExpressionStatement(mi));
 		methodBlock.statements().add(forLoop);
 	}
 
 	// Adds a method invocation to the method block
 	private void addMethodInvocation(Block methodBlock, String objectName,
 			JavaMethod method) {
 		AST ast = methodBlock.getAST();
 		CompilationUnit unit = compilationUnit(methodBlock);
 		addImport(unit, method.packageName() + "." + method.className());
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(objectName));
 		mi.setName(ast.newSimpleName(method.name()));
 		methodBlock.statements().add(ast.newExpressionStatement(mi));
 		if (method.argumentsDefined() && method.parent() instanceof JavaClass) {
 			JavaClass argument = method.arguments().get(0);
 			JavaClass javaClass = (JavaClass) method.parent();
 			if (javaClass.interfacesDefined()) {
 				JavaClass interfaceClass = javaClass.interfaces().get(0);
 				if (argument.packageName().equals(interfaceClass.packageName())
 						&& argument.className().equals(
 								interfaceClass.className())) {
 					mi.arguments().add(ast.newThisExpression());
 				}
 			}
 		}
 	}
 
 	// helper method for setting a BodyDeclaration modifier
 	private void setModifier(BodyDeclaration classType, int modifier) {
 		AST ast = classType.getAST();
 		switch (modifier) {
 		case Modifier.PUBLIC:
 			classType.modifiers().add(
 					ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
 			break;
 		case Modifier.PRIVATE:
 			classType.modifiers().add(
 					ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
 			break;
 		case Modifier.STATIC:
 			classType.modifiers().add(
 					ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
 			break;
 		case Modifier.ABSTRACT:
 			classType.modifiers().add(
 					ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD));
 			break;
 		}
 	}
 
 	// helper method for adding dot-separated names
 	private String[] getSimpleNames(String qualifiedName) {
 		StringTokenizer st = new StringTokenizer(qualifiedName, ".");
 		ArrayList list = new ArrayList();
 		while (st.hasMoreTokens()) {
 			String name = st.nextToken().trim();
 			if (!name.equals("*"))
 				list.add(name);
 		}
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 
 	/**
 	 * Camelizes a name
 	 * 
 	 * @param name
 	 *            The name to camelize
 	 * @return The camelized name
 	 */
 	public static String camelize(String name) {
 		if (name.length() > 1) {
 			return name.toLowerCase().substring(0, 1) + name.substring(1);
 		}
 		return name.toLowerCase();
 	}
 
 	/**
 	 * Compares the {@link TagNode}s source elements with the given
 	 * {@link TypeDeclaration} and sets the {@link MultiStatus} according to the
 	 * result
 	 * 
 	 * @param node
 	 *            the {@link TypeDeclaration} to compare with
 	 * @param tagnode
 	 *            the given {@link TagNode}
 	 */
 	public ICodeElement compare(TypeDeclaration node, TagNode tagnode) {
 		ICodeElement element = tagnode.getSource(getName(node),
 				getPackage(node));
 		// A matching element is found and will be checked for differences
 		if (element != null) {
 			tagnode.setVisited(element);
 			element.diff(node, status, pattern);
 			return element;
 		}
 		return null;
 	}
 
 	/**
 	 * Compares the {@link TagNode}s source elements with the given
 	 * {@link MethodDeclaration} and sets the {@link MultiStatus} according to
 	 * the result
 	 * 
 	 * @param node
 	 *            the {@link MethodDeclaration} to compare with
 	 * @param tagnode
 	 *            the given {@link TagNode}
 	 */
 	public void compare(MethodDeclaration node, ICodeElement code,
 			TagNode tagnode) {
 		JavaMethod method = createMethodStub(node);
 		ICodeElement element = tagnode.getSource(method);
 		// A matching element is found and will be checked for differences
 		if (element != null && code.children().contains(element)) {
 			tagnode.setVisited(element);
 			element.diff(node, status, pattern);
 		}
 	}
 
 	/**
 	 * Creates a stub {@link JavaMethod} object for a {@link MethodDeclaration}
 	 * object
 	 * 
 	 * @param node
 	 *            the {@link MethodDeclaration} object to create a stub for
 	 * @return a stub {@link JavaMethod} object
 	 */
 	private JavaMethod createMethodStub(MethodDeclaration node) {
 		JavaMethod method = null;
 		String tag = getArchiMateTag(node);
 		String type = Pattern.methodType(getArchiMateTag(node));
 		if (type.equals(JavaMethod.INVOCATION)) {
 			ArrayList<JavaMethod> invocations = methodInvocations(node);
 			if (invocations.size() > 0) {
 				return invocations.get(0);
 			}
 		}
 		JavaClass javaClass = new JavaClass(getPackage(node),
 				getClassName(node), "", "");
 		javaClass.addInterfaces(getInterfaceClasses(node));
 		method = new JavaMethod(getName(node), tag, type, javaClass);
 		addArgumentStubs(node, method);
 		return method;
 	}
 
 	// Creates a JavaMethod for every method invocation in the method
 	private ArrayList<JavaMethod> methodInvocations(MethodDeclaration node) {
 		ArrayList<JavaMethod> invocations = new ArrayList<JavaMethod>();
 		Block methodBlock = node.getBody();
 		if (methodBlock != null) {
 			for (Object element : methodBlock.statements()) {
 				Object newElement = element;
 				if (element instanceof EnhancedForStatement) {
 					newElement = ((EnhancedForStatement) element).getBody();
 				}
 				if (newElement instanceof ExpressionStatement
 						&& ((ExpressionStatement) newElement).getExpression() instanceof MethodInvocation) {
 					MethodInvocation invocation = (MethodInvocation) ((ExpressionStatement) newElement)
 							.getExpression();
 					JavaMethod method = methodInvocations(invocation);
 					if (method != null)
 						invocations.add(method);
 				}
 			}
 		}
 		return invocations;
 	}
 
 	// Creates a JavaMethod for the method invocation
 	private JavaMethod methodInvocations(MethodInvocation node) {
 		ITypeBinding type = null;
 		// the method is invoked on a variable
 		if (node.getExpression() instanceof SimpleName) {
 			SimpleName simpleName = (SimpleName) node.getExpression();
 			IBinding binding = simpleName.resolveBinding();
 			if (binding instanceof IVariableBinding) {
 				IVariableBinding variable = (IVariableBinding) binding;
 				type = variable.getType();
 			}
 		}
 		// the method is invoked on an instancecreation
 		if (node.getExpression() instanceof ClassInstanceCreation) {
 			ClassInstanceCreation instance = (ClassInstanceCreation) node
 					.getExpression();
 			IBinding binding = instance.getType().resolveBinding();
 			if (binding instanceof ITypeBinding) {
 				type = (ITypeBinding) binding;
 			}
 		}
 		if (type != null) {
 			IPackageBinding packageBinding = type.getPackage();
 			JavaMethod method = new JavaMethod(node.getName()
 					.getFullyQualifiedName(), "", "", new JavaClass(
 					packageBinding.getName(), type.getName(), "", ""));
 			addArgumentStubs(node, method);
 			return method;
 		}
 		return null;
 	}
 
 	// Adds a javaClass to the javaMethod for every argument in the method
 	// declaration
 	private void addArgumentStubs(MethodDeclaration node, JavaMethod method) {
 		for (Object expression : node.parameters()) {
 			if (expression instanceof SingleVariableDeclaration) {
 				SingleVariableDeclaration declaration = (SingleVariableDeclaration) expression;
 				ITypeBinding type = declaration.getType().resolveBinding();
 				IPackageBinding packageBinding = type.getPackage();
 				JavaClass argument = new JavaClass(packageBinding.getName(),
 						type.getName(), "", "");
 				method.arguments().add(argument);
 			}
 		}
 	}
 
 	// Adds a javaClass to the javaMethod for every argument in the method
 	// invocation
 	private void addArgumentStubs(MethodInvocation node, JavaMethod method) {
 		for (Object expression : node.arguments()) {
 			if (expression instanceof ThisExpression) {
 				ThisExpression thisExpression = (ThisExpression) expression;
 				ITypeBinding type = thisExpression.resolveTypeBinding();
 				IPackageBinding packageBinding = type.getPackage();
 				JavaClass argument = new JavaClass(packageBinding.getName(),
 						type.getName(), "", "");
 				// Add interfaces to argument
 				for (ITypeBinding interfaceType : type.getInterfaces()) {
 					argument.addInterface(new JavaClass(interfaceType
 							.getPackage().getName(), interfaceType.getName(),
 							"", ""));
 				}
 				method.arguments().add(argument);
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the {@link TypeDeclaration} is a violation of an interface
 	 * restriction
 	 * 
 	 * @param node
 	 *            the {@link TypeDeclaration} to analyze
 	 * @param tagnode
 	 *            the current {@link TagNode}
 	 * @param interfaces
 	 *            the restricted interfaces to trace
 	 */
 	public void checkRestricted(TypeDeclaration node, TagNode tagnode,
 			ArrayList<JavaClass> interfaces) {
 		if (!node.isInterface()) {
 			for (Iterator iter = node.superInterfaceTypes().iterator(); iter
 					.hasNext();) {
 				Object object = iter.next();
 				if (object instanceof SimpleType) {
 					SimpleType simpleType = (SimpleType) object;
 					IBinding binding = simpleType.resolveBinding();
 					if (binding instanceof ITypeBinding) {
 						ITypeBinding type = (ITypeBinding) binding;
 						IPackageBinding packageBinding = type.getPackage();
 						checkRestricted(node, tagnode, interfaces, type
 								.getName(), packageBinding.getName());
 					}
 				}
 			}
 		}
 	}
 
 	// Checks whether the interface implementation was restricted
 	private void checkRestricted(TypeDeclaration node, TagNode tagnode,
 			ArrayList<JavaClass> interfaces, String interfaceName,
 			String packageName) {
 		for (Iterator<JavaClass> iter = interfaces.iterator(); iter.hasNext();) {
 			JavaClass interfaceRest = iter.next();
 			if (interfaceRest.intendedName().equals(interfaceName)
 					&& interfaceRest.packageName().equals(packageName)
 					&& (!interfaceRest.optional || !interfaceRest
 							.intendedName().equals(interfaceRest.className()))) {
 				checkRestricted(node, tagnode, interfaceName, packageName);
 			}
 		}
 	}
 
 	// Checks whether the interface implementation was intended
 	private void checkRestricted(TypeDeclaration node, TagNode tagnode,
 			String interfaceName, String packageName) {
 		boolean found = false;
 		// Check whether the interface implementation was intended
 		for (Iterator<ICodeElement> iter = tagnode.source().iterator(); iter
 				.hasNext();) {
 			ICodeElement element = iter.next();
 			if (element instanceof JavaClass) {
 				JavaClass javaClass = (JavaClass) element;
 				for (Iterator<JavaClass> ite2 = javaClass.interfaces()
 						.iterator(); ite2.hasNext();) {
 					JavaClass interfaceImpl = ite2.next();
 					if (interfaceImpl.intendedName().equals(interfaceName)
 							&& interfaceImpl.packageName().equals(packageName)) {
 						found = true;
 					}
 				}
 			}
 		}
 		// The interface implementation was not intended
 		if (!found) {
 			reportError(node, interfaceName);
 		}
 	}
 
 	// Reports the violation of an interface implementation
 	private void reportError(TypeDeclaration node, String interfaceName) {
 		status.add(new Status(IStatus.ERROR, status.getPlugin(), 1, pattern
 				+ ": The class \"" + getName(node)
 				+ " is not allowed in to implement the \"" + interfaceName
 				+ "\" interface." + "                             ", null));
 	}
 
 	/**
 	 * Checks whether the {@link MethodInvocation} is a violation of a method
 	 * restriction
 	 * 
 	 * @param node
 	 *            the {@link MethodInvocation} to analyze
 	 * @param tagnode
 	 *            the current {@link TagNode}
 	 * @param methods
 	 *            the restricted methods to trace
 	 */
 	public void checkRestricted(MethodInvocation node, TagNode tagnode,
 			ArrayList<JavaMethod> methods) {
 		for (Iterator<JavaMethod> iter = methods.iterator(); iter.hasNext();) {
 			JavaMethod method = iter.next();
 			if (node.getName().getFullyQualifiedName().equals(method.name())) {
 				ITypeBinding type = null;
 				// the method is invoked on a variable
 				if (node.getExpression() instanceof SimpleName) {
 					SimpleName simpleName = (SimpleName) node.getExpression();
 					IBinding binding = simpleName.resolveBinding();
 					if (binding instanceof IVariableBinding) {
 						IVariableBinding variable = (IVariableBinding) binding;
 						type = variable.getType();
 					}
 				}
 				// the method is invoked on an instancecreation
 				if (node.getExpression() instanceof ClassInstanceCreation) {
 					ClassInstanceCreation instance = (ClassInstanceCreation) node
 							.getExpression();
 					IBinding binding = instance.getType().resolveBinding();
 					if (binding instanceof ITypeBinding) {
 						type = (ITypeBinding) binding;
 					}
 				}
 				// check the type of the object on which the method is invoked
 				if (type != null) {
 					IPackageBinding packageBinding = type.getPackage();
 					// Check if method is defined for type
 					if (method.className().equals(type.getName())
 							&& method.packageName().equals(
 									packageBinding.getName())) {
 						checkRestricted(node, tagnode, method);
 					}
 					// Check if method is defined for an interface type
 					for (ITypeBinding interfaceType : type.getInterfaces()) {
 						if (method.className().equals(interfaceType.getName())
 								&& method.packageName().equals(
 										interfaceType.getPackage().getName())) {
 							checkRestricted(node, tagnode, method);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	// Checks whether the method invocation was intended by the pattern
 	private void checkRestricted(MethodInvocation node, TagNode tagnode,
 			JavaMethod method) {
 		boolean found = false;
 		// Check whether the invocation was intended
 		for (Iterator<ICodeElement> iter = tagnode.source().iterator(); iter
 				.hasNext();) {
 			ICodeElement element = iter.next();
 			if (element instanceof JavaMethod) {
 				JavaMethod javaMethod = (JavaMethod) element;
 				if (javaMethod.name().equals(method.name())
 						&& javaMethod.packageName()
 								.equals(method.packageName())) {
 					found = true;
 				}
 			}
 		}
 		// The invocation was not intended
 		if (!found) {
 			reportError(node, method);
 		}
 	}
 
 	// Reports the violation of a method invocation
 	private void reportError(MethodInvocation node, JavaMethod method) {
 		String container = "One of the classes ";
 		ASTNode parent = node;
 		// Get the containing class
 		while (parent.getParent() != null) {
 			parent = parent.getParent();
 			if (parent instanceof TypeDeclaration) {
 				container = "The class \"" + getName((TypeDeclaration) parent)
 						+ "\" ";
 				break;
 			}
 		}
 		// Report the violation
 		status.add(new Status(IStatus.ERROR, status.getPlugin(), 1, pattern
 				+ ": " + container + "is not allowed to invoke the method \""
 				+ method.name() + "()\"." + "                             ",
 				null));
 	}
 
 	// ----------- From here on the implementation is incomplete ------------//
 
 	/**
 	 * Searches for methods which are not yet represented in the UML model
 	 * 
 	 * @param node
 	 *            TODO
 	 * @param self
 	 *            the {@link MethodDeclaration}
 	 * @param umlReader
 	 *            the {@link UMLAdapter}
 	 */
 	public void findNewMethod(MethodDeclaration node, TagNode self,
 			UMLAdapter umlReader) {
 		// Checks whether the method is a declaration
 		String archiMateTag = "";
 		for (Iterator<ICodeElement> iter = self.source().iterator(); iter
 				.hasNext();) {
 			ICodeElement element = iter.next();
 			if (element instanceof JavaClass
 					&& ((JavaClass) element).isInterface()) {
 				archiMateTag = self.tag();
 			}
 		}
 		// Tries to match the method with a UML element
 		if (!archiMateTag.equals("")) {
 			String name = node.getName().getFullyQualifiedName();
 			boolean match = false;
 			for (Iterator<TagNode> iter = self.children().iterator(); iter
 					.hasNext();) {
 				for (Iterator<ICodeElement> ite2 = iter.next().source()
 						.iterator(); ite2.hasNext();) {
 					ICodeElement element = ite2.next();
 					if (element instanceof JavaMethod) {
 						JavaMethod method = (JavaMethod) element;
 						if (name.equals(method.name())) {
 							match = true;
 							break;
 						}
 					}
 				}
 			}
 			// Adds the method to the UML model
 			if (!match) {
 				// TODO complete implementation
 				// String tag = umlReader.addMessage(archiMateTag, name);
 				// addArchimateTag(node, tag);
 				// reportAddedMessage(name);
 			}
 		}
 	}
 
 	private void reportAddedMessage(String name) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void addArchimateTag(MethodDeclaration node, String archiMateTag) {
 		if (getArchiMateTag(node).equals("")) {
 			AST ast = node.getAST();
 			Javadoc javadoc = node.getJavadoc();
 			if (javadoc == null) {
 				javadoc = ast.newJavadoc();
 			}
 			TagElement tag = ast.newTagElement();
 			TextElement te = ast.newTextElement();
 			tag.setTagName(ARCHIMATETAG);
 			te = ast.newTextElement();
 			tag.fragments().add(te);
 			te.setText(archiMateTag);
 			javadoc.tags().add(tag);
 			node.setJavadoc(javadoc);
 		}
 	}
 }
