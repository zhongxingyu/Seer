 package models.cmscore;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 @Entity
 public class Leaf extends Model {
 
     @Required
     public String uuid;
     
     @Required
     public String title;
 
     @Required
     public Integer version;
 
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date publish;
 
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date unPublish;
 
     public Leaf(Integer version, String title) {
         this.uuid = UUID.randomUUID().toString();
         this.title = title;
         this.version = version;
     }
     
     public Leaf(String uuid, Integer version, String title) {
         this.uuid = uuid;
         this.title = title;
         this.version = version;
     }
     
     public static List<Leaf> findAllCurrentVersions() {
         return Leaf.find(
                 "select l from Leaf l " +
                 "where l.version = (" +
                         "select max(l2.version) from Leaf l2 " +
                         "where l2.uuid = l.uuid " +
                 ")"
         ).fetch();
     }
     
     public static Leaf findWithUuidSpecificVersion(String uuid, int version) {
         return Leaf.find(
                 "select distinct l from Leaf l " +
                 "where l.uuid = :uuid and l.version = :version"
         ).bind("uuid", uuid).bind("version", Integer.valueOf(version)).first();
     }
 
    public static Leaf findWithUuidLatestVersion(String uuid) {
         return Leaf.find(
                 "select distinct l from Leaf l " +
                "where l.uuid = :uuid " +
                 "order by version desc"
        ).bind("uuid", uuid).first();
     }
 
     public static List<Leaf> findWithUuidAllVersions(String uuid) {
         return Leaf.find(
                 "select distinct l from Leaf l " +
                 "where l.uuid = :uuid"
         ).bind("uuid", uuid).fetch();
     }
 
     public String toString() {
         return "(" + uuid + "," + version + ") - " +title;
     }
 }
