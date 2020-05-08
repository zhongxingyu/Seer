 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.project;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.java.JavaRefFactory;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.WorkbenchByteArrayOutputStream;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.util.plugin.JEMUtilPlugin;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EJBJarFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.DeploymentDescriptorLoadException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.impl.CommonarchiveFactoryImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.JavaComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.componentcore.AppClientBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.componentcore.EJBBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.componentcore.JCABinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.componentcore.WebBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.moduleextension.EarModuleManager;
 import org.eclipse.jst.j2ee.project.facet.IJavaProjectMigrationDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.JavaProjectMigrationDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.JavaProjectMigrationOperation;
 import org.eclipse.jst.server.core.FacetUtil;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.server.core.IRuntime;
 
 public class J2EEProjectUtilities extends ProjectUtilities {
 
 	public static final String ENTERPRISE_APPLICATION = IModuleConstants.JST_EAR_MODULE;
 
 	public static final String APPLICATION_CLIENT = IModuleConstants.JST_APPCLIENT_MODULE;
 
 	public static final String EJB = IModuleConstants.JST_EJB_MODULE;
 
 	public static final String DYNAMIC_WEB = IModuleConstants.JST_WEB_MODULE;
 
 	public static final String UTILITY = IModuleConstants.JST_UTILITY_MODULE;
 
 	public static final String JCA = IModuleConstants.JST_CONNECTOR_MODULE;
 
 	public static final String STATIC_WEB = IModuleConstants.WST_WEB_MODULE;
 
 	/**
 	 * Return the absolute path of a loose archive in a J2EE application or WAR file
 	 */
 	public static IPath getRuntimeLocation(IProject aProject) {
 		if (JemProjectUtilities.isBinaryProject(aProject))
 			return getBinaryProjectJARLocation(aProject);
 		return JemProjectUtilities.getJavaProjectOutputAbsoluteLocation(aProject);
 	}
 
 	public static IPath getBinaryProjectJARLocation(IProject aProject) {
 		List sources = JemProjectUtilities.getLocalJARPathsFromClasspath(aProject);
 		if (!sources.isEmpty()) {
 			IPath path = (IPath) sources.get(0);
 			return aProject.getFile(path).getLocation();
 		}
 		return null;
 	}
 
 	public static Archive getClientJAR(EJBJarFile file, EARFile earFile) {
 		EJBJar jar = null;
 		try {
 			jar = file.getDeploymentDescriptor();
 		} catch (DeploymentDescriptorLoadException exc) {
 			return null;
 		}
 		if (jar == null)
 			return null;
 		String clientJAR = jar.getEjbClientJar();
 		if (clientJAR == null || clientJAR.length() == 0)
 			return null;
 		String normalized = ArchiveUtil.deriveEARRelativeURI(clientJAR, file.getURI());
 		if (normalized != null) {
 			try {
 				File aFile = earFile.getFile(normalized);
 				if (aFile.isArchive() && !aFile.isModuleFile())
 					return (Archive) aFile;
 			} catch (FileNotFoundException nothingThere) {
 			}
 		}
 		return null;
 		// TODO - release the DD here to free up space
 	}
 
 	/**
 	 * Append one IClasspathEntry to the build path of the passed project. If a classpath entry
 	 * having the same path as the parameter already exists, then does nothing.
 	 */
 	public static void appendJavaClassPath(IProject p, IClasspathEntry newEntry) throws JavaModelException {
 		IJavaProject javaProject = JemProjectUtilities.getJavaProject(p);
 		if (javaProject == null)
 			return;
 		IClasspathEntry[] classpath = javaProject.getRawClasspath();
 		List newPathList = new ArrayList(classpath.length);
 		for (int i = 0; i < classpath.length; i++) {
 			IClasspathEntry entry = classpath[i];
 			// fix dup class path entry for .JETEmitter project
 			// Skip the entry to be added if it already exists
 			if (Platform.getOS().equals(Platform.OS_WIN32)) {
 				if (!entry.getPath().toString().equalsIgnoreCase(newEntry.getPath().toString()))
 					newPathList.add(entry);
 				else
 					return;
 			} else {
 				if (!entry.getPath().equals(newEntry.getPath()))
 					newPathList.add(entry);
 				else
 					return;
 			}
 		}
 		newPathList.add(newEntry);
 		IClasspathEntry[] newClasspath = (IClasspathEntry[]) newPathList.toArray(new IClasspathEntry[newPathList.size()]);
 		javaProject.setRawClasspath(newClasspath, new NullProgressMonitor());
 	}
 
 	public static Archive asArchiveFromBinary(String jarUri, IProject aProject) throws OpenFailureException {
 		IPath path = getBinaryProjectJARLocation(aProject);
 		if (path != null) {
 			String location = path.toOSString();
 			Archive anArchive = CommonarchiveFactoryImpl.getActiveFactory().primOpenArchive(location);
 			anArchive.setURI(jarUri);
 			return anArchive;
 		}
 		return null;
 	}
 
 	public static ArchiveManifest readManifest(IFile aFile) {
 		InputStream in = null;
 		try {
 			if (aFile == null || !aFile.exists())
 				return null;
 			in = aFile.getContents();
 			return new ArchiveManifestImpl(in);
 		} catch (Exception ex) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			return null;
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException weTried) {
 				}
 			}
 		}
 	}
 
 	public static ArchiveManifest readManifest(IProject p) {
 		InputStream in = null;
 		try {
 			IFile aFile = getManifestFile(p);
 			if (aFile == null || !aFile.exists())
 				return null;
 			in = aFile.getContents();
 			return new ArchiveManifestImpl(in);
 		} catch (Exception ex) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			return null;
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException weTried) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Equavalent to calling getManifestFile(project, true)
 	 * 
 	 * @param p
 	 * @return
 	 */
 	public static IFile getManifestFile(IProject project) {
 		return getManifestFile(project, true);
 	}
 
 	/**
 	 * Returns the IFile handle to the J2EE manifest file for the specified
 	 * project. If createIfNecessary is true, the MANIFEST.MF file will be
 	 * created if it does not already exist.
 	 * 
 	 * @param p
 	 * @param createIfNecessary
 	 * @return
 	 */
 	public static IFile getManifestFile(IProject p, boolean createIfNecessary) {
 		IVirtualComponent component = ComponentCore.createComponent(p);
 		try {
 			IFile file = ComponentUtilities.findFile(component, new Path(J2EEConstants.MANIFEST_URI));
 			if (createIfNecessary && file == null) {
 				IVirtualFolder virtualFolder = component.getRootFolder();
 				file = virtualFolder.getUnderlyingFolder().getFile(new Path(J2EEConstants.MANIFEST_URI));
 
 				try {
 					ManifestFileCreationAction.createManifestFile(file, p);
 				} catch (CoreException e) {
 					Logger.getLogger().log(e);
 				} catch (IOException e) {
 					Logger.getLogger().log(e);
 				}
 			}
 			return file;
 		} catch (CoreException ce) {
 			Logger.getLogger().log(ce);
 		}
 		return null;
 	}
 
 	public static void writeManifest(IProject aProject, ArchiveManifest manifest) throws java.io.IOException {
 		writeManifest(aProject, getManifestFile(aProject), manifest);
 	}
 
 	public static void writeManifest(IFile aFile, ArchiveManifest manifest) throws java.io.IOException {
 		writeManifest(aFile.getProject(), aFile, manifest);
 	}
 
 	private static void writeManifest(IProject aProject, IFile aFile, ArchiveManifest manifest) throws java.io.IOException {
 		if (aFile != null) {
			OutputStream out = new WorkbenchByteArrayOutputStream(aFile);
			manifest.writeSplittingClasspath(out);
			out.close();
			J2EEComponentClasspathUpdater.getInstance().queueUpdateModule(aProject);
 		}
 	}
 
 	/**
 	 * Keys are the EJB JAR files and the values are the respective client JARs; includes only key
 	 * value pairs for which EJB Client JARs are defined and exist.
 	 * 
 	 * @author schacher
 	 */
 	public static Map collectEJBClientJARs(EARFile earFile) {
 		if (earFile == null)
 			return Collections.EMPTY_MAP;
 		Map ejbClientJARs = null;
 		List ejbJARFiles = earFile.getEJBJarFiles();
 		Archive clientJAR = null;
 		for (int i = 0; i < ejbJARFiles.size(); i++) {
 			EJBJarFile ejbJarFile = (EJBJarFile) ejbJARFiles.get(i);
 			clientJAR = getClientJAR(ejbJarFile, earFile);
 			if (clientJAR != null) {
 				if (ejbClientJARs == null)
 					ejbClientJARs = new HashMap();
 				ejbClientJARs.put(ejbJarFile, clientJAR);
 			}
 		}
 		return ejbClientJARs == null ? Collections.EMPTY_MAP : ejbClientJARs;
 	}
 
 	public static String computeRelativeText(String referencingURI, String referencedURI, EnterpriseBean bean) {
 		if (bean == null)
 			return null;
 
 		String beanName = bean.getName();
 		if (beanName == null)
 			return null;
 
 		String relativeUri = computeRelativeText(referencingURI, referencedURI);
 		if (relativeUri == null)
 			return beanName;
 		return relativeUri + "#" + beanName; //$NON-NLS-1$
 	}
 
 	public static String computeRelativeText(String referencingURI, String referencedURI) {
 		if (referencingURI == null || referencedURI == null)
 			return null;
 		IPath pPre = new Path(referencingURI);
 		IPath pDep = new Path(referencedURI);
 		if (pPre.getDevice() != null || pDep.getDevice() != null)
 			return null;
 		pPre = pPre.makeRelative();
 		pDep = pDep.makeRelative(); // referenced Archive path URI
 
 		while (pPre.segmentCount() > 1 && pDep.segmentCount() > 1 && pPre.segment(0).equals(pDep.segment(0))) {
 			pPre = pPre.removeFirstSegments(1);
 			pDep = pDep.removeFirstSegments(1);
 		}
 
 		IPath result = null;
 		StringBuffer buf = new StringBuffer();
 		String segment = null;
 		do {
 			segment = pDep.lastSegment();
 			pPre = pPre.removeLastSegments(1);
 			pDep = pDep.removeLastSegments(1);
 			if (segment != null) {
 				if (result == null)
 					result = new Path(segment);
 				else
 					result = new Path(segment).append(result);
 			}
 			if (!pPre.equals(pDep) && !pPre.isEmpty())
 				buf.append("../"); //$NON-NLS-1$
 		} while (!pPre.equals(pDep));
 
 		if (result != null)
 			buf.append(result.makeRelative().toString());
 
 		return buf.toString();
 	}
 
 	public static IProject getEJBProjectFromEJBClientProject(IProject ejbClientProject) {
 		try {
 			if (null != ejbClientProject && ejbClientProject.hasNature(JavaCore.NATURE_ID)) {
 				IProject[] allProjects = getAllProjects();
 				for (int i = 0; i < allProjects.length; i++) {
 					if (null != EarModuleManager.getEJBModuleExtension().getEJBJar(allProjects[i])) {
 						if (ejbClientProject == EarModuleManager.getEJBModuleExtension().getDefinedEJBClientJARProject(allProjects[i])) {
 							return allProjects[i];
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 		}
 		return null;
 	}
 
 	public static EnterpriseBean getEnterpriseBean(ICompilationUnit cu) {
 		IProject proj = cu.getJavaProject().getProject();
 		EJBJar jar = EarModuleManager.getEJBModuleExtension().getEJBJar(proj);
 		if (null == jar) {
 			jar = EarModuleManager.getEJBModuleExtension().getEJBJar(getEJBProjectFromEJBClientProject(proj));
 		}
 		if (jar != null) {
 			int index = cu.getElementName().indexOf('.');
 			String className = cu.getElementName();
 			if (index > 0)
 				className = className.substring(0, index);
 			JavaClass javaClass = (JavaClass) JavaRefFactory.eINSTANCE.reflectType(cu.getParent().getElementName(), className, jar.eResource().getResourceSet());
 			return jar.getEnterpriseBeanWithReference(javaClass);
 		}
 		return null;
 	}
 
 	public static IContainer getSourceFolderOrFirst(IProject p, String defaultSourceName) {
 		try {
 			IPath sourcePath = getSourcePathOrFirst(p, defaultSourceName);
 			if (sourcePath == null)
 				return null;
 			else if (sourcePath.isEmpty())
 				return p;
 			else
 				return p.getFolder(sourcePath);
 		} catch (IllegalArgumentException ex) {
 			return null;
 		}
 	}
 
 	public static void removeBuilders(IProject project, List builderids) throws CoreException {
 		IProjectDescription desc = project.getDescription();
 		ICommand[] oldSpec = desc.getBuildSpec();
 		int oldLength = oldSpec.length;
 		if (oldLength == 0)
 			return;
 		int remaining = 0;
 		// null out all commands that match the builder to remove
 		for (int i = 0; i < oldSpec.length; i++) {
 			if (builderids.contains(oldSpec[i].getBuilderName()))
 				oldSpec[i] = null;
 			else
 				remaining++;
 		}
 		// check if any were actually removed
 		if (remaining == oldSpec.length)
 			return;
 		ICommand[] newSpec = new ICommand[remaining];
 		for (int i = 0, newIndex = 0; i < oldLength; i++) {
 			if (oldSpec[i] != null)
 				newSpec[newIndex++] = oldSpec[i];
 		}
 		desc.setBuildSpec(newSpec);
 		project.setDescription(desc, IResource.NONE, null);
 	}
 
 	public static IPath getSourcePathOrFirst(IProject p, String defaultSourceName) {
 		IJavaProject javaProj = JemProjectUtilities.getJavaProject(p);
 		if (javaProj == null)
 			return null;
 		IClasspathEntry[] cp = null;
 		try {
 			cp = javaProj.getRawClasspath();
 		} catch (JavaModelException ex) {
 			JEMUtilPlugin.getLogger().logError(ex);
 			return null;
 		}
 		IClasspathEntry firstSource = null;
 		IPath defaultSourcePath = null;
 		if (defaultSourceName != null)
 			defaultSourcePath = createPath(p, defaultSourceName);
 		boolean found = false;
 		for (int i = 0; i < cp.length; i++) {
 			if (cp[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 				// check if it contains /META-INF/MANIFEST.MF
 				IPath sourceFolderPath = cp[i].getPath().removeFirstSegments(1);
 				IFolder sourceFolder = p.getFolder(sourceFolderPath);
 				if (isSourceFolderAnInputContainer(sourceFolder)) {
 					found = true;
 					if (firstSource == null) {
 						firstSource = cp[i];
 						if (defaultSourcePath == null)
 							break;
 					}
 					if (cp[i].getPath().equals(defaultSourcePath))
 						return defaultSourcePath.removeFirstSegments(1);
 				}
 			}
 		}
 		if (!found) {
 			for (int i = 0; i < cp.length; i++) {
 				if (cp[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					if (firstSource == null) {
 						firstSource = cp[i];
 						if (defaultSourcePath == null)
 							break;
 					}
 					if (cp[i].getPath().equals(defaultSourcePath))
 						return defaultSourcePath.removeFirstSegments(1);
 				}
 			}
 		}
 		if (firstSource == null)
 			return null;
 		if (firstSource.getPath().segment(0).equals(p.getName()))
 			return firstSource.getPath().removeFirstSegments(1);
 		return null;
 	}
 
 	public static boolean isSourceFolderAnInputContainer(IFolder sourceFolder) {
 		IContainer parent = sourceFolder;
 		while (true) {
 			parent = parent.getParent();
 			if (parent == null)
 				return false;
 			if (parent instanceof IProject)
 				break;
 		}
 		IProject project = (IProject) parent;
 		try {
 			if (!project.isAccessible())
 				return false;
 			if (isEJBProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.EJBJAR_DD_URI) != null;
 			} else if (isApplicationClientProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.APP_CLIENT_DD_URI) != null;
 			} else if (isDynamicWebProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.WEBAPP_DD_URI) != null;
 			} else if (isJCAProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.RAR_DD_URI) != null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static Archive asArchive(String jarUri, IProject project, boolean exportSource) throws OpenFailureException {
 		return asArchive(jarUri, project, exportSource, true);		
 	}
 	
 	public static Archive asArchive(String jarUri, IProject project, boolean exportSource, boolean includeClasspathComponents) throws OpenFailureException {
 		JavaComponentLoadStrategyImpl strat = new JavaComponentLoadStrategyImpl(ComponentCore.createComponent(project), includeClasspathComponents);
 		strat.setExportSource(exportSource);
 		return CommonarchiveFactoryImpl.getActiveFactory().primOpenArchive(strat, jarUri);
 	}
 
 	public static boolean isProjectOfType(IProject project, String typeID) {
 		IFacetedProject facetedProject = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 		} catch (CoreException e) {
 			return false;
 		}
 
 		if (facetedProject != null && ProjectFacetsManager.isProjectFacetDefined(typeID)) {
 			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(typeID);
 			return projectFacet != null && facetedProject.hasProjectFacet(projectFacet);
 		}
 		return false;
 	}
 
 	private static boolean isProjectOfType(IFacetedProject facetedProject, String typeID) {
 
 		if (facetedProject != null && ProjectFacetsManager.isProjectFacetDefined(typeID)) {
 			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(typeID);
 			return projectFacet != null && facetedProject.hasProjectFacet(projectFacet);
 		}
 		return false;
 	}
 
 	private static boolean isEARProject(IFacetedProject project) {
 		return isProjectOfType(project, ENTERPRISE_APPLICATION);
 	}
 
 	private static boolean isDynamicWebProject(IFacetedProject project) {
 		return isProjectOfType(project, DYNAMIC_WEB);
 	}
 
 	private static boolean isStaticWebProject(IFacetedProject project) {
 		return isProjectOfType(project, STATIC_WEB);
 	}
 
 	private static boolean isEJBProject(IFacetedProject project) {
 		return isProjectOfType(project, EJB);
 	}
 
 	private static boolean isJCAProject(IFacetedProject project) {
 		return isProjectOfType(project, JCA);
 	}
 
 	private static boolean isApplicationClientProject(IFacetedProject project) {
 		return isProjectOfType(project, APPLICATION_CLIENT);
 	}
 
 	private static boolean isUtilityProject(IFacetedProject project) {
 		return isProjectOfType(project, UTILITY);
 	}
 
 	public static boolean isEARProject(IProject project) {
 		return isProjectOfType(project, ENTERPRISE_APPLICATION);
 	}
 
 	public static boolean isDynamicWebComponent(IVirtualComponent component) {
 		if (component.isBinary()) {
 			return WebBinaryComponentHelper.handlesComponent(component);
 		}
 		return isProjectOfType(component.getProject(), DYNAMIC_WEB);
 	}
 
 	public static boolean isDynamicWebProject(IProject project) {
 		return isProjectOfType(project, DYNAMIC_WEB);
 	}
 
 	public static boolean isStaticWebProject(IProject project) {
 		return isProjectOfType(project, STATIC_WEB);
 	}
 
 	public static boolean isEJBComponent(IVirtualComponent component) {
 		if (component.isBinary()) {
 			return EJBBinaryComponentHelper.handlesComponent(component);
 		}
 		return isProjectOfType(component.getProject(), EJB);
 	}
 
 	public static boolean isEJBProject(IProject project) {
 		return isProjectOfType(project, EJB);
 	}
 
 	public static boolean isJCAComponent(IVirtualComponent component) {
 		if (component.isBinary()) {
 			return JCABinaryComponentHelper.handlesComponent(component);
 		}
 		return isProjectOfType(component.getProject(), JCA);
 	}
 
 	public static boolean isJCAProject(IProject project) {
 		return isProjectOfType(project, JCA);
 	}
 
 	public static boolean isApplicationClientComponent(IVirtualComponent component) {
 		if (component.isBinary()) {
 			return AppClientBinaryComponentHelper.handlesComponent(component);
 		}
 		return isProjectOfType(component.getProject(), APPLICATION_CLIENT);
 	}
 
 	public static boolean isApplicationClientProject(IProject project) {
 		return isProjectOfType(project, APPLICATION_CLIENT);
 	}
 
 	public static boolean isUtilityProject(IProject project) {
 		return isProjectOfType(project, UTILITY);
 	}
 
 	public static boolean isStandaloneProject(IProject project) {
 		return getReferencingEARProjects(project).length == 0;
 	}
 
 	
 	
 	public static IProject[] getReferencingEARProjects(IProject project) {
 		if(project != null && isEARProject(project)){
 			return new IProject[] {project};
 		}
 		
 		List result = new ArrayList();
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if (component != null) {
 			IVirtualComponent[] refComponents = component.getReferencingComponents();
 			for (int i = 0; i < refComponents.length; i++) {
 				if (isEARProject(refComponents[i].getProject()))
 					result.add(refComponents[i].getProject());
 			}
 		}
 		return (IProject[]) result.toArray(new IProject[result.size()]);
 	}
 
 	/**
 	 * Return all projects in workspace of the specified type
 	 * 
 	 * @param type -
 	 *            use one of the static strings on this class as a type
 	 * @return IProject[]
 	 */
 	public static IProject[] getAllProjectsInWorkspaceOfType(String type) {
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		List result = new ArrayList();
 		for (int i = 0; i < projects.length; i++) {
 			if (isProjectOfType(projects[i], type))
 				result.add(projects[i]);
 		}
 		return (IProject[]) result.toArray(new IProject[result.size()]);
 	}
 
 	public static String getJ2EEComponentType(IVirtualComponent component) {
 		if (null != component) {
 			if (component.isBinary()) {
 				if (isApplicationClientComponent(component))
 					return APPLICATION_CLIENT;
 				else if (isDynamicWebComponent(component))
 					return DYNAMIC_WEB;
 				else if (isEJBComponent(component))
 					return EJB;
 				else if (isJCAComponent(component))
 					return JCA;
 				else
 					return UTILITY;
 			}
 			return getJ2EEProjectType(component.getProject());
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	public static String getJ2EEProjectType(IProject project) {
 		if (null != project && project.isAccessible()) {
 			IFacetedProject facetedProject = null;
 			try {
 				facetedProject = ProjectFacetsManager.create(project);
 			} catch (CoreException e) {
 				return ""; //$NON-NLS-1$
 			}
 			if (isApplicationClientProject(facetedProject))
 				return APPLICATION_CLIENT;
 			else if (isDynamicWebProject(facetedProject))
 				return DYNAMIC_WEB;
 			else if (isEJBProject(facetedProject))
 				return EJB;
 			else if (isEARProject(facetedProject))
 				return ENTERPRISE_APPLICATION;
 			else if (isJCAProject(facetedProject))
 				return JCA;
 			else if (isStaticWebProject(facetedProject))
 				return STATIC_WEB;
 			else if (isUtilityProject(facetedProject))
 				return UTILITY;
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	public static IRuntime getServerRuntime(IProject project) throws CoreException {
 		if (project == null)
 			return null;
 		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
 		if (facetedProject == null)
 			return null;
 		org.eclipse.wst.common.project.facet.core.runtime.IRuntime runtime = facetedProject.getRuntime();
 		if (runtime == null)
 			return null;
 		return FacetUtil.getRuntime(runtime);
 	}
 
 	public static String getJ2EEProjectVersion(IProject project) {
 		String type = getJ2EEProjectType(project);
 		IFacetedProject facetedProject = null;
 		IProjectFacet facet = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 			facet = ProjectFacetsManager.getProjectFacet(type);
 		} catch (Exception e) {
 			// Not Faceted project or not J2EE Project
 		}
 		if (facet != null && facetedProject.hasProjectFacet(facet))
 			return facetedProject.getInstalledVersion(facet).getVersionString();
 		return null;
 	}
 
 	public static JavaProjectMigrationOperation createFlexJavaProjectForProjectOperation(IProject project) {
 		IDataModel model = DataModelFactory.createDataModel(new JavaProjectMigrationDataModelProvider());
 		model.setProperty(IJavaProjectMigrationDataModelProperties.PROJECT_NAME, project.getName());
 		return new JavaProjectMigrationOperation(model);
 	}
 
 	/**
 	 * Retrieve all the source containers for a given virtual workbench component
 	 * 
 	 * @param vc
 	 * @return the array of IPackageFragmentRoots
 	 */
 	public static IPackageFragmentRoot[] getSourceContainers(IProject project) {
 		IJavaProject jProject = JemProjectUtilities.getJavaProject(project);
 		if (jProject == null)
 			return new IPackageFragmentRoot[0];
 		List list = new ArrayList();
 		IVirtualComponent vc = ComponentCore.createComponent(project);
 		IPackageFragmentRoot[] roots;
 		try {
 			roots = jProject.getPackageFragmentRoots();
 			for (int i = 0; i < roots.length; i++) {
 				if (roots[i].getKind() != IPackageFragmentRoot.K_SOURCE)
 					continue;
 				IResource resource = roots[i].getResource();
 				if (null != resource) {
 					IVirtualResource[] vResources = ComponentCore.createResources(resource);
 					boolean found = false;
 					for (int j = 0; !found && j < vResources.length; j++) {
 						if (vResources[j].getComponent().equals(vc)) {
 							if (!list.contains(roots[i]))
 								list.add(roots[i]);
 							found = true;
 						}
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			Logger.getLogger().logError(e);
 		}
 		return (IPackageFragmentRoot[]) list.toArray(new IPackageFragmentRoot[list.size()]);
 	}
 
 	/**
 	 * Retrieve all the output containers for a given virtual component.
 	 * 
 	 * @param vc
 	 * @return array of IContainers for the output folders
 	 */
 	public static IContainer[] getOutputContainers(IProject project) {
 		List result = new ArrayList();
 		try {
 			if (!project.hasNature(JavaCore.NATURE_ID))
 				return new IContainer[] {};
 		} catch (Exception e) {
 		}
 		IPackageFragmentRoot[] sourceContainers = getSourceContainers(project);
 		for (int i = 0; i < sourceContainers.length; i++) {
 			IContainer outputFolder = getOutputContainer(project, sourceContainers[i]);
 			if (outputFolder != null && !result.contains(outputFolder))
 				result.add(outputFolder);
 		}
 		return (IContainer[]) result.toArray(new IContainer[result.size()]);
 	}
 
 	public static IContainer getOutputContainer(IProject project, IPackageFragmentRoot sourceContainer) {
 		try {
 			IJavaProject jProject = JavaCore.create(project);
 			IPath outputPath = sourceContainer.getRawClasspathEntry().getOutputLocation();
 			if (outputPath == null) {
 				if (jProject.getOutputLocation().segmentCount() == 1)
 					return project;
 				return project.getFolder(jProject.getOutputLocation().removeFirstSegments(1));
 			}
 			return project.getFolder(outputPath.removeFirstSegments(1));
 		} catch (Exception e) {
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param name
 	 * @return
 	 * @description the passed name should have either lib or var as its first segment e.g.
 	 *              lib/D:/foo/foo.jar or var/<CLASSPATHVAR>/foo.jar
 	 */
 	public static IPath getResolvedPathForArchiveComponent(String name) {
 
 		URI uri = URI.createURI(name);
 
 		String resourceType = uri.segment(0);
 		URI contenturi = ModuleURIUtil.trimToRelativePath(uri, 1);
 		String contentName = contenturi.toString();
 
 		if (resourceType.equals("lib")) { //$NON-NLS-1$
 			// module:/classpath/lib/D:/foo/foo.jar
 			return Path.fromOSString(contentName);
 
 		} else if (resourceType.equals("var")) { //$NON-NLS-1$
 
 			// module:/classpath/var/<CLASSPATHVAR>/foo.jar
 			String classpathVar = contenturi.segment(0);
 			URI remainingPathuri = ModuleURIUtil.trimToRelativePath(contenturi, 1);
 			String remainingPath = remainingPathuri.toString();
 
 			String[] classpathvars = JavaCore.getClasspathVariableNames();
 			boolean found = false;
 			for (int i = 0; i < classpathvars.length; i++) {
 				if (classpathVar.equals(classpathvars[i])) {
 					found = true;
 					break;
 				}
 			}
 			if (found) {
 				IPath path = JavaCore.getClasspathVariable(classpathVar);
 				URI finaluri = URI.createURI(path.toOSString() + IPath.SEPARATOR + remainingPath);
 				return Path.fromOSString(finaluri.toString());
 			}
 		}
 		return null;
 	}
 
 	public static List getAllJavaNonFlexProjects() throws CoreException {
 		List nonFlexJavaProjects = new ArrayList();
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (int i = 0; i < projects.length; i++) {
 			if (projects[i].isAccessible() && projects[i].hasNature(JavaCore.NATURE_ID) && !projects[i].hasNature(IModuleConstants.MODULE_NATURE_ID)) {
 				nonFlexJavaProjects.add(projects[i]);
 			}
 		}
 		return nonFlexJavaProjects;
 	}
 
 	/**
 	 * This method will retrieve the context root for the associated workbench module which is used
 	 * by the server at runtime. This method is not yet completed as the context root has to be
 	 * abstracted and added to the workbenchModule model. This API will not change though. Returns
 	 * null for now.
 	 * 
 	 * @return String value of the context root for runtime of the associated module
 	 */
 	public static String getServerContextRoot(IProject project) {
 		return ComponentUtilities.getServerContextRoot(project);
 	}
 
 	/**
 	 * This method will set the context root on the associated workbench module with the given
 	 * string value passed in. This context root is used by the server at runtime. This method is
 	 * not yet completed as the context root still needs to be abstracted and added to the workbench
 	 * module model. This API will not change though. Does nothing as of now.
 	 * 
 	 * @param contextRoot
 	 *            string
 	 */
 	public static void setServerContextRoot(IProject project, String contextRoot) {
 		ComponentUtilities.setServerContextRoot(project, contextRoot);
 	}
 	
 	/**
 	 * @param project
 	 * @return true, if jee version 5.0 (or their respective ejb, web, app versions)
 	 * , it must be noted that this method only looks at the facet & their versions to determine 
 	 * the jee level. It does not read deployment descriptors for performance reasons.
 	 */
 	public static boolean isJEEProject(IProject project){
 		boolean ret = false;
 		
 		IFacetedProject facetedProject;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 			if (facetedProject == null)
 				return false;
 			if(isEARProject(facetedProject)){
 				IProjectFacet earFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EAR_MODULE);
 				ret = facetedProject.hasProjectFacet(earFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.JEE_5_0_ID)));
 			} else if(isDynamicWebProject(facetedProject)){
 				IProjectFacet webFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_WEB_MODULE);
 				ret = facetedProject.hasProjectFacet(webFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToWebVersionID(J2EEVersionConstants.JEE_5_0_ID))));
 			} else if(isEJBProject(facetedProject)){
 				IProjectFacet ejbFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EJB_MODULE);
 				ret = facetedProject.hasProjectFacet(ejbFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToEJBVersionID(J2EEVersionConstants.JEE_5_0_ID))));
 			} else if(isApplicationClientProject(facetedProject)){
 				IProjectFacet appFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_APPCLIENT_MODULE);
 				ret = facetedProject.hasProjectFacet(appFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.JEE_5_0_ID)));
 			}
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	/**
 	 * @param project
 	 * @return true, if j2ee version 1.2, 1.3, 1.4 (or their respective ejb, web, app versions)
 	 * , it must be noted that this method only looks at the facet & their versions to determine 
 	 * the j2ee level. It does not read deployment descriptors for performance reasons.
 	 */
 	public static boolean isLegacyJ2EEProject(IProject project){
 		boolean ret = false;
 		
 		IFacetedProject facetedProject;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 			if (facetedProject == null)
 				return false;
 			
 			if(isEARProject(facetedProject)){
 				IProjectFacet earFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EAR_MODULE);
 				ret = (facetedProject.hasProjectFacet(earFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.J2EE_1_4_ID)))
 						|| facetedProject.hasProjectFacet(earFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.J2EE_1_3_ID)))
 						|| facetedProject.hasProjectFacet(earFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.J2EE_1_2_ID)))
 					);
 			} else if(isDynamicWebProject(facetedProject)){
 				IProjectFacet webFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_WEB_MODULE);
 				ret = (facetedProject.hasProjectFacet(webFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToWebVersionID(J2EEVersionConstants.J2EE_1_4_ID))))
 						|| facetedProject.hasProjectFacet(webFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToWebVersionID(J2EEVersionConstants.J2EE_1_3_ID))))
 						|| facetedProject.hasProjectFacet(webFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToWebVersionID(J2EEVersionConstants.J2EE_1_2_ID))))
 						);
 			} else if(isEJBProject(facetedProject)){
 				IProjectFacet ejbFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EJB_MODULE);
 				ret = (facetedProject.hasProjectFacet(ejbFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToEJBVersionID(J2EEVersionConstants.J2EE_1_4_ID))))
 						|| facetedProject.hasProjectFacet(ejbFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToEJBVersionID(J2EEVersionConstants.J2EE_1_3_ID))))
 						|| facetedProject.hasProjectFacet(ejbFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToEJBVersionID(J2EEVersionConstants.J2EE_1_2_ID))))
 						);
 			} else if(isApplicationClientProject(facetedProject)){
 				IProjectFacet appFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_APPCLIENT_MODULE);
 				ret = (facetedProject.hasProjectFacet(appFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.J2EE_1_4_ID)))
 						|| facetedProject.hasProjectFacet(appFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.J2EE_1_3_ID)))
 						|| facetedProject.hasProjectFacet(appFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionConstants.J2EE_1_2_ID)))
 						);
 			} else if(isJCAProject(facetedProject)){
 				IProjectFacet jcaFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_CONNECTOR_MODULE);
 				ret = (facetedProject.hasProjectFacet(jcaFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToConnectorVersionID(J2EEVersionConstants.J2EE_1_4_ID))))
 						|| facetedProject.hasProjectFacet(jcaFacet.getVersion(J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToConnectorVersionID(J2EEVersionConstants.J2EE_1_3_ID))))
 						);
 			} else if(isUtilityProject(facetedProject)){
 				// not sure if there is a better way
 				ret = true;
 			} 
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ret;
 	}
 }
