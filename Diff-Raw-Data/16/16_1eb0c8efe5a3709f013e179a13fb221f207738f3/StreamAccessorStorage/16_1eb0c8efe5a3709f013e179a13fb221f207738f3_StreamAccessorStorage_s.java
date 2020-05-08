 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.logical;
 
 import static org.eclipse.emf.compare.ide.ui.internal.util.PlatformElementUtil.adaptAs;
 import static org.eclipse.emf.compare.ide.ui.internal.util.PlatformElementUtil.findFile;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URI;
 
 import org.eclipse.compare.ISharedDocumentAdapter;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.core.variants.CachedResourceVariant;
 import org.eclipse.team.core.variants.IResourceVariant;
 import org.eclipse.ui.IEditorInput;
 
 /**
  * This implementation of IStorage simply wraps a stream content accessor.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class StreamAccessorStorage implements IStorage {
 	/** The wrapped accessor. */
 	private final IStreamContentAccessor accessor;
 
 	/** Full path of the underlying content. */
 	private final String fullPath;
 
 	/**
 	 * Wraps the given accessor.
 	 * 
 	 * @param accessor
 	 *            The accessor to wrap as an IStorage.
 	 * @param fullPath
 	 *            Full path to the underlying storage.
 	 */
 	public StreamAccessorStorage(IStreamContentAccessor accessor, String fullPath) {
 		this.accessor = accessor;
 		this.fullPath = fullPath;
 	}
 
 	/**
 	 * This is a short-hand for {@link #fromTypedElement(String, ITypedElement)}. This second one should be
 	 * preferred in case the given element is remote and we need a proper path for it.
 	 * 
 	 * @param element
 	 *            The typed element for which we need to create a wrapper.
 	 * @return The wrapped typed element.
 	 * @throws IllegalArgumentException
 	 *             If the given element does not implement {@link IStreamContentAccessor}.
 	 */
 	public static StreamAccessorStorage fromTypedElement(ITypedElement element)
 			throws IllegalArgumentException {
 		return fromTypedElement(null, element);
 	}
 
 	/**
 	 * Creates a StreamAccessorStorage given the input typed element. Note that the given typed element -must-
 	 * implement {@link IStreamContentAccessor} as well.
 	 * 
 	 * @param storagePath
 	 *            The full path to this storage, can be <code>null</code>.
 	 * @param element
 	 *            The typed element for which we need to create a wrapper.
 	 * @return The wrapped typed element.
 	 * @throws IllegalArgumentException
 	 *             If the given element does not implement {@link IStreamContentAccessor}.
 	 */
 	public static StreamAccessorStorage fromTypedElement(String storagePath, ITypedElement element)
 			throws IllegalArgumentException {
 		if (!(element instanceof IStreamContentAccessor)) {
 			throw new IllegalArgumentException();
 		}
 
 		final String fullPath;
 		if (storagePath != null) {
 			fullPath = storagePath;
 		} else {
 			fullPath = findPath(element);
 		}
 
 		return new StreamAccessorStorage((IStreamContentAccessor)element, fullPath);
 	}
 
 	/**
 	 * This will tries and find a path for the given typed element. If that element can be adapted as an
 	 * IResource, we'll use that resource's path. Otherwise, we'll try and adapt it to a File Revision and use
 	 * that revision's path.
 	 * 
 	 * @param element
 	 *            The element for which we need a path.
 	 * @return A path for the given element.
 	 */
 	private static String findPath(ITypedElement element) {
 		final String fullPath;
 		final IFile file = findFile(element);
 		if (file != null) {
 			fullPath = file.getFullPath().toString();
 		} else {
 			final IFileRevision revision = findFileRevision(element);
 			String tmp = null;
 			if (revision != null) {
 				final URI uri = revision.getURI();
 				if (uri != null) {
					tmp = uri.toString();
 				} else if (revision instanceof IAdaptable) {
 					final IResourceVariant variant = (IResourceVariant)((IAdaptable)revision)
 							.getAdapter(IResourceVariant.class);
 					if (variant instanceof CachedResourceVariant) {
 						tmp = ((CachedResourceVariant)variant).getDisplayPath().toString();
 					}
 				}
 			}
 
 			if (tmp != null) {
 				fullPath = tmp;
 			} else {
 				// We can't do much here...
 				fullPath = element.getName();
 			}
 		}
 		return fullPath;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	// suppressing since overriding.
 	@SuppressWarnings({"rawtypes", "unchecked" })
 	public Object getAdapter(Class adapter) {
 		Object adapted = null;
 		if (adapter.isInstance(this)) {
 			adapted = this;
 		} else if (adapter == IStreamContentAccessor.class) {
 			adapted = accessor;
 		} else if (accessor instanceof ITypedElement) {
 			if (adapter == ITypedElement.class) {
 				adapted = accessor;
 			} else if (adapter.isAssignableFrom(IFile.class)) {
 				adapted = findFile((ITypedElement)accessor);
 			}
 		}
 		return adapted;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IStorage#getContents()
 	 */
 	public InputStream getContents() throws CoreException {
 		return accessor.getContents();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IStorage#getFullPath()
 	 */
 	public IPath getFullPath() {
 		return new Path(fullPath);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IStorage#getName()
 	 */
 	public String getName() {
 		if (accessor instanceof ITypedElement) {
 			return ((ITypedElement)accessor).getName();
 		}
 		return getFullPath().lastSegment();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IStorage#isReadOnly()
 	 */
 	public boolean isReadOnly() {
 		if (accessor instanceof ITypedElement) {
 			final IFile file = findFile((ITypedElement)accessor);
 			if (file != null) {
 				return file.isReadOnly();
 			}
 		}
 		final File file = getFullPath().toFile();
 		return !file.exists() || !file.canWrite();
 	}
 
 	/**
 	 * Try and determine the file revision of the given element.
 	 * 
 	 * @param element
 	 *            The element for which we need an {@link IFileRevision}.
 	 * @return The file revision of the given element if we could find one, <code>null</code> otherwise.
 	 */
 	private static IFileRevision findFileRevision(ITypedElement element) {
 		if (element == null) {
 			return null;
 		}
 
 		// Can we adapt it directly?
 		IFileRevision revision = adaptAs(element, IFileRevision.class);
 		if (revision == null) {
 			// Quite the workaround... but CVS does not offer us any other way.
 			// These few lines of code is what make us depend on org.eclipse.ui... Can we find another way?
 			final ISharedDocumentAdapter documentAdapter = adaptAs(element, ISharedDocumentAdapter.class);
 			if (documentAdapter != null) {
 				final IEditorInput editorInput = documentAdapter.getDocumentKey(element);
 				if (editorInput != null) {
 					revision = adaptAs(editorInput, IFileRevision.class);
 				}
 			}
 		}
 
 		if (revision == null) {
 			// Couldn't do it the API way ...
 			// At the time of writing, this was the case with EGit
 			try {
 				final Method method = element.getClass().getMethod("getFileRevision"); //$NON-NLS-1$
 				final Object value = method.invoke(element);
 				if (value instanceof IFileRevision) {
 					revision = (IFileRevision)value;
 				}
 				// CHECKSTYLE:OFF this would require five "catch" for ignored exceptions...
 			} catch (Exception e) {
 				// CHECKSTYLE:ON
 			}
 		}
 
 		return revision;
 	}
 }
