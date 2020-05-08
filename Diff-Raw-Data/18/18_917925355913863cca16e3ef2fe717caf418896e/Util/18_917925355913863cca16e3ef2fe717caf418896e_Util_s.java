 package me.sd5.mcbetaterraingenerator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * @author sd5
  * Provides some useful methods.
  */
 public class Util {
 
 	/**
 	 * Copies a file to another one.
 	 * @param source The source file.
 	 * @param destination The destinaton file.
 	 * @throws IOException
 	 */
 	public static void copyFile(String source, String destination) throws IOException {
 		
 		File f1 = new File(source);
 		File f2 = new File(destination);
 		
 		f1.getParentFile().mkdirs();
 		f2.getParentFile().mkdirs();
 		
 		InputStream is = new FileInputStream(f1);
 		OutputStream os = new FileOutputStream(f2);
 		
 		int read = 0;
 		byte[] buffer = new byte[1024];
 		
 		while((read = is.read(buffer)) > 0) {
 			os.write(buffer, 0, read);
 		}
 		
 		is.close();
 		os.close();
 		
 	}
 	
 	/**
 	 * Copies a file from the jar archive to another file. Files in the jar archive are stored in "files"
 	 * @param name The name of the file in the jar archive.
 	 * @param destination The destination file.
 	 * @throws IOException 
 	 */
 	public static void copyFileFromJar(String name, String destination) throws IOException {
 		
		InputStream is = MCBetaTerrainGenerator.class.getResourceAsStream("/files" + name);
 		OutputStream os = new FileOutputStream(destination);
 		
 		int read = 0;
 		byte[] buffer = new byte[1024];
 		
		while((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
 		}
 		
		is.close();
		os.close();
		
 	}
 	
 	/**
 	 * Deletes a file or directory recursively.
 	 * @param path The file or directory to delete.
 	 * @throws FileNotFoundException
 	 */
 	public static void deleteFile(String path) throws FileNotFoundException {
 		
 		File file = new File(path);
 		
 		Util.deleteFile(file);
 		
 	}
 	
 	/**
 	 * Deletes a file or directory recursively.
 	 * @param file The file or directory to delete.
 	 * @throws FileNotFoundException
 	 */
 	public static void deleteFile(File file) throws FileNotFoundException {
 		
 		if(!file.exists()) {
 			throw new FileNotFoundException(file.getAbsolutePath());
 		}
 		
 		if(file.isDirectory()) {
 			for(File f : file.listFiles()) {
 				Util.deleteFile(f);
 			}
 		}
 		
 	}
 
 }
