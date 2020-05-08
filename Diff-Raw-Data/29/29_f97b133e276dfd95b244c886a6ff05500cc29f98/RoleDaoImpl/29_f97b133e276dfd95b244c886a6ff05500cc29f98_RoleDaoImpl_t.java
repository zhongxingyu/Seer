 package de.enwida.web.dao.implementation;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
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
 	public List<Role> getUserRoles(long userID)throws Exception {
 	    
	    String sql = "select DISTINCT ON (role_id) roles.role_id,roles.name,roles.description FROM users.roles " +
 	    		"INNER JOIN users.group_role ON group_role.role_id=roles.role_id " +
 	    		"INNER JOIN users.user_group ON user_group.group_id=group_role.group_id " +
 	    		" where users.user_group.user_id=?";
         return this.jdbcTemplate.query(sql,new Object[]{userID},this);
     }
 	
	@Deprecated
 	@Override
     public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
 	    Role role = new Role();
 	    role.setRoleID(rs.getLong("role_id"));
 	    role.setRoleName(rs.getString("role_name"));
 	    role.setDescription(rs.getString("description"));
         return role;
     }
 
     @Override
     public void addRole(Role role) throws Exception{
         create(role);
     }
     
     @Override
     public void removeRole(Role role) throws Exception{
         delete(role);
     }
 }
