 /* This file is part of VoltDB.
  * Copyright (C) 2008-2012 VoltDB Inc.
  *
  * VoltDB is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * VoltDB is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.voltdb.plannodes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.json_voltpatches.JSONException;
 import org.json_voltpatches.JSONString;
 import org.json_voltpatches.JSONStringer;
 import org.voltdb.catalog.Cluster;
 import org.voltdb.catalog.ColumnRef;
 import org.voltdb.catalog.Database;
 import org.voltdb.catalog.Index;
 import org.voltdb.catalog.Table;
 import org.voltdb.compiler.DatabaseEstimates;
 import org.voltdb.compiler.ScalarValueHints;
 import org.voltdb.expressions.AbstractExpression;
 import org.voltdb.expressions.ComparisonExpression;
 import org.voltdb.expressions.TupleValueExpression;
 import org.voltdb.planner.PlanStatistics;
 import org.voltdb.planner.StatsField;
 import org.voltdb.types.ExpressionType;
 import org.voltdb.types.IndexLookupType;
 import org.voltdb.types.PlanNodeType;
 import org.voltdb.utils.CatalogUtil;
 
 public class IndexCountPlanNode extends AbstractScanPlanNode {
 
     public enum Members {
         TARGET_INDEX_NAME,
         KEY_ITERATE,
         SEARCHKEY_EXPRESSIONS,
         ENDKEY_EXPRESSIONS,
         LOOKUP_TYPE,
         END_TYPE;
     }
 
     /**
      * Attributes
      */
 
     // The index to use in the scan operation
     protected String m_targetIndexName;
 
     // ???
     protected Boolean m_keyIterate = false;
 
     //
     protected List<AbstractExpression> m_endkeyExpressions = null;
 
     // This list of expressions corresponds to the values that we will use
     // at runtime in the lookup on the index
     protected List<AbstractExpression> m_searchkeyExpressions = new ArrayList<AbstractExpression>();
 
     // The overall index lookup operation type
     protected IndexLookupType m_LookupType = IndexLookupType.EQ;
 
     // The overall index lookup operation type
     protected IndexLookupType m_endType = IndexLookupType.EQ;
 
     // A reference to the Catalog index object which defined the index which
     // this index scan is going to use
     protected Index m_catalogIndex = null;
 
     protected Boolean m_endExprValid = false;
 
     public IndexCountPlanNode() {
         super();
     }
 
     public IndexCountPlanNode(IndexScanPlanNode isp) {
         super();
 
         m_catalogIndex = isp.m_catalogIndex;
 
         m_estimatedOutputTupleCount = 1;
         m_tableSchema = isp.m_tableSchema;
         m_tableScanSchema = isp.m_tableScanSchema.clone();
 
         m_targetTableAlias = isp.m_targetTableAlias;
         m_targetTableName = isp.m_targetTableName;
         m_targetIndexName = isp.m_targetIndexName;
 
         m_LookupType = isp.m_lookupType;
         m_searchkeyExpressions = isp.m_searchkeyExpressions;
         m_predicate = null;
 
         if (isp.getEndExpression() != null)
             this.setEndKeyExpression(isp.getEndExpression());
         else
             this.m_endExprValid = true;
     }
 
     @Override
     public PlanNodeType getPlanNodeType() {
         return PlanNodeType.INDEXCOUNT;
     }
 
     @Override
     public void validate() throws Exception {
         super.validate();
 
         // There needs to be at least one search key expression
         if (m_searchkeyExpressions.isEmpty()) {
             throw new Exception("ERROR: There were no search key expressions defined for " + this);
         }
 
         for (AbstractExpression exp : m_searchkeyExpressions) {
             exp.validate();
         }
     }
 
     /**
      * Accessor for flag marking the plan as guaranteeing an identical result/effect
      * when "replayed" against the same database state, such as during replication or CL recovery.
      * @return true for unique index scans
      */
     @Override
     public boolean isOrderDeterministic() {
         if (m_catalogIndex.getUnique()) {
             // Any unique index scan capable of returning multiple rows will return them in a fixed order.
             // XXX: This may not be strictly true if/when we support order-determinism based on a mix of columns
             // from different joined tables -- an equality filter based on a non-ordered column from the other table
             // would not produce predictably ordered results even when the other table is ordered by all of its display columns
             // but NOT the column used in the equality filter.
             return true;
         }
         // Assuming (?!) that the relative order of the "multiple entries" in a non-unique index can not be guaranteed,
         // the only case in which a non-unique index can guarantee determinism is for an indexed-column-only scan,
         // because it would ignore any differences in the entries.
         // TODO: return true for an index-only scan --
         // That would require testing of an inline projection node consisting solely of (functions of?) the indexed columns.
         m_nondeterminismDetail = "index scan may provide insufficient ordering";
         return false;
     }
 
     public void setCatalogIndex(Index index)
     {
         m_catalogIndex = index;
     }
 
     public Index getCatalogIndex()
     {
         return m_catalogIndex;
     }
 
     /**
      *
      * @param keyIterate
      */
     public void setKeyIterate(Boolean keyIterate) {
         m_keyIterate = keyIterate;
     }
 
     /**
      *
      * @return Does this scan iterate over values in the index.
      */
     public Boolean getKeyIterate() {
         return m_keyIterate;
     }
 
     /**
      *
      * @return The type of this lookup.
      */
     public IndexLookupType getLookupType() {
         return m_LookupType;
     }
 
     /**
      *
      * @param lookupType
      */
     public void setLookupType(IndexLookupType lookupType) {
         m_LookupType = lookupType;
     }
 
     /**
      * @return the target_index_name
      */
     public String getTargetIndexName() {
         return m_targetIndexName;
     }
 
     /**
      * @param targetIndexName the target_index_name to set
      */
     public void setTargetIndexName(String targetIndexName) {
         m_targetIndexName = targetIndexName;
     }
 
     public void addSearchKeyExpression(AbstractExpression expr)
     {
         if (expr != null)
         {
             // PlanNodes all need private deep copies of expressions
             // so that the resolveColumnIndexes results
             // don't get bashed by other nodes or subsequent planner runs
             try
             {
                 m_searchkeyExpressions.add((AbstractExpression) expr.clone());
             }
             catch (CloneNotSupportedException e)
             {
                 // This shouldn't ever happen
                 e.printStackTrace();
                 throw new RuntimeException(e.getMessage());
             }
         }
     }
 
     /**
      * @return the searchkey_expressions
      */
     // Please don't use me to add search key expressions.  Use
     // addSearchKeyExpression() so that the expression gets cloned
     public List<AbstractExpression> getSearchKeyExpressions() {
         return Collections.unmodifiableList(m_searchkeyExpressions);
     }
 
     public void addEndKeyExpression(AbstractExpression expr)
     {
         if (expr != null)
         {
             // PlanNodes all need private deep copies of expressions
             // so that the resolveColumnIndexes results
             // don't get bashed by other nodes or subsequent planner runs
             try
             {
                 m_endkeyExpressions.add(0,(AbstractExpression) expr.clone());
             }
             catch (CloneNotSupportedException e)
             {
                 // This shouldn't ever happen
                 e.printStackTrace();
                 throw new RuntimeException(e.getMessage());
             }
         }
     }
 
     public List<AbstractExpression> getEndKeyExpressions() {
         return Collections.unmodifiableList(m_endkeyExpressions);
     }
 
     public void setOutputSchema(NodeSchema schema)
     {
         // set output schema according to aggregate plan node's output schema
         m_outputSchema = schema.clone();
     }
 
     public void setParents(AbstractPlanNode parents) {
         // TODO(xin): set parents node
     }
 
     public boolean isEndExpreValid() {
         return m_endExprValid;
     }
 
     public void setEndKeyExpression(AbstractExpression endExpr) {
         // assume there is not post expression when I want to set endKey
         assert(endExpr != null);
         m_endkeyExpressions = new ArrayList<AbstractExpression>();
 
         ArrayList <AbstractExpression> subEndExpr = endExpr.findAllSubexpressionsOfClass(ComparisonExpression.class);
         int cmpSize = subEndExpr.size();
         int ctEqual = 0, ctOther = 0;
         for (AbstractExpression ae: subEndExpr) {
             ExpressionType et = ae.getExpressionType();
             // comparision type checking
             if (et == ExpressionType.COMPARE_EQUAL) {
                 ctEqual++;
             } else if (et == ExpressionType.COMPARE_LESSTHAN) {
                 ctOther++;
                 m_endType = IndexLookupType.LT;
             } else if (et == ExpressionType.COMPARE_LESSTHANOREQUALTO) {
                 ctOther++;
                 m_endType = IndexLookupType.LTE;
             } else {
                 // something wrong, we can not handle other cases
                 m_endExprValid = false;
                 return;
             }
 
             if (ae.getLeft() instanceof TupleValueExpression) {
                 this.addEndKeyExpression(ae.getRight());
             } else {
                 this.addEndKeyExpression(ae.getLeft());
             }
         }
         // Post expression cases are excluded
         // Only one non-equal comparision allowed
         if (ctOther > 1 || ctOther + ctEqual != cmpSize) {
             m_endExprValid = false;
             return;
         }
         // Two cases excluded:
         // (1) "SELECT count(*) from T1 WHERE POINTS = ?"
         // (2) "SELECT count(*) from T2 WHERE USERNAME ='XIN' AND POINTS > ?"
         if (ctEqual == 1 && ctOther == 0) {
             m_endExprValid = false;
             return;
         }
 
         // the order of the endKeyExpr is important
         List<ColumnRef> sortedColumns = CatalogUtil.getSortedCatalogItems(this.getCatalogIndex().getColumns(), "index");
 
 
         m_endExprValid = true;
     }
 
     @Override
     public void generateOutputSchema(Database db){}
 
     @Override
     public void resolveColumnIndexes(){}
 
     @Override
     public boolean computeEstimatesRecursively(PlanStatistics stats, Cluster cluster, Database db, DatabaseEstimates estimates, ScalarValueHints[] paramHints) {
 
         // HOW WE COST INDEXES
         // unique, covering index always wins
         // otherwise, pick the index with the most columns covered otherwise
         // count non-equality scans as -0.5 coverage
         // prefer array to hash to tree, all else being equal
 
         // FYI: Index scores should range between 1 and 48898 (I think)
 
         Table target = db.getTables().getIgnoreCase(m_targetTableName);
         assert(target != null);
         DatabaseEstimates.TableEstimates tableEstimates = estimates.getEstimatesForTable(target.getTypeName());
         stats.incrementStatistic(0, StatsField.TREE_INDEX_LEVELS_TRAVERSED, (long)(Math.log(tableEstimates.maxTuples)));
 
         stats.incrementStatistic(0, StatsField.TUPLES_READ, 1);
         m_estimatedOutputTupleCount = 1;
 
         return true;
     }
 
     @Override
     public void toJSONString(JSONStringer stringer) throws JSONException {
         super.toJSONString(stringer);
         stringer.key(Members.KEY_ITERATE.name()).value(m_keyIterate);
         stringer.key(Members.LOOKUP_TYPE.name()).value(m_LookupType.toString());
         stringer.key(Members.END_TYPE.name()).value(m_endType.toString());
         stringer.key(Members.TARGET_INDEX_NAME.name()).value(m_targetIndexName);
 
 
         stringer.key(Members.ENDKEY_EXPRESSIONS.name());
         if (m_endkeyExpressions == null || m_endkeyExpressions.isEmpty()) {
             stringer.value(null);
         } else {
             stringer.array();
             for (AbstractExpression ae : m_endkeyExpressions) {
                 assert (ae instanceof JSONString);
                 stringer.value(ae);
             }
             stringer.endArray();
         }
 
         stringer.key(Members.SEARCHKEY_EXPRESSIONS.name()).array();
         for (AbstractExpression ae : m_searchkeyExpressions) {
             assert (ae instanceof JSONString);
             stringer.value(ae);
         }
         stringer.endArray();
     }
 
     @Override
     protected String explainPlanForNode(String indent) {
         assert(m_catalogIndex != null);
 
         int indexSize = m_catalogIndex.getColumns().size();
         int keySize = m_searchkeyExpressions.size();
 
         String scanType = "tree-counter";
         if (m_LookupType != IndexLookupType.EQ)
             scanType = "tree-counter";
 
         String cover = "covering";
         if (indexSize > keySize)
             cover = String.format("%d/%d cols", keySize, indexSize);
 
         String usageInfo = String.format("(%s %s)", scanType, cover);
         if (keySize == 0)
             usageInfo = "(for sort order only)";
 
         String retval = "INDEX COUNT of \"" + m_targetTableName + "\"";
         retval += " using \"" + m_targetIndexName + "\"";
         retval += " " + usageInfo;
         return retval;
     }
 }
