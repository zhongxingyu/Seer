 package com.rockontrol.yaogan.model;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.Id;
 
 @Entity(name = "user_place")
 public class UserPlace {
    private Long id;
    private Long userId;
    private Long placeId;
 
   @Id
    public Long getId() {
       return id;
    }
 
    public void setId(Long id) {
       this.id = id;
    }
 
   @Column
    public Long getUserId() {
       return userId;
    }
 
    public void setUserId(Long userId) {
       this.userId = userId;
    }
 
   @Column
    public Long getPlaceId() {
       return placeId;
    }
 
    public void setPlaceId(Long placeId) {
       this.placeId = placeId;
    }
 
 }
