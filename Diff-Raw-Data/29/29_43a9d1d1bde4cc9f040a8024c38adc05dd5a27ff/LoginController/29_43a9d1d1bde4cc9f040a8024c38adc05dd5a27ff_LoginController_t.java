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
 
 public class LoginController extends HttpServlet {
 	
 
 	EntityManagerFactory emf = Persistence.createEntityManagerFactory("OnlineStore_WEB"); // CHANGE IF U HAVE DIFFERENT NAME!
 	private static final long serialVersionUID = 2L;
 	private boolean debug = true;
 	HttpSession session;
 	Query query;
 	List results;
 
 	
 
 	public LoginController() {
 		super();
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		if (request.getParameter("action") != null) {
 			if (request.getParameter("action").equals("logout")) {
 				session.invalidate(); // we destroy a session when logging out and redirecting to login page
 				response.sendRedirect("/OnlineStore/login?action=loggedout");
 				return;
 			}
 		}
 		String pathInfo = request.getServletPath();
 		String[] urlParsed = pathInfo.split("/");
 		String url = "index", section = "";
 		if (urlParsed.length >= 2)
 			section = urlParsed[1];
 		if (section.contains("login")) {
 			url = "login";
 		} else if (section.contains("registration")) {
 			url = "registration";
 			try {
 				if (session.getAttribute("user") != null)
 					url = "login";
 			} catch (Exception e) {
 			}
 		}
 		
 
 		if (request.getParameter("action") != null) {
 			if (request.getParameter("action").equals("edit")) {
 				url = "userEdit";
 			} 
 			else if (request.getParameter("action").equals("productEdit")) {
 				url = "productEdit";
 				getAllProductsForAdminEdit(request);
 				getProductForAdminEdit(request);
 				getCategoriesForAdminEdit(request);
 			} 
 			else if (request.getParameter("action").equals("productDelete")) {
 				url = "productDelete";
 				getProductForAdminEdit(request);
 			}
 			else if (request.getParameter("action").equals("categoryEdit")) {
 				url = "categoryEdit";
 				getCategoriesForAdminEdit(request);
 				getCategoryForAdminEdit(request);
 				getSectionsForAdminEdit(request);
 			}
 			else if (request.getParameter("action").equals("categoryAdd")) {
 				url = "categoryAdd";
 				getCategoriesForAdminEdit(request);
 				getSectionsForAdminEdit(request);
 			}
 			else if (request.getParameter("action").equals("categoryDelete")) {
 				url = "categoryDelete";
 				getCategoryForAdminEdit(request);
 				getNumberOfProductsForAdminEdit(request);
 			}
 			else if (request.getParameter("action").equals("productAdd")) {
 				url = "productAdd";
 				getCategoriesForAdminEdit(request);
 			}
 			else if (request.getParameter("action").equals("usersEdit")) {
 				url = "usersEdit";
 				getUsersForAdminEdit(request);
 			}
 
 			else if (request.getParameter("action").equals("userDelete")) {
 				url = "userDelete";
 				getUserForAdminEdit(request);
 			}
 		}
 		
 		
 
 		url = "/" + url + ".jsp";
 		try {
 			this.getServletContext().getRequestDispatcher(url)
 					.forward(request, response);
 		} catch (Exception e) {
 			if (debug)
 				System.out.println("Exception: " + e.getMessage());
 			request.getRequestDispatcher("/index.jsp").forward(request,
 					response);
 			return;
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String pathInfo = request.getServletPath();
 		String[] urlParsed = pathInfo.split("/");
 		String url = "index", section = "";
 		String mail = "";
 		String action = "";
 		String password = "";
 
 		EntityManager em = emf.createEntityManager();
 		
 		boolean isLoginSession = false;
 		if (urlParsed.length >= 2)
 			section = urlParsed[1];
 
 		if (section.equals("login")) {
 			url = "login";
 
 			try {
 				if (session.getAttribute("logged").equals("yes")) {
 					isLoginSession = true;
 				}
 			} catch (Exception e) {
 			}
 
 			/* CREATING LOGIN SESSION AND SENDING MESSAGES */
 			if (!isLoginSession) {
 				mail = request.getParameter("mail");
 				password = request.getParameter("password");
 				request.setAttribute("message", "wrongData");
 				try {
 					query = em
 							.createQuery("SELECT u FROM User u WHERE u.email='"
 									+ mail + "' AND u.password='" + password
 									+ "'");
 					List u = query.getResultList();
 					if (u != null) {
 						for (Object user : u) {
 							session = request.getSession();
 							session.setAttribute("user", (User)user);
 							session.setAttribute("login",
 									((User)user).getEmail());
 							session.setAttribute("logged", "yes");
 							session.setAttribute("userId", ((User)user).getIdUser());
 							if(((User)user).getAdminRights() != 0){
 								session.setAttribute("loggedAdmin", "yes");	
 							}
 							request.setAttribute("message", "okData");
 						}
 					}
 					em.close();
 				} catch (Exception e) {
 				}
 
 			}
 
 			/* SAVING EDITED USER DATA */
 			if (request.getParameter("actionPost") != null) {
 				if (request.getParameter("actionPost").equals("save")) {
 					String name = request.getParameter("name");
 					String surname = request.getParameter("surname");
 					String newEmail = request.getParameter("newEmail");
 					String phone = request.getParameter("phone");
 					String address = request.getParameter("address");
 					String password2 = request.getParameter("password2");
 					String password3 = request.getParameter("password3");
 
 					EntityTransaction et = em.getTransaction();
 					et.begin();
 					String currentEmail = "";
 					try {
 						currentEmail = (String) session.getAttribute("login");
 					} catch (Exception e) {
 					}
 
 					Query saveUser = em
 							.createQuery("SELECT u FROM User u WHERE u.email='"
 									+ currentEmail + "'");
 					results = saveUser.getResultList();
 					for (Object us : results) {
 						User u = (User) us;
 						u.setName(name);
 						u.setSurname(surname);
 						u.setEmail(newEmail);
 						u.setPhone(phone);
 						u.setAddress(address);
 						if (password2 != null && password3 != null
 								&& !password2.equals("")
 								&& !password3.equals("")
 								&& password2.equals(password3)
 								&& password3.equals(password2)) {
 							u.setPassword(password2);
 						}
 						session.setAttribute("user", (User) u);
 						et.commit();
 						em.close();
 					}
 				}
 				
 				/* SAVING EDITED PRODUCT */
 				if (request.getParameter("actionPost") != null) {
 					if (request.getParameter("actionPost").equals("saveProduct")) {
 						String name = request.getParameter("name");
 						String price = request.getParameter("price");
 						String stock = request.getParameter("stock");
 						String description = request.getParameter("description");
 						String categoryId = request.getParameter("category");
 						String imageUrl = request.getParameter("imageUrl");
 						String highlighted = request.getParameter("highlighted");
 						if(highlighted == null || highlighted.equals("")){
 							highlighted = "no";
 						}
 						Category category = null;
 
 						em = emf.createEntityManager();
 						query = em.createQuery("SELECT c FROM Category c WHERE c.idCategory="+categoryId);
 						results = query.getResultList();
 						if (results == null) {
 							request.setAttribute("categories", "");
 						} else {
 							for(Object pr: results){
 								category = (Category)pr;
 							}
 						}
 						em.close();
 						em = emf.createEntityManager();
 						
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						Query saveProduct = em
 								.createQuery("SELECT p FROM Product p WHERE p.idProduct="
 										+ request.getParameter("id"));
 						results = saveProduct.getResultList();
 						for (Object pr : results) {
 							Product p = (Product) pr;
 							p.setName(name);
 							BigDecimal price2 = new BigDecimal(price);
 							p.setPrice(price2);
 							p.setStock(Integer.parseInt(stock));
 							p.setCategory(category);
 							p.setDescription(description);
 							p.setImageUrl(imageUrl);
 							if(highlighted.equals("yes")){
 								p.setHighlighted((byte)1);
 							} else {
 								p.setHighlighted((byte)0);
 							}
 							et.commit();
 							em.close();
 						}
 					}
 				}
 				
 				
 				
 				
 				/* SAVING EDITED CATEGORY */
 				if (request.getParameter("actionPost") != null) {
 					if (request.getParameter("actionPost").equals("categoryEdit")) {
 						String name = request.getParameter("name");
 						String sectionId = request.getParameter("section");
 						Section sect = null;
 
 						em = emf.createEntityManager();
 						query = em.createQuery("SELECT s FROM Section s WHERE s.idSection="+sectionId);
 						results = query.getResultList();
 						if (results == null) {
 							request.setAttribute("categories", "");
 						} else {
 							for(Object current: results){
 								sect = (Section)current;
 							}
 						}
 						em.close();
 						em = emf.createEntityManager();
 						
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						Query categoryEdit = em
 								.createQuery("SELECT c FROM Category c WHERE c.idCategory="
 										+ request.getParameter("id"));
 						results = categoryEdit.getResultList();
 						for (Object current : results) {
 							Category c = (Category)current;
 							c.setName(name);
 							c.setSection(sect);
 							
 							et.commit();
 							em.close();
 						}
 					}
 				}
 				
 				
 				
 				/* CREATING NEW PRODUCT */
 				if (request.getParameter("actionPost") != null) {
 					if (request.getParameter("actionPost").equals("productAdd")) {
 						String name = request.getParameter("name");
 						String price = request.getParameter("price");
 						String stock = request.getParameter("stock");
 						String description = request.getParameter("description");
 						String categoryId = request.getParameter("category");
 						String imageUrl = request.getParameter("imageUrl");
 						String highlighted = request.getParameter("highlighted");
 						if(highlighted == null || highlighted.equals("")){
 							highlighted = "no";
 						}
 						Category category = null;
 
 						em = emf.createEntityManager();
 						query = em.createQuery("SELECT c FROM Category c WHERE c.idCategory="+categoryId);
 						results = query.getResultList();
 						if (results == null) {
 							request.setAttribute("categories", "");
 						} else {
 							for(Object pr: results){
 								category = (Category)pr;
 							}
 						}
 						em.close();
 						em = emf.createEntityManager();
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						
 						Query maxId = em
 								.createQuery("SELECT MAX(p.idProduct) FROM Product p");
 						results = maxId.getResultList();
 						int newId = 0;
 						Iterator it = results.iterator();
 						while (it.hasNext()) {
 							newId = ((Integer) it.next()).intValue();
 						}
 
 						Product p = new Product();
 						p.setIdProduct(newId + 1);
 						p.setName(name);
 						BigDecimal price2 = new BigDecimal(price);
 						p.setPrice(price2);
 						p.setStock(Integer.parseInt(stock));
 						p.setCategory(category);
 						p.setDescription(description);
 						p.setImageUrl(imageUrl);
 						if(highlighted.equals("yes")){
 							p.setHighlighted((byte)1);
 						} else {
 							p.setHighlighted((byte)0);
 						}
 						
 						em.persist(p);
 						et.commit();
 						em.close();
 						url = "login";
 					}
 				}
 				
 				
 				
 				
 				/* CREATING NEW CATEGORY */
 				if (request.getParameter("actionPost") != null) {
 					if (request.getParameter("actionPost").equals("categoryAdd")) {
 						String name = request.getParameter("name");
 						String sectionId = request.getParameter("section");
 						Section sect = null;
 
 						em = emf.createEntityManager();
 						query = em.createQuery("SELECT s FROM Section s WHERE s.idSection="+sectionId);
 						results = query.getResultList();
 						
 							for(Object current: results){
 								sect = (Section)current;
 							}
 						
 						em.close();
 						em = emf.createEntityManager();
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						
 						Query maxId = em
 								.createQuery("SELECT MAX(c.idCategory) FROM Category c");
 						results = maxId.getResultList();
 						int newId = 0;
 						Iterator it = results.iterator();
 						while (it.hasNext()) {
 							newId = ((Integer) it.next()).intValue();
 						}
 
 						Category c = new Category();
 						c.setIdCategory(newId + 1);
 						c.setName(name);
 						c.setSection(sect);
 						
 						em.persist(c);
 						et.commit();
 						em.close();
 						url = "login";
 					}
 				}
 				
 				
 				
 
 				/* CREATING NEW USER  */
 				if (request.getParameter("actionPost") != null) {
 					if (request.getParameter("actionPost").equals("newUser")) {
 
 						em = emf.createEntityManager();
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						String name = request.getParameter("name");
 						String surname = request.getParameter("surname");
 						String newEmail = request.getParameter("newEmail");
 						String phone = request.getParameter("phone");
 						String address = request.getParameter("address");
 						String password2 = request.getParameter("password2");
 						String password3 = request.getParameter("password3");
 						User u = null;
 						
 						Query maxId = em
 								.createQuery("SELECT MAX(u.idUser) FROM User u");
 						results = maxId.getResultList();
 						int newId = 0;
 						Iterator it = results.iterator();
 						while (it.hasNext()) {
 							newId = ((Integer) it.next()).intValue();
 						}
 						
 						
 						Query checkEmail = em
 						.createQuery("SELECT u.email FROM User u");
 						results = checkEmail.getResultList();
 						it = results.iterator();
 						boolean duplicatedEmail = false;
 						while (it.hasNext()) {
 							if(newEmail.equals((String)it.next())){
 								request.setAttribute("message","duplicatedEmail");
 								duplicatedEmail = true;
 								
 							}
 						}
 						if(!duplicatedEmail){
 							u = new User();
 							u.setIdUser(newId + 1);
 							u.setName(name);
 							u.setSurname(surname);
 							u.setEmail(newEmail);
 							u.setPhone(phone);
 							u.setAddress(address);
 
 							if (password2 != null && password3 != null
 									&& !password2.equals("")
 									&& !password3.equals("")
 									&& password2.equals(password3)
 									&& password3.equals(password2)) {
 								u.setPassword(password2);
 							}
 							em.persist(u);
 							request.setAttribute("message", "okData");
 						}
 						
 						
 						
 						
 						/* WHILE CREATING NEW USER WE HAVE TO CREATE AN UNCOMFIRMED ORDER FOR HIM 
 						 * TO MAKE HIM ABLE TO ADD PRODUCT TO CART AFTER LOGGING */
 						if(!duplicatedEmail){
 							maxId = em.createQuery("SELECT MAX(o.idOrd) FROM Ord o");
 							results = maxId.getResultList();
 							newId = 0;
 							it = results.iterator();
 							while (it.hasNext()) {
 								newId = ((Integer) it.next()).intValue();
 							}
 							Ord o = new Ord();
 							o.setIdOrd(newId +1);
 							o.setTime(new Date());
 							o.setUser(u);
 							em.persist(o);
 						}
 						
 				
 						et.commit();
 						em.close();
 						url = "login";
 					}
 				}
 				
 				
 				
 				
 				/* DELETING A PRODUCT */
 				if (request.getParameter("actionPost") != null) {
 					if (request.getParameter("actionPost").equals("deleteProduct")) {
 
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						Query deleteProduct = em
 						.createQuery("SELECT p FROM Product p WHERE p.idProduct="
 								+ request.getParameter("id"));
 						results = deleteProduct.getResultList();
 						for (Object pr : results) {
 							Product p = (Product) pr;
 							em.remove(p);
 						}
 						et.commit();
 						em.close();
 					}
 				}
 				
 				
 				
 				/* DELETING A CATEGORY */
 				if (request.getParameter("action") != null) {
 					if (request.getParameter("actionPost").equals("deleteCategory")) {
 
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						Query deleteCategory = em
 						.createQuery("SELECT c FROM Category c WHERE c.idCategory="
 								+ request.getParameter("id"));
 						results = deleteCategory.getResultList();
 						for (Object cat : results) {
 							Category c = (Category) cat;
 							em.remove(c);
 						}
 						et.commit();
 						em.close();
 					}
 				}
 				
 				
 				
 				/* DELETING AN USER */
 				if (request.getParameter("action") != null) {
 					if (request.getParameter("actionPost").equals("userDelete")) {
 
 						EntityTransaction et = em.getTransaction();
 						et.begin();
 						Query deleteUser = em
 						.createQuery("SELECT u FROM User u WHERE u.idUser="
 								+ request.getParameter("id"));
 						results = deleteUser.getResultList();
 						for (Object current : results) {
 							User u = (User) current;
							/* TO CHANGE ALL ORDERS OF THE USER TO USER OF ID 1 (user name: USER DELETED) */
							User deleted = getUser(1);
							for(Ord o: u.getOrds()){
								o.setUser(deleted);
							}
 							em.remove(u);
 						}
 						et.commit();
 						em.close();
 					}
 				}
 				
 				
 				
 			}
 		}
 		
 		url = "/" + url + ".jsp";
 		try {
 			this.getServletContext().getRequestDispatcher(url)
 					.forward(request, response);
 		} catch (Exception e) {
 			if (debug)
 				System.out.println("Exception: " + e.getMessage());
 			request.getRequestDispatcher("/index.jsp").forward(request,
 					response);
 			return;
 		}
 
 	}
 	
 
 	
 	/*    ----------------   */
 	/*    FUNCTIONS TO USE   */
 	/*    ----------------   */
 	
 	
 	
 	/* GET ALL PRODUCTS (FOR ADMIN EDIT) */
 	public void getAllProductsForAdminEdit(HttpServletRequest request) {
 		EntityManager em = emf.createEntityManager();
 		query = em.createQuery("SELECT p FROM Product p ORDER BY p.name");
 				results = query.getResultList();
 				if (results == null) {
 					request.setAttribute("editProductList", "");
 				} else {
 					request.setAttribute("editProductList", results);
 				}
 				em.close();
 	}
 	
 	/* GET PRODUCT TO EDIT (FOR ADMIN) */
 	public void getProductForAdminEdit(HttpServletRequest request) {
 		if (request.getParameter("id") != null) {
 			int idProductForAdminEdit = Integer.parseInt(request.getParameter("id"));
 			EntityManager em = emf.createEntityManager();
 			query = em.createQuery("SELECT p FROM Product p WHERE p.idProduct="+ idProductForAdminEdit);
 			results = query.getResultList();
 			if (results == null) {
 				request.setAttribute("product", "");
 			} else {
 				for (Object prod : results) {
 					request.setAttribute("product", (Product) prod);
 				}
 			}
 			em.close();
 		}
 	}
 	
 	
 	
 	/* GET CATEOGORIES (FOR ADMIN PRODUCT EDIT) */
 	public void getCategoriesForAdminEdit(HttpServletRequest request) {
 		    EntityManager em = emf.createEntityManager();
 			query = em.createQuery("SELECT c FROM Category c ORDER BY c.name");
 			results = query.getResultList();
 			if (results == null) {
 				request.setAttribute("categories", "");
 			} else {
 				request.setAttribute("categories", results);
 			}
 			em.close();
 		}
 	
 	
 	/* GET SECTIONS (FOR ADMIN CATEOGRY EDIT) */
 	public void getSectionsForAdminEdit(HttpServletRequest request) {
 		    EntityManager em = emf.createEntityManager();
 			query = em.createQuery("SELECT s FROM Section s ORDER BY s.name");
 			results = query.getResultList();
 			if (results == null) {
 				request.setAttribute("sections", "");
 			} else {
 				request.setAttribute("sections", results);
 			}
 			em.close();
 		}
 	
 	
 	
 	/* GET USERS (FOR ADMIN CATEOGRY EDIT) */
 	public void getUsersForAdminEdit(HttpServletRequest request) {
 		    EntityManager em = emf.createEntityManager();
		    /* USER id=1 IS CALLED "DELETED USER" SO WE DON'T DISPLAY HIM */
			query = em.createQuery("SELECT u FROM User u WHERE u.idUser NOT IN (1) ORDER BY u.email");
 			results = query.getResultList();
 			if (results == null) {
 				request.setAttribute("users", "");
 			} else {
 				request.setAttribute("users", results);
 			}
 			em.close();
 		}
 	
 	
 	
 	
 	/* GET CATEGORY TO EDIT (FOR ADMIN) */
 	public void getCategoryForAdminEdit(HttpServletRequest request) {
 		if (request.getParameter("id") != null) {
 			int idCategoryForAdminEdit = Integer.parseInt(request.getParameter("id"));
 			EntityManager em = emf.createEntityManager();
 			query = em.createQuery("SELECT c FROM Category c WHERE c.idCategory="+ idCategoryForAdminEdit);
 			results = query.getResultList();
 			if (results == null) {
 				request.setAttribute("category", "");
 			} else {
 				for (Object cat : results) {
 					request.setAttribute("category", (Category) cat);
 				}
 			}
 			em.close();
 		}
 	}
 	
 	
 	
	/* GET USER (FOR ADMIN) */
 	public void getUserForAdminEdit(HttpServletRequest request) {
 		if (request.getParameter("id") != null) {
 			int idUserForAdminEdit = Integer.parseInt(request.getParameter("id"));
 			EntityManager em = emf.createEntityManager();
 			query = em.createQuery("SELECT u FROM User u WHERE u.idUser="+ idUserForAdminEdit);
 			results = query.getResultList();
 			if (results == null) {
 				request.setAttribute("user", "");
 			} else {
 				for (Object current : results) {
 					request.setAttribute("user", (User) current);
 				}
 			}
 			em.close();
 		}
 	}
 	
 	
	/* GET USER WITH ID */
	public User getUser(int id) {
			EntityManager em = emf.createEntityManager();
			query = em.createQuery("SELECT u FROM User u WHERE u.idUser="+ id);
			results = query.getResultList();
			User u = null;
			if (results != null) {
				for (Object current : results) {
					u = (User)current;
				}
			em.close();
			}
			return u;
	}
	
	
 	/* GET NUMBER OF PRODUCTS IN CATEGORY TO EDIT (FOR ADMIN) */
 	public void getNumberOfProductsForAdminEdit(HttpServletRequest request) {
 		if (request.getParameter("id") != null) {
 			int idCategoryForAdminEdit = Integer.parseInt(request.getParameter("id"));
 			EntityManager em = emf.createEntityManager();
 			query = em.createQuery("SELECT p FROM Product p WHERE p.category.idCategory="+ idCategoryForAdminEdit);
 			results = query.getResultList();
 			request.setAttribute("numberOfProducts", results.size());
 			em.close();
 		}
 	}
 	
 }
