 /*
  * gw2live - GuildWars 2 Dynamic Map
  * 
  * Website: http://gw2map.com
  *
  * Copyright 2013   zyclonite    networx
  *                  http://zyclonite.net
  * Developer: Lukas Prettenthaler
  */
 package net.zyclonite.gw2live.model;
 
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import java.util.Date;
 import org.mongojack.ObjectId;
 
 /**
  *
  * @author zyclonite
  */
 @JsonIgnoreProperties({"_id"})
 public class GuildDetails {
 
     private final static int CACHETIME_HOURS = 24;
     @ObjectId
     private String _guild_id;
     private String _guild_name;
     private String _tag;
     private GuildEmblem _emblem;
     private Date _timestamp;
 
     public String getGuild_id() {
         return _guild_id;
     }
 
     public void setGuild_id(final String id) {
         this._guild_id = id;
     }
 
     public String getGuild_name() {
         return _guild_name;
     }
 
     public void setGuild_name(final String name) {
         this._guild_name = name;
     }
 
     public String getTag() {
         return _tag;
     }
 
     public void setTag(final String tag) {
         this._tag = tag;
     }
 
     public GuildEmblem getEmblem() {
         return _emblem;
     }
 
     public void setEmblem(final GuildEmblem emblem) {
         this._emblem = emblem;
     }
     public Date getTimestamp() {
         return _timestamp;
     }
 
     public void setTimestamp(Date _timestamp) {
         this._timestamp = _timestamp;
     }
     
     public Boolean needsRenewal() {
         final long timediff = (new Date()).getTime()-this._timestamp.getTime();
         if((CACHETIME_HOURS*60*60*1000) < timediff){
             return true;
         }
         return false;
     }
 }
