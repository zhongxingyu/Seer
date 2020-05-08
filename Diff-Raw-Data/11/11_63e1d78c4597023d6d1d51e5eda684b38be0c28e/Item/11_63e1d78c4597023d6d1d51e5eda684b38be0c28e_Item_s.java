 package com.mmtzj.domain;
 
 import com.google.common.collect.Lists;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: FANLU
  * Date: 13-1-7
  * Time: 下午9:01
  * To change this template use File | Settings | File Templates.
  */
 public class Item {
 
     private int id;
 
     private String name;
 
     private String title;
 
     private String pic;
 
     private String desc;
 
     private int categoryId;
 
     private String tbPath;
 
     private float newPrice;
 
     private float oldPrice;
 
     private List<Eval> evalList = Lists.newArrayList();
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
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
 
     public String getPic() {
         return pic;
     }
 
     public void setPic(String pic) {
         this.pic = pic;
     }
 
     public String getDesc() {
         return desc;
     }
 
     public void setDesc(String desc) {
         this.desc = desc;
     }
 
     public int getCategoryId() {
         return categoryId;
     }
 
     public void setCategoryId(int categoryId) {
         this.categoryId = categoryId;
     }
 
     public String getTbPath() {
         return tbPath;
     }
 
     public void setTbPath(String tbPath) {
         this.tbPath = tbPath;
     }
 
     public float getNewPrice() {
         return newPrice;
     }
 
     public void setNewPrice(float newPrice) {
         this.newPrice = newPrice;
     }
 
     public float getOldPrice() {
         return oldPrice;
     }
 
     public void setOldPrice(float oldPrice) {
         this.oldPrice = oldPrice;
     }
 
     public List<Eval> getEvalList() {
         return evalList;
     }
 
     public void setEvalList(List<Eval> evalList) {
         this.evalList = evalList;
     }
 }
