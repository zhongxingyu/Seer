 package de.fhb.autobday.beans;
 
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdGroupToContact;
 import de.fhb.autobday.exception.contact.ContactException;
 import de.fhb.autobday.exception.contact.ContactNotFoundException;
import de.fhb.autobday.exception.contact.ContactToGroupNotFoundException;
 import de.fhb.autobday.exception.group.GroupException;
 import de.fhb.autobday.exception.group.GroupNotFoundException;
 import de.fhb.autobday.manager.contact.ContactManagerLocal;
 import de.fhb.autobday.manager.group.GroupManagerLocal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.model.ListDataModel;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * Bean for group-tasks.
  *
  * @author Michael Koppen mail: koppen@fh-brandenburg.de
  */
 @Named
 @RequestScoped
 public class GroupBean {
 
 	private final static Logger LOGGER = Logger.getLogger(GroupBean.class.getName());
 	@Inject
 	private GroupManagerLocal groupManager;
 	@Inject
 	private ContactManagerLocal contactManager;
 	@Inject
 	private SessionBean sessionBean;
 	private ListDataModel<AbdContact> contactList;
 	private boolean activeState = false;
 	private String parsedTemplate = "";
 	private String template = "";
 
 	/**
 	 * Creates a new instance of GroupBean
 	 */
 	public GroupBean() {
 	}
 
 	public String showGroup() {
 		return "showgroup";
 	}
 
 	public String showTemplate() {
 		return "edittemplate";
 	}
 
 	public String editTemplate() {
 		try {
 			groupManager.setTemplate(sessionBean.getAktGroup(), template);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully edited template!", ""));
 
 		} catch (GroupException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		}
 		return "showaccount";
 	}
 
 	private void getAllContactsFromGroup() {
 		try {
 			contactList = new ListDataModel(groupManager.getAllContactsFromGroup(sessionBean.getAktGroup()));
 
 		} catch (Exception ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		}
 	}
 
 	public String toggleContactActivation() {
 		LOGGER.log(Level.INFO, "toggleContactActivation");
 		AbdGroup aktGroup = sessionBean.getAktGroup();
 		AbdContact aktContact = contactList.getRowData();
 
 		for (AbdGroupToContact gtc : aktContact.getAbdGroupToContactCollection()) {
 			if (gtc.getAbdGroup().equals(aktGroup)) {
 				try {
 					if (gtc.getActive()) {
 						LOGGER.log(Level.INFO, "was aktive");
 						contactManager.setActive(aktContact, aktGroup, false);
 						FacesContext.getCurrentInstance().addMessage(
 								null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully deaktivated " + aktContact.getFirstname() + " " + aktContact.getName() + "!", ""));
 
 					} else {
 						LOGGER.log(Level.INFO, "was inaktive");
 						contactManager.setActive(aktContact, aktGroup, true);
 						FacesContext.getCurrentInstance().addMessage(
 								null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Successfully aktivated " + aktContact.getFirstname() + " " + aktContact.getName() + "!", ""));
 
 					}
				} catch (ContactNotFoundException ex) {
					LOGGER.log(Level.SEVERE, null, ex);
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
				} catch (ContactToGroupNotFoundException ex) {
					LOGGER.log(Level.SEVERE, null, ex);
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
				} catch (GroupNotFoundException ex) {
 					LOGGER.log(Level.SEVERE, null, ex);
 					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 				}
 			} else {
 				LOGGER.log(Level.SEVERE, null, "Contact is not in a active group.");
 				FacesContext.getCurrentInstance().addMessage(
 						null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Contact " + aktContact.getFirstname() + " " + aktContact.getName() + " is not in an aktive group!", ""));
 
 			}
 		}
 		return null;
 
 	}
 
 	private boolean changeAktiveState() {
 		AbdGroup aktGroup = sessionBean.getAktGroup();
 		AbdContact aktContact = contactList.getRowData();
 		boolean active = false;
 
 		LOGGER.log(Level.INFO, "group: {0}", aktGroup.getName());
 		LOGGER.log(Level.INFO, "contact: {0}", aktContact.getFirstname());
 		try {
 			active = contactManager.getActive(aktContact.getId(), aktGroup.getId());
 		} catch (ContactNotFoundException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		} catch (GroupNotFoundException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 		}
 
 		return active;
 	}
 
 	public String testTemplate() {
 		try {
 			AbdContact contact = sessionBean.getAktContact();
 			AbdGroup group = sessionBean.getAktGroup();
 
 			if (contact != null && group != null) {
 				parsedTemplate = groupManager.testTemplate(group.getId(), contact.getId());
 				LOGGER.log(Level.INFO, "template: {0}", parsedTemplate);
 			} else {
 				LOGGER.log(Level.INFO, "Contact: {0}", contact);
 				LOGGER.log(Level.INFO, "Group: {0}", group);
 				FacesContext.getCurrentInstance().addMessage(
 						null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not parse the template with this user!", ""));
 				parsedTemplate = "ERROR";
 			}
 
 		} catch (GroupException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 			parsedTemplate = ex.getMessage();
 		} catch (ContactException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 			parsedTemplate = ex.getMessage();
 		}
 		return "showtemplate";
 	}
 
 	public boolean isActiveState() {
 		activeState = changeAktiveState();
 		LOGGER.log(Level.INFO, "is Aktive?: {0}", activeState);
 		return activeState;
 	}
 
 	public void setActiveState(boolean activeState) {
 		this.activeState = activeState;
 	}
 
 	public String getParsedTemplate() {
 		return parsedTemplate;
 	}
 
 	public void setParsedTemplate(String parsedTemplate) {
 		this.parsedTemplate = parsedTemplate;
 	}
 
 	public ListDataModel<AbdContact> getContactList() {
 		getAllContactsFromGroup();
 		return contactList;
 	}
 
 	public void setContactList(ListDataModel<AbdContact> contactList) {
 		this.contactList = contactList;
 	}
 
 	public String getTemplate() {
 		template = sessionBean.getAktGroup().getTemplate();
 		return template;
 	}
 
 	public void setTemplate(String template) {
 		this.template = template;
 	}
 }
