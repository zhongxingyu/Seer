 package ch.epfl.bbcf.gdv.access.database.dao;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.authorization.strategies.role.Roles;
 
 import ch.epfl.bbcf.gdv.access.database.Connect;
 import ch.epfl.bbcf.gdv.access.database.pojo.Track;
 import ch.epfl.bbcf.gdv.access.database.pojo.Users;
 import ch.epfl.bbcf.gdv.config.Application;
 import ch.epfl.bbcf.gdv.utility.RandomKey;
 
 /**
  * DAO for the User in the database
  * @author Yohan Jarosz
  *
  */
 public class UsersDAO extends DAO<Users>{
 
 	public UsersDAO(Connect connection) {
 		super(connection);
 	}
 
 	/**
 	 * Create a new user in the database
 	 * @param user the user
 	 * @return created
 	 */
 	public int createUser(Users user) {
 		int userId = -1;
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "insert into users values (" +
 				"default, ? , ? , ? , ? , ? , ? ,?, ? , ?);";
 
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setString(1, user.getMail());
 				statement.setString(2, user.getName());
 				statement.setString(3, user.getFirstname());
 				statement.setString(4, user.getTitle());
 				statement.setString(5, user.getPhone());
 				statement.setString(6, user.getOffice());
 				statement.setString(7, user.getType());
 				java.util.Date today = new java.util.Date();
 				java.sql.Date timestamp = new java.sql.Date(today.getTime());
 				statement.setDate(8,timestamp);
 				if(user.getType().equalsIgnoreCase("tequila")){
 					RandomKey r = new RandomKey();
 					statement.setString(9, r.getRandom());
 				} else {
 					statement.setString(9, "nokey");
 				}
 				this.executeUpdate(statement);
 				query = "select currval('users_id_seq') ; ";
 				statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				ResultSet resultSet = this.executeQuery(statement);
 				if(resultSet.first()){
 					userId = resultSet.getInt(1);
 				}
 				this.endQuery(true);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return userId;
 	}
 	/**
 	 * find if the username exist or not
 	 * @param username the username
 	 * @return exist
 	 */
 	//	public boolean usernameExists(String username) {
 	//		boolean result = false;
 	//		if(this.databaseConnected()){
 	//			this.startQuery();
 	//			try {
 	//				String query = "Select 1 from users as t1 " +
 	//				"where t1.username = ? ;";
 	//				PreparedStatement statement = this.prepareStatement(query,
 	//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 	//				statement.setString(1, username);
 	//				ResultSet resultSet = this.executeQuery(statement);
 	//				if (resultSet.first()) {
 	//					result = true;
 	//					this.endQuery(true);
 	//				}
 	//			} catch (SQLException e) {
 	//				logger.error(e);
 	//				this.endQuery(false);
 	//			}
 	//		}
 	//		return result;
 	//	}
 
 	public Users getUserByEmail(String email) {
 		Users user = null;
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
 				"where t1.mail = ? ;";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setString(1, email);
 				ResultSet resultSet = this.executeQuery(statement);
 				if(resultSet.first()){
 					user = this.getUser(resultSet);
 				}
 				this.endQuery(true);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return user;
 	}
 	/**
 	 * Find an user by his id
 	 * @param id the id
 	 * @return the user
 	 */
 	public Users getUserById(int id) {
 		Users user = null;
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
 				"where t1.id = ? ;";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, id);
 				ResultSet resultSet = this.executeQuery(statement);
 				if(resultSet.first()){
 					user = this.getUser(resultSet);
 				}
 				this.endQuery(true);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return user;
 	}
 
 
 	private List<Users> getUsers(ResultSet resultSet) {
 		List<Users> users = new ArrayList<Users>();
 		try {
 			while(resultSet.next()){
 				users.add(getUser(resultSet));
 			}
 			return users;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * the constructor of POJO
 	 * @param result the result of the query
 	 * @return the POJO
 	 */
 	private Users getUser(ResultSet result) {
 		Users user = new Users();
 		try {
 			user.setId(result.getInt("id"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setName(result.getString("name"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setMail(result.getString("mail"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setFirstname(result.getString("firstname"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setTitle(result.getString("title"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setPhone(result.getString("phone"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setOffice(result.getString("office"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setType(result.getString("type"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		try {
 			user.setKey(result.getString("key"));
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		return user;
 	}
 
 
 	public boolean emailExist(String email) {
 		boolean result = false;
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select 1 from users as t1 " +
 				"where t1.mail = ? ;";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setString(1, email);
 				ResultSet resultSet = this.executeQuery(statement);
 				if (resultSet.first()) {
 					result = true;
 					this.endQuery(true);
 				}
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return result;
 	}
 
 	public boolean isAdmin(int user) {
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select 1 from admin " +
 				"where id = ? ;";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, user);
 				ResultSet resultSet = this.executeQuery(statement);
 				if (resultSet.first()) {
 					this.endQuery(true);
 					return true;
 				}
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return false;
 	}
 
 
 
 	/**
 	 * get the users from a trackId 
 	 * @param trackId
 	 * @return
 	 */
 	public List<Users> getUserFromTrackId(int trackId) {
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
 				"inner join usertotrack as t2 on t1.id = t2.user_id " +
 				"where t2.track_id = ? ";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, trackId);
 				ResultSet resultSet = this.executeQuery(statement);
 				this.endQuery(true);
 				return this.getUsers(resultSet);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return null;
 	}
 
 	public Users getUserByProjectId(int projectId) {
 		Users user = null;
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
 				"inner join usertoproject as t2 on t1.id = t2.user_id " +
 				"where t2.project_id = ? ;";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, projectId);
 				ResultSet resultSet = this.executeQuery(statement);
 				if(resultSet.first()){
 					user = this.getUser(resultSet);
 				}
 				this.endQuery(true);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return user;
 	}
 
 	/**
 	 * retrieve the list of users belonging to a group
 	 * @param groupId
 	 * @return
 	 */
 	public List<Users> getUserListFromGroupId(int groupId) {
 		List<Users> users = new ArrayList<Users>();
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
 				"inner join usertogroup as t2 on t1.mail = t2.user_mail " +
 				"where t2.group_id = ? ";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, groupId);
 				ResultSet resultSet = this.executeQuery(statement);
 				this.endQuery(true);
 				users = this.getUsers(resultSet);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return users;
 	}
 	/**
 	 * retrieve the list of users mails belonging to a group
 	 * @param groupId
 	 * @return
 	 */
 	public List<String> getUserMailFromGroupId(int groupId) {
 		List<String> users = new ArrayList<String>();
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.user_mail from usertogroup as t1 " +
 				"where t1.group_id = ? ";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, groupId);
 				ResultSet resultSet = this.executeQuery(statement);
 				this.endQuery(true);
 				while(resultSet.next()){
 					users.add(resultSet.getString(1));
 				}
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return users;
 	}
 	/**
 	 * get the owner for a group
 	 * @param groupId
 	 * @return
 	 */
 	public Users getUserOwnerFromGroupId(int groupId) {
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
				"inner join group as t2 on t1.id = t2.owner " +
 				"where t2.id = ? ";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setInt(1, groupId);
 				ResultSet resultSet = this.executeQuery(statement);
 				Users u = null;
 				if(resultSet.first()){
 					u = this.getUser(resultSet);
 				}
				this.endQuery(true);
				return u;
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return null;
 	}
 
 	public Users getUserByEmailAndPass(String mail, String pass) {
 		Users user = null;
 		if(this.databaseConnected()){
 			this.startQuery();
 			try {
 				String query = "Select t1.* from users as t1 " +
 				"where t1.mail = ? and t1.key = ? ;";
 				PreparedStatement statement = this.prepareStatement(query,
 						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				statement.setString(1, mail);
 				statement.setString(2, pass);
 				ResultSet resultSet = this.executeQuery(statement);
 				if(resultSet.first()){
 					user = this.getUser(resultSet);
 				}
 				this.endQuery(true);
 			} catch (SQLException e) {
 				logger.error(e);
 				this.endQuery(false);
 			}
 		}
 		return user;
 	}
 
 
 }
 
 
 
 
 
 
