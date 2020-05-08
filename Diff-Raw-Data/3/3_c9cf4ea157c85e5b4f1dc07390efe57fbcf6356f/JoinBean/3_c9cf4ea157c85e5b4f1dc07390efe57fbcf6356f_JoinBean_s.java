 package com.jcertif.web.ihm.join;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.validation.ValidationException;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.jcertif.web.model.RoleParticipant;
 import com.jcertif.web.model.TypeParticipant;
 import com.jcertif.web.model.User;
 import com.jcertif.web.service.ReferentielService;
 import com.jcertif.web.service.ResourceService;
 import com.jcertif.web.service.RestService;
 
 /**
  * Join Bean.
  * 
  * @author rossi.oddet
  * 
  */
 @Named
 @RequestScoped
 public class JoinBean {
 
 	/** User **/
 	private User user;
 
 	/** REST Web Service **/
 	@Inject
 	private RestService restService;
 
 	/** Resource Service **/
 	@Inject
 	private ResourceService resourceService;
 
 	/** Referentiel Service **/
 	@Inject
 	private ReferentielService referentielService;
 
 	/**
 	 * A default constructor.
 	 */
 	public JoinBean() {
 		super();
 		this.user = new User();
 	}
 
 	/**
 	 * Save the user's data.
 	 * 
 	 * @param actionEvent
 	 *            a action event
 	 * @throws IOException
 	 *             if redirect error
 	 */
 	public void save(ActionEvent actionEvent) throws IOException {
 
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		// Update Conference for user
 		this.user.setIdConference(referentielService.getConference().getId());
 
 		try {
 			validateUser();
 			restService.post(resourceService.getUserCreateContext(), user, User.class);
 			context.getExternalContext().redirect(
 					context.getExternalContext().getRequestContextPath()
 							+ "/faces/join/confirmationJoin.jsf");
 		} catch (ValidationException e) {
 			context.addMessage("join:confirm",
 					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
 		}
 	}
 
 	/**
 	 * Validate the user's data.
 	 */
 	protected void validateUser() {
 		// Email and Confirmation Email must be equals
 		if (!user.getEmail().equals(user.getConfirmemail())) {
 			throw new ValidationException(resourceService.getLib("join.confirmemail.invalidmsg"));
 		}
 
 		// Password and Confirmation Password must be equals
 		if (!user.getPasswd().equals(user.getConfirmpasswd())) {
 			throw new ValidationException(resourceService.getLib("join.confirmpassword.invalidmsg"));
 		}
 
 		// Role is required
 		if (StringUtils.isBlank(user.getRole())) {
 			throw new ValidationException(resourceService.getLib("join.role.reqmsg"));
 		}
 
 		// Type is required
 		if (StringUtils.isBlank(user.getTypeUser())) {
 			throw new ValidationException(resourceService.getLib("join.type.reqmsg"));
 		}
 
 		// email must be unique
 		User existingUser = restService.getBuilder(
				resourceService.getUserContext() + "/" + user.getEmail()).get(User.class);
 
 		if (existingUser != null && existingUser.getId() != null) {
 			throw new ValidationException(resourceService.getLib("join.existingemail.msg"));
 		}
 	}
 
 	/**
 	 * @return the typesParticipant
 	 */
 	public List<TypeParticipant> getTypesParticipant() {
 		return referentielService.getTypesParticipantList();
 	}
 
 	/**
 	 * @return the rolesParticipant
 	 */
 	public List<RoleParticipant> getRolesParticipant() {
 		return referentielService.getRolesParticipantList();
 	}
 
 	/**
 	 * @return the user
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user
 	 *            the user to set
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 }
