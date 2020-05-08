 package de.ptb.epics.eve.editor.gef;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.SelectionManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 
 /**
  * The {@link org.eclipse.gef.SelectionManager} the GEF editor delegates 
  * selections to.
  * 
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class ModifiedSelectionManager extends SelectionManager {
 
 	private final GraphicalViewer viewer;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param viewer the viewer the manager belongs to
 	 */
 	public ModifiedSelectionManager(GraphicalViewer viewer) {
 		this.viewer = viewer;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setSelection(ISelection selection) {
 		super.setSelection(selection);
 		List<?> oldSelection = ((IStructuredSelection)selection).toList();
 		final List<Object> newSelection = new ArrayList<Object>();
 		for(Object o : oldSelection) {
 			EditPart part = (EditPart)o;
 			if(part.getSelected() == EditPart.SELECTED_PRIMARY) {
 				newSelection.add(o);
 			}
 		}
		oldSelection.removeAll(newSelection);
		newSelection.addAll(oldSelection);
 		if (!newSelection.isEmpty()) {
 			super.setSelection(new StructuredSelection(newSelection));
 		}
 	}
 }
