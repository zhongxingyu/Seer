 package translator;
 
 import translator.Printer.CodeBlock;
 import translator.Expressions.JavaExpression;
 import translator.Expressions.Identifier;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
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
 	 * The list of all the methods that are defined in this class.
 	 */
 	private HashMap<String, ArrayList<JavaMethod>> classMethods; 
 	
 	/**
 	 * The VTable list of methods (in the order they need to appear). This is used for nothing but maintaining
 	 * the order of the VTable.
 	 */
 	private ArrayList<JavaMethod> vtable = new ArrayList<JavaMethod>();
 	
 	/**
 	 * Kinda like the vTable, but for fields -- they are all in the order that they are inherited in.
 	 */
 	private ArrayList<JavaField> fieldTable;
 	
 	/**
 	 * A flag that indicates if we have loaded our data layout and methods completely.  It stops circular resolution
 	 * that kills child loading.
 	 */
 	private boolean isSetup = false;
 	
 	/**
 	 * A flag that indicates if we have printed yet.  This stops classes from printing before their data layouts have been setup.
 	 */
 	private boolean isPrinted = false;
 	
 	/**
 	 * A list of the children that need to be alerted when our data layout has been setup.
 	 */
 	private ArrayList<JavaClass> childClasses = new ArrayList<JavaClass>(); 
 	
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
 	 * The first phase of class activation.
 	 */
 	protected void process() {
 		//go for a nice visit to see everyone
 		this.dispatch(this.node);
 		
 		//if we don't have a default constructor, give us one
 		this.checkDefaultConstructor();
 		
 		//check if we have a parent; if we don't, then java.lang.Object is our parent
 		this.setParent("java.lang.Object");
 		
 		//attempt to load our data layout -- might fail if our parent hasn't been setup yet
 		this.checkParentReady(this);
 		
 		//alert the children to setup
 		for (JavaClass child : this.childClasses) {
 			child.alertParentSetup();
 		}
 		
 		//go for the second phase of our activation -- printing
 		//we only do so if our parent has already printed.  Otherwise, we wait for him to print and trigger us to print.
 		
 		//see if we can print -- we only want to print once all our children have their data layouts (solves circular dependencies)
 		this.checkParentPrinted(this);
 		
 		//and print our children
 		for (JavaClass child : this.childClasses) {
 			child.alertPrint();
 		}
 	}
 	
 	/**
 	 * The second phase of the class activation.  This is only called when the parent has been setup and we can access
 	 * the parent's data layout.
 	 */
 	private void alertParentSetup() {
 		//once we're sure we have a parent, then add all our inherited methods
 		this.setupDataLayout();
 		
 		//activate the parent file so that all the classes with him are included
 		//delay this until the parent is setup so that we can load our data layout properly
 		this.getJavaFile().activate();
 	}
 	
 	/**
 	 * We have an alert print method so that, once our parent has finished all his setup. Because once a file is activate it
 	 * triggers activation of all the children classes, we can be assured that all children will print once their parents
 	 * have resolved all their dependencies and printed.  This ensures that things print out in the right order and that
 	 * circular dependencies don't cause things to break.
 	 */
 	private void alertPrint() {
 		if (this.isPrinted)
 			return;
 		
 		this.getJavaFile().print(this);
 		this.isPrinted = true;
 	}
 	
 	/**
 	 * Checks if the data layout for this class has been setup.  If it has not been, then it adds the class that checked
 	 * to see if it is setup to the list of "children" classes of this guy, and when the parent has been setup, the child
 	 * classes will be alerted so that they can setup their data.
 	 */
 	private void checkParentReady(JavaClass child) {
 		//if we're setup, the child doesn't need to delay his initilization (how do you speelellelel that?)
 		//always make sure java.lang.Object loads its layout as it doesn't have a parent to activate it
 		if (this.parent == null || this.parent.isSetup)
 			child.alertParentSetup();
 		
 		//our parent isn't ready, so just tell him to alert us when he is
 		else
 			this.parent.childClasses.add(child);
 	}
 	
 	/**
 	 * We can only print if our parent is printed and setup.  We do this so that circular dependencies don't cause errors.
 	 */
 	private void checkParentPrinted(JavaClass child) {
 		if (this.parent == null || this.parent.isPrinted)
 			child.alertPrint();
 		else
 			this.parent.childClasses.add(child);
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
 		JavaStatic.pkgs.importNative(this, header, implm);
 	}
 	
 	/**
 	 * Prints out the prototype for this class.
 	 */
 	private void printPrototype(CodeBlock b) {
 		b
 			.pln("struct " + this.getCppName(false, false) + ";")
 			.pln("struct " + this.getCppName(false, false) + "_VT;")
 			.pln("typedef __rt::Ptr<" + this.getCppName(false, false) + "> " + this.getCppName(false) + ";")
 			.pln()
 		;
 	}
 	
 	/**
 	 * Gets that header out.
 	 */
 	private void printHeader(CodeBlock b) {
 		//---------------------------------------------------------------------------------------
 		// Print the data layout
 		
 		String name = this.getCppName(false, false);
 		CodeBlock block = b.block("struct " + name);
 		
 		if (JavaStatic.runtime.test("debug"))
 			System.out.println("Method size (" + this.getName() + "): " + this.methods.size());
 		
 		//we need to print all the fields out to each class definition
 		block.pln("//Field layout");
 		
 		//print our default internal stuff
 		block
 			.pln(name + "_VT* __vptr;")
 			.pln("static java::lang::Class __class();")
 			
 			.pln()
 		;
 		
 		//then print out the declared fields
 		for (JavaField f : this.fieldTable) {
 			f.print(block);
 		}
 		
 		//add any extra fields we might need
 		SpecialCases.addExtraFields(this, block); 
 		
 		block.pln();
 		block.pln("//Constructors");
 		for (JavaMethod m : this.methods.get(this.getName(false))) {
 			//make the constructors print before the methods
 			m.printToClassDefinition(block, this);
 		}
 		
 		//remove the constructors so that they don't print below again
 		this.methods.remove(this.getName(false));
 		
 		block.pln();
 		block.pln("//Extra Constructors");
 		
 		//add any extra constructors we might need
 		SpecialCases.addExtraConstructors(this, block); 
 		
 		block.pln();
 		block.pln("//Methods");
 		
 		//the default __delete method
 		//block.pln("static void __delete(" + this.getCppName(false, false) + "*);");
 		
 		//we only need to print our OWN methods into the class definition
 		for (ArrayList<JavaMethod> a : this.methods.values()) {
 			for (JavaMethod m : a) {
 				//ask the method to print himself to our class definition in the header block
 				m.printToClassDefinition(block, this);
 			}
 		}
 		
 		//finally, print out our static vtable variable
 		block
 			.pln()
 			.pln("//The vtable for " + name)
 			.pln("static " + name + "_VT __vtable;")
 		;
 		
 		block.close();
 		
 		//---------------------------------------------------------------------------------------
 		// Print the vTable
 		
 		block = b.block("struct " + name + "_VT");
 		
 		//emit the "__isa"...that was fun.
 		block.pln(this.getJavaFile().getImport("Class").getCppName() + " __isa;");
 		//and the __delete() method
 		block.pln("void (*__delete)(" + this.getCppName(false, false) + "*);");
 		
 		//print out the method pointers
 		for (JavaMethod m : this.vtable) {
 			//ask the method to print himself to our class definition in the header block
 			m.printToVTable(block, this);
 		}
 		
 		//go for the constructor
 		block = block
 			.pln()
 			.block(name + "_VT() :", false)
 				.pln("__isa(" + name + "::__class()),")
 				.pln("__delete(&__rt::__delete<" + name + ">),")
 		;
 		
 		int size = this.vtable.size();
 		
 		//there are always methods inherited from Object, so we don't need to worry about the "0 methods case".
 		//print out the method pointers
 		for (int i = 0; i < size; i++) {
 			JavaMethod m = this.vtable.get(i);
 			
 			//cast the vTable entry, but only if the method doesn't originate in our class
 			String cast = "";
 			if (m.getJavaClass() != this) {
 				cast = m.getSignature().getCppArguments(false);
 				cast = "(" + m.getType().getCppName() + "(*)(" + this.getCppName(true, true) + (cast.length() > 0 ? ", " + cast : "") + "))";
 			}
 			
 			//generate the vTable entry
 			String point = m.getCppName(false) + "(" + cast + "&" + m.getCppName(true, false) +")";
 			
 			//if we're at the last method, then close the block
 			if (i == (size - 1)) {
 				block = block.block(point).close();
 			} else {
 				block.pln(point + ",");
 			}
 		}
 		
 		block.close().close();
 	}
 	
 	/**
 	 * Gets the methods all printed.
 	 */
 	private void printImplementation(CodeBlock b) {
 		b.pln("//Generated method and field implementations for: " + this.getCppName(false));
 		
 		//print out the method implementations
 		for (ArrayList<JavaMethod> a : this.methods.values()) {
 			for (JavaMethod m : a) {
 				//somehow activate the method and give it something to print with
 				m.print(b, this);
 			}
 		}
 		
 		//print out any static initializations to the cpp file
 		for (JavaField f : this.fieldTable) {
 			f.initializeInImplementation(b, this);
 		}
 		
 		
 		String name = this.getCppName(false, false);
 		b
 			//print out the vtable initializer to the Cpp file so that we can use it
 			.pln(name + "_VT " + name + "::__vtable;")
 			.pln()
 		
 			//print out the getClass() initializer
     		.block("java::lang::Class " + name + "::__class()")
     			.block("static java::lang::Class k = new java::lang::__Class(", false)
     				.pln("java::lang::asString(\"" + this.getName() + "\"),")
     				.pln((this.parent == null ? "__rt::null" : this.parent.getCppName(true, false) + "::__class()") + ",")
     				.pln("false") //we're not a primitive...we're a class!
     			.close()
     			.pln(");")
     			.pln()
     			.pln("return k;")
     		.close()
     	;
 
 
 		b.pln("//End implementation for: " + this.getCppName(false));
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
 	 * An extra wrapper that passes onto the search method which group of methods we should be searching through.
 	 */
 	public JavaMethod getMethod(String name, JavaMethodSignature sig) {
 		return this.getMethod(name, sig, this.methods);
 	}
 
 	/**
 	 * Replaces the older version of getMethod() to take into account overaloding: it will find the method
 	 * that has the signature closest to the one provided.
 	 *
 	 * @param name The name of the method.
 	 * @param sig The method signature.
 	 * @param searchMethods Which set of methods should be searched for the requested method.
 	 *
 	 * @return The method that is the closest match to the requested method signature.  Null if no method
 	 * was found (but since the Java is assumed to compile, this should be considered a fatal internal error.
 	 */ 
 	public JavaMethod getMethod(String name, JavaMethodSignature sig, HashMap<String, ArrayList<JavaMethod>> searchMethods) {
 		//let's see if we actually have that method defined in the first place before we start searching
 		if (!searchMethods.containsKey(name))
 			return null;
 		
 		if (sig == null)
 			sig = new JavaMethodSignature();
 		
 		//we need to find all the methods that apply to the signature, and then
 		//find the most specific.
 		//let's see if we can do it in one loop without any major data structures
 	
 		//assuming it compiles, we're all good with being naïve and just
 		//finding some method
 		JavaMethod found = null;
 		for (JavaMethod m : searchMethods.get(name)) {
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
 	 * Only searches through the class methods in order to find the specified method.
 	 */
 	public JavaMethod getClassMethod(JavaMethod m) {
 		return this.getClassMethod(m.getName(), m.getSignature());
 	}
 	
 	/**
 	 * Only searches through the class methods in order to find the specified method.
 	 */
 	public JavaMethod getClassMethod(String name, JavaMethodSignature sig) {
 		return this.getMethod(name, sig, this.classMethods);
 	}
 	
 	/**
 	 * Setup our parent. Can only be run once, then everything is permanent.
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
 		}
 	}
 
 	public JavaClass getParent() {
 		return this.parent;
 	}
 	
 	/**
 	 * Go through all the parents and get their virtual methods, then just add them.  To do this, we test
 	 * if we first have a parent (java.lang.Object doesn't); if we have a parent, grab his virtual methods,
 	 * see if we override them, if we don't, then add them, otherwise, ignore.
 	 *
 	 * Then, add all of the fields that our parent has (at face value), then go through and add all of our
 	 * fields, and we use the names of our parent fields to mangle the names of our local fields.
 	 */
 	private void setupDataLayout() {
 		if (this.isSetup)
 			return;
 		
 		this.isSetup = true;
 		
 		if (JavaStatic.runtime.test("debug"))
 			System.out.println("Setup data layout for: " + this.getName());
 		
 		//---------------------------------------------------------------------------------------
 		// Setup the vTable
 		
 		//store all method names so that we can mangle properly
 		HashSet<String> methodNames = new HashSet<String>();
 		
 		//we're going to take all of our local methods and store them locally then reset our methods table.
 		//
 		//we do this so that:
 		// 1) we can add all our parent's methods to our local table
 		// 2) we can populate a list of method names (for mangling) from our parent and add our parent's methods in 1 loop
 		// 3) we can then loop over all our class methods, without any interference from the parent methods that were added
 		this.classMethods = this.methods;
 		this.methods = new HashMap<String, ArrayList<JavaMethod>>();
 		
 		//if we have a parent from whom we can steal methods
 		if (this.parent != null) {
 			//go through all the parent virtual methods and add them to our table
 			for (JavaMethod m : this.parent.vtable) {
 				//if we're not overriding the method
 				JavaMethod local;
 				
 				if ((local = this.getClassMethod(m)) == null || !local.equals(m)) {
 					//add our method name WITHOUT the C++ scope resolution stuff 
 					methodNames.add(m.getCppName(false));
 				
 					this.addMethod(m);
 					this.vtable.add(m);
 				}
 			}
 		}
 		
 		//and go through all our methods and add them back
 		for (ArrayList<JavaMethod> a : this.classMethods.values()) {
 			for (JavaMethod m : a) {
 				//do some name mangling
 				m.mangleName(methodNames);
 				
 				//and re-storing the method locally
 				this.addMethod(m);
 				
 				//does the method belong in the vtable?
 				if (m.isAtLeastVisible(Visibility.PROTECTED) && !m.isStatic() && !m.isConstructor())
 					this.vtable.add(m);
 			}
 		}
 
 		//---------------------------------------------------------------------------------------
 		// Setup the inherited/local fields
 		
 		//if we have a parent, then add all his fields to our list of fields
 		if (this.parent == null)
 			this.fieldTable = new ArrayList<JavaField>();
 		else
 			this.fieldTable = new ArrayList<JavaField>(this.parent.fieldTable);
 		
 		//store all of our fields that we have before we add in all the visible fields from our parent
 		ArrayList<JavaField> localFields = this.getAllFields();
 		
 		//the list of all the fields that are contained in our parent(s)
 		HashSet<String> parentFields = new HashSet<String>();
 		JavaField.reserveNames(parentFields);
 		
 		//go through all of our fields from our parent and, if we can technically access them in Java,
 		//add them to our list of fields in the class scope.
 		for (JavaField f : this.fieldTable) {
 			//also make sure that we're not "hiding" the field in our child class
 			if (f.isAtLeastVisible(Visibility.PROTECTED) && this.getField(f.getName()) == null)
 				this.addField(f);
 			
 			//either way, add the field to our list of parent fields
			parentFields.add(f.getCppName(false));
 		}
 		
 		//MANGLE MANGLE MANGLE
 		for (JavaField f : localFields) {
 			f.mangleName(parentFields);
 			this.fieldTable.add(f);
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
 	
 	public String getCppName() {
 		return this.getCppName(true);
 	}
 	
 	public String getCppName(boolean fullName) {
 		return this.getCppName(fullName, true);
 	}
 	
 	public String getCppName(boolean fullName, boolean asPointer) {
 		String name = "";
 		if (fullName)
 			name += this.getPackageName() + ".";
 		
 		return name.replace(".", "::") + (asPointer ? "" : "__") + this.name;
 	}
 	
 	/**
 	 * Checks to see if we have a default constructor -- if we don't, we need to provide one as that is
 	 * what Java does.
 	 */
 	private void checkDefaultConstructor() {
 		if (this.methods.containsKey(this.getName(false)))
 			return;
 		
 		/*
 		ConstructorDeclaration(
 			Modifiers(),
 			null,
 			"Class",
 			FormalParameters(),
 			null,
 			Block()
 		)
 		*/
 		
 		this.visitConstructorDeclaration(GNode.create(
 			"ConstructorDeclaration",
 				GNode.create("Modifiers"),
 				null,
 				this.getName(false),
 				GNode.create("FormalParameters"),
 				null,
 				GNode.create("Block")
 			)
 		);
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
 		JavaExpression ext = (JavaExpression)this.dispatch((Node)((GNode)n.get(0)).get(0));
 		this.setParent(((Identifier)ext).getRawValue());
 	}
 	
 	/**
 	 * Take in a method.  Adds the method to our method table.
 	 */
 	public void visitMethodDeclaration(GNode n) {
 		JavaMethod m = new JavaMethod(this, n);
 		this.addMethod(m);
 	}
 	
 	/**
 	 * We process modifiers on instantiation, so skip that here.
 	 */
 	public void visitModifiers(GNode n) { }
 	
 	public void visitConstructorDeclaration(GNode n) {
 		JavaMethod m = new JavaMethod.Constructor(this, n);
 		this.addMethod(m);
 	}
 }
