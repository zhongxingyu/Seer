 package translator;
 
 import xtc.tree.Node;
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 import java.util.ArrayList;
 
 class JavaVariable extends Visitor {
 	
 	/**
 	 * Java variable characteristics
 	 */
 	private JavaScope scope;
 	private boolean isObject;
 	private boolean isStatic;
 	private String type;
 	private String name;
 	
 	/**
 	 * value may only be applicable to primitive types and Strings
 	 */
 	private String value;
 	private ArrayList<String> modifiers = new ArrayList<String>();
 	
 	/**
 	 * This is used for testing if type is primitive
 	 */
 	private static ArrayList<String[]> primitives = new ArrayList<String[]>();
 	
 	/**
 	 * Constructor 
 	 */
 	JavaVariable(JavaScope scope, GNode n) {
 		setPrimitives();		
 		this.scope = scope;
 		this.dispatch((Node) n);
 	}
 	
 	/**
 	 * Grabs all modifiers placed on a variable
 	 */
 	public void visitModifiers(GNode n) {
		for (int child = 0; child < n.size(); child++) {
			modifiers.add(((GNode)n.get(0)).get(child).toString());
		}
 	}
 	
 	/**
 	 * Grabs the variable type.  If an object (not primitive), find its 
 	 * class and activate it for translation
 	 */
 	public void visitType(GNode n) {
 		type = ((GNode)n.get(0)).get(0).toString();
 		if (!(primitives.contains(type))) {
 			this.isObject = true;
 			this.scope.getFile().getImport(type).activate();
 		}
 	}
 	
 	/**
 	 * Grabs the variable name
 	 */
 	public void visitDeclarators(GNode n) {
 		name = ((GNode)n.get(0)).get(0).toString();
 	}
 	
 	/**
 	 * Grabs the value of a variable in String representation.  This may only apply to primitive types 
 	 * and String literals
 	 */
 	public void getValue(GNode n) {
 		
 	}
 	
 	/**
 	 * Sets primitive types for checking
 	 */
 	private static void setPrimitives() {
 		String[] p = {"byte", "short", "int", "long", "float", "double", "char", "boolean"};
 		primitives.add(p);
 	}
 	
 	/**
 	 * The default visitor method from Visitor.
 	 */
 	public void visit(Node n) {
 		for (Object o : n) {
 			if (o instanceof Node)
 				this.dispatch((Node)o);
 		}
 	}
 	
 	public static void main(String args[]) {
 		System.out.println("hello world");
 	}
 }
