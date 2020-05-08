 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import entities.UserEntity;
 import exceptions.BlankLoginInfoException;
 import exceptions.WrongLoginInputException;
 
 import login.Logger;
 
 import view.Style;
 
 /**
  * Servlet implementation class Login
  */
 @WebServlet("/Login")
 public class Login extends HttpServlet {
 	
 	private static final String TITLE = "Login";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Login() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 			throws ServletException, IOException {
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		out.println(Style.pageHeader(TITLE));
 		HttpSession session = request.getSession();
 		
 		// controlla se  stato gi effettuato il login
 		UserEntity user = (UserEntity) session.getAttribute("utente");		
 		if(user != null){
 			// se si  gi loggati manda al menu
 			if(user.isAdmin()){
 				response.sendRedirect("AdminMenu");				
 			}else{
 				response.sendRedirect("UserMenu");
 			}
 		}
 
 		String nome = request.getParameter("nome");
 		String password = request.getParameter("password");
 		
 		if(nome != null || password != null){
 			// se si passano dei parametri controlla il corretto login
 			Logger logger = new Logger(session);
 			try{
 				logger.login(nome, password);
 				if(logger.isLogged()){
 					session.setAttribute("utente",logger.getUser());
 					if(logger.getUser().isAdmin()){
 						response.sendRedirect("AdminMenu");						
 					}else{
 						response.sendRedirect("UserMenu");
 					}
 				}
 				out.println(Style.alertMessage("Nome utente o password non sono corretti"));
 			}catch(SQLException sqle){
 				out.println(Style.alertMessage("Errore SQL: "+sqle.getMessage()));
 			}catch(WrongLoginInputException wlie){
 				out.println(Style.alertMessage(wlie.getMessage()));
 			}catch(BlankLoginInfoException blie){
 				out.println(Style.alertMessage(blie.getMessage()));
 			}
 		}
 		// stampa form di login
 		out.println("<form name=\"login\" method=\"POST\">");
 		out.println("User: <input type=\"text\" name=\"nome\"><br>");
 		out.println("Password: <input type=\"password\" name=\"password\"><br>");
 		out.println("<input type=\"submit\" value=\"login\">");
 		out.println("</form>");	
 		
		out.println(Style.pageFooter());
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 			throws ServletException, IOException {
 		this.doGet(request, response);
 	}
 
 }
