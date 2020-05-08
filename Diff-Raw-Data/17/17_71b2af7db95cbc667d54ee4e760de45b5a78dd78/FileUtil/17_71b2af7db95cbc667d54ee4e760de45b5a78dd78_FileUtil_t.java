 /**
  *
  */
 package org.selfip.bkimmel.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.nio.ByteBuffer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.selfip.bkimmel.util.UnexpectedException;
 
 /**
  * File I/O utility methods.
  * @author brad
  */
 public final class FileUtil {
 
 	/**
 	 * Recursively removes all entries from a directory.
 	 * @param directory The directory to clear.
 	 * @return A value indicating whether the directory was successfully
 	 * 		cleared.  If unsuccessful, the directory may have been partially
 	 * 		cleared.
 	 */
 	public static boolean clearDirectory(File directory) {
 		if (directory.isDirectory()) {
 			for (File file : directory.listFiles()) {
 				if (!deleteRecursive(file)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Recursively removes a file or a directory and all its subdirectories.
 	 * @param file The file or directory to delete.
 	 * @return A value indicating whether the file directory was successfully
 	 * 		removed.  If unsuccessful, the directory may have been partially
 	 * 		cleared.
 	 */
 	public static boolean deleteRecursive(File file) {
 		return clearDirectory(file) && file.delete();
 	}
 
 	/**
 	 * Reads the contents of a file into a byte array.
 	 * @param file The <code>File</code> to read.
 	 * @return A byte array containing the file's contents.
 	 * @throws IOException If an error occurred while reading the file.
 	 */
 	public static byte[] getFileContents(File file) throws IOException {
 		FileInputStream stream = new FileInputStream(file);
 		byte[] contents = new byte[(int) file.length()];
 		stream.read(contents);
 		stream.close();
 		return contents;
 	}
 
 	/**
 	 * Writes the specified byte array to a file.
 	 * @param file The <code>File</code> to write.
 	 * @param contents The byte array to write to the file.
 	 * @throws IOException If the file could not be written.
 	 */
 	public static void setFileContents(File file, byte[] contents) throws IOException {
 		setFileContents(file, contents, false);
 	}
 
 	/**
 	 * Writes the specified byte array to a file.
 	 * @param file The <code>File</code> to write.
 	 * @param contents The byte array to write to the file.
 	 * @param createDirectory A value indicating whether the directory
 	 * 		containing the file to be written should be created if it does
 	 * 		not exist.
 	 * @throws IOException If the file could not be written.
 	 */
 	public static void setFileContents(File file, byte[] contents, boolean createDirectory) throws IOException {
 		if (createDirectory) {
 			File directory = file.getParentFile();
 			if (!directory.exists()) {
 				directory.mkdirs();
 			}
 		}
 
 		FileOutputStream stream = new FileOutputStream(file);
 		stream.write(contents);
 		stream.close();
 	}
 
 	/**
 	 * Writes the specified byte array to a file.
 	 * @param file The <code>File</code> to write.
 	 * @param contents The byte array to write to the file.
 	 * @throws IOException If the file could not be written.
 	 */
 	public static void setFileContents(File file, ByteBuffer contents) throws IOException {
 		setFileContents(file, contents, false);
 	}
 
 	/**
 	 * Writes the specified byte array to a file.
 	 * @param file The <code>File</code> to write.
 	 * @param contents The byte array to write to the file.
 	 * @param createDirectory A value indicating whether the directory
 	 * 		containing the file to be written should be created if it does
 	 * 		not exist.
 	 * @throws IOException If the file could not be written.
 	 */
 	public static void setFileContents(File file, ByteBuffer contents, boolean createDirectory) throws IOException {
 		if (createDirectory) {
 			File directory = file.getParentFile();
 			if (!directory.exists()) {
 				directory.mkdirs();
 			}
 		}
 
 		FileOutputStream stream = new FileOutputStream(file);
 		StreamUtil.writeBytes(contents, stream);
 		stream.close();
 	}
 
 	/**
 	 * Determines if the specified directory is an ancestor of the specified
 	 * file or directory.
 	 * @param file The file or directory to test.
 	 * @param ancestor The directory for which to determine whether
 	 * 		<code>file</code> is an ancestor.
 	 * @return <code>true</code> if <code>ancestor</code> is equal to or an
 	 * 		ancestor of <code>file</code>, <code>false</code> otherwise.
 	 */
 	public static boolean isAncestor(File file, File ancestor) {
 		do {
 			if (file.equals(ancestor)) {
 				return true;
 			}
 			file = file.getParentFile();
 		} while (file != null);
 		return false;
 	}
 
 	/**
 	 * Removes a file or directory and its ancestors up to, but not including
 	 * the specified directory, until a non-empty directory is reached.
 	 * @param file The file or directory at which to start pruning.
 	 * @param root The directory at which to stop pruning.
 	 */
 	public static void prune(File file, File root) {
 		while (!file.equals(root) && file.delete()) {
 			file = file.getParentFile();
 		}
 	}
 
 	public static boolean postOrderTraversal(File root, FileVisitor visitor) throws Exception {
		if (root.isDirectory()) {
			for (File child : root.listFiles()) {
				if (!postOrderTraversal(child, visitor)) {
					return false;
				}
 			}
 		}
 		return visitor.visit(root);
 	}
 
 	public static boolean preOrderTraversal(File root, FileVisitor visitor) throws Exception {
 		if (!visitor.visit(root)) {
 			return false;
 		}
		if (root.isDirectory()) {
			for (File child : root.listFiles()) {
				if (!preOrderTraversal(child, visitor)) {
					return false;
				}
 			}
 		}
 		return true;
 	}
 
 	public static void zip(File zipFile, final File contents) throws IOException {
 
 		OutputStream os = new FileOutputStream(zipFile);
 		final ZipOutputStream zs = new ZipOutputStream(os);
 
 		try {
 			preOrderTraversal(contents, new FileVisitor() {
 
 				@Override
 				public boolean visit(File file) throws IOException {
 					if (file.isFile()) {
 						String name = getRelativePath(file, contents);
 						zs.putNextEntry(new ZipEntry(name));
 
 						FileInputStream fs = new FileInputStream(file);
 						StreamUtil.writeStream(fs, zs);
 						fs.close();
 
 						zs.closeEntry();
 					}
 					return true;
 				}
 
 			});
 		} catch (IOException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new UnexpectedException(e);
 		}
 
 		zs.close();
 
 	}
 
 	public static String getRelativePath(File file, File base) {
 		StringWriter path = new StringWriter();
 
 		while (!isAncestor(file, base)) {
 			path.append("../");
 		}
 
 		String fileName = file.getAbsolutePath();
 		String baseName = base.getAbsolutePath();
 		int prefixLength = baseName.length();
 
 		if (!baseName.endsWith("/")) {
 			prefixLength++;
 		}
 
 		path.append(fileName.substring(prefixLength));
 
 		return path.toString();
 	}
 
 	/** Declared private to prevent this class from being instantiated. */
 	private FileUtil() {}
 
 }
