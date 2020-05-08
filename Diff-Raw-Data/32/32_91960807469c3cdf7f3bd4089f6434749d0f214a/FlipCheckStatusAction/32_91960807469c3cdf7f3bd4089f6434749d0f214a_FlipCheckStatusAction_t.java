 package net.syncarus.action.tree;
 
 import net.syncarus.action.SyncViewAction;
 import net.syncarus.gui.SyncTreeViewer;
import net.syncarus.model.DiffNode;
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
 		for (Object selected : selection.toList()) {
			boolean checkState = viewer.getChecked(selected);
			viewer.setSubtreeChecked(selected, !checkState);
 		}
 	}
	
	private void switchTickState(DiffNode node) {
		boolean checkState = getTreeViewer().getChecked(node);
		getTreeViewer().setChecked(node, !checkState);
		if (node.isDirectory())
			for (DiffNode subNode : node.getChildren())
				switchTickState(subNode);
	}
 }
