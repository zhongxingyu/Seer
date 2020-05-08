 package ru.spbau.martynov.task3;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.BufferedOutputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 /**
  * @author Semen A Martynov, 4 Mar 2013
  * 
  *         Class for extraction of directories and files from archive.
  */
 public class Decompressor {
 	/**
 	 * Constructor with one parameter
 	 * 
 	 * @param archiveName
 	 *            Name of archive from which files are derived
 	 * @throws ZipException
 	 *             if a ZIP format error has occurred
 	 * @throws SecurityException
 	 *             if a security manager exists and its checkRead method doesn't
 	 *             allow read access to the file.
 	 * @throws IOException
 	 *             if an I/O error has occurred
 	 */
 	Decompressor(String archiveName) throws ZipException, SecurityException,
 			IOException {
 		zipFile = new ZipFile(archiveName);
 
 	}
 
 	/**
 	 * Function views archive and finds the archived files.
 	 * 
 	 * @throws ZipException
 	 *             if a ZIP format error has occurred
 	 * @throws IOException
 	 *             if an I/O error has occurred
 	 */
 	public void get() throws ZipException, IOException {
 		Enumeration<? extends ZipEntry> entries = zipFile.entries();
 
 		while (entries.hasMoreElements()) {
 			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
 			String zipEntryPath = zipEntry.getName();
 			write(zipEntryPath, zipFile.getInputStream(zipEntry));
 		}
 	}
 
 	/**
 	 * Function creates structure of directories and pulls out files from
 	 * archive.
 	 * 
 	 * @param filePath
 	 *            The file being in archive
 	 * @param inputStream
 	 *            Stream for reading the file from archive
 	 * @throws IOException
 	 *             if an I/O error has occurred
 	 */
 	private static void write(String filePath, InputStream inputStream)
 			throws IOException {
 		System.out.print(filePath);
 
 		OutputStream outputStream = null;
 		try {
 			int index = filePath.lastIndexOf('/');
 			if (index != -1) {
 				File file = new File(filePath.substring(0, index + 1));
 				// creates structure of directories
 				file.mkdirs();
 			}
 
 			outputStream = new BufferedOutputStream(new FileOutputStream(
 					filePath));
 			byte[] buffer = new byte[8000];
 			int len;
 			// pulls out files from archive
 			while ((len = inputStream.read(buffer)) != -1)
 				outputStream.write(buffer, 0, len);
 
 		} catch (SecurityException | FileNotFoundException e) {
 			System.out.println(" securityException problem - skipped");
 			e.printStackTrace(System.err);
 		} finally {
			try {
				if (outputStream != null) {
 					outputStream.close();
 				}
			} finally {
				if (inputStream != null) {
 					inputStream.close();
 				}
 			}
 		}
 		System.out.println(" - decopressed");
 	}
 
 	/**
 	 * Closes ZipFile and releases resources
 	 * 
 	 * @throws IOException
 	 *             if an I/O error has occurred
 	 */
 	public void close() throws IOException {
 		if (zipFile != null) {
 			zipFile.close();
 		}
 	}
 
 	/**
 	 * Structure with ZIP archive
 	 */
 	private ZipFile zipFile;
 }
