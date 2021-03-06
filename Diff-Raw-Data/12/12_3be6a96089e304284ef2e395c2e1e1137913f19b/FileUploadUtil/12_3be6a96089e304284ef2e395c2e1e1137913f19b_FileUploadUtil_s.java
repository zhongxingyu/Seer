 /*
  * Created on Dec 11, 2004
  * 
  * TODO To change the template for this generated file go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 package com.idega.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.myfaces.webapp.filter.MultipartRequestWrapper;
 
 import com.idega.idegaweb.IWCacheManager;
 import com.idega.io.UploadFile;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.ui.FileInput;
 
 public class FileUploadUtil {
 
 	public FileUploadUtil() {
 		super();
 	}
 	
 	/**Returns all files that has different parameter names. Suports only files with 
 	 * different parameter names (if sending files with same parameter names only one will
 	 * be returned)
 	 * 
 	 * @param iwc
 	 * @throws IOException
 	 * @throws IllegalAccessException
 	 * @throws NoSuchFieldException
 	 * @throws IllegalArgumentException
 	 * @return Map<String,com.idega.io.UploadFile>
 	 */
	public static Map getAllUploadedFiles(IWContext iwc){
 		
 		HttpServletRequest request = iwc.getRequest();
		Map fileList = new HashMap();
 		
 		if (request instanceof HttpServletRequestWrapper) {
 			
 			HttpServletRequest childRequest = request;
 			while( childRequest instanceof HttpServletRequestWrapper){
 				
 				if(childRequest instanceof MultipartRequestWrapper){
 //					myfaces This ONLY supports one file now
 					//Cast the request to a MultipartRequestWrapper
 					MultipartRequestWrapper multiRequestWrapper = (MultipartRequestWrapper) childRequest;
 					//get the uploaded file
 					StringBuffer pathToFile = new StringBuffer();
 					pathToFile.append(iwc.getIWMainApplication().getApplicationRealPath());
 					pathToFile.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY);
 					pathToFile.append(FileUtil.getFileSeparator());
 					pathToFile.append("upload");
 					pathToFile.append(FileUtil.getFileSeparator());
 					FileUtil.createFolder(pathToFile.toString());
 					
 					
 					Map files = multiRequestWrapper.getFileItems();
 					
 					
 					Set keysSet = files.keySet();
 					Iterator keys = keysSet.iterator();
 					while(keys.hasNext()){
 						FileItem file = (FileItem)files.get(keys.next());
 						String fileName = file.getName();
 						int lastBloodySlash = fileName.lastIndexOf("\\");
 						if(lastBloodySlash>-1){
 							fileName = fileName.substring(lastBloodySlash+1);
 						}
 						if(StringUtil.isEmpty(fileName)){
 							continue;
 						}
 						
 						String mimeType = file.getContentType();
 						
 						StringBuffer webPath = new StringBuffer();
 						webPath.append('/');
 						webPath.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY);
 						webPath.append('/');
 						webPath.append("upload");
 						webPath.append('/');
 						webPath.append(fileName);
 						// Opera mimetype fix ( aron@idega.is )
 						if (mimeType != null) {
 							StringTokenizer tokenizer = new StringTokenizer(mimeType, " ;:");
 							if (tokenizer.hasMoreTokens()) {
 								mimeType = tokenizer.nextToken();
 							}
 						}
 						//write the file from wherever it is to our favorite upload
 						// folder
 						File tempFile = null;
 						try{
 							tempFile = FileUtil.streamToFile(file.getInputStream(), pathToFile.toString(), fileName);
 						}catch(IOException e){
 							Logger.getLogger("FileUploadUtil").log(Level.WARNING, "failed getting file Input Stream of "
 									+ pathToFile.toString(), e);
 							childRequest = (HttpServletRequest) ((HttpServletRequestWrapper)childRequest).getRequest();
 							continue;
 						}
 						
 						String filePath = pathToFile.toString()+fileName;
 						UploadFile uploadFile = new UploadFile(fileName, filePath,
 								iwc.getIWMainApplication().getTranslatedURIWithContext(webPath.toString()), mimeType,
 								tempFile.length());
 						fileList.put(file.getFieldName(), uploadFile);
 					}
 //					ServletFileUpload fileUpload = new ServletFileUpload();
 //					boolean is = ServletFileUpload.isMultipartContent(request);
 //					String charset = request.getCharacterEncoding();
 //			        fileUpload.setHeaderEncoding(charset);
 //			        fileUpload.setFileItemFactory(
 //		                    new DiskFileItemFactory(100000000,
 //		                            new File(System.getProperty("java.io.tmpdir"))));
 //					List requestParameters = null;
 //					try{
 //						fileUpload.setSizeMax(100000000);
 //						fileUpload.setFileSizeMax(100000000);
 //						requestParameters = fileUpload.parseRequest(childRequest);
 //					}catch(FileUploadException e){
 //						Logger.getLogger("a").log(Level.WARNING, "msg", e);
 //					}
 					
 				}
 				
 				childRequest = (HttpServletRequest) ((HttpServletRequestWrapper)childRequest).getRequest();
 			}	
 		}
 		
 
 		return fileList;
 	}
 	
 public static void handleMyFacesMultiPartRequest(IWContext iwc) throws IOException, IllegalArgumentException,IllegalAccessException, NoSuchFieldException {
 		
 		HttpServletRequest request = iwc.getRequest();
 		
 		if (request instanceof HttpServletRequestWrapper) {
 			
 			HttpServletRequest childRequest = request;
 			while( childRequest instanceof HttpServletRequestWrapper){
 				
 				if(childRequest instanceof MultipartRequestWrapper){
 //					myfaces This ONLY supports one file now
 					//Cast the request to a MultipartRequestWrapper
 					MultipartRequestWrapper multiRequestWrapper = (MultipartRequestWrapper) childRequest;
 					//get the uploaded file
 					StringBuffer pathToFile = new StringBuffer();
 					pathToFile.append(iwc.getIWMainApplication().getApplicationRealPath());
 					pathToFile.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY);
 					pathToFile.append(FileUtil.getFileSeparator());
 					pathToFile.append("upload");
 					pathToFile.append(FileUtil.getFileSeparator());
 					FileUtil.createFolder(pathToFile.toString());
 
 					FileItem file = multiRequestWrapper.getFileItem(FileInput.FILE_INPUT_DEFAULT_PARAMETER_NAME);
 					String fileName = file.getName();
 					int lastBloodySlash = fileName.lastIndexOf("\\");
 					if(lastBloodySlash>-1){
 						fileName = fileName.substring(lastBloodySlash+1);
 					}
 					
 					String mimeType = file.getContentType();
 					
 					StringBuffer webPath = new StringBuffer();
 					webPath.append('/');
 					webPath.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY);
 					webPath.append('/');
 					webPath.append("upload");
 					webPath.append('/');
 					webPath.append(fileName);
 					// Opera mimetype fix ( aron@idega.is )
 					if (mimeType != null) {
 						StringTokenizer tokenizer = new StringTokenizer(mimeType, " ;:");
 						if (tokenizer.hasMoreTokens()) {
 							mimeType = tokenizer.nextToken();
 						}
 					}
 					//write the file from wherever it is to our favorite upload
 					// folder
 					File tempFile = FileUtil.streamToFile(file.getInputStream(), pathToFile.toString(), fileName);
 					
 					String filePath = pathToFile.toString()+fileName;
 					UploadFile uploadFile = new UploadFile(fileName, filePath,
 							iwc.getIWMainApplication().getTranslatedURIWithContext(webPath.toString()), mimeType,
 							tempFile.length());
 					iwc.setUploadedFile(uploadFile);
 					//we can only handle one here
 					break;
 				}
 					
 				childRequest = (HttpServletRequest) ((HttpServletRequestWrapper)childRequest).getRequest();
 			}	
 		}
 	}
 }
