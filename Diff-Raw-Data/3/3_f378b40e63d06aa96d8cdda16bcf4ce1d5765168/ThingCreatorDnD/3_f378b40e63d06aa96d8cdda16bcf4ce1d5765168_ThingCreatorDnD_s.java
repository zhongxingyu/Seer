 package ui.isometric.builder.things;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceMotionListener;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 import javax.swing.TransferHandler;
 
 import util.DragInfo;
 
 /**
  * A set of classes used for dragging and dropping ThingCreators
  * 
  * @author melby
  *
  */
 public class ThingCreatorDnD {
 	/**
 	 * A class that is used to transfer ThingCreators between components in a drag
 	 * 
 	 * @author melby
 	 *
 	 */
 	public static class ThingCreatorTransfer implements Transferable {
 		private static DataFlavor dragAndDropDataFlavor = null;
 		
 		/**
 		 * Get the DataFlavor this transfer object uses
 		 * @return
 		 */
 		private static DataFlavor getDragAndDropDataFlavor() {
 			if(dragAndDropDataFlavor == null) {
 				try {
 					dragAndDropDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ThingCreatorTransfer.class.getName());
 				} catch (ClassNotFoundException e) {
 					System.out.println("Unable to find ThingTransfer class: " + DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ThingCreatorTransfer.class.getName());
 					e.printStackTrace();
 				}
 	        }
 
 	        return dragAndDropDataFlavor;
 		}
 		
 		private ThingCreator creator;
 		
 		/**
 		 * Create a ThingCreatorTransfer with a given ThingCreator
 		 * @param creator
 		 */
 		public ThingCreatorTransfer(ThingCreator creator) {
 			this.creator = creator;
 		}
 
 		@Override
 		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
 			if(flavor.equals(getDragAndDropDataFlavor())) {
 				return creator;
 			}
 			
 			return null;
 		}
 
 		@Override
 		public DataFlavor[] getTransferDataFlavors() {
 			return new DataFlavor[] { getDragAndDropDataFlavor() };
 		}
 
 		@Override
 		public boolean isDataFlavorSupported(DataFlavor flavors) {
 			return flavors.equals(getDragAndDropDataFlavor());
 		}
 		
 	}
 	
 	/**
 	 * A class that handles the start of a drag operation
 	 * 
 	 * @author melby
 	 *
 	 */
 	public static class ThingCreatorToThingTransferHandler extends TransferHandler implements DragSourceMotionListener {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Transferable createTransferable(JComponent c) {			
 	        if(c instanceof DragInfo) {
 	        	DragInfo p = (DragInfo)c;
 	        	if(p.dragObject() != null && p.dragObject() instanceof ThingCreator) {
 		            Transferable tip = new ThingCreatorTransfer((ThingCreator)p.dragObject());
 		            return tip;
 	        	}
 	        }
 
 	        return null;
 	    }
 
 		@Override
 	    public void dragMouseMoved(DragSourceDragEvent dsde) {}
 
 	    @Override
 	    public int getSourceActions(JComponent c) {	    	
 	        if(c instanceof DragInfo) {
 	        	DragInfo p = (DragInfo)c;
 	        	if(p.dragObject() != null && p.dragObject() instanceof ThingCreator) {
 	        		return TransferHandler.COPY;
 	        	}
 	        }
 	        	        
 	        return TransferHandler.NONE;
 	    }
 	}
 	
 	/**
 	 * A class that listens for appropriate drop actions and responds to them
 	 * 
 	 * @author melby
 	 *
 	 */
 	public static class ThingDropListener implements DropTargetListener {
 		/**
 		 * A interface to specify how to receive messages when a drop action is performed
 		 * @author melby
 		 *
 		 */
 		public static interface ThingDropListenerAction {
 			/**
 			 * A drop action was performed
 			 * @param onto - the component the drop was over
 			 * @param location - the location within this component
 			 * @param creator - the ThingCreator that was dropped
 			 */
 			public void thingCreatorDroped(Component onto, Point location, ThingCreator creator);
 		}
 		
 		private ThingDropListenerAction action;
 		
 		/**
 		 * Create a ThingDropListener with w given action to perform when the drop is made
 		 * @param action
 		 */
 	    public ThingDropListener(ThingDropListener.ThingDropListenerAction action) {
 	    	this.action = action;
 	    }
 
 	    @Override
 		public void dragEnter(DropTargetDragEvent arg0) {}
 
 		@Override
 		public void dragExit(DropTargetEvent arg0) {}
 
 		@Override
 		public void dragOver(DropTargetDragEvent arg0) {}
 
 		@Override
 		public void dropActionChanged(DropTargetDragEvent arg0) {}
 		
 		@Override
 	    public void drop(DropTargetDropEvent drag) {			
 	        DataFlavor dragAndDropFlavor = null;
 	        
 	        Object transferableObj = null;
 	        Transferable transferable = null;
 	        
 	        try {
 	        	dragAndDropFlavor = ThingCreatorTransfer.getDragAndDropDataFlavor();
 	            
 	            transferable = drag.getTransferable();
 	            
 	            if(transferable.isDataFlavorSupported(dragAndDropFlavor)) {
 	                transferableObj = drag.getTransferable().getTransferData(dragAndDropFlavor);
 	            } 
 	            
 	        } catch (Exception e) { }
 	        
 	        if(transferableObj == null) {
 	            return;
 	        }
 	        
 	        ThingCreator thingCreator = (ThingCreator)transferableObj;
 	        
 	        action.thingCreatorDroped(drag.getDropTargetContext().getComponent(), drag.getLocation(), thingCreator);
 	        
	        drag.acceptDrop(TransferHandler.COPY); // TODO: working?
 	    }
 	}
 }
