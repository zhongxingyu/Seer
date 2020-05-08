 package ch.bfh.ti.projekt1.sokoban.editor;
 
 import ch.bfh.ti.projekt1.sokoban.controller.FieldController;
 import ch.bfh.ti.projekt1.sokoban.model.FieldState;
 import ch.bfh.ti.projekt1.sokoban.view.element.Element;
 
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.*;
 import java.io.IOException;
 
 /**
  * @author svennyffenegger
  * @since 31/10/13 20:14
  */
 public class DraggableElementDestination extends Element implements DropTargetListener {
 
     private DropTarget dropTarget;
 
     public DraggableElementDestination(FieldController controller) {
        this(controller, FieldState.EMPTY);
    }
    
    public DraggableElementDestination(FieldController controller, FieldState fieldState) {
         super(controller);
         dropTarget = new DropTarget(this, this);
        addImage(fieldState);
     }
 
     @Override
     public void dragEnter(DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dragOver(DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dropActionChanged(DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dragExit(DropTargetEvent dte) {
     }
 
     @Override
     public void drop(DropTargetDropEvent dtde) {
         try {
             Transferable transferable = dtde.getTransferable();
 
             if (transferable.isDataFlavorSupported(LevelEditorTransferable.FIELD_STATE_DATA_FLAVOR)) {
                 dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                 FieldState state = (FieldState) transferable.getTransferData(LevelEditorTransferable.FIELD_STATE_DATA_FLAVOR);
                 DraggableElementDestination.this.controller.fieldStateChanged(state);
                 dtde.getDropTargetContext().dropComplete(true);
             } else {
                 dtde.rejectDrop();
             }
         } catch (IOException e) {
             dtde.rejectDrop();
         } catch (UnsupportedFlavorException e) {
             dtde.rejectDrop();
         }
     }
 }
