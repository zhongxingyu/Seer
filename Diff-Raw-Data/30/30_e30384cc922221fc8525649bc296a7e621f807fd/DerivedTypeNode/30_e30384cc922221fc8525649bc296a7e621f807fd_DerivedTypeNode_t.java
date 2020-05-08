 package util.ast.node;
 
 import back_end.Visitor;
 import util.type.Types;
 
 /**
  * A DerivedTypeNode represents a derived type 
  * which contains a local type, and an inner type.
  * 
  * For example, if those node represents the type set<list<int>>, the local
  * type is the "outermost" type (in this example, set) and the inner type
  * is everything but the outermost type (in this example, list<int>).
  * 
  * @author ben, sam
  *
  */
 public class DerivedTypeNode extends TypeNode {
 	
 	protected Types.Derived localType;
 	protected TypeNode innerTypeNode;
 	
 	protected DerivedTypeNode() {
 		// empty method to satisfy java
 	}
 	
 	/**
 	 * Construct a new DerivedTypeNode.
 	 * 
 	 * @param localType - Types.Derived representing the local type. 
 	 * @param innerTypeNode - A TypeNode representing the inner type.
 	 */
 	public DerivedTypeNode(Types.Derived localType, TypeNode innerTypeNode) {
 		this.localType = localType;
 		this.innerTypeNode = innerTypeNode;
 	}
 	
 	/**
 	 * Get the local type of this node.
 	 * 
 	 * @return the local type.
 	 */
 	public Types.Derived getLocalType() {
 		return localType;
 	}
 	
 	/**
 	 * Get the inner type of this node.
 	 * 
 	 * @return the inner type.
 	 */
 	public TypeNode getInnerTypeNode() {
 		return innerTypeNode;
 	}
 	
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 	@Override
 	public int visitorTest(Visitor v) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getName() {
		return id + "-Derived Type: " + localType.toString();
 	}
 
 	@Override
 	public boolean isBoolean() {
 		return false;
 	}
 
 	@Override
 	public boolean isNumeric() {
 		return false;
 	}
 
 	@Override
 	public boolean isText() {
 		return false;
 	}
 
 	@Override
 	public boolean isDerived() {
 		return true;
 	}
 
 	@Override
 	public boolean isDict() {
 		return false;
 	}
 
 	@Override
 	public boolean isPrimitive() {
 		return false;
 	}
 	
 	@Override
 	public boolean isException() {
 		return false;
 	}
 
 }
