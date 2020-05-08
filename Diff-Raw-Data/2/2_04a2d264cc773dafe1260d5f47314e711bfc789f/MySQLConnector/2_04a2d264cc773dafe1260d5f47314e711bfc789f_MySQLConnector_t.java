 package com.chariotsolutions.crowd.connector;
 
 import com.atlassian.crowd.integration.SearchContext;
 import com.atlassian.crowd.integration.authentication.PasswordCredential;
 import com.atlassian.crowd.integration.directory.RemoteDirectory;
 import com.atlassian.crowd.integration.exception.*;
 import com.atlassian.crowd.integration.model.DirectoryEntity;
 import com.atlassian.crowd.integration.model.RemoteGroup;
 import com.atlassian.crowd.integration.model.RemotePrincipal;
 import com.atlassian.crowd.integration.model.RemoteRole;
 import com.atlassian.crowd.manager.directory.DirectoryManager;
 import com.atlassian.crowd.model.directory.Directory;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 import javax.sql.DataSource;
 import java.rmi.RemoteException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Collections;
 import java.util.List;
 
 
 public class MySQLConnector extends DirectoryEntity implements RemoteDirectory {
 
     public final static String ATTRIBUTE_COMPANY = "company";
     public final static String ATTRIBUTE_TITLE = "title";
     public final static String ATTRIBUTE_PHONE = "phone";    
     public final static String ATTRIBUTE_COUNTRY = "country";
 
     private static final Logger log = Logger.getLogger(MySQLConnector.class);
     private SimpleJdbcTemplate simpleJdbcTemplate;
     private PrincipalRowMapper principalMapper = new PrincipalRowMapper();
     private Directory directory;
     private DirectoryManager directoryManager;
 
     ParameterizedRowMapper<RemoteGroup> groupMapper = new ParameterizedRowMapper<RemoteGroup>() {
         public RemoteGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
             RemoteGroup group = new RemoteGroup();
             group.setID(rs.getLong("id"));
             group.setName(rs.getString("group_name"));
             group.setDescription(rs.getString("description"));
             group.setActive(!rs.getBoolean("disabled"));
             group.setConception(rs.getDate("created"));
             group.setDirectory(getDirectory());
             return group;
         }
     };
 
     public static String getDirectoryType() {
         return "Open IONA Connector";
     }    
 
     @Autowired(required = true)
     public void setDirectoryManager(DirectoryManager directoryManager) {
         this.directoryManager = directoryManager;
     }
 
     @Autowired(required = true)
     public void setMySqlConnectorDataSource(DataSource dataSource) {
         log.debug("Setting Jellico DataSource");
         this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
     }
 
     private Directory getDirectory() {
         if (directory == null) {
             try {
                 directory = directoryManager.findDirectoryByID(getID());
             } catch (ObjectNotFoundException e) { // this shouldn't happen
                 throw new RuntimeException("The directory manager can't find us.", e);
             }
         }
         return directory;
     }
 
     public RemotePrincipal authenticate(String name, PasswordCredential[] credentials) throws RemoteException, InvalidPrincipalException, InactiveAccountException, InvalidAuthenticationException {
 
         if (name == null) {
             log.debug("Ignoring login attempt where name is null");
             throw new InvalidAuthenticationException("Login failed " + name);
         }
 
         if (credentials == null || credentials.length < 1) {
             log.error("Login failed for " + name + " (no password supplied)");
             throw new InvalidAuthenticationException("Login failed - no password supplied");
         }
 
         String password = getPassword(credentials);
 
         String sql = "SELECT * FROM user WHERE user_name = ? and password = ?";
 
         RemotePrincipal principal;
         try {
             principal = simpleJdbcTemplate.queryForObject(sql, principalMapper, name, password);
 
             if (!principal.isActive()) {
                 throw new InactiveAccountException("Account disabled " + name);
             }
 
             log.info("Successful login for " + name);
 
         } catch (EmptyResultDataAccessException e) {
             throw new InvalidAuthenticationException("Login failed " + name, e);
         }
         return principal;
 
     }
 
 
     public RemotePrincipal addPrincipal(RemotePrincipal principal) throws InvalidPrincipalException, ObjectNotFoundException, InvalidCredentialException {
         String sql = "INSERT INTO user " +
                 "(user_name, first_name, last_name, email, company, title, phone, country, account_disabled, password, created) " +
                 "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
 
         simpleJdbcTemplate.update(
                 sql,
                 principal.getName(),
                 principal.getAttribute(RemotePrincipal.FIRSTNAME).getSingleValue(),
                 principal.getAttribute(RemotePrincipal.LASTNAME).getSingleValue(),
                 principal.getEmail(),
                 principal.getAttribute(ATTRIBUTE_COMPANY).getSingleValue(),
                 principal.getAttribute(ATTRIBUTE_TITLE).getSingleValue(),
                 principal.getAttribute(ATTRIBUTE_PHONE).getSingleValue(),
                 principal.getAttribute(ATTRIBUTE_COUNTRY).getSingleValue(),
                 !principal.isActive(),
                 getPassword(principal),
                 new Date()
         );
 
         return findPrincipalByName(principal.getName());
     }
 
     public void removePrincipal(String name) throws ObjectNotFoundException {
         int user_id = simpleJdbcTemplate.queryForInt("SELECT id FROM user WHERE user_name = ?", name);
         simpleJdbcTemplate.update("DELETE FROM user_group WHERE user_id = ?", user_id);
         simpleJdbcTemplate.update("DELETE FROM user WHERE user_name = ?", name);
     }
 
     public void updatePrincipalCredential(String name, PasswordCredential credentials) throws ObjectNotFoundException, InvalidCredentialException {
         String password = credentials.getCredential(); // password is plaintext here
         String sql = "UPDATE user SET password = ?";
         simpleJdbcTemplate.update(sql, password);
     }
 
     // TODO implement pageing (see old implementation for details)
     public List<RemotePrincipal> searchPrincipals(SearchContext searchContext) throws InvalidSearchTermException {
 
         String sql = "SELECT * FROM user";
 
         if (searchContext.containsKey(SearchContext.PRINCIPAL_NAME)) {
             sql = sql + " WHERE user_name LIKE '" + searchContext.get(SearchContext.PRINCIPAL_NAME) + "'";
         }
 
         if (searchContext.containsKey(SearchContext.PRINCIPAL_EMAIL)) {
             if (sql.endsWith("user")) {
                 sql += " WHERE";
             } else {
                 sql += " AND";
             }
             sql = sql + " email LIKE '" + searchContext.get(SearchContext.PRINCIPAL_EMAIL) + "'";
         }
 
         if (searchContext.containsKey(SearchContext.PRINCIPAL_ACTIVE)) {
             if (sql.endsWith("user")) {
                 sql += " WHERE";
             } else {
                 sql += " AND";
             }
 
             if ((Boolean) searchContext.get(SearchContext.PRINCIPAL_ACTIVE)) {
                 sql = sql + " account_disabled = '0'";
             } else {
                 sql = sql + " account_disabled = '1'";
             }
         }
 
         return simpleJdbcTemplate.query(sql, principalMapper);
     }
 
     public RemotePrincipal findPrincipalByName(String name) throws ObjectNotFoundException {
         try {
             String sql = "SELECT * FROM user WHERE user_name like ?";
             return simpleJdbcTemplate.queryForObject(sql, principalMapper, name);
         } catch (EmptyResultDataAccessException e) {
             throw new ObjectNotFoundException(name);
         }
     }
 
     public RemotePrincipal updatePrincipal(RemotePrincipal principal) throws ObjectNotFoundException {
 
         String sql = "UPDATE user " +
                 "SET first_name = ?, last_name = ?, email = ?, company = ?, " +
                 "title = ?, phone = ?, country = ?, account_disabled = ? " +
                 "WHERE user_name = ?";
 
         simpleJdbcTemplate.update(sql,
                 principal.getAttribute(RemotePrincipal.FIRSTNAME).getSingleValue(),
                 principal.getAttribute(RemotePrincipal.LASTNAME).getSingleValue(),
                 principal.getEmail(),
                 principal.getAttribute(ATTRIBUTE_COMPANY).getSingleValue(),
                 principal.getAttribute(ATTRIBUTE_TITLE).getSingleValue(),
                 principal.getAttribute(ATTRIBUTE_PHONE).getSingleValue(),
                 principal.getAttribute(ATTRIBUTE_COUNTRY).getSingleValue(),
                 !principal.isActive(),
                 principal.getName()
         );
 
         return findPrincipalByName(principal.getName());
     }
 
     public void addPrincipalToRole(String name, String unsubscribedRole) throws ObjectNotFoundException {
         throw new UnsupportedOperationException("addPrincipalToRole");
     }
 
     public void removePrincipalFromRole(String name, String subscribedRole) throws ObjectNotFoundException {
         throw new UnsupportedOperationException("removePrincipalToRole");
     }
 
     public boolean isRoleMember(String role, String principal) throws ObjectNotFoundException {
         // unsupported operation
         return false;
     }
 
     public List<RemoteRole> findRoleMemberships(String principalName) throws ObjectNotFoundException {
         // unsupported operation
         return Collections.emptyList();
     }
 
     public List<RemoteGroup> findGroupMemberships(String principalName) throws ObjectNotFoundException {
 
         String sql = "select g.* " +
                 "from group_table g, user_groups ug, user u " +
                 "where ug.group_id = g.id " +
                 "and ug.user_id = u.id " +
                 "and u.user_name = ?";
 
         return this.simpleJdbcTemplate.query(sql, groupMapper, principalName);
     }
 
     public boolean supportsNestedGroups() {
         return false;
     }
 
     public boolean isGroupMember(String group, String principal) throws ObjectNotFoundException {
 
         String sql = "select count(*) " +
                 " from user u, group_table g, user_groups ug " +
                 " where ug.user_id = u.id " +
                 " and ug.group_id = g.id " +
                 " and u.user_name = ? " +
                 " and g.group_name = ?";
 
         int count = this.simpleJdbcTemplate.queryForInt(sql, principal, group);
 
         return count == 1;
     }
 
     public List<RemoteGroup> searchGroups(SearchContext searchContext) throws InvalidSearchTermException, ObjectNotFoundException {
         String sql = "SELECT * FROM group_table";
 
         if (searchContext.containsKey(SearchContext.GROUP_NAME)) {
             sql = sql + " WHERE group_name LIKE '" + searchContext.get(SearchContext.GROUP_NAME) + "'";
         }
 
         if (searchContext.containsKey(SearchContext.GROUP_ACTIVE)) {
             if (sql.endsWith("group_table")) {
                 sql += " WHERE";
             } else {
                 sql += " AND";
             }
 
             if ((Boolean) searchContext.get(SearchContext.GROUP_ACTIVE)) {
                 sql = sql + " disabled = '0'";
             } else {
                 sql = sql + " disabled = '1'";
             }
         }
 
         return simpleJdbcTemplate.query(sql, groupMapper);
     }
 
     public RemoteGroup findGroupByName(String name, boolean onlyFetchDirectMembers) throws ObjectNotFoundException {
         return findGroupByName(name);
     }
 
     public RemoteGroup findGroupByName(String name) throws ObjectNotFoundException {
         try {
             String sql = "SELECT * FROM group_table WHERE group_name = ?";
             return simpleJdbcTemplate.queryForObject(sql, groupMapper, name);
         } catch (EmptyResultDataAccessException e) {
             throw new ObjectNotFoundException(name);
         }
     }
 
     public RemoteGroup updateGroup(RemoteGroup group) throws ObjectNotFoundException {
         String sql = "UPDATE group_table SET description = ?, disabled = ? WHERE id = ?";
         simpleJdbcTemplate.update(sql, group.getDescription(), !group.isActive(), group.getID());
         return findGroupByName(group.getName());
     }
 
     public void addGroupToGroup(String parentGroup, String childGroup) throws ObjectNotFoundException, UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     public void removeGroupFromGroup(String parentGroup, String childGroup) throws ObjectNotFoundException, UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     public void addPrincipalToGroup(String name, String unsubscribedGroup) throws ObjectNotFoundException {
         String sql = "INSERT INTO user_groups (user_id, group_id) " +
                 "SELECT u.id, g.id FROM user u, group_table g WHERE u.user_name = ? and g.group_name = ?";
 
         simpleJdbcTemplate.update(sql, name, unsubscribedGroup);
     }
 
     public void removePrincipalFromGroup(String name, String subscribedGroup) throws ObjectNotFoundException {
        String sql = "DELETE FROM user_groups ug" +
                 "         USING user_groups ug,  user u, group_table g " +
                 "         WHERE u.id = ug.user_id AND g.id = ug.group_id " +
                 "         AND u.user_name = ?  AND g.group_name = ?";
 
         simpleJdbcTemplate.update(sql, name, subscribedGroup);
     }
 
     public List<RemotePrincipal> findAllGroupMembers(String groupName) throws ObjectNotFoundException {
         String sql = "SELECT u.* " +
                 "FROM user u, group_table g, user_groups ug " +
                 "WHERE u.id = ug.user_id " +
                 "AND g.id = ug.group_id " +
                 "AND g.group_name = ? " +
                 "ORDER BY disabled, user_name";
 
         return simpleJdbcTemplate.query(sql, principalMapper, groupName);
     }
 
     public RemoteGroup addGroup(RemoteGroup group) throws InvalidGroupException, ObjectNotFoundException {
         String sql = "INSERT INTO group_table (group_name, description, disabled, created) VALUES (?, ?, ?, ?)";
 
         simpleJdbcTemplate.update(
                 sql,
                 group.getName(),
                 group.getDescription(),
                 !group.isActive(),
                 new Date()
         );
 
         return findGroupByName(group.getName());
     }
 
     public void removeGroup(String group) throws ObjectNotFoundException {
         String sql = "DELETE FROM group_table WHERE group_name = ?";
         simpleJdbcTemplate.update(sql, group);
     }
 
     private String getPassword(PasswordCredential[] credentials) {
         PasswordCredential credential = credentials[0];
         return credential.getCredential();
     }
 
     private String getPassword(RemotePrincipal principal) {
         PasswordCredential credential = principal.getCredentials().get(0);                
         return credential.getCredential();
     }
 
     public void testConnection() throws RemoteException {
         // It this a good test?
         simpleJdbcTemplate.queryForInt("SELECT count(*) FROM user");
     }
 
     public List<RemotePrincipal> findAllRoleMembers(String role) throws ObjectNotFoundException {
         // unsupported operation
         return Collections.emptyList();
     }
 
     public List<RemoteRole> searchRoles(SearchContext searchContext) throws InvalidSearchTermException, ObjectNotFoundException {
         // unsupported operation
         return Collections.emptyList();
     }
 
     public RemoteRole findRoleByName(String roleName) throws ObjectNotFoundException {
         // unsupported operation
         throw new ObjectNotFoundException(roleName);
     }
 
     public RemoteRole addRole(RemoteRole role) throws InvalidRoleException, ObjectNotFoundException {
         throw new UnsupportedOperationException("addRole");
     }
 
     public RemoteRole updateRole(RemoteRole role) throws ObjectNotFoundException {
         throw new UnsupportedOperationException("updateRole");
     }
 
     public void removeRole(String role) throws ObjectNotFoundException {
         throw new UnsupportedOperationException("removeRole");
     }
 
 }
