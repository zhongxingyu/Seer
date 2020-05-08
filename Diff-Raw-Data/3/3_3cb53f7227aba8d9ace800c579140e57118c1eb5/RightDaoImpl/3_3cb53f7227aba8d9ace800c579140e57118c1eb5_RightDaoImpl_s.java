 package de.enwida.web.dao.implementation;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import de.enwida.transport.Aspect;
 import de.enwida.web.controller.AdminController;
 import de.enwida.web.dao.interfaces.AbstractBaseDao;
 import de.enwida.web.dao.interfaces.IRightDao;
 import de.enwida.web.model.Right;
 
 @Repository
 public class RightDaoImpl extends AbstractBaseDao<Right> implements IRightDao {
 	
 	@Autowired
 	private DataSource datasource;
 	
     private static org.apache.log4j.Logger logger = Logger.getLogger(AdminController.class);
 	
 	@Override
 	public String getDbTableName() {
 	    return "users.rights";
 	}
 	
 	/**
 	 * Enables or disables the Aspects in the database.So that right won't see that aspect 
 	 */
     @Override
     public boolean enableDisableAspect(long rightID, boolean enabled) throws Exception{
         String sql = "UPDATE users.rights SET enabled=? WHERE right_id=?";
         try{
         this.jdbcTemplate.update(sql,enabled,rightID);
         }catch(Exception e){
             logger.error(e.getMessage());
             return false;
         }
         return true;
     }
     
     @Override
     public Right mapRow(ResultSet rs, int rowNum) throws SQLException {
         Right right = new Right();
         right.setRightID(rs.getLong("right_id"));
         right.setRoleID(rs.getLong("role_id"));
         right.setEnabled(rs.getBoolean("enabled"));
         right.setTso(rs.getInt("tso"));
         right.setProduct(rs.getInt("product"));
         right.setAspect(Aspect.values()[rs.getInt("aspect_id")].name());
         right.setResolution(rs.getString("resolution"));
         right.setTimeFrom(rs.getDate("time1"));
         right.setTimeTo(rs.getDate("time2"));
         return right;
     }
     
 
     public boolean isAuthorizedByExample(Right dataAuthorization) throws Exception{
         String SELECT_QUERY = "SELECT COUNT(*) FROM users.rights WHERE role_id = ? AND tso = ? AND product = ? AND aspect_id = ? AND resolution = ? AND time1 <= ? AND time2 >= ? AND enabled = ?;";
         
         Object[] param = new Object[8];
         param[0] = dataAuthorization.getRoleID();
         param[1] = dataAuthorization.getTso();
         param[2] = dataAuthorization.getProduct();
         param[3] = Aspect.valueOf(dataAuthorization.getAspect()).ordinal();
         param[4] = dataAuthorization.getResolution();
         java.sql.Timestamp t1 = new java.sql.Timestamp(dataAuthorization.getTimeFrom().getTime());
         t1.setNanos(0);
         param[5] = t1;
         java.sql.Timestamp t2 = new java.sql.Timestamp(dataAuthorization.getTimeTo().getTime());
         t2.setNanos(0);
         param[6] = t2;
         param[7] = dataAuthorization.isEnabled();
         
         int count = jdbcTemplate.queryForInt(SELECT_QUERY, param);
         return count > 0 ? true : false;
     }
     
     public void addRight(Right right) {
         final String SELECT_QUERY = "INSERT INTO users.rights (role_id,tso,product,aspect_id,resolution,time1,time2,enabled) VALUES (?,?,?,?,?,?,?,?);";
         
         Object[] param = new Object[8];
         param[0] = right.getRoleID();
         param[1] = right.getTso();
         param[2] = right.getProduct();
         param[3] = Aspect.valueOf(right.getAspect()).ordinal();
         param[4] = right.getResolution();
         param[5] = new Date(right.getTimeFrom().getTime());
         param[6] = new Date(right.getTimeTo().getTime());
         param[7] = right.isEnabled();
         
         jdbcTemplate.update(SELECT_QUERY, param);
     }
 
     public List<Right> getListByExample(Right dataAuthorization)throws Exception {
         String SELECT_QUERY = "SELECT * FROM users.rights WHERE role_id = ? AND tso = ? AND product = ? AND aspect_id = ? AND enabled = ?;";
         
         Object[] param = new Object[5];
         param[0] = dataAuthorization.getRoleID();
         param[1] = dataAuthorization.getTso();
         param[2] = dataAuthorization.getProduct();
         param[3] = Aspect.valueOf(dataAuthorization.getAspect()).ordinal();
         param[4] = dataAuthorization.isEnabled();
         
         List<Right> dAuthorizartion = jdbcTemplate.query(SELECT_QUERY, param, this);
         return dAuthorizartion; 
     }
 
     public void enableLine(Right dataAuthorization) throws Exception{
         String UPDATET_QUERY = "UPDATE users.rights SET enabled = ? WHERE role_id = ? AND tso = ? AND product = ? AND aspect_id = ?;";
         
         Object[] param = new Object[4];
         param[0] = dataAuthorization.isEnabled();
         param[1] = dataAuthorization.getRoleID();
         param[2] = dataAuthorization.getTso();
         param[3] = dataAuthorization.getProduct();
         param[4] = Aspect.valueOf(dataAuthorization.getAspect()).ordinal();
         
         jdbcTemplate.update(UPDATET_QUERY, param);
     }
 
     @Override
     public List<Right> getAllAspects(long roleID) throws Exception{
        String sql = "SELECT * FROM users.rights WHERE role_id=? limit 10";
         return jdbcTemplate.query(sql, new Object[]{roleID}, this);
     }
     
 }
