 package ar.noxit.ehockey.web.pages.jugadores;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ar.noxit.ehockey.service.IJugadorService;
import ar.noxit.ehockey.web.pages.base.AbstractHeaderPage;
 
 public class JugadorAltaOkPage extends AbstractHeaderPage {
 
    @SpringBean
    private IJugadorService jugadorService;

     public JugadorAltaOkPage(IModel<String> mensaje) {
         super();
         add(new Label("mensaje", mensaje));
        add(new JugadorVerPanel(jugadorService));
         add(new BookmarkablePageLink<AbstractJugadorPage>("paginaalta", JugadorAltaPage.class));
         add(new BookmarkablePageLink<AbstractJugadorPage>("menujugador", JugadorPage.class));
     }
 }
