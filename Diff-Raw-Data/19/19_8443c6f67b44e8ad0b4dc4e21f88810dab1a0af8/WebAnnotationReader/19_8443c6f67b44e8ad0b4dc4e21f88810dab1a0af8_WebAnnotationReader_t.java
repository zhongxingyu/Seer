 /***********************************************************************
  * Copyright (c) 2008 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.model.internal;
 
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
 import org.eclipse.jst.javaee.core.EjbLocalRef;
 import org.eclipse.jst.javaee.core.JavaEEObject;
 import org.eclipse.jst.javaee.core.Listener;
 import org.eclipse.jst.javaee.core.ResourceRef;
 import org.eclipse.jst.javaee.core.SecurityRole;
 import org.eclipse.jst.javaee.core.SecurityRoleRef;
 import org.eclipse.jst.javaee.ejb.SecurityIdentityType;
 import org.eclipse.jst.javaee.web.Filter;
 import org.eclipse.jst.javaee.web.FilterMapping;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.ServletMapping;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jee.model.internal.common.AbstractAnnotationModelProvider;
 import org.eclipse.jst.jee.model.internal.common.ManyToOneRelation;
 import org.eclipse.jst.jee.model.internal.common.Result;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 
 /**
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public class WebAnnotationReader extends AbstractAnnotationModelProvider<WebApp> {
 	
 	private static final String WEB_SERVLET = "WebServlet"; //$NON-NLS-1$
 	private static final String WEB_SERVLET_FQ = "javax.servlet.annotation.WebServlet"; //$NON-NLS-1$
 	
	private static final String WEB_LISTENER = "WebListener"; //$NON-NLS-1$
	private static final String WEB_LISTENER_FQ = "javax.servlet.annotation.WebListener"; //$NON-NLS-1$
 
 	private static final String WEB_FILTER = "WebFilter"; //$NON-NLS-1$
 	private static final String WEB_FILTER_FQ = "javax.servlet.annotation.WebFilter"; //$NON-NLS-1$
 
 	private ManyToOneRelation<JavaEEObject, ICompilationUnit> modelToUnit;
 
 	private ManyToOneRelation<JavaEEObject, ICompilationUnit> modelToInterfaceUnit;
 
 	private WebAnnotationFactory annotationFactory;
 
 	private WebApp ddApp;
 
 	public WebAnnotationReader(IFacetedProject facetedProject, WebApp ddApp) {
 		super(facetedProject);
 		if (ddApp == null)
 			throw new IllegalArgumentException("The deployment descriptor model can not be null!"); //$NON-NLS-1$
 		this.ddApp = ddApp;
 	}
 
 	@Override
 	protected void preLoad() {
 		modelObject = WebFactory.eINSTANCE.createWebApp();
 		annotationFactory = WebAnnotationFactory.createFactory();
 	}
 
 	@Override
 	protected void loadModel() throws CoreException {
 		IJavaProject javaProject = JavaCore.create(facetedProject.getProject());
 		final Collection<ICompilationUnit> javaUnits = new HashSet<ICompilationUnit>();
 		for (final IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
 			visitJavaFiles(javaUnits, root);
 		}
 		annotationFactory = WebAnnotationFactory.createFactory();
 
 		modelToInterfaceUnit = new ManyToOneRelation<JavaEEObject, ICompilationUnit>();
 		modelToUnit = new ManyToOneRelation<JavaEEObject, ICompilationUnit>();
 		for (ICompilationUnit unit : javaUnits) {
 			Result result = analyzeCompilationUnit(unit);
 			if (result == null)
 				continue;
 			processResult(unit, result);
 		}
 	}
 
 	/**
 	 * Analyze this unit for a web artifact. If the file is not a valid java
 	 * compilation unit or it does not contain a class the method returns
 	 * <code>null</code>
 	 * 
 	 * Only the primary type of the compilation unit is processed.
 	 * 
 	 * @param unit
 	 *            the unit to be processed.
 	 * @return result from processing the file
 	 * @throws JavaModelException
 	 */
 	private Result analyzeCompilationUnit(ICompilationUnit unit) throws JavaModelException {
 		IType rootType = unit.findPrimaryType();
 		/*
 		 * If the compilation unit is not valid and can not be compiled the
 		 * rootType may be null. This can happen if a class is define as follows
 		 * <code> @Stateless public SomeClass implements SomeInterface{}</code>.
 		 * Here the "class" word is missed and the type is not valid.
 		 */
 		if (rootType == null || !rootType.isClass())
 			return null;
 		for (Iterator iter = ddApp.getServlets().iterator(); iter.hasNext();) {
 			Servlet servlet = (Servlet) iter.next();
 			if (rootType.getFullyQualifiedName().equals(servlet.getServletClass()))
 				return annotationFactory.createServlet(rootType, servlet.getServletName());
 		}
 		for (Iterator iter = ddApp.getListeners().iterator(); iter.hasNext();) {
 			Listener listener = (Listener) iter.next();
 			if (rootType.getFullyQualifiedName().equals(listener.getListenerClass()))
 				return annotationFactory.createListener(rootType);
 		}
 		for (Iterator iter = ddApp.getFilters().iterator(); iter.hasNext();) {
 			Filter filter = (Filter) iter.next();
 			if (rootType.getFullyQualifiedName().equals(filter.getFilterClass())) {
 				return annotationFactory.createFilter(rootType, filter.getFilterName());
 			}
 		}
 		if(Float.parseFloat(facetedProject.getProjectFacetVersion(WebFacetUtils.WEB_FACET).getVersionString()) > 2.5){
 			return createJavaeeObject(rootType);	
 		}
 		return null;
 	}
 	
 	public Result createJavaeeObject(IType type) throws JavaModelException {
 		Result result = null;
 		for (IAnnotation annotation : type.getAnnotations()) {
 			String annotationName = annotation.getElementName();
 			if (WEB_SERVLET.equals(annotationName) || WEB_SERVLET_FQ.equals(annotationName)) {
 				result = annotationFactory.createServlet(type, null);
 				break;
 			} else if (WEB_FILTER.equals(annotationName) || WEB_FILTER_FQ.equals(annotationName)) {
 				result = annotationFactory.createFilter(type, null);
 				break;
 			} else if (WEB_LISTENER.equals(annotationName) || WEB_LISTENER_FQ.equals(annotationName)) {
 				result = annotationFactory.createListener(type);
 				break;
 			} 
 		}
 		return result;
 	}
 
 	/**
 	 * Process the result from parsing the unit. Depending on the result this
 	 * might include adding a session bean, message driven bean, securityRole
 	 * etc.
 	 * 
 	 * @param unit
 	 * @param result
 	 * @throws JavaModelException
 	 */
 	private void processResult(ICompilationUnit unit, Result result) throws JavaModelException {
 		JavaEEObject mainObject = result.getMainObject();
 		if (Servlet.class.isInstance(mainObject))
 			servletFound(unit, (Servlet) result.getMainObject(), result.getDependedTypes());
 		if (Listener.class.isInstance(mainObject))
 			listenerFound(unit, (Listener) result.getMainObject(), result.getDependedTypes());
 		if (Filter.class.isInstance(mainObject))
 			filterFound(unit, (Filter) result.getMainObject(), result.getDependedTypes());
 		for (JavaEEObject additional : result.getAdditional()) {
 			if (EjbLocalRef.class.isInstance(additional)) {
 				ejbLocalRefFound(unit, (EjbLocalRef) additional, result.getDependedTypes());
 			} else if (ResourceRef.class.isInstance(additional)) {
 				resourceRefFound(unit, (ResourceRef) additional, result.getDependedTypes());
 			} else if (SecurityRole.class.isInstance(additional)) {
 				securityRoleFound(result.getMainObject(), (SecurityRole) additional);
 			} else if (SecurityIdentityType.class.isInstance(additional)) {
 				securityIdentityTypeFound(unit, (SecurityIdentityType) additional);
 			} else if (ServletMapping.class.isInstance(additional)) {
 				servletMappingFound(unit, (ServletMapping)additional, result.getDependedTypes());
 			} else if (FilterMapping.class.isInstance(additional)) {
 				filterMappingFound(unit, (FilterMapping)additional, result.getDependedTypes());
 			}
 		}
 	}
 
 	private void filterMappingFound(ICompilationUnit unit,
 			FilterMapping additional, Collection<IType> dependedTypes) throws JavaModelException {
 		modelObject.getFilterMappings().add(additional);
 		connectObjectWithFile(unit, additional, dependedTypes);
 	}
 
 	private void servletMappingFound(ICompilationUnit unit,
 			ServletMapping additional, Collection<IType> dependedTypes) throws JavaModelException {
 		modelObject.getServletMappings().add(additional);
 		connectObjectWithFile(unit, additional, dependedTypes);
 	}
 
 	private void servletFound(ICompilationUnit unit, Servlet servlet, Collection<IType> dependedTypes) throws JavaModelException {
 		modelObject.getServlets().add(servlet);
 		connectObjectWithFile(unit, servlet, dependedTypes);
 	}
 	
 	private void listenerFound(ICompilationUnit unit, Listener listener, Collection<IType> dependedTypes) throws JavaModelException {
 		modelObject.getListeners().add(listener);
 		connectObjectWithFile(unit, listener, dependedTypes);
 	}
 	
 	private void filterFound(ICompilationUnit unit, Filter filter, Collection<IType> dependedTypes) throws JavaModelException {
 		modelObject.getFilters().add(filter);
 		connectObjectWithFile(unit, filter, dependedTypes);
 	}
 
 	private void securityIdentityTypeFound(ICompilationUnit file, SecurityIdentityType additional) {
 	}
 
 	private void resourceRefFound(ICompilationUnit unit, ResourceRef resourceRef, Collection<IType> dependedTypes)
 			throws JavaModelException {
 		modelObject.getResourceRefs().add(resourceRef);
 		connectObjectWithFile(unit, resourceRef, dependedTypes);
 	}
 
 	private void ejbLocalRefFound(ICompilationUnit unit, EjbLocalRef localRef, Collection<IType> dependedTypes)
 			throws JavaModelException {
 		modelObject.getEjbLocalRefs().add(localRef);
 		connectObjectWithFile(unit, localRef, dependedTypes);
 	}
 
 	private void connectObjectWithFile(ICompilationUnit unit, JavaEEObject localRef, Collection<IType> dependedTypes)
 			throws JavaModelException {
 		modelToUnit.connect(localRef, unit);
 		for (IType type : dependedTypes) {
 			if (type.isBinary() || type.isInterface() == false)
 				continue;
 			modelToInterfaceUnit.connect(localRef, type.getCompilationUnit());
 		}
 	}
 
 	@Override
 	protected void processAddedCompilationUnit(IModelProviderEvent modelEvent, ICompilationUnit unit)
 			throws CoreException {
 		Result result = analyzeCompilationUnit(unit);
 		if (result == null || result.isEmpty())
 			return;
 		processResult(unit, result);
 		modelEvent.addResource(unit);
 	}
 
 	@Override
 	protected void processChangedCompilationUnit(IModelProviderEvent modelEvent, ICompilationUnit unit)
 			throws CoreException {
 		if (modelToUnit.containsTarget(unit))
 			processChangedModelUnit(modelEvent, unit);
 		else
 			processAddedCompilationUnit(modelEvent, unit);
 	}
 
 	private void processChangedModelUnit(IModelProviderEvent modelEvent, ICompilationUnit unit) throws CoreException {
 		processRemovedCompilationUnit(modelEvent, unit);
 		processAddedCompilationUnit(modelEvent, unit);
 	}
 
 	@Override
 	protected void processRemovedCompilationUnit(IModelProviderEvent modelEvent, ICompilationUnit unit)
 			throws CoreException {
 		if (modelToUnit.containsTarget(unit))
 			processRemovedModelResource(modelEvent, unit);
 		else if (modelToInterfaceUnit.containsTarget(unit))
 			processRemoveInterface(modelEvent, unit);
 	}
 
 	private void processRemoveInterface(IModelProviderEvent event, ICompilationUnit unit) {
 	}
 
 	@Override
 	protected void processRemovedPackage(IModelProviderEvent modelEvent, IJavaElementDelta delta) throws CoreException {
		Collection<ICompilationUnit> clonedCollection = cloneCollection(modelToUnit.getTargets());
		for (ICompilationUnit unit : clonedCollection) {
 			if (unit.getParent().getElementName().equals(delta.getElement().getElementName())) {
 				processRemovedCompilationUnit(modelEvent, unit);
 			}
 		}
 	}
 
	private Collection<ICompilationUnit> cloneCollection(Collection<ICompilationUnit> targets) {
		Collection<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
		for (ICompilationUnit iCompilationUnit : targets) {
			result.add(iCompilationUnit);
		}
		return result;
	}

 	private void processRemovedModelResource(IModelProviderEvent event, ICompilationUnit file) {
 		Collection<JavaEEObject> modelObjects = modelToUnit.getSources(file);
 		for (JavaEEObject o : modelObjects) {
 			if (Servlet.class.isInstance(o))
 				disconnectFromRoles(o);
 			EcoreUtil.remove((EObject) o);
 		}
 		modelToUnit.disconnect(file);
 		event.setEventCode(event.getEventCode() | IModelProviderEvent.REMOVED_RESOURCE);
 		event.addResource(file);
 	}
 
 	public void modify(Runnable runnable, IPath modelPath) {
 	}
 
 	public IStatus validateEdit(IPath modelPath, Object context) {
 		return null;
 	}
 
 	@Override
 	protected Collection<SecurityRoleRef> getSecurityRoleRefs(JavaEEObject target) {
 		if (Servlet.class.isInstance(target))
 			return ((Servlet) target).getSecurityRoleRefs();
 		return null;
 	}
 
 	@Override
 	protected Collection<SecurityRole> getSecurityRoles() {
 		return modelObject.getSecurityRoles();
 	}
 
 }
