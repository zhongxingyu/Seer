 package de.fhb.autobday.beans;
 
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
 import de.fhb.autobday.exception.contact.ContactNotFoundException;
 import de.fhb.autobday.exception.group.GroupNotFoundException;
 import de.fhb.autobday.exception.group.NoGroupGivenException;
 import de.fhb.autobday.manager.account.AccountManagerLocal;
 import de.fhb.autobday.manager.contact.ContactManagerLocal;
 import de.fhb.autobday.manager.group.GroupManagerLocal;
 import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.primefaces.model.DefaultScheduleEvent;
 import org.primefaces.model.DefaultScheduleModel;
 import org.primefaces.model.ScheduleModel;
 
 /**
  * Bean for schedule-tasks.
  *
  * @author Michael Koppen mail: koppen@fh-brandenburg.de
  */
 @Named
 @RequestScoped
 public class ScheduleBean {
 
 	private final static Logger LOGGER = Logger.getLogger(ScheduleBean.class.getName());
 	@Inject
 	private AccountManagerLocal accountManager;
 	@Inject
 	private GroupManagerLocal groupManager;
 	@Inject
 	private ContactManagerLocal contactManager;
 	@Inject
 	private SessionBean sessionBean;
 	private ScheduleModel eventModel;
 
 	/**
 	 * Creates a new instance of ScheduleBean
 	 */
 	public ScheduleBean() {
 	}
 
 	private void getAllContactsFromAccount() {
 		List<AbdGroup> groups = new ArrayList<AbdGroup>();
 		List<AbdContact> contacts = new ArrayList<AbdContact>();
 		try {
 			groups = accountManager.getAllGroupsFromAccount(sessionBean.getAktAccount());
 
 			//getAllActiveContatcts
 			for (AbdGroup abdGroup : groups) {
 				List<AbdContact> tempcontacts = groupManager.getAllContactsFromGroup(abdGroup);
 				for (AbdContact abdContact : tempcontacts) {
 					try {
 						if (contactManager.getActive(abdContact.getId(), abdGroup.getId())) {
 							contacts.add(abdContact);
 						}
 					} catch (ContactNotFoundException ex) {
 						LOGGER.log(Level.SEVERE, null, ex);
 					}
 				}
 
 			}
 			eventModel = new DefaultScheduleModel();
			
 			for (AbdContact abdContact : contacts) {
				int i = 0;
				for (int j = 0; j < 120; j++) {
					Calendar bday = Calendar.getInstance();
					bday.setTime(abdContact.getBday());
					bday.add(bday.DAY_OF_MONTH, -1);
					bday.add(bday.YEAR, +i);
					System.out.println("Added date: "+new Date(bday.getTimeInMillis()));
					eventModel.addEvent(new DefaultScheduleEvent(abdContact.getFirstname() + " " + abdContact.getName(), bday.getTime(),
						abdContact.getBday(),
						true));
					i++;
				}
				
 			}
 		} catch (AccountNotFoundException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		} catch (NoGroupGivenException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		} catch (GroupNotFoundException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		}
 
 
 	}
 
 	public ScheduleModel getEventModel() {
 		getAllContactsFromAccount();
 		return eventModel;
 	}
 
 	public void setEventModel(ScheduleModel eventModel) {
 		this.eventModel = eventModel;
 	}
 }
