 package servlet;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 
 public class UploadServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -8279791785237277465L;
 	private static final String TASKS_DIRECTORY = "/vol/project/2012/362/g1236218/TaskFiles/";
 	
 	
 	@SuppressWarnings("unchecked")
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 		
 		response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         
         //add to db - check task in db, add to subtasks,
         
         //adds to filesystem
 		List<FileItem> items = null;
     	FileItem file = null;
     	String taskDir = "";
     	String filename = "";
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
            		String task = file.getString();
         			if (task == null) {
         				//output need task name
         			} else {
         				//task is task name - add task to db if not yet inputted
             			taskDir = TASKS_DIRECTORY + task + "/";        				
         			}
         		}
         	}
         	else {
         		file = item;
         		filename = file.getName();
         	}
     	}
         InputStream fileIn = file.getInputStream();
         OutputStream fileOut = new FileOutputStream(taskDir + filename);
         IOUtils.copy(fileIn, fileOut);
         fileOut.close();
         out.println("<html>");
         out.println("<body>");
         out.println("uploaded \""+filename + "\" to task " + taskDir + "<br>");                	
         out.println("click <a href=index.jsp>here</a> to return to the homepage");
         out.println("</body>");
         out.println("</html>");
     }
 	
 }
