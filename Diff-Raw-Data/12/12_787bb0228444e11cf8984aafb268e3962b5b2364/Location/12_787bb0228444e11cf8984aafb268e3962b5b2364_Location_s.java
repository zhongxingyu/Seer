 package com.jclarity.had_one_dismissal.domain;
 
 import javax.persistence.ManyToOne;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 public class Location {
 
     @NotNull
     @Size(min = 2)
     private String name;
 
     @ManyToOne
    private com.jclarity.had_one_dismissal.domain.Location memberOf;
 }
