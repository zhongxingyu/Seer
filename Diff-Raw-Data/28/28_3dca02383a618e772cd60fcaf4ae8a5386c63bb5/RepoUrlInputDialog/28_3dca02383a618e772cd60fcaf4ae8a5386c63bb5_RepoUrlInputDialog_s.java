 package org.codefaces.ui.dialogs;
 
 import java.util.Collection;
 
 import org.codefaces.core.CodeFacesCoreActivator;
 import org.codefaces.core.models.Repo;
 import org.codefaces.core.models.RepoBranch;
 import org.codefaces.ui.widgets.IProgressMonitorInputValidator;
 import org.codefaces.ui.widgets.ValidatableComboViewer;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 public class RepoUrlInputDialog extends TitleAreaDialog {
 	private static final String WINDOW_TITLE = "Import from a repository";
 
 	private static final String NO_BRANCH_IS_SELECTED = "No branch is selected.";
 
 	private class UrlInputValidator implements IProgressMonitorInputValidator {
 		@Override
 		public String validate(String newText, IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Connecting to repository: " + newText, 100);
 				Repo repo = CodeFacesCoreActivator.getDefault()
 						.getRepoService().createRepo(newText);
 				monitor.worked(30);
 
 				monitor.setTaskName("Fetching branches...");
 				Collection<RepoBranch> branches = repo.getBranches();
 				final Object[] input = branches.toArray();
 
 				if (!branchInputViewer.getViewer().getCCombo().isDisposed()) {
 					branchInputViewer.setInput(input);
 					if (input.length > 0) {
 						branchInputViewer.setSelectedObject(input[0]);
 					} else {
 						branchInputViewer.setSelectedObject(null);
 					}
 				}
 
 				monitor.worked(70);
 			} catch (Exception e) {
 				branchInputViewer.setInput(null);
 				branchInputViewer.setSelectedObject(null);
 				return e.getMessage();
 			}
 
 			return null;
 		}
 	}
 
 	private ValidatableComboViewer urlInputViewer;
 
 	private ValidatableComboViewer branchInputViewer;
 
 	public static final String TITLE = "Checkout projects from a repository";
 
 	private static final String SAMPLE_URL = "http://github.com/jingweno/code_faces";
 
 	public static final String DESCRIPTION = "Enter a GitHub Repository URL, e.g.,"
 			+ SAMPLE_URL;
 
 	public RepoUrlInputDialog(Shell parentShell) {
 		super(parentShell);
 		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
 				| SWT.APPLICATION_MODAL | getDefaultOrientation());
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite composite = (Composite) super.createDialogArea(parent);
 		Composite dialogAreaComposite = new Composite(composite, SWT.NONE);
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
 		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
 		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
 		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
 		dialogAreaComposite.setLayout(layout);
 		dialogAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		dialogAreaComposite.setFont(composite.getFont());
 
 		Label urlInputLabel = new Label(dialogAreaComposite, SWT.NONE);
 		urlInputLabel.setText("Repository:");
 		urlInputViewer = new ValidatableComboViewer(dialogAreaComposite,
 				SWT.BORDER, new UrlInputValidator());
 		urlInputViewer.getControl().setLayoutData(
 				new GridData(GridData.GRAB_HORIZONTAL
 						| GridData.HORIZONTAL_ALIGN_FILL));
 		urlInputViewer.getViewer().getControl().setFocus();
 
 		Label branchInputLabel = new Label(dialogAreaComposite, SWT.NONE);
 		branchInputLabel.setText("Branch:");
 		branchInputViewer = new ValidatableComboViewer(dialogAreaComposite,
 				SWT.BORDER | SWT.READ_ONLY, null);
 		branchInputViewer.getControl().setLayoutData(
 				new GridData(GridData.GRAB_HORIZONTAL
 						| GridData.HORIZONTAL_ALIGN_FILL));
 		branchInputViewer.getViewer().setContentProvider(
 				new ArrayContentProvider());
 		branchInputViewer.getViewer().setLabelProvider(new LabelProvider() {
 			@Override
 			public String getText(Object element) {
 				RepoBranch branch = (RepoBranch) element;
 				return branch.getName();
 			}
 		});
 		branchInputViewer.getViewer().addSelectionChangedListener(
 				new ISelectionChangedListener() {
 					@Override
 					public void selectionChanged(SelectionChangedEvent event) {
 						if (event.getSelection().isEmpty()) {
 							branchInputViewer
 									.setErrorMessages(NO_BRANCH_IS_SELECTED);
 							getButton(IDialogConstants.OK_ID).setEnabled(false);
 						} else {
 							branchInputViewer.setErrorMessages(null);
 							getButton(IDialogConstants.OK_ID).setEnabled(true);
 						}
 					}
 				});
 
 		setTitle(TITLE);
 		setMessage(DESCRIPTION);
 		setWindowTitle(WINDOW_TITLE);
 		applyDialogFont(dialogAreaComposite);
 
 		return composite;
 	}
 
 	private void setWindowTitle(String windowTitle) {
 		if (getShell() == null) {
 			// Not created yet
 			return;
 		}
 
 		getShell().setText(windowTitle);
 
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		super.createButtonsForButtonBar(parent);
 		urlInputViewer
 				.attachToCancelComponent(getButton(IDialogConstants.CANCEL_ID));
 		getButton(IDialogConstants.OK_ID).setEnabled(false);
 	}
 
 	@Override
 	protected void okPressed() {
 		if (branchInputViewer.getSelectedObject() != null) {
 			super.okPressed();
 		}
 	}
 
 	public RepoBranch getSelectedBranch() {
 		return (RepoBranch) branchInputViewer.getSelectedObject();
 	}
 }
