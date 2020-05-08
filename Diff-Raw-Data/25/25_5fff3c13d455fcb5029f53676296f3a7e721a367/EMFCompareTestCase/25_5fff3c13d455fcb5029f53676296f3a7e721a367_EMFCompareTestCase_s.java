 package org.eclipse.emf.compare.match.statistic.test.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.match.statistic.test.EMFCompareTestPlugin;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * TestCase with usefull utility methods
  * 
  * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public class EMFCompareTestCase extends TestCase {
 	static final String PLUGIN_ID = ""; // TODO check that Id
 
 	static final String CLASS_FILE = "";
 
 	/**
 	 * Assert file contents are strictly equals
 	 * 
 	 * @param file1
 	 * @param file2
 	 */
 	public void assertFileContentsEqual(final File file1, final File file2) {
 		assertTrue("File contents are not equals for " + file1.getName()
 				+ " and " + file2.getName(), EMFCompareTestCase.readFile(file1,
 				false).equals(EMFCompareTestCase.readFile(file2, false)));
 	}
 
 	/**
 	 * Save a model in a file
 	 * 
 	 * @param root
 	 * @param outputFile
 	 * @throws IOException
 	 */
 	public void save(final EObject root, final File outputFile)
 			throws IOException {
 		final FileOutputStream out = new FileOutputStream(outputFile);
 
 		final XMIResourceImpl resource = new XMIResourceImpl();
 		resource.getContents().add(root);
 		resource.save(out, Collections.EMPTY_MAP);
 
 	}
 
 	/**
 	 * @param path
 	 * @return a file bundled inside a plugin knowing it's path
 	 */
 	public File pluginFile(final String path) {
 		return new File(getPluginDirectory() + path);
 	}
 
 	/**
 	 * Compare two dirs
 	 * 
 	 * @param dir1
 	 * @param dir2
 	 * @param fileNameSuffixExpected
 	 */
 	public void compareDirs(final File dir1, final File dir2,
 			final String fileNameSuffixExpected) {
 		if (!dir1.isDirectory() || !dir2.isDirectory()) {
 			throw new RuntimeException("dir1 and dir2 are not folders! "); //$NON-NLS-1$
 		}
 
 		// saving files from dir2 in a hashmap
 		final HashMap files2 = new HashMap();
 		for (int i = 0; i < dir2.listFiles().length; i++) {
 			files2.put(dir2.listFiles()[i].getName(), dir2.listFiles()[i]);
 		}
 
 		for (int i = 0; i < dir1.listFiles().length; i++) {
 			final File file1 = dir1.listFiles()[i];
 			if (files2.containsKey(file1.getName() + fileNameSuffixExpected)) {
 				assertFileContentsEqual(file1, (File) files2.get(file1
 						.getName()
 						+ fileNameSuffixExpected));
 			} else {
 				throw new RuntimeException(file1.getName()
 						+ fileNameSuffixExpected + " is missing for comparison"); //$NON-NLS-1$
 			}
 
 		}
 
 	}
 
 	/**
 	 * load a model from an IFile
 	 * 
 	 * @param file
 	 * @return loaded model
 	 */
 	public EObject load(final IFile file) {
 		EObject result = null;
 		final URI modelURI = URI.createURI(file.getFullPath().toString());
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
 						new XMIResourceFactoryImpl());
 		final Resource modelResource = resourceSet.getResource(modelURI, true);
 		// EcoreUtil.resolveAll(resourceSet);
 		result = (EObject) ((modelResource.getContents().size() > 0) ? modelResource
 				.getContents().get(0)
 				: null);
 		return result;
 	}
 
 	/**
 	 * Load a model from a java.io.File
 	 * 
 	 * @param file
 	 * @return the model
 	 */
 	public static EObject load(final File file) {
 		try {
 			final FileInputStream in = new FileInputStream(file);
 			try {
 				return load(in);
 			} finally {
 				if (in != null) {
 					in.close();
 				}
 			}
 		} catch (final IOException exception) {
 			throw new RuntimeException(exception);
 		}
 
 	}
 
 	/**
 	 * Load a model from an InputStream
 	 * 
 	 * @param in
 	 * @return the model
 	 */
 	public static EObject load(final InputStream in) {
 		final XMIResourceImpl resource = new XMIResourceImpl();
 
 		try {
 			resource.load(in, Collections.EMPTY_MAP);
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 
 		final EObject result = (EObject) ((resource.getContents().size() > 0) ? resource
 				.getContents().get(0)
 				: null);
 		return result;
 	}
 
 	/**
 	 * Save a model
 	 * 
 	 * @param root
 	 * @param path
 	 * @throws IOException
 	 */
 	public void save(final EObject root, final String path) throws IOException {
 		final URI modelURI = URI.createURI(path);
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
 						new XMIResourceFactoryImpl());
 		final Resource newModelResource = resourceSet.createResource(modelURI);
 		newModelResource.getContents().add(root);
 		final Map options = new HashMap();
 		options.put(XMLResource.OPTION_ENCODING, System
 				.getProperty("file.encoding"));
 		newModelResource.save(options);
 
 	}
 
 	/**
 	 * delete a file
 	 * 
 	 * @param file
 	 */
 	public static void delete(final File file) {
 		if (file.isDirectory()) {
 			final File[] children = file.listFiles();
 			for (int i = 0, maxi = children.length; i < maxi; i++) {
 				delete(children[i]);
 			}
 		}
 
 		if (file.exists()) {
 			file.delete();
 		}
 	}
 
 	/**
 	 * 
 	 * @param file
 	 * @param useSystemLineSeparator
 	 * @return the file content
 	 */
 	public static String readFile(final File file,
 			final boolean useSystemLineSeparator) {
 		final StringBuffer stringBuffer = new StringBuffer();
 		try {
 			final BufferedReader in = new BufferedReader(new FileReader(file));
 			try {
 				int size = 0;
 				final char[] buff = new char[512];
 				while ((size = in.read(buff)) >= 0) {
 					stringBuffer.append(buff, 0, size);
 				}
 			} finally {
 				if (in != null) {
 					in.close();
 				}
 			}
 		} catch (final IOException exception) {
 			throw new RuntimeException(exception);
 		}
 
 		final int length = stringBuffer.length();
 		if (length > 0) {
 			final String nl = useSystemLineSeparator ? System.getProperties()
 					.getProperty("line.separator") : "\n";
 			return stringBuffer.toString().replaceAll("\\r\\n", "\n")
 					.replaceAll("[\\n|\\r]", nl);
 		}
 		return stringBuffer.toString();
 	}
 
 	/**
 	 * 
 	 * @param pluginID
 	 * @return the plugin main directory
 	 */
 	public static String getPluginDirectory(final String pluginID) {
 		try {
 			if (Platform.isRunning()) {
 				final File file = new File(FileLocator.toFileURL(
 						Platform.getBundle(pluginID).getEntry("/")).getFile()); //$NON-NLS-1$
 				if (file.isDirectory()) {
 					return file.getAbsolutePath();
 				}
 			}
 		} catch (final Throwable t) {
 		}
 
 		final File parentDirectory = new File(getPluginDirectory());
 		final File[] plugins = parentDirectory.listFiles();
 		for (int i = 0, maxi = plugins.length; i < maxi; i++) {
 			if (plugins[i].isDirectory()) {
 				final String name = plugins[i].getName();
 				if (name.equals(pluginID) || name.startsWith(pluginID + "_")) { //$NON-NLS-1$
 					return plugins[i].getAbsolutePath();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * 
 	 * @return the plugin to look for ressources in
 	 */
 	public static Plugin getPlugin() {
 		return EMFCompareTestPlugin.getDefault();
 	}
 
 	/**
 	 * 
 	 * @return the plugin directory
 	 */
 	public static String getPluginDirectory() {
 		try {
 			return new File(FileLocator.toFileURL(
 					getPlugin().getBundle().getEntry("/")).getFile())
 					.toString();
 		} catch (final Throwable t) {
 		}
 
 		final URL url = ClassLoader
 				.getSystemResource(EMFCompareTestCase.CLASS_FILE);
 		if (url != null) {
 			String path = url.getPath();
 			path = path
 					.substring(0, path.indexOf(EMFCompareTestCase.PLUGIN_ID));
 			if (path.startsWith("file:")) {
 				path = path.substring("file:".length());
 			}
 			final File parentDir = new File(path);
 			if (parentDir.isDirectory()) {
 				final File[] files = parentDir.listFiles();
 				for (int i = 0, maxi = files.length; i < maxi; i++) {
 					if (files[i].isDirectory()
 							&& files[i].getName().startsWith(
 									EMFCompareTestCase.PLUGIN_ID)) {
 						return files[i].getAbsolutePath();
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 }
