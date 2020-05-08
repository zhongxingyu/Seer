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
 
 import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
 		
		Path path = new Path(uri.toFileString());
		IPath base = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if (base.isPrefixOf(path)) {
			IPath relative = path.makeRelativeTo(base);
			uri = URI.createPlatformResourceURI(relative.toString(), false).appendFragment(uri.fragment());
 		}
 		return uri;
 	}
 }
