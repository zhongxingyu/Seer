 package com.cl.cx.platform.dto;
 
 import com.fasterxml.jackson.annotation.JsonTypeInfo;
 import lombok.Data;
 
 /**
  * User: yanjianzou
  * Date: 12-7-30
  * Time: 下午3:24
  * FileName:Action
  */
 @Data
 @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property= "@type")
 public class Actions {
     private Action zoneInURL;
     private Action zoneOutURL;
     private Action useURL;
     private Action collectURL;
     private Action removeURL;
     private Action disableURL;
     private Action editURL;
 
     //TODO 接口下发列表 需要新的DTO 对象
     private Action recommendURL;
     private Action hotURL;
     private Action categoryURL;
    private Action mGraphicsURL;
     private Action statusURL;
     private Action holidaysURL;
     private Action customMGraphicsURL;
     private Action versionURL;
     private Action suggestionURL;
     private Action callURL;
     private Action collectionsURL;
     private Action registerURL;
     private Action purchaseURL;
     private Action inviteFriendsURL;
 }
