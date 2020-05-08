 package de.hswt.hrm.scheme.ui;
 
 import java.awt.Toolkit;
 
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.graphics.Point;
 
 /**
  * This class manages the drag and drop in the intern of the SchemeGrid
  * 
  * @author Michael Sieger
  *
  */
 public class GridDNDManager {
 	
 	private final SchemeGrid grid;
 	private final DropTarget target;
 	private final DragSource source;
 	private SchemeGridItem dragging;
 	
 	public GridDNDManager(SchemeGrid grid, DropTarget target, DragSource source){
 		this.grid = grid;
 		this.target = target;
 		this.source = source;
 		initDrag();
 		initDrop();
 	}
 	
 	private void initDrop(){
 	    target.addDropListener(new DropTargetListener() {
             
             @Override
             public void dropAccept(DropTargetEvent arg0) {}
             
             @Override
             public void drop(DropTargetEvent ev) {
                 if(dragging != null){
                     Point loc = grid.toDisplay(0, 0);
                     final int x = ev.x - loc.x;
                    final int y = ev.y - loc.x;
                     try {
                         grid.setImageAtPixel(dragging.getRenderedGridImage(), x, y);
                     } catch (PlaceOccupiedException | IllegalArgumentException e) {
                         Toolkit.getDefaultToolkit().beep();
                         try {
 							grid.setImage(dragging);
 						} catch (PlaceOccupiedException | IllegalArgumentException e1) {
 							/*
 							 * Das kann eigentlich nicht passieren, weil der Startpunkt
 							 * vor dem Drag nicht belegt war.
 							 */
 							e1.printStackTrace();
 						}
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
         });
 	}
 	
 	private void initDrag(){
 		source.addDragListener(new DragSourceListener() {
 			
 			@Override
 			public void dragStart(DragSourceEvent ev) {
                 dragging = grid.removeImagePixel(ev.x, ev.y);
 			}
 			
 			@Override
 			public void dragSetData(DragSourceEvent ev) {
 				if(dragging != null){
 					ev.data = dragging.toString();
 				}
 			}
 			
 			@Override
 			public void dragFinished(DragSourceEvent ev) {
 				dragging = null;
 			}
 		});
 	}
 	
 
 }
