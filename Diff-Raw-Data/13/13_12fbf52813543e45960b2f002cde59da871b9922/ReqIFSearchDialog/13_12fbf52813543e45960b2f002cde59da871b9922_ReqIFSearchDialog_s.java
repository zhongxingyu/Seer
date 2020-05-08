 package org.eclipse.rmf.reqif10.search.ui.dialogs;
 
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.rmf.reqif10.search.edit.util.ReqIFEditSearcher;
 import org.eclipse.rmf.reqif10.search.ui.ReqIFSearchQuery;
 import org.eclipse.rmf.reqif10.search.ui.ReqIFSearchUIPlugin;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.search.ui.NewSearchUI;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.ManagedForm;
 
 public class ReqIFSearchDialog extends Dialog {
 	private CriteriasMasterDetailsBlock masterDetailsBlock;
 
 	private static final int REPLACE_ID = 2;
 
 	/**
 	 * Create the dialog.
 	 * 
 	 * @param parentShell
 	 */
 	public ReqIFSearchDialog(Shell parentShell) {
 		super(parentShell);
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite container = (Composite) super.createDialogArea(parent);
 		container.setLayout(new FillLayout());
 		IManagedForm managedForm = new ManagedForm(container);
 		masterDetailsBlock = new CriteriasMasterDetailsBlock();
 		masterDetailsBlock.createContent(managedForm);
 		return container;
 	}
 
 	/**
 	 * Create contents of the button bar.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, "Search", true);
 		createButton(parent, REPLACE_ID, "Replace", false);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == REPLACE_ID) {
 			replacePressed();
 		} else {
 			super.buttonPressed(buttonId);
 		}
 	}
 
 	private void replacePressed() {
 		setReturnCode(OK);
 		close();
 		EditingDomain editingDomain = getActiveEditorEditingDomain();
 		ReqIFEditSearcher.findAndReplcae(editingDomain,
 				editingDomain.getResourceSet(),
 				masterDetailsBlock.getCriterias());
 	}
 
 	@SuppressWarnings("restriction")
 	@Override
 	protected void okPressed() {
 		super.okPressed();
 		ISearchQuery query = newQuery();
 
 		// Note : In the javadoc its explaned that we need to only call
 		// NewSearchUI.runQueryInForeground but due to a bug this doesn't work
 		// so I had to do the code below.
 		org.eclipse.search2.internal.ui.SearchView searchView = null;
 		try {
 			searchView = (org.eclipse.search2.internal.ui.SearchView) PlatformUI
 					.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 					.showView(NewSearchUI.SEARCH_VIEW_ID);
 		} catch (final PartInitException e) {
 			ReqIFSearchUIPlugin.INSTANCE.log(e);
 		}
		NewSearchUI.runQueryInForeground(new ProgressMonitorDialog(
 				getParentShell()), query);
 		if (searchView != null) {
 			searchView.showSearchResult(query.getSearchResult());
 		}
 	}
 
 	private EditingDomain getActiveEditorEditingDomain() {
 		IEditorPart activeEditor = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if (activeEditor instanceof IEditingDomainProvider) {
 			return ((IEditingDomainProvider) activeEditor).getEditingDomain();
 		}
 		return null;
 	}
 
 	private ISearchQuery newQuery() {
 		EditingDomain editingDomain = getActiveEditorEditingDomain();
 		return new ReqIFSearchQuery(editingDomain,
 				masterDetailsBlock.getCriterias());
 	}
 
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	/**
 	 * Return the initial size of the dialog.
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(600, 400);
 	}
 }
