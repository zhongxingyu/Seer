 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 import play.mvc.Content;
 
 import com.avaje.ebean.*;
 
 /**
  * Structure entity managed by Ebean
  */
 @SuppressWarnings("serial")
 @Entity 
 public class Structure extends Model {
 
     @Id
     public Long id;
     
     public String compositionId;
     public String glycanst;
     public String aminolink;
     public String core;
     public String type;
     public String antigenic;
     public String lectin;
     public String endoglycosidase;
     public String link;	 
    
     @OneToMany
     public List<Streference> references;
 
     @OneToMany
     public List<Stproteins> stproteins; 
 
     @OneToMany
     public List<Strtaxonomy> strtaxonomy;
 
     @OneToMany
     public List<Stsource> stsource;
 
 
      public Structure(String glycanst, String aminolink, String core, String type, String antigenic, String lectin, String endoglycosidase, String link, String compositionId) {
         this.compositionId = compositionId;
         this.glycanst = glycanst;
 	this.aminolink = aminolink;
 	this.core = core;
 	this.type = type;
 	this.antigenic = antigenic;
 	this.lectin = lectin;
    	this.endoglycosidase = endoglycosidase;
 	this.link = link; 
 }
      
      public static List<Structure> findStructureRef(Long id) {
 	System.out.println("tmp message");
     	 return find.join("references").join("references.reference").join("stproteins").join("stproteins.proteins").join("strtaxonomy").join("stsource")
     	 .where()
     	 .eq("structure_id", id)
 	 .orderBy("references.reference.year desc")
     	 .findList();
      }
 
     
     /**
      * Generic query helper for entity Reference with id Long
      */
     public static Finder<Long,Structure> find = new Finder<Long,Structure>(Long.class, Structure.class);
 
     public static String buildComposition(String[] input) {
 	String build = "";
 	for(String composition : input) {
 	if (composition.length() < 1 ) {
 	build += "0" ;
 	}
 	build += composition;	
 	}
 
         return build;   
     }
 
     public static List<Structure> findComposition(String composition) {
	return find.where().ilike("composition_id", "%" + composition).findList();
     }
 
      public static Content render(Structure strDisplay) {
 		// TODO Auto-generated method stub
 		return null;
      }
     
     
 }
 
