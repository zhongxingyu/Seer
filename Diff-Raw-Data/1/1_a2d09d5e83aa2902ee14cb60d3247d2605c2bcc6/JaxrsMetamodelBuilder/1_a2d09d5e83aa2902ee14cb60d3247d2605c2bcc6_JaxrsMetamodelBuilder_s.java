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
 
 package org.jboss.tools.ws.jaxrs.core.internal.builder;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.jboss.tools.ws.jaxrs.core.JBossJaxrsCorePlugin;
 import org.jboss.tools.ws.jaxrs.core.configuration.ProjectNatureUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.metamodel.Metamodel;
 
 /**
  * The JAX-RS Metamodel builder. Invoked when a full build or an incremental
  * build is triggered on a project on which the JAX-RS nature is installed.
  * 
  * This builder is responsible of the creation and update of the JAX-RS
  * Metamodel which is kept in the project's session properties.
  * 
  * @author xcoulon
  * 
  */
 // FIXME : add constraint : this builder must run after JDT
 // MediaTypeCapabilitiesBuilder.
 public class JaxrsMetamodelBuilder extends IncrementalProjectBuilder implements IResourceDeltaVisitor {
 
 	/** The number of steps to fully build the JAX-RS Metamodel. */
 	private static final int FULL_BUILD_STEPS = 100;
 
 	/** The standard 'Java type' marker type. */
 	public static final String JAVA_PROBLEM = "org.eclipse.jdt.core.problem";
 
 	/** The custom 'JAX-RS Problem' marker type. */
 	public static final String JAXRS_PROBLEM = "org.jboss.tools.ws.jaxrs.metamodelMarker";
 
 	/** The Java element change listener name. */
 	public static final QualifiedName JAXRS_ELEMENT_CHANGE_LISTENER_NAME = new QualifiedName(
 			JBossJaxrsCorePlugin.PLUGIN_ID, "jaxrsPostReconcileListener");
 
 	private final List<IJavaElementDelta> changeHints = new ArrayList<IJavaElementDelta>();
 
 	// TODO : add support for cancellation
 	// TODO : report build failed
 	/**
 	 * {@inheritDoc}
 	 */
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
 		case CLEAN_BUILD:
 		case FULL_BUILD:
 			fullBuild(project, monitor);
 			break;
 		case AUTO_BUILD:
 		case INCREMENTAL_BUILD:
 			if (Metamodel.get(getProject()) == null) {
 				fullBuild(getProject(), monitor);
 			} else {
 				IResourceDelta delta = getDelta(project);
 				incrementalBuild(project, delta, monitor);
 			}
 			break;
 		default:
 			break;
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if the running operation was cancelled by the user, as reported by
 	 * the progress monitor.
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
 
 	public void addChangeHint(IJavaElementDelta delta) {
 		changeHints.add(delta);
 	}
 
 	/**
 	 * Performs and incremental build on the given project, using the given
 	 * delta.
 	 * 
 	 * @param project
 	 *            the project
 	 * @param delta
 	 *            the resource delta
 	 * @param monitor
 	 *            the progress monitor (optional)
 	 * @throws CoreException
 	 *             in case of underlying exception
 	 */
 	private void incrementalBuild(final IProject project, final IResourceDelta delta, final IProgressMonitor monitor)
 			throws CoreException {
 		if (delta == null) {
 			return;
 		}
 		delta.accept(this);
 	}
 
 	@Override
 	public final boolean visit(final IResourceDelta delta) throws CoreException {
 		IResource resource = delta.getResource();
 		if (resource.getType() == IResource.FILE && getProject().getFullPath().isPrefixOf(resource.getFullPath())
 				&& resource.getFullPath().getFileExtension().equals("java")) {
 			Metamodel metamodel = Metamodel.get(resource.getProject());
 			if (metamodel == null) {
 				Logger.warn("No Metamodel found for project " + resource.getProject().getName());
 				return false;
 			}
 			logDelta(delta);
 			metamodel.applyDelta(delta, new NullProgressMonitor());
 			metamodel.validate(new NullProgressMonitor());
 		}
 		return true;
 	}
 
 	/**
 	 * Performs a full build of the project's JAX-RS Metamodel. This method has
 	 * a public visibility so that it can be called from other components
 	 * 
 	 * @param project
 	 *            the project
 	 * @param monitor
 	 *            the progress monitor
 	 */
 	private void fullBuild(final IProject project, final IProgressMonitor monitor) {
 		long startTime = new Date().getTime();
 		try {
 			monitor.beginTask("Building JAX-RS metamodel...", FULL_BUILD_STEPS);
 			IJavaProject javaProject = JavaCore.create(project);
 
 			Metamodel metamodel = Metamodel.get(project);
 			if (metamodel == null) {
 				metamodel = new Metamodel(javaProject);
 			} else {
 				metamodel.reset();
 			}
 			// FIXME : determine correct service base URI
 			metamodel.setServiceUri("/*");
 			metamodel.addElements(javaProject, monitor);
 			metamodel.validate(monitor);
 		} catch (CoreException e) {
 			Logger.error("Failed to load metamodel from project '" + project.getName() + "''s session properties", e);
 		} finally {
 			long endTime = new Date().getTime();
 			Logger.debug("JAX-RS Metamodel for project '" + project.getName() + "' fully built in "
 					+ (endTime - startTime) + "ms.");
 			monitor.done();
 		}
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
 		StringBuilder sb = new StringBuilder("'");
 		for (Field field : IncrementalProjectBuilder.class.getDeclaredFields()) {
 			String name = field.getName();
 			int value;
 			try {
 				value = field.getInt(this);
 				if (value == kind) {
 					sb.append(name.toLowerCase().replace('_', ' '));
 				}
 			} catch (IllegalArgumentException e) {
 				sb.append("*Unknow build*");
 			} catch (IllegalAccessException e) {
 				sb.append("*Unknow build*");
 			}
 		}
 		sb.append("' on project ").append(project.getName());
 		if (args != null && !args.isEmpty()) {
 			sb.append(" (");
 			for (Iterator<?> iterator = args.keySet().iterator(); iterator.hasNext();) {
 				Object key = iterator.next();
 				sb.append(key).append("=").append(args.get(key));
 				if (iterator.hasNext()) {
 					sb.append(", ");
 				}
 			}
 			sb.append(")");
 		}
 		Logger.debug(sb.toString());
 	}
 
 	/**
 	 * Output a message with an 'debug' level in the output log.
 	 * 
 	 * @param delta
 	 *            the build delta
 	 * @throws CoreException
 	 *             in case of underlying exception
 	 */
 	private static void logDelta(final IResourceDelta delta) throws CoreException {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Resource ");
 		switch (delta.getKind()) {
 		case IResourceDelta.ADDED:
 			sb.append("added: ");
 			break;
 		case IResourceDelta.CHANGED:
 			sb.append("changed: ");
 			break;
 		case IResourceDelta.REMOVED:
 			sb.append("removed: ");
 			break;
 		default:
 			break;
 		}
 		sb.append(delta.getResource().getName());
 		if (delta.getResource().exists()) {
 			IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
 			int added = 0;
 			int removed = 0;
 			for (IMarkerDelta markerDelta : markerDeltas) {
 				int severity = markerDelta.getAttribute(IMarker.SEVERITY, 0);
 				String type = markerDelta.getType();
 				if (markerDelta.getKind() == IResourceDelta.ADDED && severity == IMarker.SEVERITY_ERROR
 						&& type.equals("org.eclipse.jdt.core.problem")) {
 					added++;
 				} else if (markerDelta.getKind() == IResourceDelta.REMOVED && severity == IMarker.SEVERITY_ERROR
 						&& type.equals("org.eclipse.jdt.core.problem")) {
 					removed++;
 				}
 			}
 			sb.append("+").append(added).append("/-").append(removed);
 			IMarker[] markers = delta.getResource().findMarkers(null, true, IResource.DEPTH_INFINITE);
 			int count = 0;
 			for (IMarker marker : markers) {
 				int severity = marker.getAttribute(IMarker.SEVERITY, 0);
 				String type = marker.getType();
 				if (severity == IMarker.SEVERITY_ERROR && type.equals("org.eclipse.jdt.core.problem")) {
 					count++;
 				}
 			}
 			sb.append(" / total=").append(count).append(" problem(s)");
 		}
 
 		int flags = delta.getFlags();
 		sb.append("[flags=");
 		if (flags == 0) {
 			sb.append("none");
 		}
 		for (Field field : IResourceDelta.class.getDeclaredFields()) {
 			String name = field.getName();
 			int value;
 			try {
 				value = field.getInt(delta);
 				if ((flags & value) != 0) {
 					sb.append(name).append("+");
 				}
 			} catch (IllegalArgumentException e) {
 				Logger.debug("Failed to match the fields for flag'" + flags + "':" + e.getMessage());
 			} catch (IllegalAccessException e) {
 				Logger.debug("Failed to match the fields for flag'" + flags + "':" + e.getMessage());
 			}
 		}
 
 		sb.append("]");
 		Logger.debug(sb.toString().replace("+]", "]"));
 	}
 
 }
