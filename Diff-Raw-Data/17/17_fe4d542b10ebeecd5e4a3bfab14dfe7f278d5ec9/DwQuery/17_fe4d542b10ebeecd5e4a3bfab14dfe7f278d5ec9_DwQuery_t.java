 package org.dwQueryBuilder.data.queries;
 
 import org.dwQueryBuilder.data.DwQueryCombination;
 import org.dwQueryBuilder.data.GroupBy;
 import org.dwQueryBuilder.data.SelectColumn;
 import org.dwQueryBuilder.data.conditions.where.WhereConditionGroup;
 
 import java.util.Set;
 
 public abstract class DwQuery {
    private Set<SelectColumn> selectColumns;
    private GroupBy groupBy;
    private DwQueryCombination combineWith;
    private WhereConditionGroup whereConditionGroup;
 
     public DwQuery() {
 
     }
 
     public DwQuery(Set<SelectColumn> selectColumns) {
         this.selectColumns = selectColumns;
     }
 
     public DwQuery(Set<SelectColumn> selectColumns, GroupBy groupBy) {
         this.selectColumns = selectColumns;
         this.groupBy = groupBy;
     }
 
     public DwQuery(Set<SelectColumn> selectColumns, DwQueryCombination combineWith) {
         this.selectColumns = selectColumns;
         this.combineWith = combineWith;
     }
 
     public DwQuery(Set<SelectColumn> selectColumns, GroupBy groupBy, WhereConditionGroup whereConditionGroup) {
         this.selectColumns = selectColumns;
         this.groupBy = groupBy;
         this.whereConditionGroup = whereConditionGroup;
     }
 
     public DwQuery(Set<SelectColumn> selectColumns, GroupBy groupBy, DwQueryCombination combineWith) {
         this.selectColumns = selectColumns;
         this.groupBy = groupBy;
         this.combineWith = combineWith;
     }
 
     public DwQuery(Set<SelectColumn> selectColumns, GroupBy groupBy, WhereConditionGroup whereConditionGroup,
                    DwQueryCombination combineWith) {
         this.selectColumns = selectColumns;
         this.groupBy = groupBy;
         this.whereConditionGroup = whereConditionGroup;
         this.combineWith = combineWith;
     }
 
     public Set<SelectColumn> getSelectColumns() {
         return selectColumns;
     }
 
     public void setSelectColumns(Set<SelectColumn> selectColumns) {
         this.selectColumns = selectColumns;
     }
 
     public GroupBy getGroupBy() {
         return groupBy;
     }
 
     public void setGroupBy(GroupBy groupBy) {
         this.groupBy = groupBy;
     }
 
     public DwQueryCombination getCombineWith() {
         return combineWith;
     }
 
     public void setCombineWith(DwQueryCombination combineWith) {
         this.combineWith = combineWith;
     }
 
     public WhereConditionGroup getWhereConditionGroup() {
         return whereConditionGroup;
     }
 
     public void setWhereConditionGroup(WhereConditionGroup whereConditionGroup) {
         this.whereConditionGroup = whereConditionGroup;
     }
 }
