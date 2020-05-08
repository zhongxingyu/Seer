 /*
  * Copyright (c) 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Dmitry Stadnik (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.sketch.transformer;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.internal.common.ui.ModelSelectionPage;
 import org.eclipse.gmf.internal.common.ui.ResourceLocationProvider;
 import org.eclipse.gmf.sketch.SketchDiagram;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 
 /**
  * @author dstadnik
  */
 public class TransformSketchWizard extends Wizard implements INewWizard {
 
 	protected IWorkbench workbench;
 
 	protected IStructuredSelection selection;
 
 	protected WizardNewFileCreationPage targetPage;
 
 	protected ModelSelectionPage sourcePage;
 
 	protected ModelSelectionPage genmodelPage;
 
 	protected ResourceSet resourceSet;
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 		resourceSet = new ResourceSetImpl();
 		setWindowTitle(Messages.TransformSketchWizard_Title);
 	}
 
 	public void addPages() {
 		ResourceLocationProvider rloc = new ResourceLocationProvider(selection);
 		targetPage = new WizardNewFileCreationPage("diagram_genmodel", selection) { //$NON-NLS-1$
 
 			protected boolean validatePage() {
 				IFile file = getTargetFile();
 				if (file != null && file.exists()) {
 					return true;
 				}
 				return super.validatePage();
 			}
 		};
 		targetPage.setTitle(Messages.TransformSketchWizard_TargetTitle);
 		targetPage.setDescription(Messages.TransformSketchWizard_TargetDesc);
 		targetPage.setFileExtension("gmfgen"); //$NON-NLS-1$
 		if (selection != null && !selection.isEmpty()) {
 			Object selectedElement = selection.getFirstElement();
 			if (selectedElement instanceof IResource) {
 				IResource selectedResource = (IResource) selectedElement;
 				String baseName = "default"; //$NON-NLS-1$
 				if (selectedResource instanceof IFile) {
 					baseName = selectedResource.getFullPath().removeFileExtension().lastSegment();
 					selectedResource = selectedResource.getParent();
 				}
 				if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
 					targetPage.setContainerFullPath(selectedResource.getFullPath());
 					targetPage.setFileName(baseName + ".gmfgen"); //$NON-NLS-1$
 				}
 			}
 		}
 		addPage(targetPage);
 		sourcePage = new ModelSelectionPage("sketch_model", rloc, resourceSet, "sketch"); //$NON-NLS-1$ //$NON-NLS-2$
 		sourcePage.setModelRequired(true);
 		sourcePage.setTitle(Messages.TransformSketchWizard_SourceTitle);
 		sourcePage.setDescription(Messages.TransformSketchWizard_SourceDesc);
 		addPage(sourcePage);
 		genmodelPage = new ModelSelectionPage("domain_genmodel", rloc, resourceSet, "genmodel") { //$NON-NLS-1$ //$NON-NLS-2$
 
 			protected void initControls() {
 				List<URI> uris = rloc.getSelectedURIs(getModelFileExtension(), false);
 				if (!uris.isEmpty()) {
 					setURI(uris.get(0));
 					updateURI();
 				} else { // try genmodel file with the same name
 					IPath path = getTargetFile().getFullPath().removeFileExtension().addFileExtension(getModelFileExtension());
 					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 					if (file.exists()) {
 						setURI(URI.createPlatformResourceURI(path.toString(), true));
 						updateURI();
 					}
 				}
 			}
 		};
 		genmodelPage.setTitle(Messages.TransformSketchWizard_GenTitle);
 		genmodelPage.setDescription(Messages.TransformSketchWizard_GenDesc);
 		addPage(genmodelPage);
 	}
 
 	public boolean performFinish() {
 		SketchDiagram diagram = getDiagram();
 		if (diagram == null) {
 			return false;
 		}
 		SketchTransformer transformer = createTransformer(diagram, getGenModel());
 		try {
 			transformer.run(new NullProgressMonitor());
 			GenEditorGenerator existingEditorGen = getExistingEditorGen();
 			if (existingEditorGen != null) {
 				transformer.reconcile(existingEditorGen);
 			}
 		} catch (InvocationTargetException ite) {
 			Activator.logError(Messages.TransformSketchAction_ErrorTransforming, ite);
 		} catch (InterruptedException ie) {
 		}
 		if (transformer.getResult() != null) {
 			saveResult(transformer.getResult());
 			return true;
 		}
 		return false;
 	}
 
 	protected SketchTransformer createTransformer(SketchDiagram diagram, GenModel genModel) {
 		return new SketchTransformer(diagram, genModel);
 	}
 
 	protected SketchDiagram getDiagram() {
 		Resource resource = sourcePage.getResource();
 		if (resource.getContents().size() == 1) {
 			Object contents = resource.getContents().get(0);
 			if (contents instanceof SketchDiagram) {
 				return (SketchDiagram) contents;
 			}
 		}
 		return null;
 	}
 
 	protected GenModel getGenModel() {
 		Resource resource = genmodelPage.getResource();
 		if (resource != null && resource.getContents().size() == 1) {
 			Object contents = resource.getContents().get(0);
 			if (contents instanceof GenModel) {
 				return (GenModel) contents;
 			}
 		}
 		return null;
 	}
 
 	protected IFile getTargetFile() {
		if (targetPage.getFileName() == null || targetPage.getFileName().length() == 0) {
			return null;
		}
 		return ResourcesPlugin.getWorkspace().getRoot().getFile(targetPage.getContainerFullPath().append(targetPage.getFileName()));
 	}
 
 	protected GenEditorGenerator getExistingEditorGen() {
 		IPath path = getTargetFile().getFullPath();
 		URI uri = URI.createPlatformResourceURI(path.toString(), true);
 		try {
 			Resource resource = resourceSet.getResource(uri, true);
 			if (resource.getContents().size() == 1) {
 				Object contents = resource.getContents().get(0);
 				if (contents instanceof GenEditorGenerator) {
 					return (GenEditorGenerator) contents;
 				}
 			}
 		} catch (Exception e) {
 			// not exists
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void saveResult(GenEditorGenerator editorGen) {
 		IPath path = getTargetFile().getFullPath();
 		URI uri = URI.createPlatformResourceURI(path.toString(), true);
 		Resource resource = resourceSet.createResource(uri);
 		resource.getContents().add(editorGen);
 		try {
 			Map options = new HashMap();
 			options.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
 			resource.save(options);
 		} catch (IOException ioe) {
 			Activator.logError(Messages.TransformSketchAction_ErrorSavingResult, ioe);
 		}
 	}
 }
