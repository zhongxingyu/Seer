 package com.infinitiessoft.btrs.action;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.framework.EntityHome;
 import org.jboss.seam.log.Log;
 import org.jboss.seam.security.management.IdentityManager;
 import org.jboss.seam.security.management.JpaIdentityStore;
 
 import com.infinitiessoft.btrs.enums.GenderEnum;
 import com.infinitiessoft.btrs.model.Department;
 import com.infinitiessoft.btrs.model.Report;
 import com.infinitiessoft.btrs.model.Role;
 import com.infinitiessoft.btrs.model.StatusChange;
 import com.infinitiessoft.btrs.model.User;
 
 @Name("userHome")
 public class UserHome extends EntityHome<User> {
 
 	private static final long serialVersionUID = -5368319908832175365L;
 	
 	@Logger private Log log;
 	
 	@In(create = true)
 	DepartmentHome departmentHome;
 
 	@In
 	IdentityManager identityManager;
 	
 	@In(create = true)
 	RoleList roleList;
 	
 	public void setUserId(Long id) {
 		setId(id);
 	}
 
 	public Long getUserId() {
 		return (Long) getId();
 	}
 
 	@Override
 	protected User createInstance() {
 		User user = new User();
 		return user;
 	}
 
 	public void load() {
 		if (isIdDefined()) {
 			wire();
 		}
 	}
 
 	public void wire() {
 		getInstance();
 		Department department = departmentHome.getDefinedInstance();
 		if (department != null) {
 			getInstance().setDepartment(department);
 		}
 	}
 
 	public boolean isWired() {
 		return true;
 	}
 
 	public User getDefinedInstance() {
 		return isIdDefined() ? getInstance() : null;
 	}
 
 	public List<Report> getIncomingReports() {
 		return getInstance() == null ? null : new ArrayList<Report>(
 				getInstance().getIncomingReports());
 	}
 	public List<Report> getOutgoingReports() {
 		return getInstance() == null ? null : new ArrayList<Report>(
 				getInstance().getOutgoingReports());
 	}
 	public List<StatusChange> getStatusChanges() {
 		return getInstance() == null ? null : new ArrayList<StatusChange>(
 				getInstance().getStatusChanges());
 	}
 	
 	public List<Role> getRoles() {
 		return getInstance() == null ? null : new ArrayList<Role>(getInstance().getRoles());
 	}
 	
 	public String remove(User user) {
		setInstance(user);
 		return remove();
 	}
 	
 	public String registerNew() {
 		log.debug("registration has started: {}", getInstance());
 		identityManager.getIdentityStore().createUser(getInstance().getUsername(), getInstance().getPassword());
 		createdMessage();
 		return "registered";
 	}
 	
 	@Observer(JpaIdentityStore.EVENT_PRE_PERSIST_USER)
 	public void preProcessRegistration(User user) {
 		log.debug("registration preprocessing has started: {}", user);
 		User userData = getInstance();
 		user.setDepartment(userData.getDepartment());
 		user.setEmail(userData.getEmail());
 		user.setGender(userData.getGender());
 		user.setFirstName(userData.getFirstName());
 		user.setLastName(userData.getLastName());
 		user.setJobTitle(userData.getJobTitle());
 		user.setCreatedDate(new Date());
 		
 		user.getRoles().add(roleList.getDefaultRole());
 		log.debug("registration preprocessing has finished: {}", user);
 	}
 	
 	@Observer(JpaIdentityStore.EVENT_USER_AUTHENTICATED)
 	public void updateLastLoginDate(User user) {
		setInstance(user);
 		user.setLastLogin(new Date());
 		update();
 	}
 	
 	@Factory(value = "genders", scope = ScopeType.CONVERSATION)
 	public GenderEnum[] getGenders() {
 		return GenderEnum.values();
 	}
 	
 }
