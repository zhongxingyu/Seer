 package garin.artemiy.sqlitesimple.example.model;
 
 import garin.artemiy.sqlitesimple.library.annotations.Column;
 import garin.artemiy.sqlitesimple.library.annotations.Table;
 import garin.artemiy.sqlitesimple.library.util.ColumnType;
 
 /**
  * Author: Artemiy Garin
  * Date: 04.04.13
  */
 @Table(name = "Test_Second")
 public class TestSecond {
 
     @Column(type = ColumnType.INTEGER, isPrimaryKey = true, isAutoincrement = true)
     private String _id;
 
     @Column(type = ColumnType.TEXT, name = "Name")
     @SuppressWarnings("unused")
     private String name;
 
     @SuppressWarnings("unused")
     public String getName() {
         return name;
     }
 
 }
