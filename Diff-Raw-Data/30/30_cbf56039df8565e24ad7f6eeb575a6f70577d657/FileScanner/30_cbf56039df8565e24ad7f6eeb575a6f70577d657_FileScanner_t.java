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
  * @modified adderollen
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
 			throw new IllegalArgumentException(
 					"Object must implement Serializable");
 		}
 
 		if (!foldersExits(path)) {
 			createFolders(path);
 		}
 
 		File file = new File(path);
 		try {
 			FileOutputStream outFile = new FileOutputStream(file);
			ObjectOutputStream destination = new ObjectOutputStream(outFile);
 
 			try {
				destination.writeObject(object);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
					destination.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create a folder out of a given path.
 	 * 
 	 * @param path
 	 *            The path that will be created
 	 */
 	private static void createFolders(String path) {
		String[] pathString = path.split("/");
 
		for (int i = 0; i < pathString.length - 1; i++) {
			File direction = new File(pathString[i]);
			if (!direction.exists()) {
				direction.mkdir();
 			}
 		}
 
 	}
 
 	/**
 	 * Checks if a folder at a given path exists.
 	 * 
 	 * @param path
 	 *            The path that will be checked.
 	 * @return True if the folder exist and false otherwise.
 	 */
 	private static boolean foldersExits(String path) {
		String[] pathString = path.split("/");
 
		for (int i = 0; i < pathString.length - 1; i++) {
			File direction = new File(pathString[i]);
			if (!direction.exists()) {
 				return false;
 			}
 		}
 		return true;
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
