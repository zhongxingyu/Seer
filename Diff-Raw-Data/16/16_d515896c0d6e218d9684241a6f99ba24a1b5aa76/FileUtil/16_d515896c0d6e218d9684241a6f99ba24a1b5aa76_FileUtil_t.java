 /*
  * Sep 14, 2003
  */
 package com.thinkparity.codebase;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.SystemUtil.SystemProperty;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.config.Config;
 import com.thinkparity.codebase.config.ConfigFactory;
 
 /**
  * <b>Title:</b>  FileUtil
  * <br><b>Description:</b>  
  * @author raykroeker@gmail.com
  * @version 1.0.0
  */
 public abstract class FileUtil {
 
 	/**
 	 * Size of each block to be read/written
 	 */
 	private static final int BLOCK_SIZE;
 
 	/**
 	 * Config for fileUtil
 	 */
 	private static final Config fileUtilConfig;
 
 	/**
 	 * The read api's buffer size to use when reading the file.
 	 */
 	private static final int READ_BUFFER_SIZE;
 
 	/**
 	 * Synchronization lock for the writeFile api.
 	 * @see FileUtil#writeFile(File, byte[])
 	 */
 	private static final Object writeFileLock;
 
 	static {
 		fileUtilConfig = ConfigFactory.newInstance(FileUtil.class);
 		READ_BUFFER_SIZE = Integer.parseInt(fileUtilConfig.getProperty("read.buffer.size"));
 		BLOCK_SIZE = Integer.parseInt(fileUtilConfig.getProperty("block.size"));
 		writeFileLock = new Object();
 	}
 
 	/**
 	 * Copy a file.
 	 * 
 	 * @param file
 	 *            The file to copy.
 	 * @param target
 	 *            The target to copy the file to.
 	 */
 	public static void copy(final File file, final File target)
 			throws FileNotFoundException, IOException {
 		final BufferedInputStream bis = new BufferedInputStream(
 				new FileInputStream(file));
 		Assert.assertTrue("copy(File,File)", target.createNewFile());
 		final BufferedOutputStream bos = new BufferedOutputStream(
 				new FileOutputStream(target), 256);
 		try { copy(bis, bos); }
 		finally {
 			bis.close();
 
 			bos.flush();
 			bos.close();
 		}
 	}
 
 	/**
 	 * Create a new directory.
 	 * @param directory <code>java.io.File</code>
 	 * @return <code>java.lang.Boolean</code>
 	 * TRUE:  directory was created
 	 * FALSE: directory was not created
 	 */
 	public static synchronized Boolean createDirectory(final File directory) {
 		return directory.mkdir();
 	}
 	
 	/**
 	 * Delete a file.
 	 * @param file <code>java.io.File</code>
 	 * @return <code>java.lang.Boolean</code>
 	 */
 	public static synchronized Boolean delete(final File file) {
 		return file.delete();
 	}
 	
 	/**
 	 * Delete a filesystem tree.
 	 * 
 	 * @param directory
 	 *            The root directory of the tree.
 	 * @throws NullPointerException
 	 *             If directory is null.
 	 * @throws IllegalArgumentException
 	 *             If directory doesn't exist; is not a directory; cannot be
 	 *             read from; or written to.
 	 */
 	public static void deleteTree(final File directory) {
 		if(null == directory) { throw new NullPointerException(); }
 		if(!(directory.exists() && directory.isDirectory() &&
 				directory.canRead() && directory.canWrite())) {
 			throw new IllegalArgumentException();
 		}
 		// grab all files/directories
 		final File[] files = directory.listFiles();
 		for(int i = 0; i < files.length; i++) {
 			if(!files[i].isDirectory()) {
 				Assert.assertTrue(
 						"Could not delete file:  " + files[i].getAbsolutePath(),
 						files[i].delete());
 			}
 			else { FileUtil.deleteTree(files[i]); }
 		}
 		Assert.assertTrue(
 				"Could not delete directory:  " + directory.getAbsolutePath(),
 				directory.delete());
 	}
 	
 	/**
 	 * Obtain the file name without its extension, of the File.  If there is not 
 	 * extension the entire name is returned.
 	 * @param file <code>java.io.File</code>
 	 * @return <code>java.lang.String</code>
 	 */
 	public static synchronized String getFileName(File file) {
 		if(!file.exists())
 			throw new IllegalArgumentException(
 				"File " + file.getAbsolutePath() + " does not exist.");
 		if(!file.isFile())
 			throw new IllegalArgumentException(
 				"File " + file.getAbsolutePath() + " is not a file.");
 		final String fileName = file.getName();
 		if(-1 != fileName.indexOf(Separator.Period.toString()))
 			return fileName.substring(
 				0,
 				fileName.lastIndexOf(Separator.Period.toString()));
 		return fileName;
 	}
 
 	/**
 	 * Obtain the file extension of the File.  If none exists, null is returned.  Is the 
 	 * same as:
 	 * <code>
 	 * FileUtil.getFileNameExtension(file.getName());
 	 * </code>
 	 * @param file <code>java.io.File</code>
 	 * @return <code>java.lang.String</code> or null if no file extension exists
 	 */
 	public static synchronized String getFileNameExtension(File file) {
 		if(!file.exists())
 			throw new IllegalArgumentException(
 				"File " + file.getAbsolutePath() + " does not exist.");
 		if(!file.isFile())
 			throw new IllegalArgumentException(
 				"File " + file.getAbsolutePath() + " is not a file.");
 		return FileUtil.getFileNameExtension(file.getName());
 	}
 	
 	/**
 	 * Obtain the file extension of a file name.  If none exists null is returned.
 	 * @param fileName <code>java.lang.String</code>
 	 * @return <code>java.lang.String</code>
 	 */	
 	public static synchronized String getFileNameExtension(String fileName) {
 		if(-1 != fileName.indexOf(Separator.Period.toString()))
 			return fileName.substring(
 				fileName.lastIndexOf(Separator.Period.toString()));
 		return null;
 	}
 
 	/**
 	 * Obtain the path for the file.  If the file is a file, the path of the parent is 
 	 * returned; if it is a directory, return its absolute path.  If it is neither, return
 	 * null.
 	 * @param file <code>java.io.File</code>
 	 * @return <code>java.lang.String</code>
 	 */
 	public static synchronized String getFilePath(File file) {
 		if(!file.exists())
 			throw new IllegalArgumentException(
 				"File " + file.getAbsolutePath() + " does not exist.");
 		if(file.isFile())
 			return file.getParent();
 		else if(file.isDirectory())
 			return file.getAbsolutePath();
 		else
 			return null;
 	}
 
 	public static synchronized File getTempDirectory(
 			final String applicationName) {
 		final String tempDirectoryPath =
 			FileUtil.getTempDirectoryPath(applicationName);
 		final File tempDirectory = new File(tempDirectoryPath);
 		if(!tempDirectory.exists())
 			tempDirectory.mkdir();
 		return tempDirectory;
 	}
 
 	public static synchronized String getTempDirectoryPath(
 			final String applicationName) {
 		return new StringBuffer()
 			.append(SystemUtil.getSystemProperty(SystemProperty.TempDirectory))
 			.append(File.separatorChar)
 			.append(".")
 			.append(applicationName).toString();
 	}
 
 	/**
 	 * Read a file into a string.
 	 * 
 	 * @param file
 	 *            The file to read.
 	 * @return The contents of the file.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public static String read(final File file) throws FileNotFoundException,
 			IOException {
 		final BufferedReader br =
 			new BufferedReader(new FileReader(file), READ_BUFFER_SIZE);
 
		try {
			final StringBuffer sbuf = new StringBuffer();
			char[] cbuf = new char[READ_BUFFER_SIZE];
			int chars;
			while((chars = br.read(cbuf)) > 0) {
				sbuf.append(cbuf, 0, chars);
			}
			return sbuf.toString();
 		}
		finally { br.close(); }
 	}
 
 	/**
 	 * Read a file's contents into a byte array.
 	 * 
 	 * @param file
 	 *            The file to read.
 	 * @return The file's contents.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public static byte[] readFile(final File file)
 			throws FileNotFoundException, IOException {
 		final FileInputStream fis = new FileInputStream(file);
 		final ByteBuffer byteBuffer = ByteBuffer.allocate(fis.available());
 		final byte[] fileContentBuffer = new byte[1024];
 		int numBytesRead = fis.read(fileContentBuffer);
 		while(-1 != numBytesRead) {
 			byteBuffer.put(fileContentBuffer, 0, numBytesRead);
 			// re-read from the stream
 			numBytesRead = fis.read(fileContentBuffer);
 		}
 		fis.close();
 		return byteBuffer.array();
 	}
 
 	/**
 	 * Write the content of a byte[] to a File
 	 * @param file <code>java.io.File</code>
 	 * @param content <code>byte[]</code>
 	 * @throws IOException
 	 */
 	public static void writeFile(final File file, byte[] content)
 			throws FileNotFoundException, IOException {
 		synchronized(writeFileLock) {
 			FileOutputStream fileOutputStream = null;
 			try {
 				fileOutputStream = new FileOutputStream(file);
 				int amountWritten = 0;
 				final int contentLength = content.length;
 				int amountToWrite;
 				while(amountWritten < contentLength) {
 					amountToWrite = BLOCK_SIZE > contentLength - amountWritten
 						? contentLength - amountWritten
 						: BLOCK_SIZE;
 					fileOutputStream.write(content, amountWritten, amountToWrite);
 					amountWritten += amountToWrite;
 				}
 			}
 			finally {
 				fileOutputStream.flush();
 				fileOutputStream.close();
 			}
 		}
 	}
 
 	public static synchronized File writeTempFile(final String applicationName,
 			final String filename, final byte[] content) throws IOException {
 		final File tempFile =
 			new File(FileUtil.getTempDirectory(applicationName), filename);
 		if(tempFile.exists())
 			tempFile.delete();
 		FileUtil.writeFile(tempFile, content);
 		tempFile.deleteOnExit();
 		return tempFile;
 	}
 
 	/**
 	 * Copy bytes from an input stream to an output stream.
 	 * 
 	 * @param is
 	 *            The input to copy from.
 	 * @param os
 	 *            The output to copy to.
 	 * @throws IOException
 	 */
 	private static void copy(final InputStream is, final OutputStream os)
 			throws IOException {
 		int byteRead = is.read();
 		while(byteRead != -1) {
 			os.write(byteRead);
 			byteRead = is.read();
 		}
 	}
 
 	/**
 	 * Create a new FileUtil [Singleton]
 	 */
 	private FileUtil() { super(); }
 }
