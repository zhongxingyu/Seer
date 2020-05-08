 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import pojo.DbContainor;
 import pojo.FriendRequest;
 import pojo.UniqueId;
 import javax.servlet.http.HttpSession;
 import javax.servlet.RequestDispatcher;
 
 /**
  *
  * @author Ajit Gupta 
  */
 @WebServlet(name = "FriendRequestServlet", urlPatterns = {"/FriendRequestServlet"})
 public class FriendRequestServlet extends HttpServlet
 {
 
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		RequestDispatcher rd =n ull;
 		
 		try
 		{
 			FriendRequest frnd_req = new FriendRequest();
             HttpSession session = request.getSession(false);
 
 			String msg = request.getParameter("message");
			if(msg==null)
 			{
				msg = "";
 			}
			frnd_req.setMsg(msg);
 			frnd_req.setReqid("frnd-req"+UniqueId.generateId());
 			frnd_req.setReqSender(session.getAttribute("id").toString());
 			frnd_req.setReqReciever(session.getAttribute("qid").toString());
 			frnd_req.setReqdate(DbContainor.getDate());
 			frnd_req.setStatus("unconfirmed");
 			
 			String referer = request.getHeader("Referer");
 			if(frnd_req.sendRequest())
 			{
 				rd = request.getRequestDispatcher("referer");
 				rd.forward(request, response);
 			}
 			else
 			{
 				rd = request.getRequestDispatcher("referer");
 				out.println("<span id='req_msg'>Can not Send Request , Try Again Later !</span>");
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
