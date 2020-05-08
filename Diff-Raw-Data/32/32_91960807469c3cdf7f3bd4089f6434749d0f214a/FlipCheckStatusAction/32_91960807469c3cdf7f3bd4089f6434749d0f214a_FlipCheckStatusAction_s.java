 package net.syncarus.action.tree;
 
 import net.syncarus.action.SyncViewAction;
 import net.syncarus.gui.SyncTreeViewer;
 import net.syncarus.rcp.ResourceRegistry;
 
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 
 public class FlipCheckStatusAction extends SyncViewAction {
 	
 	public FlipCheckStatusAction(SyncTreeViewer viewer) {
 		setText("Flip tick states");
 		setIcon(ResourceRegistry.IMAGE_FLIP_CHECK_STATE);
 	}
 	
 	@Override
 	public boolean isEnabled() {
 		return !getSelectedNodes().isEmpty();
 	}
 
 	@Override
 	public void run() {
 		CheckboxTreeViewer viewer = getTreeViewer();
 		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		// FIXME: this does not work for subfolders, their tick states aren't flipped
 		for (Object selected : selection.toList()) {
			viewer.setChecked(selected, !viewer.getChecked(selected));
 		}
 	}
 }
