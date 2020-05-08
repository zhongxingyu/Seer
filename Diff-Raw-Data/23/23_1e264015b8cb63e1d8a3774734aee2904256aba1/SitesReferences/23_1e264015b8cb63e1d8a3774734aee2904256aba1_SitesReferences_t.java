 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 import play.mvc.Content;
 
 import com.avaje.ebean.*;
 
 
 /**
  * Sites entity managed by Ebean
  */
 @SuppressWarnings("serial")
 @Entity 
 @Table(name="sites_references", schema="glycosuite")
 public class SitesReferences extends Model {
 
     @Id
     public Long id;
     public String protein_name;
     public String swiss_prot;
     public String glyco_aa;
     public String glyco_aa_site;
 
     
     public static Finder<Long,SitesReferences> find = new Finder<Long,SitesReferences>(Long.class, SitesReferences.class);
 
     public static List<SitesReferences> findSites(String protein) {
         return
           find.where().ilike("swiss_prot", "%" + protein + "%").findList();
     }
 }
