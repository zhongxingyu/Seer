 package controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.Category;
 
 import dao.CategoryDao;
 
 public class DashboardController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private final static String[] STATES = { "AL", "AK", "AZ", "AR", "CA",
 			"CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS",
 			"KY", "LA", "ME", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
 			"NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR",
 			"PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV",
 			"WI", "WY" };
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		CategoryDao cdao = new CategoryDao();
 		List<Category> categories = cdao.getAllCategories();
 		java.util.Collections.sort(categories);
 		System.out.println(categories);
 		
 		request.setAttribute("states", STATES);
 		request.setAttribute("categories", categories);
 		RequestDispatcher view = request.getRequestDispatcher("/dashboard.jsp");
 		view.forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 }
