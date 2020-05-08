 package Controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import Model.Dish;
 
 /**
  * Servlet implementation class Home
  */
 @WebServlet("/Home")
 public class Home extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Home() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession sess = request.getSession();
 		ArrayList<Dish> lastApprovedDishes = (ArrayList<Dish>) Dish.GetLastApprovedDishes();
 		ArrayList<Dish> topTen = (ArrayList<Dish>) Dish.GetTopTenDishes();
 		ArrayList<Dish> randomDishes = (ArrayList<Dish>) Dish.GetRandomTenDishes();
		sess.setAttribute("randomDishes", randomDishes);
 		sess.setAttribute("lastApprovedDishes", lastApprovedDishes);
 		sess.setAttribute("topTen", topTen);
 		RequestDispatcher dispatch = request.getRequestDispatcher("HomePage.jsp");
 		dispatch.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 }
