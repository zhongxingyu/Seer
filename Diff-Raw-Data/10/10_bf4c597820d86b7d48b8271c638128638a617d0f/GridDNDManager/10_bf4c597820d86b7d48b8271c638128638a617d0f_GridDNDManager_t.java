 package de.hswt.hrm.scheme.ui;
 
 import java.util.List;
 
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.graphics.Point;
 
 import de.hswt.hrm.scheme.model.RenderedComponent;
 
 /**
  * This class manages the drag and drop in the intern of the SchemeGrid.
  * 
  * @author Michael Sieger
  *
  */
 public class GridDNDManager implements DragSourceListener, DropTargetListener{
 	
 	private final SchemeGrid grid;
 	
 	/**
 	 * The item that is dragged at the moment. Null if no drag is running.
 	 */
 	private DragData dragging;
 	
 	private List<RenderedComponent> comps;
 	
 	public GridDNDManager(SchemeGrid grid, List<RenderedComponent> comps){
 		this.grid = grid;
 		this.comps = comps;
 	}
 
 	@Override
 	public void dragStart(DragSourceEvent ev) {
 	    SchemeGridItem item = grid.removeImagePixel(ev.x, ev.y);
	    if(item != null){
		    RenderedComponent c = item.getRenderedComponent();
	        dragging = new DragData(comps.indexOf(c), 
	                item.getX(), item.getY(), item.getDirection());
	    }else{
	    	ev.doit = false;
	    }
 	}
 	
 	@Override
 	public void dragSetData(DragSourceEvent ev) {
 		if(dragging != null){
 			/*
 			 * SWT wants the data field to be filled
 			 */
 			ev.data = " ";
 		}
 	}
 	
 	@Override
 	public void dragFinished(DragSourceEvent ev) {
 		/*
 		 * dragging is not null, if it was dropped outside of the grid.
 		 * Thow it away in this case.
 		 */
 		dragging = null;
 	}
 
     @Override
     public void dropAccept(DropTargetEvent arg0) {}
     
     @Override
     public void drop(DropTargetEvent ev) {
         if(dragging != null){
             Point loc = grid.toDisplay(0, 0);
             final int x = ev.x - loc.x;
             final int y = ev.y - loc.y;
             try {
                 grid.setImageAtPixel(
                         comps.get(dragging.getId()), 
                         dragging.getDirection(), 
                 		x, y);
             } catch (PlaceOccupiedException | IllegalArgumentException e) {
                 //try {
 				//	grid.setImage(dragging);
 				//} catch (PlaceOccupiedException | IllegalArgumentException e1) {
 					/*
 					 * Das kann eigentlich nicht passieren, weil der Startpunkt
 					 * vor dem Drag nicht belegt war.
 					 */
 					//e1.printStackTrace();
 				//}
             }
             dragging = null;
         }
     }
     
     @Override
     public void dragOver(DropTargetEvent arg0) {}
     
     @Override
     public void dragOperationChanged(DropTargetEvent arg0) {}
     
     @Override
     public void dragLeave(DropTargetEvent ev) {
     	if(dragging != null){
     		ev.detail = DND.DROP_NONE;
     	}
     }
     
     @Override
     public void dragEnter(DropTargetEvent ev) {
     	if(dragging != null){
     		ev.detail = DND.DROP_COPY;
     	}
     }
 	
 
 }
