 package com.jin.tpdb;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
// <add1>
 //import javax.persistence.PersistenceContext;
// </add1>

 // modified below line. added the full "com.jin.tpdb" package to what was an import of just "entities"
 import com.jin.tpdb.entities.Pessoa;

 
 public class Search extends HttpServlet {
 	
 	//@PersistenceContext(unitName="jin")
 	//private EntityManager em;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		
 		response.setContentType("text/html");
 		
 		PrintWriter out = response.getWriter();
 		
 		String q = request.getParameter("Search");
 		
 		out.println("You searched for: " + q);
 		
 		//<add2>
 		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jin");
 		EntityManager em = factory.createEntityManager();		
 		Pessoa p = new Pessoa();
		p.setNome(q);
		p.setCargo("teste");
 		em.getTransaction().begin();
 		em.persist(p);
 		em.getTransaction().commit();
 		//removed "System" from line below
		out.println("<br />commited");
 		//</add2>
 		
 	}
 	
 }
 		
 		
 		
