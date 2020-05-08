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
 
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_METHOD_PARAMETERS;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_METHOD_RETURN_TYPE;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_NONE;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.ISourceRange;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jface.text.TypedRegion;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.validation.ValidatorMessage;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsMetamodelBuilder;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.ValidationMessages;
 import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
import org.jboss.tools.ws.jaxrs.core.jdt.CompilationUnitsRepository;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodParameter;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodSignature;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumElementKind;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumKind;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsResourceMethod;
 
 /** @author xcoulon */
 public class JaxrsResourceMethod extends JaxrsResourceElement<IMethod>
 		implements IJaxrsResourceMethod {
 
 	/** The parameter type names that can be annotated with {@link Context}. */
 	private final static List<String> CONTEXT_TYPE_NAMES = new ArrayList<String>(Arrays.asList(
 			"javax.ws.rs.core.HttpHeaders", "javax.ws.rs.core.UriInfo", "javax.ws.rs.core.Request",
 			"javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse", "javax.servlet.ServletConfig",
 			"javax.servlet.ServletContext", "javax.ws.rs.core.SecurityContext"));
 
 	private final JaxrsResource parentResource;
 
 	/**
 	 * return type of the java javaMethod. Null if this is not a subresource
 	 * locator.
 	 */
 	private IType returnedJavaType = null;
 
 	private final List<JavaMethodParameter> javaMethodParameters = new ArrayList<JavaMethodParameter>();
 
 	public static class Builder {
 
 		private final IMethod javaMethod;
 		private final JaxrsMetamodel metamodel;
 		private final JaxrsResource parentResource;
 		private Annotation httpMethod = null;
 		private Annotation consumesAnnotation = null;
 		private Annotation producesAnnotation = null;
 		private Annotation pathAnnotation = null;
 		private final List<JavaMethodParameter> javaMethodParameters = new ArrayList<JavaMethodParameter>();
 		private IType returnedJavaType;
 
 		public Builder(IMethod method, JaxrsResource parentResource,
 				JaxrsMetamodel metamodel) {
 			assert method != null;
 			assert parentResource != null;
 			assert metamodel != null;
 			this.javaMethod = method;
 			this.parentResource = parentResource;
 			this.metamodel = metamodel;
 		}
 
 		public Builder pathTemplate(Annotation pathTemplateAnnotation) {
 			this.pathAnnotation = pathTemplateAnnotation;
 			return this;
 		}
 
 		public Builder consumes(Annotation consumedMediaTypes) {
 			this.consumesAnnotation = consumedMediaTypes;
 			return this;
 		}
 
 		public Builder produces(Annotation producedMediaTypes) {
 			this.producesAnnotation = producedMediaTypes;
 			return this;
 		}
 
 		public Builder httpMethod(Annotation httpAnnotation) {
 			this.httpMethod = httpAnnotation;
 			return this;
 		}
 
 		public Builder methodParameter(JavaMethodParameter methodParameter) {
 			this.javaMethodParameters.add(methodParameter);
 			return this;
 		}
 
 		public Builder returnType(IType returnedJavaType) {
 			this.returnedJavaType = returnedJavaType;
 			return this;
 		}
 
 		public JaxrsResourceMethod build() throws JavaModelException {
 			// needs at least one of {@Path, @HttpMethod} annotations to be a
 			// valid resource method
 			List<Annotation> annotations = new ArrayList<Annotation>();
 			if (httpMethod != null) {
 				annotations.add(httpMethod);
 			}
 			if (pathAnnotation != null) {
 				annotations.add(pathAnnotation);
 			}
 			if (consumesAnnotation != null) {
 				annotations.add(consumesAnnotation);
 			}
 			if (producesAnnotation != null) {
 				annotations.add(producesAnnotation);
 			}
 			JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(
 					javaMethod, parentResource, javaMethodParameters,
 					returnedJavaType, annotations, metamodel);
 
 			return resourceMethod;
 		}
 
 	}
 
 	/**
 	 * Full constructor.
 	 * 
 	 * @param annotations
 	 * @param returnedJavaType
 	 * @param javaMethodParameters
 	 * 
 	 * @param javaMethodSignature
 	 * 
 	 * @throws CoreException
 	 */
 	private JaxrsResourceMethod(final IMethod javaMethod,
 			final JaxrsResource parentResource,
 			List<JavaMethodParameter> javaMethodParameters,
 			IType returnedJavaType, List<Annotation> annotations,
 			final JaxrsMetamodel metamodel) {
 		super(javaMethod, annotations, parentResource, metamodel);
 		this.parentResource = parentResource;
 		this.returnedJavaType = returnedJavaType;
 		if (javaMethodParameters != null) {
 			this.javaMethodParameters.addAll(javaMethodParameters);
 		}
 		this.parentResource.addMethod(this);
 	}
 
 	public int update(JavaMethodSignature methodSignature)
 			throws JavaModelException {
 		int flag = F_NONE;
 		// method parameters, including annotations
 		final List<JavaMethodParameter> methodParameters = methodSignature
 				.getMethodParameters();
 		if (!this.javaMethodParameters.equals(methodParameters)) {
 			this.javaMethodParameters.clear();
 			this.javaMethodParameters.addAll(methodParameters);
 			flag = F_METHOD_PARAMETERS;
 		}
 		// method return type
 		final IType returnedType = methodSignature.getReturnedType();
 		if ((this.returnedJavaType != null && returnedType == null)
 				|| (this.returnedJavaType == null && returnedType != null)
 				|| (this.returnedJavaType != null && returnedType != null && !this.returnedJavaType
 						.equals(returnedType))) {
 			this.returnedJavaType = returnedType;
 			flag += F_METHOD_RETURN_TYPE;
 		}
 		// TODO: method thrown exceptions..
 		return flag;
 	}
 
 	public IType getReturnType() {
 		return this.returnedJavaType;
 	}
 
 	@Override
 	public EnumElementKind getElementKind() {
 		return EnumElementKind.RESOURCE_METHOD;
 	}
 
 	@Override
 	public List<ValidatorMessage> validate() throws JavaModelException {
 		this.hasErrors(false);
 		final List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
 		messages.addAll(validateMissingPathValueInPathParamAnnotations());
 		messages.addAll(validateMissingPathParamAnnotations());
 		messages.addAll(validateParamsWithContextAnnotation());
 		messages.addAll(validateSingleParamWithoutAnnotation());
 		return messages;
 	}
 
 	/**
 	 * Validate that only one method parameter is not annotated with a JAX-RS
 	 * annotation. This non-annotated parameter is the "Entity parameter",
 	 * coming from the client's request body, unmarshalled by the appropriate
 	 * {@link MesssageBodyReader}.
 	 * 
 	 * @return
 	 */
 	private List<ValidatorMessage> validateSingleParamWithoutAnnotation() {
 		final List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
 		return messages;
 	}
 
 	/**
 	 * Validates that the method parameters annotated with {@link Context} are
 	 * of the supported types in the spec: {@link UriInfo}, {@link HttpHeaders},
 	 * {@link ServletConfig}, {@link ServletContext}, {@link HttpServletRequest}
 	 * , {@link Request}, {@link HttpServletResponse} and {@link Response}.
 	 * 
 	 * @return
	 * @throws JavaModelException 
 	 */
	private List<ValidatorMessage> validateParamsWithContextAnnotation() throws JavaModelException {
 		final List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
 		for (JavaMethodParameter parameter : this.javaMethodParameters) {
 			final Annotation annotation = parameter.getAnnotation(Context.class
 					.getName());
 			final String typeName = parameter.getTypeName();
 			if (annotation != null && typeName != null
 					&& !CONTEXT_TYPE_NAMES.contains(typeName)) {
 				final String msg = NLS
 								.bind(ValidationMessages.INVALID_CONTEXT_ANNOTATION,
 										typeName);
 				ValidatorMessage validationMsg = createValidationMessage(msg, IMarker.SEVERITY_ERROR, parameter.getRegion().getOffset(), parameter.getRegion().getLength());
 				messages.add(validationMsg);
 				this.hasErrors(true);
 			}
 		}
 		return messages;	
 	}
 
 	/**
 	 * Validates that the @Path annotation parameters have a counterpart in the
 	 * java method paramters, otherwise, issues some markers with a 'warning'
 	 * severity.
 	 * 
 	 * @return
 	 * @throws JavaModelException
 	 */
 	private List<ValidatorMessage> validateMissingPathParamAnnotations()
 			throws JavaModelException {
 		final List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
 		final List<String> pathParamValueProposals = getPathParamValueProposals();
 		for (String proposal : pathParamValueProposals) {
 			boolean matching = false;
 			for (JavaMethodParameter parameter : this.javaMethodParameters) {
 				final Annotation annotation = parameter
 						.getAnnotation(PathParam.class.getName());
 				if (annotation != null
 						&& annotation.getValue("value").equals(proposal)) {
 					matching = true;
 					break;
 				}
 			}
 			if (!matching) {
 				final String msg = NLS
 						.bind(ValidationMessages.INVALID_PATHPARAM_VALUE,
 								proposal);
 				final ISourceRange nameRange = getJavaElement().getNameRange();
 				ValidatorMessage validationMsg = createValidationMessage(msg, IMarker.SEVERITY_WARNING, nameRange.getOffset(), nameRange.getLength());
 				messages.add(validationMsg);
 			}
 		}
 		return messages;
 	}
 
 	/**
 	 * Checks that the {@link PathParam} annotation values match the params in
 	 * the {@link Path} annotations at the method and the parent type levels.
 	 * 
 	 * @return errors in case of mismatch, empty list otherwise.
	 * @throws JavaModelException 
 	 */
	private List<ValidatorMessage> validateMissingPathValueInPathParamAnnotations() throws JavaModelException {
 		final List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
 		final List<String> pathParamValueProposals = getPathParamValueProposals();
 		for (JavaMethodParameter parameter : this.javaMethodParameters) {
 			final Annotation annotation = parameter
 					.getAnnotation(PathParam.class.getName());
 			if (annotation != null) {
 				final String value = annotation.getValue("value");
 				if (!pathParamValueProposals.contains(value)) {
 					final String msg = NLS
 							.bind(ValidationMessages.INVALID_PATHPARAM_VALUE,
 									pathParamValueProposals);
 					final TypedRegion region = annotation.getRegion();
 					ValidatorMessage validationMsg = createValidationMessage(msg, IMarker.SEVERITY_ERROR, region.getOffset(), region.getLength());
 					hasErrors(true);
 					messages.add(validationMsg);
 				}
 			}
 		}
 		return messages;
 	}
 
 	/**
 	 * Creates a validation message from the given parameters. The created validation messages is of the 'JAX-RS' type.
 	 * @param msg the message to display
 	 * @param severity the severity of the marker
 	 * @param region the region that the validation marker points to
 	 * @return the created validation message.
	 * @throws JavaModelException 
 	 */
 	private ValidatorMessage createValidationMessage(final String msg,
			int severity, final int offset, int length) throws JavaModelException {
 		final ValidatorMessage validationMsg = ValidatorMessage.create(msg,
 				this.getResource());
		validationMsg.setType(JaxrsMetamodelBuilder.JAXRS_PROBLEM);
		final ICompilationUnit compilationUnit = this.getJavaElement().getCompilationUnit();
		final CompilationUnit ast = CompilationUnitsRepository.getInstance().getAST(compilationUnit);
		validationMsg.setAttribute(IMarker.LOCATION, NLS.bind(ValidationMessages.LINE_NUMBER, ast.getLineNumber(offset)));
 		validationMsg.setAttribute(IMarker.MARKER,
 				JaxrsMetamodelBuilder.JAXRS_PROBLEM);
 		validationMsg.setAttribute(IMarker.SEVERITY, severity);
 		validationMsg.setAttribute(IMarker.CHAR_START, offset);
 		validationMsg.setAttribute(IMarker.CHAR_END, offset + length);
 		Logger.debug("Validation message for {}: {}", this.getJavaElement()
 				.getElementName(), validationMsg.getAttribute(IMarker.MESSAGE));
 		return validationMsg;
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.jboss.tools.ws.jaxrs.core.internal.metamodel.IResourceMethod#hasErrors
 	 * (boolean)
 	 */
 	@Override
 	public void hasErrors(final boolean h) {
 		super.hasErrors(h);
 		if (hasErrors()) {
 			parentResource.hasErrors(true);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.jboss.tools.ws.jaxrs.core.internal.metamodel.IResourceMethod#getKind
 	 * ()
 	 */
 	@Override
 	public final EnumKind getKind() {
 		final Annotation pathAnnotation = getPathAnnotation();
 		final Annotation httpMethodAnnotation = getHttpMethodAnnotation();
 		if (pathAnnotation == null && httpMethodAnnotation != null) {
 			return EnumKind.RESOURCE_METHOD;
 		} else if (pathAnnotation != null && httpMethodAnnotation != null) {
 			return EnumKind.SUBRESOURCE_METHOD;
 		} else if (pathAnnotation != null && httpMethodAnnotation == null) {
 			return EnumKind.SUBRESOURCE_LOCATOR;
 		}
 		return EnumKind.UNDEFINED;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.IResourceMethod#
 	 * getParentResource()
 	 */
 	@Override
 	public final JaxrsResource getParentResource() {
 		return parentResource;
 	}
 
 	public Annotation getPathAnnotation() {
 		return getAnnotation(Path.class.getName());
 	}
 
 	@Override
 	public String getPathTemplate() {
 		final Annotation pathAnnotation = getPathAnnotation();
 		if (pathAnnotation == null) {
 			return null;
 		}
 		return pathAnnotation.getValue("value");
 	}
 
 	public Annotation getHttpMethodAnnotation() {
 		for (IJaxrsHttpMethod httpMethod : getMetamodel().getAllHttpMethods()) {
 			final Annotation annotation = getAnnotation(httpMethod
 					.getJavaElement().getFullyQualifiedName());
 			if (annotation != null) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getHttpMethod() {
 		final Annotation httpMethodAnnotation = getHttpMethodAnnotation();
 		if (httpMethodAnnotation == null) {
 			return null;
 		}
 		return httpMethodAnnotation.getValue("value");
 	}
 
 	public Annotation getConsumesAnnotation() {
 		return getAnnotation(Consumes.class.getName());
 	}
 
 	@Override
 	public List<String> getConsumedMediaTypes() {
 		final Annotation consumesAnnotation = getConsumesAnnotation();
 		if (consumesAnnotation == null) {
 			return null;
 		}
 		return consumesAnnotation.getValues("value");
 	}
 
 	public Annotation getProducesAnnotation() {
 		return getAnnotation(Produces.class.getName());
 	}
 
 	@Override
 	public List<String> getProducedMediaTypes() {
 		final Annotation producesAnnotation = getProducesAnnotation();
 		if (producesAnnotation == null) {
 			return null;
 		}
 		return producesAnnotation.getValues("value");
 	}
 
 	/** @return the javaMethodParameters */
 	@Override
 	public List<JavaMethodParameter> getJavaMethodParameters() {
 		return javaMethodParameters;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public final String toString() {
 		return "ResourceMethod '" + parentResource.getName() + "."
 				+ getJavaElement().getElementName() + "' ("
 				+ getKind().toString() + ")";
 	}
 
 	@Override
 	public List<String> getPathParamValueProposals() {
 		List<String> proposals = new ArrayList<String>();
 		final Annotation methodPathAnnotation = getPathAnnotation();
 		if (methodPathAnnotation != null) {
 			final String value = methodPathAnnotation.getValue("value");
 			proposals.addAll(extractParamsFromUriTemplateFragment(value));
 		}
 		final Annotation typePathAnnotation = getParentResource()
 				.getPathAnnotation();
 		if (typePathAnnotation != null) {
 			final String value = typePathAnnotation.getValue("value");
 			proposals.addAll(extractParamsFromUriTemplateFragment(value));
 		}
 		return proposals;
 	}
 
 	/**
 	 * Extracts all the character sequences inside of curly braces ('{' and '}')
 	 * and returns them as a list of strings
 	 * 
 	 * @param value
 	 *            the given value
 	 * @return the list of character sequences, or an empty list
 	 */
 	private static List<String> extractParamsFromUriTemplateFragment(
 			String value) {
 		List<String> params = new ArrayList<String>();
 		int beginIndex = -1;
 		while ((beginIndex = value.indexOf("{", beginIndex + 1)) != -1) {
 			int semicolonIndex = value.indexOf(":", beginIndex);
 			int closingCurlyBraketIndex = value.indexOf("}", beginIndex);
 			int endIndex = semicolonIndex != -1 ? semicolonIndex
 					: closingCurlyBraketIndex;
 			params.add(value.substring(beginIndex + 1, endIndex).trim());
 		}
 		return params;
 	}
 
 }
