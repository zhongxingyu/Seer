 package net.retakethe.policyauction.data.impl.manager;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import me.prettyprint.hector.api.query.QueryResult;
 import net.retakethe.policyauction.data.api.UserManager;
 import net.retakethe.policyauction.data.api.dao.UserDAO;
 import net.retakethe.policyauction.data.api.dao.UserPendingDAO;
 import net.retakethe.policyauction.data.api.dao.UsernameDAO;
 import net.retakethe.policyauction.data.api.types.UserID;
 import net.retakethe.policyauction.data.api.types.UserRole;
 import net.retakethe.policyauction.data.impl.dao.UserDAOImpl;
 import net.retakethe.policyauction.data.impl.dao.UserPendingDAOImpl;
 import net.retakethe.policyauction.data.impl.dao.UsernameDAOImpl;
 import net.retakethe.policyauction.data.impl.query.api.ColumnSlice;
 import net.retakethe.policyauction.data.impl.query.api.KeyspaceManager;
 import net.retakethe.policyauction.data.impl.query.api.Mutator;
 import net.retakethe.policyauction.data.impl.query.api.OrderedRows;
 import net.retakethe.policyauction.data.impl.query.api.RangeSlicesQuery;
 import net.retakethe.policyauction.data.impl.query.api.Row;
 import net.retakethe.policyauction.data.impl.query.api.SliceQuery;
 import net.retakethe.policyauction.data.impl.schema.Schema;
 import net.retakethe.policyauction.data.impl.schema.Schema.UserRolesCF;
 import net.retakethe.policyauction.data.impl.schema.Schema.UsersByNameCF;
 import net.retakethe.policyauction.data.impl.schema.Schema.UsersCF;
 import net.retakethe.policyauction.data.impl.schema.Schema.UsersPendingCF;
 import net.retakethe.policyauction.data.impl.schema.column.NamedColumn;
 import net.retakethe.policyauction.data.impl.schema.timestamp.MillisTimestamp;
 import net.retakethe.policyauction.data.impl.types.UserIDImpl;
 import net.retakethe.policyauction.util.CollectionUtils;
 import net.retakethe.policyauction.util.Functional;
 import net.retakethe.policyauction.util.Functional.Filter;
 import net.retakethe.policyauction.util.Functional.SkippedElementException;
 
 /**
  * User management service.
  * 
  * @author Mathew Hartley
  */
 public class UserManagerImpl extends AbstractDAOManagerImpl implements UserManager {
 
     public UserManagerImpl(KeyspaceManager keyspaceManager) {
         super(keyspaceManager);
     }
 
     @Override
     public UserID makeUserID(String idString) {
         return new UserIDImpl(idString);
     }
 
     @Override
     public UserDAO getUser(UserID userID) throws NoSuchUserException {
         List<NamedColumn<UserID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.USERNAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.EMAIL,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_HASH,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.FIRST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.LAST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.SHOW_REAL_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.CREATED_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_DATE);
         SliceQuery<UserID, MillisTimestamp, String> query =
                 Schema.USERS.createSliceQuery(getKeyspaceManager(), userID, list);
 
         QueryResult<ColumnSlice<MillisTimestamp, String>> queryResult = query.execute();
 
         ColumnSlice<MillisTimestamp, String> cs = queryResult.get();
 
         String username;
         String email;
         String passwordHash;
         Date passwordExpiryTimestamp;
         
         String firstName;
         String lastName;
         boolean showRealName = false;
         Date createdTimestamp;
         Date voteSalaryLastPaidTimestamp;
         Date voteSalaryDate;
         
         List<UserRole> userRoles;
         
         try {
             email = getNonNullColumn(cs, Schema.USERS.EMAIL);
             createdTimestamp = getNonNullColumn(cs, Schema.USERS.CREATED_TIMESTAMP);
         } catch (NoSuchColumnException e) {
             throw new NoSuchUserException(userID);
         }
 
     	username = getColumnOrNull(cs, Schema.USERS.USERNAME);
         passwordHash = getColumnOrNull(cs, Schema.USERS.PASSWORD_HASH);
         passwordExpiryTimestamp = getColumnOrNull(cs, Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP);
 
         firstName = getColumnOrNull(cs, Schema.USERS.FIRST_NAME);
         lastName = getColumnOrNull(cs, Schema.USERS.LAST_NAME);
         showRealName = getColumnOrDefault(cs, Schema.USERS.SHOW_REAL_NAME, false);
 
         voteSalaryLastPaidTimestamp = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP);
         voteSalaryDate = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_DATE);
 
         userRoles = getUserRoles(userID);
         
         return new UserDAOImpl(userID, username, email, passwordHash, passwordExpiryTimestamp, firstName, lastName, showRealName, createdTimestamp, voteSalaryLastPaidTimestamp, voteSalaryDate, userRoles);
     }
     
     @Override
     public UsernameDAO getUsername(String username) throws NoSuchUserException {
 
         List<NamedColumn<String, MillisTimestamp, String, ?>> list =
                 new ArrayList<NamedColumn<String, MillisTimestamp, String, ?>>(1);
         list.add(Schema.USERS_BY_NAME.USER_ID);
         SliceQuery<String, MillisTimestamp, String> query =
                 Schema.USERS_BY_NAME.createSliceQuery(getKeyspaceManager(), username, list);
 
         QueryResult<ColumnSlice<MillisTimestamp, String>> queryResult = query.execute();
 
         ColumnSlice<MillisTimestamp, String> cs = queryResult.get();
         try {
 			return new UsernameDAOImpl(getNonNullColumn(cs, Schema.USERS_BY_NAME.USER_ID), username);
 		} catch (NoSuchColumnException e) {
			throw new RuntimeException("Invalid username record for key " + username, e);
 		}
     }
     
     @Override
     public UserPendingDAO getUserPending(String email) throws NoSuchUserException {
 
     	List<NamedColumn<String, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<String, MillisTimestamp, String, ?>) Schema.USERS_PENDING.USER_ID,
                 (NamedColumn<String, MillisTimestamp, String, ?>) Schema.USERS_PENDING.ACTIVATION_CODE);
         SliceQuery<String, MillisTimestamp, String> query =
                 Schema.USERS_PENDING.createSliceQuery(getKeyspaceManager(), email, list);
 
         QueryResult<ColumnSlice<MillisTimestamp, String>> queryResult = query.execute();
 
         ColumnSlice<MillisTimestamp, String> cs = queryResult.get();
         try {
 			return new UserPendingDAOImpl(email, getNonNullColumn(cs, Schema.USERS_PENDING.USER_ID), getNonNullColumn(cs, Schema.USERS_PENDING.ACTIVATION_CODE));
 		} catch (NoSuchColumnException e) {
 			throw new RuntimeException("Invalid user pending record for key " + email, e);
 		}
     }
 
     @Override
     public UserDAO createUser() {
         UserDAOImpl newUserDAO = new UserDAOImpl(new UserIDImpl());
 		return newUserDAO;
     }
     
     @Override
     public UserDAO activateUser(UserDAO user) {
     	if (user.getPasswordExpiryTimestamp()!=null) {
     		throw new IllegalArgumentException("User: " + user.getEmail() + " is already activated");
     	}
     	Date now = new Date();
     	user.setCreatedTimestamp(now);
     	user.getUserRoles().addAll(getDefaultUserRoles());
     	
     	persist(user);
 		
     	return user;
     }
 
     @Override
     public List<UserDAO> getAllUsers() {
         List<NamedColumn<UserID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.USERNAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.EMAIL,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_HASH,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.FIRST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.LAST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.SHOW_REAL_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.CREATED_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_DATE);
         RangeSlicesQuery<UserID, MillisTimestamp, String> query =
                 Schema.USERS.createRangeSlicesQuery(getKeyspaceManager(), list);
 
         // TODO: may need paging of data once we have more than a few hundred.
         //       This may need some sort of indexing since we're using RandomPartitioner,
         //       in order to return them in a useful order.
         query.setRowCount(1000);
         // TODO: needed?
         // query.setKeys("fake_key_0", "fake_key_4");
 
         QueryResult<OrderedRows<UserID, MillisTimestamp, String>> result = query.execute();
 
         OrderedRows<UserID, MillisTimestamp, String> orderedRows = result.get();
         if (orderedRows == null) {
             return Collections.emptyList();
         }
 
         return Functional.filter(orderedRows.getList(),
                 new Filter<Row<UserID, MillisTimestamp, String>, UserDAO>() {
                     @Override
                     public UserDAO filter(Row<UserID, MillisTimestamp, String> row)
                             throws SkippedElementException {
                         ColumnSlice<MillisTimestamp, String> cs = row.getColumnSlice();
                         if (cs == null) {
                             throw new SkippedElementException();
                         }
 
                         String username;
                         String email;
                         String passwordHash;
                         Date passwordExpiryTimestamp;
                         
                         String firstName;
                         String lastName;
                         boolean showRealName = false;
                         Date createdTimestamp;
                         Date voteSalaryLastPaidTimestamp;
                         Date voteSalaryDate;
                         
                         List<UserRole> userRoles;
                         
                         try {
                             createdTimestamp = getNonNullColumn(cs, Schema.USERS.CREATED_TIMESTAMP);
                         	email = getNonNullColumn(cs, Schema.USERS.EMAIL);
                         	userRoles = getUserRoles(row.getKey());
                         } catch (NoSuchColumnException e) {
                         	throw new RuntimeException("Invalid user record for key " + row.getKey(), e);
                         } catch (NoSuchUserException e) {
                         	throw new RuntimeException("Invalid user record for key " + row.getKey(), e);
 						}
                         	username = getColumnOrNull(cs, Schema.USERS.USERNAME);
                             passwordHash = getColumnOrNull(cs, Schema.USERS.PASSWORD_HASH);
                             passwordExpiryTimestamp = getColumnOrNull(cs, Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP);
                             
 
                         firstName = getColumnOrNull(cs, Schema.USERS.FIRST_NAME);
                         lastName = getColumnOrNull(cs, Schema.USERS.LAST_NAME);
                         showRealName = getColumnOrDefault(cs, Schema.USERS.SHOW_REAL_NAME, false);
 
                         voteSalaryLastPaidTimestamp = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP);
                         voteSalaryDate = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_DATE);
 
                         
                         return new UserDAOImpl(row.getKey(), username, email, passwordHash, passwordExpiryTimestamp, firstName, lastName, showRealName, createdTimestamp, voteSalaryLastPaidTimestamp, voteSalaryDate, userRoles);
                     }
                 });
     }
 
     @Override
     public void update(UserDAO user) {
     	// Clear the pending user details.
     	deleteUserPending(user);
     	
         saveUserDAO(user);
 
         saveUsername(user);
 
         saveUserRoles(user);
     }
     
     /** {@inheritDoc} */
     @Override
     public void persist(UserDAO user) {
     	saveMinimalUserDAO(user);
         saveNewUser(user);
         // TODO send email here perhaps...
     }
     
     /**
      * Save new user (where the user has an email address, uuid and that's it). This is where {@link UsersPendingCF} is used.
      *
      * @param user the user
      */
     private void saveNewUser(UserDAO user) {
     	UserID userID = user.getUserID();
     	String userEmail = user.getEmail();
     	
     	UsersPendingCF usersPendingCF = Schema.USERS_PENDING;
         MillisTimestamp ts = usersPendingCF.createCurrentTimestamp();
         Mutator<String, MillisTimestamp> usersPendingMutator = usersPendingCF.createMutator(getKeyspaceManager());
         
         usersPendingCF.ACTIVATION_CODE.addColumnInsertion(usersPendingMutator, userEmail, usersPendingCF.createValue(user.getActivationCode(), ts));
         usersPendingCF.USER_ID.addColumnInsertion(usersPendingMutator, userEmail, usersPendingCF.createValue(userID, ts));
         usersPendingMutator.execute();
     }
     
     
 	/**
 	 * Save minimal user dao, for the initial creation of a new user.
 	 *
 	 * @param user the user
 	 */
 	private void saveMinimalUserDAO(UserDAO user) {
 		UserID userID = user.getUserID();
 
         UsersCF userscf = Schema.USERS;
         MillisTimestamp ts = userscf.createCurrentTimestamp();
         Mutator<UserID, MillisTimestamp> usersMutator = userscf.createMutator(getKeyspaceManager());
         
         userscf.EMAIL.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getEmail(), ts));
         userscf.CREATED_TIMESTAMP.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getCreatedTimestamp(), ts));
         
         usersMutator.execute();
 	}
 
 	private void saveUserDAO(UserDAO user) {
 		UserID userID = user.getUserID();
 
         UsersCF userscf = Schema.USERS;
         MillisTimestamp ts = userscf.createCurrentTimestamp();
         Mutator<UserID, MillisTimestamp> usersMutator = userscf.createMutator(getKeyspaceManager());
 
         userscf.USERNAME.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getUsername(), ts));
         userscf.EMAIL.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getEmail(), ts));
         userscf.PASSWORD_HASH.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getPasswordHash(), ts));
         userscf.PASSWORD_EXPIRY_TIMESTAMP.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getPasswordExpiryTimestamp(), ts));
         userscf.FIRST_NAME.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getFirstName(), ts));
         userscf.LAST_NAME.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getLastName(), ts));
         
         userscf.SHOW_REAL_NAME.addColumnInsertion(usersMutator, userID, userscf.createValue(user.isShowRealName(), ts));
         userscf.CREATED_TIMESTAMP.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getCreatedTimestamp(), ts));
         userscf.VOTE_SALARY_LAST_PAID_TIMESTAMP.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getVoteSalaryLastPaidTimestamp(), ts));
         userscf.VOTE_SALARY_DATE.addColumnInsertion(usersMutator, userID, userscf.createValue(user.getVoteSalaryDate(), ts));
         // TODO: error handling? Throws HectorException.
         usersMutator.execute();
 	}
 
 	private void saveUsername(UserDAO user) {
 		MillisTimestamp ts;
 		UsersByNameCF usersByNameCF = Schema.USERS_BY_NAME;
         ts = usersByNameCF.createCurrentTimestamp();
         Mutator<String, MillisTimestamp> usersByNameMutator = usersByNameCF.createMutator(getKeyspaceManager());
         
         usersByNameCF.USER_ID.addColumnInsertion(usersByNameMutator, user.getUsername(), usersByNameCF.createValue(user.getUserID(), ts));
         usersByNameMutator.execute();
 	}
 
 	private void saveUserRoles(UserDAO user) {
 		MillisTimestamp ts;
 		UserRolesCF userRolesCF = Schema.USER_ROLES;
         ts = userRolesCF.createCurrentTimestamp();
         Mutator<UserID, MillisTimestamp> userRolesMutator = userRolesCF.createMutator(getKeyspaceManager());
         for (UserRole role : user.getUserRoles()) {
         	userRolesCF.USER_ROLE.addColumnInsertion(userRolesMutator, user.getUserID(), userRolesCF.createValue(role, ts));
         }
         userRolesMutator.execute();
 	}
 
     @Override
     public void deleteUser(UserDAO user) {
         UserID userID = user.getUserID();
 
         Mutator<UserID, MillisTimestamp> m = Schema.USERS.createMutator(getKeyspaceManager());
         Mutator<String, MillisTimestamp> usersByNameMutator = Schema.USERS_BY_NAME.createMutator(getKeyspaceManager());
         
         Schema.USERS.addRowDeletion(m, userID);
         Schema.USERS_BY_NAME.addRowDeletion(usersByNameMutator, user.getUsername());
 
         m.execute();
         usersByNameMutator.execute();
 
         // TODO: this will need to delete from other ColumnFamilies too and trigger recalcs
     }
     
 
     @Override
     public void deleteUserPending(UserDAO user) {
 
         Mutator<String, MillisTimestamp> m = Schema.USERS_PENDING.createMutator(getKeyspaceManager());
         
         Schema.USERS_PENDING.addRowDeletion(m, user.getEmail());
 
         m.execute();
         // TODO: this will need to delete from other ColumnFamilies too and trigger recalcs
     }
 
 	@Override
 	public List<UserRole> getUserRoles(UserID userID) throws NoSuchUserException {
 		List<NamedColumn<UserID, MillisTimestamp, String, ?>> list =
                 new ArrayList<NamedColumn<UserID, MillisTimestamp, String, ?>>(1);
         list.add(Schema.USER_ROLES.USER_ROLE);
         RangeSlicesQuery<UserID, MillisTimestamp, String> query =
                 Schema.USER_ROLES.createRangeSlicesQuery(getKeyspaceManager(), list);
         query.setKeys(userID, userID);
         
         QueryResult<OrderedRows<UserID, MillisTimestamp, String>> queryResult = query.execute();
 
         OrderedRows<UserID, MillisTimestamp, String> orderedRows = queryResult.get();
         if (orderedRows == null) {
             return Collections.emptyList();
         }
 
         return Functional.filter(orderedRows.getList(),
                 new Filter<Row<UserID, MillisTimestamp, String>, UserRole>() {
                     @Override
                     public UserRole filter(Row<UserID, MillisTimestamp, String> row) throws SkippedElementException {
                         ColumnSlice<MillisTimestamp, String> cs = row.getColumnSlice();
                         if (cs == null) {
                             throw new SkippedElementException();
                         }
 
                         UserRole userRole;
                         try {
                         	userRole = getNonNullColumn(cs, Schema.USER_ROLES.USER_ROLE);
                         } catch (NoSuchColumnException e) {
                             throw new RuntimeException("Invalid user record for key " + row.getKey(), e);
                         }
                         return userRole;
                     }
                 });
 	}
 	
 	private List<UserRole> getDefaultUserRoles() {
 		List<UserRole> defaultList = new ArrayList<UserRole>();
 		defaultList.add(UserRole.LOGIN);
 		
 		defaultList.add(UserRole.COMMENT_CREATE_REPLY);
 		defaultList.add(UserRole.COMMENT_CREATE_THREAD);
 		
 		defaultList.add(UserRole.POLICY_CREATE);
 		defaultList.add(UserRole.ITEMS_REPORT);
 		
 		return defaultList;
 	}
 }
