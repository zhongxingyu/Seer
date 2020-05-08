 package de.enwida.web;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import de.enwida.web.model.Group;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.Role;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IUserService;
 import de.enwida.web.utils.Constants;
 
 @Transactional
 public class TestUtils {
 
 	public static void cleanupDatabase(IUserService userService) throws Exception {
		for (final Right right : userService.fetchAllRights()) {
 			userService.deleteRight(right.getRightID());
 		}
 		for (final Role role : userService.fetchAllRoles()) {
 			userService.deleteRole(role.getRoleID());
 		}
 		for (final Group group : userService.fetchAllGroups()) {
 			userService.deleteGroup(group.getGroupID());
 		}
 		for (final User user : userService.fetchAllUsers()) {
 			userService.deleteUser(user.getUserId());
 		}
 	}
 	
 	public static void cleanupDatabase(Connection connection) throws Exception {
 		clearTable(connection, Constants.RIGHT_TABLE_SCHEMA_NAME + "." + Constants.RIGHT_TABLE_NAME);
 		clearTable(connection, Constants.ROLE_TABLE_SCHEMA_NAME + "." + Constants.ROLE_TABLE_NAME);
 		clearTable(connection, Constants.GROUP_TABLE_SCHEMA_NAME + "." + Constants.GROUP_TABLE_NAME);
 		clearTable(connection, Constants.USER_TABLE_SCHEMA_NAME + "." + Constants.USER_TABLE_NAME);
 	}
 	
 	public static void clearTable(Connection connection, String tableName) throws Exception {
 		PreparedStatement stmt = connection.prepareStatement("TRUNCATE " + tableName + " CASCADE");
 		stmt.execute();
 	}
 	
 	public static void recreateUsersSchema(Connection connection) throws Exception {
 		final PreparedStatement stmt = connection.prepareStatement("DROP SCHEMA ? CASCADE; CREATE SCHEMA ?");
 		stmt.setString(1, Constants.USERS_SCHEMA_NAME);
 		stmt.setString(2, Constants.USERS_SCHEMA_NAME);
 	}
 	
 }
