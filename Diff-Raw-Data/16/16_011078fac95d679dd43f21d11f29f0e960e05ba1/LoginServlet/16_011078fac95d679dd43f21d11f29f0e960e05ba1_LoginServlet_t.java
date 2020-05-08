 package app.servlets;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class LoginServlet extends ApplicationController {
 	private static final long serialVersionUID = 1L;
        
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 	
 	public void listar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String[] arreglo = {"HAML", "SASS", "Compass", "Rails"};
 		for (String s : arreglo)
 			System.out.println(s);
//		render(request, response);
//		render(request, response, "index");
		render(request, response, "login/index");
 	}
 
 }
