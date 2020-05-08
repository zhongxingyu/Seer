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
 @Table(name="general_sites", schema="glycosuite")
 public class GeneralSites extends Model {
 
     @Id
     public Long id;
     public String protein_name;
     public String swiss_prot;
     public String amino_acid_position;
     public String glyco_aa;
     public String glyco_aa_site;
 
     @OneToMany
     public List<StructureToSiteGeneral> strSiteGeneral;
 
     @ManyToOne
     public Proteins proteins;
 
     
     public static Finder<Long,GeneralSites> find = new Finder<Long,GeneralSites>(Long.class, GeneralSites.class);
 
     public static List<GeneralSites> findProteinsGeneral(String protein) {
         return
           find.where().ilike("swiss_prot", "%" + protein + "%").findList();
    }
 
    public static List<GeneralSites> findStructuresGeneral(String protein, String site) {
         return
            find.where().ilike("swiss_prot", protein).ilike("glyco_aa_site", site).findList();
    }
    
    public static List<GeneralSites> findProteinsGeneralName(String protein) {
         return
            find.where().ilike("protein_name", protein).findList();
    }
 
 
 }
