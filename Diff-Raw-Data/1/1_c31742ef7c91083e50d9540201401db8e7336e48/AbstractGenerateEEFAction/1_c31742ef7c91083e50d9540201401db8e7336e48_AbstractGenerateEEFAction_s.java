 /*******************************************************************************
  * Copyright (c) 2008 - 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.ui.generators.actions;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.eef.EEFGen.EEFGenModel;
 import org.eclipse.emf.eef.codegen.EEFCodegenPlugin;
 import org.eclipse.emf.eef.codegen.ui.generators.common.GenerateAll;
 import org.eclipse.emf.eef.codegen.ui.generators.common.ImportOrganizer;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchSite;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public abstract class AbstractGenerateEEFAction extends Action implements IObjectActionDelegate {
 
 	private Shell shell;
 
 	private IWorkbenchSite site;
 
 	protected List<IFile> selectedFiles;
 
 	protected List<EEFGenModel> eefGenModels;
 
 	/**
 	 * 
 	 */
 	public AbstractGenerateEEFAction() {
 		selectedFiles = new ArrayList<IFile>();
 		eefGenModels = new ArrayList<EEFGenModel>();
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		shell = targetPart.getSite().getShell();
 		site = targetPart.getSite();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	 */
 	public void run(IAction action) {
 		run();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.action.Action#run()
 	 */
 	public void run() {
 		try {
 			if (selectedFiles != null) {
 				eefGenModels = initEEFGenModel();
 
 				IRunnableWithProgress runnable = new IRunnableWithProgress() {
 
 					public void run(IProgressMonitor monitor) throws InvocationTargetException,
 							InterruptedException {
 						try {
 							if (eefGenModels != null) {
 								for (final EEFGenModel eefGenModel : eefGenModels) {
 									final IContainer target = getGenContainer(eefGenModel);
 									if (target != null) {
 										int count = 2;
 										if (eefGenModel.getEditionContexts() != null)
 											count += eefGenModel.getEditionContexts().size() * 11;
 										if (eefGenModel.getViewsRepositories() != null)
 											count += eefGenModel.getViewsRepositories().size() * 5;
 										monitor.beginTask("Generating EEF Architecture", count);
 										final GenerateAll generator = new GenerateAll(target, eefGenModel);
 										generator.doGenerate(monitor);
 										for (Iterator<IContainer> iterator = generator.getGenerationTargets()
 												.iterator(); iterator.hasNext();) {
 											IContainer nextContainer = iterator.next();
 											nextContainer.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 										}
 										monitor.worked(1);
 										if (EEFUtils.isBundleLoaded(EEFUtils.JDT_CORE_SYMBOLIC_NAME)) {
 											monitor.beginTask("Organize imports", 1);
 											Display.getDefault().asyncExec(new Runnable() {
 												public void run() {
 													ImportOrganizer.organizeImports(site, generator
 															.getGenerationTargets());
 												}
 											});
 										}
 									}
 								}
 							}
 						} catch (IOException e) {
 							EEFCodegenPlugin.getDefault().logError(e);
 						} catch (CoreException e) {
 							EEFCodegenPlugin.getDefault().logError(e);
 						} finally {
 							monitor.done();
 							selectedFiles.clear();
 							eefGenModels.clear();
 						}
 					}
 
 				};
 				new ProgressMonitorDialog(shell).run(true, true, runnable);
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
 			for (Object selectedElement : sSelection.toList()) {
 				if (selectedElement instanceof IFile) {
 					this.selectedFiles.add((IFile)selectedElement);
 				}
 			}
 
 		}
 	}
 
 	protected abstract List<EEFGenModel> initEEFGenModel() throws IOException;
 
 	/**
 	 * Returns the container that the EEFGenModel use as generation directory
 	 * 
 	 * @param eefGenModel
 	 *            the eefGenModel
 	 * @return the generation directory
 	 * @throws IOException
 	 *             an error occurred during container creation
 	 */
 	public IContainer getGenContainer(EEFGenModel eefGenModel) throws IOException {
 		if (eefGenModel != null) {
 			if (eefGenModel.getGenDirectory() != null) {
 				final IContainer target = (IContainer)ResourcesPlugin.getWorkspace().getRoot().getFolder(
 						new Path(eefGenModel.getGenDirectory()));
 				return target;
 			}
 		}
 		return null;
 	}
 
 }
