 package com.tongji.j2ee.sp;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class Paging extends HttpServlet {
 	
 	
 
 	/**
 	 * The doGet method of the servlet. <br>
 	 *
 	 * This method is called when a form has its tag value method equals to get.
 	 * 
 	 * @param request the request send by the client to the server
 	 * @param response the response send by the server to the client
 	 * @throws ServletException if an error occurred
 	 * @throws IOException if an error occurred
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		
 		HttpSession hs = request.getSession();
 		int iCurPage = 1;
 		String pageNumberStr = request.getParameter("pageNumber");
 		
 		if(pageNumberStr!=null && !pageNumberStr.isEmpty())
 	    {
 			iCurPage = Integer.parseInt(pageNumberStr);
 	    }
 		System.out.println("Nextpage: " + iCurPage);
 		
 		//request.setAttribute("pageNumber", iCurPage);
 		
 		NotifyList lns = (NotifyList) hs.getAttribute("noteli");
 		System.out.println("allitems" + lns.allItems);
 		//request.setAttribute("noteli", lns);
 		
 		switch(lns.iSymbol){
 		case 0:
 			LoginServlet.setUpAdmin(request, response, hs, iCurPage);
 			request.getRequestDispatcher("admin.jsp").forward(
 					request, response);
 			break;
 		case 1:
 			LoginServlet.setUpStudent(request, response, hs, iCurPage);
 			request.getRequestDispatcher("student.jsp").forward(
 					request, response);
 			break;
 		case 2:
 			LoginServlet.setUpTeacher(request, response, hs, iCurPage);
 			request.getRequestDispatcher("teacher.jsp").forward(
 					request, response);
 			default:
 				break;
 		}
 		
 		
 
 	}
 
 	/**
 	 * The doPost method of the servlet. <br>
 	 *
 	 * This method is called when a form has its tag value method equals to post.
 	 * 
 	 * @param request the request send by the client to the server
 	 * @param response the response send by the server to the client
 	 * @throws ServletException if an error occurred
 	 * @throws IOException if an error occurred
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 //		response.setContentType("text/html");
 //		PrintWriter out = response.getWriter();
 //		out
 //				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
 //		out.println("<HTML>");
 //		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
 //		out.println("  <BODY>");
 //		out.print("    This is ");
 //		out.print(this.getClass());
 //		out.println(", using the POST method");
 //		out.println("  </BODY>");
 //		out.println("</HTML>");
 //		out.flush();
 //		out.close();
 	}
 
 }
