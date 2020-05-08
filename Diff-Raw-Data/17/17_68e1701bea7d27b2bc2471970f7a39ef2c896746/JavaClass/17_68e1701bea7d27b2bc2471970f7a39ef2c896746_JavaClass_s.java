 package translator;
 
 import translator.Printer.CodeBlock;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 
 public class JavaClass extends ActivatableVisitor implements Nameable, Typed {
 	/**
 	 * The name of the class (not fully qualified, just the name).
 	 */
 	private String name;
 	
 	/**
 	 * The parent class.  This is going to be something from "extends" or java.lang.Object.
 	 */
 	private JavaClass parent;
 	
 	/**
 	 * List of all virtual methods in this class (v is for virtual).
 	 * Method name -> Method object
 	 */
 	private HashMap<String, ArrayList<JavaMethod>> methods = new HashMap<String, ArrayList<JavaMethod>>();
 	
 	/**
 	 * List of all inherited fields in this scope.
 	 */
 	private LinkedHashMap<String, JavaField> inheritedFields = new LinkedHashMap<String, JavaField>();
 
 	/**
 	 * The VTable list of methods (in the order they need to appear). This is used for nothing but maintaining
 	 * the order of the VTable.
 	 */
 	private ArrayList<JavaMethod> vtable = new ArrayList<JavaMethod>();
 	
 	/**
 	 * SAEKJFA;WIE JF K;LSDFJ ASILD JFASD;IFJ!!!!!!! WHY DOES JAVA NOT INHERIT CONSTRUCTORS?!?!?!?!?!?!?!?!?!??!
 	 * This feels so dirty and wrong.
 	 */
 	JavaClass(JavaScope scope, GNode n) {
 		super(scope, n);
 	}
 	
 	/**
 	 * There are a few minor details we need to sort out once we can access our GNode.
 	 */
 	protected void onNodeSetup() {
 		//we're going to need our name, no matter what
 		this.name = this.node.get(1).toString();
 		
 		//setup our visibility -- we need this from the start so that imports work even
 		//when a class hasn't been activated yet
 		this.setupVisibility((GNode)this.node.get(0));
 		
 		//make sure we get added to the class registry
 		JavaStatic.pkgs.addClass(this);
 	}
 	
 	/**
 	 * Only to be used upon activation.  Does everything we need to get this class ready for translation.
 	 */
 	protected void process() {
 		//go for a nice visit to see everyone
 		this.dispatch(this.node);
 	
 		//activate the parent file so that all the classes with him are included
 		this.getJavaFile().activate();
 		
 		//check if we have a parent; if we don't, then java.lang.Object is our parent
 		this.setParent("java.lang.Object");
 		
 		//once we're sure we have a parent, then add all our inherited methods
 		this.setupVTable();
 	}
 	
 	/**
 	 * Determines if we are a subclass of another class.
 	 */
 	public boolean isSubclassOf(JavaClass cls) {
 		//if we are the same! yay!
 		if (this == cls)
 			return true;
 		
 		//if we reach the top, we're clearly not
 		if (this.parent == null)
 			return false;
 		
 		//if we're not at the top, ask our parent
 		return this.parent.isSubclassOf(cls);
 	}
 	
 	/**
 	 * ==================================================================================================
 	 * Printing methods
 	 */
 	
 	/**
 	 * Prints out the class.
 	 *
 	 * @param prot The prototype code block.
 	 * @param header The header code block.
 	 * @param implm The code block for the implementation.
 	 */
 	public void print(CodeBlock prot, CodeBlock header, CodeBlock implm) {
 		this.printPrototype(prot);
 		this.printHeader(header);
 		this.printImplementation(implm);
 	}
 	
 	/**
 	 * Prints out the prototype for this class.
 	 */
 	private void printPrototype(CodeBlock b) {
 		b.pln("class " + this.getName(false) + ";");
 	}
 	
 	/**
 	 * Gets that header out.
 	 */
 	private void printHeader(CodeBlock b) {
 		CodeBlock cls = b.block("class " + this.getName(false));
 		
 		System.out.println("Method size (" + this.getName() + "): " + this.methods.size());
 
 		//@TODO Print all fields
 		//we need to print all the fields out to each class definition
 		for (JavaField f : this.inheritedFields.values()) 
 			f.print(cls);
 		for (JavaField f : this.fields.values())
 			f.print(cls);
 		//we only need to print our OWN methods into the class definition
 		for (ArrayList<JavaMethod> a : this.methods.values()) {
 			for (JavaMethod m : a) {
 				//ask the method to print himself to our class definition in the header block
 				m.printToClassDefinition(cls, this);
 			}
 		}
 		
 		cls.close();
 	}
 	
 	/**
 	 * Gets the methods all printed.
 	 */
 	private void printImplementation(CodeBlock b) {
 		b.pln("METHODS AND FIELD INITIALIZATIONS FOR : " + this.name);
 		
 		for (ArrayList<JavaMethod> a : this.methods.values()) {
 			for (JavaMethod m : a) {
 				//somehow activate the method and give it something to print with
				m.print(b);
 				//b.pln("wowowow");
 			}
 		}
 
 		b.pln("END OF IMPLEMENTATION FOR : " + this.name);
 		b.pln();
 	}
 	
 	/**
 	 * Given a method, properly adds it to the methods table.
 	 */
 	private void addMethod(JavaMethod m) {
 		String name = m.getName();
 		
 		if (!this.methods.containsKey(name))
 			this.methods.put(name, new ArrayList<JavaMethod>());
 		
 		this.methods.get(name).add(m);
 	}
 	
 	/**
 	 * ==================================================================================================
 	 * Magic method finders
 	 */
 	
 	/**
 	 * Replaces the older version of getMethod() to take into account overaloding: it will find the method
 	 * that has the signature closest to the one provided.
 	 *
 	 * @return The method that is the closest match to the requested method signature.  Null if no method
 	 * was found (but since the Java is assumed to compile, this should be considered a fatal internal error.
 	 */
 	public JavaMethod getMethod(JavaMethod m) {
 		return this.getMethod(m.getName(), m.getSignature());
 	}
 	
 	/**
 	 * You're not in a method.
 	 */
 	public JavaMethod getMyMethod() {
 		return null;
 	}
 
 	/**
 	 * Replaces the older version of getMethod() to take into account overaloding: it will find the method
 	 * that has the signature closest to the one provided.
 	 *
 	 * @return The method that is the closest match to the requested method signature.  Null if no method
 	 * was found (but since the Java is assumed to compile, this should be considered a fatal internal error.
 	 */ 
 	public JavaMethod getMethod(String name, JavaMethodSignature sig) {
 		//let's see if we actually have that method defined in the first place before we start searching
 		if (!this.methods.containsKey(name))
 			return null;
 		
 		//we need to find all the methods that apply to the signature, and then
 		//find the most specific.
 		//let's see if we can do it in one loop without any major data structures
 	
 		//assuming it compiles, we're all good with being naïve and just
 		//finding some method
 		JavaMethod found = null;
 		for (JavaMethod m : this.methods.get(name)) {
 			//only look at the method if it actually applies to the signature we have
 			if (m.canBeUsedAs(sig)) {
 				//if we're on our first round
 				if (found == null) {
 					found = m;
 				} else if (!m.canBeUsedAs(found)) {
 					//if our testing method can't be used as the found,
 					//then it _must_ be more specific than found as, by now,
 					//the testing method applies to the signature, and it would
 					//only be rejected if it were more specific than found
 					found = m;
 				}
 			}
 		}
 		
 		return found;
 	}
 	
 	/**
 	 * Setup our parent.  Can only be run once, then everything is permanent.
 	 */
 	private void setParent(String parent) {
 		//java.lang.Object has no parent
 		if (this.getName(true).equals("java.lang.Object"))
 			return;
 		
 		//only allow one parent to be set
 		if (this.parent == null) {
 			if (JavaStatic.runtime.test("debug"))
 				System.out.println(this.getName() + " extends " + this.getJavaFile().getImport(parent).getName());
 			
 			//set our parent from its name in import
 			this.parent = this.getJavaFile().getImport(parent);
 			
 			//with the extension, we need to activate it (ie. process it) before we can use it
 			this.parent.activate();
 		}
 	}
 
 	public JavaClass getParent() {
 		return this.parent;
 	}
 
 	/**
 	 * Go through all the parents and get their virtual methods, then just add them.  To do this, we test
 	 * if we first have a parent (java.lang.Object doesn't); if we have a parent, grab his virtual methods,
 	 * see if we override them, if we don't, then add them, otherwise, ignore.
 	 */
 	private void setupVTable() {
 		//if we have a parent from whom we can steal methods
 		if (this.parent != null) {
 			//go through all the parent virtual methods and add them to our table
 			for (JavaMethod m : this.parent.vtable) {
 				//if we're not overriding the method
 				if (this.getMethod(m) == null) {
 					this.addMethod(m);
 					this.vtable.add(m);
 				}
 			}
 		}
 		
 		//and go through all our methods and add them back
 		for (ArrayList<JavaMethod> a : this.methods.values()) {
 			for (JavaMethod m : a) {
 				//does the method belong in the vtable?
 				if (m.isAtLeastVisible(Visibility.PROTECTED) && !m.isStatic())
 					this.vtable.add(m);
 			}
 		}
 		// If we have a parent
 		if (this.parent != null) {
 			// Inherit all of it's fields
 			this.inheritedFields.putAll(this.parent.inheritedFields);
 		}
 		// To preserve the integrity our of data we use a Set
 		HashSet<String> s = new HashSet<String>(this.inheritedFields.keySet());
 		// MANGLE MANGLE MANGLE
 		for (JavaField f : this.getAllFields()) {
 			f.mangleName(s);
 			this.inheritedFields.put(f.getName(), f);
 		}
 	}
 
 	/**
 	 * ==================================================================================================
 	 * Typed Methods
 	 */
 	
 	/**
 	 * Gets the type that this class represents.
 	 */
 	public JavaType getType() {
 		return JavaType.getType(this.getName());
 	} 
 	
 	/**
 	 * ==================================================================================================
 	 * Nameable Methods
 	 */
 	
 	/**
 	 * Gets the fully qualified java name.
 	 */
 	public String getName() {
 		return this.getName(true);
 	}
 	
 	/**
 	 * Gets the java name.
 	 *
 	 * @param fullName True for the fully-qualified java name; false for just the last part of the name.
 	 */
 	public String getName(boolean fullName) {
 		String name = "";
 		if (fullName)
 			name += this.getPackageName() + ".";
 		
 		return name + this.name; 
 	}
 
 	/**
 	 * ==================================================================================================
 	 * Visitor Methods
 	 */
 	 
 	/**
 	 * Handles resolving dependencies for inheritance.  When it sees an extension, it throws the name
 	 * to its parent file's import manager in order to resolve the name and activate the class so that 
 	 * it can extend it properly.
 	 */
 	public void visitExtension(GNode n) {
 		//java only supports single inheritance...no need for loops or anything here
 		this.setParent((String)((GNode)((GNode)n.get(0)).get(0)).get(0));
 	}
 	
 	/**
 	 * Take in a method.  Adds the method to our method table.
 	 */
 	public void visitMethodDeclaration(GNode n) {
 		JavaMethod m = new JavaMethod(this, n);
 		this.addMethod(m);
 	}
 	
 	/**
 	 * Create a FieldDec object, the FieldDec will handle everything else so this is all we need to do.
 	 */
 	public void visitFieldDeclaration(GNode n) {
 		FieldDec f = new FieldDec(this, n);
 	}
 	
 	/**
 	 * We process modifiers on instantiation, so skip that here.
 	 */
 	public void visitModifiers(GNode n) { }
 	
 	public void visitConstructorDeclaration(GNode n) {
 		//special...yay :(
 	}
 }
