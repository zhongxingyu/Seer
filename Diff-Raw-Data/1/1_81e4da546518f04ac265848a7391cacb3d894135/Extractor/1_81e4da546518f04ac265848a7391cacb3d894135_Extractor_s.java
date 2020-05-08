 package com.javastorm.zip;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 /**
  * This class is intended for unzipping of *.zip file.
  * 
  * @author Hemant Kumar
  * @version 1.0 Dated: 01/03/2013
  */
 public class Extractor 
 {
 
 	/**
 	 * This function is intended for unzipping of *.zip file.
 	 * @param file
 	 * @param extractToZipSlash
 	 * @param extractSubZip 
 	 * @throws Exception
 	 */
 	public void extractZip(File file,boolean extractToZipSlash,boolean extractSubZip) throws Exception {
 		if(file != null && file.toString().toLowerCase().endsWith(".zip")) {
 			String dir = null;
 			if(extractToZipSlash)
 				dir = file.toString().substring(0,file.toString().length()-4);
 			else
 				dir = file.toString().substring(0,file.toString().length()-file.getName().length());
 			(new File(dir)).mkdirs();
 			ZipInputStream zipInputStream =	new ZipInputStream(new FileInputStream(file));
 			ZipFile zipFile = new ZipFile(file);
 			Enumeration<? extends ZipEntry> zipEnumeration = zipFile.entries();
 			while(zipEnumeration.hasMoreElements()) {
 				File tempFile = new File(dir + "\\" + zipEnumeration.nextElement().toString());
 				ZipEntry zipEntry = zipInputStream.getNextEntry();
 				if(zipEntry.isDirectory()) {
 					tempFile.mkdirs();
 				}
 				else {
 					OutputStream out = new FileOutputStream(tempFile);
 					byte[] buf = new byte[1024];
 					int len;
 					while((len = zipInputStream.read(buf)) > 0) {
 						out.write(buf, 0, len);
 					}
 					out.close();
 					if(tempFile.toString().toLowerCase().endsWith(".zip") && extractSubZip) {
 						extractZip(tempFile,extractToZipSlash,extractSubZip);
 					}
 				}
 			}
 		}
 	}
 }
