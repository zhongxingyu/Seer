 /*******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc. and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Daniel R. Somerfield - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.core.internal.cactus;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;
 /**
  *
  */
 public class CactusModuleArtifactAdapterDelegate extends ModuleArtifactAdapterDelegate {
 	private static final String SERVLET_TEST_CASE_TYPE = "org.apache.cactus.ServletTestCase";
 
 	private static final String SUITE_METHOD = "suite";
 
 	private static final String TEST_CLASS_NAME = "junit.framework.Test";
 
 	public IModuleArtifact getModuleArtifact(Object obj) {
 		String methodName = "";
 		if (obj instanceof IMethod) {
 			IMethod method = (IMethod) obj;
 			methodName = method.getElementName();
 			obj = method.getCompilationUnit();
 		}
 		if (obj instanceof IAdaptable) {
 			IResource resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
 			String testClassName = getClassNameForType(resource, SERVLET_TEST_CASE_TYPE);
 			String projectName = resource.getProject().getName();
 			if (testClassName != null) {
 				return new WebTestableResource(getModule(resource.getProject()), false,
 						projectName, testClassName, methodName);
 			}
 		}
 		return null;
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
 			if (outputPath != null
 					&& "class".equals(path.getFileExtension()) && outputPath.isPrefixOf(path)) { //$NON-NLS-1$
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
 					if (hasSuperclass(types[i], superType) || hasSuiteMethod(types[i]))
 						return types[i].getFullyQualifiedName();
 				}
 			}
 			return null;
 		} catch (Exception e) {
			assert false : "Unexpected exception: " + e;
 			return null;
 		}
 	}
 
 	private static boolean hasSuiteMethod(IType type) throws JavaModelException {
 		IMethod[] methods = type.getMethods();
 		int size = methods.length;
 		for (int i = 0; i < size; i++) {
 			IMethod method = methods[i];
 			if (method.getParameterNames().length == 0 && method.getElementName().equals(SUITE_METHOD)) {
 				String returnType = getFullyQualifiedTypeForMangledType(method.getReturnType(), type);
 				if (TEST_CLASS_NAME.equals(returnType)) {
 					return true;
 				}
 			}
 		}
 		return false;
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
 
 	protected static IModule getModule(IProject project) {
 		IModule deployable = null;
 		Iterator iterator = Arrays.asList(ServerUtil.getModules("jst.web")).iterator();
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
 
 	private static String getFullyQualifiedTypeForMangledType(String type,
 			IType declaringType) throws JavaModelException {
 		type = Signature.toString(type);
 		return getFullyQualifiedTypeForType(type, declaringType);
 	}
 
 	private static String getFullyQualifiedTypeForType(String type, IType declaringType)
 			throws JavaModelException {
 		String[][] resolvedTypes = declaringType.resolveType(type);
 		//TODO: Are there legit cases where this would not be the case? We might need to check
 		//for this and bomb out if the type cannot be unambigiously resolved
		assert resolvedTypes.length == 1 : "The type cannot be unambigiously resolved. Need to handle this case";
 		String[] resolvedType = resolvedTypes[0];
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < resolvedType.length - 1; i++) {
 			buffer.append(resolvedType[i]);
 			buffer.append('.');
 		}
 
 		buffer.append(resolvedType[resolvedType.length - 1]);
 
 		return buffer.toString();
 	}
 }
