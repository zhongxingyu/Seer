 package com.mpower.domain.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import com.mpower.domain.entity.listener.EmptyStringNullifyerListener;
 
 @Entity
 @EntityListeners(value = { EmptyStringNullifyerListener.class })
 @Table(name = "PHONE")
 public class Phone implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column(name = "PHONE_ID")
     private Long id;
 
    @Column(name = "NUMBER")
     private String number;
 
     @Column(name = "PHONE_TYPE", nullable = false)
     private String phoneType;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getNumber() {
         return number;
     }
 
     public void setNumber(String number) {
         this.number = number;
     }
 
     public String getPhoneType() {
         return phoneType;
     }
 
     public void setPhoneType(String phoneType) {
         this.phoneType = phoneType;
     }
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 }
