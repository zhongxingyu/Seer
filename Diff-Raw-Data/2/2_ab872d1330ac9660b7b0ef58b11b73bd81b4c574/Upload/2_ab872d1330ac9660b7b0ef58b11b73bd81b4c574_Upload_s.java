 package edu.ucla.loni.server;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.jdom2.Document;
 
 import edu.ucla.loni.shared.*;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.sql.Timestamp;
 
 @SuppressWarnings("serial")
 public class Upload extends HttpServlet {
 	/**
 	 *  Writes a file
 	 */
 	private void writeFile(String filename, InputStream in, String root, String packageName) throws Exception{
 		Document doc = ServerUtils.readXML(in);  
 		Pipefile pipe = ServerUtils.parse(doc);
 		
 		// Update the packageName
 		if (packageName != null && packageName.length() > 0){
 			pipe.packageName = packageName;
 			doc = ServerUtils.update(doc, pipe, true);
 		}
 		
 		// Write the document
 		String destPath = ServerUtils.newAbsolutePath(root, pipe.packageName, pipe.type, filename);
 		File dest = new File(destPath);
 		
 		// Create parent folders if needed
 		File destDir = dest.getParentFile();
 		if (destDir.exists() == false){
 			boolean success = destDir.mkdirs();
 			if (!success){
 				throw new Exception("Destination folders could not be created");
 			}
 		}
 		
 		// Create a new file
 		if (dest.createNewFile() == false){
 			// TODO rename file to something else
 			throw new Exception("File already exists");
 		}
 		
 		ServerUtils.writeXML(dest, doc);
 		pipe.absolutePath = destPath;
 		
 		int dirId = Database.selectOrInsertDirectory(root);
 		Timestamp fs_lastModified = new Timestamp(dest.lastModified());
 		Database.insertPipefile(dirId, pipe, fs_lastModified);
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		if ( ServletFileUpload.isMultipartContent( req ) ){
 			// Create a factory for disk-based file items
 			FileItemFactory factory = new DiskFileItemFactory();
 			// Create a new file upload handler
 			ServletFileUpload upload = new ServletFileUpload( factory );
 			
 			try	{
 				@SuppressWarnings("unchecked")
 				List<FileItem> items = upload.parseRequest( req );
 				
 				// Go through the items and get the root and package
 				String root = "";
 				String packageName = "";
 				
 				for ( FileItem item : items ){
 					if ( item.isFormField() ){
 						if (item.getFieldName().equals("root")){
 							root = item.getString();
 						} else if (item.getFieldName().equals("packageName")){
 							packageName = item.getString();
 						}
 					}
 				}
 				
 				if (root == ""){
 					res.getWriter().println("error :: Root directory has not been found.");
 					return;
 				}
 				
 				// Go through items and process urls and files
 				for ( FileItem item : items ){
 					// Uploaded File
 					if (item.isFormField() == false){
 						String filename = item.getName();;
 						try {
 							InputStream in = item.getInputStream();
 							
 							writeFile(filename, in, root, packageName);
 							
 							in.close();
 						} 
 						catch (Exception e) {
 							res.getWriter().println("Failed to upload " + filename + ". " + e.getMessage() );
 						}
 					}
 					// URLs					
					if (item.isFormField() && item.getFieldName().equals("urls") && item.getString() != ""){
 						String urlListAsStr = item.getString();
 						String[] urlList = urlListAsStr.split("\n");
 						
 						for (String urlStr : urlList){
 	                        try{
 			                	URL url = new URL(urlStr);
 			                    URLConnection urlc = url.openConnection();
 			                    
 			                    String filename = ServerUtils.extractNameFromURL(urlStr);
 			                    InputStream in = urlc.getInputStream();
 			                    
 			                    writeFile(filename, in, root, packageName);
 			                    
 			                    in.close();
 			                }
 			                catch(Exception e){
 			                	res.getWriter().println("Failed to upload " + urlStr);
 			                    return;
 			                }
 						}
 					}
 				}
 			}
 			catch ( Exception e ){
 				res.getWriter().println("Error occurred while creating file. Error Message : " + e.getMessage());
 			}
 		}
 	}
 }
