 package ro.gagarin.ws;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 
 import org.apache.log4j.Logger;
 
 import ro.gagarin.AuthorizationManager;
 import ro.gagarin.BasicManagerFactory;
 import ro.gagarin.ManagerFactory;
 import ro.gagarin.RoleDAO;
 import ro.gagarin.SessionManager;
 import ro.gagarin.UserDAO;
 import ro.gagarin.exceptions.DataConstraintException;
 import ro.gagarin.exceptions.FieldRequiredException;
 import ro.gagarin.exceptions.ItemNotFoundException;
 import ro.gagarin.exceptions.OperationException;
 import ro.gagarin.exceptions.PermissionDeniedException;
 import ro.gagarin.exceptions.SessionNotFoundException;
 import ro.gagarin.session.Session;
 import ro.gagarin.user.PermissionEnum;
 import ro.gagarin.user.User;
 import ro.gagarin.user.UserPermission;
 import ro.gagarin.user.UserRole;
 import ro.gagarin.utils.ConversionUtils;
 import ro.gagarin.ws.objects.WSUser;
 import ro.gagarin.ws.objects.WSUserPermission;
 import ro.gagarin.ws.objects.WSUserRole;
 
 @WebService
 public class UserService {
 
 	private static final transient Logger LOG = Logger.getLogger(UserService.class);
 	private static final transient ManagerFactory FACTORY = BasicManagerFactory.getInstance();
 
 	@WebMethod
 	public Long createUser(String sessionId, WSUser user) throws SessionNotFoundException,
 			PermissionDeniedException, ItemNotFoundException, DataConstraintException,
 			OperationException {
 		LOG.info("createUser " + user.getUsername());
 
 		SessionManager sessionManager = FACTORY.getSessionManager();
 		Session session = sessionManager.acquireSession(sessionId);
 		UserDAO userManager = FACTORY.getDAOManager().getUserDAO(session);
 		AuthorizationManager permissionManager = FACTORY.getAuthorizationManager(session);
 
 		try {
 
 			// the session user must have CREATE_USER permission
 			permissionManager.requiresPermission(session, PermissionEnum.CREATE_USER);
 
 			// check user fields
 			if (user.getRole() == null) {
 				userManager.markRollback();
 				throw new FieldRequiredException("ROLE", User.class);
 			}
 
 			UserRole role = user.getRole();
 			if (role.getId() == null && role.getRoleName() != null) {
 				RoleDAO roleDAO = FACTORY.getDAOManager().getRoleDAO(session);
 				role = roleDAO.getRoleByName(role.getRoleName());
 				if (role == null) {
 					userManager.markRollback();
 					throw new ItemNotFoundException(UserRole.class, user.getRole().getRoleName());
 				}
 				user.setRole(role);
 			}
 
 			// the created user's permission list must not exceed session user's
 			// permissions
 			permissionManager.checkUserRole(session, user);
 
 			long userId = userManager.createUser(user);
 			LOG.info("Created User " + user.getId() + ":" + user.getUsername() + "; session:"
 					+ sessionId);
 			return userId;
 		} finally {
 			FACTORY.releaseSession(session);
 		}
 	}
 
 	@WebMethod
 	public List<WSUserRole> getRoleList(String sessionId) throws SessionNotFoundException,
 			PermissionDeniedException, OperationException {
 
 		SessionManager sessionManager = FACTORY.getSessionManager();
 		Session session = sessionManager.acquireSession(sessionId);
 		RoleDAO roleManager = FACTORY.getDAOManager().getRoleDAO(session);
 		AuthorizationManager permissionManager = FACTORY.getAuthorizationManager(session);
 
 		try {
 
 			// the session user must have LIST_ROLES permission
 			permissionManager.requiresPermission(session, PermissionEnum.LIST_ROLES);
 			List<UserRole> allRoles = roleManager.getAllRoles();
 			List<WSUserRole> convRoles = new ArrayList<WSUserRole>();
 			for (UserRole userRole : allRoles) {
 				convRoles.add(new WSUserRole(userRole));
 			}
 			return convRoles;
 
 		} finally {
 			FACTORY.releaseSession(session);
 		}
 	}
 
 	@WebMethod
 	public WSUserRole createRoleWithPermissions(String sessionId, String roleName,
			WSUserPermission[] permissions) throws SessionNotFoundException,
 			PermissionDeniedException, OperationException, ItemNotFoundException {
 
 		SessionManager sessionManager = FACTORY.getSessionManager();
 		Session session = sessionManager.acquireSession(sessionId);
 		RoleDAO roleManager = FACTORY.getDAOManager().getRoleDAO(session);
 		AuthorizationManager permissionManager = FACTORY.getAuthorizationManager(session);
 
 		try {
 
 			// the session user must have LIST_ROLES permission
 			permissionManager.requiresPermission(session, PermissionEnum.LIST_ROLES);
 			List<UserPermission> allPermissions = roleManager.getAllPermissions();
			List<UserPermission> matched;
			matched = ConversionUtils.matchPermissions(allPermissions, permissions);
 			permissionManager.checkUserHasThePermissions(session, matched);
 			return null;
 
 		} finally {
 			FACTORY.releaseSession(session);
 		}
 	}
 
 	@WebMethod
 	public List<WSUserPermission> getAllPermissionList(String session) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void getRolePermissions(String session, WSUserRole wsUserRole) {
 		// TODO Auto-generated method stub
 
 	}
 }
