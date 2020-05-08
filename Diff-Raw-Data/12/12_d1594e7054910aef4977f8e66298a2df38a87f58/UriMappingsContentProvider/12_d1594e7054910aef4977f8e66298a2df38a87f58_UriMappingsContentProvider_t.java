 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.ui.cnf;
 
 import static org.eclipse.jdt.core.IJavaElementDelta.ADDED;
 
 import java.util.Date;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.internal.ui.util.CoreUtility;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Display;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelDelta;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelLocator;
 import org.jboss.tools.ws.jaxrs.core.pubsub.EventService;
 import org.jboss.tools.ws.jaxrs.core.pubsub.Subscriber;
 import org.jboss.tools.ws.jaxrs.ui.internal.utils.Logger;
 
 @SuppressWarnings("restriction")
 public class UriMappingsContentProvider implements ITreeContentProvider, Subscriber { // ,
 
 	private TreeViewer viewer;
 
 	private Map<IProject, Object> uriPathTemplateCategories = new HashMap<IProject, Object>();
 
 	public UriMappingsContentProvider() {
 		// IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		// workspace.addResourceChangeListener(this,
 		// IResourceChangeEvent.POST_CHANGE);
 		EventService.getInstance().subscribe(this);
 	}
 
 	@Override
 	public Object[] getChildren(final Object parentElement) {
 		if (parentElement instanceof IProject) {
 			long startTime = new Date().getTime();
 			final IProject project = (IProject) parentElement;
 			try {
 				if (!uriPathTemplateCategories.containsKey(project)) {
 					if (JaxrsMetamodelLocator.get(project) == null) {
 						Logger.debug("JAX-RS Metamodel needs to be built for project '" + project.getName() + "'");
 						Job buildJob = CoreUtility.getBuildJob(project);
 						buildJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
 						buildJob.schedule();
 						Logger.debug("Displaying a temporary node in the viewer while the metamodel is being built...");
 						//return new Object[] { new WaitWhileBuildingElement() };
 					} 
 					else {
 						// edge case: if the metamodel was built but no category was initialized yet.
 						Logger.debug("Adding a UriPathTemplateCategory for project {} (case #2)", project.getName());
 						UriPathTemplateCategory uriPathTemplateCategory = new UriPathTemplateCategory(this, project);
 						uriPathTemplateCategories.put(project, uriPathTemplateCategory);
 					}
 				}
 				
 				Logger.debug("Displaying the UriPathTemplateCategory for project '{}'", project.getName());
 				return new Object[] { uriPathTemplateCategories.get(project) };
 			} catch (CoreException e) {
 				Logger.error("Failed to retrieve JAX-RS Metamodel in project '" + project.getName() + "'", e);
 			} finally {
 				long endTime = new Date().getTime();
 				Logger.debug("JAX-RS Metamodel UI for project '" + project.getName() + "' refreshed in "
 						+ (endTime - startTime) + "ms.");
 			}
 		} else if (parentElement instanceof ITreeContentProvider) {
 			Logger.debug("Displaying the children of '{}'", parentElement);
 			return ((ITreeContentProvider) parentElement).getChildren(parentElement);
 		}
 		Logger.debug("*** No children for parent of type '{}' ***", parentElement.getClass().getName());
 		
 		return null;
 	}
 
 	@Override
 	public Object getParent(Object element) {
 		return null;
 	}
 
 	@Override
 	public boolean hasChildren(Object element) {
 		if(element instanceof IProject) {
 			return true;
 		} else 
 		if (element instanceof ITreeContentProvider) {
 			return ((ITreeContentProvider) element).hasChildren(element);
 		}
 		Logger.debug("Element {} has not children", element.getClass().getName());
 		return false;
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		this.viewer = (TreeViewer) viewer;
 	}
 
 	@Override
 	public void dispose() {
 		// ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		uriPathTemplateCategories = null;
 	}
 
 	/*
 	 * @Override public void resourceChanged(IResourceChangeEvent event) { refreshChangedProjects(event.getDelta()); }
 	 */
 
 	@Override
 	public void inform(EventObject event) {
 		// FIXME: should receive a single JaxrsMetamodelChangedEvent containing 0 or more IJaxrsEndpointChangedEvent(s)
 		final Object eventSource = event.getSource();
 		if (eventSource instanceof JaxrsMetamodelDelta) {
 			final JaxrsMetamodelDelta metamodelDelta = (JaxrsMetamodelDelta) eventSource;
 			if (metamodelDelta.getDeltaKind() == ADDED || !metamodelDelta.getAffectedEndpoints().isEmpty()) {
 				refreshContent(metamodelDelta.getMetamodel().getProject());
 			} else {
 				Logger.debug("Well, the changes are *not* relevant to the UI: no refresh required");
 			}
 		}
 	}
 
 	protected void refreshContent(final IProject project) {
 		// check if the viewer is already having the appropriate UriPathTemplateCategory for the given project. If not,
 		// it is a WaitWhileBuildingElement item, and the project itself must be refresh to replace this temporary
 		// element with the expected category.
 		final Object target = uriPathTemplateCategories.containsKey(project) ? uriPathTemplateCategories.get(project)
 				: project;
 		Logger.debug("Refreshing navigator view at level: {}", target.getClass().getName());
 		if (!uriPathTemplateCategories.containsKey(project)) {
 			Logger.debug("Adding a UriPathTemplateCategory for project {} (case #1)", project.getName());
 			UriPathTemplateCategory uriPathTemplateCategory = new UriPathTemplateCategory(this, project);
 			uriPathTemplateCategories.put(project, uriPathTemplateCategory);
 		}
 		// this piece of code must run in an async manner to avoid reentrant call while viewer is busy.
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (viewer != null) {
 					TreePath[] treePaths = viewer.getExpandedTreePaths();
					Logger.debug("*** Refreshing the viewer at target level: {} (viewer busy: {}) ***", target, viewer.isBusy());
					viewer.refresh(target);
 					viewer.setExpandedTreePaths(treePaths);
 					Logger.debug("*** Refreshing the viewer... done ***");
 				} else {
 					Logger.debug("*** Cannot refresh: viewer is null :-( ***");
 				}
 			}
 		});
 	}
 
 	@Override
 	/**
 	 * Subscriber ID
 	 */
 	public String getId() {
 		return "UI";
 	}
 
 }
