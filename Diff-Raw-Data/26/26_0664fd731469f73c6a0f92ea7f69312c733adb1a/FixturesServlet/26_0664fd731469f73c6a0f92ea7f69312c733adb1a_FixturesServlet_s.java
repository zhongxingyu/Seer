 package it.polimi.chansonnier.servlet;
 
 import it.polimi.chansonnier.fixtures.Fixtures;
 import it.polimi.chansonnier.utils.FixtureManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class FixturesServlet extends HttpServlet {
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter writer = response.getWriter();
         writer.println("<form action=\"fixtures\" method=\"post\">");
         writer.println("<button type=\"submit\">Load fixtures</button>");
         writer.println("</form>");
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			FixtureManager fixtureManager = Activator.getFixtureManager("AddPipeline");
 			InputStream heroFlv = Fixtures.class.getResourceAsStream("hero.flv");
 			fixtureManager.addSong("http://www.youtube.com/watch?v=owTmJrtD7g8", heroFlv, "Enrique Iglesias- Hero (with lyrics)");
 			InputStream haloFlv = Fixtures.class.getResourceAsStream("halo.flv");
 			fixtureManager.addSong("http://www.youtube.com/watch?v=fSdgBse1o7Q", haloFlv, "Beyonce-Halo Lyrics");
			fixtureManager.commit();
 	        response.getWriter().println("<p>Fixtures added. Wait some time for indexing.</p>");
 		} catch (Exception e) {
 			throw new ServletException(e);
 		}
 	}
 }
