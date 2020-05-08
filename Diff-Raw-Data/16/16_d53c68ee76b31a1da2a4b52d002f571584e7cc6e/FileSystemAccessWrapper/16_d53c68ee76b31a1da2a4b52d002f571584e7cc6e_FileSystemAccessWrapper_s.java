 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.controller.filesystem;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FileSystemAccessWrapper {
 	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemAccessWrapper.class);
 	private static final boolean OVERWRITE_EXISITING_FILES = true;	
 	/** A simple regular expression.. to prevent album names whose folders of the same name cause problems on the filesystem
 	 * Minimum length is 3 alphanumeric characters possibly white spaces, underscores (u005F) hyphen_minuses (u002D). */
 	private static final String ALBUM_NAME_REGEX = "^(\\w|\\u005F|\\s|\\u002D){3,}$";
 	
 	/** Creates the directory using the specified path. All errors that might occur will be logged 
 	 * @param directoryPath the complete (absolute) directory path. E.g. /home/user/folder1
 	 * @return true if no problem occurred, false otherwise*/
 	private static boolean createDirectoryAndLogError(String directoryPath) {
 		File directory = new File(directoryPath);
 		
 		if (!directory.exists()) {
 			if (!directory.mkdir()) {
 				LOGGER.error("Cannot create '" + directoryPath + "' "
 						+ "although it seems that the directory does not yet exist");
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/** This method is used to create and update the file-structure
 	 *  This method must be called during initialization of the program.*/
 	public static boolean updateSammelboxFileStructure() {		
 		boolean errorOccurred = false;
 				
 		errorOccurred = errorOccurred || createDirectoryAndLogError(FileSystemLocations.getActiveHomeDir());
 		errorOccurred = errorOccurred || createDirectoryAndLogError(FileSystemLocations.getAlbumPicturesDir());
 		errorOccurred = errorOccurred || createDirectoryAndLogError(FileSystemLocations.getThumbnailsDir());
 		errorOccurred = errorOccurred || createDirectoryAndLogError(FileSystemLocations.getAppDataDir());
 		errorOccurred = errorOccurred || createDirectoryAndLogError(FileSystemLocations.getBackupDir());
 		
 		if (errorOccurred) {
 			return false;
 		}
 			
 		extractResourceToFile("graphics/placeholder.png", FileSystemLocations.getPlaceholderPNG());
 		extractResourceToFile("graphics/placeholder2.png", FileSystemLocations.getPlaceholder2PNG());
 		extractResourceToFile("graphics/placeholder3.png", FileSystemLocations.getPlaceholder3PNG());
 		extractResourceToFile("graphics/zerostars.png", FileSystemLocations.getZeroStarsPNG());
 		extractResourceToFile("graphics/onestar.png", FileSystemLocations.getOneStarPNG());
 		extractResourceToFile("graphics/twostars.png", FileSystemLocations.getTwoStarsPNG());
 		extractResourceToFile("graphics/threestars.png", FileSystemLocations.getThreeStarsPNG());
 		extractResourceToFile("graphics/fourstars.png", FileSystemLocations.getFourStarsPNG());
 		extractResourceToFile("graphics/fivestars.png", FileSystemLocations.getFiveStarsPNG());
 		extractResourceToFile("graphics/logo.png", FileSystemLocations.getLogoPNG());
 		extractResourceToFile("graphics/logo-small.png", FileSystemLocations.getLogoSmallPNG());
 		extractResourceToFile("javascript/effects.js", FileSystemLocations.getEffectsJS());
		extractResourceToFile("stylesheets/style.css", FileSystemLocations.getStyleJS());
 
 		// Add the lock file
 		try {
 			File lockFile = new File(FileSystemLocations.getLockFile());
 			if (!lockFile.exists()) {
 				lockFile.createNewFile();
 			}
 		} catch (IOException e) {
 			LOGGER.error("Cannot create '" + FileSystemLocations.getLockFile() + "' "
 					+ "although it seems that the directory does not yet exist", e);
 			return false;
 		}
 		
 		return true;
 	}
 
 	/** Extracts the specified resource to the specified output location
 	 * @param resourceName the name of the resource to be extracted 
 	 * @param outputResourcePath the absolute location to which the resource should be extracted */
 	private static void extractResourceToFile(String resourceName, String outputResourcePath) {
 		try {
 			File resource = new File(outputResourcePath);
 			
 			if (!resource.exists() || OVERWRITE_EXISITING_FILES) {			
 				InputStream istream = FileSystemAccessWrapper.class.getClassLoader().getResourceAsStream(resourceName);
 				OutputStream ostream = new FileOutputStream(outputResourcePath);
 	
 				byte buffer[] = new byte[1024];
 				int length;
 				
 				while((length=istream.read(buffer)) > 0) {
 					ostream.write(buffer, 0, length);
 				}
 				
 				ostream.close();
 				istream.close();
 			}
 		} catch (Exception ex) {
 			LOGGER.error("An error occured while extracting the resource (" + resourceName + ") to " + outputResourcePath, ex);
 		}
 	}
 
 	/** This method is used to create album picture folders if the album is set to contain pictures and the folder does not exist yet.
 	 *  During this process no existing directory or file is overwritten.
 	 *  This method must be called after a new album is created or altered.
 	 *  It should be also called during initialization of the program after the file structure has been updated. */
 	public static boolean updateAlbumFileStructure(Connection databaseConnection) {
 		// Create inside the album home directory all album directories
 		try {
 			for (String albumName : DatabaseOperations.getListOfAllAlbums()) {
 				File albumDirectory = new File(getFilePathForAlbum(albumName));
 
 				if (DatabaseOperations.isPictureAlbum(albumName) && !albumDirectory.exists()) {
 					if (!albumDirectory.mkdir()) {
 						LOGGER.error("Cannot create album directory although it seems that it does not exist");
 						return false;
 					}
 				}
 			}
 			return true;
 		} catch (DatabaseWrapperOperationException e) {
 			LOGGER.error("Updating the album file structure failed.", e);
 			return false;
 		}		
 	}
 
 	/**
 	 * Gets the path to the respective picture folders.
 	 * @param albumName The name of the album whose picture folder is requested.
 	 * @return The path to the picture directory.
 	 */
 	public static String getFilePathForAlbum(String albumName) {
 		return FileSystemLocations.getAlbumPicturesDir() + File.separatorChar + albumName;
 	}
 
 	/**
 	 * Recursively copies a directory and its content from the source to the target location.
 	 * @param sourceLocation The file handle to the source directory.
 	 * @param targetLocation The file handle to the target directory.
 	 * @throws IOException Exception raised if a problem is encountered during the copy process.
 	 */
 	public static void copyDirectory(File sourceLocation , File targetLocation)
 			throws IOException {
 
 		copyDirectory(sourceLocation, targetLocation, null);
 	}
 	
 	/**
 	 * Recursively copies a directory and its content from the source to the target location.
 	 * @param sourceLocation The file handle to the source directory.
 	 * @param targetLocation The file handle to the target directory.
 	 * @param excludeFileRegex Excludes files from being copied which match this regex. Null or empty string will match no file.
 	 * @throws IOException Exception raised if a problem is encountered during the copy process.
 	 */
 	public static void copyDirectory(File sourceLocation , File targetLocation, String excludeFileRegex) throws IOException {
 
 		if (sourceLocation.isDirectory()) {
 			if (!targetLocation.exists()) {
 				targetLocation.mkdir();
 			}
 
 			String[] children = sourceLocation.list();
 			for (int i=0; i<children.length; i++) {
 				copyDirectory(	new File(sourceLocation, children[i]), new File(targetLocation, children[i]), excludeFileRegex);
 			}
 		} else {
 			if (excludeFileRegex != null && !excludeFileRegex.isEmpty() && sourceLocation.getCanonicalFile().getName().matches(excludeFileRegex)) {
 				return;
 			}
 			copyFile(sourceLocation, targetLocation);
 		}
 	}
 
 	public static void copyFile(File sourceLocation , File targetLocation) throws IOException {
 		InputStream in = new FileInputStream(sourceLocation);
 		OutputStream out = new FileOutputStream(targetLocation);
 
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 	
 	/**
 	 * Provides a file which points to the picture location within the application folder.
 	 * @param fileNameWithExtension The name including file type extension of the picture.
 	 * @param albumName The name of the album to which the picture
 	 * @return The file which points to the physical location of the picture.
 	 */
 	public static File getFileInAppHome(String fileNameWithExtension, String albumName) {
 		return new File(FileSystemLocations.getAlbumPicturesDir() + File.separatorChar + albumName + File.separatorChar + fileNameWithExtension);
 	}
 
 	/**
 	 * Rename the old picture folder of the oldAlbumName to the new name. If it did no exist create the new folder anyway.
 	 * @param oldAlbumName The old name of the album
 	 * @param newAlbumName The new name of the album.
 	 * @return True if either the folder was succesfully renamed or the new folder was successfully create. False if either operation failed.
 	 */
 	public static boolean renameAlbumPictureFolder(String oldAlbumName, String newAlbumName) {
 		// Get the standard old picturefolderpath
 		File oldalbumPicDir = new File (getFilePathForAlbum(oldAlbumName));
 		// Get the standard new picturefolderpath
 		File newalbumPicDir = new File(getFilePathForAlbum(newAlbumName));
 		// Test if the old exists
 		if (!oldalbumPicDir.exists()) {
 			// Create the new picture folder anyway
 			return newalbumPicDir.mkdir();
 		}
 		return oldalbumPicDir.renameTo(newalbumPicDir);
 	}
 
 	/**
 	 * Internal method to add folders to the zip file.
 	 * @param zipOutputStream The output stream to which the zip file is written.
 	 * @param parentName The name of the parent directory.
 	 * @param locationFile The file handle to the source files.
 	 */
 	private static void addDirectory(ZipOutputStream zipOutputStream, String parentName, File locationFile) {
 		// retrieve all sub directories and files from the file/folder
 		File[] files = locationFile.listFiles();
 
 		for (int i=0; i < files.length; i++) {
 			// if the file is directory, call the function recursively
 			if (files[i].isDirectory()) {
 				String zipFolderPath = parentName;
 				if (parentName.isEmpty()) {
 					zipFolderPath = files[i].getName() + "/";//File.separatorChar;	
 				} else {
 					zipFolderPath += files[i].getName() + "/";// File.separatorChar;
 				}
 				addDirectory(zipOutputStream, zipFolderPath, files[i]);
 				continue;
 			}
 
 			// being here means that we have a file and not a directory
 			try {
 				byte[] buffer = new byte[1024];
 				FileInputStream fileInputStream = new FileInputStream(files[i]);
 				zipOutputStream.putNextEntry(new ZipEntry(parentName + files[i].getName()));
 
 				// Store the file inside the zip
 				int length;
 				while((length = fileInputStream.read(buffer)) > 0) {
 					zipOutputStream.write(buffer, 0, length);
 				}
 
 				// close current zip entry and the input stream of the current file
 				zipOutputStream.closeEntry();
 				fileInputStream.close();
 			}
 			catch(IOException ioe){
 				LOGGER.error("Adding folder {} to zip file failed.", ioe);				
 			}
 		}
 	}
 
 	/**
 	 * Zips a folder recursively into a file.
 	 * @param folderLocation The path to the source directory.
 	 * @param zipLocation The path to the target zip file.
 	 */
 	public static void zipFolderToFile(String folderLocation, String zipLocation) {
 		try {
 			FileOutputStream fileOutputStream = new FileOutputStream(zipLocation);
 			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
 
 			File folderLocationFile = new File(folderLocation);
 			addDirectory(zipOutputStream, "", folderLocationFile);
 
 			zipOutputStream.close();
 		}
 		catch(IOException ioe) {
 			LOGGER.error("Zipping a folder into a zip file failed " , ioe);
 		}
 	}
 
 	/**
 	 * Unzips a file to the specified location, recreating the original file structure within it
 	 * @param zipLocation The path to the future location of the zip file.
 	 * @param folderLocation The path to the source directory.
 	 */
 	public static void unzipFileToFolder(String zipLocation, String folderLocation) {		
 		try {
 			ZipFile zipFile = new ZipFile(new File(zipLocation), ZipFile.OPEN_READ);
 
 			Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
 
 			while (zipFileEntries.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
 
 				if (!entry.isDirectory()) {
 					File destFile = new File(folderLocation, entry.getName());
 					File destinationParent = destFile.getParentFile();
 
 					destinationParent.mkdirs();
 
 					FileOutputStream fileOutputStream = new FileOutputStream(destFile);
 					InputStream inputStream = zipFile.getInputStream(entry);
 
 					// Copy the bits from inputStream to fileOutputStream
 					byte[] buf = new byte[1024];
 					int len;
 
 					while ((len = inputStream.read(buf)) > 0) {
 						fileOutputStream.write(buf, 0, len);
 					}
 
 					fileOutputStream.close();
 				}
 			}
 			
 			zipFile.close();
 		} catch (IOException ioe) {
 			LOGGER.error("An error occured while unziping the archive to the folder", ioe);
 		}
 
 	}
 
 	/**
 	 * Deletes the specified file
 	 * @param pathToFile The path to the file which is to be deleted
 	 * @return true if the directory was successfully deleted, false otherwise
 	 */
 	public static boolean deleteFile(String pathToFile) {
 		return deleteFile(new File(pathToFile));
 	}
 	
 	/**
 	 * Deletes the specified file
 	 * @param file The file which is to be deleted
 	 * @return true if the directory was successfully deleted, false otherwise
 	 */
 	public static boolean deleteFile(File file) {
 		return file.delete();
 	}
 	
 	/**
 	 * Deletes all content within the specified directory.
 	 * @param directory The file to the directory which is to be deleted.
 	 * @return true if the directory was successfully deleted, false otherwise
 	 */
 	public static boolean deleteDirectoryRecursively(File directory) {
 		if( directory.exists() ) {
 			if (directory.isDirectory()) {
 				File[] files = directory.listFiles();
 				for(int i=0; i<files.length; i++) {
 					if(files[i].isDirectory()) {
 						deleteDirectoryRecursively(files[i]);
 					} else {
 						files[i].delete();
 					}
 				}
 			}
 		}
 		
 		return(directory.delete());
 	}
 
 	/**
 	 * Convenience method to erase all content from the default application home directory.
 	 * The home directory as well as the database will still exist after calling this method. 
 	 */
 	public static void clearHomeDirectory() {	
 		File[] files = new File(FileSystemLocations.getActiveHomeDir()).listFiles();
 
 		for (int i=0; i<files.length; i++) {
 			if (files[i].isDirectory()) {
 				deleteDirectoryRecursively(files[i]);
 			} else if (!files[i].getName().equals(FileSystemLocations.DATABASE_NAME)) {
 				files[i].delete();
 			}
 		}
 	}
 
 	/**
 	 * Convenience method to remove the default application home directory. 
 	 */
 	public static void removeHomeDirectory() {
 		File homeDirectory = new File(FileSystemLocations.getActiveHomeDir());
 
 		if (homeDirectory.exists()) {
 			FileSystemAccessWrapper.clearHomeDirectory();
 
 			new File(FileSystemLocations.getDatabaseFile()).delete();
 
 			homeDirectory.delete();
 		}
 	}
 
 	/**
 	 * Convenience method to delete the temporary database file {@link #DATABASE_TO_RESTORE}
 	 * @return true if and only if the file or directory is successfully deleted; false otherwise 
 	 */
 	public static boolean deleteDatabaseRestoreFile() {
 		File file = new File(FileSystemLocations.getDatabaseRestoreFile());
 		return file.delete();
 	}
 	
 	/**
 	 * Retrieves a list of files whose file name matches the provided regex. Only files in the appdate folder (no directories) are matched.
 	 * @param fileNameRegex The regular expression to match files in the home directory against.
 	 * @return A list of file handles to the matching files. Empty list if none are found.
 	 */
 	public static List<File> getAllMatchingFilesInHomeDirectory(String fileNameRegex) {
 		List<File> matchingFiles = new ArrayList<File>();
 		File path = new File(FileSystemLocations.getAppDataDir());
 		File[] files = path.listFiles();
 		for(int i=0; i<files.length; i++) {
 			if(!files[i].isDirectory()) {
 				File currentFile;
 				try {
 					currentFile = files[i].getCanonicalFile();
 				} catch (IOException ex) {
 					LOGGER.error("An error occured while processing files", ex);
 					continue;
 				}
 				if (currentFile.getName().matches(fileNameRegex)){
 					matchingFiles.add(currentFile);
 				}
 			}
 		}		
 		return matchingFiles;		
 	}
 
 	public static void writeToFile(String content, String filepath) {
 		try {
 			PrintWriter out = new PrintWriter(filepath);
 
 			out.write(content);
 
 			out.close();
 		} catch (Exception ex) {
 			LOGGER.error("An error occured while writing '" + content + "' to filepath", ex);
 		}
 	}
 	
 	/** Reads the specified file into a string
 	 * @param filePath the path to the file that should be read
 	 * @return the content of the specified file as a string or an empty string if the file does not exist */
 	public static String readFileAsString(String filePath) {
 		File file = new File(filePath);
 		if (!file.exists()) {
 			return "";
 		}
 
 		byte[] buffer = new byte[(int) new File(filePath).length()];
 
 		try {
 			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
 			bufferedInputStream.read(buffer);
 			bufferedInputStream.close();
 
 			return new String(buffer);
 		} catch (Exception e) {
 			LOGGER.error("An error occured while reading the file (" + filePath + ") into a string", e);
 		}
 
 		return new String(buffer);
 	}
 	
 	public static String readInputStreamIntoString(InputStream fileInputStream) {
 		StringBuilder stringBuilder = new StringBuilder();
 
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
 			String line = null;
 			String ls = System.getProperty("line.separator");
 			while((line = reader.readLine()) != null) {
 				stringBuilder.append(line);
 				stringBuilder.append(ls);
 			}
 		} catch (IOException ioe) {
 			LOGGER.error("An error occured while reading the input stream to a string", ioe);
 		}
 
 		return stringBuilder.toString();
 	}
 	
 	/** Returns the file extension by searching for the last dot in the given string 
 	 * @return the extension without the dot, or an empty string if no extension can be detected */
 	public static String getFileExtension(String fileNameOrPath) {
 		int positionOfLastDot = fileNameOrPath.lastIndexOf('.');
 		if (positionOfLastDot > 0) {
 		    return fileNameOrPath.substring(positionOfLastDot + 1);
 		}
 		
 		return "";
 	}
 	
 	/** This method tests whether the given album name could be used to create a picture folder having the same name 
 	 * @return if the albumName can be used to create a picture folder, false otherwise*/
 	public static boolean isSAlbumNameFileSystemCompliant(String albumName) {
 		if (!albumName.matches(ALBUM_NAME_REGEX)) {
 			return false;
 		}
 		
 		File testFile = new File(getFilePathForAlbum(albumName));
 		try {
 			testFile.getCanonicalPath();
 		} catch (IOException e) {
 			return false;
 		}
 		
 		return true;
 	}
 }
