 package com.emergentideas.webhandle.sources;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.emergentideas.logging.Logger;
 import com.emergentideas.logging.SystemOutLogger;
 import com.emergentideas.webhandle.InvocationContext;
 import com.emergentideas.webhandle.ValueSource;
 
 public class HttpBodyValueSource implements ValueSource<Object> {
 
 	protected HttpServletRequest request;
 	
 	// a map where the field name is the key and the value is either a String[] if its as simple form
 	// field or a FileItem if a file upload
 	protected Map<String, Object> multipartValues;
 	
 	
 	protected Logger log = SystemOutLogger.get(HttpBodyValueSource.class);
 	
 	public HttpBodyValueSource(HttpServletRequest request) {
 		this.request = request;
 	}
 	
 	public <T extends Object> Object get(String name, Class<T> type,
 			InvocationContext context) {
 		return getParameter(name);
 	}
 
 
 
 	public <T> boolean canGet(String name, Class<T> type,
 			InvocationContext context) {
 		Object values = getParameter(name);
 		
 		if(values != null) {
 			return true;
 		}
		
		values = getParameter(name + "[]");
		
		if(values != null) {
			return true;
		}
		
 		return false;
 	}
 	
 	protected Object getParameter(String name) {
 		if(isMultipartContent()) {
 			createMultipartContentObjects();
 			Object o = multipartValues.get(name); 
 			if(o != null) {
 				return o;
 			}
 		}
 		
 		// Fall through if the multipart resolver does not find the parameter. 
 		// This is useful for when the parameter is a url parameter and thus not parsed
 		// by the multipart resolver but still available from the request.
 		String[] values = request.getParameterValues(name);
		
		if(values == null) {
			values = request.getParameterValues(name + "[]");
		}
 		return values;
 	}
 	
 	protected boolean isMultipartContent() {
 		return ServletFileUpload.isMultipartContent(request);
 	}
 	
 	protected void createMultipartContentObjects() {
 		if(multipartValues == null) {
 			DiskFileItemFactory fif = new DiskFileItemFactory();
 			if(System.getProperty("os.name").indexOf("indows") < 0)	{
 				// since this is not a windows system, we'll set the /tmp directory to be our temp
 				// directory
 				fif.setRepository(new File("/tmp"));
 			}
 			
 			ServletFileUpload upload = new ServletFileUpload(fif);
 			try {
 				List<FileItem> items = upload.parseRequest(request);
 				multipartValues = new HashMap<String, Object>();
 				
 				for(FileItem fi : items) {
 					if(fi.isFormField()) {
 						multipartValues.put(fi.getFieldName(), new String[] { fi.getString() });
 					}
 					else {
 						multipartValues.put(fi.getFieldName(), fi);
 					}
 				}
 			} catch (FileUploadException e) {
 				log.error("Could not parse the multipart http message.", e);
 			}
 			
 		}
 	}
 	
 	
 
 	public boolean isCachable() {
 		return true;
 	}
 
 	
 }
