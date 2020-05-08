 package za.ac.sun.cs.intlola.gui;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 public class ProjectDialog extends Dialog {
 
 	private String[]				projects;
 
 	private final String		title;
 
 	private Combo projectList;
 
 	private int selected;
 
 
 	public ProjectDialog(final Shell parentShell, final String title,
 			final String[] projects) {
 		super(parentShell);
 		this.projects = projects;
 		this.title = title;
 
 	}
 
 	@Override
 	public void configureShell(final Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(title);
 	}
 
 	@Override
 	public Control createButtonBar(final Composite parent) {
 		final Control ret = super.createButtonBar(parent);
 		getButton(IDialogConstants.OK_ID).setText("Confirm");
 		return ret;
 	}
 
 	@Override
 	protected Control createDialogArea(final Composite parent) {
 		final Composite comp = (Composite) super.createDialogArea(parent);
 
 		final GridLayout layout = (GridLayout) comp.getLayout();
 		layout.numColumns = 2;
 
 		final Label projectLabel = new Label(comp, SWT.RIGHT);
 		projectLabel.setText("Project:");
 
 		projectList = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
 		projectList.setItems(projects);
 		projectList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		projectList.addSelectionListener(new SelectionListener() {
			

 			@Override
 			public void widgetSelected(SelectionEvent se) {
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				selected = projectList.getSelectionIndex();
 			}
 		});
 		
 		return comp;
 	}
 
 
 
 	public int getProject() {
 		return selected;
 	}
 
 
 }
