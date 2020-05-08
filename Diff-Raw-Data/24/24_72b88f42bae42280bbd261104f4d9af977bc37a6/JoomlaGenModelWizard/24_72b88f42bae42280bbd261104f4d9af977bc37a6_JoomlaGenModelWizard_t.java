 /**
  */
 package com.github.kanafghan.welipse.joomlagen.presentation;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ISetSelectionTarget;
 
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenFactory;
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenModel;
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenPackage;
 import com.github.kanafghan.welipse.webdsl.Website;
 
 /**
  * This is a simple wizard for creating a new model file.
  * <!-- begin-user-doc --> 
  * <!-- end-user-doc -->
  * @generated NOT
  */
 public class JoomlaGenModelWizard extends Wizard implements INewWizard {
 	/**
 	 * The supported extensions for created files.
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final List<String> FILE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(JoomlagenEditorPlugin.INSTANCE.getString("_UI_JoomlaGenEditorFilenameExtensions").split("\\s*,\\s*")));
 
 	/**
 	 * A formatted list of supported file extensions, suitable for display. 
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static final String FORMATTED_FILE_EXTENSIONS = JoomlagenEditorPlugin.INSTANCE.getString("_UI_JoomlaGenEditorFilenameExtensions").replaceAll("\\s*,\\s*", ", ");
 
 	/**
 	 * This caches an instance of the model package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected JoomlaGenPackage joomlaGenPackage = JoomlaGenPackage.eINSTANCE;
 
 	/**
 	 * This caches an instance of the model factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected JoomlaGenFactory joomlaGenFactory = joomlaGenPackage.getJoomlaGenFactory();
 
 	/**
 	 * This is the file creation page. 
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected JoomlaGenModelWizardNewFileCreationPage newFileCreationPage;
 
 	/**
 	 * This is the web model selection page. 
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected JoomlaGenModelWizardWebModelImporterPage webModelImporterPage;
 
 	/**
 	 * Remember the selection during initialization for populating the default container.
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected IStructuredSelection selection;
 
 	/**
 	 * Remember the workbench during initialization.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected IWorkbench workbench;
 	
 	protected boolean reload;
 	protected JoomlaGenModel originalGenModel;
 
 	/**
 	 * This just records the information.
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 
 		if (selection != null && !selection.isEmpty()) {
 			Object selected = selection.getFirstElement();
 			if (selected instanceof IFile) {
 				IFile file = (IFile) selected;
 				if (file.getFileExtension().equals("joomlagen")) {
 					setReload(true);
 					loadOriginalGenModel(file);
 				}
 			}
 		} else {
 			setReload(false);
 		}
 		
 		if (isReload()) {
 			setWindowTitle(JoomlagenEditorPlugin.INSTANCE.getString("_UI_Wizard_label_reload"));	
 		} else {			
 			setWindowTitle(JoomlagenEditorPlugin.INSTANCE.getString("_UI_Wizard_label"));
 		}
 		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(JoomlagenEditorPlugin.INSTANCE.getImage("full/wizban/NewJoomlaGen")));
 	}
 	
 	protected void loadOriginalGenModel(IFile modelFile) {
 		ResourceSet resourceSet = new ResourceSetImpl();
 		URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
 		Resource resource = resourceSet.getResource(fileURI, true);
 		EList<EObject> contents = resource.getContents();
 		
 		if (!contents.isEmpty()) {
 			EObject model = contents.get(0);
 			if (model instanceof JoomlaGenModel) {
 				originalGenModel = (JoomlaGenModel) model;
				// Adjust the GeModel
				originalGenModel.reconcile();
 			}
 		}
 	}
 
 	public boolean isReload() {
 		return reload;
 	}
 
 	public void setReload(boolean reload) {
 		this.reload = reload;
 	}
 
 	/**
 	 * Do the work after everything is specified.
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public boolean performFinish() {
 		try {
 			// Remember the file.
 			//
 			final IFile modelFile = getModelFile();
 
 			// Do the work within an operation.
 			//
 			WorkspaceModifyOperation operation =
 				new WorkspaceModifyOperation() {
 					@Override
 					protected void execute(IProgressMonitor progressMonitor) {
 						try {
 							Resource resource = null;
 							EObject genModel = webModelImporterPage.getGenModel();
 							if (genModel != null) {
 								if (isReload()) {
 									((JoomlaGenModel) genModel).reconcile(originalGenModel);
 								}
 								
 								resource = genModel.eResource();
 								if (resource == null) {
 									// Create a resource set
 									//
 									ResourceSet resourceSet = new ResourceSetImpl();
 
 									// Get the URI of the model file.
 									//
 									URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
 
 									// Create a resource for this file.
 									//
 									resource = resourceSet.createResource(fileURI);
 									resource.getContents().add(genModel);
 								}
 								
 								// Save the contents of the resource to the file system.
 								//
 								Map<Object, Object> options = new HashMap<Object, Object>();
 								options.put(XMLResource.OPTION_ENCODING, "UTF-8");
 								resource.save(options);
 							}							
 						}
 						catch (Exception exception) {
 							JoomlagenEditorPlugin.INSTANCE.log(exception);
 						}
 						finally {
 							progressMonitor.done();
 						}
 					}
 				};
 
 			getContainer().run(false, false, operation);
 
 			// Select the new file resource in the current view.
 			//
 			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
 			IWorkbenchPage page = workbenchWindow.getActivePage();
 			final IWorkbenchPart activePart = page.getActivePart();
 			if (activePart instanceof ISetSelectionTarget) {
 				final ISelection targetSelection = new StructuredSelection(modelFile);
 				getShell().getDisplay().asyncExec
 					(new Runnable() {
 						 public void run() {
 							 ((ISetSelectionTarget)activePart).selectReveal(targetSelection);
 						 }
 					 });
 			}
 
 			// Open an editor on the new file.
 			//
 			try {
 				page.openEditor
 					(new FileEditorInput(modelFile),
 					 workbench.getEditorRegistry().getDefaultEditor(modelFile.getFullPath().toString()).getId());					 	 
 			}
 			catch (PartInitException exception) {
 				MessageDialog.openError(workbenchWindow.getShell(), JoomlagenEditorPlugin.INSTANCE.getString("_UI_OpenEditorError_label"), exception.getMessage());
 				return false;
 			}
 
 			return true;
 		}
 		catch (Exception exception) {
 			JoomlagenEditorPlugin.INSTANCE.log(exception);
 			return false;
 		}
 	}
 
 	@Override
 	public boolean canFinish() {
 		return super.canFinish() && webModelImporterPage.isPageComplete();
 	}
 
 	/**
 	 * This is the one page of the wizard.
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public class JoomlaGenModelWizardNewFileCreationPage extends
 			WizardNewFileCreationPage {
 		/**
 		 * Pass in the selection.
 		 * <!-- begin-user-doc --> 
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public JoomlaGenModelWizardNewFileCreationPage(String pageId,
 				IStructuredSelection selection) {
 			super(pageId, selection);
 		}
 
 		/**
 		 * The framework calls this to see if the file is correct.
 		 * <!-- begin-user-doc --> 
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		@Override
 		protected boolean validatePage() {
 			if (super.validatePage()) {
 				String extension = new Path(getFileName()).getFileExtension();
 				if (extension == null || !FILE_EXTENSIONS.contains(extension)) {
 					String key = FILE_EXTENSIONS.size() > 1 ? "_WARN_FilenameExtensions" : "_WARN_FilenameExtension";
 					setErrorMessage(JoomlagenEditorPlugin.INSTANCE.getString(key, new Object [] { FORMATTED_FILE_EXTENSIONS }));
 					return false;
 				}
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * <!-- begin-user-doc --> 
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public IFile getModelFile() {
 			return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
 		}
 	}
 
 	/**
 	 * This is the page where Web model is imported and loaded. 
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public class JoomlaGenModelWizardWebModelImporterPage extends WizardPage
 			implements Listener {
 
 		protected Text uriText;
 		protected Button browseFileSystemButton;
 		protected Button browseWorkspaceButton;
 
 		protected Button loadButton;
 
 		protected String[] filterExtensions;
 		
 		protected JoomlaGenModel genModel;
 
 		/**
 		 * Pass in the selection. 
 		 * <!-- begin-user-doc --> 
 		 * <!-- end-user-doc -->
 		 * 
 		 * @generated NOT
 		 */
 		public JoomlaGenModelWizardWebModelImporterPage(String pageId) {
 			super(pageId);
 			setPageComplete(false);
 			genModel = JoomlaGenFactory.eINSTANCE.createJoomlaGenModel();
 		}
 		
 		@Deprecated
 		public void initGenModel(IFile modelFile) {
 			ResourceSet resourceSet = new ResourceSetImpl();
 			URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
 			Resource resource = resourceSet.getResource(fileURI, true);
 			EList<EObject> contents = resource.getContents();
 			
 			if (!contents.isEmpty()) {
 				EObject model = contents.get(0);
 				if (model instanceof JoomlaGenModel) {
 					genModel = (JoomlaGenModel) model;
 				}
 			}
 		}
 		
 		/**
 		 * <!-- begin-user-doc --> 
 		 * <!-- end-user-doc -->
 		 * 
 		 * @generated NOT
 		 */
 		public void createControl(Composite parent) {
 			Composite composite = new Composite(parent, SWT.NONE);
 			composite.setLayoutData(new GridData(GridData.FILL_BOTH
 					| GridData.GRAB_VERTICAL));
 
 			GridLayout layout = new GridLayout();
 			layout.verticalSpacing = 8;
 			composite.setLayout(layout);
 
 			createURIControl(composite);
 			setControl(composite);
 		}
 
 		protected void createURIControl(Composite parent) {
 			Composite composite = new Composite(parent, SWT.NONE);
 			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
 					| GridData.GRAB_HORIZONTAL));
 			{
 				FormLayout layout = new FormLayout();
 				layout.marginTop = 10;
 				layout.spacing = 10;
 				composite.setLayout(layout);
 			}
 
 			Label uriLabel = new Label(composite, SWT.LEFT);
 			{
 				FormData data = new FormData();
 				data.left = new FormAttachment(0);
 				uriLabel.setLayoutData(data);
 			}
 			uriLabel.setText("Model &URI:");
 
 			Composite uriComposite = new Composite(composite, SWT.NONE);
 			{
 				FormData data = new FormData();
 				data.top = new FormAttachment(uriLabel, 5);
 				data.left = new FormAttachment(0);
 				data.right = new FormAttachment(100);
 				uriComposite.setLayoutData(data);
 
 				GridLayout layout = new GridLayout(2, false);
 				layout.marginTop = -5;
 				layout.marginLeft = -5;
 				layout.marginRight = -5;
 				uriComposite.setLayout(layout);
 			}
 
 			Composite buttonComposite = new Composite(composite, SWT.NONE);
 			{
 				FormData data = new FormData();
 				data.top = new FormAttachment(uriLabel, 0, SWT.CENTER);
 				data.left = new FormAttachment(uriLabel, 0);
 				data.right = new FormAttachment(100);
 				buttonComposite.setLayoutData(data);
 
 				FormLayout layout = new FormLayout();
 				layout.marginTop = 0;
 				layout.marginBottom = 0;
 				layout.marginLeft = 0;
 				layout.marginRight = 0;
 				layout.spacing = 5;
 				buttonComposite.setLayout(layout);
 			}
 
 			browseFileSystemButton = new Button(buttonComposite, SWT.PUSH);
 			browseFileSystemButton.setText("Browse &File System...");
 			browseFileSystemButton.addListener(SWT.Selection, this);
 
 			browseWorkspaceButton = new Button(buttonComposite, SWT.PUSH);
 			browseWorkspaceButton.setText("Browse &Workspace...");
 			browseWorkspaceButton.addListener(SWT.Selection, this);
 
 			{
 				FormData data = new FormData();
 				data.right = new FormAttachment(browseWorkspaceButton);
 				browseFileSystemButton.setLayoutData(data);
 			}
 
 			{
 				FormData data = new FormData();
 				data.right = new FormAttachment(100);
 				browseWorkspaceButton.setLayoutData(data);
 			}
 
 			uriText = new Text(uriComposite, SWT.SINGLE | SWT.BORDER);
 			setURIText(getInitialURI());
 			if (uriText.getText().length() > 0) {
 				uriText.selectAll();
 			}
 			uriText.addListener(SWT.Modify, this);
 
 			loadButton = new Button(uriComposite, SWT.PUSH);
 			loadButton.setText("Load");
 			loadButton.setLayoutData(new GridData(GridData.END));
 			loadButton.addListener(SWT.Selection, this);
 			
 			{
 				GridData gridData = new GridData(GridData.FILL_HORIZONTAL
 						| GridData.GRAB_HORIZONTAL);
 				if (uriComposite.getChildren().length == 1) {
 					gridData.horizontalSpan = 2;
 				}
 				uriText.setLayoutData(gridData);
 			}
 			
 			adjustLoadButton();
 		}
 
 		private String getInitialURI() {			
 			if (selection != null && !selection.isEmpty()) {
 				Object selected = selection.getFirstElement();
 				if (selected instanceof IFile) {
 					IFile file = (IFile) selected;
 					if (file.getFileExtension().equals("webdsl")) {
 						return URI.createPlatformResourceURI(file.getFullPath().toString(), true).toString();
 					} else if (file.getFileExtension().equals("joomlagen")) {
 						if (originalGenModel != null) {
 							Website webmodel = originalGenModel.getWebmodel();
 							if (webmodel != null) {
 								Resource resource = webmodel.eResource();
 								if (resource != null) {
 									URI uri = resource.getURI();
 									if (uri != null) {										
 										return uri.toString();
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			
 			return "";
 		}
 
 		protected void adjustLoadButton() {
 			if (loadButton != null) {
 				String text = uriText.getText();
 				loadButton.setEnabled(text != null && text.trim().length() > 0);
 			}
 		}
 
 		protected void setURIText(String uri) {
 			if (uriText != null && uri != null) {				
 				uriText.setText(uri.trim());
 			}
 		}
 
 		@Override
 		public void handleEvent(Event event) {
 			if (event.type == SWT.Modify && event.widget == uriText) {
 				uriTextModified(uriText.getText().trim());
 			} else if (event.type == SWT.Selection
 					&& event.widget == browseFileSystemButton) {
 				browseFileSystem();
 			} else if (event.type == SWT.Selection
 					&& event.widget == browseWorkspaceButton) {
 				browseWorkspace();
 			} else if (event.type == SWT.Selection
 					&& event.widget == loadButton) {
 				refreshModel();
 				getContainer().updateButtons();
 			}
 			adjustLoadButton();
 		}
 
 		protected void refreshModel() {
 			try {
 				ResourceSetImpl resourceSet = new ResourceSetImpl();
 				Resource resource = resourceSet.getResource(getWebModelURI(), true);
 				
 				if (resource != null) {
 					if (!resource.isLoaded()) {
 						resource.load(Collections.emptyMap());
 					}
 					
 					if (resource.getErrors().isEmpty()) {
 						EList<EObject> contents = resource.getContents();
 						if (!contents.isEmpty()) {
 							EObject object = contents.get(0);
 							if (object instanceof Website) {
 								Website webModel = (Website) object;
 								genModel.setWebmodel(webModel);
 								if (genModel.getExtensionName() == null || genModel.getExtensionName().isEmpty()) {									
 									genModel.setExtensionName(webModel.getName());
 								}
 								setPageComplete(true);
 							} else {
 								throw new Exception("First object is not of type Website: "
 										+ object);
 							}
 						} else {
 							throw new Exception("Resource did not had any content: "
 									+ contents);
 						}
 					} else {
 						throw new Exception("Resouce contains errors: "+ resource.getErrors());
 					}
 				} else {
 					throw new Exception("Resource was null: "+ getWebModelURI().toString());
 				}
 			} catch (Exception exception) {
 				MessageDialog.openError(null, "Welipse", "An error occured while importing webmodel: "
 						+ exception.getMessage());
 				JoomlagenEditorPlugin.INSTANCE.log(exception);
 			}
 		}
 
 		protected URI getWebModelURI() {
 			String uri = uriText.getText(); 
 			if (!uri.isEmpty()) {
 				return URI.createURI(uri);
 			}
 			return null;
 		}
 		
 		protected void uriTextModified(String text) {
 			setErrorMessage(null);
 			setMessage(null);
 			setPageComplete(false);
 			genModel.setWebmodel(null);
 		}
 
 		protected boolean browseFileSystem() {
 			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN
 					| SWT.SINGLE);
 			fileDialog.setFilterExtensions(getFilterExtensions());
 
 			 URI modelURI = URI.createURI(getModelFile().toString());
 			 if (modelURI != null) {
 				 fileDialog.setFileName(modelURI.toFileString());
 			 }
 
 			if (fileDialog.open() != null
 					&& fileDialog.getFileNames().length > 0) {
 				String[] fileNames = fileDialog.getFileNames();
 				StringBuffer text = new StringBuffer();
 				for (int i = 0; i < fileNames.length; ++i) {
 					String filePath = fileDialog.getFilterPath()
 							+ File.separator + fileNames[i];
 					text.append(URI.createFileURI(filePath).toString());
 					text.append(" ");
 				}
 				setURIText(text.toString());
 				refreshModel();
 				return true;
 			}
 			return false;
 		}
 
 		protected boolean browseWorkspace() {
 			ViewerFilter extensionFilter = null;
 			if (getFilterExtensions().length > 0) {
 				extensionFilter = new ViewerFilter() {
 					@Override
 					public boolean select(Viewer viewer, Object parentElement, Object element) {
 						return !(element instanceof IFile)
 								|| getFilterExtensions()[0].contains(((IFile) element).getFileExtension());
 					}
 				};
 			}
 
 			IFile[] files = WorkspaceResourceDialog.openFileSelection(
 					getShell(), null,
 					null,
 					false,
 					null,
 					extensionFilter == null ? null : Collections
 							.singletonList(extensionFilter));
 			if (files.length > 0) {
 				StringBuffer text = new StringBuffer();
 				for (int i = 0; i < files.length; ++i) {
 					if (isValidWorkspaceResource(files[i])) {
 						text.append(URI.createPlatformResourceURI(files[i]
 								.getFullPath().toString(), true));
 						text.append("  ");
 					}
 				}
 				setURIText(text.toString());
 				refreshModel();
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public void dispose() {
 			browseFileSystemButton = null;
 			browseWorkspaceButton = null;
 			filterExtensions = null;
 			genModel = null;
 			loadButton = null;
 			uriText = null;
 			super.dispose();
 		}
 
 		protected String[] getFilterExtensions() {
 			if (filterExtensions == null) {
 				filterExtensions = new String[] { "*.webdsl" };
 			}
 			return filterExtensions;
 		}
 
 		protected boolean isValidWorkspaceResource(IResource resource) {
 			if (resource.getType() == IResource.FILE) {
 				String[] filterExtensions = getFilterExtensions();
 				if (filterExtensions.length > 0) {
 					for (int i = 0; i < filterExtensions.length; i++) {
 						if (filterExtensions[i].endsWith(".*")
 								|| filterExtensions[i].endsWith("."
 										+ resource.getFileExtension())) {
 							return true;
 						}
 					}
 				}
 
 			}
 			return false;
 		}
 
 		
 		@Override
 		public boolean isPageComplete() {
 			return super.isPageComplete() && genModel.getWebmodel() != null;
 		}
 		
 		public EObject getGenModel() {
 			return genModel;
 		}
 	}
 
 	/**
 	 * The framework calls this to create the contents of the wizard. 
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public void addPages() {
 		if (!isReload()) {
 			// Create a page, set the title, and the initial model file name.
 			//
 			newFileCreationPage = new JoomlaGenModelWizardNewFileCreationPage(
 					"Whatever", selection);
 			newFileCreationPage.setTitle(JoomlagenEditorPlugin.INSTANCE
 					.getString("_UI_JoomlaGenModelWizard_label"));
 			newFileCreationPage.setDescription(JoomlagenEditorPlugin.INSTANCE
 					.getString("_UI_JoomlaGenModelWizard_description"));
 			newFileCreationPage.setFileName(JoomlagenEditorPlugin.INSTANCE
 					.getString("_UI_JoomlaGenEditorFilenameDefaultBase")
 					+ "."
 					+ FILE_EXTENSIONS.get(0));
 			addPage(newFileCreationPage);
 			
 			// Try and get the resource selection to determine a current directory for the file dialog.
 			//
 			if (selection != null && !selection.isEmpty()) {
 				// Get the resource...
 				//
 				Object selectedElement = selection.iterator().next();
 				if (selectedElement instanceof IResource) {
 					String guessedFileName = null;
 					
 					// Get the resource parent, if its a file.
 					//
 					IResource selectedResource = (IResource) selectedElement;
 					if (selectedResource.getType() == IResource.FILE) {
 						selectedResource = selectedResource.getParent();
 						
 						// Extract the file name for the genenerator model name
 						IFile file = (IFile) selectedElement;
 						String fileName = file.getName();
 						String fileExtension = file.getFileExtension();
 						
 						guessedFileName = fileName.substring(0, fileName.indexOf(fileExtension)-1);
 					}
 
 					// This gives us a directory...
 					//
 					if (selectedResource instanceof IFolder
 							|| selectedResource instanceof IProject) {
 						// Set this for the container.
 						//
 						newFileCreationPage
 								.setContainerFullPath(selectedResource
 										.getFullPath());
 
 						// Make up a unique new name here.
 						//
 						String defaultModelBaseFilename = guessedFileName != null ? guessedFileName : JoomlagenEditorPlugin.INSTANCE
 								.getString("_UI_JoomlaGenEditorFilenameDefaultBase");
 						String defaultModelFilenameExtension = FILE_EXTENSIONS
 								.get(0);
 						String modelFilename = defaultModelBaseFilename + "."
 								+ defaultModelFilenameExtension;
 						for (int i = 1; ((IContainer) selectedResource)
 								.findMember(modelFilename) != null; ++i) {
 							modelFilename = defaultModelBaseFilename + i + "."
 									+ defaultModelFilenameExtension;
 						}
 						newFileCreationPage.setFileName(modelFilename);
 					}
 				}
 			}
 		}
 		
 		webModelImporterPage = new JoomlaGenModelWizardWebModelImporterPage("WebModelImporterPage");
 		webModelImporterPage.setTitle(JoomlagenEditorPlugin.INSTANCE.getString("_UI_JoomlaGenModelWizard_label"));
 		webModelImporterPage.setDescription(JoomlagenEditorPlugin.INSTANCE.getString("_UI_Wizard_webmodel_importer_description"));
 		addPage(webModelImporterPage);
 	}
 
 	/**
 	 * Get the file from the page.
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public IFile getModelFile() {
 		if (isReload()) {
 			return (IFile) selection.getFirstElement();
 		}
 		return newFileCreationPage.getModelFile();
 	}
 
 }
