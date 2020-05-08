 package cps450;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 public class ClassDeclaration extends Declaration {
 	
 	Hashtable<String, VariableDeclaration> variables;
 	Hashtable<String, MethodDeclaration> methods;
	ArrayList<MethodDeclaration> maskedInheritedMethods;
 	Integer currentMethodOffset = 0;
 	
 	String name;
 	
 	ClassDeclaration parent = null;
 	
 	public ClassDeclaration(Type _type, String _location, String _name) {
 		super(_type, _location);
 		variables = new Hashtable<String, VariableDeclaration>();
 		methods = new Hashtable<String, MethodDeclaration>();
		maskedInheritedMethods = new ArrayList<MethodDeclaration>();
 		name = _name;
 	}
 	
 	/*
 	 * Cause this class to inherit all of the variables and methods from class <_parent>.
 	 */
 	public void inheritFrom(ClassDeclaration _parent) {
 		if (_parent != null) {
 			// Link to the parent
 			parent = _parent;
 			
 			// Copy over inherited instance variables and methods
 			variables.putAll(parent.variables);
 			methods.putAll(parent.methods);
 			
 			currentMethodOffset = parent.currentMethodOffset;
 		}
 	}
 	
 	/*
 	 * Helper to determine this class is in the same hierarchy as <ancestor> and descends from it (or is the same class).
 	 */
 	private Boolean isDescendantOf(ClassDeclaration ancestor) {
 		if (ancestor == this) {
 			return true;
 		} else if (ancestor.parent == null) {
 			return false;
 		} else {
 			return this.isDescendantOf(ancestor.parent);
 		}
 	}
 	
 	/*
 	 * Semantic type checking helper to determine if this class and <klass> are compile time compatible
 	 * - that is, either <klass> is null or <klass> is in the same class hierarchy as this class.
 	 */
 	public Boolean compatibleWith(ClassDeclaration klass) {
 		if (klass == null) {
 			return true;
 		} else {
 			return this == klass || this.isDescendantOf(klass) || klass.isDescendantOf(this);
 		}
 	}
 	
 	/*
 	 * Add a method declaration to this class; overrides or masks a method
 	 * by the same name (if it exists) in the an ancestor class.
 	 */
 	public void addMethod(String name, MethodDeclaration decl) {
 		MethodDeclaration oldMethod = methods.get(name);
 		if (oldMethod != null && decl.hasIdenticalSignature(oldMethod)) {
 			// Methods have the same signature, override.
 			decl.setOffset(oldMethod.getOffset());
 		} else {
 			// Methods have different signatures, mask.
 			decl.setOffset(currentMethodOffset);
 			currentMethodOffset += 1;
			
			if (oldMethod != null) {
				// Still need to retain the old method declaration so it can be used by
				// code in the parent ancestor classes when operating on a object of the
				// descendant's type.
				maskedInheritedMethods.add(oldMethod);
			}
 		}
 		methods.put(name, decl);
 	}
 	
 	/*
 	 * Get the method declaration by its name.
 	 */
 	public MethodDeclaration getMethod(String name) {
 		return methods.get(name);
 	}
 	
 	/*
 	 * Add a new instance variable to this class.
 	 */
 	public void addVariable(String name, VariableDeclaration decl) {
 		variables.put(name, decl);
 	}
 	
 	/*
 	 * Get an instance variable in this class by <name>.
 	 */
 	public VariableDeclaration getVariable(String name) {
 		return variables.get(name);
 	}
 	
 	/*
 	 * Return the current number of instance variable defined in this class declaration.
 	 */
 	public Integer getInstanceVariableCount() {
 		return variables.size();
 	}
 	
 	/*
 	 * Get a list of methods defined in this class - in ascending VFT offset order.
 	 */
 	public ArrayList<MethodDeclaration> getMethodList() {
		Integer size = methods.size() + maskedInheritedMethods.size();
		
		ArrayList<MethodDeclaration> methodList = new ArrayList<MethodDeclaration>(size);
 		
 		// Setup initial list size
		for (int i = 0; i < size; ++i) {
 			methodList.add(null);
 		}
 		
		// Add all of the methods from the parent class that have been masked
		for (MethodDeclaration method : maskedInheritedMethods) {
			methodList.set(method.getOffset(), method);
		}
		
 		// Create a list of method declarations ordered by their offset
		// - all the methods defined directly in this class, inherited,
		// - or inherited and overridden
 		for (MethodDeclaration method : methods.values()) {
 			methodList.set(method.getOffset(), method);
 		}
		
 		return methodList;
 	}
 	
 	/*
 	 * Helper to abstract away the virtual function table label.
 	 */
 	public String getVirtualFunctionTableLabel() {
 		return "__" + name + "__" + "VFT__";
 	}
 
 	public ClassDeclaration getParent() {
 		return parent;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 }
