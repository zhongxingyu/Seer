 package org.eclipse.emf.compare.ui.legacy.wizard;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.DiffFactory;
 import org.eclipse.emf.compare.diff.ModelInputSnapshot;
 import org.eclipse.emf.compare.ui.legacy.editor.ModelCompareEditorInput;
 import org.eclipse.emf.compare.ui.util.PropertyLoader;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
 import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
 
 /**
  * Wizard in order to save the diff model
  * 
 * @author Cedric Brun  cedric.brun@obeo.fr 
  * 
  */
 public class SaveDeltaWizard extends BasicNewFileResourceWizard {
 
 	private ModelCompareEditorInput input;
 	private IFile leftFile;
 
 	@Override
 	public boolean performFinish() {
 		boolean result = false;
 		if (((WizardNewFileCreationPage)getPage("newFilePage1")).getFileName().endsWith(PropertyLoader.UI_SaveDeltaWizard_FileExtension)) { //$NON-NLS-1$
 			IFile file = ((WizardNewFileCreationPage)getPage("newFilePage1")).createNewFile(); //$NON-NLS-1$
 	        if (file != null) {
 		        try {
 		        	ModelInputSnapshot modelInputSnapshot = DiffFactory.eINSTANCE.createModelInputSnapshot();
 		        	modelInputSnapshot.setDiff(input.getDiff());
 		        	modelInputSnapshot.setMatch(input.getMatch());
 		        	modelInputSnapshot.setDate(Calendar.getInstance(Locale.getDefault()).getTime());
 					save(modelInputSnapshot,file.getFullPath().toString());
 				} catch (IOException e) {
 					EMFComparePlugin.getDefault().log(e,false);
 				}
 				result = true;
 	        }
 		} else {
 			((WizardNewFileCreationPage)getPage("newFilePage1")) //$NON-NLS-1$
 				.setErrorMessage(NLS.bind(PropertyLoader.WARN_FilenameExtension, PropertyLoader.UI_SaveDeltaWizard_FileExtension));
 		}
 		return result;
 	}
 	
 	private void save(final EObject root, final String path) throws IOException {
 		final URI modelURI = URI.createURI(path);
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
 						new XMIResourceFactoryImpl());
 		final Resource newModelResource = resourceSet.createResource(modelURI);
 		newModelResource.getContents().add(root);
 		final Map options = new HashMap();
 		options.put(XMLResource.OPTION_ENCODING, System
 				.getProperty("file.encoding"));
 		newModelResource.save(options);
 
 	}
 	
 	/*
 	 * (non-Javadoc) Method declared on IWizard.
 	 */
 	public void addPages() {
 		super.addPages();
 		((WizardNewFileCreationPage) getPage("newFilePage1")).setFileName(this.leftFile //$NON-NLS-1$
 				.getName().substring(
 						0,
 						this.leftFile.getName().length()
 								- ((this.leftFile.getFileExtension().length()) + 1))
 				+ "." + PropertyLoader.UI_SaveDeltaWizard_FileExtension); //$NON-NLS-1$
 	}
 	
 	/**
 	 * initalize the wizard
 	 * 
 	 * @param workbench
 	 *            current workbench
 	 * @param selection
 	 *            current file selected
 	 * @param input
 	 *            compare editor input
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection,
 			ModelCompareEditorInput input) {
 		super.init(workbench, selection);
 		this.input = input;
 		this.leftFile = (IFile)selection.getFirstElement();
 		setWindowTitle(ResourceMessages.FileResource_shellTitle);
 
 		setNeedsProgressMonitor(true);
 	}
 
 }
