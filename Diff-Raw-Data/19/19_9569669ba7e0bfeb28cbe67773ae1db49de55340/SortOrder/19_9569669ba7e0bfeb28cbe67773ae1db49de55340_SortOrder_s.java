 package models;
 
 /**
  * Created by IntelliJ IDEA.
  * User: sheldon
  * Date: 4/2/12
  * Time: 8:27 AM
  * To change this template use File | Settings | File Templates.
  */
 public enum SortOrder {
     DEFAULT("completed"),
    PRIORITY("completed, priority, dueDate, orderIndex"),
    DUE_DATE("completed, dueDate, priority, orderIndex"),
    DATE_CREATED("completed, dateCreated, priority, orderIndex"),
    LAST_UPDATED("completed, lastUpdated, priority, orderIndex");
 
     private String sqlSort;
     
     private SortOrder(String sqlSort) {
         this.sqlSort = sqlSort;
     }
     
     public String getSqlSort() {
         return sqlSort;
     }
 }
