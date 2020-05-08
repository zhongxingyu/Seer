 package pt.ist.expenditureTrackingSystem.domain;
 
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class ConnectPersonToUserTask extends ConnectPersonToUserTask_Base {
 
     public ConnectPersonToUserTask() {
 	super();
     }
 
     @Override
     public void executeTask() {
 	for (Person person : ExpenditureTrackingSystem.getInstance().getPeople()) {
 	    if (!person.hasUser()) {
 		String username = person.getUsername();
 		User user = User.findByUsername(username);
 		if (user == null) {
 		    user = new User(username);
 		}
 		person.setUser(user);
 	    }
 	}
 
     }
 
     @Override
     public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/ExpendituresResources", "label.task.connectPersonToUserTask");
     }
 
 }
