 package org.chaoticbits.collabcloud.eclipse.actions;
 
 import org.chaoticbits.collabcloud.eclipse.Activator;
 import org.chaoticbits.collabcloud.eclipse.RepoCloudView;
 import org.eclipse.jface.action.Action;
 
 public class SetSrcTreeAction extends Action {
 	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetSrcTreeAction.class);
 
 	private final RepoCloudView view;
 
 	public SetSrcTreeAction(RepoCloudView view) {
 		this.view = view;
 		setText("Set source tree");
 		setToolTipText("Change the root directory of the source code to visualize.");
 		setImageDescriptor(Activator.getImageDescriptor("icons/add.gif"));
 	}
 
 	@Override
 	public void run() {
//		ContainerSelectionDialog dialog = new ContainerSelectionDialog(view.getCanvas().getShell(), null, false,
//				"Select your source tree root:");
//		dialog.setTitle("Source Code Selection");
//		dialog.open();
//		Object[] results = dialog.getResult();
//		if (results != null) {
//			for (Object result : results) {
//				IPath path = (IPath) result;
//				log.info("Setting srcTree to " + path.toPortableString());
//				Activator.getDefault().getPluginPreferences().setValue("srcTree", path.toPortableString().substring(1));
//			}
//		}
 	}
 }
