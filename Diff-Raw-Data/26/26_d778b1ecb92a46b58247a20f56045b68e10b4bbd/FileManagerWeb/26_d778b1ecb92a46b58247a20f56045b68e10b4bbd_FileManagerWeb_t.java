 package com.filemanager.web;
 
 import java.io.IOException;
 import java.util.List;
 
 import com.annotation.model.AnnotationModel;
 import com.filemanager.model.FileManagerModel;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class FileManagerWeb extends HttpServlet{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4419604332233888175L;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)  throws IOException, ServletException {
 		try {
 			HttpSession session = request.getSession(false);
 			String email = (String)session.getAttribute("email");
 			String uid = (String)session.getAttribute("uid");
 			if(email != null) {
 				// do a verification check from db here
 				
 				FileManagerModel model = new FileManagerModel();
 				AnnotationModel annoModel = new AnnotationModel();
 				List<DBObject> annoList = (List<DBObject>)annoModel.getAll(email);
 				DBObject user = model.get(uid);
 				@SuppressWarnings("unchecked")
 				List<BasicDBObject> bookList = (List<BasicDBObject>)user.get("books");
 				if(annoList != null) request.setAttribute("annotations", annoList);
 				if (bookList != null )request.setAttribute("books", bookList);
 				RequestDispatcher disp = request.getRequestDispatcher("fileManager.jsp");
				if( annoList == null ) 
					request.setAttribute("noannot", "true");
				else
					request.setAttribute("noannot", "false");
				if( bookList == null)
					request.setAttribute("nobook", "true");
				else
					request.setAttribute("nobook", "false");
					disp.forward(request, response);
 			} else {
 				RequestDispatcher disp = request.getRequestDispatcher("fileManager.jsp");
 				disp.forward(request, response);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
