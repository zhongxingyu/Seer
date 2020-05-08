 /**
  *   Copyright (C) 2010, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel.model;
 
 import com.google.android.maps.GeoPoint;
 
 public class Rack {
     private Integer mId;
     private String mDescription;
     private GeoPoint mLocation;
     private Integer mViewCount;
     private Boolean mStarred;
     private Boolean mOnline;
     private Integer mEmptyLocks;
     private Integer nReadyBikes;
     
     public Rack(int id, String description, Integer latitude, 
             Integer longitude, 
             Integer viewCount, Boolean starred,
             Boolean online, Integer emptyLocks, 
             Integer readyBikes) {
         
         this.mId = id;
         this.mOnline = online;
         this.mDescription = description;
         if (latitude != null && longitude != null) {
             this.mLocation = new GeoPoint(latitude.intValue(), 
                                             longitude.intValue());
         }
         
         this.mViewCount = viewCount != null ? viewCount : 0;
         this.mStarred    = starred != null ? starred : false;
         this.mEmptyLocks = emptyLocks;
         this.nReadyBikes = readyBikes;
     }
     
     public Rack(int id, String description, Integer latitude, Integer longitude, Integer viewCount, Boolean starred) {
         this(id, description, latitude, longitude, viewCount, starred, null, null, null);
     }
     
     public Rack(int id, String description, Integer latitude, Integer longitude, Boolean online, Integer emptyLocks, Integer readyBikes) {
         this(id, description, latitude, longitude, null, null, online, emptyLocks, readyBikes);
     }
    
    public Rack() {
    }
    
     public int getId() {
         return mId;
     }
 
     public boolean isOnline() {
         return (mOnline == null) ? false : mOnline;
     }
     
     public void setOnline(boolean isOnline) {
         mOnline = isOnline;
     }
 
     public boolean hasInfoOnEmptyLocks() {
         return (mEmptyLocks == null) ? false : true;
     }
     
     public int getNumberOfEmptyLocks() {
         return mEmptyLocks;
     }
     
     public boolean hasEmptyLocks() {
         return getNumberOfEmptyLocks() >= 1;
     }
 
     
     public void setNumberOfEmptyLocks(int emptyLocks) {
         this.mEmptyLocks = emptyLocks;
     }
     
     public boolean hasInfoOnReadyBikes() {
         return (nReadyBikes == null) ? false : true;
     }
 
     public int getNumberOfReadyBikes() {
         return nReadyBikes;
     }
     
     public boolean hasReadyBikes() {
         return getNumberOfReadyBikes() >= 1;
     }
     
     public void setNumberOfReadyBikes(int readyBikes) {
         this.nReadyBikes = readyBikes;
     }
     
     public boolean hasDescription() {
         return (mDescription == null) ? false : true;
     }
 
     public String getDescription() {
         return mDescription;
     }
     
     public void setDescription(String description) {
         this.mDescription = description; 
     }
 
     public boolean hasLocationInfo() {
         return (mLocation == null) ? false : true;
     }
     
     public GeoPoint getLocation() {
         return mLocation;
     }
     
     public void setLocation(GeoPoint location) {
         this.mLocation = location;
     }
     
     public void setLocation(int latitude, int longitude) {
         this.mLocation = new GeoPoint(latitude, longitude);
     }
     
     public boolean hasBikeAndLockInfo() {
         return hasInfoOnReadyBikes() && hasInfoOnEmptyLocks();
     }
     
     public void setViewCount(Integer viewCount) {
         this.mViewCount = viewCount;
     }
     
     public Integer incrementViewCount() {
         this.mViewCount++;
         return mViewCount;
     }
 
     public Integer getViewCount() {
         return mViewCount;
     }
 
     public void setStarred(boolean isStarred) {
         this.mStarred = isStarred;
     }
 
     public Boolean isStarred() {
         return mStarred;
     }
     
     public void toggleStarred() {
         mStarred = !mStarred;
     }
 
     public String toString() {
         return getId()+": "+getDescription();
     }
     
 }
