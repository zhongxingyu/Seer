 package ro.gagarin;
 
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ro.gagarin.exceptions.DataConstraintException;
 import ro.gagarin.exceptions.ItemNotFoundException;
 import ro.gagarin.exceptions.OperationException;
 import ro.gagarin.exceptions.PermissionDeniedException;
 import ro.gagarin.exceptions.SessionNotFoundException;
import ro.gagarin.user.UserPermission;
 import ro.gagarin.user.UserRole;
 import ro.gagarin.ws.Authentication;
 import ro.gagarin.ws.UserService;
 import ro.gagarin.ws.objects.WSUser;
 import ro.gagarin.ws.objects.WSUserPermission;
 import ro.gagarin.ws.objects.WSUserRole;
 
 /**
  * Unit test for simple App.
  */
 public class UserServiceTest {
 	private static Authentication authentication = new Authentication();
 	private static String username = "_User_" + System.currentTimeMillis();
 	private static String session;
 
 	@BeforeClass
 	public static void startup() throws SessionNotFoundException, ItemNotFoundException,
 			OperationException, DataConstraintException {
 		session = authentication.createSession(null, null);
 		authentication.login(session, "admin", "password", null);
 	}
 
 	@Test
 	public void testCreateUser() throws SessionNotFoundException, ItemNotFoundException,
 			PermissionDeniedException, DataConstraintException, OperationException {
 
 		UserService userService = new UserService();
 
 		List<WSUserRole> roles = userService.getRoleList(session);
 
 		WSUser user = new WSUser();
 		user.setUsername(username);
 		user.setPassword("password");
 		user.setRole(roles.get(0));
 
 		userService.createUser(session, user);
 	}
 
 	@Test
 	public void testCreateRole() throws SessionNotFoundException, PermissionDeniedException,
 			OperationException, ItemNotFoundException {
 		UserService userService = new UserService();
 
 		UserRole role = userService.createRoleWithPermissions(session, "WONDER_ROLE",
				new UserPermission[] { new WSUserPermission("CREATE_USER") });
 		userService.getRolePermissions(session, new WSUserRole("WONDER_ROLE"));
 
 	}
 }
