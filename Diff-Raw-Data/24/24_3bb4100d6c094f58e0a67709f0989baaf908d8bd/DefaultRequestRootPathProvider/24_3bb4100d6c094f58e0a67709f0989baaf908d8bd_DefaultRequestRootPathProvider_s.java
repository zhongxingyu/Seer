 package hudson;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class DefaultRequestRootPathProvider implements RequestRootPathProvider {
 
    
     private static final String REQUEST_ROOT_PATH = "X-Request-Root-Path";
 
	
	@Override
	public String getRootPath(HttpServletRequest req) {
		  String rootPath = req.getHeader(REQUEST_ROOT_PATH);
	        if (rootPath == null) {
	            rootPath = req.getContextPath();
	        }
	        return rootPath; 
	}

 }
