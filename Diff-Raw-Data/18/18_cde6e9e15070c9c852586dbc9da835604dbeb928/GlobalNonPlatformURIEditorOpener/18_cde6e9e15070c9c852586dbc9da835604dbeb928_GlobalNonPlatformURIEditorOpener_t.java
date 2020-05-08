 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.core.ui;
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.xtext.ui.editor.GlobalURIEditorOpener;
 
 /**
  * This class is needed since the URIs we get are non-platform URIs. In this case we have to convert them if possible.
  * 
  * @author Henrik Rentz-Reichert
  *
  */
 public class GlobalNonPlatformURIEditorOpener extends GlobalURIEditorOpener {
 
 	@Override
 	public IEditorPart open(URI referenceOwnerURI, EReference reference, int indexInList, boolean select) {
 		return super.open(getPlatformURI(referenceOwnerURI), reference, indexInList, select);
 	}
 	
 	@Override
 	public IEditorPart open(URI uri, boolean select) {
 		return super.open(getPlatformURI(uri), select);
 	}
 	
 	private URI getPlatformURI(URI uri) {
 		if (uri.isPlatform())
 			return uri;
 		
		// HOWTO: find absolute path location in workspace (as platform URI)
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = root.findFilesForLocationURI(java.net.URI.create(uri.toString()));
		if (files.length!=0) {
			String pluri = files[0].toString();
			// the pluri starts with L/ which we have to omit for URI.createPlatformResourceURI
			uri = URI.createPlatformResourceURI(pluri.substring(2), true);
 		}
 		return uri;
 	}
 }
