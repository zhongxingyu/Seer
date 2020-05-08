 package com.cqlybest.common.bean;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * 马尔代夫海岛
  */
 public class MaldivesSeaIsland {
 
   private String id;
   private String zhName; // 中文名称
   private String enName; // 英文名称
   private String level; // 岛屿级别
   private String way; // 上岛方式
   private String area; // 岛屿大小
   private String snorkeling; // 浮潜等级
   private String price; // 参考价格
 
  private List<Image> pictures;// 图片
   private List<MaldivesRoom> rooms;// 房型
 
   private Date createdTime;// 创建时间
   private Date lastUpdated;// 最后更新时间
 
   public String getId() {
     return id;
   }
 
   public void setId(String id) {
     this.id = id;
   }
 
   public String getZhName() {
     return zhName;
   }
 
   public void setZhName(String zhName) {
     this.zhName = zhName;
   }
 
   public String getEnName() {
     return enName;
   }
 
   public void setEnName(String enName) {
     this.enName = enName;
   }
 
   public String getLevel() {
     return level;
   }
 
   public void setLevel(String level) {
     this.level = level;
   }
 
   public String getWay() {
     return way;
   }
 
   public void setWay(String way) {
     this.way = way;
   }
 
   public String getArea() {
     return area;
   }
 
   public void setArea(String area) {
     this.area = area;
   }
 
   public String getSnorkeling() {
     return snorkeling;
   }
 
   public void setSnorkeling(String snorkeling) {
     this.snorkeling = snorkeling;
   }
 
   public String getPrice() {
     return price;
   }
 
   public void setPrice(String price) {
     this.price = price;
   }
 
  public List<Image> getPictures() {
    return pictures;
   }
 
  public void setPictures(List<Image> pictures) {
    this.pictures = pictures;
   }
 
   public List<MaldivesRoom> getRooms() {
     return rooms;
   }
 
   public void setRooms(List<MaldivesRoom> rooms) {
     this.rooms = rooms;
   }
 
   public Date getCreatedTime() {
     return createdTime;
   }
 
   public void setCreatedTime(Date createdTime) {
     this.createdTime = createdTime;
   }
 
   public Date getLastUpdated() {
     return lastUpdated;
   }
 
   public void setLastUpdated(Date lastUpdated) {
     this.lastUpdated = lastUpdated;
   }
 
 }
