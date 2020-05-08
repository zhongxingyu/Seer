 package de.hattrickorganizer.tools;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class ZipHelper {
 
 	/* local reference to the JarFile */
 	private static ZipFile zipFile = null;
 
 	public ZipHelper(String filename) throws Exception {
 		try {
 			zipFile = new ZipFile(new File(filename));
 		} catch (Exception e) {
 			throw new Exception("The JarFile cannot be located");
 		}
 	}
 
 	public ZipHelper(File file) throws Exception {
 		try {
 			zipFile = new ZipFile(file);
 		} catch (Exception e) {
 			throw new Exception("The JarFile cannot be located");
 		}
 	}
 
 	public void extractFile(String fileToExtract, String destDir) {
 		File file = new File(destDir);
 		file.mkdirs();
 		try {
 			Enumeration<? extends ZipEntry> e = zipFile.entries();
 
 			while (e.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) e.nextElement();
 				String fileName = destDir + File.separatorChar + entry.getName();
 				if (fileName.toUpperCase(java.util.Locale.ENGLISH).endsWith(
 						fileToExtract.toUpperCase(java.util.Locale.ENGLISH))) {
 					saveEntry(entry, fileName);
 				}
 
 			}
 		} catch (Exception ex) {
			HOLogger.instance().error(ZipHelper.class, ex);
 		}
 	}
 
 	public void close() {
 		try {
 			zipFile.close();
 		} catch (IOException ex) {
			HOLogger.instance().error(ZipHelper.class, ex);
 		}
 	}
 
 	/**
 	 * unzip a file
 	 * 
 	 * @param tmpZipFile
 	 *            TODO Missing Constructuor Parameter Documentation
 	 * @param destDir
 	 *            TODO Missing Constructuor Parameter Documentation
 	 * 
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public void unzip(String destDir) {
 		File file = new File(destDir);
 		file.mkdirs();
 
 		try {
 			Enumeration<? extends ZipEntry> e = zipFile.entries();
 
 			while (e.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) e.nextElement();
 				String fileName = destDir + File.separatorChar + entry.getName();
 				saveEntry(entry, fileName);
 			}
 
 			zipFile.close();
 		} catch (Exception ex) {
			HOLogger.instance().error(ZipHelper.class, ex);
 		}
 	}
 
 	private InputStream getFile(String fileToExtract) {
 		Enumeration<? extends ZipEntry> e = zipFile.entries();
 
 		try {
 			while (e.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) e.nextElement();
 				String fileName = entry.getName();
 				if (fileName.toLowerCase(java.util.Locale.ENGLISH).endsWith(
 						fileToExtract.toLowerCase(java.util.Locale.ENGLISH))) {
 					return zipFile.getInputStream(entry);
 				}
 			}
 		} catch (IOException ex) {
			HOLogger.instance().error(ZipHelper.class, ex);
 		}
 		return null;
 	}
 
 	private void saveEntry(ZipEntry entry, String fileName) throws IOException, FileNotFoundException {
 		File f = new File(getSystemIndependentPath(fileName));
 
 		if (entry.isDirectory() && !f.exists()) {
 			f.mkdir();
 			return;
 		}
 
 		if (!f.getParentFile().exists()) {
 			f.getParentFile().mkdirs();
 		}
 
 		InputStream is = zipFile.getInputStream(entry);
 		byte[] buffer = new byte[2048];
 
 		FileOutputStream fos = new FileOutputStream(f);
 		int len = 0;
 
 		while ((len = is.read(buffer)) != -1) {
 			fos.write(buffer, 0, len);
 		}
 
 		fos.flush();
 		fos.close();
 		is.close();
 	}
 
 	private String getSystemIndependentPath(String str) {
 		return str.replace('\\', '/');
 	}
 }
