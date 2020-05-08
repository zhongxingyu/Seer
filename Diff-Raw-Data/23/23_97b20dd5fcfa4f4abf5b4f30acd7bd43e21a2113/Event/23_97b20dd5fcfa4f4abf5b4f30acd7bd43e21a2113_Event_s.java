 package de.goldstift.photoo.domain;
 
 import java.util.Date;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.entity.RooJpaEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public class Event {
 
     @NotNull
     @Size(min = 2)
     private String name;
 
     @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "MS")
     private Date fromDate;
 
     @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "MM")
     private Date toDate;
 }
