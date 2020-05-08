 package ctrl;
 
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.*;
 
 /**
  * Servlet implementation class FRU
  */
 @WebServlet(urlPatterns = {"/Start","/category","/FRU"} )
 public class FRU extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public FRU() {
         super();
     }
     
     /**
      * 
      */
 	@Override
 	public void init() throws ServletException {
 		
 		super.init();
 
 		//the singleton model need to be initialized here
 		FRUModel fru;
 		try {
 			fru = new FRUModel();
 			List<CategoryBean> cat =  fru.retrieveCategory();
 			
 			this.getServletContext().setAttribute("fru", fru);
 			
 			this.getServletContext().setAttribute("categories", cat);
 			for (CategoryBean c : cat )
 			{
 				String itemName = "item" + c.getCatID();
 				this.getServletContext().setAttribute(itemName,fru.retrieveItems(c.getCatID()));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
     
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// assume that index.html, we have four buttons , each called login , shopping cart, check out and express check out
 		// let the button call doit 	
 		String target;
 		String doit = request.getParameter("doit");
 		FRUModel model = (FRUModel) this.getServletContext().getAttribute("fru"); 
 		if (doit == null)
 		{
 			try
 			{
 				if(request.getParameter("selectedCategory")!= null)
 				{
 					String itemName = "item" + request.getParameter("selectedCategory");
 					request.setAttribute("item",this.getServletContext().getAttribute(itemName));
 					target = "/category.jspx";
 				}
 				
 				else if (request.getParameter("add")!= null)
 				{	
 					String add = request.getParameter("add");
 					if (add.equals("AddToCart"))
 					{
 					
						ShoppingCartHelper cart = (ShoppingCartHelper)request.getSession().getAttribute("cart");
 						if (cart == null)
 						{	
 							cart = new ShoppingCartHelper();
 						}
 						
						System.out.println("is cart null "  + cart == null);
 						try 
 						{
 							FRUModel fru = (FRUModel) this.getServletContext().getAttribute("fru");
 							fru.addToCart(cart, request.getParameter("itemToAdd"),request.getParameter("qtyToAdd"));	
 							
							request.getSession().setAttribute("cart", cart);
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
 						target ="/index.jspx";
 
 					}
 				}
 				else
 				{		
 					target = "/index.jspx";
 				}
 				
 			} 
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				target = "/exception.jspx";
 			}
 		}
 		
 		else 
 		{
 		
 			
 			if (doit.equals("login"))
 			{
 				target = "/login.jspx";
 			}
 			else if (doit.equals("cart"))
 			{
 				target = "/cart.jspx";
 			}
 			else if (doit.equals("checkout"))
 			{
 				target = "/checkout.jspx";
 			}
 			else if (doit.equals("search"))
 			{   //search according the part of the item name.
 				String si = (String) request.getParameter("searchItem");
 				String regex1 = "[0-9]+[a-zA-Z]*[0-9]*";
 				
 				try
 				{
 					if(si.matches(regex1))
 					{
 					  List<ItemBean> ibl = model.searchItemNumber(si);
 					  request.setAttribute("item", ibl);
 					  System.out.println("inside if1 " + si );
 					  
 					}else
 					{
 					  List<ItemBean> ibl = model.searchItemName(si);
 					  request.setAttribute("item", ibl);
 					  System.out.println("inside else1");
 					}
 				} catch (SQLException e)
 				{
 					e.printStackTrace();
 				}
 
 				target="/category.jspx";
 			}
 			else if (doit.equals("logout"))
 			{
 				target = "/index.jspx";
 			}
 			else
 			{
 				target = "/express.jspx";
 			}
 
 		
 		}
 		
 		
 		RequestDispatcher rd= request.getRequestDispatcher(target);
 		rd.forward(request, response);
 		
 	}
 
 	
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet (request, response);
 	}
 
 }
