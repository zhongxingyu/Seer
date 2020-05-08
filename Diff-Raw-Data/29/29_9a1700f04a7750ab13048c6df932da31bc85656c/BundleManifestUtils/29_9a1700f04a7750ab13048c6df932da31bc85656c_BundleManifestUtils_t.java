 /*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.manifest.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Dictionary;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IPathVariableManager;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.osgi.util.ManifestElement;
 import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
 import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
 import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
 import org.osgi.framework.Constants;
 import org.springframework.ide.eclipse.core.SpringCoreUtils;
 import org.springframework.ide.eclipse.core.java.JdtUtils;
 import org.springframework.util.StringUtils;
 
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
 
 /**
  * Helper methods to located and load {@link BundleManifest} instances.
  * @author Christian Dupuis
  * @author Steffen Pingel
 * @author Martin Lippert
  * @since 1.0.0
  */
 public class BundleManifestUtils {
 
 	/** URL file schema */
 	private static final String FILE_SCHEME = "file";
 
 	/**
 	 * Returns a {@link BundleManifest} instance for the given <code>javaProject</code>.
 	 * <p>
 	 * This implementation searches the source folders of the {@link IJavaProject} and returns the first found
 	 * META-INF/MANIFEST.MF as the valid manifest.
 	 */
 	public static BundleManifest getBundleManifest(IJavaProject javaProject, boolean testBundle) {
 		IFile manifestFile = locateManifest(javaProject, testBundle);
 		if (manifestFile != null) {
 			try {
 				return BundleManifestFactory
 						.createBundleManifest(new InputStreamReader(manifestFile.getContents(true)));
 			}
 			catch (Exception e) {
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Locates the {@link IResource} representing the MANIFEST.MF of a <code>javaProject</code>.
 	 * <p>
 	 * This implementation searches the source folders of the {@link IJavaProject} and returns the first found
 	 * META-INF/MANIFEST.MF as the valid manifest.
 	 */
 	public static IFile locateManifest(IJavaProject javaProject, boolean testBundle) {
 		String manifestLocation = (testBundle ? BundleManifestCorePlugin.TEST_MANIFEST_FILE_LOCATION
 				: BundleManifestCorePlugin.MANIFEST_FILE_LOCATION);
 		try {
 			for (IClasspathEntry entry : ServerModuleDelegate.getSourceClasspathEntries(javaProject.getProject(),
 					testBundle)) {
 				IPath path = entry.getPath().append(manifestLocation).removeFirstSegments(1);
 				IFile manifestFileHandle = javaProject.getProject().getFile(path);
 				if (manifestFileHandle.exists()) {
 					return manifestFileHandle;
 				}
 			}
 
 			if (SpringCoreUtils.hasProjectFacet(javaProject.getProject(), FacetCorePlugin.WEB_FACET_ID)) {
 				WebArtifactEdit webArtifact = WebArtifactEdit.getWebArtifactEditForRead(javaProject.getProject());
 				if (webArtifact != null) {
 					IPath webDotXmlPath = webArtifact.getDeploymentDescriptorPath();
 					if (webDotXmlPath != null) {
 						IPath path = webDotXmlPath.removeLastSegments(2).append(manifestLocation)
 								.removeFirstSegments(1);
 						IFile manifestFileHandle = javaProject.getProject().getFile(path);
 						if (manifestFileHandle.exists()) {
 							return manifestFileHandle;
 						}
 					}
 				}
 			}
 
 		}
 		catch (Exception e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a full qualified location of the META-INF folder.
 	 * <p>
 	 * This implementation searches the source folders of the {@link IJavaProject} and returns the first found
 	 * META-INF/MANIFEST.MF as the valid manifest.
 	 */
 	public static String locateManifestFolder(IJavaProject javaProject) {
 		IResource resource = locateManifest(javaProject, false);
 		if (resource != null) {
 			IContainer container = resource.getParent().getParent();
 			IPath location = container.getRawLocation();
 			if (location != null) {
 				return location.toString();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a {@link File} instance for the given <code>javaProject</code>.
 	 * <p>
 	 * This implementation searches the source folders of the {@link IJavaProject} and returns the first found
 	 * META-INF/MANIFEST.MF as the valid manifest.
 	 */
 	public static File locateManifestFile(IJavaProject javaProject, boolean testBundle) {
 		IResource resource = locateManifest(javaProject, testBundle);
 		if (resource != null) {
 			URI uri = convertResourceToUrl(resource);
 			if (uri != null) {
 				return new File(uri);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Converts a given {@link IResource} into a full qualified {@link URI} honoring eventual used Eclipse variables in
 	 * the path expression.
 	 */
 	private static URI convertResourceToUrl(IResource resource) {
 		if (resource != null) {
 			URI uri = resource.getRawLocationURI();
 			if (uri != null) {
 				String scheme = uri.getScheme();
 				if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
 					return uri;
 				}
 				else if ("sourcecontrol".equals(scheme)) {
 					// special case of Rational Team Concert
 					IPath path = resource.getLocation();
 					File file = path.toFile();
 					if (file.exists()) {
 						return file.toURI();
 					}
 				}
 				else {
 					IPathVariableManager variableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
 					return variableManager.resolveURI(uri);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Finds a {@link IResource} identified by a given <code>path</code> in the given <code>project</code>
 	 */
 	private static IResource findResource(IProject project, IPath path) {
 		if (path != null && project != null && path.removeFirstSegments(1) != null) {
 			return project.findMember(path.removeFirstSegments(1));
 		}
 		return null;
 	}
 
 	/**
 	 * Dumps a new MANIFEST.MF into the given {@link IJavaProject}.
 	 */
 	public static void createNewBundleManifest(IJavaProject javaProject, String symbolicName, String bundleVersion,
 			String providerName, String bundleName, String serverModule, Map<String, String> properties) {
 
 		BundleManifest manifest = null;
 
 		File existingManifestFile = locateManifestFile(javaProject, false);
 		if (existingManifestFile != null) {
 			FileReader reader = null;
 			try {
 				reader = new FileReader(existingManifestFile);
 				manifest = BundleManifestFactory.createBundleManifest(new FileReader(existingManifestFile));
 			}
 			catch (FileNotFoundException e) {
 			}
 			catch (IOException e) {
 			}
 			finally {
 				if (reader != null) {
 					try {
 						reader.close();
 					}
 					catch (IOException e) {
 					}
 				}
 			}
 		}
 		else {
 			manifest = BundleManifestFactory.createBundleManifest();
 		}
 
 		manifest.setBundleManifestVersion(2);
 		Dictionary<String, String> dictonary = manifest.toDictionary();
 		if (StringUtils.hasText(symbolicName)) {
 			dictonary.put(Constants.BUNDLE_SYMBOLICNAME, symbolicName);
 		}
 		if (StringUtils.hasText(bundleVersion)) {
 			dictonary.put(Constants.BUNDLE_VERSION, bundleVersion);
 		}
 		if (StringUtils.hasLength(bundleName)) {
 			dictonary.put(Constants.BUNDLE_NAME, bundleName);
 		}
 		if (StringUtils.hasText(providerName)) {
 			dictonary.put(Constants.BUNDLE_DESCRIPTION, providerName);
 		}
 
 		for (Map.Entry<String, String> entry : properties.entrySet()) {
 			if (StringUtils.hasLength(entry.getValue()) && StringUtils.hasLength(entry.getKey())) {
 				dictonary.put(entry.getKey(), entry.getValue());
 			}
 		}
 
 		Writer writer = null;
 		try {
 			if (existingManifestFile != null) {
 				writer = new FileWriter(existingManifestFile);
 			}
 			else {
 				writer = new FileWriter(getFirstPossibleManifestFile(javaProject.getProject(), false).getRawLocation()
 						.toFile());
 			}
 			BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(dictonary);
 			bundleManifest.write(writer);
 		}
 		catch (IOException e) {
 		}
 		finally {
 			if (writer != null) {
 				try {
 					writer.flush();
 					writer.close();
 				}
 				catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	public static void createNewParManifest(IProject project, String symbolicName, String version, String name,
 			String description) {
 		Dictionary<String, String> manifest = BundleManifestFactory.createBundleManifest().toDictionary();
 		if (StringUtils.hasText(symbolicName)) {
 			manifest.put("Application-SymbolicName", symbolicName);
 		}
 		if (StringUtils.hasText(version)) {
 			manifest.put("Application-Version", version);
 		}
 		if (StringUtils.hasLength(name)) {
 			manifest.put("Application-Name", name);
 		}
 		if (StringUtils.hasText(description)) {
 			manifest.put("Application-Description", description);
 		}
 
 		Writer writer = null;
 		try {
 			writer = new FileWriter(getFirstPossibleManifestFile(project, false).getRawLocation().toFile());
 			BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(manifest);
 			bundleManifest.write(writer);
 		}
 		catch (IOException e) {
 		}
 		finally {
 			if (writer != null) {
 				try {
 					writer.flush();
 					writer.close();
 				}
 				catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	public static IFile getFirstPossibleManifestFile(final IProject project, boolean isTestManifest) {
 		try {
 			if (JdtUtils.isJavaProject(project)) {
 
 				List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(ServerModuleDelegate
 						.getSourceClasspathEntries(project, isTestManifest));
 				Collections.sort(entries, new Comparator<IClasspathEntry>() {
 
 					public int compare(IClasspathEntry o1, IClasspathEntry o2) {
 						String s1 = o1.getPath().toString();
 						String s2 = o2.getPath().toString();
 						if (("/" + project.getName() + "/src/main/resources").equals(s1)) {
 							return -1;
 						}
 						else if (("/" + project.getName() + "/src/test/resources").equals(s1)) {
 							return -1;
 						}
 						if (("/" + project.getName() + "/src/main/resources").equals(s2)) {
 							return 1;
 						}
 						else if (("/" + project.getName() + "/src/test/resources").equals(s2)) {
 							return 1;
 						}
 						return s1.compareTo(s2);
 					}
 				});
 
 				for (IClasspathEntry entry : entries) {
 					return createNewManifestInFolder(findResource(project, entry.getPath()), isTestManifest);
 				}
 			}
 			else {
 				return createNewManifestInFolder(project, isTestManifest);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static IFile createNewManifestInFolder(IResource resource, boolean isTestManifest) throws CoreException {
 		String manifestFilePath = "META-INF/" + (isTestManifest ? "TEST.MF" : "MANIFEST.MF");
 		IFile manifestFile = null;
 		if (resource instanceof IFolder) {
 			manifestFile = ((IFolder) resource).getFile(manifestFilePath);
 		}
 		else if (resource instanceof IProject) {
 			manifestFile = ((IProject) resource).getFile(manifestFilePath);
 		}
 		if (manifestFile != null && !manifestFile.exists()) {
 			if (!manifestFile.getParent().exists()) {
 				((IFolder) manifestFile.getParent()).create(true, true, new NullProgressMonitor());
 			}
 			manifestFile.create(new ByteArrayInputStream("Manifest-Version: 1.0".getBytes()), true,
 					new NullProgressMonitor());
 		}
 		return manifestFile;
 	}
 
 	public static int getLineNumber(IDocument document, BundleManifestHeader header, String valueSubstring) {
 		// make sure that we have a line number in case the bundle model is crashed
 		if (header == null) {
 			return 0;
 		}
 
 		for (int l = header.getLineNumber(); l < header.getLineNumber() + header.getLinesSpan(); l++) {
 			try {
 				IRegion lineRegion = document.getLineInformation(l);
 				String lineStr = document.get(lineRegion.getOffset(), lineRegion.getLength());
 				if (lineStr.indexOf(valueSubstring) >= 0) {
 					return l + 1;
 				}
 			}
 			catch (BadLocationException ble) {
 			}
 		}
 		// it might span multiple lines, try a longer algorithm
 		try {
 			IRegion lineRegion = document.getLineInformation(header.getLineNumber());
 			String lineStr = document.get(lineRegion.getOffset(), lineRegion.getLength());
 			for (int l = header.getLineNumber() + 1; l < header.getLineNumber() + header.getLinesSpan(); l++) {
 				lineRegion = document.getLineInformation(l);
 				lineStr += document.get(lineRegion.getOffset() + 1/* the space */, lineRegion.getLength());
 				if (lineStr.indexOf(valueSubstring) >= 0) {
 					return l;
 				}
 			}
 		}
 		catch (BadLocationException ble) {
 		}
 		return header.getLineNumber() + 1;
 	}
 
 	public static int getPackageLineNumber(IDocument document, BundleManifestHeader header, ManifestElement element) {
 		String packageName = element.getValue();
 		if (element.getDirectiveKeys() != null || element.getKeys() != null)
 			return getLineNumber(document, header, packageName + ";");
 
 		// check for this exact package on the last line
 		try {
 			IRegion lineRegion = document.getLineInformation(header.getLineNumber() + header.getLinesSpan() - 1);
 			String lineStr = document.get(lineRegion.getOffset(), lineRegion.getLength());
 			if (lineStr.endsWith(packageName)) {
 				return header.getLineNumber() + header.getLinesSpan();
 			}
 		}
 		catch (BadLocationException ble) {
 		}
 
 		// search all except last line
 		return getLineNumber(document, header, packageName + ",");
 	}
 
 }
