 package com.vectorsf.jvoice.ui.wizard.page;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.ide.IDE;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.model.base.JVBean;
 import com.vectorsf.jvoice.model.base.JVPackage;
 import com.vectorsf.jvoice.model.base.JVProject;
 
 public class DslNameWizardPage extends AbstractWizardPage {
 	
 	private static final String PAGE_DESC = "Enter a locution name";
 	private static final String PAGE_TITLE = "Create a Locution";
 
 	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
 
 	Text textFieldDSL;
 	Text textFieldProject;
 	Text textFieldPackage;
 	
 
 	private Button browsePackage;
 	private Combo box;
 	
 	private Listener nameModifyListener = new Listener() {
 		@Override
 		public void handleEvent(Event e) {
 			boolean valid = validatePage();
 			setPageComplete(valid);
 
 		}
 	};
 	private Object selection;
 	private String initialFolder = "";
 	private String initialProject = "";
 	private String configuration="configuration{\n //setting variables \n}\n";
 	private String grammars="grammars{\n // User's grammar \n }\n";
 	private String outputs="outputs{\n //Variables \n}";
 	private String audios="audios{\n //Code \n}";
 	private String variables="variables{\n // User's variables \n}\n";
 	
 	
 
 	public DslNameWizardPage(String pageName, String title,
 			ImageDescriptor titleImage) {
 		super(pageName, title, titleImage);
 	}
 
 	public DslNameWizardPage(String pageName) {
 		super(pageName);
 		setTitle(PAGE_TITLE);
 		setDescription(PAGE_DESC);
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL);
 		composite.setFont(parent.getFont());
 
 		initializeDialogUnits(parent);
 
 		composite.setLayout(new GridLayout());
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		createWizardContents(composite);
 
 		// Show description on opening
 		setErrorMessage(null);
 		setMessage(null);
 
 		setPageComplete(validatePage());
 
 		setControl(composite);
 	}
 
 	public String getText() {
 		if (textFieldDSL == null) {
 			return getInitialTextFieldValue();
 		}
 
 		return getTextFieldValue();
 	}
 
 	protected boolean validatePage() {
 		String text = getTextFieldValue();
 		if (text.isEmpty()) {
 			setErrorMessage(null);
 			setMessage("Enter a locution name");
 			return false;
 		}
 
 		String projectName = getProjectFieldValue();
 		if (projectName.isEmpty()) {
 			setErrorMessage("Enter a project name");
 			browsePackage.setEnabled(false);
 			return false;
 		}
 
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceRoot root = workspace.getRoot();
 
 		if (!root.getProject(projectName).exists()) {
 			setErrorMessage("Project does not exist");
 			browsePackage.setEnabled(false);
 			return false;
 		}
 
 		JVProject proyecto = BaseModel.getInstance().getModel()
 				.getProject(projectName);
 		if (proyecto == null) {
 			setErrorMessage("Project is not jvoice project");
 			browsePackage.setEnabled(false);
 			return false;
 		}
 
 		IStatus status = doWorkspaceValidation(workspace, text);
 		if (!status.isOK()) {
 			setErrorMessage(status.getMessage());
 			return false;
 		}
 
 		String packageName = getPackageFieldValue();
 		browsePackage.setEnabled(true);
 
 		if (packageName.isEmpty()) {
 			setErrorMessage("Enter a package name");
 			return false;
 		}
 
 		JVPackage paquete = proyecto.getPackage(packageName);
 
 		if (paquete == null) {
 			setErrorMessage("Package does not exist");
 			return false;
 		}
 
 		JVBean voiceDsl = paquete.getBean(text);
 
 		if (voiceDsl != null) {
 			setErrorMessage("Locution already exists");
 			return false;
 		}
 
 		setSelection(paquete);
 		setErrorMessage(null);
 		setMessage(null);
 		return true;
 	}
 
 	@Override
 	protected void createWizardContents(Composite parent) {
 		createProjectNameGroup(parent);
 	}
 
 	private final void createProjectNameGroup(Composite parent) {
 
 		JVProject project = null;
 		JVPackage DSLFolder = null;
 		if (selection instanceof IProject) {
 			IProject iProject = (IProject) selection;
 			project = BaseModel.getInstance().getModel()
 					.getProject(iProject.getName());
 		} else if (selection instanceof IFolder) {
 			IFolder folder = (IFolder) selection;
 			IProject iProject = folder.getProject();
 			project = BaseModel.getInstance().getModel()
 					.getProject(iProject.getName());
 
 			// si el proyecto es nulo es porque el proyecto donde se quiere
 			// crear el flujo no
 			// es un proyecto jvoice
 			if (project != null) {
 				DSLFolder = project.getPackage(folder.getName());
 			}
 		} else if (selection instanceof JVProject) {
 			project = (JVProject) selection;
 		} else if (selection instanceof JVPackage) {
 			DSLFolder = (JVPackage) selection;
 			project = DSLFolder.getOwnerProject();
 		} else if (selection instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot prueba = (IPackageFragmentRoot) selection;
 			IFolder folder = (IFolder) prueba.getResource();
 			IProject iProject = folder.getProject();
 			project = BaseModel.getInstance().getModel()
 					.getProject(iProject.getName());
 			DSLFolder = project.getPackage(folder.getName());
 		}
 
 		initialFolder = DSLFolder != null ? DSLFolder.getName() : "";
 		initialProject = project != null ? project.getName() : "";
 
 		Composite projectGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		projectGroup.setLayout(layout);
 		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		// new project label
 		Label projectLabel = new Label(projectGroup, SWT.NONE);
 		projectLabel.setText("DSL name:");
 		projectLabel.setFont(parent.getFont());
 
 		// new project name entry field
 		textFieldDSL = new Text(projectGroup, SWT.BORDER);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
 		data.horizontalSpan = 2;
 		textFieldDSL.setLayoutData(data);
 		textFieldDSL.setFont(parent.getFont());
 
 		
 		// new project label
 		Label projectLabel2 = new Label(projectGroup, SWT.NONE);
 		projectLabel2.setText("Project:");
 		projectLabel2.setFont(parent.getFont());
 
 		textFieldProject = new Text(projectGroup, SWT.BORDER);
 		GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
 		data2.widthHint = SIZING_TEXT_FIELD_WIDTH;
 		textFieldProject.setLayoutData(data2);
 		textFieldProject.setFont(parent.getFont());
 		textFieldProject.setEditable(false);
 
 		// browse button on right
 		Button browse = new Button(projectGroup, SWT.PUSH);
 		browse.setText("Browse...");
 		browse.addListener(SWT.Selection, new Listener() {
 
 			@Override
 			public void handleEvent(Event event) {
 				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
 						getShell(),
 						new AdapterFactoryLabelProvider(
 								new ComposedAdapterFactory(
 										ComposedAdapterFactory.Descriptor.Registry.INSTANCE)));
 				dialog.setTitle("Container Selection");
 				dialog.setAllowDuplicates(false);
 				dialog.setMultipleSelection(false);
 
 				dialog.setElements(llenarProyectos().toArray());
 				dialog.open();
 				Object[] result = dialog.getResult();
 				if (result != null && result.length == 1) {
 					JVProject projecto = (JVProject) result[0];
 					setSelection(projecto);
 					textFieldProject.setText(projecto.getName());
 				} else {
 					textFieldProject.setText("");
 				}
 			}
 		});
 
 		// new project label
 		Label projectLabel3 = new Label(projectGroup, SWT.NONE);
 		projectLabel3.setText("Package:");
 		projectLabel3.setFont(parent.getFont());
 
 		textFieldPackage = new Text(projectGroup, SWT.BORDER);
 		GridData data3 = new GridData(GridData.FILL_HORIZONTAL);
 		data3.widthHint = SIZING_TEXT_FIELD_WIDTH;
 		textFieldPackage.setLayoutData(data3);
 		textFieldPackage.setFont(parent.getFont());
 		textFieldPackage.setEditable(false);
 
 		// browse button on right
 		browsePackage = new Button(projectGroup, SWT.PUSH);
 		browsePackage.setText("Browse...");
 		browsePackage.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
 						getShell(),
 						new AdapterFactoryLabelProvider(
 								new ComposedAdapterFactory(
 										ComposedAdapterFactory.Descriptor.Registry.INSTANCE)));
 				dialog.setTitle("Container Selection");
 				dialog.setAllowDuplicates(false);
 				dialog.setMultipleSelection(false);
 
 				dialog.setElements(llenarPaquetes().toArray());
 				dialog.open();
 				Object[] result = dialog.getResult();
 				if (result != null && result.length == 1) {
 					JVPackage paquete = (JVPackage) result[0];
 					setSelection(paquete);
 					textFieldPackage.setText(paquete.getName());
 				} else {
 					textFieldPackage.setText("");
 				}
 			}
 		});
 
 		browsePackage.setEnabled(false);
 		
 		//Label del Combo
 		Label ComboLabel4 = new Label(projectGroup, SWT.NONE);
 		ComboLabel4.setText("Select an option:");
 		ComboLabel4.setFont(parent.getFont());
 		
 		//Combo para indicar que clase de DSL queremos crear.
 		box = new Combo(projectGroup, SWT.READ_ONLY);
 		box.add("Menu");
 		box.add("Input");
 		box.add("Output");
 		box.select(0);
 		
 
 
 		// Set the initial value first before listener
 		// to avoid handling an event during the creation.
 		if (getInitialTextFieldValue() != null) {
 			textFieldDSL.setText(getInitialTextFieldValue());
 		}
 		textFieldDSL.addListener(SWT.Modify, nameModifyListener);
 
 		textFieldProject.setText(getInitialTextProjectFieldValue());
 		textFieldProject.addListener(SWT.Modify, nameModifyListener);
 
 		textFieldPackage.setText(getInitialTextFolderFieldValue());
 		textFieldPackage.addListener(SWT.Modify, nameModifyListener);
 	}
 
 	private String getTextFieldValue() {
 		if (textFieldDSL == null) {
 			return ""; //$NON-NLS-1$
 		}
 
 		return textFieldDSL.getText().trim();
 	}
 
 	private String getInitialTextFieldValue() {
 		return "newLocution"; //$NON-NLS-1$
 	}
 
 	private String getInitialTextFolderFieldValue() {
 		return initialFolder;
 	}
 
 	private String getInitialTextProjectFieldValue() {
 		return initialProject;
 	}
 
 	private IStatus doWorkspaceValidation(IWorkspace workspace, String text) {
 		IStatus ret = workspace.validateName(text, IResource.FILE);
 		return ret;
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			textFieldDSL.setFocus();
 			textFieldDSL.selectAll();
 		}
 	}
 
 	public void setSelection(Object firstElement) {
 		selection = firstElement;
 
 	}
 
 	public Object getSelection() {
 		return selection;
 	}
 
 	private List<JVProject> llenarProyectos() {
 
 		List<JVProject> input = new ArrayList<JVProject>();
 
 		for (int i = 0; i < ResourcesPlugin.getWorkspace().getRoot()
 				.getProjects().length; i++) {
 			IProject proyecto = ResourcesPlugin.getWorkspace().getRoot()
 					.getProjects()[i];
 			String nombreProyecto = proyecto.getName();
 			JVProject project = BaseModel.getInstance().getModel()
 					.getProject(nombreProyecto);
 			if (project != null) {
 				input.add(project);
 			}
 		}
 		return input;
 	}
 
 	private List<JVPackage> llenarPaquetes() {
 
 		IProject proyecto = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject(textFieldProject.getText());
 
 		JVProject project = BaseModel.getInstance().getModel()
 				.getProject(proyecto.getName());
 
 		return project.getPackages();
 	}
 
 	private String getProjectFieldValue() {
 		if (textFieldProject == null) {
 			return ""; //$NON-NLS-1$
 		}
 
 		return textFieldProject.getText().trim();
 	}
 
 	private String getPackageFieldValue() {
 		if (textFieldPackage == null) {
 			return ""; //$NON-NLS-1$
 		}
 
 		return textFieldPackage.getText().trim();
 	}
 
 	
 	//Equivalente al performFinish
 	@Override
 	public void createResource() throws CoreException {
 		String DslName = getText();
 		IProject project = null;
 		IFolder DslFolder = null;
 		String editorExtension = "voiceDsl";
 		IFile DslFile =null;
 		String contents ="";
 		String seleccion= box.getText();
 
 		Object element = getSelection();
 		if (element instanceof JVProject) {
 			project = (IProject) Platform.getAdapterManager().getAdapter(
 					element, IProject.class);
 		} else if (element instanceof JVPackage) {
 			DslFolder = (IFolder) Platform.getAdapterManager().getAdapter(
 					element, IFolder.class);
 			project = DslFolder.getProject();
 		}
 
 		if (project == null || !project.isAccessible()) {
 			String error = "No Project Found";
 			IStatus status = new Status(IStatus.ERROR, "1", error);
 			ErrorDialog.openError(getShell(), error, null, status);
 			throw new CoreException(status);
 		}
 		
 		
 		if (DslFolder != null){
 			DslFile= DslFolder.getFile(DslName + "." + editorExtension);
 			
 			if (!DslFile.exists()) {
 				if (seleccion.equals("Menu")){
 					contents="menuname "+DslName+ "\n\n"
 							+configuration
 							+grammars
 							+audios
 							+outputs;
 					
 					
 					
 				}else if(seleccion.equals("Input")){
 					contents="inputname "+DslName+"\n\n"
 							+configuration
 							+grammars
 							+audios;
 					
 				}else if(seleccion.equals("Output")){
 					contents="outputname "+DslName+"\n\n"
 							+variables
 							+audios;
 					
 				}
 				
 				
 				InputStream source = new ByteArrayInputStream(contents.getBytes());
 				DslFile.create(source, false, null);
 			}
 
 		}
 		
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IFile file = (IFile) Platform.getAdapterManager().getAdapter(DslFile, IFile.class);
 
 
 		try {
 			IDE.openEditor(page, file);
 		} catch (PartInitException e) {
 			String error = "error open editor";
 			IStatus status = new Status(IStatus.ERROR, "0", error, e);
 			ErrorDialog.openError(getShell(), error, null, status);
 			throw new CoreException(status);
 		}
 	}
 
 }
