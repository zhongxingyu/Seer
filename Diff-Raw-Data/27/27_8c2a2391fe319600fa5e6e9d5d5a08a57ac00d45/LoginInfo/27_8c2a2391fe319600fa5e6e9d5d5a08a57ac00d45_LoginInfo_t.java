 package it.ivncr.erp.jsf.managedbean.accesso.session;
 
 import it.ivncr.erp.model.accesso.Utente;
 import it.ivncr.erp.model.generale.Azienda;
 import it.ivncr.erp.service.ServiceFactory;
 import it.ivncr.erp.service.utente.UtenteService;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ManagedBean
 @SessionScoped
 public class LoginInfo implements Serializable {
 
 	private static final Logger logger = LoggerFactory.getLogger(LoginInfo.class);
 
 	private static final long serialVersionUID = 1L;
 
 	private String username;
 	private String password;
 
 	private Utente utente;
 
 	private Integer codiceAzienda;
 	private List<Azienda> listAziende;
 	private Azienda azienda;
 
 	public String cleanLoginInfo() {
 
 		logger.debug("Cleaning up login info.");
 		this.reset();
 		return null;
 	}
 
 	public String doLogoff() {
 
 		this.reset();
 
 		Subject currentUser = SecurityUtils.getSubject();
         try {
             currentUser.logout();
         } catch (Exception e) {
             logger.debug("Exception caught while logging out current user.", e);
         }
 
 		return "/faces/login/login?faces-redirect=true";
 	}
 
 	public boolean isPermitted(String permission) {
 
 		Subject currentUser = SecurityUtils.getSubject();
 		return currentUser.isPermitted(permission);
 	}
 
 	public String doLogin() {
 
         UsernamePasswordToken token = new UsernamePasswordToken(username, password);
         Subject currentUser = SecurityUtils.getSubject();
 
         try {
 
             currentUser.login(token);
 
         } catch (AuthenticationException e) {
 
             logger.debug("Exception caught while attempting login. Reason: " + e.getMessage());
             this.reset();
     		FacesMessage message = new FacesMessage(
     				FacesMessage.SEVERITY_ERROR,
     				"Login fallito",
     				"Impossibile accedere al sistema, controllare username e password");
 
     		FacesContext.getCurrentInstance().addMessage(null, message);
     		return null;
         }
 
         // Extract some useful data from current user.
  		//
  		for(Object principal : currentUser.getPrincipals()) {
  			if(principal instanceof Utente) {
 
  				this.utente = (Utente)principal;
  				break;
  			}
  		}
 
  		// Record last login timestamp.
  		//
  		UtenteService us = ServiceFactory.createService("Utente");
  		this.utente = us.updateLastLogonTimestamp(utente.getId());
 
  		// Retrieve list of available azienda entities for the logged on user.
  		//
  		this.listAziende = us.listAziende(utente.getId());
 
  		// Choose currently selected azienda by looking for a default one
  		// or picking the first available .
  		//
 		this.azienda = us.retrieveDefaultAzienda(utente.getId(), true);
  		if(azienda == null) {
  			logger.warn("The logged on user has no associated azienda entities.");
  		} else {
  			codiceAzienda = azienda.getId();
  			logger.debug(String.format("Selected the azienda with id %d: %s", azienda.getId(), azienda.getCodice()));
  		}
 
         return "/index?faces-redirect=true";
 	}
 
 	public void doSelectAzienda() {
 
 		// Find azienda corresponding to selected codice.
 		//
 		this.azienda = null;
 		for(Azienda azienda : listAziende) {
 
 			if(azienda.getId().equals(codiceAzienda)) {
 				this.azienda = azienda;
 			}
 		}
 
 		FacesMessage message = null;
 		if(this.azienda != null) {
     		message = new FacesMessage(
     				FacesMessage.SEVERITY_INFO,
     				"Selezione azienda",
     				String.format("L'azienda corrente è stata impostata a \"%s\" (%s)",
     						azienda.getCodice(),
     						azienda.getDescrizione()));
 		} else {
     		message = new FacesMessage(
     				FacesMessage.SEVERITY_WARN,
     				"Selezione azienda",
     				"Nessuna azienda selezionata");
 		}
 		FacesContext.getCurrentInstance().addMessage(null, message);
 	}
 
 	private void reset() {
 
 		username = null;
 		password = null;
 		utente = null;
 		azienda = null;
 		codiceAzienda = null;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public Utente getUtente() {
 		return utente;
 	}
 
 	public void setUtente(Utente utente) {
 		this.utente = utente;
 	}
 
 	public Azienda getAzienda() {
 		return azienda;
 	}
 
 	public void setAzienda(Azienda azienda) {
 		this.azienda = azienda;
 	}
 
 	public Integer getCodiceAzienda() {
 		return codiceAzienda;
 	}
 
 	public void setCodiceAzienda(Integer codiceAzienda) {
 		this.codiceAzienda = codiceAzienda;
 	}
 
 	public List<Azienda> getListAziende() {
 		return listAziende;
 	}
 
 	public void setListAziende(List<Azienda> listAziende) {
 		this.listAziende = listAziende;
 	}
 }
