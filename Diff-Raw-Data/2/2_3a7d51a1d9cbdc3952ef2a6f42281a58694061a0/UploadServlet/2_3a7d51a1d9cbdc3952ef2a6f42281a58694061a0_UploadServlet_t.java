 package servlet;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.Part;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 
 import db.DbAdaptor;
 
 public class UploadServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -8279791785237277465L;
 	private static final String TASKS_DIRECTORY = "/vol/project/2012/362/g1236218/TaskFiles/";
 	
 	
 	@SuppressWarnings("unchecked")
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 		
 		response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         
         //validate user credentials
         HttpSession session = request.getSession();
         if (session == null) {
         	//TODO: test this
 //        	response.sendRedirect("/index.html");
 //        	return;
         }
         
 //        int accountID = Integer.parseInt((String) session.getAttribute("account_id"));
         Connection connection;
         try {
 			connection = DbAdaptor.connect();
 		} //catch (ClassNotFoundException | SQLException e1) {
         catch (Exception e1){
 			// TODO Auto-generated catch block
         	out.println("db connection failed");
 			e1.printStackTrace();
 			return;
 		}
         
         //Process post parameters
 		List<FileItem> items = null;
     	FileItem files = null;
     	String taskDir = "";
     	int taskID = -1;
 		try {
 			items = (List<FileItem>) new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
 		} catch (FileUploadException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
     	for( FileItem item : items ) {
         	if(item.isFormField()) {
         		String field = item.getFieldName();
         		if (field.equals("task")) {
             		String task = item.getString();
         			if (task == null) {
         				//output need task name
         			} else {
         				//task is task name - add task to db if not yet inputted
             			taskDir = TASKS_DIRECTORY + task + "/";        				
         			}
         		}
         		if( field.equals("taskID") ) {
         			taskID = Integer.parseInt(item.getString());
         		}
         	}
         	//file to be uploaded
         	else {
         		files = item;
         	}
     	}
         //add to db - check task in db, add to subtasks,
     	PreparedStatement checkTask;
 		try {
 			checkTask = connection.prepareStatement("SELECT id FROM tasks WHERE id = ? AND submitter = ?");
 			checkTask.setInt(1, taskID);
 			int accountID = 1;
 			checkTask.setInt(2, accountID);
 	    	if (!checkTask.execute()) {
 	    		out.println("invalid Task");
 	    		response.sendRedirect("/upload.html");
 	    	}
 		} catch (SQLException e) {
 			out.println("SQL problem slecting task from id"); 
 			e.printStackTrace();
 			return;
 		}
     	
         //on upload page retrieve task id, submit task id as a parameter
         //check id and submitter appear in tasks
 		
 		List<String> filenames = new LinkedList<String>();
 		
     	//add to filesystem
 		if (files.getName().substring(files.getName().length()-4).toLowerCase().equals(".zip")) {
 			ZipInputStream zipIn = new ZipInputStream(files.getInputStream());
 			ZipEntry entry = zipIn.getNextEntry();
 			while( entry != null ) {
 				String filename = entry.getName();
 				OutputStream fileOut = new FileOutputStream(taskDir + filename);
 				IOUtils.copy(zipIn, fileOut);
 				zipIn.closeEntry();
 				fileOut.close();
 				filenames.add(filename);
 				entry = zipIn.getNextEntry();
 			}
 			zipIn.close();
 		} else {
 			//not a zip archive
 			String filename = files.getName();
 			filenames.add(filename);        
 	        
 	        //adds to filesystem
 	        InputStream fileIn = files.getInputStream();
 	        OutputStream fileOut = new FileOutputStream(taskDir + filename);
 	        IOUtils.copy(fileIn, fileOut);
 	        fileIn.close();
 	        fileOut.close();
 		}
     	
         //add to subtasks
     	for (int i = 0 ; i < filenames.size() ; i++) {
	        String insertQuery = "INSERT INTO subtasks VALUES (DEFAULT,?,?,?)";
 	        String filename = filenames.get(i);
 	        PreparedStatement stmt;
 	        try {
 				stmt = connection.prepareStatement(insertQuery);
 				stmt.setInt(1, taskID);
 		        stmt.setString(2, filename);
 		        stmt.setBoolean(3,  true);
 		        stmt.execute();
 	        } catch (SQLException e1) {
 				System.err.println("some error with task fields: taskID not valid?");
 				System.err.println("taskID: " + taskID + ", filename: " + filename);
 				return;
 			}
     	}		
 		
         out.println("<html>");
         out.println("<body>");
         out.println("uploaded to task " + taskDir + "<br>");                	
         out.println("click <a href=../index.jsp>here</a> to return to the homepage");
         out.println("</body>");
         out.println("</html>");
     }
 	
 }
