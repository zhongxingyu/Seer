 package com.ecs.soap.proxy.servlets;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.ecs.soap.proxy.config.Configuration;
 
 
 public class UploadServlet extends HttpServlet {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = -8968726091239040944L;
 
 	private static final Logger logger = Logger.getLogger(UploadServlet.class);
 
 	public static final String servletContextPath = "/upload";
 
 	public static final String URI_PARAM = "uri";
 
 	public static final String TARGET_ENDPOINT_URL_PARAM = "targetEnpoindUrl";
 
 	public static final String SCHEMA_FILE_PARAM = "schemaFile";
 
 	public static final String ERROR_SUFFIX = ".error";
 
 	private Configuration config;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String outputJsp = "/WEB-INF/jsp/upload.jsp";
 		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(outputJsp);
 		dispatcher.forward(req, resp);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
 		FileItemFactory factory = new DiskFileItemFactory();
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		String uri = null;
 		String targetUrl = null;
 		byte[] schemaFileContent = null;
 		String schemaFileName = null;
 		Map<String, String> errors = new HashMap<String, String>();
 		try {
 			List<FileItem> fileList = upload.parseRequest(req);
 			for (FileItem item : fileList) {
 				if (item.isFormField()) {
 					if (item.getFieldName().equals(URI_PARAM)) {
 						uri = item.getString();
 						req.setAttribute(URI_PARAM, uri);
 						logger.info("uri: " + uri);
 						if (StringUtils.isEmpty(uri)) {
 							errors.put(URI_PARAM + ERROR_SUFFIX, "the 'Proxy URI' field is mandatory");
 						} else if(uri.charAt(0) != '/') {
 							errors.put(URI_PARAM + ERROR_SUFFIX, "should be a valid URI starting with /");
 						} else {
 							try {
 								new URL("http://somehost:80" + uri);
 								if (uri.endsWith("/")) {
 									uri = uri.substring(0, uri.length() - 1);
 								}
 							} catch (MalformedURLException e) {
 								errors.put(URI_PARAM + ERROR_SUFFIX, "should be a valid URI starting with /");
 							}
 						}
 					}
 					if (item.getFieldName().equals(TARGET_ENDPOINT_URL_PARAM)) {
 						targetUrl = item.getString();
 						req.setAttribute(TARGET_ENDPOINT_URL_PARAM, targetUrl);
 						logger.info("target url: " + targetUrl);
 						if (StringUtils.isEmpty(targetUrl)) {
 							errors.put(TARGET_ENDPOINT_URL_PARAM + ERROR_SUFFIX, "the 'Target endpoint URL' field is mandatory");
 						} else {
 							try {
 								new URL(targetUrl);
 							} catch (MalformedURLException e) {
 								errors.put(TARGET_ENDPOINT_URL_PARAM + ERROR_SUFFIX, "should be a valid URL");
 							}
 						}
 					}
 				} else {
					schemaFileName = item.getName();
 					int size = (int) item.getSize();
 					if (!StringUtils.isEmpty(schemaFileName) && size > 0) {
 						logger.debug("schema file: " + schemaFileName);
 						String itemPath = item.getString();
 						logger.trace("schema file content: \n" + itemPath);
 						schemaFileContent = itemPath.getBytes();
 						/*try {
 							Node schemaNode = parseXMLSchemaNode(schemaFileContent);
 							String schemaContent = nodeToString(schemaNode);
 							schemaFactory.newSchema(new SAXSource(new InputSource(new StringReader(schemaContent))));
 						} catch (Exception e) {
 							logger.warn("Wrong schema file", e);
 							errors.put(SCHEMA_FILE_PARAM + ERROR_SUFFIX, "should be a file containing a valid XSD schema");
 						}*/
 					}
 				}
 			}
 			if (!errors.isEmpty()) {
 				for (Entry<String, String> entry : errors.entrySet()) {
 					req.setAttribute(entry.getKey(), entry.getValue());
 				}
 				doGet(req, resp);
 				return;
 			} else {
 				Properties uriMapping = new Properties();
 				uriMapping.load(new FileInputStream(this.config.getUriMappingFile()));
 				if (uriMapping.containsKey(uri)) {
 					uriMapping.remove(uri);
 				}
 				uriMapping.put(uri, targetUrl);
 				uriMapping.store(new FileOutputStream(this.config.getUriMappingFile()), "added mapping for uri " + uri);
 
 				if (schemaFileContent != null && schemaFileContent.length > 0) {
 					Properties schemaMapping = new Properties();
 					schemaMapping.load(new FileInputStream(this.config.getSchemaMappingFile()));
 					boolean found = false;
 					for(Enumeration<Object> eKey = schemaMapping.keys(); !found && eKey.hasMoreElements();){
 						String currentUri = (String) eKey.nextElement();
 						if(!currentUri.equals(uri)){
 							List<String> currentSchemaFiles = new LinkedList<String>();
 							String[] schemaFileTokens = schemaMapping.getProperty(currentUri).split(",");
 							for(String token : schemaFileTokens){
 								currentSchemaFiles.add(token.trim());
 							}
 							if(currentSchemaFiles.contains(schemaFileName)){
 								found = true;
 							}
 						}
 					}
 					if(found){
 						req.setAttribute(SCHEMA_FILE_PARAM + ERROR_SUFFIX, schemaFileName + " XSD schema file already exists for another mapping");
 						doGet(req, resp);
 						return;
 					} else {
 						File schemaFile = new File(this.config.getXsdDir(), schemaFileName);
 						if (schemaFile.exists()) {
 							schemaFile.delete();
 						}
 						FileOutputStream fos = new FileOutputStream(schemaFile);
 						fos.write(schemaFileContent);
 						fos.close();
 						if (schemaMapping.containsKey(uri)) {
 							List<String> schemaFiles = new LinkedList<String>();
 							String[] schemaFileTokens = schemaMapping.getProperty(uri).split(",");
 							for(String token : schemaFileTokens){
 								schemaFiles.add(token.trim());
 							}
 							if(!schemaFiles.contains(schemaFileName)){
 								schemaFiles.add(schemaFileName);
 								Collections.sort(schemaFiles);
 								schemaMapping.remove(uri);
 								// list as string, removing all blank spaces
 								String schemaFileNames = schemaFiles.toString().replaceAll(" ", "");
 								// removing [ and ]
 								schemaFileNames = schemaFileNames.substring(1, schemaFileNames.length() - 1);
 								schemaMapping.put(uri, schemaFileNames);
 							}
 						} else {
 							schemaMapping.put(uri, schemaFileName);
 						}
 						schemaMapping.store(new FileOutputStream(this.config.getSchemaMappingFile()), "added " + schemaFileName + " for uri " + uri);
 					}
 				}
 				resp.sendRedirect(req.getContextPath());
 			}
 		} catch (FileUploadException e) {
 			resp.sendError(500, e.getMessage());
 			return;
 		}
 
 	}
 
 	@Override
 	public void init() throws ServletException {
 		logger.trace("UploadServlet.init()");
 		this.config = Configuration.getInstance();
 	}
 
 }
