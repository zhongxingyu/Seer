 /*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.core.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
 
 import org.eclipse.wst.server.core.IRuntime;
 /**
  * 
  */
 public class GenericRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate {
 	/** (non-Javadoc)
	 * @see RuntimeClasspathProviderDelegate#resolveClasspathContainer(IProject, IRuntime)
 	 */
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
 		IPath installPath = runtime.getLocation();
 		
 		if (installPath == null)
 			return new IClasspathEntry[0];
 		
 		List list = new ArrayList();
 		addLibraryEntries(list, installPath.toFile(), false);
 		return (IClasspathEntry[])list.toArray(new IClasspathEntry[list.size()]);
 	}
 }
