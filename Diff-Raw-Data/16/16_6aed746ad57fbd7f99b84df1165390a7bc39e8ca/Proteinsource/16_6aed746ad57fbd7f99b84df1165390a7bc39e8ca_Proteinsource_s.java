 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.RawSql;
 import com.avaje.ebean.RawSqlBuilder;
 import com.avaje.ebean.SqlQuery;
 import com.avaje.ebean.SqlRow;
 
 import play.db.ebean.Model;
 
 @Entity 
@Table(schema="public", name="protein_source")
 public class Proteinsource extends Model {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Id
     public Long id;
     
     public String name;
     public String swissprot;
     public String system;
     public String common;
     public String classname;
     public String species;
     public String div1;
     public String div2;
     public String div3;
     public String div4;
     
     @ManyToOne
     public Taxonomy taxonomy;
     
     @ManyToOne
     public Tissue tissue;
     
     
     public static Model.Finder<Long,Proteinsource> find = new Model.Finder<Long,Proteinsource>(Long.class, Proteinsource.class);
 
     /*public static List<Proteinsource> findProteinsource(String protein) {
         return
            find.where().ilike("swissprot", protein).findList();
    } */
     
     public static List<SqlRow> findProteinSource(String term)  {
     	System.out.println("check this query ");
     	String sql = "SELECT p.taxonomy_id, p.tissue_id, p.species, p.system, p.div1, p.div2, p.div3, p.div4 FROM public.Proteinsource as p WHERE p.swissprot ilike '%" + term + "%' group by p.taxonomy_id, p.tissue_id, p.species, p.system, p.div1, p.div2, p.div3, p.div4";
 
         RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("p.tissue_id", "p.tissue_id").columnMapping("p.taxonomy_id", "p.taxonomy_id").columnMapping("p.system", "p.system").columnMapping("p.species", "p.species").columnMapping("p.div1", "p.div1").columnMapping("p.div2", "p.div2").columnMapping("p.div3", "p.div3").columnMapping("p.div4", "p.div4").create();
 
         SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
         List<SqlRow> listSql = sqlQuery.findList();
 
     	return listSql;
     		//find.where().ilike("species", term +  "%").setMaxRows(1).findList();
     }
 
 	
 }
