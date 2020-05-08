 package org.eclipse.gmt.modisco.jm2t.internal.ui.dialogs;
 
import java.io.File;

 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.gmt.modisco.jm2t.internal.ui.JM2TUI;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.views.navigator.ResourceComparator;
 
 public class TemplateFileSelectionDialog extends ElementTreeSelectionDialog {
 
 	/**
 	 * Constructor for JARFileSelectionDialog.
 	 * 
 	 * @param parent
 	 *            parent shell
 	 * @param multiSelect
 	 *            specifies if selecting multiple elements is allowed
 	 * @param acceptFolders
 	 *            specifies if folders can be selected as well
 	 */
 	public TemplateFileSelectionDialog(Shell parent, boolean multiSelect,
 			boolean acceptFolders) {
 		super(parent, new WorkbenchLabelProvider(),
 				new WorkbenchContentProvider());
 		setComparator(new ResourceComparator(ResourceComparator.NAME));
 		// addFilter(new FileArchiveFileFilter(acceptFolders));
 		setValidator(new FileSelectionValidator(multiSelect, acceptFolders));
 		setHelpAvailable(false);
 	}
 
 	private static class FileSelectionValidator implements
 			ISelectionStatusValidator {
 		private boolean fMultiSelect;
 		private boolean fAcceptFolders;
 
 		public FileSelectionValidator(boolean multiSelect, boolean acceptFolders) {
 			fMultiSelect = multiSelect;
 			fAcceptFolders = acceptFolders;
 		}
 
 		public IStatus validate(Object[] selection) {
 			int nSelected = selection.length;
 			if (nSelected == 0 || (nSelected > 1 && !fMultiSelect)) {
 				return JM2TUI.createErrorStatus("", null);
 			}
 			for (int i = 0; i < selection.length; i++) {
 				Object curr = selection[i];
				if (curr instanceof File) {
					File file = (File) curr;
					if (!fAcceptFolders && !file.isFile()) {
						return JM2TUI.createErrorStatus("", null);
 					}
 				}
 			}
 			return Status.OK_STATUS;
 		}
 	}
 
 }
