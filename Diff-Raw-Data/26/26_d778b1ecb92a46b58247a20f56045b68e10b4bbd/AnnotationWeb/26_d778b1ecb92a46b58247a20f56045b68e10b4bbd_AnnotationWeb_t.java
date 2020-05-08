 package com.annotation.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import com.annotation.model.AnnotationModel;
 
 public class AnnotationWeb extends HttpServlet{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2320118945150162957L;
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		try {
 			HttpSession session = request.getSession(false);
 			PrintWriter out = response.getWriter();
 			if(session != null){
 				String email = (String) session.getAttribute("email");
 				String text = (String)request.getParameter("text");
 				
 				String bookId = (String)request.getParameter("bookId");
 				String pNum = (String)request.getParameter("pNum");
 				String bookTitle = (String)request.getParameter("bookTitle");
 				AnnotationModel model = new AnnotationModel();
 				
 				String id = model.add(bookId, bookTitle, email, Integer.parseInt(pNum),text);
 				
 				if(id != null){
 				
 					response.setContentType("text/html;charset=UTF-8");
 					String tempStr = "<div class='annotation'><span class='note_text'>"+text+"</span><br><span class='author'>"+email+" @"+bookTitle+" | just seconds ago.</span>";
					tempStr += "<span class='make_public' title='Make public'> Publish</span>";
 					tempStr += "<span class='delete_note' title='Delete note'> Delete</span>";
 					tempStr += "<span class='annot_data' id='annot_b_id'>"+bookId+"</span><span class='annot_data' id='annot_p_num'>"+pNum+"</span><span class='annot_data' id='annot_id'>"+id+"</span></div>";
 					
 					out.print(tempStr);
 				} else 
 					out.print("<p style='color:red;'>Error while adding</p>");
 			} else out.print("<p style='color:red;'>Please login.</p>");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
