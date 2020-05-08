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
 import java.util.List;
 
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.javaee.core.EjbLocalRef;
 import org.eclipse.jst.javaee.core.JavaEEObject;
 import org.eclipse.jst.javaee.core.JavaeeFactory;
 import org.eclipse.jst.javaee.core.Listener;
 import org.eclipse.jst.javaee.core.ResourceRef;
 import org.eclipse.jst.javaee.core.RunAs;
 import org.eclipse.jst.javaee.core.SecurityRoleRef;
 import org.eclipse.jst.javaee.web.Filter;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jee.model.internal.common.AbstractAnnotationFactory;
 import org.eclipse.jst.jee.model.internal.common.Result;
 
 /**
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public class WebAnnotationFactory extends AbstractAnnotationFactory {
 
 	private static final String EJB = "EJB"; //$NON-NLS-1$
 	private static final String RUN_AS = "RunAs"; //$NON-NLS-1$
 	private static final String RESOURCE = "Resource"; //$NON-NLS-1$
 	private static final String RESOURCES = "Resources"; //$NON-NLS-1$
 	private static final String DECLARE_ROLES = "DeclareRoles"; //$NON-NLS-1$
 
 	private WebAnnotationFactory() {
 		super();
 	}
 
 	public static WebAnnotationFactory createFactory() {
 		return new WebAnnotationFactory();
 	}
 
	public Result createServlet(IType rootType) throws JavaModelException {
 		Result result = new Result();
 		Servlet servlet = WebFactory.eINSTANCE.createServlet();
		servlet.setServletName(rootType.getElementName());
 		servlet.setServletClass(rootType.getFullyQualifiedName());
 		result.setMainObject(servlet);
 		processTypeAnnotations(result, rootType);
 		processMethodAnnotations(result, rootType);
 		processFieldAnnotations(result, rootType);
 		return result;
 	}
 
 	public Result createListener(IType rootType) throws JavaModelException {
 		Result result = new Result();
 		Listener listener = JavaeeFactory.eINSTANCE.createListener();
 		listener.setListenerClass(rootType.getFullyQualifiedName());
 		result.setMainObject(listener);
 		processTypeAnnotations(result, rootType);
 		processMethodAnnotations(result, rootType);
 		processFieldAnnotations(result, rootType);
 		return result;
 	}
 
	public Result createFilter(IType rootType) throws JavaModelException {
 		Result result = new Result();
 		Filter filter = WebFactory.eINSTANCE.createFilter();
 		filter.setFilterClass(rootType.getFullyQualifiedName());
 		result.setMainObject(filter);
 		processTypeAnnotations(result, rootType);
 		processMethodAnnotations(result, rootType);
 		processFieldAnnotations(result, rootType);
 		return result;
 	}
 
 	private void processTypeAnnotations(Result result, IType type) throws JavaModelException {
 		JavaEEObject mainObject = result.getMainObject();
 		boolean isServlet = Servlet.class.isInstance(mainObject);
 		for (IAnnotation annotation : type.getAnnotations()) {
 			String annotationName = annotation.getElementName();
 			if (isServlet && DECLARE_ROLES.equals(annotationName)) {
 				List<SecurityRoleRef> refs = new ArrayList<SecurityRoleRef>();
 				processDeclareRoles(result, refs, annotation, type);
 				((Servlet) mainObject).getSecurityRoleRefs().addAll(refs);
 			} else if (RESOURCES.equals(annotationName)) {
 				List<ResourceRef> resourceRefs = new ArrayList<ResourceRef>(2);
 				processResourcesAnnotation(annotation, resourceRefs, type, result.getDependedTypes());
 				result.getAdditional().addAll(resourceRefs);
 			} else if (RESOURCE.equals(annotationName)) {
 				List<ResourceRef> resourceRefs = new ArrayList<ResourceRef>(2);
 				processResourceRefAnnotation(annotation, resourceRefs, type, result.getDependedTypes());
 				result.getAdditional().addAll(resourceRefs);
 			} else if (isServlet && RUN_AS.equals(annotationName)) {
 				RunAs runAs = JavaeeFactory.eINSTANCE.createRunAs();
 				processRunAs(annotation, runAs);
 				((Servlet) mainObject).setRunAs(runAs);
 			}
 		}
 	}
 
 	private void processFieldAnnotations(Result result, IType type) throws JavaModelException {
 		for (IField field : type.getFields()) {
 			for (IAnnotation annotation : field.getAnnotations()) {
 				processMemberAnnotations(result, field, annotation);
 			}
 		}
 	}
 
 	private void processMethodAnnotations(Result result, IType type) throws JavaModelException {
 		for (IMethod method : type.getMethods()) {
 			for (IAnnotation annotation : method.getAnnotations()) {
 				processMemberAnnotations(result, method, annotation);
 			}
 		}
 	}
 
 	private void processMemberAnnotations(Result result, IMember member, IAnnotation annotation)
 			throws JavaModelException {
 		String annotationName = annotation.getElementName();
 		if (EJB.equals(annotationName)) {
 			List<EjbLocalRef> refs = new ArrayList<EjbLocalRef>(1);
 			processEjbAnnotation(annotation, refs, member, result.getDependedTypes());
 			result.getAdditional().addAll(refs);
 		} else if (RESOURCE.equals(annotationName)) {
 			List<ResourceRef> refs = new ArrayList<ResourceRef>(1);
 			processResourceRefAnnotation(annotation, refs, member, result.getDependedTypes());
 			result.getAdditional().addAll(refs);
 		}
 	}
 
 }
