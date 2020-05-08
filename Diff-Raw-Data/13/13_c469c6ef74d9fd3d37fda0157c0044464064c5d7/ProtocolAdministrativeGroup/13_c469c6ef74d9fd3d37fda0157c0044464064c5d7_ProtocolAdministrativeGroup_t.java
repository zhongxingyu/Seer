 package module.protocols.domain;
 
 import java.util.Set;
 
 import pt.ist.bennu.core.domain.RoleType;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.groups.Role;
import pt.ist.bennu.core.util.BundleUtil;
 
 public class ProtocolAdministrativeGroup extends ProtocolAdministrativeGroup_Base {
 
     public ProtocolAdministrativeGroup() {
 	super();
 	this.setDelegateGroup(Role.getRole(RoleType.MANAGER));
     }
 
     @Override
     public boolean isMember(User user) {
 	return getDelegateGroup().isMember(user);
     }
 
     @Override
     public String getName() {
	return BundleUtil.getFormattedStringFromResourceBundle("resources/ProtocolsResources", "label.administrative.group",
		getDelegateGroup().getName());
     }
 
     @Override
     public Set<User> getMembers() {
 	return getDelegateGroup().getMembers();
     }
 
     @Override
     public void delete() {
 	removeDelegateGroup();
 	super.delete();
     }
 
 }
