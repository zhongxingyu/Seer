 /**
  * 
  */
 package edu.illinois.ncsa.versus.store;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 import java.util.UUID;
 
 import edu.illinois.ncsa.versus.restlet.PropertiesUtil;
 
 /**
  * Disk based file storage.
  * 
  * @author Luigi Marini <lmarini@ncsa.illinois.edu>
  * 
  */
 public class DiskFileProcessor implements FileProcessor {
 
 	private String directory;
 
 	public DiskFileProcessor() {
 		Properties properties;
 		try {
 			properties = PropertiesUtil.load();
 			String location = properties.getProperty("file.directory");
 			if (location != null) {
 				directory = location;
 			} else {
 				directory = System.getProperty("java.io.tmpdir");
 			}
 			//System.out.println("Storing file in " + directory);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String addFile(InputStream inputStream, String filename) {
 		try {
 			File file;
 			File ff = new File(directory);
 			
 			if (filename == null) {
 				file = File.createTempFile("versus", ".tmp", ff);
 			} else {
 				int idx = filename.lastIndexOf(".");
 				if (idx != -1) {
 					file = File.createTempFile(filename.substring(0, idx), filename.substring(idx), ff);
 				} else {
 					file = File.createTempFile(filename, ".tmp", ff);
 				}
 			}
 			OutputStream out = new FileOutputStream(file);
 			byte buf[] = new byte[1024];
 			int len;
 			while ((len = inputStream.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			inputStream.close();
 			return file.getName();
 		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	@Override
 	public InputStream getFile(String id) {
 		File file = new File(directory, id);
 		System.out.println("Reading file " + file);
 		try {
 			return new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
