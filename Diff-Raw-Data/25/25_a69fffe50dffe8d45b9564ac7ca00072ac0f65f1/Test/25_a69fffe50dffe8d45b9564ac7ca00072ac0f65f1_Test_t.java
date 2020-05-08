 package garin.artemiy.sqlitesimple.example.model;
 
 import garin.artemiy.sqlitesimple.library.annotations.Column;
 import garin.artemiy.sqlitesimple.library.annotations.Table;
 import garin.artemiy.sqlitesimple.library.util.ColumnType;
 
 /**
  * Author: Artemiy Garin
  * Date: 03.04.13
  */
 @Table
 public class Test {
 
    @SuppressWarnings("unused")
     @Column(type = ColumnType.INTEGER, isPrimaryKey = true, isAutoincrement = true)
     private String _id;
 
     @Column(type = ColumnType.TEXT)
     @SuppressWarnings("unused")
     private String title;
 
     @SuppressWarnings("unused")
     public String getTitle() {
         return title;
     }
 
 }
