 package ar.noxit.ehockey.web.pages.usuarios;
 
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
 
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
 
 public class AltaUsuarioPage extends AbstractContentPage{
 
     public AltaUsuarioPage(IModel<UsuarioDTO> usuario) {
         super();
 
         add(new FeedbackPanel("feedback"));
         add(new FormularioAdministradorPanel("administradorPanel", usuario, "Alta de usuario", "Se ha completado el alta de usuario"));
        add(new FormularioRepresentantePanel("administradorPanel", usuario, "Alta de usuario", "Se ha completado el alta de usuario"));
     }
 }
