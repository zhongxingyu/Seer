 package org.oark.verflixt.core.storage.provider;
 
 import java.io.File;
 import java.io.InputStream;
 
 public class SimpleStorage implements StorageProvider {
 
 	private String storagePath;
 	private File storage;
 	
	public SimpleStorage() {
 		this.storage = new File(storagePath);
 	}
 	
 	public boolean write(InputStream input, String target) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean delete(String target) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean read(String source) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public DirectoryListing ls(String root) {
 		File absoluteRoot = new File(storagePath, root);
 		
 		return null;
 	}
 
 	public String getStoragePath() {
 		return storagePath;
 	}
 
 	public void setStoragePath(String storagePath) {
 		this.storagePath = storagePath;
 	}
 }
