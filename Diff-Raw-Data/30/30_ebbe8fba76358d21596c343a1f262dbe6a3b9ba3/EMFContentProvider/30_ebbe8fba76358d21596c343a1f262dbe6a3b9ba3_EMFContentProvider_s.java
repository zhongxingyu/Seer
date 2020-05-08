 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 
 package org.eclipse.emf.compare.ui;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 
 /**
  * Generic emf content provider
  * 
 * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public class EMFContentProvider extends AdapterFactoryContentProvider {
 
 	private ResourceSet resourceSet;
 
 	/**
 	 * Initializer
 	 * 
 	 */
 	public EMFContentProvider() {
 		super(EMFAdapterFactoryProvider.getAdapterFactory());
 		this.resourceSet = new ResourceSetImpl();
 	}
 
 	public boolean hasChildren(final Object object) {
 		if (object instanceof IFile) {
 			return true;
 		}
 		return super.hasChildren(object);
 	}
 
 	public Object[] getChildren(Object object) {
 		if (object instanceof IFile) {
 			final String path = ((IFile) object).getFullPath().toString();
 			final URI uri = URI.createPlatformResourceURI(path);
 			object = this.resourceSet.getResource(uri, true);
 		}
 		return super.getChildren(object);
 	}
 
 	public Object getParent(final Object object) {
 		if (object instanceof IFile) {
 			return ((IResource) object).getParent();
 		}
 		return super.getParent(object);
 	}
 
 	public Object[] getElements(Object object) {
 		if (object instanceof IFile) {
 			final String path = ((IFile) object).getFullPath().toString();
 			final URI uri = URI.createPlatformResourceURI(path);
 			object = this.resourceSet.getResource(uri, true);
 		}
 		return super.getElements(object);
 	}
 }
