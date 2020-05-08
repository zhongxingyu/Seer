 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.dialog;
 
 import java.text.Collator;
 
 import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jst.jsf.common.ui.IFileFolderConstants;
 
 /**
  * @author sfshi
  * @version
  */
 public class MessageBundleViewerSorter extends ViewerSorter {
 
 	/**
 	 * 
 	 */
 	public MessageBundleViewerSorter() {
 		super();
 	}
 
 	/**
 	 * @param collator
 	 */
 	public MessageBundleViewerSorter(Collator collator) {
 		super(collator);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
 	 */
 	public int category(Object element) {
 		if (element instanceof IPackageFragmentRoot
				&& !(element instanceof JarPackageFragmentRoot)) {
 			IPackageFragmentRoot root = (IPackageFragmentRoot) element;
 			if (IFileFolderConstants.FOLDER_SOURCE
 					.equals(root.getElementName())) {
 				return 0;
 			}
 			return 1;
 		}
 
 		return 2;
 	}
 }
