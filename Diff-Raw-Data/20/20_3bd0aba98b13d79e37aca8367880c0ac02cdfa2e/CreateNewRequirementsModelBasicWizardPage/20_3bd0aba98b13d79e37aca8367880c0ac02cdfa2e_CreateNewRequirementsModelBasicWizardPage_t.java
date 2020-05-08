 package it.unitn.disi.unagi.rcpapp.wizards;
 
 import it.unitn.disi.unagi.rcpapp.nls.Messages;
 import it.unitn.disi.unagi.rcpapp.views.UnagiProjectExplorerView;
 import it.unitn.disi.unagi.rcpapp.views.models.AbstractProjectTreeElement;
 import it.unitn.disi.util.logging.LogUtil;
 
 import javax.inject.Inject;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.e4.core.di.annotations.Creatable;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * A page of the "Create Requirements Model" wizard that asks the user for the basic information on the requirements,
  * such as its name and base package.
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  * @version 1.0
  */
 @Creatable
 public class CreateNewRequirementsModelBasicWizardPage extends WizardPage implements KeyListener {
 	/** The platform's selection service, used to determine what is the user's current workspace selection. */
 	@Inject
 	private ESelectionService selectionService;
 
 	/** Container associated to this wizard page. */
 	private Composite container;
 
 	/** Selected project in the workspace. */
 	private IProject selectedProject;
 
 	/** Text field for the name of the project. */
 	private Text projectField;
 
 	/** Text field for the name of the model. */
 	private Text nameField;
 
 	/** Text field for the base package for this project. */
 	private Text basePackageField;
 
 	/** Constructor. */
 	public CreateNewRequirementsModelBasicWizardPage() {
		super(Messages.getString("gui.createRequirementsModelWizard.basicPage.title")); //$NON-NLS-1$
		setTitle(Messages.getString("gui.createRequirementsModelWizard.basicPage.title")); //$NON-NLS-1$
 	}
 
 	/** @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite) */
 	@Override
 	public void createControl(Composite parent) {
 		LogUtil.log.debug("Initializing \"Basic\" page for the \"Create New Requirements Model\" wizard."); //$NON-NLS-1$
 
 		// Checks that something is selected in the projects tree and determines the selected project.
 		Object selection = selectionService.getSelection(UnagiProjectExplorerView.VIEW_ID);
 		if ((selection != null) && (selection instanceof AbstractProjectTreeElement)) {
 			AbstractProjectTreeElement treeElem = (AbstractProjectTreeElement) selection;
 			selectedProject = treeElem.getProject();
 		}
 
 		// Creates a new container using a grid layout with 2 columns.
 		container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 2;
 
 		// Creates the label for the project field.
 		Label projectLabel = new Label(container, SWT.NULL);
		projectLabel.setText(Messages.getString("gui.createRequirementsModelWizard.basicPage.field.project")); //$NON-NLS-1$
 
 		// Creates the project field, which is not editable (project is already selected before).
 		projectField = new Text(container, SWT.BORDER | SWT.SINGLE);
 		projectField.setEditable(false);
 		projectField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		projectField.setText(selectedProject.getName());
 
 		// Creates the label for the name field.
 		Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(Messages.getString("gui.createRequirementsModelWizard.basicPage.field.name")); //$NON-NLS-1$
 
 		// Creates the name field and focuses on it.
 		nameField = new Text(container, SWT.BORDER | SWT.SINGLE);
 		nameField.setText(""); //$NON-NLS-1$
 		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		nameField.forceFocus();
 
 		// Listen to key release events to check if the user is finished with the page.
 		nameField.addKeyListener(this);
 
 		// Creates the label for the base package field.
 		Label basePackageLabel = new Label(container, SWT.NULL);
		basePackageLabel.setText(Messages.getString("gui.createRequirementsModelWizard.basicPage.field.basePackage")); //$NON-NLS-1$
 
 		// Creates the base package field.
 		basePackageField = new Text(container, SWT.BORDER | SWT.SINGLE);
 		basePackageField.setText(""); //$NON-NLS-1$
 		basePackageField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		// Listen to key release events to check if the user is finished with the page.
 		basePackageField.addKeyListener(this);
 
 		// Sets the created container as the control for this page so Eclipse is able to integrate this page to the wizard.
 		setControl(container);
 
 		// Indicates that the user is not yet finished with this page.
 		setPageComplete(false);
 	}
 
 	/** Getter for selectedProject. */
 	protected IProject getSelectedProject() {
 		return selectedProject;
 	}
 
 	/**
 	 * Returns the name of the model, which was filled in by the user in the name field.
 	 * 
 	 * @return A string containing whatever the user filled in the name field.
 	 */
 	protected String getModelName() {
 		return nameField.getText();
 	}
 
 	/**
 	 * Returns the base package of the model, which was filled in by the user in the base package field.
 	 * 
 	 * @return A string containing whatever the user filled in the base package field.
 	 */
 	protected String getBasePackage() {
 		return basePackageField.getText();
 	}
 
 	/** @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent) */
 	@Override
 	public void keyPressed(KeyEvent e) {}
 
 	/** @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent) */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// FIXME: check if the name has no space and the file doesn't exist. Add an Eclipse-like error to the field when
 		// there's a problem.
 		// The page is complete if the project is selected and both name and base package are filled.
 		setPageComplete((selectedProject != null) && (!nameField.getText().isEmpty()) && (!basePackageField.getText().isEmpty()));
 	}
 }
