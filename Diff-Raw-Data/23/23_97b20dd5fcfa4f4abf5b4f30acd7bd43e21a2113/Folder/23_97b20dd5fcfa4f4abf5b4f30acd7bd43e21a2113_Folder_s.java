 package de.goldstift.photoo.domain;
 
 import java.util.Date;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.entity.RooJpaEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 public class Folder {
 
     private String title;
 
     @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private Date importDate;
 
     @NotNull
     @Size(min = 1)
     private String systemPath;
 
     @NotNull
     @Size(min = 1)
     private String urlPath;
 
     private Boolean writable;
 }
