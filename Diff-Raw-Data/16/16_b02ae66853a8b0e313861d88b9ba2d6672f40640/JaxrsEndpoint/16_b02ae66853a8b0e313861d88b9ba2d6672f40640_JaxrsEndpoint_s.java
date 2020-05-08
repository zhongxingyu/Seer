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
 
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_CONSUMED_MEDIATYPES_VALUE;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_HTTP_METHOD_VALUE;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_PATH_VALUE;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_PRODUCED_MEDIATYPES_VALUE;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.MatrixParam;
 import javax.ws.rs.QueryParam;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodParameter;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsApplication;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsEndpoint;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsResourceMethod;
 
 public class JaxrsEndpoint implements IJaxrsEndpoint {
 
 	private final JaxrsMetamodel metamodel;
 
 	private JaxrsHttpMethod httpMethod;
 
 	private final LinkedList<JaxrsResourceMethod> resourceMethods;
 
 	private IJaxrsApplication application = null;
 
 	private String uriPathTemplate = null;
 
 	private List<String> consumedMediaTypes = null;
 
 	private List<String> producedMediaTypes = null;
 
 	public JaxrsEndpoint(final JaxrsMetamodel metamodel, final JaxrsHttpMethod httpMethod,
 			final JaxrsResourceMethod resourceMethod) {
 		this(metamodel, httpMethod, new LinkedList<JaxrsResourceMethod>(Arrays.asList(resourceMethod)));
 	}
 
 	public JaxrsEndpoint(final JaxrsMetamodel metamodel, final JaxrsHttpMethod httpMethod,
 			final LinkedList<JaxrsResourceMethod> resourceMethods) {
 		this.metamodel = metamodel;
 		this.application = (metamodel != null ? metamodel.getApplication() : null);
 		this.httpMethod = httpMethod;
 		this.resourceMethods = resourceMethods;
 		refreshUriPathTemplate();
 		refreshConsumedMediaTypes();
 		refreshProducedMediaTypes();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		final IMethod javaMethod = resourceMethods.getLast().getJavaElement();
 		return (httpMethod != null ? httpMethod.getHttpVerb() : null) + " " + uriPathTemplate + " | consumes:"
 				+ consumedMediaTypes + " | produces=" + producedMediaTypes + " in method "
 				+ javaMethod.getParent().getElementName() + "." + javaMethod.getElementName() + "(...)";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((consumedMediaTypes == null) ? 0 : consumedMediaTypes.hashCode());
 		result = prime * result + ((httpMethod == null) ? 0 : httpMethod.hashCode());
 		result = prime * result + ((producedMediaTypes == null) ? 0 : producedMediaTypes.hashCode());
 		result = prime * result + ((resourceMethods == null) ? 0 : resourceMethods.hashCode());
 		result = prime * result + ((uriPathTemplate == null) ? 0 : uriPathTemplate.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		JaxrsEndpoint other = (JaxrsEndpoint) obj;
 		if (consumedMediaTypes == null) {
 			if (other.consumedMediaTypes != null) {
 				return false;
 			}
 		} else if (!consumedMediaTypes.equals(other.consumedMediaTypes)) {
 			return false;
 		}
 		if (httpMethod == null) {
 			if (other.httpMethod != null) {
 				return false;
 			}
 		} else if (!httpMethod.equals(other.httpMethod)) {
 			return false;
 		}
 		if (producedMediaTypes == null) {
 			if (other.producedMediaTypes != null) {
 				return false;
 			}
 		} else if (!producedMediaTypes.equals(other.producedMediaTypes)) {
 			return false;
 		}
 		if (resourceMethods == null) {
 			if (other.resourceMethods != null) {
 				return false;
 			}
 		} else if (!resourceMethods.equals(other.resourceMethods)) {
 			return false;
 		}
 		if (uriPathTemplate == null) {
 			if (other.uriPathTemplate != null) {
 				return false;
 			}
 		} else if (!uriPathTemplate.equals(other.uriPathTemplate)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Triggers a refresh when changes occurred on the application element (whatever operation).
 	 * 
 	 * @return true if the endpoint is still valid, false otherwise (it should be removed from the metamodel)
 	 */
 	public boolean refresh(IJaxrsApplication application) {
 		this.application = application;
 		refreshUriPathTemplate();
 		return true;
 	}
 
 	/**
 	 * Triggers a refresh when changes occurred on one or more elements (HttpMethod and/or ResourcMethods) of the
 	 * endpoint.
 	 * 
 	 * @return true if the endpoint is still valid, false otherwise (it should be removed from the metamodel)
 	 */
 	public boolean refresh(IJaxrsResourceMethod changedResourceMethod, int flags) {
 		// check if the chain of resource methods still match
 		/*
 		 * if ((flags & F_ELEMENT_KIND) > 0 && changedResourceMethod.getKind() == EnumKind.SUBRESOURCE_LOCATOR) { return
 		 * false; }
 		 */
 
 		if ((flags & F_HTTP_METHOD_VALUE) > 0) {
 			refreshHttpMethod();
 		}
 
 		if ((flags & F_PATH_VALUE) > 0) {
 			refreshUriPathTemplate();
 		}
 
 		// look for mediatype capabilities at the method level, then fall back
 		// at the type level, then "any" otherwise
 		if ((flags & F_CONSUMED_MEDIATYPES_VALUE) > 0 || (flags & F_PRODUCED_MEDIATYPES_VALUE) > 0) {
 			if ((flags & F_CONSUMED_MEDIATYPES_VALUE) > 0) {
 				refreshConsumedMediaTypes();
 			}
 			if ((flags & F_PRODUCED_MEDIATYPES_VALUE) > 0) {
 				refreshProducedMediaTypes();
 			}
 		}
 		return true;
 	}
 
 	private void refreshHttpMethod() {
 		final JaxrsResourceMethod resourceMethod = resourceMethods.getLast();
 		final Annotation httpMethodAnnotation = resourceMethod.getHttpMethodAnnotation();
 		if (httpMethodAnnotation != null) {
 			this.httpMethod = metamodel.getHttpMethod(httpMethodAnnotation);
 		}
 	}
 
 	private void refreshProducedMediaTypes() {
 		final JaxrsResourceMethod resourceMethod = resourceMethods.getLast();
 		final JaxrsResource resource = resourceMethod.getParentResource();
 		if (resourceMethod.getProducedMediaTypes() != null) {
 			this.producedMediaTypes = resourceMethod.getProducedMediaTypes();
 		} else if (resourceMethod.getParentResource().getProducedMediaTypes() != null) {
 			this.producedMediaTypes = resource.getProducedMediaTypes();
 		} else {
 			this.producedMediaTypes = Arrays.asList("*/*");
 		}
 	}
 
 	private void refreshConsumedMediaTypes() {
 		final JaxrsResourceMethod resourceMethod = resourceMethods.getLast();
 		final JaxrsResource resource = resourceMethod.getParentResource();
 		if (resourceMethod.getConsumedMediaTypes() != null) {
 			this.consumedMediaTypes = resourceMethod.getConsumedMediaTypes();
 		} else if (resourceMethod.getParentResource().getConsumedMediaTypes() != null) {
 			this.consumedMediaTypes = resource.getConsumedMediaTypes();
 		} else {
 			this.consumedMediaTypes = Arrays.asList("*/*");
 		}
 	}
 
 	private void refreshUriPathTemplate() {
 		// compute the URI Path Template from the chain of Methods/Resources
 		StringBuilder uriPathTemplateBuilder = new StringBuilder();
 		if (application != null) {
 			uriPathTemplateBuilder.append(application.getApplicationPath());
 		}
 		for (JaxrsResourceMethod resourceMethod : resourceMethods) {
 			if (resourceMethod.getParentResource().getPathTemplate() != null) {
 				uriPathTemplateBuilder.append("/").append(resourceMethod.getParentResource().getPathTemplate());
 			}
 			if (resourceMethod.getPathTemplate() != null) {
 				uriPathTemplateBuilder.append("/").append(resourceMethod.getPathTemplate());
 			}
 			if (resourceMethod.getJavaMethodParameters() != null) {
 				refreshUriTemplateMatrixParams(uriPathTemplateBuilder, resourceMethod);
 				refreshUriTemplateQueryParams(uriPathTemplateBuilder, resourceMethod);
 			}
 
 		}
 		this.uriPathTemplate = uriPathTemplateBuilder.toString();
 		while (uriPathTemplate.indexOf("//") > -1) {
 			this.uriPathTemplate = uriPathTemplate.replace("//", "/");
 		}
 	}
 
 	private void refreshUriTemplateMatrixParams(StringBuilder uriPathTemplateBuilder, JaxrsResourceMethod resourceMethod) {
 		List<JavaMethodParameter> matrixParameters = new ArrayList<JavaMethodParameter>();
 		for (Iterator<JavaMethodParameter> paramIterator = resourceMethod.getJavaMethodParameters().iterator(); paramIterator
 				.hasNext();) {
 			JavaMethodParameter parameter = paramIterator.next();
 			if (parameter.getAnnotation(MatrixParam.class.getName()) != null) {
 				matrixParameters.add(parameter);
 			}
 		}
 		for (Iterator<JavaMethodParameter> iterator = matrixParameters.iterator(); iterator.hasNext();) {
 			JavaMethodParameter matrixParam = iterator.next();
 			final Annotation matrixParamAnnotation = matrixParam.getAnnotation(MatrixParam.class.getName());
 			uriPathTemplateBuilder.append(";").append(matrixParamAnnotation.getValue("value")).append("={")
 					.append(matrixParam.getTypeName()).append("}");
 		}
 	}
 
 	private void refreshUriTemplateQueryParams(StringBuilder uriPathTemplateBuilder, JaxrsResourceMethod resourceMethod) {
 		List<JavaMethodParameter> queryParameters = new ArrayList<JavaMethodParameter>();
 		for (Iterator<JavaMethodParameter> paramIterator = resourceMethod.getJavaMethodParameters().iterator(); paramIterator
 				.hasNext();) {
 			JavaMethodParameter parameter = paramIterator.next();
 			if (parameter.getAnnotation(QueryParam.class.getName()) != null) {
 				queryParameters.add(parameter);
 			}
 		}
 		if (queryParameters.size() > 0) {
 			uriPathTemplateBuilder.append('?');
 			for (Iterator<JavaMethodParameter> iterator = queryParameters.iterator(); iterator.hasNext();) {
 				JavaMethodParameter queryParam = iterator.next();
 				final Annotation queryParamAnnotation = queryParam.getAnnotation(QueryParam.class.getName());
				uriPathTemplateBuilder.append(queryParamAnnotation.getValue("value")).append("={");
				uriPathTemplateBuilder.append(queryParam.getTypeName());
 				final Annotation defaultValueAnnotation = queryParam.getAnnotation(DefaultValue.class.getName());
 				if (defaultValueAnnotation != null) {
					uriPathTemplateBuilder.append(':').append(defaultValueAnnotation.getValue("value"));
 				}
 				uriPathTemplateBuilder.append('}');
 
 				if (iterator.hasNext()) {
 					uriPathTemplateBuilder.append('&');
 				}
 			}
 		}
 	}
 
 	@Override
 	public int compareTo(IJaxrsEndpoint other) {
 		int uriPathTemplateComparison = this.uriPathTemplate.compareTo(other.getUriPathTemplate());
 		if (uriPathTemplateComparison != 0) {
 			return uriPathTemplateComparison;
 		}
 		return this.httpMethod.compareTo(other.getHttpMethod());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.IJaxrsEndpoint #getHttpMethod()
 	 */
 	@Override
 	public IJaxrsHttpMethod getHttpMethod() {
 		return httpMethod;
 	}
 
 	/**
 	 * Convenient method to check if this endpoint uses this HttpMethod.
 	 * 
 	 * @return true if this endpoint's HttpMethod is the one given in parameter
 	 */
 	public boolean match(IJaxrsHttpMethod httpMethod) {
 		return this.httpMethod.equals(httpMethod);
 	}
 
 	/** @return the resourceMethods */
 	@Override
 	public LinkedList<IJaxrsResourceMethod> getResourceMethods() {
 		return new LinkedList<IJaxrsResourceMethod>(resourceMethods);
 	}
 
 	/**
 	 * Convenient method to check if this endpoint uses this ResourceMethod.
 	 * 
 	 * @return true if this endpoint's ResourceMethod is the one given in parameter
 	 */
 	public boolean match(IJaxrsResourceMethod resourceMethod) {
 		return this.resourceMethods.contains(resourceMethod);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.IJaxrsEndpoint #getUriPathTemplate()
 	 */
 	@Override
 	public String getUriPathTemplate() {
 		return uriPathTemplate;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.IJaxrsEndpoint #getConsumedMediaTypes()
 	 */
 	@Override
 	public List<String> getConsumedMediaTypes() {
 		return consumedMediaTypes;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.IJaxrsEndpoint #getProducedMediaTypes()
 	 */
 	@Override
 	public List<String> getProducedMediaTypes() {
 		return producedMediaTypes;
 	}
 
 	@Override
 	public IJavaProject getJavaProject() {
 		return this.httpMethod.getJavaElement().getJavaProject();
 	}
 
 }
