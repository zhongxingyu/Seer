 package beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 
 import org.primefaces.event.SelectEvent;
 import org.primefaces.event.UnselectEvent;
 
 @ManagedBean
 public class UserTableBean {
 
 	
 
 	private List<User> usersSmall;
 
 	private User selectedUser;
 
 	private UserDataModel mediumUsersModel;
 
 	public UserTableBean() {
 		usersSmall = new ArrayList<User>();
 
		populateRandomUsers(usersSmall, 50);
 
 		mediumUsersModel = new UserDataModel(usersSmall);
 	}
 
 	private void populateRandomUsers(List<User> list, int size) {
 		for (int i = 0; i < size; i++)
 			list.add(new User());
 	}
 
 	public User getSelectedUser() {
 		return selectedUser;
 	}
 
 	public void setSelectedUser(User selectedUser) {
 		this.selectedUser = selectedUser;
 	}
 
 	public UserDataModel getMediumUsersModel() {
 		return mediumUsersModel;
 	}
 
 	public void onRowSelect(SelectEvent event) {
 		FacesMessage msg = new FacesMessage("User Selected",
 				((User) event.getObject()).getLogin());
 
 		FacesContext.getCurrentInstance().addMessage(null, msg);
 	}
 
 	public void onRowUnselect(UnselectEvent event) {
 		FacesMessage msg = new FacesMessage("User Unselected",
 				((User) event.getObject()).getLogin());
 
 		FacesContext.getCurrentInstance().addMessage(null, msg);
 	}
 	
 	public void deleteUser(){
 		
 	}
 }
