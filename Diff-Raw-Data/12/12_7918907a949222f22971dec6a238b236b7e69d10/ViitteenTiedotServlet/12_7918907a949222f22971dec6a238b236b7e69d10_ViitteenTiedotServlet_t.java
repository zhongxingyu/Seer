 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.viitearto.servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeMap;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import ohtu.viitearto.Rekisteri;
 import ohtu.viitearto.Viite;
 
 /**
  *
  * @author kennyhei
  */
 public class ViitteenTiedotServlet extends HttpServlet {
 
     private Rekisteri rekisteri = Rekisteri.getInstance();
     private Viite muokattava;
    private boolean muokataanko = false;
     private TreeMap<String, String> muokkausTiedot = new TreeMap<String, String>();
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");
 
        if (!muokataanko) {
             long id = Long.parseLong(request.getParameter("id"));
 
             Viite viite = rekisteri.haeViite(id);
             
             request.setAttribute("tiedot", viite.getTiedot());
             request.setAttribute("id", viite.getId());
             request.setAttribute("type", viite.getType());
             request.setAttribute("tagit", viite.getTagit());
         } else {
             maaritaViitteenTyyppi(muokattava);
             request.setAttribute("mtiedot", muokkausTiedot);
            muokataanko = false;
         }
         
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("WEB-INF/views/tiedot.jsp");
         dispatcher.forward(request, response);
         
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");
         
         long id = Long.parseLong(request.getParameter("id"));
         muokattava = rekisteri.haeViite(id);
        muokataanko = true;
        System.out.println(request.getRequestURI().toString()+" AHAOEGIHAEO");
        response.sendRedirect(request.getRequestURI()+"?id="+muokattava.getId()); // POST-pyynnöt ohjataan doGetille
     }
 
     private void maaritaViitteenTyyppi(Viite muokattava) {
         if (muokattava.getType().equals("Book"))
             muokkaaBook();
         else if (muokattava.getType().equals("Inproceedings"))
             muokkaaInproceedings();
         else
             muokkaaArticle();
     }
 
     private void muokkaaArticle() {
         muokkausTiedot = Viite.getArticleKentat();
     }
 
     private void muokkaaInproceedings() {
         muokkausTiedot = Viite.getInproceedingsKentat();
     }
 
     private void muokkaaBook() {
         muokkausTiedot = Viite.getBookKentat();
     }
 }
