 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.persistence.services;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
 import org.eclipse.tcf.te.runtime.services.AbstractService;
 
 /**
  * Persistence service implementation.
  */
 public class URIPersistenceService extends AbstractService implements IURIPersistenceService {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService#write(java.lang.Object, java.net.URI)
 	 */
 	@Override
 	public void write(Object context, URI uri) throws IOException {
 		Assert.isNotNull(context);
 
 		uri = uri != null ? uri : getURI(context);
 
 		// Determine the persistence delegate
 		IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(context, uri, false);
 		// If the persistence delegate could not be determined, throw an IOException
 		if (delegate == null) {
 			throw new IOException("The persistence delegate for context '" + context.getClass().getName() + "' cannot be determined."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		// Pass on to the delegate for writing
 		delegate.write(context, uri, null);
 	}
 
 	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService#read(java.net.URI, java.lang.Object)
 	 */
 	@Override
	public Object read(URI uri, Object context) throws IOException {
 		Assert.isNotNull(context);
 
 		uri = uri != null ? uri : getURI(context);
 
 		// Determine the persistence delegate
 		IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(context, uri, false);
 		// If the persistence delegate could not be determined, throw an IOException
 		if (delegate == null) {
 			throw new IOException("The persistence delegate for context '" + context.getClass().getName() + "' cannot be determined."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		// Pass on to the delegate for reading
 		return delegate.read(context, uri, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService#delete(java.lang.Object, java.net.URI)
 	 */
 	@Override
 	public boolean delete(Object context, URI uri) throws IOException {
 		Assert.isNotNull(context);
 
 		uri = uri != null ? uri : getURI(context);
 
 		// Determine the persistence delegate
 		IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(context, uri, false);
 		// If the persistence delegate could not be determined, throw an IOException
 		if (delegate == null) {
 			throw new IOException("The persistence delegate for context '" + context.getClass().getName() + "' cannot be determined."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		return delegate.delete(context, uri, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService#getURI(java.lang.Object)
 	 */
 	@Override
 	public URI getURI(Object context) throws IOException {
 		Assert.isNotNull(context);
 
 		// Determine the persistable element for the given data object
 		IPersistableURIProvider persistableURIProvider = context instanceof IPersistableURIProvider ? (IPersistableURIProvider)context : null;
 		// If the element isn't a persistable by itself, try to adapt the element
 		if (persistableURIProvider == null) {
 			persistableURIProvider = context instanceof IAdaptable ? (IPersistableURIProvider) ((IAdaptable)context).getAdapter(IPersistableURIProvider.class) : null;
 		}
 		if (persistableURIProvider == null) {
 			persistableURIProvider = (IPersistableURIProvider) Platform.getAdapterManager().getAdapter(context, IPersistableURIProvider.class);
 		}
 
 		// If the persistable could be still not determined, throw an IOException
 		if (persistableURIProvider == null) {
 			throw new IOException("'context' must be adaptable to IPersistableURIProvider."); //$NON-NLS-1$
 		}
 
 		URI uri = persistableURIProvider.getURI(context);
 
 		if (uri == null) {
 			throw new IOException("The URI cannot be determined for context '" + context.getClass().getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		// Determine the URI
 		return uri;
 	}
 }
