 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.pror.reqif10.editor.presentation;
 
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.StringTokenizer;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.CommonPlugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
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
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.rmf.pror.reqif10.configuration.Column;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigurationFactory;
 import org.eclipse.rmf.pror.reqif10.configuration.LabelConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrGeneralConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrToolExtension;
 import org.eclipse.rmf.pror.reqif10.provider.Reqif10EditPlugin;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.DatatypeDefinitionString;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.ReqIFContent;
 import org.eclipse.rmf.reqif10.ReqIFHeader;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecObjectType;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
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
 
 
 /**
  * This is a simple wizard for creating a new model file.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class Reqif10ModelWizard extends Wizard implements INewWizard {
 	/**
 	 * The supported extensions for created files.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final List<String> FILE_EXTENSIONS =
 		Collections.unmodifiableList(Arrays.asList(Reqif10EditorPlugin.INSTANCE.getString("_UI_Reqif10EditorFilenameExtensions").split("\\s*,\\s*")));
 
 	/**
 	 * A formatted list of supported file extensions, suitable for display.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String FORMATTED_FILE_EXTENSIONS =
 		Reqif10EditorPlugin.INSTANCE.getString("_UI_Reqif10EditorFilenameExtensions").replaceAll("\\s*,\\s*", ", ");
 
 	/**
 	 * This caches an instance of the model package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ReqIF10Package reqif10Package = ReqIF10Package.eINSTANCE;
 
 	/**
 	 * This caches an instance of the model factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ReqIF10Factory reqif10Factory = reqif10Package.getReqIF10Factory();
 
 	/**
 	 * This is the file creation page.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Reqif10ModelWizardNewFileCreationPage newFileCreationPage;
 
 	/**
 	 * This is the initial object creation page.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Reqif10ModelWizardInitialObjectCreationPage initialObjectCreationPage;
 
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
 
 	/**
 	 * Caches the names of the types that can be created as the root object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected List<String> initialObjectNames;
 
 	/**
 	 * This just records the information.
 	 * <!-- begin-user-doc -->
 	 * Uses a custom Image for the Wizard.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 		setWindowTitle(Reqif10EditorPlugin.INSTANCE.getString("_UI_Wizard_label"));
 		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(Reqif10EditorPlugin.INSTANCE.getImage("full/wizban/NewReqif10.png")));
 	}
 
 	/**
 	 * Returns the names of the types that can be created as the root object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Collection<String> getInitialObjectNames() {
 		if (initialObjectNames == null) {
 			initialObjectNames = new ArrayList<String>();
 			for (EClassifier eClassifier : reqif10Package.getEClassifiers()) {
 				if (eClassifier instanceof EClass) {
 					EClass eClass = (EClass)eClassifier;
 					if (!eClass.isAbstract()) {
 						initialObjectNames.add(eClass.getName());
 					}
 				}
 			}
 			Collections.sort(initialObjectNames, CommonPlugin.INSTANCE.getComparator());
 		}
 		return initialObjectNames;
 	}
 
 	/**
 	 * Create a new model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EObject createInitialModel() {
 		ReqIF root = reqif10Factory.createReqIF();
 
 		ReqIFHeader header = reqif10Factory.createReqIFHeader();
 		root.setTheHeader(header);
 
 		// Setting the time gets more and more complicated...
 		try {
 			GregorianCalendar cal = new GregorianCalendar();
 			cal.setTime(new Date());
 			header.setCreationTime(DatatypeFactory.newInstance()
 					.newXMLGregorianCalendar(cal));
 		} catch (DatatypeConfigurationException e) {
 			throw new RuntimeException(e);
 		}
 
 		header.setSourceToolId("ProR (http://pror.org)");
 //		header.setAuthor(System.getProperty("user.name"));
 
 		ReqIFContent content = reqif10Factory.createReqIFContent();
 		root.setCoreContent(content);
 
 		// Add a DatatypeDefinition
 		DatatypeDefinitionString ddString = reqif10Factory
 				.createDatatypeDefinitionString();
 		ddString.setLongName("T_String32k");
 		ddString.setMaxLength(new BigInteger("32000"));
 		content.getDatatypes().add(ddString);
 
 		// Add a Specification
 		Specification spec = reqif10Factory.createSpecification();
 		spec.setLongName("Specification Document");
 		content.getSpecifications().add(spec);
 
 		// Add a SpecType
 		SpecObjectType specType = reqif10Factory.createSpecObjectType();
 		specType.setLongName("Requirement Type");
 		content.getSpecTypes().add(specType);
 
 		// Add an AttributeDefinition
 		AttributeDefinitionString ad = reqif10Factory
 				.createAttributeDefinitionString();
 		ad.setType(ddString);
 		ad.setLongName("Description");
 		specType.getSpecAttributes().add(ad);
 
 		// Configure the Specification View
 		ProrToolExtension extension = ConfigurationFactory.eINSTANCE
 				.createProrToolExtension();
 		root.getToolExtensions().add(extension);
 		ProrSpecViewConfiguration prorSpecViewConfiguration = ConfigurationFactory.eINSTANCE
 				.createProrSpecViewConfiguration();
 		extension.getSpecViewConfigurations().add(prorSpecViewConfiguration);
 		prorSpecViewConfiguration.setSpecification(spec);
 		Column col = ConfigurationFactory.eINSTANCE.createColumn();
 		col.setLabel("Description");
 		col.setWidth(400);
 		prorSpecViewConfiguration.getColumns().add(col);
 
 		Column leftHeaderColumn = ConfigFactory.eINSTANCE.createColumn();
 		leftHeaderColumn
 				.setWidth(ConfigurationUtil.DEFAULT_LEFT_HEADER_COLUMN_WIDTH);
 		leftHeaderColumn
 				.setLabel(ConfigurationUtil.DEFAULT_LEFT_HEADER_COLUMN_NAME);
 		prorSpecViewConfiguration.setLeftHeaderColumn(leftHeaderColumn);
 
 		// Configure the Label configuration
 		ProrGeneralConfiguration generalConfig = ConfigurationFactory.eINSTANCE
 				.createProrGeneralConfiguration();
 		LabelConfiguration labelConfig = ConfigurationFactory.eINSTANCE
 				.createLabelConfiguration();
 		labelConfig.getDefaultLabel().add("Description");
 		generalConfig.setLabelConfiguration(labelConfig);
 		extension.setGeneralConfiguration(generalConfig);
 
 		// Create one Requirement
 		SpecObject specObject = reqif10Factory.createSpecObject();
 		specObject.setType(specType);
 		content.getSpecObjects().add(specObject);
 		AttributeValueString value = reqif10Factory.createAttributeValueString();
 		value.setTheValue("Start editing here.");
 		value.setDefinition(ad);
 		specObject.getValues().add(value);
 
 		// Add the requirement to the Specification
 		SpecHierarchy specHierarchy = reqif10Factory.createSpecHierarchy();
 		spec.getChildren().add(specHierarchy);
 		specHierarchy.setObject(specObject);	
 		return root;
 	}
 
 	/**
 	 * Do the work after everything is specified.
 	 * <!-- begin-user-doc -->
 	 * Always use UTF-8 Encoding
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
 							// Create a resource set
 							//
							ResourceSet resourceSet = new ResourceSetImpl();
 
 							// Get the URI of the model file.
 							//
 							URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
 
 							// Create a resource for this file.
 							//
 							Resource resource = resourceSet.createResource(fileURI);
 
 							// Add the initial model object to the contents.
 							//
 							EObject rootObject = createInitialModel();
 							if (rootObject != null) {
 								resource.getContents().add(rootObject);
 							}
 
 							// Save the contents of the resource to the file system.
 							//
 							Map<Object, Object> options = new HashMap<Object, Object>();
 							// Always use UTF-8 encoding
 							// options.put(XMLResource.OPTION_ENCODING,
 							// initialObjectCreationPage.getEncoding());
 							options.put(XMLResource.OPTION_ENCODING, "UTF-8");
							resource.save(options);
 						}
 						catch (Exception exception) {
 							Reqif10EditorPlugin.INSTANCE.log(exception);
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
 				MessageDialog.openError(workbenchWindow.getShell(), Reqif10EditorPlugin.INSTANCE.getString("_UI_OpenEditorError_label"), exception.getMessage());
 				return false;
 			}
 
 			return true;
 		}
 		catch (Exception exception) {
 			Reqif10EditorPlugin.INSTANCE.log(exception);
 			return false;
 		}
 	}
 
 	/**
 	 * This is the one page of the wizard.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public class Reqif10ModelWizardNewFileCreationPage extends WizardNewFileCreationPage {
 		/**
 		 * Pass in the selection.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public Reqif10ModelWizardNewFileCreationPage(String pageId, IStructuredSelection selection) {
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
 					setErrorMessage(Reqif10EditorPlugin.INSTANCE.getString(key, new Object [] { FORMATTED_FILE_EXTENSIONS }));
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
 	 * This is the page where the type of object to create is selected.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public class Reqif10ModelWizardInitialObjectCreationPage extends WizardPage {
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		protected Combo initialObjectField;
 
 		/**
 		 * @generated
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 */
 		protected List<String> encodings;
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		protected Combo encodingField;
 
 		/**
 		 * Pass in the selection.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public Reqif10ModelWizardInitialObjectCreationPage(String pageId) {
 			super(pageId);
 		}
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public void createControl(Composite parent) {
 			Composite composite = new Composite(parent, SWT.NONE);
 			{
 				GridLayout layout = new GridLayout();
 				layout.numColumns = 1;
 				layout.verticalSpacing = 12;
 				composite.setLayout(layout);
 
 				GridData data = new GridData();
 				data.verticalAlignment = GridData.FILL;
 				data.grabExcessVerticalSpace = true;
 				data.horizontalAlignment = GridData.FILL;
 				composite.setLayoutData(data);
 			}
 
 			Label containerLabel = new Label(composite, SWT.LEFT);
 			{
 				containerLabel.setText(Reqif10EditorPlugin.INSTANCE.getString("_UI_ModelObject"));
 
 				GridData data = new GridData();
 				data.horizontalAlignment = GridData.FILL;
 				containerLabel.setLayoutData(data);
 			}
 
 			initialObjectField = new Combo(composite, SWT.BORDER);
 			{
 				GridData data = new GridData();
 				data.horizontalAlignment = GridData.FILL;
 				data.grabExcessHorizontalSpace = true;
 				initialObjectField.setLayoutData(data);
 			}
 
 			for (String objectName : getInitialObjectNames()) {
 				initialObjectField.add(getLabel(objectName));
 			}
 
 			if (initialObjectField.getItemCount() == 1) {
 				initialObjectField.select(0);
 			}
 			initialObjectField.addModifyListener(validator);
 
 			Label encodingLabel = new Label(composite, SWT.LEFT);
 			{
 				encodingLabel.setText(Reqif10EditorPlugin.INSTANCE.getString("_UI_XMLEncoding"));
 
 				GridData data = new GridData();
 				data.horizontalAlignment = GridData.FILL;
 				encodingLabel.setLayoutData(data);
 			}
 			encodingField = new Combo(composite, SWT.BORDER);
 			{
 				GridData data = new GridData();
 				data.horizontalAlignment = GridData.FILL;
 				data.grabExcessHorizontalSpace = true;
 				encodingField.setLayoutData(data);
 			}
 
 			for (String encoding : getEncodings()) {
 				encodingField.add(encoding);
 			}
 
 			encodingField.select(0);
 			encodingField.addModifyListener(validator);
 
 			setPageComplete(validatePage());
 			setControl(composite);
 		}
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		protected ModifyListener validator =
 			new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					setPageComplete(validatePage());
 				}
 			};
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		protected boolean validatePage() {
 			return getInitialObjectName() != null && getEncodings().contains(encodingField.getText());
 		}
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			if (visible) {
 				if (initialObjectField.getItemCount() == 1) {
 					initialObjectField.clearSelection();
 					encodingField.setFocus();
 				}
 				else {
 					encodingField.clearSelection();
 					initialObjectField.setFocus();
 				}
 			}
 		}
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public String getInitialObjectName() {
 			String label = initialObjectField.getText();
 
 			for (String name : getInitialObjectNames()) {
 				if (getLabel(name).equals(label)) {
 					return name;
 				}
 			}
 			return null;
 		}
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		public String getEncoding() {
 			return encodingField.getText();
 		}
 
 		/**
 		 * Returns the label for the specified type name.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		protected String getLabel(String typeName) {
 			try {
 				return Reqif10EditPlugin.INSTANCE.getString("_UI_" + typeName + "_type");
 			}
 			catch(MissingResourceException mre) {
 				Reqif10EditorPlugin.INSTANCE.log(mre);
 			}
 			return typeName;
 		}
 
 		/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		protected Collection<String> getEncodings() {
 			if (encodings == null) {
 				encodings = new ArrayList<String>();
 				for (StringTokenizer stringTokenizer = new StringTokenizer(Reqif10EditorPlugin.INSTANCE.getString("_UI_XMLEncodingChoices")); stringTokenizer.hasMoreTokens(); ) {
 					encodings.add(stringTokenizer.nextToken());
 				}
 			}
 			return encodings;
 		}
 	}
 
 	/**
 	 * The framework calls this to create the contents of the wizard.
 	 * <!-- begin-user-doc -->
 	 * We always use a {@link ReqIf} as the root.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 		@Override
 	public void addPages() {
 		// Create a page, set the title, and the initial model file name.
 		//
 		newFileCreationPage = new Reqif10ModelWizardNewFileCreationPage("Whatever", selection);
 		newFileCreationPage.setTitle(Reqif10EditorPlugin.INSTANCE.getString("_UI_Reqif10ModelWizard_label"));
 		newFileCreationPage.setDescription(Reqif10EditorPlugin.INSTANCE.getString("_UI_Reqif10ModelWizard_description"));
 		newFileCreationPage.setFileName(Reqif10EditorPlugin.INSTANCE.getString("_UI_Reqif10EditorFilenameDefaultBase") + "." + FILE_EXTENSIONS.get(0));
 		addPage(newFileCreationPage);
 
 		// Try and get the resource selection to determine a current directory for the file dialog.
 		//
 		if (selection != null && !selection.isEmpty()) {
 			// Get the resource...
 			//
 			Object selectedElement = selection.iterator().next();
 			if (selectedElement instanceof IResource) {
 				// Get the resource parent, if its a file.
 				//
 				IResource selectedResource = (IResource)selectedElement;
 				if (selectedResource.getType() == IResource.FILE) {
 					selectedResource = selectedResource.getParent();
 				}
 
 				// This gives us a directory...
 				//
 				if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
 					// Set this for the container.
 					//
 					newFileCreationPage.setContainerFullPath(selectedResource.getFullPath());
 
 					// Make up a unique new name here.
 					//
 					String defaultModelBaseFilename = Reqif10EditorPlugin.INSTANCE.getString("_UI_Reqif10EditorFilenameDefaultBase");
 					String defaultModelFilenameExtension = FILE_EXTENSIONS.get(0);
 					String modelFilename = defaultModelBaseFilename + "." + defaultModelFilenameExtension;
 					for (int i = 1; ((IContainer)selectedResource).findMember(modelFilename) != null; ++i) {
 						modelFilename = defaultModelBaseFilename + i + "." + defaultModelFilenameExtension;
 					}
 					newFileCreationPage.setFileName(modelFilename);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get the file from the page.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IFile getModelFile() {
 		return newFileCreationPage.getModelFile();
 	}
 
 }
