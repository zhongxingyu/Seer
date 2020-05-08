 package org.pathwayeditor.visualeditor.behaviour;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 
 import org.apache.log4j.Logger;
 import org.pathwayeditor.figure.geometry.Dimension;
 import org.pathwayeditor.figure.geometry.Point;
 import org.pathwayeditor.visualeditor.IShapePane;
 import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse.CursorType;
 import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse.StateType;
 import org.pathwayeditor.visualeditor.controller.INodeController;
 import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
 import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;
 import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
 import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;
 
 public class MouseBehaviourController implements IMouseBehaviourController {
 	private final Logger logger = Logger.getLogger(this.getClass());
 	private MouseListener mouseSelectionListener;
 	private MouseMotionListener mouseMotionListener;
 	private KeyListener keyListener;
 	private final INodeIntersectionCalculator intCalc;
 	private IShapePane shapePane;
 	private Map<SelectionHandleType, IDragResponse> dragResponseMap;
 	private IKeyboardResponse keyboardResponseMap;
 	private Map<SelectionHandleType, IMouseFeedbackResponse> mouseResponseMap;
 	private IDragResponse currDragResponse;
 	private IMouseFeedbackResponse currMouseFeedbackResponse;
 
 	public MouseBehaviourController(IShapePane pane, IEditingOperation moveOp, IResizeOperation resizeOp){
 		this.shapePane = pane;
 		this.dragResponseMap = new HashMap<SelectionHandleType, IDragResponse>();
 		this.mouseResponseMap = new HashMap<SelectionHandleType, IMouseFeedbackResponse>();
 		this.keyboardResponseMap = new KeyboardResponse(moveOp);
 		initialiseDragResponses(moveOp, resizeOp);
 		initialiseMouseResponse();
         intCalc = new ShapeIntersectionCalculator(shapePane.getViewModel());
         this.mouseSelectionListener = new MouseListener(){
 
 			public void mouseClicked(MouseEvent e) {
 				if(e.getButton() == MouseEvent.BUTTON1){
 					if(!e.isShiftDown() && !e.isAltDown()){
 						Point location = getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
 						INodeController nodeController = findDrawingNodeAt(location);
 						if(nodeController != null){
 							shapePane.getSelectionRecord().setPrimarySelection(nodeController);
 						}
 						else{
 							shapePane.getSelectionRecord().clear();
 						}
 					}
 					else if(e.isShiftDown() && !e.isAltDown()){
 						Point location = getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
 						INodeController nodeController = findDrawingNodeAt(location);
 						if(nodeController != null){
 							shapePane.getSelectionRecord().addSecondarySelection(nodeController);
 						}
 					}
 				}
 			}
 
 			public void mouseEntered(MouseEvent e) {
 				
 			}
 
 			public void mouseExited(MouseEvent e) {
 				
 			}
 
 			public void mousePressed(MouseEvent e) {
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				if(currDragResponse != null){
 					currDragResponse.dragFinished();
 					currMouseFeedbackResponse.reset();
 					currDragResponse = null;
 					shapePane.updateView();
 				}
 				Point location = getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
 				setCurrentCursorResponse(location);
 				e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
 			}
         	
         };
         this.mouseMotionListener = new MouseMotionListener(){
 
 			public void mouseDragged(MouseEvent e) {
 				if(e.getButton() == MouseEvent.BUTTON1){
 					Point location = getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
 					if(currDragResponse == null){
 						ISelectionHandle selectionHandle = shapePane.getSelectionRecord().findSelectionModelAt(location);
 						if(selectionHandle != null){
 							currDragResponse = dragResponseMap.get(selectionHandle.getType());
 							currMouseFeedbackResponse = mouseResponseMap.get(selectionHandle.getType());
 						}
 					}
 					if(currDragResponse != null){
 						if(currDragResponse.isDragOngoing()){
 							if(currDragResponse.canContinueDrag(location)){
 								currDragResponse.dragContinuing(location);
 								if(currDragResponse.canReparent()){
 									currMouseFeedbackResponse.changeState(StateType.REPARENTING);
 									logger.trace("Setting hand cursor as reparenting enabled");
 								}
 								else if(currDragResponse.canMove()){
 									logger.trace("Can move, but cannot reparent. Setting to default for current location");
 									currMouseFeedbackResponse.changeState(StateType.DEFAULT);
 								}
 								else{
 									currMouseFeedbackResponse.changeState(StateType.FORBIDDEN);
 									logger.trace("Move is forbidden");
 								}
 							}
 						}
 						else{
 							currDragResponse.dragStarted(location);
 						}
 					}
 				}
 				e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
 			}
 
 			public void mouseMoved(MouseEvent e) {
 				Point location = getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
 				setCurrentCursorResponse(location);
 				e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
 			}
         	
         };
         this.keyListener = new KeyListener(){
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
 					handleKeyPress(CursorType.Right);
 				}
 				else if(e.getKeyCode() == KeyEvent.VK_LEFT){
 					handleKeyPress(CursorType.Left);
 				}
 				else if(e.getKeyCode() == KeyEvent.VK_UP){
 					handleKeyPress(CursorType.Up);
 				}
 				else if(e.getKeyCode() == KeyEvent.VK_DOWN){
 					handleKeyPress(CursorType.Down);
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_RIGHT ||
 						e.getKeyCode() == KeyEvent.VK_LEFT ||
 						e.getKeyCode() == KeyEvent.VK_UP ||
 						e.getKeyCode() == KeyEvent.VK_DOWN){
 					handleKeyRelease();
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 				
 			}
         	
         };
 	}
 	
 
 	private Point getAdjustedMousePosition(double originalMouseX, double originalMouseY){
 		AffineTransform paneTransform = this.shapePane.getLastUsedTransform();
 		Point retVal = null;
 		if(paneTransform == null){
 			retVal = new Point(originalMouseX, originalMouseY);
 		}
 		else{
			retVal = new Point(originalMouseX-paneTransform.getTranslateX(), originalMouseY-paneTransform.getTranslateY()); 
 		}
 		return retVal;  
 	}
 	
 	private void setCurrentCursorResponse(Point location){
 		ISelectionHandle selectionModel = shapePane.getSelectionRecord().findSelectionModelAt(location);
 		SelectionHandleType selectionRegion = selectionModel != null ? selectionModel.getType() : SelectionHandleType.None;
 		currMouseFeedbackResponse = mouseResponseMap.get(selectionRegion);
 	}
 	
 	private void initialiseMouseResponse(){
 		this.mouseResponseMap.put(SelectionHandleType.Central, new MouseFeedbackResponse(SelectionHandleType.Central));
 		this.mouseResponseMap.put(SelectionHandleType.N, new MouseFeedbackResponse(SelectionHandleType.N));
 		this.mouseResponseMap.put(SelectionHandleType.NE, new MouseFeedbackResponse(SelectionHandleType.NE));
 		this.mouseResponseMap.put(SelectionHandleType.E, new MouseFeedbackResponse(SelectionHandleType.E));
 		this.mouseResponseMap.put(SelectionHandleType.SE, new MouseFeedbackResponse(SelectionHandleType.SE));
 		this.mouseResponseMap.put(SelectionHandleType.S, new MouseFeedbackResponse(SelectionHandleType.S));
 		this.mouseResponseMap.put(SelectionHandleType.SW, new MouseFeedbackResponse(SelectionHandleType.SW));
 		this.mouseResponseMap.put(SelectionHandleType.W, new MouseFeedbackResponse(SelectionHandleType.W));
 		this.mouseResponseMap.put(SelectionHandleType.NW, new MouseFeedbackResponse(SelectionHandleType.NW));
 		this.mouseResponseMap.put(SelectionHandleType.None, new DefaultMouseFeedbackResponse());
 	}
 	
 	private void initialiseDragResponses(IEditingOperation moveOp, IResizeOperation resizeOp) {
 		this.dragResponseMap.put(SelectionHandleType.Central, new CentralHandleResponse(moveOp));
 		this.dragResponseMap.put(SelectionHandleType.N, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(0.0, delta.getY());
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(0.0, -delta.getY());
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.NE, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(0.0, delta.getY());
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(delta.getX(), -delta.getY());
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.E, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(0.0, 0.0);
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(delta.getX(), 0.0);
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.SE, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(0.0, 0.0);
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(delta.getX(), delta.getY());
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.S, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(0.0, 0.0);
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(0.0, delta.getY());
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.SW, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(delta.getX(), 0.0);
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(-delta.getX(), delta.getY());
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.W, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(delta.getX(), 0.0);
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(-delta.getX(), 0.0);
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 		this.dragResponseMap.put(SelectionHandleType.NW, new ResizeHandleResponse(new INewPositionCalculator() {
 			private Point delta;
 			
 			@Override
 			public Point getResizedOrigin() {
 				return new Point(delta.getX(), delta.getY());
 			}
 			
 			@Override
 			public Dimension getResizedDelta() {
 				return new Dimension(-delta.getX(), -delta.getY());
 			}
 			
 			@Override
 			public void calculateDeltas(Point newDelta) {
 				this.delta = newDelta;
 			}
 
 			@Override
 			public Point getLastDelta() {
 				return this.delta;
 			}
 		}, resizeOp));
 	}
 
 	private void handleKeyRelease(){
 		if(this.keyboardResponseMap.isKeyPressed()){
 			this.keyboardResponseMap.cursorsKeyUp();
 		}
 	}
 	
 	private void handleKeyPress(CursorType cursorPressed){
 		if(!this.keyboardResponseMap.isKeyPressed()){
 			this.keyboardResponseMap.cursorKeyDown(cursorPressed);
 		}
 		else{
 			this.keyboardResponseMap.cursorKeyStillDown(cursorPressed);
 		}
 	}
 	
 	private INodeController findDrawingNodeAt(Point location) {
 		SortedSet<INodeController> hits = intCalc.findNodesAt(new Point(location.getX(), location.getY()));
 		INodeController retVal = hits.first();
 		logger.info("Found hit at: " + retVal);
 		return retVal;
 	}
 
 	@Override
 	public void activate(){
 		this.shapePane.addKeyListener(this.keyListener);
         this.shapePane.addMouseListener(this.mouseSelectionListener);
         this.shapePane.addMouseMotionListener(this.mouseMotionListener);
 	}
 
 	@Override
 	public void deactivate(){
 		this.shapePane.removeKeyListener(this.keyListener);
         this.shapePane.removeMouseListener(this.mouseSelectionListener);
         this.shapePane.removeMouseMotionListener(this.mouseMotionListener);
 	}
 }
