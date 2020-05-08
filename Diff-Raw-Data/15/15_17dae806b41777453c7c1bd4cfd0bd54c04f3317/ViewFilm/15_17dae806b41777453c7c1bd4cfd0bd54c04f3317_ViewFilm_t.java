 package ca.etsmtl.log660.servlets;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import ca.etsmtl.log660.configuration.SessionFactoryHelper;
 import ca.etsmtl.log660.entity.Film;
 
 /**
  * Servlet implementation class ViewFilm
  */
 public class ViewFilm extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ViewFilm() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if(request.getSession().getAttribute("id") == null)
 		{
 			//not logged, need to redirect to index.jsp
 			return;
 		}
 		
 		String filmId = request.getParameter("filmid");
 		
 		if(filmId == null)
 		{
 			//error page or redirect to search
 			return;
 		}
 		else
 		{
 			Session session = SessionFactoryHelper.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 			
 			int id = Integer.parseInt(filmId);
 			
 			Query query = session.createQuery(
 					//"FROM Film WHERE " +
 					//"titre LIKE CONCAT('%', :titre, '%') AND " +
 					//"annee BETWEEN :anneeMin AND :anneeMax"
 					"FROM Film f WHERE f.id = :id"
 				);
 			
 			query.setParameter("id", id);
 			
 			Film f = (Film)query.uniqueResult();
 			
 			request.setAttribute("film", f);
 			
 
 			//session.close();
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
