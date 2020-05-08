 package com.snda.youni.taskweb.daos;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.jdbc.core.RowMapper;
 
 import com.snda.youni.taskweb.beans.UserObject;
 
 public class UserDAO extends AbstractDAOImpl {
 
 	static {
 		TABLENAME = "users";
 	};
 	private final static String SQL_INSERT = "insert into "+TABLENAME+" (login,password,name,email,createddate) value (?,?,?,?,?)";
 	private final static String SQL_UPDATE = "update "+TABLENAME+" set login=?,password=?,name=?,email=? where login=?";
 	private final static String SQL_QUERY_USER_BY_LOGIN = "SELECT * FROM "+TABLENAME+" WHERE login=?";
 	private final static String SQL_QUERY_USER_ALL = "SELECT * FROM "+TABLENAME+"  where deleted<>1 and _id<>0";
 	
 	public boolean save(UserObject user){
 		user.setPassword("");
 		if( this.findUserByLogin(user.getLogin())!=null ){
 			//update user;
 			getJdbcTemplate().update(SQL_UPDATE, user.getLogin(),user.getPassword(),user.getName(),user.getEmail(),user.getLogin());
 		}else{
			//user.setCreateddate( (new Date()).getTime() );
			getJdbcTemplate().update(SQL_INSERT, user.getLogin(),user.getPassword(),user.getName(),user.getEmail(),new Date());
 			//insert user;
 		}
 		return true;
 	}
 	
 	public UserObject findUserByLogin(String login){
		UserObject user = null;
		try{
			user= getJdbcTemplate().queryForObject(SQL_QUERY_USER_BY_LOGIN, new Object[]{login},new UserMapper());
		}catch(Exception e){
			user = null;
		}
 		return user;
		
 	}
 	
	
	
 	public List<UserObject> query(){
 		return getJdbcTemplate().query(SQL_QUERY_USER_ALL,new UserMapper());
 	}
 	
 	private static final class UserMapper implements RowMapper<UserObject>{
 		public UserObject mapRow(ResultSet rs,int rownum) throws SQLException{
 			UserObject user =new UserObject();
 			user.setId( rs.getInt("_id") );
 			user.setLogin( rs.getString("login") );
 			user.setName( rs.getString("name") );
 			user.setEmail( rs.getString("email") );
 			user.setCreateddate( rs.getDate("createddate").getTime() );
 			return user;
 		}
 	}
 	
 }
