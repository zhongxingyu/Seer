 /*
  File: ZipMultipleFiles.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Pasteur Institute
  - Agilent Technologies
  
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
  
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute 
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute 
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute 
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.zip.Adler32;
 import java.util.zip.CheckedOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Zip multiple files into one.
  * 
  * The created zip files can be decompressed by other utilities.
  * 
  * Special Thanks for Mr. Masanori Kouno The original code was written by him.
  * (http://www4.ocn.ne.jp/~mark44/TIPS/Java/ZipOutputStream/zip.htm)
  * 
  * @author kono
  * 
  */
 public class ZipMultipleFiles {
 	private String zipFileName;
 	private String[] files;
 	private int fileCount;
 	private String sessionDirName;
 
 	private Writer[] targets;
 
 	private final String FS = System.getProperty("file.separator");
 
 	public ZipMultipleFiles(String zipFile, String[] fileList) {
 		this.zipFileName = zipFile;
 		this.fileCount = fileList.length;
 		this.files = new String[fileCount];
 		System.arraycopy(fileList, 0, files, 0, fileCount);
 	}
 
 	public ZipMultipleFiles(String zipFile) {
 		this.zipFileName = zipFile;
 	}
 
 	public ZipMultipleFiles() {
 	}
 
 	public ZipMultipleFiles(String zipFile, String[] fileList, File tempDir) {
 		this.zipFileName = zipFile;
 		this.fileCount = fileList.length;
 		this.files = new String[fileCount];
 
 		System.arraycopy(fileList, 0, files, 0, fileCount);
 	}
 
 	public ZipMultipleFiles(String zipFile, String[] fileList, String tempDir) {
 		this.zipFileName = zipFile;
 		this.fileCount = fileList.length;
 		this.files = new String[fileCount];
 		this.sessionDirName = tempDir;
 
 		System.arraycopy(fileList, 0, files, 0, fileCount);
 	}
 
 	//
 	// Now acceptiong multiple streams instead of file names.
 	//
 	public ZipMultipleFiles(String zipFile, Writer[] entryList, String rootDir) {
 		this.zipFileName = zipFile;
 		this.fileCount = entryList.length;
 		this.targets = new Writer[fileCount];
 		this.files = new String[fileCount];
 		this.sessionDirName = rootDir;
 
 		System.arraycopy(entryList, 0, targets, 0, fileCount);
 	}
 
 	protected void clean() {
 
 		for (int i = 0; i < fileCount; i++) {
 			File tempFile = new File(files[i]);
 
 			tempFile.delete();
 		}
 
 		// Delete the temp directory
 	}
 
 
 	// Compress the files into one zipped file.
 	public void compress() throws IOException {
 
 		ByteArrayOutputStream baos = null;
 		ZipOutputStream zos = null;
 		ZipEntry zent = null;
 
 		FileOutputStream fos = null;
 		BufferedOutputStream bos = null;
 
 		baos = new ByteArrayOutputStream();
 		zos = new ZipOutputStream(baos);
 
 		ArrayList al = new ArrayList();
 
 		for (int i = 0; i < fileCount; i++) {
 
 			byte[] buf = getFileBytes(files[i]);
			//zent = new ZipEntry(sessionDirName + FS + files[i]);
			zent = new ZipEntry(sessionDirName + "/" + files[i]);
 
 			al.add(zent);
 			zos.putNextEntry(zent);
 
 			zos.write(buf, 0, buf.length);
 			zos.closeEntry();
 		}
 
 		zos.close();
 
 		baos.close();
 
 		baos = new ByteArrayOutputStream();
 		zos = new ZipOutputStream(baos);
 
 		for (int i = 0; i < fileCount; i++) {
 			byte[] buf = getFileBytes(files[i]);
 
 			zent = (ZipEntry) al.get(i);
 			zos.putNextEntry(zent);
 			zos.write(buf, 0, buf.length);
 			zos.closeEntry();
 		}
 
 		zos.finish();
 
 		byte[] bufResult = baos.toByteArray();
 
 		fos = new FileOutputStream(zipFileName);
 		bos = new BufferedOutputStream(fos);
 		bos.write(bufResult, 0, bufResult.length);
 
 		zos.close();
 		baos.close();
 		bos.close();
 		fos.close();
 
 		clean();
 	}
 
 	
 	/**
 	 * This method will be used when a network is huge. (Slower in small
 	 * networks, but faster in huge ones.)
 	 * 
 	 * @throws IOException
 	 */
 	public void compress2() throws IOException {
 		FileOutputStream f = new FileOutputStream(zipFileName);
 		CheckedOutputStream csum = new CheckedOutputStream(f, new Adler32());
 		ZipOutputStream out = new ZipOutputStream(
 				new BufferedOutputStream(csum));
 
 		// Can't read the above comment, though
 		for (int i = 0; i < fileCount; i++) {
 			System.out.println("Writing file " + files[i]);
 			BufferedReader in = new BufferedReader(new FileReader(files[i]));
 			out.putNextEntry(new ZipEntry(sessionDirName + FS + files[i]));
 			int c;
 			while ((c = in.read()) != -1)
 				out.write(c);
 			in.close();
 		}
 		out.close();
 		
 		clean();
 	}
 	
 	private byte[] getFileBytes(String filename) {
 		File file = null;
 		FileInputStream fis = null;
 		BufferedInputStream bis = null;
 		try {
 			file = new File(filename);
 
 			int len = (int) file.length();
 			fis = new FileInputStream(file);
 			bis = new BufferedInputStream(fis, len);
 			byte buf[] = new byte[len];
 			bis.read(buf, 0, len);
 
 			return buf;
 		} catch (IOException e) {
 
 		} finally {
 
 			try {
 				if (bis != null) {
 					bis.close();
 				}
 			} catch (Exception e) {
 			}
 
 			try {
 				if (fis != null) {
 					fis.close();
 				}
 			} catch (Exception e) {
 			}
 
 		}
 
 		return null;
 	}
 
 	/**
 	 * Reads a file contained within a zip file and returns an InputStream.
 	 * 
 	 * @param zipName
 	 *            The name of the zip file to read.
 	 * @param fileNameRegEx
 	 *            A regular expression that identifies the file to be read. In
 	 *            general this should just be the file name you're looking for.
 	 *            If more than one file matches the regular expression, only the
 	 *            first will be returned. If you're looking for a specific file
 	 *            remeber to build your regular expression correctly. For
 	 *            example, if you're looking for the file 'vizmap.props', make
 	 *            your regular expression '.*vizmap.props' to accomodate any
 	 *            clutter from the zip file.
 	 * @return An InputStream of the zip entry identified by the regular
 	 *         expression or null if nothing matches.
 	 */
 	public static InputStream readFile(String zipName, String fileNameRegEx)
 			throws IOException {
 
 		ZipFile sessionZipFile = new ZipFile(zipName);
 		Enumeration zipEntries = sessionZipFile.entries();
 		while (zipEntries.hasMoreElements()) {
 			ZipEntry zent = (ZipEntry) zipEntries.nextElement();
 			if (zent.getName().matches(fileNameRegEx)) {
 				return sessionZipFile.getInputStream(zent);
 			}
 		}
 
 		return null;
 	}
 
 }
