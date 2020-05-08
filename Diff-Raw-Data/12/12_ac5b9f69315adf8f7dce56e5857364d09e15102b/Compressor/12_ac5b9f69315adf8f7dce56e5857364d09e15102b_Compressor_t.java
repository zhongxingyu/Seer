 package ru.spbau.martynov.task3;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.zip.Deflater;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipOutputStream;
 
import ru.spbau.martynov.task1.IllegalMessageFormatException;

 /**
  * @author Semen A Martynov, 4 Mar 2013
  * 
  *         Class for the recursive bypass of directories, and files archiving in
  *         ZIP archive.
  */
 public class Compressor {
 
 	/**
 	 * Constructor with one parameter
 	 * 
 	 * @param archiveName
 	 *            sets a name of archive in which files will be added.
 	 * @throws FileNotFoundException
 	 *             if the file exists but is a directory rather than a regular
 	 *             file, does not exist but cannot be created, or cannot be
 	 *             opened for any other reason
 	 * @throws SecurityException
 	 *             if a security manager exists and its checkWrite method denies
 	 *             write access to the file.
 	 */
 	public Compressor(String archiveName) throws FileNotFoundException,
 			SecurityException {
 		zipOutputStream = new ZipOutputStream(new FileOutputStream(archiveName));
 		zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
 	}
 
 	/**
 	 * Function accepts a line with file name, and launches the recursive
 	 * bypass.
 	 * 
 	 * @param fileName
 	 *            Name of the root directory.
 	 * @throws ZipException
 	 *             - if a ZIP format error has occurred
 	 * @throws IOException
 	 *             - if an I/O error occurs.
 	 */
 	public void add(String fileName) throws ZipException, IOException {
 		File file = new File(fileName);
 		add(file, file);
 	}
 
 	/**
 	 * Closes ZipOutputStream and releases resources
 	 * 
 	 * @throws IOException
 	 *             - if an I/O error occurs.
 	 */
 	public void close() throws IOException {
 		if (zipOutputStream != null) {
 			zipOutputStream.close();
 		}
 	}
 
 	/**
 	 * Are recursive tree traversal of directories. If the file met, function
 	 * addToArchive is caused.
 	 * 
 	 * @param file
 	 *            The file processed at present.
 	 * @param root
 	 *            The pointer on catalog tree peak.
 	 * @throws ZipException
 	 *             - if a ZIP format error has occurred
 	 * @throws IOException
 	 *             - if an I/O error occurs.
 	 */
 	private void add(File file, File root) throws ZipException, IOException {
 		try {
 			if (!file.exists()) {
 				System.out.println(file.getAbsolutePath()
 						+ " doesn't exist - skipped");
 				return;
 			}
 
 			if (!file.canRead()) {
 				System.out.println(file.getAbsolutePath()
 						+ " in not accessible - skipped");
 				return;
 			}
 
 			if (file.isDirectory()) {
 				File[] subFiles = file.listFiles();
 				if (subFiles.length == 0) {
 					System.out.println(file.getAbsolutePath()
 							+ " is an empty directory - skipped");
 					return;
 				}
 				for (File subFile : subFiles) {
 					add(subFile, root);
 				}
 			} else {
 				addToArchive(file, root);
 			}
 		} catch (SecurityException e) {
 			System.out.println(file.getAbsolutePath()
 					+ " in not accessible - skipped");
 			return;
 		}
 	}
 
 	/**
 	 * Adds the received file in archive.
 	 * 
 	 * @param file
 	 *            The file processed at present.
 	 * @param root
 	 *            The pointer on catalog tree peak.
 	 * @throws ZipException
 	 *             - if a ZIP format error has occurred
 	 * @throws IOException
 	 *             - if an I/O error occurs.
 	 */
 	private void addToArchive(File file, File root) throws ZipException,
 			IOException {
 		System.out.print(file.getPath());
 
 		FileInputStream fileInputStream = null;
 		try {
 			fileInputStream = new FileInputStream(file);
			ZipEntry zipEntry = null;
			if (file != root) {
				zipEntry = new ZipEntry(file.getPath().substring(
						1 + root.getParent().length()));
			} else {
				zipEntry = new ZipEntry(file.getPath());
			}
 			zipOutputStream.putNextEntry(zipEntry);
 
 			int len;
 			byte[] readBuffer = new byte[8000];
 			while ((len = fileInputStream.read(readBuffer)) != -1) {
 				zipOutputStream.write(readBuffer, 0, len);
 			}
 
 			System.out.println(" - compressed");
 
 		} catch (FileNotFoundException | SecurityException e) {
 			System.out.println(" is not accessible - skipped");
 			return;
 		} finally {
 			if (fileInputStream != null) {
 				fileInputStream.close();
 			}
 		}
 	}
 
 	/**
 	 * The stream used for archiving of files.
 	 */
 	private ZipOutputStream zipOutputStream;
 }
