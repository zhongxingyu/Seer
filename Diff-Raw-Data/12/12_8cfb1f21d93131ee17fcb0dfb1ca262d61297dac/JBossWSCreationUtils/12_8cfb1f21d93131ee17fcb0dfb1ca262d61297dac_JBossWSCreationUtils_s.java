 /**
  * JBoss, a Division of Red Hat
  * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
 import java.util.Arrays;
 import java.util.Locale;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IParent;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.ws.internal.common.J2EEUtils;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerCore;
 import org.jboss.tools.ws.core.JbossWSCorePlugin;
 import org.jboss.tools.ws.core.classpath.JbossWSRuntime;
 import org.jboss.tools.ws.core.classpath.JbossWSRuntimeManager;
 import org.jboss.tools.ws.core.facet.delegate.IJBossWSFacetDataModelProperties;
 import org.jboss.tools.ws.core.messages.JbossWSCoreMessages;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 
 public class JBossWSCreationUtils {
 	
     static final String javaKeyWords[] =
     {
             "abstract", "assert", "boolean", "break", "byte", "case",
             "catch", "char", "class", "const", "continue",
             "default", "do", "double", "else", "extends",
             "false", "final", "finally", "float", "for",
             "goto", "if", "implements", "import", "instanceof",
             "int", "interface", "long", "native", "new",
             "null", "package", "private", "protected", "public",
             "return", "short", "static", "strictfp", "super",
             "switch", "synchronized", "this", "throw", "throws",
             "transient", "true", "try", "void", "volatile",
             "while"
     };
     
     public static boolean isJavaKeyword(String keyword) {
         if (hasUpperCase(keyword)) {
             return false;
         }
         return (Arrays.binarySearch(javaKeyWords, keyword, Collator.getInstance(Locale.ENGLISH)) >= 0);
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
     
 	public static IPath getWorkspace(){
 		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
 	}
 	
 	public static IProject getProjectByName(String project){
 		String projectString = replaceEscapecharactors(project);
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(
 				getProjectNameFromFramewokNameString(projectString));
 	}
 	
 	public static IPath getProjectRoot(String project){
 		String projectString = replaceEscapecharactors(project);
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(
 				getProjectNameFromFramewokNameString(projectString)).getLocation();
 	}
 
 	public static String  pathToWebProjectContainer(String project) {
 		IPath projectRoot = getProjectRoot(project);
 		IPath currentDynamicWebProjectDir = J2EEUtils.getWebContentPath(
 				getProjectByName(project));
 		IPath currentDynamicWebProjectDirWithoutProjectRoot = J2EEUtils.getWebContentPath(
 				getProjectByName(project)).removeFirstSegments(1).makeAbsolute();
 		if(projectRoot.toOSString().contains(getWorkspace().toOSString())){
 			return getWorkspace()
 						.append(currentDynamicWebProjectDir).toOSString();
 		}else{
 			return projectRoot
 						.append(currentDynamicWebProjectDirWithoutProjectRoot).toOSString();
 		}
 		
 	}
 	
 	public static String  pathToWebProjectContainerWEBINF(String project) {
 		IPath projectRoot = getProjectRoot(project);
 		IPath webContainerWEBINFDir = J2EEUtils.getWebInfPath(
 				getProjectByName(project));
 		IPath webContainerWEBINFDirWithoutProjectRoot = J2EEUtils.getWebInfPath(
 				getProjectByName(project)).removeFirstSegments(1).makeAbsolute();
 		if(projectRoot.toOSString().contains(getWorkspace().toOSString())){
 			return getWorkspace()
 						.append(webContainerWEBINFDir).toOSString();
 		}else{
 			return projectRoot
 						.append(webContainerWEBINFDirWithoutProjectRoot).toOSString();
 		}
 	}
 	
 	
 	private static String replaceEscapecharactors(String vulnarableString){
 		if (vulnarableString.indexOf("/")!=-1){
 			vulnarableString = vulnarableString.replace('/', File.separator.charAt(0));
 		}
 		return vulnarableString;
 	}
 	
 	
 	private static String getProjectNameFromFramewokNameString(String frameworkProjectString){
 		if (frameworkProjectString.indexOf(getSplitCharactor())== -1){
 			return frameworkProjectString;
 		}else{
 			return frameworkProjectString.split(getSplitCharactors())[1];
 		}
 	}
 	
 	
 	private static String getSplitCharactor(){
 		//Windows check (because from inside wtp in return I received a hard coded path)
 		if (File.separatorChar == '\\'){
 			return "\\" ;
 		}else{
 			return File.separator;
 		}
 	}
 	
 	
 	private static String getSplitCharactors(){
 		//Windows check (because from inside wtp in return I received a hard coded path)
 		if (File.separatorChar == '\\'){
 			return "\\" + File.separator;
 		}else{
 			return File.separator;
 		}
 	}
 	
 	 public static String classNameFromQualifiedName(String qualifiedCalssName){
 		 //This was done due to not splitting with . Strange
 		 qualifiedCalssName = qualifiedCalssName.replace('.', ':');
 		 String[] parts = qualifiedCalssName.split(":");
 		 if (parts.length == 0){
 			 return "";
 		 }
 		 return parts[parts.length-1];
 	 }	
 	
 	// JDT utils
 	/**
 	 * get JavaProject object from project name
 	 */
     public static IJavaProject getJavaProjectByName(String projectName) throws JavaModelException {
 
         IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
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
      * @param javaFile the java sour file to look
      * @return ICompilationUnit, JDK compilation unit for this java file.
      */
     public static ICompilationUnit getJavaUnitFromFile(IFile javaFile) {
         try {
             IJavaProject project = getJavaProjectByName(javaFile.getProject().getName());
 
             if (project == null) {
                 return null;
             }
 
             return findUnitByFileName(project, javaFile.getFullPath().toString());
         } catch (Exception e) {
             return null;
         }
     }
     
 	public static boolean validateJBossWSLocation() {
 		String location = JbossWSCorePlugin.getDefault().getPreferenceStore()
 				.getString(JbossWSCoreMessages.WS_Location);
 		if (location == null || location.equals("")) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static String getJbossWSRuntimeLocation(IProject project) throws CoreException{
 			
 		String isServerSupplied = project.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_SERVER_SUPPLIED_RUNTIME);
		if(isServerSupplied != null && 
 				!IJBossWSFacetDataModelProperties.DEFAULT_VALUE_IS_SERVER_SUPPLIED.equals(isServerSupplied)){
			String jbwsRuntimeName = project.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_QNAME_RUNTIME_NAME);
 			JbossWSRuntime jbws = JbossWSRuntimeManager.getInstance().findRuntimeByName(jbwsRuntimeName);
 			if(jbws != null){
 				return jbws.getHomeDir();
 			}else{
 				String jbwsHomeDir = project.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_RNTIME_LOCATION);
 				return jbwsHomeDir;
 			}
 		}
 		//if users select server as its jbossws runtime, then get runtime location from project target runtime
 		else{
 			IFacetedProject facetedPrj = ProjectFacetsManager.create(project);
 			org.eclipse.wst.common.project.facet.core.runtime.IRuntime prjFacetRuntime = facetedPrj.getPrimaryRuntime();
 
			IRuntime serverRuntime = getRuntime(prjFacetRuntime);
			
			if(serverRuntime != null){ 
 				String runtimeTypeName = serverRuntime.getRuntimeType().getName(); 
 				if(runtimeTypeName == null){
 					runtimeTypeName = "";
 				}
 				if(runtimeTypeName.toUpperCase().indexOf("JBOSS") >= 0){
 					return serverRuntime.getLocation().removeLastSegments(1).toOSString();
 				}
 			}
 			
 			//if no target runtime has been specified, get runtime location from default jbossws runtime 
 			if(prjFacetRuntime == null){
 				JbossWSRuntime jbws = JbossWSRuntimeManager.getInstance().getDefaultRuntime();
 				if(jbws != null){
 					return jbws.getHomeDir();
 				}else{
 					throw new CoreException(StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Message_No_Runtime_Specified));
 				}
 				
 			}
 			
 		}
 		return "";
 		
 	}
 	
 	public static IRuntime getRuntime(org.eclipse.wst.common.project.facet.core.runtime.IRuntime runtime) {
 		if (runtime == null)
 			throw new IllegalArgumentException();
 		
 		String id = runtime.getProperty("id");
 		if (id == null)
 			return null;
 		
 		org.eclipse.wst.server.core.IRuntime[] runtimes = ServerCore.getRuntimes();
 		int size = runtimes.length;
 		for (int i = 0; i < size; i++) {
 			if (id.equals(runtimes[i].getId()))
 				return runtimes[i];
 		}
 		
 		return null;
 	}
 	
 }
