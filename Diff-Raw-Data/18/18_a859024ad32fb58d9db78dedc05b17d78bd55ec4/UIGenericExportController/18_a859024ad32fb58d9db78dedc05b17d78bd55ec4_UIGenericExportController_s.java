 package org.eclipse.emf.emfstore.client.ui.commands.handler.controller.export;
 
 import java.io.File;
 
 import org.eclipse.emf.ecp.common.util.PreferenceHelper;
 import org.eclipse.emf.emfstore.client.model.controller.ExportController;
import org.eclipse.emf.emfstore.client.model.controller.export.IExport;
 import org.eclipse.emf.emfstore.client.ui.commands.handler.AbstractEMFStoreUIController;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public abstract class UIGenericExportController extends AbstractEMFStoreUIController {
 
 	public UIGenericExportController(Shell shell) {
 		super(shell);
 	}
 
	protected Object execute(IExport export) {
 		FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
 		dialog.setFilterNames(export.getFilteredNames());
 		dialog.setFilterExtensions(export.getFilteredExtensions());
 		dialog.setOverwrite(true);
 		try {
 			String initialFileName = export.getFilename();
 			dialog.setFileName(initialFileName);
 			String initialPath = PreferenceHelper.getPreference(export.getParentFolderPropertyKey(),
 				System.getProperty("user.home"));
 			dialog.setFilterPath(initialPath);
 			// NPE raised when project is not shared yet, since getBaseVersion
 			// needs the project on the server already.
 			// dialog
 		} catch (NullPointerException e) {
 
 		}
 
 		String fn = dialog.open();
 		if (fn == null) {
 			return null;
 		}
 
 		final File file = new File(fn);
 		PreferenceHelper.setPreference(export.getParentFolderPropertyKey(), file.getParent());
 
 		try {
			new ExportController(export, file).execute();
		} catch (EmfStoreException e) {
			// TODO
 		}
 
 		return null;
 	}
 
 	public abstract void export();
 }
