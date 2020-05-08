 package ch.codedump.ooplss.symbolTable;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import ch.codedump.ooplss.symbolTable.exceptions.IllegalSuperclass;
 import ch.codedump.ooplss.symbolTable.exceptions.IllegalSupertype;
 import ch.codedump.ooplss.symbolTable.exceptions.InvalidMemberRedefinitionException;
 import ch.codedump.ooplss.symbolTable.exceptions.MethodOverrideWrongArgumentsException;
 import ch.codedump.ooplss.symbolTable.exceptions.MethodOverrideWrongReturnTypeException;
 import ch.codedump.ooplss.symbolTable.exceptions.OoplssException;
 import ch.codedump.ooplss.symbolTable.exceptions.UnknownSuperClassException;
 import ch.codedump.ooplss.tree.OoplssAST;
 
 /**
  * A class symbol
  */
 public class ClassSymbol extends ScopedSymbol implements Type {
 	/**
 	 * The super type of this class
 	 */
 	protected ClassSymbol supertype;
 	
 	/**
 	 * The super class of this class
 	 */
 	protected ClassSymbol superclass;
 	
 	/**
 	 * The constructor of this class symbol
 	 */
 	protected MethodSymbol constructor;
 	
 	/**
 	 * The logger
 	 */
 	static Logger logger = Logger.getLogger(ClassSymbol.class.getName());
 
 	/**
 	 * Construct a new class symbol
 	 * @param name The name of the class
 	 * @param enclosingScope The scope that this class is defined in
 	 */
 	public ClassSymbol(String name, Scope enclosingScope) {
 		super(name,  enclosingScope);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * Resolve the member hierarchically upwards, that is,
 	 * if not found look in the super type and super class but also
 	 * in the enclosing scope for the definition.
 	 */
 	@Override
 	public Symbol resolve(String name) {
 		Symbol s = members.get(name);
 		
 		if (s != null) {
 			return s;
 		}
 		
 		if (this.supertype != null) {
 			s = this.supertype.resolve(name);
 			
 			if (s != null) {
 				return s;
 			}
 		}
 		
 		if (this.superclass != null) {
 			return this.superclass.resolve(name);
 		}
 		
 		if (enclosingScope != null) {
 			return enclosingScope.resolve(name);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Compare the arguments of two method definitions
 	 * 
 	 * @param m1 The first method definition
 	 * @param m2 The second method definition
 	 * @return Whether they have the same argument signature
 	 */
 	protected boolean checkMethodArguments(MethodSymbol m1, MethodSymbol m2) {
 		List<Symbol> args1 = m1.getArguments();
 		List<Symbol> args2 = m2.getArguments();
 
 		if (args1.size() != args2.size()) {
 			return false;
 		}
 		
 		for (int i = 0; i < args1.size(); i++) {
 			if (args1.get(i).getType() != args2.get(i).getType()) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Compare the return types of two methods
 	 * 
 	 * @param m1 The first method definition
 	 * @param m2 The second method definition
 	 * @return Whether they have the same return types
 	 */
 	protected boolean checkMethodReturnTypes(MethodSymbol m1, MethodSymbol m2) {
 		return m1.getType() == m2.getType();
 	}
 	
 	
 	/**
 	 * Resolve a member 
 	 * 
 	 * Resolve a symbol that belongs to this class. Also
 	 * look in the super type and class for the definition if
 	 * not found. This is slightly different to resolve(), since
 	 * it does not look in the enclosing scope.
 	 * @param name The name to resolve
 	 * @return resolved symbol
 	 */
 	public Symbol resolveMember(String name) {
 		Symbol s = members.get(name);
 		if (s != null) {
 			return s;
 		}
 		
 		if (this.supertype != null) {
 			s = this.supertype.resolveMember(name);
 			if (s != null) {
 				return s;
 			}
 		}
 		
 		if (this.superclass != null) {
 			return this.superclass.resolveMember(name);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Resolve the super declarations 
 	 * 
 	 * Since it is possible to add the super constructors
 	 * after the constructor declaration, these names
 	 * have to be resolved
 	 * @param sup The AST node of the super specification
 	 * @throws NoSuperTypeException 
 	 * @throws UnknownSuperClassException 
 	 */
 	public void resolveSuper(OoplssAST sup) throws UnknownSuperClassException {
 		String name = sup.getText();
 		
 		if (this.supertype != null) {
 			if (this.supertype.getName().equals(name)) {
 				sup.setSymbol(this.supertype);
 				return;
 			}
 		}
 		
 		if (this.superclass != null) {
 			if (this.superclass.getName().equals(name)) {
 				sup.setSymbol(this.superclass);
 				return;
 			}
 		}
 		throw new UnknownSuperClassException(sup);
 	}
 	
 	/**
 	 * Print all the members of this scope
 	 * 
 	 * @return Scope members
 	 */
 	@Override
 	public String toString() {
 		String str = "CLASS " + this.getName();
 		if (this.enclosingScope != null) {
 			str += "<" + this.enclosingScope.getName() + ">: ";
 		}
 				
 		str += "[";
 		boolean first = true;
 		for (Entry<String, Symbol> s: this.members.entrySet()) {
 			if (first) {
 				first = false;
 			} else {
 				str += ", ";
 			}
 			str += s.getValue().symbolString();
 		}
 		str += "]";
 				
 		return str;
 	}
 
 	@Override
 	public String symbolString() {
 		String str = "<Class>" + getName(); 
 		return str;
 	}
 	
 	/**
 	 * Set the super type of this class
 	 * 
 	 * @param superType The super type
 	 * @throws IllegalSupertype 
 	 * @throws IllegalSuperclass 
 	 */
 	public void setSupertype(ClassSymbol superType) 
 			throws OoplssException {
 		this.supertype = superType;
 		this.checkForInheritanceErrors();
 		this.define(
 			new SuperVariableSymbol(this.supertype)
 		);
 	}
 	
 	/**
 	 * Set the super class of this class
 	 * 
 	 * @param superClass The super class
 	 * @throws IllegalSuperclass 
 	 * @throws IllegalSupertype 
 	 */
 	public void setSuperclass(ClassSymbol superClass) 
 			throws OoplssException {
 		this.superclass = superClass;
 		this.checkForInheritanceErrors();
 		this.define(
 			new SuperVariableSymbol(this.superclass)
 		);
 	}
 	
 	/**
 	 * Check for illegal subtyping and -classing
 	 * 
 	 * @throws IllegalSupertype
 	 * @throws IllegalSuperclass
 	 */
 	protected void checkForInheritanceErrors() throws IllegalSupertype, IllegalSuperclass {
 		if (this.superclass != null && this.supertype != null) {
 			if (this.superclass.isSubtypeOf(this.supertype)) {
 				throw new IllegalSupertype(this, this.superclass, this.supertype);
 			}
 			if (this.supertype.isSubclassOf(this.superclass)) {
 				throw new IllegalSuperclass(this, this.supertype, this.superclass);
 			}
 		}
 	}
 	
 	/**
 	 * Check symbol override
 	 * 
 	 * Check if a symbol is overriding another one. If it is the
 	 * case and it's a variable, throw an exception to inhibit this.
 	 * If it's an method, check if the signature is the same. If it is, 
 	 * set the override flag of the overriding method, otherwise throw
 	 * exceptions.
 	 * @param scope The class to compare
 	 * @param sym The symbol to be compared
 	 * @throws OoplssException
 	 */
 	protected void checkSymbolOverride(ClassSymbol scope, Symbol sym) throws OoplssException {
 		Symbol resolvedSym = scope.resolveMember(sym.getName());
 		if (resolvedSym != null) {
 			if (resolvedSym != null) {
 				if (sym instanceof VariableSymbol && resolvedSym instanceof VariableSymbol) {
 					throw new InvalidMemberRedefinitionException(sym, resolvedSym);
 				}
 				
 				if (sym instanceof MethodSymbol && resolvedSym instanceof MethodSymbol) {
 					if (!this.checkMethodArguments((MethodSymbol)sym, (MethodSymbol)resolvedSym)) {
 						throw new MethodOverrideWrongArgumentsException(sym, resolvedSym);
 					} 
 					
 					if (!this.checkMethodReturnTypes((MethodSymbol)sym, (MethodSymbol)resolvedSym)) {
 						throw new MethodOverrideWrongReturnTypeException(sym, resolvedSym);
 					}
 					
 					// seems ok, set the override flag
 					((MethodSymbol)sym).setOriginSymbol((MethodSymbol)resolvedSym);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Go through the symbols and check for inheritance mistakes
 	 * 
 	 * @throws OoplssException
 	 */
 	public void checkForOverridings () throws OoplssException {
 		for (Entry<String, Symbol> sym: this.members.entrySet()) {
 			if (this.supertype != null) {
 				this.checkSymbolOverride(this.supertype, sym.getValue());
 			}
 			if (this.superclass != null) {
 				this.checkSymbolOverride(this.superclass, sym.getValue());
 			}
 		}
 	}
 	
 	/**
 	 * Create a constructor if there was none given explicitly
 	 * 
 	 * @throws OoplssException
 	 */
 	public void checkForConstructor() throws OoplssException {
 		if (this.constructor == null) {
 			this.constructor = new MethodSymbol("construct", this);
 		}
 	}
 	
 	/**
 	 * Return the classes super type
 	 * 
 	 * @return The super type of the class
 	 */
 	public ClassSymbol getSupertype() {
 		return this.supertype;
 	}
 	
 	/**
 	 * Return the classes superclass
 	 * 
 	 * @return The super class of the class
 	 */
 	public ClassSymbol getSuperclass() {
 		return this.superclass;
 	}
 
 	@Override
 	public int getTypeIndex() {
 		return SymbolTable.tOBJECT;
 	}
 
 	/**
 	 * Check whether this class is a subtype of another one
 	 * 
 	 * @param ClassSymbol
 	 */
 	public boolean isSubtypeOf(ClassSymbol type) {
 		if (this == type) {
 			return true;
 		}
 		if (this.supertype != null) {
 			return this.supertype.isSubtypeOf(type);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Check whether this class is a subclass of the given one
 	 * 
 	 * @param ClassSymbol
 	 */
 	public boolean isSubclassOf(ClassSymbol type) {
 		if (this == type) {
 			return true;
 		}
 		
 		if (this.superclass != null) {
			return this.superclass.isSubclassOf(type);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Set the explicit constructor of this class
 	 * 
 	 * @param c The constructor
 	 */
 	public void setConstructor(MethodSymbol c) {
 		this.constructor = c;
 	}
 	
 	/**
 	 * Return the constructor of this class
 	 * 
 	 * @return The class constructor
 	 */
 	public MethodSymbol getConstructor() {
 		return this.constructor;
 	}
 }
