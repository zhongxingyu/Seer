 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import arkisto.Ainesosa;
 import arkisto.Drinkkiresepti;
 import arkisto.Juomalaji;
 import arkisto.Rekisteri;
 import java.io.IOException;
 import java.util.ArrayList;
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
  * Lisää uuden drinkkireseptin tietokantaan.
  * 
  */
 public class LisaaDrinkkiServlet extends HttpServlet {
 
     /**
      * Tietokantaoperaatioita hoitava olio.
      */
     private Rekisteri rekisteri = new Rekisteri();
     
     /**
      * Ohjaa HTTP-pyynnön Lista-servletille.
      * 
      * @param request HTTP-pyyntö.
      * @param response HTTP-vastaus.
      * @see arkisto.ListaServlet
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("/Lista"); // ohjataan pyyntö Lista-servletille
         dispatcher.forward(request, response);
     }
     
     /**
      * Lisää uuden drinkkireseptin tietokantaan lomakkeessa annettujen tietojen avulla.
      * 
      * Kun kirjautunut käyttäjä menee "drinkkilista.jsp"-sivulle ja täyttää lomakkeen, jonka
      * avulla voi lisätä uuden reseptin, tämä metodi käsittelee lomakkeessa annetut tiedot.
      * Pakollisia tietoja ovat drinkkireseptin nimi, reseptiohjeet ja juomalaji. Jos syötteet
      * ovat oikean pituisia, haetaan lomakkeessa annetun juomalajin id:n perusteella tietty juomalaji.
      * Tämän jälkeen luodaan uusi Drinkkiresepti-olio, jolle asetetaan mihin juomalajiin se kuuluu ja
      * mitkä ainesosat reseptillä mahdollisesti on. Lopuksi resepti lisätään tietokantaan Rekisteri-olion
      * avulla ja ohjataan käyttäjä "drinkkilista.jsp"-sivulle.
      * 
      * Jos tietoja puuttui tai jokin muu asia meni pieleen, ei tehdä mitään ja ohjataan käyttäjä takaisin
      * samalle sivulle.
      * @param request HTTP-pyyntö.
      * @param response HTTP-vastaus.
      * @see arkisto.Rekisteri#lisaaDrinkki(arkisto.Drinkkiresepti) 
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         if (!onKirjautunut(request,response)) {
             return;
         }
         String nimi = request.getParameter("nimi"); // otetaan lomakkeesta drinkin nimi
         String kuvaus = request.getParameter("kuvaus"); // kuvaus
         String ohjeet = request.getParameter("ohjeet"); // ohjeet
         String[] ainesosa = request.getParameterValues("aines"); // drinkille merkityt ainesosat
         String lajinId = request.getParameter("lajinId");
         
         nimi = estaCrossSiteScripting(nimi);
         kuvaus = estaCrossSiteScripting(kuvaus);
         ohjeet = estaCrossSiteScripting(ohjeet);
        lajinId = estaCrossSiteScripting(lajinId);
         
         ArrayList<Ainesosa> lista = new ArrayList<Ainesosa>();
         if (ainesosa != null) {
             for (int i=0; i < ainesosa.length; ++i) {
                 if (ainesosa[i] != null) {
                 Ainesosa uusi = rekisteri.haeAinesosa(ainesosa[i]);
                 lista.add(uusi);
                 }
             }
         }
         
         if ((nimi.length() > 0 && nimi.length() <= 30) && (ohjeet.length() > 0 && ohjeet.length() <= 300) 
                 && lajinId != null && kuvaus.length() <= 300) { // jos drinkin nimi ja ohjeet löytyvät, tehdään uusi drinkki
             
            long juomalajinId = Long.parseLong(request.getParameter("lajinId"));
             // haetaan juomalaji, joka vastaa parametrina saatua lajin id:tä
             Juomalaji laji = rekisteri.haeJuomalaji(juomalajinId);
             Drinkkiresepti resepti = new Drinkkiresepti(nimi, kuvaus, ohjeet);
             resepti.setLaji(laji); // asetetaan mihin lajiin drinkki kuuluu
             resepti.setAinesosat(lista); // lisätään ainesosat drinkille
             rekisteri.lisaaDrinkki(resepti); // ja lisätään se tietokantaan, lopuksi
             
             request.getRequestDispatcher("/Lista").forward(request, response); // ohjataan pyyntö Lista-servletille -> doGet suoritetaan
         } else {
             request.getRequestDispatcher("/Lista").forward(request, response); // jos tietoja puuttuu, ohjataan samalle sivulle
             return;
         }
         
     }
     /**
      * Tarkistaa, että käyttäjä on kirjautunut sisään eli hänellä on istunto "meneillään".
      * @param request HTTP-pyyntö.
      * @param response HTTP-vastaus.
      * @return Tieto siitä onko kirjautunut vai ei.
      */
     private boolean onKirjautunut(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
         if (request.getSession().getAttribute("tunnus") == null) { // jos istunnon aikana ei ole kirjauduttu sisään
             response.sendRedirect(request.getContextPath()+"/Login"); // ohjataan takaisin kirjautumissivulle
             return false;
         } else
             return true;
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
