 /*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.bundlor.ui.internal.actions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.eclipse.virgo.ide.bundlor.internal.core.BundlorCorePlugin;
 import org.eclipse.virgo.ide.bundlor.ui.BundlorUiPlugin;
 import org.eclipse.virgo.ide.facet.core.FacetUtils;
 
 /**
  * {@link IObjectActionDelegate} to enables/disables the incremental Bundlor project builder.
  * 
  * @author Christian Dupuis
  * @since 1.1.3
  */
 public class AutomaticRunBundlorActionDelegate extends RunBundlorActionDelegate {
 
 	public void run(IAction action) {
 		final Set<IJavaProject> projects = new LinkedHashSet<IJavaProject>();
 		Iterator<IProject> iter = getSelected().iterator();
 		while (iter.hasNext()) {
 			IProject project = iter.next();
 			if (FacetUtils.isBundleProject(project)) {
 				projects.add(JavaCore.create(project));
 			}
 		}
 		IRunnableWithProgress op = new WorkspaceModifyOperation() {
 			protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {
 				for (final IJavaProject javaProject : projects) {
 					IProject project = javaProject.getProject();
 					IProjectDescription description = project.getDescription();
 					try {
 						List<ICommand> cmds = Arrays.asList(description.getBuildSpec());
 						List<ICommand> newCmds = new ArrayList<ICommand>(cmds);
						for (ICommand config : cmds) {
							if (config.getBuilderName().equals(BundlorCorePlugin.BUILDER_ID)) {
								if (BundlorUiPlugin.isBundlorBuilding(project)) {
 									newCmds.remove(config);
								} else {
									ICommand command = project.getDescription().newCommand();
									command.setBuilderName(BundlorCorePlugin.BUILDER_ID);
									newCmds.add(config);
 								}
 							}
 						}
 						if (!cmds.equals(newCmds)) {
 							description.setBuildSpec(newCmds.toArray(new ICommand[] {}));
 							project.setDescription(description, monitor);
 						}
 					} catch (CoreException e) {
 						StatusManager.getManager().handle(
 								new Status(IStatus.ERROR, BundlorUiPlugin.PLUGIN_ID,
 										"An exception occurred while running bundlor.", e));
 					}
 				}
 			}
 		};
 
 		try {
 			PlatformUI.getWorkbench()
 					.getProgressService()
 					.runInUI(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), op,
 							ResourcesPlugin.getWorkspace().getRoot());
 		} catch (InvocationTargetException e) {
 			StatusManager.getManager().handle(
 					new Status(IStatus.ERROR, BundlorUiPlugin.PLUGIN_ID,
 							"An exception occurred while running bundlor.", e));
 		} catch (InterruptedException e) {
 		}
 
 	}
 
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 	}
 
 }
