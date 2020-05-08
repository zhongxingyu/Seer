 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.aggregator.presentation;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.util.AggregatorResourceImpl;
 import org.eclipse.b3.aggregator.util.ITransformer;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 
 /**
  * This is a simple wizard for creating a new model file.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @author Karel Brezina
  */
 public class TransformationWizard extends Wizard implements INewWizard {
 
 	public class InfoPage extends WizardPage {
 
 		private boolean srcNamespaceFound;
 
 		private boolean transformationSequenceExists;
 
 		protected InfoPage(String pageId, boolean srcNamespaceFound, boolean transformationSequenceExists) {
 			super(pageId);
 			this.srcNamespaceFound = srcNamespaceFound;
 			this.transformationSequenceExists = transformationSequenceExists;
 
 			setPageComplete(srcNamespaceFound && transformationSequenceExists);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 		 */
 		public void createControl(Composite parent) {
 			Label label = new Label(parent, SWT.None);
 
			if(!srcNamespaceFound) {
				label.setText("Namespace of the selected resource was not found - transformation is not possible");
				label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			}
			else if(!transformationSequenceExists) {
				label.setText("Transformation sequence for the selected resource was not successfully resolved - transformation is not possible");
 				label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
 			}
 			else
 				label.setText("This wizard will take you throught Aggregation Model Transformation");
 
 			setControl(label);
 		}
 	}
 
 	/**
 	 * This is the one page of the wizard.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public class NewFileCreationPage extends WizardNewFileCreationPage {
 		/**
 		 * Pass in the selection.
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 */
 		public NewFileCreationPage(String pageId, IStructuredSelection selection) {
 			super(pageId, selection);
 		}
 
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 */
 		public IFile getModelFile() {
 			return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
 		}
 
 		/**
 		 * The framework calls this to see if the file is correct.
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 */
 		@Override
 		protected boolean validatePage() {
 			if(super.validatePage()) {
 				String extension = new Path(getFileName()).getFileExtension();
 				if(extension == null || !FILE_EXTENSIONS.contains(extension)) {
 					String key = FILE_EXTENSIONS.size() > 1
 							? "_WARN_FilenameExtensions"
 							: "_WARN_FilenameExtension";
 					setErrorMessage(AggregatorEditorPlugin.INSTANCE.getString(key,
 							new Object[] { FORMATTED_FILE_EXTENSIONS }));
 					return false;
 				}
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private static String LEGACY_TRANSFORMATION_ID = "org.eclipse.b3.aggregator.legacy_transformation";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_CLASS = "class";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_SOURCE_NS = "sourceNS";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_TARGET_NS = "targetNS";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_SOURCE_TOP_ELEMENT = "sourceTopElement";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_SOURCE_NS_ATTRIBUTE = "sourceNSAttribute";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_SOURCE_ECORE = "sourceEcoreUri";
 
 	private static String LEGACY_TRANSFORMATION_ATTR_TARGET_ECORE = "targetEcoreUri";
 
 	/**
 	 * The supported extensions for created files.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public static final List<String> FILE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(AggregatorEditorPlugin.INSTANCE.getString(
 			"_UI_AggregatorEditorFilenameExtensions").split("\\s*,\\s*")));
 
 	/**
 	 * A formatted list of supported file extensions, suitable for display. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 */
 	public static final String FORMATTED_FILE_EXTENSIONS = AggregatorEditorPlugin.INSTANCE.getString(
 			"_UI_AggregatorEditorFilenameExtensions").replaceAll("\\s*,\\s*", ", ");
 
 	/**
 	 * This caches an instance of the model package.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected AggregatorPackage aggregatorPackage = AggregatorPackage.eINSTANCE;
 
 	/**
 	 * This caches an instance of the model factory.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected AggregatorFactory aggregatorFactory = aggregatorPackage.getAggregatorFactory();
 
 	protected InfoPage infoPage;
 
 	/**
 	 * This is the file creation page.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected NewFileCreationPage newFileCreationPage;
 
 	protected URI srcResourceURI;
 
 	protected boolean srcNamespaceFound;
 
 	protected List<IConfigurationElement> transformationSequence;
 
 	/**
 	 * Remember the selection during initialization for populating the default container.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 */
 	protected IStructuredSelection selection;
 
 	/**
 	 * Remember the workbench during initialization.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected IWorkbench workbench;
 
 	/**
 	 * Caches the names of the types that can be created as the root object. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 */
 	protected List<String> initialObjectNames;
 
 	private IFile modelFile;
 
 	private Resource finalResource;
 
 	public TransformationWizard(URI srcResourceURI) {
 		this.srcResourceURI = srcResourceURI;
 
 		Set<String> nsPaths = new HashSet<String>();
 
 		IConfigurationElement[] transformations = Platform.getExtensionRegistry().getConfigurationElementsFor(
 				LEGACY_TRANSFORMATION_ID);
 
 		String xmlns = null;
 		String topElement = null;
 		String nsAttribute = null;
 		int i = 0;
 
 		while(xmlns == null && i < transformations.length) {
 			topElement = transformations[i].getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_TOP_ELEMENT);
 			nsAttribute = transformations[i].getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_NS_ATTRIBUTE);
 			i++;
 
 			if(StringUtils.trimmedOrNull(topElement) != null && StringUtils.trimmedOrNull(nsAttribute) != null
 					&& !nsPaths.contains(topElement + "/" + nsAttribute)) {
 				xmlns = ResourceUtils.getResourceXMLNS(srcResourceURI, topElement, nsAttribute);
 				nsPaths.add(topElement + "/" + nsAttribute);
 			}
 		}
 
 		srcNamespaceFound = xmlns != null;
 
		String requiredSourceNS = xmlns;
 
		String requiredTargetNS = AggregatorPackage.eNS_URI;
 
		transformationSequence = resolveTransformationSequence(transformations, requiredSourceNS, requiredTargetNS,
				new ArrayList<IConfigurationElement>());
 	}
 
 	/**
 	 * The framework calls this to create the contents of the wizard. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	@Override
 	public void addPages() {
 		infoPage = new InfoPage("InfoPage", srcNamespaceFound, transformationSequence != null
 				&& !transformationSequence.isEmpty());
 		infoPage.setTitle("Depricated Resource");
 		infoPage.setDescription("Selected resource needs to be transformed to the up-to-date structure");
 		addPage(infoPage);
 
 		newFileCreationPage = new NewFileCreationPage("FileCreationPage", selection);
 		newFileCreationPage.setTitle("Target Location");
 		newFileCreationPage.setDescription("Provide location for the transformed model");
 		newFileCreationPage.setFileName(AggregatorEditorPlugin.INSTANCE.getString("_UI_AggregatorEditorFilenameDefaultBase")
 				+ "." + FILE_EXTENSIONS.get(0));
 		addPage(newFileCreationPage);
 
 		// Try and get the resource selection to determine a current directory for the file dialog.
 		//
 		if(selection != null && !selection.isEmpty()) {
 			// Get the resource...
 			//
 			Object selectedElement = selection.iterator().next();
 			if(selectedElement instanceof IResource) {
 				// Get the resource parent, if its a file.
 				//
 				IResource selectedResource = (IResource) selectedElement;
 				if(selectedResource.getType() == IResource.FILE) {
 					selectedResource = selectedResource.getParent();
 				}
 
 				// This gives us a directory...
 				//
 				if(selectedResource instanceof IFolder || selectedResource instanceof IProject) {
 					// Set this for the container.
 					//
 					newFileCreationPage.setContainerFullPath(selectedResource.getFullPath());
 
 					// Make up a unique new name here.
 					//
 					String defaultModelBaseFilename = AggregatorEditorPlugin.INSTANCE.getString("_UI_AggregatorEditorFilenameDefaultBase");
 					String defaultModelFilenameExtension = FILE_EXTENSIONS.get(0);
 					String modelFilename = defaultModelBaseFilename + "." + defaultModelFilenameExtension;
 					for(int i = 1; ((IContainer) selectedResource).findMember(modelFilename) != null; ++i) {
 						modelFilename = defaultModelBaseFilename + i + "." + defaultModelFilenameExtension;
 					}
 					newFileCreationPage.setFileName(modelFilename);
 				}
 			}
 		}
 	}
 
 	public IFile getModelFile() {
 		return modelFile;
 	}
 
 	public Resource getTargetResource() {
 		return finalResource;
 	}
 
 	/**
 	 * This just records the information. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 		setWindowTitle("Aggregator Model Transformation");
 		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(AggregatorEditorPlugin.INSTANCE.getImage("full/wizban/NewAggregator.png")));
 	}
 
 	@Override
 	public boolean performFinish() {
 
 		// Do the work within an operation.
 		//		
 		IRunnableWithProgress operation = new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				try {
 					monitor.beginTask("Transformation is in progress", IProgressMonitor.UNKNOWN);
 					finalResource = transformResource(transformationSequence, srcResourceURI);
 
 					finalResource.setURI(URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true));
 					finalResource.save(null);
 				}
 				catch(Exception e) {
 					throw new InvocationTargetException(e);
 				}
 				finally {
 					monitor.done();
 				}
 			}
 
 		};
 
 		setNeedsProgressMonitor(true);
 
 		modelFile = newFileCreationPage.getModelFile();
 
 		try {
 			getContainer().run(true, false, operation);
 		}
 		catch(InvocationTargetException e) {
 			((WizardPage) getContainer().getCurrentPage()).setErrorMessage("Transformation error: "
 					+ e.getCause().getMessage());
 			return false;
 		}
 		catch(InterruptedException e) {
 			// not cancelable
 		}
 
 		return true;
 	}
 
 	private List<IConfigurationElement> resolveTransformationSequence(IConfigurationElement[] transformations,
 			String requiredSourceNS, String requiredTargetNS, List<IConfigurationElement> transformerSequence) {
 
 		for(IConfigurationElement transformation : transformations) {
 			String srcNS = transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_NS);
 			String trgtNS = transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_TARGET_NS);
 
 			if(requiredSourceNS.equals(srcNS)) {
 
 				List<IConfigurationElement> newTransformerSequence = new ArrayList<IConfigurationElement>();
 				newTransformerSequence.addAll(transformerSequence);
 				newTransformerSequence.add(transformation);
 
 				if(requiredTargetNS.equals(trgtNS)) {
 					return newTransformerSequence;
 				}
 				else {
 					List<IConfigurationElement> result = resolveTransformationSequence(transformations, trgtNS,
 							requiredTargetNS, newTransformerSequence);
 					if(result != null)
 						return result;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private Resource transformResource(List<IConfigurationElement> transformationSequence, URI originalResourceURI)
 			throws IOException, CoreException {
 
 		ResourceSet ecoreRs01 = null;
 		Resource ecoreRes01 = null;
 		EPackage package01 = null;
 		ResourceSet rs01 = null;
 		Resource res01 = null;
 
 		ResourceSet ecoreRs02 = null;
 		Resource ecoreRes02 = null;
 		EPackage package02 = null;
 		ResourceSet rs02 = null;
 		Resource res02 = null;
 
 		int idx = 0;
 		for(IConfigurationElement transformation : transformationSequence) {
 
 			if(idx == 0) {
 				ecoreRs01 = new ResourceSetImpl();
 				ecoreRs01.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
 						new EcoreResourceFactoryImpl());
 				ecoreRes01 = ecoreRs01.getResource(
 						URI.createURI(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_ECORE)), true);
 				package01 = (EPackage) ecoreRes01.getContents().get(0);
 
 				rs01 = new ResourceSetImpl();
 				rs01.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
 				rs01.getPackageRegistry().put(package01.getNsURI(), package01);
 
 				res01 = rs01.getResource(originalResourceURI, true);
 				rs01.getResources().add(res01);
 			}
 			else {
 				ecoreRs01 = ecoreRs02;
 				ecoreRes01 = ecoreRes02;
 				package01 = package02;
 				rs01 = rs02;
 				res01 = res02;
 			}
 
 			idx++;
 
 			File tempFile = File.createTempFile("temp", ".b3aggr");
 			tempFile.deleteOnExit();
 
 			rs02 = new ResourceSetImpl();
 			rs02.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
 
 			if(AggregatorPackage.eNS_URI.equals(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_TARGET_NS))) {
 				package02 = AggregatorPackage.eINSTANCE;
 
 				res02 = new AggregatorResourceImpl(URI.createURI(tempFile.toURI().toString()));
 				rs02.getResources().add(res02);
 			}
 			else {
 				ecoreRs02 = new ResourceSetImpl();
 				ecoreRs02.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
 						new EcoreResourceFactoryImpl());
 				ecoreRes02 = ecoreRs02.getResource(
 						URI.createURI(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_TARGET_ECORE)), true);
 				package02 = (EPackage) ecoreRes02.getContents().get(0);
 
 				res02 = rs02.createResource(URI.createURI(tempFile.toURI().toString()));
 			}
 
 			rs02.getPackageRegistry().put(package02.getNsURI(), package02);
 
 			ITransformer transformer = (ITransformer) transformation.createExecutableExtension(LEGACY_TRANSFORMATION_ATTR_CLASS);
 
 			transformer.initTransformer(res01, res02, package02);
 			transformer.startTransformation();
 		}
 
 		return res02;
 	}
 }
