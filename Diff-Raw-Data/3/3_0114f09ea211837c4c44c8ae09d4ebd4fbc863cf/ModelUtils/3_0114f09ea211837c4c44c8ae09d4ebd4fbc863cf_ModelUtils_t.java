 package org.eclipse.emf.compare.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * Utility class for model loading/saving and serialization
  * 
  * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public final class ModelUtils {
 	private ModelUtils() {
 		// prevents instantiation
 	}
 
 	/**
 	 * Loads a model from an {@link org.eclipse.core.resources.IFile IFile}.
 	 * 
 	 * @param file
 	 *            {@link org.eclipse.core.resources.IFile IFile} containing the
 	 *            model to be loaded.
 	 * @return The model loaded from the file.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(final IFile file) throws IOException {
 		return load(URI.createFileURI(file.getLocation().toOSString()));
 	}
 
 	/**
 	 * Loads a model from a {@link java.io.File File}.
 	 * 
 	 * @param file
 	 *            {@link java.io.File File} containing the model to be loaded.
 	 * @return The model loaded from the file.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(final File file) throws IOException {
 		return load(URI.createFileURI(file.getPath()));
 	}
 
 	/**
 	 * Loads a model from an {@link org.eclipse.emf.common.util.URI URI}.
 	 * 
 	 * @param modelURI
 	 *            {@link org.eclipse.emf.common.util.URI URI} where the model is
 	 *            stored.
 	 * @return The model loaded from the URI.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(URI modelURI) throws IOException {
 		EObject result = null;
 
 		String fileExtension = modelURI.fileExtension();
 		if (fileExtension == null || fileExtension.length() == 0) {
 			fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 		}
 
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
 		final Object resourceFactory = reg.getExtensionToFactoryMap().get(
 				fileExtension);
 		if (resourceFactory != null) {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 					.put(fileExtension, resourceFactory);
 		} else {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 					.put(fileExtension, new XMIResourceFactoryImpl());
 		}
 
 		final Resource modelResource = resourceSet.createResource(modelURI);
 		modelResource.load(Collections.EMPTY_MAP);
 		if (modelResource.getContents().size() > 0)
 			result = (EObject) modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Load a model from an {@link java.io.InputStream  InputStream}.
 	 * 
 	 * @param stream
 	 *            the inputstream to load from
 	 * @param fileName
 	 *            the original filename
 	 * @return the loaded model
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(InputStream stream, String fileName)
 			throws IOException {
 		EObject result = null;
 
 		String fileExtension = fileName
 				.substring(fileName.lastIndexOf(".") + 1); //$NON-NLS-1$
 		if (fileExtension == null || fileExtension.length() == 0) {
 			fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 		}
 
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
 		final Object resourceFactory = reg.getExtensionToFactoryMap().get(
 				fileExtension);
 		if (resourceFactory != null) {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 					.put(fileExtension, resourceFactory);
 		} else {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 					.put(fileExtension, new XMIResourceFactoryImpl());
 		}
 
 		final Resource modelResource = resourceSet.createResource(URI
 				.createURI(fileName));
 		modelResource.load(stream, Collections.EMPTY_MAP);
 		if (modelResource.getContents().size() > 0)
 			result = (EObject) modelResource.getContents().get(0);
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
 	 *             Thrown if an I/O operation has failed or been interrupted
 	 *             during the saving process.
 	 */
 	public static void save(final EObject root, final String path)
 			throws IOException {
 		final URI modelURI = URI.createURI(path);
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
 						new XMIResourceFactoryImpl());
 		final Resource newModelResource = resourceSet.createResource(modelURI);
 		newModelResource.getContents().add(root);
 		final Map<String, String> options = new HashMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System
 				.getProperty("file.encoding"));
 		newModelResource.save(options);
 	}
 
 	/**
 	 * Serializes the given EObjet as a String.
 	 * 
 	 * @param root
 	 *            Root of the objects to be serialized.
 	 * @return The given EObjet serialized as a String.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted
 	 *             during the saving process.
 	 */
 	public static String serialize(final EObject root) throws IOException {
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
 	public static List<EObject> getModelsFrom(File directory)
 			throws IOException {
 		final List<EObject> models = new ArrayList<EObject>();
 
 		if (directory.exists() && directory.isDirectory()) {
 			final File[] files = directory.listFiles();
			Arrays.sort(files);
 			for (int i = 0; i < files.length; i++) {
 				final File aFile = files[i];
 
 				if (!aFile.isDirectory() && !aFile.getName().startsWith(".")) {
 					models.add(load(aFile));
 				}
 			}
 		}
 
 		return models;
 	}
 }
