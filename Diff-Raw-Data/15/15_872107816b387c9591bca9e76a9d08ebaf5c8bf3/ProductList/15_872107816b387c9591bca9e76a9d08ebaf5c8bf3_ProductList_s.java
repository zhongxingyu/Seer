 package models;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Query;
 import com.avaje.ebean.RawSql;
 import com.avaje.ebean.RawSqlBuilder;
 
 import play.db.ebean.Model;
 
 @Entity
 public class ProductList extends Model{
 	@Id
 	public int listid;
 	
 	public String listname;
 		
 
     public Customer customer;
 	public String email;
 	
 	public int pid;

	public int test;
 	
 	public static Finder<Integer,ProductList> find = new Finder( Integer.class, ProductList.class );
 	    
   	public static List<ProductList> all() {
 	  return find.all();
 	}
   	
   	public static List<ProductList> selectbypid(int pid) {
   		List<ProductList> tempList = find.where().eq("pid", pid).findList(); 
 	  
   		return tempList;
 	}
   	
   	public static List<ProductList> selectlistbymail(String emailStr) { 	  
   	  String sql = "select listid, listname, email, pid from product_list group by listname";  
   
   	  RawSql rawSql = RawSqlBuilder.parse(sql).create();  
   
   	  Query<ProductList> query = Ebean.find(ProductList.class);  
   	  query.setRawSql(rawSql).where().eq("email", emailStr); 
   
   	  List<ProductList> resultList = query.findList();  
   	  
   	  return resultList;
   	}
   	
   	public static Map<Integer,String> selectpidsbymail(String emailStr) {
   		Map<Integer,String> tempMap = new HashMap<Integer,String>();
   		
   		String sql = "select listid, listname, email, pid from product_list";  
 
   		RawSql rawSql = RawSqlBuilder.parse(sql).create();  
 
   		Query<ProductList> query = Ebean.find(ProductList.class);  
   		query.setRawSql(rawSql).where().eq("email", emailStr); 
 
   		List<ProductList> resultList = query.findList(); 
   		for(int i=0; i<resultList.size(); i++)
   			tempMap.put(resultList.get(i).pid,resultList.get(i).listname);
 	  
   		return tempMap;
 	}
  
 	public static void create(ProductList prodList) {
 		prodList.save();
 	}
 
 	public static void delete(Integer listid) {
 	  find.ref(listid).delete();
 	}
 	
 	public static void deleteByPid(Integer pid, String emailStr) {
 		List<ProductList> tempList = find.where().eq("pid", pid).conjunction().eq("email", emailStr).findList();
 		
 		for(int i=0; i<tempList.size(); i++){
 			find.ref(tempList.get(i).listid).delete();
 		}
 	}
 }
 
 
