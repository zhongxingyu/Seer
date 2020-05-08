 package com.github.joakimpersson.tda367.model.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class FileScanner {
 
 	private FileScanner() {
 	}
 
 	/**
 	 * Write the specified object to the users file system using the specified
 	 * path
 	 * 
 	 * @param path
 	 *            The path to the location of the file
 	 * @param object
 	 *            The object to be written
 	 * @throws IllegalArgumentException
 	 *             If Object doesn't implement Serializable
 	 */
 	public static void writeOjbect(String path, Object object)
 			throws IllegalArgumentException {
 
 		if (!(object instanceof Serializable)) {
			System.out.println("bajs p dig!");
 		}
 
 		File file = new File(path);
 		try {
 			FileOutputStream outFile = new FileOutputStream(file);
 			ObjectOutputStream dest = new ObjectOutputStream(outFile);
 
 			try {
 				dest.writeObject(object);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					dest.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Read an object from the specified path
 	 * 
 	 * @param path
 	 *            The path to the file
 	 * @return An object that implements Serializable
 	 */
 	public static Object readObject(String path) {
 		Object object = null;
 		File file = new File(path);
 		if (file.exists()) {
 			try {
 				FileInputStream inFile = new FileInputStream(file);
 				ObjectInputStream source = new ObjectInputStream(inFile);
 
 				try {
 					object = source.readObject();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					try {
 						source.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return object;
 	}
 
 	/**
 	 * Get all the files located in a current folder as File objects
 	 * 
 	 * @param path
 	 *            The path to the folder
 	 * @return A list of files in the folder
 	 */
 	public static File[] readFilesFromFolder(String path) {
 		File file = new File(path);
 		return file.listFiles();
 	}
 
 	/**
 	 * Read all the text from a file
 	 * 
 	 * @param file
 	 *            The file that you want text from
 	 * @return A list of all the lines in the file
 	 */
 	public static List<String> readTextFromFile(File file) {
 		List<String> lines = new ArrayList<String>();
 
 		try {
 			Scanner scanner = new Scanner(file);
 
 			while (scanner.hasNextLine()) {
 				String line = scanner.nextLine();
 				if (!line.equals("")) {
 					lines.add(line);
 				}
 			}
 
 			scanner.close();
 
 		} catch (FileNotFoundException e) {
 			System.out.println("No File, sir :/");
 			e.printStackTrace();
 		}
 
 		return lines;
 	}
 }
