 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 import com.avaje.ebean.*;
 
 @Entity 
@Table(schema="public", name="proteinstaxonomy")
 public class Proteinstaxonomy extends Model {
 
     @Id
     public Long id;
     
     public String swissProt;
 
     public String species;
 
     public String common;
     
     public String protein;
   
     public String strain;
 
 
     @ManyToOne
     public Proteins proteins;
 
     public static Model.Finder<Long,Proteinstaxonomy> find = new Model.Finder<Long,Proteinstaxonomy>(Long.class, Proteinstaxonomy.class);
     
     public static Proteinstaxonomy findProteinTax(String protein) {
 		try{
 		return
 			find.where().disjunction()
 				.ilike("swiss_prot", protein)
 				.ilike("protein", protein)
 				.findUnique();
 		} catch(Exception e)  {
 		System.out.println("Error with Protein Tax query" + e) ;
 		}
 		return null;
     
     }
 
     public static List<Proteinstaxonomy> findProteinsTax(String protein) {
 		return
 			find.where().disjunction()
                                 .ilike("swiss_prot", protein)
                                 .ilike("protein", protein)
                                 .findList();
     }
     
 }
 
