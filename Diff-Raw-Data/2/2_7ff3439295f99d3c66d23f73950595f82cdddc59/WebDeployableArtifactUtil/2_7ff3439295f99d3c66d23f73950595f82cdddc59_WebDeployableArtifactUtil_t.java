 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.deployables;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.internal.web.jfaces.extension.FileURL;
 import org.eclipse.jst.j2ee.internal.web.jfaces.extension.FileURLExtensionReader;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.j2ee.webapplication.JSPType;
 import org.eclipse.jst.j2ee.webapplication.Servlet;
 import org.eclipse.jst.j2ee.webapplication.ServletMapping;
 import org.eclipse.jst.j2ee.webapplication.ServletType;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebType;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.util.WebResource;
 
 /**
  * @version 1.0
  * @author
  */
 public class WebDeployableArtifactUtil {
 	private final static String[] extensionsToExclude = new String[]{"sql", "xmi"}; //$NON-NLS-1$ //$NON-NLS-2$
 	private final static String GENERIC_SERVLET_CLASS_TYPE = "javax.servlet.GenericServlet";
 	private final static String CACTUS_SERVLET_CLASS_TYPE = "org.apache.cactus.server.ServletTestRedirector";
 
 	public WebDeployableArtifactUtil() {
 		super();
 	}
 
 	public static IModuleArtifact getModuleObject(Object obj) {
 		IResource resource = null;
 		if (obj instanceof IResource)
 			resource = (IResource) obj;
		if (obj instanceof IModuleArtifact)
			resource = ((IModuleArtifact) obj).getModule().getProject();
 		else if (obj instanceof IAdaptable)
 			resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
 		else if (obj instanceof EObject) {
 			resource = ProjectUtilities.getProject((EObject) obj);
 			if (obj instanceof Servlet) {
 				Servlet servlet = ((Servlet) obj);
 				Resource servResource = servlet.eResource();
 				IVirtualResource[] resources = null;
 				try {
 					IResource eclipeServResoruce = WorkbenchResourceHelper.getFile(servResource);
 					resources = ComponentCore.createResources(eclipeServResoruce);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				IVirtualComponent component = null;
 				if (resources[0] != null)
 					component = resources[0].getComponent();
 				String mapping = null;
 				java.util.List mappings = ((Servlet) obj).getMappings();
 				if (mappings != null && !mappings.isEmpty()) {
 					ServletMapping map = (ServletMapping) mappings.get(0);
 					mapping = map.getUrlPattern();
 				}
 				if (mapping != null) {
 					return new WebResource(getModule(resource.getProject(), component), new Path(mapping));
 				}
 				WebType webType = ((Servlet) obj).getWebType();
 				if (webType.isJspType()) {
 					resource = ((IProject) resource).getFile(((JSPType) webType).getJspFile()); //$NON-NLS-1$
 				} else if (webType.isServletType()) {
 					return new WebResource(getModule(resource.getProject(), component), new Path("servlet/" + ((ServletType) webType).getClassName())); //$NON-NLS-1$
 				}
 			}
 		}
 		if (resource == null)
 			return null;
 
 		if (resource instanceof IProject) {
 			IProject project = (IProject) resource;
 			if (hasInterestedComponents(project))
 				return new WebResource(getModule(project, null), project.getProjectRelativePath());
 		}
 		
 		if (!hasInterestedComponents(resource.getProject()))
             return null;
 		if (isCactusJunitTest(resource))
 			return null;
 
 		IPath rootPath = resource.getProjectRelativePath();
 		IPath resourcePath = resource.getFullPath();
 		IVirtualResource[] resources = ComponentCore.createResources(resource);
 		IVirtualComponent component = null;
 		if (resources[0] != null || resources.length <= 0)
 			component = resources[0].getComponent();
 		String className = getServletClassName(resource);
 		if (className != null && component != null) {
 			String mapping = getServletMapping(resource, true, className, component.getName());
 			if (mapping != null) {
 				return new WebResource(getModule(resource.getProject(), component), new Path(mapping));
 			}
 			// if there is no servlet mapping, provide direct access to the servlet
 			// through the fully qualified class name
 			return new WebResource(getModule(resource.getProject(), component), new Path("servlet/" + className)); //$NON-NLS-1$
 
 		}
 		resourcePath = trim(resourcePath, component.getName());
 		resourcePath = trim(resourcePath, WebArtifactEdit.WEB_CONTENT);
 		// resourcePath = trim(resourcePath,WebArtifactEdit.META_INF);
 		// resourcePath = trim(resourcePath,WebArtifactEdit.WEB_INF);
 
 		// Extension read to get the correct URL for Java Server Faces file if
 		// the jsp is of type jsfaces.
 		FileURL jspURL = FileURLExtensionReader.getInstance().getFilesURL();
 		if (jspURL != null) {
 			IPath correctJSPPath = jspURL.getFileURL(resource, resourcePath);
 			if (correctJSPPath != null && correctJSPPath.toString().length() > 0)
 				return new WebResource(getModule(resource.getProject(), component), correctJSPPath);
 		}
 		// return Web resource type
 		return new WebResource(getModule(resource.getProject(), component), resourcePath);
 	}
 
 	private static IPath trim(IPath resourcePath, String stripValue) {
 		int x = -1;
 		String[] segements = resourcePath.segments();
 		for (int i = 0; i < segements.length; i++) {
 			if (segements[i].equals(stripValue)) {
 				x = ++i;
 				break;
 			}
 		}
 		if (x > -1)
 			return resourcePath.removeFirstSegments(x);
 		return resourcePath;
 	}
 
 	private static boolean shouldExclude(IResource resource) {
 		String fileExt = resource.getFileExtension();
 
 		// Exclude files of certain extensions
 		for (int i = 0; i < extensionsToExclude.length; i++) {
 			String extension = extensionsToExclude[i];
 			if (extension.equalsIgnoreCase(fileExt))
 				return true;
 		}
 		return false;
 	}
 
 	protected static IModule getModule(IProject project, IVirtualComponent component) {
 		IModule deployable = null;
 		Iterator iterator = Arrays.asList(ServerUtil.getModules("j2ee.web")).iterator();
 		String componentName = null;
 		if (component != null)
 			componentName = component.getName();
 		else
 			return getModuleProject(project, iterator);
 		while (iterator.hasNext()) {
 			Object next = iterator.next();
 			if (next instanceof IModule) {
 				deployable = (IModule) next;
 				if (deployable.getName().equals(componentName)) {
 					return deployable;
 				}
 			}
 		}
 		return null;
 	}
 
 	protected static IModule getModuleProject(IProject project, Iterator iterator) {
 		IModule deployable = null;
 		while (iterator.hasNext()) {
 			Object next = iterator.next();
 			if (next instanceof IModule) {
 				deployable = (IModule) next;
 				if (deployable.getProject().equals(project))
 					return deployable;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * Very temporary api - TODO - rip this out by 1.0
 	 */
 	private static boolean isCactusJunitTest(IResource resource) {
 		return getClassNameForType(resource, CACTUS_SERVLET_CLASS_TYPE) != null;
 	}
 
 
 
 	private static IType[] getTypes(IJavaElement element) {
 		try {
 			if (element.getElementType() != IJavaElement.COMPILATION_UNIT)
 				return null;
 
 			return ((ICompilationUnit) element).getAllTypes();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static String getServletClassName(IResource resource) {
 		return getClassNameForType(resource, GENERIC_SERVLET_CLASS_TYPE);
 	}
 
 	public static String getClassNameForType(IResource resource, String superType) {
 		if (resource == null)
 			return null;
 
 		try {
 			IProject project = resource.getProject();
 			IPath path = resource.getFullPath();
 			if (!project.hasNature(JavaCore.NATURE_ID) || path == null)
 				return null;
 
 			IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
 			if (!javaProject.isOpen())
 				javaProject.open(new NullProgressMonitor());
 
 			// output location may not be on classpath
 			IPath outputPath = javaProject.getOutputLocation();
 			if (outputPath != null && "class".equals(path.getFileExtension()) && outputPath.isPrefixOf(path)) { //$NON-NLS-1$
 				int count = outputPath.segmentCount();
 				path = path.removeFirstSegments(count);
 			}
 
 			// remove initial part of classpath
 			IClasspathEntry[] classPathEntry = javaProject.getResolvedClasspath(true);
 			if (classPathEntry != null) {
 				int size = classPathEntry.length;
 				for (int i = 0; i < size; i++) {
 					IPath classPath = classPathEntry[i].getPath();
 					if (classPath.isPrefixOf(path)) {
 						int count = classPath.segmentCount();
 						path = path.removeFirstSegments(count);
 						i += size;
 					}
 				}
 			}
 
 			// get java element
 			IJavaElement javaElement = javaProject.findElement(path);
 
 			IType[] types = getTypes(javaElement);
 			if (types != null) {
 				int size2 = types.length;
 				for (int i = 0; i < size2; i++) {
 					if (hasSuperclass(types[i], superType))
 						return types[i].getFullyQualifiedName();
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static boolean hasSuperclass(IType type, String superClassName) {
 		try {
 			ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
 			IType[] superClasses = hierarchy.getAllSuperclasses(type);
 
 			int size = superClasses.length;
 			for (int i = 0; i < size; i++) {
 				if (superClassName.equals(superClasses[i].getFullyQualifiedName())) //$NON-NLS-1$
 					return true;
 			}
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 
 	private static boolean isServlet(IType type) {
 		try {
 			ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
 			IType[] superClasses = hierarchy.getAllSuperclasses(type);
 
 			int size = superClasses.length;
 			for (int i = 0; i < size; i++) {
 				if ("javax.servlet.GenericServlet".equals(superClasses[i].getFullyQualifiedName())) //$NON-NLS-1$
 					return true;
 			}
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 
 	public static String getServletMapping(IResource resource, boolean isServlet, String typeName, String componentName) {
 		if (typeName == null || typeName.equals("")) //$NON-NLS-1$
 			return null;
 
 		IProject project = resource.getProject();
 		WebArtifactEdit edit = null;
 		WebApp webApp = null;
 		try {
 			ComponentHandle handle = ComponentHandle.create(project,componentName);
 			edit = WebArtifactEdit.getWebArtifactEditForRead(handle);
 			edit.getDeploymentDescriptorRoot();
 			webApp = edit.getWebApp();
 		} finally {
 			if (edit != null) {
 				edit.dispose();
 			}
 		}
 		Object key = new Object();
 		try {
 			if (webApp == null)
 				return null;
 			Iterator iterator = webApp.getServlets().iterator();
 			while (iterator.hasNext()) {
 				Servlet servlet = (Servlet) iterator.next();
 				boolean valid = false;
 
 				WebType webType = servlet.getWebType();
 				if (webType.isServletType() && isServlet) {
 					ServletType type = (ServletType) webType;
 					if (typeName.equals(type.getClassName()))
 						valid = true;
 				} else if (webType.isJspType() && !isServlet) {
 					JSPType type = (JSPType) webType;
 					if (typeName.equals(type.getJspFile()))
 						valid = true;
 				}
 				if (valid) {
 					java.util.List mappings = servlet.getMappings();
 					if (mappings != null && !mappings.isEmpty()) {
 						ServletMapping map = (ServletMapping) mappings.get(0);
 						return map.getUrlPattern();
 					}
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	protected static boolean hasInterestedComponents(IProject project) {
 		StructureEdit edit = null;
 		try {
 			edit = StructureEdit.getStructureEditForWrite(project);
 			WorkbenchComponent[] components = edit.findComponentsByType("jst.web");
 			// WorkbenchComponent[] earComponents = edit.findComponentsByType("jst.ear");
 			if (components == null || components.length == 0) // || earComponents != null ||
 				// earComponents.length > 0
 				return false;
 			else
 				return true;
 		} catch (Exception e) {
 			System.out.println(e);
 		} finally {
 			edit.dispose();
 		}
 		return false;
 	}
 
 }
