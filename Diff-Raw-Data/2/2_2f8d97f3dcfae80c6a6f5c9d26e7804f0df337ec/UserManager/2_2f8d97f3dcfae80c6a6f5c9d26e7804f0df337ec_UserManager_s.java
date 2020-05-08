 package usersManagement;
 
 import java.io.Serializable;
 import security.Principal;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import daoLayer.UserDaoBean;
 import annotations.Logged;
 
 @Named("userManager")
 @SessionScoped
 public class UserManager implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private Principal loggedUser;
 	@Inject
 	private UserDaoBean userDao;
 
 	public UserManager() {
 	}
 
 	@PostConstruct
 	public void init() {
 		loggedUser = new Principal();
 		// TODO: rimettere GUEST quando sarà tutto montato
 	}
 
 	@RequestScoped
 	@Produces
 	@Logged
 	public Principal getLoggedUser() {
 		return loggedUser;
 	}
 
 	public String login(String serialNumber, String password) {
 		User u = userDao.getBySerialNumber(Integer.parseInt(serialNumber));
 		if (u != null && u.login(password)) {
 			loggedUser = new Principal(String.valueOf(u.getSerialNumber()), u.getRole());
 			System.out.println("User Login: loggedUser= " + u);
 			return "home";
 		} else {
 			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
 					"Matricola o password errati", "Inserire i valori corretti");
 			FacesContext.getCurrentInstance().addMessage(null, msg);
 			return "login";
 		}
 	}
 
 	public void logout() {
 		loggedUser = new Principal();
 		// TODO return value;
 	}
 
 }
