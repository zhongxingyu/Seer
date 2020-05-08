 package de.fhb.autobday.beans.actions;
 
 import de.fhb.autobday.beans.SessionBean;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
 import de.fhb.autobday.exception.connector.ConnectorException;
 import de.fhb.autobday.manager.account.AccountManagerLocal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * ActionBean for update-account-form.
  *
  * @author Michael Koppen mail: koppen@fh-brandenburg.de
  */
 @Named
 @RequestScoped
 public class UpdateAccountBean {
 
 	private final static Logger LOGGER = Logger.getLogger(ImportNewAccountBean.class.getName());
 	@Inject
 	private AccountManagerLocal accountManager;
 	@Inject
 	private SessionBean sessionBean;
 
 	/**
 	 * Creates a new instance of ImportNewAccountBean
 	 */
 	public UpdateAccountBean() {
 	}
 
 	public String updateAccount() {
 		try {
 			accountManager.updateGroupsAndContacts(sessionBean.getAktAccount().getId());
 			FacesContext.getCurrentInstance().addMessage(
					null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Account is up-to-date now!", ""));
 		} catch (AccountNotFoundException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		} catch (ConnectorException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 
 		}
 
 		return null;
 	}
 }
