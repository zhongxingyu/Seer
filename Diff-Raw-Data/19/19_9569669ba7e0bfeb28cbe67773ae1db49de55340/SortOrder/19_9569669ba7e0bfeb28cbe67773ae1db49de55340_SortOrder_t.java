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
    PRIORITY_DESC("completed, priority desc, dueDate, orderIndex"),
    DUE_DATE_DESC("completed, dueDate desc, priority, orderIndex"),
    DATE_CREATED_DESC("completed, dateCreated desc, priority, orderIndex"),
    LAST_UPDATED_DESC("completed, lastUpdated desc, priority, orderIndex"),
    PRIORITY_ASC("completed, priority asc, dueDate, orderIndex"),
    DUE_DATE_ASC("completed, dueDate asc, priority, orderIndex"),
    DATE_CREATED_ASC("completed, dateCreated asc, priority, orderIndex"),
    LAST_UPDATED_ASC("completed, lastUpdated asc, priority, orderIndex");
 
     private String sqlSort;
     
     private SortOrder(String sqlSort) {
         this.sqlSort = sqlSort;
     }
     
     public String getSqlSort() {
         return sqlSort;
     }
 }
