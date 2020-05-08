 package ca.uwaterloo.joos.ast;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import ca.uwaterloo.joos.Main;
 import ca.uwaterloo.joos.ast.AST.ASTConstructException;
 import ca.uwaterloo.joos.ast.visitor.ASTVisitor;
 
 public abstract class ASTNode {
 	protected static final Logger logger = Main.getLogger(ASTNode.class);
 
 	@SuppressWarnings("serial")
 	public static class ChildTypeUnmatchException extends ASTConstructException {
 		public ChildTypeUnmatchException(String msg) {
 			super(msg);
 		}
 	}
 
 	protected ASTNode parent = null;
 	protected String identifier = new String();
 	protected Map<Descriptor, Object> childrenList = new HashMap<Descriptor, Object>();
 
 	public ASTNode(ASTNode parent) {
 		this.parent = parent;
 	}
 
 	/**
 	 * @return the parent
 	 */
 	public ASTNode getParent() {
 		return parent;
 	}
 
 	/**
 	 * @return the identifier
 	 */
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<ASTNode> getChildByDescriptor(ChildListDescriptor listDescriptor) throws ChildTypeUnmatchException {
 		Object child = childrenList.get(listDescriptor);
 		if (child == null || child instanceof List) {
 			return (List<ASTNode>) child;
 		}
 		throw new ChildTypeUnmatchException(listDescriptor + " is not mapping to a List");
 	}
 
 	public ASTNode getChildByDescriptor(ChildDescriptor childDescriptor) throws ChildTypeUnmatchException {
 		Object child = childrenList.get(childDescriptor);
 		if (child instanceof ASTNode) {
 			return (ASTNode) child;
 		}
 		throw new ChildTypeUnmatchException(childDescriptor + " is not mapping to a ASTNode");
 	}
 
 	public void accept(ASTVisitor visitor) throws Exception {
 		visitor.willVisit(this);
		logger.finer("Visiting <" + this.getClass().getSimpleName() + ">");
 		if (visitor.visit(this)) {
 			for (Descriptor key : this.childrenList.keySet()) {
 				if (key instanceof ChildListDescriptor) {
					List<ASTNode> children = this.getChildByDescriptor((ChildListDescriptor) key);
 					for (ASTNode child : children) {
						child.accept(visitor);
 					}
 				}
 				else if(key instanceof ChildDescriptor) {
					this.getChildByDescriptor((ChildDescriptor) key).accept(visitor);
 				}
 			}
 		}
 		visitor.didVisit(this);
 	}
 
 	@Override
 	public String toString() {
		String str = "";
		str += "<" + this.getClass().getSimpleName() + ">";
		if(this.identifier.length() > 0) str += " " + this.identifier;
		if (this.parent != null)
			str += " parent: " + this.parent.getClass().getSimpleName();
		return str;
 	}
 
 	public String toString(int level) {
 		String str = "";
 		for (int i = 0; i < level; i++) {
 			str += "  ";
 		}
 		str += "<" + this.getClass().getSimpleName() + ">";
 		if(this.identifier.length() > 0) str += " " + this.identifier;
 		if (this.parent != null)
 			str += " parent: " + this.parent.getClass().getSimpleName();
 		str += "\n";
 		return str;
 	}
 }
