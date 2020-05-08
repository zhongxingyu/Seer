 /*******************************************************************************
  * Copyright (c) 2001, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.commonarchivecore.internal.helpers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.StringTokenizer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategy;
 
 
 /**
  * Helper class used for loading of mof resources contained within an archive; knows how to retrieve
  * an input stream for a given uri
  */
 public class ArchiveURIConverterImpl extends URIConverterImpl {
 	protected static final String fileProtocol = "file"; //$NON-NLS-1$
 	protected static final String platformProtocol = "platform"; //$NON-NLS-1$
 	private String inFilepath, outFilepath;
 
	public ArchiveURIConverterImpl(Archive anArchive, String resourcesPath) {
 		setArchive(anArchive);
 		this.resourcesPath = resourcesPath;
 		setInputFilepath(resourcesPath);
 		String outpath = resourcesPath;
 		if (outpath == null) {
 			LoadStrategy l = anArchive.getLoadStrategy();
 			if (l != null && l.isDirectory()) {
 				try {
 					outpath = l.getAbsolutePath();
 				} catch (FileNotFoundException ignore) {
 					//Ignore
 				}
 			}
 		}
 		setOutputFilepath(outpath);
 	}
 
 	/** The archive from which resources will be loaded */
	protected Archive archive;
 	protected String resourcesPath;
 
	public Archive getArchive() {
 		return archive;
 	}
 
 	public InputStream createInputStream(URI uri) throws IOException {
 		InputStream in = null;
 		if (resourcesPath != null)
 			in = makeInputStream(uri);
 		if (in != null)
 			return in;
 		return getArchive().getInputStream(uri.toString());
 	}
 
	public void setArchive(Archive newArchive) {
 		archive = newArchive;
 	}
 
 	/**
 	 * Gets the resourcesPath.
 	 * 
 	 * @return Returns a String
 	 */
 	public String getResourcesPath() {
 		return resourcesPath;
 	}
 
 	/**
 	 * Sets the resourcesPath.
 	 * 
 	 * @param resourcesPath
 	 *            The resourcesPath to set
 	 */
 	public void setResourcesPath(String resourcesPath) {
 		this.resourcesPath = resourcesPath;
 		setInputFilepath(resourcesPath);
 		setOutputFilepath(resourcesPath);
 	}
 
 	public URI normalize(URI uri) {
 		return getInternalURIMap().getURI(uri);
 	}
 
 	/**
 	 * The input file path consists of a string of directories or zip files separated by semi-colons
 	 * that are searched when an input stream is constructed.
 	 * 
 	 * @return The file path
 	 */
 	public String getInputFilepath() {
 		return inFilepath;
 	}
 
 	/**
 	 * @param filepath
 	 *            The file path
 	 */
 	public void setInputFilepath(String filepath) {
 		inFilepath = filepath;
 	}
 
 	/**
 	 * @return The output file path
 	 */
 	public String getOutputFilepath() {
 		return outFilepath;
 	}
 
 	/**
 	 * The output file path is the path name of a directory to prepend to relative file path names
 	 * when an output stream is constructed.
 	 * 
 	 * @param filepath
 	 *            The output file path
 	 */
 	public void setOutputFilepath(String filepath) {
 		outFilepath = filepath;
 	}
 
 	/**
 	 * Convert the URI to an input stream.
 	 * 
 	 * @param uri
 	 *            The uri
 	 */
 	public InputStream makeInputStream(URI uri) throws IOException {
 		URI converted = uri;
 
 		if ((fileProtocol.equals(converted.scheme()) || converted.scheme() == null) && inFilepath != null) {
 			return searchFilePath(converted.toString());
 		}
 		URL url = createURL(converted.toString());
 		URLConnection urlConnection = url.openConnection();
 		return urlConnection.getInputStream();
 	}
 
 	/**
 	 * Search the file path if the file portion of the URL is not absolute (does not begin with "/";
 	 * otherwise, attempt to create an input stream for the file.
 	 */
 	protected InputStream searchFilePath(String filename) throws IOException {
 		File file = new File(filename);
 		if (file.isAbsolute())
 			return new FileInputStream(file);
 		StringTokenizer st = new StringTokenizer(inFilepath, ";"); //$NON-NLS-1$
 		while (st.hasMoreTokens()) {
 			String f = st.nextToken();
 			ZipFile zf = null;
 			try {
 				zf = new ZipFile(f);
 			} catch (Exception e) {
 				//Ignore
 			}
 			InputStream in = null;
 			if (zf != null) {
 				in = searchZipFile(zf, filename);
 				if (in == null)
 					try {
 						zf.close();
 					} catch (Exception e) {
 						//Ignore
 					}
 			} else {
 				in = searchDirectory(f, filename);
 			}
 			if (in != null)
 				return in;
 		}
 		return null;
 	}
 
 	/**
 	 * This method determines whether the file with the relative path name of filename exists in the
 	 * given directory. If not, it returns null; otherwise, it opens the file up and returns the
 	 * input source.
 	 * 
 	 * @param dir
 	 *            java.lang.String
 	 * @return java.io.InputSource
 	 */
 	protected InputStream searchDirectory(String dir, String filename) throws IOException {
 		if (dir.equals(".")) //$NON-NLS-1$
 			dir = System.getProperty("user.dir"); //$NON-NLS-1$
 		File f = new File(dir + System.getProperty("file.separator") + filename.replace('/', File.separatorChar)); //$NON-NLS-1$
 		if (!f.exists())
 			return null;
 		return new FileInputStream(f);
 	}
 
 	/**
 	 * This method determines whether there is a ZipEntry whose name is filename in the given
 	 * ZipFile. If not, it returns null; otherwise, it returns an input source to read from the
 	 * ZipEntry.
 	 * 
 	 * @param zip
 	 *            java.util.zip.ZipFile
 	 * @return java.io.InputSource
 	 */
 	protected InputStream searchZipFile(ZipFile zip, String filename) throws IOException {
 		ZipEntry entry = zip.getEntry(filename);
 		if (entry == null)
 			return null;
 		return zip.getInputStream(entry);
 	}
 
 	/**
 	 * Make a URL from the uri; if the attempt fails, attempt to append "file:" to the URI; if the
 	 * attempt still fails, attempt to replace the protocol with the file protocol.
 	 * 
 	 * @param uri
 	 *            The string uri
 	 */
 	public URL createURL(String uri) throws MalformedURLException {
 		MalformedURLException m = null;
 		if (uri == null)
 			return null;
 		URL url = null;
 		try {
 			url = new URL(uri);
 		} catch (Exception e) {
 			m = (MalformedURLException) e;
 		}
 		if (url != null)
 			return url;
 		// Either treat the URI as a filepath (if there are no : or
 		// a : in position 1) or replace the given protocol with the
 		// file protocol.
 		int index = uri.indexOf(":"); //$NON-NLS-1$
 		if (index == -1 || index == 1)
 			uri = fileProtocol + ":" + uri; //$NON-NLS-1$
 		else if (index > 0)
 			uri = fileProtocol + ":" + uri.substring(index + 1); //$NON-NLS-1$
 		try {
 			url = new URL(uri);
 		} catch (Exception e) {
 			//Ignore
 		}
 		if (url != null)
 			return url;
 		throw m;
 	}
 
 	/**
 	 * Convert the URI to an output stream.
 	 * 
 	 * @param uri
 	 *            The uri
 	 */
 	public OutputStream createOutputStream(URI uri) throws IOException {
 		URI converted = uri;
 		if (platformProtocol.equals(converted.scheme())) {
 			URL resolvedURL = resolvePlatform(new URL(converted.toString()));
 			if (resolvedURL != null) {
 				converted = URI.createFileURI(resolvedURL.getFile());
 			}
 		}
 		if (fileProtocol.equals(converted.scheme()) || converted.scheme() == null) {
 			return openFileOutputStream(converted);
 		}
 		URL url = createURL(converted.toString());
 		URLConnection urlConnection = url.openConnection();
 		urlConnection.setDoOutput(true);
 		return urlConnection.getOutputStream();
 	}
 
 	protected URL resolvePlatform(URL url) throws IOException {
 		// let WorkbenchURIConverter implement this one.
 		return null;
 	}
 
 	/**
 	 * Open a file output stream for the given uri (the uri has file protocol). If an output file
 	 * path is specified and the file name is relative, prepend the output file path to the file
 	 * name. Make the directories that contain the file if they do not exist.
 	 */
 	protected OutputStream openFileOutputStream(URI uri) throws IOException {
 		File file = new File(uri.toFileString());
 		if (!file.isAbsolute() && outFilepath != null) {
 			file = new File(outFilepath + File.separator + uri.toFileString());
 		}
 		String parent = file.getParent();
 		if (parent != null) {
 			new File(parent).mkdirs();
 		}
 		OutputStream outputStream = new FileOutputStream(file);
 		return outputStream;
 	}
 }
