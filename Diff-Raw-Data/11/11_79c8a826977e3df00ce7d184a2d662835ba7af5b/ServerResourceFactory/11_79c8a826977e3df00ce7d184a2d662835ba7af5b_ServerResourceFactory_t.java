 package il.technion.ewolf.server;
 
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import javax.activation.FileTypeMap;
 
 public class ServerResourceFactory implements ServerFileFactory {
 	private final String CWD = System.getProperty("user.dir") + "/";	
 	
 	FileTypeMap map;
 	
 	ServerResourceFactory(FileTypeMap inputMap) {
 		map = inputMap;
 	}
 	
 	@Override
 	public ServerFile newInstance() {
 		return new ServerFile() {
 			
 			URL url = null;
 			
 			@Override
 			public void read(String path) throws FileNotFoundException {
 				if(path.isEmpty()) {
 					path = "home.html";
 				}
 				
				//url = ServerResources.getResource(path);
 				
 				if(url == null) {
 					try {
 						url = new URL("file", "", CWD + path);
 					} catch (MalformedURLException e) {
 						throw new FileNotFoundException();
 					}
 				}
 			}
 
 			@Override
 			public InputStream openStream() throws FileNotFoundException {
 				try {
 					return url.openStream();
 				} catch (IOException e) {
 					throw new FileNotFoundException();
 				}
 			}
 
 			@Override
 			public Date lastModified() throws FileNotFoundException {
 				if(url == null) {
 					throw new FileNotFoundException();
 				}
 				
 				try {
 					return new Date(url.openConnection().getLastModified());
 				} catch (IOException e) {
 					throw new FileNotFoundException();
 				}
 			}
 
 			@Override
 			public String getTag() throws UnsupportedOperationException {
 				throw new UnsupportedOperationException();
 			}
 
 			@Override
 			public String contentType() throws FileNotFoundException {
 				if(url == null) {
 					throw new FileNotFoundException();
 				}
 				
 				String path = url.getPath();
 				return map.getContentType(path);
 			}
 		};
 	}
 
 	
 
 }
