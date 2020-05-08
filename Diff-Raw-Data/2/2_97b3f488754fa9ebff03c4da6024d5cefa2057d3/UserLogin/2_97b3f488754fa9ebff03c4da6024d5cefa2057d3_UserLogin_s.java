 package ca.etsmtl.log660.servlets;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import ca.etsmtl.log660.configuration.SessionFactoryHelper;
 import ca.etsmtl.log660.entity.Intervenant;
 
 /**
  * Servlet implementation class TestServlet
  */
 public class UserLogin extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Default constructor.
 	 */
 	public UserLogin() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 	
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		Session session = SessionFactoryHelper.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 				
 		String courriel = request.getParameter("courriel");
 		String mdp = request.getParameter("mdp");
 		
 		Query requete = session.createQuery("FROM Intervenant i WHERE i.courriel = :courriel AND i.mdp = :mdp");
 		requete.setParameter("courriel", courriel);
 		requete.setParameter("mdp", mdp);
 		
 		Intervenant intervenant = (Intervenant) requete.uniqueResult();
 				
 		if(intervenant == null){
 			response.sendRedirect("/lab02/index.jsp?erreurMSG=true");
			
 		}else{
 			request.getSession().setAttribute("id", intervenant.getId());
 			request.getSession().setAttribute("courriel", intervenant.getCourriel());
 			request.getSession().setAttribute("prenom", intervenant.getPrenom());
 			request.getSession().setAttribute("nom", intervenant.getNom());
 			
 			response.sendRedirect("/lab02/research.jsp");
 		}
 				
 		session.close();
 		
 	}
 
 }
