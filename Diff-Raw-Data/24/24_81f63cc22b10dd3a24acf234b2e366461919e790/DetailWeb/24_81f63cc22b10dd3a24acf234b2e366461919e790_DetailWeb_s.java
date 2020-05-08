 package com.detail.web;
 
 import com.detail.model.*;
 import com.mongodb.DBObject;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.io.*;
 import java.util.List;
 
 public class DetailWeb extends HttpServlet {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7593900973669488904L;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)  throws IOException, ServletException {
 		try{
 			
 			String bookID = (String)request.getParameter("book");
 			String pageNum = (String)request.getParameter("page");
			
 			String[] searchTerms = (String[])request.getParameterValues("searchTerm");
 			DetailModel dm = new DetailModel(bookID, pageNum);
 			DBObject page = dm.getPage();
 			@SuppressWarnings("unchecked")
 			List<String> pageContent = (List<String>) page.get("content");
 			
 			if(searchTerms != null) {
 				for(int i = 0; i < pageContent.size(); i++) {
 					String temp = pageContent.get(i);
 					for(String x:searchTerms) {
 						temp = temp.replaceAll(x, "<span class='highlight'>"+x+"</span>");
 						
 					}
 					pageContent.set(i, temp);
 				}
 				request.setAttribute("searchTerms", searchTerms);
 			}
 			page.put("content", pageContent);
 			request.setAttribute("page", page);
			
 			RequestDispatcher dispatcher = request.getRequestDispatcher("display.jsp");
 			dispatcher.forward(request, response);
 			
 		} catch(Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 }
