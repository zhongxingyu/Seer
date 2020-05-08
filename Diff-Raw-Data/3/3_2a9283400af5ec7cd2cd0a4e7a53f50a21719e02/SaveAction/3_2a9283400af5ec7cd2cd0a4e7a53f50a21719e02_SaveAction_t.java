 package org.eclipse.emf.refactor.metrics.runtime.ui.actions;
 
 import java.util.List;
 
 import org.eclipse.emf.refactor.metrics.runtime.core.Result;
 import org.eclipse.emf.refactor.metrics.runtime.managers.XMLResultsManager;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 public class SaveAction extends Action {
 	
 	private Shell shell;
 	private TableViewer viewer;
 
 	public SaveAction(Shell shell, TableViewer viewer) {
 		super();
 		this.shell = shell;
 		this.viewer = viewer;
 		this.setText("Save Results");
 		this.setToolTipText("Save the results list to a file");
 		this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
 	}
 
 	@SuppressWarnings("unchecked")
 	public void run() {
 		FileDialog fd = new FileDialog(shell, SWT.SAVE);
 		fd.setText("Save Results");
 		fd.setFilterPath("C:/");
 		String[] filterExt = { "*.xml", "*.*" };
 		fd.setFilterExtensions(filterExt);
 		String selected = fd.open();
		if (selected != null)
			XMLResultsManager.saveResults(selected, (List<Result>) viewer.getInput());
 	}
 
 }
