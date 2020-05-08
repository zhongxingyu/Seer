 /*******************************************************************************
  * Copyright (c) 2005, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.core.runtime.content.IContentTypeManager;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.internal.core.ISourceCodeCache;
import org.eclipse.dltk.internal.core.ModelManager;
 
 public class DLTKContentTypeManager {
 
 	private static final boolean DEBUG = false;
 	private static final boolean DEBUG_CONTENT = false;
 
 	private static void log(String message, Object input) {
 		if (input != null) {
 			String className = input.getClass().getName();
 			int pos = className.lastIndexOf('.');
 			if (pos > 0) {
 				className = className.substring(pos + 1);
 			}
 			System.out.println(message + ' ' + input + ':' + className);
 		}
 	}
 
 	public static boolean isValidFileNameForContentType(
 			IDLTKLanguageToolkit toolkit, String name) {
 		if (DEBUG) {
 			log(toolkit.getLanguageName(), name);
 		}
 		final IContentType masterType = getMasterContentType(toolkit
 				.getLanguageContentType());
 		if (masterType == null) {
 			return false;
 		}
 		// Acquire derived content types
 		final IContentType[] derived = getDerivedContentTypes(masterType);
 		return isValidFileNameForContentType(derived, name);
 	}
 
 	public static boolean isValidFileNameForContentType(
 			IDLTKLanguageToolkit toolkit, IPath path) {
 		if (DEBUG) {
 			log(toolkit.getLanguageName(), path);
 		}
 		final IContentType masterType = getMasterContentType(toolkit
 				.getLanguageContentType());
 		if (masterType == null) {
 			return false;
 		}
 		final IContentType[] derived = getDerivedContentTypes(masterType);
 		if (isValidFileNameForContentType(derived, path.lastSegment())) {
 			return true;
 		}
 		if (EnvironmentPathUtils.isFull(path)) {
 			final IFileHandle file = EnvironmentPathUtils.getFile(path);
 			if (file != null && file.isFile()) {
 				if (file.getEnvironment().isLocal()) {
 					final File localFile = new File(file.toOSString());
 					return toolkit.canValidateContent(file)
 							&& validateLocalFileContent(masterType, derived,
 									localFile);
 				} else {
 					return toolkit.canValidateContent(file)
 							&& validateRemoteFileContent(masterType, derived,
 									file);
 				}
 			}
 			return false;
 		}
 		if (path.isAbsolute()) {
 			final File file = path.toFile();
 			if (file.isFile()) {
 				return toolkit.canValidateContent(file)
 						&& validateLocalFileContent(masterType, derived, file);
 			}
 		}
 		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		final IResource member = root.findMember(path);
 		return member != null && member.getType() == IResource.FILE
 				&& validateResourceContent(masterType, derived, (IFile) member);
 	}
 
 	private static boolean validateRemoteFileContent(IContentType masterType,
 			final IContentType[] derived, IFileHandle file) {
 		if (DEBUG_CONTENT) {
 			log("validateContent", file); //$NON-NLS-1$
 		}
 		for (int i = 0; i < derived.length; i++) {
 			IContentType type = derived[i];
 			InputStream stream = null;
 			try {
 				stream = new BufferedInputStream(file.openInputStream(null),
 						2048);
 				IContentDescription description = type.getDescriptionFor(
 						stream, IContentDescription.ALL);
 				if (description != null) {
 					if (checkDescription(type, description)) {
 						return true;
 					}
 				}
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} finally {
 				closeStream(stream);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param masterType
 	 * @param derived
 	 * @param file
 	 * @return
 	 */
 	private static boolean validateLocalFileContent(IContentType masterType,
 			IContentType[] derived, File file) {
 		if (DEBUG_CONTENT) {
 			log("validateContent", file); //$NON-NLS-1$
 		}
 		for (int i = 0; i < derived.length; i++) {
 			IContentType type = derived[i];
 			InputStream stream = null;
 			try {
 				stream = new BufferedInputStream(new FileInputStream(file),
 						2048);
 				IContentDescription description = type.getDescriptionFor(
 						stream, IContentDescription.ALL);
 				if (description != null) {
 					if (checkDescription(type, description)) {
 						return true;
 					}
 				}
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} finally {
 				closeStream(stream);
 			}
 		}
 		return false;
 	}
 
 	private static void closeStream(InputStream stream) {
 		if (stream != null) {
 			try {
 				stream.close();
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Look for derived for associated extensions
 	 */
 	private static boolean isValidFileNameForContentType(
 			IContentType[] derived, String name) {
 		for (int i = 0; i < derived.length; i++) {
 			IContentType type = derived[i];
 			if (type.isAssociatedWith(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean isValidResourceForContentType(
 			IDLTKLanguageToolkit toolkit, IResource resource) {
 		if (DEBUG) {
 			log(toolkit.getLanguageName(), resource);
 		}
 		if (!(resource instanceof IFile)) {
 			return false;
 		}
 		// Custom filtering via language toolkit
 		IStatus status = toolkit.validateSourceModule(resource);
 		if (status.getSeverity() != IStatus.OK) {
 			return false;
 		}
 		// Acquire master content type
 		final IContentType masterType = getMasterContentType(toolkit
 				.getLanguageContentType());
 		if (masterType == null) {
 			return false;
 		}
 		// Acquire derived content types
 		final IContentType[] derived = getDerivedContentTypes(masterType);
 		if (isValidFileNameForContentType(derived, resource.getName())) {
 			return true;
 		}
 		// Delegate the decision if we should validate content to the language
 		// toolkit.
 		if (!toolkit.canValidateContent(resource)) {
 			return false;
 		}
 		return validateResourceContent(masterType, derived, (IFile) resource);
 	}
 
 	private static boolean validateResourceContent(
 			final IContentType masterType, final IContentType[] derived,
 			final IFile file) {
 		if (DEBUG_CONTENT) {
 			log("validateContent", file); //$NON-NLS-1$
 		}
 		try {
 			if (file.exists()) {
 				final IContentDescription descr = file.getContentDescription();
 				if (descr != null) {
 					if (descr.getContentType().isKindOf(masterType)) {
 						return true;
 					}
 				}
 			}
 		} catch (CoreException e1) {
 			if (DLTKCore.DEBUG) {
 				e1.printStackTrace();
 			}
 		}
 		try {
 			for (int i = 0; i < derived.length; i++) {
 				final IContentType type = derived[i];
 				/*
 				 * TODO use something like LazyInputStream if there are multiple
 				 * content types
 				 */
				InputStream contents = null;
				ISourceCodeCache cache = ModelManager.getModelManager()
						.getSourceCodeCache();
				final char[] cs = cache.get(file);
				if (cs != null) {
					contents = new InputStream() {
						int pos = 0;

						public int read() throws IOException {
							if (pos >= cs.length) {
								return -1;
							}
							return cs[pos++];
						}
					};
				} else {
					contents = new BufferedInputStream(file.getContents(), 2048);
				}
 				try {
 					final IContentDescription description = type
 							.getDescriptionFor(contents,
 									IContentDescription.ALL);
 					if (description != null) {
 						if (checkDescription(type, description)) {
 							return true;
 						}
 					}
 				} catch (IOException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				} finally {
 					closeStream(contents);
 				}
 			}
 		} catch (CoreException e) {
 			/*
 			 * CoreException is thrown when resource does not exist, is out of
 			 * sync or something similar - there is no need to process other
 			 * content types if it happens.
 			 */
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	private static IContentType getMasterContentType(String languageContentType) {
 		final IContentTypeManager manager = Platform.getContentTypeManager();
 		return manager.getContentType(languageContentType);
 	}
 
 	private static boolean checkDescription(IContentType type,
 			IContentDescription description) {
 		Object object = description
 				.getProperty(ScriptContentDescriber.DLTK_VALID);
 		if (object != null && ScriptContentDescriber.TRUE.equals(object)) {
 			final IContentType contentType = description.getContentType();
 			return contentType.isKindOf(type);
 		}
 		return false;
 	}
 
 	private static final Map derivedContentTypesCache = new HashMap();
 
 	private static IContentType[] getDerivedContentTypes(IContentType masterType) {
 		IContentType[] result;
 		synchronized (derivedContentTypesCache) {
 			result = (IContentType[]) derivedContentTypesCache.get(masterType);
 		}
 		if (result != null) {
 			return result;
 		}
 		final IContentTypeManager manager = Platform.getContentTypeManager();
 		final IContentType[] types = manager.getAllContentTypes();
 		final Set derived = new HashSet();
 		for (int i = 0; i < types.length; i++) {
 			if (types[i].isKindOf(masterType)) {
 				derived.add(types[i]);
 			}
 		}
 		result = (IContentType[]) derived.toArray(new IContentType[derived
 				.size()]);
 		synchronized (derivedContentTypesCache) {
 			derivedContentTypesCache.put(masterType, result);
 		}
 		return result;
 	}
 }
