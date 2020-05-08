 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.ejb.beans;
 
 import com.flexive.core.Database;
 import com.flexive.core.LifeCycleInfoImpl;
import com.flexive.core.security.FxDBAuthentication;
 import com.flexive.core.security.LoginLogoutHandler;
 import com.flexive.core.security.UserTicketImpl;
 import com.flexive.core.security.UserTicketStore;
import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import static com.flexive.core.DatabaseConst.*;

 /**
  * Account management
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "AccountEngine", mappedName = "AccountEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class AccountEngineBean implements AccountEngine, AccountEngineLocal {
 
     private static final Log LOG = LogFactory.getLog(AccountEngineBean.class);
     @Resource
     javax.ejb.SessionContext ctx;
 
     // Constant for the function checkPermissions(..)
     private static final int MAY_SET_ROLES = 1;
     // Constant for the function checkPermissions(..)
     private static final int MAY_SET_GROUPS = 2;
     // Constant for the function checkPermissions(..)
     private static final int MAY_UPDATE = 0;
 
     @EJB
     private UserGroupEngineLocal group;
     @EJB
     private LanguageEngineLocal language;
     @EJB
     private SequencerEngineLocal seq;
     @EJB
     private ContentEngineLocal co;
     @EJB
     private ScriptingEngineLocal scripting;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<UserTicket> getActiveUserTickets() {
         return UserTicketStore.getTickets();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void login(String username, String password, boolean takeOver)
             throws FxLoginFailedException, FxAccountInUseException {
         DataSource ds;
         try {
             ds = Database.getDataSource();
         } catch (Exception exc) {
             throw new FxLoginFailedException(exc.getMessage(), FxLoginFailedException.TYPE_SQL_ERROR);
         }
         LoginLogoutHandler.doLogin(username, password, takeOver, ctx, ds);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void logout() throws FxLogoutFailedException {
         LoginLogoutHandler.doLogout();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public boolean loginCheck(String username, String password, UserTicket currentTicket) throws FxLoginFailedException, FxDbException {
         DataSource ds;
         boolean checkOK;
         try {
             ds = Database.getDataSource();
             checkOK = FxDBAuthentication.checkLogin(username, password, currentTicket, ds);
         } catch (Exception exc) {
             throw new FxLoginFailedException(exc.getMessage(), FxLoginFailedException.TYPE_SQL_ERROR);
         }
         return checkOK;
     }
 
     /**
      * Loads the account data from the database.
      *
      * @param loginName the unique name of the user, if null the accountId is used
      * @param accountId the unique id of the user to load
      * @param contactId the contact id
      * @return the Account object
      * @throws FxApplicationException if the user account could not be loaded
      */
     private Account load(String loginName, Long accountId, Long contactId) throws FxApplicationException {
         Statement stmt = null;
         String curSql;
         Connection con = null;
         try {
 
             con = Database.getDbConnection();
             // Load the account data
             curSql = "SELECT " +
                     // 1, 2   ,  3        ,  4   ,  5       ,  6      ,  7      ,  8      ,  9
                     "ID,EMAIL,CONTACT_ID,MANDATOR,VALID_FROM,VALID_TO,DESCRIPTION,USERNAME,LOGIN_NAME," +
                     //10     ,  11        ,   12       ,    13    14          15         16
                     "IS_ACTIVE,IS_VALIDATED,LANG,ALLOW_MULTILOGIN,UPDATETOKEN,CREATED_BY,CREATED_AT," +
                     //17         18
                     "MODIFIED_BY,MODIFIED_AT FROM " + TBL_ACCOUNTS +
                     " WHERE " +
                     (loginName != null
                             ? "UPPER(LOGIN_NAME)='" + loginName.toUpperCase() + "'"
                             : "") +
                     (accountId != null
                             ? ((loginName != null) ? " AND " : "") + "ID=" + accountId
                             : "") +
                     (contactId != null
                             ? ((loginName != null || accountId != null) ? " AND " : "") + "CONTACT_ID=" + contactId
                             : "");
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.account.notFound", (loginName != null ? loginName : accountId));
             long id = rs.getLong(1);
             String email = rs.getString(2);
             long contactDataId = rs.getLong(3);
             long mandator = rs.getLong(4);
             Date validFrom = new Date(rs.getLong(5));
             Date validTo = new Date(rs.getLong(6));
             String description = rs.getString(7);
             String name = rs.getString(8);
             String _loginName = rs.getString(9);
             boolean active = rs.getBoolean(10);
             boolean validated = rs.getBoolean(11);
             FxLanguage lang = language.load(rs.getInt(12));
             boolean bAllowMultiLogin = rs.getBoolean(13);
             String updateToken = rs.getString(14);
             Account ad = new Account(id, name, _loginName, mandator, email,
                     lang, active, validated, validFrom, validTo, -1,
                     description, contactDataId, bAllowMultiLogin, updateToken,
                     LifeCycleInfoImpl.load(rs, 15, 16, 17, 18));
             if (LOG.isDebugEnabled()) LOG.debug(ad.toString());
             return ad;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "ex.account.loadFailed.sql", (loginName != null ? loginName : accountId), exc.getMessage());
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public Account load(final long id)
             throws FxApplicationException {
         return load(null, id, null);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public Account load(final String loginName)
             throws FxApplicationException {
         return load(loginName, null, null);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public Account loadForContactData(FxPK contactDataPK) throws FxApplicationException {
         return load(null, null, contactDataPK.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String getPasswordHash(long accountId) throws FxApplicationException {
         if (!FxContext.getUserTicket().isGlobalSupervisor()) {
             throw new FxNoAccessException("ex.structure.noAccess.needSupervisor");
         }
         Connection con = null;
         PreparedStatement stmt = null;
         try {
             con = Database.getDbConnection();
 
             stmt = con.prepareStatement("SELECT password FROM " + TBL_ACCOUNTS + " WHERE id=?");
             stmt.setLong(1, accountId);
 
             final ResultSet rs = stmt.executeQuery();
             if (rs.next()) {
                 return rs.getString(1);
             } else {
                 throw new FxNotFoundException("ex.account.notFound", accountId);
             }
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e);
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public UserTicket getUserTicket() {
         return UserTicketStore.getTicket();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public UserTicket getGuestTicket() {
         return UserTicketImpl.getGuestTicket();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<UserGroup> getGroups(long accountId) throws FxApplicationException {
 
         Connection con = null;
         Statement stmt = null;
         String curSql;
         final UserTicket ticket = UserTicketStore.getTicket(false);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Check the user
             long mandator = getMandatorForAccount(con, accountId);
             // Permission checks (2)
             if (!ticket.isGlobalSupervisor()) {
                 // Read access for all within a mandator
                 if (mandator != ticket.getMandatorId())
                     throw new FxNoAccessException(LOG, "ex.account.groups.wrongMandator", accountId);
             }
 
             // Load the groups the account is assigned to
             curSql = "SELECT DISTINCT ass.USERGROUP,grp.NAME,grp.MANDATOR,grp.COLOR" +
                     " FROM " + TBL_ASSIGN_GROUPS + " ass, " + TBL_USERGROUPS + " grp" +
                     " WHERE grp.ID=ass.USERGROUP AND ass.ACCOUNT=" + accountId;
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             final List<UserGroup> result = new ArrayList<UserGroup>(10);
             while (rs != null && rs.next()) {
                 long id = rs.getLong(1);
                 String grpName = rs.getString(2);
                 long grpMandator = rs.getLong(3);
                 String grpColor = rs.getString(4);
                 final UserGroup aGroup = new UserGroup(id, grpName, grpMandator, grpColor);
                 if (LOG.isDebugEnabled()) LOG.debug("Found group for user [" + accountId + "]: " + aGroup);
                 result.add(aGroup);
             }
             return result;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.account.groups.loadFailed.sql", accountId, exc.getMessage());
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
 
     }
 
     /**
      * Internal helper function, returns the mandator of an account or throws a FxNotFoundException if the user does
      * not exist.
      *
      * @param con       a already opened connection, will not be closed by the function
      * @param accountId the account to check for
      * @return the mandator of the given account
      * @throws FxNotFoundException if the account does not exist
      * @throws SQLException        if the function failed because od a sql error
      */
     private long getMandatorForAccount(final Connection con, final long accountId) throws FxNotFoundException, SQLException {
         Statement stmt = null;
         try {
             String curSql = "SELECT MANDATOR FROM " + TBL_ACCOUNTS + " WHERE ID=" + accountId;
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             long mandator = -1;
             if (rs != null && rs.next())
                 mandator = rs.getLong(1);
             if (mandator == -1)
                 throw new FxNotFoundException(LOG, "ex.account.notFound", accountId);
             return mandator;
         } finally {
             Database.closeObjects(AccountEngineBean.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<Role> getRoles(long accountId, RoleLoadMode mode) throws FxApplicationException {
         Connection con = null;
         Statement stmt = null;
         String curSql;
         final UserTicket ticket = UserTicketStore.getTicket(false);
 
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Check the user
             long mandator = getMandatorForAccount(con, accountId);
             // Permission checks (2)
             if (!ticket.isGlobalSupervisor()) {
                 if (mandator != ticket.getMandatorId())
                     throw new FxNoAccessException("ex.account.roles.wrongMandator", accountId);
             }
 
             // Load the roles
             curSql = "SELECT DISTINCT ROLE FROM " + TBL_ROLE_MAPPING + " WHERE " +
                     ((mode == RoleLoadMode.ALL || mode == RoleLoadMode.FROM_USER_ONLY) ? "ACCOUNT=" + accountId : "") +
                     ((mode == RoleLoadMode.ALL) ? " OR " : "") +
                     ((mode == RoleLoadMode.ALL || mode == RoleLoadMode.FROM_GROUPS_ONLY) ? " USERGROUP IN (SELECT USERGROUP FROM " +
                             TBL_ASSIGN_GROUPS + " WHERE ACCOUNT=" + accountId + " )" : "");
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             List<Role> result = new ArrayList<Role>(15);
             while (rs != null && rs.next())
                 result.add(Role.getById(rs.getByte(1)));
 
             if (LOG.isDebugEnabled())
                 LOG.debug("Role for user [" + accountId + "]: " + result);
             return result;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.account.roles.loadFailed.sql", accountId, exc.getMessage());
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long create(Account account, String password) throws FxApplicationException {
         return create(account, password, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public long create(Account account, String password, boolean hashPassword) throws FxApplicationException {
         final UserTicket ticket = FxContext.getUserTicket();
         return create(account.getName(), account.getLoginName(), password, account.getEmail(),
                 account.getLanguage() != null ? account.getLanguage().getId() : FxLanguage.DEFAULT.getId(),
                 account.getMandatorId() != -1 ? account.getMandatorId() : ticket.getMandatorId(),
                 account.isActive(), account.isValidated(),
                 account.getValidFrom() != null ? account.getValidFrom() : new Date(),
                 account.getValidTo() != null ? account.getValidTo() : Account.VALID_FOREVER,
                 account.getDefaultNode(), account.getDescription(), account.isAllowMultiLogin(),
                 true, hashPassword);
     }
 
     private long create(String userName, String loginName, String password, String email, long lang,
                         long mandatorId, boolean isActive, boolean isConfirmed, Date validFrom, Date validTo, long defaultNode,
                         String description, boolean allowMultiLogin, boolean checkUserRoles, boolean hashPassword)
             throws FxApplicationException {
 
         final UserTicket ticket = FxContext.getUserTicket();
         // Security
         if (checkUserRoles && !ticket.isGlobalSupervisor()) {
             if (!ticket.isInRole(Role.MandatorSupervisor))
                 FxPermissionUtils.checkRole(ticket, Role.AccountManagement);
             if (ticket.getMandatorId() != mandatorId)
                 throw new FxNoAccessException(LOG, "ex.account.create.wrongMandator");
         }
 
         // Parameter checks
         try {
             CacheAdmin.getEnvironment().getMandator(mandatorId);
             loginName = checkLoginName(loginName);
             userName = checkUserName(userName);
             checkDates(validFrom, validTo);
             if (description == null) description = "";
             email = FxFormatUtils.checkEmail(email);
             if (!language.isValid(lang))
                 throw new FxInvalidParameterException("LANGUAGE", "ex.account.languageInvalid", lang);
         } catch (FxInvalidParameterException pe) {
             if (LOG.isInfoEnabled()) LOG.info(pe);
             throw pe;
         }
 
         Connection con = null;
         PreparedStatement stmt = null;
         String curSql;
         FxPK contactDataPK;
         try {
             final FxContext ri = FxContext.get();
             final FxContent contactData;
             try {
                 if (!checkUserRoles) {
                     // we're probably running in unprivileged code, but application logic
                     // already determined that a user account should be created
                     ri.runAsSystem();
                 }
                 contactData = co.initialize(CacheAdmin.getEnvironment().getType(FxType.CONTACTDATA).getId());
             } finally {
                 if (!checkUserRoles) {
                     ri.stopRunAsSystem();
                 }
             }
             contactData.setAclId(ACL.ACL_CONTACTDATA);
             contactData.setValue("/SURNAME", new FxString(false, userName));
             contactData.setValue("/EMAIL", new FxString(false, email));
             try {
                 FxContext.get().runAsSystem();
                 contactDataPK = co.save(contactData);
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Get a new Id
             long newId = seq.getId(FxSystemSequencer.ACCOUNT);
 
             if (hashPassword) {
                 password = password == null ? "" : FxFormatUtils.encodePassword(newId, userName, password);
             }
 
             curSql = "INSERT INTO " + TBL_ACCOUNTS + "(" +
                     //1 2        3        4          5        6     7          8    9
                     "ID,MANDATOR,USERNAME,LOGIN_NAME,PASSWORD,EMAIL,CONTACT_ID,LANG,VALID_FROM," +
                     //10      11          12         13         14
                     "VALID_TO,DESCRIPTION,CREATED_BY,CREATED_AT,MODIFIED_BY," +
                     //15         16        17           18           19                20
                     "MODIFIED_AT,IS_ACTIVE,IS_VALIDATED,DEFAULT_NODE,ALLOW_MULTILOGIN,UPDATETOKEN) values " +
                     //1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
                     "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 
             final long NOW = System.currentTimeMillis();
             stmt = con.prepareStatement(curSql);
             stmt.setLong(1, newId);
             stmt.setLong(2, mandatorId);
             stmt.setString(3, userName);
             stmt.setString(4, loginName);
             stmt.setString(5, password);
             stmt.setString(6, email);
             stmt.setLong(7, contactDataPK.getId());
             stmt.setInt(8, (int) lang);
             //subtract a second to prevent login-after-immediatly-create problems with databases truncating milliseconds
             stmt.setLong(9, validFrom.getTime() - 1000);
             stmt.setLong(10, validTo.getTime());
             stmt.setString(11, description);
             stmt.setLong(12, ticket.getUserId());
             stmt.setLong(13, NOW);
             stmt.setLong(14, ticket.getUserId());
             stmt.setLong(15, NOW);
             stmt.setBoolean(16, isActive);
             stmt.setBoolean(17, isConfirmed);
             stmt.setLong(18, (defaultNode < 0) ? 0 : defaultNode);
             stmt.setBoolean(19, allowMultiLogin);
             stmt.setString(20, RandomStringUtils.randomAlphanumeric(64));
             stmt.executeUpdate();
 
             //make sure the user is the owner of his contact data
             stmt.close();
             stmt = con.prepareStatement("UPDATE " + TBL_CONTENT + " SET CREATED_BY=?, MANDATOR=? WHERE ID=?");
             stmt.setLong(1, newId);
             stmt.setLong(2, mandatorId);
             stmt.setLong(3, contactDataPK.getId());
             stmt.executeUpdate();
             CacheAdmin.expireCachedContent(contactDataPK.getId());
 
             // call scripts
             final List<Long> scriptIds = scripting.getByScriptEvent(FxScriptEvent.AfterAccountCreate);
             final FxScriptBinding binding = new FxScriptBinding();
             binding.setVariable("accountId", newId);
             binding.setVariable("pk", contactDataPK);
             for (long scriptId : scriptIds)
                 scripting.runScript(scriptId, binding);
 
             StringBuilder sbHistory = new StringBuilder(1000);
             sbHistory.append("<account action=\"create\">\n").
                     append("  <id>").append(newId).append("</id>\n").
                     append("  <mandator>").append(CacheAdmin.getEnvironment().getMandator(mandatorId).getName()).append("</mandator>\n").
                     append("  <username>").append(userName).append("</username>\n").
                     append("  <loginname>").append(loginName).append("</loginname>\n").
                     append("  <email>").append(email).append("</email>\n").
                     append("  <validfrom>").append(FxFormatUtils.toString(validFrom)).append("</validfrom>\n").
                     append("  <validto>").append(FxFormatUtils.toString(validTo)).append("</validto>\n").
                     append("  <description><![CDATA[").append(description).append("]]></description>\n").
                     append("  <active>").append(isActive).append("</active>\n").
                     append("  <confirmed>").append(isConfirmed).append("</confirmed>\n").
                     append("  <multilogin>").append(allowMultiLogin).append("</multilogin>\n").
                     append("</account>");
             EJBLookup.getHistoryTrackerEngine().trackData(sbHistory.toString(), "history.account.create", loginName);
 
             // EVERY users is a member of group EVERYONE and his mandator
             final long[] groups = {UserGroup.GROUP_EVERYONE, group.loadMandatorGroup(mandatorId).getId()};
             ri.runAsSystem();
             try {
                 setGroups(newId, groups);
             } catch (Exception exc) {
                 throw new FxCreateException(exc, "ex.account.create.everyoneAssignFailed", exc.getMessage());
             } finally {
                 ri.stopRunAsSystem();
             }
 
             // Return the id
             return newId;
         } catch (SQLException exc) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(exc);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation) {
                 throw new FxEntryExistsException(LOG, exc, "ex.account.userExists", loginName, userName);
             } else {
                 throw new FxCreateException(LOG, exc, "ex.account.createFailed.sql", loginName, exc.getMessage());
             }
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
     private String checkLoginName(String name) throws FxInvalidParameterException {
         if (name == null || name.length() == 0)
             throw new FxInvalidParameterException("loginName", "ex.account.login.noName");
         if (name.length() > 255)
             throw new FxInvalidParameterException("loginName", "ex.account.login.nameTooLong", 255);
         return name.trim();
     }
 
     private String checkUserName(String name) throws FxInvalidParameterException {
         if (name == null || name.length() == 0)
             throw new FxInvalidParameterException("name", "ex.account.name.noName");
         if (name.length() > 255)
             throw new FxInvalidParameterException("name", "ex.account.name.nameTooLong", 255);
         return name.trim();
     }
 
     /**
      * Checks the dates.
      *
      * @param validFrom the from date
      * @param validTo   the to date
      * @throws FxInvalidParameterException if the dates are invalid
      */
     private void checkDates(final Date validFrom, final Date validTo) throws FxInvalidParameterException {
         if (validFrom == null)
             throw new FxInvalidParameterException("VALID_FROM", "ex.account.login.validFrom.missing");
         if (validTo == null)
             throw new FxInvalidParameterException("VALID_FROM", "ex.account.login.validTo.missing");
         if (validFrom.getTime() > validTo.getTime())
             throw new FxInvalidParameterException("VALID_FROM", "ex.account.login.dateMismatch");
         if (validTo.getTime() < java.lang.System.currentTimeMillis())
             throw new FxInvalidParameterException("VALID_TO", "ex.account.login.dateExpired");
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(long accountId) throws FxApplicationException {
         // Handle protected users
         if (accountId == Account.USER_GUEST)
             throw new FxNoAccessException("ex.account.delete.guest");
         if (accountId == Account.USER_GLOBAL_SUPERVISOR)
             throw new FxNoAccessException("ex.account.delete.supervisor");
         if (accountId == Account.NULL_ACCOUNT)
             throw new FxNoAccessException("ex.account.delete.nullAccount");
 
         Account account = load(accountId);
 
         // Permission checks
         final UserTicket ticket = FxContext.getUserTicket();
         if (!ticket.isGlobalSupervisor()) {
             if (!ticket.isInRole(Role.MandatorSupervisor))
                 FxPermissionUtils.checkRole(ticket, Role.AccountManagement);
             if (account.getMandatorId() != ticket.getMandatorId())
                 throw new FxNoAccessException(LOG, "ex.account.delete.wrongMandator");
         }
 
         Connection con = null;
         Statement stmt = null;
         String curSql = null;
         try {
             con = Database.getDbConnection();
             // Delete all group assignments ..
             stmt = con.createStatement();
             curSql = "DELETE FROM " + TBL_ASSIGN_GROUPS + " WHERE ACCOUNT=" + accountId;
             stmt.executeUpdate(curSql);
             stmt.close();
 
             // Delete all role assigments ..
             stmt = con.createStatement();
             curSql = "DELETE FROM " + TBL_ROLE_MAPPING + " WHERE ACCOUNT=" + accountId;
             stmt.executeUpdate(curSql);
             stmt.close();
 
             // TODO: delete user specific configurations
             // TODO: delete user specific locks
 
             // Finally remove the user itself
             stmt = con.createStatement();
             curSql = "DELETE FROM " + TBL_ACCOUNTS + " WHERE ID=" + accountId;
             stmt.executeUpdate(curSql);
 
             // Log the user out of the system (if he is active)
             // This is done after the commit, since a failure should not block the user delete action
             UserTicketStore.removeUserId(accountId, null);
 
             // delete contact data
             try {
                 // disable security since the contact data ACL prevents foreign users from deleting
                 // CD instances. However, in this case the user has the role AccountManagement
                 // which overrules the ACL permissions.
                 FxContext.get().runAsSystem();
                 co.remove(account.getContactData());
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
             EJBLookup.getHistoryTrackerEngine().track("history.account.remove", account.getLoginName());
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, exc, "ex.account.delete.failed.sql", accountId, exc.getMessage(), curSql);
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
 
     /**
      * Returns wether the caller may use the operations 'edit','setRoles','setGroups'
      * on a specific user.
      *
      * @param accountId the id of the user to check for
      * @return a boolean array indicating if a action may be called<br>
      *         position <code>MAY_UPDATE</code>: update the user<br>
      *         position <code>MAY_SET_ROLES</code>: set roles for the user<br>
      *         position <code>MAY_SET_GROUPS</code>: set groups for the user
      * @throws FxApplicationException on errors
      */
     private boolean[] checkPermissions(long accountId) throws FxApplicationException {
         return _checkPermissions(load(accountId));
     }
 
 
     /**
      * Returns wether the caller may use the operations 'edit','setRoles','setGroups'
      * on a specific account.
      *
      * @param account the user to check the operations for
      * @return a boolean array indicating if a action may be called<br>
      *         position MAY_UPDATE: update the user<br>
      *         position MAY_SET_ROLES: set roles for the user<br>
      *         position MAY_SET_GROUPS: set groups for the user
      */
     private boolean[] _checkPermissions(Account account) {
         final UserTicket ticket = FxContext.getUserTicket();
         if (ticket.isGlobalSupervisor() ||
                 (ticket.getMandatorId() == account.getMandatorId() &&
                         ticket.isMandatorSupervisor())) {
             return new boolean[]{true, true, true}; // full access
         }
 
         boolean edit = true;
         boolean roles = true;
         boolean groups = true;
 
         // Edit access
         if (!ticket.isInRole(Role.AccountManagement)) edit = false;
         if (ticket.getMandatorId() != account.getMandatorId()) edit = false;
 
         // Update roles access
         if (!ticket.isInRole(Role.AccountManagement)) roles = false;
         if (ticket.getMandatorId() != account.getMandatorId()) roles = false;
 
         // Update groups access. Specified via AccountManagement
         if (!ticket.isInRole(Role.AccountManagement)) groups = false;
         if (ticket.getMandatorId() != account.getMandatorId()) groups = false;
 
         boolean result[] = new boolean[3];
         result[MAY_SET_GROUPS] = groups;
         result[MAY_SET_ROLES] = roles;
         result[MAY_UPDATE] = edit;
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setGroups(long accountId, List<UserGroup> groups) throws FxApplicationException {
         if (groups != null && groups.size() > 0) {
             long groupIds[] = new long[groups.size()];
             int pos = 0;
             for (UserGroup group : groups) {
                 groupIds[pos++] = group.getId();
             }
             setGroups(accountId, groupIds);
         } else {
             setGroups(accountId);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setGroups(long accountId, long... groups) throws FxApplicationException {
         // Handle null params
         if (groups == null)
             groups = new long[0];
 
         final UserTicket ticket = FxContext.getUserTicket();
 
         // Permission checks
         try {
             if (!checkPermissions(accountId)[MAY_SET_GROUPS])
                 throw new FxNoAccessException("ex.account.groups.noAssignPermission", accountId);
         } catch (FxLoadException le) {
             throw new FxUpdateException(le.getMessage());
         }
 
         Account account = load(accountId);
 
         // Remove duplicated entries and add group everyone
         groups = FxArrayUtils.removeDuplicates(groups);
 
         // Ensure group EVERYONE and mandator group is part of the list, since every user is assigned to it
         groups = FxArrayUtils.addElement(groups, UserGroup.GROUP_EVERYONE);
         groups = FxArrayUtils.addElement(groups, group.loadMandatorGroup(account.getMandatorId()).getId());
 
         // Check the groups
         List<UserGroup> currentlyAssigned;
         try {
             currentlyAssigned = getGroups(accountId);
         } catch (FxLoadException exc) {
             throw new FxUpdateException(exc);
         }
 
         StringBuilder sbHistory = new StringBuilder(1000);
         sbHistory.append("<original>\n");
         for (UserGroup org : currentlyAssigned)
             sbHistory.append("  <group id=\"").append(org.getId()).append("\" mandator=\"").
                     append(org.getMandatorId()).append("\">").append(org.getName()).append("</group>\n");
         sbHistory.append("</original>\n").append("<new>\n");
 
         for (long grp : groups) {
             UserGroup g = group.load(grp);
             sbHistory.append("  <group id=\"").append(g.getId()).append("\" mandator=\"").
                     append(g.getMandatorId()).append("\">").append(g.getName()).append("</group>\n");
             // Do not check the special gropups
             if (grp == UserGroup.GROUP_EVERYONE) continue;
             if (grp == UserGroup.GROUP_OWNER) continue;
             if (grp == UserGroup.GROUP_UNDEFINED) continue;
             if (FxSharedUtils.indexOfSelectableObject(currentlyAssigned, grp) != -1) continue;
             // Perform the check for all regular groups, loading an inaccessible
             // group will throw an exception which is what we want here
 
             if (g.isSystem())
                 continue;
             //make sure the calling user is assigned all roles of the group as well
             for (Role check : group.getRoles(grp))
                 if (!ticket.isInRole(check))
                     throw new FxNoAccessException("ex.account.roles.assign.noMember.group", check.name(), g.getName());
             if (!ticket.isGlobalSupervisor()) {
                 if (g.getMandatorId() != account.getMandatorId())
                     throw new FxNoAccessException("ex.account.group.assign.wrongMandator", g.getName(), accountId);
             }
         }
         sbHistory.append("</new>\n");
 
         // Write group assignments to the database
         Connection con = null;
         PreparedStatement ps = null;
 
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Delete the old assignments of the user
             ps = con.prepareStatement("DELETE FROM " + TBL_ASSIGN_GROUPS + " WHERE ACCOUNT=?");
             ps.setLong(1, accountId);
             ps.executeUpdate();
             if (groups.length > 0) {
                 ps.close();
                 ps = con.prepareStatement("INSERT INTO " + TBL_ASSIGN_GROUPS + " (ACCOUNT,USERGROUP) VALUES (?,?)");
             }
 
             // Store the new assignments of the user
             for (long group : groups) {
                 // Skipp 'null' group
                 if (group == UserGroup.GROUP_UNDEFINED) continue;
                 ps.setLong(1, accountId);
                 ps.setLong(2, group);
                 ps.executeUpdate();
             }
             LifeCycleInfoImpl.updateLifeCycleInfo(TBL_ACCOUNTS, "ID", accountId);
             // Ensure any active ticket of the updated user are refreshed
             UserTicketStore.flagDirtyHavingUserId(accountId);
             EJBLookup.getHistoryTrackerEngine().trackData(sbHistory.toString(), "history.account.setGroups", account.getLoginName());
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, exc, "ex.account.roles.updateFailed.sql", accountId, exc.getMessage());
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, ps);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setRoles(long accountId, List<Role> roles) throws FxApplicationException {
         if (roles != null && roles.size() > 0) {
             setRoles(accountId, roles.toArray(new Role[roles.size()]));
         } else {
             setRoles(accountId);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setRoles(long accountId, long... roles) throws FxApplicationException {
         if (roles == null)
             roles = new long[0];
         final Account account = load(accountId);
 
         if (!_checkPermissions(account)[MAY_SET_ROLES])
             throw new FxNoAccessException("ex.account.roles.noAssignPermission", accountId);
 
         // Write roles to database
         Connection con = null;
         PreparedStatement ps = null;
 
         StringBuilder sbHistory = new StringBuilder(1000);
 
         try {
             roles = FxArrayUtils.removeDuplicates(roles);
             con = Database.getDbConnection();
 
             UserTicket ticket = FxContext.getUserTicket();
             List<Role> orgRoles = getRoles(accountId, RoleLoadMode.FROM_USER_ONLY);
             sbHistory.append("<original>\n");
             for(Role org: orgRoles)
                     sbHistory.append("  <role id=\"").append(org.getId()).append("\">").append(org.getName()).append("</role>\n");
             sbHistory.append("</original>\n");
             //only allow to assign roles which the calling user is a member of (unless it is a global supervisor)
             if (!ticket.isGlobalSupervisor() && !(ticket.isMandatorSupervisor() && account.getMandatorId() == ticket.getMandatorId())) {
                 final List<Long> orgRoleIds = FxSharedUtils.getSelectableObjectIdList(orgRoles);
                 //check removed roles
                 for (long check : orgRoleIds) {
                     if (!ArrayUtils.contains(roles, check)) {
                         if (!ticket.isInRole(Role.getById(check))) {
                             EJBUtils.rollback(ctx);
                             throw new FxNoAccessException("ex.account.roles.assign.noMember.remove", Role.getById(check).getName());
                         }
                     }
                 }
                 //check added roles
                 for (long check : roles) {
                     if (!orgRoleIds.contains(check)) {
                         if (!ticket.isInRole(Role.getById(check))) {
                             EJBUtils.rollback(ctx);
                             throw new FxNoAccessException("ex.account.roles.assign.noMember.add", Role.getById(check).getName());
                         }
                     }
                 }
             }
 
             // Delete the old assignments of the user
             ps = con.prepareStatement("DELETE FROM " + TBL_ROLE_MAPPING + " WHERE ACCOUNT=?");
             ps.setLong(1, accountId);
             ps.executeUpdate();
 
             if (roles.length > 0) {
                 ps.close();
                 ps = con.prepareStatement("INSERT INTO " + TBL_ROLE_MAPPING
                         + " (ACCOUNT,USERGROUP,ROLE) VALUES (?,?,?)");
             }
 
             sbHistory.append("<new>\n");
             // Store the new assignments of the account
             for (long role : roles) {
                 if (Role.isUndefined(role)) continue;
                 ps.setLong(1, accountId);
                 ps.setLong(2, UserGroup.GROUP_NULL);
                 ps.setLong(3, role);
                 ps.executeUpdate();
                 sbHistory.append("  <role id=\"").append(role).append("\">").append(Role.getById(role).getName()).append("</role>\n");
             }
             sbHistory.append("</new>\n");
             LifeCycleInfoImpl.updateLifeCycleInfo(TBL_ACCOUNTS, "ID", accountId);
             // Ensure any active ticket of the updated account are refreshed
             UserTicketStore.flagDirtyHavingUserId(accountId);
             EJBLookup.getHistoryTrackerEngine().trackData(sbHistory.toString(), "history.account.setRoles", account.getLoginName());
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, exc, "ex.account.roles.updateFailed.sql", accountId, exc.getMessage());
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void addRole(long accountId, long roleId) throws FxApplicationException {
         final List<Role> roles = new ArrayList<Role>();
         roles.addAll(getRoles(accountId, RoleLoadMode.ALL));
         roles.add(Role.getById(roleId));
         setRoles(accountId, roles);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void addGroup(long accountId, long groupId) throws FxApplicationException {
         final List<UserGroup> groups = new ArrayList<UserGroup>();
         groups.addAll(getGroups(accountId));
         groups.add(group.load(groupId));
         setGroups(accountId, groups);
     }
 
     /**
      * Helper function to set the roles for an account.
      *
      * @param accountId the account id
      * @param roles     the roles to set
      * @throws FxApplicationException on errors
      */
     private void setRoles(long accountId, Role[] roles)
             throws FxApplicationException {
         long[] roleIds = new long[roles.length];
         for (int i = 0; i < roles.length; i++) {
             roleIds[i] = roles[i].getId();
         }
         setRoles(accountId, roleIds);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<Account> loadAll(String name, String loginName, String email, Boolean isActive,
                                  Boolean isConfirmed, Long mandatorId, int[] isInRole, long[] isInGroup, int startIdx, int maxEntries)
             throws FxApplicationException {
 
         Connection con = null;
         Statement stmt = null;
         final UserTicket ticket = FxContext.getUserTicket();
 
         String curSql = _buildSearchStmt(ticket, name, loginName, email, isActive, isConfirmed,
                 mandatorId, isInRole, isInGroup, false);
 
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Load the users
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             ArrayList<Account> alResult = new ArrayList<Account>(1000);
             int counter = 0;
             if (startIdx < 0) startIdx = 0;
             if (rs != null) {
                 while (rs.next()) {
 
                     // jump to startIndex
                     if (startIdx > 0) {
                         startIdx--;
                         continue;
                     }
 
                     // return only maxEntries
                     if (counter > -1) {
                         if (counter == maxEntries) break;
                         counter++;
                     }
 
                     // add to result
                     long id = rs.getLong(1);
                     String _email = rs.getString(2);
                     long contactDataId = rs.getLong(3);
                     if (rs.wasNull())
                         contactDataId = -1;
                     long mandator = rs.getLong(4);
                     FxLanguage lang = language.load(rs.getInt(5));
                     Date validFrom = new Date(rs.getLong(6));
                     Date validTo = new Date(rs.getLong(7));
                     String description = rs.getString(8);
                     String _name = rs.getString(9);
                     String _loginName = rs.getString(10);
                     boolean active = rs.getBoolean(11);
                     boolean validated = rs.getBoolean(12);
                     boolean multiLogin = rs.getBoolean(13);
                     String updateToken = rs.getString(14);
 
                     alResult.add(new Account(id, _name, _loginName, mandator, _email,
                             lang, active, validated, validFrom, validTo, -1,
                             description, contactDataId, multiLogin, updateToken,
                             LifeCycleInfoImpl.load(rs, 15, 16, 17, 18)));
                 }
             }
 
 
             return alResult;
         } catch (FxInvalidLanguageException exc) {
             throw new FxLoadException(exc);
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.account.loadAll.failed.sql", exc.getMessage(), curSql);
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public List<Account> loadAll(long mandatorId) throws FxApplicationException {
         return loadAll(null, null, null, null, null, mandatorId, null, null, 0, Integer.MAX_VALUE);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Account> loadAll() throws FxApplicationException {
         final UserTicket ticket = FxContext.getUserTicket();
         return loadAll(null, null, null, null, null,
                 ticket.isGlobalSupervisor() ? null : ticket.getMandatorId(),
                 null, null, 0, Integer.MAX_VALUE);
     }
 
     /**
      * Helper function.
      *
      * @param ticket      the ticket of the calling user
      * @param name        the name filter
      * @param loginName   the login name filter
      * @param email       the email filter
      * @param isActive    the is active filter
      * @param isConfirmed the is confirmed filter
      * @param mandatorId  the mandator id filter
      * @param isInRole    the role filter
      * @param isInGroup   the group filter
      * @param isCountOnly the "count only" option
      * @return the sql filter String
      * @throws FxNoAccessException if the user does not have access
      */
     private String _buildSearchStmt(UserTicket ticket, String name, String loginName, String email, Boolean isActive,
                                     Boolean isConfirmed, Long mandatorId, int[] isInRole, long[] isInGroup, boolean isCountOnly)
             throws FxNoAccessException {
 
         // Do no filter GROUP_UNDEFINED (=null value)
         if (isInGroup != null) {
             isInGroup = ArrayUtils.removeElement(isInGroup, UserGroup.GROUP_UNDEFINED);
         }
 
         // Determine if group/role filter options are enabled
         final boolean filterByGrp = (isInGroup != null && isInGroup.length > 0);
         final boolean filterByRole = (isInRole != null && isInRole.length > 0);
 
         // Determine the mandator, and check its security
         long _mandatorId = (mandatorId == null) ? ticket.getMandatorId() : mandatorId;
         if (_mandatorId < 0 && !ticket.isGlobalSupervisor())
             throw new FxNoAccessException(LOG, "ex.account.loadFailed.wrongMandator");
 
         // Do case insensitive search
         if (email != null) email = email.toUpperCase();
         if (name != null) name = name.toUpperCase();
         if (loginName != null) loginName = loginName.toUpperCase();
 
         final String TRUE = StorageManager.getBooleanTrueExpression();
         String curSql = "SELECT " + "" +
                 (isCountOnly ? "COUNT(*)" :
                         //   1      2         3              4            5        6
                         "usr.ID,usr.EMAIL,usr.CONTACT_ID,usr.MANDATOR,usr.LANG,usr.VALID_FROM," +
                                 //   7            8               9            10             11
                                 "usr.VALID_TO,usr.DESCRIPTION,usr.USERNAME,usr.LOGIN_NAME,usr.IS_ACTIVE," +
                                 //   12               13                   14
                                 "usr.IS_VALIDATED,usr.ALLOW_MULTILOGIN,usr.UPDATETOKEN," +
                                 //   15             16             17              18
                                 "usr.CREATED_BY,usr.CREATED_AT,usr.MODIFIED_BY,usr.MODIFIED_AT") +
                 " FROM " + TBL_ACCOUNTS + " usr WHERE ID!=" + Account.NULL_ACCOUNT + " " +
                 ((_mandatorId == -1) ? "" : " AND (usr.MANDATOR=" + _mandatorId + " OR usr.ID<=" + Account.USER_GLOBAL_SUPERVISOR + ")") +
                 ((name != null && name.length() > 0) ? " AND UPPER(usr.USERNAME) LIKE '%" + name + "%'" : "") +
                 ((loginName != null && loginName.length() > 0) ? " AND UPPER(usr.LOGIN_NAME) LIKE '%" + loginName + "%'" : "") +
                 ((email != null && email.length() > 0) ? " AND UPPER(usr.EMAIL) LIKE '%" + email + "%'" : "") +
                 ((isActive != null) ? " AND usr.IS_ACTIVE" + (isActive ? "=" : "<>") + TRUE : "") +
                 ((isConfirmed != null) ? " AND usr.IS_VALIDATED" + (isConfirmed ? "=" : "<>") + TRUE : "") +
 
                 // Group link
                 ((!filterByGrp) ? "" :
                         " AND EXISTS (SELECT 1 FROM " + TBL_ASSIGN_GROUPS + " grp WHERE grp.ACCOUNT=usr.ID AND grp.USERGROUP IN (" +
                                 StringUtils.join(ArrayUtils.toObject(isInGroup), ',') + "))") +
 
                 // Role link
                 ((!filterByRole) ? "" :
                         " AND (EXISTS (SELECT 1 FROM " + TBL_ROLE_MAPPING + " rol WHERE rol.ACCOUNT=usr.ID and rol.ROLE IN (" +
                                 StringUtils.join(ArrayUtils.toObject(isInRole), ',') + ")) OR " +
                                 "EXISTS (SELECT 1 FROM " + TBL_ROLE_MAPPING + " rol, " + TBL_ASSIGN_GROUPS + " grp WHERE " +
                                 "grp.ACCOUNT=usr.ID AND rol.USERGROUP=grp.USERGROUP AND rol.ROLE IN (" + StringUtils.join(ArrayUtils.toObject(isInRole), ',') + ")))") +
 
                 // Order
                 (isCountOnly ? "" : " ORDER by usr.LOGIN_NAME");
         if (LOG.isDebugEnabled()) LOG.debug(curSql);
         return curSql;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public int getAccountMatches(String name, String loginName, String email, Boolean isActive,
                                  Boolean isConfirmed, Long mandatorId, int[] isInRole, long[] isInGroup)
             throws FxApplicationException {
 
         Connection con = null;
         Statement stmt = null;
         final UserTicket ticket = FxContext.getUserTicket();
         String curSql = _buildSearchStmt(ticket, name, loginName, email, isActive, isConfirmed,
                 mandatorId, isInRole, isInGroup, true);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Load the users
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             if (rs != null && rs.next())
                 return rs.getInt(1);
             return -1;
 
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.account.loadAll.failed.sql", exc.getMessage(), curSql);
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void update(long accountId, String password, Long defaultNode,
                        String name, String loginName, String email, Boolean isConfirmed, Boolean isActive,
                        Date validFrom, Date validTo, Long lang, String description,
                        Boolean allowMultiLogin, Long contactDataId)
             throws FxApplicationException {
 
         // Load the account to update
         Account account = load(accountId);
 
         StringBuilder sbHistory = new StringBuilder(1000);
         sbHistory.append("<original>\n").
                 append("  <id>").append(accountId).append("</id>\n").
                 append("  <mandator>").append(CacheAdmin.getEnvironment().getMandator(account.getMandatorId()).getName()).append("</mandator>\n").
                 append("  <username>").append(account.getName()).append("</username>\n").
                 append("  <loginname>").append(account.getLoginName()).append("</loginname>\n").
                 append("  <email>").append(account.getEmail()).append("</email>\n").
                 append("  <validfrom>").append(account.getValidFromString()).append("</validfrom>\n").
                 append("  <validto>").append(account.getValidToString()).append("</validto>\n").
                 append("  <description><![CDATA[").append(account.getDescription()).append("]]></description>\n").
                 append("  <active>").append(account.isActive()).append("</active>\n").
                 append("  <confirmed>").append(account.isValidated()).append("</confirmed>\n").
                 append("  <multilogin>").append(account.isAllowMultiLogin()).append("</multilogin>\n").
                 append("</original>\n");
 
         final UserTicket ticket = FxContext.getUserTicket();
         // Determine if only fields are accessed that the use may alter for himself
         final boolean protectedFields = (name != null || loginName != null || isConfirmed != null || isActive != null ||
                 validTo != null || validFrom != null || description != null);
         if (!protectedFields && ticket.getUserId() == accountId) {
             // passed
         } else {
             if (!_checkPermissions(account)[MAY_UPDATE])
                 throw new FxNoAccessException(LOG, "ex.account.update.noPermission", account.getName());
         }
 
         // Parameter checks
         try {
             if (loginName != null) loginName = checkLoginName(loginName);
             if (email != null) email = FxFormatUtils.checkEmail(email);
             if (password != null) {
                password = FxFormatUtils.encodePassword(accountId, StringUtils.defaultString(loginName, account.getLoginName()),
                        password.trim());
             }
             if (lang != null && !language.isValid(lang))
                 throw new FxInvalidParameterException("LANGUAGE", "ex.account.languageInvalid", lang);
         } catch (FxInvalidParameterException pe) {
             if (LOG.isInfoEnabled()) LOG.info(pe);
             throw pe;
         }
 
         Connection con = null;
         PreparedStatement stmt = null;
         String curSql;
 
         if (name == null) name = account.getName();
         if (loginName == null) loginName = account.getLoginName();
         if (email == null) email = account.getEmail();
         if (lang == null) lang = account.getLanguage().getId();
         if (isActive == null) isActive = account.isActive();
         if (isConfirmed == null) isConfirmed = account.isValidated();
         if (description == null) description = account.getDescription();
         if (defaultNode == null) defaultNode = account.getDefaultNode();
         if (allowMultiLogin == null) allowMultiLogin = account.isAllowMultiLogin();
         // Assign and check dates
         if (validFrom == null) validFrom = account.getValidFrom();
         if (validTo == null) validTo = account.getValidTo();
         checkDates(validFrom, validTo);
         if (defaultNode < 0) defaultNode = (long) 0;
 
         try {
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             curSql = "UPDATE " + TBL_ACCOUNTS + " SET " +
                     // 1          2            3            4           5               6
                     "EMAIL=?,LANG=?,VALID_FROM=?,VALID_TO=?,DESCRIPTION=?," +
                     // 6                  7             8            9              10
                     "MODIFIED_BY=?,MODIFIED_AT=?,IS_ACTIVE=?,IS_VALIDATED=?,DEFAULT_NODE=?," +
                     //  11,        12     ,    13      ,                                14
                     "USERNAME=?,LOGIN_NAME=?,ALLOW_MULTILOGIN=?" + ((password != null) ? ",PASSWORD=?" : "") +
                     ((contactDataId != null) ? ",CONTACT_ID=?" : "") +
                     " WHERE ID=" + accountId;
             stmt = con.prepareStatement(curSql);
             stmt.setString(1, email);
             stmt.setInt(2, lang.intValue());
             stmt.setLong(3, validFrom.getTime());
             stmt.setLong(4, validTo.getTime());
             stmt.setString(5, description);
             stmt.setLong(6, ticket.getUserId());
             stmt.setLong(7, System.currentTimeMillis());
             stmt.setBoolean(8, isActive);
             stmt.setBoolean(9, isConfirmed);
             stmt.setLong(10, defaultNode);
             stmt.setString(11, name);
             stmt.setString(12, loginName);
             stmt.setBoolean(13, allowMultiLogin);
             int pos = 14;
             if (password != null) stmt.setString(pos++, password);
             if (contactDataId != null) stmt.setLong(pos/*++*/, contactDataId);
             stmt.executeUpdate();
             if (contactDataId != null) {
                 //make sure the user is the owner of his contact data
                 stmt.close();
                 stmt = con.prepareStatement("UPDATE " + TBL_CONTENT + " SET CREATED_BY=? WHERE ID=?");
                 stmt.setLong(1, accountId);
                 stmt.setLong(2, contactDataId);
                 stmt.executeUpdate();
             }
 
             // Log the user out of the system if he was made active
             if (!isActive || !isConfirmed) {
                 UserTicketStore.removeUserId(accountId, null);
             } else {
                 // Ensure any active ticket of the updated user are refreshed
                 UserTicketStore.flagDirtyHavingUserId(account.getId());
             }
 
             sbHistory.append("<new>\n").
                     append("  <id>").append(accountId).append("</id>\n").
                     append("  <mandator>").append(CacheAdmin.getEnvironment().getMandator(account.getMandatorId()).getName()).append("</mandator>\n").
                     append("  <username>").append(name).append("</username>\n").
                     append("  <loginname>").append(loginName).append("</loginname>\n").
                     append("  <email>").append(email).append("</email>\n").
                     append("  <validfrom>").append(FxFormatUtils.toString(validFrom)).append("</validfrom>\n").
                     append("  <validto>").append(FxFormatUtils.toString(validTo)).append("</validto>\n").
                     append("  <description><![CDATA[").append(description).append("]]></description>\n").
                     append("  <active>").append(isActive).append("</active>\n").
                     append("  <confirmed>").append(isConfirmed).append("</confirmed>\n").
                     append("  <multilogin>").append(allowMultiLogin).append("</multilogin>\n").
                     append("</new>");
             EJBLookup.getHistoryTrackerEngine().trackData(sbHistory.toString(), "history.account.update", account.getLoginName());
         } catch (SQLException exc) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(exc);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation) {
                 throw new FxEntryExistsException(LOG, "ex.account.userExists", name, loginName);
             } else {
                 throw new FxUpdateException(LOG, "ex.account.update.failed.sql", account.getLoginName(), exc.getMessage());
             }
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void updateUser(long accountId, String password, String name, String loginName, String email, Long lang) throws FxApplicationException {
         // Load the account to update
         Account account = load(accountId);
 
         final UserTicket ticket = FxContext.getUserTicket();
         if (ticket.getUserId() != accountId) {
             if (!ticket.isGlobalSupervisor() ||
                     !(ticket.isMandatorSupervisor() && account.getMandatorId() == ticket.getMandatorId()))
                 throw new FxNoAccessException(LOG, "ex.account.update.noPermission", account.getName());
         }
 
         // Parameter checks
         if (loginName != null) loginName = checkLoginName(loginName);
         if (email != null) email = FxFormatUtils.checkEmail(email);
         if (password != null) {
             password = FxFormatUtils.encodePassword(accountId, account.getLoginName(), password.trim());
         }
 
         if (lang != null && !language.isValid(lang))
             throw new FxInvalidParameterException("LANGUAGE", "ex.account.languageInvalid", lang);
 
         Connection con = null;
         PreparedStatement stmt = null;
         String curSql;
 
         if (name == null) name = account.getName();
         if (loginName == null) loginName = account.getLoginName();
         if (email == null) email = account.getEmail();
         if (lang == null) lang = account.getLanguage().getId();
 
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             curSql = "UPDATE " + TBL_ACCOUNTS + " SET " +
                     // 1      2           3            4              5             6                                    7
                     "EMAIL=?,LANG=?, USERNAME=?,LOGIN_NAME=?, MODIFIED_BY=?,MODIFIED_AT=?" + ((password != null) ? ",PASSWORD=?" : "") +
                     " WHERE ID=" + accountId;
             stmt = con.prepareStatement(curSql);
             stmt.setString(1, email);
             stmt.setInt(2, lang.intValue());
             stmt.setString(3, name);
             stmt.setString(4, loginName);
 
             stmt.setLong(5, ticket.getUserId());
             stmt.setLong(6, System.currentTimeMillis());
 
             if (password != null) stmt.setString(7, password);
             stmt.executeUpdate();
 
             // Ensure any active ticket of the updated user are refreshed
             UserTicketStore.flagDirtyHavingUserId(account.getId());
         } catch (SQLException exc) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(exc);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation) {
                 throw new FxEntryExistsException(LOG, "ex.account.userExists", name, loginName);
             } else {
                 throw new FxUpdateException(LOG, "ex.account.update.failed.sql", account.getLoginName(), exc.getMessage());
             }
         } finally {
             Database.closeObjects(AccountEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<Account> getAssignedUsers(long groupId, int startIdx, int maxEntries)
             throws FxApplicationException {
 
         final UserTicket ticket = FxContext.getUserTicket();
 
         // Load the requested group
         final UserGroup theGroup = group.load(groupId);
 
 
         long[] groups = {theGroup.getId()};
 
         // Return the users from ALL mandators assigned to the group if the caller is GLOBAL_SUPERVISOR.
         // Else only look for users within the callers mandator.
         Long mandatorId = ticket.isGlobalSupervisor() ? -1 : ticket.getMandatorId();
 
         // Load the users
 
         return loadAll(null, null, null, null, null, mandatorId, null, groups, startIdx, maxEntries);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long getAssignedUsersCount(long groupId, boolean includeInvisible) throws FxApplicationException {
 
         if (groupId < 0 || groupId == UserGroup.GROUP_UNDEFINED) return 0;
         final UserTicket ticket = FxContext.getUserTicket();
         Connection con = null;
         Statement stmt = null;
         String sCurSql = null;
         try {
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Nothing is invisible for the GLOBAL_SUPERVISOR
             if (ticket.isGlobalSupervisor()) includeInvisible = true;
 
             // Load the count
             stmt = con.createStatement();
 
             if (includeInvisible) {
                 sCurSql = "SELECT COUNT(*) FROM " + TBL_ASSIGN_GROUPS + " WHERE USERGROUP=" + groupId;
             } else {
                 sCurSql = "SELECT COUNT(*) FROM " + TBL_ASSIGN_GROUPS + " ln, " +
                         TBL_ACCOUNTS + " usr WHERE ln.ACCOUNT=usr.ID AND usr.MANDATOR=" + ticket.getMandatorId() +
                         " AND ln.USERGROUP=" + groupId;
             }
 
             ResultSet rs = stmt.executeQuery(sCurSql);
             long result = 0;
             if (rs != null && rs.next()) result = rs.getLong(1);
 
             if (LOG.isDebugEnabled()) LOG.debug("Users in group [" + groupId + "]:" + result + ", caller=" + ticket);
             return result;
         } catch (SQLException exc) {
             FxLoadException ce = new FxLoadException("Database error! Last stmt was [" + sCurSql + "]:" + exc.getMessage(), exc);
             LOG.error(ce);
             throw ce;
         } finally {
             Database.closeObjects(UserGroupEngineBean.class, con, stmt);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<ACLAssignment> loadAccountAssignments(long accountId) throws
             FxApplicationException {
         Connection con = null;
         Statement stmt = null;
         String curSql;
         UserTicket ticket = UserTicketStore.getTicket(false);
 
         // Security checks
         if (!ticket.isGlobalSupervisor() && (!(accountId == ticket.getUserId() || accountId == Account.USER_GUEST))) {
             try {
                 Account usr = load(accountId);
                 if (ticket.isMandatorSupervisor() && ticket.getMandatorId() == usr.getMandatorId()) {
                     // MandatorSupervisor may access all users within his domain
                 } else {
                     FxNoAccessException nae = new FxNoAccessException("You may not access the ACLAssignment of user [" + accountId + "]");
                     if (LOG.isInfoEnabled()) LOG.info(nae);
                     throw nae;
                 }
             } catch (FxNotFoundException exc) {
                 return new ArrayList<ACLAssignment>(0);
             }
         }
 
         try {
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Fetch assignments
             //                            1             2       3         4         5           6           7
             curSql = "SELECT DISTINCT ass.USERGROUP,ass.ACL,ass.PREAD,ass.PEDIT,ass.PREMOVE,ass.PEXPORT,ass.PREL," +
                     //   8            9            10             11             12              13
                     "ass.PCREATE, acl.CAT_TYPE,ass.CREATED_BY,ass.CREATED_AT,ass.MODIFIED_BY,ass.MODIFIED_AT " +
                     "FROM " + TBL_ACLS_ASSIGNMENT + " ass, " + TBL_ASSIGN_GROUPS + " grp, " + TBL_ACLS + " acl " +
                     "WHERE acl.ID=ass.ACL AND ((ass.USERGROUP=grp.USERGROUP AND grp.ACCOUNT=" + accountId +
                     ")OR(ass.USERGROUP=" + UserGroup.GROUP_OWNER + "))";
 
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
 
             // Read the data
             ArrayList<ACLAssignment> result = new ArrayList<ACLAssignment>(20);
             while (rs != null && rs.next()) {
                 long groupId = rs.getLong(1);
                 /*if (groupId == UserGroup.GROUP_OWNER) {
                     // skip
                     continue;
                 }*/
                 result.add(new ACLAssignment(rs.getLong(2), groupId,
                         rs.getBoolean(3), rs.getBoolean(4), rs.getBoolean(7), rs.getBoolean(5),
                         rs.getBoolean(6), rs.getBoolean(8), ACLCategory.getById(rs.getByte(9)),
                         LifeCycleInfoImpl.load(rs, 10, 11, 12, 13)));
             }
             return result;
         } catch (SQLException exc) {
             FxLoadException dbe = new FxLoadException("Failed to load the ACL assignments for user [" +
                     accountId + "]: " + exc.getMessage(), exc);
             LOG.error(dbe);
             throw dbe;
         } finally {
             Database.closeObjects(ACLEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void fixContactData() throws FxApplicationException {
         List<Account> acct = loadAll(null, null, null, null, null, null, null, null, 0, Integer.MAX_VALUE);
         FxContext.get().runAsSystem();
         try {
             for (Account a : acct) {
                 if (a.getContactData().getId() == -1) {
                     FxContent contactData = co.initialize(CacheAdmin.getEnvironment().getType(FxType.CONTACTDATA).getId());
                     contactData.setAclId(ACL.ACL_CONTACTDATA);
                     contactData.setValue("/SURNAME", new FxString(false, a.getName()));
                     //contactData.setValue("/DISPLAYNAME", new FxString(true, a.getName()));
                     contactData.setValue("/EMAIL", new FxString(false, a.getEmail()));
                     FxPK contactDataPK = co.save(contactData);
                     update(a.getId(), null, null, null, null, null, null, null, null, null, null, null, null, contactDataPK.getId());
                 }
             }
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 }
