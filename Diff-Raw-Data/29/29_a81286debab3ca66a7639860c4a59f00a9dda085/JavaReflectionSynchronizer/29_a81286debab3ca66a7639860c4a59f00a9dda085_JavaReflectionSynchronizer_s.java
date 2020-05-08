 package org.eclipse.jem.internal.adapters.jdom;
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: JavaReflectionSynchronizer.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:33:53 $ 
  */
 
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jem.internal.core.MsgLogger;
 import org.eclipse.jem.internal.plugin.JavaPlugin;
 /**
  * Insert the type's description here.
  * Creation date: (11/1/2000 11:42:05 AM)
  * @author: Administrator
  */
 public class JavaReflectionSynchronizer extends JavaModelListener {
 	
 	private static final IPath CLASSPATH_PATH = new Path(".classpath"); //$NON-NLS-1$
 	protected JavaJDOMAdapterFactory fAdapterFactory;
 	protected boolean flushedAll = false;
 	protected List flushTypes = new ArrayList();
 	protected List notifications = new ArrayList();
 	/**
 	 * JavaReflectionSynchronizer constructor comment.
 	 */
 	public JavaReflectionSynchronizer(JavaJDOMAdapterFactory synchronizee) {
 		super();
 		fAdapterFactory = synchronizee;
 	}
 	/**
 	 * Tell the reflection factory to flush the passed IType
 	 */
 	protected Notification doFlush(IType element) {
 		return getAdapterFactory().flushReflectionNoNotification(element.getFullyQualifiedName());
 	}
 	protected void flush(IType element) {
 		if (!flushTypes.contains(element))
 			flushTypes.add(element);
 	}
 	protected void flushPackage(String packageName, boolean noFlushIfSourceFound) {
 		notifications.addAll(getAdapterFactory().flushPackageNoNotification(packageName, true));
 	}
 	protected JavaJDOMAdapterFactory getAdapterFactory() {
 		return fAdapterFactory;
 	}
 	private boolean isClassPathChange(IJavaElementDelta delta) {
 		int flags = delta.getFlags();
 		return (delta.getKind() == IJavaElementDelta.CHANGED && ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) || ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) || ((flags & IJavaElementDelta.F_REORDER) != 0));
 	}
 	/**
 	 * If the complation unit's content has changed, notify all adapters which point to it.
 	 * This change may not require a call to flush() if the structure of the entity has not changed.
 	 * Creation date: (8/17/2001 3:58:31 PM)
 	 * @param param org.eclipse.jdt.core.IJavaElementDelta
 	 */
 	public void processContentChanged(IJavaElementDelta delta) {
 		if (delta == null)
 			return;
 		// Any change will be notified, as changes to childrens may impact the text-location of this element.
 		IJavaElement element = delta.getElement();
 		if (element.getElementType() == IJavaElement.COMPILATION_UNIT)
 			if (((delta.getFlags()) & (IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_MODIFIERS | IJavaElementDelta.F_CHILDREN | IJavaElementDelta.F_MOVED_TO | IJavaElementDelta.F_MOVED_FROM)) != 0) {
 				getAdapterFactory().notifyContentChanged((ICompilationUnit) element);
 			}
 	}
 	/**
 	 * Handle the change for a single element, children will be handled separately.
 	 *
 	 */
 	protected void processJavaElementChanged(ICompilationUnit element, IJavaElementDelta delta) {
 		if (!element.isWorkingCopy()) {
 			switch (delta.getKind()) {
				case IJavaElementDelta.CHANGED : {
					try {
						IType[] flushTypes = element.getAllTypes();
						for (int i = 0; i < flushTypes.length; i++)
							flush(flushTypes[i]);
					} catch (JavaModelException e) {}
 					
 					break;
 				}
 				case IJavaElementDelta.ADDED :
 				case IJavaElementDelta.REMOVED : {
 					disAssociateSourcePlusInner(getFullNameFromElement(element));
 					return;
 				}
 			}
 			processChildren(element, delta);
 		}
 	}
 	/**
 	 * Handle the change for a single element, children will be handled separately.
 	 *
 	 */
 	protected void processJavaElementChanged(IJavaProject element, IJavaElementDelta delta) {
 		if (isInClasspath(element)) {
 			if (delta.getKind() == IJavaElementDelta.REMOVED) {
 				if (element.equals(getAdapterFactory().getJavaProject()))
 					stopSynchronizer();
 				else
 					flushAll(); //another dependent project has changed so flush all to be safe
 				return;
 			} else if (delta.getKind() == IJavaElementDelta.ADDED || isClasspathResourceChange(delta)) {
 				flushAll();
 				return;
 			}
 			processChildren(element, delta);
 		}
 	}
 	/**
 	 * Handle the change for a single element, children will be handled separately.
 	 */
 	protected void processJavaElementChanged(IClassFile element, IJavaElementDelta delta) {
 		int kind = delta.getKind();
 		if (kind == IJavaElementDelta.REMOVED || kind == IJavaElementDelta.ADDED) {
 			// It doesn't matter if totally removed or just moved somewhere else, we will clear out and remove the
 			// adapter because there could be a rename which would be a different class.
 			// Currently the element is already deleted and there is no way to find the types in the unit to remove.
 			// So instead we ask factory to remove all it any that start with it plus for inner classes.
 			disAssociateSourcePlusInner(getFullNameFromElement(element));
 			return; // Since the classfile was removed we don't need to process the children (actually the children list will be empty
 		}
 		IJavaElementDelta[] children = delta.getAffectedChildren();
 		for (int ii = 0; ii < children.length; ii++) {
 			processDelta(children[ii]);
 		}
 	}
 	/**
 	 * Method isClasspathResourceChange.
 	 * @param delta
 	 * @return boolean
 	 */
 	private boolean isClasspathResourceChange(IJavaElementDelta delta) {
 		IResourceDelta[] resources = delta.getResourceDeltas();
 		if (resources == null)
 			return false;
 		IPath path = null;
 		for (int i = 0; i < resources.length; i++) {
 			if (resources[i].getKind() == IResourceDelta.CHANGED) {
 				path = resources[i].getProjectRelativePath();
 				if (path.equals(CLASSPATH_PATH))
 					return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * Handle the change for a single element, children will be handled separately.
 	 *
 	 */
 	protected void processJavaElementChanged(IPackageFragmentRoot element, IJavaElementDelta delta) {
 		if (flushedAll)
 			return;
 		if (isClassPathChange(delta))
 			flushAll();
 		else
 			super.processJavaElementChanged(element, delta);
 	}
 	
 	/* 
 	 * We will force the flushing of all adaptors for the given package name.
 	 * This is necessary if a type was reflected prior to the package existing or
 	 * if the package is deleted.
 	 * @see org.eclipse.jem.internal.adapters.jdom.JavaModelListener#processJavaElementChanged(org.eclipse.jdt.core.IPackageFragment, org.eclipse.jdt.core.IJavaElementDelta)
 	 */
 	protected void processJavaElementChanged(IPackageFragment element, IJavaElementDelta delta) {
 		switch (delta.getKind()) {
 			case IJavaElementDelta.ADDED : {
 				if (delta.getAffectedChildren().length == 0)
 					flushPackage(delta.getElement().getElementName(), true);
 					break;
 			}
 			case IJavaElementDelta.REMOVED :{
 				if (delta.getAffectedChildren().length == 0)
 					getAdapterFactory().flushPackage(delta.getElement().getElementName(), false);
 					break;
 			}
 			default :
 				super.processJavaElementChanged(element, delta);
 		}
 	}
 
 	/**
 	 * Handle the change for a single element, children will be handled separately.
 	 *
 	 */
 	protected void processJavaElementChanged(IType element, IJavaElementDelta delta) {
 		int kind = delta.getKind();
 		if (kind == IJavaElementDelta.REMOVED || kind == IJavaElementDelta.ADDED) {
 			disAssociateSourcePlusInner(element.getFullyQualifiedName());
 		} else {
 			flush(element);
 			processChildren(element, delta);
 			// Note, if a method element or a field was changed, there may be delta.getAffectedChildren()
 			//       that will have to be processed if we are to update the JavaMethod/JavaField JDOMAdaptor s.	
 		}
 	}
 	/**
 	 * Given that an IType does not exists anymore, assume
 	 * that the type's name is package.filename (without the .java)
 	 * If we are wrong (if, then a rare case),  we will flush.
 	 * Next access will induce a reflection attempt.
 	 */
 	protected void processRemoveOrAdd(ICompilationUnit element) {
 		disAssociateSource(getFullNameFromElement(element));
 	}
 	protected String getFullNameFromElement(IJavaElement element) {
 		String name = element.getElementName();
 		if (element == null || name.length() <= 5 || !name.substring(name.length() - 5).equals(".java")) { //$NON-NLS-1$
 			// Should not be here, 
 			JavaPlugin.getDefault().getMsgLogger().log("Invalid .java file: " + name, MsgLogger.LOG_FINE); //$NON-NLS-1$
 			// Make a guess, at worst case, nothing will come out of this.
 			int index = name.lastIndexOf("."); //$NON-NLS-1$
 			if (index >= 0)
 				name = name.substring(0, index) + ".java"; // rename the extension to .java //$NON-NLS-1$
 			else
 				name = name + ".java"; //$NON-NLS-1$
 		}
 		if (element.getParent().getElementName() == null || element.getParent().getElementName().length() == 0)
 			return name.substring(0, name.length() - 5);
 		else
 			return element.getParent().getElementName() + "." + name.substring(0, name.length() - 5); //$NON-NLS-1$
 	}
 	/**
 	 * Stop the synchronizer from listening to any more changes.
 	 */
 	public void stopSynchronizer() {
 		JavaCore.removeElementChangedListener(this);
 	}
 	/**
 	 * This method will check to see if a <code>javaProject</code> is a project in the
 	 * classpath of the adapterFactory java project.
 	 */
 	protected boolean isInClasspath(IJavaProject javaProject) {
 		IJavaProject adapterJavaProject = getAdapterFactory().getJavaProject();
 		if (javaProject.equals(adapterJavaProject))
 			return true;
 		return isInClasspath(javaProject, adapterJavaProject, true, new HashSet());
 	}
 	protected boolean isInClasspath(IJavaProject testProject, IJavaProject targetProject, boolean isFirstLevel, Set visited) {
 		if (visited.contains(targetProject))
 			return false;
 		visited.add(targetProject);
 		IClasspathEntry[] entries = null;
 		try {
 			entries = targetProject.getRawClasspath();
 		} catch (JavaModelException e) {
 			return false;
 		}
 		IClasspathEntry entry, resEntry;
 		IJavaProject proj = null;
 		List projects = null;
 		for (int i = 0; i < entries.length; i++) {
 			entry = entries[i];
 			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
 				resEntry = JavaCore.getResolvedClasspathEntry(entry);
 				proj = getJavaProject(entry);
 				if (isFirstLevel || resEntry.isExported()) {
 					if (proj.equals(testProject))
 						return true;
 					else {
 						if (projects == null)
 							projects = new ArrayList();
 						projects.add(proj);
 					}
 				}
 			}
 		}
 		return isInClasspath(testProject, projects, false, visited);
 	}
 	protected boolean isInClasspath(IJavaProject testProject, List someJavaProjects, boolean isFirstLevel, Set visited) {
 		if (someJavaProjects == null)
 			return false;
 		int size = someJavaProjects.size();
 		IJavaProject javaProj = null;
 		for (int i = 0; i < size; i++) {
 			javaProj = (IJavaProject) someJavaProjects.get(i);
 			return isInClasspath(testProject, javaProj, isFirstLevel, visited);
 		}
 		return false;
 	}
 	protected IJavaProject getJavaProject(IClasspathEntry entry) {
 		IProject proj = getWorkspaceRoot().getProject(entry.getPath().segment(0));
 		if (proj != null)
 			return (IJavaProject) JavaCore.create(proj);
 		return null;
 	}
 	protected IWorkspaceRoot getWorkspaceRoot() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 	/**
 	 * @see org.eclipse.jem.internal.adapters.jdom.JavaModelListener#elementChanged(ElementChangedEvent)
 	 */
 	public void elementChanged(ElementChangedEvent event) {
 		try {
 			flushTypes.clear();
 			notifications.clear();
 			super.elementChanged(event);
 			flushTypes();
 			processNotifications();
 		} finally {
 			flushedAll = false;
 			flushTypes.clear();
 			notifications.clear();
 		}
 	}
 	/**
 	 * 
 	 */
 	private void flushTypes() {
 		if (!flushTypes.isEmpty()) {
 			IType type = null;
 			Notification not;
 			for (int i = 0; i < flushTypes.size(); i++) {
 				type = (IType) flushTypes.get(i);
 				not = doFlush(type);
 				if (not != null)
 					notifications.add(not);
 			}
 		}
 	}
 	/**
 	 * @param notifications
 	 */
 	private void processNotifications() {
 		Notifier notifier;
 		Notification not;
 		for (int i = 0; i < notifications.size(); i++) {
 			not = (Notification) notifications.get(i);
 			notifier = (Notifier) not.getNotifier();
 			if (notifier != null)
 				try {
 					notifier.eNotify(not);
 				} catch (Exception e) {
 					JavaPlugin.getDefault().getMsgLogger().log(e); //catch exceptions so all notifications are processed
 				} 
 		}
 	}
 	protected void disAssociateSource(String qualifiedName) {
 		Notification not = getAdapterFactory().disAssociateSource(qualifiedName, false);
 		if (not != null)
 			notifications.add(not);
 	}
 	protected void disAssociateSourcePlusInner(String qualifiedName) {
 		Notification not = getAdapterFactory().disAssociateSourcePlusInner(qualifiedName, false);
 		if (not != null)
 			notifications.add(not);
 	}
 	protected void flushAll() {
 		notifications.addAll(getAdapterFactory().flushAllNoNotification());
 		flushedAll = true;
 	}
 	/**
 	 * @see org.eclipse.jem.internal.adapters.jdom.JavaModelListener#processChildren(IJavaElement, IJavaElementDelta)
 	 */
 	protected void processChildren(IJavaElement element, IJavaElementDelta delta) {
 		if (!flushedAll)
 			super.processChildren(element, delta);
 	}
 	/**
 	 * @see org.eclipse.jem.internal.adapters.jdom.JavaModelListener#processDelta(IJavaElementDelta)
 	 */
 	public void processDelta(IJavaElementDelta delta) {
 		if (!flushedAll)
 			super.processDelta(delta);
 	}
 }
