 /*******************************************************************************
  * Copyright (c) 2011, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.utils;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ide.EMFCompareIDEPlugin;
 import org.eclipse.emf.compare.utils.DelegatingURIConverter;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.URIHandler;
 
 /**
  * This implementation of an URI converter will keep track of the storages from which it created input
  * streams.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class StorageURIConverter extends DelegatingURIConverter {
 	/** Keeps references towards the revisions that we've loaded through this URI converter. */
 	private Set<IStorage> loadedRevisions;
 
 	/**
 	 * Constructs our uri converter given its delegate.
 	 * 
 	 * @param delegate
 	 *            The delegate URI Converter.
 	 */
 	public StorageURIConverter(URIConverter delegate) {
 		super(delegate);
 		this.loadedRevisions = Sets.newLinkedHashSet();
 	}
 
 	/**
 	 * Allows clients of this API to retrieve the set of revisions that were loaded while resolving the
 	 * resource set on which this converter is installed.
 	 * 
 	 * @return The set of revisions loaded through this converter.
 	 */
 	public Set<IStorage> getLoadedRevisions() {
 		return loadedRevisions;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.utils.DelegatingURIConverter#createInputStream(org.eclipse.emf.common.util.URI,
 	 *      java.util.Map)
 	 */
 	@Override
 	public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
 		final URI normalizedURI = normalize(uri);
 		final URIHandler handler = getURIHandler(normalizedURI);
 		getLoadedRevisions().add(createStorage(normalizedURI, handler, this));
 
 		final Map<Object, Object> actualOptions = Maps.newLinkedHashMap();
 		actualOptions.put(URIConverter.OPTION_URI_CONVERTER, this);
 		if (options != null) {
 			actualOptions.putAll(options);
 		}
 		return handler.createInputStream(normalizedURI, actualOptions);
 	}
 
 	/**
 	 * Creates a new IStorage for the given URI.
 	 * 
 	 * @param uri
 	 *            The uri for which we need a storage.
 	 * @param handler
 	 *            The URI handler that can be used to retrieve this URI's contents.
 	 * @param converter
 	 *            The URI converter from which this storage was created.
 	 * @return The newly created IStorage.
 	 */
 	protected IStorage createStorage(URI uri, URIHandler handler, URIConverter converter) {
 		return new URIStorage(uri, handler, converter);
 	}
 
 	/**
 	 * This implementation of an {@link IStorage} will allow us to keep track of the {@link URIHandler} that's
 	 * been used to load a given URI from this uri converter.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private class URIStorage implements IStorage {
 		/** The target URI of this storage. */
 		private final URI uri;
 
 		/** The URI Handler that's been used to retrieve this URI's contents. */
 		private final URIHandler handler;
 
 		/** The URI converter from which this storage was created. */
 		private URIConverter converter;
 
 		/**
 		 * Creates an URIStorage for the given URI an its associated handler.
 		 * 
 		 * @param uri
 		 *            The target uri of this storage.
 		 * @param handler
 		 *            The URI handler that can be used to retrieve this URI's contents.
 		 * @param converter
 		 *            The URI converter which created this storage.
 		 */
 		public URIStorage(URI uri, URIHandler handler, URIConverter converter) {
 			this.uri = uri;
 			this.handler = handler;
 			this.converter = converter;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 		 */
 		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 			return null;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.resources.IStorage#getContents()
 		 */
 		public InputStream getContents() throws CoreException {
 			final Map<?, ?> options = Collections.singletonMap(URIConverter.OPTION_URI_CONVERTER, converter);
 			try {
 				return handler.createInputStream(uri, options);
 			} catch (IOException e) {
 				throw new CoreException(new Status(IStatus.ERROR, EMFCompareIDEPlugin.PLUGIN_ID, e
 						.getMessage(), e));
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.resources.IStorage#getFullPath()
 		 */
 		public IPath getFullPath() {
 			final String path;
 			if (uri.isRelative()) {
 				path = uri.toString();
 			} else if (uri.isPlatform()) {
 				path = uri.toPlatformString(true);
 			} else {
 				path = uri.toString();
 			}
 			return new Path(path);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.resources.IStorage#getName()
 		 */
 		public String getName() {
			return uri.lastSegment();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.resources.IStorage#isReadOnly()
 		 */
 		public boolean isReadOnly() {
 			final Map<?, ?> options = Collections.singletonMap(URIConverter.OPTION_REQUESTED_ATTRIBUTES,
 					Collections.singleton(URIConverter.ATTRIBUTE_READ_ONLY));
 			final Map<String, ?> attributes = handler.getAttributes(uri, options);
 			return Boolean.TRUE.equals(attributes.get(URIConverter.ATTRIBUTE_READ_ONLY));
 		}
 	}
 }
