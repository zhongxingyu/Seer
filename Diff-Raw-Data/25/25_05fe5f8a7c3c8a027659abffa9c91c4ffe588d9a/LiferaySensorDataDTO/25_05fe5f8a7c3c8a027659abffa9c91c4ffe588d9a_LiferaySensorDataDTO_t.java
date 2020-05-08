 package com.rcs.liferaysense.entities.chap.graph.dtos;
 
 import java.io.Serializable;
 
 /**
  *
  * @author pablo
  */
 public class LiferaySensorDataDTO implements Serializable {
     private static final long serialVersionUID = 1L;
         
     private String ip;
     private String page;
     private String previous_page;
     private long pageId;
     private long previous_pageId;
     private String color;
     private long timestamp;
     private String browser = "other";
     private String liferayUserInformation;
     private int pageCounter;
     private long liferayUserId;
     
     public String getIp() {
         return ip;
     }
 
     public void setIp(String ip) {
         this.ip = ip;
     }
 
     public String getPage() {
         return page;
     }
 
     public void setPage(String page) {
         this.page = page;
     }
 
     public String getPrevious_page() {
         return previous_page;
     }
 
     public void setPrevious_page(String previous_page) {
         this.previous_page = previous_page;
     }
 
     public long getPageId() {
         return pageId;
     }
 
     public void setPageId(long pageId) {
         this.pageId = pageId;
     }
 
     public long getPrevious_pageId() {
         return previous_pageId;
     }
 
     public void setPrevious_pageId(long previous_pageId) {
         this.previous_pageId = previous_pageId;
     }
 
     public String getColor() {
         return color;
     }
 
     public void setColor(String color) {
         this.color = color;
     }
 
     public long getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(long timestamp) {
         this.timestamp = timestamp;
     }
 
     
     public String getBrowser() {
         return browser;
     }
 
     public void setBrowser(String userAgent) {
        this.browser = "other";
        if (userAgent != null) {
            if (userAgent.indexOf("Chrome") > -1) {
                this.browser = "chrome";
            } else if (userAgent.indexOf("Firefox") > -1) {
                this.browser = "firefox";
            } else if (userAgent.indexOf("MSIE") > -1) {
                this.browser = "msie";
            } else if (userAgent.indexOf("Mobile") > -1) {
                this.browser = "mobile";
            } else if (userAgent.indexOf("Safari") > -1) {
                this.browser = "safari";
            }
         }
     }
 
     public String getLiferayUserInformation() {
         return liferayUserInformation;
     }
 
     public void setLiferayUserInformation(String liferayUserInformation) {
         this.liferayUserInformation = liferayUserInformation;
     }
 
     public int getPageCounter() {
         return pageCounter;
     }
 
     public void setPageCounter(int pageCounter) {
         this.pageCounter = pageCounter;
     }
 
     public long getLiferayUserId() {
         return liferayUserId;
     }
 
     public void setLiferayUserId(long liferayUserId) {
         this.liferayUserId = liferayUserId;
     }
     
 }
