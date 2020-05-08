 package org.oark.verflixt.core.storage.provider;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import javax.annotation.PostConstruct;
 import javax.naming.directory.SearchResult;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SimpleStorage implements StorageProvider {
 	
 	private static Logger log = LoggerFactory.getLogger(SimpleStorage.class);
	
 	private String storagePath;
 	private File storage;
 	
 	@PostConstruct
 	public void init() throws FileNotFoundException {
		this.storage = new File(getStoragePath());
 		if (! storage.exists())
 			throw new FileNotFoundException();
		log.info("initialized with path " + getStoragePath());
 	}
 	
 	public boolean write(InputStream input, Origin origin) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean read(SimpleStorageEntity entity) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public SearchResult search(SearchTerm term) {
 		// TODO Auto-generated method stub
 		return null;
 	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
	
 }
