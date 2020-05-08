 package groupone;
 
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class HomePage
  */
 @WebServlet("/HomePage")
 public class HomePage extends HttpServlet {
 	private static final long serialVersionUID = 1L;
	private static String email;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public HomePage() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
		String e = request.getParameter("email");
 		String name = request.getParameter("button");
		
		if(e != null && !e.isEmpty()) {
			email = e;
		}
		
 		if(name.equals("Browse")) {
			request.setAttribute("email", email);
 			request.getRequestDispatcher("/page_browse.jsp").forward(request, response);
 		}
 		else if(name.equals("Order History")) {
			request.setAttribute("email", email);
 			request.getRequestDispatcher("/page_orderHist.jsp").forward(request, response);
 		}
 		else if(name.equals("Account")) {
			request.setAttribute("email", email);
 			request.getRequestDispatcher("/page_account.jsp").forward(request, response);
 		}
 		else {
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
 		
		System.out.println("Account: "+email);
		
 	}
 
 }
