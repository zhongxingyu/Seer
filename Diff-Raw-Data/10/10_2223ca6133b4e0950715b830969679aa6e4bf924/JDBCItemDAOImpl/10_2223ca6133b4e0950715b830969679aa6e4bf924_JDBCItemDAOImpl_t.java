 package spring.petstore.core.dao.jdbc;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 import spring.petstore.core.dao.ItemDAO;
 import spring.petstore.core.model.Item;
 
 public class JDBCItemDAOImpl implements ItemDAO {
 	private SimpleJdbcTemplate jdbcTemplate;
 	private final ParameterizedRowMapper<Item> mapper;
 
 	public JDBCItemDAOImpl() {
 		mapper = new ParameterizedRowMapper<Item>() {
 	        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
 	        	Item item = new Item();
 	        	item.setId(rs.getLong("ID"));
 	        	item.setName(rs.getString("NAME"));
	        	item.setDescription(rs.getString("DESC"));
 	    		return item;
 	    	}
 	    };
 	}
 
 	public List<Item> getItems(long categoryId) {
 		
 		String sql = "SELECT * FROM ITEM WHERE CATEGORY_ID_FK = ?";
 		List<Item> items = jdbcTemplate.query(sql, mapper, categoryId);
 		System.out.println(items.size());
 		return items;
 	}
 
 	public void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
 		this.jdbcTemplate = jdbcTemplate;
 	}
 

 	public Item getItem(long itemId) {
 		String sql = "SELECT * FROM ITEM WHERE ID = ?";
 		Item item = jdbcTemplate.queryForObject(sql, mapper, itemId);
 		return item;
 	}
 
 }
