 package sriracha.frontend.android.designer;
 
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import sriracha.frontend.NetlistGenerator;
 import sriracha.frontend.NodeCrawler;
 import sriracha.frontend.R;
 import sriracha.frontend.android.ElementSelector;
 import sriracha.frontend.android.EpicTouchListener;
 import sriracha.frontend.android.NodeSelector;
 import sriracha.frontend.android.model.CircuitElementActivator;
 import sriracha.frontend.android.model.CircuitElementPortView;
 import sriracha.frontend.android.model.CircuitElementView;
 import sriracha.frontend.android.results.IElementSelector;
 import sriracha.frontend.model.CircuitElement;
 import sriracha.frontend.model.CircuitElementManager;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * This class knows all and sees all that has to do with the circuit designer.
  * Any gesture that is not handled at the individual element level is handled here.
  * It creates elements, deletes elements and rotates elements. It creates segments,
  * moves them and deletes them.
  */
 public class CircuitDesigner extends GestureDetector.SimpleOnGestureListener
         implements View.OnTouchListener, CircuitElementView.OnElementClickListener,
         CircuitElementView.OnDropListener, CircuitElementView.OnMoveListener
 {
     public static final int GRID_SIZE = 40;
 
     public enum CursorState
     {
         ELEMENT, HAND, SELECTION, WIRE, SELECTING_ELEMENT
     }
 
     public enum CanvasState
     {
         IDLE, DRAWING_WIRE
     }
 
     private CursorState cursor;
     private CanvasState canvasState = CanvasState.IDLE;
 
     private int selectedItemId;
     private CircuitElementView selectedElement;
 
     private WireSegment dragSegment;
 
     private boolean recentMove;
 
     private CircuitDesignerMenu circuitDesignerMenu;
     private CircuitElementActivator activator;
     private CircuitElementManager elementManager;
 
     private transient GestureDetector gestureDetector;
     private DesignerTouchListener designerTouchListener;
     private CircuitDesignerCanvas canvasView;
 
     private ArrayList<CircuitElementView> elements;
     private WireManager wireManager;
     private IWireIntersection lastInsertedIntersection;
 
     private IElementSelector elementSelector;
 
     public CircuitDesigner(View canvasView, CircuitDesignerMenu circuitDesignerMenu, CircuitElementActivator activator)
     {
         this.circuitDesignerMenu = circuitDesignerMenu;
         this.activator = activator;
         elementManager = new CircuitElementManager();
 
         gestureDetector = new GestureDetector(this);
         designerTouchListener = new DesignerTouchListener();
         this.canvasView = (CircuitDesignerCanvas) canvasView;
         this.canvasView.setOnTouchListener(this);
 
         elements = new ArrayList<CircuitElementView>();
         wireManager = new WireManager(this.canvasView);
 
         setCursorToHand();
     }
 
     public CursorState getCursor()
     {
         return cursor;
     }
 
     public void setCursorToHand()
     {
         setCursor(CursorState.HAND);
     }
 
     public void setCursorToSelection()
     {
         setCursor(CursorState.SELECTION);
     }
 
     public void setCursorToWire()
     {
         setCursor(CursorState.WIRE);
     }
 
     public void setCursorToSelectingElement(IElementSelector elementSelector)
     {
         setElementSelector(elementSelector);
         setCursor(CursorState.SELECTING_ELEMENT);
         canvasView.invalidate();
     }
 
     private void setCursor(CursorState newCursor)
     {
         cursor = newCursor;
 
         int itemId = -1;
         switch (getCursor())
         {
             case ELEMENT:
                 itemId = selectedItemId;
                 canvasState = CanvasState.IDLE;
                 break;
             case WIRE:
                 itemId = R.id.wire;
                 break;
             case HAND:
                 itemId = R.id.hand;
                 canvasState = CanvasState.IDLE;
                 break;
         }
 
         if (itemId != -1)
             circuitDesignerMenu.setSelectedItem(itemId);
     }
 
     public CanvasState getCanvasState()
     {
         return canvasState;
     }
 
     public void setCanvasState(CanvasState canvasState)
     {
         this.canvasState = canvasState;
     }
 
     public int getSelectedItemId()
     {
         return selectedItemId;
     }
 
     public CircuitElementManager getElementManager()
     {
         return elementManager;
     }
 
     /**
      * Set a circuit element as selected so that tapping on the canvas instantiates it.
      * Do not set cursor to ELEMENT anywhere else.
      *
      * @param circuitItemId
      */
     public void selectCircuitItem(int circuitItemId)
     {
         selectedItemId = circuitItemId;
         setCursor(CursorState.ELEMENT);
     }
 
     private void setElementSelector(IElementSelector elementSelector)
     {
         this.elementSelector = elementSelector;
         canvasView.setElementSelector(elementSelector);
     }
 
     @Override
     public boolean onTouch(View view, MotionEvent motionEvent)
     {
         gestureDetector.onTouchEvent(motionEvent);
         return designerTouchListener.onTouch(view, motionEvent);
     }
 
     private void onWireModeElementClick(CircuitElementView elementView, int snappedX, int snappedY)
     {
         wireManager.selectSegment(null);
         selectElement(elementView);
 
         // The first endpoint of a wire
         if (getCanvasState() == CanvasState.IDLE)
         {
            lastInsertedIntersection = elementView.getClosestPort(snappedX, snappedY, false);
             wireManager.addIntersection(lastInsertedIntersection);
             setCanvasState(CanvasState.DRAWING_WIRE);
         } else if (getCanvasState() == CanvasState.DRAWING_WIRE)
         {
             // Create new node, and end the wire at the new element
            CircuitElementPortView port = elementView.getClosestPort(snappedX, snappedY, false);
     //       switchIntersectionToClosestPort(port.getX(), port.getY());
             wireManager.connectNewIntersection(lastInsertedIntersection, port);
 
             // End the wire drawing now.
             endWireDraw();
         }
     }
 
 
     private void onWireModeTapUp(int snappedX, int snappedY)
     {
         // Possibilities:
         // 1 clicked near a port
         // 2 clicked on empty space
         // 3 clicked on our previously inserted segment
         // 4 clicked on another older segment or intersection
 
         CircuitElementPortView clickedPort = getPortAt(snappedX, snappedY, null);
         WireSegment clickedSegment = wireManager.getSegmentByPosition(snappedX, snappedY);
 
 
         if (clickedPort != null)
         {
             // Case 1: clicked a port
          //  switchIntersectionToClosestPort(snappedX, snappedY);
             onWireModeElementClick(clickedPort.getElement(), snappedX, snappedY);
         } else
         {
 
             if (lastInsertedIntersection == null) return;
 
             if (clickedSegment == null)
             {
                 // Case 2: empty space.
                 // Add new node
                 WireIntersection newIntersection = new WireIntersection(snappedX, snappedY);
                 switchIntersectionToClosestPort(snappedX, snappedY);
                 wireManager.connectNewIntersection(lastInsertedIntersection, newIntersection);
                 lastInsertedIntersection = newIntersection;
             } else if (lastInsertedIntersection instanceof WireIntersection && lastInsertedIntersection.getSegments().size() == 1 && lastInsertedIntersection.getSegments().get(0) == clickedSegment)
             {
                 // Case 3: we clicked on our last segment maybe we want to make it shorter again?
                 lastInsertedIntersection.setPosition(snappedX, snappedY);
 
             } else if (clickedSegment.isPointOnSegment(snappedX, snappedY))
             {
                 // Case 4: existing segment/node.
                 // Add a new node. we let the node consolidator take care of it later.
                 switchIntersectionToClosestPort(snappedX, snappedY);
                 WireIntersection newIntersection = wireManager.splitSegment(clickedSegment, snappedX, snappedY);
                 wireManager.connectNewIntersection(lastInsertedIntersection, newIntersection);
 
                 // End the wire drawing now.
                 endWireDraw();
 
             }
         }
     }
 
     public void endWireDraw()
     {
         selectElement(null);
         lastInsertedIntersection = null;
         wireManager.consolidateIntersections();
         wireManager.invalidateAll();
         setCanvasState(CanvasState.IDLE);
 
     }
 
     @Override
     public void onElementClick(View view, float x, float y)
     {
         wireManager.selectSegment(null);
         selectElement((CircuitElementView) view);
 
         switch (getCursor())
         {
             case SELECTING_ELEMENT:
             {
                 if (elementSelector instanceof ElementSelector && elementSelector.onSelect(view))
                 {
                     setCursorToHand();
                     setElementSelector(null);
                     canvasView.invalidate();
                 }
             }
             break;
             case HAND:
             {
                 circuitDesignerMenu.showElementPropertiesMenu(selectedElement, this);
             }
             break;
             case WIRE:
             {
                 onWireModeElementClick((CircuitElementView) view, snap(x), snap(y));
             }
             break;
         }
     }
 
     public void addElement(CircuitElementView elementView)
     {
         elementManager.addElement(elementView.getElement());
         elements.add(elementView);
         canvasView.addView(elementView);
 
         elementView.setOnElementClickListener(this);
         elementView.setOnDropListener(this);
         elementView.setOnMoveListener(this);
         elementView.updatePosition();
     }
 
 
     private void selectElement(CircuitElementView e)
     {
         for (CircuitElementView element : elements)
         {
             element.setElementSelected(false);
         }
 
         if (e != null)
         {
             e.setElementSelected(true);
         }
 
         selectedElement = e;
     }
 
     public void deselectAllElements()
     {
         setCursorToHand();
         selectElement(null);
         wireManager.selectSegment(null);
     }
 
     public CircuitElementView instantiateElement(float positionX, float positionY)
     {
         return activator.instantiateElement(getSelectedItemId(), positionX, positionY, elementManager, wireManager);
     }
 
     public CircuitElementView instantiateElement(UUID elementUUID) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
     {
         return activator.instantiateElement(elementUUID, 0, 0, elementManager, wireManager);
     }
 
     public void rotateSelectedElement(boolean cw)
     {
         if (selectedElement != null)
         {
             int rotation = cw ? 90 : -90;
             float newOrientation = (selectedElement.getOrientation() + rotation) % 360;
             for (CircuitElementPortView port : selectedElement.getPortViews())
             {
                 for (WireSegment segment : port.getSegments())
                 {
                     IWireIntersection otherIntersection = segment.getStart() != port ? segment.getStart() : segment.getEnd();
                     if (segment.isVertical())
                     {
                         int dx = (int) (selectedElement.getWidth() * (port.transformPosition(newOrientation)[0] - port.getTransformedPosition()[0]));
                         int newX = segment.getStart().getX() + dx;
                         if (dx != 0)
                         {
                             if (otherIntersection.duplicateOnMove(segment))
                             {
                                 WireIntersection duplicate = otherIntersection.duplicate(segment, wireManager);
                                 duplicate.x = newX;
                             } else
                             {
                                 ((WireIntersection) otherIntersection).x = newX;
                             }
                             segment.invalidate();
                         }
                     } else
                     {
                         int dy = (int) (selectedElement.getHeight() * (port.transformPosition(newOrientation)[1] - port.getTransformedPosition()[1]));
                         int newY = segment.getStart().getY() + dy;
                         if (dy != 0)
                         {
                             if (otherIntersection.duplicateOnMove(segment))
                             {
                                 WireIntersection duplicate = otherIntersection.duplicate(segment, wireManager);
                                 duplicate.y = newY;
                             } else
                             {
                                 ((WireIntersection) otherIntersection).y = newY;
                             }
                             segment.invalidate();
                         }
                     }
                 }
             }
             selectedElement.rotate(rotation);
             wireManager.consolidateIntersections();
         }
     }
 
     public void deleteSelectedElement()
     {
         if (getCursor() != CursorState.HAND || selectedElement == null)
             return;
 
         wireManager.removeElement(selectedElement);
         elementManager.removeElement(selectedElement.getElement());
         elements.remove(selectedElement);
         canvasView.removeView(selectedElement);
         canvasView.invalidate();
 
         deselectAllElements();
         circuitDesignerMenu.showSubMenu(R.id.circuit_menu);
     }
 
     public void deleteSelectedWire()
     {
         if (getCursor() != CursorState.HAND)
             return;
 
         wireManager.deleteSelectedSegment();
         canvasView.invalidate();
 
         deselectAllElements();
         circuitDesignerMenu.showSubMenu(R.id.circuit_menu);
     }
 
     public void InvalidateDesigner()
     {
         wireManager.invalidateAll();
         canvasView.invalidate();
     }
 
     @Override
     public void onDrop(CircuitElementView elementView)
     {
         for (CircuitElementPortView port : elementView.getPortViews())
         {
             if (port.getSegments().isEmpty())
             {
                 CircuitElementPortView portIntersection = getPortAt(port.getX(), port.getY(), port.getElement());
 
                 IWireIntersection normIntersection = getIntersectionAt(port.getX(), port.getY(), port);
 
                 if (portIntersection != null)
                 {
                     wireManager.addIntersection(port);
                     wireManager.connectNewIntersection(port, portIntersection);
                 } else if (normIntersection != null && normIntersection.getSegments().size() == 1)
                 {
 
                     NodeCrawler crawler = new NodeCrawler(wireManager);
                     crawler.computeMappings();
                     CircuitElementPortView[] ports = elementView.getPortViews();
                     boolean connect = true;
                     for (CircuitElementPortView p : ports)
                     {
                         //check all other ports if they are the same node as the hanging wire, do not connect
                         if (port != p && crawler.nodeFromIntersection(normIntersection) == crawler.nodeFromIntersection(port))
                         {
                             connect = false;
                             break;
                         }
                     }
                     if (connect)
                     {
                         wireManager.addIntersection(port);
                         wireManager.connectNewIntersection(port, normIntersection);
                     }
 
                 }
             }
         }
     }
 
     private IWireIntersection getIntersectionAt(int x, int y, IWireIntersection except)
     {
 
         for (IWireIntersection intersection : wireManager.getIntersections())
         {
 
             if (intersection == except) continue;
 
             if (x == intersection.getX() && y == intersection.getY())
             {
                 return intersection;
             }
         }
 
         return null;
     }
 
     private CircuitElementPortView getPortAt(int x, int y, CircuitElementView not)
     {
         for (CircuitElementView element : elements)
         {
             if (element == not)
                 continue;
 
             for (CircuitElementPortView port : element.getPortViews())
             {
                 if (x == port.getX() && y == port.getY())
                 {
                     return port;
                 }
             }
         }
         return null;
     }
 
     /**
      * If the last inserted intersection was a port
      *
      * @param x
      * @param y
      */
     private void switchIntersectionToClosestPort(int x, int y)
     {
         if (lastInsertedIntersection instanceof CircuitElementPortView)
         {
             CircuitElementPortView port = ((CircuitElementPortView) lastInsertedIntersection).getElement().getClosestPort(x, y, false);
             if (port.getElement().getAttachedSegmentCount() == 0)
             {
                 lastInsertedIntersection = port;
                 wireManager.addIntersection(lastInsertedIntersection);
             }
         }
     }
 
     @Override
     public void onMove(CircuitElementView elementView)
     {
         if (getCursor() == CursorState.WIRE)
             setCursorToHand();
     }
 
     public String generateNetlist()
     {
         NetlistGenerator generator = new NetlistGenerator(wireManager, elements);
         String netlist = generator.generateNetlist();
         return netlist;
     }
 
     public CircuitElement getElementByName(String elementName)
     {
         return elementManager.getElementByName(elementName);
     }
 
     public ArrayList<CircuitElementView> getElements()
     {
         return elements;
     }
 
     public WireManager getWireManager()
     {
         return wireManager;
     }
 
     public void setWireManager(WireManager wireManager)
     {
         this.wireManager = wireManager;
     }
 
     public static int snap(float coord)
     {
         return (int) (coord / GRID_SIZE + 0.5f) * GRID_SIZE;
     }
 
     private class DesignerTouchListener extends EpicTouchListener
     {
         private WireSegment selectedSegment;
 
 
         @Override
         protected void onSingleFingerDown(float x, float y)
         {
             dragSegment = wireManager.getSegmentByPosition(snap(x), snap(y));
             recentMove = false;
         }
 
         @Override
         protected boolean onSingleFingerTap(float x, float y)
         {
             boolean handled = false;
 
             float snappedX = snap(x), snappedY = snap(y);
 
             switch (getCursor())
             {
                 case HAND:
                 {
                     if (!recentMove)
                     {
                         selectedSegment = wireManager.getSegmentByPosition(snappedX, snappedY);
                         if (selectedSegment != null)
                         {
                             wireManager.selectSegment(selectedSegment);
                             selectElement(null);
                             circuitDesignerMenu.showSubMenu(R.id.wire_properties);
                             handled = true;
                         }
                     }
 
                 }
                 break;
                 case ELEMENT:
                 {
                     selectElement(null);
 
                     CircuitElementView elementView = instantiateElement(snappedX, snappedY);
                     if (elementView != null)
                     {
                         addElement(elementView);
                         handled = true;
                     }
 
                 }
             }
 
             return handled;
         }
 
         @Override
         protected boolean onAllFingersUp(float x, float y)
         {
 
             int snappedX = snap(x);
             int snappedY = snap(y);
 
             boolean handled = false;
 
 
             switch (getCursor())
             {
                 case WIRE:
                 {
                     onWireModeTapUp(snappedX, snappedY);
                     handled = true;
                 }
                 break;
                 case SELECTING_ELEMENT:
                 {
                     if (elementSelector instanceof NodeSelector)
                     {
                         WireSegment segment = wireManager.getSegmentByPosition(snappedX, snappedY);
 
                         if (segment != null && elementSelector.onSelect(segment))
                         {
                             setCursorToHand();
                             setElementSelector(null);
                             handled = true;
                         }
 
                     } else if (elementSelector instanceof ElementSelector)
                     {
                         CircuitElementView view = getPortAt(snappedX, snappedY, null).getElement();
                         if (view != null && elementSelector != null && elementSelector.onSelect(view)) ;
                         {
                             setCursorToHand();
                             setElementSelector(null);
                             handled = true;
                         }
                     }
 
                 }
                 break;
 
             }
 
 
             wireManager.consolidateIntersections();
 
             if (handled)
             {
                 InvalidateDesigner();
             }
 
 
             return handled;
         }
 
         @Override
         protected boolean onSingleFingerMove(float dX, float dY, float finalX, float finalY)
         {
             if (dragSegment == null)
                 return false;
 
             boolean canMoveHorizontally = dragSegment.isVertical();
             boolean moved = false;
             if (canMoveHorizontally)
             {
                 moved = dragSegment.moveX(snap(finalX));
             } else
             {
                 moved = dragSegment.moveY(snap(finalY));
             }
 
             if (moved)
             {
                 recentMove = true;
                 wireManager.invalidateAll();
             }
 
             return true;
         }
     }
 }
