 package arc;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
import java.net.URLDecoder;
 import java.util.Vector;
 
 public class SendMessageMobile extends HttpServlet {
   /**
 	 * 
 	 */
 public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
 	String nextServlet = "/getAfterMessagesMobile"; //Servlet destino
 
 	HttpSession session = request.getSession(false);
 
 	System.out.println("el id de sesion es: " + session.getId());
 
	String message = URLDecoder.decode(request.getParameter("message"),"UTF-8");
 	String tablonId=request.getParameter("tablonId").trim();
 	String lastMessageId = request.getParameter("messageId");//lastMessageId
 
 	User sessionUser = (User)session.getAttribute("user");
 
 	System.out.println("El mensaje que se envía: Mensaje: "+ message );
 	/**************************************************/
 	if(sessionUser != null){
 		Message msg = new Message();
 		Tablon tablon = new Tablon();
 	
 		msg.setMsg(message);
 		msg.setCreator(sessionUser);
 
 		tablon.setId(Integer.parseInt(tablonId));
 		System.out.println("El tablon es: "+ tablonId);
 
 		//envía el mensaje a la base de datos
 		long writeMessageId = tablon.sendMessage(msg);
 		//msg.setVisibility();
 		System.out.println("El mensaje que he enviado ha sido el siguiente: "+ msg.getMsg());
 	}
 
 
 	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextServlet);
 	dispatcher.forward(request,response);
 
   }
 
   public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
       throws ServletException, IOException {
   
     doGet(request,response);
 
     }
 }
