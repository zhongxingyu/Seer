 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.localstore;
 
 import java.io.File;
 
 import org.eclipse.core.internal.resources.ResourceStatus;
 import org.eclipse.core.internal.utils.Convert;
 import org.eclipse.core.internal.utils.Policy;
 import org.eclipse.core.resources.IResourceStatus;
 import org.eclipse.core.resources.ResourcesPlugin;
 
 public abstract class CoreFileSystemLibrary {
 
 	/** Indicates whether or not this FS is case sensitive */
 	private static final boolean caseSensitive = new File("a").compareTo(new File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$
 	
 	/**
 	 * The following masks are used to represent the bits
 	 * returned by the getStat() and internalGetStat() methods.
 	 * The idea is to save JNI calls. So internalGetStat() is a native
 	 * that grabs as many important information as it cans and put in
 	 * a long variable.
 	 * The lower bits represent the last modified timestamp of the
 	 * given file and the higher bits represent some relevant flags.
 	 */
 
 	/** reserved, should not be used */
 	private static final long STAT_RESERVED = 0x8000000000000000l;
 	/** indicates if this is a valid stat or some problem happened when 
 		retrieving the information */
 	private static final long STAT_VALID = 0x4000000000000000l;
 	/** indicates if the resource is a folder or a file */
 	private static final long STAT_FOLDER = 0x2000000000000000l;
 	/** indicates if the resource is marked as read-only */
 	private static final long STAT_READ_ONLY = 0x1000000000000000l;
 	/** used to extract the last modified timestamp */
 	private static final long STAT_LASTMODIFIED = ~(STAT_RESERVED | STAT_VALID | STAT_FOLDER | STAT_READ_ONLY);
 
 	/** instance of this library */
 	// The name convention is to get the plugin version at the time
 	// the library is changed.  
 	private static final String LIBRARY_NAME = "core_2_1_0b"; //$NON-NLS-1$
 	private static boolean hasNatives = false;
 	private static boolean isUnicode = false;
 
 	static {
 		try {
 			System.loadLibrary(LIBRARY_NAME);			
 			hasNatives = true;
 			isUnicode = internalIsUnicode();
 		} catch (UnsatisfiedLinkError e) {
 			logMissingNativeLibrary(e);
 		}
 	}
 
 	public static long getLastModified(long stat) {
 		return (stat & STAT_LASTMODIFIED);
 	}
 	public static long getLastModified(String fileName) {
 		if (hasNatives)
 			return getLastModified(getStat(fileName));
 
 		// inlined (no native) implementation
 		return new File(fileName).lastModified();
 	}
 	public static long getStat(String fileName) {
 		if (hasNatives)
 			return isUnicode ? internalGetStatW(fileName.toCharArray()) : internalGetStat(Convert.toPlatformBytes(fileName));
 
 		// inlined (no native) implementation
 		File target = new File(fileName);
 		long result = target.lastModified();
 		if (result == 0) // non-existing
 			return result;
 		result |= STAT_VALID;
 		if (target.isDirectory())
 			result |= STAT_FOLDER;
 		if (!(new File(fileName).canWrite()))
 			result |= STAT_READ_ONLY;
 		return result;
 	}
 	private static void logMissingNativeLibrary(UnsatisfiedLinkError e) {
 		String libName = System.mapLibraryName(LIBRARY_NAME);
 	String message = Policy.bind("localstore.couldNotLoadLibrary", libName); //$NON-NLS-1$
 	ResourceStatus status = new ResourceStatus(IResourceStatus.INFO, null, message, null);
 	ResourcesPlugin.getPlugin().getLog().log(status);
 	}
 	
 	/**
 	 * Returns the stat information for the specified filename in a long (64 bits). We just
 	 * retrieve the stat information we consider necessary and store everything in one long
 	 * to save some JNI calls (standard version)
 	 */
 	private static final native long internalGetStat(byte[] fileName);
 	
 	/**
 	 * Returns the stat information for the specified filename in a long (64 bits). We just
 	 * retrieve the stat information we consider necessary and store everything in one long
 	 * to save some JNI calls (Unicode version - should not be called if <code>isUnicode</code>
 	 * is <code>false</code>).
 	 */	
 	private static final native long internalGetStatW(char[] fileName);
 
 	/**
 	 * Returns <code>true</code> if the underlying file system API supports Unicode,
 	 * <code>false</code> otherwise.
 	 */	
 	private static final native boolean internalIsUnicode();
 	/** Set/unset the given file as read-only (Unicode
 	 * version - should not be called if <code>isUnicode</code> is
 	 * <code>false</code>). */
 	private static final native boolean internalSetReadOnlyW(char[] fileName, boolean readOnly);
 	/** Set/unset the given file as read-only. */
 	private static final native boolean internalSetReadOnly(byte[] fileName, boolean readOnly);
 	public static boolean isFile(long stat) {
 		return isSet(stat, STAT_VALID) && !isSet(stat, STAT_FOLDER);
 	}
 	public static boolean isFolder(long stat) {
 		return isSet(stat, STAT_VALID) && isSet(stat, STAT_FOLDER);
 	}
 	public static boolean isReadOnly(String fileName) {
 		// Use the same implementation whether or not we are using
 		// the natives. If the file doesn't exist then getStat() will return 0
 		// and this method will return false.
 		return isSet(getStat(fileName), STAT_READ_ONLY);
 	}
 	public static boolean isReadOnly(long stat) {
 		return isSet(stat, STAT_READ_ONLY);
 	}
 	private static boolean isSet(long stat, long mask) {
 		return (stat & mask) != 0;
 	}
 	public static boolean setReadOnly(String fileName, boolean readOnly) {
 		if (hasNatives)
 			return isUnicode ? internalSetReadOnlyW(fileName.toCharArray(), readOnly) : internalSetReadOnly(Convert.toPlatformBytes(fileName), readOnly);
 
 		// inlined (no native) implementation
 		if (!readOnly)
 			return false; // not supported
 		return new File(fileName).setReadOnly();
 	}
 	public static boolean isCaseSensitive() {
 		return caseSensitive;
 	} 
 	/**
 	 * Copies file attributes from source to destination. The copyLastModified attribute
 	 * indicates whether the lastModified attribute should be copied.
 	 */
 	public static boolean copyAttributes(String source, String destination, boolean copyLastModified) {
 		if (hasNatives) 
 			return isUnicode ? internalCopyAttributesW(source.toCharArray(), destination.toCharArray(), copyLastModified) : internalCopyAttributes(Convert.toPlatformBytes(source), Convert.toPlatformBytes(destination), copyLastModified); 
 		return false; // not supported
 	}
 	/**
 	 * Return <code>true</code> if we have found the core library and are using it for
 	 * our file-system calls, and <code>false</code> otherwise.
 	 */
 	public static boolean usingNatives() {
 		return hasNatives;
 	}
 
 	/**
 	 * Copies file attributes from source to destination. The copyLastModified attribute
 	 * indicates whether the lastModified attribute should be copied.
 	 */
 	private static final native boolean internalCopyAttributes(byte[] source, byte[] destination, boolean copyLastModified);
 	
 	/**
 	 * Copies file attributes from source to destination. The copyLastModified attribute
 	 * indicates whether the lastModified attribute should be copied (Unicode
 	 * version - should not be called if <code>isUnicode</code> is
 	 * <code>false</code>).
 	 */
 	private static final native boolean internalCopyAttributesW(char[] source, char[] destination, boolean copyLastModified);	
 }
