 package listener;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.naming.NamingException;
 import java.sql.SQLException;
 
 import db.StockSimDB;
 
 public class SessionListener implements HttpSessionListener {
 
     public void sessionCreated(HttpSessionEvent event) {
         HttpSession session = event.getSession();
         try {
            StockSim db = new StockSimDB();
             session.setAttribute("db", db);
         } catch (NamingException e) {
             session.getServletContext().
                 log("Could not create StockSimDB object: " + e.getMessage(), e);
         } catch (SQLException e) {
             session.getServletContext().
                 log("Could not create StockSimDB object: " + e.getMessage(), e);
         }
         return;
     }
 
     public void sessionDestroyed(HttpSessionEvent event) {
         HttpSession session = event.getSession();
         StockSimDB db = (StockSimDB) session.getAttribute("db");
         if (db != null) {
             db.disconnect();
             session.removeAttribute("db");
         }
         return;
     }
 }
