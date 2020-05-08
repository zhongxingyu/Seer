 package ge.edu.freeuni.restaurant.presentation;
 
 import ge.edu.freeuni.restaurant.logic.TableReserveManager;
 import ge.edu.freeuni.restaurant.logic.User;
 import ge.edu.freeuni.restaurant.logic.UserManager;
 import ge.edu.freeuni.restaurant.logic.shekveta;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class PlaceOrderServlet
  */
 @WebServlet("/PlaceOrderServlet")
 public class PlaceOrderServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public PlaceOrderServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String username = request.getParameter("username");
		int menuSize = Integer.parseInt(request.getParameter("menuSize"));
 		String reservedTables = request.getParameter("orderTables");
 		String reservedTimes = request.getParameter("orderTimes");
 		shekveta sh = new shekveta(username);
		
 		int amount;
		for (int i = 1; i <= menuSize; i++) {
			amount = Integer.parseInt(request.getParameter("dish#"+i));
			sh.addShekveta(i, amount);
 		}
 		sh.saveIntoDB(reservedTables, reservedTimes);
 		RequestDispatcher dispatch = request.getRequestDispatcher("TableView.jsp");
 		dispatch.forward(request, response);
 	}
 }
