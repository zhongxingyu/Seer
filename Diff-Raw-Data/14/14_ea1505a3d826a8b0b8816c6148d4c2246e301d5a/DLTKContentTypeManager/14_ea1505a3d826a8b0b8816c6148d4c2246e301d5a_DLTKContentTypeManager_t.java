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
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.core.runtime.content.IContentTypeManager;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 
 public class DLTKContentTypeManager {
 	public static boolean isValidFileNameForContentType(
 			IDLTKLanguageToolkit toolkit, IPath path) {
 		if (isValidFileNameForContentType(toolkit, path.lastSegment())) {
 			return true;
 		}
 		if (path.isAbsolute()) {
 			File file = path.toFile();
 			if (file.exists() && file.isFile()
 					&& (file.getName().indexOf('.') == -1)) {
 				IContentType[] derived = getDerivedContentTypes(toolkit
 						.getLanguageContentType());
 				// Look for derived for associated extensions.
 				for (int i = 0; i < derived.length; i++) {
 					IContentType type = derived[i];
 					InputStream stream = null;
 					try {
 						stream = new BufferedInputStream(new FileInputStream(
 								file), 2048);
 						IContentDescription description = type
 								.getDescriptionFor(stream,
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
 						closeStream(stream);
 					}
 				}
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
 
 	public static boolean isValidFileNameForContentType(
 			IDLTKLanguageToolkit toolkit, String name) {
 		IContentType[] derived = getDerivedContentTypes(toolkit
 				.getLanguageContentType());
 		// Look for derived for associated extensions.
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
		// Custom filtering via language tookit
		if (resource instanceof IFile) {
			IStatus status = toolkit.validateSourceModule(resource);
			if (status.getSeverity() != IStatus.OK) {
				return false;
			}
		}

 		if (isValidFileNameForContentType(toolkit, resource.getFullPath()
 				.lastSegment())) {
 			return true;
 		}
 		if (!resource.isAccessible()) {
 			return false;
 		}
 
 		// I've disable file content checking for non local environments.
 		IEnvironment environment = EnvironmentManager.getEnvironment(resource
 				.getProject());
 		if (environment == null) {
 			return false;
 		}
 		if (!EnvironmentManager.isLocal(environment)) {
 			Preferences preferences = DLTKCore.getPlugin()
 					.getPluginPreferences();
 			String value = preferences
 					.getString(DLTKCore.CORE_NON_LOCAL_EMPTY_FILE_CONTENT_TYPE_CHECKING);
 			if (DLTKCore.DISABLED.equals(value)) {
 				return false;
 			}
 		}
 
 		if (resource instanceof IFile) {
 			IFile file = (IFile) resource;
 			IContentType masterType = getMasterContentType(toolkit
 					.getLanguageContentType());
 			if (masterType == null) {
 				return false;
 			}
 			try {
 				IContentDescription descr = file.getContentDescription();
 				if (descr != null) {
 					if (descr.getContentType().isKindOf(masterType)) {
 						return true;
 					}
 				}
 			} catch (CoreException e1) {
 				if (DLTKCore.DEBUG) {
 					e1.printStackTrace();
 				}
 			}
 
 			IContentType[] derived = getDerivedContentTypes(toolkit
 					.getLanguageContentType());
 			String name = resource.getName();
 			// Look for derived for associated extensions.
 			for (int i = 0; i < derived.length; i++) {
 				IContentType type = derived[i];
 				if (type.isAssociatedWith(name)) {
 					return true;
 				}
 			}
 			// Check resource contents if name without extension
 			IPath path = file.getFullPath();
 			if (path.getFileExtension() == null) {
 				for (int i = 0; i < derived.length; i++) {
 					IContentType type = derived[i];
 					IContentDescription description;
 					InputStream contents = null;
 					try {
 						contents = new BufferedInputStream(file.getContents(),
 								2048);
 						description = type.getDescriptionFor(contents,
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
 					} catch (CoreException e) {
 						if (DLTKCore.DEBUG) {
 							e.printStackTrace();
 						}
 					} finally {
 						closeStream(contents);
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private static IContentType getMasterContentType(String languageContentType) {
 		IContentTypeManager manager = Platform.getContentTypeManager();
 		IContentType masterContentType = manager
 				.getContentType(languageContentType);
 		return masterContentType;
 	}
 
 	private static boolean checkDescription(IContentType type,
 			IContentDescription description) {
 		Object object = description
 				.getProperty(ScriptContentDescriber.DLTK_VALID);
 		if (object != null && ScriptContentDescriber.TRUE.equals(object)) {
 			return description.getContentType().isKindOf(type);
 		}
 		return false;
 	}
 
 	private static IContentType[] getDerivedContentTypes(String name) {
 		IContentTypeManager manager = Platform.getContentTypeManager();
 		IContentType masterContentType = manager.getContentType(name);
 		if (masterContentType == null) {
 			return new IContentType[0];
 		}
 		IContentType[] types = manager.getAllContentTypes();
 		Set derived = new HashSet();
 		for (int i = 0; i < types.length; i++) {
 			if (types[i].isKindOf(masterContentType)) {
 				derived.add(types[i]);
 			}
 		}
 		return (IContentType[]) derived
 				.toArray(new IContentType[derived.size()]);
 	}
 }
