 package cz.muni.fi.pv243.et.security;
 
 import cz.muni.fi.pv243.et.data.PersonListProducer;
 import cz.muni.fi.pv243.et.message.WebMessage;
 import cz.muni.fi.pv243.et.model.Person;
 import cz.muni.fi.pv243.et.service.LoginService;
 import cz.muni.fi.pv243.et.service.ReceiptService;
 import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
 import org.picketlink.Identity;
 import org.picketlink.authentication.AuthenticationException;
 import org.picketlink.extensions.core.pbox.LoginCredential;
 import org.picketlink.idm.model.Attribute;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.io.Serializable;
 import javax.enterprise.inject.Model;
 
 @Model
 public class LoginBean {
 
     private static final long serialVersionUID = 1L;
 
     @Inject
     private Identity identity;
 
     @Inject
     private FacesContext facesContext;
 
     @Inject
     private WebMessage webMessage;
    
    @Inject 
     private LoginService loginService;
 
     public String login(String userName, String password) throws AuthenticationException {
 
        loginService.login(userName, password);
        
        if (identity.isLoggedIn()) {
            return "/secured/index.xhtml";
         }
 
        this.facesContext.addMessage(null, new FacesMessage(webMessage.loginFailed()));

        return null;
     }
 }
