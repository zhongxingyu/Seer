 package archimate.patterns;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.uml2.uml.NamedElement;
 
 import archimate.codegen.ICodeElement;
 import archimate.codegen.ICodeGenerator;
 import archimate.uml.UMLAdapter;
 import archimate.util.FileHandler;
 import archimate.util.JavaClass;
 import archimate.util.JavaMethod;
 import archimate.util.SourceInspector;
 import archimate.util.TagNode;
 import archimate.util.TagTree;
 
 /**
  * This class implements the generic methods for Patterns and Primitives
  * 
  * @author Samuel Esposito
  */
 public abstract class Pattern implements ICodeGenerator {
 
 	// Nr. of estimated tasks
 	protected int tasks = 0;
 	// The name of the pattern
 	protected String name;
 	// Base of the package
 	protected String packageBase;
 	// Tree defining the structure of the MVC pattern key elements
 	protected TagTree tree;
 	// ProgressMonitor
 	protected IProgressMonitor monitor;
 	// Status
 	protected MultiStatus status;
 	// UML reader
 	protected UMLAdapter umlReader;
 
 	// Returns the name of the pattern
 	public String name() {
 		return name;
 	}
 
 	// Returns the package base
 	public String packageBase() {
 		return packageBase;
 	}
 
 	// Returns the type of the method based on the archiMateTag
 	public static String methodType(String archiMateTag) {
 		try {
 			if (archiMateTag.substring(archiMateTag.length() - 7,
 					archiMateTag.length()).equals("Message")) {
 				return JavaMethod.DECLARATION;
 			} else if (archiMateTag.substring(archiMateTag.length() - 6,
 					archiMateTag.length()).equals("Method")) {
 				return JavaMethod.IMPLEMENTATION;
 			} else if (archiMateTag.substring(archiMateTag.length() - 10,
 					archiMateTag.length()).equals("Invocation")) {
 				return JavaMethod.INVOCATION;
 			}
 		} catch (Exception e) {
 			return "";
 		}
 		return "";
 	}
 
 	// Returns the TagTree of the pattern
 	public TagTree tree() {
 		return tree;
 	}
 
 	// Returns the UML reader
 	public UMLAdapter umlReader() {
 		return umlReader;
 	}
 
 	// Returns the progress monitor of the pattern
 	public IProgressMonitor monitor() {
 		return monitor;
 	}
 
 	// Returns the status of the pattern
 	public MultiStatus status() {
 		return status;
 	}
 
 	// Estimates the number of tasks to execute
 	public int estimateTasks(String mode) {
 		if (tasks == 0) {
 			FileHandler handler = new FileHandler();
 			int count = handler.countFiles(packageBase);
 			if (mode.equals(SourceInspector.GENERATE)
 					|| mode.equals(SourceInspector.VALIDATE))
 				count *= 2;
 			count += tree.nodeCount();
 			return count;
 		} else {
 			return tasks;
 		}
 	}
 
 	// Generates code for the pattern
 	public void generate_code(final IProgressMonitor monitor, MultiStatus status) {
 		// Set progress monitor
 		this.monitor = monitor;
 		// Set status
 		this.status = status;
 
 		// Traverses the source and adds missing elements
 		SourceInspector inspector = new SourceInspector(this);
 		inspector.updateSource();
 	}
 
 	// Validates the code in the project source folder
 	public void validate_code(final IProgressMonitor monitor, MultiStatus status) {
 		// Set progress monitor
 		this.monitor = monitor;
 		// Set status
 		this.status = status;
 
 		// Traverses the source and validates its elements
 		SourceInspector inspector = new SourceInspector(this);
 		inspector.validateSource();
 	}
 
 	// Updates the currently selected UML model
 	public void update_model(final IProgressMonitor monitor, MultiStatus status) {
 		// Set progress monitor
 		this.monitor = monitor;
 		// Set status
 		this.status = status;
 
 		// Traverses the source and validates its elements
 		SourceInspector inspector = new SourceInspector(this);
 		inspector.updateModel();
 	}
 
 	// Add a number of JavaClass objects with the given settings to the TagNode
 	protected void createClasses(TagNode node, String stereotype,
 			String packageName, ArrayList<String> imports, boolean isAbstract,
 			String type, String defaultName, String format,
 			JavaClass superClass, ArrayList<JavaClass> interfaces,
 			String comment) {
 		if (stereotype == null || stereotype.equals(""))
 			stereotype = node.stereotype();
 		boolean hasrun = false;
 		int count = 0;
 		for (NamedElement element : umlReader.getElements(stereotype)) {
 			ArrayList<NamedElement> umlElements = new ArrayList<NamedElement>();
 			umlElements.add(element);
 			String name = element.getName();
 			String[] formatParts = format.split("#");
 			String className = (name.equals("") ? defaultName : formatParts[0]
 					+ name + (formatParts.length == 2 ? formatParts[1] : ""));
 			JavaClass javaClass = createClass(node, umlElements, packageName,
 					imports, type, className, superClass, interfaces, comment,
 					name.equals(""));
 			javaClass.setAbstract(isAbstract);
 			hasrun = true;
 			++count;
 		}
 		if (!hasrun) {
 			JavaClass javaClass = createClass(node, null, packageName, imports,
 					type, defaultName, superClass, interfaces, comment, true);
 			javaClass.setAbstract(isAbstract);
 		}
 	}
 
 	// Creates a JavaClass object with the given settings
 	protected JavaClass createClass(TagNode node,
 			ArrayList<NamedElement> umlElements, String packageName,
 			ArrayList<String> imports, String type, String className,
 			JavaClass superClass, ArrayList<JavaClass> interfaces,
 			String comment, boolean optional) {
 		JavaClass javaClass = new JavaClass(packageName, className, node.tag(),
 				type);
 		if (imports != null)
 			javaClass.addImports(imports);
 		if (interfaces != null)
 			javaClass.addInterfaces(interfaces);
 		if (comment != null)
 			if (comment.matches("#name#")) {
 				String[] temp = comment.split("#name#");
 				String newComment = temp[0] + className + " " + temp[1];
 				javaClass.setComment(newComment);
 			} else {
 				javaClass.setComment(comment);
 			}
 		if (superClass != null)
 			javaClass.setSuperClass(superClass);
 		// add restricted interface
 		if (type.equals(JavaClass.INTERFACE)) {
 			tree.addRestrictedInterface(javaClass);
 		}
 		javaClass.setOptional(optional);
 		javaClass.addUmlElements(umlElements);
 		node.addSource(javaClass);
 		return javaClass;
 	}
 
 	// Creates a list of Method objects with the given settings and adds it to
 	// the TagNodes sourcelist
 	protected void addMethods(TagNode node, String defaultName, String type,
 			String comment) {
 		if (node.parent() != null) {
 			TagNode parent = node.parent();
 			ArrayList<ICodeElement> source = parent.source();
 			for (ICodeElement element : source) {
 				if (element instanceof JavaClass) {
 					ArrayList<NamedElement> messages = new ArrayList<NamedElement>();
 					for (NamedElement umlElement : element.umlElements()) {
 						messages.addAll(umlReader.getReceived(umlElement, node
 								.stereotype()));
 					}
 					addMethods(node, (JavaClass) element, messages,
 							defaultName, type, comment);
 				}
 			}
 		}
 	}
 
 	// Creates a list of Methods for the current ICodeElement and adds it to the
 	// TagNode
 	private void addMethods(TagNode node, JavaClass javaClass,
 			ArrayList<NamedElement> messages, String defaultName, String type,
 			String comment) {
 		boolean hasrun = false;
 		for (int index = 0; index < messages.size(); ++index) {
 			String name = messages.get(index).getName();
 			String methodName = (name.equals("") ? defaultName
 					+ (index == 0 ? "" : index) : name);
 			JavaMethod method = new JavaMethod(methodName, node.tag(), type,
 					javaClass);
 			if (name.equals(""))
 				method.setOptional(true);
 			method.setComment(comment);
 			method.addUmlElement(messages.get(index));
 			javaClass.addChild(method);
 			node.addSource(method);
 			hasrun = true;
 		}
 		// if no method has been declared, the default method is declared
 		if (!hasrun && javaClass.optional()) {
 			JavaMethod method = new JavaMethod(defaultName, node.tag(), type,
 					javaClass);
 			method.setOptional(true);
 			method.setComment(comment);
 			javaClass.addChild(method);
 			node.addSource(method);
 		}
 	}
 
 	// Creates a list of Methods for the current ICodeElement and adds it to the
 	// TagNode
 	public void addMethods(TagNode node, JavaClass javaClass,
 			ArrayList<NamedElement> messages, ArrayList<JavaClass> args,
 			String defaultName, JavaClass defaultArg, String type,
 			String comment) {
 		boolean hasrun = false;
 		for (int index = 0; index < messages.size(); ++index) {
 			String name = messages.get(index).getName();
 			JavaClass argument = args.get(index);
 			String methodName = (name.equals("") ? defaultName
 					+ (index == 0 ? "" : index) : name);
 			JavaMethod method = new JavaMethod(methodName, node.tag(), type,
 					javaClass);
 			method.addArgument(argument);
 			if (name.equals(""))
 				method.setOptional(true);
 			method.setComment(comment);
 			method.addUmlElement(messages.get(index));
 			javaClass.addChild(method);
 			javaClass.addImport(argument.packageName() + "."
 					+ argument.className());
 			node.addSource(method);
 			hasrun = true;
 		}
 		// if no method has been declared, the default method is declared
 		if (!hasrun && javaClass.optional()) {
 			JavaMethod method = new JavaMethod(defaultName, node.tag(), type,
 					javaClass);
 			method.addArgument(defaultArg);
 			method.setOptional(true);
 			method.setComment(comment);
 			javaClass.addChild(method);
 			javaClass.addImport(defaultArg.packageName() + "."
 					+ defaultArg.className());
 			node.addSource(method);
 		}
 	}
 
 	// Clones the Method objects in the list, adds the given settings and adds
 	// the list to the TagNodes sourcelist
 	protected void addMethods(TagNode node, JavaClass javaClass,
 			JavaClass interfaceClass, String type, String comment) {
 		for (ICodeElement code : interfaceClass.children()) {
 			if (code instanceof JavaMethod) {
 				addMethod(node, javaClass, interfaceClass, (JavaMethod) code, type,
 						comment);
 			}
 		}
 	}
 
 	// Creates methods that invoke methods from the implementer class and adds
 	// them to the javaclass
 	protected void addMethods(TagNode invoker, String stereotype,
 			JavaClass javaClass, TagNode implementer, String type,
 			String comment) {
 		ArrayList<NamedElement> messages = new ArrayList<NamedElement>();
 		for (NamedElement umlElement : javaClass.umlElements()) {
 			messages.addAll(umlReader.getSent(umlElement, stereotype));
 		}
 		boolean hasrun = false;
 		for (ICodeElement element : implementer.source()) {
 			if (element instanceof JavaClass) {
 				JavaClass implementerClass = (JavaClass) element;
 				for (ICodeElement code : element.children()) {
 					boolean found = false;
 					for (NamedElement umlElement : code.umlElements()) {
 						if (messages.contains(umlElement)) {
 							found = true;
 							break;
 						}
 					}
 					if (found && code instanceof JavaMethod) {
 						hasrun = true;
 						addMethod(invoker, javaClass, implementerClass,
 								(JavaMethod) code, type, comment);
 					}
 				}
 			}
 		}
 		// if no method has been invoked, the default method invocation is added
 		if (!hasrun) {
 			for (ICodeElement element : implementer.source()) {
 				if (element instanceof JavaClass) {
 					JavaClass implementerClass = (JavaClass) element;
 					for (ICodeElement code : element.children()) {
 						if (code instanceof JavaMethod && code.optional()) {
 							addMethod(invoker, javaClass, implementerClass,
 									(JavaMethod) code, type, comment);
 						}
 						return;
 					}
 
 				}
 			}
 		}
 	}
 
 	// Creates a JavaMethod and adds it to the TagNode. When it concerns a
 	// method invocation, an import is added to the containing class and the
 	// invocation is added to the set of restrictions when needed
 	private void addMethod(TagNode node, JavaClass javaClass,
 			JavaClass objectType, JavaMethod method, String type, String comment) {
 		if (type.equals(JavaMethod.INVOCATION)
 				|| type.equals(JavaMethod.CALLBACK_INV)) {
 			if (type.equals(JavaMethod.INVOCATION))
 				javaClass.addImport(objectType.packageName() + "."
 						+ objectType.className());
 			if (!method.optional()) {
 				tree.addRestrictedMethod(method);
 			}
 		}
 		JavaMethod newMethod = new JavaMethod(method.name(), node.tag(), type,
 				objectType);
 		newMethod.addArguments(method.arguments());
 		newMethod.setOptional(method.optional());
 		newMethod.setComment(comment);
 		newMethod.addUmlElements(method.umlElements());
 		javaClass.addChild(newMethod);
 		for (JavaClass argument : method.arguments()) {
 			javaClass.addImport(argument.packageName() + "."
 					+ argument.className());
 		}
 		node.addSource(newMethod);
 	}
 
 }
