package FileParse;

import java.util.Map;

import mostrare.coltAdaptation.list.NodeArrayList;
import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.Node;
import mostrare.tree.Tree;

public class NodeAST implements Node {
     
	/**
	 * index of the node in the relative tree
	 */
	protected int				index;

	protected Node			    parent; 
	
	protected String            nodetype;
	
	protected String            nodelabel;

	protected String            nameofnode;

	protected String            typeofreturn;
	
	protected String            logicalexpressionindentity;

	/**
	 * index of the annotation
	 */
	protected int				annotationIndex;

	/**
	 * tree to which the node belongs
	 */
	public Tree				tree;

	/**
	 * contains contraints infos about the node
	 */

	public int				orderedNodesNumber;

	/**
	 * list of ordered children
	 */
	public NodeArrayList		orderedChildrenNodes;
	
	public NodeAST (int indexintree, int indexofannotation, String typeofnode)
	{
		this.index = indexintree;
		this.annotationIndex = indexofannotation;
		this.nodetype=typeofnode;
		orderedChildrenNodes = new NodeArrayList();
	}
	
	@Override
	public void init() {}

	@Override
	public int getAnnotation()
	{
		return annotationIndex;
	}

	@Override
	public void setAnnotation(int annotation)
	{
		annotationIndex = annotation;
	}

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public Node getParentNode()
	{
		return parent;
	}

	@Override
	public void setParentNode(Node parent)
	{
		this.parent = parent;
	}

	@Override
	public int getOrderedNodesNumber()
	{
		return orderedChildrenNodes.size();
	}
	
	@Override
	public void setOrderedNodesNumber(int number)
	{
		this.orderedNodesNumber=number;
	}

	@Override
	public Tree getTree()
	{
		return tree;
	}

	@Override
	public void setTree(Tree tree)
	{
		this.tree = tree;
	}

	@Override
	public Node getOrderedNodeAt(int index)
	{
		return orderedChildrenNodes.getQuick(index);
	}
	
	public void addNode(Node child)
	{
		orderedChildrenNodes.add(child);
	}

	public String getNodeLabel()
	{
		return nodelabel;
	}
	
	public void setNodeLabel(String label)
	{
		this.nodelabel=label;
	}
	
	public String getNodeType()
	{
		return nodetype;
	}
	
	public void setNodeType(String typeinfo)
	{
		this.nodetype=typeinfo;
	}
	
	protected Node	nextSibling;

	protected Node	previousSibling;

	/**
	 * position of this node regarding its siblings
	 */
	protected int	position;

	@Override
	public Node getNextSibling()
	{
		return nextSibling;
	}

	@Override
	public void setNextSibling(Node nextSibling)
	{
		this.nextSibling = nextSibling;
	}

	@Override
	public int getPosition()
	{
		return position;
	}

	@Override
	public void setPosition(int position)
	{
		this.position = position;
	}

	@Override
	public Node getPreviousSibling()
	{
		return previousSibling;
	}

	@Override
	public void setPreviousSibling(Node previousSibling)
	{
		this.previousSibling = previousSibling;
	}
	
	public Node	firstChild;

	@Override
	public Node getFirstChild()
	{
		return firstChild;
	}

	@Override
	public void setFirstChild(Node firstChild)
	{
		this.firstChild = firstChild;
	}

	public boolean isAnnotationAllowedForNode(int annotationindex) {
		return true;
	}
	
	public boolean isNodeAnnotable(int annotationindex) {
		return false;
	}

    public int getCharacterIndex() {
    	return 1;
    }
	
	public boolean getCharacterValue() {
		return false;
	}
	
	public boolean isAnnotationAllowedForEdgeChild(int parentAnnotationindex,
			int childAnnotationindex) {
		return true;
	}
	
	public boolean isTriangleAnnotable(int parentAnnotationindex,
			int leftChildAnnotationindex, int rightChildAnnotationindex) {
		return true;
	}

	@Override
	public void setCharacterIndex(int indexofcharacter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getCharacterValue(int indexofcharacter) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public NodeArrayList getOrderedChildrenNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOrderedorderedChildrenNodes(NodeArrayList ChildrenNodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CRF getStudiedCRF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStudiedCRF(CRFWithConstraintNode CRFtostudy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<Integer, Boolean> getCharacterValueMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharacterValueMap(Map<Integer, Boolean> studiedmap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExpressionIdentity(String expID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getExpressionIdentity() {
		// TODO Auto-generated method stub
		return null;
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
	public void setNameofNode (String NodeName) {
		this.nameofnode = NodeName;
	}
	
	@Override
	public String getNameofNode() {
		return this.nameofnode;
	}

	@Override
	public void setBinaryOperatorIdentity(String binoperatorID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBinaryOperatorIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTypeofReturn(String returntype) {
		this.typeofreturn = returntype;
	}

	@Override
	public String getTypeofReturn() {
		return this.typeofreturn;
	}
}
