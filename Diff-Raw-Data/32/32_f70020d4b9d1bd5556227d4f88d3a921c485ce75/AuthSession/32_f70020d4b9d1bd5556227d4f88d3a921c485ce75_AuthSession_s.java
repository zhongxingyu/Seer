 package ar.noxit.ehockey.web.pages.authentication;
 
 import ar.noxit.ehockey.exception.ErrorDeLoginException;
 import ar.noxit.ehockey.model.Usuario;
 import ar.noxit.ehockey.service.IUsuarioService;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.web.wicket.model.LDM;
 import org.apache.wicket.Request;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.injection.web.InjectorHolder;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class AuthSession extends AuthenticatedWebSession {
 
     @SpringBean
     private IUsuarioService usuarioService;
     private IModel<Usuario> usuarioModel;
 
     public AuthSession(Request request) {
         super(request);
         InjectorHolder.getInjector().inject(this);
         usuarioModel = new UsuarioIdModel();
     }
 
     public void setUsuarioService(IUsuarioService usuarioService) {
         this.usuarioService = usuarioService;
     }
 
     @Override
     public boolean authenticate(String username, String password) {
         usuarioModel = new UsuarioIdModel(username);
         Usuario usuario = usuarioModel.getObject();
         if (usuario != null) {
             try {
                 return usuarioService.logInUsuario(usuario, password);
             } catch (ErrorDeLoginException e) {
                 return false;
             }
         }
         return false;
     }
 
     @Override
     public Roles getRoles() {
         Usuario usuario = usuarioModel.getObject();
         if (usuario == null)
             return new Roles();
         return new Roles(usuario.getRoles());
     }
 
     public Usuario getUserLogged() {
         return this.usuarioModel.getObject();
     }
 
     @Override
     public void signOut() {
         super.signOut();
        this.usuarioModel.setObject(null);
     }
 
     private class UsuarioIdModel extends LDM<Usuario> {
 
         private String username;
 
         public UsuarioIdModel() {
             this.username = null;
         }
 
         public UsuarioIdModel(String username) {
             this.username = username;
         }
 
         @Override
         protected Usuario doLoad() throws NoxitException {
             if (this.username == null)
                 return null;
             Usuario usuario = null;
             try {
                 usuario = usuarioService.get(username);
             } catch (NoxitException ex) {
                 // Error de login. Se loguea la excepcion
                 usuario = null;
             }
             return usuario;
         }
     }
 
     public static AuthSession get() {
         return (AuthSession) AuthenticatedWebSession.get();
     }
 
     @Override
     protected void detach() {
         super.detach();
         usuarioModel.detach();
     }
 }
