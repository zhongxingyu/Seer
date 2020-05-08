 package com.jin.tpdb;
 import com.jin.tpdb.entities.Pessoa;
 import com.jin.tpdb.entities.Artist;
import com.jin.tpdb.entities.Album;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 public class Search extends HttpServlet {
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		
 		response.setContentType("text/html");
 		
 		PrintWriter out = response.getWriter();
 		
 		String q = request.getParameter("Search");
 		
 		out.println("You searched for: " + q);
 		
 		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jin");
 		EntityManager em = factory.createEntityManager();		
 		/*Pessoa p = new Pessoa();
 		p.setNome(q);
 		p.setCargo("teste");
 		em.getTransaction().begin();
 		em.persist(p);
 		em.getTransaction().commit();
 		out.println("<br />commited");		*/
 		
 		Artist artist = em.find(Artist.class, 1);
 		
 		out.println("Artist is: " + artist.getName());
 		out.println("Artist's site is: " + artist.getSite());
 		
 		
 	}	
 	
 	/*public Artist getArtist(Long id) {
 		
 	}*/
 }
 		
 		
 		
