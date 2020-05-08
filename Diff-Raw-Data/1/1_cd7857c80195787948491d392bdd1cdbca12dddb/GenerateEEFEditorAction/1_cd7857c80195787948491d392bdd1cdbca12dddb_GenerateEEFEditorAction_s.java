 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.extended.ui.launcher;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.eef.EEFGen.EEFGenModel;
 import org.eclipse.emf.eef.codegen.EEFCodegenPlugin;
 import org.eclipse.emf.eef.codegen.extended.flow.OverrideEMFEditorCode;
 import org.eclipse.emf.eef.codegen.flow.Workflow;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 public class GenerateEEFEditorAction implements IObjectActionDelegate {
 
 	private static final String GENERATE_EEF_EDITOR = "Generate EEF Editor for ";
 	private Shell shell;
 	protected Set<IFile> selectedFiles;
 	protected List<EEFGenModel> eefGenModels;
 
 	/**
 	 * Constructor for Action1.
 	 */
 	public GenerateEEFEditorAction() {
 		selectedFiles = new LinkedHashSet<IFile>();
 		eefGenModels = new ArrayList<EEFGenModel>();
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		shell = targetPart.getSite().getShell();
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		try {
 			if (selectedFiles != null) {
 				eefGenModels = initEEFGenModel();
 
 				if (eefGenModels != null) {
 					final Workflow flow = new Workflow("Generate EEF Editors", shell);
 					for (final EEFGenModel eefGenModel : eefGenModels) {
 						String key = GENERATE_EEF_EDITOR + eefGenModel.eResource().getURI().toString();
 						OverrideEMFEditorCode eefEditorCode = new OverrideEMFEditorCode(key, eefGenModel);
 						flow.addStep(key, eefEditorCode);
 					}
 					flow.prepare();
 					IRunnableWithProgress runnable = new IRunnableWithProgress() {
 
 						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 							flow.execute(monitor);
 							monitor.done();
 							selectedFiles.clear();
 							eefGenModels.clear();
 						}
 
 					};
 					new ProgressMonitorDialog(shell).run(true, true, runnable);
 				}
 			}
 		} catch (InvocationTargetException e) {
 			EEFCodegenPlugin.getDefault().logError(e);
 		} catch (InterruptedException e) {
 			EEFCodegenPlugin.getDefault().logWarning(e);
 		} catch (IOException e) {
 			EEFCodegenPlugin.getDefault().logError(e);
 		}
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection sSelection = (StructuredSelection)selection;
 			this.selectedFiles.clear();
 			for (Object selectedElement : sSelection.toList()) {
 				if (selectedElement instanceof IFile) {
 					this.selectedFiles.add((IFile)selectedElement);
 				} 
 			}
 
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.codegen.ui.generators.actions.AbstractGenerateEEFAction#inutEEFGenModel()
 	 */
 	protected List<EEFGenModel> initEEFGenModel() throws IOException {
 		if (!selectedFiles.isEmpty()) {
 			ResourceSet resourceSet = new ResourceSetImpl();
 			for (IFile selectedFile : selectedFiles) {
 				URI modelURI = URI.createPlatformResourceURI(selectedFile.getFullPath().toString(), true);
 				String fileExtension = modelURI.fileExtension();
 				if (fileExtension == null || fileExtension.length() == 0) {
 					fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 				}
 				final Resource.Factory.Registry registry = Resource.Factory.Registry.INSTANCE;
 				final Object resourceFactory = registry.getExtensionToFactoryMap().get(fileExtension);
 				if (resourceFactory != null) {
 					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 							resourceFactory);
 				} else {
 					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 							new XMIResourceFactoryImpl());
 				}
 				Resource res = resourceSet.createResource(modelURI);
 				res.load(Collections.EMPTY_MAP);
 				EcoreUtil.resolveAll(resourceSet);
 				if (res.getContents().size() > 0) {
 					EObject object = res.getContents().get(0);
 					if (object instanceof EEFGenModel) {
 						eefGenModels.add((EEFGenModel)object);
 					}
 				}
 			}
 		}
 		return eefGenModels;
 	}
 
 }
