 package ctrl;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class Static
  * print out the static of the website, has client number and ave checkout time now
  * more static can be add in later on
  */
 @WebServlet("/Statistic")
 public class Statistic extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      *    @see HttpServlet#HttpServlet()
      */
     public Statistic() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
		String target = "/statistic.jspx";  
 		RequestDispatcher rd = request.getRequestDispatcher(target);
 		rd.forward(request, response);
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
 	}
 
 }
