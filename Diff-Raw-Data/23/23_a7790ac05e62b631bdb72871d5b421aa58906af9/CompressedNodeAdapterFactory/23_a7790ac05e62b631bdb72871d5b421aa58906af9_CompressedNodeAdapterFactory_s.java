 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.servlet.ui.internal.navigator;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.jdt.core.IJavaProject;
 
 // 
 public class CompressedNodeAdapterFactory implements IAdapterFactory {
 	
 	private static final Class IJAVA_PROJECT_CLASS = IJavaProject.class;
 	
 	private static final Class[] ADAPTER_LIST = new Class[] { IJAVA_PROJECT_CLASS };
 
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType == IJAVA_PROJECT_CLASS) {
			if(adaptableObject instanceof CompressedJavaProject)
 				return ((CompressedJavaProject)adaptableObject).getProject();
 		}
 		return null;
 	}
 
 	public Class[] getAdapterList() { 
 		return ADAPTER_LIST;
 	}
 
 }
