 
 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import pojo.DbContainor;
 import pojo.NgoList;
 import pojo.UniqueId;
 import javax.servlet.http.HttpSession;
 import javax.servlet.RequestDispatcher;
 
 /**
  *
  * @author Ajit Gupta 
  */
 @WebServlet(name = "JoinNgoServlet", urlPatterns = {"/JoinNgoServlet"})
 public class JoinNgoServlet extends HttpServlet
 {
 
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		RequestDispatcher rd=null;
 		
 		try
 		{
 			NgoList ngo_list = new NgoList();
 			HttpSession session = request.getSession();
             
 			if(session!=null)
 			{
 				ngo_list.setNgoid(request.getParameter("qid"));
 				ngo_list.setUnid(session.getAttribute("id").toString());
 				String referer = request.getHeader("Referer");
 
				if(ngo_list.updateNgoList())
 				{
 					rd = request.getRequestDispatcher("referer");
 					rd.forward(request,response);
 				}
 				else
 				{
 					rd = request.getRequestDispatcher("referer");
 					rd.forward(request,response);
 				}
 			}
 			else
 			{
 				response.sendRedirect("LoggedOut.jsp");
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
