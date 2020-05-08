 package com.mmtzj.domain;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wangxin
  * Date: 13-1-6
  * Time: 下午10:56
  * To change this template use File | Settings | File Templates.
  */
 public class Category {
 
     private Integer id;
 
     private String name;
 
     private String title;
 
     private Integer rank;
 
     private Integer status;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Integer getRank() {
        return rank;
     }
 
     public void setRank(Integer rank) {
         this.rank = rank;
     }
 
     public Integer getStatus() {
        return status;
     }
 
     public void setStatus(Integer status) {
         this.status = status;
     }
 }
