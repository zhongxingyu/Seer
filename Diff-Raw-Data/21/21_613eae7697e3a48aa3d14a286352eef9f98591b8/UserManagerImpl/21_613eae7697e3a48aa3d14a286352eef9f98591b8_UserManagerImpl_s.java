 package net.retakethe.policyauction.data.impl.manager;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import me.prettyprint.hector.api.query.QueryResult;
 import net.retakethe.policyauction.data.api.UserManager;
 import net.retakethe.policyauction.data.api.dao.UserDAO;
 import net.retakethe.policyauction.data.api.types.UserID;
 import net.retakethe.policyauction.data.impl.dao.UserDAOImpl;
 import net.retakethe.policyauction.data.impl.query.api.ColumnSlice;
 import net.retakethe.policyauction.data.impl.query.api.KeyspaceManager;
 import net.retakethe.policyauction.data.impl.query.api.Mutator;
 import net.retakethe.policyauction.data.impl.query.api.OrderedRows;
 import net.retakethe.policyauction.data.impl.query.api.RangeSlicesQuery;
 import net.retakethe.policyauction.data.impl.query.api.Row;
 import net.retakethe.policyauction.data.impl.query.api.SliceQuery;
 import net.retakethe.policyauction.data.impl.schema.Schema;
 import net.retakethe.policyauction.data.impl.schema.Schema.UsersCF;
 import net.retakethe.policyauction.data.impl.schema.column.NamedColumn;
 import net.retakethe.policyauction.data.impl.schema.timestamp.MillisTimestamp;
 import net.retakethe.policyauction.data.impl.types.UserIDImpl;
 import net.retakethe.policyauction.util.CollectionUtils;
 import net.retakethe.policyauction.util.Functional;
 import net.retakethe.policyauction.util.Functional.Filter;
 import net.retakethe.policyauction.util.Functional.SkippedElementException;
 
 /**
  * @author Mathew Hartley
  */
 public class UserManagerImpl extends AbstractDAOManagerImpl implements UserManager {
 
    private final KeyspaceManager keyspaceManager;

     public UserManagerImpl(KeyspaceManager keyspaceManager) {
        super();
        if (keyspaceManager == null) {
            throw new IllegalArgumentException("keyspace must not be null");
        }
        this.keyspaceManager = keyspaceManager;
     }
 
     @Override
     public UserID makeUserID(String idString) {
         return new UserIDImpl(idString);
     }
 
     @Override
     public UserDAO getUser(UserID userID) throws NoSuchUserException {
         List<NamedColumn<UserID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.EMAIL,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_HASH,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.FIRST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.LAST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.SHOW_REAL_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.CREATED_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_DATE,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.USER_ROLE);
         SliceQuery<UserID, MillisTimestamp, String> query =
                Schema.USERS.createSliceQuery(keyspaceManager, userID, list);
 
         QueryResult<ColumnSlice<MillisTimestamp, String>> queryResult = query.execute();
 
         ColumnSlice<MillisTimestamp, String> cs = queryResult.get();
 
         String username;
         String email;
         String passwordHash;
         Date passwordExpiryTimestamp;
         
         String firstName;
         String lastName;
         Boolean showRealName = false;
         Date createdTimestamp;
         Date voteSalaryLastPaidTimestamp;
         Date voteSalaryDate;
         
         String userRole;
         
         // FIXME - Using userid as username, will need another call to build this field.
         username = userID.asString();
         try {
             email = getNonNullColumn(cs, Schema.USERS.EMAIL);
         } catch (NoSuchColumnException e) {
             throw new NoSuchUserException(userID);
         }
         try {
             passwordHash = getNonNullColumn(cs, Schema.USERS.PASSWORD_HASH);
             passwordExpiryTimestamp = getNonNullColumn(cs, Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP);
             
             createdTimestamp = getNonNullColumn(cs, Schema.USERS.CREATED_TIMESTAMP);
         } catch (NoSuchColumnException e) {
             throw new RuntimeException("Invalid user record for key " + userID, e);
         }
         firstName = getColumnOrNull(cs, Schema.USERS.FIRST_NAME);
         lastName = getColumnOrNull(cs, Schema.USERS.LAST_NAME);
         showRealName = getColumnOrNull(cs, Schema.USERS.SHOW_REAL_NAME);
 
         voteSalaryLastPaidTimestamp = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP);
         voteSalaryDate = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_DATE);
 
         userRole = getColumnOrNull(cs, Schema.USERS.USER_ROLE);
         
         return new UserDAOImpl(userID, username, email, passwordHash, passwordExpiryTimestamp, firstName, lastName, showRealName, createdTimestamp, voteSalaryLastPaidTimestamp, voteSalaryDate, userRole);
     }
 
     @Override
     public UserDAO createUser() {
         return new UserDAOImpl(new UserIDImpl());
     }
 
     @Override
     public List<UserDAO> getAllUsers() {
         List<NamedColumn<UserID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.EMAIL,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_HASH,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.FIRST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.LAST_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.SHOW_REAL_NAME,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.CREATED_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.VOTE_SALARY_DATE,
                 (NamedColumn<UserID, MillisTimestamp, String, ?>) Schema.USERS.USER_ROLE);
         RangeSlicesQuery<UserID, MillisTimestamp, String> query =
                Schema.USERS.createRangeSlicesQuery(keyspaceManager, list);
 
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
                         Boolean showRealName = false;
                         Date createdTimestamp;
                         Date voteSalaryLastPaidTimestamp;
                         Date voteSalaryDate;
                         
                         String userRole;
                         
                         // FIXME - Using userid as username, will need another call to build this field.
                         username = row.getKey().asString();
                         try {
                             email = getNonNullColumn(cs, Schema.USERS.EMAIL);
                             
                             passwordHash = getNonNullColumn(cs, Schema.USERS.PASSWORD_HASH);
                             passwordExpiryTimestamp = getNonNullColumn(cs, Schema.USERS.PASSWORD_EXPIRY_TIMESTAMP);
                             
                             createdTimestamp = getNonNullColumn(cs, Schema.USERS.CREATED_TIMESTAMP);
                         } catch (NoSuchColumnException e) {
                             throw new RuntimeException("Invalid user record for key " + row.getKey(), e);
                         }
                         firstName = getColumnOrNull(cs, Schema.USERS.FIRST_NAME);
                         lastName = getColumnOrNull(cs, Schema.USERS.LAST_NAME);
                         showRealName = getColumnOrNull(cs, Schema.USERS.SHOW_REAL_NAME);
 
                         voteSalaryLastPaidTimestamp = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_LAST_PAID_TIMESTAMP);
                         voteSalaryDate = getColumnOrNull(cs, Schema.USERS.VOTE_SALARY_DATE);
 
                         userRole = getColumnOrNull(cs, Schema.USERS.USER_ROLE);
                         
                         return new UserDAOImpl(row.getKey(), username, email, passwordHash, passwordExpiryTimestamp, firstName, lastName, showRealName, createdTimestamp, voteSalaryLastPaidTimestamp, voteSalaryDate, userRole);
                     }
                 });
     }
 
     @Override
     public void persist(UserDAO user) {
         UserID userID = user.getUserID();
 
         UsersCF cf = Schema.USERS;
         MillisTimestamp ts = cf.createCurrentTimestamp();
        Mutator<UserID, MillisTimestamp> m = cf.createMutator(keyspaceManager);
 
         //cf.USERNAME.addColumnInsertion(m, userID, cf.createValue(user.getShortName(), ts));
         cf.EMAIL.addColumnInsertion(m, userID, cf.createValue(user.getEmail(), ts));
         cf.PASSWORD_HASH.addColumnInsertion(m, userID, cf.createValue(user.getPasswordHash(), ts));
         cf.PASSWORD_EXPIRY_TIMESTAMP.addColumnInsertion(m, userID, cf.createValue(user.getPasswordExpiryTimestamp(), ts));
         cf.FIRST_NAME.addColumnInsertion(m, userID, cf.createValue(user.getFirstName(), ts));
         cf.LAST_NAME.addColumnInsertion(m, userID, cf.createValue(user.getLastName(), ts));
         
         cf.SHOW_REAL_NAME.addColumnInsertion(m, userID, cf.createValue(user.isShowRealName(), ts));
         cf.CREATED_TIMESTAMP.addColumnInsertion(m, userID, cf.createValue(user.getCreatedTimestamp(), ts));
         cf.VOTE_SALARY_LAST_PAID_TIMESTAMP.addColumnInsertion(m, userID, cf.createValue(user.getVoteSalaryLastPaidTimestamp(), ts));
         cf.VOTE_SALARY_DATE.addColumnInsertion(m, userID, cf.createValue(user.getVoteSalaryDate(), ts));
         cf.USER_ROLE.addColumnInsertion(m, userID, cf.createValue(user.getUserRole().toString(), ts));
 
 
         // TODO: error handling? Throws HectorException.
         m.execute();
     }
 
     @Override
     public void deleteUser(UserDAO user) {
         UserID userID = user.getUserID();
 
        Mutator<UserID, MillisTimestamp> m = Schema.USERS.createMutator(keyspaceManager);
 
         Schema.USERS.addRowDeletion(m, userID);
 
         m.execute();
 
         // TODO: this will need to delete from other ColumnFamilies too and trigger recalcs
     }
 }
