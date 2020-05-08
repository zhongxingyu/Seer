 package garin.artemiy.sqlitesimple.example.model;
 
 import garin.artemiy.sqlitesimple.library.annotations.Column;
 import garin.artemiy.sqlitesimple.library.annotations.Table;
 import garin.artemiy.sqlitesimple.library.util.ColumnType;
 
 /**
  * author: Artemiy Garin
  * date: 13.12.12
  */
 @Table
 public class Record {
 
    @SuppressWarnings("unused")
     @Column(type = ColumnType.INTEGER, isPrimaryKey = true, isAutoincrement = true)
     private String _id;
 
     @Column(type = ColumnType.NUMERIC)
     private boolean isPublished;
 
     @Column(type = ColumnType.INTEGER)
     private Long dateOfPublication;
 
     public Long getDateOfPublication() {
         return dateOfPublication;
     }
 
     public void setDateOfPublication(Long dateOfPublication) {
         this.dateOfPublication = dateOfPublication;
     }
 
     @SuppressWarnings("unused")
     public boolean isPublished() {
         return isPublished;
     }
 
     public void setPublished(boolean published) {
         isPublished = published;
     }
 }
