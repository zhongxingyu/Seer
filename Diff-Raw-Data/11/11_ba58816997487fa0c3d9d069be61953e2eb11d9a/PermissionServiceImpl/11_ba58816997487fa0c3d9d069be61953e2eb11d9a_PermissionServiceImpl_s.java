 /**
  * 
  */
 package org.cotrix.web.permissionmanager.server;
 
 import static org.cotrix.domain.dsl.Users.*;
 import static org.cotrix.repository.user.UserQueries.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 
 import org.cotrix.action.Action;
 import org.cotrix.action.ResourceType;
 import org.cotrix.application.PermissionDelegationService;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.dsl.Roles;
 import org.cotrix.domain.user.FingerPrint;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.cotrix.repository.codelist.CodelistRepository;
 import org.cotrix.repository.user.UserRepository;
 import org.cotrix.web.permissionmanager.client.PermissionService;
 import org.cotrix.web.permissionmanager.server.util.RolesSorter;
 import org.cotrix.web.permissionmanager.shared.CodelistGroup;
 import org.cotrix.web.permissionmanager.shared.RoleAction;
 import org.cotrix.web.permissionmanager.shared.RolesRow;
 import org.cotrix.web.permissionmanager.shared.RolesType;
 import org.cotrix.web.permissionmanager.shared.UIUser;
 import org.cotrix.web.permissionmanager.shared.UIUserDetails;
 import org.cotrix.web.share.server.util.CodelistLoader;
 import org.cotrix.web.share.server.util.ValueUtils;
 import org.cotrix.web.share.shared.DataWindow;
 import org.cotrix.web.share.shared.exception.ServiceException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class PermissionServiceImpl extends RemoteServiceServlet implements PermissionService {
 
 	private static final long serialVersionUID = 2621331963385532979L;
 	
 	
 	protected Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);
 	
 	@Inject
 	protected CodelistRepository codelistRepository;
 	
 	@Inject
 	protected CodelistLoader codelistLoader;
 	
 	@Inject
 	protected UserRepository userRepository;
 	
 	@Inject
 	protected RolesSorter rolesSorter;
 	
 	@Inject
 	protected PermissionDelegationService delegationService;
 	
 	@Current
 	@Inject
 	protected User currentUser;
 	
 	public void init() {
 //		codelistLoader.importAllCodelist();
 //		logger.trace("codelist in repository:");
 //		for (Codelist codelist:codelistRepository.get(allLists())) logger.trace(codelist.name().toString());
 //		logger.trace("done");
 	}
 
 	@Override
 	public List<String> getRoles(RolesType type) throws ServiceException {
 		logger.trace("getRoles type {}", type);
 		switch (type) {
 			case APPLICATION: return toRoles(Roles.getBy(ResourceType.application));
 			case CODELISTS: return  toRoles(Roles.getBy(ResourceType.codelists));
 		}
 		return new ArrayList<String>();
 	}
 	
 	protected List<String> toRoles(Collection<Role> roles) {
 		List<String> uiRoles = new ArrayList<String>(roles.size());
 		for (Role role:roles) uiRoles.add(role.name());
 		return uiRoles;
 	}
 
 	@Override
 	public DataWindow<RolesRow> getApplicationRolesRows() throws ServiceException {
 		logger.trace("getApplicationRolesRows");
 		List<RolesRow> rows = new ArrayList<RolesRow>();
 		
 		for (User user:userRepository.get(allUsers())) {
 			RolesRow row = getApplicationRolesRow(user);
 			rows.add(row);
 		}
 		
 		rolesSorter.syncUser();
 		Collections.sort(rows, rolesSorter);
 		logger.trace("rows: {}", rows);
 		return new DataWindow<RolesRow>(rows);
 	}
 	
 	protected RolesRow getApplicationRolesRow(User user) {
 		FingerPrint fp = user.fingerprint();
 		Collection<String> userRoles = fp.rolesOver(Action.any, ResourceType.application);
 		RolesRow row = new RolesRow(toUiUser(user), getRoles(userRoles), currentUser.id().equals(user.id()));
 		return row;
 	}
 
 	@Override
 	public DataWindow<RolesRow> getCodelistRolesRows(String codelistId)	throws ServiceException {
 		logger.trace("getCodelistRolesRows codelistId {}", codelistId);
 		
 		List<RolesRow> rows = new ArrayList<RolesRow>();
 		
 		for (User user:userRepository.get(allUsers().with(roleOn(codelistId, ResourceType.codelists)))) {
 			
 			// no roots
 			if (user.isRoot()) continue;
 			
 			RolesRow row = getCodelistRolesRow(user, codelistId);
 			if (row!=null) rows.add(row);
 		}
 		rolesSorter.syncUser();
 		Collections.sort(rows, rolesSorter);
 		return new DataWindow<RolesRow>(rows);
 
 	}
 	
 	protected RolesRow getCodelistRolesRow(User user, String codelistId) {
 		FingerPrint fp = user.fingerprint();
		Collection<String> userRoles = fp.rolesOver(codelistId, ResourceType.codelists);		
 		RolesRow row = new RolesRow(toUiUser(user), getRoles(userRoles), currentUser.id().equals(user.id()));
 		return row;
 	}
 
 	@Override
 	public void codelistRoleUpdated(String userId, String codelistId, String roleName, RoleAction action) {
 		logger.trace("codelistRoleUpdated userId: {} codelistId: {} role: {} action: {}", userId, codelistId, roleName, action);
 		
 		User target = userRepository.lookup(userId);
 		Role role = toRole(roleName).on(codelistId);
 		logger.trace("role for name {}: {}", roleName, role);
 		
 		switch (action) {
 			case DELEGATE: delegationService.delegate(role).to(target); break;
 			case REVOKE: delegationService.revoke(role).from(target); break;
 		}
 	}
 	
 	protected Role[] toRoles(List<String> uiRoles) {
 		Role[] roles = new Role[uiRoles.size()];
 		for (int i = 0; i < roles.length; i++) {
 			roles[i] = toRole(uiRoles.get(i));
 		}
 		return roles;
 	}
 	
 	protected Role toRole(String name) {
 		for (Role role:Roles.predefinedRoles) {
 			if (role.name().equals(name)) return role;
 		}
 		throw new IllegalArgumentException("Unknown role name "+name);
 	}
 
 	@Override
 	public void applicationRoleUpdated(String userId, String roleName, RoleAction action) {
 		logger.trace("applicationRoleUpdated userId: {} role: {} action: {}", userId, roleName, action);
 		User target = userRepository.lookup(userId);
 		Role role = toRole(roleName);
 		logger.trace("role for name {}: {}", roleName, role);
 		
 		switch (action) {
 			case DELEGATE: delegationService.delegate(role).to(target); break;
 			case REVOKE: delegationService.revoke(role).from(target); break;
 		}
 	}
 
 	@Override
 	public DataWindow<CodelistGroup> getCodelistGroups() throws ServiceException {
 		logger.trace("getCodelistGroups");
 
 		Map<QName, CodelistGroup> groups = new HashMap<QName, CodelistGroup>();
 
 		FingerPrint fp = currentUser.fingerprint();
 		
 		for (String codelistId:fp.resources(ResourceType.codelists)) {
 
 			logger.trace("checking codelist "+codelistId);
 			
 			//the user has any role with this codelist?
 			if (fp.rolesOver(codelistId, ResourceType.codelists).isEmpty()) continue;
 			
 			//the user is a semi-root for the specified codelist?
 			if (Action.any.equals(codelistId)) continue;
 			
 			Codelist codelist = codelistRepository.lookup(codelistId);
 		
 		/*for (Codelist codelist:codelistRepository.get(allLists())) {*/
 			
 			CodelistGroup group = groups.get(codelist.name());
 			if (group == null) {
 				group = new CodelistGroup(codelist.name().toString());
 				groups.put(codelist.name(), group);
 			}
 			List<String> roles = getRoles(currentUser.fingerprint().rolesOver(codelist.id(), ResourceType.codelists));
 			group.addVersion(codelist.id(), ValueUtils.safeValue(codelist.version()), roles);
 		}
 		
 		for (CodelistGroup group:groups.values()) Collections.sort(group.getVersions()); 
 		
 		return new DataWindow<CodelistGroup>(new ArrayList<CodelistGroup>(groups.values()));
 	}
 
 	@Override
 	public List<UIUser> getUsers() throws ServiceException {
 		logger.trace("getUsers");
 		List<UIUser> users = new ArrayList<UIUser>();
 		
 		for (User user:userRepository.get(allUsers())) {
 			users.add(toUiUser(user));
 		}
 		return users;
 	}
 	
 	protected UIUser toUiUser(User user) {
 		return new UIUser(user.id(), user.fullName());
 	}
 	
 	protected List<String> getRoles(Collection<String> roles) {
 		if (roles == null) return null;
 		return new ArrayList<String>(roles);
 	}
 
 	@Override
 	public UIUserDetails getUserDetails() throws ServiceException {
 		logger.trace("getUserDetails");
 		UIUserDetails userDetails = new UIUserDetails();
 		userDetails.setId(currentUser.id());
 		userDetails.setFullName(currentUser.fullName());
 		userDetails.setUsername(currentUser.name());
 		//TODO update
 		userDetails.setEmail("n/a");
 		return userDetails;
 	}
 
 	@Override
 	public void saveUserDetails(UIUserDetails userDetails) throws ServiceException {
 		logger.trace("saveUserDetails userDetails: {}", userDetails);
 		//TODO email
 		User changeSet = user(currentUser).fullName(userDetails.getFullName()).build();
 		userRepository.update(changeSet);
 	}
 
 }
