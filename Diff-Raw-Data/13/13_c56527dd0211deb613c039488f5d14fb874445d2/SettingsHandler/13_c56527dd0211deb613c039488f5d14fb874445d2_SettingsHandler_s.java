 package com.jensdriller.zeeboxzonechooser.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.model.BaseWorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 import com.jensdriller.zeeboxzonechooser.Activator;
 
 public class SettingsHandler extends AbstractHandler {
 
 	private static final String KEY = "path";
 
 	public SettingsHandler() {
 	}
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		String path = getBuildConstantsPathViaDialog();
 		if (path != null) {
 			saveBuildConstantsPath(path);
 		}
 
 		return null;
 	}
 
 	private String getBuildConstantsPathViaDialog() {
 		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				new WorkbenchLabelProvider(),
 				new BaseWorkbenchContentProvider());
 		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
 		dialog.setTitle("BuildConstants.java");
 		dialog.setMessage("Select the BuildConstants.java file from your zeebox project:");
 		dialog.setValidator(new ISelectionStatusValidator() {
 			public IStatus validate(Object[] selection) {
 				if (selection.length == 1 && !(selection[0] instanceof IFile)) {
 					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
 							"Please select a file!", null);
 				}
 				return new Status(IStatus.OK, Activator.PLUGIN_ID, 0, "", null);
 			}
 		});
 
		IResource resource = null;
 		int buttonId = dialog.open();
 		if (buttonId == IDialogConstants.OK_ID) {
			resource = (IResource) dialog.getFirstResult();
			if (!resource.isAccessible()) {
				return null;
 			}
 		}
 
		return resource.getFullPath().toString();
 	}
 
 	private void saveBuildConstantsPath(String path) {
 		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
 		prefs.put(KEY, path);
 
 		try {
 			prefs.flush();
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String loadBuildConstantsPath() {
 		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
 		return prefs.get(KEY, null);
 	}
 }
