 package eshop.controller;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.Part;
 
 import eshop.model.CartLine;
 import eshop.model.Category;
 import eshop.model.CategoryDao;
 import eshop.model.CategoryRepository;
 import eshop.model.FormAttributes;
 import eshop.model.Product;
 import eshop.model.ProductDao;
 import eshop.model.ProductRepository;
 
 @WebServlet("/")
 //see tutorial http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html
 @MultipartConfig(maxFileSize=16177215) // maxFileSize up to 16 MB
 public class ProductsController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String PRODUCTS_SESSION_KEY = "products_session_key";
 	
 	private ProductDao repository;
 	private CategoryDao categoryDao;
        
     @Override
     public void init() throws ServletException {
     	super.init();
     	   	
     	String driver = getServletContext().getInitParameter("driver");
     	String url = getServletContext().getInitParameter("url");
     	String login = getServletContext().getInitParameter("login");
     	String pass = getServletContext().getInitParameter("pass");
     	
     	repository = new ProductRepository(driver, url, login, pass);
     	categoryDao = new CategoryRepository(driver, url, login, pass);
     }
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
 		String action = request.getParameter("action");
 		String viewLocation = "/index.jsp";
 		
 		if (action != null && action.equals("edit")) {
			viewLocation = "/edit.jsp";			
 			
 			String id = request.getParameter("id");
 			if (id != null) {		
 				Product p = repository.getProduct(Integer.parseInt(id));
 				if (p != null) {
 					FormAttributes attrs = new FormAttributes();
 					attrs.setAction("edit");
 					
 					request.setAttribute("product", p);
					request.setAttribute("attributes", attrs);	
					
					List<Category> categories = categoryDao.getCategories();
					for (Category c : categories) {
						if (c.getId() == p.getCategory().getId()) {
							c.setSelected(true);
						}
					}
					request.setAttribute("categories", categories);
				}
			} else {
				List<Category> categories = categoryDao.getCategories();
				if (categories != null && categories.size() > 0) {
					request.setAttribute("categories", categories);
 				}
 			}
 		} else {		
 			List<Product> products = repository.getProducts();
 			
 			request.setAttribute("products", products);
 		}
 		
 		RequestDispatcher view = request.getRequestDispatcher(viewLocation);
 		view.forward(request, response);
 	}	
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		boolean result = false;
 		
 		String action = request.getParameter("action");
 		if (action == null || action.isEmpty()) {
 			String name = request.getParameter("name");
 			BigDecimal price = new BigDecimal(request.getParameter("price"));
 			int catId = Integer.parseInt(request.getParameter("category"));
 			Part filePart = request.getPart("file");
 			if (filePart != null) {
 				InputStream inputStream = filePart.getInputStream();
 				String mimeType = filePart.getContentType();
 				
 				if (inputStream.available() > 0)
 					result = repository.saveProduct(new Product(name, price, mimeType, inputStream, new Category(catId)));
 				else
 					result = repository.saveProduct(new Product(name, price, new Category(catId)));
 			} else {
 				result = repository.saveProduct(new Product(name, price, new Category(catId)));
 			}					
 		}
 		
 		if (action.equalsIgnoreCase("edit")) {
 			String name = request.getParameter("name");
 			BigDecimal price = new BigDecimal(request.getParameter("price"));
 			int id = Integer.parseInt(request.getParameter("id"));
 			int catId = Integer.parseInt(request.getParameter("category"));
 			
 			Part filePart = request.getPart("file");
 			if (filePart != null) {
 				InputStream inputStream = filePart.getInputStream();
 				String mimeType = filePart.getContentType();			
 				
 				if (inputStream.available() > 0)
 					result = repository.updateProduct(new Product(id, name, price, mimeType, inputStream, new Category(catId)));	
 				else
 					result = repository.updateProduct(new Product(id, name, price, new Category(catId)));
 			} else {			
 				result = repository.updateProduct(new Product(id, name, price, new Category(catId)));
 			}
 		}
 		
 		if (action.equalsIgnoreCase("addToCart")) {
 			String id = request.getParameter("id4cart");
 			if (id != null) {
 				Product p = repository.getProduct(Integer.parseInt(id));
 				if (p != null) {
 					HttpSession session = request.getSession();
 					
 					@SuppressWarnings("unchecked")
 					List<CartLine> cartLines = (ArrayList<CartLine>) session.getAttribute(PRODUCTS_SESSION_KEY);
 					if (cartLines == null) {
 						cartLines = new ArrayList<CartLine>();
 						cartLines.add(new CartLine(p, 1));
 					} else {
 						boolean found = false;
 						
 						for (CartLine c : cartLines) {
 							Product cp = c.getProduct();
 							if (cp.getId() == p.getId()) {
 								int q = c.getQuantity();
 								q++;
 								c.setQuantity(q);
 								found = true;
 							}
 						}
 						
 						if (!found)
 							cartLines.add(new CartLine(p, 1));
 					}				
 										
 					session.setAttribute(PRODUCTS_SESSION_KEY, cartLines);
 				}
 			}
 			
 			result = true;
 		}
 		
 		if (result)
 			response.sendRedirect("/eshop/products");
 		else
 			response.sendRedirect("/eshop/edit.jsp");
 	}
 }
