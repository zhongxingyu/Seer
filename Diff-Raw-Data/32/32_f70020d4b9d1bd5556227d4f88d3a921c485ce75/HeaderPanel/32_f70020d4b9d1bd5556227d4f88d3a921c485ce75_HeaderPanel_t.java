 package ar.noxit.ehockey.web.pages.header;
 
 import ar.noxit.ehockey.exception.SessionClosedException;
 import ar.noxit.ehockey.service.IHorarioService;
 import ar.noxit.ehockey.service.IUsuarioService;
 import ar.noxit.ehockey.web.pages.HomePage;
import ar.noxit.ehockey.web.pages.authentication.AuthSession;
 import ar.noxit.ehockey.web.pages.models.UsuarioAdapterModel;
 import ar.noxit.ehockey.web.pages.models.UsuarioModel;
 import ar.noxit.ehockey.web.pages.usuarios.PerfilUsuarioPage;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.exceptions.NoxitRuntimeException;
 import ar.noxit.web.wicket.model.LocalDateTimeFormatModel;
 import java.util.List;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class HeaderPanel extends Panel {
 
     @SpringBean
     private IMenuItemProvider menuItemProvider;
     @SpringBean
     private IHorarioService horarioService;
     @SpringBean
     private IUsuarioService usuarioService;
    private final static Logger logger = LoggerFactory.getLogger(HeaderPanel.class);
 
     public HeaderPanel(String id, final IMenuSelection menuSelection) {
         super(id);
         String usuarioConectado = null;
         try {
             usuarioConectado = usuarioService.getUsuarioConectado(getSession());
         } catch (SessionClosedException e) {
             usuarioConectado = null;
         }
         add(new Label("usuario", Model.of(usuarioConectado)));
 
         add(new Link<String>("signout") {
 
             @Override
             public void onClick() {
                 try {
                    AuthSession authSession = AuthSession.get();
                    authSession.signOut();
                    usuarioService.logOutUser(authSession);
                 } catch (NoxitException ex) {
                    logger.warn("excepcion deslogueando ", ex);
                 }
                setResponsePage(HomePage.class);
             }
         });
 
         add(new Link<String>("perfil") {
 
             @Override
             public void onClick() {
                 try {
                     String usuario = usuarioService.getUsuarioConectado(getSession());
                     setResponsePage(new PerfilUsuarioPage(new UsuarioAdapterModel(new UsuarioModel(Model.of(usuario),
                             usuarioService))));
                 } catch (NoxitException ex) {
                     setResponsePage(HomePage.class);
                 }
             }
         });
 
         add(new ListView<IMenuItem>("links", new MenuItemModel()) {
 
             @Override
             protected void populateItem(ListItem<IMenuItem> item) {
                 final IModel<IMenuItem> model = item.getModel();
 
                 BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", model.getObject()
                         .getPageLink());
                 link.add(new Label("titulo", new PropertyModel<String>(model, "titulo")));
                 item.add(link);
 
                 item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>() {
 
                     @Override
                     public String getObject() {
                         boolean shouldBeSelected = menuSelection.shouldBeSelected(model.getObject());
                         if (shouldBeSelected) {
                             return "nav-active";
                         }
                         return "";
                     }
                 }));
             }
 
             @Override
             protected ListItem<IMenuItem> newItem(int index) {
                 IModel<IMenuItem> modelMenuItem = getListItemModel(getModel(), index);
                 return new MenuItemListItem(index, modelMenuItem);
             }
         });
 
         add(new Label("hora", new LocalDateTimeFormatModel(new FechaSistemaModel())).setRenderBodyOnly(true));
     }
 
     private class FechaSistemaModel extends AbstractReadOnlyModel<LocalDateTime> {
 
         @Override
         public LocalDateTime getObject() {
             try {
                 return horarioService.getHoraSistema();
             } catch (NoxitException e) {
                 throw new NoxitRuntimeException(e);
             }
         }
     }
 
     public class MenuItemModel extends LoadableDetachableModel<List<IMenuItem>> {
 
         @Override
         protected List<IMenuItem> load() {
             return menuItemProvider.getMenuItems();
         }
     }
 }
