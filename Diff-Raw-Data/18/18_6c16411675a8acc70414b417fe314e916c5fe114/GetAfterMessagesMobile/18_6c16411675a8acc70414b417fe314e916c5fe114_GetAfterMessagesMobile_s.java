 package arc;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.Vector;
 
 import com.google.gson.Gson;
 
 public class GetAfterMessagesMobile extends HttpServlet {
 
 	 
 public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
 	HttpSession session = request.getSession(false);
 	User user = null;
 	Tablon tablon = new Tablon();	
 	PrintWriter out = response.getWriter();
    	Tablon returnTablon = new Tablon();
 	
    	/*parameters received*/
    	String messageId = request.getParameter("messageId");
    	String spaceId = request.getParameter("tablonSpace");
 
 	tablon = tablon.getTablonDDBB(spaceId);
 	System.out.println("el último messageId que tiene el cliente es: "+messageId);
 	
 	returnTablon = tablon.getAfterMessages(Integer.parseInt(messageId),tablon.getId());
 	System.out.println("el parámetro que llega: "+spaceId);
 	Gson gson = new Gson();
 	String tablonJson = gson.toJson(returnTablon);
 	System.out.println("Mas mensajes pedidos son: "+ tablonJson);
 	out.println(tablonJson);
 
   }
 
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
   
     doGet(request,response);
 
     }
 }
