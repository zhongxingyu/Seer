 package data;
 
 import java.util.*;
 import java.sql.*;
 import javax.sql.*;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class BrandRowGateway {		
 	private int idBrand;
 	private String name;
 	private JdbcTemplate jdbcTemplate;
 	private DataSource dataSource;
 	
 	BrandRowGateway() {};
 	
 	BrandRowGateway(int idBrand, String name) {
 		this.idBrand = idBrand;
 		this.name = name;
 	}
 	
 	public void setidBrand(int idBrand) {this.idBrand = idBrand;}
 	public int getidBrand() {return idBrand;}
 
 	public void setName(String name) {this.name = name;}
 	public String getName() {return name;}  
 	
 	public void setDataSource(DataSource dataSource) {
 		this.dataSource = dataSource;
 	}
 	
 	private void createJdbcTemplate() {
 		jdbcTemplate = new JdbcTemplate(dataSource);
 	}
   
 	private static final String insertStatement =
 		"INSERT INTO Brand "+
 		"VALUES (?,?)";
 		
 	public int insert() {
 		Random generator = new Random();
 		int idBrand = generator.nextInt(500);
 		if (jdbcTemplate == null) createJdbcTemplate();
 		jdbcTemplate.update(insertStatement, idBrand, name);
 		return idBrand;
 	}
 	
 	private static final String updateStatement =
 		"UPDATE Brand "+
		"SET brand_name = ? WHERE id_brand = ?";
 	
 	public void update() {
 		if (jdbcTemplate == null) createJdbcTemplate();
 		jdbcTemplate.update(updateStatement, name, idBrand);
 	}
   
 	private static final String deleteStatement =
 		"DELETE FROM Brand "+
 		"WHERE id_brand = ?";
 		
 	private final static String findBrandProductStatement =
 		"SELECT * "+
 		"FROM Product "+
 		"WHERE id_brand = ?";
 		
 	public void delete() {
 		if (jdbcTemplate == null) createJdbcTemplate();
 		
 		List brands = jdbcTemplate.queryForList(findBrandProductStatement, idBrand);		
 		
 		if (brands.size() < 1)	
 			jdbcTemplate.update(deleteStatement, idBrand);
 	}
 	
 	public static BrandRowGateway load(DataSource ds, Map map) {
 		BrandRowGateway brand = null;
 		if (map == null) {
 			brand = new BrandRowGateway();
 		} else {
 			int idBrand = ((Integer)map.get("id_brand")).intValue();
 			String name = (String)map.get("brand_name");
 			brand = new BrandRowGateway(idBrand, name);
 		}
 		brand.setDataSource(ds);
 		return brand;
 	}
 }
