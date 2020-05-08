 package com.bingo.eatime.test;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.bingo.eatime.core.Category;
 import com.bingo.eatime.core.CategoryManager;
 import com.bingo.eatime.core.Restaurant;
 import com.bingo.eatime.core.RestaurantManager;
 import com.google.appengine.api.datastore.PhoneNumber;
 import com.google.appengine.api.datastore.PostalAddress;
 
 public class EaTimeDatabaseTestServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 2639400166713434665L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
 		try {
 			resp.setContentType("text/html");
 			PrintWriter writer = resp.getWriter();
 			writer.println("<html><head></head><body><p>Testing Database...</p></body></html>");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		testDb();
 	}
 
 	private void testDb() {
		boolean result = false;
 
 		Category categoryChinese = Category.createCategory("Chinese");
 		result = CategoryManager.addCategory(categoryChinese);
 
 		System.out.println("Add category " + result);
 
 		Restaurant restaurantHappyChina = Restaurant.createRestaurant(
 				"Happy China", categoryChinese, new PostalAddress(
 						"219 E State Street, West Lafayette, IN 47906"),
 				new PhoneNumber("765-743-1666"));
 		result = RestaurantManager.addRestaurant(restaurantHappyChina);
 
 		System.out.println("Add restaurant " + result);
 	}
 
 }
