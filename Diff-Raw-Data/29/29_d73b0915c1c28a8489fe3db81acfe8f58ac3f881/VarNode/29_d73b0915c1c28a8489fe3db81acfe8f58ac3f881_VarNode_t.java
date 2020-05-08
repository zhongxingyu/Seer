 package nodes;
 
 import java.util.HashMap;
 
 import ci_compiler.AbstractDescr;
 
 public class VarNode extends AbstractNode {
 
 	AbstractNode identList, type;
 	int line, column;
 	
 	public VarNode(AbstractNode ident, AbstractNode type, int line, int column) {
 		super(line, column);
 		this.identList = ident;
 		this.type = type;
 	}
 	
 	public VarNode() {
 		super(0,0);
 		this.identList = null;
 		this.type = null;
 	}
 
 	public AbstractNode getIdentList() {
 		return identList;
 	}
 
 	public void setIdentList(AbstractNode ident) {
 		this.identList = ident;
 	}
 
 	public AbstractNode getType() {
 		return type;
 	}
 
 	public void setType(AbstractNode type) {
 		this.type = type;
 	}
 
 	@Override
 	public AbstractDescr compile(HashMap<String, AbstractDescr> symbolTable) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void print() {
 		trace("VarNode");
 		identList.print();
		trace("TypeNode: ");
 		type.print();
 		unindent();
		unindent();
 	}
 
 }
