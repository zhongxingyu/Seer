 package com.tantaman.ferox.webfinger.resource_provider.fs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Map;
 import java.util.Scanner;
 
 import com.tantaman.ferox.webfinger.IResourceProvider;
 import com.tantaman.ferox.webfinger.entry.IWebfingerEntry;
 
 public class SimpleFilesystemResourceProvider implements IResourceProvider {
 	private String identityRoot;
 	
 	public SimpleFilesystemResourceProvider() {
 	}
 	
	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void activate(Map<String, String> configuration) {
 		setConfiguration((Map)configuration);
 	}
 	
 	public void setConfiguration(Map<Object, Object> configuration) {
 		identityRoot = (String)configuration.get("identityRoot");
 	}
 	
 	@Override
 	public IWebfingerEntry getIdentity(String resource) {
 		resource = resource.replace("..", "").replace("/", "").replace("@", "-").replace(":", "-");
 		File f = new File(identityRoot + "/" + resource);
 		
 		String ident = readFully(f);
 		
 		if (ident == null) {
 			return null;
 		} else {
 			return new StringResource(ident);
 		}
 	}
 	
 	private String readFully(File f) {
 		Scanner scan = null;
 		try {
 			scan = new Scanner(f);
 			scan.useDelimiter("\\Z");
 			return scan.next();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			if (scan != null)
 				scan.close();
 		}
 	}
 }
