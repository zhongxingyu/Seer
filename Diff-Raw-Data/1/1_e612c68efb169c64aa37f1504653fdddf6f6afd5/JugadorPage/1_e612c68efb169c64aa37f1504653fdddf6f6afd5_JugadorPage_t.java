 package ar.noxit.ehockey.web.pages.jugadores;
 
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
 
 public class JugadorPage extends AbstractJugadorPage {
 
     public JugadorPage() {
         add(new BookmarkablePageLink<AbstractContentPage>("altajugadores", JugadorAltaPage.class));
         add(new BookmarkablePageLink<AbstractContentPage>("verjugadores", JugadorVerPage.class));
         add(new BookmarkablePageLink<AbstractContentPage>("bajajugadores", JugadorBajaPage.class));
     }
 }
