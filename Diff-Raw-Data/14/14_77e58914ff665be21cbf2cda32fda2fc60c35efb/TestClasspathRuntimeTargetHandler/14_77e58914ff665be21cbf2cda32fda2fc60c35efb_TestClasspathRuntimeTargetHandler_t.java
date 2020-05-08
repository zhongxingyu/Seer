 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.core.tests.impl;
 
 import java.util.*;
 import java.io.File;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.server.core.ClasspathRuntimeTargetHandler;
 import org.eclipse.wst.server.core.IRuntime;
 /**
  * 
  */
 public class TestClasspathRuntimeTargetHandler extends ClasspathRuntimeTargetHandler {
 	public String getClasspathContainerLabel(IRuntime runtime, String id) {
 		return null;
 	}
 
 	public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime, String id) {
 		return null;
 	}
 	
 	public void testAddMethods() {
 		List list = new ArrayList();
		try {
			addJarFiles(null, list, false);
			addLibraryEntries(list, null, false);
			addLibraryEntry(list, (File) null);
			addLibraryEntry(list, (IPath) null);
			addLibraryEntry(list, null, null, null);
		} catch (Exception e) {
			// ignore
		}
 	}
 }
