 package com.robonobo.common.util;
 
 /*
  * Robonobo Common Utils
  * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)
 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */

 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class FileUtil {
 	private static final int BYTES_IN_KB = 1024;
 	private static final int BYTES_IN_MB = 1024 * 1024;
 	private static final int BYTES_IN_GB = 1024 * 1024 * 1024;
 
 	static public boolean deleteDirectory(File path) {
 		if (path.exists()) {
 			File[] files = path.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				if (files[i].isDirectory()) {
 					if (!deleteDirectory(files[i]))
 						return false;
 				} else {
 					if (!files[i].delete())
 						return false;
 				}
 			}
 		}
 		return path.delete();
 	}
 
 	public static String makeFileNameSafe(String str) {
 		String badChars = "[*?/\\\\:;]";
 		return str.replaceAll(badChars, "-");
 	}
 
 	public static String humanReadableSize(long numBytes) {
 		if (numBytes >= BYTES_IN_GB) {
 			double numGigs = (double) numBytes / (double) BYTES_IN_GB;
 			return TextUtil.padToMinWidth(numGigs, 3) + " GB";
 		}
 		if (numBytes >= BYTES_IN_MB) {
 			double numMegs = (double) numBytes / (double) BYTES_IN_MB;
 			return TextUtil.padToMinWidth(numMegs, 3) + " MB";
 		}
 		if (numBytes >= BYTES_IN_KB) {
 			double numK = (double) numBytes / (double) BYTES_IN_KB;
 			return TextUtil.padToMinWidth(numK, 3) + " KB";
 		}
 		return TextUtil.padToMinWidth(numBytes, 3) + " B";
 	}
 
	/**
	 * Returns the file extension, without the initial period. If there is no
	 * period in the file name, returns an empty string.
	 */
 	public static String getFileExtension(File f) {
 		String fileName = f.getName();
 		if (fileName.contains("."))
 			return fileName.substring(fileName.lastIndexOf(".") + 1);
 		else
 			return "";
 	}
 
	/**
	 * Returns all the files within the parent directory (including sub-dirs)
	 * that have the supplied extension. If extension is null, will return all
	 * files. Will not return the sub-dirs themselves.
 	 * 
	 * If the path is a file, it will be returned in the list it matches, or else an empty list will be returned
	 */
 	public static List<File> getFilesWithinPath(File path, String fileExtension) {
 		List<File> result = new ArrayList<File>();
 		if (!path.isDirectory()) {
 			if (fileExtension == null || fileExtension.equalsIgnoreCase(getFileExtension(path)))
 				result.add(path);
 			return result;
 		}
 		addChildFilesToList(path, result, fileExtension);
 		return result;
 	}
 
 	public static void copyFile(File currentFile, File destFile) throws IOException {
 		FileInputStream fis = new FileInputStream(currentFile);
 		FileOutputStream fos = new FileOutputStream(destFile);
 		ByteUtil.streamDump(fis, fos);
 	}
	
 	private static void addChildFilesToList(File directory, List<File> list, String fileExtension) {
		for (File f : directory.listFiles()) {
 			if (f.isDirectory())
 				addChildFilesToList(f, list, fileExtension);
 			else if (fileExtension == null || fileExtension.equalsIgnoreCase(getFileExtension(f)))
 				list.add(f);
 		}
 	}
 }
