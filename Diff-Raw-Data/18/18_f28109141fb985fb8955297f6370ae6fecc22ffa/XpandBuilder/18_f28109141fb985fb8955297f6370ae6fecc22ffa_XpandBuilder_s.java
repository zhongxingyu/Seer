 /*
  * Copyright (c) 2005, 2008 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  *     Artem Tikhomirov (Borland) - Migration to OCL expressions
  */
 package org.eclipse.gmf.internal.xpand.build;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.RootManager;
 import org.eclipse.gmf.internal.xpand.RootManager.RootDescription;
 import org.eclipse.gmf.internal.xpand.model.AnalysationIssue;
 import org.eclipse.gmf.internal.xpand.model.ExecutionContext;
 import org.eclipse.gmf.internal.xpand.model.ExecutionContextImpl;
 import org.eclipse.gmf.internal.xpand.model.Scope;
 import org.eclipse.gmf.internal.xpand.model.XpandResource;
 import org.eclipse.gmf.internal.xpand.util.ParserException;
 import org.eclipse.gmf.internal.xpand.util.XpandMarkerManager;
 import org.eclipse.gmf.internal.xpand.util.ParserException.ErrorLocationInfo;
 
 public class XpandBuilder extends IncrementalProjectBuilder implements RootManager.IRootChangeListener {
 	private RootManager myRootManager;
 
 	private WorkspaceModelRegistry modelRegistry;
 
 	private boolean myRootsChanged = true;
 
 	public static final String getBUILDER_ID() {
 		return Activator.getId() + ".xpandBuilder";
 	}
 
 	@Override
 	protected void startupOnInitialize() {
 		super.startupOnInitialize();
 		myRootManager = Activator.getRootManager(getProject());
 		myRootManager.addRootChangeListener(this);
 		modelRegistry = new WorkspaceModelRegistry(Activator.getWorkspaceMetamodelsResourceSet());
 		Activator.registerModelSource(modelRegistry);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor) throws CoreException {
 		monitor.beginTask("Building " + getProject().getName() + " xpand project", 11);
 		Map<RootDescription, Collection<IFile>> resourcesToBuild = collectResourcesToBuild(kind, new SubProgressMonitor(monitor, 1));
 		checkCanceled(monitor);
 		doBuild(resourcesToBuild, new SubProgressMonitor(monitor, 10));
 
 		myRootsChanged = false;
 		Set<IProject> referencedProjects = myRootManager.getReferencedProjects();
 		referencedProjects.remove(getProject());
 		return referencedProjects.toArray(new IProject[referencedProjects.size()]);
 	}
 
 	private Map<RootDescription, Collection<IFile>> collectResourcesToBuild(int kind, IProgressMonitor monitor) throws CoreException {
 		if ((kind == FULL_BUILD) || haveRootsChangedSinceLastBuild()) {
 			return fullBuild(monitor);
 		} else {
 			// TODO: modify this logic - only current project resources should
 			// be built, but those having dependencies to the modified
 			// "external" resources (resources from referenced projects) should
 			// be rebuilt here
 			Set<IProject> referencedProjects = myRootManager.getReferencedProjects();
 			referencedProjects.remove(getProject());
 			Collection<IResourceDelta> deltas = new ArrayList<IResourceDelta>(referencedProjects.size());
 			IResourceDelta projectDelta = getDelta(getProject());
 			if (projectDelta == null) {
 				return fullBuild(monitor);
 			}
 			for (IProject next : referencedProjects) {
 				final IResourceDelta delta = getDelta(next);
 				if (delta == null) {
 					return fullBuild(monitor);
 				}
 				deltas.add(delta);
 			}
 			return incrementalBuild(projectDelta, deltas, monitor);
 		}
 	}
 
 	private void doBuild(Map<RootDescription, Collection<IFile>> resourcesToBuild, IProgressMonitor monitor) {
 		monitor.beginTask("Building " + getProject().getName() + " xpand project", resourcesToBuild.size());
 		for (RootDescription rootDescription : resourcesToBuild.keySet()) {
 			WorkspaceResourceManager resourceManager = Activator.createWorkspaceResourceManager(getProject(), rootDescription);
 			Scope scope = new Scope(resourceManager, null, null);
 			checkCanceled(monitor);
 			doBuid(resourceManager, scope, resourcesToBuild.get(rootDescription), new SubProgressMonitor(monitor, 1));
 		}
 	}
 
 	private void doBuid(WorkspaceResourceManager resourceManager, Scope scope, Collection<IFile> xpandFiles, IProgressMonitor monitor) {
 		monitor.beginTask("Building " + getProject().getName() + " xpand project", xpandFiles.size() * 2);
 		for (IFile xpandFile : xpandFiles) {
 			monitor.setTaskName("Building " + xpandFile.getProjectRelativePath().toOSString());
 			try {
 				XpandResource xpandResource = resourceManager.loadXpandResource(xpandFile);
 				checkCanceled(monitor);
 				monitor.worked(1);
 				ExecutionContext context = new ExecutionContextImpl(scope);
 				final Set<AnalysationIssue> issues = new HashSet<AnalysationIssue>();
 				try {
 					xpandResource.analyze(context, issues);
 					checkCanceled(monitor);
 					monitor.worked(1);
 					updateMarkers(xpandFile, issues);
 				} catch (RuntimeException ex) {
 					Activator.logError(ex);
 					XpandMarkerManager.addMarkers(xpandFile, new ParserException.ErrorLocationInfo(ex.toString()));
	}
 
 			} catch (ParserException ex) {
 				updateMarkers(xpandFile, ex.getParsingErrors());
 			} catch (IOException ex) {
 				updateMarkers(xpandFile, ex);
 			} catch (CoreException ex) {
 				updateMarkers(xpandFile, ex);
		}
 				}
 			}
 
 	public void rootsChanged(RootManager rootManager) {
 		myRootsChanged = true;
 	}
 
 	private boolean haveRootsChangedSinceLastBuild() {
 		return myRootsChanged;
 	}
 
 	private RootDescription getRootDescription(IFile file) {
 		return myRootManager.getRootDescription(file);
 	}
 
 	// TODO: do not build all referenced projects on building this one - only
 	// calls to external elements should be analyzed here.
 	protected Map<RootDescription, Collection<IFile>> fullBuild(final IProgressMonitor monitor) throws CoreException {
 		Set<IProject> referencedProjects = myRootManager.getReferencedProjects();
 		referencedProjects.add(getProject());
 		XpandMarkerManager.deleteMarkers(getProject());	//to delete markers from obsolete roots.
 		monitor.beginTask(null, 1 + referencedProjects.size());
 		Map<RootDescription, Collection<IFile>> result = new HashMap<RootDescription, Collection<IFile>>();
 		try {
 			// TODO: way to optimize it - visit not all the resources in this
 			// project, but only those located below actual template roots
 			for (IProject next : referencedProjects) {
 				checkCanceled(monitor);
 				next.accept(new XpandResourceVisitor(result, new SubProgressMonitor(monitor, 1)));
 			}
 			checkCanceled(monitor);
 			modelRegistry.build(getProject(), new SubProgressMonitor(monitor, 1));
 		} finally {
 			monitor.done();
 		}
 		return result;
 	}
 
 
 	protected Map<RootDescription, Collection<IFile>> incrementalBuild(final IResourceDelta projectDelta, final Collection<IResourceDelta> referencedProjectDeltas, final IProgressMonitor monitor) throws CoreException {
 		monitor.beginTask(null, 2 + referencedProjectDeltas.size());
 		Map<RootDescription, Collection<IFile>> result = new HashMap<RootDescription, Collection<IFile>>();
 		try {
 			for (IResourceDelta delta : referencedProjectDeltas) {
 				checkCanceled(monitor);
 				delta.accept(new XpandResourceVisitor(result, new SubProgressMonitor(monitor, 1)));
 			}
 			checkCanceled(monitor);
 			projectDelta.accept(new XpandResourceVisitor(result, new SubProgressMonitor(monitor, 1)));
 			checkCanceled(monitor);
 			modelRegistry.build(getProject(), projectDelta, new SubProgressMonitor(monitor, 1));
 		} finally {
 			monitor.done();
 		}
 		return result;
 	}
 
 
 	private void checkCanceled(final IProgressMonitor monitor) {
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 	}
 
 
 	private static void updateMarkers(IFile resource, Set<AnalysationIssue> issues) {
         XpandMarkerManager.deleteMarkers(resource);
         XpandMarkerManager.addMarkers(resource, issues.toArray(new AnalysationIssue[issues.size()]));
 	}
 
 	private static void updateMarkers(IFile resource, Exception exception) {
 		Activator.logError(exception);
 		// perhaps, depending on exception type (Core|IO) we can decide to keep
 		// old markers?
 		XpandMarkerManager.deleteMarkers(resource);
 		XpandMarkerManager.addErrorMarker(resource, exception.getMessage(), -1, -1);
 	}
 	
 	private static void updateMarkers(IFile resource, ErrorLocationInfo[] parsingErrors) {
         XpandMarkerManager.deleteMarkers(resource);
         XpandMarkerManager.addMarkers(resource, parsingErrors);
 	}
 
 	private static boolean isXpand(final IFile resource) {
 		return XpandResource.TEMPLATE_EXTENSION.equals(resource.getFileExtension());
 	}
 
 	private class XpandResourceVisitor implements IResourceVisitor, IResourceDeltaVisitor {
 		private final IProgressMonitor monitor;
 		private Map<RootDescription, Collection<IFile>> description2ResourcesMap;
 
 		public XpandResourceVisitor(Map<RootDescription, Collection<IFile>> description2ResourcesMap, final IProgressMonitor monitor) {
 			this.monitor = monitor;
 			this.description2ResourcesMap = description2ResourcesMap;
 		}
 
 		public boolean visit(final IResource resource) {
 			if (!resource.isDerived() && (resource instanceof IFile) && isFileOfInterest((IFile) resource)) {
 				reloadResource((IFile) resource);
 			}
 			monitor.worked(1);
 			return true;
 		}
 
 		public boolean visit(final IResourceDelta delta) throws CoreException {
 			final IResource resource = delta.getResource();
 			if (resource.isDerived()) {
 				return false;
 			}
 			if ((resource instanceof IFile)) {
 				IFile file = (IFile) resource;
 				if (!isFileOfInterest(file)) {
 					return false;
 				}
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 					reloadResource(file);
 					break;
 				case IResourceDelta.REMOVED:
 					handleRemovement(file);
 					break;
 				case IResourceDelta.CHANGED:
 					reloadResource(file);
 					break;
 				}
 			} else if (resource instanceof IProject) {
 				// forget about project in resource manager
 				if (delta.getKind() == IResourceDelta.REMOVED) {
 					System.err.println("Project removed:" + resource.getName());
 				}
 				if (delta.getKind() == IResourceDelta.OPEN) {
 					System.err.println("Project open:" + ((IProject) resource).isOpen());
 				}
 			}
 			monitor.worked(1);
 			return true;
 		}
 		
 		private void handleRemovement(IFile resource) {
 			XpandMarkerManager.deleteMarkers(resource);
 		}
 		
 		/**
 		 * Should be called only for resources which are {@link XpandResourceVisitor#isFileOfInterest(IFile)}
 		 */
 		private void reloadResource(IFile resource) {
 			assert resource.exists();
 			// TODO: remove this if unless we do compile other resources here
 			// (QVTO)
 			if (isXpand(resource)) {
 				RootDescription rootDescription = getRootDescription(resource);
 				Collection<IFile> resources = description2ResourcesMap.get(rootDescription);
 				if (resources == null) {
 					resources = new ArrayList<IFile>();
 					description2ResourcesMap.put(rootDescription, resources);
 				}
 				resources.add(resource);
 			}
 		}
 		
 		private boolean isFileOfInterest(IFile file) {
 			if (!isXpand(file)) {
 				return false;
 			}
 			if (getRootDescription(file) == null) {
 				return false;
 			}
 			return true;
 		}
 		
 	}
 
 }
