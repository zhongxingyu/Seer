 // @formatter:off
 /*
  * Tweet.java - JSON data model for tweets
  * Copyright (C) 2012 Matteo Panella <morpheus@level28.org>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 // @formatter:on
 
 package org.level28.android.moca.model;
 
 import java.util.Date;
 
 import android.provider.BaseColumns;
 
 import com.google.android.maps.GeoPoint;
 
 /**
  * Data model for tweets.
  * 
  * @author Matteo Panella
  */
 public final class Tweet {
     /**
      * Contract for cursors.
      */
     public static final class Contract {
         public static final String ID = BaseColumns._ID;
         public static final String CREATED_AT = "createdAt";
         public static final String FROM_USER = "fromUser";
         public static final String FROM_USER_ID = "fromUserId";
         public static final String FROM_USER_NAME = "fromUserName";
         public static final String LOCATION = "location";
         public static final String LATITUDE = "lat";
         public static final String LONGITUDE = "lon";
         public static final String PROFILE_IMAGE_URL = "profileImageUrl";
         public static final String CONTENTS = "contents";
 
         private Contract() {
             // No-op
         }
     }
 
     public static class Coordinates {
         public final float lat;
         public final float lon;
 
         public Coordinates(final float lat, final float lon) {
             this.lat = lat;
             this.lon = lon;
         }
 
         public GeoPoint getPoint() {
            final int microlat = (int) Math.floor(lat * 1e6f);
            final int microlon = (int) Math.floor(lon * 1e6f);
             return new GeoPoint(microlat, microlon);
         }
     }
 
     private Date createdAt = null;
     private String fromUser = null;
     private long fromUserId;
     private String fromUserName = null;
     private String location = null;
     private Coordinates coordinates = null;
     private long id;
     private String profileImageUrl = null;
     private String text = null;
 
     public Tweet() {
         // No-args constructor
     }
 
     public Date getCreatedAt() {
         return createdAt;
     }
 
     public void setCreatedAt(Date createdAt) {
         this.createdAt = createdAt;
     }
 
     public String getFromUser() {
         return fromUser;
     }
 
     public void setFromUser(String fromUser) {
         this.fromUser = fromUser;
     }
 
     public long getFromUserId() {
         return fromUserId;
     }
 
     public void setFromUserId(long fromUserId) {
         this.fromUserId = fromUserId;
     }
 
     public String getFromUserName() {
         return fromUserName;
     }
 
     public void setFromUserName(String fromUserName) {
         this.fromUserName = fromUserName;
     }
 
     public String getLocation() {
         return location;
     }
 
     public void setLocation(String location) {
         this.location = location;
     }
 
     public Coordinates getCoordinates() {
         return coordinates;
     }
 
     public void setCoordinates(Coordinates coordinates) {
         this.coordinates = coordinates;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getProfileImageUrl() {
         return profileImageUrl;
     }
 
     public void setProfileImageUrl(String profileImageUrl) {
         this.profileImageUrl = profileImageUrl;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 }
