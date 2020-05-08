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
 package org.eclipse.jst.j2ee.commonarchivecore.internal.strategy;
 
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jst.j2ee.commonarchivecore.internal.looseconfig.LooseApplication;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.looseconfig.LooseArchive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.looseconfig.LooseWARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.looseconfig.LooseconfigPackage;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 
 
 /**
  * Implementer that knows how to read files from the local file system and treat them as file
  * entries in an archive
  * 
  * @see DirectoryArchiveLoadStrategy
  */
 public abstract class DirectoryLoadStrategyImpl extends LoadStrategyImpl implements DirectoryArchiveLoadStrategy {
 	/**
 	 * The root uri from which all relative files entries are loaded; must point to a valid local
 	 * directory
 	 */
 	static boolean IS_AIX = "AIX".equals(System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
 	protected String directoryUri;
 	/**
 	 * Used internally; the directory uri with the system dependent file separator replaced by a
 	 * forward slash
 	 */
 	protected String directoryUriAsZipString;
 	protected static char SEPARATOR_CHAR = java.io.File.separatorChar;
 
 	public DirectoryLoadStrategyImpl(String aDirectoryUri) {
 		super();
 		setDirectoryUri(aDirectoryUri);
 		String normalized = null;
 		try {
 			normalized = new File(aDirectoryUri).getCanonicalPath();
 		} catch (IOException iox) {
 			normalized = aDirectoryUri;
 		}
 		setDirectoryUriAsZipString(normalized.replace(SEPARATOR_CHAR, '/'));
 
 	}
 
 	protected abstract void addDirectory(java.io.File aFile, List aList);
 
 	protected void addFile(java.io.File aFile, List aList) {
 
 		String uri = getURIFrom(aFile);
 		if (collectedLooseArchiveFiles.containsKey(uri))
 			return;
 
 		org.eclipse.jst.j2ee.commonarchivecore.internal.File cFile = createFile(uri);
 		cFile.setSize(aFile.length());
 		cFile.setLastModified(aFile.lastModified());
 		aList.add(cFile);
 	}
 
 	protected void addFiles(java.io.File aDirectory, List aList) {
 
 		String[] fileNames = aDirectory.list();
 		if (fileNames == null)
 			return;
 		for (int i = 0; i < fileNames.length; i++) {
 			String fileName = ArchiveUtil.concatUri(aDirectory.getPath(), fileNames[i], SEPARATOR_CHAR);
 			if (fileNames[i] == null || (IS_AIX && ".backup".equals(fileNames[i]))) //$NON-NLS-1$
 				continue;
 			java.io.File aFile = new java.io.File(fileName);
 			if (!aFile.exists())
 				continue;
 			//This could occur on some windows machines, eg C:\pagefile.sys
 			//throw new RuntimeException("Error scanning directory structure");
 			if (aFile.isDirectory() && !isArchive(getURIFrom(aFile))) {
 				addDirectory(aFile, aList);
 			} else {
 				addFile(aFile, aList);
 			}
 		}
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.impl.LoadStrategyImpl
 	 */
 	protected boolean primContains(java.lang.String uri) {
 		return new java.io.File(getFileNameFrom(uri)).exists();
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public java.lang.String getAbsolutePath() throws java.io.FileNotFoundException {
 		return new java.io.File(getDirectoryUri()).getAbsolutePath();
 	}
 
 	protected abstract java.io.File getDirectoryForList();
 
 	public java.lang.String getDirectoryUri() {
 		return directoryUri;
 	}
 
 	public java.lang.String getDirectoryUriAsZipString() {
 		return directoryUriAsZipString;
 	}
 
 	/**
 	 * Returns an OS filename from a relative uri
 	 */
 	// TODO Fix the type casing in v6.0
 	protected String getFileNameFrom(String uri) {
 		LooseArchive aLooseArchive = getLooseArchive();
 		if (aLooseArchive != null) {
 			String result = null;
 			switch (aLooseArchive.eClass().getClassifierID()) {
 				case LooseconfigPackage.LOOSE_APPLICATION :
 					result = getURIFromLooseArchivesIfAvailable(((LooseApplication) aLooseArchive).getLooseArchives(), uri);
 					break;
 				case LooseconfigPackage.LOOSE_WAR_FILE :
 					result = getURIFromLooseArchivesIfAvailable(((LooseWARFile) aLooseArchive).getLooseLibs(), uri);
 
 					break;
 			}
 			if (result != null)
 				return result;
 		}
 		String name = uri;
 		if (SEPARATOR_CHAR != '/')
 			name = name.replace('/', SEPARATOR_CHAR);
 		return getDirectoryUri() + SEPARATOR_CHAR + name;
 	}
 
 	private String getURIFromLooseArchivesIfAvailable(List looseArchives, String uri) {
 
 		for (Iterator iter = looseArchives.iterator(); iter.hasNext();) {
 			LooseArchive looseArchiveElement = (LooseArchive) iter.next();
 			if (uri.equals(looseArchiveElement.getUri()))
 				return looseArchiveElement.getBinariesPath();
 		}
 		return null;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.impl.LoadStrategyImpl
 	 */
 	public java.util.List getFiles() {
 		List list = new ArrayList();
 		java.io.File directory = getDirectoryForList();
 		addFiles(directory, list);
 		return list;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.impl.LoadStrategyImpl
 	 */
 	public java.io.InputStream getInputStream(java.lang.String uri) throws IOException, FileNotFoundException {
 		return new FileInputStream(getFileNameFrom(uri));
 	}
 
 	/**
 	 * Returns a relative uri from the java.io.File, to be used for a file entry; the separator will
 	 * be the zip standard (forward slash ("/")).
 	 */
 
 	protected String getURIFrom(File aFile) {
 		String name = ""; //$NON-NLS-1$
 		String relative = null;
 		String root = getDirectoryUriAsZipString();
 		try {
 			name = aFile.getCanonicalPath();
 			relative = makeRelative(name, root);
 		} catch (IOException iox) {
 			name = null;
 		}
 		if (relative == null) {
 			name = aFile.getAbsolutePath();
 			relative = makeRelative(name, root);
 		}
 		if (relative == null) {
 			name = aFile.getPath();
 			root = replaceSeparators(getDirectoryUri());
 			relative = makeRelative(name, root);
 		}
 		return relative;
 	}
 
 	private String replaceSeparators(String path) {
 		if (File.separatorChar != '/')
 			return path.replace(File.separatorChar, '/');
 		return path;
 	}
 
 	private String makeRelative(String fileName, String root) {
 		if (fileName == null || root == null)
 			return null;
 		String name = null;
		for (; root.endsWith("/"); root = ArchiveUtil.truncateIgnoreCase(root, "/")) //$NON-NLS-1$ //$NON-NLS-2$
 		name = replaceSeparators(fileName);
 		if (name.startsWith(root))
 			name = name.substring(root.length() + 1);
 		else
 			name = null;
 		return name;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public boolean isDirectory() {
 		return true;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public boolean isUsing(java.io.File aSystemFile) {
 		java.io.File dir = new java.io.File(getDirectoryUri());
 		return dir.equals(aSystemFile);
 	}
 
 	public void setDirectoryUri(java.lang.String newDirectoryUri) {
 		directoryUri = newDirectoryUri;
 	}
 
 	public void setDirectoryUriAsZipString(java.lang.String newDirectoryUriAsZipString) {
 		directoryUriAsZipString = newDirectoryUriAsZipString;
 	}
 }
