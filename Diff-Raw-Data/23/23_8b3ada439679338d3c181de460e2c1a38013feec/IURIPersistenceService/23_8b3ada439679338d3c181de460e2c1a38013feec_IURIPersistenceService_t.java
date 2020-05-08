 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.persistence.interfaces;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.eclipse.tcf.te.runtime.services.interfaces.IService;
 
 /**
  * A service for persisting elements to a URI persistence store.
  */
 public interface IURIPersistenceService extends IService {
 
 	/**
 	 * Writes the given context object via a persistence delegate to a persistence storage.
 	 * If the given URI is <code>null</code>, it will be determined by adapting the context
 	 * to a {@link IPersistableURIProvider}
 	 *
 	 * @param context The context object. Must not be <code>null</code>.
 	 * @param uri The URI or <code>null</code>.
 	 *
 	 * @throws IOException - if the operation fails.
 	 */
 	public void write(Object context, URI uri) throws IOException;
 
 	/**
 	 * Reads a context object via a persistence delegate from a given persistence storage.
 	 * If the given URI is <code>null</code>, it will be determined by adapting the context
 	 * to a {@link IPersistableURIProvider}. In this case the context must not be <code>null</code>.
 	 *
 	 * @param context The context object or <code>null</code>.
	 * @param uri The URI. Must not be <code>null</code>.
 	 *
 	 * @throws IOException - if the operation fails
 	 */
	public Object read(Object context, URI uri) throws IOException;
 
 	/**
 	 * Deletes the persistence storage for the given context object via a persistence delegate.
 	 * If the given URI is <code>null</code>, it will be determined by adapting the context
 	 * to a {@link IPersistableURIProvider}
 	 *
 	 * @param context The context object. Must not be <code>null</code>.
 	 * @param uri The URI or <code>null</code>.
 	 *
 	 * @return <code>True</code> if the persistence storage is successfully deleted;
 	 *         <code>false</code> otherwise.
 	 *
 	 * @throws IOException - if the operation fails
 	 */
 	public boolean delete(Object context, URI uri) throws IOException;
 
 	/**
 	 * Returns the persistence storage URI for the given context object. The persistence delegate to
 	 * use will be determined by adapting the given context object to an {@link IPersistable}.
 	 *
 	 * @param context The context object. Must not be <code>null</code>.
 	 * @return The URI or <code>null</code>.
 	 *
 	 * @throws IOException - if the operation fails
 	 */
 	public URI getURI(Object context) throws IOException;
 }
