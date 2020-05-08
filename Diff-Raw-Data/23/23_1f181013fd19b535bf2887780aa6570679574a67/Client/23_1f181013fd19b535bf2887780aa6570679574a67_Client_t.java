 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 public class Client extends Model {
   @Id
   public Long id;
 
   @Constraints.Required
   public String displayName;
 
   @Formats.DateTime(pattern="dd/MM/yyyy hh:mm:ss")
   public Date dateAdded = new Date();
 
   public int port;
   public String ip;
   public String uuid;
   public Boolean statusOk = false;
 
   @ManyToOne
   public ClientGroup group;
 
   @Formats.DateTime(pattern="dd/MM/yyyy hh:mm:ss")
   public Date lastCheckedIn = new Date();
 
   public static Finder<Long,Client> find = new Finder<Long, Client>(
     Long.class, Client.class
   ); 
 }
