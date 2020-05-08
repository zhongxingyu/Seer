 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import arkisto.Arvostelu;
 import arkisto.Drinkkiresepti;
 import arkisto.Rekisteri;
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Kenny Heinonen
  */
 
 /**
  * Lisää uuden arvostelun tietylle drinkille tietokantaan.
  * 
  */
 public class LisaaArvosteluServlet extends HttpServlet {
     
     /**
      * Tietokantaoperaatioita hoitava olio.
      */
     private Rekisteri rekisteri = new Rekisteri();
 
     /**
      * Ohjaa käyttäjän takaisin tietyn drinkin sivulle.
      * @param request HTTP-pyyntö.
      * @param response HTTP-vastaus.
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("/DrinkinTiedot?id="+request.getParameter("drinkId"));
         dispatcher.forward(request, response);
     }
 
     /**
      * Ohjaa HTTP-pyynnön ja -vastauksen processRequest-metodille.
      * @param request HTTP-pyyntö.
      * @param response HTTP-vastaus.
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Lisää uuden arvostelun drinkille & tietokantaan lomakkeessa annettujen tietojen avulla.
      * 
      * Kun kirjautunut käyttäjä on generoidulla "tiedot.jsp"-sivulla täyttänyt lomakkeen, jolla
      * arvostellaan kyseisellä sivulla esiintyvää drinkkiä, tämä metodi käsittelee lomakkeessa
      * annetut tiedot. Lomakkeen mukana annetaan parametri "drinkId", joka kertoo mille drinkille
      * arvostelu on kirjoitettu. Jos syötteet menevät tarkistuksista läpi, luodaan uusi Arvostelu-luokan
      * ilmentymä ja lisätään se Rekisteri-olion avulla tietokantaan. 
      * 
      * Lopuksi ohjataan käyttäjä samalle
      * sivulle, jossa uusi arvostelu nyt näkyy. Jos tiedot ovat väärin, ei tehdä mitään ja palataan
      * samalle sivulle ja näytetään virheilmoitus.
      * @param request HTTP-pyyntö.
      * @param response HTTP-vastaus.
      * @see arkisto.Rekisteri#lisaaArvostelu(arkisto.Arvostelu) 
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         String nimiMerkki = (String) request.getSession().getAttribute("tunnus");
         String arvostelu = request.getParameter("arvostelu");
         String arvo = request.getParameter("arvo"); // arvosana
         
         long drinkinId = Long.parseLong(request.getParameter("drinkId")); // napataan drinkin id
         Drinkkiresepti drinkki = rekisteri.haeDrinkki(drinkinId); // haetaan drinkki, jolle arvostelu kirjoitettiin
         
         if ((nimiMerkki.length() > 0 && arvostelu.length() > 0 && arvostelu.length() <= 300) && arvo != null) {
             nimiMerkki = estaCrossSiteScripting(nimiMerkki);
             arvostelu = estaCrossSiteScripting(arvostelu);
             arvo = estaCrossSiteScripting(arvo);
             
             int arvosana = Integer.parseInt(arvo);
             
             if (arvosana < 1) // varmistetaan, että arvosana pysyy rajojen sisällä
                 arvosana = 1;
             else if (arvosana > 5)
                 arvosana = 5;
             
             Arvostelu uusi = new Arvostelu(arvosana, arvostelu, nimiMerkki);
             uusi.setResepti(drinkki);
             rekisteri.lisaaArvostelu(uusi);
             response.sendRedirect(request.getContextPath()+"/DrinkinTiedot?id="+drinkinId); // ohjataan samalle sivulle
         } else {
            request.setAttribute("virhe", "Tietoja puuttuu tai arvostelusi ylitti sallitun maksimipituuden!");
             request.getRequestDispatcher("/DrinkinTiedot?id="+drinkinId).forward(request, response);
             return;
         }
             
     }
     
     /**
      * Estää "Cross-site Scripting"-hyökkäykset, joita voi esiintyä lomakkeisiin
      * annetuissa syötteissä.
      * @param mjono Lomakkeessa annettu syöte.
      * @return Korjattu syöte.
      */
     private String estaCrossSiteScripting(String mjono) {
         mjono = mjono.replace("<", "&lt;");
         mjono = mjono.replace(">", "&gt;");
         return mjono;
     }
 }
