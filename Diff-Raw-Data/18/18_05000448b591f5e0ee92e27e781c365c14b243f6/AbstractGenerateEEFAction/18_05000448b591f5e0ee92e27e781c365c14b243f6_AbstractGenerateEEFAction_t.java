 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
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
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.eef.EEFGen.EEFGenModel;
 import org.eclipse.emf.eef.codegen.EEFCodegenPlugin;
 import org.eclipse.emf.eef.codegen.ui.generators.common.GenerateAll;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public abstract class AbstractGenerateEEFAction extends Action implements IObjectActionDelegate {
 
 	protected List<IFile> selectedFiles;
 
 	protected List<EEFGenModel> eefGenModels;
 
 	/**
 	 * the job used for the generation.
 	 * @since 1.1
 	 */
 	protected Job generationJob;
 
 	/**
 	 * 
 	 */
 	public AbstractGenerateEEFAction() {
 		selectedFiles = new ArrayList<IFile>();
 		eefGenModels = new ArrayList<EEFGenModel>();
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 * @deprecated see bug #370409
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
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
 		// avoid concurrent modification
		if (generationJob != null && (generationJob.getState() == Job.RUNNING || generationJob.getState() == Job.WAITING))
 			try {
 				generationJob.join();
 			} catch (InterruptedException e) {
 
 			}
 		if (selectedFiles != null) {
 			try {
 				eefGenModels = initEEFGenModel();
 			} catch (IOException e) {
 				EEFCodegenPlugin.getDefault().logError(e);
 			}
 
 			generationJob = new Job("EEF architecture generation") {
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					try {
 						if (eefGenModels != null && !monitor.isCanceled()) {
 							monitor.beginTask("Generating EEF Architecture", IProgressMonitor.UNKNOWN);
 							for (final EEFGenModel eefGenModel : eefGenModels) {
 								final IContainer target = getGenContainer(eefGenModel);
 								if (target != null) {
 									monitor.subTask("Generating "
 											+ eefGenModel.eResource().getURI().lastSegment()
 											+ " Architecture");
 									int count = 2;
 									if (eefGenModel.getEditionContexts() != null)
 										count += eefGenModel.getEditionContexts().size() * 11;
 									if (eefGenModel.getViewsRepositories() != null)
 										count += eefGenModel.getViewsRepositories().size() * 5;
 									final GenerateAll generator = new GenerateAll(target, eefGenModel);
 									generator.doGenerate(monitor);
 									for (Iterator<IContainer> iterator = generator.getGenerationTargets()
 											.iterator(); iterator.hasNext();) {
 										IContainer nextContainer = iterator.next();
 										nextContainer.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 									}
 									monitor.worked(count);
 								}
 								// check monitor state
 								if (monitor.isCanceled()) {
 									break;
 								}
 							}
 						}
 					} catch (IOException e) {
 						EEFCodegenPlugin.getDefault().logError(e);
 						return Status.CANCEL_STATUS;
 					} catch (CoreException e) {
 						EEFCodegenPlugin.getDefault().logError(e);
 						return Status.CANCEL_STATUS;
 					} finally {
 						monitor.done();
 						selectedFiles.clear();
 						eefGenModels.clear();
 					}
 					return Status.OK_STATUS;
 				}
 			};
 			generationJob.setUser(true);
 			generationJob.schedule();
 		}
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		selectedFiles.clear();
 		eefGenModels.clear();
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
 				final IContainer target = (IContainer)ResourcesPlugin.getWorkspace().getRoot()
 						.getFolder(new Path(eefGenModel.getGenDirectory()));
 				return target;
 			}
 		}
 		return null;
 	}
 
 }
