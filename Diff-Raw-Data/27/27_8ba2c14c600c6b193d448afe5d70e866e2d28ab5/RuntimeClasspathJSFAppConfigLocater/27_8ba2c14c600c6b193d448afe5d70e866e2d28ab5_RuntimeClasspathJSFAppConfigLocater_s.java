 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.jsfappconfig;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 
 /**
  * RuntimeClasspathJSFAppConfigLocater attempts to locate application
  * configuration files in JAR files on the runtime classpath. The runtime
  * classpath includes the server runtime classpath and the JAR files that will
  * be deployed to the web application's /WEB-INF/lib folder.
  * 
  * @author Ian Trimble - Oracle
  */
 public class RuntimeClasspathJSFAppConfigLocater extends AbstractJSFAppConfigLocater
 	implements IElementChangedListener {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.jsfappconfig.AbstractJSFAppConfigLocater#startLocating()
 	 */
 	public void startLocating() {
 		locateProviders();
 		JavaCore.addElementChangedListener(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.jsfappconfig.AbstractJSFAppConfigLocater#stopLocating()
 	 */
 	public void stopLocating() {
 		JavaCore.removeElementChangedListener(this);
 	}
 
 	/**
 	 * Locates application configuration resources specified in JAR files on
 	 * the runtime classpath, and updates the set of
 	 * {@link IJSFAppConfigProvider} instances accordingly.
 	 */
 	public void locateProviders() {
 		try {
 			List JARs = JSFAppConfigUtils.getConfigFileJARsFromClasspath(manager.getProject());
 			Iterator itJARs = JARs.iterator();
 			Set newConfigProviders = new LinkedHashSet();
 			while (itJARs.hasNext()) {
 				String JARFilename = (String)itJARs.next();
 				JARFileJSFAppConfigProvider configProvider = new JARFileJSFAppConfigProvider(JARFilename);
 				newConfigProviders.add(configProvider);
 			}
 			updateConfigProviders(newConfigProviders);
 		} catch(CoreException ce) {
 			//log error
 			JSFCorePlugin.log(IStatus.ERROR, ce.getLocalizedMessage(), ce);
 		} catch(IOException ioe) {
 			//log error
 			JSFCorePlugin.log(IStatus.ERROR, ioe.getLocalizedMessage(), ioe);
 		}
 	}
 
 	/**
 	 * Called when a Java element has changed.
 	 * 
 	 * @param event ElementChangedEvent instance describing the change.
 	 */
 	public void elementChanged(ElementChangedEvent event) {
        // TODO: should this ever fire if it's not the project expected?
        // should the listener have been deregistered?
        final IJavaProject javaProject = event.getDelta().getElement().getJavaProject();
        if (javaProject != null
                && javaProject.getProject().isAccessible()
                && this.manager.getProject().isAccessible())
        {
            if (event.getDelta().getElement().getJavaProject().getProject().isAccessible())
            {
        		if (classpathChanged(event.getDelta())) {
        			locateProviders();
        		}
            }
        }
 	}
 
 	/**
 	 * Recursively tests if the passed IJavaElementDelta instance or any of its
 	 * descendents indicate a classpath change has occurred.
 	 * 
 	 * @param delta IJavaElement instance to be tested.
 	 * @return true if a claspath change has occurred, else false.
 	 */
 	protected boolean classpathChanged(IJavaElementDelta delta) {
 		int deltaFlags = delta.getFlags();
 		if (((deltaFlags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) == IJavaElementDelta.F_ADDED_TO_CLASSPATH) ||
 				((deltaFlags & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) == IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) ||
 				((deltaFlags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) == IJavaElementDelta.F_REMOVED_FROM_CLASSPATH)) {
 			return true;
 		}  
 		boolean changed = false;
 		IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
 		if (childDeltas != null) {
 			for (int i = 0; i < childDeltas.length; i++) {
 				if (classpathChanged(childDeltas[i])) {
 					changed = true;
 					break;
 				}
 			}
 		}
 		return changed;
 	}
 }
