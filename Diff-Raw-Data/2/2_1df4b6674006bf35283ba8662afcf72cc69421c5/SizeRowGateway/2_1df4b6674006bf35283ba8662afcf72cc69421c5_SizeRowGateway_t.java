 package data;
 
 import java.util.*;
 import java.sql.*;
 import javax.sql.*;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class SizeRowGateway {		
 	private int idSize;
 	private String size;
 	private JdbcTemplate jdbcTemplate;
 	private DataSource dataSource;
 	
 	SizeRowGateway() {};
 	
 	SizeRowGateway(int idSize, String size) {
 		this.idSize = idSize;
 		this.size = size;
 	}
 	
 	public void setidSize(int idSize) {this.idSize = idSize;}
 	public int getidSize() {return idSize;}
 
 	public void setSize(String size) {this.size = size;}
 	public String getSize() {return size;}  
 	
 	public void setDataSource(DataSource dataSource) {
 		this.dataSource = dataSource;
 	}
 	
 	private void createJdbcTemplate() {
 		jdbcTemplate = new JdbcTemplate(dataSource);
 	}
   
 	private static final String insertStatement =
 		"INSERT INTO Sizes "+
 		"VALUES (?,?)";
 		
 	public int insert() {
 		Random generator = new Random();
 		int idSize = Math.abs(generator.nextInt());
 		if (jdbcTemplate == null) createJdbcTemplate();
 		jdbcTemplate.update(insertStatement, idSize, size);
 		return idSize;
 	}
 	
 	private static final String updateStatement =
 		"UPDATE Sizes "+
		"SET size = ? WHERE id_size = ?";
 	
 	public void update() {
 		if (jdbcTemplate == null) createJdbcTemplate();
 		jdbcTemplate.update(updateStatement, size, idSize);
 	}
   
 	private static final String deleteStatement =
 		"DELETE FROM Sizes "+
 		"WHERE id_size = ?";
 		
 	private static final String deleteSizesStatement =
 		"DELETE FROM product_sizes "+
 		"WHERE id_size = ?";
 		
 	public void delete() {
 		if (jdbcTemplate == null) createJdbcTemplate();
 		jdbcTemplate.update(deleteStatement, idSize);
 		jdbcTemplate.update(deleteSizesStatement, idSize);
 	}
 	
 	public static SizeRowGateway load(DataSource ds, Map map) {
 		SizeRowGateway siz = null;
 		if (map == null) {
 			siz = new SizeRowGateway();
 		} else {
 			int idSize = ((Integer)map.get("id_size")).intValue();
 			String size = map.get("size").toString();
 			siz = new SizeRowGateway(idSize, size);
 		}
 		siz.setDataSource(ds);
 		return siz;
 	}
 }
