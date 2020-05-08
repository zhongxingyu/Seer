 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain;
 
 import static org.eclipse.jdt.core.IJavaElement.FIELD;
 import static org.eclipse.jdt.core.IJavaElement.METHOD;
 import static org.eclipse.jdt.core.IJavaElement.TYPE;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.ApplicationPath;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.CookieParam;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.HttpMethod;
 import javax.ws.rs.MatrixParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceMethod.Builder;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodParameter;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodSignature;
 import org.jboss.tools.ws.jaxrs.core.jdt.JaxrsAnnotationsScanner;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumElementKind;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumKind;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsHttpMethod;
 
 public class JaxrsElementFactory {
 
 	/**
 	 * Attempts to create a new JAX-RS element from the given Java annotation, <b>without adding it to the given JAX-RS
 	 * Metamodel</b>
 	 * 
 	 * @param element
 	 * @param ast
 	 * @param metamodel
 	 * @return the created JAX-RS element or null if the given Java annotation is not a valid one.
 	 * @throws CoreException
 	 */
 	public JaxrsJavaElement<?> createElement(IAnnotation javaAnnotation, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws CoreException {
 		Annotation annotation = JdtUtils.resolveAnnotation(javaAnnotation, ast);
		if(annotation == null) { // annotation on package declaration are ignored
			return null;
		}
 		final String annotationName = annotation.getName();
 		if (annotationName.equals(HttpMethod.class.getName())) {
 			final JaxrsHttpMethod httpMethod = createHttpMethod(annotation, ast, metamodel);
 			return httpMethod;
 		} else if (annotationName.equals(ApplicationPath.class.getName())) {
 			final JaxrsJavaApplication application = createApplication(annotation, ast, metamodel);
 			return application;
 		} else {
 			switch (javaAnnotation.getParent().getElementType()) {
 			case TYPE:
 				if (annotationName.equals(Path.class.getName())) {
 					return createResource(annotation, ast, metamodel);
 				}
 				break;
 			case METHOD:
 				final JaxrsHttpMethod httpMethod = (JaxrsHttpMethod) metamodel.getHttpMethod(annotationName);
 				if (annotationName.equals(Path.class.getName())) {
 					return createResourceMethod(annotation, ast, metamodel);
 				} else if (httpMethod != null) {
 					return createResourceMethod(annotation, ast, metamodel);
 				}
 				break;
 			case FIELD:
 				if (annotationName.equals(PathParam.class.getName())
 						|| annotationName.equals(QueryParam.class.getName())
 						|| annotationName.equals(MatrixParam.class.getName())) {
 					return createField(annotation, ast, metamodel);
 				}
 				break;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a JAX-RS Root Resource (including its methods and its fields) from the given path annotation and its AST,
 	 * <b>without adding it to the given JAX-RS Metamodel</b>
 	 * 
 	 * @param pathAnnotation
 	 *            the @Path annotation found on the Java Type
 	 * @param ast
 	 *            the AST associated to the annotated java type
 	 * @param metamodel
 	 *            the current metamodel, in which the Root Resource should be added
 	 * @return the created Root Resource
 	 * @throws CoreException
 	 */
 	public JaxrsResource createResource(final Annotation pathAnnotation, final CompilationUnit ast,
 			final JaxrsMetamodel metamodel) throws CoreException {
 		// create the resource:
 		return createResource((IType) pathAnnotation.getJavaParent(), ast, metamodel);
 	}
 
 	/**
 	 * Creates a JAX-RS Resource (including its methods and its fields) from the given type and its AST, <b>without
 	 * adding it to the given JAX-RS Metamodel</b>
 	 * 
 	 * @param javaType
 	 *            the Java Type
 	 * @param ast
 	 *            the AST associated to the java type
 	 * @param metamodel
 	 *            the current metamodel, in which the JAX-RS Resource should be added
 	 * @return the created resource
 	 * @throws CoreException
 	 */
 	public JaxrsResource createResource(IType javaType, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws CoreException {
 		// create the resource:
 		final JaxrsResource resource = internalCreateResource(javaType, ast, metamodel);
 		// find the resource methods, subresource methods and subresource
 		// locators of this resource:
 		final List<IMethod> javaMethods = JaxrsAnnotationsScanner.findResourceMethods(javaType,
 				metamodel.getAllHttpMethods(), new NullProgressMonitor());
 		for (IMethod javaMethod : javaMethods) {
 			internalCreateResourceMethod(javaMethod, ast, metamodel, resource);
 		}
 		// find the available type fields
 		for (IField javaField : javaType.getFields()) {
 			internalCreateField(javaField, ast, metamodel, resource);
 		}
 		// well, sorry.. this is not a valid JAX-RS resource..
 		if (resource.getKind() == EnumKind.UNDEFINED) {
 			return null;
 		}
 		return resource;
 	}
 
 	private JaxrsResource internalCreateResource(IType type, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws JavaModelException {
 		final Map<String, Annotation> annotations = JdtUtils.resolveAnnotations(type, ast, Path.class.getName(),
 				Consumes.class.getName(), Produces.class.getName());
 		final Annotation pathAnnotation = annotations.get(Path.class.getName());
 		final Annotation consumesAnnotation = annotations.get(Consumes.class.getName());
 		final Annotation producesAnnotation = annotations.get(Produces.class.getName());
 		final JaxrsResource resource = new JaxrsResource.Builder(type, metamodel).pathTemplate(pathAnnotation)
 				.consumes(consumesAnnotation).produces(producesAnnotation).build();
 		return resource;
 	}
 
 	/**
 	 * Creates a JAX-RS resource method from the given annotation (@Path or an HttpMethod) and its AST, <b>without
 	 * adding it to the given JAX-RS Metamodel</b>. If the parent resource did not exist before, its is also created.
 	 * 
 	 * @param annotation
 	 * @param ast
 	 * @param metamodel
 	 * @return
 	 * @throws CoreException
 	 */
 	public JaxrsResourceMethod createResourceMethod(Annotation annotation, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws CoreException {
 		final IMethod method = (IMethod) annotation.getJavaParent();
 		return createResourceMethod(method, ast, metamodel);
 	}
 
 	/**
 	 * Creates a JAX-RS resource method from the given annotation (@Path or an HttpMethod) and its AST, <b>without
 	 * adding it to the given JAX-RS Metamodel</b>
 	 * 
 	 * @param annotation
 	 * @param ast
 	 * @param metamodel
 	 * @return
 	 * @throws CoreException
 	 */
 	public JaxrsResourceMethod createResourceMethod(IMethod method, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws CoreException {
 
 		final IType parentType = (IType) method.getParent();
 		JaxrsResource parentResource = (JaxrsResource) metamodel.getElement(parentType);
 		if (parentResource == null) {
 			// create parentResource on-the-fly
 			parentResource = internalCreateResource(parentType, ast, metamodel);
 		}
 		final JaxrsResourceMethod resourceMethod = internalCreateResourceMethod(method, ast, metamodel, parentResource);
 		return resourceMethod;
 	}
 
 	private JaxrsResourceMethod internalCreateResourceMethod(IMethod javaMethod, CompilationUnit ast,
 			JaxrsMetamodel metamodel, JaxrsResource parentResource) throws JavaModelException {
 		final List<String> httpMethodAnnotationNames = new ArrayList<String>();
 		for (IJaxrsHttpMethod httpMethod : metamodel.getAllHttpMethods()) {
 			httpMethodAnnotationNames.add(httpMethod.getJavaElement().getFullyQualifiedName());
 		}
 		final List<String> annotationNames = new ArrayList<String>();
 		annotationNames.addAll(Arrays.asList(Path.class.getName(), Produces.class.getName(), Consumes.class.getName()));
 		annotationNames.addAll(httpMethodAnnotationNames);
 		final Map<String, Annotation> annotations = JdtUtils.resolveAnnotations(javaMethod, ast, annotationNames);
 		Annotation httpMethod = null;
 		final Annotation pathAnnotation = annotations.get(Path.class.getName());
 		for (String httpMethodAnnotationName : httpMethodAnnotationNames) {
 			if (annotations.containsKey(httpMethodAnnotationName)) {
 				httpMethod = annotations.get(httpMethodAnnotationName);
 				break;
 			}
 		}
 		if (httpMethod == null && pathAnnotation == null) {
 			Logger.debug("Cannot create ResourceMethod: no Path annotation nor HttpMethod found on method {}.{}()",
 					javaMethod.getParent().getElementName(), javaMethod.getElementName());
 		} else {
 			final Annotation producesAnnotation = annotations.get(Produces.class.getName());
 			final Annotation consumesAnnotation = annotations.get(Consumes.class.getName());
 			final JavaMethodSignature methodSignature = JdtUtils.resolveMethodSignature(javaMethod, ast);
 			// avoid creating Resource Method when the Java Method cannot be parsed (ie, syntax/compilation error)P
 			if (methodSignature != null) {
 				final Builder builder = new JaxrsResourceMethod.Builder(javaMethod, parentResource, metamodel)
 						.pathTemplate(pathAnnotation).consumes(consumesAnnotation).produces(producesAnnotation)
 						.httpMethod(httpMethod).returnType(methodSignature.getReturnedType());
 				for (JavaMethodParameter methodParam : methodSignature.getMethodParameters()) {
 					builder.methodParameter(methodParam);
 				}
 				final JaxrsResourceMethod resourceMethod = builder.build();
 
 				return resourceMethod;
 			}
 		}
 		return null;
 
 	}
 
 	/**
 	 * Creates a JAX-RS HTTP Method from the given {@link javax.ws.rs.HttpMethod} annotation and its AST, <b>without
 	 * adding it to the given JAX-RS Metamodel</b>
 	 * 
 	 * @param ast
 	 * @param metamodel
 	 * @param annotation
 	 * @return
 	 * @throws CoreException
 	 */
 	public JaxrsHttpMethod createHttpMethod(final IType javaType, final CompilationUnit ast,
 			final JaxrsMetamodel metamodel) throws CoreException {
 		Annotation httpMethodAnnotation = JdtUtils.resolveAnnotation(javaType, ast, HttpMethod.class);
 		if (httpMethodAnnotation == null) {
 			return null;
 		}
 		final JaxrsHttpMethod httpMethod = new JaxrsHttpMethod(javaType, httpMethodAnnotation, metamodel);
 		return httpMethod;
 	}
 
 	/**
 	 * Creates a JAX-RS Application from the given pathAnnotation and its AST, <b>without adding it to the given JAX-RS
 	 * Metamodel</b>.
 	 * 
 	 * @param ast
 	 * @param metamodel
 	 * @param annotation
 	 * @return
 	 * @throws CoreException
 	 */
 	public JaxrsHttpMethod createHttpMethod(final Annotation annotation, final CompilationUnit ast,
 			final JaxrsMetamodel metamodel) throws CoreException {
 		if (annotation.getJavaParent() != null && annotation.getJavaParent().getElementType() == IJavaElement.TYPE
 				&& annotation.getName().equals(HttpMethod.class.getName())) {
 			return new JaxrsHttpMethod((IType) annotation.getJavaParent(), annotation, metamodel);
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a JAX-RS Application from the given type and its AST, <b>without adding it to the given JAX-RS
 	 * Metamodel</b>
 	 * 
 	 * @param ast
 	 * @param metamodel
 	 * @param annotation
 	 * @return
 	 * @throws CoreException
 	 */
 	public JaxrsJavaApplication createApplication(final IType javaType, final CompilationUnit ast,
 			final JaxrsMetamodel metamodel) throws CoreException {
 		Annotation applicationPathAnnotation = JdtUtils.resolveAnnotation(javaType, ast, ApplicationPath.class);
 		if (applicationPathAnnotation == null) {
 			return null;
 		}
 		return new JaxrsJavaApplication(javaType, applicationPathAnnotation, metamodel);
 	}
 
 	/**
 	 * Creates a JAX-RS Application from the given {@link javax.ws.rs.ApplicationPath } annotation and its AST,
 	 * <b>without adding it to the given JAX-RS Metamodel</b>.
 	 * 
 	 * @param ast
 	 * @param metamodel
 	 * @param annotation
 	 * @return
 	 * @throws CoreException
 	 */
 	public JaxrsJavaApplication createApplication(final Annotation annotation, final CompilationUnit ast,
 			final JaxrsMetamodel metamodel) throws CoreException {
 		if (annotation.getJavaParent() != null && annotation.getJavaParent().getElementType() == IJavaElement.TYPE
 				&& annotation.getName().equals(ApplicationPath.class.getName())) {
 			return new JaxrsJavaApplication((IType) annotation.getJavaParent(), annotation, metamodel);
 		}
 		return null;
 	}
 
 	/**
 	 * Create a JAX-RS Resource field from the given annotation, <b>without adding it to the given JAX-RS Metamodel</b>
 	 * 
 	 * @param pathParamannotation
 	 * @param ast
 	 * @param metamodel
 	 * @return
 	 * @throws JavaModelException
 	 * @throws CoreException
 	 */
 	public JaxrsResourceField createField(Annotation annotation, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws JavaModelException {
 		final IField javaField = (IField) annotation.getJavaParent();
 		return createField(javaField, ast, metamodel);
 	}
 
 	public JaxrsResourceField createField(IField javaField, CompilationUnit ast, JaxrsMetamodel metamodel)
 			throws JavaModelException {
 		final IType parentType = (IType) javaField.getParent();
 		JaxrsBaseElement parentResource = metamodel.getElement(parentType);
 		if (parentResource == null) {
 			// creating the parent resource but not adding it to the metamodel
 			// yet..
 			parentResource = internalCreateResource(parentType, ast, metamodel);
 		}
 		if (parentResource != null && parentResource.getElementKind() == EnumElementKind.RESOURCE) {
 			final JaxrsResourceField field = internalCreateField(javaField, ast, metamodel,
 					(JaxrsResource) parentResource);
 			return field;
 		}
 		return null;
 	}
 
 	private JaxrsResourceField internalCreateField(IField javaField, CompilationUnit ast, JaxrsMetamodel metamodel,
 			final JaxrsResource parentResource) throws JavaModelException {
 		final List<String> supportedFieldAnnotations = Arrays.asList(MatrixParam.class.getName(),
 				QueryParam.class.getName(), PathParam.class.getName(), CookieParam.class.getName(),
 				HeaderParam.class.getName(), DefaultValue.class.getName());
 		final Map<String, Annotation> annotations = JdtUtils.resolveAnnotations(javaField, ast,
 				supportedFieldAnnotations);
 		if ((annotations.size() == 1 && !annotations.containsKey(DefaultValue.class.getName()))
 				|| (annotations.size() == 2 && annotations.containsKey(DefaultValue.class.getName()))) {
 			final JaxrsResourceField field = new JaxrsResourceField(javaField, new ArrayList<Annotation>(
 					annotations.values()), parentResource, metamodel);
 			return field;
 		}
 		return null;
 	}
 
 	public JaxrsWebxmlApplication createApplication(String applicationPath, IResource resource, JaxrsMetamodel metamodel) {
 		return new JaxrsWebxmlApplication(applicationPath, resource, metamodel);
 	}
 }
