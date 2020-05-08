 package com.m0pt0pmatt.bettereconomy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import org.bukkit.Bukkit;
 
 /**
  * Handles all file operations for the economy
  * @author Matthew
  * @param <E>
  *
  */
 public class FileManager{
 	
 	/**
 	 * Name of the folder location for HomeWorldPlugin
 	 */
 	private String folder;
 	
 	/**
 	 * File for the folder location for HomeWorldPlugin
 	 */
 	private File folderFile;
 	
 	/**
 	 * Default constructor
 	 */
 	public FileManager(){
 		
 		//get the folder, creating if needed
		folderFile = Bukkit.getPluginManager().getPlugin("HomeWorldPlugin").getDataFolder();
 		if (!folderFile.exists()){
 			folderFile.mkdir();
 		}
 		
 		//get the folder name
 		folder = folderFile.getAbsolutePath() + "/";	
 	}
 	
 	/**
 	 * Load accounts from file
 	 * @param <E>
 	 * @return List of Accounts
 	 * @throws IOException When reading from files
 	 */
 
 	@SuppressWarnings("unchecked")
 	public <T> AbstractList<?> load(String filename, boolean whichList) throws IOException{	
 		
 		//create an empty list
 		AbstractList<T> newList;
 		if (whichList){
 			newList = new ArrayList<T>();
 		}
 		else{
 			newList = new LinkedList<T>();
 		}
 		
 		
 		//return an empty list if the file does not exist
 		File file = new File(folder + filename);
 		if (!file.exists()){
 			return newList;
 		}
 		
 		//get the input stream
 		FileInputStream fin = new FileInputStream(file);
 		ObjectInputStream in = new ObjectInputStream(fin);
 		
 		//read in the length of the list
 		int length = in.readInt();
 		
 		//read in accounts
 		for (int i = 0; i < length; i++){
 			try {
 				newList.add((T) in.readObject());
 			} catch (ClassNotFoundException e) {
				System.err.println("[HomeWorldPlugin-Economy]: Error reading accounts file. Class not found.");
 				e.printStackTrace();
 			}
 		}
 		
 		//close the stream
 		in.close();
 		fin.close();
 
 		
 		//print to the console
		System.out.println("[HomeWorldPlugin] Loaded " + filename);
 		
 		return newList;
 	}
 	
 	/**
 	 * Saves accounts to file
 	 * @param <E>
 	 * @param accounts list of accounts to be saved
 	 * @throws IOException when writing to and creating files
 	 */
 	public <E> void save(AbstractList<E> list, String filename) throws IOException{
 		
 		//don't do anything if there are no accounts to save
 		if (list == null){
 			return;
 		}
 		
 		//get the file for storing data
 		File file = new File(folder + filename);
 		if (file.exists()){
 			file.delete();
 		}
 		
 		//get the output stream
 		FileOutputStream fout = new FileOutputStream(file);
 		ObjectOutputStream out = new ObjectOutputStream(fout);
 
 		//write the length of the list to file
 		out.writeInt(list.size());
 		
 		//write the accounts to file
 		for (E a: list){
 			out.writeObject(a);
 		}
 		
 		//close the stream
 		out.close();
 		fout.close();
 		
 		//print to the console
		System.out.println("[HomeWorldPlugin] Saved " + filename);
 		
 		return;
 	}
 
 }
