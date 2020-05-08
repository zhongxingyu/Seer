 package cn.uc.play.japid.util;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.commons.fileupload.InvalidFileNameException;
 import org.apache.commons.io.FileExistsException;
 
 /**
  * File Utilities.
  * 
  * @author Robin Han<sakuyahan@163.com>
  * @date 2012-4-29
  */
 public class FileUtils {
 	/**
 	 * Read all content from a file.
 	 * 
 	 * @param filePath
 	 *            File path.
 	 * @param charsetName
 	 *            Character set name.
 	 * @return Content of the file.
 	 * @throws IOException
 	 *             When file not found or stream error.
 	 */
 	public static String readToEnd(String filePath, String charsetName)
 			throws IOException {
 		byte[] buffer = new byte[(int) new File(filePath).length()];
 		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
 				filePath));
 		bis.read(buffer);
 		bis.close();
 		return new String(buffer, charsetName);
 	}
 
 	/**
 	 * Read all plaintext from a file.
 	 * 
 	 * @param filePath
 	 *            File path.
 	 * @return Content of the file.
 	 * @throws IOException
 	 *             When file not found or stream error.
 	 */
 	public static String readToEnd(String filePath) throws IOException {
 		return FileUtils.readToEnd(filePath, "UTF-8");
 	}
 
 	/**
 	 * Get file name in the path string. This method auto detect using which
 	 * separator.
 	 * 
 	 * @param path
 	 *            File path.
 	 * @return Filename.
 	 */
 	public static String getFileNameInPath(String filePath) {
 		return getFileNameInPath(filePath, File.separator);
 	}
 
 	/**
 	 * Get file name in the path string.
 	 * 
 	 * @param filePath
 	 *            File path.
 	 * @param pathSeparator
 	 *            The path separator.
 	 * @return filename.
 	 */
 	public static String getFileNameInPath(String filePath, String pathSeparator) {
 		if (filePath == null || filePath.isEmpty()) {
 			return filePath;
 		}
 
 		int last = filePath.lastIndexOf(pathSeparator);
 		if (last >= 0) {
 			return filePath.substring(last + 1);
 		}
 
 		return filePath;
 	}
 
 	/**
 	 * Get the relateive path.
 	 * 
 	 * @param path
 	 *            full path.
 	 * @param parentPath
 	 *            parent path.It will remove from the full path.
 	 * @return path string.
 	 */
 	public static String getRelativePath(String path, String parentPath) {
 		if (path == null || parentPath == null || !path.startsWith(parentPath)) {
 			return path;
 		}
 
 		if (path.length() == parentPath.length()) {
 			return "";
 		}
 
 		return path.substring(parentPath.length() + 1);
 	}
 
 	/**
 	 * Convert file extension name to newExt.
 	 * 
 	 * @param path
	 *            File path.
 	 * @param newExt
 	 *            New extension name.
 	 * @return File path with new extension.
 	 */
 	public static String convertExtensionTo(String path, String newExt) {
 		if (path == null) {
 			return null;
 		}
 
 		if (newExt == null) {
 			newExt = "";
 		} else if (!newExt.startsWith(".")) {
 			newExt = "." + newExt;
 		}
 
 		int index = path.lastIndexOf(".");
 
 		if (index < 0) {
 			return path + newExt;
 		}
 
 		return path.substring(0, index) + newExt;
 	}
 
 	/**
 	 * Remove file's extension from filePath.
 	 * 
 	 * @param filePath
 	 *            File path.
 	 * @return File path without file extension.
 	 */
 	public static String removeFileExtension(String filePath) {
 		if (filePath == null || filePath.isEmpty()) {
 			return filePath;
 		}
 
 		int extIndex = filePath.indexOf(".");
 
 		return extIndex < 0 ? filePath : filePath.substring(0, extIndex);
 	}
 
 	/**
 	 * Convert path to package. Use "." replace "/" or "\".
 	 * 
 	 * @param path
 	 * 
 	 * @return The package style string.
 	 */
 	public static String convertPathToPackage(String path) {
 		String pathWithoutExt = removeFileExtension(path);
 
 		if (pathWithoutExt == null || pathWithoutExt.isEmpty()) {
 			return path;
 		}
 
 		return pathWithoutExt.replace(File.separator, ".");
 	}
 
 	/**
 	 * Clear files by pattern.
 	 * 
 	 * @param parentPath
 	 *            Files' parent path.
 	 * @param pattern
 	 *            The pattern for matching the file you want to delete.
 	 * @param containsDescendants
 	 *            If you want delete all of the descendants files, set this by
 	 *            true.
 	 * @return
 	 * @throws FileNotFoundException
 	 *             When the parent path was not exists.
 	 */
 	public static int clearFiles(String parentPath, String pattern,
 			boolean containsDescendants) throws FileNotFoundException {
 		if (parentPath == null) {
 			return 0;
 		}
 
 		File dir = new File(parentPath);
 		if (!dir.exists() && !dir.isDirectory()) {
 			throw new FileNotFoundException(parentPath
 					+ " not found or it's not a directory.");
 		}
 
 		File[] currentLevel = dir.listFiles();
 		List<File> nextLevel = new ArrayList<File>();
 
 		if (currentLevel.length == 0) {
 			return 0;
 		}
 
 		int currentLevelIndex = 0;
 		int deleted = 0;
 		
 		while (currentLevel != null && currentLevel.length > 0) {
 			File file = currentLevel[currentLevelIndex];
 			if (file.isDirectory()) {
 				File[] subFiles = file.listFiles();
 				for (File f : subFiles) {
 					nextLevel.add(f);
 				}
 
 			} else if (file.getAbsolutePath().matches(pattern)) {
 				file.delete();
 				deleted++;
 			}
 
 			currentLevelIndex++;
 
 			if (currentLevelIndex == currentLevel.length) {
 				if (!containsDescendants) {
 					break;
 				}
 				currentLevel = nextLevel.toArray(new File[nextLevel.size()]);
 				currentLevelIndex = 0;
 				nextLevel = new ArrayList<File>();
 			}
 		}
 
 		return deleted;
 	}
 
 }
