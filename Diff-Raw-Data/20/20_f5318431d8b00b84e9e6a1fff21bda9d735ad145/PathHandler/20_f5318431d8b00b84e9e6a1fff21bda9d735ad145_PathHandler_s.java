 /*******************************************************************************
  * Copyright (c) 2012 Jens von Pilgrim
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package de.jevopi.mitra2.context;
 
 import static java.util.Collections.emptyList;
 import static java.util.Collections.emptyMap;
 import static java.util.Collections.singletonList;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ContentHandler;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.URIHandler;
 import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
 
 import de.jevopi.mitra2.MitraUtils;
 import de.jevopi.mitra2.exec.IMitraConfig;
 import de.jevopi.mitra2.exec.MitraConfig;
 import de.jevopi.mitra2.standardlibrary.MitraStandardLibrary;
 
 /**
  * Part of the project context, responsible for loading all kind of resources
  * such as modules, models, and metamodels. For all three kind, different
  * {@link URIConverter} can be configured. Additionally, base paths can be
  * defined for each each path, and an extra path for saving models, in order to
  * resolve relative paths.
  * 
  * @author Jens von Pilgrim (developer@jevopi.de)
  * @since Feb 8, 2012
  */
 public class PathHandler {
 
 	public static enum ResourceType {
 		MODULE, METAMODEL, MODEL
 	}
 
 	URIConverter uriConverter;
 
 	EnumMap<ResourceType, Set<URI>> basePaths;
 
 	URI modelSavePath;
 
 	public PathHandler() {
 		Collection<ContentHandler> emptyHandlerList = emptyList();
 		uriConverter = new ExtensibleURIConverterImpl(
 				URIHandler.DEFAULT_HANDLERS, emptyHandlerList);
 
 		basePaths = new EnumMap<PathHandler.ResourceType, Set<URI>>(
 				ResourceType.class);
 		for (ResourceType type : ResourceType.values()) {
 			basePaths.put(type, new HashSet<URI>());
 		}
 
 		modelSavePath = URI.createFileURI(".");
 
 		addPath(ResourceType.MODULE, MitraStandardLibrary.createMSLPath());
 
 	}
 	
 	public void clear() {
 		for (Set<URI> path: basePaths.values()) {
 			path.clear();
 		}
 	}
 
 	public Set<URI> getBasePaths(ResourceType type) {
 		return basePaths.get(type);
 	}
 
 	protected URIConverter getConverter() {
 		return uriConverter;
 	}
 
 	public void setPaths(IMitraConfig configuration) {
 		for (String path : configuration.getModulePaths()) {
 			addPath(ResourceType.MODULE, createBaseURI(path));
 		}
 		for (String path : configuration.getMetamodelPaths()) {
 			addPath(ResourceType.METAMODEL, createBaseURI(path));
 		}
 		for (String path : configuration.getModelPaths()) {
 			addPath(ResourceType.MODEL, createBaseURI(path));
 		}
 		setModelSavePath(createBaseURI(configuration.getSavedModelsPath()));
 	}
 
 	public static URI createBaseURI(String uriAsString) {
 		URI uri = URI.createURI(uriAsString);
		if (uri.isRelative()) {
 			uri = URI.createFileURI(uriAsString);
 		}
 		return uri;
 	}
 
 	public void addPath(ResourceType type, URI basePath) {
 		if (!basePath.isPrefix()) {
 			basePath = URI.createURI(basePath.toString() + "/");
 		}
 
 		getBasePaths(type).add(basePath);
 	}
 
 	public void setModelSavePath(URI savePath) {
 		modelSavePath = savePath;
 	}
 
 	public URI resolve(ResourceType type, String path) {
 		URI uri = URI.createURI(path);
 		return resolve(type, uri);
 	}
 
 	private URI resolve(ResourceType type, URI uri) {
 		// if (!uri.isRelative()) {
 		// return uri;
 		// }
 		for (URI base : getBasePaths(type)) {
 			URI resolved = uri.resolve(base);
 			if (uriConverter.exists(resolved, emptyMap())) {
 				return resolved;
 			}
 		}
 		return uri;
 	}
 
 	public URI resolveSaveModel(String path) {
 		URI uri = URI.createURI(path);
 		if (!uri.isRelative()) {
 			return uri;
 		}
 		return uri.resolve(modelSavePath);
 	}
 
 	public Set<URI> listAllModuleURIs() {
 		Set<URI> result = new HashSet<URI>();
 		for (URI path : getBasePaths(ResourceType.MODULE)) {
 			if (path.isFile()) {
 				File folder = new File(path.toFileString());
 				for (File f : getFilesRecursively(folder, ".mitra")) {
 					URI uri = URI.createFileURI(f.getAbsolutePath());
 					result.add(uri);
 				}
 			} else {
 				System.err.println
 				//throw new UnsupportedOperationException
 				(path.scheme()
 						+ " scheme of " + path + " not supported yet, skip this path");
 			}
 
 		}
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder strb = new StringBuilder();
 		for (ResourceType type : ResourceType.values()) {
 			strb.append("Paths for " + type);
 			for (URI uri : getBasePaths(type)) {
 				strb.append("\n  - ").append(uri);
 			}
 			strb.append("\n");
 		}
 		strb.append("Model are saved in " + modelSavePath);
 		return strb.toString();
 	}
 
 	public static List<File> getFilesRecursively(File base,
 			final String dotAndExtension) {
 		FileFilter filter = new FileFilter() {
 			@Override
 			public boolean accept(File i_pathname) {
 				return i_pathname.isFile()
 						&& i_pathname.getName().endsWith(dotAndExtension);
 			}
 		};
 		FileFilter dirs = new FileFilter() {
 			@Override
 			public boolean accept(File i_pathname) {
 				return i_pathname.isDirectory() && !i_pathname.isHidden();
 			}
 		};
 
 		if (filter.accept(base))
 			return singletonList(base);
 		List<File> result = new ArrayList<File>();
 		List<File> folders = new ArrayList<File>();
 		if (base.isDirectory())
 			folders.add(base);
 		while (!folders.isEmpty()) {
 			File folder = folders.get(0);
 			folders.remove(0);
 			for (File content : folder.listFiles(filter)) {
 				result.add(content);
 			}
 			for (File subfolder : folder.listFiles(dirs)) {
 				folders.add(subfolder);
 			}
 		}
 		return result;
 
 	}
 
 	public boolean existsModel(URI uri) {
 		URI resolved = resolve(ResourceType.MODEL, uri);
 		return uriConverter.exists(resolved, emptyMap());
 	}
 
 	public static String computeBaseURI(String moduleFilename, String fqnModule) {
 
 		String s = moduleFilename.substring(0, moduleFilename.length()
 				- MitraUtils.convertFQNToPathName(fqnModule).length());
 		URI uri = URI.createURI(s);
 		if (uri.isRelative()) {
 			uri = URI.createFileURI(s);
 		}
 		return uri.toString();
 	}
 
 	
 
 }
