 package com.giftoftheembalmer.gotefarm.server.dao;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.users.User;
 
 import javax.jdo.annotations.Element;
 import javax.jdo.annotations.Extension;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.Order;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class Account {
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
     @Persistent
     private User user;
 
     @Persistent
    private Set<Key> guilds;

    @Persistent
     private Key activeGuild;
 
     @Persistent
     private Date lastUpdate;
 
     // FIXME: Using List here due to AppEngine bug, switch to Set once
     // it is fixed.
     // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
     @Persistent(mappedBy = "account")
     @Element(dependent = "true")
     @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="main asc"))
     private List<ChrGroup> chrGroups = new ArrayList<ChrGroup>();
 
     public Account(User user) {
         this.user = user;
         lastUpdate = new Date();
     }
 
     public Key getActiveGuild() {
         return activeGuild;
     }
 
     public List<ChrGroup> getChrGroups() {
         return chrGroups;
     }
 
     public Set<Key> getGuilds() {
        return guilds;
     }
 
     public Key getKey() {
         return key;
     }
 
     public Date getLastUpdate() {
         return lastUpdate;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setActiveGuild(Key activeGuild) {
         this.activeGuild = activeGuild;
     }
 
     public void setChrGroups(List<ChrGroup> chrGroups) {
         this.chrGroups = chrGroups;
     }
 
    public void setGuilds(Set<Key> guilds) {
        this.guilds = guilds;
    }

     public void setLastUpdate(Date lastUpdate) {
         this.lastUpdate = lastUpdate;
     }
 }
