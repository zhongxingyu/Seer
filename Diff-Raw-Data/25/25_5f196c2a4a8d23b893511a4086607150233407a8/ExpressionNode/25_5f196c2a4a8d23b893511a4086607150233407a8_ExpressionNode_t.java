 package util.ast.node;
 
 import util.type.Types.Primitive;
 import java.util.ArrayList;
 import java.util.List;
 
 import back_end.Visitor;
 
 /**
  * An abstract class inherited by all expression classes, specifying type information.
  * 
  * @author sam
  *
  */
 public abstract class ExpressionNode extends StatementNode {
 	
 	protected TypeNode type;
 	
 	
 	public ExpressionNode() {
 		this(new ArrayList<Node>());
 	}
 	
 	public ExpressionNode(List<Node> children) {
		this(null, children);
 	}
 	
 	public ExpressionNode(TypeNode type) {
		this(type, new ArrayList<Node>());
 	}
 	
	public ExpressionNode(TypeNode type, List<Node> children) {
 		super(children);
 		this.type = type;
 	}
 	
 	public TypeNode getType() {
 		return type;
 	}
 	
 	protected void computeType() {
 		throw new UnsupportedOperationException("TODO");
 	}
 	
 	public void setType(TypeNode type) {
 		this.type = type;
 	}
 	
 	@Override
 	public String getName() {
 		return "ExpressionNode<" + this.getTypeName() + ">";
 	}
 	
 	public String getTypeName() {
 		if (type != null) {
 			return type.toString();
 		}
 		return "unknown";
 	}
 	
 
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 	@Override
 	public int visitorTest(Visitor v){
 		v.visit(this);
 		return 5;
 	}
 }
