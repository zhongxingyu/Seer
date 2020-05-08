 package org.araqne.storage.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageManager;
 import org.araqne.storage.api.URIResolver;
 import org.araqne.storage.localfile.LocalFileURIResolver;
 
 @Component(name = "araqne-storage-manager")
 @Provides
 public class StorageManagerImpl implements StorageManager {
 	private List<URIResolver> uriResolvers;
 	
 	public StorageManagerImpl() {
 		this.uriResolvers = new ArrayList<URIResolver>();
 	}
 	
 	@Override
 	public FilePath resolveFilePath(String path) {
 		for (URIResolver resolver : uriResolvers) {
 			FilePath filePath = resolver.resolveFilePath(path);
 			if (filePath != null)
 				return filePath;
 		}
 		
 		return null;
 	}
 	
 	@Validate
 	@Override
 	public void start() {
 		addURIResolver(new LocalFileURIResolver());
 	}
 	
 	@Invalidate
 	@Override
 	public void stop() {
 	}
 
 	@Override
 	public synchronized void addURIResolver(URIResolver r) {
 		uriResolvers.add(r);
 	}
 
 }
