 package util.ast.node;
 
 import java.util.ArrayList;
 
 import back_end.Visitor;
 import util.type.Types;
 import util.type.Types.Primitive;
 
 /**
  * A node representing the ID terminal in a parse tree.
  * 
  * @author sam
  * 
  */
 public class IdNode extends ExpressionNode {
 
 	private String identifier;
 	private boolean isDeclaration = false;
 
 	// private Types.Primitive primitiveType;
 	// private Types.Derived derivedType;
 
 	public IdNode(String id) {
 		this(null, id);
 	}
 
 	public IdNode(TypeNode type, String id) {
 		super(type, new ArrayList<Node>());
 		this.setIdentifier(id);
 		this.type = type;
 		IdNode.LOGGER.fine("Constructing IdNode");
 	}
 
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	/**
 	 * Method to Set isDeclaration to True
 	 * 
 	 */
 	public void setDeclaration() {
 		this.isDeclaration = true;
 	}
 
 	@Override
 	public String getName() {
		return id + "-IdNode<" + this.getTypeName() + "," + this.getIdentifier()
 				+ ">";
 	}
 
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 	@Override
 	public int visitorTest(Visitor v) {
 		v.visit(this);
 		System.out.println("here in id node");
 		return 6;
 	}
 
 	@Override
 	public String toSource() {
 		return (isDeclaration) ? this.getType().toSource() + " "
 				+ this.getIdentifier() : this.getIdentifier();
 	}
 }
