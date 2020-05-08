 package ca.uwaterloo.joos.ast.decl;
 
 import java.util.List;
 
 import ca.uwaterloo.joos.ast.ASTNode;
 import ca.uwaterloo.joos.ast.type.ReferenceType;
 import ca.uwaterloo.joos.parser.ParseTree.Node;
 import ca.uwaterloo.joos.parser.ParseTree.TreeNode;
 
 public class InterfaceDeclaration extends TypeDeclaration {
 	
 	public InterfaceDeclaration(Node node, ASTNode parent) throws Exception {
 		super(node, parent);
 	}
 	@Override
 	public List<Node> processTreeNode(TreeNode treeNode) throws Exception {
		if (treeNode.productionRule.getLefthand().equals("extendsinterfaces")&&(treeNode.children.size()!=0)) {
 			ReferenceType implemntsType = new ReferenceType(treeNode, this);
 			addChild(IMPLEMNTS, implemntsType);
 		}
 		else {
 			return super.processTreeNode(treeNode);
 		}
 		return null;
 	}
 }
 
