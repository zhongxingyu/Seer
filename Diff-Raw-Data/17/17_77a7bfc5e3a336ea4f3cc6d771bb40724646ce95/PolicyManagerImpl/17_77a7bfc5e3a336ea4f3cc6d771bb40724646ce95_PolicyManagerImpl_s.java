 package net.retakethe.policyauction.data.impl.manager;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import me.prettyprint.hector.api.query.QueryResult;
 import net.retakethe.policyauction.data.api.PolicyManager;
 import net.retakethe.policyauction.data.api.dao.PolicyDAO;
 import net.retakethe.policyauction.data.api.dao.PolicyDetailsDAO;
 import net.retakethe.policyauction.data.api.exceptions.NoSuchPolicyException;
 import net.retakethe.policyauction.data.api.types.PolicyID;
 import net.retakethe.policyauction.data.impl.dao.PolicyDAOImpl;
 import net.retakethe.policyauction.data.impl.dao.PolicyDetailsDAOImpl;
 import net.retakethe.policyauction.data.impl.query.api.ColumnSlice;
 import net.retakethe.policyauction.data.impl.query.api.KeyspaceManager;
 import net.retakethe.policyauction.data.impl.query.api.Mutator;
 import net.retakethe.policyauction.data.impl.query.api.OrderedRows;
 import net.retakethe.policyauction.data.impl.query.api.RangeSlicesQuery;
 import net.retakethe.policyauction.data.impl.query.api.Row;
 import net.retakethe.policyauction.data.impl.query.api.SliceQuery;
 import net.retakethe.policyauction.data.impl.schema.Schema;
 import net.retakethe.policyauction.data.impl.schema.Schema.PoliciesCF;
 import net.retakethe.policyauction.data.impl.schema.column.NamedColumn;
 import net.retakethe.policyauction.data.impl.schema.timestamp.MillisTimestamp;
 import net.retakethe.policyauction.data.impl.types.PolicyIDImpl;
 import net.retakethe.policyauction.util.CollectionUtils;
 import net.retakethe.policyauction.util.Functional;
 import net.retakethe.policyauction.util.Functional.Filter;
 import net.retakethe.policyauction.util.Functional.SkippedElementException;
 
 /**
  * @author Nick Clarke
  */
 public class PolicyManagerImpl extends AbstractDAOManagerImpl implements PolicyManager {
 
     public PolicyManagerImpl(KeyspaceManager keyspaceManager) {
         super(keyspaceManager);
     }
 
     @Override
     public PolicyID makePolicyID(String idString) {
         return new PolicyIDImpl(idString);
     }
 
     @Override
     public PolicyDAO getPolicy(PolicyID policyID) throws NoSuchPolicyException {
         // TODO: check policy state
 
        List<NamedColumn<PolicyID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.SHORT_NAME);
         SliceQuery<PolicyID, MillisTimestamp, String> query =
                 Schema.POLICIES.createSliceQuery(getKeyspaceManager(), policyID, list);
 
         QueryResult<ColumnSlice<MillisTimestamp, String>> queryResult = query.execute();
 
         ColumnSlice<MillisTimestamp, String> cs = queryResult.get();
 
         String shortName;
         try {
             shortName = getNonNullColumn(cs, Schema.POLICIES.SHORT_NAME);
         } catch (NoSuchColumnException e) {
             throw new NoSuchPolicyException(policyID);
         }
 
         return new PolicyDAOImpl(policyID, shortName);
     }
 
     @Override
     public PolicyDetailsDAO getPolicyDetails(PolicyID policyID) throws NoSuchPolicyException {
         List<NamedColumn<PolicyID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.SHORT_NAME,
                 (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.DESCRIPTION,
                 (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.LAST_EDITED);
         SliceQuery<PolicyID, MillisTimestamp, String> query =
             Schema.POLICIES.createSliceQuery(getKeyspaceManager(), policyID, list);
 
         QueryResult<ColumnSlice<MillisTimestamp, String>> queryResult = query.execute();
 
         ColumnSlice<MillisTimestamp, String> cs = queryResult.get();
 
         String shortName;
         String description;
         Date lastEdited;
         try {
             shortName = getNonNullColumn(cs, Schema.POLICIES.SHORT_NAME);
         } catch (NoSuchColumnException e) {
             throw new NoSuchPolicyException(policyID);
         }
         try {
             description = getNonNullColumn(cs, Schema.POLICIES.DESCRIPTION);
             lastEdited = getNonNullColumn(cs, Schema.POLICIES.LAST_EDITED);
         } catch (NoSuchColumnException e) {
             throw new RuntimeException("Invalid policy record for key " + policyID, e);
         }
 
         return new PolicyDetailsDAOImpl(policyID, shortName, description, lastEdited);
     }
 
     @Override
     public PolicyDetailsDAO createPolicy() {
         return new PolicyDetailsDAOImpl(new PolicyIDImpl());
     }
 
     @Override
     public List<PolicyDAO> getAllPolicies() {
        List<NamedColumn<PolicyID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.SHORT_NAME);
         RangeSlicesQuery<PolicyID, MillisTimestamp, String> query =
                Schema.POLICIES.createRangeSlicesQuery(getKeyspaceManager(),
                        list);
 
         // TODO: may need paging of data once we have more than a few hundred.
         //       This may need some sort of indexing since we're using RandomPartitioner,
         //       in order to return them in a useful order.
         query.setRowCount(1000);
         // TODO: needed?
         // query.setKeys("fake_key_0", "fake_key_4");
 
         QueryResult<OrderedRows<PolicyID, MillisTimestamp, String>> result = query.execute();
 
         OrderedRows<PolicyID, MillisTimestamp, String> orderedRows = result.get();
         if (orderedRows == null) {
             return Collections.emptyList();
         }
 
         return Functional.filter(orderedRows.getList(),
                 new Filter<Row<PolicyID, MillisTimestamp, String>, PolicyDAO>() {
                     @Override
                     public PolicyDAO filter(Row<PolicyID, MillisTimestamp, String> row)
                             throws SkippedElementException {
                         ColumnSlice<MillisTimestamp, String> cs = row.getColumnSlice();
                         if (cs == null) {
                             throw new SkippedElementException();
                         }
 
                         String shortName;
                         try {
                             shortName = getNonNullColumn(cs, Schema.POLICIES.SHORT_NAME);
                         } catch (NoSuchColumnException e) {
                             // Tombstone row
                             throw new SkippedElementException();
                         }
 
                         return new PolicyDAOImpl(row.getKey(), shortName);
                     }
                 });
     }
 
     @Override
     public List<PolicyDetailsDAO> getAllPolicyDetails() {
         List<NamedColumn<PolicyID, MillisTimestamp, String, ?>> list = CollectionUtils.list(
                 (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.SHORT_NAME,
                 (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.DESCRIPTION,
                 (NamedColumn<PolicyID, MillisTimestamp, String, ?>) Schema.POLICIES.LAST_EDITED);
         RangeSlicesQuery<PolicyID, MillisTimestamp, String> query =
             Schema.POLICIES.createRangeSlicesQuery(getKeyspaceManager(),
                     list);
         
         // TODO: may need paging of data once we have more than a few hundred.
         //       This may need some sort of indexing since we're using RandomPartitioner,
         //       in order to return them in a useful order.
         query.setRowCount(1000);
         // TODO: needed?
         // query.setKeys("fake_key_0", "fake_key_4");
         
         QueryResult<OrderedRows<PolicyID, MillisTimestamp, String>> result = query.execute();
         
         OrderedRows<PolicyID, MillisTimestamp, String> orderedRows = result.get();
         if (orderedRows == null) {
             return Collections.emptyList();
         }
         
         return Functional.filter(orderedRows.getList(),
                 new Filter<Row<PolicyID, MillisTimestamp, String>, PolicyDetailsDAO>() {
             @Override
             public PolicyDetailsDAO filter(Row<PolicyID, MillisTimestamp, String> row)
             throws SkippedElementException {
                 ColumnSlice<MillisTimestamp, String> cs = row.getColumnSlice();
                 if (cs == null) {
                     throw new SkippedElementException();
                 }
                 
                 String shortName;
                 try {
                     shortName = getNonNullColumn(cs, Schema.POLICIES.SHORT_NAME);
                 } catch (NoSuchColumnException e) {
                     // Tombstone row
                     throw new SkippedElementException();
                 }
                 
                 String description;
                 Date lastEdited;
                 try {
                     description = getNonNullColumn(cs, Schema.POLICIES.DESCRIPTION);
                     lastEdited = getNonNullColumn(cs, Schema.POLICIES.LAST_EDITED);
                 } catch (NoSuchColumnException e) {
                     throw new RuntimeException("Invalid policy record for key " + row.getKey(), e);
                 }
 
                 return new PolicyDetailsDAOImpl(row.getKey(), shortName, description,
                         lastEdited);
             }
         });
     }
     
     @Override
     public void save(PolicyDetailsDAO policy) {
         if (policy == null) {
             throw new IllegalArgumentException("policy must not be null");
         }
 
         PolicyID policyID = policy.getPolicyID();
 
         PoliciesCF cf = Schema.POLICIES;
         MillisTimestamp ts = cf.createCurrentTimestamp();
         Mutator<PolicyID, MillisTimestamp> m = cf.createMutator(getKeyspaceManager());
 
         cf.SHORT_NAME.addColumnInsertion(m, policyID, cf.createValue(policy.getShortName(), ts));
         cf.DESCRIPTION.addColumnInsertion(m, policyID, cf.createValue(policy.getDescription(), ts));
 
         // We're saving changes, so update the edit time
         cf.LAST_EDITED.addColumnInsertion(m, policyID, cf.createValue(new Date(), ts));
 
         // TODO: error handling? Throws HectorException.
         m.execute();
     }
 
     @Override
     public void deletePolicy(PolicyID policyID) {
         if (policyID == null) {
             throw new IllegalArgumentException("policyID must not be null");
         }
 
         Mutator<PolicyID, MillisTimestamp> m = Schema.POLICIES.createMutator(getKeyspaceManager());
 
         Schema.POLICIES.addRowDeletion(m, policyID);
 
         m.execute();
 
         // TODO: this will need to delete from other ColumnFamilies too and trigger recalcs
     }
 }
