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
 
     private Integer id;
 
     private String name;
 
     private String title;
 
     private String pic;
 
     private String desc;
 
     private Integer categoryId;
 
     private String tbPath;
 
     private Float newPrice;
 
     private Float oldPrice;
 
     private Integer status;
 
     private Integer wantToBuy;
 
     private Integer likeCount;
 
     private Integer collectCount;
 
     private List<Eval> evalList = Lists.newArrayList();
 
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
 
     public Integer getCategoryId() {
         return categoryId;
     }
 
     public void setCategoryId(Integer categoryId) {
         this.categoryId = categoryId;
     }
 
     public String getTbPath() {
         return tbPath;
     }
 
     public void setTbPath(String tbPath) {
         this.tbPath = tbPath;
     }
 
     public Float getNewPrice() {
        return newPrice == null ? 0f : newPrice;
     }
 
     public void setNewPrice(Float newPrice) {
         this.newPrice = newPrice;
     }
 
     public Float getOldPrice() {
        return oldPrice == null ? 0f : oldPrice;
     }
 
     public void setOldPrice(Float oldPrice) {
         this.oldPrice = oldPrice;
     }
 
     public List<Eval> getEvalList() {
         return evalList;
     }
 
     public void setEvalList(List<Eval> evalList) {
         this.evalList = evalList;
     }
 
     public Integer getStatus() {
         return status;
     }
 
     public void setStatus(Integer status) {
         this.status = status;
     }
 
     public Integer getWantToBuy() {
        return wantToBuy == null ? 0 : wantToBuy;
     }
 
     public void setWantToBuy(Integer wantToBuy) {
         this.wantToBuy = wantToBuy;
     }
 
     public Integer getLikeCount() {
        return likeCount == null ? 0 : likeCount;
     }
 
     public void setLikeCount(Integer likeCount) {
         this.likeCount = likeCount;
     }
 
     public Integer getCollectCount() {
        return collectCount == null ? 0 : collectCount;
     }
 
     public void setCollectCount(Integer collectCount) {
         this.collectCount = collectCount;
     }
 }
