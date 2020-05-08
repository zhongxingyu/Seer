 package garin.artemiy.sqlitesimple.example.model;
 
 import garin.artemiy.sqlitesimple.library.annotations.Column;
 import garin.artemiy.sqlitesimple.library.util.ColumnType;
 
 /**
  * author: Artemiy Garin
  * date: 13.12.12
  */
 public class Record {
 
     public transient static final String COLUMN_RECORD_TEXT = "recordText";
     public transient static final String COLUMN_ID = "_id";
 
     @Column(name = COLUMN_ID, type = ColumnType.INTEGER, isPrimaryKey = true, isAutoincrement = true)
     private int id;
 
    @Column
     private String recordText;
 
     public String getRecordText() {
         return recordText;
     }
 
     public void setRecordText(String recordText) {
         this.recordText = recordText;
     }
 
     public int getId() {
         return id;
     }
 
 }
