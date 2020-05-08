 package tableCtrl;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 
 import org.primefaces.event.SelectEvent;
 import org.primefaces.event.UnselectEvent;
 
 import beans.User;
 
 import dao.Users;
 
 @ManagedBean
 public class UserTableBean {
 
 	
 
 	private List<User> usersSmall;
 	private User selectedUser;
 	private UserDataModel mediumUsersModel;
 	private Users DAO;
 	
 	public UserTableBean() {
 		selectedUser = new User();
 		usersSmall = new ArrayList<User>();
 		populateRandomUsers(usersSmall, 5);
 		this.DAO = new Users();
 		mediumUsersModel = new UserDataModel(DAO.getUsers());
 	}
 
 	private void populateRandomUsers(List<User> list, int size) {
 		for (int i = 0; i < size; i++)
 			list.add(new User(i, "login"+i, "pwd"+i, "fName"+i, "lName"+i, i, "email"+i, (i%2==0)?true:false));
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
 	public void updateModel(){
 		System.out.println("UPDATEDONE");
 		
 		mediumUsersModel = new UserDataModel(DAO.getUsers());
 	}
 	
 }
