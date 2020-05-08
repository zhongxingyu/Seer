 package org.pathwayeditor.visualeditor.controller;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
 import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttributeVisitor;
 import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
 import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
 import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
 import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
 import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
 import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
 import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
 import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
 import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
 import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
 import org.pathwayeditor.businessobjects.impl.facades.DrawingElementSelectionFacade;
 import org.pathwayeditor.businessobjects.impl.facades.DrawingNodeFacade;
 import org.pathwayeditor.businessobjects.impl.facades.LabelNodeFacade;
 import org.pathwayeditor.businessobjects.impl.facades.LinkEdgeFacade;
 import org.pathwayeditor.businessobjects.impl.facades.RootNodeFacade;
 import org.pathwayeditor.businessobjects.impl.facades.ShapeNodeFacade;
 import org.pathwayeditor.figure.geometry.Envelope;
 import org.pathwayeditor.figure.geometry.Point;
 import org.pathwayeditor.visualeditor.geometry.FastShapeIntersectionCalculator;
 import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
 import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
 
 import uk.ac.ed.inf.graph.compound.ICompoundEdge;
 import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
 import uk.ac.ed.inf.graph.compound.ICompoundNode;
 import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction;
 import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction.GraphStructureChangeType;
 import uk.ac.ed.inf.graph.compound.IGraphStructureChangeListener;
 
 public class ViewControllerStore implements IViewControllerModel {
 //	private final Logger logger = Logger.getLogger(this.getClass());
 	private final IModel domainModel;
 	private final SortedMap<ICompoundGraphElement, IDrawingElementController> domainToViewMap;
 	private final SortedSet<IDrawingElementController> drawingPrimitives;
 	private IGraphStructureChangeListener modelListener;
 	private IRootController rootPrimitive;
 	private boolean isActive = false;
 	private final List<IViewControllerChangeListener> listeners;
 	private int indexCounter = 0;
 	private final IIntersectionCalculator intersectionCalculator;
 	
 	public ViewControllerStore(IModel domainModel){
 		this.domainModel = domainModel;
 		this.domainToViewMap = new TreeMap<ICompoundGraphElement, IDrawingElementController>(new Comparator<ICompoundGraphElement>(){
 
 			@Override
 			public int compare(ICompoundGraphElement o1, ICompoundGraphElement o2) {
 //				return Integer.valueOf(o1.getAttribute().getCreationSerial()).compareTo(Integer.valueOf(o2.getAttribute().getCreationSerial()));
 //				return o1.compareTo(o2);
 				int retVal = o1.getLevel() < o2.getLevel() ? -1 : (o1.getLevel() > o2.getLevel() ? 1 : 0);
 				if(retVal == 0){
 					retVal = o1.getIndex() < o2.getIndex() ? -1 : (o1.getIndex() > o2.getIndex() ? 1 : 0);
 				}
 //				if(logger.isTraceEnabled()){
 //					StringBuilder buf = new StringBuilder("retVal=");
 //					buf.append(retVal);
 //					buf.append(",o1Lvl=");
 //					buf.append(o1.getLevel());
 //					buf.append(",o1Uid=");
 //					buf.append(o1.getUniqueIndex());
 //					buf.append(",o2Lvl=");
 //					buf.append(o2.getLevel());
 //					buf.append(",o2Uid=");
 //					buf.append(o2.getUniqueIndex());
 //					buf.append(",o1=");
 //					buf.append(o1);
 //					buf.append(",o2=");
 //					buf.append(o2);
 //					logger.trace(buf.toString());
 //				}
 				return retVal;
 			}
 			
 		});
 		this.drawingPrimitives = new TreeSet<IDrawingElementController>();
 		this.listeners = new LinkedList<IViewControllerChangeListener>();
 		buildFromDomainModel();
 		this.intersectionCalculator = new FastShapeIntersectionCalculator(this);
 		initialiseDomainListeners();
 		if(this.domainModel.numDrawingElements() != this.domainToViewMap.size()){
 			throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
 		}
 		if(this.domainModel.numDrawingElements() != this.drawingPrimitives.size()){
 			throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
 		}
 	}
 	
 	private void initialiseDomainListeners() {
 		modelListener = new IGraphStructureChangeListener(){
 			@Override
 			public void graphStructureChange(IGraphStructureChangeAction event) {
 				if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_REMOVED)){
 //					rebuildModel();
 					inactivateSelection(new DrawingElementSelectionFacade(event.originalSubgraph()));
 					removeSelection(new DrawingElementSelectionFacade(event.originalSubgraph()));
 				}
 				else if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_MOVED)){
 					// reinitialise the nodes
 					inactivateSelection(new DrawingElementSelectionFacade(event.originalSubgraph()));
 					removeSelection(new DrawingElementSelectionFacade(event.originalSubgraph()));
 					addSelection(new DrawingElementSelectionFacade(event.changedSubgraph()));
 					activateSelection(new DrawingElementSelectionFacade(event.changedSubgraph()));
 				}
 				else if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_COPIED)){
 					addSelection(new DrawingElementSelectionFacade(event.changedSubgraph()));
 					activateSelection(new DrawingElementSelectionFacade(event.changedSubgraph()));
 				}
 				else if(event.getChangeType().equals(GraphStructureChangeType.ELEMENT_ADDED)){
 					addSelection(new DrawingElementSelectionFacade(event.changedSubgraph()));
 					activateSelection(new DrawingElementSelectionFacade(event.changedSubgraph()));
 				}
				if(domainModel.numDrawingElements() != domainToViewMap.size()){
					throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
				}
				if(domainModel.numDrawingElements() != drawingPrimitives.size()){
					throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
				}
 			}
 			
 		};
 		this.domainModel.getGraph().addGraphStructureChangeListener(modelListener);
 	}
 	
 	private void addSelection(IDrawingElementSelection selection){
 		Iterator<ICompoundNode> nodeIter = selection.drawingNodeIterator();
 		while(nodeIter.hasNext()){
 			ICompoundNode node = nodeIter.next();
 			INodeController newNode = createNodePrimitive(node);
 			notifyAddedNode(newNode);
 		}
 		Iterator<ICompoundEdge> linkIter = selection.linkEdgeIterator();
 		while(linkIter.hasNext()){
 			ICompoundEdge link = linkIter.next();
 			ILinkController newLinkCont = createLinkPrimitive(link);
 			notifyAddedLink(newLinkCont);
 		}
 	}
 		
 	private void removeSelection(IDrawingElementSelection selection){
 		removeNodeSelection(selection);
 		removeEdgeSelection(selection);
 	}
 	
 	private void activateSelection(IDrawingElementSelection selection) {
 		Iterator<ICompoundNode> nodeIter = selection.drawingNodeIterator();
 		while(nodeIter.hasNext()){
 			IDrawingNode node = new DrawingNodeFacade(nodeIter.next());
 			INodeController nodePrimitive = getNodeController(node);
 			nodePrimitive.activate();
 		}
 		Iterator<ICompoundEdge> linkIter = selection.linkEdgeIterator();
 		while(linkIter.hasNext()){
 			ILinkEdge link = new LinkEdgeFacade(linkIter.next());
 			ILinkController linkController = getLinkController(link);
 			linkController.activate();
 		}
 	}
 
 	private void inactivateSelection(IDrawingElementSelection selection) {
 		Iterator<ICompoundNode> nodeIter = selection.drawingNodeIterator();
 		while(nodeIter.hasNext()){
 			IDrawingNode node = new DrawingNodeFacade(nodeIter.next());
 			INodeController nodePrimitive = getNodeController(node);
 			nodePrimitive.inactivate();
 		}
 		Iterator<ICompoundEdge> linkIter = selection.linkEdgeIterator();
 		while(linkIter.hasNext()){
 			ILinkEdge link = new LinkEdgeFacade(linkIter.next());
 			ILinkController linkController = getLinkController(link);
 			linkController.inactivate();
 		}
 	}
 
 	private void removeNodeSelection(IDrawingElementSelection selection){
 		Iterator<ICompoundNode> iter = selection.drawingNodeIterator();
 		while(iter.hasNext()){
 			IDrawingNode node = new DrawingNodeFacade(iter.next());
 			INodeController nodeController = getNodeController(node); 
 			notifyRemovedNode(nodeController);
 			removeNodeController(nodeController);
 		}
 	}
 	
 	private void removeEdgeSelection(IDrawingElementSelection selection){
 		Iterator<ICompoundEdge> iter = selection.linkEdgeIterator();
 		while(iter.hasNext()){
 			ILinkEdge link = new LinkEdgeFacade(iter.next());
 			ILinkController linkController = getLinkController(link); 
 			notifyRemovedLink(linkController);
 			removeLinkController(linkController);
 		}
 	}
 	
 	private void removeLinkController(ILinkController linkController) {
 		domainToViewMap.remove(linkController.getDrawingElement().getGraphElement());
 		drawingPrimitives.remove(linkController);
 	}
 
 	private void removeNodeController(INodeController shapeNode){
 		domainToViewMap.remove(shapeNode.getDrawingElement().getGraphElement());
 		drawingPrimitives.remove(shapeNode);
 	}
 
 	private void buildFromDomainModel(){
 		Iterator<ICompoundNode> nodeIter = this.domainModel.drawingNodeIterator();
 		while(nodeIter.hasNext()){
 			ICompoundNode node = nodeIter.next();
 			createNodePrimitive(node);
 		}
 		Iterator<ICompoundEdge> edgeIter = this.domainModel.linkEdgeIterator();
 		while(edgeIter.hasNext()){
 			ICompoundEdge link = edgeIter.next();
 			createLinkPrimitive(link);
 		}
 	}
 	
 	private INodeController createNodePrimitive(final ICompoundNode graphNode){
 		ICanvasElementAttribute node = (ICanvasElementAttribute)graphNode.getAttribute();
 		ElementControllerFactory factory = new ElementControllerFactory(this); 
 		node.visit(factory);
 		INodeController viewNode = factory.getCreatedNodeController(); 
 		if(viewNode != null){
 			this.domainToViewMap.put(viewNode.getDrawingElement().getGraphElement(), viewNode);
 			this.drawingPrimitives.add(viewNode);
 		}
 		return viewNode;
 	}
 	
 	
 	private ILinkController createLinkPrimitive(ICompoundEdge graphLink){
 		ILinkEdge linkEdge = new LinkEdgeFacade(graphLink);
 		ILinkController linkCtlr = new LinkController(this, linkEdge, indexCounter++);
 		this.domainToViewMap.put(linkEdge.getGraphElement(), linkCtlr);
 		this.drawingPrimitives.add(linkCtlr);
 		return linkCtlr;
 	}
 	
 	
 	private void notifyAddedNode(final INodeController viewNode) {
 		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){
 
 			@Override
 			public ViewControllerStructureChangeType getChangeType() {
 				return ViewControllerStructureChangeType.NODE_ADDED;
 			}
 
 			@Override
 			public INodeController getChangedElement() {
 				return viewNode;
 			}
 			
 		};
 		notifyEvent(e);
 	}
 
 	private void notifyRemovedNode(final INodeController viewNode) {
 		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){
 
 			@Override
 			public ViewControllerStructureChangeType getChangeType() {
 				return ViewControllerStructureChangeType.NODE_REMOVED;
 			}
 
 			@Override
 			public INodeController getChangedElement() {
 				return viewNode;
 			}
 			
 		};
 		notifyEvent(e);
 	}
 
 	private void notifyAddedLink(final ILinkController viewNode) {
 		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){
 
 			@Override
 			public ViewControllerStructureChangeType getChangeType() {
 				return ViewControllerStructureChangeType.LINK_ADDED;
 			}
 
 			@Override
 			public ILinkController getChangedElement() {
 				return viewNode;
 			}
 			
 		};
 		notifyEvent(e);
 	}
 
 	private void notifyRemovedLink(final ILinkController viewNode) {
 		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){
 
 			@Override
 			public ViewControllerStructureChangeType getChangeType() {
 				return ViewControllerStructureChangeType.LINK_REMOVED;
 			}
 
 			@Override
 			public ILinkController getChangedElement() {
 				return viewNode;
 			}
 			
 		};
 		notifyEvent(e);
 	}
 
 	private void notifyEvent(IViewControllerNodeStructureChangeEvent e) {
 		for(IViewControllerChangeListener listener : this.listeners){
 			listener.nodeStructureChangeEvent(e);
 		}
 	}
 
 	@Override
 	public Iterator<IDrawingElementController> drawingPrimitiveIterator() {
 		return this.drawingPrimitives.iterator();
 	}
 
 	@Override
 	public IModel getDomainModel() {
 		return this.domainModel;
 	}
 
 	@Override
 	public INodeController getNodeController(IDrawingNode draggedNode) {
 		IDrawingElementController retVal = this.domainToViewMap.get(draggedNode.getGraphElement());
 		if(retVal == null){
 			throw new IllegalArgumentException("domain node is not present in this view model");
 		}
 		return (INodeController)retVal;
 	}
 
 	@Override
 	public IRootController getRootNode() {
 		return this.rootPrimitive;
 	}
 
 	@Override
 	public Iterator<ILabelController> labelControllerIterator() {
 		List<ILabelController> retList = new LinkedList<ILabelController>();
 		for(IDrawingElementController primitive : this.drawingPrimitives){
 			if(primitive instanceof ILabelController){
 				retList.add((ILabelController)primitive);
 			}
 		}
 		return retList.iterator();
 	}
 
 	@Override
 	public Iterator<ILinkController> linkControllerIterator() {
 		List<ILinkController> retList = new LinkedList<ILinkController>();
 		for(IDrawingElementController primitive : this.drawingPrimitives){
 			if(primitive instanceof ILinkController){
 				retList.add((ILinkController)primitive);
 			}
 		}
 		return retList.iterator();
 	}
 
 	@Override
 	public Iterator<INodeController> nodeControllerIterator() {
 		List<INodeController> retList = new LinkedList<INodeController>();
 		for(IDrawingElementController primitive : this.drawingPrimitives){
 			if(primitive instanceof INodeController){
 				retList.add((INodeController)primitive);
 			}
 		}
 		return retList.iterator();
 	}
 
 	@Override
 	public Iterator<IShapeController> shapeControllerIterator() {
 		List<IShapeController> retList = new LinkedList<IShapeController>();
 		for(IDrawingElementController primitive : this.drawingPrimitives){
 			if(primitive instanceof IShapeController){
 				retList.add((IShapeController)primitive);
 			}
 		}
 		return retList.iterator();
 	}
 
 	@Override
 	public boolean containsDrawingElement(IDrawingElement testPrimitive) {
 		return this.domainToViewMap.containsKey(testPrimitive.getGraphElement());
 	}
 
 	@Override
 	public void activate() {
         // now activate all the drawing primitives
 		for(IDrawingElementController prim : this.drawingPrimitives){
         	prim.activate();
         }
 		this.isActive = true;
 	}
 
 	@Override
 	public void deactivate() {
 		for(IDrawingElementController prim : this.drawingPrimitives){
         	prim.inactivate();
         }
 		this.isActive = false;
 	}
 
 	@Override
 	public boolean isActive() {
 		return this.isActive;
 	}
 
 	@Override
 	public ILinkController getLinkController(ILinkEdge attribute) {
 		return (ILinkController)this.domainToViewMap.get(attribute.getGraphElement());
 	}
 
 	@Override
 	public IShapeController getShapeController(IShapeNode attribute) {
 		return (IShapeController)this.domainToViewMap.get(attribute.getGraphElement());
 	}
 
 	@Override
 	public Envelope getCanvasBounds() {
 		double minX = Double.MAX_VALUE;
 		double maxX = Double.MIN_VALUE;
 		double minY = Double.MAX_VALUE;
 		double maxY = Double.MIN_VALUE;
 		Iterator<INodeController> nodeIter = this.nodeControllerIterator();
 		while(nodeIter.hasNext()){
 			INodeController nodeController = nodeIter.next();
 			if(!(nodeController instanceof IRootController)){
 				// ignore the root as this doesn't have real bounds values - we are only intersted in shapes and labels
 				Point nodeOrigin = nodeController.getConvexHull().getEnvelope().getOrigin();
 				minX = Math.min(minX, nodeOrigin.getX());
 				minY = Math.min(minY, nodeOrigin.getY());
 				Point nodeDiagonal = nodeController.getConvexHull().getEnvelope().getDiagonalCorner();
 				maxX = Math.max(maxX, nodeDiagonal.getX());
 				maxY = Math.max(maxY, nodeDiagonal.getY());
 			}
 		}
 		Iterator<ILinkController> edgeIter = this.linkControllerIterator();
 		while(edgeIter.hasNext()){
 			ILinkController linkController = edgeIter.next();
 			ILinkPointDefinition defn = linkController.getLinkDefinition();
 			Iterator<Point> pointIter = defn.pointIterator();
 			while(pointIter.hasNext()){
 				Point linkPoint = pointIter.next();
 				minX = Math.min(minX, linkPoint.getX());
 				minY = Math.min(minY, linkPoint.getY());
 				maxX = Math.max(maxX, linkPoint.getX());
 				maxY = Math.max(maxY, linkPoint.getY());
 			}
 		}
 		return new Envelope(minX, minY, maxX-minX, maxY-minY);
 	}
 
 	@Override
 	public void addViewControllerChangeListener(IViewControllerChangeListener listener) {
 		this.listeners.add(listener);
 	}
 
 	@Override
 	public List<IViewControllerChangeListener> getViewControllerChangeListeners() {
 		return new ArrayList<IViewControllerChangeListener>(this.listeners);
 	}
 
 	@Override
 	public void removeViewControllerChangeListener(IViewControllerChangeListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	@Override
 	public IIntersectionCalculator getIntersectionCalculator() {
 		return this.intersectionCalculator;
 	}
 
 
 	@Override
 	public IDrawingElementController getDrawingPrimitiveController(IDrawingElement testAttribute) {
 		return this.domainToViewMap.get(testAttribute.getGraphElement());
 	}
 	
 	private class ElementControllerFactory implements ICanvasElementAttributeVisitor {
 		private INodeController viewNode = null;
 		private final IViewControllerModel viewController;
 		
 		public ElementControllerFactory(IViewControllerModel viewController){
 			this.viewController = viewController;
 		}
 		
 		public INodeController getCreatedNodeController(){
 			return this.viewNode;
 		}
 		
 		@Override
 		public void visitRoot(IRootAttribute attribute) {
 			this.viewNode = new RootController(viewController, new RootNodeFacade(attribute.getCurrentElement()), indexCounter++);
 			rootPrimitive = (IRootController)this.viewNode;
 		}
 
 		@Override
 		public void visitShape(IShapeAttribute attribute) {
 			viewNode = new ShapeController(viewController, new ShapeNodeFacade(attribute.getCurrentElement()), indexCounter++);
 		}
 
 		@Override
 		public void visitLink(ILinkAttribute attribute) {
 			throw new RuntimeException("node is of unknown type");
 		}
 
 		@Override
 		public void visitLabel(ILabelAttribute attribute) {
 			viewNode = new LabelController(viewController, new LabelNodeFacade(attribute.getCurrentElement()), indexCounter++);
 		}
 		
 	}
 
 	@Override
 	public IDrawingElementController findControllerByAttribute(ICanvasElementAttribute testAttribute) {
 		return this.domainToViewMap.get(testAttribute.getCurrentElement());
 	}
 }
