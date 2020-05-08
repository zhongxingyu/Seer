 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.editor;
 
import java.util.Iterator;

 import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
 
 /**
  * Manages changes done to the resources tied to the diagram outside of the
  * editor's TransactionalEditingDomain.
  */
 public class DomainModelWorkspaceSynchronizerDelegate implements WorkspaceSynchronizer.Delegate {
 
 	private DiagramEditorBehavior deb;
 
 	/**
 	 * The DiagramEditorBehavior reacts on a setResourceChanged(true) if he gets
 	 * activated.
 	 */
 	public DomainModelWorkspaceSynchronizerDelegate(DiagramEditorBehavior deb) {
 		this.deb = deb;
 	}
 
 	@Override
	public void dispose() {
 		deb = null;
 
 	}
 
 	@Override
 	public boolean handleResourceChanged(Resource resource) {
		ResourceSet resourceSet = resource.getResourceSet();
		EList<Resource> resources = resourceSet.getResources();
		for (Iterator<Resource> iterator = resources.iterator(); iterator.hasNext();) {
			Resource r = iterator.next();
			IFile file = WorkspaceSynchronizer.getFile(r);
			if (file != null && (!file.exists() || file.getLocation().toFile().lastModified() != r.getTimeStamp())) {
				deb.setResourceChanged(true);
				return true;

			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean handleResourceDeleted(Resource resource) {
 		// This will lead to an unload of the resource, which is quite what we want.
 		// As a follow up of the unload the editor is closed.
 		return false;
 	}
 
 	@Override
 	public boolean handleResourceMoved(Resource resource, URI newURI) {
 		// Handle rename or move of resource.
 		// ((ResourceSetImpl)
 		// (ResourceSetimpl(deb.getEditingDomain().getResourceSet())).getURIResourceMap().remove(resource.getURI());
 		// Map<URI, Resource> uriResourceMap = ((ResourceSetImpl)deb.getEditingDomain().getResourceSet()).getURIResourceMap();
 		resource.setURI(newURI);
 		deb.getEditorInput().updateUri(newURI);
 		deb.refreshEditorContent();
 		return true;
 	}
 
 }
