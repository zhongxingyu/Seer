 package data;
 
 import java.util.*;
 import java.sql.*;
 import javax.sql.*;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class ProductFinder {
   
   private JdbcTemplate jdbcTemplate;
   private DataSource dataSource;
   
   public void setDataSource(DataSource dataSource) {
     this.dataSource = dataSource;
     this.jdbcTemplate = new JdbcTemplate(dataSource);
   }
   
  
   
   public ProductRowGateway create() {
     return ProductRowGateway.load(dataSource,null);
   }
   
  /* private final static String findStatement =
      "SELECT * "+
      "FROM profesor "+
      "WHERE id = ?";
      
   public ProfesorRowGateway find(String id) {
     List profs = jdbcTemplate.queryForList(findStatement,id);
     ProfesorRowGateway prof = ProfesorRowGateway.load(dataSource,(Map)profs.get(0));
     return prof;
   }
   
   private final static String findAllStatement =
      "SELECT * "+
      "FROM profesor ";
   */  
  public List<ProductRowGateway> findAll() {
     List result = new ArrayList();
    /*List profs = jdbcTemplate.queryForList(findAllStatement);
     for (int i=0; i<profs.size();i++)
      result.add(ProfesorRowGateway.load(dataSource,(Map)profs.get(i)));*/
     return result;
  }
 }
