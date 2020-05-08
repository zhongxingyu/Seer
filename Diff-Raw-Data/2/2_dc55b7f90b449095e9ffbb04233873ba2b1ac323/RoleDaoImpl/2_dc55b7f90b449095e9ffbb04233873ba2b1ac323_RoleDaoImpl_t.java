 package de.enwida.web.dao.implementation;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.stereotype.Repository;
 
 import de.enwida.web.dao.interfaces.AbstractBaseDao;
 import de.enwida.web.dao.interfaces.IRoleDao;
 import de.enwida.web.model.Role;
 
 @Repository
 public class RoleDaoImpl extends AbstractBaseDao<Role> implements IRoleDao {
 	
 	@Autowired
 	private DataSource datasource;
 	
 	@Override
 	public String getDbTableName() {
 	    return "users.roles";
 	}
 	
 	
 	@Override
 	public List<Role> getUserRoles(long userID) {
 	    
 	    String sql = "select DISTINCT ON (role_id) roles.role_id,roles.role_name,roles.description FROM users.roles " +
 	    		"INNER JOIN users.group_role ON group_role.role_id=roles.role_id " +
 	    		"INNER JOIN users.user_group ON user_group.group_id=group_role.group_id " +
 	    		" where users.user_group.user_id=?";
         return this.jdbcTemplate.query(sql,new Object[]{userID},this);
     }
 	
 	@Override
     public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
 	    Role role = new Role();
 	    role.setRoleID(rs.getLong("role_id"));
 	    role.setRoleName(rs.getString("role_name"));
 	    role.setDescription(rs.getString("description"));
         return role;
     }
 
 	@Override
 	public Role getRoleByID(Long id) {
 	    String sql = "SELECT * FROM users.roles where roles.role_id=?;";
	    return this.jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<>(Role.class));
 	}
 	
     @Override
     public void addRole(Role role) {
                 
         String sql = "INSERT INTO users.roles(name,description) VALUES (?,?);";
         this.save(sql, role);
     }
 
     @Override
     public List<Role> getAllRoles() {
         String sql = "SELECT * FROM users.roles";
         List<Role> roles  =new  ArrayList<Role>();
         
         List<Map<String,Object>> rows =this.jdbcTemplate.queryForList(sql);
         for (Map row : rows) {
             Role role = new Role();
             role.setRoleID(Long.parseLong(row.get("role_id").toString()));
             role.setRoleName((String) row.get("role_name"));
             role.setDescription((String) row.get("description"));
             roles.add(role);
         }
         return roles;
     }
 }
