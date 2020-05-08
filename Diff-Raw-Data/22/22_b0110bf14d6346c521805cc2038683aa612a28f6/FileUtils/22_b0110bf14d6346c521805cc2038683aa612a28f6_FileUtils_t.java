 package com.koujalgi.shell.utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 public class FileUtils {
 	public static boolean empty(File dir) {
 		if (dir.exists()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = delete(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public static String readFileFromClassPath(Class<?> clazz, String fileName)
 			throws IOException {
 		String ret = "";
 		String str = "";
 		BufferedReader br = new BufferedReader(new InputStreamReader(clazz
 				.getClassLoader().getResourceAsStream(fileName)));
 		while ((str = br.readLine()) != null) {
 			ret += str + "\n";
 		}
 		br.close();
 		return ret;
 	}
 
 	public static void writeToFile(String filename, String contents)
 			throws Exception {
 		PrintWriter out = new PrintWriter(new FileWriter(filename), true);
 		out.print(contents);
 		out.flush();
 		out.close();
 	}
 
 	public static boolean truncateFile(String filename) {
 		try {
 			PrintWriter out = new PrintWriter(new FileWriter(filename), true);
 			out.print("");
 			out.flush();
 			out.close();
 			return true;
 		} catch (Exception e) {
 		}
 		return false;
 	}
 
 	public static boolean delete(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = delete(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	public static String[] getFiles(String path) {
 		File dir = new File(path);
 		if (dir.exists() && dir.isDirectory())
 			return dir.list();
 		return null;
 	}
 
 	public static void copyFile(String fromPath, String toPath)
 			throws Exception {
 		File src = new File(fromPath);
 		File target = new File(toPath);
 		if (src.exists()) {
 			BufferedInputStream bis = new BufferedInputStream(
 					new FileInputStream(src), 4096);
 			BufferedOutputStream bos = new BufferedOutputStream(
 					new FileOutputStream(target), 4096);
 			int theChar;
 			while ((theChar = bis.read()) != -1) {
 				bos.write(theChar);
 			}
 			bos.close();
 			bis.close();
 
 		}
 	}
 
 	public static String getFileContents(String fileName) throws Exception {
 		BufferedReader in = null;
 		String ret = "";
 		String str = "";
 		try {
 			in = new BufferedReader(new FileReader(fileName));
 			while ((str = in.readLine()) != null) {
 				ret += str + "\n";
 			}
 			in.close();
 		} catch (FileNotFoundException e) {
 			throw e;
 		}
 		return ret;
 	}
 
 	public static String getContentsFromURL(String path) throws Exception {
 		URL u;
 		InputStream is = null;
 		String s;
 		String ret = "";
 		try {
 			u = new URL(path);
 			is = u.openStream(); // throws an IOException
			BufferedReader d = new BufferedReader(new InputStreamReader(is));
			while ((s = d.readLine()) != null) {
 				ret += s + "\n";
 			}
 			System.out.println(s);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw e;
 		} finally {
 			try {
 				is.close();
 			} catch (IOException ioe) {
 			}
 		}
 		return ret;
 	}
 
 	public static Document loadXML(String path) throws Exception {
 		return loadXMLFromString(getFileContents(path));
 	}
 
 	public static Document loadXMLFromString(String contents) throws Exception {
 		InputStream is = new ByteArrayInputStream(contents.getBytes());
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		DocumentBuilder builder = null;
 		try {
 			builder = factory.newDocumentBuilder();
 		} catch (javax.xml.parsers.ParserConfigurationException ex) {
 		}
 		Document doc = builder.parse(is);
 		is.close();
 		return doc;
 	}
 
 	public static void copyDirectory(String spath, String tpath)
 			throws IOException {
 		copyDirectory(new File(spath), new File(tpath));
 	}
 
 	public static void copyDirectory(File sourceLocation, File targetLocation)
 			throws IOException {
 		if (!sourceLocation.exists())
 			return;
 		if (sourceLocation.getName().endsWith(".svn"))
 			return;
 		if (sourceLocation.isDirectory()) {
 			if (!targetLocation.exists()) {
 				targetLocation.mkdir();
 			}
 
 			String[] children = sourceLocation.list();
 			for (int i = 0; i < children.length; i++) {
 				copyDirectory(new File(sourceLocation, children[i]), new File(
 						targetLocation, children[i]));
 			}
 		} else {
 			InputStream in = new FileInputStream(sourceLocation);
 			OutputStream out = new FileOutputStream(targetLocation);
 
 			// Copy the bits from instream to outstream
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		}
 	}
 
 	public static void copyDirectoryStructure(String spath, String tpath)
 			throws IOException {
 		copyDirectoryStructure(new File(spath), new File(tpath));
 	}
 
 	public static void copyDirectoryStructure(File sourceLocation,
 			File targetLocation) throws IOException {
 		if (!sourceLocation.exists())
 			return;
 		if (sourceLocation.getName().endsWith(".svn"))
 			return;
 		if (sourceLocation.isDirectory()) {
 			if (!targetLocation.exists()) {
 				targetLocation.mkdir();
 			}
 
 			String[] children = sourceLocation.list();
 			for (int i = 0; i < children.length; i++) {
 				copyDirectoryStructure(new File(sourceLocation, children[i]),
 						new File(targetLocation, children[i]));
 			}
 		}
 	}
 
 }
