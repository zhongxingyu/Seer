 /*
  * Copyright (c) 2006, 2009 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.build;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gmf.internal.xpand.Activator;
 
 /**
  * FIXME with the recent move of the context project knowledge into this class, there's no much value in
  * having this registry instantiated and invoked by builder. Instead, make it workspace-wide (though may filter project
  * based on xpandBuilder presence) and builder-independent (listen to changes, employ IResourceProxy)
  * 
  * @author artem
  */
 class WorkspaceModelRegistry implements MetaModelSource {
 
 	/**
 	 * 
 	 * It had been found that having gmfgraph.ecore / gmfgen.ecore from the workspace loaded tp this registry (by platform:resource URIs)
 	 * leads to problems with binding QVTO utility calls, because the xPand code (from this class) supplies the EClass'es from workspace resources 
 	 * while QVTO compiler expects the same-named classes from typed GMFGraphPackageImpl/GmfGenPackageImpl. 
 	 * 
 	 * While we are investigating the problem, as a workaround, we will allow to block all metamodles from given workspace 
 	 * project to be loaded into this registry.
 	 * 
 	 * That is, {@link WorkspaceModelRegistry} in 3.0 release will IGNORE all workspace metamodels from the projects that has the settings file with this name.
 	 * @see #380069  
 	 */
 	private static final String SETTINGS_IGNORE_PROJECT_METAMODELS = ".settings/org.eclipse.gmf.xpand.build.ignore-all-local-metamodels.txt";
 
 	private static class Descriptor {
 
 		final String workspacePath;
 
 		final String nsURI;
 
 		final Resource resource;
 
 		public Descriptor(String workspacePath, String nsURI, Resource res) {
 			assert workspacePath != null && nsURI != null && res != null;
 			this.workspacePath = workspacePath;
 			this.nsURI = nsURI;
 			this.resource = res;
 		}
 	}
 
 	private final Map<String, Descriptor> pathToDescriptor = new TreeMap<String, Descriptor>();
 
 	private final Map<String, Descriptor> uriToDescriptor = new TreeMap<String, Descriptor>();
 
 	private final IProject project;
 
 	private boolean isInFullBuild;
 
 	private boolean doneFullBuild;
 
 	//	void DEBUG_DUMP() {
 	//		System.err.println(">>> " + WorkspaceModelRegistry.class.getSimpleName());
 	//		for (Map.Entry<String, Descriptor> e : uriToDescriptor.entrySet()) {
 	//			assert e.getKey().equals(e.getValue().nsURI);
 	//			System.err.println(e.getKey() + " ==> " + e.getValue().workspacePath);
 	//		}
 	//		System.err.println("<<< " + WorkspaceModelRegistry.class.getSimpleName());
 	//	}
 
 	public WorkspaceModelRegistry(IProject project, ResourceSet resolutionResourceSet) {
 		assert project != null;
 		this.project = project;
 		resourceSet = resolutionResourceSet;
 	}
 
 	public WorkspaceModelRegistry(IProject project) {
 		this(project, new ResourceSetImpl());
 		resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap());
 	}
 
 	private Map<String, EPackage> myReturnedResults = new HashMap<String, EPackage>();
 
 	public EPackage find(String nsURI) {
 		Descriptor d = uriToDescriptor.get(nsURI);
 		return d == null ? null : (EPackage) d.resource.getContents().get(0);
 	}
 
 	public EPackage[] all() {
 		if (!doneFullBuild) {
 			try {
 				// full build never ran, need to initialize data first.
 				build(new NullProgressMonitor());
 			} catch (CoreException ex) {
 				Activator.log(ex.getStatus());
 			}
 		}
 		EPackage[] rv = new EPackage[pathToDescriptor.size()];
 		int i = 0;
 		for (Descriptor d : pathToDescriptor.values()) {
 			rv[i++] = (EPackage) d.resource.getContents().get(0);
 		}
 		return rv;
 	}
 
 	public void build(IProgressMonitor monitor) throws CoreException {
 		if (isInFullBuild) {
 			return;
 		}
 		try {
 			isInFullBuild = true;
 			EcoreModelResourceVisitor visitor = new EcoreModelResourceVisitor(monitor);
 			project.accept(visitor);
 			handleCollectedData(visitor);
 			doneFullBuild = true;
 		} finally {
 			isInFullBuild = false;
 		}
 	}
 
 	public void build(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
 		assert delta.getResource().getProject() == project;
 		if (isInFullBuild) {
 			return;
 		}
 		EcoreModelResourceVisitor visitor = new EcoreModelResourceVisitor(monitor);
 		delta.accept(visitor);
 		handleCollectedData(visitor);
 	}
 
 	private void handleCollectedData(EcoreModelResourceVisitor visitor) {
 		for (IFile f : visitor.getRemovedModelFiles()) {
 			String workspacePath = getKeyPath(f);
 			Descriptor d = pathToDescriptor.remove(workspacePath);
 			if (d != null) {
 				Descriptor d2 = uriToDescriptor.remove(d.nsURI);
 				assert d2 == d;
 			}
			delist(d);
 		}
 		Set<IFile> filesToAdd = new HashSet<IFile>(visitor.getNewModelFiles());
 		for (IFile f : visitor.getChangedModelFiles()) {
 			Descriptor d = pathToDescriptor.remove(getKeyPath(f));
 			if (d != null) {
 				Descriptor d2 = uriToDescriptor.remove(d.nsURI); // uri might be changed, let alone Descriptor is not modifyable
 				assert d2 == d;
 				delist(d);
 			}
 			filesToAdd.add(f);
 		}
 		for (IFile f : filesToAdd) {
 			if (shouldIgnoreWorkspaceMetamodel(f)) {
 				continue;
 			}
 			try {
 				Resource r = attemptLoad(f);
 				if (r != null && hasSuitableContent(r)) {
 					Descriptor d = createDescriptor(f, r);
 					assert d != null;
 					pathToDescriptor.put(d.workspacePath, d);
 					uriToDescriptor.put(d.nsURI, d);
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				// ignore
 			}
 		}
 	}
 
 	private boolean shouldIgnoreWorkspaceMetamodel(IFile metamodelFile) {
 		if (metamodelFile == null) {
 			return false;
 		}
 		IFile ignoreProjectMetamodelsSettings = metamodelFile.getProject().getFile(SETTINGS_IGNORE_PROJECT_METAMODELS);
 		return ignoreProjectMetamodelsSettings != null && ignoreProjectMetamodelsSettings.exists();
 	}
 
 	// TODO per-project?
 	private final ResourceSet resourceSet;
 
 	private Resource attemptLoad(IFile file) throws IOException {
 		URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
 		Resource res = getResourceSet(file).getResource(uri, true);
 		if (res == null) {
 			throw new FileNotFoundException("Can't load model from " + file.getFullPath());
 		}
 		return res;
 	}
 
 	private ResourceSet getResourceSet(IFile file) {
 		return resourceSet;
 	}
 
 	// works in pair with #createDescriptor - may extract these as interface to support models other than Ecore
 	private boolean hasSuitableContent(Resource r) {
 		assert r != null;
 		return r.getContents().get(0) instanceof EPackage;
 	}
 
 	private Descriptor createDescriptor(IFile f, Resource res) {
 		final String path = getKeyPath(f);
 		EPackage p = (EPackage) res.getContents().get(0);
 		final String nsURI = p.getNsURI();
 		if (nsURI == null) {
 			throw new IllegalArgumentException("Invalid model file (missed nsURI) " + path);
 		}
 		return new Descriptor(path, nsURI, res);
 	}
 
 	private void delist(Descriptor d) {
 		d.resource.unload();
 		d.resource.getResourceSet().getResources().remove(d.resource);
 	}
 
 	private String getKeyPath(IFile f) {
 		return f.getFullPath().toString();
 	}
 }
