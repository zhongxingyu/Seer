 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import pojo.DbContainor;
 import pojo.Event;
 import pojo.UniqueId;
 import javax.servlet.http.HttpSession;
 import javax.servlet.RequestDispatcher;
 
 
 
 /**
  *
  * @author Ajit Gupta 
  */
 @WebServlet(name = "CreateEventServlet", urlPatterns = {"/CreateEventServlet"})
 public class CreateEventServlet extends HttpServlet
 {
 
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
 	{
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		RequestDispatcher rd = null;
 		
 		try
 		{
 			Event event = new Event();
 			HttpSession session = request.getSession();
 			
 			event.setEventid("Eve"+UniqueId.generateId());
 			event.setOrganiserid(session.getAttribute("id").toString());
 			event.setEventdate(request.getParameter("eventdate").trim());
 			event.setExpdate(request.getParameter("expdate").trim());
 			event.setEventdesc(request.getParameter("eventdesc"));
 			event.setLikes(0);
 			event.setVisibility("friends");
 			event.setSubject(request.getParameter("subject"));
 			event.setEventname(request.getParameter("eventname"));
            
			String referer = request.getHeader("Referer");
 			if(event.createEvent())
 			{
				rd = request.getRequestDispatcher("referer");
				rd.forward(request, response);
			}
			else
			{
				rd = request.getRequestDispatcher("referer");
				out.println("<span id='event_msg'>Can not create event , Try Again Later !</span>");
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
