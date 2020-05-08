 package com.uc3.onlineStore.controller;
 import entities.*;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.io.*;
 import java.math.BigDecimal;
 import java.util.*;
 
 public class CartController extends HttpServlet {
 	
 	EntityManagerFactory emf = Persistence.createEntityManagerFactory("OnlineStore_WEB"); // CHANGE IF U HAVE DIFFERENT NAME!
 	private static final long serialVersionUID = 2L;
 	private boolean debug = true;
 	Query query;
 	List results;
 	
 	
     public CartController() {
         super();
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	String pathInfo = request.getServletPath();
     	String[] urlParsed = pathInfo.split("/");
     	String url = "index", section = "";
     	if (urlParsed.length >= 2)
     		section = urlParsed[1];
     	if (section.contains("cart")) {
     		url = "cart";
     		getAllProductsInCart(request);
     		getTotalPriceOfAllProductsInCart(request);
     		getCurrentOrderToAddProducts(request);
     	} 
     	if (section.contains("checkout")) {
     		url = "checkout";
     		getAllProductsInCart(request);
     		getTotalPriceOfAllProductsInCart(request);
     		getCurrentOrderToAddProducts(request);
     	} 
     	if (section.contains("confirmation")) {
     		url = "checkout-confirmation";
     		getAllProductsInCart(request);
     		getTotalPriceOfAllProductsInCart(request);
     		getCurrentOrderToAddProducts(request);
     	} 
     	try {
     		if(request.getSession().getAttribute("logged") == null || !request.getSession().getAttribute("logged").equals("yes")){
     			url = "login";
 				request.setAttribute("message", "notLoggedToSeeCart");
     		} 
     	} catch (Exception e) {}
 
     	
     	url = "/" + url + ".jsp";
     	try {
     		this.getServletContext().getRequestDispatcher(url)
     		.forward(request, response);
     	} catch (Exception e) {
     		request.getRequestDispatcher("/index.jsp").forward(request,
     				response);
     		return;
     	}
 
     }
     
     
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     		String pathInfo = request.getServletPath();
     		String[] urlParsed = pathInfo.split("/");
     		String url = "index", section = "";
     		String action = "";
 
     		EntityManager em = emf.createEntityManager();
     		
     		boolean isLoginSession = false;
     		if (urlParsed.length >= 2)
     			section = urlParsed[1];
     		if (section.contains("confirmation")) {
         		url = "checkout-confirmation";
         		getAllProductsInCart(request);
         		getTotalPriceOfAllProductsInCart(request);
         		getCurrentOrderToAddProducts(request);
         	} 
     		
     		getCurrentOrderToAddProducts(request);
     		
     		/* SAVING EDITED QUANTITY */
     		if (request.getParameter("actionPost") != null) {
     			if (request.getParameter("actionPost").equals("editQuantity")) {
     				String id = request.getParameter("id");
     				int quantity = Integer.parseInt(request.getParameter("quantity"));
 
     				EntityTransaction et = em.getTransaction();
     				et.begin();
     				Query editQuantity = em
     				.createQuery("SELECT op FROM Ordproduct op WHERE op.idOrdproduct="+id);
     				results = editQuantity.getResultList();
     				for (Object current : results) {
     					Ordproduct op = (Ordproduct) current;
     					op.setQuantity(quantity);
     					et.commit();
     					em.close();
     				}
     			}
     		}
 
     			/* DELETING PRODUCT FROM CART */
         		if (request.getParameter("actionPost") != null) {
         			if (request.getParameter("actionPost").equals("delete")) {
         				String id = request.getParameter("id");
 
         				EntityTransaction et = em.getTransaction();
 						et.begin();
 						Query deleteProduct = em
 						.createQuery("SELECT op FROM Ordproduct op WHERE op.idOrdproduct="+id);
 						results = deleteProduct.getResultList();
 						for (Object current : results) {
 							Ordproduct op = (Ordproduct)current;
 							em.remove(op);
 						}
 						et.commit();
 						em.close();
         				}
         			}
         		
         		
         		/* CONFIRMING ORDER AFTER PAYMENT (change to confirmed and create new unconfirmed order) */
         		if (request.getParameter("actionPost") != null) {
         			if (request.getParameter("actionPost").equals("payment-confirmation")) {
         				int idOrder = (Integer)request.getAttribute("idCurrentOrder");
         				int userId = Integer.parseInt(request.getParameter("userId"));
         				User u = null;
         				
         				em = emf.createEntityManager();
 						query = em.createQuery("SELECT u FROM User u WHERE u.idUser="+userId);
 						results = query.getResultList();
 						if (results!=null){
 							for(Object current: results){
 								u = (User)current;
 							}
 						}
 						em.close();
 						
 						
						/* CHANGING QUANTITY IF CONFIRMED */
 						em = emf.createEntityManager();
         				EntityTransaction et = em.getTransaction();
 						et.begin();
						Query changeQuantity = em
						.createQuery("SELECT op FROM Ordproduct op WHERE op.ord.idOrd="+idOrder);
						results = changeQuantity.getResultList();
						for (Object current : results) {
							Ordproduct op = (Ordproduct)current;
							op.getProduct().setStock(op.getProduct().getStock()-op.getQuantity());
						}
						et.commit();
						em.close();
						
						
						
						
						em = emf.createEntityManager();
        				et = em.getTransaction();
						et.begin();
 						Query deleteProduct = em
 						.createQuery("SELECT o FROM Ord o WHERE o.idOrd="+idOrder);
 						results = deleteProduct.getResultList();
 						for (Object current : results) {
 							Ord o = (Ord)current;
 							o.setAddress(request.getParameter("address"));
 							o.setEmail(request.getParameter("email"));
 							o.setName(request.getParameter("name"));
 							o.setSurname(request.getParameter("surname"));
 							o.setPhone(request.getParameter("phone"));
 							o.setConfirmed((byte)1);
 						}
 						Query maxId = em.createQuery("SELECT MAX(o.idOrd) FROM Ord o");
 						results = maxId.getResultList();
 						int newId = 0;
 						Iterator it = results.iterator();
 						while (it.hasNext()) {
 							newId = ((Integer) it.next()).intValue();
 						}
 						Ord o = new Ord();
 						o.setIdOrd(newId + 1);
 						o.setTime(new Date());
 						o.setUser(u);
 						em.persist(o);
 						
 						et.commit();
 						em.close();
         				}
         			}
         		
         		
         		
         		
         		/* ADDING PRODUCT TO CART */
         		try {
         			if(request.getSession().getAttribute("logged") == null || !request.getSession().getAttribute("logged").equals("yes")){
         				url = "login";
         				request.setAttribute("message", "notLoggedToSeeCart");
         			} else {
         				if (request.getParameter("actionPost") != null) {
         					if (request.getParameter("actionPost").equals("add")) {
         						int idProduct = Integer.parseInt(request.getParameter("idProduct"));
         						int quantity = Integer.parseInt(request.getParameter("quantity"));
         						int idOrd = (Integer)request.getAttribute("idCurrentOrder");
         						Ord ord = null;
         						Product product = null;
         						
         						boolean productAlreadyExistsInCart = false;
         						
         						emf = Persistence.createEntityManagerFactory("OnlineStore_WEB");
 
 
         						em = emf.createEntityManager();
         						query = em.createQuery("SELECT p FROM Product p WHERE p.idProduct="+idProduct);
         						results = query.getResultList();
         						if (results!=null){
         							for(Object current: results){
         								product = (Product)current;
         							}
         						}
         						em.close();
 
         						em = emf.createEntityManager();
         						query = em.createQuery("SELECT o FROM Ord o WHERE o.idOrd="+idOrd);
         						results = query.getResultList();
         						if (results!=null){
         							for(Object current: results){
         								ord = (Ord)current;
         							}
         						}
         						em.close();
         						
         						
         						em = emf.createEntityManager();
         						query = em.createQuery("SELECT op FROM Ordproduct op WHERE op.product.idProduct="+idProduct+" AND op.ord.idOrd="+idOrd);
         						results = query.getResultList();
         						if (results!=null){
         							for(Object current: results){
         								productAlreadyExistsInCart = true;
         								request.setAttribute("message","productAlreadyExistsInCart");
         							}
         						}
         						em.close();
 
         						
         						em = emf.createEntityManager();
         						EntityTransaction et = em.getTransaction();
         						et.begin();
 
         						Query maxId = em
         						.createQuery("SELECT MAX(op.idOrdproduct) FROM Ordproduct op");
         						results = maxId.getResultList();
         						int newId = 0;
         						Iterator it = results.iterator();
         						while (it.hasNext()) {
         							newId = ((Integer) it.next()).intValue();
         						}
         						
         						if(!productAlreadyExistsInCart){
         							Ordproduct op = new Ordproduct();
         							op.setIdOrdproduct(newId + 1);
         							op.setProduct(product);
         							op.setOrd(ord);
         							op.setQuantity(quantity);
 
         							em.persist(op);
         							et.commit();
         						} 
         						em.close();
         					}
         				}
         			}
         		} catch (Exception e) {}
 
 
     			
     			
 
         			
         			
         		/* ENDING */	
         			
         		if (section.contains("cart")) {
             		url = "cart";
             		getAllProductsInCart(request);
             		getTotalPriceOfAllProductsInCart(request);
             		getCurrentOrderToAddProducts(request);
         		}
         		try {
             		if(request.getSession().getAttribute("logged") == null || !request.getSession().getAttribute("logged").equals("yes")){
             			url = "login";
         				request.setAttribute("message", "notLoggedToSeeCart");
             		} 
             	} catch (Exception e) {}
             	
 				url = "/" + url + ".jsp";
 				try {
 					this.getServletContext().getRequestDispatcher(url)
 							.forward(request, response);
 				} catch (Exception e) {
 					request.getRequestDispatcher("/index.jsp").forward(request,
 							response);
 					return;
 				}
 
 				
 			
     }
     
     
     
     /*    ----------------   */
 	/*    FUNCTIONS TO USE   */
 	/*    ----------------   */
 	
 	
     /* GET ALL PRODUCTS WHICH ARE IN CART  */
     public void getAllProductsInCart(HttpServletRequest request){
     	EntityManager em = emf.createEntityManager();
 		try{
 			int idUser = (Integer)request.getSession().getAttribute("userId");
 			results = em.createQuery("SELECT o FROM Ordproduct o WHERE o.ord.user.idUser="+idUser+" and o.ord.confirmed=0").getResultList();
 			em.close();
 			request.setAttribute("OrdproductsInCart", (List)results);
 		}
 		catch(Exception e){e.getMessage();}
     }
     
     
     /* GET ID OF CURRENT ORDER (NOT CONFIRMED) - PRODUCTS WILL BE ADDED TO THIS ONE
      * AFTER CONFIRMING, NEW ONE UNCONFIRMED WILL HAVE TO BE CREATED  */
     public void getCurrentOrderToAddProducts(HttpServletRequest request){
     	EntityManager em = emf.createEntityManager();
 		try{
 			int idUser = (Integer)request.getSession().getAttribute("userId");
 			results = em.createQuery("SELECT o.idOrd FROM Ord o WHERE o.user.idUser="+idUser+" and o.confirmed=0").getResultList();
 			
 			int idCurrentOrder = 0;
 			for(Object current: results){
 				idCurrentOrder = (Integer)current;
 			}
 			request.setAttribute("idCurrentOrder", idCurrentOrder);
 		}
 		catch(Exception e){e.getMessage();}
 		em.close();
     }
     
     
     /* GET TOTAL PRICE PRODUCTS WHICH ARE IN CART MULTIPLIED BY CHOSEN QUANTITY */
     public void getTotalPriceOfAllProductsInCart(HttpServletRequest request){
     	EntityManager em = emf.createEntityManager();
 		try{
 			int idUser = (Integer)request.getSession().getAttribute("userId");
 			results = em.createQuery("SELECT o FROM Ordproduct o WHERE o.ord.user.idUser="+idUser+" and o.ord.confirmed=0").getResultList();
 			em.close();
 			BigDecimal totalPrice = new BigDecimal(0);
 			for(Object current: results){
 				Ordproduct op = (Ordproduct)current;
 				BigDecimal quantity = new BigDecimal(op.getQuantity());
 				BigDecimal productPrice = op.getProduct().getPrice();
 				totalPrice = totalPrice.add(productPrice.multiply(quantity));
 				}
 			request.setAttribute("totalPriceOfProductsInCart", totalPrice);
 		}
 		catch(Exception e){e.getMessage();}
     }
     
 }
