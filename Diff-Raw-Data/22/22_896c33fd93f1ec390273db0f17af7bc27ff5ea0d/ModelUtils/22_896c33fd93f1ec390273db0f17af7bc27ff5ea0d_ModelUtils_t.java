 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * Utility class for model loading/saving and serialization.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public final class ModelUtils {
 	/**
 	 * Utility classes don't need to (and shouldn't be) be instantiated.
 	 */
 	private ModelUtils() {
 		// prevents instantiation
 	}
 
 	/**
 	 * Loads a model from an {@link IPath} in a given {@link ResourceSet}.
 	 * 
 	 * @param path
 	 *            {@link IPath} where the model lies.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the path.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(IPath path, ResourceSet resourceSet) throws IOException {
 		return load(ResourcesPlugin.getWorkspace().getRoot().getFile(path), resourceSet);
 	}
 
 	/**
 	 * Loads a model from an {@link org.eclipse.core.resources.IFile IFile} in a given {@link ResourceSet}.
 	 * 
 	 * @param file
 	 *            {@link org.eclipse.core.resources.IFile IFile} containing the model to be loaded.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the file.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(IFile file, ResourceSet resourceSet) throws IOException {
 		return load(URI.createFileURI(file.getLocation().toOSString()), resourceSet);
 	}
 
 	/**
 	 * Loads a model from a {@link java.io.File File} in a given {@link ResourceSet}.
 	 * 
 	 * @param file
 	 *            {@link java.io.File File} containing the model to be loaded.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the file.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(File file, ResourceSet resourceSet) throws IOException {
 		return load(URI.createFileURI(file.getPath()), resourceSet);
 	}
 
 	/**
 	 * Loads a model from an {@link org.eclipse.emf.common.util.URI URI} in a given {@link ResourceSet}.
 	 * 
 	 * @param modelURI
 	 *            {@link org.eclipse.emf.common.util.URI URI} where the model is stored.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the URI.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	@SuppressWarnings("unchecked")
 	public static EObject load(URI modelURI, ResourceSet resourceSet) throws IOException {
 		EObject result = null;
 
 		String fileExtension = modelURI.fileExtension();
 		if (fileExtension == null || fileExtension.length() == 0) {
 			fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 		}
 
 		final Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
 		final Object resourceFactory = reg.getExtensionToFactoryMap().get(fileExtension);
 		if (resourceFactory != null) {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 					resourceFactory);
 		} else {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 					new XMIResourceFactoryImpl());
 		}
 
 		final Resource modelResource = resourceSet.createResource(modelURI);
		final Map<String, String> options = new ConcurrentHashMap<String, String>();
		options.put(XMLResource.OPTION_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
		modelResource.load(options);
 		if (modelResource.getContents().size() > 0)
 			result = (EObject)modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Load a model from an {@link java.io.InputStream  InputStream} in a given {@link ResourceSet}.
 	 * 
 	 * @param stream
 	 *            The inputstream to load from
 	 * @param fileName
 	 *            The original filename
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The loaded model
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	@SuppressWarnings("unchecked")
 	public static EObject load(InputStream stream, String fileName, ResourceSet resourceSet)
 			throws IOException {
 		EObject result = null;
 
 		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1); //$NON-NLS-1$
 		if (fileExtension == null || fileExtension.length() == 0) {
 			fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 		}
 
 		final Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
 		final Object resourceFactory = reg.getExtensionToFactoryMap().get(fileExtension);
 		if (resourceFactory != null) {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 					resourceFactory);
 		} else {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 					new XMIResourceFactoryImpl());
 		}
 
 		final Resource modelResource = resourceSet.createResource(URI.createURI(fileName));
 		modelResource.load(stream, Collections.EMPTY_MAP);
 		if (modelResource.getContents().size() > 0)
 			result = (EObject)modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Saves a model as a file to the given path.
 	 * 
 	 * @param root
 	 *            Root of the objects to be serialized in a file.
 	 * @param path
 	 *            File where the objects have to be saved.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted during the saving process.
 	 */
 	@SuppressWarnings("unchecked")
 	public static void save(EObject root, String path) throws IOException {
 		final URI modelURI = URI.createURI(path);
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
 				Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
 		final Resource newModelResource = resourceSet.createResource(modelURI);
 		newModelResource.getContents().add(root);
		final Map<String, String> options = new ConcurrentHashMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
 		newModelResource.save(options);
 	}
 
 	/**
 	 * Serializes the given EObjet as a String.
 	 * 
 	 * @param root
 	 *            Root of the objects to be serialized.
 	 * @return The given EObjet serialized as a String.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted during the saving process.
 	 */
 	@SuppressWarnings("unchecked")
 	public static String serialize(EObject root) throws IOException {
 		final XMIResourceImpl newResource = new XMIResourceImpl();
 		final StringWriter writer = new StringWriter();
 		newResource.getContents().add(root);
 		newResource.save(writer, Collections.EMPTY_MAP);
 		return writer.toString();
 	}
 
 	/**
 	 * Loads the models contained by the given directory.
 	 * 
 	 * @param directory
 	 *            The directory from which to load the models.
 	 * @return The models contained by the given directory.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted.
 	 */
 	public static List<EObject> getModelsFrom(File directory) throws IOException {
 		final List<EObject> models = new ArrayList<EObject>();
 
 		if (directory.exists() && directory.isDirectory()) {
 			final File[] files = directory.listFiles();
 			Arrays.sort(files);
 			for (int i = 0; i < files.length; i++) {
 				final File aFile = files[i];
 
 				final ResourceSet resourceSet = new ResourceSetImpl();
 				if (!aFile.isDirectory() && !aFile.getName().startsWith(".")) { //$NON-NLS-1$
 					models.add(load(aFile, resourceSet));
 				}
 			}
 		}
 
 		return models;
 	}
 }
