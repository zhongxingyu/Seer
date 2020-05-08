 package ca.etsmtl.log660.servlets;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import ca.etsmtl.log660.configuration.SessionFactoryHelper;
 import ca.etsmtl.log660.entity.Film;
 import ca.etsmtl.log660.entity.Genre;
 import ca.etsmtl.log660.entity.Langue;
 import ca.etsmtl.log660.entity.Pays;
 import ca.etsmtl.log660.entity.Personne;
 import ca.etsmtl.log660.entity.Role;
 import ca.etsmtl.log660.entity.Scenariste;
 
 /**
  * Servlet implementation class TestServlet
  */
 public class FilmResearch extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Default constructor.
 	 */
 	public FilmResearch() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Session session = SessionFactoryHelper.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		String titre = request.getParameter("titre");
 		if(titre != null && titre.length() == 0)
 		{
 			titre = null;
 		}
 		
 		String strAnnee = request.getParameter("anneeMin");
 		int anneeMin = 1900;
 		int anneeMax = 2100;
 		if(strAnnee != null)
 		{
 			anneeMin = Integer.parseInt(strAnnee);
 		}
 		
 		strAnnee = request.getParameter("anneeMax");
 		if(strAnnee != null)
 		{
 			anneeMax = Integer.parseInt(strAnnee);
 		}
 
 		String pays = request.getParameter("pays");
 		
 		if(pays != null && pays.length() == 0)
 		{
 			pays = null;
 		}
 		
 		String langue = request.getParameter("langue");
 		if(langue != null && langue.length() == 0)
 		{
 			langue = null;
 		}
 		String genre = request.getParameter("genre");
 		if(genre != null && genre.length() == 0)
 		{
 			genre = null;
 		}
 		String scenariste = request.getParameter("scenariste");
 		if(scenariste != null && scenariste.length() == 0)
 		{
 			scenariste = null;
 		}
 		String realisateur = request.getParameter("realisateur");
 		if(realisateur != null && realisateur.length() == 0)
 		{
 			realisateur = null;
 		}
 		String acteur = request.getParameter("acteur");
 		if(acteur != null && acteur.length() == 0)
 		{
 			acteur = null;
 		}
 		//y reste juste le critre du scnariste  ajouter
 		Query query = session.createQuery(
 			"FROM Film f" +
 			" WHERE ( f.titre LIKE CONCAT('%', :titre, '%')  OR :titre IS NULL) AND" +
 			" ( f.annee BETWEEN :anneeMin AND :anneeMax ) AND" +
 			" ( :langue IS NULL OR :langue = f.langue.langue) AND"+
 			" ( :pays IS NULL OR :pays IN( SELECT p.nomPays FROM f.pays p )) AND"+
 			" ( :acteur IS NULL OR :acteur IN( SELECT r.personne.nom FROM f.roles r )) AND"+
 			" ( :realisateur IS NULL OR :realisateur = f.realisateur.nom ) AND" +
			" ( :genre IS NULL OR :genre IN( SELECT g.genre FROM f.genres g ))"
 		).setParameter("titre", titre)
 		.setParameter("anneeMin", anneeMin)
 		.setParameter("anneeMax", anneeMax)
 		.setParameter("pays", pays)
 		.setParameter("langue", langue)
 		.setParameter("genre", genre)
 		.setParameter("realisateur", realisateur)
		.setParameter("acteur", acteur);
 
 		query.setMaxResults(50);
 		
 		List<Film> films = query.list();
 		
 		request.setAttribute("films", films);
 		//sending films as a attribute
 		/*for(Film f : films){
 			response.getWriter().println(f.getTitre());
 		}*/
 		
 		//session.close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 
 }
