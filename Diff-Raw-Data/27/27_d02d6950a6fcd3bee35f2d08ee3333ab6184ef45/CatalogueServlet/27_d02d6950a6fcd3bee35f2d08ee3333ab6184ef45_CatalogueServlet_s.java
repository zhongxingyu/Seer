 package j2ee.association.modele.servlets;
 
 import j2ee.association.bean.Article;
 import j2ee.association.persistence.PersistenceServiceProvider;
 import j2ee.association.persistence.services.ArticlePersistence;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class CatalogueServlet
  */
 public class CatalogueServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
	private Map<String, Integer> command = new HashMap<>();
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public CatalogueServlet() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Fetching all articles in the data base, storing them in a list of beans
 		ArticlePersistence service = PersistenceServiceProvider.getService(ArticlePersistence.class);
 		List<Article> articles = service.loadAll();
 		// Passing the list of beans to the jsp via the request
 		request.setAttribute("articles", articles);
 		// Forwarding to the jsp with the beans added
 		this.getServletContext().getRequestDispatcher("/jsp/catalogue.jsp").forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		if (checkParameter(request)){
 			request.setAttribute("command", command);
 			response.sendRedirect(request.getContextPath()+"/catalogue");
 		} else {
 			System.out.println("erreur interne");
 		}
 	}
 
 	/**
 	 * Checks all POST parameters to determine whether it has been ordered or not
 	 * @param request the HTTP request pushed by the user
 	 */
 	private boolean checkParameter(HttpServletRequest request) {
 		Enumeration<String> attributeName = request.getAttributeNames();
 		while (attributeName.hasMoreElements()) {
 			String string = (String) attributeName.nextElement();
 			int commandNumber = Integer.parseInt(request.getParameter(string));
 			if (commandNumber >= 0) {
				boolean retour = getProduct(string, commandNumber);
 				if (!retour) {
 					return retour;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * For the article <code>code</code>, checks in database if the quantity <code>quantity</code> is available.
 	 * If so, it pulls <code>quantity</code> articles out of the database
 	 * @param code The code of the product to order
 	 * @param quantity The quantity willingly to be ordered
 	 * @return <code>true</code> if the order was successfull, <code>false if not</code>
 	 */
	private boolean getProduct(String code, int quantity) {
 		Map<String, Object> name = new HashMap<String, Object>();
 		name.put("arCode", code);
 		ArticlePersistence persistence = PersistenceServiceProvider.getService(ArticlePersistence.class);
 		List<Article> informations = persistence.search(name);
 		if (informations.size() != 1) {
 			return false;
 		} else {
			checkQuantity(quantity, informations.get(0), persistence);
 			return true;
 		}
 	}
 
	private void checkQuantity(int quantity, Article article, ArticlePersistence persistence) {
 		if (article.getArStock() < quantity) {
			command.put(article.getArCode(), -quantity);
 		} else {
 			command.put(article.getArCode(), quantity);
 		}
 	}
 	
 	
 
 }
