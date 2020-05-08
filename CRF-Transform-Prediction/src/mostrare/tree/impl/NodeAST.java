package mostrare.tree.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import mostrare.coltAdaptation.list.NodeArrayList;
import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.Node;
import mostrare.tree.Tree;

public class NodeAST implements Node {

	/**
	 * index of the node in the relative tree
	 */
	protected int index;

	protected Node parent;

	protected String nodetype;

	protected String nodelabel;
	
	protected String expressionidentity;
	
	protected String logicalexpressionindentity;
	
	protected String binaryoperatorindentity;
	
	protected String typeofreturn;
	
	protected String nameofnode;

	/**
	 * index of the annotation
	 */
	protected int annotationIndex;

	/**
	 * tree to which the node belongs
	 */
	public Tree tree;

	/**
	 * contains contraints infos about the node
	 */

	public int orderedNodesNumber;

	/**
	 * list of ordered children
	 */
	public NodeArrayList orderedChildrenNodes;

	public CRFWithConstraintNode studiedCRF;

	public Map<Integer, Boolean> charactervaluemap = new HashMap<Integer, Boolean>();

	protected Node nextSibling;

	protected Node previousSibling;

	/**
	 * position of this node regarding its siblings
	 */
	protected int position;

	protected int characterindex;

	public Node firstChild;

	public JSONObject jsonnode;

	public NodeAST(int indexintree, String typeofnode, CRFWithConstraintNode crf) {
		this.index = indexintree;
		this.nodetype = typeofnode;
		this.studiedCRF = crf;
		init();
	}

	@Override
	public void init() {
		this.parent = null;
		this.expressionidentity = "";
		this.logicalexpressionindentity = "";
		this.binaryoperatorindentity = "";
		this.typeofreturn="";
		this.nameofnode = "";
		this.nodelabel = "";
		this.annotationIndex = 0;
		this.tree = null;
		this.orderedNodesNumber = 0;
		this.orderedChildrenNodes = new NodeArrayList();
		this.nextSibling = null;
		this.previousSibling = null;
		this.position = 0;
		this.characterindex = 0;
		this.firstChild = null;

		for (String value : this.studiedCRF.characters.getCharacterIntegerMap().keySet())
			charactervaluemap.put(this.studiedCRF.characters.getCharacterIntegerMap().get(value), false);
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public Node getParentNode() {
		return parent;
	}

	@Override
	public void setParentNode(Node parent) {
		this.parent = parent;
	}

	@Override
	public int getAnnotation() {
		return annotationIndex;
	}

	@Override
	public void setAnnotation(int annotation) {
		annotationIndex = annotation;
	}

	@Override
	public String getNodeType() {
		return nodetype;
	}

	@Override
	public void setNodeType(String typeinfo) {
		this.nodetype = typeinfo;
	}

	@Override
	public String getNodeLabel() {
		return nodelabel;
	}

	@Override
	public void setNodeLabel(String label) {
		this.nodelabel = label;
	}
	
	@Override
	public void setNameofNode (String NodeName) {
		this.nameofnode = NodeName;
	}
	
	@Override
	public String getNameofNode() {
		return this.nameofnode;
	}
	
	@Override
	public void setExpressionIdentity(String expID) {
		this.expressionidentity = expID;
	}
	
	@Override
	public String getExpressionIdentity() {
		return this.expressionidentity;
	}
	
	@Override
	public void setLogicalExpressionIdentity(String logexpID) {
		this.logicalexpressionindentity = logexpID;
	}
	
	@Override
	public String getLogicalExpressionIdentity() {
		return this.logicalexpressionindentity;
	}
	
	@Override
	public void setBinaryOperatorIdentity(String binoperatorID) {
		this.binaryoperatorindentity = binoperatorID;
	}
	
	@Override
	public String getBinaryOperatorIdentity() {
		return this.binaryoperatorindentity;
	}

	@Override
	public void setTypeofReturn(String returntype) {
		this.typeofreturn = returntype;
	}
	
	@Override
	public String getTypeofReturn() {
		return this.typeofreturn;
	}
	
	@Override
	public Tree getTree() {
		return tree;
	}

	@Override
	public void setTree(Tree tree) {
		this.tree = tree;
	}

	@Override
	public int getOrderedNodesNumber() {
		return this.orderedNodesNumber;
	}

	@Override
	public void setOrderedNodesNumber(int number) {
		this.orderedNodesNumber = number;
	}

	@Override
	public NodeArrayList getOrderedChildrenNodes() {
		return this.orderedChildrenNodes;
	}

	@Override
	public void setOrderedorderedChildrenNodes(NodeArrayList ChildrenNodes) {
		this.orderedChildrenNodes = ChildrenNodes;
	}

	@Override
	public Node getOrderedNodeAt(int index) {
		return orderedChildrenNodes.getQuick(index);
	}

	public void addNode(Node child) {
		orderedChildrenNodes.add(child);
	}

	@Override
	public CRF getStudiedCRF() {
		return this.studiedCRF;
	}

	@Override
	public void setStudiedCRF(CRFWithConstraintNode CRFtostudy) {
		this.studiedCRF = CRFtostudy;
	}

	@Override
	public Map<Integer, Boolean> getCharacterValueMap() {
		return this.charactervaluemap;
	}

	@Override
	public void setCharacterValueMap(Map<Integer, Boolean> studiedmap) {
		this.charactervaluemap = studiedmap;
	}

	@Override
	public Node getNextSibling() {
		return nextSibling;
	}

	@Override
	public void setNextSibling(Node nextSibling) {
		this.nextSibling = nextSibling;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public Node getPreviousSibling() {
		return previousSibling;
	}

	@Override
	public void setPreviousSibling(Node previousSibling) {
		this.previousSibling = previousSibling;
	}

	@Override
	public Node getFirstChild() {
		return firstChild;
	}

	@Override
	public void setFirstChild(Node firstChild) {
		this.firstChild = firstChild;
	}

//	public boolean checkNodeCharacter(String Character) {
//		if((this.nodetype.equals("ConstructorCall")||this.nodetype.equals("Invocation"))
//				&& Character.startsWith("M"))
//			return true;
//		else if((this.nodetype.equals("VariableRead")||this.nodetype.equals("VariableWrite")||
//				this.nodetype.equals("FieldRead")||this.nodetype.equals("FieldWrite"))
//				&& Character.startsWith("V"))
//			return true;
//		else if((this.nodetype.equals("Literal")||this.nodetype.equals("TypeAccess"))&&
//				 Character.startsWith("C")) 
//			return true;
//		else if(this.nodetype.equals("VIRTUAL ROOT")&& Character.startsWith("S"))
//			return true;
//		else if(this.nodetype.equals("Root Logical")&& Character.startsWith("LE"))
//			return true;
//		else return false;
//  } 

	@Override
	public int getCharacterIndex() {
		return this.characterindex;
	}

	@Override
	public void setCharacterIndex(int indexofcharacter) {
		this.characterindex = indexofcharacter;
	}

	@Override
	public boolean getCharacterValue(int indexofcharacter) {
		return charactervaluemap.get(indexofcharacter);
	}

}
