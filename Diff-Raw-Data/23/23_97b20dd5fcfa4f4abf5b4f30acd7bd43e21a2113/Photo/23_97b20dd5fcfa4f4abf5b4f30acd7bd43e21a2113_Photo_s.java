 package de.goldstift.photoo.domain;
 
import java.lang.Short;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.entity.RooJpaEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public class Photo {
 
     private String title;
 
     private String description;
 
     @ManyToOne(fetch = FetchType.EAGER)
     private PhotoFile thumbnailFile;
 
     @ManyToOne(fetch = FetchType.EAGER)
     private PhotoFile previewFile;
 
     @ManyToOne(fetch = FetchType.EAGER)
     private PhotoFile originalFile;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private Date importDate;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private Date shotDate;
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private Set<Tag> tags = new HashSet<Tag>();
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private Set<Person> persons = new HashSet<Person>();
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private Set<Event> events = new HashSet<Event>();
 }
