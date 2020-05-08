 package org.wonderapp.dao;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import net.mantucon.baracus.annotations.Bean;
 import net.mantucon.baracus.dao.BaseDao;
 import net.mantucon.baracus.orm.*;
 import org.wonderapp.model.Account;
 import org.wonderapp.model.Customer;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import static net.mantucon.baracus.orm.AbstractModelBase.idCol;
 import static org.wonderapp.model.Account.*;
 
 /**
  * Sample DAO class implementing a rowmapper for an entity with no
  * lazy loading functions.
  */
 @Bean
 public class AccountDao extends BaseDao<Account> {
 
     @Bean
     CustomerDao customerDao;
 
     /**
      * The rowmapper has two basic tasks : create an object out of one row
      * and to generate a content values object.
      * The decision to make hand-written rowmappers has been done in order
      * to enable the users having smart rowmappers and the freedom to decide
      * how to deal with mapping instead of having generated code,
      *
      */
     private final RowMapper<Account> accountRowMapper = new RowMapper<Account>() {
         @Override
         public Account from(Cursor c) {
             // Map the cursor row data to an appropriate object
             Account result = new Account();
             result.setId(c.getLong(idCol.fieldIndex));
             result.setAccountNumber(c.getString(accountNumberCol.fieldIndex));
             result.setAccountName(c.getString(accountNameCol.fieldIndex));
             final Long customerId = c.getLong(customerIdCol.fieldIndex);
 
             // Lazy many-to-one reference. simply pass a lazy reference containing a ReferenceLoader
             // carrying the dao and the id
             result.setCustomerReference(
                     new LazyReference<Customer>(
                             new ReferenceLoader<Customer>(customerDao,customerId)));
 
             // important, do not forget to mark this result as a already persisted record.
             result.setTransient(false);
 
             return result;
         }
 
         @Override
         public String getAffectedTable() {
             return TABLE_NAME;
         }
 
         @Override
         public FieldList getFieldList() {
             return fieldList;
         }
 
         @Override
         public Field getNameField() {
             return accountNameCol;
         }
 
         @Override
         public ContentValues getContentValues(Account item) {
         // instead of dynamic code relation, we make this mapping by hand. done once, used many
         // much faster than any annotation based dynamic generated persistence code
             ContentValues result = new ContentValues();
             if (item.getId() != null) { result.put(idCol.fieldName, item.getId()); }
             if (item.getAccountNumber() != null) { result.put(accountNumberCol.fieldName, item.getAccountNumber()); }
             if (item.getAccountName() != null) { result.put(accountNameCol.fieldName, item.getAccountName()); }
             if (item.getCustomerReference() != null) { result.put(customerIdCol.fieldName, item.getCustomerReference().getObjectRefId()); }
             return result;
 
         }
     };
 

    public AccountDao() {
        super(Account.class);
     }
 
     @Override
     public RowMapper<Account> getRowMapper() {
         return accountRowMapper;
     }
 
     public List<Account> getByCustomerId(Long customerId) {
         Cursor c = null;
         List<Account> result = new LinkedList<Account>();
         try {
             c = this.getDb().query(true, accountRowMapper.getAffectedTable(), accountRowMapper.getFieldList().getFieldNames(), Account.customerIdCol.fieldName + "=" + customerId.toString(), null, null, null, null, null);
             result = iterateCursor(c);
         } finally {
             if (c != null && !c.isClosed()) {
                 c.close();
             }
         }
         return result;
     }
 
 }
