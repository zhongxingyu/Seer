 package com.mcvs.core;
 
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 public class FileManager implements Singleton<FileManager> {
 	private static FileManager INSTANCE = null;
 	private File fileBrowser;
 	private FileChannel fileWriter;
 	private FileChannel fileReader;
 	private String basePath;
 	
 	/*
 	 * Prevents construction of a FileManager object
 	 * without using the getInstance method 
 	 */
 	private FileManager() { }
 	
 	/*
 	 * Another private constructor that sets the default
 	 * path for the file manager.
 	 */
 	private FileManager(String bPath) {
 		this.setBasePath(bPath);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.mcvs.core.Singleton#getInstance()
 	 * Creates an instance of the FileManager class if and only
 	 * if there isn't one already created.
 	 */
 	public FileManager getInstance() {
 		if(INSTANCE==null) {
 			INSTANCE = new FileManager();
 		}
 		
 		return INSTANCE;
 	}
 	
 	/*
 	 * Creates an instance of the FileManager class if and only
 	 * if there isn't one already created.
 	 */
 	public FileManager getInstance(String bPath) {
 		if(INSTANCE==null) {
 			INSTANCE = new FileManager(bPath);
 		}
 		
 		return INSTANCE;
 	}
 	
 	/*
 	 * Method used to set the base path of the FileManager
 	 */
 	public void setBasePath(String bPath) {
 		basePath = bPath;
 	}
 }
