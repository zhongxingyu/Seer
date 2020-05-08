 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.viitearto.servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import ohtu.viitearto.Rekisteri;
 import ohtu.viitearto.Tag;
 import ohtu.viitearto.Tietoturva;
 import ohtu.viitearto.Viite;
 
 /**
  *
  * @author Keni
  */
 public class LisaaViiteServlet extends HttpServlet {
 
     private Rekisteri rekisteri = Rekisteri.getInstance();
     private Tietoturva secure = new Tietoturva();
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("/Viitteet"); // ohjataan pyyntö Viitteet-servletille
         dispatcher.forward(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");       
         
         String[] type = request.getParameterValues("type");
         Viite uusi = null;
         
         if (type[0].equals("Book"))
             uusi = lisaaBookViite(request, response);
         else if (type[0].equals("Article"))
             uusi = lisaaArticleViite(request, response);
         else if (type[0].equals("Inproceedings"))
             uusi = lisaaInproceedingsViite(request, response);
 
         rekisteri.lisaaViite(uusi);
         request.getRequestDispatcher("/Viitteet").forward(request, response); // ohjataan pyyntö samalle sivulle
     }
 
     private Viite lisaaInproceedingsViite(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         String title = secure.estaCrossSiteScripting(request.getParameter("title"));
         String author = secure.estaCrossSiteScripting(request.getParameter("author"));
         String publisher = secure.estaCrossSiteScripting(request.getParameter("publisher"));
         String yearString = secure.estaCrossSiteScripting(request.getParameter("year"));
         String booktitle = secure.estaCrossSiteScripting(request.getParameter("booktitle"));
         String address = secure.estaCrossSiteScripting(request.getParameter("address"));
         String pages = secure.estaCrossSiteScripting(request.getParameter("pages"));
         
         int year = 0;
 
         secure.tarkistaPakollisetTiedot(title, author, null, booktitle);
         secure.tarkistaNumeroTiedot(yearString, null, null);
         
         if (secure.onkoVirheita()) {
             doGet(request, response);
             return null;
         }
         
         Viite uusiInproceedings = new Viite(author, title);
         uusiInproceedings.setType("Inproceedings");
         uusiInproceedings.setBooktitle(booktitle);
         
         lisaaOptionaalisetTiedot(uusiInproceedings, publisher, address, pages, year, 0, 0);
         lisaaTagit(request, uusiInproceedings);
         
         return uusiInproceedings;
     }
 
     private Viite lisaaArticleViite(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String title = secure.estaCrossSiteScripting(request.getParameter("title"));
         String author = secure.estaCrossSiteScripting(request.getParameter("author"));
         String publisher = secure.estaCrossSiteScripting(request.getParameter("publisher"));
         String yearString = secure.estaCrossSiteScripting(request.getParameter("year"));
         String address = secure.estaCrossSiteScripting(request.getParameter("address"));
         String pages = secure.estaCrossSiteScripting(request.getParameter("pages"));
         String journal = secure.estaCrossSiteScripting(request.getParameter("journal"));
         String volumeString = secure.estaCrossSiteScripting(request.getParameter("volume"));
         String numberString = secure.estaCrossSiteScripting(request.getParameter("number"));
         
         int year = 0;
         int number = 0;        
         int volume = 0;
         
         secure.tarkistaPakollisetTiedot(title, author, journal, null);
         secure.tarkistaNumeroTiedot(yearString, volumeString, numberString);
         
         if (secure.onkoVirheita()) {
             doGet(request, response);
             return null;
         }
         
         Viite uusiArticle = new Viite(title, author);
         uusiArticle.setType("Article");
         uusiArticle.setJournal(journal);
         
         lisaaOptionaalisetTiedot(uusiArticle, publisher, address, pages, year, volume, number);
         lisaaTagit(request, uusiArticle);
         
         return uusiArticle;
     }
 
     private Viite lisaaBookViite(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String title = secure.estaCrossSiteScripting(request.getParameter("title"));
         String author = secure.estaCrossSiteScripting(request.getParameter("author"));
         String publisher = secure.estaCrossSiteScripting(request.getParameter("publisher"));
         String yearString = secure.estaCrossSiteScripting(request.getParameter("year"));
         String address = secure.estaCrossSiteScripting(request.getParameter("address"));
         
         int year = 0;
         
         secure.tarkistaPakollisetTiedot(title, author, null, null);
         secure.tarkistaNumeroTiedot(yearString, null, null);
         
         if (secure.onkoVirheita()) {
             doGet(request, response);
             return null;
         }
         
         Viite uusiBook = new Viite(title, author);
         uusiBook.setType("Book");
         
         lisaaOptionaalisetTiedot(uusiBook, publisher, address, null, year, 0, 0);
         lisaaTagit(request, uusiBook);
         
         return uusiBook;
     }
     
     private void lisaaTagit(HttpServletRequest request, Viite viite) {
         String tag = request.getParameter("tag");
 
         if (tag.length() > 0) {
 
             String[] tagit = tag.split(",");
             List<Tag> tagiLista = new ArrayList<Tag>();
 
             for (int i = 0; i < tagit.length; ++i) {
                 Tag uusi = new Tag(tagit[i]);
                 tagiLista.add(uusi);
             }
 
             viite.setTagit(tagiLista);
         }
     }
     
     private void lisaaOptionaalisetTiedot(Viite uusi, String publisher, String address, String pages,
             int year, int volume, int number) {
         
         if (publisher != null && publisher.length() > 0)
             uusi.setPublisher(publisher);
         
         if (address != null && address.length() > 0)
             uusi.setAddress(address);
         
         if (year != 0)
             uusi.setYear(year);
         
         if (volume != 0)
             uusi.setVolume(volume);
         
         if (number != 0)
             uusi.setNumber(number);
         
         if (pages != null && pages.length() > 0)
             uusi.setPages(pages);
     }
 }
