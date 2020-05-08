 package org.motechproject.carereporting.domain;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import java.util.Set;
 
 @Entity
 @Table(name = "dw_query")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "dw_query_id"))
 })
 @Inheritance(strategy = InheritanceType.JOINED)
 public class DwQueryEntity extends AbstractEntity {
 
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinTable(name = "dw_query_select_column", joinColumns = { @JoinColumn(name = "dw_query_id") },
             inverseJoinColumns = { @JoinColumn(name = "select_column_id") })
     private Set<SelectColumnEntity> selectColumns;
 
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "combination_id", referencedColumnName = "combination_id")
     private CombinationEntity combination;
 
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "grouped_by_id", referencedColumnName = "grouped_by_id")
     private GroupedByEntity groupedBy;
 
     @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinColumn(name = "where_group_id", referencedColumnName = "where_group_id")
     private WhereGroupEntity whereGroup;
 
     @Column(name = "has_period_condition")
     private boolean hasPeriodCondition;
 
     public Set<SelectColumnEntity> getSelectColumns() {
         return selectColumns;
     }
 
     public void setSelectColumns(Set<SelectColumnEntity> selectColumns) {
         this.selectColumns = selectColumns;
     }
 
     public CombinationEntity getCombination() {
         return combination;
     }
 
     public void setCombination(CombinationEntity combination) {
         this.combination = combination;
     }
 
     public GroupedByEntity getGroupedBy() {
         return groupedBy;
     }
 
     public void setGroupedBy(GroupedByEntity groupedBy) {
         this.groupedBy = groupedBy;
     }
 
     public WhereGroupEntity getWhereGroup() {
         return whereGroup;
     }
 
     public void setWhereGroup(WhereGroupEntity whereGroup) {
         this.whereGroup = whereGroup;
     }
 
     public boolean getHasPeriodCondition() {
         return hasPeriodCondition;
     }
 
     public void setHasPeriodCondition(boolean hasPeriodCondition) {
         this.hasPeriodCondition = hasPeriodCondition;
     }
 }
