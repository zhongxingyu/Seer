 package org.zkoss.fiddler.executor.resources;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 
 import org.mortbay.resource.FileResource;
 import org.mortbay.resource.Resource;
 
 public class FiddleWEBINFResourceHandler extends FiddleResourceBase {
 
 	public FiddleWEBINFResourceHandler() {
 		super();
 	}
 
 	public String[] list() {
		return new String[] { "web.xml" };
 	}
 
 	public boolean isDirectory() {
 		return true;
 	}
 
 	public Resource addPath(String path) throws IOException, MalformedURLException {
		if ("web.xml".equals(path)) {
 			try {
				return new FileResource(getClass().getClassLoader().getResource("web.xml"));
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 		}
 		return new EmptyResource();
 	}
 }
