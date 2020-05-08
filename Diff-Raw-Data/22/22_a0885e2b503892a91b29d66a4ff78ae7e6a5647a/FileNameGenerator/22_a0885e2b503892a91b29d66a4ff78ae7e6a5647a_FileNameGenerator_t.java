 /*
  * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package org.openscience.cdk.applications.taverna.basicutilities;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.UUID;
 
 import org.openscience.cdk.applications.taverna.CDKTavernaException;
 import org.openscience.cdk.applications.taverna.setup.SetupController;
 
 /**
  * Class which provides methods for a centralized file name handling within the
  * cdk-taverna project
  * 
  * @author Andreas Truszkowski
  * 
  */
 public class FileNameGenerator {
 
 	private static Hashtable<String, Integer> usedFilesTable = new Hashtable<String, Integer>();
 
 	/**
 	 * Generates a unique filen from the given parameters.
 	 * 
 	 * @param path
 	 *            Path to the file
 	 * @param extension
 	 *            The file extension
 	 * @return
 	 */
 	public synchronized static File getNewFile(String path, String extension) {
 		return getNewFile(path, extension, null, null);
 	}
 
 	/**
 	 * Generates a unique filen from the given parameters.
 	 * 
 	 * @param path
 	 *            Path to the file
 	 * @param extension
 	 *            The file extension
 	 * @return
 	 */
 	public synchronized static File getNewFile(String path, String extension, Integer iteration) {
 		return getNewFile(path, extension, null, iteration);
 	}
 
 	/**
 	 * Generates a unique filen from the given parameters.
 	 * 
 	 * @param path
 	 *            Path to the file
 	 * @param extension
 	 *            The file extension
 	 * @return
 	 */
 	public synchronized static File getNewFile(String path, String extension, String name) {
 		return getNewFile(path, extension, name, null);
 	}
 
 	/**
 	 * Generates a unique filen from the given parameters.
 	 * 
 	 * @param path
 	 *            Path to the file
 	 * @param extension
 	 *            The file extension
 	 * @return
 	 */
 	public synchronized static File getNewFile(String path, String extension, String name, Integer iteration) {
 		String filename = "";
 		File file = null;
 		Integer idx = 1;
 		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
 		filename += path;
 		if (!path.endsWith(File.separator)) {
 			filename += File.separator;
 		}
 		if (iteration != null) {
 			filename += iteration + "_";
 		}
 		if (name != null) {
 			filename += name + "_";
 		}
 		filename += dateformat.format(new Date());
 		String temp;
 		String key = path + "_" + iteration + "_" + name + extension;
 		if (usedFilesTable.get(key) != null) {
 			idx = usedFilesTable.get(key);
 		}
 		do {
 			temp = "_" + idx;
 			file = new File(filename + temp + extension);
 			idx++;
 		} while (file.exists());
 		usedFilesTable.put(key, idx);
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			ErrorLogger.getInstance().writeError(CDKTavernaException.FILE_CREATION_ERROR, "FileNameGenerator", e);
 		}
 		return file;
 	}
 
 	/**
 	 * Generates a filen from the given parameters.
 	 * 
 	 * @param path
 	 *            Path to the file
 	 * @param extension
 	 *            The file extension
 	 * @return
 	 */
 	public synchronized static File getNewFileFromUUID(String path, String extension, UUID uuid) {
 		String filename = "";
 		File file = null;
 		filename += path;
 		if (!path.endsWith(File.separator)) {
 			filename += File.separator;
 		}
 		filename += uuid.toString();
 		filename += extension;
 		file = new File(filename);
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			ErrorLogger.getInstance().writeError(CDKTavernaException.FILE_CREATION_ERROR, "FileNameGenerator", e);
 		}
 		return file;
 	}
 
 	/**
 	 * @return path of the OS temporary directory.
 	 */
 	public synchronized static String getTempDir() {
		String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "cdk-taverna_2" + File.separator;
 		File file = new File(tmpDir);
 		if (!file.exists()) {
 			file.mkdir();
 		}
 		return tmpDir;
 	}
 
 	/**
 	 * @return path to the application directory
 	 */
 	public static String getApplicationDir() {
 		return new java.io.File("").getAbsolutePath() + File.separator;
 	}
 
 	/**
 	 * @return path to the cache directory. It's located in the OS temporary
 	 *         directory.
 	 */
 	public synchronized static String getCacheDir() {
 		String cacheDir = SetupController.getInstance().getWorkingDir();
 		cacheDir += "cache" + File.separator;
 		File file = new File(cacheDir);
 		if (!file.exists()) {
 			file.mkdir();
 		}
 		return cacheDir;
 	}
 
 	/**
 	 * @return path to the log directory. It's located in the OS temporary
 	 *         directory.
 	 */
 	public synchronized static String getLogDir() {
 		String logDir = SetupController.getInstance().getWorkingDir();
 		logDir += "log" + File.separator;
 		File file = new File(logDir);
 		if (!file.exists()) {
 			file.mkdir();
 		}
 		return logDir;
 	}
 
 	/**
 	 * Deletes all files and subdirectories under dir. Returns true if all
 	 * deletions were successful. If a deletion fails, the method stops
 	 * attempting to delete and returns false.
 	 * 
 	 * @param dir
 	 *            Directory to delete
 	 * @return
 	 */
 	public synchronized static boolean deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	/**
 	 * Copies target file.
 	 * 
 	 * @param inputFile
 	 * @param outputFile
 	 * @throws IOException
 	 */
 	public synchronized static void copyFile(File inputFile, File outputFile) throws IOException {
 		FileReader in = new FileReader(inputFile);
 		FileWriter out = new FileWriter(outputFile);
 		int c;
 		while ((c = in.read()) != -1)
 			out.write(c);
 		in.close();
 		out.close();
 	}
 
 }
