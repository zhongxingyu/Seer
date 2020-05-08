 package com.um.canario.models;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.EntityManager;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 public class Tweeter {
 
     @NotNull
     @Column(unique = true)
     private String username;
 
     @NotNull
     @Column(unique = true)
     private String email;
 
     @NotNull
     private String name;
 
     @NotNull
     private String lname;
 
     @Column(name = "photo_url")
     private String photoUrl;
 
     @NotNull
     @Column(name = "allow_geolocation")
     private Boolean allowGeolocation;
 
     @NotNull
     private String password;
 
     @NotNull
     @Column(name = "birth_date")
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date birthDate;
 
     @NotNull
     private Boolean enabled;
 
     @NotNull
     private String authority;
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tweeter")
     private Set<Tweet> tweets = new HashSet<Tweet>();
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "followed")
     private Set<Following> followed = new HashSet<Following>();
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "follower")
     private Set<Following> following = new HashSet<Following>();
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "tweeter")
     private Set<Mention> mentions = new HashSet<Mention>();
 
     public static com.um.canario.models.Tweeter findTweeterByUsername(String username) {
         List<Tweeter> list = entityManager().createQuery("SELECT o FROM Tweeter o WHERE username=?", Tweeter.class).setParameter(1, username).getResultList();
         if (list.isEmpty()) {
             return null;
         }
         return list.get(0);
     }
 }
