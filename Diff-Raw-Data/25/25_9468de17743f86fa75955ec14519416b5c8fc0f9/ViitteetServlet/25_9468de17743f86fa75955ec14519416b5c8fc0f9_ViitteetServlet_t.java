 package ohtu.viitearto.servlets;
 
 import java.io.IOException;
import java.util.Collection;
 import java.util.HashMap;
 import java.util.TreeMap;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import ohtu.viitearto.Rekisteri;
 import ohtu.viitearto.Tietoturva;
 
 
 public class ViitteetServlet extends HttpServlet {
 
     private Tietoturva security = new Tietoturva();
     private Rekisteri rekisteri = new Rekisteri();
     private String lomakeTyyppi;
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");
  
         
         if (lomakeTyyppi != null) {
             
             TreeMap<String, String> lomakeTiedot = new TreeMap<String, String>();
             
             if (lomakeTyyppi.equals("book")) {
                 bookLomake(request, response, lomakeTiedot);
             } else if (lomakeTyyppi.equals("inproceedings")) {
                 inproceedingsLomake(request, response, lomakeTiedot);
             } else if (lomakeTyyppi.equals("article")) {
                 articleLomake(request, response, lomakeTiedot);
             }
             
             request.setAttribute("tiedot", lomakeTiedot);
             lomakeTyyppi = null;
         }
         
        request.setAttribute("errors", security.getVirheIlmoitukset());
         security.nollaaVirheet();
         
         request.setAttribute("viitteet", rekisteri.getViitteet()); // hakee viitteet tietokannasta
         
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("WEB-INF/views/viitteet.jsp");
         dispatcher.forward(request, response); // ohjaudutaan index.jsp-sivulle
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         lomakeTyyppi = request.getParameter("viiteTyyppi");
         
         response.sendRedirect(request.getRequestURI()); // POST-pyynnöt ohjataan doGetille
     }
     
     private void articleLomake(HttpServletRequest request, HttpServletResponse response, TreeMap<String, String> tiedot) throws ServletException, IOException {
         tiedot.put("title", "<font color=\"red\">*</font> Title: ");
         tiedot.put("author", "<font color=\"red\">*</font> Author: ");
         tiedot.put("publisher", "Publisher: ");
         tiedot.put("year", "Year: ");
         tiedot.put("address", "Address: ");
         tiedot.put("pages", "Pages: ");
         tiedot.put("journal", "Journal: ");
         tiedot.put("volume", "Volume: ");
         tiedot.put("number", "Number: ");
     }
 
     private void inproceedingsLomake(HttpServletRequest request, HttpServletResponse response, TreeMap<String, String> tiedot) throws ServletException, IOException {
         tiedot.put("title", "<font color=\"red\">*</font> Title: ");
         tiedot.put("author", "<font color=\"red\">*</font> Author: ");
         tiedot.put("publisher", "Publisher: ");
         tiedot.put("year", "Year: ");
         tiedot.put("address", "Address: ");
         tiedot.put("booktitle", "Booktitle: ");
         tiedot.put("pages", "Pages: ");
     }
 
     private void bookLomake(HttpServletRequest request, HttpServletResponse response, TreeMap<String, String> tiedot) throws ServletException, IOException {
         tiedot.put("title", "<font color=\"red\">*</font> Title: ");
         tiedot.put("author", "<font color=\"red\">*</font> Author: ");
         tiedot.put("year", "Year: ");
         tiedot.put("publisher", "Publisher: ");
         tiedot.put("address", "Address: ");
     }
 }
