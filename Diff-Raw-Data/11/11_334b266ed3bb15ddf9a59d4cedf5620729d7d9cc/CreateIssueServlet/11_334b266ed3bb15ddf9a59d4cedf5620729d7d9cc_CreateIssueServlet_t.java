 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import pojo.Issue;
 import pojo.UniqueId;
 import javax.servlet.http.HttpSession;
 import javax.servlet.RequestDispatcher;
 import pojo.DbContainor;
 
 
 /**
  *
  * @author Ajit Gupta 
  */
 @WebServlet(name = "CreateIssueServlet", urlPatterns = {"/CreateIssueServlet"})
 public class CreateIssueServlet extends HttpServlet
 {
 
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		RequestDispatcher rd = null;
 		
 		try
 		{
 			Issue issue = new Issue();
             HttpSession session = request.getSession();
             
 			issue.setIssueid("Iss"+UniqueId.generateId());
 			issue.setUnid(session.getAttribute("id").toString());
 			issue.setIssuedate(request.getParameter("issuedate"));
 			issue.setContent(request.getParameter("content"));
 			issue.setVisibility("Friends");
             
			String referer = request.getHeader("Referer");
 			if(issue.createIssue())
 			{
				rd = request.getRequestDispatcher("referer");
 				rd.forward(request, response);    
 			}
			else
			{
				rd = request.getRequestDispatcher("referer");
				out.println("<span id='issue_msg'>Can not create Issue , Try Again Later !</span>");
				rd.forward(request, response);
			}
 		}
 		finally
 		{
 			out.close();
 		}
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   	/** 
 	* Handles the HTTP <code>GET</code> method.
 	* @param request servlet request
 	* @param response servlet response
 	* @throws ServletException if a servlet-specific error occurs
 	* @throws IOException if an I/O error occurs
 	*/
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		processRequest(request, response);
 	}
 
   	/** 
 	* Handles the HTTP <code>GET</code> method.
 	* @param request servlet request
 	* @param response servlet response
 	* @throws ServletException if a servlet-specific error occurs
 	* @throws IOException if an I/O error occurs
 	*/
 	
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		processRequest(request, response);
 	}
 
 	/** 
 	* Returns a short description of the servlet.
 	* @return a String containing servlet description
 	*/
 	@Override
 	public String getServletInfo()
 	{
 		return "Short description";
 	}// </editor-fold>
 }
