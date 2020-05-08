 package edu.rit.asksg.domain;
 
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
 import org.hibernate.annotations.Type;
 import org.joda.time.LocalDateTime;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.validation.constraints.NotNull;
import java.util.*;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson(deepSerialize = true)
 public class Conversation {
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private Set<Message> messages = new HashSet<Message>();
 
     @NotNull
     @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
     @DateTimeFormat(style = "M-")
     private LocalDateTime created = new LocalDateTime();
 
     @NotNull
     @DateTimeFormat(style = "M-")
     @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
     private LocalDateTime modified = new LocalDateTime();
 
 }
