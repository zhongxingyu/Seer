 package uk.ac.ed.inf.graph.compound.newimpl;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.ed.inf.graph.compound.CompoundNodePair;
 import uk.ac.ed.inf.graph.compound.IChildCompoundGraph;
 import uk.ac.ed.inf.graph.compound.ICompoundChildEdgeFactory;
 import uk.ac.ed.inf.graph.compound.ICompoundEdge;
 import uk.ac.ed.inf.graph.compound.ICompoundGraph;
 import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
 import uk.ac.ed.inf.graph.compound.ICompoundGraphMoveBuilder;
 import uk.ac.ed.inf.graph.compound.ICompoundNode;
 import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
 import uk.ac.ed.inf.graph.compound.IElementAttributeFactory;
 import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction;
 import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;
 import uk.ac.ed.inf.graph.compound.ISubCompoundGraphFactory;
 
 public class CompoundGraphMoveBuilder implements ICompoundGraphMoveBuilder {
 	private final Logger logger = Logger.getLogger(this.getClass());
 	private ISubCompoundGraph sourceSubCigraph;
 	private final IChildCompoundGraph destChildGraph;
 	private ISubCompoundGraphFactory movedDestnElementsSubgraphFactory;
 	private final Map<ICompoundGraphElement, ICompoundGraphElement> oldNewEquivList;
 	private ISubCompoundGraphFactory removalSubGraphFactory;
 
 	public CompoundGraphMoveBuilder(IChildCompoundGraph destn){
 		this.oldNewEquivList = new HashMap<ICompoundGraphElement, ICompoundGraphElement>();
 		this.destChildGraph = destn;
 		this.movedDestnElementsSubgraphFactory = this.destChildGraph.getSuperGraph().subgraphFactory();
 		this.removalSubGraphFactory = this.destChildGraph.getSuperGraph().subgraphFactory();
 	}
 	
 	@Override
 	public ISubCompoundGraph getMovedComponents() {
 		return this.movedDestnElementsSubgraphFactory.createSubgraph();
 	}
 
 	@Override
 	public IChildCompoundGraph getDestinationChildGraph() {
 		return this.destChildGraph;
 	}
 
 	@Override
 	public ISubCompoundGraph getSourceSubgraph() {
 		return this.sourceSubCigraph;
 	}
 
 	@Override
 	public boolean canMoveHere() {
 		ISubCompoundGraph subGraph = this.sourceSubCigraph;
 		boolean retVal = subGraph != null
 //			&& this.elementAttributeFactory != null
 			&& subGraph.getSuperGraph().equals(this.destChildGraph.getSuperGraph())
 			&& subGraph.isInducedSubgraph()
 			&& subGraph.isConsistentSnapShot()
 			&& !subGraph.containsRoot();
 		if(retVal){
 			// check is destination is a child of any of the top nodes
 			// and that at least one of the top nodes will be moved to a new child graph
 			// and that the top nodes attributes are permitted to be moved.
 //			this.elementAttributeFactory.setDestinationAttribute(this.destChildGraph.getRoot().getAttribute());
 			boolean isDestnChildOfTopNode = false;
 			int numTopElsWithDifferntParents = subGraph.numTopElements();
 			Iterator<ICompoundGraphElement> topNodeIter = subGraph.topElementIterator();
 			while(topNodeIter.hasNext() && !isDestnChildOfTopNode){
 				ICompoundGraphElement topNode = topNodeIter.next();
 				ICompoundGraphElement destn = this.destChildGraph.getRoot();
 				if(topNode.getParent().equals(destn)){
 					numTopElsWithDifferntParents--;
 				}
				isDestnChildOfTopNode = topNode.isChild(destn);
 				IElementAttributeFactory elementAttributeFactory = topNode.getAttribute().elementAttributeMoveFactory();
 //				this.elementAttributeFactory.setElementToMove(topNode.getAttribute());
 				elementAttributeFactory.setDestinationAttribute(this.destChildGraph.getRoot().getAttribute());
 				retVal = elementAttributeFactory.canCreateAttribute();
 			}
 			retVal = !isDestnChildOfTopNode && numTopElsWithDifferntParents > 0;
 		}
 		if(retVal){
 			// now check if has any incident nodes in the subgraph as these will violate the compound graph structure
 			Iterator<ICompoundNode> incidentNodeIterator = ((ICompoundNode)this.destChildGraph.getRoot()).connectedNodeIterator();
 			while(incidentNodeIterator.hasNext() && retVal){
 				ICompoundNode incidentNode = incidentNodeIterator.next();
 				retVal = !subGraph.containsNode(incidentNode);
 			}
 		}
 		return retVal;
 	}
 
 	@Override
 	public void makeMove() {
 		this.oldNewEquivList.clear();
 		this.movedDestnElementsSubgraphFactory = this.destChildGraph.getSuperGraph().subgraphFactory();
 		moveElements();
 		// avoid holding onto additional unneeded memory
 		this.oldNewEquivList.clear();
 		ICompoundGraph graph = this.destChildGraph.getSuperGraph();
 		graph.notifyGraphStructureChange(new IGraphStructureChangeAction(){
 
 			@Override
 			public GraphStructureChangeType getChangeType() {
 				return GraphStructureChangeType.SUBGRAPH_MOVED;
 			}
 
 			@Override
 			public ISubCompoundGraph originalSubgraph() {
 				return removalSubGraphFactory.createSubgraph();
 			}
 
 			@Override
 			public ISubCompoundGraph changedSubgraph() {
 				return movedDestnElementsSubgraphFactory.createSubgraph();
 			}
 		});
 	}
 
 	private void moveElements(){
 		initRootNodeEquivalent(); 
 		Iterator<ICompoundGraphElement> sourceNodeIter = this.sourceSubCigraph.edgeLastElementIterator();
 		while(sourceNodeIter.hasNext()){
 			ICompoundGraphElement srcElement = sourceNodeIter.next();
 			ICompoundGraphElement newElement = null;
 			ICompoundGraphElement equivParent = this.oldNewEquivList.get(srcElement.getParent());
 			if(srcElement instanceof ICompoundNode){
 				if(logger.isTraceEnabled()){
 					logger.trace("Creating node src=" + srcElement + ", parent=" + equivParent);
 				}
 				if(!srcElement.getParent().equals(equivParent)){
 					newElement = moveNode((ICompoundNode)srcElement, equivParent);
 				}
 				else{
 					newElement = srcElement;
 				}
 			}
 			else if(srcElement instanceof ICompoundEdge){
 				ICompoundEdge srcEdge = (ICompoundEdge)srcElement;
 				CompoundNodePair srcNodePair = srcEdge.getConnectedNodes();
 				ICompoundNode equivOutNode = (ICompoundNode)this.oldNewEquivList.get(srcNodePair.getOutNode());
 				ICompoundNode equivInNode = (ICompoundNode)this.oldNewEquivList.get(srcNodePair.getInNode());
 				if(logger.isTraceEnabled()){
 					logger.trace("Creating edge src=" + srcElement + ", parent=" + equivParent +", outNode=" + equivOutNode + ", inNode=" + equivInNode);
 				}
 				// test if edge connections and parent are unchanged. If so leave edge alone. otherwise create a new edge
 				if(!(equivInNode.equals(srcNodePair.getInNode()) && equivOutNode.equals(srcNodePair.getOutNode()) && equivParent.equals(srcElement.getParent()))){
 					newElement = moveEdge((ICompoundEdge)srcElement, equivParent, equivOutNode, equivInNode);
 				}
 				else{
 					newElement = srcElement;
 				}
 			}
 			else{
 				throw new RuntimeException("Unrecognised element type");
 			}
 			if(logger.isTraceEnabled()){
 				logger.trace("Moving src=" + srcElement + ", tgt=" + newElement);
 			}
 			this.oldNewEquivList.put(srcElement, newElement);
 			this.movedDestnElementsSubgraphFactory.addElement(newElement);
 		}
 	}
 
 	private void initRootNodeEquivalent() {
 		Iterator<ICompoundGraphElement> topElements = this.sourceSubCigraph.topElementIterator();
 		while(topElements.hasNext()){
 			ICompoundGraphElement topElement = topElements.next();
 			ICompoundGraphElement topParent = topElement.getParent();
 			ICompoundGraphElement destElement = this.destChildGraph.getRoot(); 
 			if(logger.isTraceEnabled()){
 				logger.trace("topElement=" + topElement + ", topParent=" + topParent +", destRoot=" + destElement);
 			}
 			this.oldNewEquivList.put(topParent, destElement);
 		}
 		
 	}
 
 	private ICompoundEdge moveEdge(ICompoundEdge srcEdge, ICompoundGraphElement parent, ICompoundNode outNode, ICompoundNode inNode) {
 		ICompoundEdge retVal = srcEdge;
 		ICompoundChildEdgeFactory edgefact = parent.getChildCompoundGraph().edgeFactory();
 		edgefact.setPair(new CompoundNodePair(outNode, inNode));
 		IElementAttributeFactory elementAttributeFactory = srcEdge.getAttribute().elementAttributeMoveFactory();
 		elementAttributeFactory.setDestinationAttribute(parent.getAttribute());
 		elementAttributeFactory.setInAttribute(inNode.getAttribute());
 		elementAttributeFactory.setOutAttribute(outNode.getAttribute());
 //		this.elementAttributeFactory.setElementToMove(srcEdge.getAttribute());
 		edgefact.setAttributeFactory(elementAttributeFactory);
 		retVal = edgefact.createEdge();
 		srcEdge.markRemoved(true);
 		this.removalSubGraphFactory.addElement(srcEdge);
 		return retVal;
 	}
 
 	private ICompoundNode moveNode(ICompoundNode srcNode, ICompoundGraphElement destParentNode){
 		ICompoundNode newNode = srcNode; 
 		ICompoundNodeFactory fact = destParentNode.getChildCompoundGraph().nodeFactory();
 		IElementAttributeFactory elementAttributeFactory = srcNode.getAttribute().elementAttributeMoveFactory();
 		elementAttributeFactory.setDestinationAttribute(destParentNode.getAttribute());
 //		elementAttributeFactory.setElementToMove(srcNode.getAttribute());
 		fact.setAttributeFactory(elementAttributeFactory);
 		newNode = fact.createNode();
 		srcNode.markRemoved(true);
 		this.removalSubGraphFactory.addElement(srcNode);
 		return newNode;
 	}
 
 	@Override
 	public void setSourceSubgraph(ISubCompoundGraph sourceSubCompoundGraph) {
 		this.sourceSubCigraph = sourceSubCompoundGraph;
 		if(sourceSubCompoundGraph != null){
 			this.removalSubGraphFactory = this.sourceSubCigraph.getSuperGraph().subgraphFactory();
 		}
 		else{
 			this.removalSubGraphFactory = this.destChildGraph.getSuperGraph().subgraphFactory();
 		}
 	}
 
 	@Override
 	public ISubCompoundGraph getRemovedComponents() {
 		return this.removalSubGraphFactory.createSubgraph();
 	}
 
 //	@Override
 //	public void setElementAttributeFactory(IElementAttributeMoveFactory elementAttributeFactory) {
 //		this.elementAttributeFactory = elementAttributeFactory;
 //	}
 //
 //	@Override
 //	public IElementAttributeMoveFactory getElementAttributeFactory() {
 //		return this.elementAttributeFactory;
 //	}
 
 }
