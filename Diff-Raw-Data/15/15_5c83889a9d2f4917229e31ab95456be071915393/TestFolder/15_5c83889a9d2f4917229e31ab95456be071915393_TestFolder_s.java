 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Andrei Sobolev)
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.model;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.internal.core.ProjectFragment;
 import org.eclipse.dltk.internal.core.ScriptFolder;
 
 public class TestFolder extends ScriptFolder {
	protected TestFolder(ProjectFragment parent, IPath path) {
 		super(parent, path);
 	}
 
 	public String getElementName() {
 		return super.getElementName() + "Goo";
 	}
 
 	public boolean isRootFolder() {
 		return false;
 	}
 
 	public IResource getResource() {
 		IProjectFragment root = this.getProjectFragment();
 		if (root.isArchive()) {
 			return root.getResource();
 		} else {
 			if (path.segmentCount() == 0)
 				return root.getResource();
 			IContainer container = (IContainer) root.getResource();
 			if (container != null) {
 				return container.getFolder(path);
 			}
 			return null;
 		}
 	}
 
 	public IPath getPath() {
 		return super.getPath().append("Goo");
 	}
 
 	protected char getHandleMementoDelimiter() {
 		return JEM_USER_ELEMENT;
 	}
 }
