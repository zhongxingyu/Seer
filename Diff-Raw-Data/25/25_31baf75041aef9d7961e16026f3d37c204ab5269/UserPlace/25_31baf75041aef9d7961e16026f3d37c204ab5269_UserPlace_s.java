 package com.rockontrol.yaogan.model;
 
 import javax.persistence.Entity;
 
 @Entity(name = "user_place")
 public class UserPlace {
    private Long id;
    private Long userId;
    private Long placeId;
 
    public Long getId() {
       return id;
    }
 
    public void setId(Long id) {
       this.id = id;
    }
 
    public Long getUserId() {
       return userId;
    }
 
    public void setUserId(Long userId) {
       this.userId = userId;
    }
 
    public Long getPlaceId() {
       return placeId;
    }
 
    public void setPlaceId(Long placeId) {
       this.placeId = placeId;
    }
 
 }
