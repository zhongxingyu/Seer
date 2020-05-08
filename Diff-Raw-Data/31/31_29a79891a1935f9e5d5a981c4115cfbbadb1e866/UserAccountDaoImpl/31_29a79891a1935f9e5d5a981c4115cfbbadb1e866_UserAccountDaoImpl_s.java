 package com.robonobo.wang.server.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import com.robonobo.wang.server.UserAccount;
 
 @Repository("userAccountDao")
 public class UserAccountDaoImpl implements UserAccountDao {
 	private static final String CREATE_UA_SQL = "INSERT INTO user_account (friendly_name, email, password, balance) values (?, ?, ?, 0)";
 	private static final String GET_UA_SQL = "SELECT * FROM user_account WHERE email = ?";
 	private static final String LOCK_UA_SQL = GET_UA_SQL + " FOR UPDATE";
 	private static final String PUT_UA_SQL = "UPDATE user_account SET friendly_name = ?, password = ?, balance = ? WHERE email = ?";
 	private static final String COUNT_SQL = "SELECT count(*) FROM user_account";
 
 	private JdbcTemplate db;
 	private Log log = LogFactory.getLog(getClass());
 	private UserAccountMapper uaMapper = new UserAccountMapper();
 
 	@Autowired
 	public void setDataSource(DataSource dataSource) {
 		db = new JdbcTemplate(dataSource);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.robonobo.wang.server.dao.UserAccountDao#createUserAccount(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void createUserAccount(String friendlyName, String email, String password) throws DAOException {
		UserAccount ua = getUserAccount(email);
		if(ua != null)
 			throw new DAOException("Account for "+email+" already exists");
 		try {
 			db.update(CREATE_UA_SQL, friendlyName, email, password);
 		} catch (DataAccessException e) {
 			throw new DAOException(e);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.robonobo.wang.server.dao.UserAccountDao#getUserAccount(java.lang.String)
 	 */
 	@Override
 	public UserAccount getUserAccount(String email) throws DAOException {
 		try {
 			return db.queryForObject(GET_UA_SQL, uaMapper, email);
 		} catch (DataAccessException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.robonobo.wang.server.dao.UserAccountDao#getAndLockUserAccount(java.lang.String)
 	 */
 	@Override
 	public UserAccount getAndLockUserAccount(String email) throws DAOException {
 		try {
 			return db.queryForObject(LOCK_UA_SQL, uaMapper, email);
 		} catch (DataAccessException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.robonobo.wang.server.dao.UserAccountDao#putUserAccount(com.robonobo.wang.server.UserAccount)
 	 */
 	@Override
 	public void putUserAccount(UserAccount ua) throws DAOException {
 		try {
 			db.update(PUT_UA_SQL, ua.getName(), ua.getPassword(), ua.getBalance(), ua.getEmail());
 		} catch (DataAccessException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.robonobo.wang.server.dao.UserAccountDao#countUsers()
 	 */
 	@Override
 	public Long countUsers() throws DAOException {
 		try {
 			return db.queryForLong(COUNT_SQL);
 		} catch (DataAccessException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	private class UserAccountMapper implements RowMapper<UserAccount> {
 		@Override
 		public UserAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
 			UserAccount ua = new UserAccount();
 			ua.setEmail(rs.getString("email"));
 			ua.setName(rs.getString("friendly_name"));
 			ua.setPassword(rs.getString("password"));
 			ua.setBalance(rs.getDouble("balance"));
 			return ua;
 		}
 	}
 }
