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
 package org.eclipse.jst.jee.model.internal.common;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.j2ee.model.IModelProviderListener;
 import org.eclipse.jst.javaee.core.JavaEEObject;
 import org.eclipse.jst.javaee.core.SecurityRole;
 import org.eclipse.jst.javaee.core.SecurityRoleRef;
 import org.eclipse.jst.javaee.ejb.SessionBean;
 import org.eclipse.jst.jee.JEEPlugin;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 
 /**
  * Base implementation for model providers based on annotations in java files.
  * 
  * Listeners can be registered with {@link #addListener(IModelProviderListener)}
  * 
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public abstract class AbstractAnnotationModelProvider<T> implements IElementChangedListener, IModelProvider {
 
 	private static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
 
 	/**
 	 * Find the security role with the given name in the given assembly
 	 * descriptor.
 	 * 
 	 * @param assembly
 	 * @param name
 	 * @return <code>null</code> if a security role with this name can not be
 	 *         found
 	 */
 	private static SecurityRole findRole(Collection<SecurityRole> securityRoles, String name) {
 		for (SecurityRole role : securityRoles) {
 			if (role.getRoleName().equals(name))
 				return role;
 		}
 		return null;
 	}
 
 	protected T modelObject;
 
 	private Collection<IModelProviderListener> listeners;
 
 	private Lock listenersLock = new ReentrantLock();
 
 	protected IFacetedProject facetedProject;
 
 	private ManyToOneRelation<SecurityRoleRef, SecurityRole> rolesToRolesRef = new ManyToOneRelation<SecurityRoleRef, SecurityRole>();
 
 	/**
 	 * Constructs a new AnnotationReader for this faceted project. An illegal
 	 * argument if a project with value <code>null</code> is passed. No loading
 	 * is done in this constructor. Loading the model is made on demand when
 	 * calling {@link #getModelObject()}.
 	 * 
 	 * @param project
 	 *            the ejb project. Can not be <code>null</code>
 	 */
 	public AbstractAnnotationModelProvider(IFacetedProject project) {
 		if (project == null)
 			throw new IllegalArgumentException("The project argument can not be null"); //$NON-NLS-1$
 		this.facetedProject = project;
 	}
 
 	public T getConcreteModel() {
 		if (modelObject == null) {
 			preLoad();
 			try {
 				loadModel();
 				/*
 				 * Adding the resource change listener after loading the model.
 				 * No resource change event are acceptable while loading the
 				 * model.
 				 */
 				postLoad();
 			} catch (CoreException e) {
 				log(e.getStatus());
 				return null;
 			}
 		}
 		return modelObject;
 	}
 
 	public Object getModelObject() {
 		return getConcreteModel();
 	}
 
 	public Object getModelObject(IPath modelPath) {
 		return getConcreteModel();
 	}
 
 	protected abstract void loadModel() throws CoreException;
 
 	protected void preLoad() {
 	}
 
 	protected void postLoad() {
 		JavaCore.addElementChangedListener(this);
 	}
 
 	/**
 	 * Notifies the currently registered listeners with this model event. If the
 	 * {@link IModelProviderEvent#getChangedResources()} is empty or
 	 * <code>null</code> the method returns immediately.
 	 * 
 	 * @param event
 	 *            the event that should be send to the listeners
 	 */
 	protected void notifyListeners(final IModelProviderEvent event) {
 		if (listeners == null)
 			return;
 		listenersLock.lock();
 		try {
 			IModelProviderListener[] backup = listeners.toArray(new IModelProviderListener[listeners.size()]);
 			notifyListeners(backup, event);
 			backup = null;
 		} finally {
 			listenersLock.unlock();
 		}
 	}
 
 	/**
 	 * Clears the list of listeners. No notifications can occur while clearing
 	 * the listeners.
 	 */
 	protected void clearListeners() {
 		if (listeners == null)
 			return;
 		try {
 			listenersLock.lock();
 			listeners.clear();
 			listeners = null;
 		} finally {
 			listenersLock.unlock();
 		}
 	}
 
 	private void notifyListeners(final IModelProviderListener[] aListeners, final IModelProviderEvent event) {
 		if (event.getChangedResources() == null || event.getChangedResources().isEmpty())
 			return;
 		for (final IModelProviderListener listener : aListeners) {
 			SafeRunner.run(new ISafeRunnable() {
 				public void handleException(Throwable exception) {
 				}
 
 				public void run() throws Exception {
 					listener.modelsChanged(event);
 				}
 			});
 		}
 	}
 
 	/**
 	 * @return the currently registered listeners.
 	 */
 	protected Collection<IModelProviderListener> getListeners() {
 		if (listeners == null) {
 			listeners = new ArrayList<IModelProviderListener>();
 		}
 		return listeners;
 	}
 
 	/**
 	 * Adds a listener to this instance. No listeners can be added during
 	 * notifying the current listeners.
 	 * 
 	 * @param listener
 	 */
 	public void addListener(IModelProviderListener listener) {
 		listenersLock.lock();
 		try {
 			getModelObject();
 			getListeners().add(listener);
 		} finally {
 			listenersLock.unlock();
 		}
 	}
 
 	/**
 	 * Removes the listener from this instance. Has no effect if an identical
 	 * listener is not registered.
 	 * 
 	 * @param listener
 	 *            the listener to be removed.
 	 */
 	public void removeListener(IModelProviderListener listener) {
 		listenersLock.lock();
 		try {
 			getListeners().remove(listener);
 		} finally {
 			listenersLock.unlock();
 		}
 
 	}
 
 	/**
 	 * @param javaProject
 	 * @return true if the given project contains resources that are relative to
 	 *         the model. This method returns <code>true</code> for the
 	 *         ejbProject on which this instance is working a <code>true</code>
 	 *         for its client project.
 	 */
 	protected boolean isProjectRelative(IJavaProject javaProject) {
 		if (javaProject == null || facetedProject == null)
 			return false;
 		else if (javaProject.getProject().equals(facetedProject.getProject()))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Dispose the current instance. The actual dispose may occur in another
 	 * thread. Use {@link #addListener(IModelProviderListener)} to register a
 	 * listener that will be notified when the instance is disposed. After all
 	 * the listeners are notified the list of listeners is cleared.
 	 */
 	public void dispose() {
 		IModelProviderEvent modelEvent = createModelProviderEvent();
 		modelEvent.addResource(facetedProject.getProject());
 		modelEvent.setEventCode(IModelProviderEvent.UNLOADED_RESOURCE);
 		JavaCore.removeElementChangedListener(this);
 		modelObject = null;
 		notifyListeners(modelEvent);
 		clearListeners();
 	}
 
 	/**
 	 * Process a unit as "removed". The method is allowed not to make checks
 	 * whether the unit was added/removed/change. It is processing the unit as
 	 * "removed".
 	 * 
 	 * If no model object depends on the given file "modelEvent" is not changed.
 	 * 
 	 * @see #processAddedCompilationUnit(IModelProviderEvent, ICompilationUnit)
 	 * @param modelEvent
 	 *            subclasses should "fill" modelEvent with information about the
 	 *            change that has happened. This event will be propagated to
 	 *            model provided listeners.
 	 * @param file
 	 *            the file to be removed.
 	 * @throws CoreException
 	 *             if there was an error during parsing the file
 	 */
 	protected abstract void processRemovedCompilationUnit(IModelProviderEvent modelEvent, ICompilationUnit unit)
 			throws CoreException;
 
 	/**
 	 * Process a unit as "added". The method is allowed not to make checks
 	 * whether the unit was added/removed/change. It is processing the file as
 	 * "added". It is the responsibility of the caller to make sure the
 	 * processing of the file as added will not leave the model in a wrong
 	 * state.
 	 * 
 	 * modelEvent is changed to contain information about the added modelObject.
 	 * 
 	 * @see #processRemovedCompilationUnit(IModelProviderEvent,
 	 *      ICompilationUnit)
 	 * @param modelEvent
 	 *            subclasses should "fill" modelEvent with information about the
 	 *            change that has happened. This event will be propagated to
 	 *            model provided listeners.
 	 * @param file
 	 *            the file that was added
 	 * @throws CoreException
 	 */
 	protected abstract void processAddedCompilationUnit(IModelProviderEvent modelEvent, ICompilationUnit file)
 			throws CoreException;
 
 	/**
 	 * Process a unit as "changed". The method is allowed not to make checks
 	 * whether the unit was added/removed/change. It is processing the unit as
 	 * "changed". It is the responsibility of the caller to make sure the
 	 * processing of the file as "changed" will not leave the model in a wrong
 	 * state.
 	 * 
 	 * @see #processAddedCompilationUnit(IModelProviderEvent, ICompilationUnit)
 	 * @see #processRemovedCompilationUnit(IModelProviderEvent,
 	 *      ICompilationUnit)
 	 * @param modelEvent
 	 *            subclasses should "fill" modelEvent with information about the
 	 *            change that has happened. This event will be propagated to
 	 *            model provided listeners.
 	 * @param unit
 	 *            the unti that was changed
 	 * @throws CoreException
 	 */
 	protected abstract void processChangedCompilationUnit(IModelProviderEvent modelEvent, ICompilationUnit file)
 			throws CoreException;
 
 	protected void log(IStatus status) {
 	}
 
 	protected MyModelProviderEvent createModelProviderEvent() {
 		return new MyModelProviderEvent(0, null, facetedProject.getProject());
 	}
 
 	// ---------------SECURITY ROLES ---------------------------//
 	protected abstract Collection<SecurityRole> getSecurityRoles();
 
 	protected abstract Collection<SecurityRoleRef> getSecurityRoleRefs(JavaEEObject target);
 
 	/**
 	 * Deletes the connection maintained by the given bean and the security
 	 * roles defined in the bean. If this is the only bean in which the role is
 	 * defined, the role will also be deleted. Calling this method makes sense
 	 * only if the bean and the security role and the bean were connected with
 	 * {@link #connectWithRole(SecurityRole, SessionBean)}
 	 * 
 	 * <p>
 	 * If the bean is not of type org.eclipse.jst.javaee.ejb.SessionBean the
 	 * method returns immediately.
 	 * </p>
 	 * 
 	 * @see #connectWithRole(SecurityRole, SessionBean)
 	 * @see #rolesToRolesRef
 	 * @param bean
 	 */
 	protected void disconnectFromRoles(JavaEEObject target) {
 		Collection<SecurityRole> roles = getSecurityRoles();
 		if (roles == null)
 			return;
 		Collection<SecurityRoleRef> refs = getSecurityRoleRefs(target);
 		if (refs == null)
 			return;
 		for (SecurityRoleRef ref : refs) {
 			SecurityRole role = rolesToRolesRef.getTarget(ref);
 			rolesToRolesRef.disconnectSource(ref);
 			if (!rolesToRolesRef.containsTarget(role)) {
 				getSecurityRoles().remove(role);
 			}
 		}
 	}
 
 	/**
 	 * A security role was found in the given file. Add this security role to
 	 * the assembly descriptor. If the ejbJar does not have an assembly
 	 * descriptor a new one is created.
 	 * 
 	 * @see #connectRoleWithBean(SecurityRole, SessionBean)s
 	 * @param file
 	 * @param securityRole
 	 */
 	protected void securityRoleFound(JavaEEObject object, SecurityRole securityRole) {
 		connectWithRole(securityRole, object);
 	}
 
 	/**
 	 * A security role can be defined in more the one bean. A bean can define
 	 * more then one security role. This means we have a many-to-many relation
 	 * between sessionBeans and securityRoles.
 	 * 
 	 * <p>
 	 * Luckily a sessionBean contains a list of securityRoleRefs. This method
 	 * creates a connection between the securityRole contained in the assembly
 	 * descriptor and the security role ref contained in the bean.
 	 * 
 	 * If a security role is define only in one bean, deleting the bean means
 	 * deleting the security role. But if the security role is defined in two
 	 * beans only deleting both beans will result in deleting the security role.
 	 * </p>
 	 * 
 	 * @see #disconnectFromRoles(JavaEEObject)
 	 * @see #rolesToRolesRef
 	 * @param securityRole
 	 * @param target
 	 */
 	private void connectWithRole(SecurityRole securityRole, JavaEEObject target) {
 		Collection<SecurityRole> roles = getSecurityRoles();
 		if (roles == null)
 			return;
 		Collection<SecurityRoleRef> refs = getSecurityRoleRefs(target);
 		if (refs == null)
 			return;
 		/*
 		 * If there is a security role with this name use the existing security
 		 * role.
 		 */
 		SecurityRole role = findRole(roles, securityRole.getRoleName());
 		if (role == null) {
 			roles.add(securityRole);
 			role = securityRole;
 		}
 		for (SecurityRoleRef ref : refs) {
 			if (ref.getRoleName().equals(role.getRoleName()))
 				rolesToRolesRef.connect(ref, role);
 		}
 	}
 
 	public void elementChanged(final ElementChangedEvent javaEvent) {
 		if (javaEvent.getType() == ElementChangedEvent.POST_RECONCILE)
 			internalPostReconcile(javaEvent);
 		else if (javaEvent.getType() == ElementChangedEvent.POST_CHANGE)
 			internalPostChange(javaEvent);
 	}
 
 	private void internalPostChange(ElementChangedEvent javaEvent) {
 		IModelProviderEvent modelEvent = createModelProviderEvent();
 		// handles ElementChangedEvent.POST_CHANGE - the case when the
 		// compilation unit has been changed
 		for (IJavaElementDelta child : javaEvent.getDelta().getAffectedChildren()) {
 			if (child.getElement() instanceof IJavaProject) {
 				processChangedProject(modelEvent, child);
 				notifyListeners(modelEvent);
 			}
 		}
 	}
 
 	private void internalPostReconcile(final ElementChangedEvent javaEvent) {
 		IModelProviderEvent modelEvent = createModelProviderEvent();
 		if (javaEvent.getDelta().getElement() instanceof ICompilationUnit) {
 			recursevilyProcessCompilationUnits(modelEvent, javaEvent.getDelta());
 			notifyListeners(modelEvent);
 		}
 	}
 
 	protected void processChangedProject(IModelProviderEvent event, IJavaElementDelta projectDelta) {
 		if (!isProjectRelative(projectDelta.getElement().getJavaProject())) {
 			return;
 		}
 		Assert.isTrue(projectDelta.getElement() instanceof IJavaProject,
 				"An invalid change notification has occured. Element is <" + projectDelta.getElement() + ">"); //$NON-NLS-1$//$NON-NLS-2$
 		if (((projectDelta.getFlags() & IJavaElementDelta.F_OPENED) != 0)
 				|| projectDelta.getKind() == IJavaElementDelta.ADDED) {
 			try {
 				loadModel();
 			} catch (CoreException e) {
 				JEEPlugin.getDefault().getLog().log(
 						new Status(IStatus.ERROR, JEEPlugin.getDefault().getPluginID(), e.getMessage(), e));
 			}
 		}
 
 		if (((projectDelta.getFlags() & IJavaElementDelta.F_CLOSED) != 0)
 				|| projectDelta.getKind() == IJavaElementDelta.REMOVED) {
 			dispose();
 		}
 
 		processChangedProjectChildren(event, projectDelta);
 	}
 
 	protected void processChangedProjectChildren(IModelProviderEvent event, IJavaElementDelta projectDelta) {
 		for (IJavaElementDelta childDelta : projectDelta.getAffectedChildren()) {
 			if (!(childDelta.getElement() instanceof IPackageFragmentRoot)) {
 				continue;
 			}
 			if ((childDelta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
 				recursevilyProcessPackages(event, childDelta);
 			}
 		}
 	}
 
 	public void recursevilyProcessPackages(IModelProviderEvent modelEvent, IJavaElementDelta delta) {
 		if (delta.getElement() instanceof IPackageFragment) {
 			try {
 				IPackageFragment fragment = (IPackageFragment) delta.getElement();
 				if (delta.getKind() == IJavaElementDelta.ADDED) {
 					for (ICompilationUnit unit : fragment.getCompilationUnits()) {
 						processAddedCompilationUnit(modelEvent, unit);
 					}
 				} else if (delta.getKind() == IJavaElementDelta.REMOVED) {
 					if (delta.getKind() == IJavaElementDelta.REMOVED) {
 						processRemovedPackage(modelEvent, delta);
 					}
 				} else if (delta.getKind() == IJavaElementDelta.CHANGED) {
 					recursevilyProcessCompilationUnits(modelEvent, delta);
 				}
 			} catch (CoreException e) {
 				JEEPlugin.getDefault().getLog().log(
 						new Status(IStatus.ERROR, JEEPlugin.getDefault().getPluginID(), e.getMessage(), e));
 			}
 		} else {
 			for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
 				recursevilyProcessPackages(modelEvent, childDelta);
 			}
 		}
 	}
 
 	protected abstract void processRemovedPackage(IModelProviderEvent modelEvent, IJavaElementDelta delta)
 			throws CoreException;
 
 	public void recursevilyProcessCompilationUnits(IModelProviderEvent modelEvent, IJavaElementDelta delta) {
 		if (delta.getElement() instanceof ICompilationUnit) {
 			if (!isProjectRelative(delta.getElement().getJavaProject()))
 				return;
 			try {
 				final ICompilationUnit unit = (ICompilationUnit) delta.getElement();
 
 				if (delta.getKind() == IJavaElementDelta.ADDED) {
 					processAddedCompilationUnit(modelEvent, unit);
 				}
 				if (delta.getKind() == IJavaElementDelta.REMOVED) {
 					processRemovedCompilationUnit(modelEvent, unit);
 				}
 				if (delta.getKind() == IJavaElementDelta.CHANGED) {
 					if (((delta.getFlags() & IJavaElementDelta.F_PRIMARY_RESOURCE) == 0)
 							|| ((delta.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) == 0)) {
 						modelEvent
 								.setEventCode(IModelProviderEvent.KNOWN_RESOURCES_CHANGED | modelEvent.getEventCode());
 						processChangedCompilationUnit(modelEvent, unit);
 					}
 				}
 			} catch (CoreException e) {
 				JEEPlugin.getDefault().getLog().log(
 						new Status(IStatus.ERROR, JEEPlugin.getDefault().getPluginID(), e.getMessage(), e));
 			}
 		} else {
 			for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
 				recursevilyProcessCompilationUnits(modelEvent, childDelta);
 			}
 		}
 	}
 
 	protected void visitJavaFiles(final Collection<ICompilationUnit> javaFiles, final IPackageFragmentRoot root)
 			throws CoreException {
 		if (root.getKind() != IPackageFragmentRoot.K_SOURCE)
 			return;
 		root.getCorrespondingResource().accept(new IResourceProxyVisitor() {
 			public boolean visit(IResourceProxy proxy) throws CoreException {
 				if (proxy.getType() == IResource.FILE) {
 					if (proxy.getName().endsWith("." + JAVA_EXTENSION)) { //$NON-NLS-1$
 						IFile file = (IFile) proxy.requestResource();
 						if (!root.getJavaProject().isOnClasspath(file))
 							return false;
 						if (!file.isSynchronized(IResource.DEPTH_ONE))
 							return false;
 						javaFiles.add(JavaCore.createCompilationUnitFrom(file));
 					}
 					return false;
 				}
 				return true;
 			}
 		}, IContainer.NONE);
 
 	}
 }
