 package module.organization.domain.groups;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import myorg.domain.groups.PersistentGroup;
 import myorg.util.BundleUtil;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class PersonGroup extends PersonGroup_Base {
 
     public PersonGroup() {
 	super();
	setSystemGroupMyOrg(MyOrg.getInstance());
     }
 
     @Service
     public static PersonGroup getInstance() {
 	final PersonGroup personGroup = (PersonGroup) PersistentGroup.getSystemGroup(PersonGroup.class);
 	return personGroup == null ? new PersonGroup() : personGroup;
     }
 
     @Override
     public Set<User> getMembers() {
 	Set<User> users = new HashSet<User>();
 	for (final Party party : MyOrg.getInstance().getPartiesSet()) {
 	    if (party.isPerson()) {
 		users.add(((Person) party).getUser());
 	    }
 	}
 	return users;
     }
 
     @Override
     public String getName() {
 	return BundleUtil.getStringFromResourceBundle("resources/OrganizationResources",
 		"label.persistent.group.personGroup.name");
     }
 
     @Override
     public boolean isMember(User user) {
 	return user != null && user.getPerson() != null;
     }
 
 }
