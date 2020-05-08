 /**
  * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.tools.ws.creation.core.utils;
 
 import java.io.File;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IParent;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.ServerCore;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntime;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntimeManager;
 import org.jboss.tools.ws.core.facet.delegate.IJBossWSFacetDataModelProperties;
 import org.jboss.tools.ws.core.facet.delegate.JBossWSFacetInstallDataModelProvider;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCorePlugin;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 
 public class JBossWSCreationUtils {
 
 	static final String javaKeyWords[] = { "abstract", "assert", "boolean", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			"break", "byte", "case", "catch", "char", "class", "const", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
 			"continue", "default", "do", "double", "else", "extends", "false", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
 			"final", "finally", "float", "for", "goto", "if", "implements", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
 			"import", "instanceof", "int", "interface", "long", "native", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 			"new", "null", "package", "private", "protected", "public", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 			"return", "short", "static", "strictfp", "super", "switch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 			"synchronized", "this", "throw", "throws", "transient", "true", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 			"try", "void", "volatile", "while" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 	static final String WEBINF = "WEB-INF"; //$NON-NLS-1$
 	private static String JAVA = ".java"; //$NON-NLS-1$
 
 	public static boolean isJavaKeyword(String keyword) {
 		if (hasUpperCase(keyword)) {
 			return false;
 		}
 		return (Arrays.binarySearch(javaKeyWords, keyword,
 				Collator.getInstance(Locale.ENGLISH)) >= 0);
 	}
 
 	private static boolean hasUpperCase(String nodeName) {
 		if (nodeName == null) {
 			return false;
 		}
 		for (int i = 0; i < nodeName.length(); i++) {
 			if (Character.isUpperCase(nodeName.charAt(i))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static IProject getProjectByName(String project) {
 		String projectString = replaceEscapecharactors(project);
 		return ResourcesPlugin
 				.getWorkspace()
 				.getRoot()
 				.getProject(getProjectNameFromFramewokNameString(projectString));
 	}
 
 	public static IPath getProjectRoot(String project) {
 		String projectString = replaceEscapecharactors(project);
 		return ResourcesPlugin
 				.getWorkspace()
 				.getRoot()
 				.getProject(getProjectNameFromFramewokNameString(projectString))
 				.getLocation();
 	}
 
 	private static String replaceEscapecharactors(String vulnarableString) {
 		if (vulnarableString.indexOf("/") != -1) { //$NON-NLS-1$
 			vulnarableString = vulnarableString.replace('/',
 					File.separator.charAt(0));
 		}
 		return vulnarableString;
 	}
 
 	private static String getProjectNameFromFramewokNameString(
 			String frameworkProjectString) {
 		if (frameworkProjectString.indexOf(getSplitCharactor()) == -1) {
 			return frameworkProjectString;
 		} else {
 			return frameworkProjectString.split(getSplitCharactors())[1];
 		}
 	}
 
 	private static String getSplitCharactor() {
 		// Windows check (because from inside wtp in return I received a hard
 		// coded path)
 		if (File.separatorChar == '\\') {
 			return "\\"; //$NON-NLS-1$
 		} else {
 			return File.separator;
 		}
 	}
 
 	private static String getSplitCharactors() {
 		// Windows check (because from inside wtp in return I received a hard
 		// coded path)
 		if (File.separatorChar == '\\') {
 			return "\\" + File.separator; //$NON-NLS-1$
 		} else {
 			return File.separator;
 		}
 	}
 
 	public static String classNameFromQualifiedName(String qualifiedCalssName) {
 		// This was done due to not splitting with . Strange
 		qualifiedCalssName = qualifiedCalssName.replace('.', ':');
 		String[] parts = qualifiedCalssName.split(":"); //$NON-NLS-1$
 		if (parts.length == 0) {
 			return ""; //$NON-NLS-1$
 		}
 		return parts[parts.length - 1];
 	}
 
 	// JDT utils
 	/**
 	 * get JavaProject object from project name
 	 */
 	public static IJavaProject getJavaProjectByName(String projectName)
 			throws JavaModelException {
 
 		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace()
 				.getRoot());
 		model.open(null);
 
 		IJavaProject[] projects = model.getJavaProjects();
 
 		for (IJavaProject proj : projects) {
 			if (proj.getProject().getName().equals(projectName)) {
 				return proj;
 			}
 		}
 
 		return null;
 	}
 
 	public static ICompilationUnit findUnitByFileName(IJavaElement javaElem,
 			String filePath) throws Exception {
 		ICompilationUnit unit = null;
 
 		if (!javaElem.getOpenable().isOpen()) {
 			javaElem.getOpenable().open(null);
 		}
 
 		IJavaElement[] elems = null;
 
 		if (javaElem instanceof IParent) {
 			IParent parent = (IParent) javaElem;
 			elems = parent.getChildren();
 		}
 
 		if (elems == null) {
 			return null;
 		}
 
 		for (IJavaElement elem : elems) {
 			if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
 				IPackageFragmentRoot root = (IPackageFragmentRoot) elem;
 
 				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
 					unit = findUnitByFileName(elem, filePath);
 
 					if (unit != null) {
 						return unit;
 					}
 				}
 			} else if ((elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
 					|| (elem.getElementType() == IJavaElement.JAVA_PROJECT)) {
 				unit = findUnitByFileName(elem, filePath);
 
 				if (unit != null) {
 					return unit;
 				}
 			} else if (elem.getElementType() == IJavaElement.COMPILATION_UNIT) {
 				ICompilationUnit compUnit = (ICompilationUnit) elem;
 
 				if (compUnit.getPath().toString().equals(filePath)) {
 					compUnit.open(null);
 
 					return compUnit;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * get Java compilation unit by file path
 	 * 
 	 * @param javaFile
 	 *            the java sour file to look
 	 * @return ICompilationUnit, JDK compilation unit for this java file.
 	 */
 	public static ICompilationUnit getJavaUnitFromFile(IFile javaFile) {
 		try {
 			IJavaProject project = getJavaProjectByName(javaFile.getProject()
 					.getName());
 
 			if (project == null) {
 				return null;
 			}
 
 			return findUnitByFileName(project, javaFile.getFullPath()
 					.toString());
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static String getJBossWSRuntimeLocation(IProject project)
 			throws CoreException {
 
 		String isServerSupplied = project
 				.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_SERVER_SUPPLIED_RUNTIME);
 		String jbwsRuntimeName = project
 				.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_QNAME_RUNTIME_NAME);
 
 		if (jbwsRuntimeName != null
 				&& !"".equals(jbwsRuntimeName) //$NON-NLS-1$
 				&& !IJBossWSFacetDataModelProperties.DEFAULT_VALUE_IS_SERVER_SUPPLIED
 						.equals(isServerSupplied)) {
 			JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance()
 					.findRuntimeByName(jbwsRuntimeName);
 			if (jbws != null) {
 				return jbws.getHomeDir();
 			} else {
 				String jbwsHomeDir = project
 						.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_RNTIME_LOCATION);
 				if (new File(jbwsHomeDir).exists()) {
 					return jbwsHomeDir;
 				}
 			}
 		}
 		// if users select server as its jbossws runtime, then get runtime
 		// location from project target runtime
 
 		IFacetedProject facetedPrj = ProjectFacetsManager.create(project);
 		org.eclipse.wst.common.project.facet.core.runtime.IRuntime prjFacetRuntime = facetedPrj
 				.getPrimaryRuntime();
 
 		if (prjFacetRuntime != null) {
 			IRuntime serverRuntime = getRuntime(prjFacetRuntime);
 			String runtimeTypeName = serverRuntime.getRuntimeType().getName();
 			if (runtimeTypeName == null) {
 				runtimeTypeName = ""; //$NON-NLS-1$
 			}
 			if (runtimeTypeName.toUpperCase().indexOf("JBOSS") >= 0) { //$NON-NLS-1$
 				String runtimeLocation = serverRuntime.getLocation()
 						.toOSString();
 				if (runtimeLocation.endsWith("bin")) { //$NON-NLS-1$
 					return serverRuntime.getLocation().removeLastSegments(1)
 							.toOSString();
 				} else {
 					return runtimeLocation;
 				}
 			}
 		}
 
 		// if no target runtime has been specified, get runtime location from
 		// default jbossws runtime configured at Web Service preference page
 		if (prjFacetRuntime == null) {
 			JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance()
 					.getDefaultRuntime();
 			if (jbws != null) {
 				return jbws.getHomeDir();
 			} else {
 				throw new CoreException(
 						StatusUtils
 								.errorStatus(JBossWSCreationCoreMessages.Error_Message_No_Runtime_Specified));
 			}
 
 		}
 
 		return ""; //$NON-NLS-1$
 
 	}
 
 	public static boolean supportSOAP12(String projectName) {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(projectName);
 			if (project == null) {
 				return false;
 			}
 
 			IFacetedProject facetedPrj = ProjectFacetsManager.create(project);
 			IProjectFacet jbossWSFacet = ProjectFacetsManager
 					.getProjectFacet(JBossWSFacetInstallDataModelProvider.JBOSS_WS_FACET_ID);
 			IProjectFacetVersion fpVersion = facetedPrj
 					.getProjectFacetVersion(jbossWSFacet);
 			if (fpVersion != null
 					&& fpVersion.getVersionString().compareTo("3.0") >= 0) { //$NON-NLS-1$
 				return true;
 			}
 
 			// if the project doesn't get JBossWS facet installed, check its
 			// primary target runtime
 			// if the jboss runtime version is 5.0 or higher, return true
 			org.eclipse.wst.common.project.facet.core.runtime.IRuntime targetRuntime = facetedPrj
 					.getPrimaryRuntime();
 			if (targetRuntime != null) {
 				IRuntime runtime = getRuntime(targetRuntime);
 				IRuntimeType rt = runtime.getRuntimeType();
 				if (rt.getName().toUpperCase().indexOf("JBOSS") >= 0) { //$NON-NLS-1$
 					String runtimeVersion = rt.getVersion();
 					if (runtimeVersion != null
 							&& runtimeVersion.compareTo("5.0") >= 0) { //$NON-NLS-1$
 						return true;
 					}
 				}
 
 			}
 		} catch (CoreException e) {
 			// ignore
 			// e.printStackTrace();
 		}
 
 		// check the version of default jbossws runtime configured at the Web
 		// Service preference page
 		JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance()
 				.getDefaultRuntime();
 		if (jbws != null && "3.0".compareTo(jbws.getVersion()) <= 0) { //$NON-NLS-1$
 			return true;
 		}
 
 		return false;
 	}
 
 	public static IRuntime getRuntime(
 			org.eclipse.wst.common.project.facet.core.runtime.IRuntime runtime) {
 		if (runtime == null)
 			throw new IllegalArgumentException();
 
 		String id = runtime.getProperty("id"); //$NON-NLS-1$
 		if (id == null)
 			return null;
 
 		org.eclipse.wst.server.core.IRuntime[] runtimes = ServerCore
 				.getRuntimes();
 		int size = runtimes.length;
 		for (int i = 0; i < size; i++) {
 			if (id.equals(runtimes[i].getId()))
 				return runtimes[i];
 		}
 
 		return null;
 	}
 
 	public static String getJavaProjectSrcLocation(IProject project)
 			throws JavaModelException {
 		IResource[] rs = getJavaSourceRoots(project);
 		String src = ""; //$NON-NLS-1$
 		if (rs == null || rs.length == 0)
 			return src;
 		for (int i = 0; i < rs.length; i++) {
 			IPath p = rs[i].getLocation();
 			if (p != null) {
 				src = p.toOSString();
 				return src;
 			}
 		}
 		return src;
 	}
 
 	public static IResource[] getJavaSourceRoots(IProject project)
 			throws JavaModelException {
 		IJavaProject javaProject = JavaCore.create(project);
 		if (javaProject == null)
 			return null;
 		List<IResource> resources = new ArrayList<IResource>();
 		IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
 		for (int i = 0; i < es.length; i++) {
 			if (es[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 				IResource findMember = ResourcesPlugin.getWorkspace().getRoot()
 						.findMember(es[i].getPath());
 				if (findMember != null && findMember.exists()) {
					resources.add(findMember);
 				}
 			}
 		}
 		return resources.toArray(new IResource[resources.size()]);
 	}
 
 	public static IPath getWebContentRootPath(IProject project) {
 		if (project == null)
 			return null;
 
 		if (!ModuleCoreNature.isFlexibleProject(project))
 			return null;
 
 		IPath path = null;
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if (component != null && component.exists()) {
 			path = component.getRootFolder().getWorkspaceRelativePath();
 		}
 		return path;
 	}
 
 	public static String composeSrcPackageClassPath(IProject project,
 			String packageName, String className) {
 		String classFilePath = null;
 		try {
 			String srcPath = JBossWSCreationUtils
 					.getJavaProjectSrcLocation(project);
 			if (srcPath != null && srcPath.trim().length() > 0) {
 				String pathSeparator = "" + File.separatorChar; //$NON-NLS-1$
 				String packageToFolderPath = packageName.replace(
 						".", pathSeparator); //$NON-NLS-1$
 				classFilePath = srcPath + pathSeparator + packageToFolderPath
 						+ pathSeparator + className + JAVA;
 				return classFilePath;
 			}
 		} catch (JavaModelException e) {
 			return null;
 		}
 		return null;
 
 	}
 
 	public static File findFileByPath(String path) {
 		File file = new File(path);
 		if (file.exists())
 			return file;
 		return null;
 	}
 
 	public static File findFileByPath(String name, String path) {
 		File ret = null;
 		File folder = new File(path);
 		if (folder.isDirectory()) {
 			File[] files = folder.listFiles();
 			for (File file : files) {
 				ret = findFileByPath(name, file.getAbsolutePath());
 				if (ret != null) {
 					break;
 				}
 			}
 		} else {
 			if (name.equals(folder.getName())) {
 				ret = folder;
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * find compilationunit by annotation
 	 * 
 	 * @param project
 	 * @param annotation
 	 * @return
 	 */
 	public static List<ICompilationUnit> findJavaUnitsByAnnotation(
 			IJavaProject project, String annotation, String packageName) {
 		List<ICompilationUnit> units = new LinkedList<ICompilationUnit>();
 		try {
 			IPath path = addPackagetoPath(project, packageName);
 			if (path == null) {
 				IResource[] resources = JBossWSCreationUtils
 						.getJavaSourceRoots(project.getProject());
 				if (resources != null && resources.length > 0) {
 					IJavaElement[] elements = project.getPackageFragmentRoot(
 							resources[0]).getChildren();
 					for (IJavaElement element : elements) {
 						if (IJavaElement.PACKAGE_FRAGMENT == element
 								.getElementType()) {
 							findInPackageFragment(units, element.getPath(),
 									project, annotation);
 						}
 					}
 				}
 			} else {
 				findInPackageFragment(units, path, project, annotation);
 			}
 		} catch (JavaModelException e) {
 			JBossWSCreationCorePlugin.getDefault().logError(e);
 		}
 		return units;
 	}
 
 	private static void findInPackageFragment(List<ICompilationUnit> units,
 			IPath path, IJavaProject project, String annotation) {
 		ICompilationUnit[] javaFiles = null;
 		try {
 			if (project.findPackageFragment(path) != null) {
 				javaFiles = project.findPackageFragment(path)
 						.getCompilationUnits();
 			}
 			if (javaFiles != null && javaFiles.length > 0) {
 				for (ICompilationUnit unit : javaFiles) {
 					if (unit.getTypes().length > 0) {
 						IType type = unit.getTypes()[0];
 						if (type.getAnnotation(annotation).exists()) {
 							File file = new File(unit.getResource().getLocation().toOSString());
                             if(file.lastModified() > JBossWSCreationCorePlugin.getDefault().getGenerateTime()){
                             	units.add(unit);
                             }
 						}
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			JBossWSCreationCorePlugin.getDefault().logError(e);
 		}
 	}
 
 	/**
 	 * new a path by adding a java package
 	 * 
 	 * @param project
 	 * @return
 	 * @throws JavaModelException
 	 */
 	public static IPath addPackagetoPath(IJavaProject project,
 			String packageName) throws JavaModelException {
 		String PACAKAGESPLIT = "\\."; //$NON-NLS-1$
 		if ("".equals(packageName)) { //$NON-NLS-1$
 			return null;
 		}
 		IPath path = new Path(
 				JBossWSCreationUtils.getJavaProjectSrcLocation(project
 						.getProject()));
 		String[] names = packageName.split(PACAKAGESPLIT);
 		path = project.getPath().append(
 				path.makeRelativeTo(project.getProject().getLocation()));
 
 		if (names != null && names.length > 0) {
 			for (String name : names) {
 				path = path.append(name);
 			}
 		}
 		return path;
 	}
 
 }
