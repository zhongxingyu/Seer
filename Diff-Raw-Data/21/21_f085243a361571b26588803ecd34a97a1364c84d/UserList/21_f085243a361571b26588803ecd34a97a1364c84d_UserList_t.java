 package war.webapp.action;
 
 import org.springframework.security.context.HttpSessionContextIntegrationFilter;
 import org.springframework.security.context.SecurityContext;
 import war.webapp.model.User;
 import war.webapp.util.FacesUtils;
 
 import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
 import java.util.List;
 
 public class UserList extends BasePage implements Serializable {
     private static final long serialVersionUID = 972359310602744018L;
 
     public UserList() {
 
         setSortColumn("username");
     }
 
     public List<User> getUsers() {
        return sortByRoom(userManager.getUsers());
     }
 
     public List<User> getUsersByFloorheadFloor() {
        User user = userManager.getUserByUsername(getRequest().getRemoteUser());
         String floor = user.getAddress().getHostelFloor();
         List<User> floorUsers = userManager.getUsersByFloor(floor);
         floorUsers.remove(user);
        return sortByRoom(floorUsers);
    }

    private List<User> sortByRoom(List<User> users) {
        Collections.sort(users, new Comparator<User>() {
            public int compare(User u1, User u2) {
                return u1.getAddress().getHostelRoom().compareToIgnoreCase(u2.getAddress().getHostelRoom());
            }
        });
        return users;
     }
 
     public String getCurrentUserFloor() {
         User user = (User) ((SecurityContext) FacesUtils.getSession().getAttribute(
                 HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY)).getAuthentication().getPrincipal();
         return user.getAddress().getHostelFloor();
     }
 }
