 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.adapters.jdom;
 /*
  *  $RCSfile: JavaJDOMAdapterFactory.java,v $
 *  $Revision: 1.6 $  $Date: 2005/08/24 21:13:53 $ 
  */
 import java.util.*;
 
 import org.eclipse.emf.common.notify.*;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jem.internal.java.adapters.*;
 import org.eclipse.jem.internal.java.adapters.JavaReflectionAdapterFactory;
 import org.eclipse.jem.internal.java.adapters.ReflectionAdaptor;
 /**
  * Insert the type's description here.
  * Creation date: (6/13/2000 1:20:31 PM)
  * @author: Administrator
  */
 public class JavaJDOMAdapterFactory extends JavaReflectionAdapterFactory {
 	
 	protected JavaReflectionSynchronizer synchronizer;
 	protected IJavaProject javaProject;
 /**
  * JavaJDOMAdapterFactory constructor comment.
  */
 public JavaJDOMAdapterFactory() {
 	super();
 }
 /**
  * JavaJDOMAdapterFactory constructor comment.
  */
 public JavaJDOMAdapterFactory(IJavaProject aJavaProject) {
 	this();
 	setJavaProject(aJavaProject);
 }
 protected ReflectionAdaptor createJavaClassAdaptor(Notifier target) {
 	return new JavaClassJDOMAdaptor(target, getJavaProject(), this);
 }
 protected ReflectionAdaptor createJavaFieldAdaptor(Notifier target) {
 	return new JavaFieldJDOMAdaptor(target, getJavaProject());
 }
 protected ReflectionAdaptor createJavaMethodAdaptor(Notifier target) {
 	return new JavaMethodJDOMAdaptor(target, getJavaProject());
 }
 /**
  *  Flush ALL adapters, worst case
  * We also want to ensure that the source types are also cleared
  * in this worst case scenario.
  */
 public void flushAll() {
 	doFlush(reflected.values(),true, true);
 }
 
 public List flushAllNoNotification() {
 	return doFlush(reflected.values(),true, false);
 }
 
 public void flushPackage(String packageName, boolean noFlushIfSourceFound) {
 	List adaptors = getReflectedForPackage(packageName, noFlushIfSourceFound);
 	doFlush(adaptors, true, true);
 }
 public List flushPackageNoNotification(String packageName, boolean noFlushIfSourceFound) {
 	List adaptors = getReflectedForPackage(packageName, noFlushIfSourceFound);
 	return doFlush(adaptors, true, false);
 }
 /**
  * Return a List of reflection adaptors that belong to 
  * the packageName.
  * @param packageName
  * @return
  */
 private List getReflectedForPackage(String packageName, boolean filterFoundTypes) {
 	if (packageName != null && !reflected.isEmpty()) {
 		isBusyIteratingReflected = true;
 		List result = null;
 		try {
 			Iterator it = reflected.entrySet().iterator();
 			Map.Entry entry;
 			String key;
 			JavaClassJDOMAdaptor adaptor;
 			while (it.hasNext()) {
 				entry = (Map.Entry) it.next();
 				adaptor = (JavaClassJDOMAdaptor) entry.getValue();
 				if (filterFoundTypes && adaptor.sourceType != null)
 					continue;
 				key = (String) entry.getKey();
 				if (key.startsWith(packageName) && key.indexOf('.', packageName.length() + 1) < 0) {
 					if (result == null)
 						result = new ArrayList();
 					result.add(entry.getValue());
 				}	
 			}
 		} finally {
 			finishedIteratingReflected();
 		}
 		if (result != null)
 			return result;
 	}
 	return Collections.EMPTY_LIST;
 }
 private List doFlush(Collection adaptors, boolean clearSourceType, boolean doNotify) {
 	if (!adaptors.isEmpty()) {
 		isBusyIteratingReflected = true;
 		List notifications = doNotify ? null : new ArrayList(adaptors.size());
 		try {
 			Notification notification;
 			Iterator i = adaptors.iterator();
 			JDOMAdaptor adaptor;
 			while (i.hasNext()) {
 				adaptor = (JDOMAdaptor) i.next();
 				if (doNotify)
 					adaptor.flushReflectedValuesIfNecessary(clearSourceType);
 				else {
 					notification = adaptor.flushReflectedValuesIfNecessaryNoNotification(clearSourceType);
 					if (notification != null)
 						notifications.add(notification);
 				}
 			}
 		} finally {
 			finishedIteratingReflected();
 		}
 		return notifications;
 	}
 	return Collections.EMPTY_LIST;
 }
 // Flush the adapter for a source object
 public void flushReflection(String source) {
 	JDOMAdaptor a = (JDOMAdaptor) reflected.get(source);
 	if (a != null)
 		a.flushReflectedValuesIfNecessary();
 }
 public Notification flushReflectionNoNotification(String source) {
 	JDOMAdaptor a = (JDOMAdaptor) reflected.get(source);
 	if (a != null)
 		return a.flushReflectedValuesIfNecessaryNoNotification(false);
 	return null;
 }
 
 public Notification flushReflectionPlusInnerNoNotification(String source) {
 	isBusyIteratingReflected = true;
 	Notification notification = null;
 	try {
 		String innerName = source + '$';
 		Iterator it = reflected.entrySet().iterator();
 		Map.Entry entry;
 		String key;
 		JavaReflectionAdaptor adaptor;
 		while (it.hasNext()) {
 			entry = (Map.Entry) it.next();
 			key = (String) entry.getKey();
 			if (key.equals(source) || key.startsWith(innerName)) {
 				adaptor = (JavaReflectionAdaptor) reflected.get(key);
 				if (adaptor != null) {
 					if (notification == null)
 						notification = adaptor.flushReflectedValuesIfNecessaryNoNotification(false);
 					else
 						((NotificationChain) notification).add(adaptor.flushReflectedValuesIfNecessaryNoNotification(false));
 				}
 			}
 		}
 	} finally {
 		finishedIteratingReflected();
 	}
 	return notification;
 
 }
 /**
  * Insert the method's description here.
  * Creation date: (11/2/2000 3:02:31 PM)
  * @return org.eclipse.jdt.core.api.IJavaProject
  */
 public IJavaProject getJavaProject() {
 	return javaProject;
 }
 /**
  * Create a Java Model listener which will flush invalidated adaptors.
  * This will cause those adapters to re-reflect their target object's contents.
  */
 protected void initializeSynchronizer() {
 	synchronizer = new JavaReflectionSynchronizer(this);
 }
 /**
  * Notify all JDOMAdapters which use the same target ICompilationUnit
  * Creation date: (8/17/2001 4:45:43 PM)
  */
 public void notifyContentChanged(ICompilationUnit targetCU) {
 
 	if (targetCU == null || reflected.values()==null) return ;
 	isBusyIteratingReflected = true;
 	try {
 		Iterator i = reflected.values().iterator();
 		while (i.hasNext()) {
			Object a = (ReflectionAdaptor) i.next() ;
 			if (a instanceof JDOMAdaptor) {
 				JDOMAdaptor adaptor = (JDOMAdaptor) a;
 				IMember reflectionSource = (IMember) adaptor.getReflectionSource();
 				ICompilationUnit adapterCU = null ;
 				if (reflectionSource != null) {
 					try {
 					   adapterCU = reflectionSource.getCompilationUnit();
 					} catch (Throwable e) {} 
 				}
 				if (adapterCU != null && targetCU.equals(adapterCU)) {
 			       adaptor.contentChanged();
 				}
 			}
 		}
 	} finally {
 		finishedIteratingReflected();  	
 	}
 }
 /**
  * Insert the method's description here.
  * Creation date: (11/2/2000 3:02:31 PM)
  * @param newJavaProject org.eclipse.jdt.core.IJavaProject
  */
 public void setJavaProject(IJavaProject newJavaProject) {
 	javaProject = newJavaProject;
 	if (newJavaProject != null && synchronizer == null)
 		initializeSynchronizer();
 }
 }
