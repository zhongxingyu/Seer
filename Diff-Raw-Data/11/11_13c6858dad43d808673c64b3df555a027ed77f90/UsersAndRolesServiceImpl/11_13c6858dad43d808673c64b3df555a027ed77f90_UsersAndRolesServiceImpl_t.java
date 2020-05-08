 package com.dynamobi.ws.impl;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.jws.WebService;
 import javax.ws.rs.Path;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.dynamobi.ws.api.UsersAndRolesService;
 import com.dynamobi.ws.domain.UserDetails;
 import com.dynamobi.ws.domain.SessionInfo;
 import com.dynamobi.ws.domain.RolesDetails;
 import com.dynamobi.ws.domain.RolesDetailsHolder;
 import com.dynamobi.ws.domain.PermissionsInfo;
 import com.dynamobi.ws.util.AppException;
 import com.dynamobi.ws.util.DBAccess;
 
 /**
  * UsersAndRolesService implementation
  * 
  * @author Kevin Secretan
  * @since June 24, 2010
  */
 @WebService(endpointInterface = "com.dynamobi.ws.api.UsersAndRolesService")
 @Path("/users")
 public class UsersAndRolesServiceImpl implements UsersAndRolesService {
 
 
   /* TODO: use prepared statements / better db interface
    */
 
   public List<UserDetails> getUsersDetails() throws AppException {
     List<UserDetails> details = new ArrayList<UserDetails>();
     try {
       final String query = "SELECT name, password, creation_timestamp, "
         + "modification_timestamp FROM sys_root.dba_users u WHERE "
         + "u.name <> '_SYSTEM'";
       ResultSet rs = DBAccess.rawResultExec(query);
       while (rs.next()) {
         UserDetails ud = new UserDetails();
         ud.name = rs.getString(1);
         ud.password = rs.getString(2);
         ud.creation_timestamp = rs.getString(3);
         ud.modification_timestamp = rs.getString(4);
         details.add(ud);
       }
     } catch (SQLException e) {
       e.printStackTrace();
     }
 
     return details;
   }
 
   public List<SessionInfo> getCurrentSessions() throws AppException {
     return DBAccess.getCurrentSessions();
   }
 
   private enum Action { NEW, MOD, DEL };
   private String userAction(String user, String password, Action act)
                  throws AppException {
     // TODO: rewrite this to use prepared statements
     String query = "";
     if (act == Action.NEW) {
       query = "CREATE USER " + user + " IDENTIFIED BY '" + password + "'";
     } else if (act == Action.MOD) {
       query = "CREATE OR REPLACE USER " + user + " IDENTIFIED BY '" + password + "'";
     } else if (act == Action.DEL) {
       query = "DROP USER " + user;
     }
 
     String retval = "";
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
   }
 
   public String addNewUser(String user, String password) throws AppException {
     return userAction(user, password, Action.NEW);
   }
 
   // TODO: also adjust the roles when modifying and deleting.
   // Research: what happens if I just change the name/password in
   // the table rather than doing CREATE or REPLACE? That would be a better
   // solution and should preserve roles due to mofIds remaining the same.
   // Just gotta find out if user/pass are in more than one place.
   public String modifyUser(String user, String password) throws AppException {
     return userAction(user, password, Action.MOD);
   }
 
   public String deleteUser(String user) throws AppException {
     return userAction(user, "", Action.DEL);
   }
 
   public RolesDetailsHolder getRolesDetails() throws AppException {
     String query = "SELECT DISTINCT granted_catalog, granted_schema, granted_element, "
       + "grantee, grantor, action, "
       + "CASE WHEN r.role IS NOT NULL THEN r.role "
       + "WHEN grant_type = 'Role' THEN grantee END AS role_name, "
      + "grant_type, class_name, g.with_grant_option "
       + "FROM sys_root.dba_element_grants g left outer join sys_root.dba_user_roles r "
       + "ON action = 'INHERIT_ROLE' AND r.role_mof_id = element_mof_id "
       + "WHERE (grant_type = 'Role' AND grantee <> 'PUBLIC' "
       + "AND grantee <> '_SYSTEM') OR action = 'INHERIT_ROLE'";
     Map<String, RolesDetails> details = new LinkedHashMap<String, RolesDetails>();
     Map<String, PermissionsInfo> perms = new LinkedHashMap<String, PermissionsInfo>();
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
       while (rs.next()) {
         int col = 1;
         final String catalog = rs.getString(col++);
         final String schema = rs.getString(col++);
         final String element = rs.getString(col++);
         final String grantee = rs.getString(col++);
         final String grantor = rs.getString(col++);
         final String action = rs.getString(col++);
         final String role_name = rs.getString(col++);
         final String grant_type = rs.getString(col++);
        final String class_name = rs.getString(col++);
         final boolean with_grant = rs.getBoolean(col++);
 
         RolesDetails rd;
         if (!details.containsKey(role_name)) {
           rd = new RolesDetails();
           rd.name = role_name;
           details.put(role_name, rd);
         } else {
           rd = details.get(role_name);
         }
 
         // Dealing with a users or permissions entry?
         if (grant_type.equals("User")) {
           rd.users.add(grantee);
           if (with_grant) {
             rd.users_with_grant_option.add(grantee);
           }
         } else if (grant_type.equals("Role")) {
          final String key = role_name + catalog + schema + element + class_name;
           PermissionsInfo p;
           if (!perms.containsKey(key)) {
             p = new PermissionsInfo();
             p.catalog_name = catalog;
             p.schema_name = schema;
             p.item_name = element;
            p.item_type = class_name;
             perms.put(key, p);
             rd.permissions.add(p);
           } else {
             p = perms.get(key);
           }
           p.actions.add(action);
         } else {
           throw new AppException("Unknown grant type");
         }
 
       }
 
       ResultSet rs_extra_roles = DBAccess.rawResultExec("SELECT name FROM sys_root.dba_roles WHERE name <> 'PUBLIC'");
       while (rs_extra_roles.next()) {
         final String role_name = rs_extra_roles.getString(1);
         if (!details.containsKey(role_name)) {
           RolesDetails rd = new RolesDetails();
           rd.name = role_name;
           details.put(role_name, rd);
         }
       }
 
     } catch (SQLException e) {
       e.printStackTrace();
     }
 
     RolesDetailsHolder rh = new RolesDetailsHolder();
     rh.value.addAll(details.values());
     return rh;
   }
 
   public String addNewRole(String role) throws AppException {
     String retval = "";
     String query = "CREATE ROLE " + role;
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
   }
 
   public String deleteRole(String role) throws AppException {
     String retval = "";
     String query = "DROP ROLE " + role;
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
   }
 
   public String userToRole(String user, String role, boolean added, boolean with_grant) throws AppException {
     String retval = "";
     String query = "";
     if (added) {
       query = "GRANT ROLE " + role + " TO \"" + user + "\"";
       if (with_grant)
         query += " WITH GRANT OPTION";
     } else {
       // oh yeah, I forgot. 
       // TODO: get ability to remove users from roles.
       // Note: apparently only one user can have grant option?
       retval = "Not Implemented: REVOKE";
       return retval;
     }
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
   }
 
   public String grantPermissionsOnSchema(String catalog, String schema,
                 String permissions, String grantee) throws AppException {
     String retval = "";
     /*String query = "CALL APPLIB.DO_FOR_ENTIRE_SCHEMA('GRANT " +
       permissions + " ON %TABLE_NAME% TO \"" + grantee + "\"', " +
       "'" + schema + "', 'TABLES_AND_VIEWS')";
       * cannot do above because do_for_entire_schema does not respect
       * catalogs.
       */
     try {
       ResultSet rs = DBAccess.rawResultExec("SELECT TABLE_NAME "
           + "FROM SYS_ROOT.DBA_TABLES "
           + "WHERE CATALOG_NAME = '" + catalog + "' AND "
           + "SCHEMA_NAME = '" + schema + "'");
       while (rs.next()) {
         String query = "GRANT " + permissions + " ON " +
           "\"" + catalog + "\".\"" + schema + "\".\"" + rs.getString(1) + "\" "
           + "TO \"" + grantee + "\"";
         DBAccess.rawResultExec(query);
       }
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
   }
 
   public String grantPermissions(String catalog, String schema, String type,
                 String element, String permissions, String grantee)
                 throws AppException {
     String retval = "";
     String query = "GRANT " + permissions + " ON \"" + catalog +
      "\".\"" + schema + "\".\"" + element + "\" TO \"" + grantee + "\"";
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
   }
 
   public String revokePermissionsOnSchema(String catalog, String schema,
                 String permissions, String grantee) throws AppException {
     return "Not Implemented";
     /*String retval = "";
     try {
       ResultSet rs = DBAccess.rawResultExec("SELECT TABLE_NAME "
           + "FROM SYS_ROOT.DBA_TABLES "
           + "WHERE CATALOG_NAME = '" + catalog + "' AND "
           + "SCHEMA_NAME = '" + schema + "'");
       while (rs.next()) {
         String query = "REVOKE " + permissions + " ON " +
           "\"" + catalog + "\".\"" + schema + "\".\"" + rs.getString(1) + "\" "
           + "FROM \"" + grantee + "\"";
         DBAccess.rawResultExec(query);
       }
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
     */
   }
 
   public String revokePermissions(String catalog, String schema, String type,
                 String element, String permissions, String grantee)
                 throws AppException {
     return "Not Implemented";
     /*String retval = "";
     String query = "REVOKE " + permissions + " ON \"" + catalog +
      "\".\"" + schema + "\".\"" + element + "\" FROM \"" + grantee + "\"";
     try {
       ResultSet rs = DBAccess.rawResultExec(query);
     } catch (SQLException e) {
       e.printStackTrace();
       retval = e.getMessage();
     }
     return retval;
     */
   }
 
 }
