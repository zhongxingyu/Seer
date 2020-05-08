 package servlets;
 
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import pojo.Discussion;
 import javax.servlet.RequestDispatcher;
 import pojo.UniqueId;
 import pojo.DbContainor;
 
 
 
 
 /**
  *
  * @author Ajit Gupta 
  */
 @WebServlet(name = "DiscussionServlet", urlPatterns = {"/DiscussionServlet"})
 public class DiscussionServlet extends HttpServlet
 {
 
     
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter out = response.getWriter();
         RequestDispatcher dispatcher = null;
 		
 		try
 		{
			DiscussionBean disc_bean = new DiscussionBean();            
 			disc_bean.setTopic(request.getParameter("topic").trim());
 			disc_bean.setTopicdesc(request.getParameter("topicdesc").trim());
 			disc_bean.setDiscid("dis"+UniqueId.generateId());
 			disc_bean.setTopicdate(DbContainor.getDate());
 			disc_bean.createDiscussion();
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
