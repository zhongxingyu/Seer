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
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_ELEMENT_KIND;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsElementDelta.F_FINE_GRAINED;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.ws.rs.core.Application;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsElementFactory;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsJavaApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsMetamodel;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResource;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceField;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsWebxmlApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.CollectionUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.WtpUtils;
 import org.jboss.tools.ws.jaxrs.core.jdt.CompilationUnitsRepository;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodSignature;
 import org.jboss.tools.ws.jaxrs.core.jdt.JaxrsAnnotationsScanner;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumKind;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsApplication;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelDelta;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelLocator;
 
 public class ResourceChangedProcessor {
 
 	private final JaxrsElementFactory factory = new JaxrsElementFactory();
 
 	/**
 	 * Process the given project's resources changes.
 	 * 
 	 * @param project
 	 *            the project on which resource changes occurred
 	 * @param withReset
 	 *            true if the existing metamodel be reset (after clean/full builds)
 	 * @param events
 	 *            the resource changes that are relevant to the JAX-RS Metamodel.
 	 * @param progressMonitor
 	 * @return a JaxrsMetamodelChangedEvent containing information about the change at the metamodel level
 	 *         (added/updated) and its inner changes (or empty list if the metamodel is empty).
 	 * @throws CoreException
 	 */
 	public JaxrsMetamodelDelta processAffectedResources(final IProject project, final boolean withReset,
 			final List<ResourceDelta> events, final IProgressMonitor progressMonitor) throws CoreException {
 		JaxrsMetamodel metamodel = JaxrsMetamodelLocator.get(project);
 		if (metamodel == null) {
 			return processEntireProject(project, ADDED, progressMonitor);
 		} else if (withReset) {
 			return processEntireProject(project, CHANGED, progressMonitor);
 		} else {
 			return processAffectedResources(events, metamodel, progressMonitor);
 		}
 	}
 
 	/**
 	 * Process the entire project since there was no metamodel yet for it.
 	 * 
 	 * @param project
 	 *            the project
 	 * @param deltaKind
 	 * @param progressMonitor
 	 *            the progress monitor
 	 * @return the metamodel along with all the JAX-RS elements.
 	 * @throws CoreException
 	 */
 	private JaxrsMetamodelDelta processEntireProject(IProject project, int deltaKind, IProgressMonitor progressMonitor)
 			throws CoreException {
 		// start with a fresh new metamodel
 		final JaxrsMetamodel metamodel = JaxrsMetamodel.create(JavaCore.create(project));
 		final JaxrsMetamodelDelta metamodelDelta = new JaxrsMetamodelDelta(metamodel, deltaKind);
 		try {
 			progressMonitor.beginTask("Processing Project '" + project.getName() + "'...", 1);
 			Logger.debug("Processing Project '" + project.getName() + "'...");
 			metamodelDelta.addAll(processEvent(new ResourceDelta(project, ADDED, 0), progressMonitor));
 			if(WtpUtils.hasWebDeploymentDescriptor(project)) {
 				processEvent(new ResourceDelta(WtpUtils.getWebDeploymentDescriptor(project), ADDED, 0), progressMonitor);
 			}
 			progressMonitor.worked(1);
 		} catch (CoreException e) {
 			Logger.error("Failed while processing Resource results", e);
 		} finally {
 			progressMonitor.done();
 			Logger.debug("Done processing Resource results.");
 
 		}
 
 		return metamodelDelta;
 	}
 
 	/**
 	 * Process the project resource that changed.
 	 * 
 	 * @param affectedResources
 	 *            the affected resources, all in the same project
 	 * @param metamodel
 	 *            the metamodel associated with the resources' project
 	 * @param progressMonitor
 	 *            the progress monitor
 	 * @return the affectedMetamodel containing the affected (JAX-RS) Elements
 	 */
 	private JaxrsMetamodelDelta processAffectedResources(final List<ResourceDelta> affectedResources,
 			JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) {
 		final List<JaxrsElementDelta> elementChanges = new ArrayList<JaxrsElementDelta>();
 		final JaxrsMetamodelDelta metamodelDelta = new JaxrsMetamodelDelta(metamodel, CHANGED, elementChanges);
 		// process resource changes
 		try {
 			progressMonitor.beginTask("Processing Resource " + affectedResources.size() + " change(s)...",
 					affectedResources.size());
 			Logger.debug("Processing {} Resource change(s)...", affectedResources.size());
 			for (ResourceDelta event : affectedResources) {
 				elementChanges.addAll(processEvent(event, progressMonitor));
 				progressMonitor.worked(1);
 			}
 
 		} catch (CoreException e) {
 			Logger.error("Failed while processing Resource results", e);
 			elementChanges.clear();
 		} finally {
 			progressMonitor.done();
 			Logger.debug("Done processing Resource results.");
 
 		}
 		return metamodelDelta;
 	}
 
 	public List<JaxrsElementDelta> processEvent(ResourceDelta event, IProgressMonitor progressMonitor)
 			throws CoreException {
 		Logger.debug("Processing {}", event);
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final IResource resource = event.getResource();
 		if (resource == null) {
 			return results;
 		}
 		final IJavaElement scope = JavaCore.create(resource);
 		final JaxrsMetamodel metamodel = JaxrsMetamodelLocator.get(resource.getProject());
 		final int deltaKind = event.getDeltaKind();
 		if (scope != null) {
 			switch (deltaKind) {
 			case ADDED:
 			case CHANGED:
 				results.addAll(processApplicationChangesOnScopeAdditionOrChange(scope, metamodel, progressMonitor));
 				results.addAll(processHttpMethodChangesOnScopeAdditionOrChange(scope, metamodel, progressMonitor));
 				results.addAll(processResourceChangesOnScopeAdditionOrChange(scope, metamodel, deltaKind,
 						progressMonitor));
 				break;
 			case REMOVED:
 				results.addAll(processApplicationChangesOnScopeRemoval(scope, metamodel, progressMonitor));
 				results.addAll(processHttpMethodChangesOnScopeRemoval(scope, metamodel, progressMonitor));
 				results.addAll(processResourceChangesOnScopeRemoval(scope, metamodel, progressMonitor));
 				break;
 			}
 		} else if (WtpUtils.isWebDeploymentDescriptor(resource)) {
 			switch (deltaKind) {
 			case ADDED:
 			case CHANGED:
 				results.addAll(processApplicationChangesOnWebxmlAdditionOrChange(resource, metamodel, progressMonitor));
 				break;
 			case REMOVED:
 				results.addAll(processApplicationChangesOnWebxmlRemoval(resource, metamodel, progressMonitor));
 				break;
 			}
 		}
 
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processApplicationChangesOnWebxmlAdditionOrChange(IResource resource,
 			JaxrsMetamodel metamodel, IProgressMonitor progressMonitor) throws CoreException {
 		final IType applicationType = JdtUtils.resolveType(Application.class.getName(), metamodel.getJavaProject(),
 				progressMonitor);
 		// occurs when the project has the jax-rs nature (the builder is called), but no jaxrs library is in the classpath
 		if(applicationType == null) {
 			return Collections.emptyList();
 		}
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final ITypeHierarchy applicationTypeHierarchy = JdtUtils.resolveTypeHierarchy(applicationType, false,
 				progressMonitor);
 		final IType[] applicationSubclasses = applicationTypeHierarchy.getSubclasses(applicationType);
 		// add the standard Application type in last position in the hierarchy
 		final IType[] applicationClasses = new IType[applicationSubclasses.length + 1];
 		System.arraycopy(applicationSubclasses, 0, applicationClasses, 0, applicationSubclasses.length);
 		applicationClasses[applicationClasses.length -1] = applicationType;
 		String resolvedApplicationPath = null;
 		for (IType applicationClass : applicationClasses) {
 			final String applicationPath = WtpUtils.getApplicationPath(resource.getProject(),
 					applicationClass.getFullyQualifiedName());
 			if (applicationPath != null) {
 				resolvedApplicationPath = applicationPath;
 				break;
 			}
 		}
 		final IJaxrsApplication application = metamodel.getApplication();
 		if (resolvedApplicationPath != null) {
 			final JaxrsWebxmlApplication webxmlApplication = factory.createApplication(resolvedApplicationPath, resource, metamodel);
 			if (application == null || application.getKind() == EnumKind.APPLICATION_JAVA) {
 				metamodel.add(webxmlApplication);
 				results.add(new JaxrsElementDelta(webxmlApplication, ADDED));
 			} else if (application != null && application.getKind() == EnumKind.APPLICATION_WEBXML) {
 				int flags = webxmlApplication.update(webxmlApplication);
 				results.add(new JaxrsElementDelta(webxmlApplication, CHANGED, flags));
 			}
 		} else if(application != null && application.getKind() == EnumKind.APPLICATION_WEBXML){
 			final JaxrsWebxmlApplication webxmlApplication = (JaxrsWebxmlApplication) application;
 			metamodel.remove(webxmlApplication);
 			results.add(new JaxrsElementDelta(webxmlApplication, REMOVED));
 		}
 		// otherwise, do nothing (application path not declared in web.xml, or not valid but can't be discovered)
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processApplicationChangesOnWebxmlRemoval(IResource resource,
 			JaxrsMetamodel metamodel, IProgressMonitor progressMonitor) {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		for (Iterator<IJaxrsApplication> iterator = metamodel.getAllApplications().iterator(); iterator.hasNext();) {
 			IJaxrsApplication application = (IJaxrsApplication) iterator.next();
 			if(application instanceof JaxrsWebxmlApplication) {
 				Logger.debug("Removing {}", application);
 				final JaxrsWebxmlApplication webxmlApplication = (JaxrsWebxmlApplication)application;
 				metamodel.remove(webxmlApplication);
 				results.add(new JaxrsElementDelta(webxmlApplication, REMOVED));
 			}
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processApplicationChangesOnScopeAdditionOrChange(IJavaElement scope,
 			JaxrsMetamodel metamodel, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final List<JaxrsElementDelta> changes = preprocessApplicationChangesOnScopeAdditionOrChange(scope, metamodel,
 				progressMonitor);
 		for (JaxrsElementDelta change : changes) {
 			results.addAll(postProcessApplication(change, progressMonitor));
 		}
 		return results;
 	}
 
 	/**
 	 * See if Applications exist in the given scope. The exact kind of the {@link JaxrsElementDelta} event is not
 	 * determined at this stage, it's the responsibility of the
 	 * {@link ResourceChangedProcessor#postProcessApplication(JaxrsElementDelta, IProgressMonitor)} method.
 	 * 
 	 * @param scope
 	 * @param metamodel
 	 * @param progressMonitor
 	 * @return events containing the new Applications (already added to the metamodel)
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	private List<JaxrsElementDelta> preprocessApplicationChangesOnScopeAdditionOrChange(final IJavaElement scope,
 			final JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) throws CoreException,
 			JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		// see if there may be elements to add/change from the given scope
 		final List<JaxrsJavaApplication> matchingApplications = new ArrayList<JaxrsJavaApplication>();
 		final List<IType> matchingApplicationTypes = JaxrsAnnotationsScanner.findApplicationTypes(scope,
 				progressMonitor);
 		for (IType matchingApplicationType : matchingApplicationTypes) {
 			final CompilationUnit ast = JdtUtils.parse(matchingApplicationType, progressMonitor);
 			final JaxrsJavaApplication matchingApplication = factory.createApplication(matchingApplicationType, ast,
 					metamodel);
 			if (matchingApplication != null) {
 				matchingApplications.add(matchingApplication);
 			}
 		}
 		// retrieve the existing elements from the Metamodel
 		final List<JaxrsJavaApplication> existingApplications = metamodel
 				.getElements(scope, JaxrsJavaApplication.class);
 		// compute the differences, with a 'fuzzy' case when the kind is
 		// 'changed'
 		final Collection<JaxrsJavaApplication> addedApplications = CollectionUtils.difference(matchingApplications,
 				existingApplications);
 		for (JaxrsJavaApplication application : addedApplications) {
 			results.add(new JaxrsElementDelta(application, ADDED));
 		}
 		final Collection<JaxrsJavaApplication> changedApplications = CollectionUtils.intersection(matchingApplications,
 				existingApplications);
 		for (JaxrsJavaApplication application : changedApplications) {
 			results.add(new JaxrsElementDelta(application, CHANGED, F_FINE_GRAINED));
 		}
 		final Collection<JaxrsJavaApplication> removedApplications = CollectionUtils.difference(existingApplications,
 				matchingApplications);
 		for (JaxrsJavaApplication application : removedApplications) {
 			results.add(new JaxrsElementDelta(application, REMOVED));
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processApplicationChangesOnScopeRemoval(IJavaElement scope,
 			JaxrsMetamodel metamodel, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final List<JaxrsElementDelta> changes = preprocessApplicationChangesOnScopeRemoval(scope, metamodel,
 				progressMonitor);
 		for (JaxrsElementDelta change : changes) {
 			results.addAll(postProcessApplication(change, progressMonitor));
 		}
 		return results;
 	}
 
 	/**
 	 * See if Applications existed in the given scope. These elements can only be marked as removed.
 	 * 
 	 * @param scope
 	 * @param metamodel
 	 * @param progressMonitor
 	 * @return events containing the new Applications (already added to the metamodel)
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	private List<JaxrsElementDelta> preprocessApplicationChangesOnScopeRemoval(final IJavaElement scope,
 			final JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) throws CoreException,
 			JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		// retrieve the existing elements from the Metamodel
 		final List<JaxrsJavaApplication> existingApplications = metamodel
 				.getElements(scope, JaxrsJavaApplication.class);
 		for (JaxrsJavaApplication application : existingApplications) {
 			results.add(new JaxrsElementDelta(application, REMOVED));
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> postProcessApplication(JaxrsElementDelta event, IProgressMonitor progressMonitor) {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final JaxrsJavaApplication eventApplication = (JaxrsJavaApplication) event.getElement();
 		final JaxrsMetamodel metamodel = eventApplication.getMetamodel();
 		switch (event.getDeltaKind()) {
 		case ADDED:
 			metamodel.add(eventApplication);
 			results.add(event);
 			break;
 		case REMOVED:
 			metamodel.remove(eventApplication);
 			results.add(event);
 			break;
 		case CHANGED:
 			final JaxrsJavaApplication existingApplication = metamodel.getElement(eventApplication.getJavaElement(),
 					JaxrsJavaApplication.class);
 			final int flags = existingApplication.update(eventApplication);
 			if (flags != 0) {
 				results.add(new JaxrsElementDelta(existingApplication, CHANGED, flags));
 			}
 			break;
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processHttpMethodChangesOnScopeAdditionOrChange(IJavaElement scope,
 			JaxrsMetamodel metamodel, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final List<JaxrsElementDelta> changes = preprocessHttpMethodChangesOnScopeAdditionOrChange(scope, metamodel,
 				progressMonitor);
 		for (JaxrsElementDelta change : changes) {
 			results.addAll(postProcessHttpMethod(change, progressMonitor));
 		}
 		return results;
 	}
 
 	/**
 	 * See if HttpMethods exist in the given scope. The exact kind of the {@link JaxrsElementDelta} event is not
 	 * determined at this stage, it's the responsibility of the
 	 * {@link ResourceChangedProcessor#postProcessHttpMethod(JaxrsElementDelta, IProgressMonitor)} method.
 	 * 
 	 * @param scope
 	 * @param metamodel
 	 * @param progressMonitor
 	 * @return events containing the new HttpMethods (already added to the metamodel)
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	private List<JaxrsElementDelta> preprocessHttpMethodChangesOnScopeAdditionOrChange(final IJavaElement scope,
 			final JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) throws CoreException,
 			JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		// see if there may be elements to add/change from the given scope
 		final List<JaxrsHttpMethod> matchingHttpMethods = new ArrayList<JaxrsHttpMethod>();
 		final List<IType> matchingHttpMethodTypes = JaxrsAnnotationsScanner.findHttpMethodTypes(scope, progressMonitor);
 		for (IType matchingHttpMethodType : matchingHttpMethodTypes) {
 			final CompilationUnit ast = JdtUtils.parse(matchingHttpMethodType, progressMonitor);
 			final JaxrsHttpMethod matchingHttpMethod = factory.createHttpMethod(matchingHttpMethodType, ast, metamodel);
 			if (matchingHttpMethod != null) {
 				matchingHttpMethods.add(matchingHttpMethod);
 			}
 		}
 		// retrieve the existing elements from the Metamodel
 		final List<JaxrsHttpMethod> existingHttpMethods = metamodel.getElements(scope, JaxrsHttpMethod.class);
 		// compute the differences, with a 'fuzzy' case when the kind is
 		// 'changed'
 		final Collection<JaxrsHttpMethod> addedHttpMethods = CollectionUtils.difference(matchingHttpMethods,
 				existingHttpMethods);
 		for (JaxrsHttpMethod httpMethod : addedHttpMethods) {
 			results.add(new JaxrsElementDelta(httpMethod, ADDED));
 		}
 		final Collection<JaxrsHttpMethod> changedHttpMethods = CollectionUtils.intersection(matchingHttpMethods,
 				existingHttpMethods);
 		for (JaxrsHttpMethod httpMethod : changedHttpMethods) {
 			results.add(new JaxrsElementDelta(httpMethod, CHANGED, F_FINE_GRAINED));
 		}
 		final Collection<JaxrsHttpMethod> removedHttpMethods = CollectionUtils.difference(existingHttpMethods,
 				matchingHttpMethods);
 		for (JaxrsHttpMethod httpMethod : removedHttpMethods) {
 			results.add(new JaxrsElementDelta(httpMethod, REMOVED));
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processHttpMethodChangesOnScopeRemoval(IJavaElement scope,
 			JaxrsMetamodel metamodel, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final List<JaxrsElementDelta> changes = preprocessHttpMethodChangesOnScopeRemoval(scope, metamodel,
 				progressMonitor);
 		for (JaxrsElementDelta change : changes) {
 			results.addAll(postProcessHttpMethod(change, progressMonitor));
 		}
 		return results;
 	}
 
 	/**
 	 * See if HttpMethods existed in the given scope. These elements can only be marked as removed.
 	 * 
 	 * @param scope
 	 * @param metamodel
 	 * @param progressMonitor
 	 * @return events containing the new HttpMethods (already added to the metamodel)
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	private List<JaxrsElementDelta> preprocessHttpMethodChangesOnScopeRemoval(final IJavaElement scope,
 			final JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) throws CoreException,
 			JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		// retrieve the existing elements from the Metamodel
 		final List<JaxrsHttpMethod> existingHttpMethods = metamodel.getElements(scope, JaxrsHttpMethod.class);
 		for (JaxrsHttpMethod httpMethod : existingHttpMethods) {
 			results.add(new JaxrsElementDelta(httpMethod, REMOVED));
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> postProcessHttpMethod(JaxrsElementDelta event, IProgressMonitor progressMonitor) {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final JaxrsHttpMethod eventHttpMethod = (JaxrsHttpMethod) event.getElement();
 		final JaxrsMetamodel metamodel = eventHttpMethod.getMetamodel();
 		switch (event.getDeltaKind()) {
 		case ADDED:
 			metamodel.add(eventHttpMethod);
 			results.add(event);
 			break;
 		case REMOVED:
 			metamodel.remove(eventHttpMethod);
 			results.add(event);
 			break;
 		case CHANGED:
 			final JaxrsHttpMethod existingHttpMethod = metamodel.getElement(eventHttpMethod.getJavaElement(),
 					JaxrsHttpMethod.class);
 			final int flags = existingHttpMethod.update(eventHttpMethod);
 			if (flags != 0) {
 				results.add(new JaxrsElementDelta(existingHttpMethod, CHANGED, flags));
 			}
 			break;
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processResourceChangesOnScopeAdditionOrChange(IJavaElement scope,
 			JaxrsMetamodel metamodel, int deltaKind, IProgressMonitor progressMonitor) throws JavaModelException,
 			CoreException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final List<JaxrsElementDelta> changes = preprocessResourceChangesOnScopeAdditionOrChange(scope, metamodel,
 				progressMonitor);
 		for (JaxrsElementDelta change : changes) {
 			results.addAll(postProcessResource(change, progressMonitor));
 		}
 		return results;
 	}
 
 	/**
 	 * See if new JAX-RS Resources exist in the given scope. The exact kind of the {@link JaxrsElementDelta} event is
 	 * not determined at this stage, it's the responsibility of the
 	 * {@link ResourceChangedProcessor#postProcessResource(JaxrsElementDelta, IProgressMonitor)} method.
 	 * 
 	 * @param scope
 	 * @param metamodel
 	 * @param progressMonitor
 	 * @return events containing the JAX-RS Resources
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	private List<JaxrsElementDelta> preprocessResourceChangesOnScopeAdditionOrChange(final IJavaElement scope,
 			final JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) throws CoreException,
 			JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		// see if there may be elements to add/change from the given scope
 		final List<JaxrsResource> matchingResources = new ArrayList<JaxrsResource>();
 		final List<IType> matchingResourceTypes = JaxrsAnnotationsScanner.findResourceTypes(scope, progressMonitor);
 		for (IType matchingResourceType : matchingResourceTypes) {
 			final CompilationUnit ast = JdtUtils.parse(matchingResourceType, progressMonitor);
 			final JaxrsResource matchingResource = factory.createResource(matchingResourceType, ast, metamodel);
 			if (matchingResource != null) {
 				matchingResources.add(matchingResource);
 			}
 		}
 		// retrieve the existing elements from the Metamodel
 		final List<JaxrsResource> existingResources = metamodel.getElements(scope, JaxrsResource.class);
 		// compute the differences, with a 'fuzzy' case when the kind is
 		// 'changed'
 		final Collection<JaxrsResource> addedResources = CollectionUtils.difference(matchingResources,
 				existingResources);
 		for (JaxrsResource resource : addedResources) {
 			results.add(new JaxrsElementDelta(resource, ADDED));
 		}
 		final Collection<JaxrsResource> changedResources = CollectionUtils.intersection(matchingResources,
 				existingResources);
 		for (JaxrsResource resource : changedResources) {
 			results.add(new JaxrsElementDelta(resource, CHANGED, F_FINE_GRAINED));
 		}
 		final Collection<JaxrsResource> removedResources = CollectionUtils.difference(existingResources,
 				matchingResources);
 		for (JaxrsResource resource : removedResources) {
 			results.add(new JaxrsElementDelta(resource, REMOVED));
 		}
 
 		return results;
 	}
 
 	private List<JaxrsElementDelta> processResourceChangesOnScopeRemoval(IJavaElement scope, JaxrsMetamodel metamodel,
 			IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final List<JaxrsElementDelta> changes = preprocessResourceChangesOnScopeRemoval(scope, metamodel,
 				progressMonitor);
 		for (JaxrsElementDelta change : changes) {
 			results.addAll(postProcessResource(change, progressMonitor));
 		}
 		return results;
 	}
 
 	/**
 	 * See if JAX-RS Resources existed in the given scope. These elements can only be marked as removed.
 	 * 
 	 * @param scope
 	 * @param metamodel
 	 * @param progressMonitor
 	 * @return events containing the JAX-RS Resources
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	private List<JaxrsElementDelta> preprocessResourceChangesOnScopeRemoval(final IJavaElement scope,
 			final JaxrsMetamodel metamodel, final IProgressMonitor progressMonitor) throws CoreException,
 			JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		// retrieve the existing elements from the Metamodel
 		final List<JaxrsResource> existingResources = metamodel.getElements(scope, JaxrsResource.class);
 		for (JaxrsResource resource : existingResources) {
 			results.add(new JaxrsElementDelta(resource, REMOVED));
 		}
 		return results;
 	}
 
 	private List<JaxrsElementDelta> postProcessResource(final JaxrsElementDelta event,
 			final IProgressMonitor progressMonitor) throws JavaModelException {
 		final List<JaxrsElementDelta> results = new ArrayList<JaxrsElementDelta>();
 		final JaxrsResource eventResource = (JaxrsResource) event.getElement();
 		final JaxrsMetamodel metamodel = eventResource.getMetamodel();
 		switch (event.getDeltaKind()) {
 		case ADDED:
 			metamodel.add(eventResource);
 			results.add(event);
 			break;
 		case REMOVED:
 			metamodel.remove(eventResource);
 			results.add(event);
 			break;
 		case CHANGED:
 			final JaxrsResource existingResource = metamodel.getElement(eventResource.getJavaElement(),
 					JaxrsResource.class);
 			// compare at the fields level
 			results.addAll(mergeResourceFields(existingResource, eventResource, metamodel));
 			// and compare at the methods level
 			results.addAll(mergeResourceMethods(existingResource, eventResource, metamodel));
 			// finally, compare at the annotations level
 			results.addAll(mergeResourceAnnotations(existingResource, eventResource));
 			break;
 		}
 		return results;
 	}
 
 	private final List<JaxrsElementDelta> mergeResourceAnnotations(final JaxrsResource existingResource,
 			final JaxrsResource matchingResource) {
 		final List<JaxrsElementDelta> changes = new ArrayList<JaxrsElementDelta>();
 		final int flags = existingResource.mergeAnnotations(matchingResource.getAnnotations());
 		if (flags > 0) {
 			changes.add(new JaxrsElementDelta(existingResource, CHANGED, flags));
 		}
 		return changes;
 	}
 
 	/**
 	 * Update this Resource fields with the ones of the given resource
 	 * 
 	 * @param metamodel
 	 * 
 	 * @param otherResource
 	 * @return the flags indicating the kind of changes that occurred during the update.
 	 */
 	private List<JaxrsElementDelta> mergeResourceFields(final JaxrsResource existingResource,
 			final JaxrsResource matchingResource, final JaxrsMetamodel metamodel) {
 		final List<JaxrsElementDelta> changes = new ArrayList<JaxrsElementDelta>();
 		final Map<String, JaxrsResourceField> addedFields = CollectionUtils.difference(matchingResource.getFields(),
 				existingResource.getFields());
 		final Map<String, JaxrsResourceField> removedFields = CollectionUtils.difference(existingResource.getFields(),
 				matchingResource.getFields());
 		final Map<String, JaxrsResourceField> changedFields = CollectionUtils.intersection(
 				existingResource.getFields(), matchingResource.getFields());
 
 		for (Entry<String, JaxrsResourceField> entry : addedFields.entrySet()) {
 			final JaxrsResourceField addedField = entry.getValue();
 			existingResource.addElement(addedField);
 			metamodel.add(addedField);
 			changes.add(new JaxrsElementDelta(addedField, ADDED));
 		}
 		for (Entry<String, JaxrsResourceField> entry : changedFields.entrySet()) {
 			final JaxrsResourceField existingField = entry.getValue();
 			final JaxrsResourceField matchingField = matchingResource.getFields().get(entry.getKey());
 			int flags = existingField.mergeAnnotations(matchingField.getAnnotations());
 			if ((flags & F_ELEMENT_KIND) > 0 && existingField.getKind() == EnumKind.UNDEFINED) {
 				metamodel.remove(existingField);
 				changes.add(new JaxrsElementDelta(existingField, REMOVED));
 			} else if (flags > 0) {
 				changes.add(new JaxrsElementDelta(existingField, CHANGED, flags));
 			}
 		}
 		for (Entry<String, JaxrsResourceField> entry : removedFields.entrySet()) {
 			final JaxrsResourceField removedField = entry.getValue();
 			existingResource.removeField(removedField);
 			metamodel.remove(removedField);
 			changes.add(new JaxrsElementDelta(removedField, REMOVED));
 		}
 
 		return changes;
 	}
 
 	private List<JaxrsElementDelta> mergeResourceMethods(final JaxrsResource existingResource,
 			final JaxrsResource matchingResource, final JaxrsMetamodel metamodel) throws JavaModelException {
 		final List<JaxrsElementDelta> changes = new ArrayList<JaxrsElementDelta>();
 		final Map<String, JaxrsResourceMethod> addedMethods = CollectionUtils.difference(matchingResource.getMethods(),
 				existingResource.getMethods());
 		final Map<String, JaxrsResourceMethod> removedMethods = CollectionUtils.difference(
 				existingResource.getMethods(), matchingResource.getMethods());
 		final Map<String, JaxrsResourceMethod> changedMethods = CollectionUtils.intersection(
 				existingResource.getMethods(), matchingResource.getMethods());
 
 		for (Entry<String, JaxrsResourceMethod> entry : addedMethods.entrySet()) {
 			final JaxrsResourceMethod addedMethod = entry.getValue();
 			existingResource.addMethod(addedMethod);
 			metamodel.add(addedMethod);
 			changes.add(new JaxrsElementDelta(addedMethod, ADDED));
 		}
 		for (Entry<String, JaxrsResourceMethod> entry : changedMethods.entrySet()) {
 			final JaxrsResourceMethod existingMethod = entry.getValue();
 			final JaxrsResourceMethod matchingMethod = matchingResource.getMethods().get(entry.getKey());
 			int flags = existingMethod.mergeAnnotations(matchingMethod.getAnnotations());
 			final CompilationUnit matchingResourceAST = CompilationUnitsRepository.getInstance().getAST(matchingResource.getResource());
 			final JavaMethodSignature matchingResourceMethodSignature = JdtUtils.resolveMethodSignature(matchingMethod.getJavaElement(), matchingResourceAST);
			flags += existingMethod.update(matchingResourceMethodSignature);
			if ((flags & F_ELEMENT_KIND) > 0 && existingMethod.getKind() == EnumKind.UNDEFINED) {
				metamodel.remove(existingMethod);
				changes.add(new JaxrsElementDelta(existingMethod, REMOVED));
			} else if (flags > 0) {
				changes.add(new JaxrsElementDelta(existingMethod, CHANGED, flags));
 			}
 		}
 		for (Entry<String, JaxrsResourceMethod> entry : removedMethods.entrySet()) {
 			final JaxrsResourceMethod removedMethod = entry.getValue();
 			existingResource.removeMethod(removedMethod);
 			metamodel.remove(removedMethod);
 			changes.add(new JaxrsElementDelta(removedMethod, REMOVED));
 		}
 		return changes;
 	}
 }
