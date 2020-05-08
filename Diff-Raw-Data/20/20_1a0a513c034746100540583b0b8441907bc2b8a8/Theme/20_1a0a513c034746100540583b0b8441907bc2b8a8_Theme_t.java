 package com.wgcat.cheatsheet.dbobjects;
 import javax.jdo.annotations.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="theme")
 public class Theme {
     @Id
    @Column(name="id")
     @GeneratedValue
     private Integer _id;
     
     @Column(name="title")
     private String _title;
     
     public String getTitle()
     {
         return _title;
     }
     
     public void setTitle(String title)
     {
         _title = title;
     }
     
     public Integer getId()
     {
         return _id;
     }
 }
