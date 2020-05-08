 package chameleon.eclipse.project;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class ProjectDetailsPage extends WizardPage implements IWizardPage {
 //public class ProjectDetailsPage extends WizardNewProjectCreationPage implements IWizardPage {
 
 	public ProjectDetailsPage(String pageName, ProjectWizard chameleonProjectWizard) {
 		super(pageName);
 		_chameleonProjectWizard = chameleonProjectWizard;
 	}
 
 	private ProjectWizard chameleonProjectWizard() {
 		return _chameleonProjectWizard;
 	}
 
 	private final ProjectWizard _chameleonProjectWizard;
 	
 	public Text projectTitle;
 	
 	public Text projectPath;
 
 	private Composite controlContainer;
 
 	private Button addDirectoryButton;
 	
 	private boolean customLocation() {
 		return projectPath.isEnabled();
 	}
 	
 	private void useCustomLocation() {
 		projectPath.setEnabled(true);
 		addDirectoryButton.setEnabled(true);
 	}
 	
 	private void useDefaultLocation() {
 		projectPath.setEnabled(false);
 		syncLocation();
 //		setPathColor(black());
 	}
 	
 	private void syncLocation() {
 		if(! customLocation()) {
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			IWorkspaceRoot workspaceRoot = workspace.getRoot();
 			String rootString = workspaceRoot.getLocation().toOSString();
 			projectPath.setText(rootString + File.separator + projectName());
 		}
 	}
 	
 	public void createControl(Composite parent) {
 		controlContainer = new Composite(parent,SWT.NONE);
 		GridLayout gl = new GridLayout();
 		gl.numColumns = 3;
 		controlContainer.setLayout(gl);
 		Label nameLabel = new Label(controlContainer, SWT.NONE);
 		nameLabel.setText("Project name:");
 		projectTitle = new Text(controlContainer,SWT.BORDER);
 		GridData projectTitleGridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_CENTER, true, false);
 		projectTitleGridData.horizontalSpan = 2;
 		projectTitle.setLayoutData(projectTitleGridData);
 		projectTitle.setText("MyProject");
 
 		projectTitle.addModifyListener(new ModifyListener(){
 			@Override
 			public void modifyText(ModifyEvent arg0) {
 				syncName();
 			}
 		});
 		
 		Button checkBox = new Button(controlContainer,SWT.CHECK);
 		checkBox.setSelection(true);
 		checkBox.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 				if(customLocation()) {
 					useDefaultLocation();
 				} else {
 					useCustomLocation();
 				}
 			}
 		});
 		Label customLabel = new Label(controlContainer, SWT.NONE);
 		customLabel.setText("Use default project location");
 		GridData customLabelTitleGridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_CENTER, true, false);
 		customLabelTitleGridData.horizontalSpan = 2;
 		customLabel.setLayoutData(customLabelTitleGridData);
 
 		
 		Label pathLabel = new Label(controlContainer, SWT.NONE);
 		pathLabel.setText("Project path:");
 		projectPath = new Text(controlContainer,SWT.BORDER);
 		projectPath.setEnabled(false);
 		syncLocation();
 		projectPath.addModifyListener(new ModifyListener(){
 		
 			@Override
 			public void modifyText(ModifyEvent arg0) {
 				syncRoot();
 			}
 		});
 
 		addDirectoryButton = new Button(controlContainer,SWT.PUSH);
 		addDirectoryButton.setText("Browse");
 		addDirectoryButton.setEnabled(false);
 		GridData directoryButtonGridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
 		addDirectoryButton.setLayoutData(directoryButtonGridData);
 		addDirectoryButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
 				@Override
 				public void mouseDown(MouseEvent arg0) {
 					DirectoryDialog sourceDirectoryDialog = new DirectoryDialog(getShell());
 					sourceDirectoryDialog.setMessage("Select a source directory");
 					IPath workspacePath = chameleonProjectWizard().workspacePath();
 					sourceDirectoryDialog.setFilterPath(workspacePath.toString());
 					String absoluteDirectory = sourceDirectoryDialog.open();
 					if(absoluteDirectory != null) {
 						projectPath.setText(absoluteDirectory);
 					}
 				}
 		});		
 		setControl(controlContainer);
 
 		getControl().addFocusListener(new FocusListener(){
 			
 			@Override
 			public void focusLost(FocusEvent arg0) {
 			}
 		
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				syncName();
 				syncRoot();
 			}
 
 		});
 	}
 
 	protected void syncName() {
 		chameleonProjectWizard().setName(projectName());
 		syncLocation();
 	}
 	
 	protected void syncRoot() {
 		File file = new File(projectPath.getText());
 //		if(customLocation()) {
 //			if(! (file.exists() && file.isDirectory())) {
 //				setPathColor(red());
 //			} else {
 //				setPathColor(black());
 //			}
 //		}
 		chameleonProjectWizard().setRoot(projectPath.getText());
 	}
 
 //	protected void setPathColor(Color color) {
 //		projectPath.setForeground(color);
 //	}
 //	
 //	protected Color red() {
 //		return new Color(projectPath.getDisplay(), 200, 90, 90);
 //	}
 //
 //	protected Color black() {
 //		return new Color(projectPath.getDisplay(), 0, 0, 0);
 //	}
 
 	public String projectName() {
 		return projectTitle.getText();
 	}
 	
 	public String projectPath() {
 		return projectPath.getText();
 	}
 	
 	public File projectDirectory() {
 		File result = new File(projectPath());
 		if(result.exists()) {
 			if(result.isFile()) {
 				result = null;
 			}
 		} else {
 			result.mkdirs();
 		}
 		return result;
 	}
 
 }
