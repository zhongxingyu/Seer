 package ch.bazaruto.storage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 
 public class FileStorage implements Storage {
 
 	private File homeDir;
 	
 	public FileStorage() {}
 	
	public FileStorage(String path) {
		homeDir = new File(path);
 	}
 	
 	public String[] list(String path) {
 		return getFile(path).list();
 	}
 
 	public InputStream open(String path) throws FileNotFoundException {
 		return new FileInputStream(getFile(path));
 	}
 
 	public boolean exists(String path) {
 		return getFile(path).exists();
 	}
 
 	public boolean isDirectory(String path) {
 		return getFile(path).isDirectory();
 	}
 	
 	public long lastModified(String path) {
 		return getFile(path).lastModified();
 	}
 
 	public long length(String path) {
 		return getFile(path).length();
 	}
 	
 	public File getFile(String path) {
		return new File(homeDir.getAbsolutePath() + File.separatorChar + path);
 	}
 
 }
