 package ar.noxit.ehockey.web.pages.jugadores;
 
import ar.noxit.ehockey.web.pages.base.AbstractHeaderPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.model.IModel;
 
 public class JugadorAltaOkPage extends AbstractHeaderPage {
 
     public JugadorAltaOkPage(IModel<String> mensaje) {
         super();
         add(new Label("mensaje", mensaje));
         add(new BookmarkablePageLink<AbstractJugadorPage>("paginaalta", JugadorAltaPage.class));
         add(new BookmarkablePageLink<AbstractJugadorPage>("menujugador", JugadorPage.class));
     }
 }
