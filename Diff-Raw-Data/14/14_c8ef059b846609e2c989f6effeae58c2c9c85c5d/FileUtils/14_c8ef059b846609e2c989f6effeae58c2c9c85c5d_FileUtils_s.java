 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It provides a simple object for creating an editor instance.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.util;
 
 import java.io.File;
 
 import org.apache.commons.io.FilenameUtils;
 
 /**
 * The Class FileUtils.
  */
 public class FileUtils {
 
 	/**
 	 * Sanitizes a filename from certain chars.<br />
 	 * 
 	 * This method enforces the <code>forceSingleExtension</code> property and
 	 * then replaces all occurrences of \, /, |, :, ?, *, &quot;, &lt;, &gt;,
 	 * control chars by _ (underscore).
 	 * 
 	 * @param name
 	 *            a potentially 'malicious' filename
 	 * @return sanitized filename
 	 */
 	public static String sanitizeName(final String name) {
 		if (StringUtils.isNullOrEmpty(name))
 			return name;
 
 		// Remove \ / | : ? * " < > 'Control Chars' with _
 		return name.replaceAll("\\\\|/|\\||:|\\?|\\*|\"|<|>|\\p{Cntrl}", "_");
 	}
 
 	/**
 	 * Replaces all dots in a filename with underscores except the last one.
 	 * 
 	 * @param filename
 	 *            filename to sanitize
 	 * @return string with a single dot only
 	 */
 	public static String forceSingleExtension(final String filename) {
 		return filename.replaceAll("\\.(?![^.]+$)", "_");
 	}
 
 	/**
 	 * Checks if a filename contains more than one dot.
 	 * 
 	 * @param filename
 	 *            filename to check
 	 * @return <code>true</code> if filename contains severals dots, else
 	 *         <code>false</code>
 	 */
 	public static boolean isSingleExtension(final String filename) {
 		return filename.matches("[^\\.]+\\.[^\\.]+");
 	}
 
 	/**
	 * Checks a DIRECTORY for existence and creates it if non-existent.
 	 * 
 	 * @param dir
	 *            DIRECTORY to check/create
 	 */
 	public static void checkDirAndCreate(File dir) {
 		if (!dir.exists())
 			dir.mkdirs();
 	}
 
 	/**
 	 * Iterates over a base name and returns the first non-existent FILE.<br />
 	 * This method extracts a FILE's base name, iterates over it until the first
 	 * non-existent appearance with <code>basename(n).ext</code>. Where n is a
 	 * positive integer starting from one.
 	 * 
	 * @param FILE
	 *            base FILE
	 * @return first non-existent FILE
 	 */
 	public static File getUniqueFile(final File file) {
 		if (!file.exists())
 			return file;
 
 		File tmpFile = new File(file.getAbsolutePath());
 		File parentDir = tmpFile.getParentFile();
 		int count = 1;
 		String extension = FilenameUtils.getExtension(tmpFile.getName());
 		String baseName = FilenameUtils.getBaseName(tmpFile.getName());
 		do {
 			String fileName = String.format("%s(%d).%s", baseName, count, extension);
 			tmpFile = new File(parentDir, fileName);
 		} while (tmpFile.exists());
 		return tmpFile;
 	}
 }
