
 package com.cisco.diddo.entity;
 
 import javax.persistence.Column;
 import javax.validation.constraints.NotNull;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.layers.repository.mongo.RooMongoEntity;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooMongoEntity
 @RooJson
 public class Team {
 
     @NotNull
     @Column(unique = true)
     private String name;
 
     @NotNull
     private String email;
 }
