 package org.dwQueryBuilder.data.conditions.where;
 
 import org.dwQueryBuilder.data.conditions.BaseCondition;
 
 public abstract class WhereCondition extends BaseCondition {
    protected String table1Name;
    protected String field1Name;
 
     protected WhereCondition() {
         super();
     }
 
     protected WhereCondition(String table1Name, String field1Name) {
         this.table1Name = table1Name;
         this.field1Name = field1Name;
     }
 
     public String getTable1Name() {
         return table1Name;
     }
 
     public void setTable1Name(String table1Name) {
         this.table1Name = table1Name;
     }
 
     public String getField1Name() {
         return field1Name;
     }
 
     public void setField1Name(String field1Name) {
         this.field1Name = field1Name;
     }
 }
