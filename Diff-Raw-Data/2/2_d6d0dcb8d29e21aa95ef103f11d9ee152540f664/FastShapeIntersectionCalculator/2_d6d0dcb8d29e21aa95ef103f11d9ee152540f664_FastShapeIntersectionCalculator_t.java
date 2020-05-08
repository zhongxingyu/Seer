 /**
  * 
  */
 package org.pathwayeditor.visualeditor.geometry;
 
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 import org.pathwayeditor.figure.geometry.Envelope;
 import org.pathwayeditor.figure.geometry.IConvexHull;
 import org.pathwayeditor.figure.geometry.Point;
 import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
 import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveControllerEvent;
 import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveControllerListener;
 import org.pathwayeditor.visualeditor.controller.IRootController;
 import org.pathwayeditor.visualeditor.controller.IViewControllerChangeListener;
 import org.pathwayeditor.visualeditor.controller.IViewControllerNodeStructureChangeEvent;
 import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
 import org.pathwayeditor.visualeditor.controller.IViewControllerNodeStructureChangeEvent.ViewControllerStructureChangeType;
 
 /**
  * @author smoodie
  *
  */
 public class FastShapeIntersectionCalculator implements IIntersectionCalculator {
 	private final Logger logger = Logger.getLogger(this.getClass());
 	
 	private static final IIntersectionCalcnFilter DEFAULT_FILTER = new IIntersectionCalcnFilter(){
 		public boolean accept(IDrawingPrimitiveController node) {
 			return true;
 		}
 	};
 	
 	private static final Comparator<IDrawingPrimitiveController> DEFAULT_COMPARATOR = new Comparator<IDrawingPrimitiveController>(){
 
 		public int compare(IDrawingPrimitiveController o1, IDrawingPrimitiveController o2) {
 			int retVal = 0;
 			if(o1.getDrawingElement().getCurrentDrawingElement().getLevel() < o2.getDrawingElement().getCurrentDrawingElement().getLevel()){
 				retVal = 1;
 			}
 			else if(o1.getDrawingElement().getCurrentDrawingElement().getLevel() > o2.getDrawingElement().getCurrentDrawingElement().getLevel()){
 				retVal = -1;
 			}
 			else{
 				long o1Idx = o1.getDrawingElement().getCurrentDrawingElement().getUniqueIndex();
 				long o2Idx = o2.getDrawingElement().getCurrentDrawingElement().getUniqueIndex();
 				retVal = o1Idx < o2Idx ? 1 : (o1Idx > o2Idx ? -1 : 0); 
 			}
 			return retVal;
 		}
 		
 	};
 	
 	private final IMutableSpacialIndex2D<IDrawingPrimitiveController> spacialIndex;
 	private final IViewControllerStore model;
 	private IIntersectionCalcnFilter filter;
 	private Comparator<IDrawingPrimitiveController> comparator = null;
 	private IDrawingPrimitiveControllerListener primitiveControllerChangeListener;
 	private IViewControllerChangeListener viewControllerChangeListener;
 	
 	public FastShapeIntersectionCalculator(IViewControllerStore model){
 		this.model = model;
 		this.filter = DEFAULT_FILTER;
 		this.spacialIndex = new RTree<IDrawingPrimitiveController>();
 		this.primitiveControllerChangeListener = new IDrawingPrimitiveControllerListener() {
 			@Override
 			public void drawnBoundsChanged(IDrawingPrimitiveControllerEvent e) {
 				spacialIndex.delete(e.getController());
 				Envelope drawnBounds = e.getController().getDrawnBounds();
 				spacialIndex.insert(e.getController(), drawnBounds);
 				if(logger.isTraceEnabled()){
 					logger.trace("FastShapeIntersectionCalc: bounds changed for contoller=" + e.getController() + ", bounds=" + drawnBounds);
 				}
 			}
 		};
 		this.viewControllerChangeListener = new IViewControllerChangeListener() {
 			
 			@Override
 			public void nodeStructureChangeEvent(IViewControllerNodeStructureChangeEvent e) {
 				if(e.getChangeType().equals(ViewControllerStructureChangeType.NODE_ADDED)
 						|| e.getChangeType().equals(ViewControllerStructureChangeType.LINK_ADDED)){
 					IDrawingPrimitiveController cont = e.getChangedElement();
 					cont.addDrawingPrimitiveControllerListener(primitiveControllerChangeListener);
 					Envelope drawnBounds = cont.getDrawnBounds();
 					spacialIndex.insert(cont, drawnBounds);
 					if(logger.isTraceEnabled()){
 						logger.trace("FastShapeIntersectionCalc: inserted to RTree: contoller=" + cont + ",bound=" + drawnBounds);
 					}
 				}
 				else if(e.getChangeType().equals(ViewControllerStructureChangeType.NODE_REMOVED)
 						|| e.getChangeType().equals(ViewControllerStructureChangeType.LINK_REMOVED)){
 					IDrawingPrimitiveController cont = e.getChangedElement();
 					cont.removeDrawingPrimitiveControllerListener(primitiveControllerChangeListener);
 					spacialIndex.delete(cont);
 					if(logger.isTraceEnabled()){
 						logger.trace("FastShapeIntersectionCalc: deleted from RTree: contoller=" + cont);
 					}
 				}
 			}
 		};
 		buildFromViewController();
 		this.model.addViewControllerChangeListener(viewControllerChangeListener);
 	}
 	
 	private void buildFromViewController() {
 		Iterator<IDrawingPrimitiveController> nodeIter = model.drawingPrimitiveIterator();
 		while(nodeIter.hasNext()){
 			IDrawingPrimitiveController node = nodeIter.next();
 			if(!(node instanceof IRootController)){
 				// ignore root
 				Envelope drawnBounds = node.getDrawnBounds();
 				Point origin = drawnBounds.getOrigin();
 				Point diagonal = drawnBounds.getDiagonalCorner();
 				this.spacialIndex.insert(node, (float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY());
 				if(logger.isTraceEnabled()){
 					logger.trace("Inserted element=" + node + " into RTree with extent=" + drawnBounds);
 				}
 				node.addDrawingPrimitiveControllerListener(this.primitiveControllerChangeListener);
 			}
 		}
 	}
 
 	@Override
 	public void setComparator(Comparator<IDrawingPrimitiveController> comparator){
 		this.comparator = comparator;
 	}
 	
 	
 	@Override
 	public IViewControllerStore getModel(){
 		return this.model;
 	}
 	
 	@Override
 	public void setFilter(IIntersectionCalcnFilter filter){
 		if(filter == null){
 			this.filter = DEFAULT_FILTER;
 		}
 		else{
 			this.filter = filter;
 		}
 	}
 	
 	private SortedSet<IDrawingPrimitiveController> createSortedSet(){
 		SortedSet<IDrawingPrimitiveController> retVal = null;
 		if(this.comparator != null){
 			retVal = new TreeSet<IDrawingPrimitiveController>(this.comparator);
 		}
 		else{
 			retVal = new TreeSet<IDrawingPrimitiveController>(DEFAULT_COMPARATOR);
 		}
 		return retVal;
 	}
 	
 	@Override
 	public SortedSet<IDrawingPrimitiveController> findIntersectingNodes(IConvexHull queryHull, IDrawingPrimitiveController queryNode){
 		SortedSet<IDrawingPrimitiveController> retVal = createSortedSet();
 		// the root node will always intersect - that's a give so add it in and exclude it from
 		// intersection tests
 		IRootController rootNode = model.getRootNode();
 		if(filter.accept(rootNode)){
 			retVal.add(rootNode);
 		}
		Envelope drawnBounds = queryHull.getEnvelope();
 		Point origin = drawnBounds.getOrigin();
 		Point diagonal = drawnBounds.getDiagonalCorner();
 		ISpacialEntry2DEnumerator< IDrawingPrimitiveController> iter = this.spacialIndex.queryOverlap((float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY(), null, 0, false);
 		while(iter.numRemaining() > 0){
 			IDrawingPrimitiveController node = iter.nextInt();
 			// ignore matches to self
 			if(!node.equals(queryNode) && !node.equals(rootNode) && filter.accept(node) && node.intersectsHull(queryHull)){
 				retVal.add(node);
 			}
 		}
 		return retVal;
 	}
 
 	@Override
 	public SortedSet<IDrawingPrimitiveController> findDrawingPrimitivesAt(Point p) {
 		SortedSet<IDrawingPrimitiveController> retVal = createSortedSet();
 		Point origin = p;
 		Point diagonal = p;
 		ISpacialEntry2DEnumerator<IDrawingPrimitiveController> iter = this.spacialIndex.queryOverlap((float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY(), null, 0, false);
 		while(iter.numRemaining() > 0){
 			IDrawingPrimitiveController node = iter.nextInt();
 			if(logger.isTraceEnabled()){
 				logger.trace("RTree found overlapping node: " + node +",bound=" + node.getDrawnBounds());
 			}
 			if(filter.accept(node) && node.containsPoint(p)){
 				logger.trace("Found containing node");
 				retVal.add(node);
 			}
 		}
 		return retVal;
 	}
 }
