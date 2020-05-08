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
 
 import java.util.Map;
 
 import org.eclipse.core.internal.events.ResourceChangeEvent;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.jboss.tools.ws.jaxrs.core.JBossJaxrsCorePlugin;
 import org.jboss.tools.ws.jaxrs.core.configuration.ProjectNatureUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.ConstantUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 
 /**
  * The JAX-RS Metamodel builder. Invoked when a full build or an incremental build is triggered on a project on which
  * the JAX-RS nature is installed.
  * 
  * This builder is responsible of the creation and update of the JAX-RS Metamodel which is kept in the project's session
  * properties.
  * 
  * @author xcoulon
  */
 @SuppressWarnings("restriction")
 public class JaxrsMetamodelBuilder extends IncrementalProjectBuilder {
 
 	public static final int SCALE = 10;
 
 	/** The builder ID. */
 	public static final String BUILDER_ID = "org.jboss.tools.ws.jaxrs.metamodelBuilder";
 	
 	/** The Java element change listener name. */
 	public static final QualifiedName JAXRS_ELEMENT_CHANGE_LISTENER_NAME = new QualifiedName(
 			JBossJaxrsCorePlugin.PLUGIN_ID, "jaxrsPostReconcileListener");
 
 	// TODO : add support for cancellation
 	// TODO : report build failed
 	/** {@inheritDoc} */
 	@Override
 	protected final IProject[] build(final int kind, @SuppressWarnings("rawtypes") final Map args,
 			final IProgressMonitor monitor) throws CoreException {
 		IProject project = getProject();
 		if (!ProjectNatureUtils.isProjectNatureInstalled(project, ProjectNatureUtils.JAXRS_NATURE_ID)) {
 			Logger.warn("Project '" + project.getName() + "' is not a JAX-RS project.");
 			return null;
 		}
 		logBuild(kind, args, project);
 		switch (kind) {
 		case FULL_BUILD: // also called after a CLEAN_BUILD
 		case AUTO_BUILD:
 		case INCREMENTAL_BUILD:
 			build(kind, project, monitor);
 			break;
 		}
 		return new IProject[] { project };
 	}
 
 	/**
 	 * Checks if the running operation was cancelled by the user, as reported by the progress monitor.
 	 * 
 	 * @param monitor
 	 *            the progress monitor.
 	 */
 	// @see http://www.eclipse.org/articles/Article-Builders/builders.html
 	protected final void checkCancel(final IProgressMonitor monitor) {
 		if (monitor.isCanceled()) {
 			forgetLastBuiltState(); // not always necessary
 			throw new OperationCanceledException();
 		}
 	}
 
 	/**
 	 * Performs a full build of the project's JAX-RS Metamodel. This method has a public visibility so that it can be
 	 * called from other components
 	 * 
 	 * @param project
 	 *            the project
 	 * @param progressMonitor
 	 *            the progress monitor
 	 */
 	private void build(final int buildKind, final IProject project, final IProgressMonitor progressMonitor) {
 		ResourceChangedBuildJob job = new ResourceChangedBuildJob(project, getResourceChangeEvent(project, buildKind));
 		job.setRule(MutexJobSchedulingRule.getInstance());
 		job.schedule();
 		try {
 			job.join();
 		} catch (InterruptedException e) {
 			Logger.warn("JAX-RS Metamodel build job was interrupted", e);
 		}
 
 	}
 
 	private ResourceChangeEvent getResourceChangeEvent(final IProject project, final int buildKind) {
 		return new ResourceChangeEvent(project, IResourceChangeEvent.POST_CHANGE, buildKind, getDelta(project));
 	}
 
 	/**
 	 * Trace the kind of build in the log.
 	 * 
 	 * @param kind
 	 *            the build kind
 	 * @param args
 	 * @param project
 	 *            the project being built
 	 */
 	private void logBuild(final int kind, @SuppressWarnings("rawtypes") final Map args, final IProject project) {
 		StringBuilder sb = new StringBuilder("JAX-RS Builder called after '");
 		sb.append(ConstantUtils.getStaticFieldName(IncrementalProjectBuilder.class, kind));
 		sb.append("' on project ").append(project.getName());
 		Logger.debug(sb.toString());
 	}
 
 }
