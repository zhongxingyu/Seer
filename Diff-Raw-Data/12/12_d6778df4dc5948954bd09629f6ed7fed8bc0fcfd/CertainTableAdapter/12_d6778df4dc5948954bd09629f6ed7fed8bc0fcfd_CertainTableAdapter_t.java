 package com.worldofzaar.adapter;
 
 import com.worldofzaar.entity.CertainTable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Дмитрий
  * Date: 01.12.13
  * Time: 19:33
  * To change this template use File | Settings | File Templates.
  */
 public class CertainTableAdapter {
     private Integer seatPosition;
     private String blazonPath;
    private String clothPath;
     private Integer requestId;
     private Integer userId;
     private String userName;
     private Integer level;
 
     public CertainTableAdapter(CertainTable table) {
         if(table == null)
             return;
         seatPosition = table.getSeatPosition();
         blazonPath = table.getUser().getGameProfile().getBlazon().getBlazonPath();
        clothPath = table.getUser().getGameProfile().getBlazon().getCloth().getClothPath();
         requestId = table.getRequestId();
         userId = table.getUser().getUserId();
         userName = table.getUser().getUserName();
         level = table.getUser().getGameProfile().getLevel();
     }
 
     public static List<CertainTableAdapter> getTables(List<CertainTable> tables) {
         List<CertainTableAdapter> newTables = new ArrayList<CertainTableAdapter>();
         for (CertainTable table : tables) {
             newTables.add(new CertainTableAdapter(table));
         }
         return newTables;
     }
 
    public String getClothPath() {
        return clothPath;
    }

    public void setClothPath(String clothPath) {
        this.clothPath = clothPath;
    }

     public Integer getSeatPosition() {
         return seatPosition;
     }
 
     public void setSeatPosition(Integer seatPosition) {
         this.seatPosition = seatPosition;
     }
 
     public String getBlazonPath() {
         return blazonPath;
     }
 
     public void setBlazonPath(String blazonPath) {
         this.blazonPath = blazonPath;
     }
 
     public Integer getRequestId() {
         return requestId;
     }
 
     public void setRequestId(Integer requestId) {
         this.requestId = requestId;
     }
 
     public Integer getUserId() {
         return userId;
     }
 
     public void setUserId(Integer userId) {
         this.userId = userId;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     public Integer getLevel() {
         return level;
     }
 
     public void setLevel(Integer level) {
         this.level = level;
     }
 }
