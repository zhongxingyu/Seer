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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jst.servlet.ui.internal.plugin.WEBUIMessages;
 import org.eclipse.swt.graphics.Image;
 
 public class CompressedJavaLibraries implements ICompressedNode {
 	
 	
 	private CompressedJavaProject compressedProject;
 	private Image image;
 
 	public CompressedJavaLibraries(CompressedJavaProject compressedProject) { 
 		this.compressedProject = compressedProject;
 	}
 	
 	public Image getImage() { 
 		if(image == null) {
 			image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
 		}
 		return image;
 	}
 	
 	public String getLabel() {		
 		return WEBUIMessages.Compressed_Libraries;
 	}
 	
 	public Object[] getChildren(ITreeContentProvider delegateContentProvider) {
 		
 		List classpathContainers = new ArrayList();
 		Object[] delegateChildren = delegateContentProvider.getChildren(compressedProject.getProject());
 		for (int i = 0; i < delegateChildren.length; i++) {
 			if (delegateChildren[i] instanceof IPackageFragmentRoot) {
 				try {
 					IClasspathEntry rawClasspathEntry = ((IPackageFragmentRoot) delegateChildren[i]).getRawClasspathEntry();
 					if (rawClasspathEntry.getEntryKind() != IClasspathEntry.CPE_CONTAINER && 
 								rawClasspathEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE)
 						classpathContainers.add(delegateChildren[i]);
 				} catch (JavaModelException e) {
 
 				}
 			} else if (!(delegateChildren[i] instanceof IJavaElement) && !(delegateChildren[i] instanceof IResource)) {
 				classpathContainers.add(delegateChildren[i]);
 			}
 		}
 		return classpathContainers.toArray();
 	}
 	
 	
 
 }
