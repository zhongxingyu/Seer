 package de.fhb.autobday.beans.actions;
 
 import de.fhb.autobday.beans.SessionBean;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
import de.fhb.autobday.exception.commons.CouldNotDecryptException;
import de.fhb.autobday.exception.commons.CouldNotLoadMasterPasswordException;
import de.fhb.autobday.exception.connector.*;
 import de.fhb.autobday.manager.account.AccountManagerLocal;
 import java.util.List;
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
 	/**
 	 * will update the given Google-Account.
 	 * @return 
 	 */
 	public String updateAccount() {
 		List<String> errorStack;
 		try {
 			errorStack = accountManager.updateGroupsAndContacts(sessionBean.getAktAccount().getId());
 			for (String string : errorStack) {
 				FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Skipped Contact: "+string , ""));
 			}
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Account is up-to-date now!" , ""));
 		} catch (AccountNotFoundException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		} catch (ConnectorException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
		}  catch (CouldNotDecryptException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			FacesContext.getCurrentInstance().addMessage(
					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
		} catch (CouldNotLoadMasterPasswordException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			FacesContext.getCurrentInstance().addMessage(
					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		}
 
 		return null;
 	}
 }
