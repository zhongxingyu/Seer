 package sorcer.util;
 
import sorcer.util.IOUtils;

 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 /**
  * @author Rafał Krupiński
  */
 public class Zip {
 	/**
 	 * Zip a directory
 	 * @param targetFile    target ZIP file
 	 * @param root          source directory
 	 * @param zipRoot       root directory in resulting zip file, may be null
 	 * @param filter        source filter, may be null
 	 * @throws IOException if there is an error while creating the zip file
 	 */
 	public static void zip(File targetFile, File root, String zipRoot, FileFilter filter) throws IOException {
 		ZipOutputStream targetZip = new ZipOutputStream(new FileOutputStream(targetFile));
 		for (File file : root.listFiles(filter)) {
 			ZipEntry entry = new ZipEntry(new File(zipRoot, file.getName()).getPath());
 			targetZip.putNextEntry(entry);
 			FileInputStream iStream = new FileInputStream(file);
 			IOUtils.copy(iStream, targetZip);
 			IOUtils.closeQuietly(iStream);
 		}
 		IOUtils.closeQuietly(targetZip);
 	}
 
 	/**
 	 * Unzip file
 	 * @param zip          the zip file to unzip
 	 * @param targetDir    target directory to unzip to
 	 * @throws IOException
 	 */
 	public static void unzip(File zip, File targetDir) throws IOException {
 		ZipFile zipFile = new ZipFile(zip);
 		Enumeration<? extends ZipEntry> entries = zipFile.entries();
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = entries.nextElement();
 			if (entry.isDirectory()) {
 				continue;
 			}
 			copyInputStream(zipFile, entry, targetDir);
 		}
 
 		IOUtils.closeQuietly(zipFile);
 	}
 
 	private static void copyInputStream(ZipFile zipFile, ZipEntry entry, File targetDir) throws IOException {
 		File target = new File(targetDir, entry.getName());
 		target.getParentFile().mkdirs();
 		InputStream inputStream = zipFile.getInputStream(entry);
 		FileOutputStream outputStream = new FileOutputStream(target);
 		try {
 			IOUtils.copy(inputStream, outputStream);
 		} finally {
 			IOUtils.closeQuietly(inputStream);
 			IOUtils.closeQuietly(outputStream);
 		}
 	}
 }
