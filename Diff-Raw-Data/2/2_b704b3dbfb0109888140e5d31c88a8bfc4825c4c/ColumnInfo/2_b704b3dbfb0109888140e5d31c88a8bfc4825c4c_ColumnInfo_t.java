 package com.nearinfinity.hbaseclient;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 7/25/12
  * Time: 2:06 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ColumnInfo {
     private long id;
     private String name;
     private ColumnMetadata metadata;
     public ColumnInfo(long id, String name, ColumnMetadata metadata) {
         this.id = id;
         this.name = name;
        this.metadata = metadata;
     }
 
     public long getId() {
         return this.id;
     }
 
     public String getName() {
         return this.name;
     }
 
     public ColumnMetadata getMetadata() {
         return this.metadata;
     }
 }
