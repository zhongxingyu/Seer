 package com.mgl.restdemo.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.FluentSetter;
 import lombok.NonNull;
 
 @Entity
 @Data @EqualsAndHashCode(of="id") @FluentSetter
 @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
 public class Location implements Serializable {
 
     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     @ManyToOne(optional=false)
     @XmlTransient
     private @NonNull Vehicle vehicle = null;
 
     @Temporal(TemporalType.TIMESTAMP)
     private @NonNull @NotNull Date ts = new Date();;
 
     private @NotNull double latitude;
    private @NotNull double longiture;
 
 }
