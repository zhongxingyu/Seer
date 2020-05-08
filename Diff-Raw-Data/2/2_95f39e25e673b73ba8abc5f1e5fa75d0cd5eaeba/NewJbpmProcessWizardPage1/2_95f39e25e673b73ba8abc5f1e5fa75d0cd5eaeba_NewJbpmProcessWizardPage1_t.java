 package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wizards;
 
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.validation.SyntaxCheckerUtils;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.JBPM5RuntimeExtension;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 /**
  * The "New" wizard page allows setting the container for the new file as well
  * as the file name. The page will only accept file name without the extension
  * OR with the extension that matches the expected one (bpmn).
  */
 
 public class NewJbpmProcessWizardPage1 extends WizardPage {
 	private Text containerText;
 	private Text fileText;
 	private Text nameText;
 	private Text processIdText;
 	private Text packageText;
 	private Button isJbpmRuntimeCheckbox;
 	private ISelection selection;
 
 	/**
 	 * Constructor for SampleNewWizardPage.
 	 * 
 	 * @param pageName
 	 */
 	public NewJbpmProcessWizardPage1(ISelection selection) {
 		super("wizardPage");
 		setTitle("JBPM Process Editor File");
 		setDescription("This wizard creates a new JBPM Process file.");
 		this.selection = selection;
 	}
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;
 		Label label;
 		GridData gridData;
 		
 		label = new Label(container, SWT.NULL);
 		label.setText("Process &name:");
 		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1);
 		nameText.setLayoutData(gridData);
 		nameText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
				fileText.setText(nameText.getText() + ".bpmn2");
 				String processid = packageText.getText() + "." + nameText.getText();
 				processid = SyntaxCheckerUtils.toNCName(processid.replaceAll(" ", "_"));
 				processIdText.setText(processid);
 				dialogChanged();
 			}
 		});
 
 		label = new Label(container, SWT.NULL);
 		label.setText("&Package:");
 		packageText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1);
 		packageText.setLayoutData(gridData);
 		packageText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				String processid = packageText.getText() + "." + nameText.getText();
 				processid = SyntaxCheckerUtils.toNCName(processid.replaceAll(" ", "_"));
 				processIdText.setText(processid);
 				dialogChanged();
 			}
 		});
 
 		label = new Label(container, SWT.NULL);
 		label.setText("Process &ID:");
 		processIdText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1);
 		processIdText.setLayoutData(gridData);
 		processIdText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		
 		label = new Label(container, SWT.NULL);
 		label.setText("&Container:");
 		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gridData = new GridData(GridData.FILL_HORIZONTAL);
 		containerText.setLayoutData(gridData);
 		containerText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 
 		Button button = new Button(container, SWT.PUSH);
 		button.setText("Browse...");
 		button.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowse();
 			}
 		});
 		label = new Label(container, SWT.NULL);
 		label.setText("&File name:");
 
 		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1);
 		fileText.setLayoutData(gridData);
 		fileText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		
 		isJbpmRuntimeCheckbox = new Button(container, SWT.CHECK);
 		isJbpmRuntimeCheckbox.setText("Set jBPM Runtime as the default for this project.");
 		isJbpmRuntimeCheckbox.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				dialogChanged();
 			}
 		});
 		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);
 		isJbpmRuntimeCheckbox.setLayoutData(gridData);
 
 		initialize();
 		dialogChanged();
 		setControl(container);
 	}
 
 	/**
 	 * Tests if the current workbench selection is a suitable container to use.
 	 */
 
 	private void initialize() {
 		IContainer container = null;
 		if (selection != null && selection.isEmpty() == false
 				&& selection instanceof IStructuredSelection) {
 			IStructuredSelection ssel = (IStructuredSelection) selection;
 			if (ssel.size() > 1)
 				return;
 			Object obj = ssel.getFirstElement();
 			// The selected TreeElement could be a JavaProject, which is adaptable
 			if (!(obj instanceof IResource) && obj instanceof IAdaptable) {
 				obj = ((IAdaptable)obj).getAdapter(IResource.class);
 			}
 			if (obj instanceof IResource) {
 				if (obj instanceof IContainer)
 					container = (IContainer) obj;
 				else
 					container = ((IResource) obj).getParent();
 				containerText.setText(container.getFullPath().toString());
 			}
 		}
 		String basename = "new_process";
 		String filename = basename + ".bpmn";
 		if (container!=null) {
 			int i = 1;
 			while (container.findMember(filename)!=null) {
 				filename = basename + "_" + i + ".bpmn";
 				++i;
 			}
 		}
 		fileText.setText(filename);
 		nameText.setText("New Process");
 		processIdText.setText("com.sample.bpmn");
 		packageText.setText("defaultPackage");
 	}
 
 	/**
 	 * Uses the standard container selection dialog to choose the new value for
 	 * the container field.
 	 */
 
 	private void handleBrowse() {
 		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
 				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
 				"Select new file container");
 		if (dialog.open() == ContainerSelectionDialog.OK) {
 			Object[] result = dialog.getResult();
 			if (result.length == 1) {
 				containerText.setText(((Path) result[0]).toString());
 			}
 		}
 	}
 
 	/**
 	 * Ensures that both text fields are set.
 	 */
 
 	private void dialogChanged() {
 		IResource container = ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(new Path(getContainerName()));
 		String fileName = getFileName();
 
 		if (getContainerName().length() == 0) {
 			updateStatus("File container must be specified");
 			return;
 		}
 		if (container == null
 				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
 			updateStatus("File container must exist");
 			return;
 		}
 		if (!container.isAccessible()) {
 			updateStatus("Project must be writable");
 			return;
 		}
 		if (fileName.length() == 0) {
 			updateStatus("File name must be specified");
 			return;
 		}
 		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
 			updateStatus("File name must be valid");
 			return;
 		}
 		int dotLoc = fileName.lastIndexOf('.');
 		if (dotLoc != -1) {
 			String ext = fileName.substring(dotLoc + 1);
 			if (!ext.equalsIgnoreCase("bpmn") && !ext.equalsIgnoreCase("bpmn2")) {
 				updateStatus("File extension must be \"bpmn\" or \"bpmn2\"");
 				return;
 			}
 			if ( ((IContainer)container).findMember(fileName)!=null ) {
 				updateStatus("File \""+fileName+"\"already exists");
 				return;
 			}
 		}
 		String packageName = packageText.getText();
 		if (!isValidPackageName(packageName)) {
 			updateStatus("Package name is not valid");
 			return;
 		}
 		String processId = processIdText.getText();
 		if (!isValidPackageName(processId)) {
 			updateStatus("Process ID is not valid");
 			return;
 		}
 		
 		String runtimeId = null;
 		if (container instanceof IProject) {
 			Bpmn2Preferences prefs = Bpmn2Preferences.getInstance((IProject)container);
 			if (prefs!=null) {
 				TargetRuntime rt = prefs.getRuntime();
 				runtimeId = rt.getId();
 			}						
 		}
 		if (JBPM5RuntimeExtension.JBPM5_RUNTIME_ID.equals(runtimeId)) {
 			isJbpmRuntimeCheckbox.setSelection(true);
 			isJbpmRuntimeCheckbox.setEnabled(false);
 		}
 		else {
 			isJbpmRuntimeCheckbox.setSelection(true);
 			isJbpmRuntimeCheckbox.setEnabled(true);
 		}
 
 		updateStatus(null);
 	}
 	
 	private boolean isValidPackageName(String name) {
 		if (name==null || name.isEmpty())
 			return false;
 		if (! Character.isJavaIdentifierStart(name.charAt(0)))
 			return false;
 		char last = 0;
 		for (char c : name.toCharArray()) {
 			if (c=='.') {
 				if (last=='.')
 					return false;
 			}
 			else if ( !Character.isJavaIdentifierPart(c))
 				return false;
 			last = c;
 		}
 		return true;
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	public IProject getProject() {
 		IResource container = ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(new Path(getContainerName()));
 		if (container instanceof IProject)
 			return (IProject)container;
 		return null;
 	}
 	
 	public String getContainerName() {
 		return containerText.getText();
 	}
 
 	public String getFileName() {
 		return fileText.getText();
 	}
 
 	public String getProcessName() {
 		return nameText.getText();
 	}
 
 	public String getProcessId() {
 		return processIdText.getText();
 	}
 
 	public String getPackageName() {
 		return packageText.getText();
 	}
 
 	public boolean isSetJbpmRuntime() {
 		return isJbpmRuntimeCheckbox.getSelection();
 	}
 }
