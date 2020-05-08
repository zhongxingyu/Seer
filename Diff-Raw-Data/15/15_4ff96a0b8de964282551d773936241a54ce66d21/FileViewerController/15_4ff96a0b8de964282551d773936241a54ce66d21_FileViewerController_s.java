 package com.buglabs.app.bugdash2.controller.system;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.buglabs.app.bugdash2.LogManager;
 import com.buglabs.app.bugdash2.controller.ApplicationController;
 import com.buglabs.osgi.sewing.pub.util.RequestParameters;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.SimpleScalar;
 import freemarker.template.TemplateModelRoot;
 
 public class FileViewerController extends ApplicationController {
 	
 	private static final String DEFAULT_MIME_TYPE = "text/plain";
 
 	File file;
 	public String getTemplateName() {
 		return "system_fileBrowser_viewer.fml";
 	}
 
 	public TemplateModelRoot get(RequestParameters params,
 			HttpServletRequest req, HttpServletResponse resp) {
 		
 		String file_path, file_title, file_type, file_last_modified, file_content; 
 		long file_size; 
 		
 		boolean is_image = false; 
 		boolean is_text = false; 
 		
 		file_type = ""; file_last_modified = ""; file_content = ""; file_size = 0; 
 		
		file_path = req.getContextPath(); 
 		file_path = htmlSpaceCheck(file_path);
 		file_title = file_path.substring(file_path.lastIndexOf("/") + 1);
		if (file_title.equals("viewer"))
			file_title = ""; // no file specified 
 		
 		file_path = parseFilePath(req.getContextPath());
 		file_path = htmlSpaceCheck(file_path);
 		if(file_path.compareTo("") != 0) {
 			file = new File(file_path);
 		
 			if(file.exists()) {
 				file_type = getMimeType(file_path);
 				file_size = file.length();
 				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 				java.util.Date date = new java.util.Date(file.lastModified());
 				file_last_modified = dateFormat.format(date); 
 				is_image = file_type.indexOf("image") > -1;
 				if(!is_image){
 					is_text = isText(file); 
 					if (is_text) {
 						FileInputStream fis = null;
 						BufferedInputStream bis = null;
 						DataInputStream dis = null;
 						try {
 					      fis = new FileInputStream(file);
 					      bis = new BufferedInputStream(fis);
 					      dis = new DataInputStream(bis);
 					      file_content = "";
 					      while (dis.available() != 0) {
 					    	  file_content += encodeHTML(dis.readLine()) + "\n";
 					      }
 					      
 						} catch (FileNotFoundException e) {
 							LogManager.logDebug(this.getClass().getName() + ": " + e.getMessage());
 						} catch (IOException e) {
 							LogManager.logDebug(this.getClass().getName() + ": " + e.getMessage());
 						} finally {
 							try {
 								if (fis != null) fis.close(); 
 								if (bis != null) bis.close(); 
 								if (dis != null) dis.close();
 							} catch (IOException e) {
 								LogManager.logDebug(this.getClass().getName() + ": " + e.getMessage());
 							}
 						}
 					} else {
 						file_type = "unknown";
 					}
 				}
 			}
 			else
 			{
 				file_title = "";
 			}
 		}
 		SimpleHash root = new SimpleHash();
 		root.put("file_title", file_title);
 		root.put("file_path", file_path);
 		root.put("file_type", file_type); 
 		root.put("file_last_modified", file_last_modified); 
 		root.put("file_content", file_content);
 		root.put("file_size", file_size/1000); 
 		root.put("is_image", is_image+"");
 		root.put("is_text", is_text+"");
 		return root;
 	}
 
 	private String htmlSpaceCheck(String file_path) {
 		int index;
 		String result = "";
 		while((index = file_path.indexOf("%20")) != -1)
 		{
 			result += file_path.substring(0, index) + " ";
 			file_path = file_path.substring(index + 3);
 		}
 		if(file_path.length() > 0) {
 			result += file_path;
 		}
 		if(result.compareTo("") == 0) {
 			return file_path;
 		} else {
 			return result;
 		}
 	}
 
 	public TemplateModelRoot post(RequestParameters params,
 			HttpServletRequest req, HttpServletResponse resp) {
 		String filePath = params.get("hiddenFilePath");
 		File file = new File(filePath);
 		String msg = ""; 
 		boolean success = file.delete();
 		if(success) {
 			msg = "{category: 'info', message: 'File is deleted'}"; 
 		}
 		else {
 			msg = "{category: 'error', message: 'There was a problem deleting the file'}"; 
 		}
 		TemplateModelRoot root = get(params, req, resp);
 		root.put("js_submit_status", new SimpleScalar(msg)); 
 		return root;
 	}
 	
 	/**
 	 * Parses out the full file path from the url
 	 * @param path - the full url
 	 * @return - the filepath
 	 */
 	private String parseFilePath(String path)
 	{
 		int index = path.indexOf("/viewer");
 		String result = path.substring(index + "/viewer".length());
 		if(result.compareTo("") == 0)
 		{
 			return result;
 		}
 		else
 		{
 			result = result.substring(1);
 		}
 		return result;
 	}
 	/**
 	 * Helper method to get the extension of the image file
 	 * @param name - the name of the file (or the whole path in this case
 	 * @return - the extension
 	 */
 	private String getExtension(String name) {
 		int pos = name.lastIndexOf('.');
 		if (pos > 0 && pos < name.length()) {
 			return name.substring(pos + 1).toLowerCase();
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Determines if a given file is text or binary; if any char > 127 is in the file, assume it's binary 
 	 * @param file
 	 * @return boolean is_text
 	 */
 	private boolean isText(File file) {	
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			int count = 0; 
 			while (fis.available() > 0 && count < 100) {
 				count++;
 				if (fis.read() > 127) return false;  
 			}
 			fis.close(); 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		return true; 
 	}
 	
 	/**
 	 * Return the mimetype of the file.
 	 * @param name - the name of the file
 	 * @return - the mimetype of the file
 	 */
 	public String getMimeType(String name) {
 
 		String extension = getExtension(name);
 
 		if (extension == null || extension.length() == 0) {
 			return DEFAULT_MIME_TYPE;
 		}
 
 		if (extension.equals("jpg") || extension.equals("jpeg")) {
 			return "image/jpeg";
 		}
 		
 		if (extension.equals("png")) {
 			return "image/png";
 		}
 
 		if (extension.equals("gif")) {
 			return "image/gif";
 		}
 
 		if (extension.equals("css")) {
 			return "text/css";
 		}
 
 		if (extension.equals("txt")) {
 			return "text/plain";
 		}
 
 		if (extension.equals("wml")) {
 			return "text/vnd.wap.wml";
 		}
 
 		if (extension.equals("htm") || extension.equals("html")) {
 			return "txt/html";
 		}
 
 		if (extension.equals("wbmp")) {
 			return "image/vnd.wap.wbmp";
 		}
 		return DEFAULT_MIME_TYPE;
 	}
 	
 	public static String encodeHTML(String text) {
 		StringTokenizer tokenizer = new StringTokenizer(text, "&<>\"", true);
 		int tokenCount = tokenizer.countTokens();
 		if (tokenCount == 1)
 		 return text;
 		StringBuffer buffer = new StringBuffer(text.length() + tokenCount * 6);
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			if (token.length() == 1) {
 				switch (token.charAt(0)) {
 					case '&':
 						buffer.append("&amp;"); break;
 					case '<':
 						buffer.append("&lt;"); break;
 					case '>':
 						buffer.append("&gt;"); break;
 					case '"':
 						buffer.append("&quot;"); break;
 					default:
 						buffer.append(token);
 				}
 			} else {
 				buffer.append(token);
 			}
 		}
 		return buffer.toString();
 	}
 
 }
