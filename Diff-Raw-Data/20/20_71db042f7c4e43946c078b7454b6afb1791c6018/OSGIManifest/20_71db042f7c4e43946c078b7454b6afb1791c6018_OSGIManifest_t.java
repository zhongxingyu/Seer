 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.ui.helper;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.osgi.util.ManifestElement;
 import org.osgi.framework.BundleException;
 
 /**
  * Utility class used to read / modify an OSGI manifest
  */
 
 public class OSGIManifest {
 
 	/**
 	 * Manifest header identifying the packages that the bundle offers to the
 	 * Framework for export.
 	 * 
 	 * <p>
 	 * The attribute value may be retrieved from the <code>Dictionary</code>
 	 * object returned by the <code>Bundle.getHeaders</code> method.
 	 */
 	public static final String	EXPORT_PACKAGE							= "Export-Package";
 
 	/**
 	 * Manifest header identifying the packages on which the bundle depends.
 	 * 
 	 * <p>
 	 * The attribute value may be retrieved from the <code>Dictionary</code>
 	 * object returned by the <code>Bundle.getHeaders</code> method.
 	 */
 	public static final String	IMPORT_PACKAGE							= "Import-Package";	
 
 	/**
 	 * internal Map representing the Manifest headers. 
 	 * 
 	 * <p>
 	 * initialized by 
 	 * loadManifest(InputStream manifestStream), 
 	 * modified by 
 	 * setExports(ManifestElement[] exports) and 
 	 * setImports(ManifestElement[] imports) and can be 
 	 * read by getManifestInputStream()
 	 * 
 	 */
 
 	private Map<String,String> manifest;
 	
 	public OSGIManifest (InputStream manifestStream) throws BundleException {
 		loadManifest(manifestStream);
 	}
 			
 	/**
 	 * Initialize the manifest using an InputStream.
 	 * @param manifestStream
 	 * @throws BundleException
 	 */
 	private void loadManifest(InputStream manifestStream) throws BundleException {
 		try {
 			manifest = new LinkedHashMap<String,String>();
 			ManifestElement.parseBundleManifest(manifestStream, manifest);
 		} catch (IOException ioe) {
 			throw new BundleException(ioe.getMessage());
 		} finally {
 			try {
 				if (manifestStream != null)
 					manifestStream.close();
 			} catch (IOException e1) {
 				//Ignore
 			}
 		}
 	}
 	
 	/**
 	 * Return an InputStream representing the manifest.
 	 * @return
 	 */
 	public InputStream getManifestInputStream() {
 		StringWriter writer = new StringWriter();
 		for (String key: manifest.keySet()) {
 			writer.write(key);
 			writer.write(": ");
 			writer.write(manifest.get(key));
 			writer.write('\n');				
 		}
 		byte[] stringBytes = writer.toString().getBytes();
 		ByteArrayInputStream bais = new ByteArrayInputStream(stringBytes);
 		return bais;
 	}
 
 	/*
 	 * @return bundle export packages
 	 * @throws BundleException
 	 */
 	public ManifestElement[] getExports() throws BundleException {
 		ManifestElement[] exports = ManifestElement.parseHeader(EXPORT_PACKAGE, manifest.get(EXPORT_PACKAGE));
 		if (exports == null)
 			exports = new ManifestElement[0];
 		return exports;
 	}
 	
 	/**
 	 * @return bundle import packages
 	 * @throws BundleException
 	 */
 	public ManifestElement[] getImports() throws BundleException {
 		ManifestElement[] imports = ManifestElement.parseHeader(IMPORT_PACKAGE, manifest.get(IMPORT_PACKAGE));
 		if (imports == null)
 			imports = new ManifestElement[0];
 		return imports;
 	}
 
 	/**
 	 * set bundle export packages
 	 * @param exports
 	 */
 	public void setExports(ManifestElement[] exports) {
 		if (exports == null || exports.length == 0) {
 			manifest.remove(EXPORT_PACKAGE);
 		} else {
 			StringBuilder buffer = new StringBuilder();
 			for (int i = 0; i < exports.length; i++) {
 				buffer.append(exports[i].toString());
 				if (i < exports.length-1)
 					buffer.append(",");			
 			}
 			manifest.put(EXPORT_PACKAGE,buffer.toString());
 		}
 	}
 	
 	/**
 	 * set bundle import packages
 	 * @param imports
 	 */
 	public void setImports(ManifestElement[] imports) {
 		if (imports == null || imports.length == 0) {
 			manifest.remove(IMPORT_PACKAGE);
 		} else {
 			StringBuilder buffer = new StringBuilder();
 			for (int i = 0; i < imports.length; i++) {
 				buffer.append(imports[i].toString());
 				if (i < imports.length-1)
 					buffer.append(",");			
 			}
 			manifest.put(IMPORT_PACKAGE,buffer.toString());
 		}
 	}
 
 	/**
 	 * add bundle import package
 	 * avoids double entries
 	 * @param importElement
 	 * @throws BundleException 
 	 */
 	public void addImport(ManifestElement importElement) throws BundleException {
 		if (importElement == null)
 			return;
 		ManifestElement[] elements = getImports();
 		for (ManifestElement element: elements) {
			if (equalElements(element,importElement))
 				return; // avoid double entries
 		}
 		ManifestElement[] newElements = new ManifestElement[elements.length+1];
 		System.arraycopy(elements, 0, newElements, 0, elements.length);
 		newElements[elements.length] = importElement;
 		setImports(newElements);
 	}
 
 	/**
 	 * add bundle export package
 	 * avoids double entries
 	 * @param exportElement
 	 * @throws BundleException 
 	 */
 	public void addExport(ManifestElement exportElement) throws BundleException {
 		if (exportElement == null)
 			return;
 		ManifestElement[] elements = getExports();
 		for (ManifestElement element: elements) {
			if (equalElements(element,exportElement))
 				return; // avoid double entries
 		}
 		ManifestElement[] newElements = new ManifestElement[elements.length+1];
 		System.arraycopy(elements, 0, newElements, 0, elements.length);
 		newElements[elements.length] = exportElement;
 		setExports(newElements);
 	}
 
 	/**
 	 * dumps the manifest
 	 */
 	public String toString() {
 		if (manifest == null)
 			return "";
 		else
 			return manifest.toString();
 	}
 	
 	/**
 	 * converts a root directory / jar-file into an InputStream representing the OSGI manifest
 	 * @param bundleLocation
 	 * @return
 	 */
 	static InputStream getInputStream(File bundleLocation) {
 		InputStream manifestStream = null;
 		ZipFile jarFile = null;
 		try {
 			String fileExtention = bundleLocation.getName();
 			fileExtention = fileExtention.substring(fileExtention.lastIndexOf('.') + 1);
 			// Handle a JAR'd bundle
 			if ("jar".equalsIgnoreCase(fileExtention) && bundleLocation.isFile()) { //$NON-NLS-1$
 				jarFile = new ZipFile(bundleLocation, ZipFile.OPEN_READ);
 				ZipEntry manifestEntry = jarFile.getEntry(JarFile.MANIFEST_NAME);
 				if (manifestEntry != null) {
 					manifestStream = jarFile.getInputStream(manifestEntry);
 				}
 			} else {
 				// we have a directory-based bundle
 				File bundleManifestFile = new File(bundleLocation, JarFile.MANIFEST_NAME);
 				if (bundleManifestFile.exists())
 					manifestStream = new BufferedInputStream(new FileInputStream(new File(bundleLocation, JarFile.MANIFEST_NAME)));
 			}
 		} catch (IOException e) {
 			//ignore
 		}
 		return manifestStream;
 	}
	
	private static boolean equalElements(ManifestElement e1, ManifestElement e2) {
		return removeWhitespaces(e1.toString()).equals(removeWhitespaces(e2.toString()));
	}
	
	private static String removeWhitespaces(String input) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (!Character.isWhitespace(c))
				sb.append(c);
		}
		return sb.toString();
	}
 
 }
