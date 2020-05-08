 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 import com.avaje.ebean.*;
 
 @Entity 
 public class Taxonomy extends Model {
 
     @Id
     public Long id;
     
     public String species;
    
     public String classtype;
 
     public String common;
 
     @OneToMany
     public List<Strtaxonomy> strtaxonomy;
 
     @OneToMany
     public List<Taxprotein> taxprotein;
     
     @OneToMany
     public List<Proteinsource> taxproteinsource;
     
     @OneToMany
     public List<Taxtissue> taxtissue;
     
     public static Model.Finder<Long,Taxonomy> find = new Model.Finder<Long,Taxonomy>(Long.class, Taxonomy.class);
 
     
     public static List<Taxonomy> searchTaxonomy(int taxon) { 
         return
             find.where().disjunction()
                 .ilike("species", "%" + taxon + "%")
                 .ilike("common", "%" + taxon + "%")
                 .endJunction()
                 .join("strtaxonomy")
 		.findList();
     }
 
    public static List<Taxonomy> findSpeciesTemp(String species) {
 	return
 	   find.where().ilike("species", species).findList();
    }
 
 
    public static List<String> findSpecies() {
    final List<String> result = new ArrayList<String>();
    List<Taxonomy> taxonomy = Taxonomy.find.all();
    HashSet glycobasetax = GlycobaseSource.taxSummary(); 
    for (Taxonomy tax : taxonomy){
 	final String species = tax.species;
          result.add(species);
    }
    String tax = "";
    for(Object glycotax : glycobasetax) {
 	if(glycotax != null) {tax = glycotax.toString();
 		result.add(tax);
 	}
    }
 
 
    Collections.sort(result);
    return result;
    }
 
    public static HashSet findSpeciesUnique() {
 	HashSet taxUnique = new HashSet();
 	List<Taxonomy> taxonomy = Taxonomy.find.all();
 
 	for(Taxonomy tax : taxonomy) {
 		taxUnique.add(tax);
 	}
    return taxUnique;
    }
 
    public static /*List<Taxonomy>*/  List<SqlRow> findTaxonomy(String term)  {
 
 	String sql = "SELECT t.species FROM public.taxonomy as t WHERE t.species ilike '" + term + "%'";
 
     RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("t.species", "t.species").columnMapping("t.species", "t.species").create();
 
     SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
     List<SqlRow> listSql = sqlQuery.findList();
 
 	System.out.println("query test");
 	return listSql;
 		//find.where().ilike("species", term +  "%").setMaxRows(1).findList();
 }
 
 public static String join(Collection<?> s, String delimiter) {
      StringBuilder builder = new StringBuilder();
      Iterator iter = s.iterator();
      while (iter.hasNext()) {
          builder.append(iter.next());
          if (!iter.hasNext()) {
            break;
          }
          builder.append(delimiter);
      }
      return builder.toString();
  }
 
 
 }
 
