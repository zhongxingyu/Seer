 /*******************************************************************************
  * Copyright (c) 2009 - 2013 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.bundlor.internal.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.Manifest;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.osgi.util.ManifestElement;
 import org.eclipse.pde.internal.core.text.bundle.ManifestHeader;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.eclipse.virgo.bundlor.ClassPath;
 import org.eclipse.virgo.bundlor.ClassPathEntry;
 import org.eclipse.virgo.bundlor.EntryScannerListener;
 import org.eclipse.virgo.bundlor.ManifestGenerator;
 import org.eclipse.virgo.bundlor.support.ArtifactAnalyzer;
 import org.eclipse.virgo.bundlor.support.classpath.StandardClassPathFactory;
 import org.eclipse.virgo.bundlor.support.partialmanifest.PartialManifest;
 import org.eclipse.virgo.bundlor.support.partialmanifest.ReadablePartialManifest;
 import org.eclipse.virgo.bundlor.support.properties.FileSystemPropertiesSource;
 import org.eclipse.virgo.bundlor.support.properties.PropertiesSource;
 import org.eclipse.virgo.bundlor.util.SimpleManifestContents;
 import org.eclipse.virgo.ide.bundlor.internal.core.asm.ExtensibleAsmTypeArtefactAnalyser;
 import org.eclipse.virgo.ide.bundlor.internal.core.maven.MavenPropertiesSourceFactory;
 import org.eclipse.virgo.ide.bundlor.jdt.core.AstTypeArtifactAnalyser;
 import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
 import org.eclipse.virgo.ide.facet.core.FacetUtils;
 import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
 import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
 import org.eclipse.virgo.ide.manifest.core.editor.model.SpringBundleModel;
 import org.eclipse.virgo.ide.manifest.core.editor.model.SpringBundleModelFactory;
 import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
 import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
 import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
 import org.eclipse.virgo.util.parser.manifest.ManifestContents;
 import org.eclipse.virgo.util.parser.manifest.ManifestParser;
 import org.eclipse.virgo.util.parser.manifest.RecoveringManifestParser;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 
 /**
  * {@link IncrementalProjectBuilder} that runs on a bundle project and generates the MANIFEST.MF based on actual
  * dependencies from the source code and the Bundlor template.
  * 
  * @author Christian Dupuis
  * @author Leo Dos Santos
  * @author Miles Parker
  * @since 1.1.2
  */
 @SuppressWarnings("restriction")
 public class BundlorProjectBuilder extends IncrementalProjectBuilder {
 
 	private static final String WEB_XML_PATH = "WEB-INF/web.xml";
 
 	private final static String CLASS_FILE_EXTENSION = ".class";
 
 	/** Deleted sources from a source directory */
 	private final Set<IResource> deletedSourceResources = new HashSet<IResource>();
 
 	/** Deleted sources from a test source directory */
 	private final Set<IResource> deletedTestResources = new HashSet<IResource>();
 
 	/** Change or new sources from a source directory */
 	private final Set<IResource> sourceResources = new HashSet<IResource>();
 
 	/** Change or new sources from a test source directory */
 	private final Set<IResource> testResources = new HashSet<IResource>();
 
 	/**
 	 * <code>true</code> if a MANIFEST.MF, TEST.MF or template.mf has been changed
 	 */
 	private boolean forceFullBuild = false;
 
 	/** <code>true</code> if Bundlor should scan byte code instead of source code */
 	private boolean scanByteCode = true;
 
 	private final List<ImportedPackage> templatePackageImports = new ArrayList<ImportedPackage>();
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
 
 		// Clean out old state
 		deletedSourceResources.clear();
 		sourceResources.clear();
 		deletedTestResources.clear();
 		testResources.clear();
 		templatePackageImports.clear();
 		forceFullBuild = false;
 
 		IProject project = getProject();
 		IResourceDelta delta = getDelta(project);
 
 		// Get the configuration setting
 		scanByteCode = getProjectPreferences(project).getBoolean(BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_KEY,
 				BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_DEFAULT);
 
 		// Prepare the list of changed resources
 		visitResourceDelta(project, kind, delta);
 
 		// Trigger build
 		build(kind, monitor);
 
 		// Refresh PDE manifest in the root of the project
 		IFile file = project.getFile("META-INF/MANIFEST.MF");
 		if (file != null && file.exists()) {
 			file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
 		}
 
 		return null;
 	}
 
 	/**
 	 * Checks if the given {@link IResource} is either on source or test source folders.
 	 */
 	private void addResourceIfInSourceFolder(IResource resource, Set<IClasspathEntry> classpathEntries,
 			Set<IClasspathEntry> testClasspathEntries) {
 		for (IClasspathEntry entry : classpathEntries) {
 			if (entry.getPath().isPrefixOf(resource.getFullPath())) {
 				sourceResources.add(resource);
 				return;
 			}
 		}
 		for (IClasspathEntry entry : testClasspathEntries) {
 			if (entry.getPath().isPrefixOf(resource.getFullPath())) {
 				testResources.add(resource);
 				return;
 			}
 		}
 	}
 
 	private void build(int kind, final IProgressMonitor monitor) throws CoreException {
 
 		monitor.beginTask("Scanning source code to generate MANIFEST.MF", sourceResources.size() + testResources.size());
 
 		// No resources selected -> on relevant change
 		if (sourceResources.size() == 0 && testResources.size() == 0 && deletedSourceResources.size() == 0
 				&& deletedTestResources.size() == 0 && !forceFullBuild) {
 			return;
 		}
 
 		// Increase scope of build to FULL_BUILD if template.mf, MANIFEST.MF or
 		// TEST.MF has changed
 		if (forceFullBuild) {
 			kind = IncrementalProjectBuilder.FULL_BUILD;
 		}
 
 		IncrementalPartialManifestManager manifestManager = BundlorCorePlugin.getDefault().getManifestManager();
 
 		IJavaProject javaProject = JavaCore.create(getProject());
 
 		// No incremental manifest model has been recorded or the build is a
 		// full build
 		final boolean isFullBuild = !manifestManager.hasPartialManifest(javaProject)
 				|| kind == IncrementalProjectBuilder.FULL_BUILD;
 
 		final ReadablePartialManifest model = manifestManager.getPartialManifest(javaProject, false, isFullBuild);
 		final ReadablePartialManifest testModel = manifestManager.getPartialManifest(javaProject, true, isFullBuild);
 
 		PropertiesSource[] propertiesSources = createPropertiesSource(javaProject);
 
 		// Firstly create the MANFIEST.MF
 		ArtifactAnalyzer artefactAnalyser = (scanByteCode
 				? new ProgressReportingAsmTypeArtefactAnalyser(monitor)
 				: new ProgressReportingAstTypeArtefactAnalyser(javaProject, monitor));
 
 		try {
 			BundleManifest manifest = generateManifest(javaProject, model,
 					ManifestGeneratorFactory.create(model, artefactAnalyser, propertiesSources), sourceResources,
 					isFullBuild, false);
 			// Secondly create the TEST.MF
 			BundleManifest testManifest = generateManifest(javaProject, testModel,
 					ManifestGeneratorFactory.create(testModel, artefactAnalyser, propertiesSources), testResources,
 					isFullBuild, true);
 
 			// Lastly merge the manifests
 			mergeManifests(javaProject, manifest, testManifest);
 
 			monitor.done();
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR, BundlorCorePlugin.PLUGIN_ID,
 					"Exception while generating manifest.", e));
 		}
 
 	}
 
 	/**
 	 * Set up {@link PropertiesSource} instances for configured properties files.
 	 */
 	private PropertiesSource[] createPropertiesSource(final IJavaProject javaProject) throws CoreException {
 
 		IProject project = javaProject.getProject();
 		IEclipsePreferences preferences = getProjectPreferences(project);
 		String propertiesFiles = preferences.get(BundlorCorePlugin.TEMPLATE_PROPERTIES_FILE_KEY,
 				BundlorCorePlugin.TEMPLATE_PROPERTIES_FILE_DEFAULT);
 		String[] properties = StringUtils.split(propertiesFiles, ";");
 
 		List<IPath> paths = new ArrayList<IPath>();
 		if (properties != null && properties.length > 0) {
 			for (String propertiesFile : properties) {
 				IResource resource = null;
 
 				// Assume file is relative to the project but still in the
 				// workspace
 				IPath location = new Path(propertiesFile);
 				if (project.exists(location)) {
 					IFile propertiesResource = project.getFile(location);
 					if (propertiesResource.isLinked()) {
 						paths.add(propertiesResource.getLocation());
 					} else {
 						paths.add(propertiesResource.getRawLocation());
 					}
 					continue;
 				}
 
 				// Assume file is relative to the project and can be outside of
 				// the project and workspace
 				IPath projectRelativeLocation = project.getLocation();
 				if (projectRelativeLocation == null) {
 					projectRelativeLocation = project.getRawLocation();
 				}
				projectRelativeLocation = location.append(propertiesFile);
 				if (projectRelativeLocation.toFile().exists()) {
 					paths.add(projectRelativeLocation);
 					continue;
 				}
 
 				// Assume file is relative to the workspace
 				IPath workspaceRelativeLocation = project.getWorkspace().getRoot().getLocation().append(location);
 				if (workspaceRelativeLocation.toFile().exists()) {
 					paths.add(workspaceRelativeLocation);
 					continue;
 				}
 
 				// Assume absolute path
 				if (location.toFile().exists()) {
 					paths.add(location);
 					continue;
 				}
 				// TODO CD we could add an error marker for those files that
 				// can't be resolved to a resource
 				StatusManager.getManager().handle(
 						new Status(IStatus.INFO, BundlorCorePlugin.PLUGIN_ID,
 								"Bundlor property substitution is skipping over file " + location.toString()
 										+ " for project " + project.getName() + " because it could not be found."));
 			}
 		}
 
 		// Add in properties sources for the resolved properties files
 		Set<PropertiesSource> propertiesSources = new HashSet<PropertiesSource>();
 		for (IPath path : paths) {
 			propertiesSources.add(new FileSystemPropertiesSource(path.toFile()));
 		}
 
 		// Check if the project has a pom.xml in the root of the project file structure
 		if (MavenPropertiesSourceFactory.shouldCreate(getProject())) {
 			propertiesSources.add(MavenPropertiesSourceFactory.createPropertiesSource(getProject()));
 		}
 		return propertiesSources.toArray(new PropertiesSource[] {});
 	}
 
 	/**
 	 * Create a {@link Manifest} instance from the file at the given path.
 	 */
 	private ManifestContents createManifestFromPath(IResource templateResource) throws IOException {
 		if (templateResource != null) {
 			ManifestParser parser = new RecoveringManifestParser();
 			ManifestContents manifest = null;
 			manifest = parser.parse(new FileReader(templateResource.getRawLocation().toString()));
 			return manifest;
 		}
 		// Create new empty MANIFEST
 		else {
 			return new SimpleManifestContents();
 		}
 	}
 
 	private void doGetAffectedResources(IResource resource, int kind, int deltaKind) throws CoreException {
 
 		IJavaProject project = JavaCore.create(getProject());
 		if (project == null) {
 			return;
 		}
 
 		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 
 		// Get the source folders
 		Set<IClasspathEntry> classpathEntries = ServerModuleDelegate.getSourceClasspathEntries(resource.getProject(),
 				false);
 		Set<IClasspathEntry> testClasspathEntries = ServerModuleDelegate.getSourceClasspathEntries(
 				resource.getProject(), true);
 
 		// Java source files
 		if (!scanByteCode && resource.getName().endsWith("java")) { //$NON-NLS-1$
 			IJavaElement element = JavaCore.create(resource);
 			if (element != null && element.getJavaProject().isOnClasspath(element)) {
 				IPackageFragmentRoot root = (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
 				try {
 					IClasspathEntry classpathEntry = root.getRawClasspathEntry();
 					for (IClasspathEntry entry : classpathEntries) {
 						if (classpathEntry.equals(entry)) {
 							if (deltaKind == IResourceDelta.REMOVED) {
 								deletedSourceResources.add(resource);
 							} else {
 								sourceResources.add(resource);
 							}
 							break;
 						}
 					}
 					for (IClasspathEntry entry : testClasspathEntries) {
 						if (classpathEntry.equals(entry)) {
 							if (deltaKind == IResourceDelta.REMOVED) {
 								deletedTestResources.add(resource);
 							} else {
 								testResources.add(resource);
 							}
 							break;
 						}
 					}
 				} catch (JavaModelException e) {
 					// This can happen in case of .java resources not on the
 					// classpath of the project
 				}
 			}
 		}
 		// Java byte code
 		else if (scanByteCode && resource.getName().endsWith(CLASS_FILE_EXTENSION)) {
 			IPath classFilePath = resource.getFullPath();
 
 			// Check default output folders
 			IPath defaultOutputLocation = project.getOutputLocation();
 			if (defaultOutputLocation.isPrefixOf(classFilePath)) {
 				// Ok we know that the file is a class in the default output
 				// location; let's get the class name
 				String className = classFilePath.removeFirstSegments(defaultOutputLocation.segmentCount()).toString();
 				className = className.substring(0, className.length() - CLASS_FILE_EXTENSION.length());
 
 				int ix = className.indexOf('$');
 				if (ix > 0) {
 					className = className.substring(0, ix);
 				}
 
 				className = className + ".java";
 
 				if (deltaKind == IResourceDelta.REMOVED) {
 					deletedSourceResources.add(resource);
 					deletedTestResources.add(resource);
 				} else {
 
 					for (IClasspathEntry entry : classpathEntries) {
 						IPath sourceLocation = entry.getPath();
 						IResource sourceFolder = wsRoot.findMember(sourceLocation);
 						if (sourceFolder instanceof IFolder) {
 							if (((IFolder) sourceFolder).findMember(className) != null) {
 								sourceResources.add(resource);
 								break;
 							}
 						}
 					}
 					for (IClasspathEntry entry : testClasspathEntries) {
 						IPath sourceLocation = entry.getPath();
 						IResource sourceFolder = wsRoot.findMember(sourceLocation);
 						if (sourceFolder instanceof IFolder) {
 							if (((IFolder) sourceFolder).findMember(className) != null) {
 								testResources.add(resource);
 								break;
 							}
 						}
 					}
 				}
 			}
 
 			// Check output folders of source folders
 			for (IClasspathEntry entry : classpathEntries) {
 				IPath outputLocation = entry.getOutputLocation();
 				if (outputLocation != null && outputLocation.isPrefixOf(classFilePath)) {
 					if (deltaKind == IResourceDelta.REMOVED) {
 						deletedSourceResources.add(resource);
 					} else {
 						sourceResources.add(resource);
 					}
 
 					break;
 				}
 			}
 
 			// Check output folders for test source folders
 			for (IClasspathEntry entry : testClasspathEntries) {
 				IPath outputLocation = entry.getOutputLocation();
 				if (outputLocation != null && outputLocation.isPrefixOf(classFilePath)) {
 					if (deltaKind == IResourceDelta.REMOVED) {
 						deletedTestResources.add(resource);
 					} else {
 						testResources.add(resource);
 					}
 
 					break;
 				}
 			}
 		}
 		// Some template or actual manifest file (whether or not it actually
 		// affects packaged build) has changed. Note that this is different
 		// behavior than
 		// pre-Virgo server. Still, it seems reasonably conservative as worst
 		// case we'll be re-building the project when we really don't need to,
 		// and best case we'll catch an edge case that we would have otherwise
 		// missed.
 		else if (resource.getName().equals("template.mf") || resource.getName().equals("MANIFEST.MF")) {
 			forceFullBuild = true;
 		}
 		// Hibernate mapping files
 		else if (resource.getName().endsWith(".hbm")) {
 			addResourceIfInSourceFolder(resource, classpathEntries, testClasspathEntries);
 		}
 		// JPA persistence descriptor
 		else if (resource.getName().equals("persistence.xml") && resource.getParent() != null
 				&& resource.getParent().getName().equals("META-INF")) {
 			addResourceIfInSourceFolder(resource, classpathEntries, testClasspathEntries);
 		} else if (isWebXML(resource)) {
 			sourceResources.add(resource);
 		}
 		// Spring configuration file
 		else if (resource.getName().endsWith(".xml")) {
 			addResourceIfInSourceFolder(resource, classpathEntries, testClasspathEntries);
 		}
 	}
 
 	private boolean isWebXML(IResource resource) {
 		return resource.getFullPath().toString().endsWith(WEB_XML_PATH)
 				&& FacetUtils.hasProjectFacet(resource, FacetCorePlugin.WEB_FACET_ID);
 	}
 
 	/**
 	 * Generate a new or update the existing manifest.
 	 * 
 	 * @throws IOException
 	 */
 	private BundleManifest generateManifest(IJavaProject javaProject, ReadablePartialManifest model,
 			ManifestGenerator generator, Set<IResource> resources, boolean isFullBuild, boolean isTestManifest)
 			throws JavaModelException, CoreException, IOException {
 
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 
 		Set<String> folders = getSourceFolders(javaProject, root, isTestManifest);
 
 		// Removed deleted resources
 		for (IResource deletedResource : (isTestManifest ? deletedTestResources : deletedSourceResources)) {
 			String path = deletedResource.getRawLocation().toString();
 			for (String folder : folders) {
 				if (path.startsWith(folder)) {
 					path = path.substring(folder.length() + 1);
 					if (model instanceof EntryScannerListener) {
 						((EntryScannerListener) model).onBeginEntry(path);
 					}
 					((EntryScannerListener) model).onEndEntry();
 				}
 			}
 		}
 
 		IResource templateResource = getProject().findMember("template.mf");
 
 		ManifestContents templateManifest = createManifestFromPath(templateResource);
 
 		// Test-Import-Package and Test-Import-Bundle -> Import-Package and
 		// Import-Bundle
 		if (isTestManifest) {
 			templateManifest.getMainAttributes().remove(Constants.IMPORT_PACKAGE);
 			templateManifest.getMainAttributes().remove(IHeaderConstants.IMPORT_BUNDLE);
 			templateManifest.getMainAttributes().remove(IHeaderConstants.IMPORT_LIBRARY);
 
 			if (templateManifest.getMainAttributes().containsKey(IHeaderConstants.TEST_IMPORT_BUNDLE)) {
 				templateManifest.getMainAttributes().put(IHeaderConstants.IMPORT_BUNDLE,
 						templateManifest.getMainAttributes().get(IHeaderConstants.TEST_IMPORT_BUNDLE));
 			}
 			if (templateManifest.getMainAttributes().containsKey(IHeaderConstants.TEST_IMPORT_PACKAGE)) {
 				templateManifest.getMainAttributes().put(Constants.IMPORT_PACKAGE,
 						templateManifest.getMainAttributes().get(IHeaderConstants.TEST_IMPORT_PACKAGE));
 			}
 			if (templateManifest.getMainAttributes().containsKey(IHeaderConstants.TEST_IMPORT_LIBRARY)) {
 				templateManifest.getMainAttributes().put(IHeaderConstants.IMPORT_LIBRARY,
 						templateManifest.getMainAttributes().get(IHeaderConstants.TEST_IMPORT_LIBRARY));
 			}
 		} else {
 			String importPackageHeader = templateManifest.getMainAttributes().get(Constants.IMPORT_PACKAGE);
 			Dictionary<String, String> contents = new Hashtable<String, String>();
 			if (importPackageHeader != null) {
 				contents.put(Constants.IMPORT_PACKAGE, importPackageHeader);
 			}
 			templatePackageImports.addAll(BundleManifestFactory.createBundleManifest(contents)
 					.getImportPackage()
 					.getImportedPackages());
 		}
 
 		ManifestContents manifest = null;
 		List<ClassPath> classpathEntries = new ArrayList<ClassPath>();
 		StandardClassPathFactory factory = new StandardClassPathFactory();
 		if (isFullBuild) {
 			for (String folder : folders) {
 				classpathEntries.add(factory.create(folder));
 			}
 		} else {
 			for (String folder : folders) {
 				classpathEntries.add(new FilteringClassPath(resources, folder));
 			}
 		}
 
 		manifest = generator.generate(templateManifest,
 				classpathEntries.toArray(new ClassPath[classpathEntries.size()]));
 		return org.eclipse.virgo.bundlor.util.BundleManifestUtils.createBundleManifest(manifest);
 	}
 
 	/**
 	 * Returns the source folders of the given {@link IJavaProject}.
 	 */
 	private Set<String> getSourceFolders(IJavaProject javaProject, IWorkspaceRoot root, boolean testFolders)
 			throws JavaModelException {
 		Set<String> folders = new HashSet<String>();
 		for (IClasspathEntry entry : ServerModuleDelegate.getSourceClasspathEntries(getProject(), testFolders)) {
 			IResource sourceFolder = root.findMember(entry.getPath());
 			if (sourceFolder instanceof IFolder && !(sourceFolder instanceof IWorkspaceRoot)) {
 				folders.add(((IFolder) sourceFolder).getRawLocation().toString());
 			}
 
 			if (scanByteCode && entry.getOutputLocation() != null) {
 				IResource classFolder = root.findMember(entry.getOutputLocation());
 				if (classFolder instanceof IFolder && !(classFolder instanceof IWorkspaceRoot)) {
 					folders.add(((IFolder) classFolder).getRawLocation().toString());
 				}
 			}
 		}
 
 		if (scanByteCode) {
 			IResource sourceFolder = root.findMember(javaProject.getOutputLocation());
 			if (sourceFolder instanceof IFolder && !(sourceFolder instanceof IWorkspaceRoot)) {
 				folders.add(((IFolder) sourceFolder).getRawLocation().toString());
 			}
 		}
 
 		// Add parent folder of web.xml
 		if (!testFolders && FacetUtils.hasProjectFacet(getProject(), FacetCorePlugin.WEB_FACET_ID)) {
 			// We're really cheating a bit here, as we aren't handling the case
 			// where the user has multiple WEB-INF dirs, but that seems like an
 			// edge case.
 			IResource resource = getProject().findMember(WEB_XML_PATH);
 			if (resource != null) {
 				folders.add(resource.getRawLocation().removeLastSegments(2).toString());
 			}
 		}
 
 		return folders;
 	}
 
 	/**
 	 * Merges the manifests.
 	 */
 	private void mergeManifests(final IJavaProject javaProject, BundleManifest manifest, BundleManifest testManifest)
 			throws CoreException {
 
 		// Check if there is a manifest
 		if (manifest == null) {
 			return;
 		}
 
 		BundleManifest cleanTestManifest = null;
 		if (testManifest != null) {
 
 			/*
 			 * As the test manifest should not contain imported packages,
 			 * bundles and libraries those need to be removed. Furthermore there
 			 * shouldn't be any bundlor related headers in the manifest.
 			 */
 			cleanTestManifest = BundleManifestFactory.createBundleManifest();
 			cleanTestManifest.setBundleManifestVersion(2);
 			if (testManifest.getImportBundle() != null && testManifest.getImportBundle().getImportedBundles() != null
 					&& testManifest.getImportBundle().getImportedBundles().size() > 0) {
 				cleanTestManifest.getImportBundle()
 						.getImportedBundles()
 						.addAll(testManifest.getImportBundle().getImportedBundles());
 			}
 			if (testManifest.getImportLibrary() != null
 					&& testManifest.getImportLibrary().getImportedLibraries() != null
 					&& testManifest.getImportLibrary().getImportedLibraries().size() > 0) {
 				cleanTestManifest.getImportLibrary()
 						.getImportedLibraries()
 						.addAll(testManifest.getImportLibrary().getImportedLibraries());
 			}
 			for (ImportedPackage packageImport : testManifest.getImportPackage().getImportedPackages()) {
 				boolean notImported = true;
 				for (ImportedPackage manifestPackageImport : manifest.getImportPackage().getImportedPackages()) {
 					if (manifestPackageImport.getPackageName().equals(packageImport.getPackageName())) {
 						notImported = false;
 						break;
 					}
 				}
 
 				if (notImported) {
 					boolean skip = false;
 					for (ExportedPackage packageExport : manifest.getExportPackage().getExportedPackages()) {
 						String packageImportName = packageImport.getPackageName();
 						if (packageExport.getPackageName().equals(packageImportName)) {
 							skip = true;
 						}
 					}
 					if (!skip) {
 						cleanTestManifest.getImportPackage().getImportedPackages().add(packageImport);
 					}
 				}
 			}
 		}
 
 		/*
 		 * Never ever import packages that are exported already; we need this as
 		 * the STS integrated support will sometimes try to import certain
 		 * packages that are usually exported already if the classpath is
 		 * incomplete.
 		 */
 		List<ImportedPackage> importedPackagesCopy = new ArrayList<ImportedPackage>(manifest.getImportPackage()
 				.getImportedPackages());
 		for (ExportedPackage packageExport : manifest.getExportPackage().getExportedPackages()) {
 			for (ImportedPackage packageImport : importedPackagesCopy) {
 				if (packageExport.getPackageName().equals(packageImport.getPackageName())) {
 
 					boolean remove = true;
 					// It might still be that the user explicitly wants the
 					// package import in the template.mf
 					for (ImportedPackage templatePackageImport : templatePackageImports) {
 						if (packageExport.getPackageName().equals(templatePackageImport.getPackageName())) {
 							remove = false;
 							break;
 						}
 					}
 					if (remove) {
 						manifest.getImportPackage().getImportedPackages().remove(packageImport);
 						break;
 					}
 				}
 			}
 		}
 
 		IResource manifestResource = BundleManifestUtils.locateManifest(javaProject, false);
 		if (manifestResource == null) {
 			manifestResource = BundleManifestUtils.getFirstPossibleManifestFile(getProject(), false);
 		}
 		IResource testManifestResource = BundleManifestUtils.locateManifest(javaProject, true);
 		if (testManifestResource == null) {
 			testManifestResource = BundleManifestUtils.getFirstPossibleManifestFile(getProject(), true);
 		}
 
 		boolean formatPref = getProjectPreferences(javaProject.getProject()).getBoolean(
 				BundlorCorePlugin.FORMAT_GENERATED_MANIFESTS_KEY, BundlorCorePlugin.FORMAT_GENERATED_MANIFESTS_DEFAULT);
 		if (manifestResource != null && manifestResource instanceof IFile) {
 			try {
 				StringWriter writer = new StringWriter();
 				manifest.write(writer);
 				InputStream manifestStream = new ByteArrayInputStream(writer.toString().getBytes());
 				if (formatPref) {
 					manifestStream = formatManifest((IFile) manifestResource, manifestStream);
 				}
 				IStatus valid = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] { (IFile) manifestResource },
 						IWorkspace.VALIDATE_PROMPT);
 				if (valid.isOK()) {
 					((IFile) manifestResource).setContents(manifestStream, IResource.FORCE | IResource.KEEP_HISTORY,
 							new NullProgressMonitor());
 				}
 				writer.close();
 				manifestStream.close();
 			} catch (IOException e) {
 			}
 
 		}
 		if (testManifestResource != null && testManifestResource instanceof IFile) {
 			try {
 				StringWriter writer = new StringWriter();
 				cleanTestManifest.write(writer);
 				InputStream testManifestStream = new ByteArrayInputStream(writer.toString().getBytes());
 				if (formatPref) {
 					testManifestStream = formatManifest((IFile) testManifestResource, testManifestStream);
 				}
 				IStatus valid = ResourcesPlugin.getWorkspace().validateEdit(
 						new IFile[] { (IFile) testManifestResource }, IWorkspace.VALIDATE_PROMPT);
 				if (valid.isOK()) {
 					((IFile) testManifestResource).setContents(testManifestStream, IResource.FORCE
 							| IResource.KEEP_HISTORY, new NullProgressMonitor());
 				}
 				writer.close();
 				testManifestStream.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	private InputStream formatManifest(IFile file, InputStream manifestInput) throws IOException {
 		StringWriter writer = new StringWriter();
 		SpringBundleModel model = new SpringBundleModel("", true);
 		SpringBundleModelFactory factory = new SpringBundleModelFactory(model);
 		try {
 			Map headers = ManifestElement.parseBundleManifest(manifestInput, null);
 			for (Object obj : headers.keySet()) {
 				String key = (String) obj;
 				String value = (String) headers.get(key);
 				ManifestHeader header = (ManifestHeader) factory.createHeader(key, value);
 				header.update(false);
 				String result = header.write();
 				writer.write(result);
 			}
 		} catch (BundleException e) {
 		}
 		String manifestOutput = writer.toString();
 		writer.close();
 		manifestInput.close();
 		model.dispose();
 		return new ByteArrayInputStream(manifestOutput.getBytes());
 	}
 
 	/**
 	 * Visit the {@link IResourceDelta} and prepare the internal structures of changed and removed resources.
 	 */
 	private void visitResourceDelta(IProject project, final int kind, IResourceDelta delta) throws CoreException {
 		if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
 			IResourceVisitor visitor = new IResourceVisitor() {
 
 				public boolean visit(IResource resource) throws CoreException {
 					doGetAffectedResources(resource, IncrementalProjectBuilder.FULL_BUILD, IResourceDelta.CHANGED);
 					return true;
 				}
 			};
 			project.accept(visitor);
 		} else {
 			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
 
 				public boolean visit(IResourceDelta delta) throws CoreException {
 					doGetAffectedResources(delta.getResource(), kind, delta.getKind());
 					return true;
 				}
 			};
 			delta.accept(visitor);
 		}
 	}
 
 	public IEclipsePreferences getProjectPreferences(IProject project) {
 		IScopeContext context = new ProjectScope(project);
 		IEclipsePreferences node = context.getNode(BundlorCorePlugin.PLUGIN_ID);
 		return node;
 	}
 
 	/**
 	 * Extension to {@link AstTypeArtifactAnalyser} that takes a {@link IProgressMonitor} to report monitor
 	 */
 	class ProgressReportingAstTypeArtefactAnalyser extends AstTypeArtifactAnalyser {
 
 		private final IProgressMonitor monitor;
 
 		public ProgressReportingAstTypeArtefactAnalyser(IJavaProject javaProject, IProgressMonitor monitor) {
 			super(javaProject);
 			this.monitor = monitor;
 		}
 
 		@Override
 		public void analyse(InputStream is, String name, PartialManifest model) throws Exception {
 			monitor.subTask("Scanning '" + name + "'");
 			super.analyse(is, name, model);
 			monitor.worked(1);
 		}
 	}
 
 	/**
 	 * @author Christian Dupuis
 	 * @since 2.1.1
 	 */
 	class ProgressReportingAsmTypeArtefactAnalyser extends ExtensibleAsmTypeArtefactAnalyser {
 
 		private final IProgressMonitor monitor;
 
 		public ProgressReportingAsmTypeArtefactAnalyser(IProgressMonitor monitor) {
 			this.monitor = monitor;
 		}
 
 		@Override
 		public void analyse(InputStream is, String name, PartialManifest model) throws Exception {
 			monitor.subTask("Scanning '" + name + "'");
 			super.analyse(is, name, model);
 			monitor.worked(1);
 		}
 	}
 
 	class FilteringClassPath implements ClassPath {
 
 		private final Map<String, ClassPathEntry> entries;
 
 		public FilteringClassPath(Set<IResource> resources, String folder) {
 			this.entries = new HashMap<String, ClassPathEntry>();
 
 			for (IResource resource : resources) {
 				if (resource instanceof IFile) {
 					String path = resource.getRawLocation().toString();
 					if (path.startsWith(folder)) {
 						path = path.substring(folder.length() + 1);
 						entries.put(path, new FileClassPathEntry(path, (IFile) resource));
 					}
 				}
 			}
 		}
 
 		public void close() {
 			// Nothing to close
 		}
 
 		public ClassPathEntry getEntry(String name) {
 			return entries.get(name);
 		}
 
 		public Iterator<ClassPathEntry> iterator() {
 			return entries.values().iterator();
 		}
 
 	}
 
 	class FileClassPathEntry implements ClassPathEntry {
 
 		private final String name;
 
 		private final IFile file;
 
 		public FileClassPathEntry(String name, IFile file) {
 			this.name = name;
 			this.file = file;
 		}
 
 		public InputStream getInputStream() {
 			try {
 				return file.getContents(true);
 			} catch (CoreException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public Reader getReader() {
 			return new InputStreamReader(getInputStream());
 		}
 
 		public boolean isDirectory() {
 			return false;
 		}
 
 	}
 
 }
