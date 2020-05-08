 package automaatnehindaja;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 //TODO õppejõu conf
 @MultipartConfig(location = "tmp", fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024, maxRequestSize = 1024 * 1024 * 2)
 @WebServlet("/upload")
 public class FileUploadServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public FileUploadServlet() {
 		super();
 	}
 
 	// References:
 	// http://www.avajava.com/tutorials/lessons/how-do-i-upload-a-file-to-a-servlet.html?page=1
 	// References: http://docs.oracle.com/javaee/6/tutorial/doc/glrbb.html
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		int taskid = Integer.parseInt(request.getParameter("id"));
 		boolean isMultipartContent = ServletFileUpload
 				.isMultipartContent(request);
 		if (!isMultipartContent) {
 			response.sendRedirect("/automaatnehindaja/taskview.html?id=" + taskid +"&result=incorrect");
 			return;
 		}
 		FileItemFactory factory = new DiskFileItemFactory();
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		try {
 			List<FileItem> fields = upload.parseRequest(request);
 			Iterator<FileItem> it = fields.iterator();
 			System.out.println(fields.size());
 			if (fields.size()>1) {
 				response.sendRedirect("/automaatnehindaja/taskview.html?id=" + taskid +"&result=incorrect");
 				return;
 			}
 			if (it.hasNext()) {
 				Connection c = null;
 				PreparedStatement stmt = null;
 				FileItem fileitem = it.next();
 				if (!fileitem.getName().endsWith(".py")){
 					//TODO other language support
 					response.sendRedirect("/automaatnehindaja/taskview.html?id=" + taskid +"&result=incorrect");
 					return;
 				}
 				if (fileitem.getSize()>1024*1024){
 					response.sendRedirect("/automaatnehindaja/taskview.html?id=" + taskid +"&result=toolarge");
 					return;
 				}
 				Class.forName("com.mysql.jdbc.Driver");
 				c = DriverManager.getConnection(
 						"jdbc:mysql://localhost:3306/automaatnehindaja",
 						"root", "t6urott");
 				String statement = "select username from attempt where username = ? and task = ?";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(1, request.getRemoteUser());
 				stmt.setInt(2, taskid);
 				ResultSet rs = stmt.executeQuery();
 				if (rs.next()){
 					stmt.close();
					statement = "UPDATE attempt SET time = ?, source_code= ?, language = ?, result = ? WHERE username = ? and task = ?;";
 					stmt = c.prepareStatement(statement);
 					stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 					stmt.setBinaryStream(2, fileitem.getInputStream());
 					stmt.setString(3, "python"); // TODO support for other languages
					stmt.setString(4, "kontrollimata");
					stmt.setString(5, request.getRemoteUser());
					stmt.setInt(6, taskid);
 					stmt.executeUpdate();
 				}
 				else {
 					stmt.close();
					statement = "INSERT INTO attempt (username, task, time, source_code, language, result) VALUES (?, ?, ?, ?, ?, ?);";
 					stmt = c.prepareStatement(statement);
 					stmt.setString(1, request.getRemoteUser());
 					stmt.setInt(2, taskid);
 					stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
 					stmt.setBinaryStream(4, fileitem.getInputStream());
 					stmt.setString(5, "python"); // TODO support for other languages
					stmt.setString(6, "kontrollimata");
 					stmt.executeUpdate();
 				}				
 				response.sendRedirect("/automaatnehindaja/taskview.html?id=" + taskid + "&result=ok");
 			}
 		} 
 		catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println("SQL Exception");
 			response.sendRedirect("/automaatnehindaja/error.html");
 		}
 		catch (FileUploadException f){
 			f.printStackTrace();
 			System.out.println("FileUploadException");
 			response.sendRedirect("/automaatnehindaja/error.html");
 		}
 		catch (ClassNotFoundException g){
 			g.printStackTrace();
 			System.out.println("ClassNotFoundException");
 			response.sendRedirect("/automaatnehindaja/error.html");
 		}
 		  
 
 	}
 
 }
