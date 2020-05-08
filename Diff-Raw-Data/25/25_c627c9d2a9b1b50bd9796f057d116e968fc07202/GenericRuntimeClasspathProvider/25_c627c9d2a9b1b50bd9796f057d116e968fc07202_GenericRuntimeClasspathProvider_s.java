 /*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
 
 import org.eclipse.wst.server.core.IRuntime;
 /**
  * 
  */
 public class GenericRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate {
	/**
	 * @see RuntimeClasspathProviderDelegate#getClasspathContainerLabel(IRuntime, String)
	 */
	public String getClasspathContainerLabel(IRuntime runtime, String id) {
		return runtime.getRuntimeType().getName();
	}

 	/** (non-Javadoc)
	 * @see RuntimeClasspathProviderDelegate#resolveClasspathContainer(IRuntime, String)
 	 */
	public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime, String id) {
 		IPath installPath = runtime.getLocation();
 		
 		if (installPath == null)
 			return new IClasspathEntry[0];
 		
 		List list = new ArrayList();
 		addLibraryEntries(list, installPath.toFile(), false);
 		return (IClasspathEntry[])list.toArray(new IClasspathEntry[list.size()]);
 	}
 }
