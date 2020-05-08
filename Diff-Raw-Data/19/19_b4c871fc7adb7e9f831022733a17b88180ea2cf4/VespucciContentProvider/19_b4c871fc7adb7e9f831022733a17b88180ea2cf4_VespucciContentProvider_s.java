 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.ui.navigator.providers;
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 
 import de.opalproject.vespucci.datamodel.EnsembleRepository;
 import de.opalproject.vespucci.datamodel.SliceRepository;
 
 /**
  * Provides the datamodel content for the navigator
  * 
  * @author Marco Jacobasch
  * 
  */
 public class VespucciContentProvider extends
 		TransactionalAdapterFactoryContentProvider implements
 		ITreeContentProvider, IResourceChangeListener, IResourceDeltaVisitor {
 
 	private static TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
 			.getEditingDomain("de.opalproject.vespucci.navigator.domain.DatamodelEditingDomain");
 	private static ResourceSet resourceSet = domain.getResourceSet();
 
 	public VespucciContentProvider() {
 		super(domain, ProjectAdapterFactoryProvider.getAdapterFactory());
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
 				IResourceChangeEvent.POST_CHANGE);
 	}
 
 	@Override
 	public boolean hasChildren(Object object) {
 		if (object instanceof IFile)
 			return true;
 		boolean r = super.hasChildren(object);
 		return r;
 	}
 
 	@Override
 	public Object[] getChildren(Object object) {
 		if (object instanceof IFile) {
 			String path = ((IFile) object).getFullPath().toString();
 			URI uri = URI.createPlatformResourceURI(path, true);
 			object = resourceSet.getResource(uri, true);
 			return stripDiagrams(super.getChildren(object));
 		}
 		Object[] r = super.getChildren(object);
 		return r;
 	}
 
 	@Override
 	public Object getParent(Object object) {
 		if (object instanceof IFile)
 			return ((IResource) object).getParent();
 		return super.getParent(object);
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	@Override
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 	}
 
 	@Override
 	public void resourceChanged(IResourceChangeEvent event) {
 		try {
 			IResourceDelta delta = event.getDelta();
 			delta.accept(this);
 		} catch (CoreException e) {
 			System.out.println("Resource Changed Fail - " + e.toString());
 		}
 	}
 
 	@Override
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		IResource changedResource = delta.getResource();
		if (changedResource.getType() == IResource.FILE
				&& changedResource.getFileExtension().equals("ecore")) {
 			try {
 				String path = ((IFile) changedResource).getFullPath()
 						.toString();
 				URI uri = URI.createPlatformResourceURI(path, true);
 				Resource res = resourceSet.getResource(uri, true);
 				res.unload();
 				res.load(resourceSet.getLoadOptions());
 			} catch (IOException ie) {
 				System.out.println("Error reloading resource - "
 						+ ie.toString());
 			}
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Used to strips Diagrams from emf resources.
 	 * 
 	 * Remove every object which are not an instance of
 	 * {@link EnsembleRepository} or {@link SliceRepository}.
 	 * 
 	 * TODO find a better way to hide the graphiti diagrams
 	 * 
 	 * @param objects
 	 * @return
 	 */
 	private Object[] stripDiagrams(Object[] objects) {
		Object[] r = new Object[2];
		int index = 0;
 
 		for (int i = 0; i < objects.length; i++) {
 			if (objects[i] instanceof SliceRepository
 					|| objects[i] instanceof EnsembleRepository) {
				r[index] = objects[i];
				index++;
 			}

 		}
 
		return r;
 	}
 
 }
