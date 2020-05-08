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
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder;
 
 import static org.eclipse.jdt.core.IJavaElementDelta.ADDED;
 import static org.eclipse.jdt.core.IJavaElementDelta.CHANGED;
 import static org.eclipse.jdt.core.IJavaElementDelta.REMOVED;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isOneOf;
 import static org.jboss.tools.ws.jaxrs.core.WorkbenchUtils.getAnnotation;
 import static org.jboss.tools.ws.jaxrs.core.WorkbenchUtils.getMethod;
 import static org.jboss.tools.ws.jaxrs.core.WorkbenchUtils.getType;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_ELEMENT_KIND;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_PATH_VALUE;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.spy;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.ApplicationPath;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.HttpMethod;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.jboss.tools.ws.jaxrs.core.AbstractCommonTestCase;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsEndpoint;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsJavaApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsMetamodel;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResource;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceMethod.Builder;
 import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodParameter;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodSignature;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumKind;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsEndpoint;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsEndpointDelta;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelDelta;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class JaxrsMetamodelChangedProcessorTestCase extends AbstractCommonTestCase {
 
 	private JaxrsMetamodel metamodel;
 
 	private final JaxrsMetamodelChangedProcessor delegate = new JaxrsMetamodelChangedProcessor();
 
 	private static final IProgressMonitor progressMonitor = new NullProgressMonitor();
 
 	@Before
 	public void setup() throws CoreException {
 		metamodel = spy(JaxrsMetamodel.create(javaProject));
 	}
 
 	private JaxrsJavaApplication createApplication(String typeName) throws CoreException, JavaModelException {
 		final IType applicationType = getType(typeName, javaProject);
 		final JaxrsJavaApplication application = new JaxrsJavaApplication(applicationType,
 				getAnnotation(applicationType, ApplicationPath.class), metamodel);
 		metamodel.add(application);
 		return application;
 	}
 	
 	private JaxrsResource createResource(String typeName) throws CoreException, JavaModelException {
 		final IType resourceType = getType(typeName, javaProject);
 		final JaxrsResource resource = new JaxrsResource.Builder(resourceType, metamodel).pathTemplate(
 				getAnnotation(resourceType, Path.class)).build();
 		metamodel.add(resource);
 		return resource;
 	}
 
 	private JaxrsResourceMethod createResourceMethod(String methodName, JaxrsResource parentResource,
 			Class<?> httpMethod) throws CoreException, JavaModelException {
 		final IType javaType = parentResource.getJavaElement();
 		final ICompilationUnit compilationUnit = javaType.getCompilationUnit();
 		final IMethod javaMethod = getMethod(javaType, methodName);
 		final JavaMethodSignature methodSignature = JdtUtils.resolveMethodSignature(javaMethod,
 				JdtUtils.parse(compilationUnit, progressMonitor));
 
 		final Builder builder = new JaxrsResourceMethod.Builder(javaMethod, (JaxrsResource) parentResource, metamodel)
 				.httpMethod(getAnnotation(javaMethod, httpMethod)).pathTemplate(getAnnotation(javaMethod, Path.class))
 				.returnType(methodSignature.getReturnedType());
 		for (JavaMethodParameter methodParam : methodSignature.getMethodParameters()) {
 			builder.methodParameter(methodParam);
 		}
 		final JaxrsResourceMethod resourceMethod = builder.build();
 		metamodel.add(resourceMethod);
 		return resourceMethod;
 	}
 
 	private JaxrsHttpMethod createHttpMethod(Class<?> annotationClass) throws JavaModelException, CoreException {
 		final IType type = getType(annotationClass.getName(), javaProject);
 		final Annotation httpAnnotation = getAnnotation(type, HttpMethod.class);
 		final JaxrsHttpMethod httpMethod = new JaxrsHttpMethod(type, httpAnnotation, metamodel);
 		metamodel.add(httpMethod);
 		return httpMethod;
 	}
 
 	private JaxrsEndpoint createEndpoint(JaxrsMetamodel metamodel, JaxrsHttpMethod httpMethod, JaxrsResourceMethod... resourceMethods) {
 		JaxrsEndpoint endpoint = new JaxrsEndpoint(metamodel, httpMethod, new LinkedList<JaxrsResourceMethod>(
 				Arrays.asList(resourceMethods)));
 		metamodel.add(endpoint);
 		return endpoint;
 	}
 
 	private JaxrsEndpoint createEndpoint(JaxrsHttpMethod httpMethod, JaxrsResourceMethod... resourceMethods) {
 		JaxrsEndpoint endpoint = new JaxrsEndpoint(null, httpMethod, new LinkedList<JaxrsResourceMethod>(
 				Arrays.asList(resourceMethods)));
 		metamodel.add(endpoint);
 		return endpoint;
 	}
 
 	private List<JaxrsEndpointDelta> processEvent(JaxrsElementDelta affectedElement,
 			IProgressMonitor progressMonitor) {
 		JaxrsMetamodelDelta affectedMetamodel = new JaxrsMetamodelDelta(metamodel, CHANGED);
 		affectedMetamodel.add(affectedElement);
 		delegate.processAffectedMetamodel(affectedMetamodel, progressMonitor);
 		return affectedMetamodel.getAffectedEndpoints();
 	}
 
 	@Test
 	public void shouldConstructSimpleEndpoint() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(), Consumes.class));
 		customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(), Produces.class));
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomerAsVCard", customerResource,
 				GET.class);
 		customerResourceMethod.addOrUpdateAnnotation(getAnnotation(customerResourceMethod.getJavaElement(),
 				Produces.class));
 		// operation
 		JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		// verifications
 		assertThat(endpoint.getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// @produces and @consumes annotations were explicitly declared
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("text/x-vcard")));
 	}
 
 	@Test
 	public void shouldConstructEndpointFromSubresource() throws CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource producLocatorResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productLocatorMethod = createResourceMethod("getProductResourceLocator",
 				producLocatorResource, GET.class);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod subresourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		// operation
 		JaxrsEndpoint endpoint = createEndpoint(httpMethod, productLocatorMethod, subresourceMethod);
 		// verifications
 		assertThat(endpoint.getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/products/{productType}/{id}"));
 		// @produces and @consumes annotations were not declared in the setup(),
 		// default values should be set
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("*/*")));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("*/*")));
 	}
 
 	@Test
 	public void shouldConstructEndpointWithQueryParams() throws CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomers", customerResource,
 				GET.class);
 		// operation
 		JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		// verifications
 		assertThat(endpoint.getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers?start={int}&size={int:2}"));
 	}
 
 	@Test
 	public void shoudCreateEndpointWhenAddingResourceMethodInRootResource() throws CoreException {
 		// pre-conditions
 		createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomers", customerResource,
 				GET.class);
 		// operation
 		JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, ADDED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 	}
 
 	@Test
 	public void shoudCreateEndpointWhenAddingSubresourceMethodInRootResource() throws JavaModelException, CoreException {
 		// pre-conditions
 		createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerSubresourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		// operation
 		JaxrsElementDelta event = new JaxrsElementDelta(customerSubresourceMethod, ADDED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 	}
 
 	@Test
 	public void shoudCreateEndpointWhenAddingSubresourceLocatorMethodInRootResource() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		createHttpMethod(GET.class);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		createResourceMethod("getProduct", bookResource, GET.class);
 		// createEndpoint(httpMethod, bookResourceMethod);
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		createResourceMethod("getProduct", gameResource, GET.class);
 		// createEndpoint(httpMethod, gameResourceMethod);
 
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		// operation
 		JaxrsElementDelta event = new JaxrsElementDelta(productResourceLocatorMethod, ADDED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 		assertThat(changes.get(1).getDeltaKind(), equalTo(ADDED));
 	}
 
 	@Test
 	public void shoudCreateEndpointWhenAddingResourceMethodInSubresource() throws JavaModelException, CoreException {
 		// pre-conditions
 		createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		createResourceMethod("getProductResourceLocator", productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getAllProducts", bookResource, GET.class);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(bookResourceMethod, ADDED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 	}
 
 	@Test
 	public void shoudCreateEndpointWhenChangingSubresourceLocatorMethodIntoSubresourceMethod()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerSubresourceMethod = createResourceMethod("getCustomer", customerResource,
 				null);
 		assertThat(customerSubresourceMethod.getKind(), equalTo(EnumKind.SUBRESOURCE_LOCATOR));
 		// operation
 		Annotation httpAnnotation = getAnnotation(customerSubresourceMethod.getJavaElement(), GET.class);
 		final int flags = customerSubresourceMethod.addOrUpdateAnnotation(httpAnnotation);
 		JaxrsElementDelta event = new JaxrsElementDelta(customerSubresourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 
 	}
 
 	@Test
 	public void shoudCreateEndpointWhenAddingSubresourceMethodInSubresource() throws JavaModelException, CoreException {
 		// pre-conditions
 		createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		createResourceMethod("getProductResourceLocator", productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(bookResourceMethod, ADDED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 	}
 
 	@Test
 	@Ignore("deferred for now")
 	public void shoudCreateEndpointWhenAddingSubresourceLocatorMethodInSubresource() {
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenAddingApplication() throws JavaModelException, CoreException {
 		// the subresource becomes a root resource !
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final JaxrsJavaApplication application = createApplication("org.jboss.tools.ws.jaxrs.sample.services.RestApplication");
 		final JaxrsElementDelta event = new JaxrsElementDelta(application, ADDED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(CHANGED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(changes.get(0).getEndpoint().getUriPathTemplate(), equalTo("/app/customers/{id}"));
 	}
 	
 	@Test
 	public void shoudChangeUriPathTemplateWhenAddingResourcePathAnnotation() throws JavaModelException, CoreException {
 		// the subresource becomes a root resource !
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomers", customerResource,
 				GET.class);
 		final JaxrsEndpoint fakeEndpoint = createEndpoint(httpMethod, customerResourceMethod);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, F_ELEMENT_KIND
 				+ F_PATH_VALUE);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(REMOVED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) fakeEndpoint));
 		assertThat(changes.get(1).getDeltaKind(), equalTo(ADDED));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenAddingMethodPathAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Path.class);
 		customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers"));
 		// operation
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED,
 				F_PATH_VALUE);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(change.getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(change.getEndpoint().getUriPathTemplate(), equalTo("/customers/{id}"));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenChangingApplicationPathAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsJavaApplication application = createApplication("org.jboss.tools.ws.jaxrs.sample.services.RestApplication");
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(metamodel, httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/app/customers/{id}"));
 		// operation
 		final Annotation annotation = getAnnotation(application.getJavaElement(), ApplicationPath.class, "/foo");
 		int flags = application.addOrUpdateAnnotation(annotation);
 		final JaxrsElementDelta event = new JaxrsElementDelta(application, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(change.getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(change.getEndpoint().getUriPathTemplate(), equalTo("/foo/customers/{id}"));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenChangingResourcePathAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final Annotation annotation = getAnnotation(customerResource.getJavaElement(), Path.class, "/foo");
 		customerResource.addOrUpdateAnnotation(annotation);
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, F_PATH_VALUE);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(change.getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(change.getEndpoint().getUriPathTemplate(), equalTo("/foo/{id}"));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenChangingMethodPathAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Path.class, "{foo}");
 		final int flags = customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(change.getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(change.getEndpoint().getUriPathTemplate(), equalTo("/customers/{foo}"));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenRemovingResourcePathAnnotationAndMatchingSubresourceLocatorFound()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		// the subresource locator that will match the resourcemethod when the
 		// rootresource becomes a subresource
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		createResourceMethod("getProductResourceLocator", productResourceLocator, null);
 		// the root resource that will become a subresource
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final Annotation annotation = getAnnotation(customerResource.getJavaElement(), Path.class);
 		final int flags = customerResource.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		final JaxrsEndpointDelta change1 = changes.get(0);
 		assertThat(change1.getDeltaKind(), equalTo(REMOVED));
 		assertThat(change1.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		final JaxrsEndpointDelta change2 = changes.get(1);
 		assertThat(change2.getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(change2.getEndpoint().getUriPathTemplate(), equalTo("/products/{productType}/{id}"));
 	}
 
 	@Test
 	public void shoudChangeHttpVerbWhenChangingHttpMethodAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		int flags = httpMethod
 				.addOrUpdateAnnotation(getAnnotation(httpMethod.getJavaElement(), HttpMethod.class, "BAR"));
 		final JaxrsElementDelta event = new JaxrsElementDelta(httpMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(CHANGED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(changes.get(0).getEndpoint().getHttpMethod().getHttpVerb(), equalTo("BAR"));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenRemovingMetamodelApplication() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsJavaApplication application = createApplication("org.jboss.tools.ws.jaxrs.sample.services.RestApplication");
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(metamodel, httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/app/customers/{id}"));
 		// operation : no 'application' left in the metamodel
 		metamodel.remove(application);
 		final JaxrsElementDelta event = new JaxrsElementDelta(application, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(CHANGED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(changes.get(0).getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(changes.get(0).getEndpoint().getUriPathTemplate(), equalTo("/customers/{id}"));
 		assertThat(metamodel.getAllApplications().size(), equalTo(0));
 	}
 
 	@Test
 	public void shoudChangeUriPathTemplateWhenRemovingMethodPathAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Path.class);
 		final int flags = customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(REMOVED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(changes.get(1).getDeltaKind(), equalTo(ADDED));
 		assertThat(changes.get(1).getEndpoint().getHttpMethod(), equalTo((IJaxrsHttpMethod) httpMethod));
 		assertThat(changes.get(1).getEndpoint().getUriPathTemplate(), equalTo("/customers"));
 	}
 
 	@Test
 	public void shoudChangeConsumedMediatypesWhenAddingResourceAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("*/*")));
 		// operation
 		final int flags = customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(),
 				Consumes.class));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeConsumedMediatypesWhenAddingResourceMethodAnnotation() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("*/*")));
 		// operation
 		final int flags = customerResourceMethod.addOrUpdateAnnotation(getAnnotation(
 				customerResourceMethod.getJavaElement(), Consumes.class));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeConsumedMediatypesWhenChangingResourceMethodAnnotation() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Consumes.class,
 				"application/foo");
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResourceMethod.addOrUpdateAnnotation(getAnnotation(customerResourceMethod.getJavaElement(),
 				Consumes.class));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeConsumedMediatypesWhenChangingResourceAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final Annotation annotation = getAnnotation(customerResource.getJavaElement(), Consumes.class,
 				"application/foo");
 		customerResource.addOrUpdateAnnotation(annotation);
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(),
 				Consumes.class));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeConsumedMediatypesWhenRemovingMethodAnnotationWithResourceDefault()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(), Consumes.class,
 				"application/xml"));
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Consumes.class,
 				"application/foo");
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeConsumedMediatypesWhenRemovingMethodAnnotationWithoutResourceDefault()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Consumes.class,
 				"application/foo");
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("*/*")));
 	}
 
 	@Test
 	public void shoudChangeProducedMediatypesWhenAddingResourceAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomerAsVCard", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("*/*")));
 		// operation
 		final int flags = customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(),
 				Produces.class, "application/xml"));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeProducedMediatypesWhenAddingResourceMethodAnnotation() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomerAsVCard", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("*/*")));
 		// operation
 		final int flags = customerResourceMethod.addOrUpdateAnnotation(getAnnotation(
 				customerResourceMethod.getJavaElement(), Produces.class));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("text/x-vcard")));
 	}
 
 	@Test
 	public void shoudChangeProducedMediatypesWhenChangingResourceAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final Annotation annotation = getAnnotation(customerResource.getJavaElement(), Produces.class,
 				"application/foo");
 		customerResource.addOrUpdateAnnotation(annotation);
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomerAsVCard", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(),
 				Produces.class, "application/xml"));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeProducedMediatypesWhenChangingResourceMethodAnnotation() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomerAsVCard", customerResource,
 				POST.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Produces.class,
 				"application/foo");
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResourceMethod.addOrUpdateAnnotation(getAnnotation(customerResourceMethod.getJavaElement(),
 				Produces.class));
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("text/x-vcard")));
 	}
 
 	@Test
 	public void shoudChangeProducedMediatypesWhenRemovingMethodAnnotationWithResourceDefault()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		customerResource.addOrUpdateAnnotation(getAnnotation(customerResource.getJavaElement(), Produces.class,
 				"application/xml"));
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomerAsVCard", customerResource,
 				POST.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Produces.class,
 				"application/foo");
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("application/xml")));
 	}
 
 	@Test
 	public void shoudChangeProducedMediatypesWhenRemovingMethodAnnotationWithoutResourceDefault()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(POST.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("createCustomer", customerResource,
 				POST.class);
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Consumes.class,
 				"application/foo");
 		customerResourceMethod.addOrUpdateAnnotation(annotation);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getConsumedMediaTypes(), equalTo(Arrays.asList("application/foo")));
 		// operation
 		int flags = customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(CHANGED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 		assertThat(endpoint.getProducedMediaTypes(), equalTo(Arrays.asList("*/*")));
 	}
 
 	@Test
 	public void shoudRemoveEndpointWhenRemovingHttpMethodAnnotation() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(httpMethod, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(REMOVED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 	}
 
 	@Test
 	public void shoudRemoveEndpointWhenRemovingResourcePathAnnotationAndMatchingSubresourceLocatorNotFound()
 			throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		assertThat(endpoint.getUriPathTemplate(), equalTo("/customers/{id}"));
 		// operation
 		final Annotation annotation = getAnnotation(customerResource.getJavaElement(), Path.class);
 		final int flags = customerResource.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(REMOVED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 	}
 
 	@Test
 	public void shoudRemoveEndpointsWhenRemovingRootResource() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod1 = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint1 = createEndpoint(httpMethod, customerResourceMethod1);
 		final JaxrsResourceMethod customerResourceMethod2 = createResourceMethod("getCustomers", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint2 = createEndpoint(httpMethod, customerResourceMethod2);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResource, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		for (JaxrsEndpointDelta change : changes) {
 			assertThat(change.getDeltaKind(), equalTo(REMOVED));
 			assertThat(change.getEndpoint(), isOneOf((IJaxrsEndpoint) endpoint1, (IJaxrsEndpoint) endpoint2));
 		}
 	}
 
 	@Test
 	public void shoudRemoveEndpointsWhenRemovingSubresource() throws JavaModelException, CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		final JaxrsEndpoint bookEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, bookResourceMethod);
 		// adding an extra endpoint that shouldn't be affected
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		createEndpoint(httpMethod, gameResourceMethod);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(bookResource, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(REMOVED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) bookEndpoint));
 	}
 
 	@Test
 	public void shoudRemoveEndpointsWhenRemovingHttpMethod() throws JavaModelException, CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		createResourceMethod("getProductResourceLocator", productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		final JaxrsEndpoint bookEndpoint = createEndpoint(httpMethod, bookResourceMethod);
 		// adding an extra endpoint that shouldn't be affected
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		createEndpoint(httpMethod, gameResourceMethod);
 		final Annotation httpAnnotation = bookResourceMethod.getHttpMethodAnnotation();
 		final int flags = bookResourceMethod.removeAnnotation(httpAnnotation.getJavaAnnotation().getHandleIdentifier());
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(bookResourceMethod, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(REMOVED));
 		assertThat(changes.get(0).getEndpoint(), equalTo((IJaxrsEndpoint) bookEndpoint));
 	}
 
 	@Test
 	public void shoudAddEndpointsWhenChangingSubresourceLocatorReturnType() throws JavaModelException, CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		productResourceLocatorMethod.update(new JavaMethodSignature(productResourceLocatorMethod.getJavaElement(),
 				bookResource.getJavaElement(), productResourceLocatorMethod.getJavaMethodParameters()));
 		createEndpoint(httpMethod, productResourceLocatorMethod, bookResourceMethod);
 		// adding an extra subresource that should be affected later
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		assertThat(metamodel.getAllEndpoints().size(), equalTo(1));
 		// operation
 		final IType objectType = JdtUtils.resolveType(Object.class.getName(), javaProject, progressMonitor);
 		int flags = productResourceLocatorMethod.update(new JavaMethodSignature(productResourceLocatorMethod
 				.getJavaElement(), objectType, productResourceLocatorMethod.getJavaMethodParameters()));
 		final JaxrsElementDelta event = new JaxrsElementDelta(productResourceLocatorMethod, CHANGED,
 				flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(ADDED));
 		assertThat(changes.get(0).getEndpoint().getResourceMethods().contains(gameResourceMethod), is(true));
 		assertThat(metamodel.getAllEndpoints().size(), equalTo(2));
 	}
 
 	@Test
 	public void shoudRemoveEndpointsWhenChangingSubresourceLocatorReturnType() throws JavaModelException, CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		createEndpoint(httpMethod, productResourceLocatorMethod, bookResourceMethod);
 		// adding an extra subresource that should be affected later
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		createEndpoint(httpMethod, productResourceLocatorMethod, gameResourceMethod);
 		assertThat(metamodel.getAllEndpoints().size(), equalTo(2));
 		// operation
 		final IType bookResourceType = bookResource.getJavaElement();
 		int flags = productResourceLocatorMethod.update(new JavaMethodSignature(productResourceLocatorMethod
 				.getJavaElement(), bookResourceType, productResourceLocatorMethod.getJavaMethodParameters()));
 		final JaxrsElementDelta event = new JaxrsElementDelta(productResourceLocatorMethod, CHANGED,
 				flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		assertThat(changes.get(0).getDeltaKind(), equalTo(REMOVED));
 		assertThat(changes.get(0).getEndpoint().getResourceMethods().contains(gameResourceMethod), is(true));
 		assertThat(metamodel.getAllEndpoints().size(), equalTo(1));
 
 	}
 
 	@Test
 	public void shoudRemoveEndpointsWhenRemovingSubresourceLocatorResource() throws JavaModelException, CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		final JaxrsEndpoint bookEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, bookResourceMethod);
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		final JaxrsEndpoint gameEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, gameResourceMethod);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(productResourceLocator, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		for (JaxrsEndpointDelta change : changes) {
 			assertThat(change.getDeltaKind(), equalTo(REMOVED));
 			assertThat(change.getEndpoint(), isOneOf((IJaxrsEndpoint) bookEndpoint, (IJaxrsEndpoint) gameEndpoint));
 		}
 	}
 
 	@Test
 	public void shoudRemoveEndpointWhenRemovingResourceMethodInRootResource() throws JavaModelException, CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomers", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(REMOVED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 	}
 
 	@Test
 	public void shoudRemoveEndpointWhenRemovingSubresourceMethodInRootResource() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		final JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource customerResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.CustomerResource");
 		final JaxrsResourceMethod customerResourceMethod = createResourceMethod("getCustomer", customerResource,
 				GET.class);
 		final JaxrsEndpoint endpoint = createEndpoint(httpMethod, customerResourceMethod);
 		// operation
 		final Annotation annotation = getAnnotation(customerResourceMethod.getJavaElement(), Path.class);
 		final int flags = customerResourceMethod.removeAnnotation(annotation.getJavaAnnotation().getHandleIdentifier());
 		final JaxrsElementDelta event = new JaxrsElementDelta(customerResourceMethod, REMOVED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(1));
 		final JaxrsEndpointDelta change = changes.get(0);
 		assertThat(change.getDeltaKind(), equalTo(REMOVED));
 		assertThat(change.getEndpoint(), equalTo((IJaxrsEndpoint) endpoint));
 	}
 
 	@Test
 	public void shoudRemoveEndpointWhenRemovingSubresourceLocatorMethod() throws JavaModelException, CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		final JaxrsEndpoint bookEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, bookResourceMethod);
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		final JaxrsEndpoint gameEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, gameResourceMethod);
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(productResourceLocatorMethod, REMOVED);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		for (JaxrsEndpointDelta change : changes) {
 			assertThat(change.getDeltaKind(), equalTo(REMOVED));
 			assertThat(change.getEndpoint(), isOneOf((IJaxrsEndpoint) bookEndpoint, (IJaxrsEndpoint) gameEndpoint));
 		}
 	}
 
 	@Test
 	public void shoudRemoveEndpointWhenSubresourceLocatorRootResourceBecomesSubresource() throws JavaModelException,
 			CoreException {
 		// pre-conditions
 		JaxrsHttpMethod httpMethod = createHttpMethod(GET.class);
 		final JaxrsResource productResourceLocator = createResource("org.jboss.tools.ws.jaxrs.sample.services.ProductResourceLocator");
 		final JaxrsResourceMethod productResourceLocatorMethod = createResourceMethod("getProductResourceLocator",
 				productResourceLocator, null);
 		final JaxrsResource bookResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.BookResource");
 		final JaxrsResourceMethod bookResourceMethod = createResourceMethod("getProduct", bookResource, GET.class);
 		final JaxrsEndpoint bookEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, bookResourceMethod);
 		final JaxrsResource gameResource = createResource("org.jboss.tools.ws.jaxrs.sample.services.GameResource");
 		final JaxrsResourceMethod gameResourceMethod = createResourceMethod("getProduct", gameResource, GET.class);
 		final JaxrsEndpoint gameEndpoint = createEndpoint(httpMethod, productResourceLocatorMethod, gameResourceMethod);
 		final Annotation productResourceLocatorPathAnnotation = getAnnotation(productResourceLocator.getJavaElement(),
 				Path.class);
 		final int flags = productResourceLocator.removeAnnotation(productResourceLocatorPathAnnotation
 				.getJavaAnnotation().getHandleIdentifier());
 		// operation
 		final JaxrsElementDelta event = new JaxrsElementDelta(productResourceLocator, CHANGED, flags);
 		final List<JaxrsEndpointDelta> changes = processEvent(event, progressMonitor);
 		// verifications
 		assertThat(changes.size(), equalTo(2));
 		for (JaxrsEndpointDelta change : changes) {
 			assertThat(change.getDeltaKind(), equalTo(REMOVED));
 			assertThat(change.getEndpoint(), isOneOf((IJaxrsEndpoint) bookEndpoint, (IJaxrsEndpoint) gameEndpoint));
 		}
 	}
 }
