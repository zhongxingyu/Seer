 package ia.datahub.rest;
 
 import ia.datahub.beans.Podracer;
 import ia.datahub.beans.Recall;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URLDecoder;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 
 /**
  * A REST end-point for serving up podracer details
  */
 public class RestGetPodracerServlet extends HttpServlet {
     private EntityManager em;
 
     public RestGetPodracerServlet(EntityManager em) {
         this.em = em;
     }
 
     /** Return podracer details in "Name: Value" format, with recalls listed at the end on multiple lines */
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         PrintWriter pw = resp.getWriter();
         String decodedUri = URLDecoder.decode(req.getRequestURI());
         String[] uriParts = decodedUri.split("/");
         if (uriParts.length > 2)
         {
             String podracerModel = uriParts[2];
             Podracer p = em.find(Podracer.class, podracerModel);
             if (null != p) {
                 pw.println("Model: " + p.model);
                 pw.println("Manufacturer: " + p.manufacturer);
                 pw.println("Class: " + p.craftClass);
                 pw.println("Max speed: " + p.maxSpeed);
                 pw.println("Recalls:");
                 for (Recall r : p.getRecalls())
                 {
                     pw.println(r.description);
                 }
             } else {
                 pw.println("Not found");
             }
         } else {
             for (Podracer p : em.createQuery("SELECT p FROM Podracer p", Podracer.class).getResultList())
             {
                 pw.println("<p><a href='/podracer/" + p.model + "'>" + p.model + "</a></p>");
             }
         }
     }
     
     /** Update podracer details based on "Name: Value" format */
     @Override
     protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String decodedUri = URLDecoder.decode(req.getRequestURI());
         String[] uriParts = decodedUri.split("/");
         if (uriParts.length > 2)
         {
             String podracerModel = uriParts[2];
             
             Podracer p = em.find(Podracer.class, podracerModel);
             String line;
             while (null != (line = req.getReader().readLine())) {
                 String[] nv = line.split(":");
                if (2 == nv.length && nv[0].equals("Maximum speed"))
                 {
                     em.getTransaction().begin();
                     p.maxSpeed = nv[1];
                     em.persist(p);
                     em.getTransaction().commit();
                 }
             }
         }
     }      
 
 }
 
 
