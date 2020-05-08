 package ctrl;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
 import model.FRUModel;
 import model.ItemBean;
 import model.ShoppingCartHelper;
 
 /**
  * Servlet implementation class ShoppingCart
  */
 @WebServlet("/ShoppingCart")
 public class ShoppingCart extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ShoppingCart() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String add = request.getParameter("add");
 		String target ;
		HttpSession session = request.getSession();
 		if (add != null)
 		{
 			if (add.equals("AddToCart"))
 			{
 				
				ShoppingCartHelper cart = (ShoppingCartHelper)session.getAttribute("cart");
 				if (cart == null)
 				{	
 					cart = new ShoppingCartHelper();
 				}
 				try 
 				{
 					FRUModel fru = (FRUModel) this.getServletContext().getAttribute("fru");
 
 					fru.addToCart(cart, request.getParameter("itemToAdd"),request.getParameter("qtyToAdd"));	
					session.setAttribute("cart", cart);
 					request.setAttribute("cart",cart);
 				}
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 					response.sendError(500);
 				}
 				target ="/cart.jspx";
 			}
 			else
 			{
 				target = "/index.jspx";
 			}
 		}
 		else
 		{
 			target ="/index.jspx";
 		}
 		
 		request.getRequestDispatcher(target).forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request,response);
 	}
 
 }
