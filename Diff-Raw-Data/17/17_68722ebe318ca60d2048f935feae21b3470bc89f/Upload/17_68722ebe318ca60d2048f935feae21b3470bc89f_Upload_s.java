 package edu.ucla.loni.server;
 
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
 
 import java.io.File;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
import org.apache.commons.fileupload.FileItem;
 
 @SuppressWarnings("serial")
 public class Upload extends HttpServlet//extends UploadAction {
 	/*@Override
 	  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
 	    	String root = request.getParameter("root");
 		String response = "";
 	    	for (FileItem item : sessionFiles) {
 	    		if (false == item.isFormField()) {
 	    			try {
 	    				/// Create a new file based on the remote file name in the client
 	    				// String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
 	    				// File file =new File("/tmp/" + saveName);
 	          
 	    				/// Create a temporary file placed in /tmp (only works in unix)
 	    				// File file = File.createTempFile("upload-", ".bin", new File("/tmp"));
 	          
 	    				/// Create a temporary file placed in the default system temp folder
 	    				String name = root + File.separatorChar + item.getName();
 	    				File file = new File(name);
 	    				item.write(file);
 	          
 	    				/// Send a customized message to the client.
 	    				response += "File saved as " + file.getAbsolutePath();
 
 	    			} catch (Exception e) {
 	    				throw new UploadActionException(e);
 	    			}
 	    		}
 	    	}
 	    
 	    	/// Remove files from session because we have a copy of them
 	    	removeSessionFileItems(request);
 	    
 	    	/// Send your customized message to the client.
 	    	return response;
 	  }*/
 	 /**
 	 * temporary folder where the pipefiles downloaded from client computer
 	 * will be stored at first
 	 */
 	private String temp_dir = "C:\\tmp";//"/tmp";
 	
 	/** 
 	 * JUST A COPY FROM FileServiceImpl
 	 * 
 	 * REASON WHY COPIED :: it was set as private inside FileServiceImpl,
 	 *                      so I decided not to change its visibility...
 	 * 
 	 * Gets the directoryID of the root directory by selecting it from the database,
 	 * inserts the directory into the database if needed
 	 * 
 	 * @param absolutePath absolute path of the root directory  
 	 * @return directoryID of the root directory
 	 */
 	private int getDirectoryId(String absolutePath) throws Exception{
 		int ret = Database.selectDirectory(absolutePath);
 		if(ret == -1){
 			Database.insertDirectory(absolutePath);
 			ret = Database.selectDirectory(absolutePath);
 		}
 		return ret;
 	}
 	
 	//THIS IS JUST A FIRST VERSION OF doPost function
 	//does not yet have URL implementation
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		File uploadedFile;
 		try
 		{   
 			//to get the content type information from JSP Request Header   
 			String contentType = req.getContentType();
 			if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0))   
 			{   
 				DataInputStream in = new DataInputStream(req.getInputStream());   
 				int formDataLength = req.getContentLength();
 				byte dataBytes[] = new byte[req.getContentLength()];   
 				int byteRead = 0;   
 				int totalBytesRead = 0;   
 				
 				while (totalBytesRead < formDataLength)
 				{
 					byteRead = in.read(dataBytes, totalBytesRead,formDataLength);   
 					totalBytesRead += byteRead;   
 				}
 				
 				
 				res.setContentType("application/octet-stream");
 				String file = new String(dataBytes);
 				
 				//apparently each transfer of file gets encapsulated with header and tail_inforamtion
 				//It has been noticed that header ends with following byte pattern: 0x0D 0X0A 0x0D 0X0A
 				//so this pattern is searched and everything before it along with it is removed...
 				//same applies to the tail_information
 				byte end_byte_codes[] = new byte[4];
 				end_byte_codes[0] = 0x0D;
 				end_byte_codes[1] = 0x0A;
 				end_byte_codes[2] = 0x0D;
 				end_byte_codes[3] = 0x0A;
 				String str_end_byte_codes = new String(end_byte_codes);
 				int start_index = file.indexOf(str_end_byte_codes, file.indexOf("Content-Type:")) + 4;
 				
 				//under normal circumstances when app functions on the actual server
 				//it does  not make sense to check whether 'tmp' folder exusts or not
 				//but since this app right now runs not only on the actual server
 				//but also on other windows machines, the existence of tmp folder is checked
 				//and if it does not exist it gets created...
 				File temp_folder = new File(temp_dir);
 				//the following IF_clause is subject for removal for actual release version
 				if( temp_folder.exists() == false )
 				{
 					boolean success = temp_folder.mkdirs();
 					if (!success){
 						throw new Exception("Temporary folder (which did not exist) could not be created");
 					}
 				}
 				
 				//create temp file, make it with PIPE extension so that
 				//it can be analyzed by XML parser... 
 			    	uploadedFile = File.createTempFile("upload_file-", ".pipe", new File(temp_dir));
 				
 				FileOutputStream fileOut = new FileOutputStream(uploadedFile);
 				fileOut.write(dataBytes, start_index, totalBytesRead - start_index - 50);
 				fileOut.flush();   
 				fileOut.close();
 				
 				//analyze XML of this file
 				Pipefile pipe = ServerUtils.parseFile(uploadedFile);
 				
 				// Get old and new absolute path directory
 				String oldAbsolutePath = pipe.absolutePath;
 				String newAbsolutePath = ServerUtils.newAbsolutePath(pipe.absolutePath, pipe.packageName, pipe.type);
 				//update actual name of file from temp_name to real_name stored inside pipe variable
 				newAbsolutePath = ServerUtils.extractDirName(newAbsolutePath) + File.separatorChar + pipe.name + ".pipe";
  				
  				//getRootDir is new function inside Database.java
  				//I addressed problem with it at that file
  				//right now it is just a quick and dirty fix, later will be corrected
 				String rootDir = Database.getRootDir();
 				if( rootDir == "" )
 				{
 					throw new Exception("rootDir can not be acquired");
 				}
 				
 				//so the actual newAbsolutePath just stores the part of path starting inside library
 				//which means to make the absolute path we need to add address to the root folder
 				//do not need separatorChar, since newAbsolutePath starts with it...
 				//i.e. newAbsolutePath = "\package_dir\type\file_name.pipe"
 				//     root = "\home\...\CraniumLibrary"
 				newAbsolutePath = rootDir + newAbsolutePath;
 				
 				File dest = new File(newAbsolutePath);
 				// If the destination directory does not exist, create it and necessary parent directories
 				File destDir = dest.getParentFile();
 				
 				if (destDir.exists() == false){
 					boolean success = destDir.mkdirs();
 					if (!success){
 						throw new Exception("Destination folders could not be created");
 					}
 				}
 				
 				//move file from temp dir to the actual dir
 				boolean success = uploadedFile.renameTo(dest);
 				//out.println("<br>move the file to " + dest.getAbsolutePath());
 				if(success == false) {
 					throw new Exception("File could not be moved");
 				}
 				
 				//update database
 				//get dir id
 				int dirId = getDirectoryId(rootDir);
 				//get timestamp
 				Timestamp fs_lastModified = new Timestamp(dest.lastModified());
 				
 				//insert entry in database
 				Database.insertPipefile(dirId, ServerUtils.parseFile(dest), fs_lastModified);
 			}
 		}
 		catch(Exception e)   
 		{
 			System.out.println("Exception Due to"+e);   
 			e.printStackTrace();   
 		}
 		// If uploading from a URL
 		  // Get the file from the URL
 		  // Add it to the filesystem
 		  // Update the database
 		// If uploading a folder or files
 		  // For each file
 		    // Add it to the filesystem
 		    // Update the database
 	}
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		doPost(req, resp);
 	}
 }
