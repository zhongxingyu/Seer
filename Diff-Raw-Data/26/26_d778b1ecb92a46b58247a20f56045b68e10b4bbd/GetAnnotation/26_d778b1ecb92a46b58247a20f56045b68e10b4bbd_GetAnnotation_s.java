 package com.annotation.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import com.annotation.model.AnnotationModel;
 import com.mongodb.DBObject;
 
 public class GetAnnotation extends HttpServlet {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -150691783774775448L;
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		try{
 			HttpSession session = request.getSession(false);
 			PrintWriter out = response.getWriter();
 			
 			if(session != null) {
 				String email = (String) session.getAttribute("email");
 				String bookId = (String)request.getParameter("bookId");
 				String pNum = (String)request.getParameter("pNum");
 				AnnotationModel model = new AnnotationModel();
 				List<DBObject> annotList = model.get(bookId, email, Integer.parseInt(pNum));
 				
 				response.setContentType("text/html;charset=UTF-8");
 				int size = annotList.size();
 				if(size > 0){
 					for(int i = 0; i < size ; i++){
 						DBObject a = annotList.get(i);
 						String tempOwner = (String) a.get("owner");
 						String title = (String)a.get("title");
 						String time = (String)a.get("time");
 						boolean isPublic = Boolean.valueOf((String)a.get("isPublic"));
 						
 						String printStr = "<div class='annotation'><div><span class='note_text'>"+(String)a.get("text")+"</span><br><span class='author'>"+tempOwner+" @"+title+"</span><span class='time'> | "+time+"</span>";
 						if(tempOwner != null){
 							if(tempOwner.equals(email)){
 								if(!isPublic) 
 									printStr += "<span class='make_public' title='Make public'> Publish</span>";
 								else
									printStr += "<span class='make_public' title='Make public'> Published!</span>";
 								printStr += "<span class='delete_note' title='Delete note'> Delete</span>";
 								
 							}
 						}
 						printStr += "<span class='annot_data' id='annot_b_id'>"+(String)a.get("bookId")+"</span><span class='annot_data' id='annot_p_num'>"+a.get("pNum")+"</span><span class='annot_data' id='annot_id'>"+(a.get("_id")).toString()+"</span></div></div>"; 
 						out.print(printStr);
 					}
 				} else out.print("");
 			} else out.print("");
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
